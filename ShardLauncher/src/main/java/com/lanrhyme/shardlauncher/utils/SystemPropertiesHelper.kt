package com.lanrhyme.shardlauncher.utils

import android.os.Build
import java.io.BufferedReader
import java.io.InputStreamReader

/**
 * 系统属性读取工具类
 * 提供安全的系统属性访问方法，避免权限警告
 */
object SystemPropertiesHelper {
    
    /**
     * 安全获取系统属性值
     * 使用反射或命令行方式获取属性，避免直接访问系统属性导致的权限警告
     */
    fun getSystemProperty(key: String, defaultValue: String = ""): String {
        return try {
            // 首先尝试使用反射调用 SystemProperties.get 方法
            val systemProperties = Class.forName("android.os.SystemProperties")
            val getMethod = systemProperties.getMethod("get", String::class.java, String::class.java)
            getMethod.invoke(null, key, defaultValue) as? String ?: defaultValue
        } catch (e: Exception) {
            // 如果反射失败，使用 getprop 命令行方式（Android 特定）
            getSystemPropertyViaCommand(key, defaultValue)
        }
    }
    
    /**
     * 通过命令行方式获取系统属性
     * 参考 FoldCraftLauncher 的实现方式
     */
    private fun getSystemPropertyViaCommand(key: String, defaultValue: String): String {
        return try {
            val process = Runtime.getRuntime().exec("getprop $key")
            val reader = BufferedReader(InputStreamReader(process.inputStream))
            val value = reader.readLine()
            reader.close()
            value?.takeIf { it.isNotEmpty() } ?: defaultValue
        } catch (e: Exception) {
            // Logger.lWarning("Failed to get system property via command: $key")
            defaultValue
        }
    }
    
    /**
     * 安全获取布尔类型的系统属性
     */
    fun getBooleanSystemProperty(key: String, defaultValue: Boolean): Boolean {
        val value = getSystemProperty(key, if (defaultValue) "1" else "0")
        return when (value.lowercase()) {
            "1", "true", "yes", "on" -> true
            "0", "false", "no", "off" -> false
            else -> defaultValue
        }
    }
    
    /**
     * 安全获取整型的系统属性
     */
    fun getIntSystemProperty(key: String, defaultValue: Int): Int {
        val value = getSystemProperty(key, defaultValue.toString())
        return try {
            value.toInt()
        } catch (e: NumberFormatException) {
            defaultValue
        }
    }
    
    /**
     * 检查是否为特定设备或系统
     */
    fun isSpecificDeviceOrSystem(key: String, value: String): Boolean {
        return getSystemProperty(key, "").lowercase().contains(value.lowercase())
    }
    
    /**
     * 获取当前设备信息
     */
    fun getDeviceInfo(): Map<String, String> {
        val deviceInfo = mutableMapOf<String, String>()
        
        // 安全获取常见设备信息
        deviceInfo["model"] = Build.MODEL
        deviceInfo["brand"] = Build.BRAND
        deviceInfo["manufacturer"] = Build.MANUFACTURER
        deviceInfo["version_release"] = Build.VERSION.RELEASE
        deviceInfo["version_sdk_int"] = Build.VERSION.SDK_INT.toString()
        
        // 通过系统属性获取额外信息（可能因权限限制无法获取）
        deviceInfo["soc_model"] = getSystemProperty("ro.soc.model", "")
        deviceInfo["hardware"] = getSystemProperty("ro.hardware", "")
        deviceInfo["product_board"] = getSystemProperty("ro.product.board", "")
        deviceInfo["product_brand"] = getSystemProperty("ro.product.brand", "")
        
        return deviceInfo
    }
}