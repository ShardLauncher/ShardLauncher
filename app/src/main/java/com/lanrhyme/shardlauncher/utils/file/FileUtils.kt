/*
 * Shard Launcher
 * Adapted from Zalith Launcher 2
 */

package com.lanrhyme.shardlauncher.utils.file

import java.io.File
import java.security.MessageDigest
import kotlin.math.ln
import kotlin.math.pow

/**
 * Ensure directory exists
 */
fun File.ensureDirectory(): File {
    if (!exists()) {
        mkdirs()
    }
    return this
}

/**
 * Ensure parent directory exists
 */
fun File.ensureParentDirectory(): File {
    parentFile?.ensureDirectory()
    return this
}

/**
 * Compare SHA1 hash of file
 */
fun compareSHA1(file: File, expectedSha1: String): Boolean {
    if (!file.exists()) return false
    
    return try {
        val digest = MessageDigest.getInstance("SHA-1")
        val bytes = file.readBytes()
        val hash = digest.digest(bytes)
        val hashString = hash.joinToString("") { "%02x".format(it) }
        hashString.equals(expectedSha1, ignoreCase = true)
    } catch (e: Exception) {
        e.printStackTrace()
        false
    }
}

/**
 * Format file size in human readable format
 */
fun formatFileSize(bytes: Long): String {
    if (bytes < 1024) return "$bytes B"
    val exp = (ln(bytes.toDouble()) / ln(1024.0)).toInt()
    val pre = "KMGTPE"[exp - 1]
    return String.format("%.1f %sB", bytes / 1024.0.pow(exp.toDouble()), pre)
}

/**
 * Read the entire content of an InputStream as a UTF-8 string
 */
fun java.io.InputStream.readString(): String {
    return this.bufferedReader(Charsets.UTF_8).use { it.readText() }
}