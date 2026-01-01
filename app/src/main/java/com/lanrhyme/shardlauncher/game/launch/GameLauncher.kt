/*
 * Shard Launcher
 * Adapted from Zalith Launcher 2
 */

package com.lanrhyme.shardlauncher.game.launch

import android.app.Activity
import android.opengl.EGL14
import android.opengl.EGLConfig
import android.os.Build
import androidx.compose.ui.unit.IntSize
import com.lanrhyme.shardlauncher.BuildConfig
import com.lanrhyme.shardlauncher.bridge.LoggerBridge
import com.lanrhyme.shardlauncher.bridge.ZLBridge
import com.lanrhyme.shardlauncher.bridge.ZLNativeInvoker
import com.lanrhyme.shardlauncher.game.account.Account
import com.lanrhyme.shardlauncher.game.account.AccountType
import com.lanrhyme.shardlauncher.game.account.AccountsManager
import com.lanrhyme.shardlauncher.game.account.offline.OfflineYggdrasilServer
import com.lanrhyme.shardlauncher.game.multirt.Runtime
import com.lanrhyme.shardlauncher.game.multirt.RuntimesManager
import com.lanrhyme.shardlauncher.game.plugin.driver.DriverPluginManager
import com.lanrhyme.shardlauncher.game.plugin.renderer.RendererPluginManager
import com.lanrhyme.shardlauncher.game.renderer.Renderers
import com.lanrhyme.shardlauncher.game.version.installed.Version
import com.lanrhyme.shardlauncher.game.version.installed.getGameManifest
import com.lanrhyme.shardlauncher.game.version.remote.MinecraftVersionJson
import com.lanrhyme.shardlauncher.path.PathManager
import com.lanrhyme.shardlauncher.settings.AllSettings
import com.lanrhyme.shardlauncher.utils.device.Architecture
import com.lanrhyme.shardlauncher.game.account.isLocalAccount
import com.lanrhyme.shardlauncher.utils.logging.Logger
import kotlinx.coroutines.runBlocking
import java.io.File

