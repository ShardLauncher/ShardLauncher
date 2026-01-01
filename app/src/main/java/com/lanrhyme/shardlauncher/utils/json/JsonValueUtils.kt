/*
 * Shard Launcher
 * Adapted from Zalith Launcher 2
 */

package com.lanrhyme.shardlauncher.utils.json

/**
 * Insert JSON value list with variable replacement
 */
fun insertJSONValueList(varArgMap: Map<String, String>, vararg args: String): Array<String> {
    return args.map { arg ->
        var result = arg
        varArgMap.forEach { (key, value) ->
            result = result.replace("\${$key}", value)
        }
        result
    }.toTypedArray()
}