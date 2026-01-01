/*
 * Shard Launcher
 * Example usage of game launcher
 */

package com.lanrhyme.shardlauncher.game.launch

import android.app.Activity
import androidx.compose.ui.unit.IntSize
import com.lanrhyme.shardlauncher.game.version.installed.Version
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

/**
 * Example of how to launch a Minecraft game
 */
fun launchMinecraftExample(
    activity: Activity,
    version: Version,
    onSuccess: () -> Unit = {},
    onError: (String) -> Unit = {}
) {
    CoroutineScope(Dispatchers.Main).launch {
        LaunchGame.launchGame(
            context = activity,
            version = version,
            getWindowSize = { IntSize(1280, 720) }, // Default window size
            onSuccess = onSuccess,
            onError = onError
        )
    }
}

/**
 * Example of how to launch a JVM application
 */
fun launchJvmExample(
    activity: Activity,
    jvmArgs: String,
    onExit: (Int, Boolean) -> Unit = { _, _ -> }
) {
    CoroutineScope(Dispatchers.Main).launch {
        val launcher = JvmLauncher(
            context = activity,
            getWindowSize = { IntSize(1280, 720) },
            jvmLaunchInfo = JvmLaunchInfo(
                jvmArgs = jvmArgs,
                userHome = null, // Use default
                jreName = null   // Use default runtime
            ),
            onExit = onExit
        )
        
        launcher.launch()
    }
}