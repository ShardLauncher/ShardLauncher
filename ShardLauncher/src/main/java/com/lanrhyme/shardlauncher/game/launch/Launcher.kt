/*
 * Shard Launcher
 * Adapted from Zalith Launcher 2
 * Copyright (C) 2025 MovTery <movtery228@qq.com> and contributors
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.
 * See the GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <https://www.gnu.org/licenses/gpl-3.0.txt>.
 */

package com.lanrhyme.shardlauncher.game.launch

import android.content.Context
import android.os.Build
import android.os.LocaleList
import android.system.Os
import androidx.compose.ui.unit.IntSize
import com.lanrhyme.shardlauncher.bridge.SLBridge
import com.lanrhyme.shardlauncher.bridge.SLNativeInvoker
import com.lanrhyme.shardlauncher.game.multirt.RuntimesManager
import com.lanrhyme.shardlauncher.game.multirt.Runtime
import com.lanrhyme.shardlauncher.game.path.getGameHome
import com.lanrhyme.shardlauncher.game.plugin.ffmpeg.FFmpegPluginManager
import com.lanrhyme.shardlauncher.game.plugin.renderer.RendererPluginManager
import com.lanrhyme.shardlauncher.info.InfoDistributor
import com.lanrhyme.shardlauncher.path.LibPath
import com.lanrhyme.shardlauncher.path.PathManager
import com.lanrhyme.shardlauncher.settings.AllSettings
import com.lanrhyme.shardlauncher.settings.unit.getOrMin
import com.lanrhyme.shardlauncher.utils.device.Architecture
import com.lanrhyme.shardlauncher.utils.device.Architecture.ARCH_X86
import com.lanrhyme.shardlauncher.utils.device.Architecture.is64BitsDevice
import com.lanrhyme.shardlauncher.utils.logging.Logger
import com.lanrhyme.shardlauncher.utils.platform.getDisplayFriendlyRes
import com.oracle.dalvik.VMLauncher
import org.apache.commons.io.FileUtils
import java.io.File
import java.io.IOException
import java.util.Locale
import java.util.TimeZone

/**
 * Base launcher class for launching JVM-based applications
 */
