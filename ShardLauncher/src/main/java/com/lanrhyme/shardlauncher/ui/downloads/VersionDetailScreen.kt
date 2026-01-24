package com.lanrhyme.shardlauncher.ui.downloads

import android.app.Application
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.animateColorAsState
import androidx.compose.foundation.Image
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.filled.Download
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ElevatedCard
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavController
import com.lanrhyme.shardlauncher.R
import com.lanrhyme.shardlauncher.coroutine.TaskState
import com.lanrhyme.shardlauncher.model.FabricLoaderVersion
import com.lanrhyme.shardlauncher.model.LoaderVersion
import com.lanrhyme.shardlauncher.model.ModrinthVersion
import com.lanrhyme.shardlauncher.ui.components.CapsuleTextField
import com.lanrhyme.shardlauncher.ui.components.CombinedCard
import com.lanrhyme.shardlauncher.ui.components.LoaderVersionDropdown
import com.lanrhyme.shardlauncher.ui.components.LocalCardLayoutConfig
import com.lanrhyme.shardlauncher.ui.components.ScalingActionButton
import com.lanrhyme.shardlauncher.ui.components.StyledFilterChip
import com.lanrhyme.shardlauncher.ui.components.SubPageNavigationBar

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

    Column(modifier = Modifier.fillMaxSize().padding(16.dp)) {
        SubPageNavigationBar(
            title = "安装游戏",
            onBack = { navController.popBackStack() },
            modifier = Modifier.padding(bottom = 16.dp)
        )

        Row(
            modifier = Modifier.weight(1f),
            horizontalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            // Left Pane: Hero, Input, Action
            Column(
                modifier = Modifier
                    .weight(0.4f)
                    .fillMaxHeight(),
                verticalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                // Hero Section
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(16.dp)
                ) {
                    Image(
                        painter = painterResource(id = R.drawable.img_minecraft),
                        contentDescription = "Minecraft Logo",
                        modifier = Modifier.size(64.dp)
                    )
                    Column(modifier = Modifier.weight(1f)) {
                        Text(
                            text = "Minecraft",
                            style = MaterialTheme.typography.labelMedium,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                        Text(
                            text = versionId,
                            style = MaterialTheme.typography.headlineMedium,
                            fontWeight = FontWeight.Bold
                        )
                    }
                }

                CapsuleTextField(
                    value = versionName,
                    onValueChange = { viewModel.setVersionName(it) },
                    label = "版本名称",
                    modifier = Modifier.fillMaxWidth()
                )

                Spacer(modifier = Modifier.weight(1f))

                ScalingActionButton(
                    onClick = { viewModel.download() },
                    icon = androidx.compose.material.icons.Icons.Default.Download,
                    text = if (downloadTask?.taskState == TaskState.RUNNING) "下载中..." else "开始下载",
                    enabled = downloadTask == null || downloadTask?.taskState == TaskState.COMPLETED,
                    modifier = Modifier.fillMaxWidth()
                )
            }

            // Right Pane: Configuration (Scrollable)
            Column(
                modifier = Modifier
                    .weight(0.6f)
                    .fillMaxHeight()
                    .verticalScroll(rememberScrollState()),
                verticalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                // Mod Loader Section
                CombinedCard(title = "模组加载器", summary = "选择一个模组加载器") {
                    Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .horizontalScroll(rememberScrollState()),
                            horizontalArrangement = Arrangement.spacedBy(12.dp)
                        ) {
                            LoaderSelectionCard(
                                title = "Fabric",
                                iconRes = R.drawable.img_loader_fabric,
                                isSelected = selectedModLoader == ModLoader.Fabric,
                                onClick = { viewModel.selectModLoader(ModLoader.Fabric) }
                            )
                            LoaderSelectionCard(
                                title = "Forge",
                                iconRes = R.drawable.img_anvil, // Using Anvil as fallback/representative
                                isSelected = selectedModLoader == ModLoader.Forge,
                                onClick = { viewModel.selectModLoader(ModLoader.Forge) }
                            )
                            LoaderSelectionCard(
                                title = "NeoForge",
                                iconRes = R.drawable.img_loader_neoforge,
                                isSelected = selectedModLoader == ModLoader.NeoForge,
                                onClick = { viewModel.selectModLoader(ModLoader.NeoForge) }
                            )
                            LoaderSelectionCard(
                                title = "Quilt",
                                iconRes = R.drawable.img_loader_quilt,
                                isSelected = selectedModLoader == ModLoader.Quilt,
                                onClick = { viewModel.selectModLoader(ModLoader.Quilt) }
                            )
                        }

                        // Versions Dropdowns
                        AnimatedVisibility(visible = selectedModLoader == ModLoader.Fabric) {
                            Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                                val fabricVersions by viewModel.fabricVersions.collectAsState()
                                val selectedVersion by viewModel.selectedFabricVersion.collectAsState()
                                LoaderVersionDropdown(
                                    versions = fabricVersions,
                                    selectedVersion = selectedVersion,
                                    onVersionSelected = {
                                        viewModel.selectFabricVersion(it as FabricLoaderVersion)
                                    }
                                )
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

                // Addons Section
                CombinedCard(title = "附加组件", summary = "安装常用的附加组件") {
                    Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                        // Fabric API
                        AnimatedVisibility(visible = selectedModLoader == ModLoader.Fabric) {
                            Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                                StyledFilterChip(
                                    selected = isFabricApiSelected,
                                    onClick = { viewModel.toggleFabricApi(!isFabricApiSelected) },
                                    label = { Text("Fabric API") }
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
                                            viewModel.selectFabricApiVersion(it as ModrinthVersion)
                                        }
                                    )
                                }
                            }
                        }

                        // Optifine
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
            }
        }
    }
}

@Composable
private fun LoaderSelectionCard(
    title: String,
    iconRes: Int,
    isSelected: Boolean,
    onClick: () -> Unit
) {
    val borderColor by animateColorAsState(
        if (isSelected) MaterialTheme.colorScheme.primary else Color.Transparent, label = "borderColor"
    )
    val containerColor by animateColorAsState(
        if (isSelected) MaterialTheme.colorScheme.primaryContainer else MaterialTheme.colorScheme.surfaceContainerLow, label = "containerColor"
    )

    ElevatedCard(
        modifier = Modifier
            .width(100.dp)
            .height(110.dp)
            .clip(RoundedCornerShape(12.dp))
            .clickable { onClick() }
            .border(2.dp, borderColor, RoundedCornerShape(12.dp)),
        colors = CardDefaults.elevatedCardColors(containerColor = containerColor)
    ) {
        Column(
            modifier = Modifier.fillMaxSize().padding(8.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {
            Image(
                painter = painterResource(id = iconRes),
                contentDescription = title,
                modifier = Modifier.size(48.dp)
            )
            Spacer(modifier = Modifier.height(8.dp))
            Text(
                text = title,
                style = MaterialTheme.typography.labelLarge,
                fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal
            )
        }
    }
}

