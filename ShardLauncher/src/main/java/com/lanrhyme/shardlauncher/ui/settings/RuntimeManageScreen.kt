/*
 * Shard Launcher
 * Adapted from Zalith Launcher 2
 */

package com.lanrhyme.shardlauncher.ui.settings

import android.net.Uri
import android.provider.OpenableColumns
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import com.lanrhyme.shardlauncher.ui.components.filemanager.FileSelectorConfig
import com.lanrhyme.shardlauncher.ui.components.filemanager.FileSelectorMode
import com.lanrhyme.shardlauncher.ui.components.filemanager.FileSelectorResult
import com.lanrhyme.shardlauncher.ui.components.filemanager.FileSelectorScreen
import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.tween
import androidx.compose.foundation.clickable
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
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.lanrhyme.shardlauncher.game.multirt.Runtime
import com.lanrhyme.shardlauncher.game.multirt.RuntimesManager
import com.lanrhyme.shardlauncher.game.multirt.RuntimeInstaller
import com.lanrhyme.shardlauncher.ui.components.basic.ShardDialog
import com.lanrhyme.shardlauncher.ui.components.basic.ShardCard
import kotlinx.coroutines.launch

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun RuntimeManageScreen(
    visible: Boolean,
    onDismissRequest: () -> Unit,
    animationSpeed: Float = 1.0f,
    isGlowEffectEnabled: Boolean = true
) {
    val context = LocalContext.current
    val scope = rememberCoroutineScope()
    var runtimes by remember { mutableStateOf(RuntimesManager.getRuntimes()) }
    var bundledRuntimes by remember { mutableStateOf(RuntimeInstaller.getBundledRuntimes(context)) }
    var showDeleteDialog by remember { mutableStateOf<Runtime?>(null) }
    var showBundledInstallDialog by remember { mutableStateOf(false) }
    var showProgressDialog by remember { mutableStateOf(false) }
    var progressMessage by remember { mutableStateOf("") }
    var progressValue by remember { mutableStateOf(0) }
    var importErrorMessage by remember { mutableStateOf<String?>(null) }

    // File picker for importing tar.xz files
    var showFileSelector by remember { mutableStateOf(false) }

    ShardDialog(
        visible = visible,
        onDismissRequest = onDismissRequest,
        modifier = Modifier.padding(16.dp)
    ) {
        ShardCard(modifier = Modifier.fillMaxSize()) {
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(12.dp)
            ) {
                Row(
                    modifier = Modifier
                        .padding(horizontal = 12.dp, vertical = 8.dp)
                        .fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    OutlinedButton(
                        onClick = {
                            runtimes = RuntimesManager.getRuntimes()
                            bundledRuntimes = RuntimeInstaller.getBundledRuntimes(context)
                        },
                        shape = RoundedCornerShape(12.dp)
                    ) {
                        Icon(Icons.Default.Refresh, contentDescription = null)
                        Spacer(Modifier.width(8.dp))
                        androidx.compose.material3.Text("刷新")
                    }
                    OutlinedButton(
                        onClick = { showBundledInstallDialog = true },
                        shape = RoundedCornerShape(12.dp)
                    ) {
                        Icon(Icons.Default.GetApp, contentDescription = null)
                        Spacer(Modifier.width(8.dp))
                        androidx.compose.material3.Text("内置")
                    }
                    OutlinedButton(
                        onClick = { showFileSelector = true },
                        shape = RoundedCornerShape(12.dp)
                    ) {
                        Icon(Icons.Default.FileOpen, contentDescription = null)
                        Spacer(Modifier.width(8.dp))
                        androidx.compose.material3.Text("导入")
                    }
                    Spacer(Modifier.weight(1f))
                    IconButton(onClick = onDismissRequest) {
                        Icon(Icons.Default.Close, contentDescription = "关闭")
                    }
                }

                LazyColumn(
                    modifier = Modifier.fillMaxWidth().weight(1f),
                    contentPadding = PaddingValues(horizontal = 12.dp, vertical = 6.dp)
                ) {
                    items(runtimes) { runtime ->
                        RuntimeItem(
                            runtime = runtime,
                            modifier = Modifier.padding(vertical = 6.dp),
                            onDeleteClick = { showDeleteDialog = runtime }
                        )
                    }
                }
            }
        }
    }

    showDeleteDialog?.let { runtime ->
        ShardDialog(
            visible = true,
            onDismissRequest = { showDeleteDialog = null },
            width = 320.dp,
            height = 220.dp
        ) {
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(16.dp)
            ) {
                Text(
                    text = "删除运行时",
                    style = MaterialTheme.typography.titleLarge,
                    fontWeight = FontWeight.Bold
                )
                Spacer(Modifier.height(16.dp))
                Text(
                    text = "确定要删除运行时 \"${runtime.name}\" 吗？此操作不可撤销。",
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
                Spacer(Modifier.height(16.dp))
                Row(modifier = Modifier.fillMaxWidth()) {
                    OutlinedButton(
                        onClick = { showDeleteDialog = null },
                        shape = RoundedCornerShape(12.dp)
                    ) {
                        androidx.compose.material3.Text("取消")
                    }
                    Spacer(Modifier.width(8.dp))
                    Button(
                        onClick = {
                            RuntimesManager.removeRuntime(runtime.name)
                            showDeleteDialog = null
                            runtimes = RuntimesManager.getRuntimes()
                        },
                        shape = RoundedCornerShape(12.dp)
                    ) {
                        androidx.compose.material3.Text("删除")
                    }
                }
            }
        }
    }

    if (showBundledInstallDialog) {
        BundledRuntimeInstallDialog(
            bundledRuntimes = bundledRuntimes,
            installedRuntimes = runtimes,
            onDismiss = { showBundledInstallDialog = false },
            onInstall = { bundledRuntime ->
                showBundledInstallDialog = false
                showProgressDialog = true
                progressMessage = "准备安装..."
                progressValue = 0
                
                scope.launch {
                    RuntimeInstaller.installBundledRuntime(
                        context = context,
                        runtime = bundledRuntime,
                        onProgress = { progress, message ->
                            progressValue = progress
                            progressMessage = message
                        }
                    ).onSuccess {
                        showProgressDialog = false
                        runtimes = RuntimesManager.getRuntimes()
                    }.onFailure { error ->
                        showProgressDialog = false
                        // TODO: Show error dialog
                    }
                }
            }
        )
    }

    if (showProgressDialog) {
        ShardDialog(
            visible = true,
            onDismissRequest = { },
            width = 360.dp,
            height = 220.dp
        ) {
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(16.dp)
            ) {
                Text(
                    text = "安装运行时",
                    style = MaterialTheme.typography.titleLarge,
                    fontWeight = FontWeight.Bold
                )
                Spacer(Modifier.height(16.dp))
                Text(
                    text = progressMessage,
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
                Spacer(Modifier.height(12.dp))
                LinearProgressIndicator(
                    progress = progressValue / 100f,
                    modifier = Modifier.fillMaxWidth()
                )
                Spacer(Modifier.height(8.dp))
                Text(
                    text = "${progressValue}%",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        }
    }

    importErrorMessage?.let { message ->
        ShardDialog(
            visible = true,
            onDismissRequest = { importErrorMessage = null },
            width = 320.dp,
            height = 220.dp
        ) {
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(16.dp)
            ) {
                Text(
                    text = "导入失败",
                    style = MaterialTheme.typography.titleLarge,
                    fontWeight = FontWeight.Bold
                )
                Spacer(Modifier.height(16.dp))
                Text(
                    text = message,
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
                Spacer(Modifier.height(16.dp))
                Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.End) {
                    Button(onClick = { importErrorMessage = null }, shape = RoundedCornerShape(12.dp)) {
                        androidx.compose.material3.Text("关闭")
                    }
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
                    file.isFile && file.name.lowercase().endsWith(".tar.xz")
                },
                allowMultipleSelection = true
            ),
            onDismissRequest = { showFileSelector = false },
            onSelection = { result ->
                when (result) {
                    is FileSelectorResult.MultipleSelected -> {
                        val files = result.paths
                        if (files.isNotEmpty()) {
                            showProgressDialog = true
                            progressMessage = "准备导入..."
                            progressValue = 0

                            scope.launch {
                                val total = files.size
                            files.forEachIndexed { index, selectedFile ->
                                val rawName = selectedFile.name
                                if (rawName.isBlank()) {
                                    progressMessage = "导入失败：无法获取文件名"
                                    importErrorMessage = progressMessage
                                    return@forEachIndexed
                                }
                                if (!isTarXzFileName(rawName)) {
                                    progressMessage = "导入失败：仅支持 .tar.xz 文件"
                                    importErrorMessage = progressMessage
                                    return@forEachIndexed
                                }
                                val runtimeName = normalizeRuntimeName(rawName)
                                if (runtimeName.isBlank()) {
                                    progressMessage = "导入失败：文件名无效"
                                    importErrorMessage = progressMessage
                                    return@forEachIndexed
                                }
                                RuntimeInstaller.importRuntimeFromFile(
                                    context = context,
                                    uri = Uri.fromFile(selectedFile),
                                    runtimeName = runtimeName,
                                    onProgress = { progress, message ->
                                        val base = (index * 100) / total
                                        val step = progress / total
                                        progressValue = (base + step).coerceAtMost(100)
                                        progressMessage = message
                                    }
                                ).onFailure {
                                    progressMessage = "导入失败：$runtimeName"
                                    importErrorMessage = progressMessage
                                }
                            }
                                showProgressDialog = false
                                runtimes = RuntimesManager.getRuntimes()
                            }
                        } else {
                            showFileSelector = false
                        }
                    }
                    is FileSelectorResult.Selected -> {
                        showProgressDialog = true
                        progressMessage = "准备导入..."
                        progressValue = 0

                        scope.launch {
                            val selectedFile = result.path
                            val rawName = selectedFile.name
                            if (rawName.isBlank()) {
                                progressMessage = "导入失败：无法获取文件名"
                                importErrorMessage = progressMessage
                            } else if (!isTarXzFileName(rawName)) {
                                progressMessage = "导入失败：仅支持 .tar.xz 文件"
                                importErrorMessage = progressMessage
                            } else {
                                val runtimeName = normalizeRuntimeName(rawName)
                                if (runtimeName.isBlank()) {
                                    progressMessage = "导入失败：文件名无效"
                                    importErrorMessage = progressMessage
                                } else {
                                    RuntimeInstaller.importRuntimeFromFile(
                                        context = context,
                                        uri = Uri.fromFile(selectedFile),
                                        runtimeName = runtimeName,
                                        onProgress = { progress, message ->
                                            progressValue = progress
                                            progressMessage = message
                                        }
                                    ).onFailure {
                                        progressMessage = "导入失败：$runtimeName"
                                        importErrorMessage = progressMessage
                                    }
                                    showProgressDialog = false
                                    runtimes = RuntimesManager.getRuntimes()
                                }
                            }
                        }
                    }
                    FileSelectorResult.Cancelled -> { /* 用户取消 */ }
                }
                showFileSelector = false
            }
        )
    }
}

