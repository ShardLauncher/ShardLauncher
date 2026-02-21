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

object PanfrostRenderer : RendererInterface {
    override fun getRendererId(): String = "gallium_panfrost"

    override fun getUniqueIdentifier(): String = "b8c9d0e1-f2a3-4b5c-6d7e-8f9a0b1c2d3e"

    override fun getRendererName(): String = "Panfrost (Mali)"

    override fun getRendererSummary(): String = "专为 ARM Mali GPU 优化的开源驱动"

    override fun getRendererEnv(): Lazy<Map<String, String>> = lazy { emptyMap() }

    override fun getDlopenLibrary(): Lazy<List<String>> = lazy { emptyList() }

    override fun getRendererLibrary(): String = "libOSMesa_8.so"
}