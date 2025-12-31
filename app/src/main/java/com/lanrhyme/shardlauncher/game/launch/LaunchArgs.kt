/*
 * Shard Launcher
 * Adapted from Zalith Launcher 2
 * Copyright (C) 2025 MovTery <movtery228@qq.com> and contributors
 */

package com.lanrhyme.shardlauncher.game.launch

import com.lanrhyme.shardlauncher.BuildConfig
import com.lanrhyme.shardlauncher.game.account.Account
import com.lanrhyme.shardlauncher.game.account.isAuthServerAccount
import com.lanrhyme.shardlauncher.game.account.isLocalAccount
import com.lanrhyme.shardlauncher.game.multirt.Runtime
import com.lanrhyme.shardlauncher.game.path.getAssetsHome
import com.lanrhyme.shardlauncher.game.path.getLibrariesHome
import com.lanrhyme.shardlauncher.game.version.download.artifactToPath
import com.lanrhyme.shardlauncher.game.version.download.filterLibrary
import com.lanrhyme.shardlauncher.game.version.installed.Version
import com.lanrhyme.shardlauncher.game.version.installed.getGameManifest
import com.lanrhyme.shardlauncher.game.version.remote.MinecraftVersionJson
import com.lanrhyme.shardlauncher.path.LibPath
import com.lanrhyme.shardlauncher.path.PathManager
import com.lanrhyme.shardlauncher.utils.logging.Logger
import com.lanrhyme.shardlauncher.utils.string.insertJSONValueList
import java.io.File

