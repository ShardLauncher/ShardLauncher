package com.lanrhyme.shardlauncher.ui.version

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.Crossfade
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.Image
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
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavController
import coil.compose.AsyncImage
import com.lanrhyme.shardlauncher.R
import com.lanrhyme.shardlauncher.game.version.installed.Version
import com.lanrhyme.shardlauncher.game.version.installed.VersionsManager
import com.lanrhyme.shardlauncher.ui.components.SearchTextField
import com.lanrhyme.shardlauncher.ui.components.animatedAppearance
import com.lanrhyme.shardlauncher.ui.components.selectableCard

enum class VersionDetailPane(val title: String, val icon: ImageVector) {
    Config("版本配置", Icons.Default.Settings), // TODO: i18n
    Mods("模组管理", Icons.Default.Extension), // TODO: i18n
    Saves("存档管理", Icons.Default.Save), // TODO: i18n
    ResourcePacks("资源包管理", Icons.Default.Style), // TODO: i18n
    ShaderPacks("光影包管理", Icons.Default.WbSunny) // TODO: i18n
}

@Composable
fun VersionScreen(navController: NavController, animationSpeed: Float) {
    // Refresh versions on load
    LaunchedEffect(Unit) {
        VersionsManager.refresh("VersionScreen_Init")
    }

    // Use versions from manager
    // Note: In a real architecture, this should come from a ViewModel observing the Manager
    val versions = VersionsManager.versions
    var selectedVersion by remember { mutableStateOf<Version?>(null) }
    var selectedPane by remember { mutableStateOf<VersionDetailPane?>(null) }

    fun resetToVersionList() {
        selectedPane = null
    }

    Row(modifier = Modifier.fillMaxSize()) {
        // Left Pane (25%)
        Card(
            modifier = Modifier
                .weight(0.25f)
                .fillMaxHeight()
                .padding(start = 16.dp, top = 16.dp, end = 8.dp, bottom = 16.dp),
            shape = RoundedCornerShape(16.dp),
            colors = CardDefaults.cardColors(
                containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.6f)
            )
        ) {
            LeftNavigationPane(
                selectedVersion = selectedVersion,
                selectedPane = selectedPane,
                onPaneSelected = { pane -> selectedPane = pane }
            )
        }

        // Right Pane (75%)
        Box(modifier = Modifier
            .weight(0.75f)
            .fillMaxHeight()) {
            Crossfade(targetState = selectedPane, label = "RightPaneCrossfade") {
                pane ->
                if (pane == null) {
                    GameVersionListContent(
                        versions = versions,
                        selectedVersion = selectedVersion,
                        onVersionClick = { version ->
                            selectedVersion = version
                        },
                        animationSpeed = animationSpeed
                    )
                } else {
                    RightDetailContent(pane, selectedVersion, onBack = { resetToVersionList() })
                }
            }
        }
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
                        modifier = Modifier
                            .size(100.dp)
                            .padding(8.dp)
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
                Text("请从右侧选择一个版本", textAlign = TextAlign.Center, modifier = Modifier.padding(16.dp)) // TODO: i18n
            }
        } else {
            LazyColumn(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                items(VersionDetailPane.entries) {
                    pane ->
                    val isSelected = selectedPane == pane
                    TextButton(
                        onClick = { onPaneSelected(pane) },
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(12.dp),
                        colors = ButtonDefaults.textButtonColors(
                            containerColor = if (isSelected) MaterialTheme.colorScheme.primary.copy(alpha = 0.1f) else Color.Transparent,
                            contentColor = if (isSelected) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onSurface
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
    onVersionClick: (Version) -> Unit,
    animationSpeed: Float
) {
    var searchText by remember { mutableStateOf("") }
    
    // Filter versions based on search text
    val filteredVersions = remember(versions, searchText) {
        if (searchText.isBlank()) versions
        else versions.filter { it.getVersionName().contains(searchText, ignoreCase = true) }
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp)
    ) {
        Row(
            modifier = Modifier.fillMaxWidth().height(36.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            SearchTextField(
                value = searchText,
                onValueChange = { searchText = it },
                hint = "搜索版本", // TODO: i18n
                modifier = Modifier.weight(1f)
            )
            IconButton(onClick = { VersionsManager.refresh("Manual") }) {
                Icon(Icons.Default.Refresh, contentDescription = "Refresh") // TODO: i18n
            }
            IconButton(onClick = { /* TODO: Sort */ }) {
                Icon(Icons.Default.Sort, contentDescription = "Sort") // TODO: i18n
            }
            IconButton(onClick = { /* TODO: Filter */ }) {
                Icon(Icons.Default.MoreVert, contentDescription = "Filter") // TODO: i18n
            }
            IconButton(onClick = { /* TODO: Directory switch */ }) {
                Icon(Icons.Default.Folder, contentDescription = "Directory") // TODO: i18n
            }
        }

        if (VersionsManager.isRefreshing) {
            Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                CircularProgressIndicator()
            }
        } else if (filteredVersions.isEmpty()) {
            Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                Text("暂无版本", fontSize = 18.sp) // TODO: i18n
            }
        } else {
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
                        onClick = { onVersionClick(version) },
                        index = index,
                        animationSpeed = animationSpeed
                    )
                }
            }
        }
    }
}


