package com.lanrhyme.shardlauncher.ui.components.basic

import android.os.Build
import androidx.compose.animation.core.Spring
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.spring
import androidx.compose.animation.core.tween
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.interaction.collectIsPressedAsState
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.RowScope
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.defaultMinSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonColors
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.scale
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Shape
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import com.lanrhyme.shardlauncher.ui.components.layout.LocalCardLayoutConfig
import dev.chrisbanes.haze.hazeEffect

/**
 * 按钮类型枚举
 */
enum class ButtonType {
    FILLED,         // 填充样式
    OUTLINED,       // 描边样式
    TEXT,           // 文字样式
    GRADIENT,       // 渐变背景
    GLASS           // 毛玻璃效果
}

/**
 * 按钮尺寸枚举
 */
enum class ButtonSize {
    SMALL,          // 小型按钮
    MEDIUM,         // 中型按钮（默认）
    LARGE           // 大型按钮
}

/**
 * 统一的按钮组件 - ShardButton
 *
 * 支持多种样式、尺寸和效果：
 * - 填充/描边/文字三种基础样式
 * - 渐变背景样式
 * - 毛玻璃效果样式（Android 12+）
 * - 内置缩放动画反馈
 *
 * @param onClick 点击回调
 * @param modifier 修饰符
 * @param type 按钮类型，参考 [ButtonType]
 * @param size 按钮尺寸，参考 [ButtonSize]
 * @param enabled 是否启用
 * @param shape 按钮形状
 * @param colors 自定义按钮颜色（可选）
 * @param contentPadding 内容内边距
 * @param interactionSource 交互源
 * @param content 按钮内容
 */
@Composable
fun ShardButton(
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    type: ButtonType = ButtonType.FILLED,
    size: ButtonSize = ButtonSize.MEDIUM,
    enabled: Boolean = true,
    shape: Shape = RoundedCornerShape(16.dp),
    colors: ButtonColors? = null,
    contentPadding: PaddingValues? = null,
    interactionSource: MutableInteractionSource = remember { MutableInteractionSource() },
    content: @Composable RowScope.() -> Unit
) {
    val (isCardBlurEnabled, _, hazeState) = LocalCardLayoutConfig.current
    val isPressed by interactionSource.collectIsPressedAsState()

    val scale by animateFloatAsState(
        targetValue = if (isPressed) 0.96f else 1f,
        animationSpec = spring(stiffness = Spring.StiffnessMediumLow),
        label = "button_scale"
    )

    // 尺寸配置
    val (defaultPadding, minHeight) = when (size) {
        ButtonSize.SMALL -> PaddingValues(horizontal = 12.dp, vertical = 6.dp) to 32.dp
        ButtonSize.MEDIUM -> PaddingValues(horizontal = 24.dp, vertical = 8.dp) to 44.dp
        ButtonSize.LARGE -> PaddingValues(horizontal = 32.dp, vertical = 12.dp) to 56.dp
    }
    val padding = contentPadding ?: defaultPadding

    // 基础修饰符，处理缩放和最小高度
    val baseModifier = modifier
        .scale(scale)
        .defaultMinSize(minHeight = minHeight)

    // 应用 Haze 效果（如果启用）
    val contentModifier = if (isCardBlurEnabled && Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
        baseModifier.clip(shape).hazeEffect(state = hazeState)
    } else baseModifier

    // 根据类型渲染
    when (type) {
        ButtonType.GRADIENT -> {
            GradientButton(
                onClick = onClick,
                modifier = contentModifier,
                enabled = enabled,
                shape = shape,
                contentPadding = padding,
                interactionSource = interactionSource,
                content = content
            )
        }
        ButtonType.GLASS -> {
            GlassButton(
                onClick = onClick,
                modifier = contentModifier,
                enabled = enabled,
                shape = shape,
                contentPadding = padding,
                interactionSource = interactionSource,
                content = content
            )
        }
        ButtonType.FILLED -> {
            val defaultColors = if (isCardBlurEnabled) {
                ButtonDefaults.buttonColors(
                    containerColor = MaterialTheme.colorScheme.primary.copy(alpha = 0.85f),
                    contentColor = MaterialTheme.colorScheme.onPrimary
                )
            } else ButtonDefaults.buttonColors()

            Button(
                onClick = onClick,
                modifier = contentModifier,
                enabled = enabled,
                shape = shape,
                colors = colors ?: defaultColors,
                contentPadding = padding,
                interactionSource = interactionSource,
                content = content
            )
        }
        ButtonType.OUTLINED -> {
            OutlinedButton(
                onClick = onClick,
                modifier = contentModifier,
                enabled = enabled,
                shape = shape,
                colors = colors ?: ButtonDefaults.outlinedButtonColors(),
                contentPadding = padding,
                interactionSource = interactionSource,
                content = content
            )
        }
        ButtonType.TEXT -> {
            TextButton(
                onClick = onClick,
                modifier = contentModifier,
                enabled = enabled,
                shape = shape,
                colors = colors ?: ButtonDefaults.textButtonColors(),
                contentPadding = padding,
                interactionSource = interactionSource,
                content = content
            )
        }
    }
}

/**
 * 渐变按钮实现
 */
