package com.lanrhyme.shardlauncher.ui.version.management

import android.os.Build
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material.icons.outlined.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import com.lanrhyme.shardlauncher.game.mod.LocalMod
import com.lanrhyme.shardlauncher.game.mod.ModCache
import com.lanrhyme.shardlauncher.game.mod.ModMetadataParser
import com.lanrhyme.shardlauncher.game.mod.RemoteMod
import com.lanrhyme.shardlauncher.game.version.installed.Version
import com.lanrhyme.shardlauncher.ui.components.layout.LocalCardLayoutConfig
import com.lanrhyme.shardlauncher.ui.version.detail.ModDetailsDialog
import com.lanrhyme.shardlauncher.ui.components.filemanager.FileSelectorScreen
import com.lanrhyme.shardlauncher.ui.components.filemanager.FileSelectorConfig
import com.lanrhyme.shardlauncher.ui.components.filemanager.FileSelectorMode
import com.lanrhyme.shardlauncher.ui.components.filemanager.FileSelectorResult
import com.lanrhyme.shardlauncher.utils.file.FolderUtils
import dev.chrisbanes.haze.hazeEffect
import kotlinx.coroutines.launch
import java.io.File

@Composable
fun ModsManagementScreen(
    version: Version,
    onBack: () -> Unit
) {
    val (isCardBlurEnabled, cardAlpha, hazeState) = LocalCardLayoutConfig.current
    val modsFolder = File(version.getGameDir(), "mods")
    var modsList by remember { mutableStateOf<List<RemoteMod>?>(null) }
    var refreshTrigger by remember { mutableIntStateOf(0) }
    var selectedModForDetails by remember { mutableStateOf<RemoteMod?>(null) }
    val context = LocalContext.current
    val scope = rememberCoroutineScope()
    
    // 初始化缓存
    LaunchedEffect(Unit) {
        ModCache.init(context)
        ModCache.cleanExpiredCache()
    }

    // 文件选择器
    var showFileSelector by remember { mutableStateOf(false) }

    // 刷新模组列表
    LaunchedEffect(refreshTrigger) {
        modsList = null // 显示加载状态
        
        if (modsFolder.exists()) {
            val localMods = modsFolder.listFiles()?.filter { 
                it.isFile && (it.extension == "jar" || it.extension == "zip")
            }?.mapNotNull { modFile ->
                ModMetadataParser.parseModFile(modFile)
            }?.sortedBy { it.name } ?: emptyList()
            
            val remoteMods = localMods.map { RemoteMod(it) }
            modsList = remoteMods
            
            // 自动加载所有模组的远程信息（从缓存）
            remoteMods.forEach { remoteMod ->
                scope.launch {
                    remoteMod.load(loadFromCache = true)
                }
            }
        } else {
            modsList = emptyList()
        }
    }

    Column(
        modifier = Modifier.fillMaxSize(),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        // 顶部操作栏
        val topCardShape = RoundedCornerShape(16.dp)
        Card(
            modifier = Modifier
                .fillMaxWidth()
                .then(
                    if (isCardBlurEnabled && Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
                        Modifier.clip(topCardShape).hazeEffect(state = hazeState)
                    } else Modifier
                ),
            shape = topCardShape,
            colors = CardDefaults.cardColors(
                containerColor = MaterialTheme.colorScheme.surface.copy(alpha = cardAlpha)
            )
        ) {
            Column(
                modifier = Modifier.padding(16.dp),
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = "模组管理",
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold,
                        modifier = Modifier.weight(1f)
                    )

                    Text(
                        text = "${modsList?.size ?: 0} 个模组",
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
                
                Row(
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    OutlinedButton(
                        onClick = { 
                            // 创建mods文件夹
                            if (!modsFolder.exists()) {
                                modsFolder.mkdirs()
                            }
                            // 只刷新加载失败的模组
                            modsList?.forEach { remoteMod ->
                                if (remoteMod.lastLoadFailed && !remoteMod.isLoading) {
                                    scope.launch {
                                        remoteMod.load(loadFromCache = false)
                                    }
                                }
                            }
                        }
                    ) {
                        Icon(Icons.Default.Refresh, contentDescription = null)
                        Spacer(modifier = Modifier.width(4.dp))
                        Text("刷新失败")
                    }
                    
                    Button(
                        onClick = {
                            showFileSelector = true
                        }
                    ) {
                        Icon(Icons.Default.Add, contentDescription = null)
                        Spacer(modifier = Modifier.width(4.dp))
                        Text("添加模组")
                    }
                    
                    OutlinedButton(
                        onClick = {
                            FolderUtils.openFolder(context, modsFolder) { error ->
                                // Handle error if needed
                            }
                        }
                    ) {
                        Icon(Icons.Default.Folder, contentDescription = null)
                        Spacer(modifier = Modifier.width(4.dp))
                        Text("打开文件夹")
                    }
                }
            }
        }

        // 模组列表
        ModsList(
            modsList = modsList,
            onLoad = { remoteMod ->
                scope.launch {
                    remoteMod.load(loadFromCache = true)
                }
            },
            onForceRefresh = { remoteMod ->
                scope.launch {
                    remoteMod.load(loadFromCache = false)
                }
            },
            onEnable = { remoteMod ->
                remoteMod.localMod.enable()
                refreshTrigger++
            },
            onDisable = { remoteMod ->
                remoteMod.localMod.disable()
                refreshTrigger++
            },
            onSwapMoreInfo = { id, platform ->
                // 找到对应的模组并显示详情
                modsList?.find { it.projectInfo?.id == id }?.let { remoteMod ->
                    selectedModForDetails = remoteMod
                }
            },
            onDelete = { remoteMod ->
                remoteMod.localMod.file.delete()
                refreshTrigger++
            },
            isCardBlurEnabled = isCardBlurEnabled,
            cardAlpha = cardAlpha,
            hazeState = hazeState
        )
    }
    
    // 显示模组详情对话框
    selectedModForDetails?.let { remoteMod ->
        ModDetailsDialog(
            remoteMod = remoteMod,
            onDismiss = { selectedModForDetails = null }
        )
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
                    file.isFile && (file.extension == "jar" || file.extension == "zip")
                }
            ),
            onDismissRequest = { showFileSelector = false },
            onSelection = { result ->
                when (result) {
                    is FileSelectorResult.Selected -> {
                        try {
                            val sourceFile = result.path
                            val fileName = sourceFile.name
                            val targetFile = File(modsFolder, fileName)
                            
                            if (!modsFolder.exists()) {
                                modsFolder.mkdirs()
                            }
                            
                            sourceFile.inputStream().use { input ->
                                targetFile.outputStream().use { output ->
                                    input.copyTo(output)
                                }
                            }
                            refreshTrigger++
                        } catch (e: Exception) {
                            // TODO: 显示错误信息
                        }
                    }
                    FileSelectorResult.Cancelled -> { /* 用户取消 */ }
                    is FileSelectorResult.MultipleSelected -> { /* 不支持多选 */ }
                }
                showFileSelector = false
            }
        )
    }
}