@Composable
private fun RuntimeItem(
    runtime: Runtime,
    modifier: Modifier = Modifier,
    onDeleteClick: () -> Unit
) {
    val contentColor = MaterialTheme.colorScheme.onSurface
    val compatible = runtime.isCompatible()
    val scale = remember { Animatable(initialValue = 0.95f) }
    LaunchedEffect(Unit) {
        scale.animateTo(targetValue = 1f, animationSpec = tween(durationMillis = 300))
    }
    Surface(
        modifier = modifier.graphicsLayer(scaleY = scale.value, scaleX = scale.value),
        shape = MaterialTheme.shapes.large,
        color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f),
        contentColor = contentColor,
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 14.dp, vertical = 8.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column(
                modifier = Modifier.weight(1f)
            ) {
                Text(
                    text = runtime.name,
                    style = MaterialTheme.typography.titleSmall
                )
                Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                    if (runtime.isProvidedByLauncher) {
                        Text(
                            text = "内置运行时",
                            style = MaterialTheme.typography.bodySmall
                        )
                    }
                    Text(
                        text = runtime.versionString?.let { "版本: $it" } ?: "运行时损坏",
                        style = MaterialTheme.typography.bodySmall,
                        color = if (runtime.versionString != null) contentColor else MaterialTheme.colorScheme.error
                    )
                    runtime.javaVersion.takeIf { it != 0 }?.let { javaVersion ->
                        Text(
                            text = "Java $javaVersion",
                            style = MaterialTheme.typography.bodySmall
                        )
                    }
                    runtime.arch?.let { arch ->
                        Text(
                            text = if (compatible) "架构: $arch" else "架构不兼容: $arch",
                            style = MaterialTheme.typography.bodySmall,
                            color = if (compatible) contentColor else MaterialTheme.colorScheme.error
                        )
                    }
                }
            }

            IconButton(
                enabled = !runtime.isProvidedByLauncher || !compatible,
                onClick = onDeleteClick
            ) {
                Icon(
                    modifier = Modifier.padding(8.dp),
                    imageVector = Icons.Default.Delete,
                    contentDescription = "删除"
                )
            }
        }
    }
}

