/*
 * Shard Launcher
 * Adapted from Zalith Launcher 2
 */

package com.lanrhyme.shardlauncher.utils.device

import android.os.Build

/**
 * Device architecture utilities
 */
object Architecture {
    
    /**
     * Get architecture as string
     */
    fun archAsString(): String {
        return when (Build.SUPPORTED_ABIS[0]) {
            "arm64-v8a" -> "aarch64"
            "armeabi-v7a" -> "arm"
            "x86_64" -> "x86_64"
            "x86" -> "x86"
            else -> Build.SUPPORTED_ABIS[0]
        }
    }

    /**
     * Check if device is 64-bit
     */
    fun is64Bit(): Boolean {
        return Build.SUPPORTED_64_BIT_ABIS.isNotEmpty()
    }

    /**
     * Check if device is ARM
     */
    fun isARM(): Boolean {
        val arch = archAsString()
        return arch == "aarch64" || arch == "arm"
    }

    /**
     * Check if device is x86
     */
    fun isX86(): Boolean {
        val arch = archAsString()
        return arch == "x86_64" || arch == "x86"
    }
}