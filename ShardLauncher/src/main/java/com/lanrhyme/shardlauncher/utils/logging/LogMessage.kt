package com.lanrhyme.shardlauncher.utils.logging

/**
 * [Modified from HMCL](https://github.com/HMCL-dev/HMCL/blob/57018be/HMCLCore/src/main/java/org/jackhuang/hmcl/util/logging/LogEvent.java)
 */
data class LogMessage(
    val time: Long,
    val caller: String?,
    val level: Level,
    val message: String,
    val throwable: Throwable?
)