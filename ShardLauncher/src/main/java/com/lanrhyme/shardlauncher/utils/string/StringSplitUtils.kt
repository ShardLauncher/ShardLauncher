/*
 * Shard Launcher
 * Adapted from Zalith Launcher 2
 */

package com.lanrhyme.shardlauncher.utils.string

/**
 * Split string preserving quoted sections
 */
fun String.splitPreservingQuotes(): List<String> {
    val result = mutableListOf<String>()
    val current = StringBuilder()
    var inQuotes = false
    var i = 0
    
    while (i < length) {
        val char = this[i]
        when {
            char == '"' -> {
                inQuotes = !inQuotes
                current.append(char)
            }
            char == ' ' && !inQuotes -> {
                if (current.isNotEmpty()) {
                    result.add(current.toString().trim('"'))
                    current.clear()
                }
            }
            else -> {
                current.append(char)
            }
        }
        i++
    }
    
    if (current.isNotEmpty()) {
        result.add(current.toString().trim('"'))
    }
    
    return result
}