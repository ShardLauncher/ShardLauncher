/*
 * Shard Launcher
 * Adapted from Zalith Launcher 2
 */

package com.lanrhyme.shardlauncher.game.version.download

import com.lanrhyme.shardlauncher.game.version.remote.MinecraftVersionJson

/**
 * Convert artifact name to path
 */
fun artifactToPath(library: MinecraftVersionJson.Library): String {
    val parts = library.name.split(":")
    if (parts.size < 3) return library.name
    
    val group = parts[0].replace(".", "/")
    val artifact = parts[1]
    val version = parts[2]
    
    return "$group/$artifact/$version/$artifact-$version.jar"
}

/**
 * Convert artifact name to path (GameManifest version)
 */
fun artifactToPath(library: com.lanrhyme.shardlauncher.game.versioninfo.models.GameManifest.Library): String? {
    val parts = library.name.split(":")
    if (parts.size < 3) return null
    
    val group = parts[0].replace(".", "/")
    val artifact = parts[1]
    val version = parts[2]
    
    return "$group/$artifact/$version/$artifact-$version.jar"
}

/**
 * Filter library based on rules
 */
fun MinecraftVersionJson.Library.filterLibrary(): Boolean {
    // Simplified filtering - in real implementation, this would check OS rules, etc.
    return false
}

/**
 * Process libraries for download
 */
fun processLibraries(libraries: List<MinecraftVersionJson.Library>) {
    libraries.forEach { library ->
        if (library.filterLibrary()) return@forEach
        // Process library download here
    }
}

/**
 * Process libraries for download (GameManifest version)
 */
fun processLibraries(librariesProvider: () -> List<com.lanrhyme.shardlauncher.game.versioninfo.models.GameManifest.Library>) {
    librariesProvider().forEach { library ->
        // Process library download here
    }
}