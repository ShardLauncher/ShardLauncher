/*
 * Shard Launcher
 */

package com.lanrhyme.shardlauncher.utils

import androidx.compose.ui.unit.IntSize

/**
 * Get display-friendly resolution by applying a scale factor
 */
fun getDisplayFriendlyRes(originalRes: Int, scaleFactor: Float): Int {
    return (originalRes * scaleFactor).toInt()
}
