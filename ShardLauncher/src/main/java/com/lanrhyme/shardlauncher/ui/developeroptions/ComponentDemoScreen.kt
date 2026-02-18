package com.lanrhyme.shardlauncher.ui.developeroptions

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Favorite
import androidx.compose.material.icons.filled.PhotoCamera
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material.icons.filled.Share
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.lanrhyme.shardlauncher.common.SidebarPosition
import com.lanrhyme.shardlauncher.ui.components.basic.BackgroundTextTag
import com.lanrhyme.shardlauncher.ui.components.basic.CapsuleTextField
import com.lanrhyme.shardlauncher.ui.components.basic.CombinedCard
import com.lanrhyme.shardlauncher.ui.components.basic.ShardButton
import com.lanrhyme.shardlauncher.ui.components.basic.ShardDialog
import com.lanrhyme.shardlauncher.ui.components.business.FluidFab
import com.lanrhyme.shardlauncher.ui.components.business.FluidFabDirection
import com.lanrhyme.shardlauncher.ui.components.business.FluidFabItem
import com.lanrhyme.shardlauncher.ui.components.layout.LocalCardLayoutConfig
import com.lanrhyme.shardlauncher.ui.components.basic.PopupContainer
import com.lanrhyme.shardlauncher.ui.components.basic.ButtonType
import com.lanrhyme.shardlauncher.ui.components.basic.SegmentedNavigationBar
import com.lanrhyme.shardlauncher.ui.components.basic.ShardCard
import com.lanrhyme.shardlauncher.ui.components.basic.ShardInputField
import com.lanrhyme.shardlauncher.ui.components.layout.SimpleListLayoutCard
import com.lanrhyme.shardlauncher.ui.components.layout.SliderLayoutCard
import com.lanrhyme.shardlauncher.ui.components.basic.StyledFilterChip
import com.lanrhyme.shardlauncher.ui.components.layout.SwitchLayoutCard
import com.lanrhyme.shardlauncher.ui.components.basic.TitleAndSummary
import com.lanrhyme.shardlauncher.ui.components.basic.TitledDivider

@Composable
fun ComponentDemoScreen() {
    val cardLayoutConfig = LocalCardLayoutConfig.current
    val isCardBlurEnabled = cardLayoutConfig.isCardBlurEnabled
    val cardAlpha = cardLayoutConfig.cardAlpha
    val hazeState = cardLayoutConfig.hazeState
    var textState by remember { mutableStateOf("Hello") }
    var switchState by remember { mutableStateOf(false) }
    var sliderState by remember { mutableStateOf(0.5f) }
    var selectedListPage by remember { mutableStateOf(SidebarPosition.Left) }
    var selectedSegment by remember { mutableStateOf("tab1") }

    LazyColumn(
            modifier = Modifier.fillMaxSize(),
            contentPadding = PaddingValues(16.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        item { TitleAndSummary(title = "Component Demo", summary = "A showcase of all components") }

        item {
            var showDialog by remember { mutableStateOf(false) }
            ShardButton(onClick = { showDialog = true }) { Text("Show CustomDialog") }
            ShardDialog(visible = showDialog, onDismissRequest = { showDialog = false }) {
                ShardCard(modifier = Modifier.padding(16.dp)) {
                    Text(
                        "This is a custom dialog with custom content.",
                        modifier = Modifier.padding(16.dp)
                    )
                }
            }
        }

        item {
            ShardButton(
                    onClick = {},
                    type = ButtonType.GRADIENT
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(
                        imageVector = Icons.Default.Favorite,
                        contentDescription = null,
                        modifier = Modifier.size(24.dp)
                    )
                    Spacer(Modifier.size(8.dp))
                    Text("Scaling Action Button")
                }
            }
        }

        item { ShardButton(onClick = {}) { Text("Custom Button") } }

        item {
            ShardInputField(
                    value = textState,
                    onValueChange = { textState = it },
                    label = "ShardInputField"
            )
        }

        item {
            CapsuleTextField(
                value = textState,
                onValueChange = { textState = it },
                label = "Capsule Input",
                hint = "Type here..."
            )
        }

        item {
            SwitchLayoutCard(
                    checked = switchState,
                    onCheckedChange = { switchState = !switchState },
                    title = "Switch Layout"
            )
        }

        item {
            SliderLayoutCard(
                    value = sliderState,
                    onValueChange = { sliderState = it },
                    title = "Slider Layout",
                    isGlowEffectEnabled = true
            )
        }

        item {
            SimpleListLayoutCard(
                    title = "Simple List Layout",
                    items = SidebarPosition.entries,
                    selectedItem = selectedListPage,
                    onValueChange = { selectedListPage = it },
                    getItemText = { pos -> pos.name }
            )
        }

        item { TitledDivider(title = "New Components Demo") }

        item {
            Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                BackgroundTextTag(title = "Primary Tag", icon = Icons.Default.Add)
                BackgroundTextTag(
                        title = "Surface Tag",
                        backgroundColor = MaterialTheme.colorScheme.surfaceVariant
                )
            }
        }

        item {
            var showPopup by remember { mutableStateOf(false) }
            Box {
                ShardButton(onClick = { showPopup = true }) { Text("Show Popup Container") }
                PopupContainer(visible = showPopup, onDismissRequest = { showPopup = false }) {
                    Column(modifier = Modifier.padding(16.dp)) {
                        Text("This is a PopupContainer", fontWeight = FontWeight.Bold)
                        Spacer(modifier = Modifier.height(8.dp))
                        Text("It can contain any content.")
                        Spacer(modifier = Modifier.height(16.dp))
                        ShardButton(onClick = { showPopup = false }) { Text("Close") }
                    }
                }
            }
        }

        item { TitledDivider(title = "Existing Components") }

        item {
            SegmentedNavigationBar(
                    title = "Segmented Nav",
                    selectedPage = selectedSegment,
                    onPageSelected = { selectedSegment = it },
                    pages = listOf("tab1", "tab2", "tab3"),
                    getTitle = { title -> title }
            )
        }

        item {
            var chipSelected by remember { mutableStateOf(false) }
            StyledFilterChip(
                    selected = chipSelected,
                    onClick = { chipSelected = !chipSelected },
                    label = { Text("Styled Filter Chip") }
            )
        }

        item {
            CombinedCard(title = "Combined Card", summary = "With some content") {
                Text("This is the content of the combined card", modifier = Modifier.padding(16.dp))
            }
        }

        item {
            // Fluid FAB Demo
            // Make sure there is enough space for expansion
            Box(
                    modifier = Modifier.fillMaxWidth().height(400.dp).padding(vertical = 16.dp),
                    contentAlignment = Alignment.BottomCenter
            ) {
                Text(
                    "Fluid FAB (Top Direction)",
                    modifier = Modifier.align(Alignment.TopCenter).padding(bottom = 16.dp)
                )

                Spacer(modifier = Modifier.height(16.dp))

                FluidFab(
                        items =
                                listOf(
                                        FluidFabItem("Camera", Icons.Default.PhotoCamera, {}),
                                        FluidFabItem("Settings", Icons.Default.Settings, {}),
                                        FluidFabItem("Share", Icons.Default.Share, {})
                                ),
                        direction = FluidFabDirection.TOP,
                        modifier = Modifier.padding(bottom = 16.dp)
                )
            }
        }
    }
}

