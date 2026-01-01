/*
 * Shard Launcher
 * Test for launch functionality
 */

package com.lanrhyme.shardlauncher.game.launch

import android.app.Activity
import androidx.compose.ui.unit.IntSize
import com.lanrhyme.shardlauncher.game.account.Account
import com.lanrhyme.shardlauncher.game.account.AccountType
import com.lanrhyme.shardlauncher.game.account.isLocalAccount
import com.lanrhyme.shardlauncher.game.account.isMicrosoftAccount
import com.lanrhyme.shardlauncher.game.account.isAuthServerAccount
import com.lanrhyme.shardlauncher.game.version.installed.Version
import com.lanrhyme.shardlauncher.utils.logging.Logger

/**
 * Simple test to verify launch components work
 */
object LaunchTest {
    
    fun testGameLauncherCreation(activity: Activity, version: Version): Boolean {
        return try {
            val launcher = GameLauncher(
                activity = activity,
                version = version,
                getWindowSize = { IntSize(1280, 720) }
            ) { exitCode, isSignal ->
                Logger.lInfo("Test launcher exited with code: $exitCode")
            }
            Logger.lInfo("GameLauncher created successfully")
            true
        } catch (e: Exception) {
            Logger.lError("Failed to create GameLauncher", e)
            false
        }
    }
    
    fun testJvmLauncherCreation(activity: Activity): Boolean {
        return try {
            val launcher = JvmLauncher(
                context = activity,
                getWindowSize = { IntSize(1280, 720) },
                jvmLaunchInfo = JvmLaunchInfo(
                    jvmArgs = "-version",
                    userHome = null,
                    jreName = null
                )
            ) { exitCode, isSignal ->
                Logger.lInfo("Test JVM launcher exited with code: $exitCode")
            }
            Logger.lInfo("JvmLauncher created successfully")
            true
        } catch (e: Exception) {
            Logger.lError("Failed to create JvmLauncher", e)
            false
        }
    }
    
    fun testAccountExtensions(): Boolean {
        return try {
            val localAccount = Account(
                uniqueUUID = "test-uuid",
                username = "TestUser",
                profileId = "test-profile",
                accessToken = "test-token",
                accountType = AccountType.LOCAL,
                otherBaseUrl = null,
                xUid = null
            )
            
            val microsoftAccount = localAccount.copy(accountType = AccountType.MICROSOFT)
            val authServerAccount = localAccount.copy(accountType = AccountType.AUTHSERVER)
            
            val tests = listOf<Pair<Boolean, String>>(
                Pair(localAccount.isLocalAccount(), "Local account check"),
                Pair(microsoftAccount.isMicrosoftAccount(), "Microsoft account check"),
                Pair(authServerAccount.isAuthServerAccount(), "Auth server account check"),
                Pair(!localAccount.isMicrosoftAccount(), "Local account is not Microsoft"),
                Pair(!microsoftAccount.isLocalAccount(), "Microsoft account is not local")
            )
            
            tests.all { (result, description) ->
                if (!result) {
                    Logger.lError("Failed test: $description")
                    false
                } else {
                    Logger.lInfo("Passed test: $description")
                    true
                }
            }
        } catch (e: Exception) {
            Logger.lError("Failed account extensions test", e)
            false
        }
    }
    
    fun runAllTests(activity: Activity, version: Version): Boolean {
        Logger.lInfo("Starting launch system tests...")
        
        val results = listOf<Pair<Boolean, String>>(
            Pair(testAccountExtensions(), "Account Extensions"),
            Pair(testGameLauncherCreation(activity, version), "GameLauncher Creation"),
            Pair(testJvmLauncherCreation(activity), "JvmLauncher Creation")
        )
        
        val passed = results.count { it.first }
        val total = results.size
        
        Logger.lInfo("Launch system tests completed: $passed/$total passed")
        
        results.forEach { (result, name) ->
            val status = if (result) "PASS" else "FAIL"
            Logger.lInfo("[$status] $name")
        }
        
        return passed == total
    }
}