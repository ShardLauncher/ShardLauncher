/*
 * Shard Launcher
 * Real-time log viewer for debugging
 */

package com.lanrhyme.shardlauncher.ui.developeroptions

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavController
import com.lanrhyme.shardlauncher.ui.components.*
import com.lanrhyme.shardlauncher.utils.logging.LogCollector
import kotlinx.coroutines.delay

@Composable
fun LogViewerScreen(navController: NavController) {
    val cardLayoutConfig = LocalCardLayoutConfig.current
    val isCardBlurEnabled = cardLayoutConfig.isCardBlurEnabled
    val cardAlpha = cardLayoutConfig.cardAlpha
    val hazeState = cardLayoutConfig.hazeState
    
    // 日志级别过滤
    var selectedLevel by remember { mutableStateOf<LogCollector.LogLevel?>(null) }
    
    // 自动刷新日志
    var autoRefresh by remember { mutableStateOf(true) }
    
    // 日志列表
    var logs by remember { mutableStateOf(emptyList<LogCollector.LogEntry>()) }
    
    // 自动刷新日志
    LaunchedEffect(autoRefresh) {
        while (autoRefresh) {
            logs = if (selectedLevel != null) {
                LogCollector.getLogsByLevel(selectedLevel!!)
            } else {
                LogCollector.getAllLogs()
            }
            delay(500) // 每500ms刷新一次
        }
    }
    
    // 手动刷新一次
    LaunchedEffect(selectedLevel) {
        logs = if (selectedLevel != null) {
            LogCollector.getLogsByLevel(selectedLevel!!)
        } else {
            LogCollector.getAllLogs()
        }
    }
    
    Column(
        modifier = Modifier.fillMaxSize()
    ) {
        // 顶部导航栏
        Card(
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(22.dp)
        ) {
            Column(modifier = Modifier.padding(18.dp)) {
                SubPageNavigationBar(
                    title = "实时日志",
                    onBack = { navController.navigateUp() }
                )
            }
        }
        
        // 控制面板
        Card(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp, vertical = 8.dp),
            shape = RoundedCornerShape(22.dp)
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(16.dp)
            ) {
                TitleAndSummary(
                    title = "日志控制",
                    summary = "过滤和刷新日志"
                )
                
                Spacer(modifier = Modifier.height(12.dp))
                
                // 日志级别过滤
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    FilterChip(
                        selected = selectedLevel == null,
                        onClick = { selectedLevel = null },
                        label = { Text("全部") }
                    )
                    FilterChip(
                        selected = selectedLevel == LogCollector.LogLevel.DEBUG,
                        onClick = { selectedLevel = LogCollector.LogLevel.DEBUG },
                        label = { Text("调试") }
                    )
                    FilterChip(
                        selected = selectedLevel == LogCollector.LogLevel.INFO,
                        onClick = { selectedLevel = LogCollector.LogLevel.INFO },
                        label = { Text("信息") }
                    )
                    FilterChip(
                        selected = selectedLevel == LogCollector.LogLevel.WARNING,
                        onClick = { selectedLevel = LogCollector.LogLevel.WARNING },
                        label = { Text("警告") }
                    )
                    FilterChip(
                        selected = selectedLevel == LogCollector.LogLevel.ERROR,
                        onClick = { selectedLevel = LogCollector.LogLevel.ERROR },
                        label = { Text("错误") }
                    )
                }
                
                Spacer(modifier = Modifier.height(12.dp))
                
                // 控制按钮
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    // 自动刷新开关
                    ScalingActionButton(
                        onClick = { autoRefresh = !autoRefresh },
                        modifier = Modifier.weight(1f),
                        text = if (autoRefresh) "停止刷新" else "自动刷新",
                        icon = if (autoRefresh) Icons.Default.Pause else Icons.Default.PlayArrow
                    )
                    
                    // 清空日志
                    ScalingActionButton(
                        onClick = { 
                            LogCollector.clear()
                            logs = emptyList()
                        },
                        modifier = Modifier.weight(1f),
                        text = "清空日志",
                        icon = Icons.Default.Delete
                    )
                    
                    // 导出日志
                    ScalingActionButton(
                        onClick = { 
                            // TODO: 实现导出到文件
                        },
                        modifier = Modifier.weight(1f),
                        text = "导出日志",
                        icon = Icons.Default.Save
                    )
                }
                
                Spacer(modifier = Modifier.height(8.dp))
                
                // 日志统计
                val stats = LogCollector.getStats()
                Text(
                    text = "日志统计: " +
                        "调试:${stats[LogCollector.LogLevel.DEBUG] ?: 0} " +
                        "信息:${stats[LogCollector.LogLevel.INFO] ?: 0} " +
                        "警告:${stats[LogCollector.LogLevel.WARNING] ?: 0} " +
                        "错误:${stats[LogCollector.LogLevel.ERROR] ?: 0}",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        }
        
        // 日志列表
        Card(
            modifier = Modifier
                .fillMaxWidth()
                .weight(1f)
                .padding(horizontal = 16.dp, vertical = 8.dp),
            shape = RoundedCornerShape(22.dp)
        ) {
            if (logs.isEmpty()) {
                Box(
                    modifier = Modifier.fillMaxSize(),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = "暂无日志",
                        style = MaterialTheme.typography.bodyLarge,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            } else {
                LazyColumn(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(8.dp),
                    reverseLayout = true // 最新的日志在最下面
                ) {
                    items(logs.reversed()) { log ->
                        LogItem(log = log)
                        Divider(modifier = Modifier.padding(vertical = 2.dp))
                    }
                }
            }
        }
    }
}

@Composable
private fun LogItem(log: LogCollector.LogEntry) {
    val backgroundColor = when (log.level) {
        LogCollector.LogLevel.DEBUG -> Color(0xFF37474F)
        LogCollector.LogLevel.INFO -> Color(0xFF0D47A1)
        LogCollector.LogLevel.WARNING -> Color(0xFFF57C00)
        LogCollector.LogLevel.ERROR -> Color(0xFFB71C1C)
    }
    
    val timeStr = remember(log.timestamp) {
        java.text.SimpleDateFormat("HH:mm:ss").format(log.timestamp)
    }
    
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .background(
                color = backgroundColor.copy(alpha = 0.1f),
                shape = RoundedCornerShape(4.dp)
            )
            .padding(8.dp)
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Text(
                text = "[${log.level.name.first()}] ${log.tag}",
                style = MaterialTheme.typography.labelSmall,
                fontWeight = FontWeight.Bold,
                color = backgroundColor
            )
            Text(
                text = timeStr,
                style = MaterialTheme.typography.labelSmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
        
        Spacer(modifier = Modifier.height(2.dp))
        
        Text(
            text = log.message,
            style = MaterialTheme.typography.bodySmall,
            fontFamily = FontFamily.Monospace,
            lineHeight = 16.sp
        )
        
        log.throwback?.let { throwable ->
            Spacer(modifier = Modifier.height(4.dp))
            Text(
                text = throwable.stackTraceToString(),
                style = MaterialTheme.typography.bodySmall,
                fontFamily = FontFamily.Monospace,
                color = MaterialTheme.colorScheme.error,
                lineHeight = 14.sp
            )
        }
    }
}