@Composable
private fun GradientButton(
    onClick: () -> Unit,
    modifier: Modifier,
    enabled: Boolean,
    shape: Shape,
    contentPadding: PaddingValues,
    interactionSource: MutableInteractionSource,
    content: @Composable RowScope.() -> Unit
) {
    val isPressed by interactionSource.collectIsPressedAsState()
    
    Surface(
        onClick = onClick,
        modifier = modifier.glow(
            color = MaterialTheme.colorScheme.primary,
            cornerRadius = 16.dp, // 默认对应 RoundedCornerShape(16.dp)
            blurRadius = if (isPressed) 16.dp else 0.dp,
            enabled = true
        ),
        enabled = enabled,
        shape = shape,
        color = Color.Transparent,
        contentColor = MaterialTheme.colorScheme.onPrimary,
        interactionSource = interactionSource
    ) {
        Box(
            modifier = Modifier
                .background(
                    brush = Brush.linearGradient(
                        colors = listOf(
                            MaterialTheme.colorScheme.primary,
                            MaterialTheme.colorScheme.secondary
                        )
                    ),
                    shape = shape
                )
                .padding(contentPadding),
            contentAlignment = Alignment.Center
        ) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.Center
            ) {
                content()
            }
        }
    }
}

/**
 * 毛玻璃按钮实现
 */
@Composable
private fun GlassButton(
    onClick: () -> Unit,
    modifier: Modifier,
    enabled: Boolean,
    shape: Shape,
    contentPadding: PaddingValues,
    interactionSource: MutableInteractionSource,
    content: @Composable RowScope.() -> Unit
) {
    Surface(
        onClick = onClick,
        modifier = modifier,
        enabled = enabled,
        shape = shape,
        color = MaterialTheme.colorScheme.surface.copy(alpha = 0.4f),
        border = BorderStroke(
            0.5.dp,
            MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.3f)
        ),
        interactionSource = interactionSource
    ) {
        Row(
            modifier = Modifier.padding(contentPadding),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.Center
        ) {
            content()
        }
    }
}

/**
 * 图标按钮组件
 *
 * 小型圆形按钮，常用于卡片内部或工具栏
 *
 * @param onClick 点击回调
 * @param icon 图标
 * @param modifier 修饰符
 * @param enabled 是否启用
 * @param size 按钮大小
 * @param containerColor 背景色
 * @param contentColor 图标颜色
 */
@Composable
fun ShardIconButton(
    onClick: () -> Unit,
    icon: ImageVector,
    modifier: Modifier = Modifier,
    enabled: Boolean = true,
    size: Dp = 40.dp,
    containerColor: Color = Color.Transparent, // 默认透明以便显示发光
    contentColor: Color = MaterialTheme.colorScheme.primary
) {
    val (isCardBlurEnabled, _, hazeState) = LocalCardLayoutConfig.current
    val interactionSource = remember { MutableInteractionSource() }
    val isPressed by interactionSource.collectIsPressedAsState()
    val scale by animateFloatAsState(
        targetValue = if (isPressed) 0.9f else 1f,
        animationSpec = spring(stiffness = Spring.StiffnessMediumLow),
        label = "icon_button_scale"
    )

    val shape = RoundedCornerShape(size / 2)
    
    val baseModifier = modifier
        .size(size)
        .scale(scale)
        .glow(
            color = contentColor,
            cornerRadius = size / 2,
            blurRadius = if (isPressed) 12.dp else 6.dp,
            enabled = true
        )

    val finalModifier = if (isCardBlurEnabled && Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
        baseModifier.clip(shape).hazeEffect(state = hazeState)
    } else baseModifier

    Surface(
        modifier = finalModifier.clickable(
            interactionSource = interactionSource,
            indication = null,
            enabled = enabled,
            onClick = onClick
        ),
        shape = shape,
        color = if (containerColor == Color.Transparent && isCardBlurEnabled) {
             MaterialTheme.colorScheme.surface.copy(alpha = 0.5f)
        } else containerColor
    ) {
        Box(
            modifier = Modifier.fillMaxWidth(),
            contentAlignment = Alignment.Center
        ) {
            Icon(
                imageVector = icon,
                contentDescription = null,
                modifier = Modifier.size(size * 0.5f),
                tint = contentColor
            )
        }
    }
}

/**
 * 带图标和文字的按钮（便捷组件）
 *
 * @param onClick 点击回调
 * @param text 按钮文字
 * @param icon 按钮图标（可选）
 * @param modifier 修饰符
 * @param type 按钮类型
 * @param size 按钮尺寸
 * @param enabled 是否启用
 * @param iconPosition 图标位置（前/后）
 * @param shape 按钮形状
 */
@Composable
fun ShardButtonWithIcon(
    onClick: () -> Unit,
    text: String,
    icon: ImageVector? = null,
    modifier: Modifier = Modifier,
    type: ButtonType = ButtonType.FILLED,
    size: ButtonSize = ButtonSize.MEDIUM,
    enabled: Boolean = true,
    iconPosition: IconPosition = IconPosition.START,
    shape: Shape = RoundedCornerShape(16.dp)
) {
    ShardButton(
        onClick = onClick,
        modifier = modifier,
        type = type,
        size = size,
        enabled = enabled,
        shape = shape
    ) {
        if (icon != null && iconPosition == IconPosition.START) {
            Icon(
                imageVector = icon,
                contentDescription = null,
                modifier = Modifier.size(20.dp)
            )
            Spacer(modifier = Modifier.width(8.dp))
        }
        Text(text)
        if (icon != null && iconPosition == IconPosition.END) {
            Spacer(modifier = Modifier.width(8.dp))
            Icon(
                imageVector = icon,
                contentDescription = null,
                modifier = Modifier.size(20.dp)
            )
        }
    }
}

/**
 * 图标位置枚举
 */
enum class IconPosition {
    START,
    END
}

// -------------- 废弃组件的兼容性别名 --------------
// 注意：ScalingActionButton 和 TileButton 的废弃别名定义在 CommonComponents.kt 中
// 以避免重复定义编译错误
