package com.lanrhyme.shardlauncher.ui.components.filemanager

import android.os.Environment
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.CreateNewFolder
import androidx.compose.material.icons.filled.Folder
import androidx.compose.material.icons.filled.FolderOpen
import androidx.compose.material.icons.filled.InsertDriveFile
import androidx.compose.material.icons.filled.NavigateBefore
import androidx.compose.material.icons.filled.Sort
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.lanrhyme.shardlauncher.ui.components.basic.PopupContainer
import com.lanrhyme.shardlauncher.ui.components.basic.ScalingActionButton
import com.lanrhyme.shardlauncher.ui.components.basic.ShardButton
import com.lanrhyme.shardlauncher.ui.components.basic.ShardDialog
import com.lanrhyme.shardlauncher.ui.components.basic.ShardInputField
import java.io.File
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

/**
 * 文件选择器屏幕
 *
 * @param visible 是否显示
 * @param config 配置
 * @param onDismissRequest 关闭请求
 * @param onSelection 选择结果回调
 */
@Composable
fun FileSelectorScreen(
    visible: Boolean,
    config: FileSelectorConfig,
    onDismissRequest: () -> Unit,
    onSelection: (FileSelectorResult) -> Unit
) {
    val viewModel: FileManagerViewModel = viewModel()
    
    LaunchedEffect(config) {
        viewModel.configure(config)
    }
    
    val currentPath by viewModel.currentPath.collectAsState()
    val fileItems by viewModel.fileItems.collectAsState()
    val selectedPath by viewModel.selectedPath.collectAsState()
    val showCreateDirDialog by viewModel.showCreateDirDialog.collectAsState()
    val errorMessage by viewModel.errorMessage.collectAsState()
    
    var showSortMenu by remember { mutableStateOf(false) }
    
    PopupContainer(
        visible = visible,
        onDismissRequest = {
            onSelection(FileSelectorResult.Cancelled)
            onDismissRequest()
        },
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp)
    ) {
        Surface(
            modifier = Modifier.fillMaxSize(),
            shape = RoundedCornerShape(24.dp),
            color = MaterialTheme.colorScheme.surface
        ) {
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(20.dp)
            ) {
                // 标题栏
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = "选择目录",
                        style = MaterialTheme.typography.titleLarge,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.onSurface
                    )
                    IconButton(onClick = { showSortMenu = true }) {
                        Icon(
                            imageVector = Icons.Default.Sort,
                            contentDescription = "排序",
                            tint = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }
                
                Spacer(modifier = Modifier.height(16.dp))
                
                // 当前路径显示
                PathBar(
                    currentPath = currentPath,
                    onPathClick = { viewModel.loadFiles(it) }
                )
                
                Spacer(modifier = Modifier.height(16.dp))
                
                // 文件列表
                FileList(
                    fileItems = fileItems,
                    selectedPath = selectedPath,
                    onItemClick = { file ->
                        if (file.isDirectory) {
                            viewModel.navigateToDirectory(file)
                        } else {
                            viewModel.selectPath(file)
                        }
                    },
                    onItemLongClick = { file ->
                        viewModel.selectPath(file)
                    },
                    modifier = Modifier
                        .weight(1f)
                        .fillMaxWidth()
                )
                
                Spacer(modifier = Modifier.height(16.dp))
                
                // 操作按钮
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    // 返回上级目录按钮
                    ShardButton(
                        onClick = { viewModel.navigateToParent() },
                        modifier = Modifier.weight(1f),
                        type = com.lanrhyme.shardlauncher.ui.components.basic.ButtonType.OUTLINED,
                        enabled = currentPath.parentFile != null
                    ) {
                        Icon(
                            imageVector = Icons.Default.NavigateBefore,
                            contentDescription = null,
                            modifier = Modifier.size(20.dp)
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        Text("上级")
                    }
                    
                    // 创建目录按钮
                    if (config.allowCreateDirectory) {
                        ShardButton(
                            onClick = { viewModel.showCreateDirectoryDialog() },
                            modifier = Modifier.weight(1f),
                            type = com.lanrhyme.shardlauncher.ui.components.basic.ButtonType.OUTLINED
                        ) {
                            Icon(
                                imageVector = Icons.Default.CreateNewFolder,
                                contentDescription = null,
                                modifier = Modifier.size(20.dp)
                            )
                            Spacer(modifier = Modifier.width(8.dp))
                            Text("新建")
                        }
                    }
                    
                    // 确认按钮
                    ScalingActionButton(
                        onClick = {
                            selectedPath?.let { path ->
                                onSelection(FileSelectorResult.Selected(path))
                            }
                            onDismissRequest()
                        },
                        modifier = Modifier.weight(1.5f),
                        text = "选择",
                        enabled = selectedPath != null
                    )
                }
            }
        }
    }
    
    // 创建目录对话框
    if (showCreateDirDialog) {
        CreateDirectoryDialog(
            onDismissRequest = { viewModel.hideCreateDirectoryDialog() },
            onConfirm = { name ->
                viewModel.createDirectory(name)
            }
        )
    }
    
    // 错误消息提示
    errorMessage?.let { error ->
        LaunchedEffect(error) {
            // 可以在这里添加Toast或SnackBar提示
            viewModel.clearError()
        }
    }
}

