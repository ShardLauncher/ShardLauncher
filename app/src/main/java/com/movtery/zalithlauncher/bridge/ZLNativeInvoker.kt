/*
 * Compatibility class for native library
 * Redirects calls to the new package location
 */

package com.movtery.zalithlauncher.bridge

import androidx.annotation.Keep

@Keep
object ZLNativeInvoker {
    @JvmStatic
    var staticLauncher: Any?
        get() = com.lanrhyme.shardlauncher.bridge.ZLNativeInvoker.staticLauncher
        set(value) {
            com.lanrhyme.shardlauncher.bridge.ZLNativeInvoker.staticLauncher = value
        }

    @Keep
    @JvmStatic
    fun openLink(link: String) {
        com.lanrhyme.shardlauncher.bridge.ZLNativeInvoker.openLink(link)
    }

    @Keep
    @JvmStatic
    fun querySystemClipboard() {
        com.lanrhyme.shardlauncher.bridge.ZLNativeInvoker.querySystemClipboard()
    }

    @Keep
    @JvmStatic
    fun putClipboardData(data: String, mimeType: String) {
        com.lanrhyme.shardlauncher.bridge.ZLNativeInvoker.putClipboardData(data, mimeType)
    }

    @Keep
    @JvmStatic
    fun putFpsValue(fps: Int) {
        com.lanrhyme.shardlauncher.bridge.ZLNativeInvoker.putFpsValue(fps)
    }

    @Keep
    @JvmStatic
    fun jvmExit(exitCode: Int, isSignal: Boolean) {
        com.lanrhyme.shardlauncher.bridge.ZLNativeInvoker.jvmExit(exitCode, isSignal)
    }
}