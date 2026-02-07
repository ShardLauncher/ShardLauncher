package com.lanrhyme.shardlauncher.ui.version.list

import android.os.Build
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.animation.*
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.interaction.collectIsPressedAsState
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.itemsIndexed
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.automirrored.filled.Sort
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.rotate
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavController
import coil.compose.AsyncImage
import com.lanrhyme.shardlauncher.R
import com.lanrhyme.shardlauncher.game.path.GamePathManager
import com.lanrhyme.shardlauncher.game.version.installed.Version
import com.lanrhyme.shardlauncher.game.version.installed.VersionType
import com.lanrhyme.shardlauncher.game.version.installed.VersionsManager
import com.lanrhyme.shardlauncher.ui.components.layout.LocalCardLayoutConfig
import com.lanrhyme.shardlauncher.ui.components.basic.PopupContainer
import com.lanrhyme.shardlauncher.ui.components.basic.SearchTextField
import com.lanrhyme.shardlauncher.ui.components.basic.ShardAlertDialog
import com.lanrhyme.shardlauncher.ui.components.basic.ShardDropdownMenu
import com.lanrhyme.shardlauncher.ui.components.basic.ShardEditDialog
import com.lanrhyme.shardlauncher.ui.components.basic.ShardInputField
import com.lanrhyme.shardlauncher.ui.components.basic.animatedAppearance
import com.lanrhyme.shardlauncher.ui.components.basic.selectableCard
import com.lanrhyme.shardlauncher.utils.file.PathHelper
import dev.chrisbanes.haze.hazeEffect
import com.lanrhyme.shardlauncher.ui.version.config.VersionConfigScreen
import com.lanrhyme.shardlauncher.ui.version.detail.ModDetailsDialog
import com.lanrhyme.shardlauncher.ui.version.detail.VersionOverviewScreen
import com.lanrhyme.shardlauncher.ui.version.dialog.CopyVersionDialog
import com.lanrhyme.shardlauncher.ui.version.dialog.DeleteVersionDialog
import com.lanrhyme.shardlauncher.ui.version.dialog.RenameVersionDialog
import com.lanrhyme.shardlauncher.ui.version.dialog.VersionsOperation
import com.lanrhyme.shardlauncher.ui.version.management.ModsManagementScreen
import com.lanrhyme.shardlauncher.ui.version.management.ResourcePacksManagementScreen
import com.lanrhyme.shardlauncher.ui.version.management.SavesManagementScreen
import com.lanrhyme.shardlauncher.ui.version.management.ShaderPacksManagementScreen
import com.lanrhyme.shardlauncher.ui.components.filemanager.FileSelectorScreen
import com.lanrhyme.shardlauncher.ui.components.filemanager.FileSelectorConfig
import com.lanrhyme.shardlauncher.ui.components.filemanager.FileSelectorMode
import com.lanrhyme.shardlauncher.ui.components.filemanager.FileSelectorResult

enum class VersionDetailPane(val title: String, val icon: ImageVector) {
    Overview("版本概览", Icons.Default.Info),
    Config("版本配置", Icons.Default.Settings),
    Mods("模组管理", Icons.Default.Extension),
    Saves("存档管理", Icons.Default.Save),
    ResourcePacks("资源包管理", Icons.Default.Style),
    ShaderPacks("光影包管理", Icons.Default.WbSunny)
}

