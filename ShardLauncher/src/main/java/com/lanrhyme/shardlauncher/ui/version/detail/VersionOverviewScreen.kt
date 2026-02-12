package com.lanrhyme.shardlauncher.ui.version.detail

import android.content.Intent
import android.os.Build
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.Spring
import androidx.compose.animation.core.spring
import androidx.compose.animation.fadeIn
import androidx.compose.animation.slideInVertically
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.outlined.KeyboardArrowRight
import androidx.compose.material.icons.outlined.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import coil.compose.AsyncImage
import com.lanrhyme.shardlauncher.R
import com.lanrhyme.shardlauncher.game.version.installed.Version
import com.lanrhyme.shardlauncher.game.version.installed.VersionsManager
import com.lanrhyme.shardlauncher.ui.components.basic.*
import com.lanrhyme.shardlauncher.ui.components.filemanager.FileSelectorConfig
import com.lanrhyme.shardlauncher.ui.components.filemanager.FileSelectorMode
import com.lanrhyme.shardlauncher.ui.components.filemanager.FileSelectorResult
import com.lanrhyme.shardlauncher.ui.components.filemanager.FileSelectorScreen
import com.lanrhyme.shardlauncher.ui.components.layout.LocalCardLayoutConfig
import com.lanrhyme.shardlauncher.ui.version.dialog.DeleteVersionDialog
import com.lanrhyme.shardlauncher.ui.version.dialog.RenameVersionDialog
import com.lanrhyme.shardlauncher.utils.file.FolderUtils
import com.lanrhyme.shardlauncher.utils.logging.Logger.lError
import com.lanrhyme.shardlauncher.utils.string.getMessageOrToString
import dev.chrisbanes.haze.hazeEffect
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import org.apache.commons.io.FileUtils
import java.io.File

@Composable
fun VersionOverviewScreen(
    version: Version,
    onBack: () -> Unit,
    onError: (String) -> Unit
) {
    if (!version.isValid()) {
        onBack()
        return
    }

    val (isCardBlurEnabled, cardAlpha, hazeState) = LocalCardLayoutConfig.current
    var versionSummary by remember { mutableStateOf(version.getVersionSummary()) }
    var refreshVersionIconInt by remember { mutableIntStateOf(0) }
    
    val context = LocalContext.current
    var iconFileExists by remember { mutableStateOf(VersionsManager.getVersionIconFile(version).exists()) }

    var versionsOperation by remember { mutableStateOf<VersionOverviewOperation>(VersionOverviewOperation.None) }
    var showIconSelector by remember { mutableStateOf(false) }

    // Animation state
    var isVisible by remember { mutableStateOf(false) }
    LaunchedEffect(Unit) { isVisible = true }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState()),
        verticalArrangement = Arrangement.spacedBy(24.dp)
    ) {
        AnimatedVisibility(
            visible = isVisible,
            enter = fadeIn() + slideInVertically(spring(stiffness = Spring.StiffnessLow))
        ) {
            InstanceHeader(
                version = version,
                versionSummary = versionSummary,
                iconFileExists = iconFileExists,
                refreshKey = refreshVersionIconInt,
                onIconClick = { showIconSelector = true },
                onEditSummary = { versionsOperation = VersionOverviewOperation.EditSummary(version) },
                isCardBlurEnabled = isCardBlurEnabled,
                hazeState = hazeState
            )
        }

        AnimatedVisibility(
            visible = isVisible,
            enter = fadeIn(spring(stiffness = Spring.StiffnessLow)) + 
                    slideInVertically(spring(stiffness = Spring.StiffnessLow)) { it / 2 }
        ) {
            Column(
                modifier = Modifier.padding(horizontal = 16.dp),
                verticalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                Text(
                    "快速访问",
                    style = MaterialTheme.typography.titleMedium,
                    color = MaterialTheme.colorScheme.primary,
                    fontWeight = FontWeight.Bold
                )
                
                QuickActionsGrid(
                    version = version,
                    onError = onError,
                    isCardBlurEnabled = isCardBlurEnabled,
                    cardAlpha = cardAlpha,
                    hazeState = hazeState
                )
            }
        }

        AnimatedVisibility(
            visible = isVisible,
            enter = fadeIn(spring(stiffness = Spring.StiffnessLow)) + 
                    slideInVertically(spring(stiffness = Spring.StiffnessLow)) { it }
        ) {
            Column(
                modifier = Modifier.padding(horizontal = 16.dp),
                verticalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                Text(
                    "高级管理",
                    style = MaterialTheme.typography.titleMedium,
                    color = MaterialTheme.colorScheme.primary,
                    fontWeight = FontWeight.Bold
                )
                
                ManagementSection(
                    version = version,
                    onRename = { versionsOperation = VersionOverviewOperation.Rename(version) },
                    onDelete = { versionsOperation = VersionOverviewOperation.Delete(version) },
                    onResetIcon = { versionsOperation = VersionOverviewOperation.ResetIconAlert },
                    iconFileExists = iconFileExists,
                    isCardBlurEnabled = isCardBlurEnabled,
                    cardAlpha = cardAlpha,
                    hazeState = hazeState
                )
            }
        }
        
        Spacer(modifier = Modifier.height(32.dp))
    }

    // 处理操作
    VersionOverviewOperations(
        operation = versionsOperation,
        updateOperation = { versionsOperation = it },
        onError = onError,
        resetIcon = {
            val iconFile = VersionsManager.getVersionIconFile(version)
            FileUtils.deleteQuietly(iconFile)
            refreshVersionIconInt++
            iconFileExists = iconFile.exists()
        },
        setVersionSummary = { value ->
            version.getVersionConfig().apply {
                this.versionSummary = value
                save()
            }
            versionSummary = version.getVersionSummary()
        }
    )
    
    // 显示文件选择器
    if (showIconSelector) {
        FileSelectorScreen(
            visible = showIconSelector,
            config = FileSelectorConfig(
                initialPath = android.os.Environment.getExternalStorageDirectory(),
                mode = FileSelectorMode.FILE_ONLY,
                showHiddenFiles = true,
                allowCreateDirectory = false,
                fileFilter = { file ->
                    file.isFile && file.extension.lowercase() in listOf("png", "jpg", "jpeg", "webp", "gif")
                }
            ),
            onDismissRequest = { showIconSelector = false },
            onSelection = { result ->
                when (result) {
                    is FileSelectorResult.Selected -> {
                        val iconFile = VersionsManager.getVersionIconFile(version)
                        try {
                            val sourceFile = result.path
                            sourceFile.inputStream().use { input ->
                                iconFile.outputStream().use { output ->
                                    input.copyTo(output)
                                }
                            }
                            refreshVersionIconInt++
                            iconFileExists = iconFile.exists()
                        } catch (e: Exception) {
                            lError("Failed to import icon!", e)
                            FileUtils.deleteQuietly(iconFile)
                            onError("导入图标失败: ${e.message}")
                        }
                    }
                    else -> { }
                }
                showIconSelector = false
            }
        )
    }
}

