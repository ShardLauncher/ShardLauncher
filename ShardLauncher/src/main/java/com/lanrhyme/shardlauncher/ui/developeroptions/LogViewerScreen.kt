/*
 * Shard Launcher
 * Real-time log viewer for debugging
 */

package com.lanrhyme.shardlauncher.ui.developeroptions

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
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
import com.lanrhyme.shardlauncher.ui.components.layout.LocalCardLayoutConfig
import com.lanrhyme.shardlauncher.ui.components.basic.SubPageNavigationBar
import com.lanrhyme.shardlauncher.ui.components.basic.TitleAndSummary
import com.lanrhyme.shardlauncher.ui.components.basic.ScalingActionButton
import com.lanrhyme.shardlauncher.utils.logging.LogCollector
import kotlinx.coroutines.delay
import android.content.Context
import android.content.Intent
import android.net.Uri
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import java.io.File
import java.io.FileWriter
import java.text.SimpleDateFormat
import java.util.*

@Composable
fun LogViewerScreen(navController: NavController) {
    val context = LocalContext.current
    val cardLayoutConfig = LocalCardLayoutConfig.current
    val isCardBlurEnabled = cardLayoutConfig.isCardBlurEnabled
    val cardAlpha = cardLayoutConfig.cardAlpha
    val hazeState = cardLayoutConfig.hazeState
    
    // 日志级别过滤
    var selectedLevel by remember { mutableStateOf<LogCollector.LogLevel?>(null) }
    var showExportDialog by remember { mutableStateOf(false) }
    var exportMessage by remember { mutableStateOf<String?>(null) }
    
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
                        label = { androidx.compose.material3.Text("全部") }
                    )
                    FilterChip(
                        selected = selectedLevel == LogCollector.LogLevel.DEBUG,
                        onClick = { selectedLevel = LogCollector.LogLevel.DEBUG },
                        label = { androidx.compose.material3.Text("调试") }
                    )
                    FilterChip(
                        selected = selectedLevel == LogCollector.LogLevel.INFO,
                        onClick = { selectedLevel = LogCollector.LogLevel.INFO },
                        label = { androidx.compose.material3.Text("信息") }
                    )
                    FilterChip(
                        selected = selectedLevel == LogCollector.LogLevel.WARNING,
                        onClick = { selectedLevel = LogCollector.LogLevel.WARNING },
                        label = { androidx.compose.material3.Text("警告") }
                    )
                    FilterChip(
                        selected = selectedLevel == LogCollector.LogLevel.ERROR,
                        onClick = { selectedLevel = LogCollector.LogLevel.ERROR },
                        label = { androidx.compose.material3.Text("错误") }
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
                            exportLogs(context) { message ->
                                exportMessage = message
                                showExportDialog = true
                            }
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

    // 导出成功对话框
    if (showExportDialog && exportMessage != null) {
        AlertDialog(
            onDismissRequest = { showExportDialog = false },
            title = { Text("导出结果") },
            text = { Text(exportMessage!!) },
            confirmButton = {
                TextButton(onClick = { showExportDialog = false }) {
                    Text("确定")
                }
            }
        )
    }
}

@Composable
fun LogItem(log: LogCollector.LogEntry) {
    val backgroundColor = when (log.level) {
        LogCollector.LogLevel.DEBUG -> Color(0xFF4CAF50)
        LogCollector.LogLevel.INFO -> Color(0xFF2196F3)
        LogCollector.LogLevel.WARNING -> Color(0xFFFF9800)
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
    }
}

private fun exportLogs(context: Context, onResult: (String) -> Unit) {
    try {
        val logs = LogCollector.getAllLogs()
        if (logs.isEmpty()) {
            onResult("没有可导出的日志")
            return
        }
        
        val timestamp = SimpleDateFormat("yyyyMMdd_HHmmss", Locale.getDefault()).format(Date())
        val fileName = "ShardLauncher_Log_$timestamp.txt"
        val downloadsDir = android.os.Environment.getExternalStoragePublicDirectory(
            android.os.Environment.DIRECTORY_DOWNLOADS
        )
        val outputFile = File(downloadsDir, fileName)
        
        FileWriter(outputFile).use { writer ->
            writer.write("ShardLauncher 日志导出\n")
            writer.write("导出时间: ${SimpleDateFormat("yyyy-MM-dd HH:mm:ss", Locale.getDefault()).format(Date())}\n")
            writer.write("日志数量: ${logs.size}\n")
            writer.write("==================================================\n\n")
            
            val dateFormat = SimpleDateFormat("HH:mm:ss.SSS", Locale.getDefault())
            logs.forEach { log ->
                writer.write("[${dateFormat.format(log.timestamp)}] [${log.level.name}] [${log.tag}] ${log.message}\n")
                log.throwable?.let { throwable ->
                    writer.write("异常: ${throwable.stackTraceToString()}\n")
                }
                writer.write("\n")
            }
        }
        
        // 通知系统文件已创建
        val intent = Intent(Intent.ACTION_MEDIA_SCANNER_SCAN_FILE)
        intent.data = Uri.fromFile(outputFile)
        context.sendBroadcast(intent)
        
        onResult("日志已导出到: ${outputFile.absolutePath}")
    } catch (e: Exception) {
        onResult("导出失败: ${e.message}")
    }
}
