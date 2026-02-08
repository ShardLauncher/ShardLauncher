package com.lanrhyme.shardlauncher.ui.home

import android.os.Build
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.ArrowDropDown
import androidx.compose.material.icons.rounded.Star
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.rotate
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import coil.compose.AsyncImage
import com.lanrhyme.shardlauncher.R
import com.lanrhyme.shardlauncher.game.version.installed.Version
import com.lanrhyme.shardlauncher.game.version.installed.VersionsManager
import com.lanrhyme.shardlauncher.ui.components.layout.LocalCardLayoutConfig
import com.lanrhyme.shardlauncher.ui.components.basic.PopupContainer
import dev.chrisbanes.haze.hazeEffect
import java.text.SimpleDateFormat
import java.util.*

@Composable
fun VersionSelector(
    selectedVersion: Version?,
    versions: List<Version>,
    onVersionSelected: (Version) -> Unit,
    modifier: Modifier = Modifier
) {
    val (isCardBlurEnabled, cardAlpha, hazeState) = LocalCardLayoutConfig.current
    var showVersionList by remember { mutableStateOf(false) }
    val dateFormat = remember { SimpleDateFormat("yyyy-MM-dd HH:mm", Locale.getDefault()) }
    val cardShape = RoundedCornerShape(16.dp)

    // 版本选择卡片
    Card(
        modifier = modifier
            .clickable { showVersionList = true }
            .then(
                if (isCardBlurEnabled && Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
                    Modifier.clip(cardShape).hazeEffect(state = hazeState)
                } else Modifier
            ),
        shape = cardShape,
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
            // 版本图标
            AsyncImage(
                model = selectedVersion?.let { VersionsManager.getVersionIconFile(it) },
                contentDescription = "版本图标",
                placeholder = painterResource(R.drawable.img_minecraft),
                error = painterResource(R.drawable.img_minecraft),
                modifier = Modifier
                    .size(32.dp)
                    .clip(RoundedCornerShape(8.dp)),
                contentScale = ContentScale.Fit
            )

            Column(
                modifier = Modifier.weight(1f)
            ) {
                Text(
                    text = selectedVersion?.getVersionName() ?: "未选择版本",
                    style = MaterialTheme.typography.titleSmall,
                    fontWeight = FontWeight.Bold,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
                
                if (selectedVersion != null) {
                    // 显示上次启动时间
                    val lastLaunchTime = selectedVersion.getVersionConfig().lastLaunchTime
                    if (lastLaunchTime > 0) {
                        Text(
                            text = "上次启动: ${dateFormat.format(Date(lastLaunchTime))}",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    } else {
                        Text(
                            text = "从未启动",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                } else {
                    Text(
                        text = "点击选择游戏版本",
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }

            Icon(
                imageVector = Icons.Rounded.ArrowDropDown,
                contentDescription = "",
                tint = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
    }

    // 版本选择弹窗
    if (showVersionList) {
        PopupContainer(
            visible = showVersionList,
            onDismissRequest = { showVersionList = false },
            modifier = Modifier.width(320.dp)
        ) {
            VersionListPopup(
                versions = versions,
                selectedVersion = selectedVersion,
                onVersionSelected = { version ->
                    onVersionSelected(version)
                    showVersionList = false
                },
                onDismiss = { showVersionList = false }
            )
        }
    }
}

@Composable
private fun VersionListPopup(
    versions: List<Version>,
    selectedVersion: Version?,
    onVersionSelected: (Version) -> Unit,
    onDismiss: () -> Unit
) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(8.dp)
    ) {
        // 版本列表
        LazyColumn(
            modifier = Modifier
                .fillMaxWidth()
                .heightIn(max = 300.dp),
            verticalArrangement = Arrangement.spacedBy(4.dp)
        ) {
            items(versions.filter { it.isValid() }) { version ->
                VersionListItem(
                    version = version,
                    isSelected = version == selectedVersion,
                    onClick = { onVersionSelected(version) }
                )
            }
        }
    }
}

@Composable
private fun VersionListItem(
    version: Version,
    isSelected: Boolean,
    onClick: () -> Unit
) {
    val (isCardBlurEnabled, cardAlpha, hazeState) = LocalCardLayoutConfig.current
    val dateFormat = remember { SimpleDateFormat("MM-dd HH:mm", Locale.getDefault()) }
    val itemCardShape = RoundedCornerShape(16.dp)

    Card(
        modifier = Modifier
            .fillMaxWidth()
            .clickable { onClick() }
            .then(
                if (isCardBlurEnabled && Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
                    Modifier.clip(itemCardShape).hazeEffect(state = hazeState)
                } else Modifier
            ),
        colors = CardDefaults.cardColors(
            containerColor = if (isSelected) 
                MaterialTheme.colorScheme.primaryContainer.copy(alpha = cardAlpha * 0.8f)
            else MaterialTheme.colorScheme.surfaceVariant.copy(alpha = cardAlpha * 0.6f)
        ),
        shape = itemCardShape
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(8.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            // 版本图标
            AsyncImage(
                model = VersionsManager.getVersionIconFile(version),
                contentDescription = "版本图标",
                placeholder = painterResource(R.drawable.img_minecraft),
                error = painterResource(R.drawable.img_minecraft),
                modifier = Modifier
                    .size(24.dp)
                    .clip(RoundedCornerShape(8.dp)),
                contentScale = ContentScale.Fit
            )

            Column(
                modifier = Modifier.weight(1f)
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    Text(
                        text = version.getVersionName(),
                        style = MaterialTheme.typography.titleSmall,
                        fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Medium,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis,
                        modifier = Modifier.weight(1f)
                    )
                    
                    // 置顶图标
                    if (version.pinnedState) {
                        Icon(
                            imageVector = Icons.Rounded.Star,
                            contentDescription = "",
                            tint = MaterialTheme.colorScheme.primary,
                            modifier = Modifier
                                .size(16.dp)
                        )
                    }
                }

                // 版本详细信息
                version.getVersionInfo()?.let { info ->
                    Row(
                        horizontalArrangement = Arrangement.spacedBy(2.dp)
                    ) {
                        Text(
                            text = info.minecraftVersion,
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                        info.loaderInfo?.let { loader ->
                            Text(
                                text = "• ${loader.loader.displayName}",
                                style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                    }
                }
                
                // 上次启动时间
                val lastLaunchTime = version.getVersionConfig().lastLaunchTime
                if (lastLaunchTime > 0) {
                    Text(
                        text = "上次启动: ${dateFormat.format(Date(lastLaunchTime))}",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                } else {
                    Text(
                        text = "从未启动",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }

            // 展开指示器
            if (isSelected) {
                Icon(
                    imageVector = Icons.Rounded.ArrowDropDown,
                    contentDescription = "",
                    tint = MaterialTheme.colorScheme.primary,
                    modifier = Modifier
                        .size(24.dp)
                        .rotate(-90f)
                )
            }
        }
    }
}