@Composable
private fun InstanceHeader(
    version: Version,
    versionSummary: String,
    iconFileExists: Boolean,
    refreshKey: Any?,
    onIconClick: () -> Unit,
    onEditSummary: () -> Unit,
    isCardBlurEnabled: Boolean,
    hazeState: dev.chrisbanes.haze.HazeState
) {
    val iconFile = remember { VersionsManager.getVersionIconFile(version) }
    
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(24.dp))
            .background(
                Brush.linearGradient(
                    colors = listOf(
                        MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.6f),
                        MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.4f)
                    )
                )
            )
            .then(
                 if (isCardBlurEnabled && Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
                     Modifier.hazeEffect(state = hazeState)
                 } else Modifier
            )
            .clickable { onIconClick() }
            .padding(24.dp)
    ) {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            modifier = Modifier.fillMaxWidth()
        ) {
            // Icon with Glow
            Box(
                contentAlignment = Alignment.Center,
                modifier = Modifier
                    .size(100.dp)
                    .glow(
                        color = MaterialTheme.colorScheme.primary,
                        cornerRadius = 50.dp,
                        blurRadius = 24.dp,
                        enabled = true
                    )
            ) {
                AsyncImage(
                    model = iconFile.takeIf { it.exists() },
                    contentDescription = "Version Icon",
                    placeholder = painterResource(R.drawable.img_minecraft),
                    error = painterResource(R.drawable.img_minecraft),
                    modifier = Modifier
                        .size(100.dp)
                        .clip(CircleShape)
                        .background(MaterialTheme.colorScheme.surface),
                    contentScale = ContentScale.Crop
                )
                
                // Edit overlay hint
                Box(
                    modifier = Modifier
                        .align(Alignment.BottomEnd)
                        .offset(x = 4.dp, y = 4.dp)
                        .size(32.dp)
                        .clip(CircleShape)
                        .background(MaterialTheme.colorScheme.tertiaryContainer),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        Icons.Outlined.Edit, 
                        contentDescription = "Edit Icon",
                        modifier = Modifier.size(16.dp),
                        tint = MaterialTheme.colorScheme.onTertiaryContainer
                    )
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

            Text(
                text = version.getVersionName(),
                style = MaterialTheme.typography.headlineSmall.copy(
                    fontWeight = FontWeight.ExtraBold,
                    letterSpacing = 0.5.sp
                ),
                color = MaterialTheme.colorScheme.onSurface
            )
            
            Spacer(modifier = Modifier.height(8.dp))
            
            Row(
                verticalAlignment = Alignment.CenterVertically,
                modifier = Modifier
                    .clip(RoundedCornerShape(12.dp))
                    .clickable { onEditSummary() }
                    .background(MaterialTheme.colorScheme.surface.copy(alpha = 0.5f))
                    .padding(horizontal = 12.dp, vertical = 6.dp)
            ) {
                Text(
                    text = if (versionSummary.isBlank()) "点击添加描述..." else versionSummary,
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
                 if (versionSummary.isBlank()) {
                    Spacer(modifier = Modifier.width(4.dp))
                    Icon(
                        Icons.Outlined.Edit, 
                        contentDescription = null, 
                        modifier = Modifier.size(14.dp),
                        tint = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }
            
            Spacer(modifier = Modifier.height(16.dp))

            // Info Chips
            Row(
                horizontalArrangement = Arrangement.Center,
                modifier = Modifier.fillMaxWidth().wrapContentWidth()
            ) {
                version.getVersionInfo()?.let { info ->
                    info.minecraftVersion?.let { mcVer ->
                        GameInfoChip(text = "Minecraft $mcVer", icon = Icons.Outlined.GridView)
                        Spacer(modifier = Modifier.width(8.dp))
                    }
                    info.loaderInfo?.let { loader ->
                        GameInfoChip(text = "${loader.loader.displayName}", icon = Icons.Outlined.Extension)
                    }
                }
            }
        }
    }
}

@Composable
private fun GameInfoChip(text: String, icon: ImageVector) {
    Surface(
        color = MaterialTheme.colorScheme.secondaryContainer,
        shape = RoundedCornerShape(8.dp)
    ) {
        Row(
            modifier = Modifier.padding(horizontal = 10.dp, vertical = 6.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Icon(
                icon, 
                contentDescription = null, 
                modifier = Modifier.size(14.dp),
                tint = MaterialTheme.colorScheme.onSecondaryContainer
            )
            Spacer(modifier = Modifier.width(6.dp))
            Text(
                text = text,
                style = MaterialTheme.typography.labelMedium,
                color = MaterialTheme.colorScheme.onSecondaryContainer,
                fontWeight = FontWeight.SemiBold
            )
        }
    }
}

@Composable
private fun QuickActionsGrid(
    version: Version,
    onError: (String) -> Unit,
    isCardBlurEnabled: Boolean,
    cardAlpha: Float,
    hazeState: dev.chrisbanes.haze.HazeState
) {
    val context = LocalContext.current
    
    fun openFolder(folderName: String) {
        val folder = if (folderName.isEmpty()) {
            version.getVersionPath()
        } else {
             val gameRelatedFolders = setOf("saves", "resourcepacks", "shaderpacks", "mods", "screenshots", "config")
            if (gameRelatedFolders.contains(folderName)) {
                File(version.getGameDir(), folderName)
            } else {
                File(version.getVersionPath(), folderName)
            }
        }
        FolderUtils.openFolder(context, folder, onError)
    }
    
    Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
        Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
            QuickActionItem(
                modifier = Modifier.weight(1f),
                title = "根目录",
                icon = Icons.Outlined.Folder,
                color = MaterialTheme.colorScheme.primary,
                onClick = { openFolder("") },
                isCardBlurEnabled = isCardBlurEnabled, hazeState = hazeState, baseAlpha = cardAlpha
            )
            QuickActionItem(
                modifier = Modifier.weight(1f),
                title = "存档",
                icon = Icons.Outlined.Save, 
                color = MaterialTheme.colorScheme.secondary,
                onClick = { openFolder("saves") },
                isCardBlurEnabled = isCardBlurEnabled, hazeState = hazeState, baseAlpha = cardAlpha
            )
        }
        Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
             QuickActionItem(
                modifier = Modifier.weight(1f),
                title = "资源包",
                icon = Icons.Outlined.Texture, 
                color = MaterialTheme.colorScheme.tertiary,
                onClick = { openFolder("resourcepacks") },
                isCardBlurEnabled = isCardBlurEnabled, hazeState = hazeState, baseAlpha = cardAlpha
            )
            QuickActionItem(
                modifier = Modifier.weight(1f),
                title = "光影",
                icon = Icons.Outlined.LightMode,
                color = MaterialTheme.colorScheme.error,
                onClick = { openFolder("shaderpacks") },
                isCardBlurEnabled = isCardBlurEnabled, hazeState = hazeState, baseAlpha = cardAlpha
            )
        }
        Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
            QuickActionItem(
                modifier = Modifier.weight(1f),
                title = "模组",
                icon = Icons.Outlined.Extension,
                color = MaterialTheme.colorScheme.secondary,
                onClick = { openFolder("mods") },
                isCardBlurEnabled = isCardBlurEnabled, hazeState = hazeState, baseAlpha = cardAlpha
            )
            QuickActionItem(
                modifier = Modifier.weight(1f),
                title = "截图",
                icon = Icons.Outlined.Image,
                color = MaterialTheme.colorScheme.primary,
                onClick = { openFolder("screenshots") },
                isCardBlurEnabled = isCardBlurEnabled, hazeState = hazeState, baseAlpha = cardAlpha
            )
        }
        
         Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
            QuickActionItem(
                modifier = Modifier.weight(1f),
                title = "配置",
                icon = Icons.Outlined.Settings,
                color = MaterialTheme.colorScheme.outline,
                onClick = { openFolder("config") },
                isCardBlurEnabled = isCardBlurEnabled, hazeState = hazeState, baseAlpha = cardAlpha
            )
             QuickActionItem(
                 modifier = Modifier.weight(1f),
                title = "分享",
                icon = Icons.Outlined.Share,
                color = MaterialTheme.colorScheme.primary,
                onClick = { 
                    try {
                        val intent = Intent(Intent.ACTION_SEND).apply {
                            type = "text/plain"
                            putExtra(Intent.EXTRA_TEXT, "版本路径: ${version.getVersionPath().absolutePath}")
                            putExtra(Intent.EXTRA_SUBJECT, "Minecraft版本: ${version.getVersionName()}")
                        }
                        context.startActivity(Intent.createChooser(intent, "分享版本信息"))
                    } catch (e: Exception) {
                        onError("分享失败: ${e.message}")
                    }
                },
                isCardBlurEnabled = isCardBlurEnabled, hazeState = hazeState, baseAlpha = cardAlpha
             )
        }
    }
}

