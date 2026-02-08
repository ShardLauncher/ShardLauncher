/*
 * Shard Launcher
 * 当前游戏状态信息管理
 */

package com.lanrhyme.shardlauncher.game.version.installed

import com.google.gson.annotations.SerializedName
import com.lanrhyme.shardlauncher.game.path.getGameHome
import com.lanrhyme.shardlauncher.utils.GSON
import com.lanrhyme.shardlauncher.utils.logging.Logger
import java.io.File
import java.io.FileWriter
import java.util.concurrent.ConcurrentHashMap

/**
 * 当前游戏状态信息
 * @property version 当前选择的版本名称
 * @property favoritesMap 收藏夹映射表 <收藏夹名称, 包含的版本集合>
 * @property lastSelectedProfile 上次选择的Profile ID（用于多实例支持）
 */
data class CurrentGameInfo(
    @SerializedName("version")
    var version: String = "",
    @SerializedName("favoritesInfo")
    val favoritesMap: MutableMap<String, MutableSet<String>> = ConcurrentHashMap(),
    @SerializedName("lastSelectedProfile")
    var lastSelectedProfile: String = "default"
) {
    /**
     * 保存当前状态到文件
     */
    fun saveCurrentInfo() {
        val infoFile = getInfoFile()
        try {
            if (!infoFile.parentFile?.exists()!!) {
                infoFile.parentFile?.mkdirs()
            }
            FileWriter(infoFile, false).use { writer ->
                writer.write(GSON.toJson(this))
            }
            Logger.i("CurrentGameInfo", "Current version $version has been saved to ${infoFile.absolutePath}")
        } catch (e: Exception) {
            Logger.e("CurrentGameInfo", "Failed to save current game info: ${infoFile.absolutePath}", e)
        }
    }
}

/**
 * 获取配置文件路径
 */
private fun getInfoFile(): File {
    return File(getGameHome(), "shard-game.cfg")
}

/**
 * 刷新并返回最新的游戏信息
 * 如果文件不存在则创建新配置
 */
fun refreshCurrentInfo(): CurrentGameInfo {
    val infoFile = getInfoFile()

    return try {
        when {
            infoFile.exists() -> loadFromJsonFile(infoFile)
            else -> createNewConfig()
        }
    } catch (e: Exception) {
        Logger.e("CurrentGameInfo", "Failed to refresh current game info, creating new config", e)
        createNewConfig()
    }
}

/**
 * 从JSON文件加载配置
 */
private fun loadFromJsonFile(infoFile: File): CurrentGameInfo {
    return try {
        val jsonText = infoFile.readText()
        val info = GSON.fromJson(jsonText, CurrentGameInfo::class.java)
            ?: throw IllegalStateException("Deserialization returned null")
        info
    } catch (e: Exception) {
        Logger.e("CurrentGameInfo", "Failed to load from JSON file: ${infoFile.absolutePath}", e)
        createNewConfig()
    }
}

/**
 * 创建新的配置文件
 */
private fun createNewConfig(): CurrentGameInfo {
    return CurrentGameInfo().apply {
        saveCurrentInfo()
    }
}