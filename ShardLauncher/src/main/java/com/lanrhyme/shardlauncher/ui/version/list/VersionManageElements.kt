package com.lanrhyme.shardlauncher.ui.version.list

import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.foundation.background
import androidx.compose.foundation.basicMarquee
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.interaction.collectIsPressedAsState
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.rotate
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Shape
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.lanrhyme.shardlauncher.R
import com.movtery.zalithlauncher.game.path.GamePath
import com.movtery.zalithlauncher.game.path.GamePathManager
import com.movtery.zalithlauncher.game.version.installed.Version
import com.movtery.zalithlauncher.game.version.installed.VersionsManager
import com.lanrhyme.shardlauncher.ui.components.basic.*
import com.lanrhyme.shardlauncher.ui.components.layout.LocalCardLayoutConfig
import com.lanrhyme.shardlauncher.utils.logging.Logger.lError
import com.lanrhyme.shardlauncher.utils.string.getMessageOrToString

sealed interface GamePathOperation {
    data object None: GamePathOperation
    data object PathExists: GamePathOperation
    data class AddNewPath(val path: String): GamePathOperation
    data class RenamePath(val item: GamePath): GamePathOperation
    data class DeletePath(val item: GamePath): GamePathOperation
}

@Composable
fun GamePathItemLayout(
    item: GamePath,
    selected: Boolean,
    modifier: Modifier = Modifier,
    onClick: () -> Unit = {},
    onRename: () -> Unit = {},
    onDelete: () -> Unit = {}
) {
    val notDefault = item.id != GamePathManager.DEFAULT_ID

    NavigationDrawerItem(
        modifier = modifier,
        label = {
            Column(
                modifier = Modifier.padding(top = 4.dp, bottom = 4.dp)
            ) {
                Text(
                    modifier = Modifier.basicMarquee(iterations = Int.MAX_VALUE),
                    text = if (notDefault) item.title else "默认路径", // TODO: i18n
                    style = MaterialTheme.typography.labelMedium,
                    maxLines = 1
                )
                Text(
                    modifier = Modifier.basicMarquee(iterations = Int.MAX_VALUE),
                    text = item.path,
                    style = MaterialTheme.typography.labelSmall,
                    maxLines = 1
                )
            }
        },
        badge = {
            var menuExpanded by remember { mutableStateOf(false) }

            Row {
                IconButton(
                    modifier = Modifier.size(24.dp),
                    onClick = { menuExpanded = !menuExpanded }
                ) {
                    Icon(
                        modifier = Modifier.size(20.dp),
                        imageVector = Icons.Default.MoreHoriz,
                        contentDescription = "更多", // TODO: i18n
                    )
                }

                ShardDropdownMenu(
                    expanded = menuExpanded,
                    shape = MaterialTheme.shapes.large,
                    onDismissRequest = { menuExpanded = false }
                ) {
                    DropdownMenuItem(
                        enabled = notDefault,
                        text = { Text(text = "重命名") }, // TODO: i18n
                        leadingIcon = {
                            Icon(
                                modifier = Modifier.size(20.dp),
                                imageVector = Icons.Filled.Edit,
                                contentDescription = "重命名" // TODO: i18n
                            )
                        },
                        onClick = {
                            onRename()
                            menuExpanded = false
                        }
                    )
                    DropdownMenuItem(
                        enabled = notDefault,
                        text = { Text(text = "删除") }, // TODO: i18n
                        leadingIcon = {
                            Icon(
                                modifier = Modifier.size(20.dp),
                                imageVector = Icons.Filled.Delete,
                                contentDescription = "删除" // TODO: i18n
                            )
                        },
                        onClick = {
                            onDelete()
                            menuExpanded = false
                        }
                    )
                }
            }
        },
        selected = selected,
        onClick = onClick
    )
}

