/*
 * Shard Launcher
 * Adapted from Zalith Launcher 2
 */

package com.lanrhyme.shardlauncher.game.launch

import android.content.Context
import androidx.compose.ui.unit.IntSize
import com.lanrhyme.shardlauncher.bridge.LoggerBridge
import com.lanrhyme.shardlauncher.game.multirt.RuntimesManager
import com.lanrhyme.shardlauncher.path.LibPath
import com.lanrhyme.shardlauncher.path.PathManager
import com.lanrhyme.shardlauncher.settings.AllSettings
import com.lanrhyme.shardlauncher.utils.logging.Logger
import com.lanrhyme.shardlauncher.utils.string.splitPreservingQuotes
import java.io.File

data class JvmLaunchInfo(
    val jvmArgs: String,
    val userHome: String? = null,
    val jreName: String? = null
)

open class JvmLauncher(
    private val context: Context,
    private val getWindowSize: () -> IntSize,
    private val jvmLaunchInfo: JvmLaunchInfo,
    onExit: (code: Int, isSignal: Boolean) -> Unit
) : Launcher(onExit) {

    private var currentScreenSize: IntSize = IntSize(0, 0)

    override fun getLaunchScreenSize(): IntSize {
        return currentScreenSize
    }

    override suspend fun launch(): Int {
        currentScreenSize = getWindowSize()
        generateLauncherProfiles(jvmLaunchInfo.userHome ?: PathManager.DIR_FILES_PRIVATE.absolutePath)
        val (runtime, argList) = getStartupNeeded()

        this.runtime = runtime

        return launchJvm(
            context = context,
            jvmArgs = argList,
            userArgs = AllSettings.jvmArgs.getValue(),
            getWindowSize = getWindowSize
        )
    }

    override fun chdir(): String {
        return PathManager.DIR_FILES_PRIVATE.absolutePath
    }

    override fun getLogName(): String = "jvm_${System.currentTimeMillis()}"

    override fun exit() {
        // JVM launcher specific cleanup
    }

    private fun getStartupNeeded(): Pair<com.lanrhyme.shardlauncher.game.multirt.Runtime, List<String>> {
        val args = jvmLaunchInfo.jvmArgs.splitPreservingQuotes()

        val runtime = jvmLaunchInfo.jreName?.let { jreName ->
            RuntimesManager.forceReload(jreName)
        } ?: run {
            RuntimesManager.forceReload(AllSettings.javaRuntime.getValue())
        }

        val argList: MutableList<String> = ArrayList(
            getCacioJavaArgs(currentScreenSize, runtime.javaVersion == 8)
        ).apply {
            addAll(args)
        }

        Logger.lInfo("==================== Launch JVM ====================")
        Logger.lInfo("Info: Java arguments: \r\n${argList.joinToString("\r\n")}")

        return Pair(runtime, argList)
    }

    private fun generateLauncherProfiles(userHome: String) {
        val launcherProfiles = File(userHome, "launcher_profiles.json")
        if (!launcherProfiles.exists()) {
            runCatching {
                launcherProfiles.writeText("""
                    {
                        "profiles": {},
                        "settings": {
                            "enableSnapshots": false,
                            "enableAdvanced": false,
                            "keepLauncherOpen": false,
                            "showGameLog": false,
                            "showMenu": false,
                            "soundOn": false
                        },
                        "version": 3
                    }
                """.trimIndent())
            }.onFailure {
                Logger.lWarning("Failed to create launcher_profiles.json", it)
            }
        }
    }
}