package com.lanrhyme.shardlauncher.ui.components

import androidx.compose.animation.animateContentSize
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.outlined.Download
import androidx.compose.material.icons.outlined.Schedule
import androidx.compose.material3.Icon
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import com.lanrhyme.shardlauncher.R
import com.lanrhyme.shardlauncher.coroutine.Task
import com.lanrhyme.shardlauncher.coroutine.TaskState
import com.lanrhyme.shardlauncher.coroutine.TitledTask

/**
 * 任务流进度对话框
 * @param title 对话框标题
 * @param tasks 任务列表
 * @param visible 是否显示对话框
 * @param onDismiss 关闭对话框回调（点击取消按钮）
 * @param onCancel 取消任务回调
 * @param isCompleted 是否已完成
 * @param onComplete 完成回调（点击完成按钮）
 */
@Composable
fun TaskFlowDialog(
    title: String,
    tasks: List<TitledTask>,
    visible: Boolean,
    onDismiss: () -> Unit = {},
    onCancel: () -> Unit = {},
    isCompleted: Boolean = false,
    onComplete: () -> Unit = {}
) {
    CustomDialog(
        visible = visible,
        onDismissRequest = onDismiss,
        modifier = Modifier.padding(16.dp)
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp)
        ) {
            // 标题
            Text(
                text = title,
                style = MaterialTheme.typography.titleMedium,
                color = MaterialTheme.colorScheme.onSurface
            )
            
            Spacer(modifier = Modifier.height(16.dp))
            
            // 任务列表
            val scrollState = rememberLazyListState()
            LazyColumn(
                modifier = Modifier
                    .weight(1f)
                    .fillMaxWidth(),
                state = scrollState,
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                items(tasks) { task ->
                    TaskItem(
                        modifier = Modifier.fillMaxWidth(),
                        title = task.title,
                        icon = task.icon,
                        task = task.task
                    )
                }
            }
            
            Spacer(modifier = Modifier.height(16.dp))
            
            // 取消/完成按钮
            CustomButton(
                onClick = {
                    if (isCompleted) {
                        onComplete()
                    } else {
                        onCancel()
                    }
                    onDismiss()
                },
                modifier = Modifier.fillMaxWidth()
            ) {
                Text(if (isCompleted) "完成" else "取消")
            }
        }
    }
}

/**
 * 单个任务项
 */
@Composable
private fun TaskItem(
    modifier: Modifier = Modifier,
    title: String,
    icon: androidx.compose.ui.graphics.vector.ImageVector?,
    task: Task
) {
    Row(
        modifier = modifier,
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        // 任务状态图标
        val statusIcon = when (task.taskState) {
            TaskState.PREPARING -> Icons.Outlined.Schedule
            TaskState.RUNNING -> icon ?: Icons.Outlined.Download
            TaskState.COMPLETED -> Icons.Filled.Check
        }
        
        Icon(
            modifier = Modifier.size(24.dp),
            imageVector = statusIcon,
            contentDescription = null,
            tint = when (task.taskState) {
                TaskState.PREPARING -> MaterialTheme.colorScheme.onSurfaceVariant
                TaskState.RUNNING -> MaterialTheme.colorScheme.primary
                TaskState.COMPLETED -> MaterialTheme.colorScheme.primary
            }
        )
        
        // 任务信息
        Column(
            modifier = Modifier
                .weight(1f)
                .animateContentSize(),
            verticalArrangement = Arrangement.spacedBy(4.dp)
        ) {
            // 任务标题
            Text(
                text = title,
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurface
            )
            
            // 任务进度（仅在运行时显示）
            if (task.taskState == TaskState.RUNNING) {
                // 任务消息
                task.currentMessageRes?.let { messageRes ->
                    val args = task.currentMessageArgs
                    val context = androidx.compose.ui.platform.LocalContext.current
                    Text(
                        text = if (args != null) {
                            context.getString(messageRes, *args)
                        } else {
                            context.getString(messageRes)
                        },
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
                
                // 进度条
                if (task.currentProgress < 0) {
                    // 不确定进度
                    LinearProgressIndicator(
                        modifier = Modifier.fillMaxWidth()
                    )
                } else {
                    // 确定进度
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        LinearProgressIndicator(
                            progress = { task.currentProgress },
                            modifier = Modifier.weight(1f)
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(
                            text = "${(task.currentProgress * 100).toInt()}%",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }
            }
        }
    }
}