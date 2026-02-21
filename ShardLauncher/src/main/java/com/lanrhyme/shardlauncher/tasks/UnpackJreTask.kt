/*
 * Shard Launcher
 * Adapted from Zalith Launcher 2
 */

package com.lanrhyme.shardlauncher.tasks

import android.content.Context
import android.content.res.AssetManager
import com.lanrhyme.shardlauncher.components.jre.Jre
import com.lanrhyme.shardlauncher.game.multirt.RuntimesManager
import com.lanrhyme.shardlauncher.utils.device.Architecture
import com.lanrhyme.shardlauncher.utils.file.readString
import com.lanrhyme.shardlauncher.utils.logging.Logger

class UnpackJreTask(
    private val context: Context,
    private val jre: Jre
) : AbstractUnpackTask() {
    private lateinit var assetManager: AssetManager
    private lateinit var launcherRuntimeVersion: String
    private var isCheckFailed: Boolean = false

    init {
        runCatching {
            assetManager = context.assets
            launcherRuntimeVersion = assetManager.open(jre.jrePath + "/version").use { it.readString() }
        }.getOrElse {
            isCheckFailed = true
        }
    }

    fun isCheckFailed() = isCheckFailed

    override fun isNeedUnpack(): Boolean {
        if (isCheckFailed) return false

        return runCatching {
            val installedRuntimeVersion = RuntimesManager.loadInternalRuntimeVersion(jre.jreName)
            return launcherRuntimeVersion != installedRuntimeVersion
        }.onFailure { e ->
            Logger.lError("An exception occurred while detecting the Java Runtime.", e)
        }.getOrElse { false }
    }

    override suspend fun run() {
        runCatching {
            taskMessage = "Unpacking ${jre.jreName}..."
            
            val archString = Architecture.archAsString(Architecture.getDeviceArchitecture())
            
            RuntimesManager.installRuntimeBinPack(
                universalFileInputStream = assetManager.open(jre.jrePath + "/universal.tar.xz"),
                platformBinsInputStream = assetManager.open(
                    jre.jrePath + "/bin-" + archString + ".tar.xz"
                ),
                name = jre.jreName,
                binPackVersion = launcherRuntimeVersion,
                updateProgress = { _, args ->
                    if (args.isNotEmpty() && args[0] is String) {
                        taskMessage = "Unpacking ${jre.jreName}: ${args[0]}"
                    }
                }
            )
            RuntimesManager.postPrepare(jre.jreName)
            
            Logger.lInfo("${jre.jreName} unpacked successfully")
        }.onFailure {
            Logger.lError("Internal JRE unpack failed", it)
        }.getOrThrow()
    }
}
