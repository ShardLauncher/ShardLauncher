package com.lanrhyme.shardlauncher.ui.version.management

import android.os.Build
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import com.lanrhyme.shardlauncher.game.version.installed.Version
import com.lanrhyme.shardlauncher.ui.components.layout.LocalCardLayoutConfig
import com.lanrhyme.shardlauncher.ui.components.filemanager.FileSelectorScreen
import com.lanrhyme.shardlauncher.ui.components.filemanager.FileSelectorConfig
import com.lanrhyme.shardlauncher.ui.components.filemanager.FileSelectorMode
import com.lanrhyme.shardlauncher.ui.components.filemanager.FileSelectorResult
import com.lanrhyme.shardlauncher.utils.file.FolderUtils
import dev.chrisbanes.haze.hazeEffect
import java.io.File

@Composable
fun ResourcePacksManagementScreen(
    version: Version,
    onBack: () -> Unit
) {
    val (isCardBlurEnabled, cardAlpha, hazeState) = LocalCardLayoutConfig.current
    val resourcePacksFolder = File(version.getGameDir(), "resourcepacks")
    var resourcePackFiles by remember { mutableStateOf<List<File>>(emptyList()) }
    var refreshTrigger by remember { mutableIntStateOf(0) }
    val context = LocalContext.current

    // 文件选择器
    var showFileSelector by remember { mutableStateOf(false) }

    // 刷新资源包列表
    LaunchedEffect(refreshTrigger) {
        resourcePackFiles = if (resourcePacksFolder.exists()) {
            resourcePacksFolder.listFiles()?.filter { 
                it.isFile && it.extension == "zip"
            }?.sortedBy { it.name } ?: emptyList()
        } else {
            emptyList()
        }
    }

    Column(
        modifier = Modifier.fillMaxSize(),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        // 顶部操作栏
        val topCardShape = RoundedCornerShape(16.dp)
        Card(
            modifier = Modifier
                .fillMaxWidth()
                .then(
                    if (isCardBlurEnabled && Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
                        Modifier.clip(topCardShape).hazeEffect(state = hazeState)
                    } else Modifier
                ),
            shape = topCardShape,
            colors = CardDefaults.cardColors(
                containerColor = MaterialTheme.colorScheme.surface.copy(alpha = cardAlpha)
            )
        ) {
            Column(
                modifier = Modifier.padding(16.dp),
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = "资源包管理",
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold,
                        modifier = Modifier.weight(1f)
                    )

                    Text(
                        text = "${resourcePackFiles.size} 个资源包",
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
                
                Row(
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    OutlinedButton(
                        onClick = { 
                            if (!resourcePacksFolder.exists()) {
                                resourcePacksFolder.mkdirs()
                            }
                            refreshTrigger++
                        }
                    ) {
                        Icon(Icons.Default.Refresh, contentDescription = null)
                        Spacer(modifier = Modifier.width(4.dp))
                        Text("刷新")
                    }
                    
                    Button(
                        onClick = {
                            showFileSelector = true
                        }
                    ) {
                        Icon(Icons.Default.Add, contentDescription = null)
                        Spacer(modifier = Modifier.width(4.dp))
                        Text("添加资源包")
                    }
                    
                    OutlinedButton(
                        onClick = {
                            FolderUtils.openFolder(context, resourcePacksFolder) { error ->
                                // Handle error if needed
                            }
                        }
                    ) {
                        Icon(Icons.Default.Folder, contentDescription = null)
                        Spacer(modifier = Modifier.width(4.dp))
                        Text("打开文件夹")
                    }
                }
            }
        }

        // 资源包列表
        if (resourcePackFiles.isEmpty()) {
            val emptyCardShape = RoundedCornerShape(16.dp)
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .then(
                        if (isCardBlurEnabled && Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
                            Modifier.clip(emptyCardShape).hazeEffect(state = hazeState)
                        } else Modifier
                    ),
                shape = emptyCardShape,
                colors = CardDefaults.cardColors(
                    containerColor = MaterialTheme.colorScheme.surface.copy(alpha = cardAlpha)
                )
            ) {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(32.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Column(
                        horizontalAlignment = Alignment.CenterHorizontally,
                        verticalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        Icon(
                            imageVector = Icons.Default.Style,
                            contentDescription = null,
                            modifier = Modifier.size(48.dp),
                            tint = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                        Text(
                            text = "暂无资源包",
                            style = MaterialTheme.typography.titleMedium,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                        Text(
                            text = "点击\"添加资源包\"按钮来安装资源包",
                            style = MaterialTheme.typography.bodyMedium,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }
            }
        } else {
            LazyColumn(
                modifier = Modifier.fillMaxWidth(),
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                items(resourcePackFiles) { resourcePackFile ->
                    ResourcePackItem(
                        resourcePackFile = resourcePackFile,
                        onDelete = {
                            resourcePackFile.delete()
                            refreshTrigger++
                        },
                        isCardBlurEnabled = isCardBlurEnabled,
                        cardAlpha = cardAlpha,
                        hazeState = hazeState
                    )
                }
                            }
                        }
                    }
                    
                    // 显示文件选择器
                    if (showFileSelector) {
                        FileSelectorScreen(
                            visible = showFileSelector,
                            config = FileSelectorConfig(
                                initialPath = android.os.Environment.getExternalStorageDirectory(),
                                mode = FileSelectorMode.FILE_ONLY,
                                showHiddenFiles = true,
                                allowCreateDirectory = false,
                                fileFilter = { file ->
                                    file.isFile && file.extension == "zip"
                                }
                            ),
                            onDismissRequest = { showFileSelector = false },
                            onSelection = { result ->
                                when (result) {
                                    is FileSelectorResult.Selected -> {
                                        try {
                                            val sourceFile = result.path
                                            val fileName = sourceFile.name
                                            val targetFile = File(resourcePacksFolder, fileName)
                                            
                                            if (!resourcePacksFolder.exists()) {
                                                resourcePacksFolder.mkdirs()
                                            }
                                            
                                            sourceFile.inputStream().use { input ->
                                                targetFile.outputStream().use { output ->
                                                    input.copyTo(output)
                                                }
                                            }
                                            refreshTrigger++
                                        } catch (e: Exception) {
                                            // TODO: 显示错误信息
                                        }
                                    }
                                    FileSelectorResult.Cancelled -> { /* 用户取消 */ }
                                    is FileSelectorResult.MultipleSelected -> { /* 不支持多选 */ }
                                }
                                showFileSelector = false
                            }
                        )
                    }
                }
                
                
                @Composable
                private fun ResourcePackItem(
    resourcePackFile: File,
    onDelete: () -> Unit,
    isCardBlurEnabled: Boolean,
    cardAlpha: Float,
    hazeState: dev.chrisbanes.haze.HazeState
) {
    var showDeleteDialog by remember { mutableStateOf(false) }
    val itemCardShape = RoundedCornerShape(12.dp)

    Card(
        modifier = Modifier
            .fillMaxWidth()
            .then(
                if (isCardBlurEnabled && Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
                    Modifier.clip(itemCardShape).hazeEffect(state = hazeState)
                } else Modifier
            ),
        shape = itemCardShape,
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surface.copy(alpha = cardAlpha)
        )
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            // 资源包图标
            Icon(
                imageVector = Icons.Default.Style,
                contentDescription = null,
                modifier = Modifier.size(32.dp),
                tint = MaterialTheme.colorScheme.primary
            )

            Column(
                modifier = Modifier.weight(1f)
            ) {
                Text(
                    text = resourcePackFile.nameWithoutExtension,
                    style = MaterialTheme.typography.bodyLarge,
                    fontWeight = FontWeight.Medium,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
                Text(
                    text = "${(resourcePackFile.length() / 1024).toInt()} KB",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }

            // 删除按钮
            IconButton(
                onClick = { showDeleteDialog = true }
            ) {
                Icon(
                    imageVector = Icons.Default.Delete,
                    contentDescription = "删除资源包",
                    tint = MaterialTheme.colorScheme.error
                )
            }
        }
    }

    if (showDeleteDialog) {
        AlertDialog(
            onDismissRequest = { showDeleteDialog = false },
            title = { Text("删除资源包") },
            text = { Text("确定要删除资源包 ${resourcePackFile.nameWithoutExtension} 吗？") },
            confirmButton = {
                TextButton(
                    onClick = {
                        onDelete()
                        showDeleteDialog = false
                    }
                ) {
                    Text("删除")
                }
            },
            dismissButton = {
                TextButton(
                    onClick = { showDeleteDialog = false }
                ) {
                    Text("取消")
                }
            }
        )
    }
}