/**
 * 路径栏
 */
@Composable
private fun PathBar(
    currentPath: File,
    onPathClick: (File) -> Unit
) {
    Surface(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(16.dp)),
        color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp, vertical = 12.dp),
            horizontalArrangement = Arrangement.spacedBy(8.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            // 存储根目录
            PathChip(
                label = "存储",
                onClick = { onPathClick(Environment.getExternalStorageDirectory()) }
            )
            
            // 路径分段
            val pathSegments = currentPath
                .absolutePath
                .substringAfter(Environment.getExternalStorageDirectory().absolutePath)
                .split("/")
                .filter { it.isNotEmpty() }
            
            pathSegments.forEach { segment ->
                Icon(
                    imageVector = Icons.Default.NavigateBefore,
                    contentDescription = null,
                    modifier = Modifier.size(16.dp),
                    tint = MaterialTheme.colorScheme.onSurfaceVariant
                )
                PathChip(
                    label = segment,
                    onClick = {
                        val fullPath = Environment.getExternalStorageDirectory()
                        val index = pathSegments.indexOf(segment)
                        for (i in 0..index) {
                            File(fullPath, pathSegments[i])
                        }
                    }
                )
            }
        }
    }
}

/**
 * 路径芯片
 */
@Composable
private fun PathChip(
    label: String,
    onClick: () -> Unit
) {
    Surface(
        onClick = onClick,
        modifier = Modifier.clip(RoundedCornerShape(12.dp)),
        color = MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.5f),
        shape = RoundedCornerShape(12.dp)
    ) {
        Text(
            text = label,
            style = MaterialTheme.typography.labelMedium,
            color = MaterialTheme.colorScheme.onPrimaryContainer,
            modifier = Modifier.padding(horizontal = 12.dp, vertical = 6.dp),
            maxLines = 1,
            overflow = TextOverflow.Ellipsis
        )
    }
}

/**
 * 文件列表
 */
