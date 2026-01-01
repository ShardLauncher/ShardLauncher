/*
 * Shard Launcher
 * Adapted from Zalith Launcher 2
 */

package com.lanrhyme.shardlauncher.utils.version

/**
 * Check if version is lower or equal to target
 */
fun String.isLowerOrEqualVer(target: String, snapshot: String? = null): Boolean {
    // Simple version comparison - in a real implementation this would be more sophisticated
    return this <= target || (snapshot != null && this.contains(snapshot))
}