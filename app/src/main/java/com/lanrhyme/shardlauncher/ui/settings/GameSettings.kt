package com.lanrhyme.shardlauncher.ui.settings

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.lanrhyme.shardlauncher.ui.components.LocalCardLayoutConfig
import com.lanrhyme.shardlauncher.ui.components.SwitchLayout
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.ui.Alignment
import com.lanrhyme.shardlauncher.ui.components.animatedAppearance
import com.lanrhyme.shardlauncher.ui.components.ScrollIndicator

@Composable
fun GameSettingsContent(
    animationSpeed: Float,
    isGlowEffectEnabled: Boolean
) {
    val listState = rememberLazyListState()
    val allSettings = com.lanrhyme.shardlauncher.settings.AllSettings

    Box(modifier = Modifier.fillMaxSize()) {
        LazyColumn(
            state = listState,
            modifier = Modifier.fillMaxSize(),
            contentPadding = PaddingValues(16.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            // === Game Infrastructure ===
            item { com.lanrhyme.shardlauncher.ui.components.TitledDivider(title = "基础设置", modifier = Modifier.animatedAppearance(0, animationSpeed)) }
            
            item {
                SwitchLayout(
                    modifier = Modifier.animatedAppearance(1, animationSpeed),
                    title = "版本隔离",
                    summary = "为每个游戏版本创建独立的存档文件夹",
                    checked = allSettings.versionIsolation.state,
                    onCheckedChange = { allSettings.versionIsolation.setValue(!allSettings.versionIsolation.state) }
                )
            }

            item {
                SwitchLayout(
                    modifier = Modifier.animatedAppearance(2, animationSpeed),
                    title = "跳过完整性检查",
                    summary = "不建议开启。开启后启动游戏将不再检查文件完整性",
                    checked = allSettings.skipGameIntegrityCheck.state,
                    onCheckedChange = { allSettings.skipGameIntegrityCheck.setValue(!allSettings.skipGameIntegrityCheck.state) }
                )
            }

            // === Runtime & Memory ===
            item { com.lanrhyme.shardlauncher.ui.components.TitledDivider(title = "运行环境", modifier = Modifier.animatedAppearance(3, animationSpeed)) }

            item {
                SwitchLayout(
                    modifier = Modifier.animatedAppearance(4, animationSpeed),
                    title = "自动选择 Java",
                    summary = "根据游戏版本要求自动选择最合适的 Java 运行时",
                    checked = allSettings.autoPickJavaRuntime.state,
                    onCheckedChange = { allSettings.autoPickJavaRuntime.setValue(!allSettings.autoPickJavaRuntime.state) }
                )
            }

            item {
                com.lanrhyme.shardlauncher.ui.components.SliderLayout(
                    modifier = Modifier.animatedAppearance(5, animationSpeed),
                    title = "最大内存分配",
                    summary = "游戏运行时允许使用的最大内存 (MB)",
                    value = allSettings.ramAllocation.state.toFloat(),
                    onValueChange = { allSettings.ramAllocation.setValue(it.toInt()) },
                    valueRange = 512f..8192f,
                    displayValue = allSettings.ramAllocation.state.toFloat(),
                    isGlowEffectEnabled = isGlowEffectEnabled
                )
            }

            // === Graphics & Rendering ===
            item { com.lanrhyme.shardlauncher.ui.components.TitledDivider(title = "图形渲染", modifier = Modifier.animatedAppearance(6, animationSpeed)) }

            item {
                com.lanrhyme.shardlauncher.ui.components.SliderLayout(
                    modifier = Modifier.animatedAppearance(7, animationSpeed),
                    title = "分辨率比例",
                    summary = "降低分辨率可以显著提高 FPS",
                    value = allSettings.resolutionRatio.state.toFloat(),
                    onValueChange = { allSettings.resolutionRatio.setValue(it.toInt()) },
                    valueRange = 25f..300f,
                    displayValue = allSettings.resolutionRatio.state.toFloat(),
                    isGlowEffectEnabled = isGlowEffectEnabled
                )
            }

            item {
                SwitchLayout(
                    modifier = Modifier.animatedAppearance(8, animationSpeed),
                    title = "全屏模式",
                    summary = "启动游戏时自动进入全屏状态",
                    checked = allSettings.gameFullScreen.state,
                    onCheckedChange = { allSettings.gameFullScreen.setValue(!allSettings.gameFullScreen.state) }
                )
            }

            item {
                SwitchLayout(
                    modifier = Modifier.animatedAppearance(9, animationSpeed),
                    title = "持续性能模式",
                    summary = "尝试保持 CPU 处于高频状态以获得更稳定的 FPS",
                    checked = allSettings.sustainedPerformance.state,
                    onCheckedChange = { allSettings.sustainedPerformance.setValue(!allSettings.sustainedPerformance.state) }
                )
            }

            // === Downloader Settings ===
            item { com.lanrhyme.shardlauncher.ui.components.TitledDivider(title = "下载设置", modifier = Modifier.animatedAppearance(10, animationSpeed)) }

            item {
                com.lanrhyme.shardlauncher.ui.components.SimpleListLayout(
                    modifier = Modifier.animatedAppearance(11, animationSpeed),
                    title = "游戏文件下载源",
                    items = com.lanrhyme.shardlauncher.settings.enums.MirrorSourceType.entries,
                    selectedItem = allSettings.fileDownloadSource.state,
                    getItemText = @Composable { androidx.compose.ui.res.stringResource(it.textRes) },
                    onValueChange = { allSettings.fileDownloadSource.setValue(it) }
                )
            }

            item {
                com.lanrhyme.shardlauncher.ui.components.SimpleListLayout(
                    modifier = Modifier.animatedAppearance(12, animationSpeed),
                    title = "模组加载器下载源",
                    items = com.lanrhyme.shardlauncher.settings.enums.MirrorSourceType.entries,
                    selectedItem = allSettings.fetchModLoaderSource.state,
                    getItemText = @Composable { androidx.compose.ui.res.stringResource(it.textRes) },
                    onValueChange = { allSettings.fetchModLoaderSource.setValue(it) }
                )
            }
        }

        ScrollIndicator(
            listState = listState,
            modifier = Modifier.align(Alignment.CenterEnd)
        )
    }
}
