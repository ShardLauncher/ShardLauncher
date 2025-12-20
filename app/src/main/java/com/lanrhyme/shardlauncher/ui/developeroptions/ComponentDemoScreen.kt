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
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Favorite
import androidx.compose.material.icons.filled.PhotoCamera
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material.icons.filled.Share
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
import com.lanrhyme.shardlauncher.ui.components.BackgroundTextTag
import com.lanrhyme.shardlauncher.ui.components.CombinedCard
import com.lanrhyme.shardlauncher.ui.components.CustomButton
import com.lanrhyme.shardlauncher.ui.components.CustomCard
import com.lanrhyme.shardlauncher.ui.components.CustomDialog
import com.lanrhyme.shardlauncher.ui.components.CustomTextField
import com.lanrhyme.shardlauncher.ui.components.FluidFab
import com.lanrhyme.shardlauncher.ui.components.FluidFabDirection
import com.lanrhyme.shardlauncher.ui.components.FluidFabItem
import com.lanrhyme.shardlauncher.ui.components.PopupContainer
import com.lanrhyme.shardlauncher.ui.components.ScalingActionButton
import com.lanrhyme.shardlauncher.ui.components.SegmentedNavigationBar
import com.lanrhyme.shardlauncher.ui.components.SimpleListLayout
import com.lanrhyme.shardlauncher.ui.components.SliderLayout
import com.lanrhyme.shardlauncher.ui.components.StyledFilterChip
import com.lanrhyme.shardlauncher.ui.components.SwitchLayout
import com.lanrhyme.shardlauncher.ui.components.TitleAndSummary
import com.lanrhyme.shardlauncher.ui.components.TitledDivider
import dev.chrisbanes.haze.HazeState

@Composable
fun ComponentDemoScreen(isCardBlurEnabled: Boolean, cardAlpha: Float, hazeState: HazeState) {
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
            CustomButton(onClick = { showDialog = true }) { Text("Show CustomDialog") }
            CustomDialog(visible = showDialog, onDismissRequest = { showDialog = false }) {
                CustomCard(modifier = Modifier.padding(16.dp)) {
                    Text(
                            "This is a custom dialog with custom content.",
                            modifier = Modifier.padding(16.dp)
                    )
                }
            }
        }

        item {
            ScalingActionButton(
                    onClick = {},
                    text = "Scaling Action Button",
                    icon = Icons.Default.Favorite
            )
        }

        item { CustomCard { Text("This is a CustomCard", modifier = Modifier.padding(16.dp)) } }

        item { CustomButton(onClick = {}) { Text("Custom Button") } }

        item {
            CustomTextField(
                    value = textState,
                    onValueChange = { textState = it },
                    label = "Custom Text Field"
            )
        }

        item {
            SwitchLayout(
                    checked = switchState,
                    onCheckedChange = { switchState = !switchState },
                    title = "Switch Layout",
                    isCardBlurEnabled = isCardBlurEnabled,
                    cardAlpha = cardAlpha,
                    hazeState = hazeState
            )
        }

        item {
            SliderLayout(
                    value = sliderState,
                    onValueChange = { sliderState = it },
                    title = "Slider Layout",
                    isGlowEffectEnabled = true,
                    isCardBlurEnabled = isCardBlurEnabled,
                    cardAlpha = cardAlpha,
                    hazeState = hazeState
            )
        }

        item {
            SimpleListLayout(
                    title = "Simple List Layout",
                    items = SidebarPosition.entries,
                    selectedItem = selectedListPage,
                    onValueChange = { selectedListPage = it },
                    getItemText = { pos -> pos.name },
                    isCardBlurEnabled = isCardBlurEnabled,
                    cardAlpha = cardAlpha,
                    hazeState = hazeState
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
                CustomButton(onClick = { showPopup = true }) { Text("Show Popup Container") }
                PopupContainer(visible = showPopup, onDismissRequest = { showPopup = false }) {
                    Column(modifier = Modifier.padding(16.dp)) {
                        Text("This is a PopupContainer", fontWeight = FontWeight.Bold)
                        Spacer(modifier = Modifier.height(8.dp))
                        Text("It can contain any content.")
                        Spacer(modifier = Modifier.height(16.dp))
                        CustomButton(onClick = { showPopup = false }) { Text("Close") }
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
            CombinedCard(
                    title = "Combined Card",
                    summary = "With some content",
                    isCardBlurEnabled = isCardBlurEnabled,
                    cardAlpha = cardAlpha,
                    hazeState = hazeState
            ) {
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
