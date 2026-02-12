package com.lanrhyme.shardlauncher.ui.settings

import android.content.Intent
import android.net.Uri
import androidx.annotation.DrawableRes
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.Image
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
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
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.OpenInNew
import androidx.compose.material.icons.filled.Code
import androidx.compose.material.icons.filled.Computer
import androidx.compose.material.icons.filled.ContentCopy
import androidx.compose.material.icons.filled.Description
import androidx.compose.material.icons.filled.Group
import androidx.compose.material.icons.filled.Link
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalClipboardManager
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.AnnotatedString
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.lanrhyme.shardlauncher.R
import com.lanrhyme.shardlauncher.ui.components.basic.ButtonType
import com.lanrhyme.shardlauncher.ui.components.basic.PopupContainer
import com.lanrhyme.shardlauncher.ui.components.basic.ShardButton
import com.lanrhyme.shardlauncher.ui.components.basic.ShardGlassCard
import com.lanrhyme.shardlauncher.ui.components.basic.ShardSectionHeader
import com.lanrhyme.shardlauncher.ui.components.basic.ShardTag
import com.lanrhyme.shardlauncher.ui.components.basic.animatedAppearance
import com.lanrhyme.shardlauncher.ui.components.tiles.TileCard
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
fun AboutScreen() {
    val animationSpeed = 1.0f
    
    Row(
        modifier = Modifier
            .fillMaxSize()
            .padding(20.dp),
        horizontalArrangement = Arrangement.spacedBy(20.dp)
    ) {
        // Left Panel (32%): Version info & basic identity
        LeftPanel(
            modifier = Modifier
                .weight(0.32f)
                .fillMaxHeight(),
            animationSpeed = animationSpeed
        )

        // Right Panel (68%): Detailed info, credits, licenses
        RightPanel(
            modifier = Modifier
                .weight(0.68f)
                .fillMaxHeight(),
            animationSpeed = animationSpeed
        )
    }
}

// ==================== 左侧面板 ====================

@Composable
private fun LeftPanel(
    modifier: Modifier = Modifier,
    animationSpeed: Float
) {
    Column(
        modifier = modifier.verticalScroll(rememberScrollState()),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        VersionInfoTile(animationSpeed = animationSpeed)
    }
}

@Composable
private fun RightPanel(
    modifier: Modifier = Modifier,
    animationSpeed: Float
) {
    val context = LocalContext.current
    var showLicenses by remember { mutableStateOf(false) }
    
    LazyColumn(
        modifier = modifier,
        verticalArrangement = Arrangement.spacedBy(20.dp)
    ) {
        // Item indices for animatedAppearance are used to sequence the entrance
        item {
            AppHeaderTile(animationSpeed = animationSpeed)
        }

        item {
            QuickActionsTile(
                animationSpeed = animationSpeed,
                onLicensesClick = { showLicenses = true },
                onGithubClick = {
                    context.startActivity(
                        Intent(Intent.ACTION_VIEW, Uri.parse("https://github.com/LanRhyme/ShardLauncher"))
                    )
                }
            )
        }

        item {
            CreditsSection(animationSpeed = animationSpeed)
        }

        item {
            ThanksSection(animationSpeed = animationSpeed)
        }

        item {
            ApiServicesTile(animationSpeed = animationSpeed)
        }

        item {
            FooterSection(animationSpeed = animationSpeed)
            Spacer(modifier = Modifier.height(20.dp))
        }
    }
    
    if (showLicenses) {
        LicensesDialog(onDismiss = { showLicenses = false })
    }
}

// ==================== 头部磁贴 ====================

