/*
 * Shard Launcher
 * Adapted from Zalith Launcher 2
 */

package com.lanrhyme.shardlauncher.game.launch

import android.app.Activity
import androidx.compose.ui.unit.IntSize
import com.lanrhyme.shardlauncher.game.account.Account
import com.lanrhyme.shardlauncher.game.version.installed.Version

/**
 * Factory for creating game launchers
 */
object LauncherFactory {
    
    /**
     * Create a game launcher for Minecraft
     */
    fun createGameLauncher(
        activity: Activity,
        version: Version,
        account: Account,
        getWindowSize: () -> IntSize = { IntSize(1280, 720) },
        onExit: (code: Int, isSignal: Boolean) -> Unit
    ): GameLauncher {
        return GameLauncher(
            activity = activity,
            version = version,
            getWindowSize = getWindowSize,
            onExit = onExit
        )
    }
}