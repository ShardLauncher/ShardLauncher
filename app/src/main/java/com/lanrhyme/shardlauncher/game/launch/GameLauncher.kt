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
import com.lanrhyme.shardlauncher.utils.logging.Logger
import java.io.File

class GameLauncher(
    private val activity: Activity,
    private val version: Version,
    private val getWindowSize: () -> IntSize,
    private val onExit: (code: Int, isSignal: Boolean) -> Unit
) : Launcher() {
    
    private lateinit var gameManifest: MinecraftVersionJson
    private var runtime: Runtime? = null
    private var offlinePort: Int = 0

    override suspend fun launch(): Int {
        // Initialize renderer if needed
        if (!Renderers.isCurrentRendererValid()) {
            Renderers.setCurrentRenderer(activity, version.getRenderer())
        }

        // Get game manifest
        gameManifest = getGameManifest(version)

        // Get current account
        val currentAccount = AccountsManager.currentAccountFlow.value!!
        val account = if (version.offlineAccountLogin) {
            currentAccount.copy(accountType = "离线登录")
        } else {
            currentAccount
        }

        val customArgs = version.getJvmArgs().takeIf { it.isNotBlank() } ?: AllSettings.jvmArgs.getValue()
        val javaRuntimeName = getRuntimeName()
        val selectedRuntime = RuntimesManager.getRuntime(javaRuntimeName)
        this.runtime = selectedRuntime

        // Initialize LoggerBridge
        val logFile = File(PathManager.DIR_NATIVE_LOGS, "${getLogName()}.log")
        LoggerBridge.start(logFile.absolutePath)

        // Initialize MCOptions and set language
        MCOptions.setup(activity, version)
        MCOptions.loadLanguage(version.getVersionName())
        MCOptions.save()

        // Start offline Yggdrasil if needed
        if (version.offlineAccountLogin) {
            offlinePort = OfflineYggdrasilServer.start()
            OfflineYggdrasilServer.addCharacter(account.username, account.profileId)
        }

        printLauncherInfo(
            javaArguments = customArgs.takeIf { it.isNotEmpty() } ?: "NONE",
            javaRuntime = javaRuntimeName,
            account = account
        )

        val gameDirPath = version.getGameDir()
        disableSplash(gameDirPath)

        val ldLibraryPath = getRuntimeLibraryPath(RuntimesManager.getRuntimeHome(selectedRuntime.name).absolutePath)
        
        // Build environment
        val env = initEnv(ldLibraryPath)

        // Build launch arguments
        val launchArgs = LaunchArgs(
            runtimeLibraryPath = ldLibraryPath,
            account = account,
            gameDirPath = gameDirPath,
            version = version,
            gameManifest = gameManifest,
            runtime = selectedRuntime,
            getCacioJavaArgs = { javaVersion ->
                getCacioJavaArgs(javaVersion)
            },
            offlineServerPort = offlinePort
        ).getAllArgs()

        val finalArgs = progressFinalUserArgs(launchArgs + parseJavaArguments(customArgs))

        return launchJvm(finalArgs.toTypedArray(), ldLibraryPath, env)
    }

    override fun chdir() {
        val path = version.getGameDir().absolutePath
        ZLBridge.chdir(path)
    }

    override fun getLogName(): String = "game_${version.getVersionName()}_${System.currentTimeMillis()}"

    override fun exit(exitCode: Int) {
        if (offlinePort != 0) {
            OfflineYggdrasilServer.stop()
        }
        onExit(exitCode, false)
    }

    private fun initEnv(ldLibraryPath: String): Map<String, String> {
        val envMap = mutableMapOf<String, String>()

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

        // Set window size with resolution scaling
        val windowSize = getWindowSize()
        val scaleFactor = AllSettings.resolutionRatio.state / 100f
        val scaledWidth = getDisplayFriendlyRes(windowSize.width, scaleFactor)
        val scaledHeight = getDisplayFriendlyRes(windowSize.height, scaleFactor)
        
        envMap["glfwstub.windowWidth"] = scaledWidth.toString()
        envMap["glfwstub.windowHeight"] = scaledHeight.toString()
        envMap["AWTSTUB_WIDTH"] = scaledWidth.toString()
        envMap["AWTSTUB_HEIGHT"] = scaledHeight.toString()

        envMap["SHARD_VERSION_CODE"] = BuildConfig.VERSION_CODE.toString()
        
        super.initEnv(ldLibraryPath, envMap)
        return envMap
    }

    override fun dlopenEngine() {
        super.dlopenEngine()
        
        // Load renderer libraries
        RendererPluginManager.selectedRendererPlugin?.let { _ ->
             // Implementation for loading plugin libs
        }

        loadGraphicsLibrary()?.let { rendererLib ->
             ZLBridge.dlopen(rendererLib)
        }
    }

    private fun printLauncherInfo(
        javaArguments: String,
        javaRuntime: String,
        account: Account
    ) {
        val renderer = Renderers.getCurrentRenderer()
        LoggerBridge.appendTitle("Launch Minecraft")
        LoggerBridge.append("Info: Launcher version: ${BuildConfig.VERSION_NAME}")
        LoggerBridge.append("Info: Architecture: ${Architecture.archAsString()}")
        LoggerBridge.append("Info: Renderer: ${renderer.getRendererName()}")
        LoggerBridge.append("Info: Selected Minecraft version: ${version.getVersionName()}")
        LoggerBridge.append("Info: Game Path: ${version.getGameDir().absolutePath} (Isolation: ${version.isIsolation()})")
        LoggerBridge.append("Info: Custom Java arguments: $javaArguments")
        LoggerBridge.append("Info: Java Runtime: $javaRuntime")
        LoggerBridge.append("Info: Account: ${account.username} (${account.accountType})")
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