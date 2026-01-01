/*
 * Shard Launcher
 * Adapted from Zalith Launcher 2
 */

package com.lanrhyme.shardlauncher.game.launch

import android.content.Context
import androidx.compose.ui.unit.IntSize
import com.lanrhyme.shardlauncher.bridge.LoggerBridge
import com.lanrhyme.shardlauncher.game.multirt.RuntimesManager
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

    override suspend fun launch(): Int {
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

        val windowSize = getWindowSize()
        val argList: MutableList<String> = ArrayList(
            getCacioJavaArgs(windowSize.width, windowSize.height, runtime.javaVersion == 8)
        ).apply {
            addAll(args)
        }

        Logger.lInfo("==================== Launch JVM ====================")
        Logger.lInfo("Info: Java arguments: \r\n${argList.joinToString("\r\n")}")

        return Pair(runtime, argList)
    }

    private fun getCacioJavaArgs(windowWidth: Int, windowHeight: Int, isJava8: Boolean): List<String> {
        val args = mutableListOf<String>()
        
        args.add("-Djava.awt.headless=false")
        args.add("-Dawt.toolkit=net.java.openjdk.cacio.ctk.CToolkit")
        args.add("-Djava.awt.graphicsenv=net.java.openjdk.cacio.ctk.CGraphicsEnvironment")
        args.add("-Dglfwstub.windowWidth=$windowWidth")
        args.add("-Dglfwstub.windowHeight=$windowHeight")
        args.add("-Dglfwstub.initEgl=false")
        
        if (!isJava8) {
            args.add("--add-exports=java.desktop/sun.awt=ALL-UNNAMED")
            args.add("--add-exports=java.desktop/sun.awt.image=ALL-UNNAMED")
            args.add("--add-exports=java.desktop/sun.java2d=ALL-UNNAMED")
            args.add("--add-exports=java.desktop/java.awt.peer=ALL-UNNAMED")
            
            if (runtime.javaVersion >= 17) {
                args.add("-javaagent:${PathManager.DIR_COMPONENTS}/cacio-17/cacio-agent.jar")
            }
        }
        
        val cacioJarDir = if (runtime.javaVersion >= 17) {
            File(PathManager.DIR_COMPONENTS, "cacio-17")
        } else {
            File(PathManager.DIR_COMPONENTS, "cacio-8")
        }
        args.add("-Xbootclasspath/a:${File(cacioJarDir, "cacio-ttc.jar").absolutePath}")
        
        return args
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