@Composable
fun VersionScreen(navController: NavController, animationSpeed: Float) {
    val cardLayoutConfig = LocalCardLayoutConfig.current
    val isCardBlurEnabled = cardLayoutConfig.isCardBlurEnabled
    val cardAlpha = cardLayoutConfig.cardAlpha
    val hazeState = cardLayoutConfig.hazeState
    
    // 使用新的VersionsManager API，但保持原有UI
    LaunchedEffect(Unit) { VersionsManager.refresh("VersionScreen_Init") }

    var showDirectoryPopup by remember { mutableStateOf(false) }

    // 使用新的状态管理
    val versions = VersionsManager.versions
    val currentVersion by VersionsManager.currentVersion.collectAsState()
    val isRefreshing by VersionsManager.isRefreshing.collectAsState()
    
    var selectedVersion by remember { mutableStateOf<Version?>(null) }
    var selectedPane by remember { mutableStateOf<VersionDetailPane?>(null) }

    // 版本分类状态
    var versionCategory by remember { mutableStateOf(VersionCategory.ALL) }
    
    // 过滤版本（使用 derivedStateOf 确保 versions 变化时能响应更新）
    val filteredVersions by remember(versionCategory, isRefreshing) {
        derivedStateOf {
            when (versionCategory) {
                VersionCategory.ALL -> versions
                VersionCategory.VANILLA -> versions.filter { it.versionType == VersionType.VANILLA }
                VersionCategory.MODLOADER -> versions.filter { it.versionType == VersionType.MODLOADERS }
            }
        }
    }

    // 版本操作状态
    var versionsOperation by remember { mutableStateOf<VersionsOperation>(VersionsOperation.None) }
    var errorMessage by remember { mutableStateOf<String?>(null) }

    fun resetToVersionList() {
        selectedPane = null
    }

    Row(modifier = Modifier.fillMaxSize()) {
        // Left Pane (25%) - 保持原有设计
        val leftShape = RoundedCornerShape(16.dp)
        Card(
            modifier = Modifier.weight(0.25f)
                .fillMaxHeight()
                .padding(start = 16.dp, top = 16.dp, end = 8.dp, bottom = 16.dp)
                .then(
                    if (isCardBlurEnabled && Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
                        Modifier.clip(leftShape).hazeEffect(state = hazeState)
                    } else Modifier
                ),
            shape = leftShape,
            colors = CardDefaults.cardColors(
                containerColor = MaterialTheme.colorScheme.surface.copy(alpha = cardAlpha)
            )
        ) {
            LeftNavigationPane(
                selectedVersion = selectedVersion,
                selectedPane = selectedPane,
                onPaneSelected = { pane -> selectedPane = pane }
            )
        }

        // Right Pane (75%) - 保持原有设计但增强功能
        Box(modifier = Modifier.weight(0.75f).fillMaxHeight()) {
            Crossfade(targetState = selectedPane, label = "RightPaneCrossfade") { pane ->
                if (pane == null) {
                    GameVersionListContent(
                        versions = filteredVersions,
                        selectedVersion = selectedVersion,
                        currentVersion = currentVersion,
                        isRefreshing = isRefreshing,
                        versionCategory = versionCategory,
                        onVersionClick = { version -> selectedVersion = version },
                        onCategoryChange = { versionCategory = it },
                        animationSpeed = animationSpeed,
                        onShowDirectoryPopup = { showDirectoryPopup = true },
                        onVersionOperation = { versionsOperation = it },
                        onError = { errorMessage = it }
                    )
                } else {
                    RightDetailContent(pane, selectedVersion, onBack = { resetToVersionList() })
                }
            }
        }
    }

    // 弹窗和对话框
    if (showDirectoryPopup) {
        DirectorySelectionPopup(onDismissRequest = { showDirectoryPopup = false })
    }

    // 版本操作对话框
    VersionsOperation(
        versionsOperation = versionsOperation,
        updateVersionsOperation = { versionsOperation = it },
        onError = { errorMessage = it }
    )

    // 错误对话框
    errorMessage?.let { message ->
        ShardAlertDialog(
            title = "错误",
            text = message,
            onDismiss = { errorMessage = null }
        )
    }
}

