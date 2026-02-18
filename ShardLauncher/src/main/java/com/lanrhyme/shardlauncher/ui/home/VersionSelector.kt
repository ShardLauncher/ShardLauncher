package com.lanrhyme.shardlauncher.ui.home

import android.os.Build
import androidx.compose.foundation.border
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
import com.lanrhyme.shardlauncher.ui.components.basic.*
import com.lanrhyme.shardlauncher.ui.components.layout.LocalCardLayoutConfig
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
    ShardCard(
        modifier = modifier
            .height(72.dp)
            .clickable { showVersionList = true },
        shape = RoundedCornerShape(20.dp),
        style = CardStyle.GLASS
    ) {
        Row(
            modifier = Modifier.fillMaxSize(),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            // 版本图标
            AsyncImage(
                model = selectedVersion?.let { VersionsManager.getVersionIconFile(it) },
                contentDescription = "版本图标",
                placeholder = painterResource(R.drawable.img_minecraft),
                error = painterResource(R.drawable.img_minecraft),
                modifier = Modifier
                    .size(40.dp)
                    .clip(RoundedCornerShape(12.dp)),
                contentScale = ContentScale.Fit
            )

            Column(
                modifier = Modifier.weight(1f)
            ) {
                Text(
                    text = selectedVersion?.getVersionName() ?: "选择当前运行版本",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
                
                selectedVersion?.let {
                    val lastLaunchTime = it.getVersionConfig().lastLaunchTime
                    Text(
                        text = if (lastLaunchTime > 0) "上次启动: ${dateFormat.format(Date(lastLaunchTime))}" else "从未启动",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.8f)
                    )
                } ?: run {
                    Text(
                        text = "点击展开版本选择列表",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.6f)
                    )
                }
            }

            Icon(
                imageVector = Icons.Rounded.ArrowDropDown,
                contentDescription = null,
                tint = MaterialTheme.colorScheme.primary,
                modifier = Modifier.size(28.dp)
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

    ShardCard(
        modifier = Modifier
            .fillMaxWidth()
            .clickable { onClick() }
            .then(if (isSelected) Modifier.border(1.5.dp, MaterialTheme.colorScheme.primary, RoundedCornerShape(20.dp)) else Modifier),
        shape = RoundedCornerShape(20.dp),
        style = CardStyle.GLASS
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            // 版本图标
            AsyncImage(
                model = VersionsManager.getVersionIconFile(version),
                contentDescription = "版本图标",
                placeholder = painterResource(R.drawable.img_minecraft),
                error = painterResource(R.drawable.img_minecraft),
                modifier = Modifier
                    .size(36.dp)
                    .clip(RoundedCornerShape(10.dp)),
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
                        fontWeight = if (isSelected) FontWeight.ExtraBold else FontWeight.Bold,
                        color = if (isSelected) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onSurface,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis,
                        modifier = Modifier.weight(1f)
                    )
                    
                    if (version.pinnedState) {
                        Icon(
                            imageVector = Icons.Rounded.Star,
                            contentDescription = null,
                            tint = MaterialTheme.colorScheme.primary,
                            modifier = Modifier.size(16.dp)
                        )
                    }
                }

                // 版本详细信息
                version.getVersionInfo()?.let { info ->
                    Row(
                        horizontalArrangement = Arrangement.spacedBy(6.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        ShardTag(
                            text = info.minecraftVersion,
                            containerColor = MaterialTheme.colorScheme.secondaryContainer.copy(alpha = 0.5f)
                        )
                        info.loaderInfo?.let { loader ->
                            ShardTag(
                                text = loader.loader.displayName,
                                containerColor = MaterialTheme.colorScheme.tertiaryContainer.copy(alpha = 0.5f)
                            )
                        }
                    }
                }
                
                val lastLaunchTime = version.getVersionConfig().lastLaunchTime
                Text(
                    text = if (lastLaunchTime > 0) "最近运行: ${dateFormat.format(Date(lastLaunchTime))}" else "尚未运行过",
                    style = MaterialTheme.typography.labelSmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.6f)
                )
            }

            if (isSelected) {
                Icon(
                    imageVector = Icons.Rounded.ArrowDropDown,
                    contentDescription = null,
                    tint = MaterialTheme.colorScheme.primary,
                    modifier = Modifier
                        .size(24.dp)
                        .rotate(-90f)
                )
            }
        }
    }
}