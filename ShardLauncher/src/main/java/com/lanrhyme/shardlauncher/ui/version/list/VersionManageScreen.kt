package com.lanrhyme.shardlauncher.ui.version.list

import android.os.Build
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Download
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue

import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import androidx.lifecycle.ViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewModelScope
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavController
import com.lanrhyme.shardlauncher.game.path.GamePathManager
import com.lanrhyme.shardlauncher.game.version.installed.Version
import com.lanrhyme.shardlauncher.game.version.installed.VersionComparator
import com.lanrhyme.shardlauncher.game.version.installed.VersionType
import com.lanrhyme.shardlauncher.game.version.installed.VersionsManager
import com.lanrhyme.shardlauncher.ui.components.basic.ShardAlertDialog
import com.lanrhyme.shardlauncher.ui.components.layout.LocalCardLayoutConfig
import com.lanrhyme.shardlauncher.ui.version.dialog.VersionsOperation
import dev.chrisbanes.haze.hazeEffect
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.sync.withLock
import kotlinx.coroutines.withContext

private class VersionsScreenViewModel() : ViewModel() {
    /** 版本类别分类 */
    var versionCategory by mutableStateOf(VersionCategory.ALL)
        private set

    private val _versions = MutableStateFlow<List<Version>>(emptyList())
    val versions = _versions.asStateFlow()

    /** 全部版本的数量 */
    var allVersionsCount by mutableIntStateOf(0)
        private set
    /** 原版版本数量 */
    var vanillaVersionsCount by mutableIntStateOf(0)
        private set
    /** 模组加载器版本数量 */
    var modloaderVersionsCount by mutableIntStateOf(0)
        private set

    fun startRefreshVersions() {
        if (!VersionsManager.isRefreshing.value) {
            _versions.update { emptyList() }
            VersionsManager.refresh("VersionsScreenViewModel.startRefreshVersions")
        }
    }

    /**
     * 刷新当前版本列表
     */
    suspend fun refreshVersions(
        currentVersions: List<Version>,
        clearCurrent: Boolean = true
    ) {
        withContext(Dispatchers.Main) {
            if (clearCurrent) {
                _versions.update { emptyList() }
            }

            val filteredVersions = withContext(Dispatchers.Default) {
                allVersionsCount = currentVersions.size

                val vanillaVersions = currentVersions
                    .filter { ver -> ver.versionType == VersionType.VANILLA }
                    .also { vanillaVersionsCount = it.size }
                val modloaderVersions = currentVersions
                    .filter { ver -> ver.versionType == VersionType.MODLOADERS }
                    .also { modloaderVersionsCount = it.size }

                when (versionCategory) {
                    VersionCategory.ALL -> currentVersions
                    VersionCategory.VANILLA -> vanillaVersions
                    VersionCategory.MODLOADER -> modloaderVersions
                }
            }

            _versions.update {
                filteredVersions.sortedWith(VersionComparator)
            }
        }
    }

    private var currentJob: Job? = null
    private var mutex: Mutex = Mutex()

    /**
     * 变更当前版本列表的过滤类型
     */
    fun changeCategory(category: VersionCategory) {
        currentJob?.cancel()
        currentJob = viewModelScope.launch {
            mutex.withLock {
                this@VersionsScreenViewModel.versionCategory = category
                refreshVersions(VersionsManager.versions, false)
            }
        }
    }

    /**
     * 重新排序当前版本列表
     */
    fun resortVersions() {
        _versions.update {
            it.sortedWith(VersionComparator)
        }
    }

    private val listener: suspend (List<Version>) -> Unit = { versions ->
        refreshVersions(versions)
    }

    init {
        viewModelScope.launch {
            //初始化时刷新一次版本
            refreshVersions(VersionsManager.versions)
        }

        VersionsManager.registerListener(listener)
    }

    override fun onCleared() {
        VersionsManager.unregisterListener(listener)
        currentJob?.cancel()
    }
}

