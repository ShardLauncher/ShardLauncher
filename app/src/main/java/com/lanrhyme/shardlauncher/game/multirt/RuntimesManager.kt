/*
 * Shard Launcher
 * Adapted from Zalith Launcher 2
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

package com.lanrhyme.shardlauncher.game.multirt

import com.lanrhyme.shardlauncher.path.PathManager
import java.io.File

object RuntimesManager {
    fun getRuntimeHome(runtimeName: String): File {
        return File(PathManager.DIR_MULTIRT, runtimeName)
    }

    fun isJDK8(runtimePath: String): Boolean {
        // Check if the runtime is JDK 8 by looking for jre subdirectory
        val jreDir = File(runtimePath, "jre")
        return jreDir.exists() && jreDir.isDirectory
    }

    /**
     * Get runtime by name
     */
    fun getRuntime(name: String): Runtime {
        val runtimeHome = getRuntimeHome(name)
        val isJDK8 = isJDK8(runtimeHome.absolutePath)
        
        return Runtime(
            name = name,
            versionString = null,
            arch = System.getProperty("os.arch"),
            javaVersion = if (isJDK8) 8 else 17,
            isJDK8 = isJDK8
        )
    }

    /**
     * Get default runtime for Java version
     */
    fun getDefaultRuntime(javaVersion: Int): Runtime? {
        val runtimesDir = PathManager.DIR_MULTIRT
        runtimesDir.listFiles()?.let { runtimes ->
            for (runtime in runtimes) {
                val isJDK8 = isJDK8(runtime.absolutePath)
                val runtimeJavaVersion = if (isJDK8) 8 else 17
                
                if (runtimeJavaVersion >= javaVersion) {
                    return Runtime(
                        name = runtime.name,
                        versionString = null,
                        arch = System.getProperty("os.arch"),
                        javaVersion = runtimeJavaVersion,
                        isJDK8 = isJDK8
                    )
                }
            }
        }
        return null
    }
}
