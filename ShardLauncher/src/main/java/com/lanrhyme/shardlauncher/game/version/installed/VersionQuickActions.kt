/*
 * Shard Launcher
 * 版本快速操作工具
 */

package com.lanrhyme.shardlauncher.game.version.installed

import android.content.Context
import android.content.Intent
import android.net.Uri
import androidx.core.content.FileProvider
import com.lanrhyme.shardlauncher.utils.logging.Logger
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.io.File

/**
 * 检查版本是否置顶
 */
fun Version.isPinned(): Boolean {
    return getVersionConfig().pinned
}

/**
 * 快速操作类型
 */
enum class QuickAction {
    PIN,           // 置顶/取消置顶
    COPY,          // 复制版本
    EXPORT,        // 导出版本
    DELETE,        // 删除版本
    RENAME,        // 重命名版本
    OPEN_FOLDER,   // 打开版本文件夹
    SHARE,         // 分享版本
    VALIDATE,      // 验证版本完整性
    REFRESH_INFO,  // 刷新版本信息
    EDIT_CONFIG    // 编辑版本配置
}

/**
 * 快速操作结果
 */
data class QuickActionResult(
    val action: QuickAction,
    val success: Boolean,
    val message: String,
    val data: Any? = null
)

/**
 * 版本快速操作管理器
 */
object VersionQuickActions {
    /**
     * 执行快速操作
     * @param context 上下文
     * @param version 版本
     * @param action 操作类型
     * @param extraData 额外数据（如新名称等）
     * @return 操作结果
     */
    suspend fun executeQuickAction(
        context: Context,
        version: Version,
        action: QuickAction,
        extraData: Map<String, Any?> = emptyMap()
    ): QuickActionResult = withContext(Dispatchers.IO) {
        when (action) {
            QuickAction.PIN -> togglePin(version)
            QuickAction.COPY -> copyVersion(version, extraData["newName"] as? String)
            QuickAction.EXPORT -> exportVersion(context, version)
            QuickAction.DELETE -> deleteVersion(version)
            QuickAction.RENAME -> renameVersion(version, extraData["newName"] as? String)
            QuickAction.OPEN_FOLDER -> openFolder(context, version)
            QuickAction.SHARE -> shareVersion(context, version)
            QuickAction.VALIDATE -> validateVersion(version)
            QuickAction.REFRESH_INFO -> refreshVersionInfo(version)
            QuickAction.EDIT_CONFIG -> editConfig(version)
        }
    }

    /**
     * 切换置顶状态
     */
    private fun togglePin(version: Version): QuickActionResult {
        try {
            val newPinnedState = !version.isPinned()
            version.getVersionConfig().setPinnedAndSave(newPinnedState) {}

            Logger.i("VersionQuickActions", "Version ${version.getVersionName()} ${if (newPinnedState) "pinned" else "unpinned"}")

            return QuickActionResult(
                QuickAction.PIN,
                true,
                if (newPinnedState) "已置顶" else "已取消置顶"
            )
        } catch (e: Exception) {
            Logger.e("VersionQuickActions", "Failed to toggle pin", e)
            return QuickActionResult(
                QuickAction.PIN,
                false,
                "操作失败: ${e.message}"
            )
        }
    }

    /**
     * 复制版本
     */
    private suspend fun copyVersion(version: Version, newName: String?): QuickActionResult {
        if (newName.isNullOrBlank()) {
            return QuickActionResult(
                QuickAction.COPY,
                false,
                "请输入新版本名称"
            )
        }

        return try {
            VersionsManager.copyVersion(version, newName, false)
            Logger.i("VersionQuickActions", "Version ${version.getVersionName()} copied to $newName")

            QuickActionResult(
                QuickAction.COPY,
                true,
                "复制成功: $newName",
                newName
            )
        } catch (e: Exception) {
            Logger.e("VersionQuickActions", "Failed to copy version", e)
            QuickActionResult(
                QuickAction.COPY,
                false,
                "复制失败: ${e.message}"
            )
        }
    }

