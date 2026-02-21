/*
 * Shard Launcher
 */

package com.lanrhyme.shardlauncher.utils

import android.content.Context
import java.io.File
import java.io.FileOutputStream
import java.io.IOException

/**
 * Copy a file from assets to external storage
 */
@Throws(IOException::class)
fun Context.copyAssetFile(fileName: String, output: File, overwrite: Boolean) {
    if (!output.parentFile.exists()) {
        output.parentFile.mkdirs()
    }
    if (!output.exists() || overwrite) {
        assets.open(fileName).use { inputStream ->
            FileOutputStream(output).use { outputStream ->
                val buffer = ByteArray(4096)
                var bytesRead: Int
                while (inputStream.read(buffer).also { bytesRead = it } != -1) {
                    outputStream.write(buffer, 0, bytesRead)
                }
            }
        }
    }
}
