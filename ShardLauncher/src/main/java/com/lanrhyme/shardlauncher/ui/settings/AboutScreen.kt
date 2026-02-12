package com.lanrhyme.shardlauncher.ui.settings

import android.content.Intent
import android.net.Uri
import androidx.annotation.DrawableRes
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.tween
import androidx.compose.animation.expandVertically
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.shrinkVertically
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.interaction.collectIsPressedAsState
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.Article
import androidx.compose.material.icons.automirrored.filled.OpenInNew
import androidx.compose.material.icons.filled.Code
import androidx.compose.material.icons.filled.Computer
import androidx.compose.material.icons.filled.ContentCopy
import androidx.compose.material.icons.filled.Description
import androidx.compose.material.icons.filled.Groups
import androidx.compose.material.icons.filled.HdrWeak
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.filled.Link
import androidx.compose.material.icons.filled.Schedule
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material.icons.filled.ThumbUp
import androidx.compose.material.icons.filled.Update
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.scale
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalClipboardManager
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.AnnotatedString
import androidx.compose.ui.text.SpanStyle
import androidx.compose.ui.text.buildAnnotatedString
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.text.withStyle
import androidx.compose.ui.unit.dp
import com.lanrhyme.shardlauncher.R
import com.lanrhyme.shardlauncher.ui.components.basic.ScalingActionButton
import com.lanrhyme.shardlauncher.ui.components.basic.TitleAndSummary
import com.lanrhyme.shardlauncher.ui.components.basic.animatedAppearance
import com.lanrhyme.shardlauncher.ui.components.basic.glow
import com.lanrhyme.shardlauncher.ui.components.layout.LocalCardLayoutConfig
import com.lanrhyme.shardlauncher.ui.components.tiles.ActionTile
import com.lanrhyme.shardlauncher.ui.components.tiles.ContentTile
import com.lanrhyme.shardlauncher.ui.components.tiles.InfoTile
import com.lanrhyme.shardlauncher.ui.components.tiles.TileCard
import com.lanrhyme.shardlauncher.ui.components.tiles.TileGrid
import com.lanrhyme.shardlauncher.ui.components.tiles.TileGridScope
import com.lanrhyme.shardlauncher.ui.components.tiles.TileStyle

// ==================== 数据模型 ====================

data class OssLibrary(val name: String, val author: String, val url: String, val license: String)

data class CreditAction(val icon: ImageVector, val text: String? = null, val url: String)

data class CreditItem(
    @DrawableRes val image: Int? = null,
    val title: String,
    val summary: String,
    val actions: List<CreditAction>
)

data class ApiService(
    val name: String,
    val description: String,
    val url: String
)

// ==================== 主屏幕 ====================

@Composable
fun AboutScreen(animationSpeed: Float) {
    val context = LocalContext.current
    var showLicensesDialog by remember { mutableStateOf(false) }
    var expandedSection by remember { mutableStateOf<String?>(null) }

    if (showLicensesDialog) {
        LicensesDialog { showLicensesDialog = false }
    }

    // 使用 Row 实现左右分栏布局
    Row(
        modifier = Modifier
            .fillMaxSize()
            .padding(horizontal = 16.dp, vertical = 12.dp)
            // 底部 padding 避免被导航栏遮挡
            .padding(bottom = 80.dp)
    ) {
        // 左侧区域 - 版本信息（占比 35%）
        LeftPanel(
            modifier = Modifier
                .weight(0.35f)
                .fillMaxHeight(),
            animationSpeed = animationSpeed
        )

        Spacer(modifier = Modifier.width(16.dp))

        // 右侧区域 - 应用简介、快捷操作、贡献者、鸣谢、第三方服务等（占比 65%）
        RightPanel(
            modifier = Modifier
                .weight(0.65f)
                .fillMaxHeight(),
            animationSpeed = animationSpeed,
            showLicensesDialog = showLicensesDialog,
            onLicensesClick = { showLicensesDialog = true },
            onGithubClick = {
                context.startActivity(
                    Intent(Intent.ACTION_VIEW, Uri.parse("https://github.com/LanRhyme/ShardLauncher"))
                )
            },
            expandedSection = expandedSection,
            onToggleSection = { section ->
                expandedSection = if (expandedSection == section) null else section
            }
        )
    }
}

