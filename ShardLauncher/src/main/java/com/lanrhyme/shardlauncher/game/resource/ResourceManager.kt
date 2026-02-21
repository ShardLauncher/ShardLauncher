/*
 * Shard Launcher
 * Resource manager using ZalithLauncherCore
 */

package com.lanrhyme.shardlauncher.game.resource

import android.content.Context
import com.movtery.zalithlauncher.game.multirt.RuntimesManager
import com.movtery.zalithlauncher.game.renderer.RenderersList
import com.lanrhyme.shardlauncher.utils.logging.Logger
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

object ResourceManager {
    
    data class ResourceCheckResult(
        val hasJavaRuntime: Boolean,
        val hasRenderers: Boolean,
        val missingResources: List<String>,
        val recommendedActions: List<String>
    )

    suspend fun checkResources(context: Context): ResourceCheckResult = withContext(Dispatchers.IO) {
        val missingResources = mutableListOf<String>()
        val recommendedActions = mutableListOf<String>()
        
        val runtimes = RuntimesManager.getRuntimes()
        val hasJavaRuntime = runtimes.isNotEmpty()
        
        if (!hasJavaRuntime) {
            missingResources.add("Java 运行时")
            recommendedActions.add("安装内置 Java 运行时环境")
        }
        
        val renderers = RenderersList.RENDERERS
        val hasRenderers = renderers.isNotEmpty()
        
        if (!hasRenderers) {
            missingResources.add("渲染器库")
            recommendedActions.add("渲染器库已内置，无需额外安装")
        }
        
        val hasJava8 = runtimes.any { it.javaVersion == 8 }
        if (hasJavaRuntime && !hasJava8) {
            recommendedActions.add("建议安装 Java 8 运行时以获得最佳兼容性")
        }
        
        val hasJava17 = runtimes.any { it.javaVersion >= 17 }
        if (hasJavaRuntime && !hasJava17) {
            recommendedActions.add("建议安装 Java 17+ 运行时以支持现代 Minecraft 版本")
        }
        
        ResourceCheckResult(
            hasJavaRuntime = hasJavaRuntime,
            hasRenderers = hasRenderers,
            missingResources = missingResources,
            recommendedActions = recommendedActions
        )
    }

    suspend fun installEssentialResources(
        context: Context,
        onProgress: (progress: Int, message: String) -> Unit = { _, _ -> }
    ): Result<Unit> = withContext(Dispatchers.IO) {
        try {
            onProgress(0, "检查内置资源...")
            
            val runtimes = RuntimesManager.getRuntimes()
            
            if (runtimes.isEmpty()) {
                onProgress(50, "未检测到 Java 运行时，请前往设置安装")
            } else {
                onProgress(100, "基础资源检查完成")
            }
            
            Logger.i("ResourceManager", "Resource check completed")
            Result.success(Unit)
            
        } catch (e: Exception) {
            Logger.e("ResourceManager", "Failed to check resources", e)
            Result.failure(e)
        }
    }

    fun getResourceStatusSummary(context: Context): String {
        val runtimes = RuntimesManager.getRuntimes()
        val renderers = RenderersList.RENDERERS
        
        return buildString {
            appendLine("=== 资源状态摘要 ===")
            appendLine("Java 运行时: ${runtimes.size} 个")
            runtimes.forEach { runtime ->
                appendLine("  - ${runtime.name} (Java ${runtime.javaVersion})")
            }
            appendLine("渲染器库: ${renderers.size} 个 (已内置)")
            renderers.forEach { renderer ->
                appendLine("  - ${renderer.name}")
            }
            
            if (runtimes.isEmpty()) {
                appendLine()
                appendLine("⚠️ 缺少 Java 运行时，请安装内置运行时")
            } else {
                appendLine()
                appendLine("✅ 所有必要资源已就绪")
            }
        }
    }
}
