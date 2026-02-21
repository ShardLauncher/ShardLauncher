/*
 * Shard Launcher
 * Adapted from Zalith Launcher 2
 * Copyright (C) 2025 MovTery <movtery228@qq.com> and contributors
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.
 * See the GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <https://www.gnu.org/licenses/gpl-3.0.txt>.
 */

package com.lanrhyme.shardlauncher.game.renderer

import android.content.Context
import com.lanrhyme.shardlauncher.game.renderer.renderers.*
import com.lanrhyme.shardlauncher.utils.device.Architecture
import com.lanrhyme.shardlauncher.utils.device.checkVulkanSupport
import com.lanrhyme.shardlauncher.utils.logging.Logger

/**
 * 启动器所有渲染器总管理者，启动器内置的渲染器与渲染器插件加载的渲染器，都会加载到这里
 */
object Renderers {
    private val renderers: MutableList<RendererInterface> = mutableListOf()
    private var currentRenderer: RendererInterface? = null
    private var isInitialized: Boolean = false

    fun init(reset: Boolean = false) {
        if (isInitialized && !reset) return
        isInitialized = true

        if (reset) {
            renderers.clear()
            currentRenderer = null
        }

        addRenderers(
            NGGL4ESRenderer,
            GL4ESRenderer,
            VulkanZinkRenderer,
            VirGLRenderer,
            FreedrenoRenderer,
            PanfrostRenderer
        )
    }

    fun getAllRenderers(): List<RendererInterface> {
        if (!isInitialized) init()
        return renderers.toList()
    }

    /**
     * 获取兼容当前设备的所有渲染器
     */
    fun getCompatibleRenderers(context: Context): List<RendererInterface> {
        if (!isInitialized) init()
        
        val deviceHasVulkan = checkVulkanSupport(context.packageManager)
        val deviceHasZinkBinary = !(Architecture.is32BitsDevice && Architecture.isx86Device())

        return renderers.filter { renderer ->
            when {
                renderer.getRendererId().contains("vulkan") && !deviceHasVulkan -> false
                renderer.getRendererId().contains("zink") && !deviceHasZinkBinary -> false
                else -> true
            }
        }
    }

    /**
     * 加入一些渲染器
     */
    @JvmStatic
    fun addRenderers(vararg renderers: RendererInterface) {
        renderers.forEach { renderer ->
            addRenderer(renderer)
        }
    }

    /**
     * 加入单个渲染器
     */
    @JvmStatic
    fun addRenderer(renderer: RendererInterface): Boolean {
        return if (this.renderers.any { it.getUniqueIdentifier() == renderer.getUniqueIdentifier() }) {
            Logger.w("Renderers", "The unique identifier of this renderer (${renderer.getRendererName()} - ${renderer.getUniqueIdentifier()}) conflicts with an already loaded renderer.")
            false
        } else {
            this.renderers.add(renderer)
            Logger.i("Renderers", "Renderer loaded: ${renderer.getRendererName()} (${renderer.getRendererId()} - ${renderer.getUniqueIdentifier()})")
            true
        }
    }

    fun findRendererByIdentifier(uniqueIdentifier: String): RendererInterface? {
        if (!isInitialized) init()
        return renderers.find { it.getUniqueIdentifier() == uniqueIdentifier }
    }

    /**
     * 设置当前的渲染器
     * @param context 用于初始化适配当前设备的渲染器
     * @param uniqueIdentifier 渲染器的唯一标识符，用于找到当前想要设置的渲染器
     * @param retryToFirstOnFailure 如果未找到匹配的渲染器，是否跳回渲染器列表的首个渲染器
     */
    fun setCurrentRenderer(context: Context, uniqueIdentifier: String, retryToFirstOnFailure: Boolean = true) {
        if (!isInitialized) init()
        val compatibleRenderers = getCompatibleRenderers(context)
        currentRenderer = compatibleRenderers.find { it.getUniqueIdentifier() == uniqueIdentifier } ?: run {
            if (retryToFirstOnFailure && compatibleRenderers.isNotEmpty()) {
                val renderer = compatibleRenderers[0]
                Logger.w("Renderers", "Incompatible renderer $uniqueIdentifier will be replaced with ${renderer.getUniqueIdentifier()} (${renderer.getRendererName()})")
                renderer
            } else null
        }
    }

    /**
     * 获取当前的渲染器
     */
    fun getCurrentRenderer(): RendererInterface {
        if (!isInitialized) init()
        return currentRenderer ?: throw IllegalStateException("Current renderer not set")
    }

    /**
     * 当前是否设置了渲染器
     */
    fun isCurrentRendererValid(): Boolean = isInitialized && currentRenderer != null
}