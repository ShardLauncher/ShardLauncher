package com.lanrhyme.shardlauncher.ui.components.business

import android.graphics.ColorMatrix
import android.graphics.ColorMatrixColorFilter
import android.graphics.RenderEffect
import android.graphics.Shader
import android.os.Build
import androidx.annotation.RequiresApi
import androidx.compose.animation.core.Easing
import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.LinearEasing
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.key
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.draw.rotate
import androidx.compose.ui.draw.scale
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.asComposeRenderEffect
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.IntOffset
import androidx.compose.ui.unit.dp
import kotlin.math.PI
import kotlin.math.cos
import kotlin.math.roundToInt
import kotlin.math.sin

/**
 * FluidFab 展开方向枚举
 * 定义了 FAB 展开时子项分布的中心角度
 *
 * @property angle 角度值（度）
 */
enum class FluidFabDirection(val angle: Float) {
    TOP(270f),
    TOP_RIGHT(315f),
    RIGHT(0f),
    BOTTOM_RIGHT(45f),
    BOTTOM(90f),
    BOTTOM_LEFT(135f),
    LEFT(180f),
    TOP_LEFT(225f)
}

/**
 * FluidFab 中的单个项目数据类
 *
 * @property label 项目显示的标签文本
 * @property icon 项目显示的图标
 * @property onClick 点击项目时的回调操作
 */
data class FluidFabItem(
    val label: String,
    val icon: ImageVector,
    val onClick: () -> Unit
)

/**
 * 一个带有流体“粘稠”动画的浮动操作按钮 (FAB)，可展开以显示一组项目
 *
 * 这个可组合组件创建了一个视觉上引人入胜的 FAB，点击后会将其子项目以指定的方向和扇区动画化地展示出来
 * 该动画通过分层两个组件实现：
 * 1. 一个模糊的“流体”层，其中的斑点会合并和分离，从而产生粘稠效果
 * 2. 一个清晰的内容层，用于图标和交互，呈现在流体层之上
 *
 * 展开和项目动画是交错的，以获得更动态的感觉。脉动的圆环效果可提供点击反馈
 *
 * @param items 展开 FAB 时要显示的 [FluidFabItem] 列表。每个项目都定义了其图标、标签和点击操作
 * @param modifier 要应用于组件的 [Modifier]
 * @param direction 项目应展开的 [FluidFabDirection]。默认为 [FluidFabDirection.TOP]
 * @param icon 主 FAB 折叠时要显示的图标，默认为 `Icons.Default.Add`
 * @param containerColor FAB 和展开的项目斑点的背景颜色。默认为 `MaterialTheme.colorScheme.primary`
 * @param contentColor 主 FAB 和展开的项目上图标的颜色。默认为 `MaterialTheme.colorScheme.onPrimary`
 * @param radius 从 FAB 中心到展开的项目中心的距离，默认为 `90.dp`
 * @param sectorSize 项目分布的总角度（以度为单位），例如，对于 3 个项目和 90f 的 `sectorSize`，项目将分布在一个 90 度的圆弧上。默认为 `133f`
 */