@Composable
fun LeftNavigationPane(
        selectedVersion: Version?,
        selectedPane: VersionDetailPane?,
        onPaneSelected: (VersionDetailPane) -> Unit
) {
    Column(
            modifier = Modifier.fillMaxSize().padding(8.dp),
            horizontalAlignment = Alignment.CenterHorizontally
    ) {
        AnimatedVisibility(visible = selectedVersion != null) {
            Column(
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.Center
                ) {
                val iconFile = selectedVersion?.let { VersionsManager.getVersionIconFile(it) }
                AsyncImage(
                        model = iconFile,
                        contentDescription = selectedVersion?.getVersionName(),
                        placeholder = painterResource(id = R.drawable.img_minecraft), // Fallback
                        error = painterResource(id = R.drawable.img_minecraft),
                        modifier = Modifier.size(100.dp).padding(8.dp)
                )
                Text(
                    text = selectedVersion?.getVersionName() ?: "",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold,
                    textAlign = TextAlign.Center
                )
                Spacer(modifier = Modifier.height(24.dp))
                HorizontalDivider()
                Spacer(modifier = Modifier.height(16.dp))
            }
        }

        if (selectedVersion == null) {
            Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                Text(
                    "请从右侧选择一个版本",
                    textAlign = TextAlign.Center,
                    modifier = Modifier.padding(16.dp)
                ) // TODO: i18n
            }
        } else {
            LazyColumn(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                items(VersionDetailPane.entries) { pane ->
                    val isSelected = selectedPane == pane
                    TextButton(
                            onClick = { onPaneSelected(pane) },
                            modifier = Modifier.fillMaxWidth(),
                            shape = RoundedCornerShape(12.dp),
                            colors =
                                    ButtonDefaults.textButtonColors(
                                            containerColor =
                                                    if (isSelected)
                                                            MaterialTheme.colorScheme.primary.copy(
                                                                    alpha = 0.1f
                                                            )
                                                    else Color.Transparent,
                                            contentColor =
                                                    if (isSelected)
                                                            MaterialTheme.colorScheme.primary
                                                    else MaterialTheme.colorScheme.onSurface
                                    ),
                            contentPadding = PaddingValues(horizontal = 16.dp, vertical = 12.dp)
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(imageVector = pane.icon, contentDescription = pane.title)
                            Spacer(Modifier.width(16.dp))
                            Text(pane.title)
                        }
                        Spacer(Modifier.weight(1f))
                    }
                }
            }
        }
    }
}

@Composable
fun GameVersionListContent(
    versions: List<Version>,
    selectedVersion: Version?,
    currentVersion: Version?,
    isRefreshing: Boolean,
    versionCategory: VersionCategory,
    onVersionClick: (Version) -> Unit,
    onCategoryChange: (VersionCategory) -> Unit,
    animationSpeed: Float,
    onShowDirectoryPopup: () -> Unit,
    onVersionOperation: (VersionsOperation) -> Unit,
    onError: (String) -> Unit
) {
    val (isCardBlurEnabled, cardAlpha, hazeState) = LocalCardLayoutConfig.current
    var searchText by remember { mutableStateOf("") }

    // Filter versions based on search text
    val filteredVersions = remember(versions, searchText) {
        if (searchText.isBlank()) versions
        else versions.filter { it.getVersionName().contains(searchText, ignoreCase = true) }
    }

    // 计算各类别版本数量
    val allVersionsCount = VersionsManager.versions.size
    val vanillaVersionsCount = VersionsManager.versions.count { it.versionType == VersionType.VANILLA }
    val modloaderVersionsCount = VersionsManager.versions.count { it.versionType == VersionType.MODLOADERS }

    Column(modifier = Modifier.fillMaxSize().padding(16.dp)) {
        // 顶部工具栏 - 保持原有设计但增加分类
        Row(
            modifier = Modifier.fillMaxWidth().height(36.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            SearchTextField(
                value = searchText,
                onValueChange = { searchText = it },
                hint = "搜索版本",
                modifier = Modifier.weight(1f)
            )
            IconButton(onClick = { VersionsManager.refresh("Manual") }) {
                Icon(Icons.Default.Refresh, contentDescription = "刷新")
            }
            IconButton(onClick = { /* TODO: Sort */}) {
                Icon(Icons.AutoMirrored.Filled.Sort, contentDescription = "排序")
            }
            IconButton(onClick = { /* TODO: Filter */}) {
                Icon(Icons.Default.MoreVert, contentDescription = "筛选")
            }
            IconButton(onClick = onShowDirectoryPopup) {
                Icon(Icons.Default.Folder, contentDescription = "目录")
            }
        }

        // 版本分类标签
        Row(
            modifier = Modifier.fillMaxWidth().padding(vertical = 8.dp),
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            VersionCategoryItem(
                value = VersionCategory.ALL,
                versionsCount = allVersionsCount,
                selected = versionCategory == VersionCategory.ALL,
                onClick = { onCategoryChange(VersionCategory.ALL) }
            )
            VersionCategoryItem(
                value = VersionCategory.VANILLA,
                versionsCount = vanillaVersionsCount,
                selected = versionCategory == VersionCategory.VANILLA,
                onClick = { onCategoryChange(VersionCategory.VANILLA) }
            )
            VersionCategoryItem(
                value = VersionCategory.MODLOADER,
                versionsCount = modloaderVersionsCount,
                selected = versionCategory == VersionCategory.MODLOADER,
                onClick = { onCategoryChange(VersionCategory.MODLOADER) }
            )
        }

        if (isRefreshing) {
            Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                CircularProgressIndicator()
            }
        } else if (filteredVersions.isEmpty()) {
            Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                Text("暂无版本", fontSize = 18.sp)
            }
        } else {
            // 保持原有的网格卡片布局
            LazyVerticalGrid(
                columns = GridCells.Adaptive(minSize = 150.dp),
                contentPadding = PaddingValues(vertical = 16.dp),
                horizontalArrangement = Arrangement.spacedBy(16.dp),
                verticalArrangement = Arrangement.spacedBy(16.dp),
                modifier = Modifier.fillMaxSize()
            ) {
                itemsIndexed(filteredVersions) { index, version ->
                    GameVersionCard(
                        version = version,
                        isSelected = version == selectedVersion,
                        isCurrent = version == currentVersion,
                        onClick = { onVersionClick(version) },
                        onLongClick = { 
                            // 长按显示操作菜单
                            onVersionOperation(VersionsOperation.None) // 可以扩展为显示菜单
                        },
                        onPinClick = {
                            try {
                                version.setPinnedAndSave(!version.pinnedState)
                            } catch (e: Exception) {
                                onError("保存版本配置失败: ${e.message}")
                            }
                        },
                        onRenameClick = { onVersionOperation(VersionsOperation.Rename(version)) },
                        onCopyClick = { onVersionOperation(VersionsOperation.Copy(version)) },
                        onDeleteClick = { onVersionOperation(VersionsOperation.Delete(version)) },
                        index = index,
                        animationSpeed = animationSpeed
                    )
                }
            }
        }
    }
}

