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

import android.content.Context;
import androidx.annotation.Keep;
import com.lanrhyme.shardlauncher.utils.logging.Logger;

/**
 * Temporary ZLBridge implementation that doesn't rely on native libraries
 * This is a fallback implementation until native libraries are properly compiled
 */
@Keep
public final class ZLBridge {
    // AWT
    public static final int EVENT_TYPE_CHAR = 1000;
    public static final int EVENT_TYPE_CURSOR_POS = 1003;
    public static final int EVENT_TYPE_KEY = 1005;
    public static final int EVENT_TYPE_MOUSE_BUTTON = 1006;

    public static void sendKey(char keychar, int keycode) {
        // TODO: Android -> AWT keycode mapping
        sendInputData(EVENT_TYPE_KEY, (int) keychar, keycode, 1, 0);
        sendInputData(EVENT_TYPE_KEY, (int) keychar, keycode, 0, 0);
    }

    public static void sendKey(char keychar, int keycode, int state) {
        // TODO: Android -> AWT keycode mapping
        sendInputData(EVENT_TYPE_KEY, (int) keychar, keycode, state, 0);
    }

    public static void sendChar(char keychar) {
        sendInputData(EVENT_TYPE_CHAR, (int) keychar, 0, 0, 0);
    }

    public static void sendMousePress(int awtButtons, boolean isDown) {
        sendInputData(EVENT_TYPE_MOUSE_BUTTON, awtButtons, isDown ? 1 : 0, 0, 0);
    }

    public static void sendMousePress(int awtButtons) {
        sendMousePress(awtButtons, true);
        sendMousePress(awtButtons, false);
    }

    public static void sendMousePos(int x, int y) {
        sendInputData(EVENT_TYPE_CURSOR_POS, x, y, 0, 0);
    }

    // Game
    @Keep
    public static void initializeGameExitHook() {
        Logger.INSTANCE.lInfo("ZLBridge: initializeGameExitHook (stub)");
    }

    @Keep
    public static void setupExitMethod(Context context) {
        Logger.INSTANCE.lInfo("ZLBridge: setupExitMethod (stub)");
    }

    // Launch
    @Keep
    public static void setLdLibraryPath(String ldLibraryPath) {
        Logger.INSTANCE.lInfo("ZLBridge: setLdLibraryPath: " + ldLibraryPath);
        // TODO: Set LD_LIBRARY_PATH environment variable
    }

    @Keep
    public static boolean dlopen(String libPath) {
        Logger.INSTANCE.lInfo("ZLBridge: dlopen: " + libPath);
        // TODO: Load library dynamically
        return true; // Return true for now to avoid failures
    }

    // Render
    @Keep
    public static void setupBridgeWindow(Object surface) {
        Logger.INSTANCE.lInfo("ZLBridge: setupBridgeWindow (stub)");
    }

    @Keep
    public static void releaseBridgeWindow() {
        Logger.INSTANCE.lInfo("ZLBridge: releaseBridgeWindow (stub)");
    }

    @Keep
    public static void moveWindow(int xOffset, int yOffset) {
        Logger.INSTANCE.lInfo("ZLBridge: moveWindow: " + xOffset + ", " + yOffset);
    }

    @Keep
    public static int[] renderAWTScreenFrame() {
        // Return empty array for now
        return new int[0];
    }

    // Input
    @Keep
    public static void sendInputData(int type, int i1, int i2, int i3, int i4) {
        Logger.INSTANCE.lDebug("ZLBridge: sendInputData: " + type + ", " + i1 + ", " + i2 + ", " + i3 + ", " + i4);
    }

    @Keep
    public static void clipboardReceived(String data, String mimeTypeSub) {
        Logger.INSTANCE.lInfo("ZLBridge: clipboardReceived: " + data + ", " + mimeTypeSub);
    }

    // Utils
    @Keep
    public static int chdir(String path) {
        Logger.INSTANCE.lInfo("ZLBridge: chdir: " + path);
        // TODO: Change directory
        return 0; // Return success for now
    }

    // No static block to load native libraries - using stub implementation
}
