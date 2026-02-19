/*
 * Shard Launcher
 * Adapted from Zalith Launcher 2
 */

package com.lanrhyme.shardlauncher.game.plugin.renderer

/**
 * Represents a renderer plugin
 */
data class RendererPlugin(
    val uniqueIdentifier: String,
    val name: String,
    val path: String,
    val glName: String,
    val eglName: String = "libEGL.so",
    val dlopen: List<String> = emptyList()
)
