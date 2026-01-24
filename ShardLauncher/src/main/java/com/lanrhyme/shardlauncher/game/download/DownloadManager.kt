package com.lanrhyme.shardlauncher.game.download

import android.content.Context
import com.lanrhyme.shardlauncher.coroutine.Task
import com.lanrhyme.shardlauncher.coroutine.TaskState
import com.lanrhyme.shardlauncher.coroutine.TitledTask
import com.lanrhyme.shardlauncher.game.download.game.GameDownloadInfo
import com.lanrhyme.shardlauncher.game.download.game.GameInstaller
import com.lanrhyme.shardlauncher.ui.notification.Notification
import com.lanrhyme.shardlauncher.ui.notification.NotificationManager
import com.lanrhyme.shardlauncher.ui.notification.NotificationType
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import java.util.UUID

object DownloadManager {
    private val scope = CoroutineScope(Dispatchers.Main + SupervisorJob())
    
    private var installer: GameInstaller? = null
    
    private val _downloadTask = MutableStateFlow<Task?>(null)
    val downloadTask = _downloadTask.asStateFlow()
    
    // Tasks flow from the installer
    private val _tasksFlow = MutableStateFlow<List<TitledTask>>(emptyList())
    val tasksFlow = _tasksFlow.asStateFlow()
    
    private val _showDialog = MutableStateFlow(false)
    val showDialog = _showDialog.asStateFlow()
    
    private var notificationId: String? = null
    var currentVersionId: String? = null
        private set
    
    private var _dialogTitle = MutableStateFlow("")
    val dialogTitle = _dialogTitle.asStateFlow()

    fun startDownload(context: Context, downloadInfo: GameDownloadInfo, versionId: String) {
        if (_downloadTask.value != null) return // Already downloading
        
        currentVersionId = versionId
        _dialogTitle.value = "正在安装 $versionId"
        notificationId = UUID.randomUUID().toString()
        _showDialog.value = true
        
        scope.launch {
            // Initialize notification
            NotificationManager.show(
                Notification(
                    id = notificationId!!,
                    title = "正在安装 $versionId",
                    message = "准备中...",
                    type = NotificationType.Progress,
                    progress = 0f,
                    isClickable = true,
                    onClick = { _showDialog.value = true }
                )
            )

            val newInstaller = GameInstaller(
                context = context.applicationContext,
                info = downloadInfo,
                scope = scope
            )
            installer = newInstaller
            
            // Connect installer tasks flow to local flow
            launch {
                newInstaller.tasksFlow.collect { tasks ->
                    _tasksFlow.value = tasks
                    updateNotification(tasks)
                }
            }

            val task = Task.runTask(
                id = "game_download_${versionId}",
                task = { task ->
                    task.taskState = TaskState.RUNNING
                },
                onError = { error ->
                    _downloadTask.value = null
                    dismissNotification()
                    com.lanrhyme.shardlauncher.utils.logging.Logger.lError("Download task error: ${error.message}", error)
                },
                onFinally = {
                    scope.launch {
                        delay(500)
                        _downloadTask.value = null
                        dismissNotification()
                    }
                }
            )
            
            _downloadTask.value = task
            
            newInstaller.installGame(
                isRunning = {
                    com.lanrhyme.shardlauncher.utils.logging.Logger.lWarning("Installation already in progress")
                    task.taskState = TaskState.COMPLETED
                    cleanup()
                },
                onInstalled = { installedVersion ->
                    com.lanrhyme.shardlauncher.utils.logging.Logger.lInfo("Game installed successfully: $installedVersion")
                    task.taskState = TaskState.COMPLETED
                     com.lanrhyme.shardlauncher.game.version.installed.VersionsManager.refresh(
                        tag = "DownloadManager",
                        trySetVersion = installedVersion
                    )
                    cleanup()
                },
                onError = { error ->
                     com.lanrhyme.shardlauncher.utils.logging.Logger.lError("Game installation failed: ${error.message}", error)
                     task.taskState = TaskState.COMPLETED
                     cleanup()
                },
                onGameAlreadyInstalled = {
                    com.lanrhyme.shardlauncher.utils.logging.Logger.lWarning("Game already installed: ${downloadInfo.customVersionName}")
                    task.taskState = TaskState.COMPLETED
                    cleanup()
                }
            )
        }
    }
    
    fun cancelDownload() {
        installer?.cancelInstall()
        _showDialog.value = false
        cleanup()
    }
    
    fun closeDialog() {
        _showDialog.value = false
    }
    
    private fun cleanup() {
        scope.launch {
            delay(500)
            _downloadTask.value = null
            dismissNotification()
        }
    }

    private fun updateNotification(tasks: List<TitledTask>) {
        val currentTask = tasks.lastOrNull { it.task.taskState == TaskState.RUNNING } 
            ?: tasks.lastOrNull()
            
        if (currentTask != null && notificationId != null && currentVersionId != null) {
            NotificationManager.update(
                Notification(
                    id = notificationId!!,
                    title = "正在安装 $currentVersionId",
                    message = currentTask.title,
                    type = NotificationType.Progress,
                    progress = currentTask.task.currentProgress,
                    isClickable = true,
                    onClick = { _showDialog.value = true }
                )
            )
        }
    }
    
    private fun dismissNotification() {
        notificationId?.let { NotificationManager.dismiss(it) }
        notificationId = null
    }
}
