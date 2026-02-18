package com.lanrhyme.shardlauncher.ui.version.config

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import com.lanrhyme.shardlauncher.game.version.installed.SettingState
import com.lanrhyme.shardlauncher.game.version.installed.Version
import com.lanrhyme.shardlauncher.game.version.installed.VersionConfig
import com.lanrhyme.shardlauncher.ui.components.basic.ShardAlertDialog
import com.lanrhyme.shardlauncher.ui.components.layout.ButtonLayoutCard
import com.lanrhyme.shardlauncher.ui.components.layout.SimpleListLayoutCard
import com.lanrhyme.shardlauncher.ui.components.layout.SliderLayoutCard
import com.lanrhyme.shardlauncher.ui.components.layout.SwitchLayoutCard
import com.lanrhyme.shardlauncher.ui.components.layout.TextInputLayoutCard

@Composable
fun VersionConfigScreen(
    version: Version,
    config: VersionConfig,
    onConfigChange: (VersionConfig) -> Unit,
    onSave: () -> Unit,
    onError: (String) -> Unit
) {
    val context = LocalContext.current
    var showSaveDialog by remember { mutableStateOf(false) }
    
    // Local state variables for immediate UI updates
    var isolationType by remember { mutableStateOf(config.isolationType) }
    var skipGameIntegrityCheck by remember { mutableStateOf(config.skipGameIntegrityCheck) }
    var ramAllocation by remember { mutableIntStateOf(if (config.ramAllocation > 0) config.ramAllocation else 2048) }
    var jvmArgs by remember { mutableStateOf(config.jvmArgs) }
    var javaRuntime by remember { mutableStateOf(config.javaRuntime) }
    var renderer by remember { mutableStateOf(config.renderer) }
    var serverIp by remember { mutableStateOf(config.serverIp) }
    var customPath by remember { mutableStateOf(config.customPath) }
    var customInfo by remember { mutableStateOf(config.customInfo) }
    var enableTouchProxy by remember { mutableStateOf(config.enableTouchProxy) }
    var touchVibrateDuration by remember { mutableIntStateOf(config.touchVibrateDuration) }

    // Helper function to update config and notify parent
    fun updateConfig(updater: (VersionConfig) -> Unit) {
        updater(config)
        onConfigChange(config)
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        // 版本设置标题
        com.lanrhyme.shardlauncher.ui.components.basic.TitledDivider(title = "版本设置")

        // 版本隔离
        val isolationOptions = listOf(
            SettingState.FOLLOW_GLOBAL to "跟随全局设置",
            SettingState.ENABLE to "隔离此游戏版本",
            SettingState.DISABLE to "不隔离此游戏版本"
        )
        val selectedIsolationOption = isolationOptions.find { it.first == isolationType }?.second ?: "跟随全局设置"
        
        SimpleListLayoutCard(
            title = "版本隔离",
            summary = "为此版本创建独立的游戏目录",
            items = isolationOptions.map { it.second },
            selectedItem = selectedIsolationOption,
            getItemText = @Composable { it },
            onValueChange = { selectedName ->
                isolationType = isolationOptions.find { it.second == selectedName }?.first 
                    ?: SettingState.FOLLOW_GLOBAL
                updateConfig { it.isolationType = isolationType }
            }
        )

        // 跳过游戏完整性检查
        val integrityCheckOptions = listOf(
            SettingState.FOLLOW_GLOBAL to "跟随全局设置",
            SettingState.ENABLE to "跳过完整性检查",
            SettingState.DISABLE to "执行完整性检查"
        )
        val selectedIntegrityCheckOption = integrityCheckOptions.find { it.first == skipGameIntegrityCheck }?.second ?: "跟随全局设置"
        
        SimpleListLayoutCard(
            title = "游戏完整性检查",
            summary = "跳过游戏文件完整性检查",
            items = integrityCheckOptions.map { it.second },
            selectedItem = selectedIntegrityCheckOption,
            getItemText = @Composable { it },
            onValueChange = { selectedName ->
                skipGameIntegrityCheck = integrityCheckOptions.find { it.second == selectedName }?.first 
                    ?: SettingState.FOLLOW_GLOBAL
                updateConfig { it.skipGameIntegrityCheck = skipGameIntegrityCheck }
            }
        )

        // 游戏设置标题
        Spacer(modifier = Modifier.height(8.dp))
        com.lanrhyme.shardlauncher.ui.components.basic.TitledDivider(title = "游戏设置")

        // 内存分配
        SliderLayoutCard(
            value = ramAllocation.toFloat(),
            onValueChange = { value ->
                ramAllocation = value.toInt()
                updateConfig { it.ramAllocation = ramAllocation }
            },
            title = "内存分配",
            summary = "为游戏分配的内存大小 (MB)",
            valueRange = 512f..8192f,
            displayValue = ramAllocation.toFloat(),
            isGlowEffectEnabled = true
        )

        // JVM参数
        TextInputLayoutCard(
            value = jvmArgs,
            onValueChange = { value ->
                jvmArgs = value
                updateConfig { it.jvmArgs = jvmArgs }
            },
            title = "JVM参数",
            summary = "自定义Java虚拟机启动参数",
            placeholder = "例如: -XX:+UseG1GC -XX:+UnlockExperimentalVMOptions"
        )

        // Java运行时选择
        val runtimes = com.lanrhyme.shardlauncher.game.multirt.RuntimesManager.getRuntimes()
        val runtimeNames = listOf("跟随全局配置") + runtimes.map { it.name }
        val selectedRuntime = if (javaRuntime.isEmpty()) "跟随全局配置" else javaRuntime
        
        SimpleListLayoutCard(
            title = "Java运行时",
            summary = "选择用于启动游戏的Java运行时环境",
            items = runtimeNames,
            selectedItem = selectedRuntime,
            getItemText = @Composable { it },
            onValueChange = { selectedName ->
                javaRuntime = if (selectedName == "跟随全局配置") "" else selectedName
                updateConfig { it.javaRuntime = javaRuntime }
            }
        )

        // 渲染器选择
        val renderers = com.lanrhyme.shardlauncher.game.renderer.Renderers.getAllRenderers()
        val rendererNames = listOf("跟随全局配置") + renderers.map { it.getRendererName() }
        val selectedRenderer = if (renderer.isEmpty()) {
            "跟随全局配置"
        } else {
            renderers.find { it.getUniqueIdentifier() == renderer }?.getRendererName() ?: renderer
        }
        
        SimpleListLayoutCard(
            title = "渲染器",
            summary = "选择游戏使用的渲染器",
            items = rendererNames,
            selectedItem = selectedRenderer,
            getItemText = @Composable { it },
            onValueChange = { selectedName ->
                renderer = if (selectedName == "跟随全局配置") {
                    ""
                } else {
                    renderers.find { it.getRendererName() == selectedName }?.getUniqueIdentifier() ?: ""
                }
                updateConfig { it.renderer = renderer }
            }
        )

        // 高级设置标题
        Spacer(modifier = Modifier.height(8.dp))
        com.lanrhyme.shardlauncher.ui.components.basic.TitledDivider(title = "高级设置")

        // 服务器IP
        TextInputLayoutCard(
            value = serverIp,
            onValueChange = { value ->
                serverIp = value
                updateConfig { it.serverIp = serverIp }
            },
            title = "默认服务器",
            summary = "启动游戏时自动连接的服务器",
            placeholder = "例如: mc.hypixel.net"
        )

        // 自定义路径
        TextInputLayoutCard(
            value = customPath,
            onValueChange = { value ->
                customPath = value
                updateConfig { it.customPath = customPath }
            },
            title = "自定义路径",
            summary = "自定义游戏目录路径（留空使用默认）",
            placeholder = "例如: /custom/minecraft/path"
        )

        // 自定义信息
        TextInputLayoutCard(
            value = customInfo,
            onValueChange = { value ->
                customInfo = value
                updateConfig { it.customInfo = customInfo }
            },
            title = "自定义信息",
            summary = "版本的自定义信息或备注",
            placeholder = "例如: 我的自定义版本"
        )

        // 触控设置标题
        Spacer(modifier = Modifier.height(8.dp))
        com.lanrhyme.shardlauncher.ui.components.basic.TitledDivider(title = "触控设置")

        // 启用触控代理
        SwitchLayoutCard(
            checked = enableTouchProxy,
            onCheckedChange = {
                enableTouchProxy = !enableTouchProxy
                updateConfig { it.enableTouchProxy = enableTouchProxy }
            },
            title = "启用触控代理",
            summary = "为移动设备优化触控操作"
        )

        // 触控震动时长
        SliderLayoutCard(
            value = touchVibrateDuration.toFloat(),
            onValueChange = { value ->
                touchVibrateDuration = value.toInt()
                updateConfig { it.touchVibrateDuration = touchVibrateDuration }
            },
            title = "触控震动时长",
            summary = "触控反馈震动持续时间 (毫秒)",
            valueRange = 0f..500f,
            displayValue = touchVibrateDuration.toFloat(),
            isGlowEffectEnabled = true
        )

        // 保存按钮
        ButtonLayoutCard(
            onClick = { showSaveDialog = true },
            title = "保存配置",
            summary = "保存当前版本的所有配置更改",
            buttonText = "保存"
        )
    }

    ShardAlertDialog(
        visible = showSaveDialog,
        title = "保存配置",
        text = "确定要保存版本配置吗？",
        onDismiss = { showSaveDialog = false },
        onConfirm = {
            try {
                onSave()
                showSaveDialog = false
            } catch (e: Exception) {
                onError("保存配置失败: ${e.message}")
                showSaveDialog = false
            }
        }
    )
}