@Composable
fun FluidFab(
    items: List<FluidFabItem>,
    modifier: Modifier = Modifier,
    direction: FluidFabDirection = FluidFabDirection.TOP,
    icon: ImageVector = Icons.Default.Add,
    containerColor: Color = MaterialTheme.colorScheme.primary,
    contentColor: Color = MaterialTheme.colorScheme.onPrimary,
    radius: Dp = 90.dp,
    sectorSize: Float = 133f
) {
    var isExpanded by remember { mutableStateOf(false) }

    // Fluid Animation progress
    val expandedProgress by animateFloatAsState(
        targetValue = if (isExpanded) 1f else 0f,
        animationSpec = tween(
            durationMillis = 1500, // Slower animation
            easing = LinearEasing
        ),
        label = "Expansion"
    )

    // Click/Circle Animation Progress
    val clickAnimationProgress by animateFloatAsState(
        targetValue = if (isExpanded) 1f else 0f,
        animationSpec = tween(
            durationMillis = 600, // Slower click animation
            easing = LinearEasing
        ),
        label = "ClickCircle"
    )

    // Render Effect
    val renderEffect = remember {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
            getBlurRenderEffect().asComposeRenderEffect()
        } else {
            null
        }
    }

    val fabSize = 56.dp

    Box(
        modifier = modifier,
        contentAlignment = Alignment.Center
    ) {
        // --------------------------------------------------------
        // 1. Fluid Layer: The "Goo" (Blobs only)
        // --------------------------------------------------------
        Box(
            modifier = Modifier
                .size(radius * 5)
                .graphicsLayer {
                    if (renderEffect != null) {
                        this.renderEffect = renderEffect
                        this.alpha = 0.99f
                    }
                },
            contentAlignment = Alignment.Center
        ) {
            // Main Center Blob (The Solid Background)
            // Logic: Gradually shrink from start to near end
            val centerBlobScale = 1f - LinearEasing.transform(0.1f, 0.9f, expandedProgress)

            Box(
                modifier = Modifier
                    .size(55.dp)
                    .scale(centerBlobScale)
                    .background(containerColor, CircleShape)
            )

            // Item Blobs
            items.forEachIndexed { index, _ ->
                val staggerStart = (index.toFloat() / items.size) * 0.35f
                val staggerEnd = staggerStart + 0.65f
                val itemProgress = FastOutSlowInEasing.transform(staggerStart, staggerEnd, expandedProgress)

                // Position
                val currentRadiusDp = radius * itemProgress
                val density = LocalDensity.current
                val currentRadiusPx = with(density) { currentRadiusDp.toPx() }

                val offset = calculateOffset(direction, index, items.size, currentRadiusPx, sectorSize)

                // Blob
                Box(
                    modifier = Modifier
                        .offset { IntOffset(offset.first.roundToInt(), offset.second.roundToInt()) }
                        .size(60.dp)
                        .background(containerColor, CircleShape)
                )
            }
        }

        // --------------------------------------------------------
        // 2. Content Layer: Icons and Interactions (Crisp)
        // --------------------------------------------------------
        Box(
            modifier = Modifier.size(radius * 5),
            contentAlignment = Alignment.Center
        ) {
            items.forEachIndexed { index, item ->
                key(index) {
                    val staggerStart = (index.toFloat() / items.size) * 0.35f
                    val staggerEnd = staggerStart + 0.65f
                    val itemProgress = FastOutSlowInEasing.transform(staggerStart, staggerEnd, expandedProgress)

                    if (itemProgress > 0.05f) {
                        val currentRadiusDp = radius * itemProgress
                        val density = LocalDensity.current
                        val currentRadiusPx = with(density) { currentRadiusDp.toPx() }

                        val offset = calculateOffset(direction, index, items.size, currentRadiusPx, sectorSize)

                        // Interactive Button (No Ripple)
                        Box(
                            contentAlignment = Alignment.Center,
                            modifier = Modifier
                                .offset { IntOffset(offset.first.roundToInt(), offset.second.roundToInt()) }
                                .size(fabSize)
                                .scale(LinearEasing.transform(0.5f, 1f, itemProgress))
                                .background(Color.Transparent, CircleShape)
                                .clickable(
                                    interactionSource = remember { MutableInteractionSource() },
                                    indication = null,
                                    onClick = {
                                        item.onClick()
                                        isExpanded = false
                                    }
                                )
                        ) {
                            Icon(
                                imageVector = item.icon,
                                contentDescription = item.label,
                                tint = contentColor.copy(alpha = LinearEasing.transform(0.5f, 1f, itemProgress)),
                                modifier = Modifier.scale(0.8f)
                            )
                        }

                        // Label
                        if (itemProgress > 0.85f) {
                            val labelAlpha = LinearEasing.transform(0.85f, 1f, itemProgress)

                            val labelRadiusDp = radius + 48.dp
                            val labelRadiusPx = with(density) { labelRadiusDp.toPx() }
                            val labelOffset = calculateOffset(direction, index, items.size, labelRadiusPx, sectorSize)

                            Text(
                                text = item.label,
                                style = MaterialTheme.typography.labelSmall,
                                color = MaterialTheme.colorScheme.onSurface,
                                modifier = Modifier
                                    .offset { IntOffset(labelOffset.first.roundToInt(), labelOffset.second.roundToInt()) }
                                    .alpha(labelAlpha),
                                textAlign = TextAlign.Center
                            )
                        }
                    }
                }
            }

            // White expanding border circle
            val borderAlpha = LinearEasing.transform(0.8f, 1f, expandedProgress)
            if (borderAlpha > 0f) {
                Box(
                    modifier = Modifier
                        .size(fabSize)
                        .border(
                            width = 2.dp,
                            color = Color.White.copy(alpha = 0.5f * borderAlpha),
                            shape = CircleShape
                        )
                )
            }

            // White Pulsing Circle (Reference Style) with 50% opacity
            AnimatedBorderCircle(
                color = Color.White.copy(alpha = 0.5f),
                animationProgress = clickAnimationProgress
            )

            // Main Toggle Button (Interactive)
            Box(
                contentAlignment = Alignment.Center,
                modifier = Modifier
                    .size(fabSize)
                    .background(Color.Transparent, CircleShape)
                    .clickable(
                        interactionSource = remember { MutableInteractionSource() },
                        indication = null, // Remove ripple
                        onClick = { isExpanded = !isExpanded }
                    )
            ) {
                Icon(
                    imageVector = icon,
                    contentDescription = if (isExpanded) "Collapse" else "Expand",
                    tint = contentColor,
                    modifier = Modifier.rotate(expandedProgress * 225f)
                )
            }
        }
    }
}

