/*
 * Shard Launcher
 * Adapted from Zalith Launcher 2
 */

package com.lanrhyme.shardlauncher.game.launch

import com.lanrhyme.shardlauncher.BuildConfig
import com.lanrhyme.shardlauncher.game.account.Account
import com.lanrhyme.shardlauncher.game.account.isAuthServerAccount
import com.lanrhyme.shardlauncher.game.account.isLocalAccount
import com.lanrhyme.shardlauncher.game.account.isMicrosoftAccount
import com.lanrhyme.shardlauncher.game.multirt.Runtime
import com.lanrhyme.shardlauncher.game.path.getAssetsHome
import com.lanrhyme.shardlauncher.game.path.getLibrariesHome
import com.lanrhyme.shardlauncher.game.version.download.artifactToPath
import com.lanrhyme.shardlauncher.game.version.download.filterLibrary
import com.lanrhyme.shardlauncher.game.version.installed.Version
import com.lanrhyme.shardlauncher.game.version.installed.getGameManifest
import com.lanrhyme.shardlauncher.game.version.installed.getVersionType
import com.lanrhyme.shardlauncher.game.version.remote.MinecraftVersionJson
import com.lanrhyme.shardlauncher.path.LibPath
import com.lanrhyme.shardlauncher.path.PathManager
import com.lanrhyme.shardlauncher.utils.logging.Logger
import com.lanrhyme.shardlauncher.utils.json.insertJSONValueList
import com.lanrhyme.shardlauncher.utils.version.isLowerOrEqualVer
import com.lanrhyme.shardlauncher.utils.network.ServerAddress
import com.lanrhyme.shardlauncher.utils.string.toUnicodeEscaped
import java.io.File

