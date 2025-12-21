/*
 * Shard Launcher
 * Adapted from Zalith Launcher 2
 */

package com.lanrhyme.shardlauncher.game.plugin.renderer

/**
 * Renderer types supported by the launcher
 */
enum class RendererType {
    DEFAULT,      // System OpenGL ES
    ZINK,         // Vulkan to OpenGL translation
    VIRGL,        // GPU virtualization
    GL4ES,        // OpenGL 4.x to ES translation
    ANGLE         // DirectX to OpenGL (future)
}

/**
 * Represents a renderer plugin
 */
data class RendererPlugin(
    val name: String,
    val type: RendererType,
    val path: String,
    val description: String,
    val requiresVulkan: Boolean = false
) {
    companion object {
        fun createDefault() = RendererPlugin(
            name = "default",
            type = RendererType.DEFAULT,
            path = "",
            description = "System OpenGL ES (default)"
        )
    }
}