@Composable
private fun FileList(
    fileItems: List<FileItem>,
    selectedPath: File?,
    onItemClick: (File) -> Unit,
    onItemLongClick: (File) -> Unit,
    modifier: Modifier = Modifier
) {
    if (fileItems.isEmpty()) {
        Box(
            modifier = modifier.fillMaxSize(),
            contentAlignment = Alignment.Center
        ) {
            Column(
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Icon(
                    imageVector = Icons.Default.Folder,
                    contentDescription = null,
                    modifier = Modifier.size(64.dp),
                    tint = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.3f)
                )
                Spacer(modifier = Modifier.height(16.dp))
                Text(
                    text = "空目录",
                    style = MaterialTheme.typography.bodyLarge,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        }
    } else {
        LazyColumn(
            modifier = modifier.fillMaxSize(),
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            items(fileItems, key = { it.file.absolutePath }) { fileItem ->
                FileListItem(
                    fileItem = fileItem,
                    isSelected = selectedPath?.absolutePath == fileItem.file.absolutePath,
                    onClick = { onItemClick(fileItem.file) },
                    onLongClick = { onItemLongClick(fileItem.file) }
                )
            }
        }
    }
}

/**
 * 文件列表项
 */
@Composable
private fun FileListItem(
    fileItem: FileItem,
    isSelected: Boolean,
    onClick: () -> Unit,
    onLongClick: () -> Unit
) {
    val scale by animateFloatAsState(
        targetValue = if (isSelected) 1.02f else 1f,
        label = "scale"
    )
    
    Surface(
        onClick = onClick,
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(16.dp)),
        shape = RoundedCornerShape(16.dp),
        color = if (isSelected) {
            MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.8f)
        } else {
            MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.3f)
        },
        border = if (isSelected) {
            BorderStroke(2.dp, MaterialTheme.colorScheme.primary)
        } else null
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp, vertical = 12.dp),
            horizontalArrangement = Arrangement.spacedBy(12.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            // 图标
            Icon(
                imageVector = if (fileItem.isDirectory) {
                    Icons.Default.FolderOpen
                } else {
                    Icons.Default.InsertDriveFile
                },
                contentDescription = null,
                modifier = Modifier.size(32.dp),
                tint = if (fileItem.isDirectory) {
                    MaterialTheme.colorScheme.primary
                } else {
                    MaterialTheme.colorScheme.onSurfaceVariant
                }
            )
            
            // 文件信息
            Column(
                modifier = Modifier.weight(1f)
            ) {
                Text(
                    text = fileItem.name,
                    style = MaterialTheme.typography.bodyLarge,
                    fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal,
                    color = MaterialTheme.colorScheme.onSurface,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
                Text(
                    text = if (fileItem.isDirectory) "文件夹" else formatFileSize(fileItem.size),
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
            
            // 修改时间
            Text(
                text = formatDate(fileItem.lastModified),
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
    }
}

/**
 * 创建目录对话框
 */
@Composable
private fun CreateDirectoryDialog(
    onDismissRequest: () -> Unit,
    onConfirm: (String) -> Unit
) {
    var dirName by remember { mutableStateOf("") }
    var isError by remember { mutableStateOf(false) }
    
    ShardDialog(
        visible = true,
        onDismissRequest = onDismissRequest,
        width = 400.dp,
        height = 300.dp
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(20.dp)
        ) {
            Text(
                text = "新建文件夹",
                style = MaterialTheme.typography.titleLarge,
                fontWeight = FontWeight.Bold
            )
            
            Spacer(modifier = Modifier.height(24.dp))
            
            ShardInputField(
                value = dirName,
                onValueChange = { 
                    dirName = it
                    isError = it.isEmpty() || it.contains(Regex("[\\/:*?\"<>|]"))
                },
                label = "文件夹名称",
                placeholder = "请输入文件夹名称",
                isError = isError,
                singleLine = true,
                modifier = Modifier.fillMaxWidth()
            )
            
            if (isError) {
                Spacer(modifier = Modifier.height(8.dp))
                Text(
                    text = "名称不能为空且不能包含特殊字符",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.error
                )
            }
            
            Spacer(modifier = Modifier.height(24.dp))
            
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.End
            ) {
                ShardButton(
                    onClick = onDismissRequest,
                    type = com.lanrhyme.shardlauncher.ui.components.basic.ButtonType.TEXT
                ) {
                    Text("取消")
                }
                
                Spacer(modifier = Modifier.width(8.dp))
                
                ShardButton(
                    onClick = {
                        if (dirName.isNotBlank()) {
                            onConfirm(dirName)
                        }
                    },
                    enabled = dirName.isNotBlank() && !isError
                ) {
                    Text("创建")
                }
            }
        }
    }
}

/**
 * 格式化文件大小
 */
private fun formatFileSize(size: Long): String {
    return when {
        size < 1024 -> "$size B"
        size < 1024 * 1024 -> "${size / 1024} KB"
        size < 1024 * 1024 * 1024 -> "${size / (1024 * 1024)} MB"
        else -> "${size / (1024 * 1024 * 1024)} GB"
    }
}

/**
 * 格式化日期
 */
private fun formatDate(timestamp: Long): String {
    val date = Date(timestamp)
    val format = SimpleDateFormat("yyyy-MM-dd HH:mm", Locale.getDefault())
    return format.format(date)
}