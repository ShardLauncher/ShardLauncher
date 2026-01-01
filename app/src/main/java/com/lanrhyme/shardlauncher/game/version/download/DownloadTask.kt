package com.lanrhyme.shardlauncher.game.version.download

import com.lanrhyme.shardlauncher.utils.file.compareSHA1
import com.lanrhyme.shardlauncher.utils.logging.Logger.lError
import com.lanrhyme.shardlauncher.utils.network.downloadFromMirrorList
import kotlinx.coroutines.CancellationException
import org.apache.commons.io.FileUtils
import java.io.File
import java.io.FileNotFoundException
import java.io.IOException

class DownloadTask(
    val urls: List<String>,
    private val verifyIntegrity: Boolean,
    private val bufferSize: Int = 32768,
    val targetFile: File,
    val sha1: String?,
    /** 是否本身是可以被下载的，如果不可下载，则通过提供url尝试下载，如果失败则抛出 FileNotFoundException */
    val isDownloadable: Boolean,
    private val onDownloadFailed: (DownloadTask) -> Unit = {},
    private val onFileDownloadedSize: (Long) -> Unit = {},
    private val onFileDownloaded: () -> Unit = {},
    var fileDownloadedTask: (() -> Unit)? = null
) {
    fun download() {
        //若目标文件存在，验证通过或关闭完整性验证时，跳过此次下载
        if (verifySha1()) {
            downloadedSize(FileUtils.sizeOf(targetFile))
            downloadedFile()
            return
        }

        try {
            val success = downloadFromMirrorList(
                urls = urls,
                sha1 = sha1,
                outputFile = targetFile,
                bufferSize = bufferSize,
                onProgress = { size ->
                    downloadedSize(size)
                }
            )
            if (!success) {
                throw IOException("Download failed from all mirrors")
            }
            downloadedFile()
        } catch (e: CancellationException) {
            throw e // Re-throw cancellation
        } catch (e: Exception) {
            lError("Download failed: ${targetFile.absolutePath}\nurls: ${urls.joinToString("\n")}", e)
            if (!isDownloadable && e is FileNotFoundException) throw e
            onDownloadFailed(this)
        }
    }

    private fun downloadedSize(size: Long) {
        onFileDownloadedSize(size)
    }

    private fun downloadedFile() {
        onFileDownloaded()
        fileDownloadedTask?.invoke()
    }

    /**
     * 若目标文件存在，验证完整性
     * @return 是否跳过此次下载
     */
    private fun verifySha1(): Boolean {
        if (targetFile.exists()) {
            sha1 ?: return true //sha1 不存在，可能目标无法被下载
            if (!verifyIntegrity || compareSHA1(targetFile, sha1)) {
                return true
            } else {
                FileUtils.deleteQuietly(targetFile)
            }
        }
        return false
    }
}
