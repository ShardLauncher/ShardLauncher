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

import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import com.lanrhyme.shardlauncher.R
import com.lanrhyme.shardlauncher.game.path.getVersionsHome
import com.lanrhyme.shardlauncher.game.version.installed.utils.parseJsonToVersionInfo
import com.lanrhyme.shardlauncher.utils.logging.Logger.lDebug
import com.lanrhyme.shardlauncher.utils.logging.Logger.lError
import com.lanrhyme.shardlauncher.utils.logging.Logger.lInfo
import com.lanrhyme.shardlauncher.utils.logging.Logger.lWarning
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.sync.withLock
import org.apache.commons.io.FileUtils
import java.io.File

object VersionsManager {
    private val scope = CoroutineScope(Dispatchers.IO)
    private val mutex = Mutex()
    private val listeners: MutableList<suspend (List<Version>) -> Unit> = mutableListOf()

    private const val LAUNCHER_IDENTIFIER = ".shard" // TODO: Add to InfoDistributor or similar

    /**
     * 注册版本列表刷新监听器
     */
    fun registerListener(listener: suspend (List<Version>) -> Unit) {
        listeners.add(listener)
    }

    /**
     * 移除版本列表刷新监听器
     */
    fun unregisterListener(listener: suspend (List<Version>) -> Unit) {
        listeners.remove(listener)
    }

    /**
     * 当前所有的游戏版本
     */
    var versions: List<Version> = emptyList()
        private set

    /**
     * 当前的游戏信息
     */
    var currentGameInfo: CurrentGameInfo? = null
        private set

    private val _currentVersion = MutableStateFlow<Version?>(null)
    val currentVersion = _currentVersion.asStateFlow()

    private var currentJob: Job? = null

    private val _isRefreshing = MutableStateFlow(false)
    /** 是否正在刷新版本 */
    val isRefreshing = _isRefreshing.asStateFlow()

    /**
     * 检查版本是否已经存在
     */
    fun isVersionExists(versionName: String, checkJson: Boolean = false): Boolean {
        val folder = File(getVersionsHome(), versionName)
        //保证版本文件夹存在的同时，也应保证其版本json文件存在
        return if (checkJson) File(folder, "${folder.name}.json").exists()
        else folder.exists()
    }

    fun refresh(tag: String, trySetVersion: String? = null) {
        currentJob?.cancel()
        currentJob = scope.launch {
            mutex.withLock {
                _isRefreshing.value = true
                lDebug("Initiated by $tag: starting to refresh the version list.")

                if (trySetVersion != null) {
                    saveCurrentVersion(trySetVersion, refresh = false)
                    lDebug("Has attempted to save the current version: $trySetVersion")
                }

                versions = emptyList()

                val newVersions = mutableListOf<Version>()
                File(getVersionsHome()).listFiles()?.forEach { versionFile ->
                    runCatching {
                        processVersionFile(versionFile)
                    }.getOrNull()?.let {
                        newVersions.add(it)
                    }
                }

                versions = newVersions.toList()

                lDebug("Version list refreshed, refreshing the current version now.")
                refreshCurrentVersion()

                // Notify listeners
                runCatching {
                    listeners.forEach { listener ->
                        listener.invoke(versions)
                    }
                }.onFailure { e ->
                    lError("Failed to notify listeners", e)
                }

                _isRefreshing.value = false
            }
        }
    }

