/*
 * Shard Launcher
 */

package com.lanrhyme.shardlauncher.settings.unit

val DEFAULT_INT_RANGE = Int.MIN_VALUE..Int.MAX_VALUE

/**
 * Setting unit for Int values with optional range validation
 */
class IntSettingUnit(
    key: String,
    defaultValue: Int,
    private val valueRange: IntRange = DEFAULT_INT_RANGE
) : AbstractSettingUnit<Int>(key, defaultValue) {

    override fun readValue(): Int {
        val value = repository.getInt(key, defaultValue)
        return value.coerceIn(valueRange)
    }

    override fun writeValue(value: Int) {
        val coerced = value.coerceIn(valueRange)
        repository.setInt(key, coerced)
    }
}