@Composable
private fun BundledRuntimeInstallDialog(
    bundledRuntimes: List<RuntimeInstaller.BundledRuntime>,
    installedRuntimes: List<Runtime>,
    onDismiss: () -> Unit,
    onInstall: (RuntimeInstaller.BundledRuntime) -> Unit
) {
    val installedNames = installedRuntimes.map { it.name }.toSet()
    val availableRuntimes = bundledRuntimes.filter { it.name !in installedNames }

    ShardDialog(
        visible = true,
        onDismissRequest = onDismiss,
        width = 360.dp,
        height = 520.dp
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(16.dp)
        ) {
            Text(
                text = "安装内置运行时",
                style = MaterialTheme.typography.titleLarge,
                fontWeight = FontWeight.Bold
            )
            Spacer(Modifier.height(16.dp))
            if (availableRuntimes.isEmpty()) {
                Text(
                    text = "所有兼容的内置运行时都已安装。",
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            } else {
                LazyColumn(
                    modifier = Modifier.height(400.dp).fillMaxWidth(),
                    verticalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    items(availableRuntimes) { runtime ->
                        Card(
                            modifier = Modifier
                                .fillMaxWidth()
                                .clickable { onInstall(runtime) },
                            shape = RoundedCornerShape(12.dp),
                            colors =
                                CardDefaults.cardColors(
                                    containerColor =
                                        MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f)
                                )
                        ) {
                            Column(
                                modifier = Modifier.padding(12.dp),
                                verticalArrangement = Arrangement.spacedBy(4.dp)
                            ) {
                                Text(
                                    text = runtime.displayName,
                                    style = MaterialTheme.typography.titleMedium,
                                    fontWeight = FontWeight.Bold
                                )
                                Text(
                                    text = runtime.description,
                                    style = MaterialTheme.typography.bodySmall,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant
                                )
                                Text(
                                    text = "预计大小: ${formatSize(runtime.estimatedSize)}",
                                    style = MaterialTheme.typography.bodySmall,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant
                                )
                            }
                        }
                    }
                }
            }
            Spacer(Modifier.height(16.dp))
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.End
            ) {
                OutlinedButton(
                    onClick = onDismiss,
                    shape = RoundedCornerShape(12.dp)
                ) {
                    androidx.compose.material3.Text("关闭")
                }
            }
        }
    }
}

