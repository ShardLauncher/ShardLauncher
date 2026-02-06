package com.lanrhyme.shardlauncher.ui.components.filemanager

import java.io.File

/**
 * 文件选择模式
 */
enum class FileSelectorMode {
    /** 仅选择目录 */
    DIRECTORY_ONLY,
    /** 仅选择文件 */
    FILE_ONLY,
    /** 选择文件或目录 */
    FILE_OR_DIRECTORY
}

/**
 * 文件选择器配置
 */
data class FileSelectorConfig(
    /** 初始路径 */
    val initialPath: File,
    /** 选择模式 */
    val mode: FileSelectorMode = FileSelectorMode.DIRECTORY_ONLY,
    /** 是否显示隐藏文件（以.开头的文件） */
    val showHiddenFiles: Boolean = true,
    /** 是否允许创建新目录 */
    val allowCreateDirectory: Boolean = true,
    /** 文件过滤器 */
    val fileFilter: ((File) -> Boolean)? = null
)

/**
 * 文件选择结果
 */
sealed class FileSelectorResult {
    /** 用户取消选择 */
    data object Cancelled : FileSelectorResult()
    
    /** 用户选择了路径 */
    data class Selected(val path: File) : FileSelectorResult()
}

/**
 * 文件项数据类
 */
data class FileItem(
    val file: File,
    val isDirectory: Boolean,
    val isHidden: Boolean,
    val name: String,
    val size: Long = 0L,
    val lastModified: Long = 0L
) {
    companion object {
        fun fromFile(file: File): FileItem {
            return FileItem(
                file = file,
                isDirectory = file.isDirectory,
                isHidden = file.isHidden || file.name.startsWith("."),
                name = file.name,
                size = if (file.isFile) file.length() else 0L,
                lastModified = file.lastModified()
            )
        }
    }
}

/**
 * 文件排序方式
 */
enum class FileSortMode {
    /** 按名称排序 */
    BY_NAME,
    /** 按大小排序 */
    BY_SIZE,
    /** 按修改时间排序 */
    BY_DATE
}

/**
 * 文件排序顺序
 */
enum class FileSortOrder {
    /** 升序 */
    ASCENDING,
    /** 降序 */
    DESCENDING
}