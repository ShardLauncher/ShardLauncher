package com.lanrhyme.shardlauncher.game.version.download

import com.lanrhyme.shardlauncher.coroutine.Task
import com.lanrhyme.shardlauncher.game.versioninfo.models.GameManifest
import com.lanrhyme.shardlauncher.utils.logging.Logger
import com.lanrhyme.shardlauncher.utils.network.downloadFromMirrorListSuspend
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.sync.Semaphore
import kotlinx.coroutines.sync.withPermit
import kotlinx.coroutines.withContext
import java.io.File
import java.util.concurrent.atomic.AtomicLong

/**
 * 游戏库文件多线程下载器
 * 参考 ZL2 的实现，支持多线程并发下载库文件
 */
class GameLibDownloader(
    private val downloader: BaseMinecraftDownloader,
    private val gameManifest: GameManifest,
    private val maxDownloadThreads: Int = 64
) {
    // 原子计数器，线程安全
    private val downloadedFileSize = AtomicLong(0)
    private val downloadedFileCount = AtomicLong(0)
    
    // 信号量控制并发
    private val semaphore = Semaphore(maxDownloadThreads)
    
    /**
     * 下载所有库文件
     */
    suspend fun download(task: Task) {
        withContext(Dispatchers.IO) {
            // 收集所有下载任务
            val downloadTasks = mutableListOf<LibraryDownloadTask>()
            
            downloader.loadLibraryDownloads(gameManifest, downloader.librariesTarget) { 
                urls, hash, targetFile, size, isDownloadable ->
                if (isDownloadable) {
                    downloadTasks.add(
                        LibraryDownloadTask(
                            urls = urls,
                            hash = hash,
                            targetFile = targetFile,
                            size = size,
                            verifyIntegrity = downloader.verifyIntegrity
                        )
                    )
                }
            }
            
            val totalFiles = downloadTasks.size
            val totalSize = downloadTasks.sumOf { it.size }
            
            Logger.lInfo("Starting download of $totalFiles libraries (${"%.2f".format(totalSize / 1024.0 / 1024.0)} MB)")
            Logger.lInfo("Max download threads: $maxDownloadThreads")
            
            // 并发下载所有库文件
            val downloadJobs = downloadTasks.map { downloadTask ->
                launch {
                    semaphore.withPermit {
                        try {
                            Logger.lDebug("Starting download: ${downloadTask.targetFile.name} (${"%.2f".format(downloadTask.size / 1024.0 / 1024.0)} MB)")
                            
                            // 下载文件
                            downloadFromMirrorListSuspend(
                                urls = downloadTask.urls,
                                targetFile = downloadTask.targetFile,
                                sha1 = downloadTask.hash,
                                size = downloadTask.size,
                                verifyIntegrity = downloadTask.verifyIntegrity
                            )
                            
                            // 更新计数器
                            val count = downloadedFileCount.incrementAndGet()
                            val sizeDownloaded = downloadedFileSize.addAndGet(downloadTask.size)
                            
                            // 更新进度
                            val progress = count.toFloat() / totalFiles
                            task.updateProgress(progress)
                            
                            Logger.lDebug("Downloaded library: ${downloadTask.targetFile.name} ($count/$totalFiles, ${"%.2f".format(sizeDownloaded / 1024.0 / 1024.0)}/${"%.2f".format(totalSize / 1024.0 / 1024.0)} MB)")
                        } catch (e: Exception) {
                            Logger.lError("Failed to download library: ${downloadTask.targetFile.name} - ${e.message}", e)
                            throw e
                        }
                    }
                }
            }
            
            // 等待所有下载完成
            downloadJobs.forEach { it.join() }
            
            Logger.lInfo("Successfully downloaded all $totalFiles libraries (${"%.2f".format(downloadedFileSize.get() / 1024.0 / 1024.0)} MB)")
        }
    }
    
    /**
     * 库文件下载任务
     */
    private data class LibraryDownloadTask(
        val urls: List<String>,
        val hash: String?,
        val targetFile: File,
        val size: Long,
        val verifyIntegrity: Boolean
    )
}