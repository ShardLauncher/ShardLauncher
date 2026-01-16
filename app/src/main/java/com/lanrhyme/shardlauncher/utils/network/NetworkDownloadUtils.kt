/*
 * Shard Launcher
 * Network download utilities
 */

package com.lanrhyme.shardlauncher.utils.network

import com.lanrhyme.shardlauncher.utils.logging.Logger
import kotlinx.coroutines.delay
import java.io.File
import java.io.IOException
import java.net.HttpURLConnection
import java.net.URL

/**
 * Download and parse JSON from URL
 */
fun <T> downloadAndParseJson(
    url: String,
    targetFile: File? = null,
    expectedSHA: String? = null,
    verifyIntegrity: Boolean = false,
    classOfT: Class<T>
): T? {
    return try {
        val jsonString = fetchStringFromUrl(url)
        targetFile?.let { file ->
            file.parentFile?.mkdirs()
            file.writeText(jsonString)
        }
        com.lanrhyme.shardlauncher.utils.GSON.fromJson(jsonString, classOfT)
    } catch (e: Exception) {
        Logger.lError("Failed to download and parse JSON from $url", e)
        null
    }
}

/**
 * Fetch string content from URL
 */
fun fetchStringFromUrl(url: String): String {
    return try {
        val connection = URL(url).openConnection() as HttpURLConnection
        connection.requestMethod = "GET"
        connection.connectTimeout = 10000
        connection.readTimeout = 30000
        
        if (connection.responseCode == HttpURLConnection.HTTP_OK) {
            connection.inputStream.bufferedReader().use { it.readText() }
        } else {
            throw IOException("HTTP ${connection.responseCode}: ${connection.responseMessage}")
        }
    } catch (e: Exception) {
        Logger.lError("Failed to fetch from URL: $url", e)
        throw e
    }
}

/**
 * Fetch string from multiple URLs (try each until one succeeds)
 */
fun fetchStringFromUrls(urls: List<String>): String {
    var lastException: Exception? = null
    
    for (url in urls) {
        try {
            return fetchStringFromUrl(url)
        } catch (e: Exception) {
            lastException = e
            Logger.lWarning("Failed to fetch from $url, trying next...")
        }
    }
    
    throw lastException ?: IOException("All URLs failed")
}

/**
 * Download from mirror list with fallback
 */
fun downloadFromMirrorList(
    urls: List<String>,
    sha1: String? = null,
    outputFile: File,
    bufferSize: Int = 32768,
    onProgress: (Long) -> Unit = { }
): Boolean {
    var lastException: Exception? = null
    
    for (url in urls) {
        try {
            val connection = URL(url).openConnection() as HttpURLConnection
            connection.requestMethod = "GET"
            connection.connectTimeout = 10000
            connection.readTimeout = 60000
            
            if (connection.responseCode == HttpURLConnection.HTTP_OK) {
                outputFile.parentFile?.mkdirs()
                connection.inputStream.use { input ->
                    outputFile.outputStream().use { output ->
                        val buffer = ByteArray(bufferSize)
                        var totalBytes = 0L
                        var bytesRead: Int
                        
                        while (input.read(buffer).also { bytesRead = it } != -1) {
                            output.write(buffer, 0, bytesRead)
                            totalBytes += bytesRead
                            onProgress(totalBytes)
                        }
                    }
                }
                
                // Verify SHA1 if provided
                sha1?.let { expectedSha1 ->
                    if (!com.lanrhyme.shardlauncher.utils.file.compareSHA1(outputFile, expectedSha1)) {
                        outputFile.delete()
                        throw IOException("SHA1 verification failed")
                    }
                }
                
                return true
            } else {
                throw IOException("HTTP ${connection.responseCode}: ${connection.responseMessage}")
            }
        } catch (e: Exception) {
            lastException = e
            Logger.lWarning("Failed to download from $url, trying next...")
        }
    }
    
    Logger.lError("All mirrors failed", lastException)
    return false
}

/**
 * Download bytes from URL
 */
suspend fun downloadFromUrl(url: String): ByteArray {
    return try {
        val connection = URL(url).openConnection() as HttpURLConnection
        connection.requestMethod = "GET"
        connection.connectTimeout = 10000
        connection.readTimeout = 60000
        
        if (connection.responseCode == HttpURLConnection.HTTP_OK) {
            connection.inputStream.use { it.readBytes() }
        } else {
            throw IOException("HTTP ${connection.responseCode}: ${connection.responseMessage}")
        }
    } catch (e: Exception) {
        Logger.lError("Failed to download from URL: $url", e)
        throw e
    }
}

/**
 * Retry operation with exponential backoff
 */
suspend fun <T> withRetry(
    operation: String,
    maxRetries: Int = 3,
    initialDelay: Long = 1000,
    maxDelay: Long = 10000,
    factor: Double = 2.0,
    block: suspend () -> T
): T {
    var currentDelay = initialDelay
    var lastException: Exception? = null
    
    repeat(maxRetries) { attempt ->
        try {
            return block()
        } catch (e: Exception) {
            lastException = e
            Logger.lWarning("$operation attempt ${attempt + 1} failed: ${e.message}")
            
            if (attempt < maxRetries - 1) {
                delay(currentDelay)
                currentDelay = (currentDelay * factor).toLong().coerceAtMost(maxDelay)
            }
        }
    }
    
    throw lastException ?: IOException("All retry attempts failed for $operation")
}

/**
 * Suspend version of download from mirror list with fallback
 */
suspend fun downloadFromMirrorListSuspend(
    urls: List<String>,
    targetFile: File,
    sha1: String? = null,
    size: Long? = null,
    verifyIntegrity: Boolean = false
): Boolean {
    return try {
        withRetry("Download from mirror list") {
            val success = downloadFromMirrorList(
                urls = urls,
                sha1 = sha1,
                outputFile = targetFile
            ) { downloadedBytes ->
                // Progress callback - could be enhanced
            }
            
            if (!success) {
                throw IOException("Failed to download from all mirrors")
            }
            
            // Verify size if provided
            size?.let { expectedSize ->
                if (targetFile.length() != expectedSize) {
                    targetFile.delete()
                    throw IOException("Size verification failed: expected $expectedSize, got ${targetFile.length()}")
                }
            }
            
            true
        }
    } catch (e: Exception) {
        Logger.lError("Download failed from all URLs", e)
        false
    }
}