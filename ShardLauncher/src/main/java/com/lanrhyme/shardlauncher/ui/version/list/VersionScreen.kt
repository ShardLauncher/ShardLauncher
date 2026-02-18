package com.lanrhyme.shardlauncher.ui.version.list

import android.os.Build
import androidx.compose.animation.*
import androidx.compose.animation.core.Spring
import androidx.compose.animation.core.spring
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.interaction.collectIsPressedAsState
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.itemsIndexed
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.*
import androidx.compose.material.icons.outlined.Search
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.rotate
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.graphicsLayer
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
import com.lanrhyme.shardlauncher.ui.components.basic.*
import com.lanrhyme.shardlauncher.ui.version.config.VersionConfigScreen
import com.lanrhyme.shardlauncher.ui.version.detail.VersionOverviewScreen
import com.lanrhyme.shardlauncher.ui.version.dialog.VersionsOperation
import com.lanrhyme.shardlauncher.ui.version.management.ModsManagementScreen
import com.lanrhyme.shardlauncher.ui.version.management.ResourcePacksManagementScreen
import com.lanrhyme.shardlauncher.ui.version.management.SavesManagementScreen
import com.lanrhyme.shardlauncher.ui.version.management.ShaderPacksManagementScreen
import com.lanrhyme.shardlauncher.ui.components.filemanager.FileSelectorScreen
import com.lanrhyme.shardlauncher.ui.components.filemanager.FileSelectorConfig
import com.lanrhyme.shardlauncher.ui.components.filemanager.FileSelectorMode
import com.lanrhyme.shardlauncher.ui.components.filemanager.FileSelectorResult
import com.lanrhyme.shardlauncher.ui.version.dialog.VersionOperationState
import dev.chrisbanes.haze.hazeEffect

enum class VersionDetailPane(val title: String, val icon: ImageVector) {
    Overview("实例概览", Icons.Default.Dashboard),
    Config("运行配置", Icons.Default.Tune),
    Mods("模组仓库", Icons.Default.Extension),
    Saves("存档管理", Icons.Default.Source),
    ResourcePacks("资源中心", Icons.Default.Palette),
    ShaderPacks("视觉光影", Icons.Default.WbSunny)
}

