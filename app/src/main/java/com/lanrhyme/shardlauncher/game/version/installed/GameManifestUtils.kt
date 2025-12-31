/*
 * Shard Launcher
 * Adapted from Zalith Launcher 2
 */

package com.lanrhyme.shardlauncher.game.version.installed

import com.lanrhyme.shardlauncher.game.path.getGameHome
import com.lanrhyme.shardlauncher.game.version.remote.MinecraftVersionJson
import com.lanrhyme.shardlauncher.utils.GSON
import java.io.File

/**
 * Get game manifest from version
 */
fun getGameManifest(version: Version, forceReload: Boolean = false): MinecraftVersionJson {
    val gameDir = File(getGameHome())
    val versionJsonFile = File(gameDir, "versions/${version.getVersionName()}/${version.getVersionName()}.json")
    
    if (!versionJsonFile.exists()) {
        throw IllegalStateException("Version JSON not found: ${versionJsonFile.absolutePath}")
    }
    
    return GSON.fromJson(versionJsonFile.readText(), MinecraftVersionJson::class.java)
}