// ==================== 左侧面板 ====================

@Composable
private fun LeftPanel(
    modifier: Modifier = Modifier,
    animationSpeed: Float
) {
    val leftScrollState = rememberScrollState()

    // 左侧使用 Column，只显示版本信息
    Column(
        modifier = modifier.verticalScroll(leftScrollState),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        // 完整版本信息区域
        VersionInfoTile(animationSpeed = animationSpeed)
    }
}

// ==================== 右侧面板 ====================

@Composable
private fun RightPanel(
    modifier: Modifier = Modifier,
    animationSpeed: Float,
    showLicensesDialog: Boolean,
    onLicensesClick: () -> Unit,
    onGithubClick: () -> Unit,
    expandedSection: String?,
    onToggleSection: (String) -> Unit
) {
    val rightListState = rememberLazyListState()

    LazyColumn(
        state = rightListState,
        modifier = modifier,
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        // 头部区域 - 应用信息大磁贴
        item {
            AppHeaderTile(animationSpeed = animationSpeed)
        }

        // 快捷操作区域
        item {
            QuickActionsTile(
                animationSpeed = animationSpeed,
                onLicensesClick = onLicensesClick,
                onGithubClick = onGithubClick
            )
        }

        // 贡献者区域
        item {
            CreditsSection(
                animationSpeed = animationSpeed,
                isExpanded = expandedSection == "credits",
                onToggle = { onToggleSection("credits") }
            )
        }

        // 鸣谢区域
        item {
            ThanksSection(
                animationSpeed = animationSpeed,
                isExpanded = expandedSection == "thanks",
                onToggle = { onToggleSection("thanks") }
            )
        }

        // 第三方API区域
        item {
            ApiServicesTile(animationSpeed = animationSpeed)
        }

        // 底部留白
        item {
            Spacer(modifier = Modifier.height(8.dp))
        }
    }
}

// ==================== 头部磁贴 ====================

@Composable
private fun AppHeaderTile(animationSpeed: Float) {
    val context = LocalContext.current

    TileCard(
        modifier = Modifier
            .fillMaxWidth()
            .height(180.dp)
            .animatedAppearance(0, animationSpeed),
        style = TileStyle.GRADIENT
    ) {
        Row(
            modifier = Modifier.fillMaxSize(),
            verticalAlignment = Alignment.CenterVertically
        ) {
            // 左侧图标区域
            Box(
                modifier = Modifier
                    .size(120.dp)
                    .background(
                        color = Color.White.copy(alpha = 0.15f),
                        shape = RoundedCornerShape(20.dp)
                    ),
                contentAlignment = Alignment.Center
            ) {
                Image(
                    painter = painterResource(id = R.drawable.img_kotlin),
                    contentDescription = "App Icon",
                    modifier = Modifier.size(100.dp),
                    contentScale = ContentScale.Fit
                )
            }

            Spacer(modifier = Modifier.width(20.dp))

            // 右侧文字区域
            Column(
                modifier = Modifier.weight(1f)
            ) {
                Text(
                    text = "ShardLauncher",
                    style = MaterialTheme.typography.headlineSmall,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.onPrimary
                )
                Spacer(modifier = Modifier.height(8.dp))
                Text(
                    text = "一款使用 Kotlin 和 Jetpack Compose 开发的现代化 Android Minecraft: Java Edition 启动器",
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onPrimary.copy(alpha = 0.85f),
                    lineHeight = MaterialTheme.typography.bodyMedium.lineHeight * 1.2f
                )
                Spacer(modifier = Modifier.height(12.dp))
                Row(
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    Surface(
                        shape = RoundedCornerShape(8.dp),
                        color = Color.White.copy(alpha = 0.2f)
                    ) {
                        Text(
                            text = "GPL-3.0",
                            modifier = Modifier.padding(horizontal = 10.dp, vertical = 4.dp),
                            style = MaterialTheme.typography.labelSmall,
                            color = MaterialTheme.colorScheme.onPrimary
                        )
                    }
                    Surface(
                        shape = RoundedCornerShape(8.dp),
                        color = Color.White.copy(alpha = 0.2f)
                    ) {
                        Text(
                            text = "开源",
                            modifier = Modifier.padding(horizontal = 10.dp, vertical = 4.dp),
                            style = MaterialTheme.typography.labelSmall,
                            color = MaterialTheme.colorScheme.onPrimary
                        )
                    }
                }
            }
        }
    }
}

