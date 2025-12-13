package com.lanrhyme.shardlauncher.ui.components

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
import androidx.compose.foundation.layout.padding
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

data class FluidFabItem(
    val label: String,
    val icon: ImageVector,
    val onClick: () -> Unit
)

@Composable
fun FluidFab(
    items: List<FluidFabItem>,
    modifier: Modifier = Modifier,
    direction: FluidFabDirection = FluidFabDirection.TOP,
    icon: ImageVector = Icons.Default.Add,
    containerColor: Color = MaterialTheme.colorScheme.primary,
    contentColor: Color = MaterialTheme.colorScheme.onPrimary,
    radius: Dp = 90.dp
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
                
                val offset = calculateOffset(direction, index, items.size, currentRadiusPx)
                
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
            // Animating Ring Circle (The "White Border Circle")
            // Sits behind toggle button, but above blobs? 
            // In reference it's top level in the stack (line 140), so above everything.
            
            items.forEachIndexed { index, item ->
                key(index) {
                    val staggerStart = (index.toFloat() / items.size) * 0.35f
                    val staggerEnd = staggerStart + 0.65f
                    val itemProgress = FastOutSlowInEasing.transform(staggerStart, staggerEnd, expandedProgress)
                    
                    if (itemProgress > 0.05f) {
                         val currentRadiusDp = radius * itemProgress
                         val density = LocalDensity.current
                         val currentRadiusPx = with(density) { currentRadiusDp.toPx() }
                         
                         val offset = calculateOffset(direction, index, items.size, currentRadiusPx)
                         
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
                             val labelOffset = calculateOffset(direction, index, items.size, labelRadiusPx)
    
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

private fun calculateOffset(
    direction: FluidFabDirection,
    index: Int,
    totalCount: Int,
    radiusPx: Float
): Pair<Float, Float> {
    if (totalCount == 0) return 0f to 0f
    
    val centerAngle = direction.angle
    val sectorSize = 133f // Increased field of view angle
    
    val startAngle = centerAngle - (sectorSize / 2)
    val step = if (totalCount > 1) sectorSize / (totalCount - 1) else 0f
    
    val currentAngle = if (totalCount > 1) startAngle + (step * index) else centerAngle
    
    val rad = currentAngle * (PI / 180.0)
    
    val x = radiusPx * cos(rad).toFloat()
    val y = radiusPx * sin(rad).toFloat()
    
    return x to y
}

fun Easing.transform(from: Float, to: Float, value: Float): Float {
    return transform(((value - from) * (1f / (to - from))).coerceIn(0f, 1f))
}
