/*
 * Shard Launcher
 */

package com.lanrhyme.shardlauncher.settings.unit

/**
 * Setting unit for String values
 */
class StringSettingUnit(
    key: String,
    defaultValue: String
) : AbstractSettingUnit<String>(key, defaultValue) {

    override fun readValue(): String {
        return repository.getString(key, defaultValue)
    }

    override fun writeValue(value: String) {
        repository.setString(key, value)
    }
}
