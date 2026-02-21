/*
 * Shard Launcher
 * Adapted from Zalith Launcher 2
 */

package com.lanrhyme.shardlauncher.game.renderer

/**
 * @param rendererIdentifier 渲染器唯一标识符
 * @param rendererNames 渲染器名称列表
 */
data class RenderersList(val rendererIdentifier: List<String>, val rendererNames: List<String>)