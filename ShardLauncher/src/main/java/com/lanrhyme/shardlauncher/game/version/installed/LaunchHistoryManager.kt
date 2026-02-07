/*
 * Shard Launcher
 * 版本启动历史管理
 */

package com.lanrhyme.shardlauncher.game.version.installed

import com.google.gson.annotations.SerializedName
import com.lanrhyme.shardlauncher.game.path.getGameHome
import com.lanrhyme.shardlauncher.utils.GSON
import com.lanrhyme.shardlauncher.utils.logging.Logger
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.io.File
import java.io.FileWriter
import java.util.concurrent.ConcurrentHashMap

/**
 * 单次启动记录
 */
data class LaunchRecord(
    @SerializedName("versionName")
    val versionName: String,
    @SerializedName("launchTime")
    val launchTime: Long,
    @SerializedName("exitCode")
    val exitCode: Int?,
    @SerializedName("playDuration")
    val playDuration: Long? = null,  // 游戏时长（毫秒）
    @SerializedName("success")
    val success: Boolean
) {
    /**
     * 获取格式化的时长
     */
    fun getFormattedDuration(): String {
        return playDuration?.let { duration ->
            val minutes = (duration / 1000 / 60).toInt()
            val hours = minutes / 60
            val mins = minutes % 60
            when {
                hours > 0 -> "${hours}小时${mins}分钟"
                mins > 0 -> "${mins}分钟"
                else -> "< 1分钟"
            }
        } ?: "未知"
    }
}

/**
 * 启动历史统计
 */
data class LaunchStatistics(
    @SerializedName("totalLaunches")
    val totalLaunches: Int,
    @SerializedName("totalPlayTime")
    val totalPlayTime: Long,
    @SerializedName("successRate")
    val successRate: Float,
    @SerializedName("averagePlayTime")
    val averagePlayTime: Long,
    @SerializedName("mostPlayedVersion")
    val mostPlayedVersion: String?
)

/**
 * 启动历史管理器
 */
object LaunchHistoryManager {
    private const val MAX_HISTORY_SIZE = 1000  // 最大历史记录数
    private const val HISTORY_FILE = "shard_launch_history.json"

    // 版本启动历史 <versionName, List<LaunchRecord>>
    private val historyMap = ConcurrentHashMap<String, MutableList<LaunchRecord>>()

    // 总启动历史
    private val allHistory = mutableListOf<LaunchRecord>()

    /**
     * 初始化历史记录
     */
    suspend fun initialize() = withContext(Dispatchers.IO) {
        try {
            val historyFile = getHistoryFile()
            if (historyFile.exists()) {
                val jsonText = historyFile.readText()
                val data = GSON.fromJson(jsonText, HistoryData::class.java)
                data?.let {
                    it.versionHistory.forEach { (key, value) ->
                        historyMap[key] = value.toMutableList()
                    }
                    allHistory.addAll(it.allHistory)
                }
                Logger.i("LaunchHistoryManager", "Loaded ${allHistory.size} launch history records")
            }
        } catch (e: Exception) {
            Logger.e("LaunchHistoryManager", "Failed to load launch history", e)
        }
    }

    /**
     * 记录启动事件
     * @param versionName 版本名称
     * @param success 是否成功启动
     */
    fun recordLaunch(versionName: String, success: Boolean = true) {
        val record = LaunchRecord(
            versionName = versionName,
            launchTime = System.currentTimeMillis(),
            exitCode = null,
            playDuration = null,
            success = success
        )

        // 添加到版本历史
        historyMap.getOrPut(versionName) { mutableListOf() }.add(0, record)

        // 添加到总历史
        allHistory.add(0, record)

        // 限制历史记录大小
        if (historyMap[versionName]!!.size > MAX_HISTORY_SIZE) {
            historyMap[versionName]!!.removeAt(historyMap[versionName]!!.size - 1)
        }
        if (allHistory.size > MAX_HISTORY_SIZE) {
            allHistory.removeAt(allHistory.size - 1)
        }

        // 保存到文件
        save()

        Logger.d("LaunchHistoryManager", "Recorded launch: $versionName, success: $success")
    }

    /**
     * 更新启动记录（游戏结束时调用）
     * @param versionName 版本名称
     * @param exitCode 退出码
     * @param playDuration 游戏时长（毫秒）
     */
    fun updateLaunchRecord(versionName: String, exitCode: Int, playDuration: Long) {
        val versionHistory = historyMap[versionName] ?: return

        // 找到最近的一次启动记录
        val lastRecord = versionHistory.firstOrNull { it.exitCode == null }
        if (lastRecord != null) {
            val updatedRecord = lastRecord.copy(
                exitCode = exitCode,
                playDuration = playDuration,
                success = exitCode == 0
            )

            // 更新记录
            versionHistory[0] = updatedRecord

            // 同时更新总历史
            val index = allHistory.indexOfFirst { it == lastRecord }
            if (index >= 0) {
                allHistory[index] = updatedRecord
            }

            save()

            Logger.d("LaunchHistoryManager", "Updated launch record: $versionName, exitCode: $exitCode, duration: ${playDuration}ms")
        }
    }

