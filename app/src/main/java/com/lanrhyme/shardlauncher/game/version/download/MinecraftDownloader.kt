package com.lanrhyme.shardlauncher.game.version.download

import com.lanrhyme.shardlauncher.utils.GSON
import android.content.Context
import com.lanrhyme.shardlauncher.R
import com.lanrhyme.shardlauncher.coroutine.Task
import com.lanrhyme.shardlauncher.game.versioninfo.models.GameManifest
import com.lanrhyme.shardlauncher.utils.file.formatFileSize
import com.lanrhyme.shardlauncher.utils.logging.Logger.lError
import com.lanrhyme.shardlauncher.utils.logging.Logger.lInfo
import com.lanrhyme.shardlauncher.utils.string.getMessageOrToString
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.cancel
import kotlinx.coroutines.coroutineScope
import kotlinx.coroutines.delay
import kotlinx.coroutines.ensureActive
import kotlinx.coroutines.isActive
import kotlinx.coroutines.joinAll
import kotlinx.coroutines.launch
import kotlinx.coroutines.sync.Semaphore
import kotlinx.coroutines.sync.withPermit
import java.io.File
import java.io.FileNotFoundException
import java.io.IOException
import java.util.concurrent.atomic.AtomicLong

class MinecraftDownloader(
    private val context: Context,
    private val version: String,
    private val customName: String = version,
    private val verifyIntegrity: Boolean,
    private val downloader: BaseMinecraftDownloader = BaseMinecraftDownloader(verifyIntegrity = verifyIntegrity),
    private val mode: DownloadMode = DownloadMode.DOWNLOAD,
    private val onCompletion: () -> Unit = {},
    private val onError: (message: String) -> Unit = {},
    private val maxDownloadThreads: Int = 64
) {
    //已下载文件计数器
    private var downloadedFileSize: AtomicLong = AtomicLong(0)
    private var downloadedFileCount: AtomicLong = AtomicLong(0)
    private var totalFileSize: AtomicLong = AtomicLong(0)
    private var totalFileCount: AtomicLong = AtomicLong(0)

    private var allDownloadTasks = mutableListOf<DownloadTask>()
    private var downloadFailedTasks = mutableListOf<DownloadTask>()

    private fun getTaskMessage(download: Int, verify: Int): Int =
        when (mode) {
            DownloadMode.DOWNLOAD -> download
            DownloadMode.VERIFY_AND_REPAIR -> verify
        }

    /**
     * 自定义 client 目录 ->client<-\/versions\/..
     */
    fun getDownloadTask(
        clientName: String = this.customName,
        clientVersionsDir: File = downloader.versionsTarget
    ): Task {
        return Task.runTask(
            id = DOWNLOADER_TAG,
            dispatcher = Dispatchers.Default,
            task = { task ->
                task.updateProgress(-1f, getTaskMessage(R.string.minecraft_download_stat_download_task, R.string.minecraft_download_stat_verify_task))
                if (mode == DownloadMode.DOWNLOAD) {
                    progressNewDownloadTasks(clientName, clientVersionsDir)
                } else {
                    val jsonFile = downloader.getVersionJsonPath(customName).takeIf { it.canRead() } ?: throw IOException("Version $customName JSON file is unreadable.")
                    val jsonText = jsonFile.readText()
                    val gameManifest = GSON.fromJson(jsonText, GameManifest::class.java)
                    progressDownloadTasks(gameManifest, clientName)
                }

                if (allDownloadTasks.isNotEmpty()) {
                    downloadAll(task, allDownloadTasks, getTaskMessage(R.string.minecraft_download_downloading_game_files, R.string.minecraft_download_verifying_and_repairing_files))
                    if (downloadFailedTasks.isNotEmpty()) {
                        downloadedFileCount.set(0)
                        totalFileCount.set(downloadFailedTasks.size.toLong())
                        downloadAll(task, downloadFailedTasks.toList(), getTaskMessage(R.string.minecraft_download_progress_retry_downloading_files, R.string.minecraft_download_progress_retry_verifying_files))
                    }
                    if (downloadFailedTasks.isNotEmpty()) throw DownloadFailedException()
                }
                //清除任务信息
                task.updateProgress(1f, null)

                onCompletion()
            },
            onError = { e ->
                lError("Failed to download Minecraft!", e)
                val message = when(e) {
                    is CancellationException -> return@runTask
                    is FileNotFoundException -> context.getString(R.string.minecraft_download_failed_notfound)
                    is DownloadFailedException -> {
                        val failedUrls = downloadFailedTasks.map { it.urls.joinToString(", ") }
                        "${ context.getString(R.string.minecraft_download_failed_retried) }\r\n${ failedUrls.joinToString("\r\n") }"
                    }
                    else -> e.getMessageOrToString()
                }
                onError(message)
            }
        )
    }

    private suspend fun downloadAll(
        task: Task,
        tasks: List<DownloadTask>,
        taskMessageRes: Int
    ) = coroutineScope {
        downloadFailedTasks.clear()

        val semaphore = Semaphore(maxDownloadThreads)

        val downloadJobs = tasks.map { downloadTask ->
            launch {
                semaphore.withPermit {
                    downloadTask.download()
                }
            }
        }

        val progressJob = launch(Dispatchers.Main) {
            while (isActive) {
                try {
                    ensureActive()
                    val currentFileSize = downloadedFileSize.get()
                    val totalFileSize = totalFileSize.get().run { if (this < currentFileSize) currentFileSize else this }
                    task.updateProgress(
                        (currentFileSize.toFloat() / totalFileSize.toFloat()).coerceIn(0f, 1f),
                        taskMessageRes,
                        downloadedFileCount.get(), totalFileCount.get(), //文件个数
                        formatFileSize(currentFileSize), formatFileSize(totalFileSize) //文件大小
                    )
                    delay(100)
                } catch (_: CancellationException) {
                    break //取消
                }
            }
        }

        try {
            downloadJobs.joinAll()
        } catch (e: CancellationException) {
            downloadJobs.forEach { it.cancel("Parent cancelled", e) }
        } finally {
            progressJob.cancel()
        }
    }

    /**
     * 仅将 Jar、Json 文件安装到自定义版本目录中
     */
    private val lastDownloadedSizes = mutableMapOf<File, Long>()

    private suspend fun progressNewDownloadTasks(
        clientName: String,
        clientVersionsDir: File
    ) {
        val gameManifest = downloader.findVersion(this.version)?.let {
            downloader.createVersionJson(it, clientName, clientVersionsDir)
        } ?: throw IllegalArgumentException("Version not found: $version")

        commonScheduleDownloads(gameManifest, null, clientName, clientVersionsDir)
    }

    private suspend fun progressDownloadTasks(
        gameManifest: GameManifest,
        clientName: String,
        clientVersionsDir: File = downloader.versionsTarget
    ) {
        val inheritsFrom = if (gameManifest.inheritsFrom != null) {
            downloader.findVersion(gameManifest.inheritsFrom!!)
        } else null

        if (inheritsFrom != null) {
            downloader.createVersionJson(inheritsFrom)
        }

        commonScheduleDownloads(gameManifest, inheritsFrom, clientName, clientVersionsDir)
    }

    private suspend fun commonScheduleDownloads(
        gameManifest: GameManifest,
        inheritsFrom: com.lanrhyme.shardlauncher.game.versioninfo.models.VersionManifest.Version? = null,
        clientName: String,
        clientVersionsDir: File
    ) {
        val assetsIndex = downloader.createAssetIndex(downloader.assetIndexTarget, gameManifest)

        downloader.loadClientJarDownload(
            gameManifest, 
            clientName, 
            clientVersionsDir,
            scheduleDownload = { urls, hash, targetFile, size ->
                scheduleDownload(urls, hash, targetFile, size)
            },
            scheduleCopy = { targetFile ->
                inheritsFrom?.let { inherits ->
                    val inheritsJar = downloader.getVersionJarPath(inherits.id)
                    // Find the task that downloads/verifies the inheritsJar
                    allDownloadTasks.find { it.targetFile.absolutePath == inheritsJar.absolutePath }?.let { task ->
                        task.fileDownloadedTask = {
                            if (!targetFile.exists() && inheritsJar.exists()) {
                                try {
                                    inheritsJar.copyTo(targetFile, overwrite = true)
                                    com.lanrhyme.shardlauncher.utils.logging.Logger.lInfo("Copied ${inheritsJar.absolutePath} to ${targetFile.absolutePath}")
                                } catch (e: Exception) {
                                    com.lanrhyme.shardlauncher.utils.logging.Logger.lError("Failed to copy inherited jar", e)
                                }
                            }
                        }
                    }
                }
            }
        )
        downloader.loadAssetsDownload(assetsIndex) { urls, hash, targetFile, size ->
            scheduleDownload(urls, hash, targetFile, size)
        }
        downloader.loadLibraryDownloads(gameManifest) { urls, hash, targetFile, size, isDownloadable ->
            scheduleDownload(urls, hash, targetFile, size, isDownloadable)
        }
    }

    /**
     * 提交计划下载
     */
    private fun scheduleDownload(urls: List<String>, sha1: String?, targetFile: File, size: Long, isDownloadable: Boolean = true) {
        totalFileCount.incrementAndGet()
        totalFileSize.addAndGet(size)
        allDownloadTasks.add(
            DownloadTask(
                urls = urls,
                verifyIntegrity = verifyIntegrity,
                targetFile = targetFile,
                sha1 = sha1,
                isDownloadable = isDownloadable,
                onDownloadFailed = { task ->
                    downloadFailedTasks.add(task)
                },
                onFileDownloadedSize = { currentSize ->
                    val lastSize = lastDownloadedSizes[targetFile] ?: 0L
                    val delta = currentSize - lastSize
                    downloadedFileSize.addAndGet(delta)
                    lastDownloadedSizes[targetFile] = currentSize
                },
                onFileDownloaded = {
                    downloadedFileCount.incrementAndGet()
                }
            )
        )
    }
}
