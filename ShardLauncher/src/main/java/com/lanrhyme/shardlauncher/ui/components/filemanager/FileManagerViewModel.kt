package com.lanrhyme.shardlauncher.ui.components.filemanager

import android.app.Application
import android.os.Environment
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import java.io.File

/**
 * 文件管理器ViewModel
 */
class FileManagerViewModel(application: Application) : AndroidViewModel(application) {
    
    private val _currentPath = MutableStateFlow<File>(Environment.getExternalStorageDirectory())
    val currentPath: StateFlow<File> = _currentPath.asStateFlow()
    
    private val _fileItems = MutableStateFlow<List<FileItem>>(emptyList())
    val fileItems: StateFlow<List<FileItem>> = _fileItems.asStateFlow()
    
    private val _selectedPath = MutableStateFlow<File?>(null)
    val selectedPath: StateFlow<File?> = _selectedPath.asStateFlow()
    
    private val _showCreateDirDialog = MutableStateFlow(false)
    val showCreateDirDialog: StateFlow<Boolean> = _showCreateDirDialog.asStateFlow()
    
    private val _errorMessage = MutableStateFlow<String?>(null)
    val errorMessage: StateFlow<String?> = _errorMessage.asStateFlow()
    
    private val config = FileSelectorConfig(
        initialPath = Environment.getExternalStorageDirectory()
    )
    
    private var sortMode = FileSortMode.BY_NAME
    private var sortOrder = FileSortOrder.ASCENDING
    
    init {
        loadFiles(_currentPath.value)
    }
    
    /**
     * 配置文件选择器
     */
    fun configure(config: FileSelectorConfig) {
        _currentPath.value = config.initialPath
        loadFiles(config.initialPath)
    }
    
    /**
     * 加载指定路径的文件
     */
    fun loadFiles(path: File) {
        viewModelScope.launch {
            try {
                if (!path.exists() || !path.canRead()) {
                    _errorMessage.value = "无法访问路径: ${path.absolutePath}"
                    return@launch
                }
                
                _currentPath.value = path
                
                val files = path.listFiles()?.toList() ?: emptyList()
                
                val fileItems = files
                    .filter { file ->
                        // 过滤隐藏文件
                        if (!config.showHiddenFiles && (file.isHidden || file.name.startsWith("."))) {
                            return@filter false
                        }
                        // 应用自定义过滤器
                        config.fileFilter?.invoke(file) ?: true
                    }
                    .map { FileItem.fromFile(it) }
                    .let { items ->
                        when (config.mode) {
                            FileSelectorMode.DIRECTORY_ONLY -> items.filter { it.isDirectory }
                            FileSelectorMode.FILE_ONLY -> items.filter { !it.isDirectory }
                            FileSelectorMode.FILE_OR_DIRECTORY -> items
                        }
                    }
                    .sortedWith(compareBy<FileItem> { !it.isDirectory }
                        .then(getComparator()))
                
                _fileItems.value = fileItems
                _errorMessage.value = null
            } catch (e: Exception) {
                _errorMessage.value = "加载文件失败: ${e.message}"
            }
        }
    }
    
    /**
     * 导航到父目录
     */
    fun navigateToParent() {
        val parent = _currentPath.value.parentFile
        if (parent != null && parent.canRead()) {
            loadFiles(parent)
        }
    }
    
    /**
     * 导航到子目录
     */
    fun navigateToDirectory(file: File) {
        if (file.isDirectory && file.canRead()) {
            loadFiles(file)
        }
    }
    
    /**
     * 选择路径
     */
    fun selectPath(file: File) {
        when (config.mode) {
            FileSelectorMode.DIRECTORY_ONLY -> {
                if (file.isDirectory) {
                    _selectedPath.value = file
                }
            }
            FileSelectorMode.FILE_ONLY -> {
                if (file.isFile) {
                    _selectedPath.value = file
                }
            }
            FileSelectorMode.FILE_OR_DIRECTORY -> {
                _selectedPath.value = file
            }
        }
    }
    
    /**
     * 显示创建目录对话框
     */
    fun showCreateDirectoryDialog() {
        _showCreateDirDialog.value = true
    }
    
    /**
     * 隐藏创建目录对话框
     */
    fun hideCreateDirectoryDialog() {
        _showCreateDirDialog.value = false
    }
    
    /**
     * 创建新目录
     */
    fun createDirectory(name: String): Boolean {
        return try {
            val newDir = File(_currentPath.value, name)
            if (newDir.exists()) {
                _errorMessage.value = "目录已存在: $name"
                false
            } else {
                val result = newDir.mkdirs()
                if (result) {
                    loadFiles(_currentPath.value)
                    _errorMessage.value = null
                } else {
                    _errorMessage.value = "创建目录失败: $name"
                }
                result
            }
        } catch (e: Exception) {
            _errorMessage.value = "创建目录失败: ${e.message}"
            false
        }
    }
    
    /**
     * 清除错误消息
     */
    fun clearError() {
        _errorMessage.value = null
    }
    
    /**
     * 设置排序模式
     */
    fun setSortMode(mode: FileSortMode) {
        if (sortMode != mode) {
            sortMode = mode
            loadFiles(_currentPath.value)
        }
    }
    
    /**
     * 切换排序顺序
     */
    fun toggleSortOrder() {
        sortOrder = if (sortOrder == FileSortOrder.ASCENDING) {
            FileSortOrder.DESCENDING
        } else {
            FileSortOrder.ASCENDING
        }
        loadFiles(_currentPath.value)
    }
    
    /**
     * 获取比较器
     */
    private fun getComparator(): Comparator<FileItem> {
        val baseComparator: Comparator<FileItem> = when (sortMode) {
            FileSortMode.BY_NAME -> compareBy { it.name.lowercase() }
            FileSortMode.BY_SIZE -> compareBy { it.size }
            FileSortMode.BY_DATE -> compareBy { it.lastModified }
        }
        
        return if (sortOrder == FileSortOrder.ASCENDING) {
            baseComparator
        } else {
            baseComparator.reversed()
        }
    }
    
    /**
     * 获取根目录列表
     */
    fun getRootDirectories(): List<File> {
        return listOfNotNull(
            Environment.getExternalStorageDirectory(),
            Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOWNLOADS),
            Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOCUMENTS),
            Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_PICTURES),
            Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_MUSIC)
        ).distinctBy { it.absolutePath }
    }
}