@Composable
fun VersionScreen(navController: NavController, animationSpeed: Float) {
    val cardLayoutConfig = LocalCardLayoutConfig.current
    val isCardBlurEnabled = cardLayoutConfig.isCardBlurEnabled
    val cardAlpha = cardLayoutConfig.cardAlpha
    val hazeState = cardLayoutConfig.hazeState
    
    LaunchedEffect(Unit) { VersionsManager.refresh("VersionScreen_Init") }

    var showDirectoryPopup by remember { mutableStateOf(false) }

    val versions = VersionsManager.versions
    val currentVersion by VersionsManager.currentVersion.collectAsState()
    val isRefreshing by VersionsManager.isRefreshing.collectAsState()
    
    var selectedVersion by remember { mutableStateOf<Version?>(null) }
    var selectedPane by remember { mutableStateOf<VersionDetailPane?>(null) }

    var versionCategory by remember { mutableStateOf(VersionCategory.ALL) }
    
    val filteredVersions by remember(versionCategory, isRefreshing, versions) {
        derivedStateOf {
            when (versionCategory) {
                VersionCategory.ALL -> versions
                VersionCategory.VANILLA -> versions.filter { it.versionType == VersionType.VANILLA }
                VersionCategory.MODLOADER -> versions.filter { it.versionType == VersionType.MODLOADERS }
            }
        }
    }

    var versionsOperation by remember { mutableStateOf<VersionOperationState>(VersionOperationState.None) }
    var errorMessage by remember { mutableStateOf<String?>(null) }

    fun resetToVersionList() {
        selectedPane = null
    }

    // Main Layout
    Row(
        modifier = Modifier
            .fillMaxSize()
    ) {
        // Sidebar
        Column(
            modifier = Modifier
                .width(260.dp)
                .fillMaxHeight()
                .padding(16.dp)
                .clip(RoundedCornerShape(24.dp))
                .background(MaterialTheme.colorScheme.surfaceVariant.copy(alpha = cardAlpha))
                .then(
                    if (isCardBlurEnabled && Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
                        Modifier.hazeEffect(state = hazeState)
                    } else Modifier
                )
        ) {
            LeftNavigationPane(
                selectedVersion = selectedVersion,
                selectedPane = selectedPane,
                onPaneSelected = { pane -> selectedPane = pane }
            )
        }

        // Content
        Box(
            modifier = Modifier
                .weight(1f)
                .fillMaxHeight()
                .padding(16.dp)
        ) {
            AnimatedContent(
                targetState = selectedPane,
                transitionSpec = {
                    fadeIn(animationSpec = spring(stiffness = Spring.StiffnessLow)) togetherWith
                    fadeOut(animationSpec = spring(stiffness = Spring.StiffnessLow))
                },
                label = "MainContentSwitch"
            ) { pane ->
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
                    RightDetailContent(
                        pane = pane, 
                        version = selectedVersion, 
                        onBack = { resetToVersionList() },
                        isCardBlurEnabled = isCardBlurEnabled,
                        hazeState = hazeState,
                        cardAlpha = cardAlpha
                    )
                }
            }
        }
    }

    if (showDirectoryPopup) {
        DirectorySelectionPopup(onDismissRequest = { showDirectoryPopup = false })
    }

    VersionsOperation(
        versionsOperation = versionsOperation,
        updateVersionsOperation = { versionsOperation = it },
        onError = { errorMessage = it }
    )

    errorMessage?.let { message ->
        ShardAlertDialog(
            visible = true,
            title = "提示",
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
            modifier = Modifier.fillMaxSize().padding(12.dp),
            horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Text(
            selectedVersion?.getVersionName() ?: "",
            style = MaterialTheme.typography.titleLarge.copy(
                fontWeight = FontWeight.ExtraBold,
                letterSpacing = 1.sp
            ),
            color = MaterialTheme.colorScheme.primary,
            modifier = Modifier.padding(vertical = 16.dp)
        )

        Spacer(modifier = Modifier.height(16.dp))

        AnimatedVisibility(
            visible = selectedVersion != null,
            enter = fadeIn() + expandVertically()
        ) {
            Column(
                horizontalAlignment = Alignment.CenterHorizontally,
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(bottom = 24.dp)
            ) {
                    AsyncImage(
                        model = selectedVersion?.let { VersionsManager.getVersionIconFile(it) },
                        contentDescription = null,
                        placeholder = painterResource(id = R.drawable.img_minecraft),
                        error = painterResource(id = R.drawable.img_minecraft),
                        modifier = Modifier
                            .size(72.dp)
                    )
                
                Spacer(modifier = Modifier.height(12.dp))
            }
        }

        if (selectedVersion == null) {
            Box(
                modifier = Modifier.weight(1f).fillMaxWidth(),
                contentAlignment = Alignment.Center
            ) {
                Text(
                    "请选择一个游戏实例\n以开始管理",
                    textAlign = TextAlign.Center,
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.6f)
                )
            }
        } else {
            LazyColumn(
                verticalArrangement = Arrangement.spacedBy(8.dp),
                modifier = Modifier.weight(1f)
            ) {
                items(VersionDetailPane.entries) { pane ->
                    val isSelected = selectedPane == pane
                    Surface(
                        onClick = { onPaneSelected(pane) },
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(16.dp),
                        color = if (isSelected) MaterialTheme.colorScheme.primary else Color.Transparent,
                        contentColor = if (isSelected) MaterialTheme.colorScheme.onPrimary else MaterialTheme.colorScheme.onSurface
                    ) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            modifier = Modifier.padding(horizontal = 16.dp, vertical = 12.dp)
                        ) {
                            Icon(imageVector = pane.icon, contentDescription = null, modifier = Modifier.size(20.dp))
                            Spacer(Modifier.width(12.dp))
                            Text(
                                text = pane.title,
                                style = MaterialTheme.typography.labelLarge,
                                fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal
                            )
                        }
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
    onVersionOperation: (VersionOperationState) -> Unit,
    onError: (String) -> Unit
) {
    var searchText by remember { mutableStateOf("") }
    val filteredVersions = remember(versions, searchText) {
        if (searchText.isBlank()) versions
        else versions.filter { it.getVersionName().contains(searchText, ignoreCase = true) }
    }

    val allVersionsCount = VersionsManager.versions.size
    val vanillaVersionsCount = VersionsManager.versions.count { it.versionType == VersionType.VANILLA }
    val modloaderVersionsCount = VersionsManager.versions.count { it.versionType == VersionType.MODLOADERS }

    Column(modifier = Modifier.fillMaxSize()) {
        Row(
            modifier = Modifier.fillMaxWidth().padding(bottom = 16.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            SearchTextField(
                value = searchText,
                onValueChange = { searchText = it },
                hint = "查找游戏实例...",
                modifier = Modifier.width(320.dp)
            )
            
            Spacer(modifier = Modifier.weight(1f))
            
            IconButton(
                onClick = { VersionsManager.refresh("Manual") },
                colors = IconButtonDefaults.iconButtonColors(
                )
            ) {
                Icon(Icons.Default.Refresh, contentDescription = null, modifier = Modifier.size(20.dp))
            }
            
            IconButton(
                onClick = onShowDirectoryPopup,
                 colors = IconButtonDefaults.iconButtonColors(
                )
            ) {
                Icon(Icons.Default.FolderOpen, contentDescription = null, modifier = Modifier.size(20.dp))
            }
        }

        Row(
            modifier = Modifier.padding(bottom = 16.dp),
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            StyledFilterChip(
                selected = versionCategory == VersionCategory.ALL,
                onClick = { onCategoryChange(VersionCategory.ALL) },
                label = { Text("${stringResource(VersionCategory.ALL.textRes)} (${allVersionsCount})") }
            )
            StyledFilterChip(
                selected = versionCategory == VersionCategory.VANILLA,
                onClick = { onCategoryChange(VersionCategory.VANILLA) },
                label = { Text("${stringResource(VersionCategory.VANILLA.textRes)} (${vanillaVersionsCount})") }
            )
            StyledFilterChip(
                selected = versionCategory == VersionCategory.MODLOADER,
                onClick = { onCategoryChange(VersionCategory.MODLOADER) },
                label = { Text("${stringResource(VersionCategory.MODLOADER.textRes)} (${modloaderVersionsCount})") }
            )
        }

        if (isRefreshing) {
            Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                CircularProgressIndicator(strokeWidth = 3.dp)
            }
        } else if (filteredVersions.isEmpty()) {
            Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Icon(Icons.Outlined.Search, contentDescription = null, modifier = Modifier.size(64.dp), tint = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.2f))
                    Spacer(modifier = Modifier.height(16.dp))
                    Text("没有找到匹配的游戏实例", color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.5f))
                }
            }
        } else {
            LazyVerticalGrid(
                columns = GridCells.Adaptive(minSize = 160.dp),
                contentPadding = PaddingValues(vertical = 8.dp),
                horizontalArrangement = Arrangement.spacedBy(16.dp),
                verticalArrangement = Arrangement.spacedBy(16.dp),
                modifier = Modifier.fillMaxSize()
            ) {
                itemsIndexed(filteredVersions) { index, version ->
                    AdvancedVersionCard(
                        version = version,
                        isSelected = version == selectedVersion,
                        isCurrent = version == currentVersion,
                        onClick = { onVersionClick(version) },
                        onPinClick = { version.setPinnedAndSave(!version.pinnedState) },
                        onRenameClick = { onVersionOperation(VersionOperationState.Rename(version)) },
                        onCopyClick = { onVersionOperation(VersionOperationState.Copy(version)) },
                        onDeleteClick = { onVersionOperation(VersionOperationState.Delete(version)) },
                        index = index,
                        animationSpeed = animationSpeed
                    )
                }
            }
        }
    }
}