@Composable
private fun QuickActionItem(
    modifier: Modifier = Modifier,
    title: String,
    icon: ImageVector,
    color: Color,
    onClick: () -> Unit,
    isCardBlurEnabled: Boolean,
    hazeState: dev.chrisbanes.haze.HazeState,
    baseAlpha: Float
) {
    val containerShape = RoundedCornerShape(16.dp)
    
    Surface(
        onClick = onClick,
        shape = containerShape,
        color = MaterialTheme.colorScheme.surface.copy(alpha = baseAlpha),
        modifier = modifier
            .height(80.dp)
            .then(
                 if (isCardBlurEnabled && Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
                     Modifier.clip(containerShape).hazeEffect(state = hazeState)
                 } else Modifier
            )
    ) {
        Row(
            verticalAlignment = Alignment.CenterVertically,
            modifier = Modifier.padding(16.dp)
        ) {
            Box(
                modifier = Modifier
                    .size(48.dp)
                    .clip(RoundedCornerShape(12.dp))
                    .background(color.copy(alpha = 0.1f)),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = icon,
                    contentDescription = null,
                    tint = color,
                    modifier = Modifier.size(24.dp)
                )
            }
            
            Spacer(modifier = Modifier.width(16.dp))
            
            Text(
                text = title,
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.SemiBold,
                color = MaterialTheme.colorScheme.onSurface
            )
        }
    }
}

