package com.lanrhyme.shardlauncher.utils.file

import android.content.Context
import android.net.Uri
import android.provider.DocumentsContract
import androidx.documentfile.provider.DocumentFile

object PathHelper {
    /**
     * 将 OpenDocumentTree 返回的 URI 转换为绝对路径（如果可能）
     * 注意：这可能在某些 Android 版本或特殊路径下不起作用
     */
    fun getPathFromUri(context: Context, uri: Uri): String? {
        val docFile = DocumentFile.fromTreeUri(context, uri) ?: return null
        // 尝试从 URI 中解析路径（简单实现，针对外部存储）
        val docId = DocumentsContract.getTreeDocumentId(uri)
        val split = docId.split(":")
        val type = split[0]
        return if ("primary".equals(type, ignoreCase = true)) {
            // TODO: i18n (if needed for path logs, but usually not)
            context.getExternalFilesDir(null)?.parentFile?.parentFile?.parentFile?.parentFile?.absolutePath + "/" + split[1]
        } else {
            // TODO: 处理 SD 卡等其他存储
            null
        }
    }
}