class LaunchArgs(
    private val runtimeLibraryPath: String,
    private val account: Account,
    private val gameDirPath: File,
    private val version: Version,
    private val gameManifest: MinecraftVersionJson,
    private val runtime: Runtime,
    private val getCacioJavaArgs: (isJava8: Boolean) -> List<String>,
    private val offlineServerPort: Int = 0
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
        if (version.offlineAccountLogin) {
            // No automatic server join for offline login
        } else {
            version.getServerIp()?.let { addressString ->
                runCatching {
                    val address = ServerAddress.parse(addressString)
                    argsList.add("--server")
                    argsList.add(address.host)
                    if (address.port != -1) {
                        argsList.add("--port")
                        argsList.add(address.port.toString())
                    }
                }.onFailure {
                    Logger.lWarning("Invalid server address: $addressString")
                }
            }
        }
        
        // Quick Play Singleplayer
        version.quickPlaySingle?.let { saveName ->
            argsList.add("--quickPlaySingleplayer")
            argsList.add(saveName.toUnicodeEscaped())
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
            if (offlineServerPort != 0) {
                Logger.lInfo("Using offline Yggdrasil server on port $offlineServerPort")
                argsList.add("-javaagent:${LibPath.AUTHLIB_INJECTOR.absolutePath}=http://localhost:$offlineServerPort")
                argsList.add("-Dauthlibinjector.side=client")
            }
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
            val is7 = (version.getVersionInfo()?.minecraftVersion ?: "0.0").isLowerOrEqualVer("1.12")
            runCatching {
                val content = if (is7) {
                    // In a real implementation, this would read from assets
                    "<!-- Log4j 1.7 config -->"
                } else {
                    "<!-- Log4j 1.12 config -->"
                }
                configFilePath.writeText(content)
            }.onFailure {
                Logger.lWarning("Failed to write fallback Log4j configuration autonomously!", it)
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
            ?.mapNotNull { arg ->
                when {
                    arg.isJsonPrimitive && arg.asJsonPrimitive.isString -> arg.asString.processJvmArg()
                    arg.isJsonObject -> {
                        // Handle conditional arguments
                        val rules = arg.asJsonObject.get("rules")?.asJsonArray
                        val value = arg.asJsonObject.get("value")
                        
                        if (rules != null && checkArgumentRules(rules)) {
                            when {
                                value?.isJsonPrimitive == true && value.asJsonPrimitive.isString -> 
                                    value.asString.processJvmArg()
                                value?.isJsonArray == true -> 
                                    value.asJsonArray.joinToString(" ") { it.asString }
                                else -> null
                            }
                        } else null
                    }
                    else -> null
                }
            }
            ?.toTypedArray()
            ?: emptyArray()

        val allArgs = jvmArgs.toList() + varArgMap.keys.toList()
        val replacedArgs = insertJSONValueList(varArgMap, *allArgs.toTypedArray())
        return if (hasClasspath) {
            replacedArgs
        } else {
            replacedArgs + arrayOf("-cp", launchClassPath)
        }
    }

    private fun checkArgumentRules(rules: com.google.gson.JsonArray): Boolean {
        var allowed = false
        for (rule in rules) {
            val ruleObj = rule.asJsonObject
            val action = ruleObj.get("action")?.asString ?: continue
            val os = ruleObj.get("os")?.asJsonObject
            
            val osMatches = os?.get("name")?.asString?.equals("linux", ignoreCase = true) ?: true
            
            when (action) {
                "allow" -> if (osMatches) allowed = true
                "disallow" -> if (osMatches) allowed = false
            }
        }
        return allowed
    }

    private fun generateLaunchClassPath(gameManifest: MinecraftVersionJson): String {
        val classpathList = mutableListOf<String>()
        val classpath: Array<String> = generateLibClasspath(gameManifest)
        val clientClass = version.getClientJar()

        for (jarFile in classpath) {
            val jarFileObj = File(jarFile)
            if (!jarFileObj.exists()) continue
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
            if (!checkLibraryRules(libItem.rules)) continue
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
        varArgMap["user_type"] = if (account.isMicrosoftAccount()) "msa" else "legacy"
        varArgMap["version_name"] = version.getVersionInfo()?.minecraftVersion ?: ""

        setLauncherInfo(varArgMap)

        val minecraftArgs: MutableList<String> = ArrayList()
        gameManifest.arguments?.apply {
            game?.forEach { arg ->
                when {
                    arg.isJsonPrimitive && arg.asJsonPrimitive.isString -> 
                        minecraftArgs.add(arg.asString)
                    arg.isJsonObject -> {
                        // Handle conditional arguments
                        val rules = arg.asJsonObject.get("rules")?.asJsonArray
                        val value = arg.asJsonObject.get("value")
                        
                        if (rules != null && checkArgumentRules(rules)) {
                            when {
                                value?.isJsonPrimitive == true && value.asJsonPrimitive.isString -> 
                                    minecraftArgs.add(value.asString)
                                value?.isJsonArray == true -> 
                                    value.asJsonArray.forEach { minecraftArgs.add(it.asString) }
                            }
                        }
                    }
                }
            }
        }

        val baseArgs = splitAndFilterEmpty(
            gameManifest.minecraftArguments ?:
            minecraftArgs.toTypedArray().joinToString(" ")
        )
        val allArgs = baseArgs.toList() + varArgMap.keys.toList()
        return insertJSONValueList(varArgMap, *allArgs.toTypedArray())
    }

    private fun setLauncherInfo(verArgMap: MutableMap<String, String>) {
        verArgMap["launcher_name"] = "ShardLauncher"
        verArgMap["launcher_version"] = BuildConfig.VERSION_NAME
        verArgMap["version_type"] = version.getCustomInfo()
            .takeIf { it.isNotBlank() }
            ?: version.getVersionInfo()?.getVersionType()?.name ?: "release"
    }

    private fun splitAndFilterEmpty(arg: String): Array<String> {
        val list: MutableList<String> = ArrayList()
        arg.split(" ").forEach {
            if (it.isNotEmpty()) list.add(it)
        }
        return list.toTypedArray()
    }
}