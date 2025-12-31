/*
 * Shard Launcher
 * Adapted from Zalith Launcher 2
 */

package com.lanrhyme.shardlauncher.settings.unit

/**
 * Enum setting unit, saves enum name to SettingsRepository
 */
class EnumSettingUnit<E : Enum<E>>(
    key: String,
    defaultValue: E,
    private val values: Array<E>
) : AbstractSettingUnit<E>(key, defaultValue) {
    override fun readValue(): E {
        val name = repository.getString(key, defaultValue.name)
        return values.find { it.name == name } ?: defaultValue
    }

    override fun writeValue(value: E) {
        repository.setString(key, value.name)
    }
}
