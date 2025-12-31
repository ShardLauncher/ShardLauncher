/*
 * Shard Launcher
 * Adapted from Zalith Launcher 2
 */

package com.lanrhyme.shardlauncher.game.version.installed.utils

import com.lanrhyme.shardlauncher.game.addons.modloader.ModLoader
import com.lanrhyme.shardlauncher.game.version.installed.VersionInfo
import com.lanrhyme.shardlauncher.utils.GSON
import com.lanrhyme.shardlauncher.utils.logging.Logger
import java.io.File

/**
 * Parse version JSON file to VersionInfo
 */
fun parseJsonToVersionInfo(jsonFile: File): VersionInfo? {
    return try {
        if (!jsonFile.exists()) {
            Logger.lWarning("Version JSON file does not exist: ${jsonFile.absolutePath}")
            return null
        }
        
        val jsonContent = jsonFile.readText()
        val versionJson = GSON.fromJson(jsonContent, VersionJsonData::class.java)
        
        VersionInfo(
            minecraftVersion = versionJson.id ?: "unknown",
            quickPlay = VersionInfo.QuickPlay(
                hasQuickPlaysSupport = false,
                isQuickPlaySingleplayer = false,
                isQuickPlayMultiplayer = false
            ),
            loaderInfo = parseLoaderInfo(versionJson)
        )
    } catch (e: Exception) {
        Logger.lError("Failed to parse version JSON: ${jsonFile.absolutePath}", e)
        null
    }
}

/**
 * Parse loader information from version JSON
 */
private fun parseLoaderInfo(versionJson: VersionJsonData): VersionInfo.LoaderInfo? {
    // Check for Forge
    versionJson.libraries?.find { it.name?.contains("forge") == true }?.let {
        return VersionInfo.LoaderInfo(
            loader = ModLoader.FORGE,
            version = extractForgeVersion(it.name ?: "")
        )
    }
    
    // Check for Fabric
    versionJson.libraries?.find { it.name?.contains("fabric") == true }?.let {
        return VersionInfo.LoaderInfo(
            loader = ModLoader.FABRIC,
            version = extractFabricVersion(it.name ?: "")
        )
    }
    
    // Check for Quilt
    versionJson.libraries?.find { it.name?.contains("quilt") == true }?.let {
        return VersionInfo.LoaderInfo(
            loader = ModLoader.QUILT,
            version = extractQuiltVersion(it.name ?: "")
        )
    }
    
    return null
}

private fun extractForgeVersion(name: String): String {
    return name.split(":").getOrNull(2) ?: "unknown"
}

private fun extractFabricVersion(name: String): String {
    return name.split(":").getOrNull(2) ?: "unknown"
}

private fun extractQuiltVersion(name: String): String {
    return name.split(":").getOrNull(2) ?: "unknown"
}

/**
 * Data class for parsing version JSON
 */
private data class VersionJsonData(
    val id: String?,
    val type: String?,
    val libraries: List<LibraryData>?
)

private data class LibraryData(
    val name: String?
)