/*
 * Shard Launcher
 * Adapted from Zalith Launcher 2
 */

package com.lanrhyme.shardlauncher.utils.string

/**
 * Insert JSON value list with variable replacement
 */
fun insertJSONValueList(args: Array<String>, varArgMap: Map<String, String>): Array<String> {
    return args.map { arg ->
        var result = arg
        varArgMap.forEach { (key, value) ->
            result = result.replace("\${$key}", value)
        }
        result
    }.toTypedArray()
}

/**
 * Check if string is not empty or blank
 */
fun String?.isNotEmptyOrBlank(): Boolean {
    return !this.isNullOrBlank()
}

/**
 * Check if version is lower than another
 */
fun String.isVersionLowerThan(other: String): Boolean {
    val thisParts = this.split(".").map { it.toIntOrNull() ?: 0 }
    val otherParts = other.split(".").map { it.toIntOrNull() ?: 0 }
    
    val maxLength = maxOf(thisParts.size, otherParts.size)
    
    for (i in 0 until maxLength) {
        val thisPart = thisParts.getOrNull(i) ?: 0
        val otherPart = otherParts.getOrNull(i) ?: 0
        
        when {
            thisPart < otherPart -> return true
            thisPart > otherPart -> return false
        }
    }
    
    return false
}

/**
 * Check if string equals another (case sensitive)
 */
fun String.isStringEqualTo(other: String): Boolean {
    return this == other
}

/**
 * Convert string to Unicode escaped format
 */
fun String.toUnicodeEscaped(): String {
    return this.map { char ->
        if (char.code > 127) {
            "\\u${char.code.toString(16).padStart(4, '0')}"
        } else {
            char.toString()
        }
    }.joinToString("")
}

/**
 * Check if string is empty or blank
 */
fun String?.isEmptyOrBlank(): Boolean {
    return this.isNullOrBlank()
}

/**
 * Get message or toString for exceptions
 */
fun Throwable.getMessageOrToString(): String {
    return this.message ?: this.toString()
}

/**
 * Get string not null - return empty string if null
 */
fun getStringNotNull(string: String?): String = string ?: ""