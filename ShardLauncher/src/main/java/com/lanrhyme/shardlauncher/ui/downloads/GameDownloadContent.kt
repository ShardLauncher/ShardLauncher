package com.lanrhyme.shardlauncher.ui.downloads

import android.os.Build
import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.Image
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyItemScope
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavController
import com.lanrhyme.shardlauncher.R
import com.lanrhyme.shardlauncher.model.BmclapiManifest
import com.lanrhyme.shardlauncher.ui.components.basic.*
import com.lanrhyme.shardlauncher.ui.components.layout.LocalCardLayoutConfig
import dev.chrisbanes.haze.hazeEffect

@OptIn(ExperimentalMaterial3Api::class, ExperimentalFoundationApi::class)
@Composable
fun GameDownloadContent(navController: NavController) {
    val cardLayoutConfig = LocalCardLayoutConfig.current
    val isCardBlurEnabled = cardLayoutConfig.isCardBlurEnabled
    val cardAlpha = cardLayoutConfig.cardAlpha
    val hazeState = cardLayoutConfig.hazeState
    val viewModel: GameDownloadViewModel = viewModel()

    val versions by viewModel.filteredVersions.collectAsState()
    val selectedVersionTypes by viewModel.selectedVersionTypes.collectAsState()
    val searchQuery by viewModel.searchQuery.collectAsState()
    val isLoading by viewModel.isLoading.collectAsState()

    LaunchedEffect(Unit) { viewModel.loadVersions() }

    val animatedSpeed = 1.0f

    Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
        if (isLoading && versions.isEmpty()) {
            CircularProgressIndicator(strokeWidth = 3.dp)
        } else {
            LazyColumn(
                    modifier = Modifier.fillMaxSize(),
                    verticalArrangement = Arrangement.spacedBy(16.dp),
                    contentPadding = PaddingValues(24.dp)
            ) {
                item {
                    ShardSectionHeader(
                        title = "版本筛选",
                        modifier = Modifier.animatedAppearance(0, animatedSpeed)
                    )
                    Spacer(modifier = Modifier.height(8.dp))
                    ShardGlassCard(
                        modifier = Modifier.animatedAppearance(1, animatedSpeed),
                        shape = RoundedCornerShape(24.dp)
                    ) {
                        Row(
                                modifier = Modifier.fillMaxWidth().height(48.dp).padding(horizontal = 12.dp),
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.spacedBy(12.dp)
                        ) {
                            VersionType.entries.forEach { versionType ->
                                StyledFilterChip(
                                        selected = versionType in selectedVersionTypes,
                                        onClick = { viewModel.toggleVersionType(versionType) },
                                        label = { Text(versionType.title) },
                                )
                            }
                            
                            Spacer(modifier = Modifier.width(4.dp))

                            SearchTextField(
                                    value = searchQuery,
                                    onValueChange = { viewModel.setSearchQuery(it) },
                                    hint = "搜索游戏版本...",
                                    modifier = Modifier.weight(1f).fillMaxHeight()
                            )
                            IconButton(
                                    onClick = {
                                        viewModel.loadVersions(forceRefresh = true)
                                    },
                                    modifier = Modifier.size(36.dp)
                            ) { Icon(Icons.Default.Refresh, contentDescription = "Refresh", modifier = Modifier.size(20.dp)) }
                        }
                    }
                }

                items(versions.take(50)) { version ->
                    VersionItem(
                        version = version,
                        modifier = Modifier.animatedAppearance(2, animatedSpeed)
                    ) {
                        navController.navigate("version_detail/${version.id}")
                    }
                }
            }
        }
    }
}

@Composable
fun VersionItem(
    version: BmclapiManifest.Version,
    modifier: Modifier = Modifier,
    onClick: () -> Unit
) {
    ShardGlassCard(
            modifier = modifier
                    .fillMaxWidth()
                    .clickable { onClick() },
            shape = RoundedCornerShape(20.dp),
    ) {
        Row(
                modifier = Modifier.padding(12.dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            Image(
                    painter = painterResource(id = R.drawable.img_minecraft),
                    contentDescription = "MinecraftIcon",
                    modifier = Modifier.size(40.dp).clip(RoundedCornerShape(10.dp))
            )
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = "Minecraft ${version.id}",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold
                )
                
                Spacer(modifier = Modifier.height(4.dp))
                
                val (versionTypeString, tagColor) = when (version.type) {
                    "release" -> "正式版" to MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.6f)
                    "snapshot" -> "快照版" to MaterialTheme.colorScheme.secondaryContainer.copy(alpha = 0.6f)
                    "old_alpha", "old_beta" -> "远古版" to MaterialTheme.colorScheme.tertiaryContainer.copy(alpha = 0.6f)
                    else -> version.type to MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.6f)
                }

                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    ShardTag(
                        text = versionTypeString,
                        containerColor = tagColor
                    )
                    Text(
                        text = version.releaseTime.substringBefore('T'),
                        style = MaterialTheme.typography.labelMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.7f)
                    )
                }
            }
        }
    }
}