/**
 * 右侧详细信息内容组件
 * 根据选中的面板显示不同的详细信息
 *
 * @param pane 当前选中的面板
 * @param version 当前选中的版本
 * @param onBack 返回回调
 */
@Composable
fun RightDetailContent(pane: VersionDetailPane, version: Version?, onBack: () -> Unit) {
    if (version == null) return

    Column(modifier = Modifier.fillMaxSize().padding(16.dp)) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            IconButton(onClick = onBack) {
                Icon(
                    Icons.AutoMirrored.Filled.ArrowBack,
                    contentDescription = "返回版本列表"
                )
            }
            Spacer(Modifier.width(8.dp))
            Text(
                text = "${pane.title} - ${version.getVersionName()}",
                style = MaterialTheme.typography.titleLarge
            )
        }
        Spacer(Modifier.height(16.dp))

        when (pane) {
            VersionDetailPane.Overview -> {
                VersionOverviewScreen(
                    version = version,
                    onBack = onBack,
                    onError = { /* TODO: Handle error */ }
                )
            }
            VersionDetailPane.Config -> {
                VersionConfigScreen(
                    version = version,
                    config = version.getVersionConfig(),
                    onConfigChange = { /* Config updates are handled internally */ },
                    onSave = {
                        version.getVersionConfig().save()
                    },
                    onError = { /* TODO: Handle error */ }
                )
            }
            VersionDetailPane.Mods -> {
                ModsManagementScreen(version = version, onBack = onBack)
            }
            VersionDetailPane.Saves -> {
                SavesManagementScreen(version = version, onBack = onBack)
            }
            VersionDetailPane.ResourcePacks -> {
                ResourcePacksManagementScreen(version = version, onBack = onBack)
            }
            VersionDetailPane.ShaderPacks -> {
                ShaderPacksManagementScreen(version = version, onBack = onBack)
            }
        }
    }
}