@Composable
fun RightDetailContent(
    pane: VersionDetailPane, 
    version: Version?, 
    onBack: () -> Unit,
    isCardBlurEnabled: Boolean,
    hazeState: dev.chrisbanes.haze.HazeState,
    cardAlpha: Float
) {
    if (version == null) return
    Column(modifier = Modifier.fillMaxSize()) {
        Row(modifier = Modifier.fillMaxWidth().padding(bottom = 16.dp), verticalAlignment = Alignment.CenterVertically) {
            IconButton(onClick = onBack, colors = IconButtonDefaults.iconButtonColors(containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f))) {
                Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = null)
            }
            Spacer(Modifier.width(16.dp))
            Column {
                Text(text = pane.title, style = MaterialTheme.typography.titleLarge, fontWeight = FontWeight.ExtraBold)
                Text(text = version.getVersionName(), style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
            }
        }
        Surface(
            modifier = Modifier.weight(1f).fillMaxWidth().clip(RoundedCornerShape(24.dp))
                .then(if (isCardBlurEnabled && Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) Modifier.hazeEffect(state = hazeState) else Modifier),
            color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = cardAlpha),
            shape = RoundedCornerShape(24.dp)
        ) {
            Box(modifier = Modifier.fillMaxSize()) {
                when (pane) {
                    VersionDetailPane.Overview -> VersionOverviewScreen(version = version, onBack = onBack, onError = {})
                    VersionDetailPane.Config -> VersionConfigScreen(version = version, config = version.getVersionConfig(), onConfigChange = {}, onSave = { version.getVersionConfig().save() }, onError = {})
                    VersionDetailPane.Mods -> ModsManagementScreen(version = version, onBack = onBack)
                    VersionDetailPane.Saves -> SavesManagementScreen(version = version, onBack = onBack)
                    VersionDetailPane.ResourcePacks -> ResourcePacksManagementScreen(version = version, onBack = onBack)
                    VersionDetailPane.ShaderPacks -> ShaderPacksManagementScreen(version = version, onBack = onBack)
                }
            }
        }
    }
}

