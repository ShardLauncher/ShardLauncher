package com.lanrhyme.shardlauncher.game.launch

import android.content.Context
import android.os.Build
import androidx.compose.ui.unit.IntSize
import com.lanrhyme.shardlauncher.bridge.LoggerBridge
import com.lanrhyme.shardlauncher.bridge.ZLBridge
import com.lanrhyme.shardlauncher.game.multirt.RuntimesManager
import com.lanrhyme.shardlauncher.game.multirt.Runtime
import com.lanrhyme.shardlauncher.info.InfoDistributor
import com.lanrhyme.shardlauncher.path.LibPath
import com.lanrhyme.shardlauncher.path.PathManager
import com.lanrhyme.shardlauncher.settings.AllSettings
import com.lanrhyme.shardlauncher.utils.logging.Logger
import com.lanrhyme.shardlauncher.utils.platform.getDisplayFriendlyRes
import com.lanrhyme.shardlauncher.bridge.ZLNativeInvoker
import com.oracle.dalvik.VMLauncher
import java.io.File
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
     * Exit handling
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
        envMap["POJAV_NATIVEDIR"] = PathManager.DIR_NATIVE_LIB
        envMap["JAVA_HOME"] = getJavaHome()
        envMap["HOME"] = PathManager.DIR_FILES_PRIVATE.absolutePath
        envMap["TMPDIR"] = PathManager.DIR_CACHE.absolutePath
        envMap["PATH"] = System.getenv("PATH") ?: "/sbin:/vendor/bin:/system/sbin:/system/bin:/system/xbin"
        return envMap
    }

    /**
     * Set up the JVM environment and launch
     */
    protected suspend fun launchJvm(
        context: Context,
        jvmArgs: List<String>,
        userArgs: String,
        getWindowSize: () -> IntSize
    ): Int {
        ZLNativeInvoker.staticLauncher = this

        val runtimeLibraryPath = getRuntimeLibraryPath()
        ZLBridge.setLdLibraryPath(runtimeLibraryPath)

        LoggerBridge.appendTitle("Env Map")
        setEnv()

        LoggerBridge.appendTitle("DLOPEN Java Runtime")
        dlopenJavaRuntime()

        dlopenEngine()

        return launchJavaVM(
            context = context,
            jvmArgs = jvmArgs,
            userArgs = userArgs,
            getWindowSize = getWindowSize
        )
    }

    private suspend fun launchJavaVM(
        context: Context,
        jvmArgs: List<String>,
        userArgs: String,
        getWindowSize: () -> IntSize
    ): Int {
        val windowSize = getWindowSize()
        val args = getJavaArgs(userArgs, windowSize).toMutableList()
        progressFinalUserArgs(args)

        args.addAll(jvmArgs)
        args.add(0, "$runtimeHome/bin/java")

        LoggerBridge.appendTitle("JVM Args")
        args.forEach { arg ->
            LoggerBridge.append("ARG: $arg")
        }

        ZLBridge.chdir(chdir())

        val exitCode = VMLauncher.launchJVM(args.toTypedArray())
        LoggerBridge.append("Java Exit code: $exitCode")
        exit()
        onExit(exitCode, false)
        return exitCode
    }

    private fun setEnv() {
        val envMap = initEnv()
        envMap.forEach { (key, value) ->
            LoggerBridge.append("ENV: $key=$value")
            // Note: Actual environment variable setting would be done via JNI
        }
    }

    /**
     * Construct runtime library path
     */
    protected fun getRuntimeLibraryPath(): String {
        val runtimeHome = RuntimesManager.getRuntimeHome(runtime.name).absolutePath
        val abi = Build.SUPPORTED_ABIS[0]
        val base = "$runtimeHome/lib"
        return listOf(
            "$base/$abi/jli",
            "$base/$abi/server", 
            "$base/$abi",
            base,
            PathManager.DIR_NATIVE_LIB
        ).joinToString(":")
    }

    /**
     * Load Java runtime libraries via dlopen
     */
    protected fun dlopenJavaRuntime() {
        val libs = listOf(
            "libjli.so", "libjvm.so", "libverify.so", "libjava.so",
            "libnet.so", "libnio.so", "libawt.so", "libawt_headless.so"
        )
        libs.forEach { lib ->
            val path = findLibInPath(lib, getRuntimeLibraryPath())
            if (path != null) {
                ZLBridge.dlopen(path)
            }
        }
    }

    private fun findLibInPath(libName: String, path: String): String? {
        path.split(":").forEach { dir ->
            val file = File(dir, libName)
            if (file.exists()) return file.absolutePath
        }
        return null
    }

    /**
     * Load engine specific libraries
     */
    protected open fun dlopenEngine() {
        // Load OpenAL or other engine specific libs
        val openal = File(PathManager.DIR_NATIVE_LIB, "libopenal.so")
        if (openal.exists()) ZLBridge.dlopen(openal.absolutePath)
    }

    /**
     * Get basic Java system properties and arguments
     */
    private fun getJavaArgs(userArgs: String, windowSize: IntSize): List<String> {
        // Ensure DNS configuration
        ensureDNSConfig()

        val userArguments = parseJavaArguments(userArgs).toMutableList()

        val overridableArguments = mutableMapOf<String, String>().apply {
            put("java.home", getJavaHome())
            put("java.io.tmpdir", PathManager.DIR_CACHE.absolutePath)
            put("jna.boot.library.path", PathManager.DIR_NATIVE_LIB)
            put("user.home", PathManager.DIR_FILES_PRIVATE.absolutePath)
            put("user.language", System.getProperty("user.language") ?: Locale.getDefault().language)
            put("user.country", Locale.getDefault().country)
            put("user.timezone", TimeZone.getDefault().id)
            put("os.name", "Linux")
            put("os.version", "Android-${Build.VERSION.RELEASE}")
            put("pojav.path.minecraft", PathManager.DIR_GAME.absolutePath)
            put("pojav.path.private.account", PathManager.DIR_FILES_PRIVATE.absolutePath)
            put("org.lwjgl.vulkan.libname", "libvulkan.so")
            
            val scaleFactor = AllSettings.resolutionRatio.getValue() / 100f
            put("glfwstub.windowWidth", getDisplayFriendlyRes(windowSize.width, scaleFactor).toString())
            put("glfwstub.windowHeight", getDisplayFriendlyRes(windowSize.height, scaleFactor).toString())
            put("glfwstub.initEgl", "false")
            put("ext.net.resolvPath", File(PathManager.DIR_FILES_PRIVATE, "resolv.conf").absolutePath)

            // Security fixes
            put("log4j2.formatMsgNoLookups", "true")
            put("java.rmi.server.useCodebaseOnly", "true")
            put("com.sun.jndi.rmi.object.trustURLCodebase", "false")
            put("com.sun.jndi.cosnaming.object.trustURLCodebase", "false")

            put("net.minecraft.clientmodname", InfoDistributor.LAUNCHER_NAME)

            // FML
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

    private fun ensureDNSConfig() {
        val resolvFile = File(PathManager.DIR_FILES_PRIVATE, "resolv.conf")
        if (!resolvFile.exists()) {
            runCatching {
                val configText = if (Locale.getDefault().displayName != Locale.CHINA.displayName) {
                    "nameserver 1.1.1.1\nnameserver 1.0.0.1\n"
                } else {
                    "nameserver 8.8.8.8\nnameserver 8.8.4.4\n"
                }
                resolvFile.writeText(configText)
            }.onFailure {
                Logger.lWarning("Failed to create resolv.conf", it)
            }
        }
    }

    /**
     * Finalize user-provided arguments (memory, GC, etc.)
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

        args.add("-Xms${ramAllocation}M")
        args.add("-Xmx${ramAllocation}M")

        // Add patches if missing
        val patcherPath = LibPath.MIO_LIB_PATCHER.absolutePath
        if (args.none { it.contains("MioLibPatcher.jar") }) {
            args.add("-javaagent:$patcherPath")
        }
    }

    private fun MutableList<String>.purgeArg(argPrefix: String) {
        removeAll { it.startsWith(argPrefix) }
    }

    /**
     * Helper to parse multiline Java arguments
     */
    protected fun parseJavaArguments(args: String): List<String> {
        return args.split(Regex("\\s+")).filter { it.isNotBlank() }
    }
}