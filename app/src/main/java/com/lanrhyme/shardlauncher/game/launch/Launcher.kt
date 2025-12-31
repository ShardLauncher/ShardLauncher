/*
 * Shard Launcher
 * Adapted from Zalith Launcher 2
 */

package com.lanrhyme.shardlauncher.game.launch

import android.content.Context
import androidx.compose.ui.unit.IntSize
import com.lanrhyme.shardlauncher.bridge.LoggerBridge
import com.lanrhyme.shardlauncher.bridge.ZLBridge
import com.lanrhyme.shardlauncher.game.multirt.Runtime
import com.lanrhyme.shardlauncher.game.multirt.RuntimesManager

abstract class Launcher(
    val onExit: (code: Int, isSignal: Boolean) -> Unit
) {
    lateinit var runtime: Runtime
        protected set

    private val runtimeHome: String by lazy {
        RuntimesManager.getRuntimeHome(runtime.name).absolutePath
    }

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
        args.forEach { arg ->
            if (arg.startsWith("--accessToken")) {
                LoggerBridge.append("JVMArgs: $arg")
                LoggerBridge.append("JVMArgs: ********************")
            } else {
                LoggerBridge.append("JVMArgs: $arg")
            }
        }

        ZLBridge.setupExitMethod(context.applicationContext)
        ZLBridge.initializeGameExitHook()
        ZLBridge.chdir(chdir())

        val exitCode = VMLauncher.launchJVM(args.toTypedArray())
        LoggerBridge.append("Java Exit code: $exitCode")
        return exitCode
    }

    protected open fun MutableMap<String, String>.putJavaArgs() {}

    protected open fun initEnv(): MutableMap<String, String> {
        return mutableMapOf()
    }

    private fun setEnv() {
        val envMap = initEnv()
        envMap.putJavaArgs()
    }

    protected open fun dlopenJavaRuntime() {
        // Load Java runtime libraries
    }

    protected open fun dlopenEngine() {
        LoggerBridge.appendTitle("DLOPEN Engine")
    }

    protected fun getRuntimeLibraryPath(): String {
        return if (runtime.isJDK8) {
            "$runtimeHome/jre/lib:$runtimeHome/jre/lib/amd64:$runtimeHome/jre/lib/aarch64"
        } else {
            "$runtimeHome/lib:$runtimeHome/lib/amd64:$runtimeHome/lib/aarch64"
        }
    }

    protected fun findInLdLibPath(libName: String): String {
        return libName
    }

    protected fun getCacioJavaArgs(width: Int, height: Int, isJava8: Boolean): List<String> {
        return listOf(
            "-Djava.awt.headless=false",
            "-Dcacio.managed.screensize=${width}x$height",
            "-Dcacio.font.fontmanager=sun.awt.X11FontManager",
            "-Dcacio.font.fontscaler=sun.font.FreetypeFontScaler",
            "-Dswing.defaultlaf=javax.swing.plaf.metal.MetalLookAndFeel",
            "-Dawt.toolkit=net.java.openjdk.cacio.ctc.CTCToolkit",
            "-Djava.awt.graphicsenv=net.java.openjdk.cacio.ctc.CTCGraphicsEnvironment"
        )
    }

    protected open fun progressFinalUserArgs(args: MutableList<String>, ramAllocation: Int = 2048) {
        args.add("-Xmx${ramAllocation}M")
        args.add("-Xms256M")
        args.add("-XX:+UseG1GC")
        args.add("-XX:+UnlockExperimentalVMOptions")
        args.add("-XX:G1NewSizePercent=20")
        args.add("-XX:G1ReservePercent=20")
        args.add("-XX:MaxGCPauseMillis=50")
        args.add("-XX:G1HeapRegionSize=32M")
    }

    private fun getJavaArgs(
        userHome: String? = null,
        userArgumentsString: String,
        windowSize: IntSize
    ): List<String> {
        val userArguments = parseJavaArguments(userArgumentsString)
        val args = mutableListOf<String>()
        
        userHome?.let {
            args.add("-Duser.home=$it")
        }
        
        args.addAll(getCacioJavaArgs(windowSize.width, windowSize.height, runtime.isJDK8))
        args.addAll(userArguments)
        
        return args
    }

    private fun parseJavaArguments(argumentsString: String): List<String> {
        if (argumentsString.isBlank()) return emptyList()
        
        return argumentsString.split(" ").filter { it.isNotBlank() }.map { it.trim() }
    }
}