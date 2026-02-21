/*
 * Shard Launcher
 * Adapted from Zalith Launcher 2
 */

package com.lanrhyme.shardlauncher.game.multirt

import android.content.Context
import android.net.Uri
import com.lanrhyme.shardlauncher.utils.asset.AssetExtractor
import com.lanrhyme.shardlauncher.utils.device.Architecture
import com.lanrhyme.shardlauncher.utils.logging.Logger
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.io.File
import java.io.InputStream

/**
 * Runtime installer for extracting bundled Java runtimes and importing external ones
 */
object RuntimeInstaller {
    
    /**
     * Available bundled runtime
     */
    data class BundledRuntime(
        val name: String,
        val displayName: String,
        val description: String,
        val javaVersion: Int,
        val architecture: String,
        val assetPath: String,
        val estimatedSize: Long = 0L
    )

    /**
     * Get available bundled runtimes
     */
    fun getBundledRuntimes(context: Context): List<BundledRuntime> {
        val runtimes = mutableListOf<BundledRuntime>()
        val deviceArch = Architecture.getDeviceArchitecture()
        
        // Check for bundled runtimes in assets
        val runtimeAssets = AssetExtractor.listAssets(context, "runtimes")
        
        for (runtimeDir in runtimeAssets) {
            when {
                runtimeDir == "jre-8" -> {
                    val archSuffix = getArchSuffix(deviceArch)
                    val binFile = "bin-$archSuffix.tar.xz"
                    
                    // Check if architecture-specific binary exists
                    if (AssetExtractor.assetExists(context, "runtimes/jre-8/$binFile")) {
                        runtimes.add(BundledRuntime(
                            name = "openjdk-8-${archSuffix}",
                            displayName = "OpenJDK 8 (${getArchDisplayName(deviceArch)})",
                            description = "适用于 ${getArchDisplayName(deviceArch)} 设备的 OpenJDK 8，兼容大部分 Minecraft 版本",
                            javaVersion = 8,
                            architecture = getArchString(deviceArch),
                            assetPath = "runtimes/jre-8",
                            estimatedSize = 45 * 1024 * 1024 // ~45MB
                        ))
                    }
                }
                runtimeDir == "jre-17" -> {
                    val archSuffix = getArchSuffix(deviceArch)
                    val binFile = "bin-$archSuffix.tar.xz"
                    
                    if (AssetExtractor.assetExists(context, "runtimes/jre-17/$binFile")) {
                        runtimes.add(BundledRuntime(
                            name = "openjdk-17-${archSuffix}",
                            displayName = "OpenJDK 17 (${getArchDisplayName(deviceArch)})",
                            description = "适用于 ${getArchDisplayName(deviceArch)} 设备的 OpenJDK 17，支持现代 Minecraft 版本",
                            javaVersion = 17,
                            architecture = getArchString(deviceArch),
                            assetPath = "runtimes/jre-17",
                            estimatedSize = 55 * 1024 * 1024 // ~55MB
                        ))
                    }
                }
                runtimeDir == "jre-21" -> {
                    val archSuffix = getArchSuffix(deviceArch)
                    val binFile = "bin-$archSuffix.tar.xz"
                    
                    if (AssetExtractor.assetExists(context, "runtimes/jre-21/$binFile")) {
                        runtimes.add(BundledRuntime(
                            name = "openjdk-21-${archSuffix}",
                            displayName = "OpenJDK 21 (${getArchDisplayName(deviceArch)})",
                            description = "适用于 ${getArchDisplayName(deviceArch)} 设备的 OpenJDK 21，最新长期支持版本",
                            javaVersion = 21,
                            architecture = getArchString(deviceArch),
                            assetPath = "runtimes/jre-21",
                            estimatedSize = 60 * 1024 * 1024 // ~60MB
                        ))
                    }
                }
            }
        }
        
        return runtimes
    }

    /**
     * Extract and install bundled runtime
     */
    suspend fun installBundledRuntime(
        context: Context,
        runtime: BundledRuntime,
        onProgress: (progress: Int, message: String) -> Unit = { _, _ -> }
    ): Result<Runtime> = withContext(Dispatchers.IO) {
        try {
            onProgress(0, "开始安装 ${runtime.displayName}...")
            
            // Check if runtime already exists
            val existingRuntimes = RuntimesManager.getRuntimes()
            if (existingRuntimes.any { it.name == runtime.name }) {
                return@withContext Result.failure(Exception("运行时 ${runtime.name} 已存在"))
            }
            
            val deviceArch = Architecture.getDeviceArchitecture()
            val archSuffix = getArchSuffix(deviceArch)
            
            // Extract universal files first
            onProgress(10, "提取通用文件...")
            val universalAssetPath = "${runtime.assetPath}/universal.tar.xz"
            val tempUniversalFile = File(context.cacheDir, "universal_${System.currentTimeMillis()}.tar.xz")
            
            AssetExtractor.extractAsset(
                context = context,
                assetPath = universalAssetPath,
                targetFile = tempUniversalFile,
                onProgress = { progress, message ->
                    onProgress(10 + progress / 4, message)
                }
            ).onFailure { error ->
                tempUniversalFile.delete()
                return@withContext Result.failure(error)
            }
            
            // Extract architecture-specific binary files
            onProgress(35, "提取架构特定文件...")
            val binAssetPath = "${runtime.assetPath}/bin-$archSuffix.tar.xz"
            val tempBinFile = File(context.cacheDir, "bin_${System.currentTimeMillis()}.tar.xz")
            
            AssetExtractor.extractAsset(
                context = context,
                assetPath = binAssetPath,
                targetFile = tempBinFile,
                onProgress = { progress, message ->
                    onProgress(35 + progress / 4, message)
                }
            ).onFailure { error ->
                tempUniversalFile.delete()
                tempBinFile.delete()
                return@withContext Result.failure(error)
            }
            
            val installedRuntime = tempUniversalFile.inputStream().use { universalStream ->
                tempBinFile.inputStream().use { binStream ->
                    RuntimesManager.installRuntimeBinPack(
                        universalFileInputStream = universalStream,
                        platformBinsInputStream = binStream,
                        name = runtime.name,
                        binPackVersion = runtime.name
                    ) { progress: Int, args: Array<Any> ->
                        onProgress(60, "安装文件... ${args.getOrNull(0) ?: ""}")
                    }
                }
            }
            
            // Clean up temp files
            tempUniversalFile.delete()
            tempBinFile.delete()
            
            onProgress(100, "安装完成")
            Logger.i("RuntimeInstaller", "Successfully installed bundled runtime: ${runtime.displayName}")
            Result.success(installedRuntime)
            
        } catch (e: Exception) {
            Logger.e("RuntimeInstaller", "Failed to install bundled runtime: ${runtime.displayName}", e)
            Result.failure(e)
        }
    }

