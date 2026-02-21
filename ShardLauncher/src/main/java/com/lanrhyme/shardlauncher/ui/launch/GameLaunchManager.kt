/*
 * Shard Launcher
 * Game launch manager using ZalithLauncherCore
 */

package com.lanrhyme.shardlauncher.ui.launch

import android.app.Activity
import androidx.compose.ui.unit.IntSize
import com.lanrhyme.shardlauncher.utils.Logger
import com.movtery.zalithlauncher.game.account.AccountsManager
import com.movtery.zalithlauncher.game.launch.GameLauncher
import com.movtery.zalithlauncher.game.version.installed.Version
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

object GameLaunchManager {
    
    suspend fun launchGame(
        activity: Activity,
        version: Version,
        getWindowSize: () -> IntSize,
        onExit: (code: Int, isSignal: Boolean) -> Unit
    ): Int = withContext(Dispatchers.IO) {
        try {
            val currentAccount = AccountsManager.currentAccountFlow.value
                ?: throw IllegalStateException("No account selected for launch")
            
            Logger.lInfo("Starting game launch for version: ${version.getVersionName()}")
            Logger.lInfo("Using account: ${currentAccount.username}")
            
            val launcher = GameLauncher(
                activity = activity,
                version = version,
                onExit = onExit
            )
            
            val exitCode = launcher.launch(screenSize = getWindowSize())
            
            Logger.lInfo("Game launch completed with exit code: $exitCode")
            exitCode
            
        } catch (e: Exception) {
            Logger.lError("Failed to launch game", e)
            -1
        }
    }
}
