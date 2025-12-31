/*
 * Shard Launcher
 * Adapted from Zalith Launcher 2
 */

package com.lanrhyme.shardlauncher.utils.network

import com.google.gson.Gson
import com.google.gson.JsonSyntaxException
import com.lanrhyme.shardlauncher.utils.string.isEmptyOrBlank
import java.io.File
import java.io.IOException
import java.net.HttpURLConnection
import java.net.URL

/**
 * Download and parse JSON from URL
 */
inline fun <reified T> downloadAndParseJson(
    url: String,
    targetFile: File? = null,
    expectedSHA: String? = null,
    verifyIntegrity: Boolean = false,
    classOfT: Class<T>? = null
): T? {
    return try {
        val connection = URL(url).openConnection() as HttpURLConnection
        connection.requestMethod = "GET"
        connection.connectTimeout = 10000
        connection.readTimeout = 10000
        
        val responseCode = connection.responseCode
        if (responseCode == HttpURLConnection.HTTP_OK) {
            val response = connection.inputStream.bufferedReader().use { it.readText() }
            if (!response.isEmptyOrBlank()) {
                // Save to file if specified
                targetFile?.let { file ->
                    file.parentFile?.mkdirs()
                    file.writeText(response)
                }
                
                Gson().fromJson(response, T::class.java)
            } else {
                null
            }
        } else {
            null
        }
    } catch (e: IOException) {
        e.printStackTrace()
        null
    } catch (e: JsonSyntaxException) {
        e.printStackTrace()
        null
    }
}

/**
 * Simple HTTP GET request
 */
fun httpGet(url: String): String? {
    return try {
        val connection = URL(url).openConnection() as HttpURLConnection
        connection.requestMethod = "GET"
        connection.connectTimeout = 10000
        connection.readTimeout = 10000
        
        val responseCode = connection.responseCode
        if (responseCode == HttpURLConnection.HTTP_OK) {
            connection.inputStream.bufferedReader().use { it.readText() }
        } else {
            null
        }
    } catch (e: IOException) {
        e.printStackTrace()
        null
    }
}

/**
 * Fetch string from URL (throws exception on failure)
 */
fun fetchStringFromUrl(url: String): String {
    return httpGet(url) ?: throw IOException("Failed to fetch from $url")
}

/**
 * Fetch string from multiple URLs (try each until one succeeds)
 */
suspend fun fetchStringFromUrls(urls: List<String>): String {
    for (url in urls) {
        try {
            return fetchStringFromUrl(url)
        } catch (e: Exception) {
            if (url == urls.last()) {
                throw e
            }
        }
    }
    throw RuntimeException("All URLs failed")
}

/**
 * Retry function with exponential backoff
 */
suspend fun <T> withRetry(
    tag: String,
    maxRetries: Int = 3,
    initialDelay: Long = 1000,
    block: suspend () -> T
): T {
    var currentDelay = initialDelay
    repeat(maxRetries) { attempt ->
        try {
            return block()
        } catch (e: Exception) {
            if (attempt == maxRetries - 1) {
                throw e
            }
            kotlinx.coroutines.delay(currentDelay)
            currentDelay *= 2
        }
    }
    throw RuntimeException("Should not reach here")
}

/**
 * Download from mirror list with progress callback
 */
fun downloadFromMirrorList(
    urls: List<String>, 
    sha1: String?, 
    outputFile: File, 
    bufferSize: Int = 32768,
    onProgress: (Long) -> Unit
): Boolean {
    for (url in urls) {
        try {
            val connection = URL(url).openConnection() as HttpURLConnection
            connection.requestMethod = "GET"
            connection.connectTimeout = 10000
            connection.readTimeout = 10000
            
            if (connection.responseCode == HttpURLConnection.HTTP_OK) {
                val totalSize = connection.contentLengthLong
                var downloadedSize = 0L
                
                connection.inputStream.use { input ->
                    outputFile.outputStream().use { output ->
                        val buffer = ByteArray(bufferSize)
                        var bytesRead: Int
                        while (input.read(buffer).also { bytesRead = it } != -1) {
                            output.write(buffer, 0, bytesRead)
                            downloadedSize += bytesRead
                            onProgress(downloadedSize)
                        }
                    }
                }
                return true
            }
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }
    return false
}