@Composable
fun AdvancedVersionCard(
    version: Version,
    isSelected: Boolean,
    isCurrent: Boolean = false,
    onClick: () -> Unit,
    onPinClick: () -> Unit,
    onRenameClick: () -> Unit,
    onCopyClick: () -> Unit,
    onDeleteClick: () -> Unit,
    index: Int,
    animationSpeed: Float
) {
    val (isCardBlurEnabled, cardAlpha, hazeState) = LocalCardLayoutConfig.current
    val interactionSource = remember { MutableInteractionSource() }
    val isPressed by interactionSource.collectIsPressedAsState()
    var showMenu by remember { mutableStateOf(false) }
    val shape = RoundedCornerShape(20.dp)

    Card(
        modifier = Modifier.animatedAppearance(index, animationSpeed).height(180.dp)
            .selectableCard(isSelected = isSelected, isPressed = isPressed)
            .then(if (isCardBlurEnabled && Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) Modifier.clip(shape).hazeEffect(state = hazeState) else Modifier.clip(shape))
            .clickable(interactionSource = interactionSource, indication = null, onClick = onClick),
        shape = shape,
        border = when {
            isSelected -> BorderStroke(2.dp, MaterialTheme.colorScheme.secondary.copy(alpha = 0.5f))
            else -> BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.3f))
        },
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface.copy(alpha = cardAlpha))
    ) {
        Box(modifier = Modifier.fillMaxSize()) {
            Column(modifier = Modifier.fillMaxSize().padding(12.dp), horizontalAlignment = Alignment.CenterHorizontally, verticalArrangement = Arrangement.Center) {
                AsyncImage(model = VersionsManager.getVersionIconFile(version), contentDescription = null, placeholder = painterResource(id = R.drawable.img_minecraft), error = painterResource(id = R.drawable.img_minecraft), contentScale = ContentScale.Fit, modifier = Modifier.size(64.dp).clip(RoundedCornerShape(12.dp)))
                Spacer(modifier = Modifier.height(12.dp))
                Text(text = version.getVersionName(), style = MaterialTheme.typography.titleSmall, fontWeight = FontWeight.Bold, maxLines = 2, overflow = TextOverflow.Ellipsis, textAlign = TextAlign.Center)
                version.getVersionInfo()?.minecraftVersion?.let { Text(text = it, style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.6f)) }
            }
            Row(modifier = Modifier.align(Alignment.TopEnd).padding(8.dp), horizontalArrangement = Arrangement.spacedBy(4.dp)) {
                if (version.pinnedState) Icon(Icons.Default.PushPin, contentDescription = null, tint = MaterialTheme.colorScheme.primary, modifier = Modifier.size(14.dp).rotate(45f))
                if (isCurrent) Surface(color = MaterialTheme.colorScheme.primary, shape = CircleShape) { Box(modifier = Modifier.size(6.dp)) }
            }
            Box(modifier = Modifier.align(Alignment.BottomEnd)) {
                IconButton(onClick = { showMenu = true }, modifier = Modifier.size(32.dp)) { Icon(Icons.Default.MoreVert, contentDescription = null, modifier = Modifier.size(16.dp)) }
                ShardDropdownMenu(expanded = showMenu, onDismissRequest = { showMenu = false }) {
                    DropdownMenuItem(text = { Text(if (version.pinnedState) "取消置顶" else "置顶") }, leadingIcon = { Icon(Icons.Default.PushPin, null, modifier = Modifier.size(18.dp)) }, onClick = { onPinClick(); showMenu = false })
                    DropdownMenuItem(text = { Text("重命名") }, leadingIcon = { Icon(Icons.Default.Edit, null, modifier = Modifier.size(18.dp)) }, onClick = { onRenameClick(); showMenu = false })
                    DropdownMenuItem(text = { Text("复制") }, leadingIcon = { Icon(Icons.Default.FileCopy, null, modifier = Modifier.size(18.dp)) }, onClick = { onCopyClick(); showMenu = false })
                    DropdownMenuItem(text = { Text("删除", color = MaterialTheme.colorScheme.error) }, leadingIcon = { Icon(Icons.Default.Delete, null, tint = MaterialTheme.colorScheme.error, modifier = Modifier.size(18.dp)) }, onClick = { onDeleteClick(); showMenu = false })
                }
            }
        }
    }
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

    PopupContainer(visible = true, onDismissRequest = onDismissRequest, modifier = Modifier.width(360.dp).padding(16.dp)) {
        Column(modifier = Modifier.padding(20.dp)) {
            Text("游戏目录管理", style = MaterialTheme.typography.titleLarge, fontWeight = FontWeight.ExtraBold)
            Spacer(modifier = Modifier.height(16.dp))
            LazyColumn(modifier = Modifier.heightIn(max = 400.dp).fillMaxWidth(), verticalArrangement = Arrangement.spacedBy(10.dp)) {
                items(gamePaths) { path ->
                    val isSelected = path.id == currentPathId
                    Surface(
                        onClick = {
                            if (!GamePathManager.hasStoragePermission(context)) { permissionError = "未授予存储权限"; showPermissionErrorDialog = true; return@Surface }
                            try { GamePathManager.selectPath(context, path.id); onDismissRequest() } catch (e: Exception) { permissionError = e.message; showPermissionErrorDialog = true }
                        },
                        shape = RoundedCornerShape(16.dp),
                        color = if (isSelected) MaterialTheme.colorScheme.primaryContainer else MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.3f),
                        border = if (isSelected) BorderStroke(1.5.dp, MaterialTheme.colorScheme.primary) else null
                    ) {
                        Row(modifier = Modifier.padding(16.dp), verticalAlignment = Alignment.CenterVertically) {
                            Column(modifier = Modifier.weight(1f)) {
                                Text(path.title, style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)
                                Text(path.path, style = MaterialTheme.typography.bodySmall, maxLines = 1, overflow = TextOverflow.Ellipsis, color = MaterialTheme.colorScheme.onSurfaceVariant)
                            }
                            if (path.id != GamePathManager.DEFAULT_ID) IconButton(onClick = { GamePathManager.removePath(path.id) }) { Icon(Icons.Default.DeleteOutline, null, tint = MaterialTheme.colorScheme.error, modifier = Modifier.size(20.dp)) }
                        }
                    }
                }
                item { OutlinedButton(onClick = { showFileSelector = true }, modifier = Modifier.fillMaxWidth().height(48.dp), shape = RoundedCornerShape(12.dp)) { Icon(Icons.Default.Add, null, modifier = Modifier.size(18.dp)); Spacer(modifier = Modifier.width(8.dp)); Text("添加目录") } }
            }
        }
    }
    if (showFileSelector) {
        FileSelectorScreen(visible = showFileSelector, config = FileSelectorConfig(initialPath = android.os.Environment.getExternalStorageDirectory(), mode = FileSelectorMode.DIRECTORY_ONLY, showHiddenFiles = true, allowCreateDirectory = true), onDismissRequest = { showFileSelector = false }, onSelection = { result ->
            if (result is FileSelectorResult.Selected) { if (result.path.absolutePath.contains("/FCL/.minecraft")) { GamePathManager.addNewPath("FCL公有目录", result.path.absolutePath); showFileSelector = false } else { pendingGamePath = result.path.absolutePath; showGamePathNameDialog = true } }
        })
    }
    if (showGamePathNameDialog) {
        GamePathNameDialog(onDismissRequest = { showGamePathNameDialog = false }, onConfirm = { name -> GamePathManager.addNewPath(name, pendingGamePath ?: ""); showGamePathNameDialog = false; showFileSelector = false })
    }
    if (showPermissionErrorDialog) {
        ShardAlertDialog(visible = true, title = "权限错误", text = permissionError ?: "未知错误", onDismiss = { showPermissionErrorDialog = false })
    }
}

@Composable
fun GamePathNameDialog(onDismissRequest: () -> Unit, onConfirm: (String) -> Unit) {
    var name by remember { mutableStateOf("自定义目录") }
    ShardEditDialog(title = "命名游戏目录", value = name, onValueChange = { name = it }, label = "目录名称", singleLine = true, onDismissRequest = onDismissRequest, onConfirm = { if (name.isNotEmpty()) onConfirm(name) })
}
