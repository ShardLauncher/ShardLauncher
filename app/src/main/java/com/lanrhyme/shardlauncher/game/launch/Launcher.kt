package com.lanrhyme.shardlauncher.game.launch

import android.os.Build
import com.lanrhyme.shardlauncher.bridge.ZLBridge
import com.lanrhyme.shardlauncher.bridge.ZLNativeInvoker
import com.lanrhyme.shardlauncher.info.InfoDistributor
import com.lanrhyme.shardlauncher.path.LibPath
import com.lanrhyme.shardlauncher.path.PathManager
import com.oracle.dalvik.VMLauncher
import java.io.File
import java.util.Locale
import java.util.TimeZone

abstract class Launcher {
    /**
     * Launch the game/JVM application
     */
    abstract suspend fun launch(): Int

    /**
     * Change working directory
     */
    abstract fun chdir()

    /**
     * Get log file name
     */
    abstract fun getLogName(): String

    /**
     * Exit handling
     */
    abstract fun exit(exitCode: Int)

    /**
     * Set up the JVM environment and launch
     */
    protected fun launchJvm(
        allArgs: Array<String>,
        ldLibraryPath: String,
        envMap: Map<String, String> = emptyMap()
    ): Int {
        ZLNativeInvoker.staticLauncher = this
        
        // Ensure DNS configuration
        ensureDNSConfig()
        
        // Initialize environment variables
        initEnv(ldLibraryPath, envMap)

        // Load Java runtime and other necessary libraries
        dlopenJavaRuntime(ldLibraryPath)
        dlopenEngine()

        // Change directory
        chdir()

        // Launch JVM
        val exitCode = VMLauncher.launchJVM(allArgs)
        exit(exitCode)
        return exitCode
    }

    private fun ensureDNSConfig() {
        val resolvFile = File(PathManager.DIR_FILES_PRIVATE, "resolv.conf")
        if (!resolvFile.exists()) {
            runCatching {
                resolvFile.writeText("nameserver 8.8.8.8\nnameserver 8.8.4.4\n")
            }
        }
    }

    /**
     * Get basic Java system properties
     */
    protected fun getJavaArgs(): MutableMap<String, String> {
        val args = mutableMapOf<String, String>()
        val locale = Locale.getDefault()
        
        args["java.home"] = PathManager.DIR_MULTIRT.absolutePath // Placeholder, will be updated by specific runtime
        args["java.io.tmpdir"] = PathManager.DIR_CACHE.absolutePath
        args["user.home"] = PathManager.DIR_FILES_PRIVATE.absolutePath
        args["user.language"] = locale.language
        args["user.country"] = locale.country
        args["user.timezone"] = TimeZone.getDefault().id
        args["os.name"] = "Linux"
        args["os.version"] = Build.VERSION.RELEASE
        
        // Launcher identification
        args["pojav.path.minecraft"] = PathManager.DIR_GAME.absolutePath
        args["pojav.path.private.account"] = PathManager.DIR_FILES_PRIVATE.absolutePath
        
        // Log4j RCE Fixes
        args["log4j2.formatMsgNoLookups"] = "true"
        
        // Extra DNS config for java
        args["ext.net.resolvPath"] = File(PathManager.DIR_FILES_PRIVATE, "resolv.conf").absolutePath
        
        return args
    }

    /**
     * Initialize environment variables
     */
    protected open fun initEnv(ldLibraryPath: String, extraEnv: Map<String, String>) {
        val env = mutableMapOf<String, String>()
        env["POJAV_NATIVEDIR"] = PathManager.DIR_NATIVE_LIB
        env["JAVA_HOME"] = PathManager.DIR_MULTIRT.absolutePath // Should be specific to selected JRE
        env["HOME"] = PathManager.DIR_FILES_PRIVATE.absolutePath
        env["TMPDIR"] = PathManager.DIR_CACHE.absolutePath
        env["LD_LIBRARY_PATH"] = ldLibraryPath
        env["PATH"] = System.getenv("PATH") ?: "/sbin:/vendor/bin:/system/sbin:/system/bin:/system/xbin"
        
        // AWT Stub size
        env["AWTSTUB_WIDTH"] = "1280"
        env["AWTSTUB_HEIGHT"] = "720"
        
        env.putAll(extraEnv)
        
        // Note: Actual environment variable setting is usually done via JNI or native dlopen environment
        ZLBridge.setLdLibraryPath(ldLibraryPath)
    }

    /**
     * Construct runtime library path
     */
    protected fun getRuntimeLibraryPath(javaPath: String): String {
        val abi = Build.SUPPORTED_ABIS[0]
        val base = "$javaPath/lib"
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
    protected fun dlopenJavaRuntime(ldLibraryPath: String) {
        val libs = listOf(
            "libjli.so", "libjvm.so", "libverify.so", "libjava.so", 
            "libnet.so", "libnio.so", "libawt.so", "libawt_headless.so"
        )
        libs.forEach { lib ->
            val path = findLibInPath(lib, ldLibraryPath)
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
     * Get Cacio specific JVM arguments
     */
    protected fun getCacioJavaArgs(javaVersion: Int): List<String> {
        val args = mutableListOf<String>()
        val cacioJarDir = if (javaVersion >= 17) LibPath.CACIO_17 else LibPath.CACIO_8
        
        args.add("-Djava.awt.headless=false")
        args.add("-Dawt.toolkit=net.java.openjdk.cacio.ctk.CToolkit")
        args.add("-Djava.awt.graphicsenv=net.java.openjdk.cacio.ctk.CGraphicsEnvironment")
        
        if (javaVersion >= 9) {
            args.add("--add-exports=java.desktop/sun.awt=ALL-UNNAMED")
            args.add("--add-exports=java.desktop/sun.awt.image=ALL-UNNAMED")
            args.add("--add-exports=java.desktop/sun.java2d=ALL-UNNAMED")
            args.add("--add-exports=java.desktop/java.awt.peer=ALL-UNNAMED")
            
            if (javaVersion >= 17) {
                args.add("-javaagent:${LibPath.CACIO_17_AGENT.absolutePath}")
            }
        }
        
        args.add("-Xbootclasspath/a:${File(cacioJarDir, "cacio-ttc.jar").absolutePath}")
        return args
    }

    /**
     * Finalize user-provided arguments (memory, GC, etc.)
     */
    protected fun progressFinalUserArgs(userArgs: List<String>): List<String> {
        val result = userArgs.toMutableList()
        
        // Remove potentially conflicting args
        result.removeAll { it.startsWith("-Xmx") || it.startsWith("-Xms") }
        
        // Add patches if missing
        val patcherPath = LibPath.MIO_LIB_PATCHER.absolutePath
        if (result.none { it.contains("MioLibPatcher.jar") }) {
            result.add("-javaagent:$patcherPath")
        }
        
        return result
    }

    /**
     * Helper to parse multiline Java arguments
     */
    protected fun parseJavaArguments(args: String): List<String> {
        return args.split(Regex("\\s+")).filter { it.isNotBlank() }
    }
}