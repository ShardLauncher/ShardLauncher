/*
 * Shard Launcher
 * 版本搜索和过滤工具
 */

package com.lanrhyme.shardlauncher.game.version.installed

import com.lanrhyme.shardlauncher.game.addons.modloader.ModLoader as ModLoaderType

/**
 * 版本分类
 */
enum class VersionCategory {
    ALL,        // 全部版本
    VANILLA,    // 原版版本
    MODLOADER   // 模组加载器版本
    PINNED      // 置顶版本
    RECENT      // 最近启动
    FAVORITE    // 收藏版本
    INVALID     // 无效版本
}

/**
 * 版本排序方式
 */
enum class VersionSortOrder {
    LAST_LAUNCH,      // 最后启动时间
    NAME_ASC,         // 名称升序
    NAME_DESC,        // 名称降序
    MC_VERSION_ASC,   // MC版本升序
    MC_VERSION_DESC,  // MC版本降序
    SIZE_ASC,         // 大小升序
    SIZE_DESC         // 大小降序
}

/**
 * 版本过滤器
 */
data class VersionFilter(
    val category: VersionCategory = VersionCategory.ALL,
    val keyword: String = "",
    val modLoader: ModLoaderType? = null,
    val minMcVersion: String = "",
    val maxMcVersion: String = "",
    val onlyValid: Boolean = true,
    val onlyPinned: Boolean = false,
    val favoriteCategory: String? = null
) {
    /**
     * 检查过滤器是否为空（默认状态）
     */
    fun isEmpty(): Boolean {
        return category == VersionCategory.ALL &&
               keyword.isBlank() &&
               modLoader == null &&
               minMcVersion.isBlank() &&
               maxMcVersion.isBlank() &&
               !onlyPinned &&
               favoriteCategory == null
    }

    companion object {
        val DEFAULT = VersionFilter()
    }
}

/**
 * 版本搜索和过滤工具
 */
object VersionFilter {
    /**
     * 过滤版本列表
     * @param versions 所有版本
     * @param filter 过滤器
     * @return 过滤后的版本列表
     */
    fun filterVersions(versions: List<Version>, filter: VersionFilter): List<Version> {
        var result = versions

        // 1. 按分类过滤
        result = filterByCategory(result, filter.category, filter.favoriteCategory)

        // 2. 按关键词搜索
        if (filter.keyword.isNotBlank()) {
            result = result.filter { matchesKeyword(it, filter.keyword) }
        }

        // 3. 按模组加载器过滤
        if (filter.modLoader != null) {
            result = result.filter { matchesModLoader(it, filter.modLoader) }
        }

        // 4. 按MC版本范围过滤
        if (filter.minMcVersion.isNotBlank() || filter.maxMcVersion.isNotBlank()) {
            result = result.filter { matchesMcVersionRange(it, filter.minMcVersion, filter.maxMcVersion) }
        }

        // 5. 按有效性过滤
        if (filter.onlyValid) {
            result = result.filter { it.isValid() }
        }

        // 6. 按置顶过滤
        if (filter.onlyPinned) {
            result = result.filter { it.isPinned() }
        }

        return result
    }

    /**
     * 排序版本列表
     * @param versions 版本列表
     * @param sortOrder 排序方式
     * @return 排序后的版本列表
     */
    fun sortVersions(versions: List<Version>, sortOrder: VersionSortOrder): List<Version> {
        return when (sortOrder) {
            VersionSortOrder.LAST_LAUNCH -> {
                versions.sortedByDescending { it.getVersionConfig().lastLaunchTime }
            }
            VersionSortOrder.NAME_ASC -> {
                versions.sortedBy { it.getVersionName().lowercase() }
            }
            VersionSortOrder.NAME_DESC -> {
                versions.sortedByDescending { it.getVersionName().lowercase() }
            }
            VersionSortOrder.MC_VERSION_ASC -> {
                versions.sortedBy { version ->
                    version.getVersionInfo()?.minecraftVersion?.toVersionString() ?: ""
                }
            }
            VersionSortOrder.MC_VERSION_DESC -> {
                versions.sortedByDescending { version ->
                    version.getVersionInfo()?.minecraftVersion?.toVersionString() ?: ""
                }
            }
            VersionSortOrder.SIZE_ASC -> {
                versions.sortedBy { calculateVersionSize(it) }
            }
            VersionSortOrder.SIZE_DESC -> {
                versions.sortedByDescending { calculateVersionSize(it) }
            }
        }
    }

    /**
     * 搜索版本（组合过滤和排序）
     * @param versions 所有版本
     * @param filter 过滤器
     * @param sortOrder 排序方式
     * @return 搜索结果
     */
    fun searchVersions(
        versions: List<Version>,
        filter: VersionFilter = VersionFilter.DEFAULT,
        sortOrder: VersionSortOrder = VersionSortOrder.LAST_LAUNCH
    ): List<Version> {
        val filtered = filterVersions(versions, filter)
        return sortVersions(filtered, sortOrder)
    }

