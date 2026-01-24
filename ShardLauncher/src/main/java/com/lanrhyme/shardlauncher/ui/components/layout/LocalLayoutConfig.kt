package com.lanrhyme.shardlauncher.ui.components.layout

import androidx.compose.runtime.compositionLocalOf
import dev.chrisbanes.haze.HazeState

data class CardLayoutConfig(
        val isCardBlurEnabled: Boolean = false,
        val cardAlpha: Float = 1f,
        val hazeState: HazeState = HazeState()
)

/**
 * 提供卡片布局配置的 CompositionLocal
 */
val LocalCardLayoutConfig = compositionLocalOf { CardLayoutConfig() }