class LaunchArgs(
    private val runtimeLibraryPath: String,
    private val account: Account,
    private val gameDirPath: File,
    private val version: Version,
    private val gameManifest: MinecraftVersionJson,
    private val runtime: Runtime,
    private val getCacioJavaArgs: (isJava8: Boolean) -> List<String>
) {
    
    fun getAllArgs(): List<String> {
        val argsList: MutableList<String> = ArrayList()

        argsList.addAll(getJavaArgs())
        argsList.addAll(getMinecraftJVMArgs())

        if (runtime.javaVersion > 8) {
            argsList.add("--add-exports")
            val pkg: String = gameManifest.mainClass.substring(0, gameManifest.mainClass.lastIndexOf("."))
            argsList.add("$pkg/$pkg=ALL-UNNAMED")
        }

        argsList.add(gameManifest.mainClass)
        argsList.addAll(getMinecraftClientArgs())

        // Handle quick play and server connection
        version.getVersionInfo()?.let { info ->
            version.getServerIp()?.let { address ->
                val parts = address.split(":")
                val host = parts[0]
                val port = if (parts.size > 1) parts[1].toIntOrNull() ?: 25565 else 25565
                
                argsList.addAll(listOf("--server", host, "--port", port.toString()))
            }
        }

        return argsList
    }

    private fun getLWJGL3ClassPath(): String =
        File(PathManager.DIR_COMPONENTS, "lwjgl3")
            .listFiles { file -> file.name.endsWith(".jar") }
            ?.joinToString(":") { it.absolutePath }
            ?: ""

    private fun getJavaArgs(): List<String> {
        val argsList: MutableList<String> = ArrayList()

        // Handle authentication
        if (account.isLocalAccount()) {
            // Local account - no special handling needed for now
        } else if (account.isAuthServerAccount()) {
            account.otherBaseUrl?.let { baseUrl ->
                if (baseUrl.contains("auth.mc-user.com")) {
                    argsList.add("-javaagent:${LibPath.NIDE_8_AUTH.absolutePath}=${baseUrl.replace("https://auth.mc-user.com:233/", "")}")
                    argsList.add("-Dnide8auth.client=true")
                } else {
                    argsList.add("-javaagent:${LibPath.AUTHLIB_INJECTOR.absolutePath}=$baseUrl")
                }
            }
        }

        // Add Cacio args for window management
        argsList.addAll(getCacioJavaArgs(runtime.javaVersion == 8))

        // Configure Log4j
        val configFilePath = File(version.getVersionPath(), "log4j2.xml")
        if (!configFilePath.exists()) {
            // Create default log4j configuration
            val defaultConfig = """<?xml version="1.0" encoding="UTF-8"?>
<Configuration status="WARN">
    <Appenders>
        <Console name="SysOut" target="SYSTEM_OUT">
            <PatternLayout pattern="[%d{HH:mm:ss}] [%t/%level]: %msg%n" />
        </Console>
    </Appenders>
    <Loggers>
        <Root level="info">
            <AppenderRef ref="SysOut" />
        </Root>
    </Loggers>
</Configuration>"""
            runCatching {
                configFilePath.writeText(defaultConfig)
            }.onFailure {
                Logger.lWarning("Failed to write Log4j configuration", it)
            }
        }
        
        argsList.add("-Dlog4j.configurationFile=${configFilePath.absolutePath}")
        argsList.add("-Dminecraft.client.jar=${version.getClientJar().absolutePath}")

        return argsList
    }

    private fun getMinecraftJVMArgs(): Array<String> {
        val gameManifest1 = getGameManifest(version, true)

        val varArgMap: MutableMap<String, String> = mutableMapOf()
        val launchClassPath = "${getLWJGL3ClassPath()}:${generateLaunchClassPath(gameManifest)}"
        var hasClasspath = false

        varArgMap["classpath_separator"] = ":"
        varArgMap["library_directory"] = getLibrariesHome()
        varArgMap["version_name"] = gameManifest1.id
        varArgMap["natives_directory"] = runtimeLibraryPath
        setLauncherInfo(varArgMap)

        fun Any.processJvmArg(): String? = (this as? String)?.let {
            when {
                it.startsWith("-DignoreList=") -> {
                    "$it,${version.getVersionName()}.jar"
                }
                it.contains("-Dio.netty.native.workdir") ||
                it.contains("-Djna.tmpdir") ||
                it.contains("-Dorg.lwjgl.system.SharedLibraryExtractPath") -> {
                    it.replace("\${natives_directory}", PathManager.DIR_CACHE.absolutePath)
                }
                it == "\${classpath}" -> {
                    hasClasspath = true
                    launchClassPath
                }
                else -> it
            }
        }

        val jvmArgs = gameManifest1.arguments?.jvm
            ?.mapNotNull { it.processJvmArg() }
            ?.toTypedArray()
            ?: emptyArray()

        val replacedArgs = insertJSONValueList(jvmArgs, varArgMap)
        return if (hasClasspath) {
            replacedArgs
        } else {
            replacedArgs + arrayOf("-cp", launchClassPath)
        }
    }

    private fun generateLaunchClassPath(gameManifest: MinecraftVersionJson): String {
        val classpathList = mutableListOf<String>()
        val classpath: Array<String> = generateLibClasspath(gameManifest)
        val clientClass = version.getClientJar()

        for (jarFile in classpath) {
            val jarFileObj = File(jarFile)
            if (!jarFileObj.exists()) {
                Logger.lDebug("Ignored non-exists file: $jarFile")
                continue
            }
            classpathList.add(jarFile)
        }
        
        if (clientClass.exists()) {
            classpathList.add(clientClass.absolutePath)
        }

        return classpathList.joinToString(":")
    }

    private fun generateLibClasspath(gameManifest: MinecraftVersionJson): Array<String> {
        val libDir: MutableList<String> = ArrayList()
        for (libItem in gameManifest.libraries) {
            if (!(checkLibraryRules(libItem.rules) && libItem.downloads.artifact != null)) continue
            val libArtifactPath: String = libItem.progressLibrary() ?: continue
            libDir.add(getLibrariesHome() + "/" + libArtifactPath)
        }
        return libDir.toTypedArray()
    }

    private fun checkLibraryRules(rules: List<MinecraftVersionJson.Library.Rule>?): Boolean {
        if (rules.isNullOrEmpty()) return true
        
        var allowed = false
        for (rule in rules) {
            val osMatches = rule.os?.name?.equals("linux", ignoreCase = true) ?: true
            when (rule.action) {
                "allow" -> if (osMatches) allowed = true
                "disallow" -> if (osMatches) allowed = false
            }
        }
        return allowed
    }

    private fun MinecraftVersionJson.Library.progressLibrary(): String? {
        if (filterLibrary()) return null
        return artifactToPath(this)
    }

    private fun getMinecraftClientArgs(): Array<String> {
        val varArgMap: MutableMap<String, String> = mutableMapOf()
        varArgMap["auth_session"] = account.accessToken
        varArgMap["auth_access_token"] = account.accessToken
        varArgMap["auth_player_name"] = account.username
        varArgMap["auth_uuid"] = account.profileId.replace("-", "")
        varArgMap["auth_xuid"] = account.xUid ?: ""
        varArgMap["assets_root"] = getAssetsHome()
        varArgMap["assets_index_name"] = gameManifest.assetIndex.id
        varArgMap["game_assets"] = getAssetsHome()
        varArgMap["game_directory"] = gameDirPath.absolutePath
        varArgMap["user_properties"] = "{}"
        varArgMap["user_type"] = "msa"
        varArgMap["version_name"] = version.getVersionInfo()!!.minecraftVersion

        setLauncherInfo(varArgMap)

        val minecraftArgs: MutableList<String> = ArrayList()
        gameManifest.arguments?.apply {
            game?.forEach { if (it.isJsonPrimitive && it.asJsonPrimitive.isString) minecraftArgs.add(it.asString) }
        }

        return insertJSONValueList(
            splitAndFilterEmpty(
                gameManifest.minecraftArguments ?:
                minecraftArgs.toTypedArray().joinToString(" ")
            ), varArgMap
        )
    }

    private fun setLauncherInfo(verArgMap: MutableMap<String, String>) {
        verArgMap["launcher_name"] = "ShardLauncher"
        verArgMap["launcher_version"] = BuildConfig.VERSION_NAME
        verArgMap["version_type"] = version.getCustomInfo()
            .takeIf { it.isNotBlank() }
            ?: gameManifest.type ?: "release"
    }

    private fun splitAndFilterEmpty(arg: String): Array<String> {
        val list: MutableList<String> = ArrayList()
        arg.split(" ").forEach {
            if (it.isNotEmpty()) list.add(it)
        }
        return list.toTypedArray()
    }
}