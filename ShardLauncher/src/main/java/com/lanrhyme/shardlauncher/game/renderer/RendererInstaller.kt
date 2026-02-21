/*
 * Shard Launcher
 * Adapted from Zalith Launcher 2
 */

package com.lanrhyme.shardlauncher.game.renderer

import com.lanrhyme.shardlauncher.path.PathManager
import com.lanrhyme.shardlauncher.utils.device.Architecture
import com.lanrhyme.shardlauncher.utils.logging.Logger
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.io.File

/**
 * Renderer installer for managing bundled renderer libraries
 */
object RendererInstaller {
    
    /**
     * Available bundled renderer
     */
    data class BundledRenderer(
        val name: String,
        val displayName: String,
        val description: String,
        val rendererId: String,
        val architecture: String,
        val libraryName: String,
        val estimatedSize: Long = 0L
    )

    /**
     * Get available bundled renderers from jniLibs
     */
    fun getBundledRenderers(): List<BundledRenderer> {
        val renderers = mutableListOf<BundledRenderer>()
        val deviceArch = Architecture.getDeviceArchitecture()
        val archString = getArchString(deviceArch)
        
        // Define available renderers based on jniLibs content
        val rendererLibs = mapOf(
            "libgl4es_114.so" to Triple("GL4ES", "OpenGL ES 2.0 渲染器，兼容性最好", "opengles2"),
            "libOSMesa_2121.so" to Triple("VirGL", "软件渲染器，兼容性极佳", "gallium_virgl"),
            "libOSMesa_8.so" to Triple("Vulkan Zink", "基于 Vulkan 的高性能渲染器", "vulkan_zink"),
            "libOSMesa_2300d.so" to Triple("Panfrost", "专为 Mali GPU 优化的渲染器", "gallium_panfrost")
        )
        
        for ((libName, info) in rendererLibs) {
            val (displayName, description, rendererId) = info
            
            renderers.add(BundledRenderer(
                name = "${rendererId}-${getArchSuffix(deviceArch)}",
                displayName = "$displayName (${getArchDisplayName(deviceArch)})",
                description = description,
                rendererId = rendererId,
                architecture = archString,
                libraryName = libName,
                estimatedSize = 2 * 1024 * 1024 // ~2MB estimate
            ))
        }
        
        return renderers
    }

    /**
     * Get compatible renderers for current device
     */
    fun getCompatibleRenderers(): List<BundledRenderer> {
        return getBundledRenderers() // All bundled renderers are compatible
    }

    /**
     * Check if renderer library is available in jniLibs
     */
    fun isRendererAvailable(libraryName: String): Boolean {
        // Since renderers are bundled in jniLibs, they're always available
        return true
    }

    /**
     * Get installed renderers (from jniLibs - they're always "installed")
     */
    fun getInstalledRenderers(): List<File> {
        val deviceArch = Architecture.getDeviceArchitecture()
        val archString = getArchString(deviceArch)
        
        // Return list of available renderer libraries
        val rendererLibs = listOf(
            "libgl4es_114.so",
            "libOSMesa_2121.so", 
            "libOSMesa_8.so",
            "libOSMesa_2300d.so"
        )
        
        return rendererLibs.map { libName ->
            File("jniLibs/$archString/$libName")
        }
    }

    /**
     * Install renderer (no-op since they're bundled in jniLibs)
     */
    suspend fun installBundledRenderer(
        renderer: BundledRenderer,
        onProgress: (progress: Int, message: String) -> Unit = { _, _ -> }
    ): Result<File> = withContext(Dispatchers.IO) {
        try {
            onProgress(0, "检查 ${renderer.displayName}...")
            
            if (!isRendererAvailable(renderer.libraryName)) {
                return@withContext Result.failure(Exception("渲染器库 ${renderer.libraryName} 不可用"))
            }
            
            onProgress(50, "渲染器已内置...")
            
            // Create a reference file
            val deviceArch = Architecture.getDeviceArchitecture()
            val archString = getArchString(deviceArch)
            val referenceFile = File("jniLibs/$archString/${renderer.libraryName}")
            
            onProgress(100, "渲染器可用")
            Logger.i("RendererInstaller", "Renderer available: ${renderer.displayName}")
            Result.success(referenceFile)
            
        } catch (e: Exception) {
            Logger.e("RendererInstaller", "Failed to check renderer: ${renderer.displayName}", e)
            Result.failure(e)
        }
    }

    /**
     * Remove renderer (no-op since they're bundled)
     */
    fun removeRenderer(libraryName: String): Boolean {
        Logger.w("RendererInstaller", "Cannot remove bundled renderer: $libraryName")
        return false
    }

    /**
     * Check if renderer is installed (always true for bundled renderers)
     */
    fun isRendererInstalled(libraryName: String): Boolean {
        return isRendererAvailable(libraryName)
    }

    /**
     * Get architecture suffix
     */
    private fun getArchSuffix(deviceArch: Int): String {
        return when (deviceArch) {
            Architecture.ARCH_ARM64 -> "arm64"
            Architecture.ARCH_ARM -> "arm32"
            Architecture.ARCH_X86_64 -> "x64"
            Architecture.ARCH_X86 -> "x86"
            else -> "unknown"
        }
    }

    /**
     * Get architecture display name
     */
    private fun getArchDisplayName(deviceArch: Int): String {
        return when (deviceArch) {
            Architecture.ARCH_ARM64 -> "ARM64"
            Architecture.ARCH_ARM -> "ARM32"
            Architecture.ARCH_X86_64 -> "x86_64"
            Architecture.ARCH_X86 -> "x86"
            else -> "Unknown"
        }
    }

    /**
     * Get architecture string
     */
    private fun getArchString(deviceArch: Int): String {
        return when (deviceArch) {
            Architecture.ARCH_ARM64 -> "arm64-v8a"
            Architecture.ARCH_ARM -> "armeabi-v7a"
            Architecture.ARCH_X86_64 -> "x86_64"
            Architecture.ARCH_X86 -> "x86"
            else -> "unknown"
        }
    }
}