@Composable
fun VersionCategoryItem(
    modifier: Modifier = Modifier,
    value: VersionCategory,
    versionsCount: Int,
    selected: Boolean,
    shape: Shape = MaterialTheme.shapes.large,
    selectedContentColor: Color = MaterialTheme.colorScheme.onSurface,
    unselectedContentColor: Color = MaterialTheme.colorScheme.onSurface,
    style: TextStyle = MaterialTheme.typography.labelMedium,
    enabled: Boolean = true,
    onClick: () -> Unit = {}
) {
    FilterChip(
        modifier = modifier,
        selected = selected,
        onClick = onClick,
        enabled = enabled,
        label = {
            Row {
                Text(
                    text = when(value) {
                        VersionCategory.ALL -> "全部" // TODO: i18n
                        VersionCategory.VANILLA -> "原版" // TODO: i18n
                        VersionCategory.MODLOADER -> "模组" // TODO: i18n
                    },
                    style = style
                )
                Spacer(modifier = Modifier.width(4.dp))
                Text(
                    text = "($versionsCount)",
                    style = style
                )
            }
        }
    )
}

@Composable
fun VersionItemLayout(
    version: Version,
    selected: Boolean,
    submitError: (String) -> Unit,
    modifier: Modifier = Modifier,
    onSelected: () -> Unit = {},
    onSettingsClick: () -> Unit = {},
    onRenameClick: () -> Unit = {},
    onCopyClick: () -> Unit = {},
    onDeleteClick: () -> Unit = {},
    onPinned: () -> Unit = {}
) {
    val context = LocalContext.current
    val (isCardBlurEnabled, cardAlpha, hazeState) = LocalCardLayoutConfig.current

    val interactionSource = remember { MutableInteractionSource() }
    val isPressed by interactionSource.collectIsPressedAsState()
    
    val scale by animateFloatAsState(
        targetValue = if (isPressed) 0.98f else if (selected) 1.02f else 1f,
        label = "ItemScale"
    )

    ShardCard(
        modifier = modifier
            .graphicsLayer(scaleX = scale, scaleY = scale)
            .selectableCard(isSelected = selected, isPressed = isPressed),
        onClick = {
            if (!selected) onSelected()
        },
        shape = RoundedCornerShape(20.dp),
        style = CardStyle.GLASS
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Box(
                modifier = Modifier
                    .size(12.dp)
                    .clip(CircleShape)
                    .background(
                        if (selected) MaterialTheme.colorScheme.primary 
                        else MaterialTheme.colorScheme.outline.copy(alpha = 0.3f)
                    )
            )
            
            Spacer(modifier = Modifier.width(16.dp))
            
            CommonVersionInfoLayout(
                modifier = Modifier.weight(1f),
                version = version
            )

            Row(verticalAlignment = Alignment.CenterVertically) {
                // Pin button
                IconButton(
                    onClick = {
                        val currentValue = version.pinnedState
                        runCatching {
                            version.setPinnedAndSave(!currentValue)
                        }.onFailure { e ->
                            lError("Failed to save version config!", e)
                            submitError("保存版本配置失败: ${e.getMessageOrToString()}")
                        }.onSuccess {
                            onPinned()
                        }
                    },
                    enabled = version.isValid()
                ) {
                    Icon(
                        modifier = Modifier.rotate(if (version.pinnedState) 0f else 45f),
                        imageVector = if (version.pinnedState) Icons.Default.PushPin else Icons.Default.PushPin,
                        contentDescription = "置顶",
                        tint = if (version.pinnedState) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.6f)
                    )
                }

                // Settings button
                IconButton(
                    onClick = onSettingsClick,
                    enabled = version.isValid()
                ) {
                    Icon(
                        modifier = Modifier.size(22.dp),
                        imageVector = Icons.Default.Settings,
                        contentDescription = "设置",
                        tint = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.8f)
                    )
                }

                // More menu
                var menuExpanded by remember { mutableStateOf(false) }
                Box {
                    IconButton(onClick = { menuExpanded = !menuExpanded }) {
                        Icon(
                            modifier = Modifier.size(22.dp),
                            imageVector = Icons.Default.MoreHoriz,
                            contentDescription = "更多",
                            tint = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.8f)
                        )
                    }

                    ShardDropdownMenu(
                        expanded = menuExpanded,
                        onDismissRequest = { menuExpanded = false }
                    ) {
                        DropdownMenuItem(
                            text = { Text(text = "重命名") },
                            leadingIcon = { Icon(Icons.Filled.Edit, contentDescription = null, modifier = Modifier.size(18.dp)) },
                            onClick = {
                                onRenameClick()
                                menuExpanded = false
                            }
                        )
                        DropdownMenuItem(
                            text = { Text(text = "复制") },
                            leadingIcon = { Icon(Icons.Filled.FileCopy, contentDescription = null, modifier = Modifier.size(18.dp)) },
                            onClick = {
                                onCopyClick()
                                menuExpanded = false
                            }
                        )
                        DropdownMenuItem(
                            text = { Text(text = "删除") },
                            leadingIcon = { Icon(Icons.Filled.Delete, contentDescription = null, modifier = Modifier.size(18.dp)) },
                            onClick = {
                                onDeleteClick()
                                menuExpanded = false
                            }
                        )
                    }
                }
            }
        }
    }
}

