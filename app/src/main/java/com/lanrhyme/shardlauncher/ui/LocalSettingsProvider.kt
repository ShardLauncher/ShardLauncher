package com.lanrhyme.shardlauncher.ui

import androidx.compose.runtime.staticCompositionLocalOf
import com.lanrhyme.shardlauncher.settings.AllSettings
import com.lanrhyme.shardlauncher.settings.enums.MirrorSourceType

class SettingsProvider {
    val fileDownloadSource: MirrorSourceType
        get() = AllSettings.fileDownloadSource.state
    
    val fetchModLoaderSource: MirrorSourceType
        get() = AllSettings.fetchModLoaderSource.state
}

val LocalSettingsProvider = staticCompositionLocalOf<SettingsProvider> { error("No SettingsProvider provided") }