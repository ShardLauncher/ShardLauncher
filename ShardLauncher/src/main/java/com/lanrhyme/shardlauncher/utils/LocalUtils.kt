/*
 * Shard Launcher
 */

package com.lanrhyme.shardlauncher.utils

import java.util.Locale

/**
 * Get system language in format "lan_country"
 */
fun getSystemLanguage(): String {
    val locale = Locale.getDefault()
    return locale.language + "_" + locale.country.lowercase(Locale.getDefault())
}