@Composable
private fun ManagementSection(
    version: Version,
    onRename: () -> Unit,
    onDelete: () -> Unit,
    onResetIcon: () -> Unit,
    iconFileExists: Boolean,
    isCardBlurEnabled: Boolean,
    cardAlpha: Float,
    hazeState: dev.chrisbanes.haze.HazeState
) {
     val containerShape = RoundedCornerShape(24.dp)
     
     Column(
         verticalArrangement = Arrangement.spacedBy(1.dp), // Divider effect
         modifier = Modifier
            .fillMaxWidth()
            .clip(containerShape)
            .then(
                 if (isCardBlurEnabled && Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
                     Modifier.hazeEffect(state = hazeState)
                 } else Modifier
            )
            .background(MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f))
     ) {
         ManagementItem(
             title = "重命名实例",
             subtitle = "修改在启动器中显示的名称",
             icon = Icons.Outlined.Edit,
             onClick = onRename
         )
         
         if (iconFileExists) {
             ManagementItem(
                 title = "恢复默认图标",
                 subtitle = "移除自定义图标",
                 icon = Icons.Outlined.RestartAlt,
                 onClick = onResetIcon
             )
         }
         
         ManagementItem(
             title = "删除实例",
             subtitle = "永久删除此版本及相关文件",
             icon = Icons.Outlined.DeleteForever,
             onClick = onDelete,
             isDestructive = true
         )
     }
}

