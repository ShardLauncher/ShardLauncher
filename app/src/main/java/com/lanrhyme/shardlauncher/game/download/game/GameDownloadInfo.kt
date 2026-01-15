package com.lanrhyme.shardlauncher.game.download.game

/**
 * 游戏下载信息
 * @param gameVersion Minecraft 版本
 * @param customVersionName 自定义版本名称
 * @param fabric Fabric 版本
 */
data class GameDownloadInfo(
    /** Minecraft 版本 */
    val gameVersion: String,
    /** 自定义版本名称 */
    val customVersionName: String,
    /** Fabric 版本 */
    val fabric: FabricVersion? = null
)

/**
 * Fabric 版本信息
 */
data class FabricVersion(
    val version: String,
    val loaderName: String = "Fabric"
)