@Composable
private fun ModsList(
    modsList: List<RemoteMod>?,
    onLoad: (RemoteMod) -> Unit,
    onForceRefresh: (RemoteMod) -> Unit,
    onEnable: (RemoteMod) -> Unit,
    onDisable: (RemoteMod) -> Unit,
    onSwapMoreInfo: (id: String, platform: com.lanrhyme.shardlauncher.game.mod.Platform) -> Unit,
    onDelete: (RemoteMod) -> Unit,
    isCardBlurEnabled: Boolean,
    cardAlpha: Float,
    hazeState: dev.chrisbanes.haze.HazeState
) {
    modsList?.let { list ->
        if (list.isNotEmpty()) {
            LazyColumn(
                modifier = Modifier.fillMaxWidth(),
                contentPadding = PaddingValues(horizontal = 12.dp, vertical = 6.dp),
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                items(list) { mod ->
                    ModItemLayout(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(vertical = 6.dp),
                        mod = mod,
                        onLoad = { onLoad(mod) },
                        onForceRefresh = { onForceRefresh(mod) },
                        onClick = { /* TODO: 实现选择功能 */ },
                        onEnable = { onEnable(mod) },
                        onDisable = { onDisable(mod) },
                        onSwapMoreInfo = onSwapMoreInfo,
                        onDelete = { onDelete(mod) },
                        selected = false, // TODO: 实现选择状态
                        isCardBlurEnabled = isCardBlurEnabled,
                        cardAlpha = cardAlpha,
                        hazeState = hazeState
                    )
                }
            }
        } else {
            // 空状态
            val emptyCardShape = RoundedCornerShape(16.dp)
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .then(
                        if (isCardBlurEnabled && Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
                            Modifier.clip(emptyCardShape).hazeEffect(state = hazeState)
                        } else Modifier
                    ),
                shape = emptyCardShape,
                colors = CardDefaults.cardColors(
                    containerColor = MaterialTheme.colorScheme.surface.copy(alpha = cardAlpha)
                )
            ) {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(32.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Column(
                        horizontalAlignment = Alignment.CenterHorizontally,
                        verticalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        Icon(
                            imageVector = Icons.Default.Extension,
                            contentDescription = null,
                            modifier = Modifier.size(48.dp),
                            tint = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                        Text(
                    text = "暂无模组",
                    style = MaterialTheme.typography.titleMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
                Text(
                    text = "点击\"添加模组\"按钮来安装模组",
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
                    }
                }
            }
        }
    } ?: run {
        // 加载状态
        Box(
            modifier = Modifier.fillMaxWidth().padding(32.dp),
            contentAlignment = Alignment.Center
        ) {
            CircularProgressIndicator()
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun ModItemLayout(
    modifier: Modifier = Modifier,
    mod: RemoteMod,
    onLoad: () -> Unit = {},
    onForceRefresh: () -> Unit = {},
    onClick: () -> Unit = {},
    onEnable: () -> Unit,
    onDisable: () -> Unit,
    onSwapMoreInfo: (id: String, platform: com.lanrhyme.shardlauncher.game.mod.Platform) -> Unit,
    onDelete: () -> Unit,
    selected: Boolean,
    isCardBlurEnabled: Boolean,
    cardAlpha: Float,
    hazeState: dev.chrisbanes.haze.HazeState
) {
    val context = LocalContext.current
    val projectInfo = mod.projectInfo
    val itemCardShape = RoundedCornerShape(12.dp)
    
    // 不再自动加载，因为已经在初始化时从缓存加载了

    Surface(
        modifier = modifier,
        onClick = onClick,
        shape = itemCardShape,
        color = if (isCardBlurEnabled && Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
            MaterialTheme.colorScheme.surface.copy(alpha = cardAlpha)
        } else {
            MaterialTheme.colorScheme.surface
        }
    ) {
        Row(
            modifier = Modifier.padding(all = 8.dp),
            horizontalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            // 模组图标
            ModIcon(
                modifier = Modifier.clip(shape = RoundedCornerShape(10.dp)),
                mod = mod,
                iconSize = 48.dp
            )

            // 模组信息
            Column(
                modifier = Modifier
                    .weight(1f)
                    .align(Alignment.CenterVertically),
                verticalArrangement = Arrangement.spacedBy(4.dp)
            ) {
                val localMod = mod.localMod
                when {
                    localMod.notMod && projectInfo == null -> {
                        // 非模组，只展示文件名称
                        Text(
                            text = localMod.file.name,
                            style = MaterialTheme.typography.titleSmall,
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis
                        )
                        if (localMod.loader != LocalMod.ModLoader.UNKNOWN) {
                            Surface(
                                shape = RoundedCornerShape(4.dp),
                                color = MaterialTheme.colorScheme.primaryContainer
                            ) {
                                Text(
                                    text = localMod.loader.displayName,
                                    style = MaterialTheme.typography.labelSmall,
                                    modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                                )
                            }
                        }
                    }
                    else -> {
                        Row(
                            horizontalArrangement = Arrangement.spacedBy(8.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            val displayTitle = projectInfo?.title ?: localMod.name
                            Text(
                                modifier = Modifier.weight(1f, fill = false),
                                text = displayTitle,
                                style = MaterialTheme.typography.titleSmall,
                                maxLines = 1,
                                overflow = TextOverflow.Ellipsis
                            )
                            
                            // 模组加载器标签
                            if (localMod.loader != LocalMod.ModLoader.UNKNOWN) {
                                Surface(
                                    shape = RoundedCornerShape(4.dp),
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
                                        style = MaterialTheme.typography.labelSmall,
                                        color = when (localMod.loader) {
                                            LocalMod.ModLoader.FABRIC -> MaterialTheme.colorScheme.primary
                                            LocalMod.ModLoader.FORGE -> MaterialTheme.colorScheme.secondary
                                            LocalMod.ModLoader.QUILT -> MaterialTheme.colorScheme.tertiary
                                            LocalMod.ModLoader.NEOFORGE -> MaterialTheme.colorScheme.error
                                            else -> MaterialTheme.colorScheme.onSurfaceVariant
                                        },
                                        modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                                    )
                                }
                            }
                        }

                        Text(
                            modifier = Modifier.alpha(0.7f),
                            text = "文件名: ${localMod.file.name}",
                            style = MaterialTheme.typography.bodySmall,
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis
                        )
                    }
                }
            }

            // 操作按钮区域
            Row(
                modifier = Modifier.align(Alignment.CenterVertically),
                horizontalArrangement = Arrangement.spacedBy(8.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                // 加载状态指示器
                if (mod.isLoading) {
                    CircularProgressIndicator(
                        modifier = Modifier
                            .size(18.dp)
                            .alpha(0.7f),
                        strokeWidth = 2.dp
                    )
                    Spacer(modifier = Modifier.width(4.dp))
                } else if (mod.lastLoadFailed) {
                    // 加载失败指示器
                    Icon(
                        imageVector = Icons.Default.Warning,
                        contentDescription = "加载失败",
                        tint = MaterialTheme.colorScheme.error,
                        modifier = Modifier
                            .size(18.dp)
                            .alpha(0.7f)
                    )
                    Spacer(modifier = Modifier.width(4.dp))
                } else if (mod.isLoaded) {
                    // 刷新按钮
                    IconButton(
                        modifier = Modifier.size(38.dp),
                        onClick = onForceRefresh
                    ) {
                        Icon(
                            imageVector = Icons.Outlined.Refresh,
                            contentDescription = "刷新"
                        )
                    }
                }

                // 启用/禁用复选框
                Checkbox(
                    checked = mod.localMod.isEnabled(),
                    onCheckedChange = { checked ->
                        if (checked) onEnable()
                        else onDisable()
                    }
                )

                // 详细信息按钮
                if (projectInfo != null) {
                    IconButton(
                        modifier = Modifier.size(38.dp),
                        onClick = {
                            onSwapMoreInfo(projectInfo.id, projectInfo.platform)
                        }
                    ) {
                        Icon(
                            imageVector = Icons.Outlined.Info,
                            contentDescription = "详细信息"
                        )
                    }
                } else if (!mod.localMod.notMod) {
                    // 本地模组信息提示
                    LocalModInfoTooltip(mod.localMod)
                }

                // 删除按钮
                IconButton(
                    modifier = Modifier.size(38.dp),
                    onClick = onDelete
                ) {
                    Icon(
                        imageVector = Icons.Outlined.Delete,
                        contentDescription = "删除"
                    )
                }
            }
        }
    }
}

@Composable
private fun ModIcon(
    modifier: Modifier = Modifier,
    mod: RemoteMod,
    iconSize: androidx.compose.ui.unit.Dp,
    disableContainerSize: androidx.compose.ui.unit.Dp = 28.dp
) {
    Box(modifier = modifier) {
        val projectInfo = mod.projectInfo
        
        // 优先显示远程图标，然后是本地图标，最后是默认图标
        when {
            projectInfo?.iconUrl != null -> {
                // 显示远程图标
                coil.compose.AsyncImage(
                    model = projectInfo.iconUrl,
                    contentDescription = "模组图标",
                    modifier = Modifier
                        .size(iconSize)
                        .clip(RoundedCornerShape(8.dp))
                )
            }
            mod.localMod.icon != null -> {
                // 显示本地图标
                Image(
                    bitmap = android.graphics.BitmapFactory.decodeByteArray(
                        mod.localMod.icon, 0, mod.localMod.icon.size
                    ).asImageBitmap(),
                    contentDescription = "模组图标",
                    modifier = Modifier
                        .size(iconSize)
                        .clip(RoundedCornerShape(8.dp))
                )
            }
            else -> {
                // 显示默认图标
                DefaultModIcon(mod.localMod.loader, iconSize)
            }
        }

        // 禁用状态覆盖
        androidx.compose.animation.AnimatedVisibility(
            modifier = Modifier.align(Alignment.Center),
            visible = mod.localMod.isDisabled(),
            enter = androidx.compose.animation.fadeIn(),
            exit = androidx.compose.animation.fadeOut()
        ) {
            Surface(
                modifier = Modifier
                    .padding(all = 4.dp)
                    .size(disableContainerSize),
                color = MaterialTheme.colorScheme.secondaryContainer,
                contentColor = MaterialTheme.colorScheme.onSecondaryContainer,
                shape = CircleShape
            ) {
                Box(contentAlignment = Alignment.Center) {
                    Icon(
                        imageVector = Icons.Default.Block,
                        contentDescription = "已禁用",
                        modifier = Modifier.size(16.dp)
                    )
                }
            }
        }
    }
}

@Composable
private fun DefaultModIcon(
    loader: LocalMod.ModLoader,
    iconSize: androidx.compose.ui.unit.Dp
) {
    val iconColor = when (loader) {
        LocalMod.ModLoader.FABRIC -> MaterialTheme.colorScheme.primary
        LocalMod.ModLoader.FORGE -> MaterialTheme.colorScheme.secondary
        LocalMod.ModLoader.QUILT -> MaterialTheme.colorScheme.tertiary
        LocalMod.ModLoader.NEOFORGE -> MaterialTheme.colorScheme.error
        LocalMod.ModLoader.UNKNOWN -> MaterialTheme.colorScheme.onSurfaceVariant
    }
    
    Box(
        modifier = Modifier
            .size(iconSize)
            .background(iconColor.copy(alpha = 0.1f), RoundedCornerShape(8.dp)),
        contentAlignment = Alignment.Center
    ) {
        Icon(
            imageVector = Icons.Default.Extension,
            contentDescription = "模组图标",
            tint = iconColor,
            modifier = Modifier.size(iconSize * 0.5f)
        )
    }
}

@Composable
private fun LocalModInfoTooltip(localMod: LocalMod) {
    var showDialog by remember { mutableStateOf(false) }

    IconButton(
        modifier = Modifier.size(38.dp),
        onClick = { showDialog = true }
    ) {
        Icon(
            imageVector = Icons.Outlined.Info,
            contentDescription = "模组信息",
            modifier = Modifier.alpha(0.7f)
        )
    }

    if (showDialog) {
        AlertDialog(
            onDismissRequest = { showDialog = false },
            title = {
                Text(
                    text = localMod.name,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
            },
            text = {
                Column(
                    verticalArrangement = Arrangement.spacedBy(8.dp),
                    modifier = Modifier.verticalScroll(rememberScrollState())
                ) {
                    // ID
                    if (localMod.id.isNotEmpty()) {
                        Text(
                            text = "ID: ${localMod.id}",
                            style = MaterialTheme.typography.bodySmall
                        )
                    }
                    // 版本
                    if (!localMod.version.isNullOrEmpty()) {
                        Text(
                            text = "版本: ${localMod.version}",
                            style = MaterialTheme.typography.bodySmall
                        )
                    }
                    // 加载器
                    Text(
                        text = "加载器: ${localMod.loader.displayName}",
                        style = MaterialTheme.typography.bodySmall
                    )
                    // 作者
                    if (localMod.authors.isNotEmpty()) {
                        Text(
                            text = "作者: ${localMod.authors.joinToString(", ")}",
                            style = MaterialTheme.typography.bodySmall
                        )
                    }
                    // 描述
                    if (!localMod.description.isNullOrEmpty()) {
                        Text(
                            text = "描述: ${localMod.description}",
                            style = MaterialTheme.typography.bodyMedium,
                            modifier = Modifier.padding(top = 4.dp)
                        )
                    }
                    // 文件名
                    Text(
                        text = "文件: ${localMod.file.name}",
                        style = MaterialTheme.typography.labelSmall,
                        modifier = Modifier.alpha(0.7f)
                    )
                }
            },
            confirmButton = {
                TextButton(onClick = { showDialog = false }) {
                    Text("关闭")
                }
            }
        )
    }
}