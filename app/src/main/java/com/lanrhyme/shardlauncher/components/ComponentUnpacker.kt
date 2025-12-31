/*
 * Shard Launcher
 */

package com.lanrhyme.shardlauncher.components

import android.content.Context
import com.lanrhyme.shardlauncher.utils.logging.Logger
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

object ComponentUnpacker {
    /**
     * Unpack all components if needed
     */
    suspend fun unpackAll(context: Context) = withContext(Dispatchers.IO) {
        runCatching {
            Components.entries.forEach { component ->
                val task = UnpackComponentsTask(context, component)
                if (task.isNeedUnpack()) {
                    Logger.lInfo("Unpacking component: ${component.displayName}")
                    task.run()
                }
            }
        }.onFailure { e ->
            Logger.lError("Failed to unpack components", e)
        }
    }
}
