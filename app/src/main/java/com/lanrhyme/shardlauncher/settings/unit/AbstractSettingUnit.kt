/*
 * Shard Launcher
 * Adapted from Zalith Launcher 2
 */

package com.lanrhyme.shardlauncher.settings.unit

import com.lanrhyme.shardlauncher.data.SettingsRepository
import androidx.compose.runtime.getValue
import androidx.compose.runtime.setValue

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

    var state by androidx.compose.runtime.mutableStateOf(defaultValue)
        private set

    fun init(repo: SettingsRepository) {
        repository = repo
        state = readValue()
        initialized = true
    }

    fun getValue(): T = state

    fun setValue(value: T) {
        checkInitialized()
        writeValue(value)
        state = value
    }

    protected abstract fun readValue(): T
    protected abstract fun writeValue(value: T)

    private fun checkInitialized() {
        if (!initialized) {
            throw IllegalStateException("Setting '$key' not initialized. Call init() first.")
        }
    }
}
