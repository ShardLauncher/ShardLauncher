/*
 * Shard Launcher
 * Adapted from Zalith Launcher 2
 */

package com.lanrhyme.shardlauncher.components.jre

enum class Jre(val jreName: String, val jrePath: String, val majorVersion: Int) {
    JRE_8("Internal-8", "runtimes/jre-8", 8),
    JRE_17("Internal-17", "runtimes/jre-17", 17),
    JRE_21("Internal-21", "runtimes/jre-21", 21)
}
