package com.lanrhyme.shardlauncher.game.version.modloader

import com.lanrhyme.shardlauncher.api.ApiClient
import com.lanrhyme.shardlauncher.model.ModrinthVersion

abstract class ModVersions(private val modrinthId: String) {
    private var cacheVersions: List<ModrinthVersion>? = null

    suspend fun fetchVersionList(mcVersion: String, force: Boolean = false): List<ModrinthVersion>? {
        if (!force && cacheVersions != null) {
            return filterVersions(cacheVersions!!, mcVersion)
        }

        return try {
            // Fetch versions from Modrinth. 
            // We could filter by game_versions in API, but caching all might be better for switching versions.
            // However, for efficiency, filtering by loader (e.g. fabric/quilt) might be good if we knew which one.
            // Since this is generic ModVersions, we just fetch project versions.
            val versions = ApiClient.modrinthApiService.getProjectVersions(modrinthId)
            cacheVersions = versions
            filterVersions(versions, mcVersion)
        } catch (e: Exception) {
            e.printStackTrace()
            null
        }
    }

    private fun filterVersions(versions: List<ModrinthVersion>, mcVersion: String): List<ModrinthVersion> {
        return versions.filter { version ->
            version.gameVersions.contains(mcVersion)
        }
    }
}
