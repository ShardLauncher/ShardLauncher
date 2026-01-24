package com.lanrhyme.shardlauncher.ui.components.effect

import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.LinearEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.blur
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.unit.dp

/**
 * 背景光效组件，在屏幕背景上绘制两个缓慢移动和脉动的径向渐变光斑
 *
 * @param modifier 应用于组件的修饰符
 * @param themeColor 光斑的主题颜色
 * @param animationSpeed 动画速度系数
 */
@Composable
fun BackgroundLightEffect(
    modifier: Modifier = Modifier,
    themeColor: Color,
    animationSpeed: Float
) {
    BoxWithConstraints(modifier = modifier
        .fillMaxSize()
        .blur(100.dp)) {
        val width = constraints.maxWidth.toFloat()
        val height = constraints.maxHeight.toFloat()

        val infiniteTransition = rememberInfiniteTransition(label = "background_light_effect")

        val animSpeed = animationSpeed * animationSpeed

        val radius1 by infiniteTransition.animateFloat(
            initialValue = with(LocalDensity.current) { 200.dp.toPx() },
            targetValue = with(LocalDensity.current) { 250.dp.toPx() },
            animationSpec = infiniteRepeatable(
                animation = tween((10000 / animSpeed).toInt(), easing = FastOutSlowInEasing),
                repeatMode = RepeatMode.Reverse
            ),
            label = "radius1"
        )
        val x1 by infiniteTransition.animateFloat(
            initialValue = 0f,
            targetValue = width,
            animationSpec = infiniteRepeatable(
                animation = tween((12000 / animSpeed).toInt(), easing = LinearEasing),
                repeatMode = RepeatMode.Reverse
            ),
            label = "x1"
        )
        val y1 by infiniteTransition.animateFloat(
            initialValue = 0f,
            targetValue = height,
            animationSpec = infiniteRepeatable(
                animation = tween((15000 / animSpeed).toInt(), easing = FastOutSlowInEasing),
                repeatMode = RepeatMode.Reverse
            ),
            label = "y1"
        )

        val radius2 by infiniteTransition.animateFloat(
            initialValue = with(LocalDensity.current) { 300.dp.toPx() },
            targetValue = with(LocalDensity.current) { 375.dp.toPx() },
            animationSpec = infiniteRepeatable(
                animation = tween((13000 / animSpeed).toInt(), easing = FastOutSlowInEasing),
                repeatMode = RepeatMode.Reverse
            ),
            label = "radius2"
        )
        val x2 by infiniteTransition.animateFloat(
            initialValue = width,
            targetValue = 0f,
            animationSpec = infiniteRepeatable(
                animation = tween((18000 / animSpeed).toInt(), easing = LinearEasing),
                repeatMode = RepeatMode.Reverse
            ),
            label = "x2"
        )
        val y2 by infiniteTransition.animateFloat(
            initialValue = height,
            targetValue = 0f,
            animationSpec = infiniteRepeatable(
                animation = tween((20000 / animSpeed).toInt(), easing = FastOutSlowInEasing),
                repeatMode = RepeatMode.Reverse
            ),
            label = "y2"
        )

        val color1 = themeColor.copy(alpha = 0.5f)
        val color2 = themeColor.copy(alpha = 0.3f)

        Canvas(modifier = Modifier.fillMaxSize()) {
            drawCircle(
                brush = Brush.radialGradient(
                    colors = listOf(color1, Color.Transparent),
                    center = Offset(x1, y1),
                    radius = radius1
                ),
                radius = radius1,
                center = Offset(x1, y1)
            )
            drawCircle(
                brush = Brush.radialGradient(
                    colors = listOf(color2, Color.Transparent),
                    center = Offset(x2, y2),
                    radius = radius2
                ),
                radius = radius2,
                center = Offset(x2, y2)
            )
        }
    }
}