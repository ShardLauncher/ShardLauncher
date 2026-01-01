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

package com.lanrhyme.shardlauncher.bridge;

import androidx.annotation.Keep;
import com.lanrhyme.shardlauncher.utils.logging.Logger;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;

/**
 * Temporary LoggerBridge implementation that doesn't rely on native libraries
 * This is a fallback implementation until native libraries are properly compiled
 */
@Keep
public final class LoggerBridge {
    private static File logFile;
    private static EventLogListener listener;
    
    /** Reset the log file, effectively erasing any previous logs */
    @Keep
    public static void start(String filePath) {
        try {
            logFile = new File(filePath);
            logFile.getParentFile().mkdirs();
            if (logFile.exists()) {
                logFile.delete();
            }
            logFile.createNewFile();
            Logger.INSTANCE.lInfo("LoggerBridge started with file: " + filePath);
        } catch (IOException e) {
            Logger.INSTANCE.lError("Failed to start LoggerBridge", e);
        }
    }

    /** Print the text to the log file if not censored */
    @Keep
    public static void append(String log) {
        try {
            // Write to file if available
            if (logFile != null && logFile.exists()) {
                try (FileWriter writer = new FileWriter(logFile, true)) {
                    writer.write(log + "\n");
                    writer.flush();
                }
            }
            
            // Also log to Android log
            Logger.INSTANCE.lInfo("[Bridge] " + log);
            
            // Notify listener if available
            if (listener != null) {
                listener.onEventLogged(log);
            }
        } catch (IOException e) {
            Logger.INSTANCE.lError("Failed to append to log", e);
        }
    }

    /** Link a log listener to the logger */
    @Keep
    public static void setListener(EventLogListener listener) {
        LoggerBridge.listener = listener;
    }

    /** Small listener for anything listening to the log */
    @Keep
    public interface EventLogListener {
        @Keep
        void onEventLogged(String text);
    }

    public static void appendTitle(String title) {
        String logText = "==================== " + title + " ====================";
        append(logText);
    }
}