    /**
     * 导出版本
     */
    private suspend fun exportVersion(context: Context, version: Version): QuickActionResult {
        return try {
            val exportDir = File(context.getExternalFilesDir(null), "exports")
            if (!exportDir.exists()) {
                exportDir.mkdirs()
            }

            val fileName = "${version.getVersionName()}.slpack"
            val outputFile = File(exportDir, fileName)

            val result = VersionImporterExporter.exportVersion(
                version,
                outputFile,
                includeLibraries = false,
                includeNatives = false
            )

            if (result.success) {
                // 触发媒体扫描
                val intent = Intent(Intent.ACTION_MEDIA_SCANNER_SCAN_FILE)
                intent.data = Uri.fromFile(outputFile)
                context.sendBroadcast(intent)

                QuickActionResult(
                    QuickAction.EXPORT,
                    true,
                    result.message,
                    outputFile
                )
            } else {
                QuickActionResult(
                    QuickAction.EXPORT,
                    false,
                    result.message
                )
            }
        } catch (e: Exception) {
            Logger.e("VersionQuickActions", "Failed to export version", e)
            QuickActionResult(
                QuickAction.EXPORT,
                false,
                "导出失败: ${e.message}"
            )
        }
    }

    /**
     * 删除版本
     */
    private fun deleteVersion(version: Version): QuickActionResult {
        return try {
            VersionsManager.deleteVersion(version)
            Logger.i("VersionQuickActions", "Version ${version.getVersionName()} deleted")

            QuickActionResult(
                QuickAction.DELETE,
                true,
                "删除成功"
            )
        } catch (e: Exception) {
            Logger.e("VersionQuickActions", "Failed to delete version", e)
            QuickActionResult(
                QuickAction.DELETE,
                false,
                "删除失败: ${e.message}"
            )
        }
    }

    /**
     * 重命名版本
     */
    private suspend fun renameVersion(version: Version, newName: String?): QuickActionResult {
        if (newName.isNullOrBlank()) {
            return QuickActionResult(
                QuickAction.RENAME,
                false,
                "请输入新版本名称"
            )
        }

        return try {
            VersionsManager.renameVersion(version, newName)
            Logger.i("VersionQuickActions", "Version ${version.getVersionName()} renamed to $newName")

            QuickActionResult(
                QuickAction.RENAME,
                true,
                "重命名成功: $newName",
                newName
            )
        } catch (e: Exception) {
            Logger.e("VersionQuickActions", "Failed to rename version", e)
            QuickActionResult(
                QuickAction.RENAME,
                false,
                "重命名失败: ${e.message}"
            )
        }
    }

    /**
     * 打开版本文件夹
     */
    private fun openFolder(context: Context, version: Version): QuickActionResult {
        return try {
            val versionPath = version.getVersionPath()
            
            // 使用FileProvider创建URI
            val uri = FileProvider.getUriForFile(
                context,
                "${context.packageName}.fileprovider",
                versionPath
            )

            val intent = Intent(Intent.ACTION_VIEW).apply {
                setDataAndType(uri, "resource/folder")
                flags = Intent.FLAG_GRANT_READ_URI_PERMISSION
                // 使用Chooser让用户选择文件管理器
                addCategory(Intent.CATEGORY_BROWSABLE)
            }

            // 尝试启动
            context.startActivity(intent)

            QuickActionResult(
                QuickAction.OPEN_FOLDER,
                true,
                "正在打开文件夹..."
            )
        } catch (e: Exception) {
            Logger.e("VersionQuickActions", "Failed to open folder", e)
            QuickActionResult(
                QuickAction.OPEN_FOLDER,
                false,
                "打开文件夹失败: ${e.message}"
            )
        }
    }

    /**
     * 分享版本
     */
    private suspend fun shareVersion(context: Context, version: Version): QuickActionResult {
        return try {
            // 先导出版本
            val exportResult = exportVersion(context, version)
            if (!exportResult.success) {
                return QuickActionResult(
                    QuickAction.SHARE,
                    false,
                    "导出失败，无法分享"
                )
            }

            val outputFile = exportResult.data as File
            val uri = FileProvider.getUriForFile(
                context,
                "${context.packageName}.fileprovider",
                outputFile
            )

            val intent = Intent(Intent.ACTION_SEND).apply {
                type = "application/zip"
                putExtra(Intent.EXTRA_STREAM, uri)
                putExtra(Intent.EXTRA_SUBJECT, "分享 Minecraft 版本: ${version.getVersionName()}")
                putExtra(Intent.EXTRA_TEXT, "这是一个 Minecraft 版本包，使用 ShardLauncher 导入")
                flags = Intent.FLAG_GRANT_READ_URI_PERMISSION
            }

            val chooser = Intent.createChooser(intent, "分享版本")
            context.startActivity(chooser)

            QuickActionResult(
                QuickAction.SHARE,
                true,
                "正在分享..."
            )
        } catch (e: Exception) {
            Logger.e("VersionQuickActions", "Failed to share version", e)
            QuickActionResult(
                QuickAction.SHARE,
                false,
                "分享失败: ${e.message}"
            )
        }
    }

