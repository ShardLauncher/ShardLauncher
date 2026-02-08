package com.lanrhyme.shardlauncher.ui.version.detail

import android.os.Build
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import coil.compose.AsyncImage
import com.lanrhyme.shardlauncher.game.mod.LocalMod
import com.lanrhyme.shardlauncher.game.mod.Platform
import com.lanrhyme.shardlauncher.game.mod.RemoteMod
import com.lanrhyme.shardlauncher.ui.components.layout.LocalCardLayoutConfig
import dev.chrisbanes.haze.hazeEffect
import java.text.SimpleDateFormat
import java.util.*

@Composable
fun ModDetailsDialog(
    remoteMod: RemoteMod,
    onDismiss: () -> Unit
) {
    val (isCardBlurEnabled, cardAlpha, hazeState) = LocalCardLayoutConfig.current
    val context = LocalContext.current
    
    Dialog(
        onDismissRequest = onDismiss,
        properties = DialogProperties(
            usePlatformDefaultWidth = false
        )
    ) {
        val dialogCardShape = RoundedCornerShape(16.dp)
        Card(
            modifier = Modifier
                .fillMaxWidth(0.5f)
                .fillMaxHeight(0.9f)
                .then(
                    if (isCardBlurEnabled && Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
                        Modifier.clip(dialogCardShape).hazeEffect(state = hazeState)
                    } else Modifier
                ),
            shape = dialogCardShape,
            colors = CardDefaults.cardColors(
                containerColor = MaterialTheme.colorScheme.surface.copy(alpha = cardAlpha)
            )
        ) {
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(16.dp)
            ) {
                // 标题栏
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = "模组详情",
                        style = MaterialTheme.typography.titleLarge,
                        fontWeight = FontWeight.Bold
                    )
                    
                    IconButton(onClick = onDismiss) {
                        Icon(
                            imageVector = Icons.Default.Close,
                            contentDescription = "关闭"
                        )
                    }
                }
                
                Divider(modifier = Modifier.padding(vertical = 8.dp))
                
                // 内容区域
                Column(
                    modifier = Modifier
                        .fillMaxSize()
                        .verticalScroll(rememberScrollState()),
                    verticalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    ModDetailsContent(remoteMod = remoteMod)
                }
            }
        }
    }
}

