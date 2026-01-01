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

import android.content.Context
import android.os.Parcelable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import com.lanrhyme.shardlauncher.game.path.getGameHome
import com.lanrhyme.shardlauncher.game.path.getVersionsHome
import com.lanrhyme.shardlauncher.utils.string.isNotEmptyOrBlank
import kotlinx.parcelize.IgnoredOnParcel
import kotlinx.parcelize.Parcelize
import java.io.File
import kotlin.math.min

/**
 * Minecraft 版本，由版本名称进行区分
 * @param versionName 版本名称
 * @param versionConfig 独立版本的配置
 * @param versionInfo 版本信息
 * @param isValid 版本的有效性
 * @param versionType 版本的类型
 */
@Parcelize
class Version(
    private val versionName: String,
    private val versionConfig: VersionConfig,
    private val versionInfo: VersionInfo?,
    private val isValid: Boolean,
    val versionType: VersionType,
    /**
     * 控制是否将当前账号视为离线账号启动游戏
     */
    var offlineAccountLogin: Boolean = false,
    /**
     * 快速启动单人游戏（存档名），仅支持  1.20+  23w14a+
     */
    var quickPlaySingle: String? = null
): Parcelable {
    /**
     * 当前版本是否被置顶
     */
    @IgnoredOnParcel
    var pinnedState by mutableStateOf(versionConfig.pinned)
        private set

    /**
     * 设置版本的置顶状态并保存
     */
    fun setPinnedAndSave(value: Boolean) {
        this.versionConfig.setPinnedAndSave(value) { state ->
            this.pinnedState = state
        }
    }

    /**
     * @return 获取版本所属的版本文件夹
     */
    fun getVersionsFolder(): String = getVersionsHome()

    /**
     * @return 获取版本文件夹
     */
    fun getVersionPath(): File = File(getVersionsHome(), versionName)

    /**
     * @return 获取版本名称
     */
    fun getVersionName(): String = getVersionPath().name

    /**
     * @return 获取客户端 jar 文件
     */
    fun getClientJar(): File = File(getVersionPath(), "$versionName.jar")

    /**
     * @return 获取版本隔离配置
     */
    fun getVersionConfig() = versionConfig

    /**
     * @return 获取版本信息
     */
    fun getVersionInfo() = versionInfo

    /**
     * @return 版本描述是否可用
     */
    fun isSummaryValid(): Boolean {
        val summary = versionConfig.versionSummary
        return summary.isNotEmptyOrBlank()
    }

    /**
     * @return 获取版本描述
     */
    fun getVersionSummary(): String {
        if (!isValid()) throw IllegalStateException("The version is invalid!")
        return if (isSummaryValid()) versionConfig.versionSummary else versionInfo!!.getInfoString()
    }

    /**
     * @return 版本的有效性：是否存在版本JSON文件、版本文件夹是否存在
     */
    fun isValid() = isValid && getVersionPath().exists()

    /**
     * @return 是否开启了版本隔离
     */
    fun isIsolation() = versionConfig.isIsolation()

    /**
     * @return 是否跳过游戏完整性检查
     */
    fun skipGameIntegrityCheck() = versionConfig.skipGameIntegrityCheck()

    /**
     * @return 获取版本的游戏文件夹路径（若开启了版本隔离，则路径为版本文件夹）
     */
    fun getGameDir(): File {
        return if (versionConfig.isIsolation()) versionConfig.getVersionPath()
        //未开启版本隔离可以使用自定义路径，如果自定义路径为空（则为未设置），那么返回默认游戏路径（.minecraft/）
        else if (versionConfig.customPath.isNotEmpty()) File(versionConfig.customPath)
        else File(getGameHome())
    }

    private fun String.getValueOrDefault(default: String): String = this.takeIf { it.isNotEmpty() } ?: default

    // TODO: Implement settings
    fun getRenderer(): String = versionConfig.renderer.getValueOrDefault(com.lanrhyme.shardlauncher.settings.AllSettings.renderer.state)

    // TODO: Implement settings
    fun getDriver(): String = versionConfig.driver.getValueOrDefault(com.lanrhyme.shardlauncher.settings.AllSettings.vulkanDriver.state)

    // TODO: Implement settings and PathManager
//    fun getControlPath(): File? = versionConfig.control
//        .getValueOrDefault("") // AllSettings.controlLayout.getValue()
//        .takeIf { it.isNotEmpty() }
//        ?.let { fileName -> File(PathManager.DIR_CONTROL_LAYOUTS, fileName) }
    
    fun getJavaRuntime(): String = versionConfig.javaRuntime

    fun getJvmArgs(): String = versionConfig.jvmArgs

    fun getCustomInfo(): String = versionConfig.customInfo.getValueOrDefault(com.lanrhyme.shardlauncher.settings.AllSettings.versionCustomInfo.state)
        .replace("[zl_version]", com.lanrhyme.shardlauncher.BuildConfig.VERSION_NAME)

    fun getServerIp(): String? = versionConfig.serverIp.takeIf { it.isNotEmptyOrBlank() }

    // TODO: Implement settings
    fun getRamAllocation(context: Context? = null): Int = versionConfig.ramAllocation.takeIf { it >= 256 }?.let {
        if (context != null) {
            min(it, com.lanrhyme.shardlauncher.utils.platform.getMaxMemoryForSettings())
        } else {
            it
        }
    } ?: com.lanrhyme.shardlauncher.settings.AllSettings.ramAllocation.state

    fun isTouchProxyEnabled(): Boolean = versionConfig.enableTouchProxy

    fun getTouchVibrateDuration(): Int? = versionConfig.touchVibrateDuration.takeIf { it >= 80 }
}
