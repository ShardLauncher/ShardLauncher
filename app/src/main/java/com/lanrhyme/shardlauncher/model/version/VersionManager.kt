package com.lanrhyme.shardlauncher.model.version

import com.google.gson.Gson
import com.lanrhyme.shardlauncher.path.PathManager
import com.lanrhyme.shardlauncher.utils.network.fetchStringFromUrls
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.io.File
import java.util.concurrent.TimeUnit

object VersionManager {
    private const val MINECRAFT_VERSION_MANIFEST_URL = "https://piston-meta.mojang.com/mc/game/version_manifest_v2.json"
    private const val MANIFEST_FILE_NAME = "version_manifest_v2.json"

    private val gson = Gson()
    private var manifest: VersionManifest? = null

    suspend fun getVersionManifest(force: Boolean = false): VersionManifest {
        manifest?.takeIf { !force }?.let { return it }

        val cacheDir = PathManager.DIR_CACHE
        val manifestFile = File(cacheDir, MANIFEST_FILE_NAME)

        val isOutdated = !manifestFile.exists() ||
                manifestFile.lastModified() + TimeUnit.DAYS.toMillis(1) < System.currentTimeMillis()

        val newManifest = if (force || isOutdated) {
            withContext(Dispatchers.IO) {
                downloadAndCacheManifest(manifestFile)
            }
        } else {
            try {
                gson.fromJson(manifestFile.readText(), VersionManifest::class.java)
            } catch (e: Exception) {
                withContext(Dispatchers.IO) {
                    downloadAndCacheManifest(manifestFile)
                }
            }
        }

        this.manifest = newManifest
        return newManifest
    }

    private fun downloadAndCacheManifest(manifestFile: File): VersionManifest {
        val rawJson = fetchStringFromUrls(listOf(MINECRAFT_VERSION_MANIFEST_URL))
        manifestFile.writeText(rawJson)
        return gson.fromJson(rawJson, VersionManifest::class.java)
    }

    fun getGameManifest(version: Version): GameManifest {
        val rawJson = fetchStringFromUrls(listOf(version.url ?: throw IllegalArgumentException("Version URL is null")))
        return gson.fromJson(rawJson, GameManifest::class.java)
    }
}
