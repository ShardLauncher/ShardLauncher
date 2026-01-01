/*
 * Shard Launcher
 * Adapted from Zalith Launcher 2
 */

package com.lanrhyme.shardlauncher.game.input;

import androidx.annotation.Keep;
import dalvik.annotation.optimization.CriticalNative;

/**
 * Critical native test class for JNI optimization
 * Adapted from PojavLauncher
 */
@Keep
public class CriticalNativeTest {
    
    @Keep
    @CriticalNative
    public static native void testCriticalNative(int arg0, int arg1);
    
    @Keep
    public static void invokeTest() {
        // This method is called from native code to test critical native functionality
        testCriticalNative(0, 0);
    }
}