    /**
     * 获取指定版本的启动历史
     * @param versionName 版本名称
     * @param limit 最多返回多少条记录
     * @return 启动记录列表
     */
    fun getVersionHistory(versionName: String, limit: Int = 10): List<LaunchRecord> {
        return historyMap[versionName]?.take(limit) ?: emptyList()
    }

    /**
     * 获取所有启动历史
     * @param limit 最多返回多少条记录
     * @return 启动记录列表
     */
    fun getAllHistory(limit: Int = 50): List<LaunchRecord> {
        return allHistory.take(limit)
    }

    /**
     * 获取最近启动的版本列表
     * @param limit 最多返回多少个版本
     * @return 版本名称列表（按最近启动时间排序）
     */
    fun getRecentlyLaunchedVersions(limit: Int = 5): List<String> {
        return allHistory
            .groupBy { it.versionName }
            .mapValues { (_, records) -> records.maxByOrNull { it.launchTime } }
            .values
            .filterNotNull()
            .sortedByDescending { it.launchTime }
            .take(limit)
            .map { it.versionName }
    }

    /**
     * 获取版本启动统计
     * @param versionName 版本名称
     * @return 启动统计信息
     */
    fun getVersionStatistics(versionName: String): LaunchStatistics? {
        val versionHistory = historyMap[versionName] ?: return null

        val totalLaunches = versionHistory.size
        val successfulLaunches = versionHistory.count { it.success }
        val totalPlayTime = versionHistory.sumOf { it.playDuration ?: 0 }
        val averagePlayTime = if (successfulLaunches > 0) {
            totalPlayTime / successfulLaunches
        } else {
            0L
        }
        val successRate = if (totalLaunches > 0) {
            successfulLaunches.toFloat() / totalLaunches
        } else {
            0f
        }

        return LaunchStatistics(
            totalLaunches = totalLaunches,
            totalPlayTime = totalPlayTime,
            successRate = successRate,
            averagePlayTime = averagePlayTime,
            mostPlayedVersion = versionName
        )
    }

    /**
     * 获取总启动统计
     * @return 启动统计信息
     */
    fun getTotalStatistics(): LaunchStatistics {
        val totalLaunches = allHistory.size
        val successfulLaunches = allHistory.count { it.success }
        val totalPlayTime = allHistory.sumOf { it.playDuration ?: 0 }
        val averagePlayTime = if (successfulLaunches > 0) {
            totalPlayTime / successfulLaunches
        } else {
            0L
        }
        val successRate = if (totalLaunches > 0) {
            successfulLaunches.toFloat() / totalLaunches
        } else {
            0f
        }

        // 找出游玩时间最长的版本
        val versionPlayTimes = allHistory
            .groupBy { it.versionName }
            .mapValues { (_, records) -> records.sumOf { it.playDuration ?: 0 } }
        val mostPlayedVersion = versionPlayTimes.maxByOrNull { it.value }?.key

        return LaunchStatistics(
            totalLaunches = totalLaunches,
            totalPlayTime = totalPlayTime,
            successRate = successRate,
            averagePlayTime = averagePlayTime,
            mostPlayedVersion = mostPlayedVersion
        )
    }

    /**
     * 清除指定版本的历史
     * @param versionName 版本名称
     */
    fun clearVersionHistory(versionName: String) {
        historyMap.remove(versionName)
        allHistory.removeAll { it.versionName == versionName }
        save()
    }

    /**
     * 清除所有历史
     */
    fun clearAllHistory() {
        historyMap.clear()
        allHistory.clear()
        save()
    }

    /**
     * 保存历史记录到文件
     */
    private fun save() {
        try {
            val historyFile = getHistoryFile()
            if (!historyFile.parentFile?.exists()!!) {
                historyFile.parentFile?.mkdirs()
            }

            val data = HistoryData(
                versionHistory = historyMap.toMap(),
                allHistory = allHistory.toList()
            )

            FileWriter(historyFile, false).use { writer ->
                writer.write(GSON.toJson(data))
            }
        } catch (e: Exception) {
            Logger.e("LaunchHistoryManager", "Failed to save launch history", e)
        }
    }

    /**
     * 获取历史文件路径
     */
    private fun getHistoryFile(): File {
        return File(getGameHome(), HISTORY_FILE)
    }

    /**
     * 历史数据
     */
    private data class HistoryData(
        @SerializedName("versionHistory")
        val versionHistory: Map<String, List<LaunchRecord>>,
        @SerializedName("allHistory")
        val allHistory: List<LaunchRecord>
    )
}