@Composable
private fun rememberVersionViewModel() : VersionsScreenViewModel {
    return viewModel {
        VersionsScreenViewModel()
    }
}

@Composable
fun VersionManageScreen(
    navController: NavController,
    animationSpeed: Float
) {
    val viewModel = rememberVersionViewModel()
    val context = LocalContext.current

    val versions by viewModel.versions.collectAsStateWithLifecycle()
    val currentVersion by VersionsManager.currentVersion.collectAsStateWithLifecycle()
    val isRefreshing by VersionsManager.isRefreshing.collectAsStateWithLifecycle()

    var errorMessage by remember { mutableStateOf<String?>(null) }

    Row(modifier = Modifier.fillMaxSize()) {
        LeftMenu(
            isRefreshing = isRefreshing,
            modifier = Modifier
                .fillMaxHeight()
                .weight(2.5f)
        )

        VersionsLayout(
            isRefreshing = isRefreshing,
            versions = versions,
            currentVersion = currentVersion,
            versionCategory = viewModel.versionCategory,
            onCategoryChange = { viewModel.changeCategory(it) },
            allVersionsCount = viewModel.allVersionsCount,
            vanillaVersionsCount = viewModel.vanillaVersionsCount,
            modloaderVersionsCount = viewModel.modloaderVersionsCount,
            navigateToVersions = { version ->
                // TODO: Navigate to version details
            },
            modifier = Modifier
                .fillMaxHeight()
                .weight(7.5f)
                .padding(vertical = 12.dp)
                .padding(end = 12.dp),
            submitError = { errorMessage = it },
            onRefresh = {
                viewModel.startRefreshVersions()
            },
            onVersionPinned = {
                viewModel.resortVersions()
            },
            onInstall = {
                // TODO: Navigate to download screen
            }
        )
    }

    // Error dialog
    errorMessage?.let { message ->
        ShardAlertDialog(
            title = "错误", // TODO: i18n
            text = { Text(message) },
            onDismiss = { errorMessage = null }
        )
    }
}

@Composable
private fun LeftMenu(
    isRefreshing: Boolean,
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current
    val cardLayoutConfig = LocalCardLayoutConfig.current
    val isCardBlurEnabled = cardLayoutConfig.isCardBlurEnabled
    val cardAlpha = cardLayoutConfig.cardAlpha
    val hazeState = cardLayoutConfig.hazeState

    val leftShape = RoundedCornerShape(16.dp)
    Card(
        modifier = modifier
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
        Column(
            modifier = Modifier.padding(all = 12.dp)
        ) {
            val gamePaths by GamePathManager.gamePathData.collectAsState()
            val currentPath = GamePathManager.currentPath

            LazyColumn(
                modifier = Modifier
                    .fillMaxWidth()
                    .weight(1f)
            ) {
                items(gamePaths, key = { it.id }) { pathItem ->
                    GamePathItemLayout(
                        item = pathItem,
                        selected = currentPath == pathItem.path,
                        onClick = {
                            if (!isRefreshing) {
                                if (pathItem.id == GamePathManager.DEFAULT_ID) {
                                    GamePathManager.selectPath(context, GamePathManager.DEFAULT_ID)
                                } else {
                                    GamePathManager.selectPath(context, pathItem.id)
                                }
                            }
                        },
                        onDelete = {
                            // TODO: Implement delete path
                        },
                        onRename = {
                            // TODO: Implement rename path
                        }
                    )
                }
            }

            OutlinedButton(
                modifier = Modifier
                    .padding(top = 8.dp)
                    .fillMaxWidth(),
                onClick = {
                    // TODO: Add new path
                }
            ) {
                Text(text = "添加新路径") // TODO: i18n
            }

            OutlinedButton(
                modifier = Modifier
                    .padding(vertical = 8.dp)
                    .fillMaxWidth(),
                onClick = {
                    // TODO: Cleanup game files
                }
            ) {
                Text(text = "清理游戏文件") // TODO: i18n
            }
        }
    }
}

