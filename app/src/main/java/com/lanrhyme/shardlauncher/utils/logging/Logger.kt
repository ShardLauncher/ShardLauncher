/*
 * Shard Launcher
 * Adapted from Zalith Launcher 2
 */

package com.lanrhyme.shardlauncher.utils.logging

import android.content.Context
import android.util.Log

/**
 * Logging utilities
 */
object Logger {
    private const val TAG = "ShardLauncher"

    fun initialize(context: Context) {
        // Initialize logging system
        lInfo("Logger initialized")
    }

    fun lDebug(message: String, throwable: Throwable? = null) {
        Log.d(TAG, message, throwable)
    }

    fun lInfo(message: String, throwable: Throwable? = null) {
        Log.i(TAG, message, throwable)
    }

    fun lWarning(message: String, throwable: Throwable? = null) {
        Log.w(TAG, message, throwable)
    }

    fun lError(message: String, throwable: Throwable? = null) {
        Log.e(TAG, message, throwable)
    }
}