@Composable
fun GameVersionCard(
    version: Version,
    isSelected: Boolean,
    isCurrent: Boolean = false,
    onClick: () -> Unit,
    onLongClick: () -> Unit = {},
    onPinClick: () -> Unit = {},
    onRenameClick: () -> Unit = {},
    onCopyClick: () -> Unit = {},
    onDeleteClick: () -> Unit = {},
    index: Int,
    animationSpeed: Float
) {
    val (isCardBlurEnabled, cardAlpha, hazeState) = LocalCardLayoutConfig.current
    val interactionSource = remember { MutableInteractionSource() }
    val isPressed by interactionSource.collectIsPressedAsState()
    var showMenu by remember { mutableStateOf(false) }

    val shape = RoundedCornerShape(18.dp)

    Card(
        modifier = Modifier
            .animatedAppearance(index, animationSpeed)
            .size(150.dp)
            .selectableCard(isSelected = isSelected, isPressed = isPressed)
            .then(
                if (isCardBlurEnabled && Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
                    Modifier.clip(shape).hazeEffect(state = hazeState)
                } else Modifier.clip(shape)
            )
            .clickable(
                interactionSource = interactionSource,
                indication = null,
                onClick = onClick
            ),
        shape = shape,
        border = when {
            isCurrent -> BorderStroke(2.dp, MaterialTheme.colorScheme.primary)
            isSelected -> BorderStroke(2.dp, MaterialTheme.colorScheme.secondary)
            else -> null
        },
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surface.copy(alpha = cardAlpha)
        )
    ) {
        Box(modifier = Modifier.fillMaxSize()) {
            // 版本图标
                            Box(modifier = Modifier.align(Alignment.Center).fillMaxSize(0.5f)) {
                                AsyncImage(
                                    model = VersionsManager.getVersionIconFile(version),
                                    contentDescription = "${version.getVersionName()} icon",
                                    placeholder = painterResource(id = R.drawable.img_minecraft),
                                    error = painterResource(id = R.drawable.img_minecraft),
                                    contentScale = ContentScale.Fit,
                                    modifier = Modifier.fillMaxSize()
                                )
                            }

                            // 置顶图标
                            if (version.pinnedState) {
                                Icon(
                                    imageVector = Icons.Default.PushPin,
                                    contentDescription = "置顶",
                                    tint = MaterialTheme.colorScheme.primary,
                                    modifier = Modifier
                                        .align(Alignment.TopEnd)
                                        .padding(8.dp)
                                        .size(16.dp)
                                        .rotate(45f)
                                )
                            }

                            // 当前版本标识
                            if (isCurrent) {
                                Surface(
                                    color = MaterialTheme.colorScheme.primary,
                                    shape = RoundedCornerShape(bottomStart = 8.dp),
                                    modifier = Modifier.align(Alignment.TopEnd)
                                ) {
                                    Text(
                                        text = "当前",
                                        color = MaterialTheme.colorScheme.onPrimary,
                                        style = MaterialTheme.typography.labelSmall,
                                        modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                                    )
                                }
                            }

                            // 版本名称
                            Text(
                                text = version.getVersionName(),
                                style = MaterialTheme.typography.titleSmall,
                                fontWeight = FontWeight.Bold,
                                color = MaterialTheme.colorScheme.onSurface,
                                maxLines = 2,
                                overflow = TextOverflow.Ellipsis,
                                modifier = Modifier.align(Alignment.BottomStart).padding(12.dp)
                            )

                            // 更多操作按钮
                            Box(modifier = Modifier.align(Alignment.BottomEnd)) {
                                IconButton(
                                    onClick = { showMenu = true },
                                    modifier = Modifier.size(32.dp)
                                ) {
                                    Icon(
                                        imageVector = Icons.Default.MoreVert,
                                        contentDescription = "更多操作",
                                        modifier = Modifier.size(16.dp)
                                    )
                                }

                                ShardDropdownMenu(
                                    expanded = showMenu,
                                    onDismissRequest = { showMenu = false }
                                ) {
                                    DropdownMenuItem(
                                        text = { Text(if (version.pinnedState) "取消置顶" else "置顶") },
                        leadingIcon = {
                            Icon(
                                imageVector = Icons.Default.PushPin,
                                contentDescription = null
                            )
                        },
                        onClick = {
                            onPinClick()
                            showMenu = false
                        }
                    )
                    DropdownMenuItem(
                        text = { Text("重命名") },
                        leadingIcon = {
                            Icon(
                                imageVector = Icons.Default.Edit,
                                contentDescription = null
                            )
                        },
                        onClick = {
                            onRenameClick()
                            showMenu = false
                        }
                    )
                    DropdownMenuItem(
                        text = { Text("复制") },
                        leadingIcon = {
                            Icon(
                                imageVector = Icons.Default.FileCopy,
                                contentDescription = null
                            )
                        },
                        onClick = {
                            onCopyClick()
                            showMenu = false
                        }
                    )
                    DropdownMenuItem(
                        text = { Text("删除") },
                        leadingIcon = {
                            Icon(
                                imageVector = Icons.Default.Delete,
                                contentDescription = null,
                                tint = MaterialTheme.colorScheme.error
                            )
                        },
                        onClick = {
                            onDeleteClick()
                            showMenu = false
                        }
                    )
                }
            }
        }
    }
}


