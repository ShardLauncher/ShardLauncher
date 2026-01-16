/*
 * Shard Launcher
 * Real-time log collector for debugging
 */

package com.lanrhyme.shardlauncher.utils.logging

import java.util.concurrent.CopyOnWriteArrayList

/**
 * 实时日志收集器，用于调试
 * 收集所有日志并在内存中保存，方便实时查看
 */
object LogCollector {
    private const val MAX_LOGS = 10000 // 最多保存10000条日志
    
    private val logs = CopyOnWriteArrayList<LogEntry>()
    
    data class LogEntry(
        val level: LogLevel,
        val tag: String,
        val message: String,
        val timestamp: Long = System.currentTimeMillis(),
        val throwable: Throwable? = null
    )
    
    enum class LogLevel {
        DEBUG, INFO, WARNING, ERROR
    }
    
    /**
     * 添加日志
     */
    fun add(level: LogLevel, tag: String, message: String, throwable: Throwable? = null) {
        val entry = LogEntry(level, tag, message, throwable = throwable)
        logs.add(entry)
        
        // 限制日志数量，避免内存溢出
        if (logs.size > MAX_LOGS) {
            logs.subList(0, logs.size - MAX_LOGS).clear()
        }
    }
    
    /**
     * 获取所有日志
     */
    fun getAllLogs(): List<LogEntry> = logs.toList()
    
    /**
     * 按级别过滤日志
     */
    fun getLogsByLevel(level: LogLevel): List<LogEntry> = logs.filter { it.level == level }
    
    /**
     * 清空日志
     */
    fun clear() {
        logs.clear()
    }
    
    /**
     * 获取日志统计
     */
    fun getStats(): Map<LogLevel, Int> {
        return logs.groupBy { it.level }.mapValues { it.value.size }
    }
    
    /**
     * 将日志格式化为字符串
     */
    fun formatLogEntry(entry: LogEntry): String {
        val timeStr = java.text.SimpleDateFormat("HH:mm:ss.SSS").format(entry.timestamp)
        val levelStr = when (entry.level) {
            LogLevel.DEBUG -> "D"
            LogLevel.INFO -> "I"
            LogLevel.WARNING -> "W"
            LogLevel.ERROR -> "E"
        }
        val throwableStr = entry.throwable?.let { "\n${it.stackTraceToString()}" } ?: ""
        return "[$timeStr] $levelStr/${entry.tag}: ${entry.message}$throwableStr"
    }
    
    /**
     * 导出所有日志为字符串
     */
    fun exportLogs(): String {
        return buildString {
            logs.forEach { entry ->
                appendLine(formatLogEntry(entry))
            }
        }
    }
}

/**
 * 包装 Logger，自动将日志发送到 LogCollector
 */
object LoggerWithCollector {
    fun d(tag: String, message: String) {
        Logger.d(tag, message)
        LogCollector.add(LogCollector.LogLevel.DEBUG, tag, message)
    }
    
    fun i(tag: String, message: String) {
        Logger.i(tag, message)
        LogCollector.add(LogCollector.LogLevel.INFO, tag, message)
    }
    
    fun w(tag: String, message: String, throwable: Throwable? = null) {
        Logger.w(tag, message)
        LogCollector.add(LogCollector.LogLevel.WARNING, tag, message, throwable)
    }
    
    fun e(tag: String, message: String, throwable: Throwable? = null) {
        Logger.e(tag, message, throwable)
        LogCollector.add(LogCollector.LogLevel.ERROR, tag, message, throwable)
    }
    
    // Legacy methods
    fun lDebug(message: String) {
        Logger.lDebug(message)
        LogCollector.add(LogCollector.LogLevel.DEBUG, "ShardLauncher", message)
    }
    
    fun lInfo(message: String) {
        Logger.lInfo(message)
        LogCollector.add(LogCollector.LogLevel.INFO, "ShardLauncher", message)
    }
    
    fun lWarning(message: String, throwable: Throwable? = null) {
        Logger.lWarning(message, throwable)
        LogCollector.add(LogCollector.LogLevel.WARNING, "ShardLauncher", message, throwable)
    }
    
    fun lError(message: String, throwable: Throwable? = null) {
        Logger.lError(message, throwable)
        LogCollector.add(LogCollector.LogLevel.ERROR, "ShardLauncher", message, throwable)
    }
}