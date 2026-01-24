/*
 * Shard Launcher
 * Adapted from Zalith Launcher 2
 */

package com.lanrhyme.shardlauncher.ui.components.dialog

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.lanrhyme.shardlauncher.game.resource.ResourceManager
import kotlinx.coroutines.launch

/**
 * 资源安装对话框
 * 用于检测并安装缺少的游戏资源
 *
 * @param visible 是否显示对话框
 * @param onDismiss 关闭对话框回调
 * @param onResourcesReady 资源准备就绪回调
 */
@Composable
fun ResourceInstallDialog(
    visible: Boolean,
    onDismiss: () -> Unit,
    onResourcesReady: () -> Unit
) {
    if (!visible) return
    
    val context = LocalContext.current
    val scope = rememberCoroutineScope()
    var checkResult by remember { mutableStateOf<ResourceManager.ResourceCheckResult?>(null) }
    var isInstalling by remember { mutableStateOf(false) }
    var installProgress by remember { mutableStateOf(0) }
    var installMessage by remember { mutableStateOf("") }
    var installError by remember { mutableStateOf<String?>(null) }

    // Check resources on first composition
    LaunchedEffect(Unit) {
        checkResult = ResourceManager.checkResources(context)
    }

    PopupContainer(
        visible = true,
        onDismissRequest = if (isInstalling) { {} } else onDismiss
    ) {
        Card(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            shape = RoundedCornerShape(16.dp)
        ) {
            Column(
                modifier = Modifier.padding(24.dp),
                verticalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                // Header
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    Icon(
                        Icons.Default.CloudDownload,
                        contentDescription = null,
                        tint = MaterialTheme.colorScheme.primary,
                        modifier = Modifier.size(32.dp)
                    )
                    Column {
                        Text(
                            text = "资源安装",
                            style = MaterialTheme.typography.headlineSmall,
                            fontWeight = FontWeight.Bold
                        )
                        Text(
                            text = "检测到缺少必要的游戏资源",
                            style = MaterialTheme.typography.bodyMedium,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }

                checkResult?.let { result ->
                    if (result.missingResources.isNotEmpty()) {
                        // Missing resources section
                        Card(
                            colors = CardDefaults.cardColors(
                                containerColor = MaterialTheme.colorScheme.errorContainer
                            )
                        ) {
                            Column(
                                modifier = Modifier.padding(16.dp),
                                verticalArrangement = Arrangement.spacedBy(8.dp)
                            ) {
                                Text(
                                    text = "缺少的资源:",
                                    style = MaterialTheme.typography.titleSmall,
                                    fontWeight = FontWeight.Bold,
                                    color = MaterialTheme.colorScheme.onErrorContainer
                                )
                                result.missingResources.forEach { resource ->
                                    Row(
                                        verticalAlignment = Alignment.CenterVertically,
                                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                                    ) {
                                        Icon(
                                            Icons.Default.Error,
                                            contentDescription = null,
                                            tint = MaterialTheme.colorScheme.error,
                                            modifier = Modifier.size(16.dp)
                                        )
                                        Text(
                                            text = resource,
                                            style = MaterialTheme.typography.bodyMedium,
                                            color = MaterialTheme.colorScheme.onErrorContainer
                                        )
                                    }
                                }
                            }
                        }
                    }

                    if (result.recommendedActions.isNotEmpty()) {
                        // Recommended actions section
                        Card(
                            colors = CardDefaults.cardColors(
                                containerColor = MaterialTheme.colorScheme.secondaryContainer
                            )
                        ) {
                            Column(
                                modifier = Modifier.padding(16.dp),
                                verticalArrangement = Arrangement.spacedBy(8.dp)
                            ) {
                                Text(
                                    text = "建议操作:",
                                    style = MaterialTheme.typography.titleSmall,
                                    fontWeight = FontWeight.Bold,
                                    color = MaterialTheme.colorScheme.onSecondaryContainer
                                )
                                result.recommendedActions.forEach { action ->
                                    Row(
                                        verticalAlignment = Alignment.CenterVertically,
                                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                                    ) {
                                        Icon(
                                            Icons.Default.Info,
                                            contentDescription = null,
                                            tint = MaterialTheme.colorScheme.secondary,
                                            modifier = Modifier.size(16.dp)
                                        )
                                        Text(
                                            text = action,
                                            style = MaterialTheme.typography.bodyMedium,
                                            color = MaterialTheme.colorScheme.onSecondaryContainer
                                        )
                                    }
                                }
                            }
                        }
                    }
                }

                // Installation progress
                if (isInstalling) {
                    Card(
                        colors = CardDefaults.cardColors(
                            containerColor = MaterialTheme.colorScheme.primaryContainer
                        )
                    ) {
                        Column(
                            modifier = Modifier.padding(16.dp),
                            verticalArrangement = Arrangement.spacedBy(12.dp)
                        ) {
                            Text(
                                text = "正在安装资源...",
                                style = MaterialTheme.typography.titleSmall,
                                fontWeight = FontWeight.Bold,
                                color = MaterialTheme.colorScheme.onPrimaryContainer
                            )

                            Text(
                                text = installMessage,
                                style = MaterialTheme.typography.bodyMedium,
                                color = MaterialTheme.colorScheme.onPrimaryContainer
                            )
                            
                            LinearProgressIndicator(
                                progress = installProgress / 100f,
                                modifier = Modifier.fillMaxWidth()
                            )

                            Text(
                                text = "${installProgress}%",
                                style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.onPrimaryContainer
                            )
                        }
                    }
                }

                // Error display
                installError?.let { error ->
                    Card(
                        colors = CardDefaults.cardColors(
                            containerColor = MaterialTheme.colorScheme.errorContainer
                        )
                    ) {
                        Column(
                            modifier = Modifier.padding(16.dp),
                            verticalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            Text(
                                text = "安装失败",
                                style = MaterialTheme.typography.titleSmall,
                                fontWeight = FontWeight.Bold,
                                color = MaterialTheme.colorScheme.onErrorContainer
                            )
                            Text(
                                text = error,
                                style = MaterialTheme.typography.bodyMedium,
                                color = MaterialTheme.colorScheme.onErrorContainer
                            )
                        }
                    }
                }

                // Action buttons
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    if (!isInstalling) {
                        OutlinedButton(
                            onClick = onDismiss,
                            modifier = Modifier.weight(1f)
                        ) {
                            androidx.compose.material3.Text("稍后安装")
                        }
                        
                        Button(
                            onClick = {
                                isInstalling = true
                                installError = null
                                scope.launch {
                                    ResourceManager.installEssentialResources(
                                        context = context,
                                        onProgress = { progress, message ->
                                            installProgress = progress
                                            installMessage = message
                                        }
                                    ).onSuccess {
                                        isInstalling = false
                                        onResourcesReady()
                                    }.onFailure { error ->
                                        isInstalling = false
                                        installError = error.message ?: "未知错误"
                                    }
                                }
                            },
                            modifier = Modifier.weight(1f)
                        ) {
                            androidx.compose.material3.Text("立即安装")
                        }
                    } else {
                        // Show cancel button during installation
                        OutlinedButton(
                            onClick = {
                                isInstalling = false
                                installError = "用户取消安装"
                            },
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            androidx.compose.material3.Text("取消安装")
                        }
                    }
                }

                // Additional options when not installing
                if (!isInstalling && installError == null) {
                    TextButton(
                        onClick = {
                            scope.launch {
                                isInstalling = true
                                ResourceManager.installRecommendedResources(
                                    context = context,
                                    onProgress = { progress, message ->
                                        installProgress = progress
                                        installMessage = message
                                    }
                                ).onSuccess {
                                    isInstalling = false
                                    onResourcesReady()
                                }.onFailure { error ->
                                    isInstalling = false
                                    installError = error.message ?: "未知错误"
                                }
                            }
                        },
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        androidx.compose.material3.Text("安装推荐资源 (包含高性能渲染器)")
                    }
                }

                // Retry button when there's an error
                if (installError != null) {
                    Button(
                        onClick = {
                            installError = null
                            installProgress = 0
                            installMessage = ""
                        },
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        androidx.compose.material3.Text("重试")
                    }
                }
            }
        }
    }
}