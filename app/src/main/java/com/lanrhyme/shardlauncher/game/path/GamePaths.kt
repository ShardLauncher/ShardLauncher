/*
 * Shard Launcher
 * Adapted from Zalith Launcher 2
 */

package com.lanrhyme.shardlauncher.game.path

import java.io.File

/**
 * Get game home directory
 */
fun getGameHome(): String {
    return GamePathManager.currentPath
}

/**
 * Get assets home directory
 */
fun getAssetsHome(): String {
    return File(getGameHome(), "assets").absolutePath
}

/**
 * Get libraries home directory
 */
fun getLibrariesHome(): String {
    return File(getGameHome(), "libraries").absolutePath
}

/**
 * Get versions home directory
 */
fun getVersionsHome(): String {
    return File(getGameHome(), "versions").absolutePath
}

/**
 * Get resources home directory
 */
fun getResourcesHome(): String {
    return File(getGameHome(), "resources").absolutePath
}