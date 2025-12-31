/*
 * Shard Launcher
 */

package com.lanrhyme.shardlauncher.components

import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue

abstract class AbstractUnpackTask {
    /**
     * Description of the current task message
     */
    var taskMessage by mutableStateOf<String?>(null)

    /**
     * Check if unpacking is needed
     */
    abstract fun isNeedUnpack(): Boolean

    /**
     * Execute the unpacking task
     */
    abstract suspend fun run()
}
