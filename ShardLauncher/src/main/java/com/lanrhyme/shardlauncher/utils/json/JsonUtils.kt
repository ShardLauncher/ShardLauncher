/*
 * Shard Launcher
 * Adapted from Zalith Launcher 2
 */

package com.lanrhyme.shardlauncher.utils.json

import com.google.gson.JsonObject

/**
 * Get string value from JsonObject, return empty string if null
 */
fun JsonObject.getStringNotNull(key: String): String {
    return this.get(key)?.asString ?: ""
}

/**
 * Get string value from JsonObject with default
 */
fun JsonObject.getStringOrDefault(key: String, default: String = ""): String {
    return this.get(key)?.asString ?: default
}

/**
 * 插入JSON值列表
 */
fun insertJSONValueList(vararg values: String): Array<String> {
    return values.toList().toTypedArray()
}