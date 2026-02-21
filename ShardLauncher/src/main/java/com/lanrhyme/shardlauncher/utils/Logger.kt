package com.lanrhyme.shardlauncher.utils

import android.content.Context
import android.util.Log

object Logger {
    private const val TAG = "ShardLauncherLogger"

    fun log(context: Context?, tag: String, message: String) {
        Log.d(tag, message)
    }
    
    // For compatibility with ported code
    fun lDebug(message: String) {
        Log.d(TAG, message)
    }

    fun lInfo(message: String) {
        Log.i(TAG, message)
    }

    fun lError(message: String, e: Throwable? = null) {
        if (e != null) {
            Log.e(TAG, message, e)
        } else {
            Log.e(TAG, message)
        }
    }

    fun lWarning(message: String) {
        Log.w(TAG, message)
    }
}