abstract class Launcher(
    val onExit: (code: Int, isSignal: Boolean) -> Unit
) {
    companion object {
        private const val TAG = "Launcher"
    }

    lateinit var runtime: Runtime
        protected set

    private val runtimeHome: String by lazy {
        RuntimesManager.getRuntimeHome(runtime.name).absolutePath
    }

    private fun getJavaHome() = if (runtime.isJDK8) "$runtimeHome/jre" else runtimeHome

    /**
     * Launch the game/JVM application
     */
    abstract suspend fun launch(): Int

    /**
     * Change working directory
     */
    abstract fun chdir(): String

    /**
     * Get log file name
     */
    abstract fun getLogName(): String

    /**
     * Exit handling - cleanup resources
     */
    abstract fun exit()

    /**
     * Put Java system properties specific to this launcher
     */
    protected open fun MutableMap<String, String>.putJavaArgs() {
        // Override in subclasses for specific Java args
    }

    /**
     * Initialize environment variables
     */
    protected open fun initEnv(): MutableMap<String, String> {
        val envMap = mutableMapOf<String, String>()
        setJavaEnv { envMap }
        return envMap
    }

    /**
     * Set Java environment variables
     */
    private fun setJavaEnv(envMap: () -> MutableMap<String, String>) {
        val path = listOfNotNull("$runtimeHome/bin", Os.getenv("PATH"))

        envMap().let { map ->
            map["POJAV_NATIVEDIR"] = PathManager.DIR_NATIVE_LIB
            map["JAVA_HOME"] = getJavaHome()
            map["HOME"] = PathManager.DIR_FILES_EXTERNAL.absolutePath
            map["TMPDIR"] = PathManager.DIR_CACHE.absolutePath
            map["LD_LIBRARY_PATH"] = getLibraryPath()
            map["PATH"] = path.joinToString(":")
            map["MOD_ANDROID_RUNTIME"] = PathManager.DIR_RUNTIME_MOD?.absolutePath ?: ""

            // Apply settings
            if (AllSettings.dumpShaders.getValue()) map["LIBGL_VGPU_DUMP"] = "1"
            if (AllSettings.zinkPreferSystemDriver.getValue()) map["POJAV_ZINK_PREFER_SYSTEM_DRIVER"] = "1"
            if (AllSettings.vsyncInZink.getValue()) map["POJAV_VSYNC_IN_ZINK"] = "1"
            if (AllSettings.bigCoreAffinity.getValue()) map["POJAV_BIG_CORE_AFFINITY"] = "1"

            // FFmpeg path
            if (FFmpegPluginManager.isAvailable) map["POJAV_FFMPEG_PATH"] = FFmpegPluginManager.executablePath!!
        }
    }

    /**
     * Set up the JVM environment and launch
     */
    protected suspend fun launchJvm(
        context: Context,
        jvmArgs: List<String>,
        userHome: String? = null,
        userArgs: String,
        getWindowSize: () -> IntSize
    ): Int {
        // Set the static launcher reference for native callbacks
        SLNativeInvoker.staticLauncher = this

        val runtimeLibraryPath = getRuntimeLibraryPath()
        
        // Set LD_LIBRARY_PATH
        safeJniCall("setLdLibraryPath") {
            SLBridge.setLdLibraryPath(runtimeLibraryPath)
        }

        Logger.lInfo("==================== Env Map ====================")
        setEnv()

        Logger.lInfo("==================== DLOPEN Java Runtime ====================")
        dlopenJavaRuntime()

        dlopenEngine()

        return launchJavaVM(
            context = context,
            jvmArgs = jvmArgs,
            userHome = userHome,
            userArgs = userArgs,
            getWindowSize = getWindowSize
        )
    }

    /**
     * Actually launch the JVM
     */
    private suspend fun launchJavaVM(
        context: Context,
        jvmArgs: List<String>,
        userHome: String? = null,
        userArgs: String,
        getWindowSize: () -> IntSize
    ): Int {
        val windowSize = getWindowSize()
        val args = getJavaArgs(userHome, userArgs, windowSize).toMutableList()
        progressFinalUserArgs(args)

        args.addAll(jvmArgs)
        args.add(0, "$runtimeHome/bin/java")

        Logger.lInfo("==================== JVM Args ====================")
        val iterator = args.iterator()
        while (iterator.hasNext()) {
            val arg = iterator.next()
            // Hide access token in logs
            if (arg.startsWith("--accessToken") && iterator.hasNext()) {
                Logger.lInfo("ARG: $arg")
                Logger.lInfo("ARG: ********************")
                iterator.next()
                continue
            }
            Logger.lInfo("ARG: $arg")
        }

        // Setup exit hook
        SLBridge.setupExitMethod(context.applicationContext)
        SLBridge.initializeGameExitHook()

        // Change working directory
        safeJniCall("chdir") {
            SLBridge.chdir(chdir())
        }

        // Launch JVM
        val exitCode = VMLauncher.launchJVM(args.toTypedArray())
        Logger.lInfo("Java Exit code: $exitCode")
        
        return exitCode
    }

    /**
     * Set environment variables via Os.setenv
     */
    private fun setEnv() {
        val envMap = initEnv()
        envMap.forEach { (key, value) ->
            Logger.lInfo("ENV: $key=$value")
            runCatching {
                Os.setenv(key, value, true)
            }.onFailure {
                Logger.lError("Unable to set environment variable: $key", it)
            }
        }
    }

    /**
     * Get Java library directory for the current runtime
     */
    protected fun getJavaLibDir(): String {
        val architecture = runtime.arch?.let { arch ->
            if (Architecture.archAsInt(arch) == ARCH_X86) "i386/i486/i586"
            else arch
        } ?: throw IOException("Unsupported architecture!")

        var libDir = "/lib"
        architecture.split("/").forEach { arch ->
            val file = File(runtimeHome, "lib/$arch")
            if (file.exists() && file.isDirectory()) {
                libDir = "/lib/$arch"
            }
        }
        return libDir
    }

    /**
     * Get JVM library directory
     */
    private fun getJvmLibDir(): String {
        val jvmLibDir: String
        val path = (if (RuntimesManager.isJDK8(runtimeHome)) "/jre" else "") + getJavaLibDir()
        val jvmFile = File("$runtimeHome$path/server/libjvm.so")
        jvmLibDir = if (jvmFile.exists()) "/server" else "/client"
        return jvmLibDir
    }

    /**
     * Get runtime library path for LD_LIBRARY_PATH
     */
    protected fun getRuntimeLibraryPath(): String {
        val javaLibDir = getJavaLibDir()
        val jvmLibDir = getJvmLibDir()

        val libName = if (is64BitsDevice) "lib64" else "lib"
        val path = listOfNotNull(
            FFmpegPluginManager.takeIf { it.isAvailable }?.libraryPath,
            RendererPluginManager.selectedRendererPlugin?.path,
            "$runtimeHome$javaLibDir",
            "$runtimeHome$javaLibDir/jli",
            if (runtime.isJDK8) {
                "$runtimeHome/jre$javaLibDir$jvmLibDir:$runtimeHome/jre$javaLibDir"
            } else {
                "$runtimeHome$javaLibDir$jvmLibDir"
            },
            "/system/$libName",
            "/vendor/$libName",
            "/vendor/$libName/hw",
            LibPath.JNA.absolutePath,
            PathManager.DIR_RUNTIME_MOD?.absolutePath,
            PathManager.DIR_NATIVE_LIB
        )
        return path.joinToString(":")
    }

    /**
     * Get library path for loading native libraries
     */
    protected fun getLibraryPath(): String {
        val libDirName = if (is64BitsDevice) "lib64" else "lib"
        val path = listOfNotNull(
            "/system/$libDirName",
            "/vendor/$libDirName",
            "/vendor/$libDirName/hw",
            RendererPluginManager.selectedRendererPlugin?.path,
            PathManager.DIR_RUNTIME_MOD?.absolutePath,
            PathManager.DIR_NATIVE_LIB
        )
        return path.joinToString(":")
    }

    /**
     * Find a library in the LD_LIBRARY_PATH
     */
    protected fun findInLdLibPath(libName: String): String? {
        val path = getLibraryPath()
        return path.split(":").find { libPath ->
            val file = File(libPath, libName)
            file.exists() && file.isFile
        }?.let {
            File(it, libName).absolutePath
        } ?: libName
    }

    /**
     * Load Java runtime libraries via dlopen
     */
    protected fun dlopenJavaRuntime() {
        var javaLibDir = "$runtimeHome${getJavaLibDir()}"
        val jliLibDir = if (File("$javaLibDir/jli/libjli.so").exists()) "$javaLibDir/jli" else javaLibDir

        if (runtime.isJDK8) {
            javaLibDir = "$runtimeHome/jre${getJavaLibDir()}"
        }
        val jvmLibDir = "$javaLibDir${getJvmLibDir()}"

        // Load essential Java libraries
        safeJniCall("dlopen libjli.so") { SLBridge.dlopen("$jliLibDir/libjli.so") }
        safeJniCall("dlopen libjvm.so") { SLBridge.dlopen("$jvmLibDir/libjvm.so") }
        safeJniCall("dlopen libfreetype.so") { SLBridge.dlopen("$javaLibDir/libfreetype.so") }
        safeJniCall("dlopen libverify.so") { SLBridge.dlopen("$javaLibDir/libverify.so") }
        safeJniCall("dlopen libjava.so") { SLBridge.dlopen("$javaLibDir/libjava.so") }
        safeJniCall("dlopen libnet.so") { SLBridge.dlopen("$javaLibDir/libnet.so") }
        safeJniCall("dlopen libnio.so") { SLBridge.dlopen("$javaLibDir/libnio.so") }
        safeJniCall("dlopen libawt.so") { SLBridge.dlopen("$javaLibDir/libawt.so") }
        safeJniCall("dlopen libawt_headless.so") { SLBridge.dlopen("$javaLibDir/libawt_headless.so") }
        safeJniCall("dlopen libfontmanager.so") { SLBridge.dlopen("$javaLibDir/libfontmanager.so") }

        // Load any additional .so files in the runtime
        locateLibs(File(runtimeHome)).forEach { file ->
            safeJniCall("dlopen ${file.name}") { SLBridge.dlopen(file.absolutePath) }
        }
    }

    /**
     * Find all .so files in a directory recursively
     */
    private fun locateLibs(path: File): List<File> {
        val children = path.listFiles() ?: return emptyList()
        return children.flatMap { file ->
            when {
                file.isFile && file.name.endsWith(".so") -> listOf(file)
                file.isDirectory -> locateLibs(file)
                else -> emptyList()
            }
        }
    }

    /**
     * Load engine specific libraries (OpenAL, etc.)
     */
    protected open fun dlopenEngine() {
        safeJniCall("dlopen libopenal.so") {
            SLBridge.dlopen("${PathManager.DIR_NATIVE_LIB}/libopenal.so")
        }
    }

    /**
     * Get Java arguments list
     */
    private fun getJavaArgs(
        userHome: String? = null,
        userArgumentsString: String,
        windowSize: IntSize
    ): List<String> {
        val userArguments = parseJavaArguments(userArgumentsString).toMutableList()
        val resolvFile = ensureDNSConfig()

        val overridableArguments = mutableMapOf<String, String>().apply {
            put("java.home", getJavaHome())
            put("java.io.tmpdir", PathManager.DIR_CACHE.absolutePath)
            put("jna.boot.library.path", PathManager.DIR_NATIVE_LIB)
            put("user.home", userHome ?: PathManager.DIR_FILES_EXTERNAL.absolutePath)
            put("user.language", System.getProperty("user.language"))
            put("user.country", Locale.getDefault().country)
            put("user.timezone", TimeZone.getDefault().id)
            put("os.name", "Linux")
            put("os.version", "Android-${Build.VERSION.RELEASE}")
            put("pojav.path.minecraft", getGameHome())
            put("pojav.path.private.account", PathManager.DIR_DATA_BASES.absolutePath)
            put("org.lwjgl.vulkan.libname", "libvulkan.so")
            
            val scaleFactor = AllSettings.resolutionRatio.getValue() / 100f
            put("glfwstub.windowWidth", getDisplayFriendlyRes(windowSize.width, scaleFactor).toString())
            put("glfwstub.windowHeight", getDisplayFriendlyRes(windowSize.height, scaleFactor).toString())
            put("glfwstub.initEgl", "false")
            put("ext.net.resolvPath", resolvFile.absolutePath)

            // Log4j security fixes
            put("log4j2.formatMsgNoLookups", "true")
            put("java.rmi.server.useCodebaseOnly", "true")
            put("com.sun.jndi.rmi.object.trustURLCodebase", "false")
            put("com.sun.jndi.cosnaming.object.trustURLCodebase", "false")

            put("net.minecraft.clientmodname", InfoDistributor.LAUNCHER_NAME)

            // FML settings
            put("fml.earlyprogresswindow", "false")
            put("fml.ignoreInvalidMinecraftCertificates", "true")
            put("fml.ignorePatchDiscrepancies", "true")

            put("loader.disable_forked_guis", "true")
            put("jdk.lang.Process.launchMechanism", "FORK")

            put("sodium.checks.issue2561", "false")

            putJavaArgs()
        }.map { entry ->
            "-D${entry.key}=${entry.value}"
        }

        val additionalArguments = overridableArguments.filter { arg ->
            val stripped = arg.substringBefore('=')
            val overridden = userArguments.any { it.startsWith(stripped) }
            if (overridden) {
                Logger.lInfo("Arg skipped: $arg")
            }
            !overridden
        }

        userArguments += additionalArguments
        return userArguments
    }

    /**
     * Ensure DNS configuration file exists
     */
    private fun ensureDNSConfig(): File {
        val resolvFile = File(PathManager.DIR_GAME, "resolv.conf")
        if (!resolvFile.exists()) {
            val configText = if (LocaleList.getDefault().get(0).displayName != Locale.CHINA.displayName) {
                """
                    nameserver 1.1.1.1
                    nameserver 1.0.0.1
                """.trimIndent()
            } else {
                """
                    nameserver 8.8.8.8
                    nameserver 8.8.4.4
                """.trimIndent()
            }
            runCatching {
                resolvFile.writeText(configText)
            }.onFailure {
                Logger.lWarning("Failed to create resolv.conf", it)
                FileUtils.deleteQuietly(resolvFile)
            }
        }
        return resolvFile
    }

    /**
     * Finalize user-provided arguments
     */
    protected open fun progressFinalUserArgs(
        args: MutableList<String>,
        ramAllocation: Int = AllSettings.ramAllocation.getOrMin()
    ) {
        args.purgeArg("-Xms")
        args.purgeArg("-Xmx")
        args.purgeArg("-d32")
        args.purgeArg("-d64")
        args.purgeArg("-Xint")
        args.purgeArg("-XX:+UseTransparentHugePages")
        args.purgeArg("-XX:+UseLargePagesInMetaspace")
        args.purgeArg("-XX:+UseLargePages")
        args.purgeArg("-Dorg.lwjgl.opengl.libname")
        args.purgeArg("-Dorg.lwjgl.freetype.libname")
        args.purgeArg("-XX:ActiveProcessorCount")

        // Add MioLibPatcher agent
        args.add("-javaagent:${LibPath.MIO_LIB_PATCHER.absolutePath}")

        // Add memory settings
        val ramAllocationString = ramAllocation.toString()
        args.add("-Xms${ramAllocationString}M")
        args.add("-Xmx${ramAllocationString}M")

        // Force LWJGL to use our Freetype library
        args.add("-Dorg.lwjgl.freetype.libname=${PathManager.DIR_NATIVE_LIB}/libfreetype.so")

        // Set correct processor count
        args.add("-XX:ActiveProcessorCount=${java.lang.Runtime.getRuntime().availableProcessors()}")
    }

    private fun MutableList<String>.purgeArg(argPrefix: String) {
        removeAll { it.startsWith(argPrefix) }
    }

    /**
     * Parse Java arguments string into list
     */
    protected fun parseJavaArguments(args: String): List<String> {
        val parsedArguments = mutableListOf<String>()
        var cleanedArgs = args.trim().replace(" ", "")
        val separators = listOf("-XX:-", "-XX:+", "-XX:", "--", "-D", "-X", "-javaagent:", "-verbose")

        for (prefix in separators) {
            while (true) {
                val start = cleanedArgs.indexOf(prefix)
                if (start == -1) break

                val end = separators
                    .mapNotNull { sep ->
                        val i = cleanedArgs.indexOf(sep, start + prefix.length)
                        if (i != -1) i else null
                    }
                    .minOrNull() ?: cleanedArgs.length

                val parsedSubstring = cleanedArgs.substring(start, end)
                cleanedArgs = cleanedArgs.replace(parsedSubstring, "")

                if (parsedSubstring.indexOf('=') == parsedSubstring.lastIndexOf('=')) {
                    val last = parsedArguments.lastOrNull()
                    if (last != null && (last.endsWith(',') || parsedSubstring.contains(','))) {
                        parsedArguments[parsedArguments.lastIndex] = last + parsedSubstring
                    } else {
                        parsedArguments.add(parsedSubstring)
                    }
                } else {
                    Logger.lWarning("Removed improper arguments: $parsedSubstring")
                }
            }
        }

        return parsedArguments
    }

    /**
     * Safely call a JNI method with error handling
     */
    protected inline fun safeJniCall(name: String, block: () -> Unit) {
        try {
            block()
            Logger.lInfo("JNI call succeeded: $name")
        } catch (e: UnsatisfiedLinkError) {
            Logger.lWarning("JNI call failed ($name): ${e.message}")
        } catch (e: Exception) {
            Logger.lError("JNI call error ($name)", e)
        }
    }
}