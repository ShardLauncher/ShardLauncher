/*
 * Shard Launcher
 * Adapted from Zalith Launcher 2
 */

package com.lanrhyme.shardlauncher.game.launch

import android.app.Activity
import androidx.compose.ui.unit.IntSize
import com.lanrhyme.shardlauncher.bridge.LoggerBridge
import com.lanrhyme.shardlauncher.game.account.Account
import com.lanrhyme.shardlauncher.game.account.AccountsManager
import com.lanrhyme.shardlauncher.game.plugin.driver.DriverPluginManager
import com.lanrhyme.shardlauncher.game.plugin.renderer.RendererPluginManager
import com.lanrhyme.shardlauncher.game.version.installed.Version
import com.lanrhyme.shardlauncher.utils.GSON
import com.lanrhyme.shardlauncher.utils.logging.Logger
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

/**
 * Manages the game launch process
 */
object GameLaunchManager {
    
    /**
     * Launch Minecraft with the specified version and account
     */
    suspend fun launchGame(
        activity: Activity,
        version: Version,
        account: Account? = null,
        getWindowSize: () -> IntSize = { IntSize(1280, 720) },
        onExit: (code: Int, isSignal: Boolean) -> Unit = { _, _ -> }
    ): Int = withContext(Dispatchers.IO) {
        
        val exitCodeResult = try {
            // Initialize plugins
            initializePlugins(activity)
            
            // Use provided account or current account
            val launchAccount = account ?: AccountsManager.currentAccountFlow.value
                ?: throw IllegalStateException("No account selected for launch")
            
            // Validate version
            if (!version.isValid()) {
                throw IllegalStateException("Invalid version: ${version.getVersionName()}")
            }
            
            Logger.lInfo("Starting game launch for version: ${version.getVersionName()}")
            Logger.lInfo("Using account: ${launchAccount.username}")
            
            // Skip native logging initialization for now to avoid crashes
            // TODO: Initialize native logging properly after game launch system is stable
            
            // Create and launch game
            val launcher = LauncherFactory.createGameLauncher(
                activity = activity,
                version = version,
                account = launchAccount,
                getWindowSize = getWindowSize,
                onExit = onExit
            )
            
            val exitCode = launcher.launch()
            
            Logger.lInfo("Game launch completed with exit code: $exitCode")
            exitCode
            
        } catch (e: Exception) {
            Logger.lError("Failed to launch game", e)
            -1
        }

        exitCodeResult as Int
    }
    
    /**
     * Initialize plugins and managers
     */
    private fun initializePlugins(activity: Activity) {
        try {
            // Initialize driver plugins
            DriverPluginManager.initDriver(activity, reset = false)
            
            // Initialize renderer plugins
            RendererPluginManager.initializePlugins(activity)
            
            Logger.lInfo("Plugins initialized successfully")
        } catch (e: Exception) {
            Logger.lWarning("Failed to initialize some plugins", e)
        }
    }
    
    /**
     * Check if game can be launched
     */
    fun canLaunchGame(version: Version, account: Account? = null): Boolean {
        return try {
            // Check version validity
            if (!version.isValid()) {
                Logger.lWarning("Version is invalid: ${version.getVersionName()}")
                return false
            }
            
            // Check account
            val launchAccount = account ?: AccountsManager.currentAccountFlow.value
            if (launchAccount == null) {
                Logger.lWarning("No account available for launch")
                return false
            }
            
            // Check if client jar exists
            if (!version.getClientJar().exists()) {
                Logger.lWarning("Client jar not found: ${version.getClientJar().absolutePath}")
                return false
            }
            
            true
        } catch (e: Exception) {
            Logger.lError("Error checking launch readiness", e)
            false
        }
    }
}