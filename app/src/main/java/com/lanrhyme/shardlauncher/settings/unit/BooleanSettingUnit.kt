/*
 * Shard Launcher
 */

package com.lanrhyme.shardlauncher.settings.unit

/**
 * Setting unit for Boolean values
 */
class BooleanSettingUnit(
    key: String,
    defaultValue: Boolean
) : AbstractSettingUnit<Boolean>(key, defaultValue) {

    override fun readValue(): Boolean {
        return repository.getBoolean(key, defaultValue)
    }

    override fun writeValue(value: Boolean) {
        repository.setBoolean(key, value)
    }
}
