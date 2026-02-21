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

package com.lanrhyme.shardlauncher.game.renderer.renderers

import com.lanrhyme.shardlauncher.game.renderer.RendererInterface

object VulkanZinkRenderer : RendererInterface {
    override fun getRendererId(): String = "vulkan_zink"

    override fun getUniqueIdentifier(): String = "0fa435e2-46df-45c9-906c-b29606aaef00"

    override fun getRendererName(): String = "Vulkan Zink"

    override fun getRendererSummary(): String = "OpenGL 到 Vulkan 转换层，提供现代图形 API 性能"

    override fun getRendererEnv(): Lazy<Map<String, String>> = lazy {
        mapOf(
            "MESA_GL_VERSION_OVERRIDE" to "4.6",
            "MESA_GLSL_VERSION_OVERRIDE" to "460"
        )
    }

    override fun getDlopenLibrary(): Lazy<List<String>> = lazy { emptyList() }

    override fun getRendererLibrary(): String = "libOSMesa_8.so"
}