// ==================== 快捷操作磁贴 ====================

@Composable
private fun QuickActionsTile(
    animationSpeed: Float,
    onLicensesClick: () -> Unit,
    onGithubClick: () -> Unit
) {
    Column(
        modifier = Modifier.animatedAppearance(1, animationSpeed)
    ) {
        Text(
            text = "快捷操作",
            style = MaterialTheme.typography.titleSmall,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            modifier = Modifier.padding(start = 4.dp, bottom = 8.dp)
        )

        TileGrid(
            columns = 4,
            horizontalSpacing = 10.dp,
            verticalSpacing = 10.dp
        ) {
            // 文档按钮
            item(1f) {
                ActionTile(
                    title = "文档",
                    icon = Icons.AutoMirrored.Filled.Article,
                    onClick = { /* TODO: 打开文档 */ },
                    style = TileStyle.DEFAULT
                )
            }
            // GitHub按钮
            item(1f) {
                ActionTile(
                    title = "GitHub",
                    icon = Icons.Default.Code,
                    onClick = onGithubClick,
                    style = TileStyle.ACCENT
                )
            }
            // 许可证按钮
            item(1f) {
                ActionTile(
                    title = "许可",
                    icon = Icons.Default.Description,
                    onClick = onLicensesClick,
                    style = TileStyle.DEFAULT
                )
            }
            // 检查更新按钮
            item(1f) {
                ActionTile(
                    title = "更新",
                    icon = Icons.Default.Update,
                    onClick = { /* TODO: 检查更新 */ },
                    style = TileStyle.DEFAULT
                )
            }
        }
    }
}

// ==================== 版本信息磁贴 ====================

@Composable
private fun VersionInfoTile(animationSpeed: Float) {
    val clipboardManager = LocalClipboardManager.current

    val versionName = stringResource(id = R.string.version_name)
    val gitHash = stringResource(id = R.string.git_hash)
    val gitBranch = stringResource(id = R.string.git_branch)
    val buildStatus = stringResource(id = R.string.build_status)
    val lastUpdateTime = stringResource(id = R.string.last_update_time)

    Column(
        modifier = Modifier.animatedAppearance(0, animationSpeed)
    ) {
        Text(
            text = "版本信息",
            style = MaterialTheme.typography.titleSmall,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            modifier = Modifier.padding(start = 4.dp, bottom = 8.dp)
        )

        TileCard(
            modifier = Modifier.fillMaxWidth()
        ) {
            Column(
                modifier = Modifier.fillMaxWidth(),
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                // 版本号行
                VersionInfoRow(
                    icon = Icons.Default.Info,
                    label = "版本号",
                    value = versionName,
                    onCopy = {
                        clipboardManager.setText(AnnotatedString(versionName))
                    }
                )

                HorizontalDivider(
                    color = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.3f)
                )

                // Git Commit行
                VersionInfoRow(
                    icon = Icons.Default.Code,
                    label = "Git Commit",
                    value = gitHash,
                    valueColor = MaterialTheme.colorScheme.primary,
                    onCopy = {
                        clipboardManager.setText(AnnotatedString(gitHash))
                    }
                )

                HorizontalDivider(
                    color = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.3f)
                )

                // 分支行
                VersionInfoRow(
                    icon = Icons.Default.HdrWeak,
                    label = "分支",
                    value = gitBranch,
                    onCopy = {
                        clipboardManager.setText(AnnotatedString(gitBranch))
                    }
                )

                HorizontalDivider(
                    color = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.3f)
                )

                // 构建状态行
                VersionInfoRow(
                    icon = Icons.Default.Settings,
                    label = "构建状态",
                    value = buildStatus,
                    onCopy = null
                )

                HorizontalDivider(
                    color = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.3f)
                )

                // 更新时间行
                VersionInfoRow(
                    icon = Icons.Default.Schedule,
                    label = "上次更新",
                    value = lastUpdateTime,
                    onCopy = null
                )
            }
        }
    }
}