@Composable
fun GamePathNameDialog(
    onDismissRequest: () -> Unit,
    onConfirm: (String) -> Unit
) {
    var name by remember { mutableStateOf("自定义目录") }

    ShardEditDialog(
        title = "命名游戏目录",
        value = name,
        onValueChange = { name = it },
        label = "目录名称",
        isError = name.isEmpty(),
        supportingText = {
            if (name.isEmpty()) {
                Text(text = "目录名称不能为空")
            }
        },
        singleLine = true,
        onDismissRequest = onDismissRequest,
        onConfirm = {
            if (name.isNotEmpty()) {
                onConfirm(name)
            }
        }
    )
}
@Composable
fun DirectorySelectionPopup(onDismissRequest: () -> Unit) {
    val context = LocalContext.current
    val gamePaths by GamePathManager.gamePathData.collectAsState()

    val currentPathId = GamePathManager.currentPathId

    var showFileSelector by remember { mutableStateOf(false) }
    var showGamePathNameDialog by remember { mutableStateOf(false) }
    var pendingGamePath by remember { mutableStateOf<String?>(null) }
    var showPermissionErrorDialog by remember { mutableStateOf(false) }
    var permissionError by remember { mutableStateOf<String?>(null) }

    PopupContainer(
        visible = true,
        onDismissRequest = onDismissRequest,
        modifier = Modifier
            .width(320.dp)
            .padding(16.dp)
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Text(
                "选择游戏目录", // TODO: i18n
                style = MaterialTheme.typography.titleLarge,
                fontWeight = FontWeight.Bold
            )
            Spacer(modifier = Modifier.height(16.dp))

            LazyColumn(
                modifier = Modifier.height(400.dp).fillMaxWidth(),
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                items(gamePaths) { path ->
                    val isSelected = path.id == currentPathId
                    Card(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clickable {
                                // 检查存储权限
                                if (!GamePathManager.hasStoragePermission(context)) {
                                    permissionError = "未授予存储权限，无法切换游戏目录"
                                    showPermissionErrorDialog = true
                                    return@clickable
                                }
                                try {
                                    GamePathManager.selectPath(context, path.id)
                                    onDismissRequest()
                                } catch (e: Exception) {
                                    permissionError = e.message ?: "切换游戏目录失败"
                                    showPermissionErrorDialog = true
                                }
                            },
                        shape = RoundedCornerShape(12.dp),
                        colors = CardDefaults.cardColors(
                            containerColor = if (isSelected)
                                MaterialTheme.colorScheme.primaryContainer
                            else MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f)
                        ),
                        border = if (isSelected) BorderStroke(1.dp, MaterialTheme.colorScheme.primary) else null
                    ) {
                        Row(
                            modifier = Modifier.padding(12.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Column(modifier = Modifier.weight(1f)) {
                                Text(
                                    path.title,
                                    style = MaterialTheme.typography.titleMedium,
                                    fontWeight = FontWeight.Bold
                                )
                                Text(
                                    path.path,
                                    style = MaterialTheme.typography.bodySmall,
                                    maxLines = 1,
                                    overflow = TextOverflow.Ellipsis,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant
                                )
                            }
                            if (path.id != GamePathManager.DEFAULT_ID) {
                                IconButton(onClick = { GamePathManager.removePath(path.id) }) {
                                    Icon(
                                        Icons.Default.Delete,
                                        contentDescription = "Delete", // TODO: i18n
                                        tint = MaterialTheme.colorScheme.error
                                    )
                                }
                            }
                        }
                    }
                }

                item {
                    OutlinedButton(
                        onClick = { showFileSelector = true },
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(12.dp)
                    ) {
                        Icon(Icons.Default.Add, contentDescription = null)
                        Spacer(modifier = Modifier.width(8.dp))
                        Text("添加新目录") // TODO: i18n
                    }
                }
            }
        }
    }

    // 自定义文件选择器
    if (showFileSelector) {
        FileSelectorScreen(
            visible = showFileSelector,
            config = FileSelectorConfig(
                initialPath = android.os.Environment.getExternalStorageDirectory(),
                mode = FileSelectorMode.DIRECTORY_ONLY,
                showHiddenFiles = true,
                allowCreateDirectory = true
            ),
            onDismissRequest = { showFileSelector = false },
            onSelection = { result ->
                when (result) {
                    is FileSelectorResult.Selected -> {
                        // 检查是否是 FCL 公有目录
                        if (result.path.absolutePath.contains("/FCL/.minecraft")) {
                            // FCL公有目录直接添加，不显示命名对话框
                            GamePathManager.addNewPath("FCL公有目录", result.path.absolutePath)
                            showFileSelector = false
                        } else {
                            // 其他目录显示命名对话框
                            pendingGamePath = result.path.absolutePath
                            showGamePathNameDialog = true
                        }
                    }
                    FileSelectorResult.Cancelled -> { /* 用户取消 */ }
                    is FileSelectorResult.MultipleSelected -> { /* 不支持多选 */ }
                }
            }
        )
    }

    // 游戏目录命名对话框
    if (showGamePathNameDialog) {
        GamePathNameDialog(
            onDismissRequest = { 
                showGamePathNameDialog = false
                pendingGamePath = null
            },
            onConfirm = { name ->
                val path = pendingGamePath
                if (path != null) {
                    GamePathManager.addNewPath(name, path)
                }
                showGamePathNameDialog = false
                pendingGamePath = null
                showFileSelector = false
            }
        )
    }

    // 权限错误提示对话框
    if (showPermissionErrorDialog) {
        ShardAlertDialog(
            title = "需要权限",
            text = permissionError ?: "操作失败",
            onDismiss = { showPermissionErrorDialog = false },
            onConfirm = { showPermissionErrorDialog = false },
            onDismissRequest = { showPermissionErrorDialog = false }
        )
    }
}