    /**
     * 按分类过滤
     */
    private fun filterByCategory(
        versions: List<Version>,
        category: VersionCategory,
        favoriteCategory: String? = null
    ): List<Version> {
        return when (category) {
            VersionCategory.ALL -> versions
            VersionCategory.VANILLA -> versions.filter { it.getVersionType() == VersionType.VANILLA }
            VersionCategory.MODLOADER -> versions.filter { it.getVersionType() == VersionType.MODLOADERS }
            VersionCategory.PINNED -> versions.filter { it.isPinned() }
            VersionCategory.RECENT -> {
                // 返回最近启动的版本（基于lastLaunchTime）
                versions.filter { it.getVersionConfig().lastLaunchTime > 0 }
                    .sortedByDescending { it.getVersionConfig().lastLaunchTime }
            }
            VersionCategory.FAVORITE -> {
                // 返回指定收藏夹中的版本
                if (favoriteCategory != null) {
                    versions.filter { isInFavorite(it, favoriteCategory) }
                } else {
                    versions.filter { isFavorite(it) }
                }
            }
            VersionCategory.INVALID -> versions.filter { !it.isValid() }
        }
    }

    /**
     * 检查版本是否匹配关键词
     */
    private fun matchesKeyword(version: Version, keyword: String): Boolean {
        val lowerKeyword = keyword.lowercase()
        
        // 搜索版本名称
        if (version.getVersionName().lowercase().contains(lowerKeyword)) {
            return true
        }

        // 搜索MC版本号
        version.getVersionInfo()?.minecraftVersion?.let { mcVersion ->
            if (mcVersion.lowercase().contains(lowerKeyword)) {
                return true
            }
        }

        // 搜索模组加载器
        version.getVersionInfo()?.loaderInfo?.let { loaderInfo ->
            if (loaderInfo.loader.displayName.lowercase().contains(lowerKeyword)) {
                return true
            }
            if (loaderInfo.version.lowercase().contains(lowerKeyword)) {
                return true
            }
        }

        // 搜索版本描述
        if (version.getVersionConfig().versionSummary.lowercase().contains(lowerKeyword)) {
            return true
        }

        // 搜索自定义信息
        if (version.getVersionConfig().customInfo.lowercase().contains(lowerKeyword)) {
            return true
        }

        return false
    }

    /**
     * 检查版本是否匹配模组加载器
     */
    private fun matchesModLoader(version: Version, modLoader: ModLoaderType): Boolean {
        return version.getVersionInfo()?.loaderInfo?.loader == modLoader
    }

    /**
     * 检查版本是否在MC版本范围内
     */
    private fun matchesMcVersionRange(
        version: Version,
        minMcVersion: String,
        maxMcVersion: String
    ): Boolean {
        val mcVersion = version.getVersionInfo()?.minecraftVersion ?: return false
        val mcVersionString = mcVersion.toVersionString()

        if (minMcVersion.isNotBlank()) {
            if (mcVersionString.toVersionNumber() < minMcVersion.toVersionNumber()) {
                return false
            }
        }

        if (maxMcVersion.isNotBlank()) {
            if (mcVersionString.toVersionNumber() > maxMcVersion.toVersionNumber()) {
                return false
            }
        }

        return true
    }

    /**
     * 检查版本是否在收藏夹中
     */
    private fun isInFavorite(version: Version, favoriteName: String): Boolean {
        // TODO: 实现收藏夹逻辑
        // 需要从CurrentGameInfo.favoritesMap中检查
        return false
    }

    /**
     * 检查版本是否被收藏
     */
    private fun isFavorite(version: Version): Boolean {
        // TODO: 实现收藏夹逻辑
        return false
    }

    /**
     * 计算版本文件夹大小
     */
    private fun calculateVersionSize(version: Version): Long {
        val versionPath = version.getVersionPath()
        return calculateFolderSize(versionPath)
    }

    /**
     * 递归计算文件夹大小
     */
    private fun calculateFolderSize(file: File): Long {
        if (!file.exists()) return 0L
        
        return if (file.isDirectory) {
            file.listFiles()?.sumOf { calculateFolderSize(it) } ?: 0L
        } else {
            file.length()
        }
    }

    /**
     * 将MC版本字符串转换为可比较的版本号
     * 例如: "1.20.4" -> 12004
     */
    private fun String.toVersionString(): String {
        return this.split(".").joinToString("") { part ->
            part.toIntOrNull()?.toString() ?: part
        }
    }

    /**
     * 将版本字符串转换为数字
     */
    private fun String.toVersionNumber(): Long {
        return try {
            this.toVersionString().toLong()
        } catch (e: Exception) {
            0L
        }
    }
}