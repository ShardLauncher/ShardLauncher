/*
 * Shard Launcher
 */

package com.lanrhyme.shardlauncher.components

import android.content.Context
import android.content.res.AssetManager
import com.lanrhyme.shardlauncher.utils.copyAssetFile
import com.lanrhyme.shardlauncher.utils.file.readString
import com.lanrhyme.shardlauncher.utils.logging.Logger
import java.io.File

abstract class UnpackSingleTask(
    val context: Context,
    val rootDir: File,
    val assetsDirName: String,
    val fileDirName: String,
) : AbstractUnpackTask() {
    private lateinit var am: AssetManager
    private lateinit var versionFile: File
    private var isCheckFailed: Boolean = false

    init {
        runCatching {
            am = context.assets
            versionFile = File(rootDir, "$fileDirName/version")
        }.onFailure {
            isCheckFailed = true
        }
    }

    fun isCheckFailed() = isCheckFailed

    override fun isNeedUnpack(): Boolean {
        if (isCheckFailed) return false

        if (!versionFile.exists()) {
            requestEmptyParentDir(versionFile)
            Logger.lInfo("$fileDirName: Pack was installed manually, or does not exist...")
            return true
        } else {
            return runCatching {
                val release1 = am.open("$assetsDirName/$fileDirName/version").use { it.readString() }
                val release2 = versionFile.inputStream().use { it.readString() }
                if (release1 != release2) {
                    requestEmptyParentDir(versionFile)
                    true
                } else {
                    Logger.lInfo("$fileDirName: Pack is up-to-date with the launcher, continuing...")
                    false
                }
            }.getOrElse {
                requestEmptyParentDir(versionFile)
                true
            }
        }
    }

    override suspend fun run() {
        val dir = File(rootDir, fileDirName)
        dir.deleteRecursively()
        dir.mkdirs()

        val fileList = am.list("$assetsDirName/$fileDirName")
        if (fileList != null) {
            for (fileName in fileList) {
                val file = File(dir, fileName)
                context.copyAssetFile("$assetsDirName/$fileDirName/$fileName", file, true)
                moreProgress(file)
            }
        }
    }

    /**
     * Perform more operations on the unpacked file
     */
    open suspend fun moreProgress(file: File) {}

    private fun requestEmptyParentDir(file: File) {
        val parent = file.parentFile
        if (parent != null) {
            if (parent.exists() && parent.isDirectory) {
                parent.deleteRecursively()
            }
            parent.mkdirs()
        }
    }
}