@Composable
fun VersionsOperation(
    versionsOperation: VersionsOperation,
    updateVersionsOperation: (VersionsOperation) -> Unit,
    onError: (String) -> Unit
) {
    when(versionsOperation) {
        is VersionsOperation.None -> {}
        is VersionsOperation.Rename -> {
            RenameVersionDialog(
                version = versionsOperation.version,
                onDismissRequest = { updateVersionsOperation(VersionsOperation.None) },
                onConfirm = { name: String ->
                    try {
                        VersionsManager.renameVersion(versionsOperation.version, name)
                        updateVersionsOperation(VersionsOperation.None)
                    } catch (e: Exception) {
                        onError("重命名版本失败: ${e.message}")
                        updateVersionsOperation(VersionsOperation.None)
                    }
                }
            )
        }
        is VersionsOperation.Copy -> {
            CopyVersionDialog(
                version = versionsOperation.version,
                onDismissRequest = { updateVersionsOperation(VersionsOperation.None) },
                onConfirm = { name: String, copyAll: Boolean ->
                    try {
                        VersionsManager.copyVersion(versionsOperation.version, name, copyAll)
                        updateVersionsOperation(VersionsOperation.None)
                    } catch (e: Exception) {
                        onError("复制版本失败: ${e.message}")
                        updateVersionsOperation(VersionsOperation.None)
                    }
                }
            )
        }
        is VersionsOperation.InvalidDelete -> {
            updateVersionsOperation(
                VersionsOperation.Delete(
                    versionsOperation.version,
                    "此版本无效，将被删除"
                )
            )
        }
        is VersionsOperation.Delete -> {
            DeleteVersionDialog(
                version = versionsOperation.version,
                message = versionsOperation.text,
                onDismissRequest = { updateVersionsOperation(VersionsOperation.None) },
                onConfirm = {
                    try {
                        VersionsManager.deleteVersion(versionsOperation.version)
                        updateVersionsOperation(VersionsOperation.None)
                    } catch (e: Exception) {
                        onError("删除版本失败: ${e.message}")
                        updateVersionsOperation(VersionsOperation.None)
                    }
                }
            )
        }
    }
}


