/*
 * Zalith Launcher 2
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

package com.lanrhyme.shardlauncher.game.version.installed

import android.os.Parcelable
import com.lanrhyme.shardlauncher.game.addons.modloader.ModLoader
import kotlinx.parcelize.Parcelize

@Parcelize
class VersionInfo(
    val minecraftVersion: String,
    val quickPlay: QuickPlay,
    val loaderInfo: LoaderInfo?
): Parcelable {
    /**
     * 拼接Minecraft的版本信息，包括ModLoader信息
     * @return 用", "分割的信息字符串
     */
    fun getInfoString(): String {
        val infoList = mutableListOf<String>().apply {
            add(minecraftVersion)
            loaderInfo?.takeIf { it.version.isNotBlank() }?.let { info ->
                add("${info.loader.displayName} - ${info.version}")
            }
        }
        return infoList.joinToString(", ")
    }

    /**
     * [Reference PCL2](https://github.com/Hex-Dragon/PCL2/blob/dc611a982f8f97fab2c4275d1176db484f8549a4/Plain%20Craft%20Launcher%202/Modules/Minecraft/ModMinecraft.vb#L426-L438)
     */
    fun getMcVersionCode(): McVersionCode {
        return when {
            minecraftVersion.contains("w") || minecraftVersion.equals("pending", ignoreCase = true) -> {
                //快照或未发布版本，特殊处理
                McVersionCode(99, 99)
            }

            minecraftVersion.startsWith("1.") -> {
                val parts = minecraftVersion.split(" ", "_", "-", ".")
                val main = parts.getOrNull(1)?.takeIf { it.length <= 2 }?.toIntOrNull() ?: 0
                val sub = parts.getOrNull(2)?.takeIf { it.length <= 2 }?.toIntOrNull() ?: 0
                McVersionCode(main, sub)
            }

            else -> {
                McVersionCode(0, 0)
            }
        }
    }

    data class McVersionCode(
        val main: Int,
        val sub: Int
    )

    @Parcelize
    data class LoaderInfo(
        val loader: ModLoader,
        val version: String
    ): Parcelable {
        /**
         * 通过加载器名称，获得对应的环境变量键名
         */
        fun getLoaderEnvKey(): String? {
            return when(loader) {
                ModLoader.OPTIFINE -> "INST_OPTIFINE"
                ModLoader.FORGE -> "INST_FORGE"
                ModLoader.NEOFORGE -> "INST_NEOFORGE"
                ModLoader.FABRIC -> "INST_FABRIC"
                ModLoader.QUILT -> "INST_QUILT"
                ModLoader.LITE_LOADER -> "INST_LITELOADER"
                ModLoader.CLEANROOM -> "INST_CLEANROOM"
                else -> null
            }
        }
    }

    @Parcelize
    data class QuickPlay(
        val hasQuickPlaysSupport: Boolean,
        val isQuickPlaySingleplayer: Boolean,
        val isQuickPlayMultiplayer: Boolean
    ): Parcelable
}
