package com.lanrhyme.shardlauncher.utils.file

import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.io.File
import java.io.FileInputStream
import java.security.MessageDigest

/**
 * 文件哈希计算工具
 */
object HashUtils {
    
    /**
     * 计算文件的SHA1哈希值
     */
    suspend fun calculateFileSha1(file: File): String = withContext(Dispatchers.IO) {
        val digest = MessageDigest.getInstance("SHA-1")
        FileInputStream(file).use { fis ->
            val buffer = ByteArray(8192)
            var bytesRead: Int
            while (fis.read(buffer).also { bytesRead = it } != -1) {
                digest.update(buffer, 0, bytesRead)
            }
        }
        digest.digest().joinToString("") { "%02x".format(it) }
    }
    
    /**
     * 计算文件的MD5哈希值
     */
    suspend fun calculateFileMd5(file: File): String = withContext(Dispatchers.IO) {
        val digest = MessageDigest.getInstance("MD5")
        FileInputStream(file).use { fis ->
            val buffer = ByteArray(8192)
            var bytesRead: Int
            while (fis.read(buffer).also { bytesRead = it } != -1) {
                digest.update(buffer, 0, bytesRead)
            }
        }
        digest.digest().joinToString("") { "%02x".format(it) }
    }
}