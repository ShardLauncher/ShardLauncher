/*
 * Shard Launcher
 * Adapted from Zalith Launcher 2
 * Copyright (C) 2025 MovTery <movtery228@qq.com> and contributors
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 */

package com.lanrhyme.shardlauncher.game.launch

import android.app.Activity
import android.os.Build
import androidx.compose.ui.unit.IntSize
import com.lanrhyme.shardlauncher.BuildConfig
import com.lanrhyme.shardlauncher.bridge.LoggerBridge
import com.lanrhyme.shardlauncher.bridge.ZLBridge
import com.lanrhyme.shardlauncher.game.account.Account
import com.lanrhyme.shardlauncher.game.account.AccountType
import com.lanrhyme.shardlauncher.game.account.AccountsManager
import com.lanrhyme.shardlauncher.game.multirt.Runtime
import com.lanrhyme.shardlauncher.game.multirt.RuntimesManager
import com.lanrhyme.shardlauncher.game.plugin.driver.DriverPluginManager
import com.lanrhyme.shardlauncher.game.plugin.renderer.RendererPlugin
import com.lanrhyme.shardlauncher.game.plugin.renderer.RendererPluginManager
import com.lanrhyme.shardlauncher.game.renderer.Renderers
import com.lanrhyme.shardlauncher.game.version.installed.Version
import com.lanrhyme.shardlauncher.game.version.installed.getGameManifest
import com.lanrhyme.shardlauncher.game.version.remote.MinecraftVersionJson
import com.lanrhyme.shardlauncher.path.PathManager
import com.lanrhyme.shardlauncher.settings.AllSettings
import com.lanrhyme.shardlauncher.utils.device.Architecture
import com.lanrhyme.shardlauncher.utils.logging.Logger
import com.lanrhyme.shardlauncher.game.path.getGameHome
import java.io.File

