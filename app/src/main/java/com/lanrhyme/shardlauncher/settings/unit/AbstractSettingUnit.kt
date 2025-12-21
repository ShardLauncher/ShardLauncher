/*
 * Shard Launcher
 * Adapted from Zalith Launcher 2
 */

package com.lanrhyme.shardlauncher.settings.unit

import com.lanrhyme.shardlauncher.data.SettingsRepository

/**
 * Abstract base class for all setting units
 * Handles reading/writing values from/to SettingsRepository
 */
abstract class AbstractSettingUnit<T>(
    val key: String,
    val defaultValue: T
) {
    protected lateinit var repository: SettingsRepository
    private var initialized = false

    fun init(repo: SettingsRepository) {
        repository = repo
        initialized = true
    }

    fun getValue(): T {
        checkInitialized()
        return readValue()
    }

    fun setValue(value: T) {
        checkInitialized()
        writeValue(value)
    }

    protected abstract fun readValue(): T
    protected abstract fun writeValue(value: T)

    private fun checkInitialized() {
        if (!initialized) {
            throw IllegalStateException("Setting '$key' not initialized. Call init() first.")
        }
    }
}
