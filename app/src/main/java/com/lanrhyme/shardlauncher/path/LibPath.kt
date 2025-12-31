/*
 * Shard Launcher
 * Adapted from Zalith Launcher 2
 */

package com.lanrhyme.shardlauncher.path

import java.io.File

/**
 * Library paths for authentication and other components
 */
object LibPath {
    val AUTHLIB_INJECTOR: File by lazy {
        File(PathManager.DIR_COMPONENTS, "authlib-injector.jar")
    }
    
    val NIDE_8_AUTH: File by lazy {
        File(PathManager.DIR_COMPONENTS, "nide8auth.jar")
    }
    
    val JNA: File by lazy {
        PathManager.DIR_JNA
    }
}