@Composable
private fun AppHeaderTile(animationSpeed: Float) {
    ShardGlassCard(
        modifier = Modifier
            .fillMaxWidth()
            .animatedAppearance(0, animationSpeed)
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically
        ) {
            // App Icon with glass background
            Surface(
                modifier = Modifier.size(120.dp),
                shape = RoundedCornerShape(28.dp),
                color = MaterialTheme.colorScheme.primary.copy(alpha = 0.08f)
            ) {
                Box(contentAlignment = Alignment.Center) {
                    Image(
                        painter = painterResource(id = R.drawable.img_logo),
                        contentDescription = "App Icon",
                        modifier = Modifier.size(80.dp),
                        contentScale = ContentScale.Fit
                    )
                }
            }

            Spacer(modifier = Modifier.width(24.dp))

            // Text Info
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = "Shard Launcher",
                    style = MaterialTheme.typography.headlineSmall,
                    fontWeight = FontWeight.Black,
                    color = MaterialTheme.colorScheme.onSurface
                )
                Spacer(modifier = Modifier.height(4.dp))
                Text(
                    text = "现代化 Android Minecraft 启动器",
                    style = MaterialTheme.typography.titleSmall,
                    color = MaterialTheme.colorScheme.primary,
                    fontWeight = FontWeight.Bold
                )
                Spacer(modifier = Modifier.height(12.dp))
                Text(
                    text = "基于 Kotlin 与 Jetpack Compose。追求极简与美感的设计，为您带来最纯粹的启动体验。",
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    lineHeight = 22.sp
                )
                
                Spacer(modifier = Modifier.height(16.dp))
                
                Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    ShardTag(text = "GPL-3.0", containerColor = MaterialTheme.colorScheme.secondary.copy(alpha = 0.15f), contentColor = MaterialTheme.colorScheme.secondary)
                    ShardTag(text = "Open Source")
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
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .animatedAppearance(1, animationSpeed),
        horizontalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        QuickActionButton(
            modifier = Modifier.weight(1f),
            icon = Icons.Default.Code,
            title = "GitHub",
            subtitle = "查看源代码",
            onClick = onGithubClick,
            color = Color(0xFF24292E)
        )
        QuickActionButton(
            modifier = Modifier.weight(1f),
            icon = Icons.Default.Description,
            title = "开源许可",
            subtitle = "第三方库授权",
            onClick = onLicensesClick,
            color = MaterialTheme.colorScheme.primary
        )
    }
}

@Composable
private fun QuickActionButton(
    modifier: Modifier = Modifier,
    icon: ImageVector,
    title: String,
    subtitle: String,
    onClick: () -> Unit,
    color: Color
) {
    ShardGlassCard(
        modifier = modifier,
        onClick = onClick
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            Surface(
                shape = RoundedCornerShape(12.dp),
                color = color.copy(alpha = 0.1f),
                modifier = Modifier.size(44.dp)
            ) {
                Box(contentAlignment = Alignment.Center) {
                    Icon(
                        imageVector = icon,
                        contentDescription = null,
                        modifier = Modifier.size(20.dp),
                        tint = color
                    )
                }
            }
            Column {
                Text(
                    text = title,
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold
                )
                Text(
                    text = subtitle,
                    style = MaterialTheme.typography.labelSmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        }
    }
}

// ==================== 版本信息磁贴 ====================

@Composable
private fun VersionInfoTile(animationSpeed: Float) {
    val clipboardManager = LocalClipboardManager.current
    val context = LocalContext.current

    val versionName = stringResource(id = R.string.version_name)
    val gitHash = stringResource(id = R.string.git_hash)
    val gitBranch = stringResource(id = R.string.git_branch)
    val buildStatus = stringResource(id = R.string.build_status)

    ShardGlassCard(
        modifier = Modifier.animatedAppearance(0, animationSpeed)
    ) {
        Column(verticalArrangement = Arrangement.spacedBy(16.dp)) {
            ShardSectionHeader(title = "编译信息")
            
            Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                VersionInfoRow(
                    label = "应用版本",
                    value = versionName,
                    onClick = { clipboardManager.setText(AnnotatedString(versionName)) }
                )
                VersionInfoRow(
                    label = "Git Commit",
                    value = gitHash,
                    onClick = { clipboardManager.setText(AnnotatedString(gitHash)) }
                )
                VersionInfoRow(
                    label = "构建分支",
                    value = gitBranch,
                    onClick = { clipboardManager.setText(AnnotatedString(gitBranch)) }
                )
                VersionInfoRow(
                    label = "状态",
                    value = buildStatus,
                    color = if (buildStatus.contains("release", true)) Color(0xFF4CAF50) else MaterialTheme.colorScheme.primary
                )
            }
        }
    }
}

