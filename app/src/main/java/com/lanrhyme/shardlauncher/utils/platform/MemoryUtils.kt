/*
 * Shard Launcher
 * Adapted from Zalith Launcher 2
 */

package com.lanrhyme.shardlauncher.utils.platform

import kotlin.math.roundToInt

/**
 * Calculate display-friendly resolution
 */
fun getDisplayFriendlyRes(pixels: Int, scaleFactor: Float): Int {
    return (pixels * scaleFactor).roundToInt().coerceAtLeast(1)
}

/**
 * Get maximum memory for settings (in MB)
 */
fun getMaxMemoryForSettings(): Int {
    val runtime = Runtime.getRuntime()
    val maxMemory = runtime.maxMemory()
    
    // Convert to MB and leave some headroom
    return ((maxMemory / 1024 / 1024) * 0.8).toInt().coerceAtLeast(512)
}