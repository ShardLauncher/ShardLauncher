/*
 * Shard Launcher
 */

package com.lanrhyme.shardlauncher.game.plugin.renderer

import com.lanrhyme.shardlauncher.path.PathManager
import com.lanrhyme.shardlauncher.settings.AllSettings
import java.io.File

/**
 * Manages renderer plugins
 * Detects available renderers and handles selection
 */
object RendererPluginManager {
    
    private val renderers = mutableListOf<RendererPlugin>()
    
    /**
     * Currently selected renderer plugin
     */
    var selectedRendererPlugin: RendererPlugin? = null
        private set
    
    /**
     * Initialize and detect available renderers
     */
    fun initialize() {
        renderers.clear()
        
        // Always add default renderer
        renderers.add(RendererPlugin.createDefault())
        
        // Detect installed renderers
        detectZink()
        detectVirGL()
        detectGL4ES()
        
        // Select renderer based on settings
        selectFromSettings()
    }
    
    /**
     * Get list of all available renderers
     */
    fun getAvailableRenderers(): List<RendererPlugin> = renderers.toList()
    
    /**
     * Select a renderer by name
     */
    fun selectRenderer(name: String) {
        selectedRendererPlugin = renderers.firstOrNull { it.name == name }
            ?: RendererPlugin.createDefault()
        
        // Save to settings
        AllSettings.renderer.setValue(name)
    }
    
    /**
     * Get currently selected renderer or default
     */
    fun getSelectedRenderer(): RendererPlugin {
        return selectedRendererPlugin ?: RendererPlugin.createDefault()
    }
    
    private fun selectFromSettings() {
        val savedName = AllSettings.renderer.getValue()
        if (savedName.isNotEmpty()) {
            selectRenderer(savedName)
        } else {
            selectedRendererPlugin = RendererPlugin.createDefault()
        }
    }
    
    private fun detectZink() {
        // Zink renderer library path
        val zinkLibPath = File(PathManager.DIR_NATIVE_LIB, "libvulkan_zink.so")
        
        if (zinkLibPath.exists()) {
            renderers.add(
                RendererPlugin(
                    name = "zink",
                    type = RendererType.ZINK,
                    path = zinkLibPath.parent ?: "",
                    description = "Zink (Vulkan to OpenGL)",
                    requiresVulkan = true
                )
            )
        }
    }
    
    private fun detectVirGL() {
        // VirGL renderer library path
        val virglLibPath = File(PathManager.DIR_NATIVE_LIB, "libvirgl_test_server.so")
        
        if (virglLibPath.exists()) {
            renderers.add(
                RendererPlugin(
                    name = "virgl",
                    type = RendererType.VIRGL,
                    path = virglLibPath.parent ?: "",
                    description = "VirGL (GPU Virtualization)"
                )
            )
        }
    }
    
    private fun detectGL4ES() {
        // GL4ES renderer library path
        val gl4esLibPath = File(PathManager.DIR_NATIVE_LIB, "libgl4es.so")
        
        if (gl4esLibPath.exists()) {
            renderers.add(
                RendererPlugin(
                    name = "gl4es",
                    type = RendererType.GL4ES,
                    path = gl4esLibPath.parent ?: "",
                    description = "GL4ES (OpenGL 4.x to ES)"
                )
            )
        }
    }
}
