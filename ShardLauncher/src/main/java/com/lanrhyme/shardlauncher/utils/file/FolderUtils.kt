package com.lanrhyme.shardlauncher.utils.file

import android.content.Context
import android.content.Intent
import android.net.Uri
import java.io.File

object FolderUtils {
    
    /**
     * 尝试打开文件夹，使用多种方式确保兼容性
     */
    fun openFolder(context: Context, folder: File, onError: (String) -> Unit) {
        try {
            if (!folder.exists()) {
                folder.mkdirs()
            }
            
            // 尝试多种方式打开文件夹
            val success = tryOpenWithFileManager(context, folder) ||
                         tryOpenWithDocumentsUI(context, folder) ||
                         tryOpenWithGenericIntent(context, folder)
            
            if (!success) {
                // 如果都失败了，显示路径信息
                onError("无法打开文件管理器\n文件夹路径: ${folder.absolutePath}")
            }
        } catch (e: Exception) {
            onError("打开文件夹失败: ${e.message}")
        }
    }

    // 尝试使用系统文件管理器
    private fun tryOpenWithFileManager(context: Context, folder: File): Boolean {
        return try {
            val intent = Intent(Intent.ACTION_VIEW).apply {
                setDataAndType(Uri.fromFile(folder), "resource/folder")
                addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
            }
            context.startActivity(intent)
            true
        } catch (e: Exception) {
            false
        }
    }

    // 尝试使用DocumentsUI
    private fun tryOpenWithDocumentsUI(context: Context, folder: File): Boolean {
        return try {
            val intent = Intent(Intent.ACTION_VIEW).apply {
                setDataAndType(Uri.fromFile(folder), "vnd.android.document/directory")
                addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
            }
            context.startActivity(intent)
            true
        } catch (e: Exception) {
            false
        }
    }

    // 尝试使用通用Intent
    private fun tryOpenWithGenericIntent(context: Context, folder: File): Boolean {
        return try {
            val intent = Intent(Intent.ACTION_GET_CONTENT).apply {
                setDataAndType(Uri.fromFile(folder), "*/*")
                addCategory(Intent.CATEGORY_OPENABLE)
                addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
            }
            context.startActivity(Intent.createChooser(intent, "选择文件管理器"))
            true
        } catch (e: Exception) {
            false
        }
    }
}