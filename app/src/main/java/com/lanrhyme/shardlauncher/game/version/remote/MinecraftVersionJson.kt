/*
 * Shard Launcher  
 */

package com.lanrhyme.shardlauncher.game.version.remote

import com.google.gson.JsonElement
import com.google.gson.annotations.SerializedName

/**
 * Minecraft version JSON structure
 * Based on official Minecraft launcher format
 */
data class MinecraftVersionJson(
    val id: String,
    val type: String?,
    val mainClass: String,
    val minecraftArguments: String?, // Legacy format (pre-1.13)
    val arguments: Arguments?, // Modern format (1.13+)
    val libraries: List<Library>,
    val assetIndex: AssetIndex,
    val javaVersion: JavaVersion? = null
) {
    data class Arguments(
        val game: List<JsonElement>?,
        val jvm: List<JsonElement>?
    )
    
    data class Library(
        val name: String,
        val downloads: Downloads,
        val rules: List<Rule>? = null
    ) {
        data class Downloads(
            val artifact: Artifact
        )
        
        data class Artifact(
            val path: String,
            val url: String,
            val sha1: String?,
            val size: Long?
        )
        
        data class Rule(
            val action: String,
            val os: Os? = null
        )
        
        data class Os(
            val name: String?
        )
    }
    
    data class AssetIndex(
        val id: String,
        val url: String?,
        val sha1: String?,
        val size: Long?,
        val totalSize: Long?
    )
    
    data class JavaVersion(
        val component: String?,
        val majorVersion: Int?
    )
}