@Composable
private fun ManagementItem(
    title: String,
    subtitle: String,
    icon: ImageVector,
    onClick: () -> Unit,
    isDestructive: Boolean = false
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .background(MaterialTheme.colorScheme.surface.copy(alpha = 0.6f))
            .clickable { onClick() }
            .padding(20.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Icon(
            imageVector = icon,
            contentDescription = null,
            tint = if (isDestructive) MaterialTheme.colorScheme.error else MaterialTheme.colorScheme.primary,
            modifier = Modifier.size(24.dp)
        )
        
        Spacer(modifier = Modifier.width(20.dp))
        
        Column(modifier = Modifier.weight(1f)) {
            Text(
                text = title,
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.SemiBold,
                color = if (isDestructive) MaterialTheme.colorScheme.error else MaterialTheme.colorScheme.onSurface
            )
            Text(
                text = subtitle,
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
        
        Icon(
            imageVector = Icons.AutoMirrored.Outlined.KeyboardArrowRight,
            contentDescription = null,
            modifier = Modifier.size(20.dp),
            tint = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.5f)
        )
    }
}

sealed interface VersionOverviewOperation {
    data object None: VersionOverviewOperation
    data object ResetIconAlert: VersionOverviewOperation
    data class EditSummary(val version: Version): VersionOverviewOperation
    data class Rename(val version: Version): VersionOverviewOperation
    data class Delete(val version: Version): VersionOverviewOperation
    data class RunTask(val title: String, val task: suspend () -> Unit): VersionOverviewOperation
}

@Composable
private fun VersionOverviewOperations(
    operation: VersionOverviewOperation,
    updateOperation: (VersionOverviewOperation) -> Unit,
    onError: (String) -> Unit,
    resetIcon: () -> Unit,
    setVersionSummary: (String) -> Unit
) {
    val scope = rememberCoroutineScope()
    when(operation) {
        is VersionOverviewOperation.None -> {}
        is VersionOverviewOperation.ResetIconAlert -> {
            ShardAlertDialog(
                title = "重置图标",
                text = "确定要重置版本图标吗？",
                onDismiss = { updateOperation(VersionOverviewOperation.None) },
                onConfirm = {
                    resetIcon()
                    updateOperation(VersionOverviewOperation.None)
                }
            )
        }
        is VersionOverviewOperation.Rename -> {
            RenameVersionDialog(
                version = operation.version,
                onDismissRequest = { updateOperation(VersionOverviewOperation.None) },
                onConfirm = { newName: String ->
                    updateOperation(
                        VersionOverviewOperation.RunTask(
                            title = "重命名版本",
                            task = {
                                VersionsManager.renameVersion(operation.version, newName)
                            }
                        )
                    )
                }
            )
        }
        is VersionOverviewOperation.Delete -> {
            DeleteVersionDialog(
                version = operation.version,
                onDismissRequest = { updateOperation(VersionOverviewOperation.None) },
                onConfirm = {
                    updateOperation(
                        VersionOverviewOperation.RunTask(
                            title = "删除版本",
                            task = {
                                VersionsManager.deleteVersion(operation.version)
                            }
                        )
                    )
                }
            )
        }
        is VersionOverviewOperation.EditSummary -> {
            val version = operation.version
            var value by remember { mutableStateOf(version.getVersionConfig().versionSummary) }

            ShardEditDialog(
                title = "编辑版本描述",
                value = value,
                onValueChange = { value = it },
                label = "版本描述",
                singleLine = true,
                onDismissRequest = { updateOperation(VersionOverviewOperation.None) },
                onConfirm = {
                    setVersionSummary(value)
                    updateOperation(VersionOverviewOperation.None)
                }
            )
        }
        is VersionOverviewOperation.RunTask -> {
            ShardTaskDialog(
                title = operation.title,
                task = operation.task,
                context = scope,
                onDismiss = { updateOperation(VersionOverviewOperation.None) },
                onError = { e ->
                    lError("Failed to run task.", e)
                    onError("任务执行失败: ${e.getMessageOrToString()}")
                }
            )
        }
    }
}
