/*
 * Shard Launcher
 * Adapted from Zalith Launcher 2
 */

package com.lanrhyme.shardlauncher.game.launch

import android.content.Context
import androidx.compose.ui.unit.IntSize
import com.lanrhyme.shardlauncher.game.account.AccountsManager
import com.lanrhyme.shardlauncher.game.version.download.BaseMinecraftDownloader
import com.lanrhyme.shardlauncher.game.version.download.DownloadMode
import com.lanrhyme.shardlauncher.game.version.installed.Version
import com.lanrhyme.shardlauncher.utils.logging.Logger
import com.lanrhyme.shardlauncher.utils.network.isNetworkAvailable
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

object LaunchGame {
    private var isLaunching = false

    fun launchGame(
        context: Context,
        version: Version,
        getWindowSize: () -> IntSize,
        onSuccess: () -> Unit,
        onError: (String) -> Unit
    ) {
        if (isLaunching) return

        val account = AccountsManager.getCurrentAccount()
        if (account == null) {
            onError("No account selected")
            return
        }

        isLaunching = true

        CoroutineScope(Dispatchers.Main).launch {
            try {
                // Download and verify game files if needed
                // In a real implementation, this would run the download task
                // For now, we'll skip to launching

                runGame(context, version, getWindowSize, onSuccess, onError)
            } catch (e: Exception) {
                Logger.lError("Failed to launch game", e)
                onError("Launch failed: ${e.message}")
            } finally {
                isLaunching = false
            }
        }
    }

    private suspend fun runGame(
        context: Context,
        version: Version,
        getWindowSize: () -> IntSize,
        onSuccess: () -> Unit,
        onError: (String) -> Unit
    ) {
        try {
            val launcher = GameLauncher(
                activity = context as android.app.Activity,
                version = version,
                getWindowSize = getWindowSize
            ) { exitCode, isSignal ->
                Logger.lInfo("Game exited with code: $exitCode (signal: $isSignal)")
                if (exitCode == 0) {
                    onSuccess()
                } else {
                    onError("Game exited with code: $exitCode")
                }
            }

            val exitCode = launcher.launch()
            Logger.lInfo("Game launcher returned: $exitCode")
        } catch (e: Exception) {
            Logger.lError("Failed to run game", e)
            onError("Game launch failed: ${e.message}")
        }
    }
}