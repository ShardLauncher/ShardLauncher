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

package com.lanrhyme.shardlauncher.bridge

import androidx.annotation.Keep

@Keep
object SLNativeInvoker {
    @JvmStatic
    var staticLauncher: Any? = null

    @Keep
    @JvmStatic
    fun openLink(link: String) {
        // TODO: Implement link opening functionality
        // This will be connected to the game launch activity later
        println("Open Link: $link")
    }

    @Keep
    @JvmStatic
    fun querySystemClipboard() {
        // TODO: Implement clipboard query
        SLBridge.clipboardReceived(null, null)
    }

    @Keep
    @JvmStatic
    fun putClipboardData(data: String, mimeType: String) {
        // TODO: Implement clipboard data set
        println("Put Clipboard: $data ($mimeType)")
    }

    @Keep
    @JvmStatic
    fun putFpsValue(fps: Int) {
        ZLBridgeStates.currentFPS = fps
    }

    @Keep
    @JvmStatic
    fun jvmExit(exitCode: Int, isSignal: Boolean) {
        println("JVM Exit: $exitCode, isSignal: $isSignal")
        // TODO: Implement proper exit handling
        staticLauncher = null
    }
}
