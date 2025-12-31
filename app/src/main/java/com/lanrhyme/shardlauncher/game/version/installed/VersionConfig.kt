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

import android.os.Parcel
import android.os.Parcelable
import com.google.gson.annotations.SerializedName
import com.lanrhyme.shardlauncher.R
import com.lanrhyme.shardlauncher.game.version.installed.VersionsManager.getZalithVersionPath
import com.lanrhyme.shardlauncher.utils.GSON
import com.lanrhyme.shardlauncher.utils.logging.Logger.lError
import com.lanrhyme.shardlauncher.utils.logging.Logger.lInfo
import com.lanrhyme.shardlauncher.utils.json.*
import com.lanrhyme.shardlauncher.utils.string.getStringNotNull
import java.io.File
import java.io.FileWriter

class VersionConfig(
    @Transient
    private var versionPath: File
) : Parcelable {
    @SerializedName("pinned")
    var pinned: Boolean = false
        private set

    fun setPinnedAndSave(
        value: Boolean,
        applyState: (Boolean) -> Unit
    ) {
        val oldValue = pinned
        applyState(value)

        try {
            this.pinned = value
            saveWithThrowable()
        } catch (e: Exception) {
            this.pinned = oldValue
            applyState(oldValue)
            throw e
        }
    }

    @SerializedName("isolationType")
    var isolationType: SettingState = SettingState.FOLLOW_GLOBAL
        get() = getSettingStateNotNull(field)
    @SerializedName("skipGameIntegrityCheck")
    var skipGameIntegrityCheck: SettingState = SettingState.FOLLOW_GLOBAL
        get() = getSettingStateNotNull(field)
    @SerializedName("javaRuntime")
    var javaRuntime: String = ""
        get() = getStringNotNull(field)
    @SerializedName("jvmArgs")
    var jvmArgs: String = ""
        get() = getStringNotNull(field)
    @SerializedName("renderer")
    var renderer: String = ""
        get() = getStringNotNull(field)
    @SerializedName("driver")
    var driver: String = ""
        get() = getStringNotNull(field)
    @SerializedName("control")
    var control: String = ""
        get() = getStringNotNull(field)
    @SerializedName("customPath")
    var customPath: String = ""
        get() = getStringNotNull(field)
    @SerializedName("customInfo")
    var customInfo: String = ""
        get() = getStringNotNull(field)
    @SerializedName("versionSummary")
    var versionSummary: String = ""
        get() = getStringNotNull(field)
    @SerializedName("serverIp")
    var serverIp: String = ""
        get() = getStringNotNull(field)
    @SerializedName("ramAllocation")
    var ramAllocation: Int = -1
    @SerializedName("enableTouchProxy")
    var enableTouchProxy: Boolean = false
    @SerializedName("touchVibrateDuration")
    var touchVibrateDuration: Int = 100

    constructor(
        filePath: File,
        isolationType: SettingState = SettingState.FOLLOW_GLOBAL,
        skipGameIntegrityCheck: SettingState = SettingState.FOLLOW_GLOBAL,
        javaRuntime: String = "",
        jvmArgs: String = "",
        renderer: String = "",
        driver: String = "",
        control: String = "",
        customPath: String = "",
        customInfo: String = "",
        versionSummary: String = "",
        serverIp: String = "",
        ramAllocation: Int = -1,
        enableTouchProxy: Boolean = false,
        touchVibrateDuration: Int = 100
    ) : this(filePath) {
        this.isolationType = isolationType
        this.skipGameIntegrityCheck = skipGameIntegrityCheck
        this.javaRuntime = javaRuntime
        this.jvmArgs = jvmArgs
        this.renderer = renderer
        this.driver = driver
        this.control = control
        this.customPath = customPath
        this.customInfo = customInfo
        this.versionSummary = versionSummary
        this.serverIp = serverIp
        this.ramAllocation = ramAllocation
        this.enableTouchProxy = enableTouchProxy
        this.touchVibrateDuration = touchVibrateDuration
    }

    fun copy(): VersionConfig = VersionConfig(
        versionPath,
        getSettingStateNotNull(isolationType),
        getSettingStateNotNull(skipGameIntegrityCheck),
        getStringNotNull(javaRuntime),
        getStringNotNull(jvmArgs),
        getStringNotNull(renderer),
        getStringNotNull(driver),
        getStringNotNull(control),
        getStringNotNull(customPath),
        getStringNotNull(customInfo),
        getStringNotNull(versionSummary),
        getStringNotNull(serverIp),
        ramAllocation,
        enableTouchProxy,
        touchVibrateDuration
    )

    fun save() {
        runCatching {
            saveWithThrowable()
        }.onFailure { e ->
            lError("An exception occurred while saving the version configuration.", e)
        }
    }

    @Throws(Throwable::class)
    fun saveWithThrowable() {
        val zalithVersionPath = getZalithVersionPath(versionPath)
        val configFile = File(zalithVersionPath, "version.config")
        if (!zalithVersionPath.exists()) zalithVersionPath.mkdirs()

        FileWriter(configFile, false).use {
            val json = GSON.toJson(this)
            it.write(json)
        }
        lInfo("Saved version configuration: $this")
    }

    fun getVersionPath() = versionPath

    fun setVersionPath(versionPath: File) {
        this.versionPath = versionPath
    }

    // TODO: Implement settings
    fun isIsolation(): Boolean = isolationType.toBoolean(com.lanrhyme.shardlauncher.settings.AllSettings.versionIsolation.state)

    // TODO: Implement settings
    fun skipGameIntegrityCheck(): Boolean = skipGameIntegrityCheck.toBoolean(com.lanrhyme.shardlauncher.settings.AllSettings.skipGameIntegrityCheck.state)

    private fun SettingState.toBoolean(global: Boolean) = when(getSettingStateNotNull(this)) {
        SettingState.FOLLOW_GLOBAL -> global
        SettingState.ENABLE -> true
        SettingState.DISABLE -> false
    }

    override fun describeContents(): Int = 0

    override fun writeToParcel(dest: Parcel, flags: Int) {
        dest.run {
            writeString(versionPath.absolutePath)
            writeInt(getSettingStateNotNull(isolationType).ordinal)
            writeInt(getSettingStateNotNull(skipGameIntegrityCheck).ordinal)
            writeString(getStringNotNull(javaRuntime))
            writeString(getStringNotNull(jvmArgs))
            writeString(getStringNotNull(renderer))
            writeString(getStringNotNull(driver))
            writeString(getStringNotNull(control))
            writeString(getStringNotNull(customPath))
            writeString(getStringNotNull(customInfo))
            writeString(getStringNotNull(versionSummary))
            writeString(getStringNotNull(serverIp))
            writeInt(ramAllocation)
            writeInt(if (enableTouchProxy) 1 else 0)
            writeInt(touchVibrateDuration)
        }
    }

    companion object CREATOR : Parcelable.Creator<VersionConfig> {
        override fun createFromParcel(parcel: Parcel): VersionConfig {
            val versionPath = File(parcel.readString().orEmpty())
            val isolationType = SettingState.entries.getOrNull(parcel.readInt()) ?: SettingState.FOLLOW_GLOBAL
            val skipGameIntegrityCheck = SettingState.entries.getOrNull(parcel.readInt()) ?: SettingState.FOLLOW_GLOBAL
            val javaRuntime = parcel.readString().orEmpty()
            val jvmArgs = parcel.readString().orEmpty()
            val renderer = parcel.readString().orEmpty()
            val driver = parcel.readString().orEmpty()
            val control = parcel.readString().orEmpty()
            val customPath = parcel.readString().orEmpty()
            val customInfo = parcel.readString().orEmpty()
            val versionSummary = parcel.readString().orEmpty()
            val serverIp = parcel.readString().orEmpty()
            val ramAllocation = parcel.readInt()
            val enableTouchProxy = parcel.readInt() == 1
            val touchVibrateDuration = parcel.readInt()

            return VersionConfig(
                versionPath,
                isolationType,
                skipGameIntegrityCheck,
                javaRuntime,
                jvmArgs,
                renderer,
                driver,
                control,
                customPath,
                customInfo,
                versionSummary,
                serverIp,
                ramAllocation,
                enableTouchProxy,
                touchVibrateDuration
            )
        }

        override fun newArray(size: Int): Array<VersionConfig?> {
            return arrayOfNulls(size)
        }

        fun parseConfig(versionPath: File): VersionConfig {
            val configFile = File(getZalithVersionPath(versionPath), "version.config")

            return runCatching getConfig@{
                when {
                    configFile.exists() -> {
                        //读取此文件的内容，并解析为VersionConfig
                        val config = GSON.fromJson(configFile.readText(), VersionConfig::class.java)
                        config.setVersionPath(versionPath)
                        config
                    }
                    else -> createNewConfig(versionPath)
                }
            }.onFailure {  e ->
                lError("An exception occurred while parsing the version configuration.", e)
            }.getOrElse {
                createNewConfig(versionPath)
            }
        }

        private fun createNewConfig(versionPath: File): VersionConfig {
            val config = VersionConfig(versionPath)
            return config.apply { save() }
        }

        fun createIsolation(versionPath: File): VersionConfig {
            val config = VersionConfig(versionPath)
            config.isolationType = SettingState.ENABLE
            return config
        }
    }
}

enum class SettingState(val textRes: Int) {
    FOLLOW_GLOBAL(R.string.generic_follow_global),
    ENABLE(R.string.generic_enable),
    DISABLE(R.string.generic_disable)
}

private fun getSettingStateNotNull(type: SettingState?) = type ?: SettingState.FOLLOW_GLOBAL