class GameLauncher(
    private val activity: Activity,
    private val version: Version,
    private val getWindowSize: () -> IntSize,
    onExit: (code: Int, isSignal: Boolean) -> Unit
) : Launcher(onExit) {
    
    private lateinit var gameManifest: MinecraftVersionJson
    private var offlinePort: Int = 0

    override suspend fun launch(): Int {
        // Initialize renderer if needed
        if (!Renderers.isCurrentRendererValid()) {
            val rendererIdentifier = version.getRenderer()
            if (rendererIdentifier.isNotEmpty()) {
                Renderers.setCurrentRenderer(activity, rendererIdentifier)
            } else {
                // Auto-select first compatible renderer if none specified
                val compatibleRenderers = Renderers.getCompatibleRenderers(activity)
                if (compatibleRenderers.isNotEmpty()) {
                    Renderers.setCurrentRenderer(activity, compatibleRenderers[0].getUniqueIdentifier())
                    Logger.lInfo("Auto-selected renderer: ${compatibleRenderers[0].getRendererName()}")
                } else {
                    throw IllegalStateException("No compatible renderers available")
                }
            }
        }

        // Get game manifest
        gameManifest = getGameManifest(version)
        
        // Skip input stack queue setup for now to avoid UI thread issues
        // TODO: Set input stack queue usage after game is fully launched
        // org.lwjgl.glfw.CallbackBridge.nativeSetUseInputStackQueue(gameManifest.arguments != null)

        // Get current account
        val currentAccount = AccountsManager.currentAccountFlow.value!!
        val account = if (version.offlineAccountLogin) {
            currentAccount.copy(accountType = AccountType.LOCAL.toString())
        } else {
            currentAccount
        }

        val customArgs = version.getJvmArgs().takeIf { it.isNotBlank() } ?: AllSettings.jvmArgs.getValue()
        val javaRuntimeName = getRuntimeName()
        val selectedRuntime = RuntimesManager.getRuntime(javaRuntimeName)
        this.runtime = selectedRuntime

        // Initialize LoggerBridge (temporarily disabled due to JNI issues)
        // TODO: Fix LoggerBridge JNI method resolution
        try {
            // Skip native logging for now to avoid UnsatisfiedLinkError
            // val logFile = File(PathManager.DIR_NATIVE_LOGS, "${getLogName()}.log")
            // LoggerBridge.start(logFile.absolutePath)
            Logger.lInfo("Native logging skipped - using Java logging only")
        } catch (e: Exception) {
            Logger.lWarning("Failed to initialize native logging", e)
        }

        // Initialize MCOptions and set language (skip for now to avoid potential issues)
        // TODO: Re-enable after fixing stability issues
        try {
            MCOptions.setup(activity, version)
            MCOptions.loadLanguage(version.getVersionName())
            MCOptions.save()
            Logger.lInfo("MCOptions initialized successfully")
        } catch (e: Exception) {
            Logger.lWarning("Failed to initialize MCOptions, continuing without it", e)
        }

        // Start offline Yggdrasil if needed (skip for now to avoid potential issues)
        // TODO: Re-enable after fixing stability issues
        try {
            if (account.isLocalAccount() && account.hasSkinFile) {
                offlinePort = OfflineYggdrasilServer.start()
                OfflineYggdrasilServer.addCharacter(account.username, account.profileId)
                Logger.lInfo("Offline Yggdrasil server started on port $offlinePort")
            }
        } catch (e: Exception) {
            Logger.lWarning("Failed to start offline Yggdrasil server, continuing without it", e)
        }

        printLauncherInfo(
            javaArguments = customArgs.takeIf { it.isNotEmpty() } ?: "NONE",
            javaRuntime = javaRuntimeName,
            account = account
        )

        return launchGame(account, javaRuntimeName, customArgs)
    }

    private suspend fun launchGame(
        account: Account,
        javaRuntime: String,
        customArgs: String
    ): Int {
        val runtime = RuntimesManager.forceReload(javaRuntime)
        this.runtime = runtime

        val gameDirPath = version.getGameDir()
        disableSplash(gameDirPath)

        val runtimeLibraryPath = getRuntimeLibraryPath()

        val launchArgs = LaunchArgs(
            runtimeLibraryPath = runtimeLibraryPath,
            account = account,
            gameDirPath = gameDirPath,
            version = version,
            gameManifest = gameManifest,
            runtime = runtime,
            getCacioJavaArgs = { isJava8 ->
                val windowSize = getWindowSize()
                getCacioJavaArgs(windowSize.width, windowSize.height, isJava8)
            },
            offlineServerPort = offlinePort
        ).getAllArgs()

        return launchJvm(
            context = activity,
            jvmArgs = launchArgs,
            userArgs = customArgs,
            getWindowSize = getWindowSize
        )
    }

    override fun chdir(): String {
        return version.getGameDir().absolutePath
    }

    override fun getLogName(): String = "game_${version.getVersionName()}_${System.currentTimeMillis()}"

    override fun exit() {
        if (offlinePort != 0) {
            OfflineYggdrasilServer.stop()
        }
    }

    override fun MutableMap<String, String>.putJavaArgs() {
        val versionInfo = version.getVersionInfo()
        
        // Fix Forge 1.7.2
        val is172 = (versionInfo?.minecraftVersion ?: "0.0") == "1.7.2"
        if (is172 && (versionInfo?.loaderInfo?.loader?.name == "forge")) {
            Logger.lDebug("Is Forge 1.7.2, use the patched sorting method.")
            put("sort.patch", "true")
        }

        // JNA library path
        gameManifest.libraries?.find { library ->
            library.name.startsWith("net.java.dev.jna:jna:")
        }?.let { library ->
            val components = library.name.split(":")
            if (components.size >= 3) {
                val jnaVersion = components[2]
                val jnaDir = File(PathManager.DIR_COMPONENTS, "jna/$jnaVersion")
                if (jnaDir.exists()) {
                    val dirPath = jnaDir.absolutePath
                    put("java.library.path", "$dirPath:${PathManager.DIR_NATIVE_LIB}")
                    put("jna.boot.library.path", dirPath)
                }
            }
        }
    }

    override fun initEnv(): MutableMap<String, String> {
        val envMap = super.initEnv()

        // Set driver
        DriverPluginManager.setDriverById(version.getDriver())
        envMap["DRIVER_PATH"] = DriverPluginManager.getDriver().path

        // Set loader environment
        version.getVersionInfo()?.loaderInfo?.getLoaderEnvKey()?.let { loaderKey ->
            envMap[loaderKey] = "1"
        }

        // Set renderer environment
        if (Renderers.isCurrentRendererValid()) {
            setRendererEnv(envMap)
        }

        envMap["SHARD_VERSION_CODE"] = BuildConfig.VERSION_CODE.toString()
        
        return envMap
    }

    override fun dlopenEngine() {
        super.dlopenEngine()
        
        try {
            LoggerBridge.appendTitle("DLOPEN Renderer")
        } catch (e: Exception) {
            Logger.lInfo("DLOPEN Renderer")
        }
        
        // Load renderer plugin libraries
        RendererPluginManager.selectedRendererPlugin?.let { renderer ->
            renderer.dlopen.forEach { lib -> 
                // ZLBridge.dlopen("${renderer.path}/$lib")  // Temporarily disabled due to JNI issues
                // Logger.lInfo("Skipping dlopen for renderer plugin due to JNI issues - lib: ${renderer.path}/$lib")
                
                // Try to restore renderer plugin library loading
                try {
                    val libPath = "${renderer.path}/$lib"
                    val success = ZLBridge.dlopen(libPath)
                    if (success) {
                        Logger.lInfo("Successfully loaded renderer plugin library: $libPath")
                    } else {
                        Logger.lWarning("Failed to load renderer plugin library: $libPath")
                    }
                } catch (e: UnsatisfiedLinkError) {
                    Logger.lWarning("JNI error loading renderer plugin library ${renderer.path}/$lib: ${e.message}")
                }
            }
        }

        // Load graphics library
        val rendererLib = loadGraphicsLibrary()
        if (rendererLib != null) {
            // if (!ZLBridge.dlopen(rendererLib) && !ZLBridge.dlopen(findInLdLibPath(rendererLib))) {
            //     Logger.lError("Failed to load renderer $rendererLib")
            // }
            // Logger.lInfo("Skipping dlopen for renderer due to JNI issues - lib: $rendererLib")
            
            // Try to restore renderer library loading - this is critical for graphics
            try {
                var success = ZLBridge.dlopen(rendererLib)
                if (!success) {
                    // Try to find in LD_LIBRARY_PATH if direct loading fails
                    val pathLib = findInLdLibPath(rendererLib)
                    if (pathLib != null) {
                        success = ZLBridge.dlopen(pathLib)
                    }
                }
                
                if (success) {
                    Logger.lInfo("Successfully loaded renderer library: $rendererLib")
                } else {
                    Logger.lError("Failed to load renderer library: $rendererLib")
                }
            } catch (e: UnsatisfiedLinkError) {
                Logger.lWarning("JNI error loading renderer library $rendererLib: ${e.message}")
            }
        }
    }

    override fun progressFinalUserArgs(args: MutableList<String>, ramAllocation: Int) {
        super.progressFinalUserArgs(args, ramAllocation)
        if (Renderers.isCurrentRendererValid()) {
            args.add("-Dorg.lwjgl.opengl.libname=${loadGraphicsLibrary()}")
        }
    }

    private fun getCacioJavaArgs(windowWidth: Int, windowHeight: Int, isJava8: Boolean): List<String> {
        val args = mutableListOf<String>()
        val scaleFactor = AllSettings.resolutionRatio.getValue() / 100f
        val scaledWidth = getDisplayFriendlyRes(windowWidth, scaleFactor)
        val scaledHeight = getDisplayFriendlyRes(windowHeight, scaleFactor)
        
        args.add("-Djava.awt.headless=false")
        args.add("-Dawt.toolkit=net.java.openjdk.cacio.ctk.CToolkit")
        args.add("-Djava.awt.graphicsenv=net.java.openjdk.cacio.ctk.CGraphicsEnvironment")
        args.add("-Dglfwstub.windowWidth=$scaledWidth")
        args.add("-Dglfwstub.windowHeight=$scaledHeight")
        args.add("-Dglfwstub.initEgl=false")
        
        if (!isJava8) {
            args.add("--add-exports=java.desktop/sun.awt=ALL-UNNAMED")
            args.add("--add-exports=java.desktop/sun.awt.image=ALL-UNNAMED")
            args.add("--add-exports=java.desktop/sun.java2d=ALL-UNNAMED")
            args.add("--add-exports=java.desktop/java.awt.peer=ALL-UNNAMED")
            
            if (runtime?.javaVersion ?: 8 >= 17) {
                args.add("-javaagent:${PathManager.DIR_COMPONENTS}/cacio-17/cacio-agent.jar")
            }
        }
        
        val cacioJarDir = if (runtime?.javaVersion ?: 8 >= 17) {
            File(PathManager.DIR_COMPONENTS, "cacio-17")
        } else {
            File(PathManager.DIR_COMPONENTS, "cacio-8")
        }
        args.add("-Xbootclasspath/a:${File(cacioJarDir, "cacio-ttc.jar").absolutePath}")
        
        return args
    }

    private fun findInLdLibPath(libName: String): String? {
        val ldLibraryPath = getRuntimeLibraryPath()
        return ldLibraryPath.split(":").firstNotNullOfOrNull { dir ->
            val file = File(dir, libName)
            if (file.exists()) file.absolutePath else null
        }
    }

    private fun printLauncherInfo(
        javaArguments: String,
        javaRuntime: String,
        account: Account
    ) {
        val renderer = Renderers.getCurrentRenderer()
        
        // Use regular Logger instead of LoggerBridge to avoid native library issues
        Logger.lInfo("==================== Launch Minecraft ====================")
        Logger.lInfo("Info: Launcher version: ${BuildConfig.VERSION_NAME}")
        Logger.lInfo("Info: Architecture: ${Architecture.archAsString(Architecture.getDeviceArchitecture())}")
        Logger.lInfo("Info: Renderer: ${renderer.getRendererName()}")
        Logger.lInfo("Info: Selected Minecraft version: ${version.getVersionName()}")
        Logger.lInfo("Info: Game Path: ${version.getGameDir().absolutePath} (Isolation: ${version.isIsolation()})")
        Logger.lInfo("Info: Custom Java arguments: $javaArguments")
        Logger.lInfo("Info: Java Runtime: $javaRuntime")
        Logger.lInfo("Info: Account: ${account.username} (${account.accountType})")
    }

    private fun getRuntimeName(): String {
        val versionRuntime = version.getJavaRuntime().takeIf { it.isNotEmpty() } ?: ""
        if (versionRuntime.isNotEmpty()) return versionRuntime

        val defaultRuntime = AllSettings.javaRuntime.getValue()
        if (AllSettings.autoPickJavaRuntime.getValue()) {
            val targetJavaVersion = gameManifest.javaVersion?.majorVersion ?: 8
            val runtime0 = RuntimesManager.getDefaultRuntime(targetJavaVersion)
            if (runtime0 != null) return runtime0.name
        }
        return defaultRuntime
    }

    private fun disableSplash(dir: File) {
        val configDir = File(dir, "config")
        if (configDir.exists() || configDir.mkdirs()) {
            val forgeSplashFile = File(configDir, "splash.properties")
            runCatching {
                if (forgeSplashFile.exists()) {
                    val content = forgeSplashFile.readText()
                    if (content.contains("enabled=true")) {
                        forgeSplashFile.writeText(content.replace("enabled=true", "enabled=false"))
                    }
                } else {
                    forgeSplashFile.writeText("enabled=false")
                }
            }
        }
    }

    private fun setRendererEnv(envMap: MutableMap<String, String>) {
        val renderer = Renderers.getCurrentRenderer()
        val rendererId = renderer.getRendererId()

        if (rendererId.startsWith("opengles")) {
            envMap["LIBGL_ES"] = if (getDetectedVersion() >= 3) "3" else "2"
        }

        envMap.putAll(renderer.getRendererEnv().value)
        
        envMap["POJAV_RENDERER"] = rendererId
        
        if (!rendererId.startsWith("opengles")) {
            envMap["MESA_LOADER_DRIVER_OVERRIDE"] = "zink"
            envMap["MESA_GLSL_CACHE_DIR"] = PathManager.DIR_CACHE.absolutePath
        }

        // Apply renderer-specific settings
        if (AllSettings.dumpShaders.state) {
            envMap["LIBGL_VGPU_DUMP"] = "1"
        }
        
        if (AllSettings.zinkPreferSystemDriver.state) {
            envMap["POJAV_ZINK_PREFER_SYSTEM_DRIVER"] = "1"
        }
        
        if (AllSettings.vsyncInZink.state) {
            envMap["POJAV_VSYNC_IN_ZINK"] = "1"
        }
        
        if (AllSettings.bigCoreAffinity.state) {
            envMap["POJAV_BIG_CORE_AFFINITY"] = "1"
        }
        
        if (AllSettings.sustainedPerformance.state) {
            envMap["POJAV_SUSTAINED_PERFORMANCE"] = "1"
        }
    }

    private fun getDetectedVersion(): Int {
        return runCatching {
            val display = EGL14.eglGetDisplay(EGL14.EGL_DEFAULT_DISPLAY)
            val version = IntArray(2)
            EGL14.eglInitialize(display, version, 0, version, 1)
            
            val attribList = intArrayOf(
                EGL14.EGL_RENDERABLE_TYPE, 0x0040, // EGL_OPENGL_ES3_BIT
                EGL14.EGL_NONE
            )
            val configs = arrayOfNulls<EGLConfig>(1)
            val numConfig = IntArray(1)
            EGL14.eglChooseConfig(display, attribList, 0, configs, 0, 1, numConfig, 0)
            
            if (numConfig[0] > 0) 3 else 2
        }.getOrElse { 2 }
    }

    private fun loadGraphicsLibrary(): String? {
        val rendererPlugin = RendererPluginManager.selectedRendererPlugin
        return if (rendererPlugin != null) {
            "${rendererPlugin.path}/${rendererPlugin.glName}"
        } else {
            Renderers.getCurrentRenderer().getRendererLibrary()
        }
    }

    /**
     * Calculate display-friendly resolution
     */
    private fun getDisplayFriendlyRes(pixels: Int, scaleFactor: Float): Int {
        return (pixels * scaleFactor).toInt().coerceAtLeast(1)
    }
}