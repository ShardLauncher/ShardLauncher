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
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
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
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavController
import com.lanrhyme.shardlauncher.R
import com.lanrhyme.shardlauncher.model.BmclapiManifest
import com.lanrhyme.shardlauncher.ui.components.CombinedCard
import com.lanrhyme.shardlauncher.ui.components.LocalCardLayoutConfig
import com.lanrhyme.shardlauncher.ui.components.SearchTextField
import com.lanrhyme.shardlauncher.ui.components.StyledFilterChip
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

    Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
        if (isLoading && versions.isEmpty()) {
            CircularProgressIndicator()
        } else {
            LazyColumn(
                    modifier = Modifier.fillMaxSize().padding(horizontal = 12.dp, vertical = 8.dp),
                    verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                item { ->
                    CombinedCard(title = "版本筛选", summary = null) {
                        Row(
                                modifier = Modifier.fillMaxWidth().height(36.dp),
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            VersionType.entries.forEach { versionType ->
                                StyledFilterChip(
                                        selected = versionType in selectedVersionTypes,
                                        onClick = { viewModel.toggleVersionType(versionType) },
                                        label = { Text(versionType.title) },
                                        modifier = Modifier.fillMaxHeight()
                                )
                            }
                            SearchTextField(
                                    value = searchQuery,
                                    onValueChange = { viewModel.setSearchQuery(it) },
                                    hint = "搜索",
                                    modifier = Modifier.weight(1f).fillMaxHeight()
                            )
                            IconButton(
                                    onClick = {
                                        viewModel.loadVersions(forceRefresh = true)
                                    },
                                    modifier = Modifier.fillMaxHeight()
                            ) { Icon(Icons.Default.Refresh, contentDescription = "Refresh") }
                        }
                    }
                }

                items(versions) { version ->
                    VersionItem(version = version) {
                        navController.navigate("version_detail/${version.id}")
                    }
                }
            }
        }
    }
}

@OptIn(ExperimentalFoundationApi::class)
@Composable
fun LazyItemScope.VersionItem(version: BmclapiManifest.Version, onClick: () -> Unit) {
    val cardLayoutConfig = LocalCardLayoutConfig.current
    val isCardBlurEnabled = cardLayoutConfig.isCardBlurEnabled
    val cardAlpha = cardLayoutConfig.cardAlpha
    val hazeState = cardLayoutConfig.hazeState
    var appeared by remember { mutableStateOf(false) }
    LaunchedEffect(Unit) { appeared = true }

    val scale by
            animateFloatAsState(
                    targetValue = if (appeared) 1f else 0.9f,
                    animationSpec = tween(durationMillis = 350, easing = FastOutSlowInEasing),
                    label = "scale"
            )

    val shape = RoundedCornerShape(22.dp)
    val cardModifier =
            if (isCardBlurEnabled && Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
                Modifier.fillMaxWidth()
                        .graphicsLayer {
                            this.scaleX = scale
                            this.scaleY = scale
                        }
                        .clip(shape)
                        .hazeEffect(state = hazeState)
                        .clickable { onClick() }
            } else {
                Modifier.fillMaxWidth()
                        .graphicsLayer {
                            this.scaleX = scale
                            this.scaleY = scale
                        }
                        .clickable { onClick() }
            }

    Card(
            modifier = cardModifier,
            shape = shape,
            colors =
                    CardDefaults.cardColors(
                            containerColor =
                                    MaterialTheme.colorScheme.surface.copy(alpha = cardAlpha)
                    ),
    ) {
        Row(
                modifier = Modifier.padding(8.dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            Image(
                    painter = painterResource(id = R.drawable.img_minecraft),
                    contentDescription = "Minecraft",
                    modifier = Modifier.size(32.dp)
            )
            Column(modifier = Modifier.weight(1f)) {
                Text(text = "Minecraft ${version.id}", style = MaterialTheme.typography.bodyLarge)
                val versionTypeString =
                        when (version.type) {
                            "release" -> VersionType.Release.title
                            "snapshot" -> VersionType.Snapshot.title
                            "old_alpha", "old_beta" -> VersionType.Ancient.title
                            else -> version.type
                        }
                Text(
                        text = "$versionTypeString - ${version.releaseTime.substringBefore('T')}",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        }
    }
}
