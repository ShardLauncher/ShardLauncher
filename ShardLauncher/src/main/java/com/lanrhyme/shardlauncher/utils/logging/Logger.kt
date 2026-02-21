/*
 * Shard Launcher
 * Adapted from Zalith Launcher 2
 * Copyright (C) 2025 MovTery <movtery228@qq.com> and contributors
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.
 * See the GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <https://www.gnu.org/licenses/gpl-3.0.txt>.
 */

package com.lanrhyme.shardlauncher.utils.logging

import android.util.Log

object Logger {
    private const val TAG = "ShardLauncher"

    fun d(tag: String, message: String) {
        Log.d("$TAG-$tag", message)
        LogCollector.add(LogCollector.LogLevel.DEBUG, tag, message)
    }

    fun i(tag: String, message: String) {
        Log.i("$TAG-$tag", message)
        LogCollector.add(LogCollector.LogLevel.INFO, tag, message)
    }

    fun w(tag: String, message: String) {
        Log.w("$TAG-$tag", message)
        LogCollector.add(LogCollector.LogLevel.WARNING, tag, message)
    }

    fun e(tag: String, message: String, throwable: Throwable? = null) {
        Log.e("$TAG-$tag", message, throwable)
        LogCollector.add(LogCollector.LogLevel.ERROR, tag, message, throwable)
    }

    // Legacy methods for compatibility
    fun lDebug(message: String) {
        Log.d(TAG, message)
        LogCollector.add(LogCollector.LogLevel.DEBUG, TAG, message)
    }

    fun lInfo(message: String) {
        Log.i(TAG, message)
        LogCollector.add(LogCollector.LogLevel.INFO, TAG, message)
    }

    fun lWarning(message: String, throwable: Throwable? = null) {
        Log.w(TAG, message, throwable)
        LogCollector.add(LogCollector.LogLevel.WARNING, TAG, message, throwable)
    }

    fun lError(message: String, throwable: Throwable? = null) {
        Log.e(TAG, message, throwable)
        LogCollector.add(LogCollector.LogLevel.ERROR, TAG, message, throwable)
    }
}