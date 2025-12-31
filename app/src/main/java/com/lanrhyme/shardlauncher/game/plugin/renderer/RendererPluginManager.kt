/*
 * Shard Launcher
 * Adapted from Zalith Launcher 2
 */

package com.lanrhyme.shardlauncher.game.plugin.renderer

import android.content.Context
import android.content.pm.ApplicationInfo
import com.lanrhyme.shardlauncher.game.renderer.Renderers

/**
 * Renderer plugin manager for handling external renderer plugins
 */
object RendererPluginManager {
    private val rendererPluginList: MutableList<RendererPlugin> = mutableListOf()

    /**
     * Get all loaded renderer plugins
     */
    @JvmStatic
    fun getRendererList(): List<RendererPlugin> = rendererPluginList

    /**
     * Remove specific renderer plugins
     */
    @JvmStatic
    fun removeRenderer(rendererPlugins: Collection<RendererPlugin>) {
        rendererPluginList.removeAll(rendererPlugins)
    }

    /**
     * Check if any renderer plugins are available
     */
    @JvmStatic
    fun isAvailable(): Boolean {
        return rendererPluginList.isNotEmpty()
    }

    /**
     * Get currently selected renderer plugin based on current renderer ID
     */
    @JvmStatic
    val selectedRendererPlugin: RendererPlugin?
        get() {
            val currentRenderer = runCatching {
                Renderers.getCurrentRenderer().getUniqueIdentifier()
            }.getOrNull()
            return rendererPluginList.find { it.uniqueIdentifier == currentRenderer }
        }

    /**
     * Clear all renderer plugins
     */
    fun clearPlugin() {
        rendererPluginList.clear()
    }

    /**
     * Check if renderer plugin is configurable
     */
    @JvmStatic
    fun isConfigurablePlugin(rendererUniqueIdentifier: String): Boolean {
        // Add configurable plugin package names here
        return false
    }

    /**
     * Parse renderer plugin from APK
     */
    fun parseApkPlugin(
        context: Context,
        info: ApplicationInfo,
        loaded: (RendererPlugin) -> Unit = {}
    ) {
        // Implementation for parsing APK-based renderer plugins
        // This would scan for renderer plugins in installed APKs
    }

    /**
     * Initialize renderer plugins
     */
    fun initializePlugins(context: Context) {
        clearPlugin()
        // Scan for installed renderer plugins
        // This is a simplified implementation
    }
}