    /**
     * 验证版本完整性
     */
    private fun validateVersion(version: Version): QuickActionResult {
        return try {
            val validation = VersionValidator.validateVersion(version.getVersionPath())
            
            val message = if (validation.isValid) {
                if (validation.hasWarnings) {
                    "验证通过，但存在警告: ${validation.issues.size} 个问题"
                } else {
                    "验证通过，版本完整"
                }
            } else {
                "验证失败: ${validation.issues.size} 个错误"
            }

            QuickActionResult(
                QuickAction.VALIDATE,
                validation.isValid,
                message,
                validation
            )
        } catch (e: Exception) {
            Logger.e("VersionQuickActions", "Failed to validate version", e)
            QuickActionResult(
                QuickAction.VALIDATE,
                false,
                "验证失败: ${e.message}"
            )
        }
    }

    /**
     * 刷新版本信息
     */
    private fun refreshVersionInfo(version: Version): QuickActionResult {
        return try {
            VersionsManager.refresh("VersionQuickActions.refreshVersionInfo")
            
            QuickActionResult(
                QuickAction.REFRESH_INFO,
                true,
                "版本信息已刷新"
            )
        } catch (e: Exception) {
            Logger.e("VersionQuickActions", "Failed to refresh version info", e)
            QuickActionResult(
                QuickAction.REFRESH_INFO,
                false,
                "刷新失败: ${e.message}"
            )
        }
    }

    /**
     * 编辑版本配置
     */
    private fun editConfig(version: Version): QuickActionResult {
        // TODO: 实现打开配置编辑界面
        // 这个功能需要UI层的支持
        
        return QuickActionResult(
            QuickAction.EDIT_CONFIG,
            false,
            "配置编辑功能开发中..."
        )
    }

    /**
     * 获取可用快速操作列表
     */
    fun getAvailableActions(version: Version): List<QuickAction> {
        val actions = mutableListOf<QuickAction>()

        actions.add(QuickAction.PIN)
        actions.add(QuickAction.COPY)
        actions.add(QuickAction.EXPORT)
        actions.add(QuickAction.DELETE)
        actions.add(QuickAction.RENAME)
        actions.add(QuickAction.OPEN_FOLDER)
        actions.add(QuickAction.SHARE)
        actions.add(QuickAction.VALIDATE)
        actions.add(QuickAction.REFRESH_INFO)
        // actions.add(QuickAction.EDIT_CONFIG)  // TODO: 等待UI支持

        return actions
    }

    /**
     * 获取快速操作的显示名称
     */
    fun getActionDisplayName(action: QuickAction): String {
        return when (action) {
            QuickAction.PIN -> "置顶"
            QuickAction.COPY -> "复制"
            QuickAction.EXPORT -> "导出"
            QuickAction.DELETE -> "删除"
            QuickAction.RENAME -> "重命名"
            QuickAction.OPEN_FOLDER -> "打开文件夹"
            QuickAction.SHARE -> "分享"
            QuickAction.VALIDATE -> "验证完整性"
            QuickAction.REFRESH_INFO -> "刷新信息"
            QuickAction.EDIT_CONFIG -> "编辑配置"
        }
    }

    /**
     * 获取快速操作的图标描述
     */
    fun getActionIcon(action: QuickAction): String {
        return when (action) {
            QuickAction.PIN -> "push_pin"
            QuickAction.COPY -> "content_copy"
            QuickAction.EXPORT -> "download"
            QuickAction.DELETE -> "delete"
            QuickAction.RENAME -> "edit"
            QuickAction.OPEN_FOLDER -> "folder_open"
            QuickAction.SHARE -> "share"
            QuickAction.VALIDATE -> "verified"
            QuickAction.REFRESH_INFO -> "refresh"
            QuickAction.EDIT_CONFIG -> "settings"
        }
    }
}