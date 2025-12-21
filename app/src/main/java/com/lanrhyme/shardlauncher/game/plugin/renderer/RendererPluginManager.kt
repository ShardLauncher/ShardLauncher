/*
 * Shard Launcher
 */

package com.lanrhyme.shardlauncher.game.plugin.renderer

/**
 * Stub for Renderer plugin manager
 * Future implementation will support custom renderers (Zink, VirGL, etc.)
 */
object RendererPluginManager {
    val selectedRendererPlugin: RendererPlugin? = null
}

data class RendererPlugin(
    val name: String,
    val path: String
)