@Composable
private fun VersionInfoRow(
    label: String,
    value: String,
    color: Color = MaterialTheme.colorScheme.onSurface,
    onClick: (() -> Unit)? = null
) {
    Surface(
        onClick = onClick ?: {},
        enabled = onClick != null,
        shape = RoundedCornerShape(12.dp),
        color = Color.Transparent
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(vertical = 4.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = label,
                style = MaterialTheme.typography.labelMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
            Text(
                text = value,
                style = MaterialTheme.typography.labelSmall,
                fontWeight = FontWeight.Bold,
                color = color,
                fontFamily = FontFamily.Monospace,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis
            )
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
    VersionInfoRowContent(
        iconProvider = {
            Icon(
                imageVector = icon,
                contentDescription = null,
                modifier = Modifier.size(20.dp),
                tint = MaterialTheme.colorScheme.primary.copy(alpha = 0.8f)
            )
        },
        label = label,
        value = value,
        modifier = modifier,
        valueColor = valueColor,
        onCopy = onCopy
    )
}

@Composable
private fun VersionInfoRow(
    icon: androidx.compose.ui.graphics.painter.Painter,
    label: String,
    value: String,
    modifier: Modifier = Modifier,
    valueColor: Color = MaterialTheme.colorScheme.onSurface,
    onCopy: (() -> Unit)?
) {
    VersionInfoRowContent(
        iconProvider = {
            Icon(
                painter = icon,
                contentDescription = null,
                modifier = Modifier.size(20.dp),
                tint = MaterialTheme.colorScheme.primary.copy(alpha = 0.8f)
            )
        },
        label = label,
        value = value,
        modifier = modifier,
        valueColor = valueColor,
        onCopy = onCopy
    )
}

@Composable
private fun VersionInfoRowContent(
    iconProvider: @Composable () -> Unit,
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
            iconProvider()
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
                    modifier = Modifier.clickable(
                        interactionSource = remember { MutableInteractionSource() },
                        indication = null,
                        onClick = onCopy
                    )
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
private fun CreditsSection(animationSpeed: Float) {
    ShardGlassCard(
        modifier = Modifier.animatedAppearance(3, animationSpeed)
    ) {
        Column(verticalArrangement = Arrangement.spacedBy(16.dp)) {
            ShardSectionHeader(title = "核心团队")
            
            Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                CreditPersonTile(
                    image = R.drawable.img_lanrhyme,
                    name = "LanRhyme",
                    role = "Lead Developer & UI Designer",
                    GithubUrl = "https://github.com/LanRhyme",
                    WebsiteUrl = "https://lanrhyme.netlify.app"
                )
                CreditPersonTile(
                    image = R.drawable.img_herbrine8403,
                    name = "爱科技的学生党",
                    role = "联合开发者，项目维护者",
                    GithubUrl = "https://github.com/herbrine8403"
                )
            }
        }
    }
}

@Composable
private fun CreditPersonTile(
    @DrawableRes image: Int,
    name: String,
    role: String,
    GithubUrl : String? = null,
    BiliBiliUrl: String? = null,
    WebsiteUrl: String? = null
) {
    val context = LocalContext.current
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
                    .size(48.dp)
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
                horizontalArrangement = Arrangement.spacedBy(8.dp),
            ) {
                if (GithubUrl != null) {
                    Surface(
                        shape = RoundedCornerShape(10.dp),
                        color = MaterialTheme.colorScheme.primary.copy(alpha = 0.1f),
                        modifier = Modifier.clickable(
                            interactionSource = remember { MutableInteractionSource() },
                            indication = null,
                            onClick = {
                                context.startActivity(
                                    Intent(Intent.ACTION_VIEW, Uri.parse(GithubUrl))
                                )
                            }
                        )
                    ) {
                        Row(
                            modifier = Modifier.padding(horizontal = 12.dp, vertical = 8.dp),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(4.dp)
                        ) {
                            Icon(
                                painter = painterResource(id = R.drawable.icon_github),
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
                }
                if (BiliBiliUrl != null) {
                    Surface(
                        shape = RoundedCornerShape(10.dp),
                        color = MaterialTheme.colorScheme.primary.copy(alpha = 0.1f),
                        modifier = Modifier.clickable(
                            interactionSource = remember { MutableInteractionSource() },
                            indication = null,
                            onClick = {
                                context.startActivity(
                                    Intent(Intent.ACTION_VIEW, Uri.parse(BiliBiliUrl))
                                )
                            }
                        )
                    ) {
                        Row(
                            modifier = Modifier.padding(horizontal = 12.dp, vertical = 8.dp),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(4.dp)
                        ) {
                            Icon(
                                painter = painterResource(id = R.drawable.icon_bilibili),
                                contentDescription = null,
                                modifier = Modifier.size(16.dp),
                                tint = MaterialTheme.colorScheme.primary
                            )
                            Text(
                                text = "BiliBili",
                                style = MaterialTheme.typography.labelMedium,
                                color = MaterialTheme.colorScheme.primary
                            )
                        }
                    }
                }
                if (WebsiteUrl != null) {
                    Surface(
                        shape = RoundedCornerShape(10.dp),
                        color = MaterialTheme.colorScheme.secondary.copy(alpha = 0.1f),
                        modifier = Modifier.clickable(
                            interactionSource = remember { MutableInteractionSource() },
                            indication = null,
                            onClick = {
                                context.startActivity(
                                    Intent(Intent.ACTION_VIEW, Uri.parse(WebsiteUrl))
                                )
                            }
                        )
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
}

// ==================== 鸣谢区域 ====================

@Composable
private fun ThanksSection(animationSpeed: Float) {
    ShardGlassCard(
        modifier = Modifier.animatedAppearance(4, animationSpeed)
    ) {
        Column(verticalArrangement = Arrangement.spacedBy(16.dp)) {
            ShardSectionHeader(title = "鸣谢")
            
            ThanksTile(
                icon = R.drawable.img_zalithlauncher,
                name = "ZalithLauncher",
                description = "参考和引用了 ZalithLauncher 的代码",
                onClick = { /* Open GitHub */ }
            )
        }
    }
}

@Composable
private fun ThanksTile(
    name: String,
    description: String,
    @DrawableRes icon: Int? = null,
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
            if (icon != null) {
                Image(
                    painter = painterResource(id = icon),
                    contentDescription = null,
                    modifier = Modifier
                        .size(48.dp)
                        .clip(shape = RoundedCornerShape(12.dp)),
                )
            }
           else {
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
            }

            Spacer(modifier = Modifier.width(16.dp))

            // 项目信息
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
                    text = description,
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }

            Icon(
                imageVector = Icons.AutoMirrored.Filled.OpenInNew,
                contentDescription = null,
                modifier = Modifier.size(20.dp),
                tint = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.5f)
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
            ApiService("BMCLAPI", "提供 Minecraft 版本和资源下载服务", "https://bmclapidoc.bangbang93.com/"),
            ApiService("新闻主页", "启动器主页中的 Minecraft 更新卡片", "https://news.bugjump.net/static/")
        )
    }

    ShardGlassCard(
        modifier = Modifier.animatedAppearance(5, animationSpeed)
    ) {
        Column(verticalArrangement = Arrangement.spacedBy(16.dp)) {
            ShardSectionHeader(title = "第三方服务")
            
            Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                apiServices.forEach { service ->
                    ApiServiceRow(
                        service = service,
                        onClick = {
                            context.startActivity(Intent(Intent.ACTION_VIEW, Uri.parse(service.url)))
                        }
                    )
                }
            }
        }
    }
}

@Composable
private fun FooterSection(animationSpeed: Float) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 32.dp)
            .animatedAppearance(6, animationSpeed),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        Text(
            text = "© 2024-2025 LanRhyme. All Rights Reserved.",
            style = MaterialTheme.typography.labelSmall,
            color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.5f)
        )
        
        Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
            SocialButton(Icons.Default.Code) { /* GitHub */ }
            SocialButton(Icons.Default.Group) { /* Team */ }
            SocialButton(Icons.Default.Link) { /* Website */ }
        }
    }
}

