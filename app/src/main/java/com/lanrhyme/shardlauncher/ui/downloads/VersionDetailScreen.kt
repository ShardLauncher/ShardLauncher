package com.lanrhyme.shardlauncher.ui.downloads

import android.app.Application
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.filled.Download
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavController
import com.lanrhyme.shardlauncher.coroutine.TaskState
import com.lanrhyme.shardlauncher.model.FabricLoaderVersion
import com.lanrhyme.shardlauncher.model.LoaderVersion
import com.lanrhyme.shardlauncher.ui.components.CombinedCard
import com.lanrhyme.shardlauncher.ui.components.CustomTextField
import com.lanrhyme.shardlauncher.ui.components.LoaderVersionDropdown
import com.lanrhyme.shardlauncher.ui.components.LocalCardLayoutConfig
import com.lanrhyme.shardlauncher.ui.components.ScalingActionButton
import com.lanrhyme.shardlauncher.ui.components.StyledFilterChip
import com.lanrhyme.shardlauncher.ui.components.SubPageNavigationBar
import com.lanrhyme.shardlauncher.ui.components.TaskFlowDialog

@OptIn(ExperimentalLayoutApi::class)
@Composable
fun VersionDetailScreen(navController: NavController, versionId: String?) {
    val cardLayoutConfig = LocalCardLayoutConfig.current
    val isCardBlurEnabled = cardLayoutConfig.isCardBlurEnabled
    val cardAlpha = cardLayoutConfig.cardAlpha
    val hazeState = cardLayoutConfig.hazeState
    if (versionId == null) {
        Text("Error: Version ID is missing.")
        return
    }
    val application = LocalContext.current.applicationContext as Application
    val viewModel: VersionDetailViewModel = viewModel {
        VersionDetailViewModel(application, versionId)
    }
    val versionName by viewModel.versionName.collectAsState()
    val selectedModLoader by viewModel.selectedModLoader.collectAsState()
    val isOptifineSelected by viewModel.isOptifineSelected.collectAsState()
    val isFabricApiSelected by viewModel.isFabricApiSelected.collectAsState()
    val downloadTask by viewModel.downloadTask.collectAsState()
    val tasksFlow by viewModel.getTasksFlow().collectAsState()

    Column(modifier = Modifier.fillMaxSize().padding(8.dp)) {
        SubPageNavigationBar(
                title = "安装 $versionId",
                onBack = { navController.popBackStack() },
                modifier = Modifier.padding(bottom = 8.dp)
        )

        Column(
                modifier = Modifier.weight(1f).verticalScroll(rememberScrollState()),
                verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            Row(
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                CustomTextField(
                        value = versionName,
                        onValueChange = { viewModel.setVersionName(it) },
                        label = "版本名称",
                        modifier = Modifier.weight(1f)
                )
                ScalingActionButton(
                        onClick = { viewModel.download() },
                        icon = androidx.compose.material.icons.Icons.Default.Download,
                        text = "下载",
                        enabled =
                                downloadTask == null ||
                                        downloadTask?.taskState == TaskState.COMPLETED
                )
            }

            CombinedCard(title = "模组加载器", summary = "选择一个模组加载器 (可选)") {
                Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    FlowRow(
                            horizontalArrangement = Arrangement.spacedBy(8.dp),
                            verticalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        StyledFilterChip(
                                selected = selectedModLoader == ModLoader.Fabric,
                                onClick = { viewModel.selectModLoader(ModLoader.Fabric) },
                                label = { Text("Fabric") }
                        )
                        StyledFilterChip(
                                selected = selectedModLoader == ModLoader.Forge,
                                onClick = { viewModel.selectModLoader(ModLoader.Forge) },
                                label = { Text("Forge") }
                        )
                        StyledFilterChip(
                                selected = selectedModLoader == ModLoader.NeoForge,
                                onClick = { viewModel.selectModLoader(ModLoader.NeoForge) },
                                label = { Text("NeoForge") }
                        )
                        StyledFilterChip(
                                selected = selectedModLoader == ModLoader.Quilt,
                                onClick = { viewModel.selectModLoader(ModLoader.Quilt) },
                                label = { Text("Quilt") }
                        )
                    }

                    AnimatedVisibility(visible = selectedModLoader == ModLoader.Fabric) {
                        Column {
                            val fabricVersions by viewModel.fabricVersions.collectAsState()
                            val selectedVersion by viewModel.selectedFabricVersion.collectAsState()
                            LoaderVersionDropdown(
                                    versions = fabricVersions,
                                    selectedVersion = selectedVersion,
                                    onVersionSelected = {
                                        viewModel.selectFabricVersion(it as FabricLoaderVersion)
                                    }
                            )

                            StyledFilterChip(
                                    selected = isFabricApiSelected,
                                    onClick = { viewModel.toggleFabricApi(!isFabricApiSelected) },
                                    label = { Text("同时安装 Fabric API") }
                            )

                            AnimatedVisibility(visible = isFabricApiSelected) {
                                val fabricApiVersions by
                                        viewModel.fabricApiVersions.collectAsState()
                                val selectedApiVersion by
                                        viewModel.selectedFabricApiVersion.collectAsState()
                                LoaderVersionDropdown(
                                        versions = fabricApiVersions,
                                        selectedVersion = selectedApiVersion,
                                        onVersionSelected = {
                                            viewModel.selectFabricApiVersion(it as String)
                                        }
                                )
                            }
                        }
                    }

                    AnimatedVisibility(visible = selectedModLoader == ModLoader.Forge) {
                        val forgeVersions by viewModel.forgeVersions.collectAsState()
                        val selectedVersion by viewModel.selectedForgeVersion.collectAsState()
                        LoaderVersionDropdown(
                                versions = forgeVersions,
                                selectedVersion = selectedVersion,
                                onVersionSelected = {
                                    viewModel.selectForgeVersion(it as LoaderVersion)
                                }
                        )
                    }
                    AnimatedVisibility(visible = selectedModLoader == ModLoader.NeoForge) {
                        val neoForgeVersions by viewModel.neoForgeVersions.collectAsState()
                        val selectedVersion by viewModel.selectedNeoForgeVersion.collectAsState()
                        LoaderVersionDropdown(
                                versions = neoForgeVersions,
                                selectedVersion = selectedVersion,
                                onVersionSelected = {
                                    viewModel.selectNeoForgeVersion(it as LoaderVersion)
                                }
                        )
                    }
                    AnimatedVisibility(visible = selectedModLoader == ModLoader.Quilt) {
                        val quiltVersions by viewModel.quiltVersions.collectAsState()
                        val selectedVersion by viewModel.selectedQuiltVersion.collectAsState()
                        LoaderVersionDropdown(
                                versions = quiltVersions,
                                selectedVersion = selectedVersion,
                                onVersionSelected = {
                                    viewModel.selectQuiltVersion(it as LoaderVersion)
                                }
                        )
                    }
                }
            }

            CombinedCard(title = "光影加载器", summary = "为你的游戏添加光影 (可选)") {
                Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    StyledFilterChip(
                            selected = isOptifineSelected,
                            onClick = { viewModel.toggleOptifine(!isOptifineSelected) },
                            label = { Text("Optifine") }
                    )

                    AnimatedVisibility(visible = isOptifineSelected) {
                        val optifineVersions by viewModel.optifineVersions.collectAsState()
                        val selectedVersion by viewModel.selectedOptifineVersion.collectAsState()
                        LoaderVersionDropdown(
                                versions = optifineVersions,
                                selectedVersion = selectedVersion,
                                onVersionSelected = {
                                    viewModel.selectOptifineVersion(it as LoaderVersion)
                                }
                        )
                    }
                }
            }
        }
        
        // 下载进度对话框
        TaskFlowDialog(
            title = "正在安装 $versionId",
            tasks = tasksFlow,
            visible = downloadTask != null,
            onDismiss = { /* 不允许点击背景关闭 */ },
            onCancel = { viewModel.cancelInstall() },
            isCompleted = downloadTask?.taskState == com.lanrhyme.shardlauncher.coroutine.TaskState.COMPLETED,
            onComplete = {
                // 完成下载，导航到未安装版本列表界面
                viewModel.completeDownload()
                navController.navigate("download") {
                    // 清除导航栈，确保用户不能返回到下载页面
                    popUpTo("download") { inclusive = true }
                }
            }
        )
    }
}
