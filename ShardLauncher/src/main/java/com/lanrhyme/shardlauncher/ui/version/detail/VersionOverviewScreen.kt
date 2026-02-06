package com.lanrhyme.shardlauncher.ui.version.detail

import android.os.Build
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.Image
import androidx.compose.material.icons.outlined.RestartAlt
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import coil.compose.AsyncImage
import com.lanrhyme.shardlauncher.R
import com.lanrhyme.shardlauncher.game.version.installed.Version
import com.lanrhyme.shardlauncher.game.version.installed.VersionsManager
import com.lanrhyme.shardlauncher.ui.components.layout.LocalCardLayoutConfig
import com.lanrhyme.shardlauncher.ui.components.filemanager.FileSelectorScreen
import com.lanrhyme.shardlauncher.ui.components.filemanager.FileSelectorConfig
import com.lanrhyme.shardlauncher.ui.components.filemanager.FileSelectorMode
import com.lanrhyme.shardlauncher.ui.components.filemanager.FileSelectorResult
import com.lanrhyme.shardlauncher.ui.components.basic.ShardAlertDialog
import com.lanrhyme.shardlauncher.ui.components.basic.ShardEditDialog
import com.lanrhyme.shardlauncher.ui.components.basic.ShardTaskDialog
import com.lanrhyme.shardlauncher.ui.version.dialog.DeleteVersionDialog
import com.lanrhyme.shardlauncher.ui.version.dialog.RenameVersionDialog
import com.lanrhyme.shardlauncher.utils.file.FolderUtils
import com.lanrhyme.shardlauncher.utils.logging.Logger.lError
import com.lanrhyme.shardlauncher.utils.string.getMessageOrToString
import dev.chrisbanes.haze.hazeEffect
import kotlinx.coroutines.Dispatchers
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
    var refreshVersionIcon by remember { mutableIntStateOf(0) }
    val context = LocalContext.current
    var iconFileExists by remember { mutableStateOf(VersionsManager.getVersionIconFile(version).exists()) }

    var versionsOperation by remember { mutableStateOf<VersionOverviewOperation>(VersionOverviewOperation.None) }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        // 版本信息卡片
        var showIconSelector by remember { mutableStateOf(false) }
        
        VersionInfoCard(
            version = version,
            versionSummary = versionSummary,
            iconFileExists = iconFileExists,
            refreshKey = refreshVersionIcon,
            onIconPicked = {
                refreshVersionIcon++
                iconFileExists = VersionsManager.getVersionIconFile(version).exists()
            },
            onResetIcon = { versionsOperation = VersionOverviewOperation.ResetIconAlert },
            onShowIconSelector = { showIconSelector = true },
            onError = onError,
            isCardBlurEnabled = isCardBlurEnabled,
            cardAlpha = cardAlpha,
            hazeState = hazeState
        )

        // 版本管理卡片
        VersionManagementCard(
            onEditSummary = { versionsOperation = VersionOverviewOperation.EditSummary(version) },
            onRename = { versionsOperation = VersionOverviewOperation.Rename(version) },
            onDelete = { versionsOperation = VersionOverviewOperation.Delete(version) },
            isCardBlurEnabled = isCardBlurEnabled,
            cardAlpha = cardAlpha,
            hazeState = hazeState
        )

        // 快速操作卡片
        VersionQuickActionsCard(
            version = version,
            onError = onError,
            isCardBlurEnabled = isCardBlurEnabled,
            cardAlpha = cardAlpha,
            hazeState = hazeState
        )
    }

    // 处理操作
    VersionOverviewOperations(
        operation = versionsOperation,
        updateOperation = { versionsOperation = it },
        onError = onError,
        resetIcon = {
            val iconFile = VersionsManager.getVersionIconFile(version)
            FileUtils.deleteQuietly(iconFile)
            refreshVersionIcon++
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
                        try {
                            val iconFile = VersionsManager.getVersionIconFile(version)
                            val sourceFile = result.path
                            sourceFile.inputStream().use { input ->
                                iconFile.outputStream().use { output ->
                                    input.copyTo(output)
                                }
                            }
                            refreshVersionIcon++
                            iconFileExists = iconFile.exists()
                        } catch (e: Exception) {
                            lError("Failed to import icon!", e)
                            FileUtils.deleteQuietly(iconFile)
                            onError("导入图标失败: ${e.message}")
                        }
                    }
                    FileSelectorResult.Cancelled -> { /* 用户取消 */ }
                    FileSelectorResult.MultipleSelected -> { /* 不支持多选 */ }
                }
                showIconSelector = false
            }
        )
    }
}

