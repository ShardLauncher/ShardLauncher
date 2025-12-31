/*
 * Shard Launcher
 * Adapted from Zalith Launcher 2
 */

package com.lanrhyme.shardlauncher.game.version.download.game

import com.lanrhyme.shardlauncher.R
import com.lanrhyme.shardlauncher.coroutine.Task
import com.lanrhyme.shardlauncher.game.version.download.BaseMinecraftDownloader
import com.lanrhyme.shardlauncher.game.version.download.DownloadFailedException
import com.lanrhyme.shardlauncher.game.version.download.DownloadTask
import com.lanrhyme.shardlauncher.game.versioninfo.models.GameManifest
import com.lanrhyme.shardlauncher.utils.file.formatFileSize
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.coroutineScope
import kotlinx.coroutines.delay
import kotlinx.coroutines.ensureActive
import kotlinx.coroutines.isActive
import kotlinx.coroutines.joinAll
import kotlinx.coroutines.launch
import kotlinx.coroutines.sync.Semaphore
import kotlinx.coroutines.sync.withPermit
import kotlinx.coroutines.withContext
import java.io.File
import java.util.concurrent.ConcurrentHashMap
import java.util.concurrent.atomic.AtomicLong

/**
 * Multi-threaded downloader for game libraries and assets
 */
class GameLibDownloader(
    private val downloader: BaseMinecraftDownloader,
    private val gameManifest: GameManifest,
    private val maxDownloadThreads: Int = 64
) {
    // Progress counters
    private var downloadedFileSize: AtomicLong = AtomicLong(0)
    private var downloadedFileCount: AtomicLong = AtomicLong(0)
    private var totalFileSize: AtomicLong = AtomicLong(0)
    private var totalFileCount: AtomicLong = AtomicLong(0)

    private val allDownloadTasks = mutableListOf<DownloadTask>()
    private val downloadFailedTasks = mutableListOf<DownloadTask>()
    private val lastDownloadedSizes = ConcurrentHashMap<File, Long>()

    private var isDownloadStarted: Boolean = false

    /**
     * Schedule all libraries from manifest
     */
    fun schedule(targetDir: File = downloader.librariesTarget) {
        downloader.loadLibraryDownloads(gameManifest, targetDir) { urls, hash, targetFile, size, isDownloadable ->
            scheduleDownload(urls, hash, targetFile, size, isDownloadable)
        }
    }

    /**
     * Start multi-threaded download
     */
    suspend fun download(task: Task) {
        isDownloadStarted = true
        if (allDownloadTasks.isNotEmpty()) {
            downloadAll(task, allDownloadTasks, R.string.minecraft_download_downloading_game_files)
            if (downloadFailedTasks.isNotEmpty()) {
                downloadedFileCount.set(0)
                totalFileCount.set(downloadFailedTasks.size.toLong())
                // In retry, we might want to reset downloadedFileSize or handle it carefully.
                // For simplicity, we just reset file count progress.
                downloadAll(task, downloadFailedTasks.toList(), R.string.minecraft_download_progress_retry_downloading_files)
            }
            if (downloadFailedTasks.isNotEmpty()) throw DownloadFailedException()
        }

        task.updateProgress(1f, null)
    }

    private suspend fun downloadAll(
        task: Task,
        tasks: List<DownloadTask>,
        taskMessageRes: Int
    ) = withContext(Dispatchers.IO) {
        coroutineScope {
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
                    ensureActive()
                    val currentFileSize = downloadedFileSize.get()
                    val totalSize = totalFileSize.get().run { if (this < currentFileSize) currentFileSize else this }
                    
                    val progress = if (totalSize > 0) (currentFileSize.toFloat() / totalSize.toFloat()).coerceIn(0f, 1f) else 0f
                    
                    task.updateProgress(
                        progress,
                        taskMessageRes,
                        downloadedFileCount.get(), totalFileCount.get(),
                        formatFileSize(currentFileSize), formatFileSize(totalSize)
                    )
                    delay(100)
                }
            }

            try {
                downloadJobs.joinAll()
            } catch (e: CancellationException) {
                downloadJobs.forEach { it.cancel(e) } // Changed from it.cancel(CancellationException("Parent cancelled", e))
                throw e
            } finally {
                progressJob.cancel()
            }
        }
    }

    fun scheduleDownload(urls: List<String>, sha1: String?, targetFile: File, size: Long, isDownloadable: Boolean = true) {
        if (isDownloadStarted) throw IllegalStateException("Download already started")

        totalFileCount.incrementAndGet()
        totalFileSize.addAndGet(size)
        allDownloadTasks.add(
            DownloadTask(
                urls = urls,
                verifyIntegrity = true,
                targetFile = targetFile,
                sha1 = sha1,
                isDownloadable = isDownloadable,
                onDownloadFailed = { failedTask ->
                    downloadFailedTasks.add(failedTask)
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

    fun removeDownload(predicate: (DownloadTask) -> Boolean) {
        if (isDownloadStarted) throw IllegalStateException("Download already started")
        allDownloadTasks.removeAll(predicate)
    }
}
