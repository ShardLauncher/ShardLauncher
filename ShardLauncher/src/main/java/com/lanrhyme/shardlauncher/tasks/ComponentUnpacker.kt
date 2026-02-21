/*
 * Shard Launcher
 */

package com.lanrhyme.shardlauncher.tasks

import android.content.Context
import com.lanrhyme.shardlauncher.components.jre.Jre
import com.lanrhyme.shardlauncher.settings.AllSettings
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
            
            unpackJres(context)
        }.onFailure { e ->
            Logger.lError("Failed to unpack components", e)
        }
    }
    
    private suspend fun unpackJres(context: Context) {
        Jre.entries.forEach { jre ->
            val task = UnpackJreTask(context, jre)
            if (!task.isCheckFailed() && task.isNeedUnpack()) {
                Logger.lInfo("Unpacking JRE: ${jre.jreName}")
                task.run()
            }
        }
        
        if (AllSettings.javaRuntime.getValue().isEmpty()) {
            AllSettings.javaRuntime.setValue(Jre.JRE_8.jreName)
            Logger.lInfo("Default Java runtime set to ${Jre.JRE_8.jreName}")
        }
    }
}