@Composable
private fun VersionInfoRow(
    icon: ImageVector,
    label: String,
    value: String,
    modifier: Modifier = Modifier,
    valueColor: Color = MaterialTheme.colorScheme.onSurface,
    onCopy: (() -> Unit)?
) {
    Row(
        modifier = modifier
            .fillMaxWidth()
            .padding(vertical = 4.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.SpaceBetween
    ) {
        Row(
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            Icon(
                imageVector = icon,
                contentDescription = null,
                modifier = Modifier.size(20.dp),
                tint = MaterialTheme.colorScheme.primary.copy(alpha = 0.8f)
            )
            Text(
                text = label,
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }

        Row(
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            Text(
                text = value,
                style = MaterialTheme.typography.bodyMedium,
                fontWeight = FontWeight.Medium,
                color = valueColor,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis
            )
            if (onCopy != null) {
                Surface(
                    shape = RoundedCornerShape(6.dp),
                    color = MaterialTheme.colorScheme.surfaceVariant,
                    modifier = Modifier.clickable(onClick = onCopy)
                ) {
                    Icon(
                        imageVector = Icons.Default.ContentCopy,
                        contentDescription = "复制",
                        modifier = Modifier
                            .size(24.dp)
                            .padding(4.dp),
                        tint = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }
        }
    }
}

// ==================== 贡献者区域 ====================

@Composable
private fun CreditsSection(
    animationSpeed: Float,
    isExpanded: Boolean,
    onToggle: () -> Unit
) {
    val context = LocalContext.current

    Column(
        modifier = Modifier.animatedAppearance(3, animationSpeed)
    ) {
        // 标题栏
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .clickable(onClick = onToggle)
                .padding(4.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                Icon(
                    imageVector = Icons.Default.Groups,
                    contentDescription = null,
                    modifier = Modifier.size(20.dp),
                    tint = MaterialTheme.colorScheme.primary
                )
                Text(
                    text = "贡献者",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.SemiBold
                )
            }

            val rotation by animateFloatAsState(
                targetValue = if (isExpanded) 180f else 0f,
                label = "expand_rotation"
            )
            Icon(
                imageVector = Icons.Default.Update,
                contentDescription = if (isExpanded) "收起" else "展开",
                modifier = Modifier.size(20.dp),
                tint = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }

        Spacer(modifier = Modifier.height(8.dp))

        AnimatedVisibility(
            visible = isExpanded,
            enter = expandVertically() + fadeIn(),
            exit = shrinkVertically() + fadeOut()
        ) {
            // 主要开发者磁贴
            CreditPersonTile(
                image = R.drawable.img_lanrhyme,
                name = "LanRhyme",
                role = "项目发起者，主要开发者",
                onGithubClick = {
                    context.startActivity(
                        Intent(Intent.ACTION_VIEW, Uri.parse("https://github.com/LanRhyme"))
                    )
                },
                onWebsiteClick = {
                    context.startActivity(
                        Intent(Intent.ACTION_VIEW, Uri.parse("https://lanrhyme.netlify.app/"))
                    )
                }
            )
        }

        if (!isExpanded) {
            // 收起状态显示预览
            CreditPersonPreview(
                image = R.drawable.img_lanrhyme,
                name = "LanRhyme",
                role = "项目发起者，主要开发者"
            )
        }
    }
}

@Composable
private fun CreditPersonTile(
    @DrawableRes image: Int,
    name: String,
    role: String,
    onGithubClick: () -> Unit,
    onWebsiteClick: () -> Unit
) {
    TileCard(
        modifier = Modifier.fillMaxWidth(),
        style = TileStyle.GLASS
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically
        ) {
            // 头像
            Image(
                painter = painterResource(id = image),
                contentDescription = name,
                modifier = Modifier
                    .size(64.dp)
                    .clip(CircleShape),
                contentScale = ContentScale.Crop
            )

            Spacer(modifier = Modifier.width(16.dp))

            // 信息
            Column(
                modifier = Modifier.weight(1f)
            ) {
                Text(
                    text = name,
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.SemiBold
                )
                Spacer(modifier = Modifier.height(2.dp))
                Text(
                    text = role,
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }

            // 操作按钮
            Row(
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                Surface(
                    shape = RoundedCornerShape(10.dp),
                    color = MaterialTheme.colorScheme.primary.copy(alpha = 0.1f),
                    modifier = Modifier.clickable(onClick = onGithubClick)
                ) {
                    Row(
                        modifier = Modifier.padding(horizontal = 12.dp, vertical = 8.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(4.dp)
                    ) {
                        Icon(
                            imageVector = Icons.Default.Code,
                            contentDescription = null,
                            modifier = Modifier.size(16.dp),
                            tint = MaterialTheme.colorScheme.primary
                        )
                        Text(
                            text = "GitHub",
                            style = MaterialTheme.typography.labelMedium,
                            color = MaterialTheme.colorScheme.primary
                        )
                    }
                }

                Surface(
                    shape = RoundedCornerShape(10.dp),
                    color = MaterialTheme.colorScheme.secondary.copy(alpha = 0.1f),
                    modifier = Modifier.clickable(onClick = onWebsiteClick)
                ) {
                    Row(
                        modifier = Modifier.padding(horizontal = 12.dp, vertical = 8.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(4.dp)
                    ) {
                        Icon(
                            imageVector = Icons.Default.Computer,
                            contentDescription = null,
                            modifier = Modifier.size(16.dp),
                            tint = MaterialTheme.colorScheme.secondary
                        )
                        Text(
                            text = "网站",
                            style = MaterialTheme.typography.labelMedium,
                            color = MaterialTheme.colorScheme.secondary
                        )
                    }
                }
            }
        }
    }
}

@Composable
private fun CreditPersonPreview(
    @DrawableRes image: Int,
    name: String,
    role: String
) {
    TileCard(
        modifier = Modifier.fillMaxWidth()
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Image(
                painter = painterResource(id = image),
                contentDescription = name,
                modifier = Modifier
                    .size(48.dp)
                    .clip(CircleShape),
                contentScale = ContentScale.Crop
            )

            Spacer(modifier = Modifier.width(12.dp))

            Column(
                modifier = Modifier.weight(1f)
            ) {
                Text(
                    text = name,
                    style = MaterialTheme.typography.bodyMedium,
                    fontWeight = FontWeight.Medium
                )
                Text(
                    text = role,
                    style = MaterialTheme.typography.labelSmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }

            Icon(
                imageVector = Icons.AutoMirrored.Filled.OpenInNew,
                contentDescription = null,
                modifier = Modifier.size(18.dp),
                tint = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.5f)
            )
        }
    }
}

// ==================== 鸣谢区域 ====================

@Composable
private fun ThanksSection(
    animationSpeed: Float,
    isExpanded: Boolean,
    onToggle: () -> Unit
) {
    val context = LocalContext.current

    Column(
        modifier = Modifier.animatedAppearance(4, animationSpeed)
    ) {
        // 标题栏
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .clickable(onClick = onToggle)
                .padding(4.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                Icon(
                    imageVector = Icons.Default.ThumbUp,
                    contentDescription = null,
                    modifier = Modifier.size(20.dp),
                    tint = MaterialTheme.colorScheme.primary
                )
                Text(
                    text = "鸣谢",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.SemiBold
                )
            }
        }

        Spacer(modifier = Modifier.height(8.dp))

        // ZalithLauncher磁贴
        ThanksTile(
            name = "ZalithLauncher2",
            description = "参考和引用了 ZalithLauncher2 的部分代码",
            onClick = {
                context.startActivity(
                    Intent(Intent.ACTION_VIEW, Uri.parse("https://github.com/ZalithLauncher/ZalithLauncher2"))
                )
            }
        )
    }
}

@Composable
private fun ThanksTile(
    name: String,
    description: String,
    onClick: () -> Unit
) {
    TileCard(
        modifier = Modifier.fillMaxWidth(),
        onClick = onClick,
        style = TileStyle.GLASS
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically
        ) {
            // 项目图标
            Surface(
                modifier = Modifier.size(48.dp),
                shape = RoundedCornerShape(12.dp),
                color = MaterialTheme.colorScheme.primary.copy(alpha = 0.15f)
            ) {
                Box(
                    modifier = Modifier.fillMaxSize(),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = Icons.Default.Code,
                        contentDescription = null,
                        modifier = Modifier.size(24.dp),
                        tint = MaterialTheme.colorScheme.primary
                    )
                }
            }

            Spacer(modifier = Modifier.width(16.dp))

            Column(
                modifier = Modifier.weight(1f)
            ) {
                Text(
                    text = name,
                    style = MaterialTheme.typography.titleSmall,
                    fontWeight = FontWeight.Medium
                )
                Spacer(modifier = Modifier.height(2.dp))
                Text(
                    text = description,
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }

            Icon(
                imageVector = Icons.AutoMirrored.Filled.OpenInNew,
                contentDescription = null,
                modifier = Modifier.size(20.dp),
                tint = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.6f)
            )
        }
    }
}

// ==================== API服务磁贴 ====================

@Composable
private fun ApiServicesTile(animationSpeed: Float) {
    val context = LocalContext.current

    val apiServices = remember {
        listOf(
            ApiService(
                name = "BMCLAPI",
                description = "提供 Minecraft 版本和资源下载服务",
                url = "https://bmclapidoc.bangbang93.com/"
            ),
            ApiService(
                name = "新闻主页",
                description = "启动器主页中的 Minecraft 更新卡片",
                url = "https://news.bugjump.net/static/"
            ),
            ApiService(
                name = "星之阁 API",
                description = "提供皮肤模型图的获取服务",
                url = "https://api.xingzhige.com/doc/35"
            ),
            ApiService(
                name = "crafatar.com",
                description = "用于皮肤和头像的获取服务",
                url = "https://crafatar.com/"
            )
        )
    }

    Column(
        modifier = Modifier.animatedAppearance(5, animationSpeed)
    ) {
        Text(
            text = "第三方服务",
            style = MaterialTheme.typography.titleSmall,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            modifier = Modifier.padding(start = 4.dp, bottom = 8.dp)
        )

        TileCard(
            modifier = Modifier.fillMaxWidth()
        ) {
            Column(
                modifier = Modifier.fillMaxWidth(),
                verticalArrangement = Arrangement.spacedBy(4.dp)
            ) {
                apiServices.forEachIndexed { index, service ->
                    ApiServiceRow(
                        service = service,
                        onClick = {
                            context.startActivity(
                                Intent(Intent.ACTION_VIEW, Uri.parse(service.url))
                            )
                        }
                    )
                    if (index < apiServices.size - 1) {
                        HorizontalDivider(
                            modifier = Modifier.padding(vertical = 4.dp),
                            color = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.2f)
                        )
                    }
                }
            }
        }
    }
}

@Composable
private fun ApiServiceRow(
    service: ApiService,
    onClick: () -> Unit
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clickable(onClick = onClick)
            .padding(vertical = 8.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.SpaceBetween
    ) {
        Row(
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            Icon(
                imageVector = Icons.Default.Link,
                contentDescription = null,
                modifier = Modifier.size(18.dp),
                tint = MaterialTheme.colorScheme.primary.copy(alpha = 0.7f)
            )
            Column {
                Text(
                    text = service.name,
                    style = MaterialTheme.typography.bodyMedium,
                    fontWeight = FontWeight.Medium
                )
                Text(
                    text = service.description,
                    style = MaterialTheme.typography.labelSmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        }

        Icon(
            imageVector = Icons.AutoMirrored.Filled.OpenInNew,
            contentDescription = null,
            modifier = Modifier.size(16.dp),
            tint = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.5f)
        )
    }
}

// ==================== 许可证对话框 ====================

@Composable
private fun LicensesDialog(onDismiss: () -> Unit) {
    val context = LocalContext.current
    val libraries = remember {
        listOf(
            OssLibrary(
                "Jetpack Compose",
                "Google",
                "https://developer.android.com/jetpack/compose",
                "Apache License 2.0"
            ),
            OssLibrary(
                "AndroidX",
                "Google",
                "https://source.android.com/",
                "Apache License 2.0"
            ),
            OssLibrary(
                "Coil",
                "Coil Contributors",
                "https://coil-kt.github.io/coil/",
                "Apache License 2.0"
            ),
            OssLibrary(
                "Retrofit",
                "Square, Inc.",
                "https://square.github.io/retrofit/",
                "Apache License 2.0"
            ),
            OssLibrary(
                "Gson",
                "Google",
                "https://github.com/google/gson",
                "Apache License 2.0"
            ),
            OssLibrary(
                "PCL2-NewsHomepage",
                "Light-Beacon",
                "https://github.com/Light-Beacon/PCL2-NewsHomepage",
                "MIT License"
            ),
            OssLibrary(
                "ZalithLauncher2",
                "ZalithLauncher",
                "https://github.com/ZalithLauncher/ZalithLauncher2",
                "GNU GPL v3.0"
            ),
        )
    }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                Surface(
                    shape = RoundedCornerShape(10.dp),
                    color = MaterialTheme.colorScheme.primary.copy(alpha = 0.15f)
                ) {
                    Icon(
                        imageVector = Icons.Default.Description,
                        contentDescription = null,
                        modifier = Modifier
                            .size(40.dp)
                            .padding(8.dp),
                        tint = MaterialTheme.colorScheme.primary
                    )
                }
                Column {
                    Text(
                        text = "开源许可",
                        style = MaterialTheme.typography.titleLarge,
                        fontWeight = FontWeight.Bold
                    )
                    Text(
                        text = "感谢这些优秀的开源项目",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }
        },
        text = {
            LazyColumn(
                verticalArrangement = Arrangement.spacedBy(8.dp),
                modifier = Modifier.heightIn(max = 400.dp)
            ) {
                items(libraries) { lib ->
                    LicenseItem(library = lib) {
                        context.startActivity(
                            Intent(Intent.ACTION_VIEW, Uri.parse(lib.url))
                        )
                    }
                }
            }
        },
        confirmButton = {
            TextButton(onClick = onDismiss) {
                Text("关闭")
            }
        },
        shape = RoundedCornerShape(20.dp)
    )
}

@Composable
private fun LicenseItem(
    library: OssLibrary,
    onClick: () -> Unit
) {
    Surface(
        shape = RoundedCornerShape(12.dp),
        color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f),
        modifier = Modifier.clickable(onClick = onClick)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(12.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Column(
                modifier = Modifier.weight(1f)
            ) {
                Text(
                    text = library.name,
                    style = MaterialTheme.typography.bodyMedium,
                    fontWeight = FontWeight.SemiBold
                )
                Spacer(modifier = Modifier.height(2.dp))
                Text(
                    text = "by ${library.author}",
                    style = MaterialTheme.typography.labelSmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
                Surface(
                    shape = RoundedCornerShape(4.dp),
                    color = MaterialTheme.colorScheme.primary.copy(alpha = 0.1f),
                    modifier = Modifier.padding(top = 4.dp)
                ) {
                    Text(
                        text = library.license,
                        style = MaterialTheme.typography.labelSmall,
                        color = MaterialTheme.colorScheme.primary,
                        modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                    )
                }
            }

            Icon(
                imageVector = Icons.AutoMirrored.Filled.OpenInNew,
                contentDescription = null,
                modifier = Modifier.size(18.dp),
                tint = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.5f)
            )
        }
    }
}