/*
 * Shard Launcher
 * Adapted from Zalith Launcher 2
 */

package com.lanrhyme.shardlauncher.utils.platform

import android.app.ActivityManager
import android.content.Context
import androidx.annotation.WorkerThread
import com.lanrhyme.shardlauncher.utils.device.Architecture
import java.math.BigDecimal
import java.math.RoundingMode
import kotlin.math.*

private const val BYTES_PER_MB = 1024.0 * 1024.0

/**
 * Get memory info from ActivityManager
 */
@WorkerThread
fun getMemoryInfo(context: Context): ActivityManager.MemoryInfo {
    val activityManager = context.getSystemService(Context.ACTIVITY_SERVICE) as ActivityManager
    val memoryInfo = ActivityManager.MemoryInfo()
    activityManager.getMemoryInfo(memoryInfo)
    return memoryInfo
}

/**
 * Get total device memory
 */
@WorkerThread
fun getTotalMemory(context: Context) = getMemoryInfo(context).totalMem

/**
 * Get available memory
 */
@WorkerThread
fun getFreeMemory(context: Context) = getMemoryInfo(context).availMem

/**
 * Get maximum memory value for settings (reserve some memory for system)
 */
@WorkerThread
fun getMaxMemoryForSettings(context: Context): Int {
    val deviceRam = getTotalMemory(context).bytesToMB()
    val maxRam: Int = if (Architecture.is32BitsDevice || deviceRam < 2048) {
        min(1024.0, deviceRam).toInt()
    } else {
        // Reserve memory for the device to breathe
        (deviceRam - (if (deviceRam < 3064) 800 else 1024)).toInt()
    }
    return maxRam
}

/**
 * Convert bytes to MB
 */
fun Long.bytesToMB(decimals: Int = 2, roundDown: Boolean = false): Double {
    val megaBytes = this.toDouble() / BYTES_PER_MB
    return if (decimals == 0) {
        if (roundDown) floor(megaBytes) else round(megaBytes)
    } else {
        val roundingMode = if (roundDown) RoundingMode.DOWN else RoundingMode.HALF_UP
        BigDecimal(megaBytes).setScale(decimals, roundingMode).toDouble()
    }
}