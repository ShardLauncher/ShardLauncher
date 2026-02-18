package com.lanrhyme.shardlauncher.ui.downloads

import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.layout.ColumnScope
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavController
import com.lanrhyme.shardlauncher.model.BmclapiManifest
import com.lanrhyme.shardlauncher.ui.components.basic.*
import com.lanrhyme.shardlauncher.ui.components.business.VersionListItem
import com.lanrhyme.shardlauncher.ui.components.layout.PageLazyColumn
import com.lanrhyme.shardlauncher.ui.components.layout.PageStateContainer
import com.lanrhyme.shardlauncher.ui.components.layout.PageSection

@OptIn(ExperimentalMaterial3Api::class, ExperimentalFoundationApi::class)
@Composable
fun GameDownloadContent(navController: NavController) {
    val viewModel: GameDownloadViewModel = viewModel()

    val versions by viewModel.filteredVersions.collectAsState()
    val selectedVersionTypes by viewModel.selectedVersionTypes.collectAsState()
    val searchQuery by viewModel.searchQuery.collectAsState()
    val isLoading by viewModel.isLoading.collectAsState()

    LaunchedEffect(Unit) { viewModel.loadVersions() }

    val animatedSpeed = 1.0f

    PageStateContainer(
        isLoading = isLoading && versions.isEmpty(),
        isEmpty = versions.isEmpty() && !isLoading,
        modifier = Modifier.fillMaxSize()
    ) {
        PageLazyColumn(
            contentPadding = PaddingValues(16.dp),
            showScrollIndicator = true
        ) {
            // Filter Section
            item {
                PageSection(title = "版本筛选") {
                    Spacer(modifier = Modifier.height(8.dp))
                    FilterBar(
                        selectedVersionTypes = selectedVersionTypes.toList(),
                        searchQuery = searchQuery,
                        onVersionTypeToggle = { viewModel.toggleVersionType(it) },
                        onSearchQueryChange = { viewModel.setSearchQuery(it) },
                        onRefresh = { viewModel.loadVersions(forceRefresh = true) },
                        animatedSpeed = animatedSpeed
                    )
                }
            }

            // Version List
            items(
                items = versions.take(50),
                key = { it.id }
            ) { version ->
                VersionListItem(
                    version = version,
                    modifier = Modifier.animatedAppearance(2, animatedSpeed)
                ) {
                    navController.navigate("version_detail/${version.id}")
                }
            }
        }
    }
}

@Composable
private fun FilterBar(
    selectedVersionTypes: List<VersionType>,
    searchQuery: String,
    onVersionTypeToggle: (VersionType) -> Unit,
    onSearchQueryChange: (String) -> Unit,
    onRefresh: () -> Unit,
    animatedSpeed: Float
) {
    ShardGlassCard(
        modifier = Modifier.animatedAppearance(1, animatedSpeed),
        shape = RoundedCornerShape(24.dp)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .height(48.dp)
                .padding(horizontal = 12.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            VersionType.entries.forEach { versionType ->
                StyledFilterChip(
                    selected = versionType in selectedVersionTypes,
                    onClick = { onVersionTypeToggle(versionType) },
                    label = { androidx.compose.material3.Text(versionType.title) }
                )
            }

            Spacer(modifier = Modifier.width(4.dp))

            SearchTextField(
                value = searchQuery,
                onValueChange = onSearchQueryChange,
                hint = "搜索游戏版本...",
                modifier = Modifier
                    .weight(1f)
                    .fillMaxHeight()
            )

            IconButton(
                onClick = onRefresh,
                modifier = Modifier.size(36.dp)
            ) {
                Icon(
                    Icons.Default.Refresh,
                    contentDescription = "刷新",
                    modifier = Modifier.size(20.dp)
                )
            }
        }
    }
}