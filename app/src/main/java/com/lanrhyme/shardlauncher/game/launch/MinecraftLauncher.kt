/*
 * Shard Launcher
 * Adalted from Zalith Launcher 2
 */

package com.lanrhyme.shardlauncher.game.launch

import android.content.Context
import androidx.compose.ui.unit.IntSize
import com.google.gson.JsonPrimitive
import com.lanrhyme.shardlauncher.bridge.LoggerBridge
import com.lanrhyme.shardlauncher.game.account.Account
import com.lanrhyme.shardlauncher.game.multirt.Runtime
import com.lanrhyme.shardlauncher.game.multirt.RuntimesManager
import com.lanrhyme.shardlauncher.game.path.GamePathManager
import com.lanrhyme.shardlauncher.game.version.installed.VersionConfig
import com.lanrhyme.shardlauncher.game.version.installed.VersionInfo
import com.lanrhyme.shardlauncher.game.version.remote.MinecraftVersionJson
import com.lanrhyme.shardlauncher.path.PathManager
import com.lanrhyme.shardlauncher.settings.AllSettings
import com.lanrhyme.shardlauncher.utils.GSON
import java.io.File

/**
 * Concrete Minecraft launcher implementation
 * Handles version JSON parsing, classpath building, and argument construction
 */
class MinecraftLauncher(
    private val context: Context,
    private val versionId: String,
    private val versionInfo: VersionInfo,
    private val versionConfig: VersionConfig,
    private val account: Account,
    onExit: (code: Int, isSignal: Boolean) -> Unit
) : Launcher(onExit) {

    private var logName: String = ""
    private lateinit var versionJson: MinecraftVersionJson

    override suspend fun launch(): Int {
        // Setup runtime
        this.runtime = resolveRuntime()
        
        logName = "minecraft_${versionId}_${System.currentTimeMillis()}"
        val logFile = File(PathManager.DIR_NATIVE_LOGS, "$logName.log")
        
        LoggerBridge.start(logFile.absolutePath)
        LoggerBridge.appendTitle("Minecraft Launch")
        LoggerBridge.append("Version: $versionId")
        LoggerBridge.append("Account: ${account.username}")
        LoggerBridge.append("Runtime: ${this.runtime.name}")

        // Parse version.json
        versionJson = parseVersionJson()
        
        // Build game args
        val jvmArgs = buildMinecraftArgs()
        val userJvmArgs = versionConfig.jvmArgs
        
        return launchJvm(
            context = context,
            jvmArgs = jvmArgs,
            userHome = GamePathManager.getUserHome(),
            userArgs = userJvmArgs,
            getWindowSize = { IntSize(1280, 720) }
        )
    }

    override fun progressFinalUserArgs(args: MutableList<String>, ramAllocation: Int) {
        val customRam = if (versionConfig.ramAllocation > 0) {
            versionConfig.ramAllocation
        } else {
            ramAllocation
        }
        super.progressFinalUserArgs(args, customRam)
    }

    private fun resolveRuntime(): Runtime {
        val runtimeName = versionConfig.javaRuntime.takeIf { it.isNotEmpty() }
            ?: detectDefaultRuntime()
        
        val runtimeHome = RuntimesManager.getRuntimeHome(runtimeName)
        val isJDK8 = RuntimesManager.isJDK8(runtimeHome.absolutePath)
        
        return Runtime(
            name = runtimeName,
            versionString = null,
            arch = System.getProperty("os.arch"),
            javaVersion = if (isJDK8) 8 else 17,
            isJDK8 = isJDK8
        )
    }

    private fun detectDefaultRuntime(): String {
        val runtimesDir = PathManager.DIR_MULTIRT
        val versionCode = versionInfo.getMcVersionCode()
        val preferJDK8 = versionCode.main < 13
        
        runtimesDir.listFiles()?.let { runtimes ->
            if (preferJDK8) {
                runtimes.firstOrNull { RuntimesManager.isJDK8(it.absolutePath) }?.let {
                    LoggerBridge.append("Auto-detected JDK8: ${it.name}")
                    return it.name
                }
            } else {
                runtimes.firstOrNull { !RuntimesManager.isJDK8(it.absolutePath) }?.let {
                    LoggerBridge.append("Auto-detected modern runtime: ${it.name}")
                    return it.name
                }
            }
            runtimes.firstOrNull()?.let {
                LoggerBridge.append("Auto-detected runtime: ${it.name}")
                return it.name
            }
        }
        
        val defaultName = if (preferJDK8) "jre8" else "jre17"
        LoggerBridge.append("No runtime found, using: $defaultName")
        return defaultName
    }

    private fun parseVersionJson(): MinecraftVersionJson {
        val gameDir = File(com.lanrhyme.shardlauncher.game.path.getGameHome())
        val versionJsonFile = File(gameDir, "versions/$versionId/$versionId.json")
        
        if (!versionJsonFile.exists()) {
            throw IllegalStateException("Version JSON not found: ${versionJsonFile.absolutePath}")
        }
        
        return GSON.fromJson(versionJsonFile.readText(), MinecraftVersionJson::class.java)
    }

    private fun buildMinecraftArgs(): List<String> {
        val args = mutableListOf<String>()
        
        args.add("-cp")
        args.add(buildClassPath())
        args.add(versionJson.mainClass)
        args.addAll(buildGameArguments())
        
        return args
    }

    private fun buildClassPath(): String {
        val gameDir = File(com.lanrhyme.shardlauncher.game.path.getGameHome())
        val librariesDir = File(gameDir, "libraries")
        val versionJar = File(gameDir, "versions/$versionId/$versionId.jar")
        
        val classPathParts = mutableListOf<String>()
        
        versionJson.libraries.forEach { library ->
            if (shouldIncludeLibrary(library)) {
                val libPath = File(librariesDir, library.downloads.artifact.path)
                if (libPath.exists()) {
                    classPathParts.add(libPath.absolutePath)
                }
            }
        }
        
        if (versionJar.exists()) {
            classPathParts.add(versionJar.absolutePath)
        }
        
        return classPathParts.joinToString(":")
    }

    private fun shouldIncludeLibrary(library: MinecraftVersionJson.Library): Boolean {
        if (library.rules.isNullOrEmpty()) return true
        
        library.rules.forEach { rule ->
            val osMatches = rule.os?.name?.equals("linux", ignoreCase = true) ?: true
            when (rule.action) {
                "allow" -> if (osMatches) return true
                "disallow" -> if (osMatches) return false
            }
        }
        return true
    }

    private fun buildGameArguments(): List<String> {
        val args = mutableListOf<String>()
        val gameDir = com.lanrhyme.shardlauncher.game.path.getGameHome()
        
        // Legacy format
        versionJson.minecraftArguments?.let { minecraftArgs ->
            val replaced = replaceArgumentVariables(minecraftArgs, gameDir)
            args.addAll(replaced.split(" ").filter { it.isNotBlank() })
            return args
        }
        
        // Modern format
        versionJson.arguments?.game?.forEach { argument ->
            if (argument is JsonPrimitive && argument.isString) {
                val replaced = replaceArgumentVariables(argument.asString, gameDir)
                args.add(replaced)
            }
        }
        
        return args
    }

    private fun replaceArgumentVariables(arg: String, gameDir: String): String {
        return arg
            .replace("\${auth_player_name}", account.username)
            .replace("\${version_name}", versionId)
            .replace("\${game_directory}", gameDir)
            .replace("\${assets_root}", File(gameDir, "assets").absolutePath)
            .replace("\${assets_index_name}", versionJson.assetIndex.id)
            .replace("\${auth_uuid}", account.profileId.takeIf { it.isNotEmpty() } ?: account.uniqueUUID)
            .replace("\${auth_access_token}", account.accessToken)
            .replace("\${user_type}", "mojang")
            .replace("\${version_type}", versionJson.type ?: "release")
            .replace("\${user_properties}", "{}")
    }

    override fun chdir(): String = com.lanrhyme.shardlauncher.game.path.getGameHome()
    override fun getLogName(): String = logName
    override fun exit() {
        LoggerBridge.append("Minecraft exiting...")
    }
}