@Composable
fun CommonVersionInfoLayout(
    modifier: Modifier = Modifier,
    version: Version
) {
    Row(modifier = modifier) {
        VersionIconImage(
            modifier = Modifier
                .size(48.dp)
                .align(Alignment.CenterVertically),
            version = version
        )
        Spacer(modifier = Modifier.width(16.dp))
        Column(
            modifier = Modifier.weight(1f)
        ) {
            // Version name
            Text(
                modifier = Modifier.basicMarquee(iterations = Int.MAX_VALUE),
                maxLines = 1,
                text = version.getVersionName(),
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold
            )
            
            // Version summary if available
            if (version.isValid() && version.isSummaryValid()) {
                Text(
                    modifier = Modifier.basicMarquee(iterations = Int.MAX_VALUE),
                    maxLines = 1,
                    text = version.getVersionSummary(),
                    style = MaterialTheme.typography.bodyMedium
                )
            }
            
            // Version details
            Row(
                horizontalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                if (!version.isValid()) {
                    Surface(
                color = MaterialTheme.colorScheme.errorContainer,
                contentColor = MaterialTheme.colorScheme.onErrorContainer,
                shape = MaterialTheme.shapes.small
            ) {
                Text(
                    text = "无效", // TODO: i18n
                    style = MaterialTheme.typography.labelSmall,
                    modifier = Modifier.padding(horizontal = 8.dp, vertical = 2.dp)
                )
            }
                } else {
                    version.getVersionInfo()?.let { versionInfo ->
                        Text(
                            text = versionInfo.minecraftVersion,
                            style = MaterialTheme.typography.labelMedium,
                        )
                        versionInfo.loaderInfo?.let { loaderInfo ->
                            Text(
                                text = loaderInfo.loader.displayName,
                                style = MaterialTheme.typography.labelMedium
                            )
                            Text(
                                text = loaderInfo.version,
                                style = MaterialTheme.typography.labelMedium
                            )
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun VersionIconImage(
    version: Version?,
    modifier: Modifier = Modifier,
    refreshKey: Any? = null
) {
    val model = remember(version, refreshKey) {
        version?.let {
            val iconFile = VersionsManager.getVersionIconFile(it)
            when {
                iconFile.exists() -> iconFile
                else -> getLoaderIconRes(it)
            }
        } ?: R.drawable.img_minecraft
    }

    when (model) {
        is Int -> {
            androidx.compose.foundation.Image(
                painter = painterResource(id = model),
                contentDescription = null,
                contentScale = androidx.compose.ui.layout.ContentScale.Fit,
                modifier = modifier
            )
        }
        else -> {
            coil.compose.AsyncImage(
                model = model,
                contentDescription = null,
                contentScale = androidx.compose.ui.layout.ContentScale.Fit,
                modifier = modifier
            )
        }
    }
}

private fun getLoaderIconRes(version: Version): Int {
    return when (version.getVersionInfo()?.loaderInfo?.loader?.displayName) {
        "Fabric" -> R.drawable.img_loader_fabric
        "Forge" -> R.drawable.img_anvil
        "Quilt" -> R.drawable.img_loader_quilt
        "NeoForge" -> R.drawable.img_loader_neoforge
        "OptiFine" -> R.drawable.img_loader_optifine
        "LiteLoader" -> R.drawable.img_minecraft // 使用默认图标替代
        else -> R.drawable.img_minecraft
    }
}