    /**
     * Import runtime from external tar.xz file
     */
    suspend fun importRuntimeFromFile(
        context: Context,
        uri: Uri,
        runtimeName: String,
        onProgress: (progress: Int, message: String) -> Unit = { _, _ -> }
    ): Result<Runtime> = withContext(Dispatchers.IO) {
        try {
            onProgress(0, "开始导入运行时...")
            
            // Check if runtime already exists
            val existingRuntimes = RuntimesManager.getRuntimes()
            if (existingRuntimes.any { it.name == runtimeName }) {
                return@withContext Result.failure(Exception("运行时 $runtimeName 已存在"))
            }
            
            context.contentResolver.openInputStream(uri)?.use { inputStream ->
                onProgress(10, "正在读取文件...")
                
                val installedRuntime = RuntimesManager.installRuntime(
                    inputStream = inputStream,
                    name = runtimeName
                ) { progress: Int, args: Array<Any> ->
                    onProgress(10 + (progress * 90 / 100), "安装中... ${args.getOrNull(0) ?: ""}")
                }
                
                onProgress(100, "导入完成")
                Logger.i("RuntimeInstaller", "Successfully imported runtime from file: $runtimeName")
                Result.success(installedRuntime)
            } ?: Result.failure(Exception("无法读取文件"))
            
        } catch (e: Exception) {
            Logger.e("RuntimeInstaller", "Failed to import runtime from file: $runtimeName", e)
            Result.failure(e)
        }
    }

    /**
     * Install runtime from input stream (for backward compatibility)
     */
    suspend fun installRuntimeFromStream(
        inputStream: InputStream,
        name: String,
        onProgress: (progress: Int, message: String) -> Unit = { _, _ -> }
    ): Result<Runtime> = withContext(Dispatchers.IO) {
        try {
            onProgress(0, "开始安装运行时...")
            
            val installedRuntime = RuntimesManager.installRuntime(
                inputStream = inputStream,
                name = name
            ) { progress: Int, args: Array<Any> ->
                onProgress(progress, "安装中... ${args.getOrNull(0) ?: ""}")
            }
            
            onProgress(100, "安装完成")
            Result.success(installedRuntime)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    /**
     * Find runtime directory in extracted assets
     */
    private fun findRuntimeDirectory(baseDir: File): File? {
        // Look for common runtime directory patterns
        val candidates = listOf(
            baseDir,
            File(baseDir, "jre"),
            File(baseDir, "java"),
            File(baseDir, "runtime")
        )
        
        for (candidate in candidates) {
            if (candidate.exists()) {
                // Check if it's a tar.xz file
                if (candidate.isFile && candidate.name.endsWith(".tar.xz")) {
                    return candidate
                }
                
                // Check if it's a valid runtime directory
                if (candidate.isDirectory) {
                    val binDir = File(candidate, "bin")
                    val javaExe = File(binDir, "java")
                    if (binDir.exists() && javaExe.exists()) {
                        return candidate
                    }
                }
            }
        }
        
        // If not found, look recursively
        return baseDir.walkTopDown()
            .filter { it.isDirectory }
            .find { dir ->
                val binDir = File(dir, "bin")
                val javaExe = File(binDir, "java")
                binDir.exists() && javaExe.exists()
            } ?: baseDir.walkTopDown()
            .filter { it.isFile && it.name.endsWith(".tar.xz") }
            .firstOrNull()
    }

    /**
     * Check if runtime directory is compatible with device architecture
     */
    private fun isArchCompatible(runtimeDir: String, deviceArch: Int): Boolean {
        return when (deviceArch) {
            Architecture.ARCH_ARM64 -> runtimeDir.contains("arm64") || runtimeDir.contains("aarch64")
            Architecture.ARCH_ARM -> runtimeDir.contains("arm") && !runtimeDir.contains("arm64")
            Architecture.ARCH_X86_64 -> runtimeDir.contains("x86_64") || runtimeDir.contains("x64")
            Architecture.ARCH_X86 -> runtimeDir.contains("x86") && !runtimeDir.contains("x86_64")
            else -> false
        }
    }

    /**
     * Get architecture suffix for runtime name (ZalithLauncher format)
     */
    private fun getArchSuffix(deviceArch: Int): String {
        return when (deviceArch) {
            Architecture.ARCH_ARM64 -> "arm64"
            Architecture.ARCH_ARM -> "arm"
            Architecture.ARCH_X86_64 -> "x86_64"
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
