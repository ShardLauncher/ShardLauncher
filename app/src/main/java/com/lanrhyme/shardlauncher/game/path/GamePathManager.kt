package com.lanrhyme.shardlauncher.game.path

import android.content.Context
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import com.lanrhyme.shardlauncher.data.SettingsRepository
import com.lanrhyme.shardlauncher.database.AppDatabase
import com.lanrhyme.shardlauncher.path.PathManager
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import java.io.File
import java.util.UUID

/**
 * 游戏目录管理，支持多个游戏目录切换
 */
object GamePathManager {
    private val scope = CoroutineScope(Dispatchers.IO)
    private val defaultGamePath = File(PathManager.DIR_FILES_EXTERNAL, ".minecraft").absolutePath

    /**
     * 默认游戏目录的ID
     */
    const val DEFAULT_ID = "default"

    private val _gamePathData = MutableStateFlow<List<GamePath>>(listOf())
    val gamePathData: StateFlow<List<GamePath>> = _gamePathData

    /**
     * 当前选择的路径
     */
    var currentPath by mutableStateOf(defaultGamePath)
        private set

    /**
     * 当前选择的路径ID
     */
    var currentPathId by mutableStateOf(DEFAULT_ID)
        private set

    private lateinit var database: AppDatabase
    private lateinit var gamePathDao: GamePathDao
    private lateinit var settingsRepository: SettingsRepository

    fun initialize(context: Context) {
        database = AppDatabase.getInstance(context)
        gamePathDao = database.gamePathDao()
        settingsRepository = SettingsRepository(context)
        reloadPaths()
    }

    /**
     * 重新从数据库加载所有路径
     */
    fun reloadPaths() {
        scope.launch {
            val paths = gamePathDao.getAllPaths()
            val displays = mutableListOf<GamePath>()
            // 添加默认目录
            displays.add(GamePath(DEFAULT_ID, "默认目录", defaultGamePath)) // TODO: i18n
            displays.addAll(paths.sortedBy { it.title })

            _gamePathData.update { displays }

            refreshCurrentPath()
        }
    }

    private fun refreshCurrentPath() {
        val id = settingsRepository.getCurrentGamePathId()
        currentPathId = id
        val item = _gamePathData.value.find { it.id == id }
        if (item != null) {
            currentPath = item.path
        } else {
            // 如果找不到（比如被删除了），回退到默认
            currentPath = defaultGamePath
            currentPathId = DEFAULT_ID
            settingsRepository.setCurrentGamePathId(DEFAULT_ID)
        }
        com.lanrhyme.shardlauncher.game.version.installed.VersionsManager.refresh("GamePathManager.refreshCurrentPath")
    }

    /**
     * 切换当前选中的路径
     */
    fun selectPath(id: String) {
        settingsRepository.setCurrentGamePathId(id)
        refreshCurrentPath()
    }

    /**
     * 添加新路径
     */
    fun addNewPath(title: String, path: String) {
        if (_gamePathData.value.any { it.path == path }) return
        scope.launch {
            val newGamePath = GamePath(UUID.randomUUID().toString(), title, path)
            gamePathDao.savePath(newGamePath)
            reloadPaths()
        }
    }

    /**
     * 修改路径标题
     */
    fun modifyTitle(id: String, newTitle: String) {
        if (id == DEFAULT_ID) return // 默认目录暂不支持改名
        scope.launch {
            val item = gamePathDao.getPath(id)
            if (item != null) {
                gamePathDao.savePath(item.copy(title = newTitle))
                reloadPaths()
            }
        }
    }

    /**
     * 删除路径
     */
    fun removePath(id: String) {
        if (id == DEFAULT_ID) return
        scope.launch {
            val item = gamePathDao.getPath(id)
            if (item != null) {
                gamePathDao.deletePath(item)
                reloadPaths()
            }
        }
    }

    /**
     * Get user home directory for Minecraft (used by Launcher)
     */
    fun getUserHome(): String {
        return currentPath
    }
}