@Composable
private fun SocialButton(icon: ImageVector, onClick: () -> Unit) {
    Surface(
        onClick = onClick,
        shape = CircleShape,
        color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f),
        modifier = Modifier.size(44.dp)
    ) {
        Box(contentAlignment = Alignment.Center) {
            Icon(
                imageVector = icon,
                contentDescription = null,
                modifier = Modifier.size(18.dp),
                tint = MaterialTheme.colorScheme.onSurfaceVariant
            )
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
            .clickable(
                interactionSource = remember { MutableInteractionSource() },
                indication = null,
                onClick = onClick
            )
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
            OssLibrary("Jetpack Compose", "Google", "https://developer.android.com/jetpack/compose", "Apache 2.0"),
            OssLibrary("AndroidX", "Google", "https://source.android.com/", "Apache 2.0"),
            OssLibrary("Coil", "Coil Contributors", "https://coil-kt.github.io/coil/", "Apache 2.0"),
            OssLibrary("ZalithLauncher2", "ZalithLauncher", "https://github.com/ZalithLauncher/ZalithLauncher2", "GPL v3.0"),
            OssLibrary("Haze", "Chris Banes", "https://github.com/chrisbanes/haze", "Apache 2.0")
        )
    }

    PopupContainer(
        visible = true,
        onDismissRequest = onDismiss,
        width = 400.dp,
        height = 600.dp
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(24.dp)
        ) {
            // Header
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                Surface(
                    shape = RoundedCornerShape(12.dp),
                    color = MaterialTheme.colorScheme.primary.copy(alpha = 0.1f),
                    modifier = Modifier.size(48.dp)
                ) {
                    Box(contentAlignment = Alignment.Center) {
                        Icon(
                            imageVector = Icons.Default.Description,
                            contentDescription = null,
                            modifier = Modifier.size(24.dp),
                            tint = MaterialTheme.colorScheme.primary
                        )
                    }
                }
                Column {
                    Text(
                        text = "开源许可",
                        style = MaterialTheme.typography.titleLarge,
                        fontWeight = FontWeight.Black
                    )
                    Text(
                        text = "本应用使用的第三方组件",
                        style = MaterialTheme.typography.labelSmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }
            
            Spacer(modifier = Modifier.height(24.dp))
            
            // List
            LazyColumn(
                modifier = Modifier.weight(1f),
                verticalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                items(libraries) { lib ->
                    LicenseItem(library = lib) {
                        context.startActivity(Intent(Intent.ACTION_VIEW, Uri.parse(lib.url)))
                    }
                }
            }
            
            Spacer(modifier = Modifier.height(24.dp))
            
            ShardButton(
                onClick = onDismiss,
                modifier = Modifier.fillMaxWidth(),
                type = ButtonType.FILLED
            ) {
                Text("我知道了", fontWeight = FontWeight.Bold)
            }
        }
    }
}

@Composable
private fun LicenseItem(
    library: OssLibrary,
    onClick: () -> Unit
) {
    Surface(
        onClick = onClick,
        shape = RoundedCornerShape(16.dp),
        color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.3f),
        border = BorderStroke(0.5.dp, MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.2f))
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = library.name,
                    style = MaterialTheme.typography.titleSmall,
                    fontWeight = FontWeight.Bold
                )
                Text(
                    text = library.author,
                    style = MaterialTheme.typography.labelSmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
                Spacer(modifier = Modifier.height(6.dp))
                ShardTag(
                    text = library.license,
                    containerColor = MaterialTheme.colorScheme.primary.copy(alpha = 0.1f),
                    contentColor = MaterialTheme.colorScheme.primary
                )
            }
            Icon(
                imageVector = Icons.AutoMirrored.Filled.OpenInNew,
                contentDescription = null,
                modifier = Modifier.size(16.dp),
                tint = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.5f)
            )
        }
    }
}