@Composable
fun RightDetailContent(pane: VersionDetailPane, version: Version?, onBack: () -> Unit) {
    if (version == null) return

    Column(modifier = Modifier
        .fillMaxSize()
        .padding(16.dp)) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            IconButton(onClick = onBack) {
                Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back to version list") // TODO: i18n
            }
            Spacer(Modifier.width(8.dp))
            Text(
                text = "${pane.title} - ${version.getVersionName()}",
                style = MaterialTheme.typography.titleLarge
            )
        }
        Spacer(Modifier.height(16.dp))

        when (pane) {
            VersionDetailPane.Config -> {
                VersionConfigScreen(
                    version = version,
                    config = version.getVersionConfig(),
                    onConfigChange = { /* Config updates are handled internally in VersionConfigScreen state for now */ },
                    onSave = {
                        // In a real app, we might want to propagate changes back to a ViewModel
                        // For now, VersionConfig is mutable and reference is shared, so we just call save()
                        version.getVersionConfig().save()
                    }
                )
            }
            else -> {
                // Placeholder for other panes
                Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                    Text("这是 ${pane.title} 页面", fontSize = 20.sp) // TODO: i18n
                }
            }
        }
    }
}

@Composable
fun GameVersionCard(version: Version, isSelected: Boolean, onClick: () -> Unit, index: Int, animationSpeed: Float) {
    val interactionSource = remember { MutableInteractionSource() }
    val isPressed by interactionSource.collectIsPressedAsState()

    val shape = RoundedCornerShape(18.dp)

    Card(
        modifier = Modifier
            .animatedAppearance(index, animationSpeed)
            .size(150.dp)
            .selectableCard(isSelected = isSelected, isPressed = isPressed)
            .clip(shape)
            .clickable(interactionSource = interactionSource, indication = null, onClick = onClick),
        shape = shape,
        border = if (isSelected) BorderStroke(2.dp, MaterialTheme.colorScheme.primary) else null,
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.6f)
        )
    ) {
        Box(modifier = Modifier.fillMaxSize()) {
            Box(
                modifier = Modifier
                    .align(Alignment.Center)
                    .fillMaxSize(0.5f)
            ) {
                AsyncImage(
                    model = VersionsManager.getVersionIconFile(version),
                    contentDescription = "${version.getVersionName()} icon",
                    placeholder = painterResource(id = R.drawable.img_minecraft),
                    error = painterResource(id = R.drawable.img_minecraft),
                    contentScale = ContentScale.Fit,
                    modifier = Modifier.fillMaxSize()
                )
            }
            Text(
                text = version.getVersionName(),
                style = MaterialTheme.typography.titleSmall,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.onSurface,
                maxLines = 2,
                overflow = TextOverflow.Ellipsis,
                modifier = Modifier
                    .align(Alignment.BottomStart)
                    .padding(12.dp)
            )
        }
    }
}
