/*
 * Zalith Launcher 2
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
import android.util.ArrayMap
import androidx.annotation.CallSuper
import androidx.compose.ui.unit.IntSize
import com.lanrhyme.shardlauncher.bridge.LoggerBridge
import com.lanrhyme.shardlauncher.bridge.ZLBridge
import com.lanrhyme.shardlauncher.bridge.ZLNativeInvoker
import com.lanrhyme.shardlauncher.game.multirt.Runtime
import com.lanrhyme.shardlauncher.game.multirt.RuntimesManager
import com.lanrhyme.shardlauncher.game.path.GamePathManager
import com.lanrhyme.shardlauncher.game.path.getGameHome
import com.lanrhyme.shardlauncher.game.plugin.ffmpeg.FFmpegPluginManager
import com.lanrhyme.shardlauncher.game.plugin.renderer.RendererPluginManager
import com.lanrhyme.shardlauncher.info.InfoDistributor
import com.lanrhyme.shardlauncher.path.LibPath
import com.lanrhyme.shardlauncher.path.PathManager
import com.lanrhyme.shardlauncher.setting.AllSettings
import com.lanrhyme.shardlauncher.utils.device.Architecture
import com.lanrhyme.shardlauncher.utils.device.Architecture.ARCH_X86
import com.lanrhyme.shardlauncher.utils.device.Architecture.is64BitsDevice
import com.lanrhyme.shardlauncher.utils.getDisplayFriendlyRes
import com.lanrhyme.shardlauncher.utils.logging.Logger.lError
import com.lanrhyme.shardlauncher.utils.logging.Logger.lInfo
import com.lanrhyme.shardlauncher.utils.logging.Logger.lWarning
import com.oracle.dalvik.VMLauncher
import org.apache.commons.io.FileUtils
import org.lwjgl.glfw.CallbackBridge
import java.io.File
import java.io.IOException
import java.util.Locale
import java.util.TimeZone

abstract class Launcher(
    val onExit: (code: Int, isSignal: Boolean) -> Unit
) {
    lateinit var runtime: Runtime
        protected set

    private val runtimeHome: String by lazy {
        RuntimesManager.getRuntimeHome(runtime.name).absolutePath
    }

    private fun getJavaHome() = if (runtime.isJDK8) "$runtimeHome/jre" else runtimeHome

    abstract suspend fun launch(): Int
    abstract fun chdir(): String
    abstract fun getLogName(): String
    abstract fun exit()

    protected suspend fun launchJvm(
        context: Context,
        jvmArgs: List<String>,
        userHome: String? = null,
        userArgs: String,
        getWindowSize: () -> IntSize
    ): Int {
        ZLNativeInvoker.staticLauncher = this

        ZLBridge.setLdLibraryPath(getRuntimeLibraryPath())

        LoggerBridge.appendTitle("Env Map")
        setEnv()

        LoggerBridge.appendTitle("DLOPEN Java Runtime")
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

    //�?suspend 函数，等�?JVM 的退出代�?
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

        LoggerBridge.appendTitle("JVM Args")
        val iterator = args.iterator()
        while (iterator.hasNext()) {
            val arg = iterator.next()
            if (arg.startsWith("--accessToken") && iterator.hasNext()) {
                iterator.next()
                LoggerBridge.append("JVMArgs: $arg")
                LoggerBridge.append("JVMArgs: ********************")
                continue
            }
            LoggerBridge.append("JVMArgs: $arg")
        }

        ZLBridge.setupExitMethod(context.applicationContext)
        ZLBridge.initializeGameExitHook()
        ZLBridge.chdir(chdir())

        val exitCode = VMLauncher.launchJVM(args.toTypedArray())
        LoggerBridge.append("Java Exit code: $exitCode")
        return exitCode
    }

    /**
     * 添加 JVM 参数
     */
    protected open fun MutableMap<String, String>.putJavaArgs() {}

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
            put("user.home", userHome ?: GamePathManager.getUserHome())
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

            put("log4j2.formatMsgNoLookups", "true")
            // Fix RCE vulnerability of log4j2
            put("java.rmi.server.useCodebaseOnly", "true")
            put("com.sun.jndi.rmi.object.trustURLCodebase", "false")
            put("com.sun.jndi.cosnaming.object.trustURLCodebase", "false")

            put("net.minecraft.clientmodname", InfoDistributor.LAUNCHER_NAME)

            // fml
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
                lInfo("Arg skipped: $arg")
            }
            !overridden
        }

        userArguments += additionalArguments
        return userArguments
    }

    /**
     * 确保 DNS 配置文件存在
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
                lWarning("Failed to create resolv.conf", it)
                FileUtils.deleteQuietly(resolvFile)
            }
        }
        return resolvFile
    }

    /**
     * @param args 需要进行处理的参数
     * @param ramAllocation 指定内存空间大小
     */
    protected open fun progressFinalUserArgs(
        args: MutableList<String>,
        ramAllocation: Int = AllSettings.ramAllocation.getValue()
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
        // Don't let the user specify a custom Freetype library (as the user is unlikely to specify a version compiled for Android)
        args.purgeArg("-Dorg.lwjgl.freetype.libname")
        // Overridden by us to specify the exact number of cores that the android system has
        args.purgeArg("-XX:ActiveProcessorCount")

        args.add("-javaagent:${LibPath.MIO_LIB_PATCHER.absolutePath}")

        //Add automatically generated args
        val ramAllocationString = ramAllocation.toString()
        args.add("-Xms${ramAllocationString}M")
        args.add("-Xmx${ramAllocationString}M")

        // Force LWJGL to use the Freetype library intended for it, instead of using the one
        // that we ship with Java (since it may be older than what's needed)
        args.add("-Dorg.lwjgl.freetype.libname=${PathManager.DIR_NATIVE_LIB}/libfreetype.so")

        // Some phones are not using the right number of cores, fix that
        args.add("-XX:ActiveProcessorCount=${java.lang.Runtime.getRuntime().availableProcessors()}")
    }

    protected fun MutableList<String>.purgeArg(argStart: String) {
        removeIf { arg: String -> arg.startsWith(argStart) }
    }

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

    private fun getJvmLibDir(): String {
        val jvmLibDir: String
        val path = (if (RuntimesManager.isJDK8(runtimeHome)) "/jre" else "") + getJavaLibDir()
        val jvmFile = File("$runtimeHome$path/server/libjvm.so")
        jvmLibDir = if (jvmFile.exists()) "/server" else "/client"
        return jvmLibDir
    }

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

    protected fun findInLdLibPath(libName: String): String {
        val path = getLibraryPath()
        return path.split(":").find { libPath ->
            val file = File(libPath, libName)
            file.exists() && file.isFile
        }?.let {
            File(it, libName).absolutePath
        } ?: libName
    }

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

    private fun setEnv() {
        val envMap = initEnv()
        envMap.forEach { (key, value) ->
            LoggerBridge.append("Added env: $key = $value")
            runCatching {
                Os.setenv(key, value, true)
            }.onFailure {
                lError("Unable to set environment variable.", it)
            }
        }
    }

    @CallSuper
    protected open fun initEnv(): MutableMap<String, String> {
        val envMap: MutableMap<String, String> = ArrayMap()
        setJavaEnv(envMap = { envMap })
        return envMap
    }

    private fun setJavaEnv(envMap: () -> MutableMap<String, String>) {
        val path = listOfNotNull("$runtimeHome/bin", Os.getenv("PATH"))

        envMap().let { map ->
            map["POJAV_NATIVEDIR"] = PathManager.DIR_NATIVE_LIB
            map["JAVA_HOME"] = getJavaHome()
            map["HOME"] = PathManager.DIR_FILES_EXTERNAL.absolutePath
            map["TMPDIR"] = PathManager.DIR_CACHE.absolutePath
            map["LD_LIBRARY_PATH"] = getLibraryPath()
            map["PATH"] = path.joinToString(":")
            map["AWTSTUB_WIDTH"] = (CallbackBridge.windowWidth.takeIf { it > 0 } ?: CallbackBridge.physicalWidth).toString()
            map["AWTSTUB_HEIGHT"] = (CallbackBridge.windowHeight.takeIf { it > 0 } ?: CallbackBridge.physicalHeight).toString()
            map["MOD_ANDROID_RUNTIME"] = PathManager.DIR_RUNTIME_MOD?.absolutePath ?: ""

            if (AllSettings.dumpShaders.getValue()) map["LIBGL_VGPU_DUMP"] = "1"
            if (AllSettings.zinkPreferSystemDriver.getValue()) map["POJAV_ZINK_PREFER_SYSTEM_DRIVER"] = "1"
            if (AllSettings.vsyncInZink.getValue()) map["POJAV_VSYNC_IN_ZINK"] = "1"
            if (AllSettings.bigCoreAffinity.getValue()) map["POJAV_BIG_CORE_AFFINITY"] = "1"

            if (FFmpegPluginManager.isAvailable) map["POJAV_FFMPEG_PATH"] = FFmpegPluginManager.executablePath!!
        }
    }

    private fun dlopenJavaRuntime() {
        var javaLibDir = "$runtimeHome${getJavaLibDir()}"
        val jliLibDir = if (File("$javaLibDir/jli/libjli.so").exists()) "$javaLibDir/jli" else javaLibDir

        if (runtime.isJDK8) {
            javaLibDir = "$runtimeHome/jre${getJavaLibDir()}"
        }
        val jvmLibDir = "$javaLibDir${getJvmLibDir()}"
        ZLBridge.dlopen("$jliLibDir/libjli.so")
        ZLBridge.dlopen("$jvmLibDir/libjvm.so")
        ZLBridge.dlopen("$javaLibDir/libfreetype.so")
        ZLBridge.dlopen("$javaLibDir/libverify.so")
        ZLBridge.dlopen("$javaLibDir/libjava.so")
        ZLBridge.dlopen("$javaLibDir/libnet.so")
        ZLBridge.dlopen("$javaLibDir/libnio.so")
        ZLBridge.dlopen("$javaLibDir/libawt.so")
        ZLBridge.dlopen("$javaLibDir/libawt_headless.so")
        ZLBridge.dlopen("$javaLibDir/libfontmanager.so")
        locateLibs(File(runtimeHome)).forEach { file ->
            ZLBridge.dlopen(file.absolutePath)
        }
    }

    @CallSuper
    protected open fun dlopenEngine() {
        ZLBridge.dlopen("${PathManager.DIR_NATIVE_LIB}/libopenal.so")
    }
}

