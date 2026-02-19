/*
 * Shard Launcher
 * Adapted from Zalith Launcher 2
 */

package com.lanrhyme.shardlauncher.game.launch

import android.app.Activity
import android.opengl.EGL14
import android.opengl.EGLConfig
import androidx.compose.ui.unit.IntSize
import com.lanrhyme.shardlauncher.BuildConfig
import com.lanrhyme.shardlauncher.bridge.LoggerBridge
import com.lanrhyme.shardlauncher.bridge.SLBridge
import com.lanrhyme.shardlauncher.game.account.Account
import com.lanrhyme.shardlauncher.game.account.AccountType
import com.lanrhyme.shardlauncher.game.account.AccountsManager
import com.lanrhyme.shardlauncher.game.account.offline.OfflineYggdrasilServer
import com.lanrhyme.shardlauncher.game.account.isLocalAccount
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
import com.lanrhyme.shardlauncher.utils.logging.Logger
import org.lwjgl.glfw.CallbackBridge
import java.io.File

/**
 * Game launcher for launching Minecraft
 */
class GameLauncher(
    private val activity: Activity,
    private val version: Version,
    private val getWindowSize: () -> IntSize,
    onExit: (code: Int, isSignal: Boolean) -> Unit
) : Launcher(onExit) {
    
    companion object {
        private const val TAG = "GameLauncher"
    }

    private lateinit var gameManifest: MinecraftVersionJson
    private var offlinePort: Int = 0
    private var currentScreenSize: IntSize = IntSize(0, 0)

    override fun getLaunchScreenSize(): IntSize {
        return currentScreenSize
    }

    override suspend fun launch(): Int {
        // Get window size first
        currentScreenSize = getWindowSize()
        
        // Initialize renderer if needed
        initializeRenderer()

        // Get game manifest
        gameManifest = getGameManifest(version)
        
        // Set input stack queue usage based on game arguments support
        try {
            CallbackBridge.nativeSetUseInputStackQueue(gameManifest.arguments != null)
        } catch (e: Exception) {
            Logger.lWarning("Failed to set input stack queue mode")
        }

        // Get current account
        val currentAccount = AccountsManager.currentAccountFlow.value
            ?: throw IllegalStateException("No account selected for launch")
            
        val account = if (version.offlineAccountLogin) {
            currentAccount.copy(accountType = AccountType.LOCAL.toString())
        } else {
            currentAccount
        }

        val customArgs = version.getJvmArgs().takeIf { it.isNotBlank() } ?: AllSettings.jvmArgs.getValue()
        val javaRuntimeName = getRuntimeName()
        this.runtime = RuntimesManager.loadRuntime(javaRuntimeName)

        // Initialize MCOptions and set language
        initializeMCOptions()

        // Start offline Yggdrasil if needed for local accounts with skins
        if (account.isLocalAccount() && account.hasSkinFile) {
            startOfflineYggdrasil(account)
        }

        printLauncherInfo(
            javaArguments = customArgs.takeIf { it.isNotEmpty() } ?: "NONE",
            javaRuntime = javaRuntimeName,
            account = account
        )

        return launchGame(account, javaRuntimeName, customArgs)
    }

    /**
     * Initialize the renderer
     */
    private fun initializeRenderer() {
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
    }

    /**
     * Initialize MCOptions
     */
    private fun initializeMCOptions() {
        runCatching {
            MCOptions.setup(activity, version)
            MCOptions.loadLanguage(version.getVersionName())
            MCOptions.save()
            Logger.lInfo("MCOptions initialized successfully")
        }.onFailure { e ->
            Logger.lWarning("Failed to initialize MCOptions, continuing without it", e)
        }
    }

    /**
     * Start offline Yggdrasil server for skin support
     */
    private suspend fun startOfflineYggdrasil(account: Account) {
        runCatching {
            offlinePort = OfflineYggdrasilServer.start()
            OfflineYggdrasilServer.addCharacter(account.username, account.profileId)
            Logger.lInfo("Offline Yggdrasil server started on port $offlinePort")
        }.onFailure { e ->
            Logger.lWarning("Failed to start offline Yggdrasil server, continuing without it", e)
        }
    }

    /**
     * Launch the game
     */
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
                getCacioJavaArgs(currentScreenSize, isJava8)
            },
            offlineServerPort = offlinePort,
            readAssetsFile = { path ->
                activity.assets.open(path).bufferedReader().use { it.readText() }
            }
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
        // Stop offline Yggdrasil server if it was started
        if (offlinePort != 0) {
            runCatching {
                OfflineYggdrasilServer.stop()
                Logger.lInfo("Offline Yggdrasil server stopped")
            }.onFailure { e ->
                Logger.lWarning("Failed to stop offline Yggdrasil server", e)
            }
        }
    }

    override fun progressFinalUserArgs(args: MutableList<String>, ramAllocation: Int) {
        super.progressFinalUserArgs(args, ramAllocation)
        // Set the OpenGL library name for LWJGL - matching ZalithLauncher2
        if (Renderers.isCurrentRendererValid()) {
            args.add("-Dorg.lwjgl.opengl.libname=${loadGraphicsLibrary()}")
        }
    }

    override fun MutableMap<String, String>.putJavaArgs() {
        val versionInfo = version.getVersionInfo()
        
        // Fix Forge 1.7.2 sorting issue
        val is172 = (versionInfo?.minecraftVersion ?: "0.0") == "1.7.2"
        if (is172 && (versionInfo?.loaderInfo?.loader?.name == "forge")) {
            Logger.lDebug("Is Forge 1.7.2, using patched sorting method")
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

    override fun initEnv(screenSize: IntSize): MutableMap<String, String> {
        val envMap = super.initEnv(screenSize)

        // Set driver
        DriverPluginManager.setDriverById(version.getDriver())
        envMap["DRIVER_PATH"] = DriverPluginManager.getDriver().path

        // Set loader environment
        version.getVersionInfo()?.loaderInfo?.getLoaderEnvKey()?.let { loaderKey ->
            envMap[loaderKey] = "1"
        }

        // Set renderer environment - always try, even if currentRenderer is not set yet
        // This ensures POJAV_RENDERER and POJAVEXEC_EGL are set before dlopenEngine
        try {
            if (!Renderers.isCurrentRendererValid()) {
                // Initialize renderer if not set yet
                val rendererIdentifier = version.getRenderer()
                if (rendererIdentifier.isNotEmpty()) {
                    Renderers.setCurrentRenderer(activity, rendererIdentifier)
                } else {
                    val compatibleRenderers = Renderers.getCompatibleRenderers(activity)
                    if (compatibleRenderers.isNotEmpty()) {
                        Renderers.setCurrentRenderer(activity, compatibleRenderers[0].getUniqueIdentifier())
                    }
                }
            }
            if (Renderers.isCurrentRendererValid()) {
                setRendererEnv(envMap)
            }
        } catch (e: Exception) {
            Logger.lWarning("Failed to set renderer environment", e)
        }

        envMap["SHARD_VERSION_CODE"] = BuildConfig.VERSION_CODE.toString()
        
        return envMap
    }

    override fun dlopenEngine() {
        // Load libopenal.so first (audio library) - matching ZalithLauncher2 order
        safeJniCall("dlopen libopenal.so") {
            SLBridge.dlopen("${PathManager.DIR_NATIVE_LIB}/libopenal.so")
        }

        Logger.lInfo("==================== DLOPEN Renderer ====================")

        // Load renderer plugin libraries
        RendererPluginManager.selectedRendererPlugin?.let { renderer ->
            renderer.dlopen.forEach { lib ->
                safeJniCall("dlopen renderer plugin $lib") {
                    SLBridge.dlopen("${renderer.path}/$lib")
                }
            }
        }

        // Load graphics library (GL4ES/Zink) - matching ZalithLauncher2
        val rendererLib = loadGraphicsLibrary()
        if (rendererLib != null) {
            Logger.lInfo("Loading renderer library: $rendererLib")
            var success = SLBridge.dlopen(rendererLib)
            if (!success) {
                // Try to find in library path
                val foundPath = findInLdLibPath(rendererLib)
                if (foundPath != null && foundPath != rendererLib) {
                    Logger.lInfo("Found renderer in library path: $foundPath")
                    success = SLBridge.dlopen(foundPath)
                }
            }
            
            if (success) {
                Logger.lInfo("Successfully loaded renderer library: $rendererLib")
            } else {
                Logger.lError("Failed to load renderer $rendererLib")
            }
        }
    }

    /**
     * Print launcher information to logs
     */
    private fun printLauncherInfo(
        javaArguments: String,
        javaRuntime: String,
        account: Account
    ) {
        val renderer = Renderers.getCurrentRenderer()
        
        Logger.lInfo("==================== Launch Minecraft ====================")
        Logger.lInfo("Info: Launcher version: ${BuildConfig.VERSION_NAME}")
        Logger.lInfo("Info: Architecture: ${Architecture.archAsString(Architecture.getDeviceArchitecture())}")
        Logger.lInfo("Info: Renderer: ${renderer.getRendererName()}")
        Logger.lInfo("Info: Selected Minecraft version: ${version.getVersionName()}")
        Logger.lInfo("Info: Game Path: ${version.getGameDir().absolutePath} (Isolation: ${version.isIsolation()})")
        Logger.lInfo("Info: Custom Java arguments: $javaArguments")
        Logger.lInfo("Info: Java Runtime: $javaRuntime")
        Logger.lInfo("Info: Account: ${account.username} (${account.accountType})")
        
        // Also log to native logger
        runCatching {
            LoggerBridge.appendTitle("Launch Minecraft")
            LoggerBridge.append("Info: Launcher version: ${BuildConfig.VERSION_NAME}")
            LoggerBridge.append("Info: Architecture: ${Architecture.archAsString(Architecture.getDeviceArchitecture())}")
            LoggerBridge.append("Info: Renderer: ${renderer.getRendererName()}")
            LoggerBridge.append("Info: Selected Minecraft version: ${version.getVersionName()}")
            LoggerBridge.append("Info: Game Path: ${version.getGameDir().absolutePath}")
            LoggerBridge.append("Info: Account: ${account.username}")
        }
    }

    /**
     * Get the runtime name to use
     */
    private fun getRuntimeName(): String {
        // Check version-specific runtime first
        val versionRuntime = version.getJavaRuntime().takeIf { it.isNotEmpty() }
        if (versionRuntime != null) return versionRuntime

        // Check if auto-pick is enabled
        val defaultRuntime = AllSettings.javaRuntime.getValue()
        if (AllSettings.autoPickJavaRuntime.getValue()) {
            val targetJavaVersion = gameManifest.javaVersion?.majorVersion ?: 8
            val runtimeName = RuntimesManager.getNearestJreName(targetJavaVersion)
            if (runtimeName != null) return runtimeName
        }
        
        return defaultRuntime
    }

    /**
     * Disable Forge splash screen
     */
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
            }.onFailure { e ->
                Logger.lWarning("Failed to disable splash screen", e)
            }
        }
    }

    /**
     * Set renderer environment variables - matches ZalithLauncher2 implementation
     */
    private fun setRendererEnv(envMap: MutableMap<String, String>) {
        val renderer = Renderers.getCurrentRenderer()
        val rendererId = renderer.getRendererId()

        // Set GL4ES environment variables for opengles2 - matching ZalithLauncher2
        if (rendererId.startsWith("opengles2")) {
            envMap["LIBGL_ES"] = "2"
            envMap["LIBGL_MIPMAP"] = "3"
            envMap["LIBGL_NOERROR"] = "1"
            envMap["LIBGL_NOINTOVLHACK"] = "1"
            envMap["LIBGL_NORMALIZE"] = "1"
        }

        // Add renderer-specific environment variables
        envMap.putAll(renderer.getRendererEnv().value)

        // Set EGL name for pojav exec
        renderer.getRendererEGL()?.let { eglName ->
            envMap["POJAVEXEC_EGL"] = eglName
        }

        // Set Pojav renderer
        envMap["POJAV_RENDERER"] = rendererId

        // If using renderer plugin, skip additional settings
        if (RendererPluginManager.selectedRendererPlugin != null) return

        // Set Mesa configuration for non-GLES renderers (Zink)
        if (!rendererId.startsWith("opengles")) {
            envMap["MESA_LOADER_DRIVER_OVERRIDE"] = "zink"
            envMap["MESA_GLSL_CACHE_DIR"] = PathManager.DIR_CACHE.absolutePath
            envMap["force_glsl_extensions_warn"] = "true"
            envMap["allow_higher_compat_version"] = "true"
            envMap["allow_glsl_extension_directive_midshader"] = "true"
            envMap["LIB_MESA_NAME"] = loadGraphicsLibrary() ?: "null"
        }

        // Set GLES version if not already set
        if (!envMap.containsKey("LIBGL_ES")) {
            val glesMajor = getDetectedVersion()
            Logger.lInfo("GLES version detected: $glesMajor")

            envMap["LIBGL_ES"] = if (glesMajor < 3) {
                // Fallback to 2 since it's the minimum for the entire app
                "2"
            } else if (rendererId.startsWith("opengles")) {
                // Extract just the version number (e.g., "opengles3_ng_gl4es" -> "3")
                rendererId.substring(8).takeWhile { it.isDigit() }.ifEmpty { "3" }
            } else {
                // TODO if can: other backends such as Vulkan.
                // Sure, they should provide GLES 3 support.
                "3"
            }
        }
    }

    /**
     * Detect OpenGL ES version
     */
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

    /**
     * Load the graphics library for the current renderer
     */
    private fun loadGraphicsLibrary(): String? {
        val rendererPlugin = RendererPluginManager.selectedRendererPlugin
        return if (rendererPlugin != null) {
            "${rendererPlugin.path}/${rendererPlugin.glName}"
        } else {
            Renderers.getCurrentRenderer().getRendererLibrary()
        }
    }
}