@Composable
private fun VersionInfoCard(
    version: Version,
    versionSummary: String,
    iconFileExists: Boolean,
    refreshKey: Any?,
    onIconPicked: () -> Unit,
    onResetIcon: () -> Unit,
    onShowIconSelector: () -> Unit,
    onError: (String) -> Unit,
    isCardBlurEnabled: Boolean,
    cardAlpha: Float,
    hazeState: dev.chrisbanes.haze.HazeState
) {
    val context = LocalContext.current
    val iconFile = remember { VersionsManager.getVersionIconFile(version) }
    val cardShape = RoundedCornerShape(16.dp)

    Card(
        modifier = Modifier
            .fillMaxWidth()
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
        Column(
            modifier = Modifier.padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            Text(
                text = "版本信息",
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold
            )

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(16.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                // 版本图标
                AsyncImage(
                    model = iconFile.takeIf { it.exists() },
                    contentDescription = "版本图标",
                    placeholder = androidx.compose.ui.res.painterResource(R.drawable.img_minecraft),
                    error = androidx.compose.ui.res.painterResource(R.drawable.img_minecraft),
                    modifier = Modifier.size(64.dp)
                )

                Column(
                    modifier = Modifier.weight(1f)
                ) {
                    Text(
                        text = version.getVersionName(),
                        style = MaterialTheme.typography.titleLarge,
                        fontWeight = FontWeight.Bold
                    )
                    if (version.isValid() && version.isSummaryValid()) {
                        Text(
                            text = versionSummary,
                            style = MaterialTheme.typography.bodyMedium,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                    version.getVersionInfo()?.let { info ->
                        Text(
                            text = "Minecraft ${info.minecraftVersion}",
                            style = MaterialTheme.typography.bodySmall
                        )
                        info.loaderInfo?.let { loader ->
                            Text(
                                text = "${loader.loader.displayName} ${loader.version}",
                                style = MaterialTheme.typography.bodySmall
                            )
                        }
                    }
                }
            }

            Row(
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                OutlinedButton(
                    onClick = onShowIconSelector
                ) {
                    Icon(Icons.Outlined.Image, contentDescription = null)
                    Spacer(modifier = Modifier.width(8.dp))
                    Text("自定义图标")
                }

                if (iconFileExists) {
                    OutlinedButton(
                        onClick = onResetIcon
                    ) {
                        Icon(Icons.Outlined.RestartAlt, contentDescription = null)
                        Spacer(modifier = Modifier.width(8.dp))
                        Text("重置图标")
                    }
                }
            }
        }
    }
}

@Composable
private fun VersionManagementCard(
    onEditSummary: () -> Unit,
    onRename: () -> Unit,
    onDelete: () -> Unit,
    isCardBlurEnabled: Boolean,
    cardAlpha: Float,
    hazeState: dev.chrisbanes.haze.HazeState
) {
    val cardShape = RoundedCornerShape(16.dp)
    
    Card(
        modifier = Modifier
            .fillMaxWidth()
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
        Column(
            modifier = Modifier.padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            Text(
                text = "版本管理",
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold
            )

            Row(
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                OutlinedButton(onClick = onEditSummary) {
                    Text("编辑描述")
                }
                OutlinedButton(onClick = onRename) {
                    Text("重命名")
                }
                OutlinedButton(
                    onClick = onDelete,
                    colors = ButtonDefaults.outlinedButtonColors(
                        contentColor = MaterialTheme.colorScheme.error
                    )
                ) {
                    Text("删除版本")
                }
            }
        }
    }
}

@Composable
private fun VersionQuickActionsCard(
    version: Version,
    onError: (String) -> Unit,
    isCardBlurEnabled: Boolean,
    cardAlpha: Float,
    hazeState: dev.chrisbanes.haze.HazeState
) {
    val context = LocalContext.current
    val cardShape = RoundedCornerShape(16.dp)

    fun openFolder(folderName: String) {
        val folder = if (folderName.isEmpty()) {
            version.getVersionPath()
        } else {
            // 对于游戏相关文件夹，使用getGameDir()
            val gameRelatedFolders = setOf("saves", "resourcepacks", "shaderpacks", "mods", "screenshots", "config")
            if (gameRelatedFolders.contains(folderName)) {
                File(version.getGameDir(), folderName)
            } else {
                // 对于版本相关文件夹（如logs, crash-reports），使用getVersionPath()
                File(version.getVersionPath(), folderName)
            }
        }
        FolderUtils.openFolder(context, folder, onError)
    }

    Card(
        modifier = Modifier
            .fillMaxWidth()
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
        Column(
            modifier = Modifier.padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            Text(
                text = "快速操作",
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold
            )

            // 第一行按钮
            Row(
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                OutlinedButton(
                    onClick = { openFolder("") },
                    modifier = Modifier.weight(1f)
                ) {
                    Text("版本文件夹")
                }
                OutlinedButton(
                    onClick = { openFolder("saves") },
                    modifier = Modifier.weight(1f)
                ) {
                    Text("存档文件夹")
                }
            }

            // 第二行按钮
            Row(
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                OutlinedButton(
                    onClick = { openFolder("resourcepacks") },
                    modifier = Modifier.weight(1f)
                ) {
                    Text("资源包")
                }
                OutlinedButton(
                    onClick = { openFolder("shaderpacks") },
                    modifier = Modifier.weight(1f)
                ) {
                    Text("光影包")
                }
            }

            // 第三行按钮
            Row(
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                OutlinedButton(
                    onClick = { openFolder("mods") },
                    modifier = Modifier.weight(1f)
                ) {
                    Text("模组文件夹")
                }
                OutlinedButton(
                    onClick = { openFolder("screenshots") },
                    modifier = Modifier.weight(1f)
                ) {
                    Text("截图文件夹")
                }
            }

            // 第四行按钮
            Row(
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                OutlinedButton(
                    onClick = { openFolder("logs") },
                    modifier = Modifier.weight(1f)
                ) {
                    Text("日志文件夹")
                }
                OutlinedButton(
                    onClick = { openFolder("crash-reports") },
                    modifier = Modifier.weight(1f)
                ) {
                    Text("崩溃报告")
                }
            }

            // 第五行按钮 - 额外功能
            Row(
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                OutlinedButton(
                    onClick = { openFolder("config") },
                    modifier = Modifier.weight(1f)
                ) {
                    Text("配置文件")
                }
                OutlinedButton(
                    onClick = { 
                        // 分享版本文件夹
                        try {
                            val intent = android.content.Intent(android.content.Intent.ACTION_SEND).apply {
                                type = "text/plain"
                                putExtra(android.content.Intent.EXTRA_TEXT, "版本路径: ${version.getVersionPath().absolutePath}")
                                putExtra(android.content.Intent.EXTRA_SUBJECT, "Minecraft版本: ${version.getVersionName()}")
                            }
                            context.startActivity(android.content.Intent.createChooser(intent, "分享版本信息"))
                        } catch (e: Exception) {
                            onError("分享失败: ${e.message}")
                        }
                    },
                    modifier = Modifier.weight(1f)
                ) {
                    Text("分享版本")
                }
            }
        }
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
                context = kotlinx.coroutines.CoroutineScope(Dispatchers.IO),
                onDismiss = { updateOperation(VersionOverviewOperation.None) },
                onError = { e ->
                    lError("Failed to run task.", e)
                    onError("任务执行失败: ${e.getMessageOrToString()}")
                }
            )
        }
    }
}