/**
 * [Modified from PojavLauncher](https://github.com/PojavLauncherTeam/PojavLauncher/blob/98947f2/app_pojavlauncher/src/main/java/net/kdt/pojavlaunch/utils/JREUtils.java#L411-L456)
 */
fun parseJavaArguments(args: String): List<String> {
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
                lWarning("Removed improper arguments: $parsedSubstring")
            }
        }
    }

    return parsedArguments
}

fun getCacioJavaArgs(
    screenWidth: Int,
    screenHeight: Int,
    isJava8: Boolean
): List<String> {
    val argsList: MutableList<String> = ArrayList()

    // Caciocavallo config AWT-enabled version
    argsList.add("-Djava.awt.headless=false")
    argsList.add("-Dcacio.managed.screensize=" + (screenWidth * 0.8).toInt() + "x" + (screenHeight * 0.8).toInt())
    argsList.add("-Dcacio.font.fontmanager=sun.awt.X11FontManager")
    argsList.add("-Dcacio.font.fontscaler=sun.font.FreetypeFontScaler")
    argsList.add("-Dswing.defaultlaf=javax.swing.plaf.metal.MetalLookAndFeel")
    if (isJava8) {
        argsList.add("-Dawt.toolkit=net.java.openjdk.cacio.ctc.CTCToolkit")
        argsList.add("-Djava.awt.graphicsenv=net.java.openjdk.cacio.ctc.CTCGraphicsEnvironment")
    } else {
        argsList.add("-Dawt.toolkit=com.github.caciocavallosilano.cacio.ctc.CTCToolkit")
        argsList.add("-Djava.awt.graphicsenv=com.github.caciocavallosilano.cacio.ctc.CTCGraphicsEnvironment")
        argsList.add("-Djava.system.class.loader=com.github.caciocavallosilano.cacio.ctc.CTCPreloadClassLoader")

        argsList.add("--add-exports=java.desktop/java.awt=ALL-UNNAMED")
        argsList.add("--add-exports=java.desktop/java.awt.peer=ALL-UNNAMED")
        argsList.add("--add-exports=java.desktop/sun.awt.image=ALL-UNNAMED")
        argsList.add("--add-exports=java.desktop/sun.java2d=ALL-UNNAMED")
        argsList.add("--add-exports=java.desktop/java.awt.dnd.peer=ALL-UNNAMED")
        argsList.add("--add-exports=java.desktop/sun.awt=ALL-UNNAMED")
        argsList.add("--add-exports=java.desktop/sun.awt.event=ALL-UNNAMED")
        argsList.add("--add-exports=java.desktop/sun.awt.datatransfer=ALL-UNNAMED")
        argsList.add("--add-exports=java.desktop/sun.font=ALL-UNNAMED")
        argsList.add("--add-exports=java.base/sun.security.action=ALL-UNNAMED")
        argsList.add("--add-opens=java.base/java.util=ALL-UNNAMED")
        argsList.add("--add-opens=java.desktop/java.awt=ALL-UNNAMED")
        argsList.add("--add-opens=java.desktop/sun.font=ALL-UNNAMED")
        argsList.add("--add-opens=java.desktop/sun.java2d=ALL-UNNAMED")
        argsList.add("--add-opens=java.base/java.lang.reflect=ALL-UNNAMED")

        // Opens the java.net package to Arc DNS injector on Java 9+
        argsList.add("--add-opens=java.base/java.net=ALL-UNNAMED")
    }

    val cacioClassPath = StringBuilder()
    cacioClassPath.append("-Xbootclasspath/").append(if (isJava8) "p" else "a")
    val cacioFiles = if (isJava8) LibPath.CACIO_8 else LibPath.CACIO_17
    cacioFiles.listFiles()?.onEach {
        if (it.name.endsWith(".jar")) cacioClassPath.append(":").append(it.absolutePath)
    }

    argsList.add(cacioClassPath.toString())

    return argsList
}