    private fun processVersionFile(versionFile: File): Version? {
        if (versionFile.exists() && versionFile.isDirectory) {
            var isVersion = false

            // 通过判断是否存在版本的.json文件，来确定其是否为一个版本
            val jsonFile = File(versionFile, "${versionFile.name}.json")
            val versionInfo = if (jsonFile.exists() && jsonFile.isFile) {
                parseJsonToVersionInfo(jsonFile)?.also {
                    isVersion = true
                }
            } else {
                null
            }

            val versionConfig = VersionConfig.parseConfig(versionFile)

            // 使用增强的版本验证
            val validation = VersionValidator.validateVersion(versionFile)
            if (!validation.isValid && validation.hasErrors) {
                lWarning("Version ${versionFile.name} validation failed: ${validation.issues.joinToString("; ") { it.message }}")
                // 仍然添加版本，但标记为无效
            }

            val version = Version(
                versionFile.name,
                versionConfig,
                versionInfo,
                validation.isValid,  // 使用验证结果
                versionInfo.getVersionType()
            )

            lInfo(
                "Identified and added version: ${version.getVersionName()}, " +
                        "Path: (${version.getVersionPath()}), " +
                        "Info: ${version.getVersionInfo()?.getInfoString()}, " +
                        "Status: ${VersionValidator.getStatusDescription(validation)}"
            )

            return version
        }
        return null
    }

    private fun refreshCurrentVersion() {
    // 确保 currentGameInfo 已初始化
    if (currentGameInfo == null) {
        currentGameInfo = refreshCurrentInfo()
    }

    val version = run {
        if (versions.isEmpty()) return@run null

        fun getVersionByFirst(): Version? {
            return versions.find { it.isValid() }?.apply {
                // 确保版本有效
                saveCurrentVersion(getVersionName(), refresh = false)
            }
        }

        runCatching {
            val versionString = currentGameInfo!!.version
            getVersion(versionString) ?: run {
                lDebug("Stored version $versionString not found, using the first available version instead.")
                getVersionByFirst()
            }
        }.onFailure { e ->
            lWarning("The current version information has not been initialized yet.", e)
        }.getOrElse {
            getVersionByFirst()
        }
    }.also { version ->
        lDebug("The current version is: ${version?.getVersionName()}")
    }

    _currentVersion.value = version
}

    private fun getVersion(name: String?): Version? {
        name?.let { versionName ->
            return versions.find { it.getVersionName() == versionName }?.takeIf { it.isValid() }
        }
        return null
    }

    /**
     * @return 通过版本名，判断其版本是否存在
     */
    fun checkVersionExistsByName(versionName: String?) =
        versionName?.let { name -> versions.any { it.getVersionName() == name } } ?: false

    /**
     * @return 获取 Zalith 启动器版本标识文件夹
     */
    fun getZalithVersionPath(version: Version) = File(version.getVersionPath(), LAUNCHER_IDENTIFIER)

    /**
     * @return 通过目录获取 Zalith 启动器版本标识文件夹
     */
    fun getZalithVersionPath(folder: File) = File(folder, LAUNCHER_IDENTIFIER)

    /**
     * @return 通过名称获取 Zalith 启动器版本标识文件夹
     */
    fun getZalithVersionPath(name: String) = File(getVersionPath(name), LAUNCHER_IDENTIFIER)

    /**
     * @return 获取当前版本设置的图标
     */
    fun getVersionIconFile(version: Version) = File(getZalithVersionPath(version), "VersionIcon.png")

    /**
     * @return 通过目录获取 Zalith 启动器版本标识文件夹
     */
    fun getVersionIconFile(folder: File) = File(getZalithVersionPath(folder), "VersionIcon.png")

    /**
     * @return 通过名称获取当前版本设置的图标
     */
    fun getVersionIconFile(name: String) = File(getZalithVersionPath(name), "VersionIcon.png")

    /**
     * @return 通过名称获取版本的文件夹路径
     */
    fun getVersionPath(name: String) = File(getVersionsHome(), name)

    /**
     * 保存当前选择的版本
     */
    fun saveCurrentVersion(versionName: String, refresh: Boolean = true) {
        try {
            // 确保 currentGameInfo 已初始化
            if (currentGameInfo == null) {
                currentGameInfo = refreshCurrentInfo()
            }

            currentGameInfo!!.apply {
                version = versionName
                saveCurrentInfo()
            }

            if (refresh) {
                lDebug("CurrentGameInfo saved, refreshing the current version now.")
                refreshCurrentVersion()
            }
        } catch (e: Exception) {
            lError("An exception occurred while saving the currently selected version information.", e)
        }
    }

