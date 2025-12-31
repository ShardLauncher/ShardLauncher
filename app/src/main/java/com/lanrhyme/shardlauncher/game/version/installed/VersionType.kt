/*
 * Shard Launcher
 * Adapted from Zalith Launcher 2
 */

package com.lanrhyme.shardlauncher.game.version.installed

/**
 * Version type enumeration
 */
enum class VersionType {
    RELEASE,
    SNAPSHOT,
    BETA,
    ALPHA,
    UNKNOWN;
    
    companion object {
        fun fromString(type: String?): VersionType {
            return when (type?.lowercase()) {
                "release" -> RELEASE
                "snapshot" -> SNAPSHOT
                "beta" -> BETA
                "alpha" -> ALPHA
                else -> UNKNOWN
            }
        }
    }
}

/**
 * Extension function to get version type from VersionInfo
 */
fun VersionInfo?.getVersionType(): VersionType {
    // Since VersionInfo doesn't have a type field in the current implementation,
    // we'll determine type based on version string
    val version = this?.minecraftVersion ?: return VersionType.UNKNOWN
    return when {
        version.contains("w") -> VersionType.SNAPSHOT
        version.contains("pre") -> VersionType.BETA
        version.contains("rc") -> VersionType.BETA
        version.matches(Regex("\\d+\\.\\d+(\\.\\d+)?")) -> VersionType.RELEASE
        else -> VersionType.UNKNOWN
    }
}