class GameLauncher(
    private val activity: Activity,
    private val version: Version,
    private val getWindowSize: () -> IntSize,
    onExit: (code: Int, isSignal: Boolean) -> Unit
) : Launcher(onExit) {
    
    private lateinit var gameManifest: MinecraftVersionJson

    override fun exit() {
        // Clean up resources
    }

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
            // Use temporary offline account for game launch
            currentAccount.copy(
                accountType = AccountType.LOCAL
            )
        } else {
            currentAccount
        }

        val customArgs = version.getJvmArgs().takeIf { it.isNotBlank() } ?: AllSettings.jvmArgs.getValue()
        val javaRuntime = getRuntime()

        printLauncherInfo(
            javaArguments = customArgs.takeIf { it.isNotEmpty() } ?: "NONE",
            javaRuntime = javaRuntime,
            account = account
        )

        return launchGame(
            account = account,
            javaRuntime = javaRuntime,
            customArgs = customArgs
        )
    }

    override fun MutableMap<String, String>.putJavaArgs() {
        // Handle JNA library path
        gameManifest.libraries.find { library ->
            library.name.startsWith("net.java.dev.jna:jna:")
        }?.let { library ->
            val versionParts = library.name.split(":")
            if (versionParts.size >= 3) {
                val jnaVersion = versionParts[2]
                val jnaDir = File(PathManager.DIR_JNA, jnaVersion)
                if (jnaDir.exists()) {
                    val dirPath = jnaDir.absolutePath
                    put("java.library.path", "$dirPath:${PathManager.DIR_NATIVE_LIB}")
                    put("jna.boot.library.path", dirPath)
                }
            }
        }
    }

    override fun chdir(): String {
        return version.getGameDir().absolutePath
    }

    override fun getLogName(): String = "game_${version.getVersionName()}_${System.currentTimeMillis()}"

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
        LoggerBridge.appendTitle("DLOPEN Renderer")

        // Load renderer libraries
        RendererPluginManager.selectedRendererPlugin?.let { rendererPlugin ->
            // rendererPlugin.dlopen.forEach { lib -> 
            //     ZLBridge.dlopen("${rendererPlugin.path}/$lib") 
            // }
        }

        loadGraphicsLibrary()?.let { rendererLib ->
            // if (!ZLBridge.dlopen(rendererLib) && !ZLBridge.dlopen(findInLdLibPath(rendererLib))) {
            //     Logger.lError("Failed to load renderer $rendererLib")
            // }
        }
    }

    override fun progressFinalUserArgs(args: MutableList<String>, ramAllocation: Int) {
        super.progressFinalUserArgs(args, version.getRamAllocation(activity))
        if (Renderers.isCurrentRendererValid()) {
            args.add("-Dorg.lwjgl.opengl.libname=${loadGraphicsLibrary()}")
        }
    }

    private suspend fun launchGame(
        account: Account,
        javaRuntime: String,
        customArgs: String
    ): Int {
        val runtime = RuntimesManager.getRuntime(javaRuntime)
        val gameDirPath = version.getGameDir()

        // Disable Forge splash screen
        disableSplash(gameDirPath)

        // Initialize runtime environment
        this.runtime = runtime
        val runtimeLibraryPath = getRuntimeLibraryPath()

        // Build launch arguments
        val launchArgs = LaunchArgs(
            runtimeLibraryPath = runtimeLibraryPath,
            account = account,
            gameDirPath = gameDirPath,
            version = version,
            gameManifest = gameManifest,
            runtime = runtime,
            getCacioJavaArgs = { isJava8 ->
                val size = getWindowSize()
                getCacioJavaArgs(size.width, size.height, isJava8)
            }
        ).getAllArgs()

        return launchJvm(
            context = activity,
            jvmArgs = launchArgs,
            userArgs = customArgs,
            getWindowSize = getWindowSize
        )
    }

    private fun printLauncherInfo(
        javaArguments: String,
        javaRuntime: String,
        account: Account
    ) {
        var mcInfo = version.getVersionName()
        version.getVersionInfo()?.let { info -> mcInfo = info.getInfoString() }
        val renderer = Renderers.getCurrentRenderer()

        LoggerBridge.appendTitle("Launch Minecraft")
        LoggerBridge.append("Info: Launcher version: ${BuildConfig.VERSION_NAME} (${BuildConfig.VERSION_CODE})")
        LoggerBridge.append("Info: Architecture: ${Architecture.archAsString()}")
        LoggerBridge.append("Info: Device model: ${Build.MANUFACTURER}, ${Build.MODEL}")
        LoggerBridge.append("Info: API version: ${Build.VERSION.SDK_INT}")
        LoggerBridge.append("Info: Renderer: ${renderer.getRendererName()}")
        renderer.getRendererSummary()?.let { summary ->
            LoggerBridge.append("Info: Renderer Summary: $summary")
        }
        LoggerBridge.append("Info: Selected Minecraft version: ${version.getVersionName()}")
        LoggerBridge.append("Info: Minecraft Info: $mcInfo")
        LoggerBridge.append("Info: Game Path: ${version.getGameDir().absolutePath} (Isolation: ${version.isIsolation()})")
        LoggerBridge.append("Info: Custom Java arguments: $javaArguments")
        LoggerBridge.append("Info: Java Runtime: $javaRuntime")
        LoggerBridge.append("Info: Account: ${account.username} (${account.accountType})")
    }

    private fun getRuntime(): String {
        val versionRuntime = version.getJavaRuntime().takeIf { it.isNotEmpty() } ?: ""
        if (versionRuntime.isNotEmpty()) return versionRuntime

        val runtime = AllSettings.javaRuntime.getValue()
        val pickedRuntime = RuntimesManager.getRuntime(runtime)

        if (AllSettings.autoPickJavaRuntime.getValue()) {
            // Auto-select based on game requirements
            val targetJavaVersion = gameManifest.javaVersion?.majorVersion ?: 8
            if (pickedRuntime.javaVersion == 0 || pickedRuntime.javaVersion < targetJavaVersion) {
                val runtime0 = RuntimesManager.getDefaultRuntime(targetJavaVersion)
                if (runtime0 != null) {
                    return runtime0.name
                }
            }
        }
        return runtime
    }

    private fun disableSplash(dir: File) {
        val configDir = File(dir, "config")
        if (configDir.exists() || configDir.mkdirs()) {
            val forgeSplashFile = File(configDir, "splash.properties")
            runCatching {
                var forgeSplashContent = "enabled=true"
                if (forgeSplashFile.exists()) {
                    forgeSplashContent = forgeSplashFile.readText()
                }
                if (forgeSplashContent.contains("enabled=true")) {
                    forgeSplashFile.writeText(
                        forgeSplashContent.replace("enabled=true", "enabled=false")
                    )
                }
            }.onFailure {
                Logger.lWarning("Could not disable Forge splash screen!", it)
            }
        }
    }

    private fun setRendererEnv(envMap: MutableMap<String, String>) {
        val renderer = Renderers.getCurrentRenderer()
        val rendererId = renderer.getRendererId()

        if (rendererId.startsWith("opengles2")) {
            envMap["LIBGL_ES"] = "2"
            envMap["LIBGL_MIPMAP"] = "3"
            envMap["LIBGL_NOERROR"] = "1"
            envMap["LIBGL_NOINTOVLHACK"] = "1"
            envMap["LIBGL_NORMALIZE"] = "1"
        }

        envMap += renderer.getRendererEnv().value

        renderer.getRendererEGL()?.let { eglName ->
            envMap["POJAVEXEC_EGL"] = eglName
        }

        envMap["POJAV_RENDERER"] = rendererId

        if (RendererPluginManager.selectedRendererPlugin != null) return

        if (!rendererId.startsWith("opengles")) {
            envMap["MESA_LOADER_DRIVER_OVERRIDE"] = "zink"
            envMap["MESA_GLSL_CACHE_DIR"] = PathManager.DIR_CACHE.absolutePath
            envMap["force_glsl_extensions_warn"] = "true"
            envMap["allow_higher_compat_version"] = "true"
            envMap["allow_glsl_extension_directive_midshader"] = "true"
            envMap["LIB_MESA_NAME"] = loadGraphicsLibrary() ?: "null"
        }

        if (!envMap.containsKey("LIBGL_ES")) {
            envMap["LIBGL_ES"] = "3" // Default to OpenGL ES 3
        }
    }

    private fun loadGraphicsLibrary(): String? {
        if (!Renderers.isCurrentRendererValid()) return null
        
        val rendererPlugin = RendererPluginManager.selectedRendererPlugin
        return if (rendererPlugin != null) {
            "${rendererPlugin.path}/${rendererPlugin.glName}"
        } else {
            Renderers.getCurrentRenderer().getRendererLibrary()
        }
    }
}