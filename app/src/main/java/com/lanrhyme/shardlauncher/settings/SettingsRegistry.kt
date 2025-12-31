/*
 * Shard Launcher
 * Adapted from Zalith Launcher 2
 */

package com.lanrhyme.shardlauncher.settings

import com.lanrhyme.shardlauncher.settings.unit.AbstractSettingUnit
import com.lanrhyme.shardlauncher.settings.unit.BooleanSettingUnit
import com.lanrhyme.shardlauncher.settings.unit.IntSettingUnit
import com.lanrhyme.shardlauncher.settings.unit.StringSettingUnit

/**
 * Base class for settings registry
 * Manages creation and initialization of setting units
 */
abstract class SettingsRegistry {
    protected val settingsList = mutableListOf<AbstractSettingUnit<*>>()

    protected fun boolSetting(key: String, def: Boolean) =
        BooleanSettingUnit(key, def).also { settingsList.add(it) }

    protected fun intSetting(key: String, def: Int, valueRange: IntRange = Int.MIN_VALUE..Int.MAX_VALUE) =
        IntSettingUnit(key, def, valueRange).also { settingsList.add(it) }

    protected fun intSetting(key: String, def: Int, min: Int = Int.MIN_VALUE, max: Int = Int.MAX_VALUE) =
        IntSettingUnit(key, def, min..max).also { settingsList.add(it) }

    protected fun stringSetting(key: String, def: String) =
        StringSettingUnit(key, def).also { settingsList.add(it) }

    protected inline fun <reified E : Enum<E>> enumSetting(key: String, def: E) =
        com.lanrhyme.shardlauncher.settings.unit.EnumSettingUnit(key, def, enumValues<E>()).also { settingsList.add(it) }

    fun initialize(repo: com.lanrhyme.shardlauncher.data.SettingsRepository) {
        settingsList.forEach { it.init(repo) }
    }
}
