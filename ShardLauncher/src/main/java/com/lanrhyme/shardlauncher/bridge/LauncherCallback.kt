/*
 * Shard Launcher
 * Copyright (C) 2025 LanRhyme
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

package com.lanrhyme.shardlauncher.bridge

/**
 * Callback interface for launcher exit handling
 * Implemented by the Launcher class in the main module
 */
interface LauncherCallback {
    /**
     * Called when the JVM exits
     * @param exitCode The exit code from the JVM
     * @param isSignal Whether the exit was triggered by a signal
     */
    fun onJvmExit(exitCode: Int, isSignal: Boolean)
    
    /**
     * Called for cleanup when the JVM exits
     */
    fun onCleanup()
}
