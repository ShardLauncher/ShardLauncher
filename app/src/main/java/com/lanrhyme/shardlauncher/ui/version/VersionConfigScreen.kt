package com.lanrhyme.shardlauncher.ui.version

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import com.lanrhyme.shardlauncher.game.version.installed.SettingState
import com.lanrhyme.shardlauncher.game.version.installed.Version
import com.lanrhyme.shardlauncher.game.version.installed.VersionConfig

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun VersionConfigScreen(
    version: Version,
    config: VersionConfig,
    onConfigChange: (VersionConfig) -> Unit,
    onSave: () -> Unit
) {
    val context = LocalContext.current
    var currentConfig by remember(config) { mutableStateOf(config.copy()) }

    // Helper to update config and trigger callback
    fun updateConfig(update: VersionConfig.() -> Unit) {
        val newConfig = currentConfig.copy()
        newConfig.update()
        currentConfig = newConfig
        onConfigChange(newConfig)
    }

    LazyColumn(
        modifier = Modifier.fillMaxSize(),
        contentPadding = PaddingValues(16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        item {
            Text(
                text = "版本设置: ${version.getVersionName()}", // TODO: i18n
                style = MaterialTheme.typography.titleMedium,
                color = MaterialTheme.colorScheme.primary
            )
        }

        // Isolation
        item {
            var expanded by remember { mutableStateOf(false) }
            Card(
                colors = CardDefaults.cardColors(
                    containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.6f)
                )
            ) {
                Column(Modifier.fillMaxWidth().padding(16.dp)) {
                    Text("版本隔离", style = MaterialTheme.typography.titleSmall) // TODO: i18n
                    Spacer(Modifier.height(4.dp))
                    ExposedDropdownMenuBox(
                        expanded = expanded,
                        onExpandedChange = { expanded = it }
                    ) {
                        OutlinedTextField(
                            value = context.getString(currentConfig.isolationType.textRes),
                            onValueChange = {},
                            readOnly = true,
                            trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = expanded) },
                            modifier = Modifier.fillMaxWidth().menuAnchor(),
                            colors = OutlinedTextFieldDefaults.colors()
                        )
                        ExposedDropdownMenu(
                            expanded = expanded,
                            onDismissRequest = { expanded = false }
                        ) {
                            SettingState.entries.forEach { state ->
                                DropdownMenuItem(
                                    text = { Text(context.getString(state.textRes)) },
                                    onClick = {
                                        updateConfig { isolationType = state }
                                        expanded = false
                                    }
                                )
                            }
                        }
                    }
                }
            }
        }

        // Game Integrity Check
        item {
            var expanded by remember { mutableStateOf(false) }
            Card(
                colors = CardDefaults.cardColors(
                    containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.6f)
                )
            ) {
                Column(Modifier.fillMaxWidth().padding(16.dp)) {
                    Text("跳过游戏完整性检查", style = MaterialTheme.typography.titleSmall) // TODO: i18n
                    Spacer(Modifier.height(4.dp))
                    ExposedDropdownMenuBox(
                        expanded = expanded,
                        onExpandedChange = { expanded = it }
                    ) {
                        OutlinedTextField(
                            value = context.getString(currentConfig.skipGameIntegrityCheck.textRes),
                            onValueChange = {},
                            readOnly = true,
                            trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = expanded) },
                            modifier = Modifier.fillMaxWidth().menuAnchor(),
                            colors = OutlinedTextFieldDefaults.colors()
                        )
                        ExposedDropdownMenu(
                            expanded = expanded,
                            onDismissRequest = { expanded = false }
                        ) {
                            SettingState.entries.forEach { state ->
                                DropdownMenuItem(
                                    text = { Text(context.getString(state.textRes)) },
                                    onClick = {
                                        updateConfig { skipGameIntegrityCheck = state }
                                        expanded = false
                                    }
                                )
                            }
                        }
                    }
                }
            }
        }

        // Memory Allocation
        item {
            var memory by remember(currentConfig.ramAllocation) { 
                mutableFloatStateOf(if (currentConfig.ramAllocation < 0) 0f else currentConfig.ramAllocation.toFloat()) 
            }
            
            Card(
                colors = CardDefaults.cardColors(
                    containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.6f)
                )
            ) {
                Column(Modifier.fillMaxWidth().padding(16.dp)) {
                    Text("内存分配 (MB)", style = MaterialTheme.typography.titleSmall) // TODO: i18n
                    Spacer(Modifier.height(8.dp))
                    Slider(
                        value = memory,
                        onValueChange = { 
                            memory = it
                            updateConfig { ramAllocation = it.toInt() } 
                        },
                        valueRange = 0f..16384f,
                        steps = 63
                    )
                    Text(
                        text = if (memory <= 0) "跟随全局设置 (0 MB)" else "${memory.toInt()} MB", // TODO: i18n
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }
        }

        // Java Runtime
        item {
            Card(
                colors = CardDefaults.cardColors(
                    containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.6f)
                )
            ) {
                Column(Modifier.fillMaxWidth().padding(16.dp)) {
                    OutlinedTextField(
                        value = currentConfig.javaRuntime,
                        onValueChange = { updateConfig { javaRuntime = it } },
                        label = { Text("Java 运行时路径") }, // TODO: i18n
                        modifier = Modifier.fillMaxWidth(),
                        singleLine = true,
                        placeholder = { Text("留空跟随全局设置") } // TODO: i18n
                    )
                }
            }
        }

        // JVM Args
        item {
            Card(
                colors = CardDefaults.cardColors(
                    containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.6f)
                )
            ) {
                Column(Modifier.fillMaxWidth().padding(16.dp)) {
                    OutlinedTextField(
                        value = currentConfig.jvmArgs,
                        onValueChange = { updateConfig { jvmArgs = it } },
                        label = { Text("JVM 参数") }, // TODO: i18n
                        modifier = Modifier.fillMaxWidth(),
                        minLines = 3,
                        maxLines = 5,
                        placeholder = { Text("留空跟随全局设置") } // TODO: i18n
                    )
                }
            }
        }

        // Action Buttons
        item {
            Row(
                modifier = Modifier.fillMaxWidth().padding(top = 16.dp),
                horizontalArrangement = Arrangement.End
            ) {
                Button(onClick = onSave) {
                    Text("保存配置") // TODO: i18n
                }
            }
        }
        
        // Bottom Spacer
        item {
            Spacer(modifier = Modifier.height(32.dp))
        }
    }
}