private fun formatSize(bytes: Long): String {
    return when {
        bytes < 1024 -> "${bytes}B"
        bytes < 1024 * 1024 -> "${bytes / 1024}KB"
        bytes < 1024 * 1024 * 1024 -> "${bytes / (1024 * 1024)}MB"
        else -> "${bytes / (1024 * 1024 * 1024)}GB"
    }
}

private fun getFileName(context: android.content.Context, uri: Uri): String? {
    return try {
        context.contentResolver.query(uri, null, null, null, null)?.use { cursor ->
            val nameIndex = cursor.getColumnIndex(OpenableColumns.DISPLAY_NAME)
            if (nameIndex != -1 && cursor.moveToFirst()) {
                cursor.getString(nameIndex)
            } else {
                uri.lastPathSegment
            }
        } ?: uri.lastPathSegment
    } catch (_: Exception) {
        uri.lastPathSegment
    }
}

private fun isTarXzFileName(fileName: String): Boolean {
    return fileName.lowercase().endsWith(".tar.xz")
}

private fun normalizeRuntimeName(fileName: String): String {
    val trimmedName = fileName.trim()
    return if (trimmedName.lowercase().endsWith(".tar.xz")) {
        trimmedName.dropLast(".tar.xz".length)
    } else {
        trimmedName
    }
}

