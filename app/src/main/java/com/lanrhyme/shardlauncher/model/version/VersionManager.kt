package com.lanrhyme.shardlauncher.model.version

import android.content.Context
import com.google.gson.Gson
import com.lanrhyme.shardlauncher.utils.network.fetchStringFromUrls
import java.io.File
import java.util.concurrent.TimeUnit

object VersionManager {
    private const val MINECRAFT_VERSION_MANIFEST_URL = "https://piston-meta.mojang.com/mc/game/version_manifest_v2.json"
    private const val MANIFEST_FILE_NAME = "version_manifest_v2.json"

    private lateinit var cacheDir: File
    private val gson = Gson()
    private var manifest: VersionManifest? = null

    fun init(context: Context) {
        cacheDir = context.cacheDir
    }

    suspend fun getVersionManifest(force: Boolean = false): VersionManifest {
        manifest?.takeIf { !force }?.let { return it }

        val manifestFile = File(cacheDir, MANIFEST_FILE_NAME)

        val isOutdated = !manifestFile.exists() ||
                manifestFile.lastModified() + TimeUnit.DAYS.toMillis(1) < System.currentTimeMillis()

        val newManifest = if (force || isOutdated) {
            downloadAndCacheManifest(manifestFile)
        } else {
            try {
                gson.fromJson(manifestFile.readText(), VersionManifest::class.java)
            } catch (e: Exception) {
                downloadAndCacheManifest(manifestFile)
            }
        }

        this.manifest = newManifest
        return newManifest
    }

    private suspend fun downloadAndCacheManifest(manifestFile: File): VersionManifest {
        val rawJson = fetchStringFromUrls(listOf(MINECRAFT_VERSION_MANIFEST_URL))
        manifestFile.writeText(rawJson)
        return gson.fromJson(rawJson, VersionManifest::class.java)
    }

    suspend fun getGameManifest(version: Version): GameManifest {
        val rawJson = fetchStringFromUrls(listOf(version.url ?: throw IllegalArgumentException("Version URL is null")))
        return gson.fromJson(rawJson, GameManifest::class.java)
    }
}
