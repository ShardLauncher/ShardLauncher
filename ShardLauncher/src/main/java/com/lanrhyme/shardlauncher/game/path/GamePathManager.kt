package com.lanrhyme.shardlauncher.game.path

import android.Manifest
import android.content.Context
import android.content.pm.PackageManager
import android.os.Build
import android.os.Environment
import androidx.core.content.ContextCompat
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
     * 检查存储权限
     * @return true 如果有存储权限，否则 false
     */
    fun hasStoragePermission(context: Context): Boolean {
        return if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.R) {
            // Android 11+ 需要检查 MANAGE_EXTERNAL_STORAGE 权限
            Environment.isExternalStorageManager()
        } else {
            // Android 10 及以下检查 READ_EXTERNAL_STORAGE 和 WRITE_EXTERNAL_STORAGE
            val readPermission = ContextCompat.checkSelfPermission(
                context,
                Manifest.permission.READ_EXTERNAL_STORAGE
            ) == PackageManager.PERMISSION_GRANTED
            val writePermission = ContextCompat.checkSelfPermission(
                context,
                Manifest.permission.WRITE_EXTERNAL_STORAGE
            ) == PackageManager.PERMISSION_GRANTED
            readPermission && writePermission
        }
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
     * @param context 上下文，用于权限检查
     * @param id 路径ID
     * @throws IllegalStateException 如果没有存储权限
     * @throws IllegalArgumentException 如果路径ID不存在
     */
    fun selectPath(context: Context, id: String) {
        // 检查存储权限
        if (!hasStoragePermission(context)) {
            throw IllegalStateException("未授予存储/管理所有文件权限")
        }

        // 检查路径是否存在
        if (!_gamePathData.value.any { it.id == id }) {
            throw IllegalArgumentException("未找到匹配的路径ID: $id")
        }

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