    fun validateVersionName(
        newName: String,
        versionInfo: VersionInfo?
    ): String? {
        return when {
            isVersionExists(newName, true) -> {
                "版本已存在" // TODO: i18n
            }
            versionInfo?.loaderInfo?.let {
                //如果这个版本是有ModLoader加载器信息的，则不允许修改为与原版名称一致的名称，防止冲突
                newName == versionInfo.minecraftVersion
            } ?: false -> {
                "不能使用Minecraft原版名称" // TODO: i18n
            }
            else -> null
        }
    }

    /**
     * 重命名当前版本，但并不会在这里对即将重命名的名称，进行非法性判断
     */
    fun renameVersion(version: Version, name: String) {
        //如果当前的版本是即将被重命名的版本，那么就把将要重命名的名字设置为当前版本
        val saveToCurrent = version.getVersionName() == _currentVersion.value?.getVersionName()

        val versionFolder = version.getVersionPath()
        val renameFolder = File(getVersionsHome(), name)

        //不管重命名之后的文件夹是什么，只要这个文件夹存在，那么就必须删除
        //否则将出现问题
        FileUtils.deleteQuietly(renameFolder)

        val originalName = versionFolder.name

        versionFolder.renameTo(renameFolder)

        val versionJsonFile = File(renameFolder, "$originalName.json")
        val versionJarFile = File(renameFolder, "$originalName.jar")
        val renameJsonFile = File(renameFolder, "$name.json")
        val renameJarFile = File(renameFolder, "$name.jar")

        versionJsonFile.renameTo(renameJsonFile)
        versionJarFile.renameTo(renameJarFile)

        FileUtils.deleteQuietly(versionFolder)

        if (saveToCurrent) {
            //设置并刷新当前版本
            saveCurrentVersion(name)
        }

        refresh("VersionsManager.renameVersion")
    }

    /**
     * 将选中的版本复制为一个新的版本
     * @param version 选中的版本
     * @param name 新的版本的名称
     * @param copyAllFile 是否复制全部文件
     */
    fun copyVersion(version: Version, name: String, copyAllFile: Boolean) {
        val versionsFolder = version.getVersionsFolder()
        val newVersion = File(versionsFolder, name)

        val originalName = version.getVersionName()

        //新版本的json与jar文件
        val newJsonFile = File(newVersion, "$name.json")
        val newJarFile = File(newVersion, "$name.jar")

        val originalVersionFolder = version.getVersionPath()
        if (copyAllFile) {
            //启用复制所有文件时，直接将原文件夹整体复制到新版本
            FileUtils.copyDirectory(originalVersionFolder, newVersion)
            //重命名json、jar文件
            val jsonFile = File(newVersion, "$originalName.json")
            val jarFile = File(newVersion, "$originalName.jar")
            if (jsonFile.exists()) jsonFile.renameTo(newJsonFile)
            if (jarFile.exists()) jarFile.renameTo(newJarFile)
        } else {
            //不复制所有文件时，仅复制并重命名json、jar文件
            val originalJsonFile = File(originalVersionFolder, "$originalName.json")
            val originalJarFile = File(originalVersionFolder, "$originalName.jar")
            newVersion.mkdirs()
            // versions/1.21.3/1.21.3.json -> versions/name/name.json
            if (originalJsonFile.exists()) originalJsonFile.copyTo(newJsonFile)
            // versions/1.21.3/1.21.3.jar -> versions/name/name.jar
            if (originalJarFile.exists()) originalJarFile.copyTo(newJarFile)
        }

        //保存版本配置文件
        version.getVersionConfig().copy().let { config ->
            config.setVersionPath(newVersion)
            config.isolationType = SettingState.ENABLE
            config.saveWithThrowable()
        }

        refresh("VersionsManager.copyVersion")
    }

    /**
     * 删除版本
     */
    fun deleteVersion(version: Version) {
        FileUtils.deleteQuietly(version.getVersionPath())
        refresh("VersionsManager.deleteVersion")
    }
}