@Composable
private fun VersionsLayout(
    modifier: Modifier = Modifier,
    isRefreshing: Boolean,
    versions: List<Version>,
    currentVersion: Version?,
    versionCategory: VersionCategory,
    onCategoryChange: (VersionCategory) -> Unit,
    allVersionsCount: Int,
    vanillaVersionsCount: Int,
    modloaderVersionsCount: Int,
    navigateToVersions: (Version) -> Unit,
    submitError: (String) -> Unit,
    onRefresh: () -> Unit,
    onVersionPinned: () -> Unit,
    onInstall: () -> Unit
) {
    val cardLayoutConfig = LocalCardLayoutConfig.current
    val isCardBlurEnabled = cardLayoutConfig.isCardBlurEnabled
    val cardAlpha = cardLayoutConfig.cardAlpha
    val hazeState = cardLayoutConfig.hazeState

    val rightShape = RoundedCornerShape(16.dp)
    Card(
        modifier = modifier
            .then(
                if (isCardBlurEnabled && Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
                    Modifier.clip(rightShape).hazeEffect(state = hazeState)
                } else Modifier
            ),
        shape = rightShape,
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surface.copy(alpha = cardAlpha)
        )
    ) {
        if (isRefreshing) {
            Box(
                modifier = Modifier.fillMaxSize(),
                contentAlignment = Alignment.Center
            ) {
                CircularProgressIndicator()
            }
        } else {
            var versionsOperation by remember { mutableStateOf<VersionsOperation>(VersionsOperation.None) }
            VersionsOperation(
                versionsOperation = versionsOperation,
                updateVersionsOperation = { versionsOperation = it },
                onError = { message -> submitError(message) }
            )

            Column(modifier = Modifier.fillMaxSize()) {
                // Header with category tabs and action buttons
                Column(modifier = Modifier.padding(16.dp)) {
                    val scrollState = rememberScrollState()
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .horizontalScroll(state = scrollState)
                            .padding(vertical = 8.dp),
                        horizontalArrangement = Arrangement.spacedBy(8.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        IconButton(onClick = onRefresh) {
                            Icon(Icons.Filled.Refresh, contentDescription = "刷新") // TODO: i18n
                        }
                        IconButton(onClick = onInstall) {
                            Icon(Icons.Filled.Download, contentDescription = "安装新版本") // TODO: i18n
                        }
                        
                        // Version category tabs
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
                }

                if (versions.isNotEmpty()) {
                    LazyColumn(
                        modifier = Modifier
                            .fillMaxWidth()
                            .weight(1f),
                        contentPadding = PaddingValues(horizontal = 12.dp, vertical = 6.dp)
                    ) {
                        items(versions, key = { it.toString() }) { version ->
                            VersionItemLayout(
                                version = version,
                                selected = version == currentVersion,
                                submitError = submitError,
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(vertical = 6.dp),
                                onSelected = {
                                    if (version.isValid() && version != currentVersion) {
                                        VersionsManager.saveCurrentVersion(version.getVersionName())
                                    } else {
                                        versionsOperation = VersionsOperation.InvalidDelete(version)
                                    }
                                },
                                onSettingsClick = {
                                    navigateToVersions(version)
                                },
                                onRenameClick = { versionsOperation = VersionsOperation.Rename(version) },
                                onCopyClick = { versionsOperation = VersionsOperation.Copy(version) },
                                onDeleteClick = { versionsOperation = VersionsOperation.Delete(version) },
                                onPinned = onVersionPinned
                            )
                        }
                    }
                } else {
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .weight(1f)
                    ) {
                        androidx.compose.material3.Text(
                            modifier = Modifier.align(Alignment.Center),
                            text = "暂无版本" // TODO: i18n
                        )
                    }
                }
            }
        }
    }
}

