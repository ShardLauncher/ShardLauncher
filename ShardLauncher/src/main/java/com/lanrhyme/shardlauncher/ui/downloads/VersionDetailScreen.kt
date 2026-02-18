package com.lanrhyme.shardlauncher.ui.downloads

import android.app.Application
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.animateColorAsState
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
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
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.SolidColor
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
import com.lanrhyme.shardlauncher.ui.components.basic.CapsuleTextField
import com.lanrhyme.shardlauncher.ui.components.basic.CombinedCard
import com.lanrhyme.shardlauncher.ui.components.business.LoaderVersionDropdown
import com.lanrhyme.shardlauncher.ui.components.layout.LocalCardLayoutConfig
import com.lanrhyme.shardlauncher.ui.components.basic.ScalingActionButton
import com.lanrhyme.shardlauncher.ui.components.basic.ShardGlassCard
import com.lanrhyme.shardlauncher.ui.components.basic.StyledFilterChip
import com.lanrhyme.shardlauncher.ui.components.basic.SubPageNavigationBar
import com.lanrhyme.shardlauncher.ui.components.basic.animatedAppearance

@OptIn(ExperimentalLayoutApi::class)
@Composable
fun VersionDetailScreen(navController: NavController, versionId: String?) {
    val cardLayoutConfig = LocalCardLayoutConfig.current
    val isCardBlurEnabled = cardLayoutConfig.isCardBlurEnabled
    val cardAlpha = cardLayoutConfig.cardAlpha
    val hazeState = cardLayoutConfig.hazeState
    
    val animatedSpeed = 1.0f

    if (versionId == null) {
        Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
            Text("Error: Version ID is missing.")
        }
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

    Row(
        modifier = Modifier
            .fillMaxSize()
            .padding(20.dp),
        horizontalArrangement = Arrangement.spacedBy(20.dp)
    ) {
        // Left Pane: Hero, Input, Action
        Column(
            modifier = Modifier
                .weight(0.4f)
                .fillMaxHeight(),
            verticalArrangement = Arrangement.spacedBy(20.dp)
        ) {
            SubPageNavigationBar(
                title = "安装游戏",
                description = "配置并安装 Minecraft",
                onBack = { navController.popBackStack() },
                modifier = Modifier.animatedAppearance(0, animatedSpeed)
            )

            // Hero Section
            ShardGlassCard(
                modifier = Modifier.animatedAppearance(1, animatedSpeed)
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(16.dp)
                ) {
                    Surface(
                        shape = RoundedCornerShape(16.dp),
                        color = MaterialTheme.colorScheme.primary.copy(alpha = 0.1f),
                        modifier = Modifier.size(72.dp)
                    ) {
                        Box(contentAlignment = Alignment.Center) {
                            Image(
                                painter = painterResource(id = R.drawable.img_minecraft),
                                contentDescription = "Minecraft Logo",
                                modifier = Modifier.size(48.dp)
                            )
                        }
                    }
                    Column(modifier = Modifier.weight(1f)) {
                        Text(
                            text = "Minecraft",
                            style = MaterialTheme.typography.labelMedium,
                            color = MaterialTheme.colorScheme.primary,
                            fontWeight = FontWeight.Bold
                        )
                        Text(
                            text = versionId,
                            style = MaterialTheme.typography.headlineSmall,
                            fontWeight = FontWeight.Black
                        )
                    }
                }
            }

            CapsuleTextField(
                value = versionName,
                onValueChange = { viewModel.setVersionName(it) },
                label = "版本名称",
                modifier = Modifier.fillMaxWidth()
            )

            Spacer(modifier = Modifier.weight(1f))

            // Download Button Area
            Box(
                modifier = Modifier
                    .animatedAppearance(3, animatedSpeed)
                    .fillMaxWidth()
            ) {
                ScalingActionButton(
                    onClick = { viewModel.download() },
                    icon = androidx.compose.material.icons.Icons.Default.Download,
                    text = if (downloadTask?.taskState == TaskState.RUNNING) "正在准备下载..." else "开始下载游戏",
                    enabled = downloadTask == null || downloadTask?.taskState == TaskState.COMPLETED,
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(56.dp)
                )
            }
        }

        // 右窗格：配置（可滚动）
        Column(
            modifier = Modifier
                .weight(0.6f)
                .fillMaxHeight()
                .verticalScroll(rememberScrollState()),
            verticalArrangement = Arrangement.spacedBy(20.dp)
        ) {
            Spacer(Modifier.height(44.dp))
            // Mod Loader Section
            CombinedCard(
                title = "模组加载器", 
                summary = "选择您喜欢的模组加载框架",
                modifier = Modifier.animatedAppearance(4, animatedSpeed)
            ) {
                Column(verticalArrangement = Arrangement.spacedBy(16.dp)) {
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
                            iconRes = R.drawable.img_anvil,
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
                    AnimatedVisibility(visible = selectedModLoader != ModLoader.None) {
                        Column(
                            modifier = Modifier
                                .fillMaxWidth()
                                .background(
                                    MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.3f),
                                    RoundedCornerShape(12.dp)
                                )
                                .padding(12.dp),
                            verticalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            Text(
                                text = "选择加载器版本",
                                style = MaterialTheme.typography.labelMedium,
                                color = MaterialTheme.colorScheme.primary,
                                fontWeight = FontWeight.Bold
                            )
                            
                            when (selectedModLoader) {
                                ModLoader.Fabric -> {
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
                                ModLoader.Forge -> {
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
                                ModLoader.NeoForge -> {
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
                                ModLoader.Quilt -> {
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
                                else -> {}
                            }
                        }
                    }
                }
            }

            // Addons Section
            CombinedCard(
                title = "附加组件", 
                summary = "自动下载并安装常用的优化件或 API",
                modifier = Modifier.animatedAppearance(5, animatedSpeed)
            ) {
                Column(verticalArrangement = Arrangement.spacedBy(16.dp)) {
                    // Fabric API
                    AnimatedVisibility(visible = selectedModLoader == ModLoader.Fabric) {
                        Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                            StyledFilterChip(
                                selected = isFabricApiSelected,
                                onClick = { viewModel.toggleFabricApi(!isFabricApiSelected) },
                                label = { Text("Fabric API") }
                            )

                            AnimatedVisibility(visible = isFabricApiSelected) {
                                val fabricApiVersions by viewModel.fabricApiVersions.collectAsState()
                                val selectedApiVersion by viewModel.selectedFabricApiVersion.collectAsState()
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
                            label = { Text("Optifine (高清修复)") }
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

@Composable
private fun LoaderSelectionCard(
    title: String,
    iconRes: Int,
    isSelected: Boolean,
    onClick: () -> Unit
) {
    val borderColor by animateColorAsState(
        if (isSelected) MaterialTheme.colorScheme.primary else Color.Transparent, 
        label = "borderColor"
    )
    val containerColor by animateColorAsState(
        if (isSelected) MaterialTheme.colorScheme.primary.copy(alpha = 0.12f) 
        else MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.3f), 
        label = "containerColor"
    )
    val contentColor by animateColorAsState(
        if (isSelected) MaterialTheme.colorScheme.primary 
        else MaterialTheme.colorScheme.onSurfaceVariant, 
        label = "contentColor"
    )

    Surface(
        onClick = onClick,
        modifier = Modifier
            .width(100.dp)
            .height(110.dp)
            .border(
                width = if (isSelected) 2.dp else 1.dp,
                brush = if (isSelected)
                    Brush.verticalGradient(
                        listOf(
                            MaterialTheme.colorScheme.primary,
                            MaterialTheme.colorScheme.tertiary
                        )
                    )
                else SolidColor(MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.5f)),
                shape = RoundedCornerShape(20.dp)
            ),
        shape = RoundedCornerShape(20.dp),
        color = containerColor
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(8.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {
            Box(
                modifier = Modifier
                    .size(52.dp)
                    .clip(RoundedCornerShape(12.dp))
                    .background(if (isSelected) Color.White.copy(alpha = 0.1f) else Color.Transparent),
                contentAlignment = Alignment.Center
            ) {
                Image(
                    painter = painterResource(id = iconRes),
                    contentDescription = title,
                    modifier = Modifier.size(40.dp)
                )
            }
            Spacer(modifier = Modifier.height(10.dp))
            Text(
                text = title,
                style = MaterialTheme.typography.labelLarge,
                fontWeight = if (isSelected) FontWeight.ExtraBold else FontWeight.Bold,
                color = contentColor
            )
        }
    }
}
