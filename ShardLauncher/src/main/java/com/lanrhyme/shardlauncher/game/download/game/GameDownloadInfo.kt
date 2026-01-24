package com.lanrhyme.shardlauncher.game.download.game

import com.lanrhyme.shardlauncher.model.ModrinthVersion

/**
 * 游戏下载信息
 * @param gameVersion Minecraft 版本
 * @param customVersionName 自定义版本名称
 * @param fabric Fabric 版本
 * @param forge Forge 版本
 * @param neoForge NeoForge 版本
 * @param quilt Quilt 版本
 * @param fabricApi Fabric API 版本
 */
data class GameDownloadInfo(
    /** Minecraft 版本 */
    val gameVersion: String,
    /** 自定义版本名称 */
    val customVersionName: String,
    /** Fabric 版本 */
    val fabric: ModLoaderVersion? = null,
    /** Forge 版本 */
    val forge: ModLoaderVersion? = null,
    /** NeoForge 版本 */
    val neoForge: ModLoaderVersion? = null,
    /** Quilt 版本 */
    val quilt: ModLoaderVersion? = null,
    /** Fabric API 版本 */
    val fabricApi: ModrinthVersion? = null
)

/**
 * Mod Loader 版本信息基类
 */
open class ModLoaderVersion(
    open val version: String,
    open val loaderName: String
)

/**
 * Fabric 版本信息
 */
data class FabricVersion(
    override val version: String,
    override val loaderName: String = "Fabric"
) : ModLoaderVersion(version, loaderName)

/**
 * Forge 版本信息
 */
data class ForgeVersion(
    override val version: String,
    override val loaderName: String = "Forge",
    val installerPath: String? = null
) : ModLoaderVersion(version, loaderName)

/**
 * NeoForge 版本信息
 */
data class NeoForgeVersion(
    override val version: String,
    override val loaderName: String = "NeoForge",
    val installerPath: String? = null
) : ModLoaderVersion(version, loaderName)

/**
 * Quilt 版本信息
 */
data class QuiltVersion(
    override val version: String,
    override val loaderName: String = "Quilt"
) : ModLoaderVersion(version, loaderName)