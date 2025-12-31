/*
 * Shard Launcher
 * Adapted from Zalith Launcher 2
 */

package com.lanrhyme.shardlauncher.settings

/**
 * Comprehensive game settings matching ZalithLauncher
 * Includes renderer, game, and performance settings
 */
object AllSettings : SettingsRegistry() {
    
    // === Renderer Settings ===
    
    /**
     * Global renderer selection
     */
    val renderer = stringSetting("renderer", "")
    
    /**
     * Vulkan driver selection
     */
    val vulkanDriver = stringSetting("vulkanDriver", "default")
    
    /**
     * Resolution ratio (25-300%)
     */
    val resolutionRatio = intSetting("resolutionRatio", 100, 25..300)
    
    /**
     * Use system Vulkan driver in Zink
     */
    val zinkPreferSystemDriver = boolSetting("zinkPreferSystemDriver", false)
    
    /**
     * Enable VSync in Zink
     */
    val vsyncInZink = boolSetting("vsyncInZink", false)
    
    /**
     * Force game to run on big cores
     */
    val bigCoreAffinity = boolSetting("bigCoreAffinity", false)
    
    /**
     * Enable shader debugging/dump
     */
    val dumpShaders = boolSetting("dumpShaders", false)
    
    // === Game Settings ===
    
    /**
     * Enable version isolation (.minecraft/versions/{version})
     */
    val versionIsolation = boolSetting("versionIsolation", true)
    
    /**
     * Skip game integrity check
     */
    val skipGameIntegrityCheck = boolSetting("skipGameIntegrityCheck", false)

    /**
     * Version custom info
     */
    val versionCustomInfo = stringSetting("versionCustomInfo", "ShardLauncher[zl_version]")
    
    /**
     * Java runtime selection
     */
    val javaRuntime = stringSetting("javaRuntime", "")
    
    /**
     * Auto-pick appropriate Java runtime
     */
    val autoPickJavaRuntime = boolSetting("autoPickJavaRuntime", true)
    
    /**
     * RAM allocation in MB (-1 = use default)
     */
    val ramAllocation = intSetting("ramAllocation", 2048, min = 256)
    
    /**
     * Custom JVM arguments
     */
    val jvmArgs = stringSetting("jvmArgs", "")
    
    // === Game Display Settings ===
    
    /**
     * Game fullscreen mode
     */
    val gameFullScreen = boolSetting("gameFullScreen", true)
    
    /**
     * Sustained performance mode (keeps CPU at high frequency)
     */
    val sustainedPerformance = boolSetting("sustainedPerformance", false)
    
    /**
     * Automatically show log until game starts rendering
     */
    val showLogAutomatic = boolSetting("showLogAutomatic", false)
    
    /**
     * Log text size (5-20sp)
     */
    val logTextSize = intSetting("logTextSize", 15, 5..20)

    /**
     * Log buffer flush interval in ms
     */
    val logBufferFlushInterval = intSetting("logBufferFlushInterval", 200, 100..1000)

    /**
     * Mirror source for mod loaders
     */
    val fetchModLoaderSource = enumSetting("fetchModLoaderSource", com.lanrhyme.shardlauncher.settings.enums.MirrorSourceType.OFFICIAL_FIRST)

    /**
     * Mirror source for game files
     */
    val fileDownloadSource = enumSetting("fileDownloadSource", com.lanrhyme.shardlauncher.settings.enums.MirrorSourceType.OFFICIAL_FIRST)
}
