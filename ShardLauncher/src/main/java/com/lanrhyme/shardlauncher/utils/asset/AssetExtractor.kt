/*
 * Shard Launcher
 * Adapted from Zalith Launcher 2
 */

package com.lanrhyme.shardlauncher.utils.asset

import android.content.Context
import com.lanrhyme.shardlauncher.utils.logging.Logger
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.io.File
import java.io.FileOutputStream
import java.io.IOException

/**
 * Asset extractor for extracting bundled resources from APK assets
 */
object AssetExtractor {
    
    /**
     * Extract asset file to target location
     */
    suspend fun extractAsset(
        context: Context,
        assetPath: String,
        targetFile: File,
        onProgress: (progress: Int, message: String) -> Unit = { _, _ -> }
    ): Result<File> = withContext(Dispatchers.IO) {
        try {
            onProgress(0, "开始提取 $assetPath...")
            
            // Ensure parent directory exists
            targetFile.parentFile?.mkdirs()
            
            context.assets.open(assetPath).use { inputStream ->
                FileOutputStream(targetFile).use { outputStream ->
                    val buffer = ByteArray(8192)
                    var bytesRead: Int
                    var totalBytes = 0L
                    
                    while (inputStream.read(buffer).also { bytesRead = it } != -1) {
                        outputStream.write(buffer, 0, bytesRead)
                        totalBytes += bytesRead
                        
                        // Update progress (we don't know total size, so just show bytes transferred)
                        val progress = minOf(90, (totalBytes / 1024).toInt()) // Rough progress
                        onProgress(progress, "已提取 ${formatSize(totalBytes)}")
                    }
                }
            }
            
            onProgress(100, "提取完成")
            Logger.i("AssetExtractor", "Successfully extracted asset: $assetPath to ${targetFile.absolutePath}")
            Result.success(targetFile)
            
        } catch (e: Exception) {
            Logger.e("AssetExtractor", "Failed to extract asset: $assetPath", e)
            Result.failure(e)
        }
    }

    /**
     * Extract directory recursively from assets
     */
    suspend fun extractAssetDirectory(
        context: Context,
        assetDirPath: String,
        targetDir: File,
        onProgress: (progress: Int, message: String) -> Unit = { _, _ -> }
    ): Result<File> = withContext(Dispatchers.IO) {
        try {
            onProgress(0, "开始提取目录 $assetDirPath...")
            
            val assetManager = context.assets
            val files = assetManager.list(assetDirPath) ?: emptyArray()
            
            if (files.isEmpty()) {
                // This is a file, not a directory
                return@withContext extractAsset(context, assetDirPath, targetDir, onProgress)
            }
            
            targetDir.mkdirs()
            var processedFiles = 0
            val totalFiles = files.size
            
            for (file in files) {
                val assetFilePath = if (assetDirPath.isEmpty()) file else "$assetDirPath/$file"
                val targetFile = File(targetDir, file)
                
                val subFiles = assetManager.list(assetFilePath)
                if (subFiles != null && subFiles.isNotEmpty()) {
                    // This is a subdirectory
                    extractAssetDirectory(context, assetFilePath, targetFile) { subProgress, message ->
                        val overallProgress = (processedFiles * 100 + subProgress) / totalFiles
                        onProgress(overallProgress, message)
                    }
                } else {
                    // This is a file
                    extractAsset(context, assetFilePath, targetFile) { subProgress, message ->
                        val overallProgress = (processedFiles * 100 + subProgress) / totalFiles
                        onProgress(overallProgress, message)
                    }
                }
                
                processedFiles++
            }
            
            onProgress(100, "目录提取完成")
            Logger.i("AssetExtractor", "Successfully extracted directory: $assetDirPath to ${targetDir.absolutePath}")
            Result.success(targetDir)
            
        } catch (e: Exception) {
            Logger.e("AssetExtractor", "Failed to extract directory: $assetDirPath", e)
            Result.failure(e)
        }
    }

    /**
     * Check if asset exists
     */
    fun assetExists(context: Context, assetPath: String): Boolean {
        return try {
            context.assets.open(assetPath).use { true }
        } catch (e: IOException) {
            false
        }
    }

    /**
     * List assets in directory
     */
    fun listAssets(context: Context, assetDirPath: String): List<String> {
        return try {
            context.assets.list(assetDirPath)?.toList() ?: emptyList()
        } catch (e: IOException) {
            Logger.e("AssetExtractor", "Failed to list assets in: $assetDirPath", e)
            emptyList()
        }
    }

    /**
     * Get asset size (approximate, since we can't get exact size without reading)
     */
    suspend fun getAssetSize(context: Context, assetPath: String): Long = withContext(Dispatchers.IO) {
        try {
            context.assets.open(assetPath).use { inputStream ->
                var size = 0L
                val buffer = ByteArray(8192)
                var bytesRead: Int
                while (inputStream.read(buffer).also { bytesRead = it } != -1) {
                    size += bytesRead
                }
                size
            }
        } catch (e: IOException) {
            Logger.e("AssetExtractor", "Failed to get asset size: $assetPath", e)
            0L
        }
    }

    /**
     * Format file size for display
     */
    private fun formatSize(bytes: Long): String {
        return when {
            bytes < 1024 -> "${bytes}B"
            bytes < 1024 * 1024 -> "${bytes / 1024}KB"
            bytes < 1024 * 1024 * 1024 -> "${bytes / (1024 * 1024)}MB"
            else -> "${bytes / (1024 * 1024 * 1024)}GB"
        }
    }
}