@Composable
private fun ModDetailsContent(remoteMod: RemoteMod) {
    val localMod = remoteMod.localMod
    val projectInfo = remoteMod.projectInfo
    val remoteFile = remoteMod.remoteFile
    
    // 模组图标和基本信息
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        // 图标
        Box(
            modifier = Modifier.size(64.dp)
        ) {
            if (projectInfo?.iconUrl != null) {
                AsyncImage(
                    model = projectInfo.iconUrl,
                    contentDescription = "模组图标",
                    modifier = Modifier
                        .size(64.dp)
                        .clip(RoundedCornerShape(10.dp))
                )
            } else if (localMod.icon != null) {
                Image(
                    bitmap = android.graphics.BitmapFactory.decodeByteArray(
                        localMod.icon, 0, localMod.icon.size
                    ).asImageBitmap(),
                    contentDescription = "模组图标",
                    modifier = Modifier
                        .size(64.dp)
                        .clip(RoundedCornerShape(10.dp))
                )
            } else {
                Box(
                    modifier = Modifier
                        .size(64.dp)
                        .background(
                            MaterialTheme.colorScheme.primary.copy(alpha = 0.1f),
                            RoundedCornerShape(10.dp)
                        ),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = Icons.Default.Extension,
                        contentDescription = "默认图标",
                        tint = MaterialTheme.colorScheme.primary,
                        modifier = Modifier.size(32.dp)
                    )
                }
            }
        }
        
        // 基本信息
        Column(
            modifier = Modifier.weight(1f),
            verticalArrangement = Arrangement.spacedBy(3.dp)
        ) {
            Text(
                text = projectInfo?.title ?: localMod.name,
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold,
                maxLines = 2,
                overflow = TextOverflow.Ellipsis
            )
            
            if (localMod.version != null) {
                Text(
                    text = "版本: ${localMod.version}",
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
            
            if (localMod.authors.isNotEmpty()) {
                Text(
                    text = "作者: ${localMod.authors.joinToString(", ")}",
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    maxLines = 2,
                    overflow = TextOverflow.Ellipsis
                )
            }
            
            // 模组加载器标签
            Surface(
                shape = RoundedCornerShape(6.dp),
                color = when (localMod.loader) {
                    LocalMod.ModLoader.FABRIC -> MaterialTheme.colorScheme.primary.copy(alpha = 0.1f)
                    LocalMod.ModLoader.FORGE -> MaterialTheme.colorScheme.secondary.copy(alpha = 0.1f)
                    LocalMod.ModLoader.QUILT -> MaterialTheme.colorScheme.tertiary.copy(alpha = 0.1f)
                    LocalMod.ModLoader.NEOFORGE -> MaterialTheme.colorScheme.error.copy(alpha = 0.1f)
                    else -> MaterialTheme.colorScheme.surfaceVariant
                }
            ) {
                Text(
                    text = localMod.loader.displayName,
                    style = MaterialTheme.typography.labelMedium,
                    color = when (localMod.loader) {
                        LocalMod.ModLoader.FABRIC -> MaterialTheme.colorScheme.primary
                        LocalMod.ModLoader.FORGE -> MaterialTheme.colorScheme.secondary
                        LocalMod.ModLoader.QUILT -> MaterialTheme.colorScheme.tertiary
                        LocalMod.ModLoader.NEOFORGE -> MaterialTheme.colorScheme.error
                        else -> MaterialTheme.colorScheme.onSurfaceVariant
                    },
                    modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
                )
            }
        }
    }
    
    // 描述
    if (!localMod.description.isNullOrBlank()) {
        Card(
            colors = CardDefaults.cardColors(
                containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f)
            )
        ) {
            Column(
                modifier = Modifier.padding(10.dp),
                verticalArrangement = Arrangement.spacedBy(6.dp)
            ) {
                Text(
                    text = "描述",
                    style = MaterialTheme.typography.titleSmall,
                    fontWeight = FontWeight.Medium
                )
                Text(
                    text = localMod.description,
                    style = MaterialTheme.typography.bodyMedium
                )
            }
        }
    }
    
    // 文件信息
    Card(
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f)
        )
    ) {
        Column(
            modifier = Modifier.padding(10.dp),
            verticalArrangement = Arrangement.spacedBy(6.dp)
        ) {
            Text(
                text = "文件信息",
                style = MaterialTheme.typography.titleSmall,
                fontWeight = FontWeight.Medium
            )
            
            InfoRow(
                label = "文件名",
                value = localMod.file.name
            )
            
            InfoRow(
                label = "文件大小",
                value = formatFileSize(localMod.fileSize)
            )
            
            InfoRow(
                label = "状态",
                value = if (localMod.isEnabled()) "已启用" else "已禁用"
            )
        }
    }
    
    // 远程信息（如果有）
    if (projectInfo != null) {
        Card(
            colors = CardDefaults.cardColors(
                containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f)
            )
        ) {
            Column(
                modifier = Modifier.padding(10.dp),
                verticalArrangement = Arrangement.spacedBy(6.dp)
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    Text(
                        text = "远程信息",
                        style = MaterialTheme.typography.titleSmall,
                        fontWeight = FontWeight.Medium
                    )
                    
                    Surface(
                        shape = RoundedCornerShape(4.dp),
                        color = when (projectInfo.platform) {
                            Platform.MODRINTH -> MaterialTheme.colorScheme.primary.copy(alpha = 0.1f)
                            Platform.CURSEFORGE -> MaterialTheme.colorScheme.secondary.copy(alpha = 0.1f)
                        }
                    ) {
                        Text(
                            text = when (projectInfo.platform) {
                                Platform.MODRINTH -> "Modrinth"
                                Platform.CURSEFORGE -> "CurseForge"
                            },
                            style = MaterialTheme.typography.labelSmall,
                            color = when (projectInfo.platform) {
                                Platform.MODRINTH -> MaterialTheme.colorScheme.primary
                                Platform.CURSEFORGE -> MaterialTheme.colorScheme.secondary
                            },
                            modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                        )
                    }
                }
                
                InfoRow(
                    label = "项目ID",
                    value = projectInfo.id
                )
                
                InfoRow(
                    label = "项目标识",
                    value = projectInfo.slug
                )
                
                if (remoteFile != null) {
                    InfoRow(
                        label = "发布日期",
                        value = formatDate(remoteFile.datePublished)
                    )
                    
                    if (remoteFile.loaders.isNotEmpty()) {
                        InfoRow(
                            label = "支持加载器",
                            value = remoteFile.loaders.joinToString(", ") { it.getDisplayName() }
                        )
                    }
                }
            }
        }
    }
}

@Composable
private fun InfoRow(
    label: String,
    value: String
) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween
    ) {
        Text(
            text = label,
            style = MaterialTheme.typography.bodySmall,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            modifier = Modifier.weight(1f)
        )
        Text(
            text = value,
            style = MaterialTheme.typography.bodySmall,
            modifier = Modifier.weight(2f)
        )
    }
}

private fun formatFileSize(bytes: Long): String {
    val units = arrayOf("B", "KB", "MB", "GB")
    var size = bytes.toDouble()
    var unitIndex = 0
    
    while (size >= 1024 && unitIndex < units.size - 1) {
        size /= 1024
        unitIndex++
    }
    
    return "%.1f %s".format(size, units[unitIndex])
}

private fun formatDate(dateString: String): String {
    return try {
        val inputFormat = SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss.SSS'Z'", Locale.getDefault())
        val outputFormat = SimpleDateFormat("yyyy-MM-dd HH:mm", Locale.getDefault())
        val date = inputFormat.parse(dateString)
        outputFormat.format(date ?: Date())
    } catch (e: Exception) {
        dateString
    }
}