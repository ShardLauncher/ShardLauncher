/*
 * Shard Launcher
 * Adapted from Zalith Launcher 2
 */

package com.lanrhyme.shardlauncher.game.plugin.renderer

data class RendererPlugin(
    val id: String,
    val displayName: String,
    val summary: String? = null,
    val minMCVer: String? = null,
    val maxMCVer: String? = null,
    val uniqueIdentifier: String,
    val glName: String,
    val eglName: String = "libEGL.so",
    val path: String,
    val env: Map<String, String> = emptyMap(),
    val dlopen: List<String> = emptyList(),
    val packageName: String? = null
)
