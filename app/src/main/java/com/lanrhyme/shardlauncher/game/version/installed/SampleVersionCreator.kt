/*
 * Shard Launcher
 */

package com.lanrhyme.shardlauncher.game.version.installed

import com.lanrhyme.shardlauncher.game.path.getGameHome
import com.lanrhyme.shardlauncher.utils.GSON
import com.lanrhyme.shardlauncher.utils.logging.Logger
import java.io.File

/**
 * Creates sample versions for testing
 */
object SampleVersionCreator {
    
    /**
     * Create a sample Minecraft version for testing
     */
    fun createSampleVersion() {
        try {
            val gameHome = File(getGameHome())
            val versionsDir = File(gameHome, "versions")
            val sampleVersionDir = File(versionsDir, "1.20.1-sample")
            
            if (sampleVersionDir.exists()) {
                Logger.lInfo("Sample version already exists")
                return
            }
            
            sampleVersionDir.mkdirs()
            
            // Create sample version.json
            val versionJson = createSampleVersionJson()
            val versionJsonFile = File(sampleVersionDir, "1.20.1-sample.json")
            versionJsonFile.writeText(GSON.toJson(versionJson))
            
            // Create empty jar file (for testing)
            val jarFile = File(sampleVersionDir, "1.20.1-sample.jar")
            jarFile.createNewFile()
            
            Logger.lInfo("Sample version created: ${sampleVersionDir.absolutePath}")
            
        } catch (e: Exception) {
            Logger.lError("Failed to create sample version", e)
        }
    }
    
    private fun createSampleVersionJson(): Map<String, Any> {
        return mapOf(
            "id" to "1.20.1-sample",
            "type" to "release",
            "mainClass" to "net.minecraft.client.main.Main",
            "minecraftArguments" to "--username \${auth_player_name} --version \${version_name} --gameDir \${game_directory} --assetsDir \${assets_root} --assetIndex \${assets_index_name} --uuid \${auth_uuid} --accessToken \${auth_access_token} --userType \${user_type} --versionType \${version_type}",
            "libraries" to emptyList<Map<String, Any>>(),
            "assetIndex" to mapOf(
                "id" to "1.20",
                "sha1" to "sample",
                "size" to 0,
                "totalSize" to 0,
                "url" to "sample"
            )
        )
    }
}