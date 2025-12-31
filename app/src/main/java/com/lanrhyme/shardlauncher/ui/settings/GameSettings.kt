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
import androidx.compose.ui.res.stringResource
import com.lanrhyme.shardlauncher.game.multirt.RuntimesManager

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
            // === 基础设置 (Foundation) ===
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

            item {
                com.lanrhyme.shardlauncher.ui.components.TextInputLayout(
                    modifier = Modifier.animatedAppearance(3, animationSpeed),
                    title = "自定义版本信息",
                    summary = "自定义游戏版本显示信息，[zl_version] 将被替换为实际版本号",
                    value = allSettings.versionCustomInfo.state,
                    onValueChange = { allSettings.versionCustomInfo.setValue(it) },
                    placeholder = "ShardLauncher[zl_version]"
                )
            }

            // === 运行环境 (Runtime) ===
            item { com.lanrhyme.shardlauncher.ui.components.TitledDivider(title = "运行环境", modifier = Modifier.animatedAppearance(4, animationSpeed)) }

            item {
                val runtimes = RuntimesManager.getRuntimes()
                val runtimeNames = listOf("自动选择") + runtimes.map { it.name }
                val selectedRuntime = if (allSettings.javaRuntime.state.isEmpty()) "自动选择" else allSettings.javaRuntime.state
                
                com.lanrhyme.shardlauncher.ui.components.SimpleListLayout(
                    modifier = Modifier.animatedAppearance(5, animationSpeed),
                    title = "Java 运行时",
                    summary = "选择用于启动游戏的 Java 运行时环境",
                    items = runtimeNames,
                    selectedItem = selectedRuntime,
                    getItemText = @Composable { it },
                    onValueChange = { 
                        allSettings.javaRuntime.setValue(if (it == "自动选择") "" else it)
                    }
                )
            }

            item {
                SwitchLayout(
                    modifier = Modifier.animatedAppearance(6, animationSpeed),
                    title = "自动选择 Java",
                    summary = "根据游戏版本要求自动选择最合适的 Java 运行时",
                    checked = allSettings.autoPickJavaRuntime.state,
                    onCheckedChange = { allSettings.autoPickJavaRuntime.setValue(!allSettings.autoPickJavaRuntime.state) }
                )
            }

            item {
                com.lanrhyme.shardlauncher.ui.components.SliderLayout(
                    modifier = Modifier.animatedAppearance(7, animationSpeed),
                    title = "最大内存分配",
                    summary = "游戏运行时允许使用的最大内存 (MB)",
                    value = allSettings.ramAllocation.state.toFloat(),
                    onValueChange = { allSettings.ramAllocation.setValue(it.toInt()) },
                    valueRange = 512f..8192f,
                    displayValue = allSettings.ramAllocation.state.toFloat(),
                    isGlowEffectEnabled = isGlowEffectEnabled
                )
            }

            item {
                com.lanrhyme.shardlauncher.ui.components.TextInputLayout(
                    modifier = Modifier.animatedAppearance(8, animationSpeed),
                    title = "JVM 启动参数",
                    summary = "自定义 Java 虚拟机启动参数，多个参数用空格分隔",
                    value = allSettings.jvmArgs.state,
                    onValueChange = { allSettings.jvmArgs.setValue(it) },
                    placeholder = "-XX:+UseG1GC -Dsun.rmi.dgc.server.gcInterval=2147483646"
                )
            }

            // === 图形渲染 (Graphics) ===
            item { com.lanrhyme.shardlauncher.ui.components.TitledDivider(title = "图形渲染", modifier = Modifier.animatedAppearance(9, animationSpeed)) }

            item {
                com.lanrhyme.shardlauncher.ui.components.TextInputLayout(
                    modifier = Modifier.animatedAppearance(10, animationSpeed),
                    title = "全局渲染器",
                    summary = "选择游戏使用的渲染器",
                    value = allSettings.renderer.state,
                    onValueChange = { allSettings.renderer.setValue(it) },
                    placeholder = "gl4es_114"
                )
            }

            item {
                com.lanrhyme.shardlauncher.ui.components.TextInputLayout(
                    modifier = Modifier.animatedAppearance(11, animationSpeed),
                    title = "Vulkan 驱动",
                    summary = "选择 Vulkan 图形驱动",
                    value = allSettings.vulkanDriver.state,
                    onValueChange = { allSettings.vulkanDriver.setValue(it) },
                    placeholder = "default turnip"
                )
            }

            item {
                com.lanrhyme.shardlauncher.ui.components.SliderLayout(
                    modifier = Modifier.animatedAppearance(12, animationSpeed),
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
                    modifier = Modifier.animatedAppearance(13, animationSpeed),
                    title = "全屏模式",
                    summary = "启动游戏时自动进入全屏状态",
                    checked = allSettings.gameFullScreen.state,
                    onCheckedChange = { allSettings.gameFullScreen.setValue(!allSettings.gameFullScreen.state) }
                )
            }

            item {
                SwitchLayout(
                    modifier = Modifier.animatedAppearance(14, animationSpeed),
                    title = "使用系统 Vulkan 驱动",
                    summary = "在 Zink 渲染器中使用系统提供的 Vulkan 驱动",
                    checked = allSettings.zinkPreferSystemDriver.state,
                    onCheckedChange = { allSettings.zinkPreferSystemDriver.setValue(!allSettings.zinkPreferSystemDriver.state) }
                )
            }

            item {
                SwitchLayout(
                    modifier = Modifier.animatedAppearance(15, animationSpeed),
                    title = "Zink 垂直同步",
                    summary = "在 Zink 渲染器中启用垂直同步以减少画面撕裂",
                    checked = allSettings.vsyncInZink.state,
                    onCheckedChange = { allSettings.vsyncInZink.setValue(!allSettings.vsyncInZink.state) }
                )
            }

            item {
                SwitchLayout(
                    modifier = Modifier.animatedAppearance(16, animationSpeed),
                    title = "大核心亲和性",
                    summary = "强制游戏在高性能 CPU 核心上运行",
                    checked = allSettings.bigCoreAffinity.state,
                    onCheckedChange = { allSettings.bigCoreAffinity.setValue(!allSettings.bigCoreAffinity.state) }
                )
            }

            item {
                SwitchLayout(
                    modifier = Modifier.animatedAppearance(17, animationSpeed),
                    title = "持续性能模式",
                    summary = "尝试保持 CPU 处于高频状态以获得更稳定的 FPS",
                    checked = allSettings.sustainedPerformance.state,
                    onCheckedChange = { allSettings.sustainedPerformance.setValue(!allSettings.sustainedPerformance.state) }
                )
            }

            // === 日志管理 (Logs) ===
            item { com.lanrhyme.shardlauncher.ui.components.TitledDivider(title = "日志管理", modifier = Modifier.animatedAppearance(18, animationSpeed)) }

            item {
                SwitchLayout(
                    modifier = Modifier.animatedAppearance(19, animationSpeed),
                    title = "自动显示日志",
                    summary = "启动游戏时自动显示日志，直到游戏开始渲染",
                    checked = allSettings.showLogAutomatic.state,
                    onCheckedChange = { allSettings.showLogAutomatic.setValue(!allSettings.showLogAutomatic.state) }
                )
            }

            item {
                com.lanrhyme.shardlauncher.ui.components.SliderLayout(
                    modifier = Modifier.animatedAppearance(20, animationSpeed),
                    title = "日志文字大小",
                    summary = "调整日志显示的文字大小",
                    value = allSettings.logTextSize.state.toFloat(),
                    onValueChange = { allSettings.logTextSize.setValue(it.toInt()) },
                    valueRange = 5f..20f,
                    displayValue = allSettings.logTextSize.state.toFloat(),
                    isGlowEffectEnabled = isGlowEffectEnabled
                )
            }

            item {
                com.lanrhyme.shardlauncher.ui.components.SliderLayout(
                    modifier = Modifier.animatedAppearance(21, animationSpeed),
                    title = "缓冲区刷新间隔",
                    summary = "日志缓冲区刷新间隔 (毫秒)，较小的值会更及时显示日志",
                    value = allSettings.logBufferFlushInterval.state.toFloat(),
                    onValueChange = { allSettings.logBufferFlushInterval.setValue(it.toInt()) },
                    valueRange = 100f..1000f,
                    displayValue = allSettings.logBufferFlushInterval.state.toFloat(),
                    isGlowEffectEnabled = isGlowEffectEnabled
                )
            }

            // === 下载设置 (Downloads) ===
            item { com.lanrhyme.shardlauncher.ui.components.TitledDivider(title = "下载设置", modifier = Modifier.animatedAppearance(22, animationSpeed)) }

            item {
                com.lanrhyme.shardlauncher.ui.components.SimpleListLayout(
                    modifier = Modifier.animatedAppearance(23, animationSpeed),
                    title = "游戏文件下载源",
                    summary = "选择下载游戏文件时优先使用的源",
                    items = com.lanrhyme.shardlauncher.settings.enums.MirrorSourceType.entries,
                    selectedItem = allSettings.fileDownloadSource.state,
                    getItemText = @Composable { stringResource(it.textRes) },
                    onValueChange = { allSettings.fileDownloadSource.setValue(it) }
                )
            }

            item {
                com.lanrhyme.shardlauncher.ui.components.SimpleListLayout(
                    modifier = Modifier.animatedAppearance(24, animationSpeed),
                    title = "模组加载器下载源",
                    summary = "选择下载模组加载器时优先使用的源",
                    items = com.lanrhyme.shardlauncher.settings.enums.MirrorSourceType.entries,
                    selectedItem = allSettings.fetchModLoaderSource.state,
                    getItemText = @Composable { stringResource(it.textRes) },
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