@Composable
fun AnimatedBorderCircle(color: Color, animationProgress: Float) {
    val animationValue = sin(PI * animationProgress).toFloat()

    // Scale goes 2 -> 1 -> 2 based on reference logic: scale(2 - animationValue)
    // At 0: sin(0)=0 -> scale(2)
    // At 0.5: sin(pi/2)=1 -> scale(1)
    // At 1: sin(pi)=0 -> scale(2)

    // Alpha: color.alpha * animationValue
    // At 0: 0
    // At 0.5: Max
    // At 1: 0

    if (animationValue > 0f) {
        Box(
            modifier = Modifier
                .size(56.dp)
                .scale(2f - animationValue)
                .border(
                    width = 2.dp,
                    color = color.copy(alpha = color.alpha * animationValue),
                    shape = CircleShape
                )
        )
    }
}

@RequiresApi(Build.VERSION_CODES.S)
private fun getBlurRenderEffect(): RenderEffect {
    val blurEffect = RenderEffect.createBlurEffect(80f, 80f, Shader.TileMode.DECAL)

    val alphaMatrix = RenderEffect.createColorFilterEffect(
        ColorMatrixColorFilter(
            ColorMatrix(
                floatArrayOf(
                    1f, 0f, 0f, 0f, 0f,
                    0f, 1f, 0f, 0f, 0f,
                    0f, 0f, 1f, 0f, 0f,
                    0f, 0f, 0f, 50f, -5000f
                )
            )
        )
    )

    return RenderEffect.createChainEffect(alphaMatrix, blurEffect)
}

/**
 * 计算子项位置偏移量
 *
 * @param direction 展开方向
 * @param index 子项索引
 * @param totalCount 子项总数
 * @param radiusPx 半径（像素）
 * @param sectorSize 扇区大小（度）
 * @return 坐标对 (x, y)
 */
private fun calculateOffset(
    direction: FluidFabDirection,
    index: Int,
    totalCount: Int,
    radiusPx: Float,
    sectorSize: Float
): Pair<Float, Float> {
    if (totalCount == 0) return 0f to 0f

    val centerAngle = direction.angle

    val startAngle = centerAngle - (sectorSize / 2)
    val step = if (totalCount > 1) sectorSize / (totalCount - 1) else 0f

    val currentAngle = if (totalCount > 1) startAngle + (step * index) else centerAngle

    val rad = currentAngle * (PI / 180.0)

    val x = radiusPx * cos(rad).toFloat()
    val y = radiusPx * sin(rad).toFloat()

    return x to y
}

/**
 * 根据给定的范围将值映射并应用缓动函数
 *
 * @param from 范围起始值
 * @param to 范围结束值
 * @param value 当前值
 * @return 应用缓动后的转换值
 */
fun Easing.transform(from: Float, to: Float, value: Float): Float {
    return transform(((value - from) * (1f / (to - from))).coerceIn(0f, 1f))
}
