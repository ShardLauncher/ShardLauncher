package com.lanrhyme.shardlauncher.ui.components.basic

import android.os.Build
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.tween
import androidx.compose.animation.expandVertically
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.shrinkVertically
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.interaction.collectIsPressedAsState
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ColumnScope
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ExposedDropdownMenuDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.scale
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Shape
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.lanrhyme.shardlauncher.ui.components.layout.LocalCardLayoutConfig
import dev.chrisbanes.haze.hazeEffect

/**
 * 卡片样式枚举
 */
enum class CardStyle {
    DEFAULT,        // 默认样式
    GLASS,          // 毛玻璃效果
    GRADIENT,       // 渐变背景
    ACCENT,         // 强调色背景
    BORDERED        // 带边框
}

/**
 * 统一的卡片组件 - ShardCard
 *
 * 这是项目中所有卡片的基础组件，支持多种样式和效果：
 * - 默认样式：标准 Surface 颜色
 * - 毛玻璃效果：使用 Haze 模糊（Android 12+）
 * - 渐变背景：主色到第三色的渐变
 * - 强调色：使用主色调
 * - 边框样式：带发光边框
 *
 * @param modifier 应用于卡片的修饰符
 * @param enabled 控制卡片是否启用，影响透明度
 * @param shape 卡片形状，默认为 16dp 圆角
 * @param style 卡片样式，参考 [CardStyle]
 * @param onClick 点击回调，为 null 时不可点击
 * @param border 是否显示边框，仅在 DEFAULT 和 GLASS 样式下有效
 * @param contentPadding 内容内边距
 * @param content 卡片内容
 */
@Composable
fun ShardCard(
    modifier: Modifier = Modifier,
    enabled: Boolean = true,
    shape: Shape = RoundedCornerShape(16.dp),
    style: CardStyle = CardStyle.DEFAULT,
    onClick: (() -> Unit)? = null,
    border: Boolean = true,
    contentPadding: androidx.compose.foundation.layout.PaddingValues = androidx.compose.foundation.layout.PaddingValues(16.dp),
    content: @Composable ColumnScope.() -> Unit
) {
    val (isCardBlurEnabled, cardAlpha, hazeState) = LocalCardLayoutConfig.current
    val interactionSource = remember { MutableInteractionSource() }
    val isPressed by interactionSource.collectIsPressedAsState()

    val scale by animateFloatAsState(
        targetValue = if (isPressed && onClick != null) 0.98f else 1f,
        animationSpec = tween(durationMillis = 100),
        label = "card_scale"
    )

    // 计算背景色和修饰符
    val (backgroundModifier, containerColor) = when (style) {
        CardStyle.GRADIENT -> {
            Modifier.background(
                brush = Brush.linearGradient(
                    colors = listOf(
                        MaterialTheme.colorScheme.primary,
                        MaterialTheme.colorScheme.tertiary
                    )
                ),
                shape = shape
            ) to Color.Transparent
        }
        CardStyle.ACCENT -> {
            Modifier.background(
                brush = Brush.linearGradient(
                    colors = listOf(
                        MaterialTheme.colorScheme.primary.copy(alpha = 0.9f),
                        MaterialTheme.colorScheme.secondary.copy(alpha = 0.7f)
                    )
                ),
                shape = shape
            ) to Color.Transparent
        }
        else -> Modifier to MaterialTheme.colorScheme.surface.copy(
            alpha = if (style == CardStyle.GLASS) 0.7f else cardAlpha
        )
    }

    // 基础修饰符
    var cardModifier = modifier
        .fillMaxWidth()
        .scale(scale)
        .clip(shape)

    // 添加模糊效果（仅在 GLASS 样式或启用模糊时）
    cardModifier = if ((style == CardStyle.GLASS || isCardBlurEnabled) &&
        Build.VERSION.SDK_INT >= Build.VERSION_CODES.S
    ) {
        cardModifier.hazeEffect(state = hazeState)
    } else cardModifier

    // 添加点击效果
    cardModifier = if (onClick != null) {
        cardModifier.clickable(
            interactionSource = interactionSource,
            indication = null,
            enabled = enabled,
            onClick = onClick
        )
    } else cardModifier

    // 边框处理
    val borderStroke = when {
        !border -> null
        style == CardStyle.BORDERED -> BorderStroke(
            0.5.dp,
            Brush.verticalGradient(
                listOf(
                    Color.White.copy(alpha = 0.2f),
                    Color.White.copy(alpha = 0.05f)
                )
            )
        )
        else -> BorderStroke(
            1.dp,
            MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.3f)
        )
    }

    Card(
        modifier = cardModifier,
        shape = shape,
        border = borderStroke,
        colors = CardDefaults.cardColors(
            containerColor = if (enabled) containerColor else containerColor.copy(alpha = 0.5f)
        ),
        elevation = CardDefaults.cardElevation(defaultElevation = 0.dp)
    ) {
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .then(backgroundModifier)
                .padding(contentPadding)
        ) {
            Column(
                modifier = Modifier.fillMaxWidth(),
                content = content
            )
        }
    }
}

/**
 * 可折叠卡片组件
 *
 * 继承自 ShardCard，添加展开/收起功能
 *
 * @param title 卡片标题
 * @param summary 卡片摘要（可选）
 * @param expanded 是否展开，为 null 时内部管理状态
 * @param onExpandedChange 展开状态变化回调
 * @param modifier 应用于卡片的修饰符
 * @param enabled 是否启用
 * @param shape 卡片形状
 * @param style 卡片样式
 * @param animationSpeed 动画速度
 * @param headerContent 自定义头部内容（可选）
 * @param content 展开时显示的内容
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ExpandableCard(
    title: String,
    summary: String? = null,
    expanded: Boolean? = null,
    onExpandedChange: ((Boolean) -> Unit)? = null,
    modifier: Modifier = Modifier,
    enabled: Boolean = true,
    shape: Shape = RoundedCornerShape(16.dp),
    style: CardStyle = CardStyle.DEFAULT,
    animationSpeed: Float = 1.0f,
    headerContent: @Composable (() -> Unit)? = null,
    content: @Composable ColumnScope.() -> Unit
) {
    val internalExpanded = remember { mutableStateOf(false) }
    val isExpanded = expanded ?: internalExpanded.value
    val setExpanded = onExpandedChange ?: { internalExpanded.value = it }

    val animationDuration = (300 / animationSpeed).toInt()

    ShardCard(
        modifier = modifier,
        enabled = enabled,
        shape = shape,
        style = style,
        border = true,
        contentPadding = androidx.compose.foundation.layout.PaddingValues(0.dp)
    ) {
        Column {
            // 头部区域
            Row(
                modifier = Modifier
                    .clickable(enabled = enabled) { setExpanded(!isExpanded) }
                    .padding(horizontal = 16.dp, vertical = 12.dp)
                    .fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically
            ) {
                if (headerContent != null) {
                    headerContent()
                } else {
                    TitleAndSummary(
                        modifier = Modifier.weight(1f),
                        title = title,
                        summary = summary
                    )
                }
                ExposedDropdownMenuDefaults.TrailingIcon(expanded = isExpanded)
            }

            // 展开内容
            AnimatedVisibility(
                visible = isExpanded,
                enter = expandVertically(
                    animationSpec = tween(animationDuration, easing = FastOutSlowInEasing)
                ) + fadeIn(animationSpec = tween(animationDuration)),
                exit = shrinkVertically(
                    animationSpec = tween(animationDuration, easing = FastOutSlowInEasing)
                ) + fadeOut(animationSpec = tween(animationDuration))
            ) {
                Column(
                    modifier = Modifier.padding(
                        start = 16.dp,
                        end = 16.dp,
                        bottom = 16.dp
                    )
                ) {
                    content()
                }
            }
        }
    }
}

/**
 * 信息卡片组件
 *
 * 用于显示标题、数值和图标的组合信息
 *
 * @param title 标题
 * @param value 主要数值/文字
 * @param icon 图标
 * @param modifier 修饰符
 * @param subtitle 副标题
 * @param style 卡片样式
 * @param onClick 点击回调
 */
@Composable
fun InfoCard(
    title: String,
    value: String,
    icon: ImageVector,
    modifier: Modifier = Modifier,
    subtitle: String? = null,
    style: CardStyle = CardStyle.DEFAULT,
    onClick: (() -> Unit)? = null
) {
    val contentColor = when (style) {
        CardStyle.ACCENT, CardStyle.GRADIENT -> MaterialTheme.colorScheme.onPrimary
        else -> MaterialTheme.colorScheme.onSurface
    }

    ShardCard(
        modifier = modifier,
        style = style,
        onClick = onClick,
        contentPadding = androidx.compose.foundation.layout.PaddingValues(16.dp)
    ) {
        Column(
            modifier = Modifier.fillMaxWidth(),
            verticalArrangement = androidx.compose.foundation.layout.Arrangement.spacedBy(12.dp)
        ) {
            // 图标
            Surface(
                modifier = Modifier.size(40.dp),
                shape = RoundedCornerShape(12.dp),
                color = if (style == CardStyle.DEFAULT) {
                    MaterialTheme.colorScheme.primary.copy(alpha = 0.15f)
                } else {
                    contentColor.copy(alpha = 0.2f)
                }
            ) {
                Box(
                    modifier = Modifier.fillMaxSize(),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = icon,
                        contentDescription = null,
                        modifier = Modifier.size(22.dp),
                        tint = if (style == CardStyle.DEFAULT) {
                            MaterialTheme.colorScheme.primary
                        } else contentColor
                    )
                }
            }

            // 文字内容
            Column {
                Text(
                    text = value,
                    style = MaterialTheme.typography.titleLarge,
                    fontWeight = FontWeight.Bold,
                    color = contentColor,
                    maxLines = 1
                )
                if (subtitle != null) {
                    Spacer(modifier = Modifier.height(2.dp))
                    Text(
                        text = subtitle,
                        style = MaterialTheme.typography.labelSmall,
                        color = contentColor.copy(alpha = 0.7f),
                        maxLines = 1
                    )
                }
                Spacer(modifier = Modifier.height(2.dp))
                Text(
                    text = title,
                    style = MaterialTheme.typography.labelMedium,
                    color = contentColor.copy(alpha = 0.6f)
                )
            }
        }
    }
}

/**
 * 操作卡片组件
 *
 * 用于显示可点击的操作项
 *
 * @param title 标题
 * @param icon 图标
 * @param onClick 点击回调
 * @param modifier 修饰符
 * @param subtitle 副标题
 * @param style 卡片样式
 */
@Composable
fun ActionCard(
    title: String,
    icon: ImageVector,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    subtitle: String? = null,
    style: CardStyle = CardStyle.DEFAULT
) {
    val contentColor = when (style) {
        CardStyle.ACCENT, CardStyle.GRADIENT -> MaterialTheme.colorScheme.onPrimary
        else -> MaterialTheme.colorScheme.onSurface
    }

    ShardCard(
        modifier = modifier,
        style = style,
        onClick = onClick,
        contentPadding = androidx.compose.foundation.layout.PaddingValues(16.dp)
    ) {
        Column(
            modifier = Modifier.fillMaxWidth(),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = androidx.compose.foundation.layout.Arrangement.Center
        ) {
            Icon(
                imageVector = icon,
                contentDescription = null,
                modifier = Modifier.size(32.dp),
                tint = if (style == CardStyle.DEFAULT) {
                    MaterialTheme.colorScheme.primary
                } else contentColor
            )
            Spacer(modifier = Modifier.height(8.dp))
            Text(
                text = title,
                style = MaterialTheme.typography.titleSmall,
                fontWeight = FontWeight.Medium,
                color = contentColor
            )
            if (subtitle != null) {
                Spacer(modifier = Modifier.height(2.dp))
                Text(
                    text = subtitle,
                    style = MaterialTheme.typography.labelSmall,
                    color = contentColor.copy(alpha = 0.7f)
                )
            }
        }
    }
}

/**
 * 组合卡片组件
 *
 * 包含标题、摘要和内容的卡片
 *
 * @param title 标题
 * @param summary 摘要（可选）
 * @param modifier 修饰符
 * @param enabled 是否启用
 * @param style 卡片样式
 * @param content 卡片内容
 */
@Composable
fun CombinedCard(
    title: String,
    summary: String? = null,
    modifier: Modifier = Modifier,
    enabled: Boolean = true,
    style: CardStyle = CardStyle.DEFAULT,
    content: @Composable ColumnScope.() -> Unit
) {
    ShardCard(
        modifier = modifier,
        enabled = enabled,
        style = style
    ) {
        TitleAndSummary(title = title, summary = summary)
        Spacer(modifier = Modifier.height(12.dp))
        content()
    }
}

// -------------- 废弃组件的兼容性别名 --------------

/**
 * @deprecated 使用 [ShardCard] 配合 [CardStyle.GLASS] 替代
 */
@Deprecated(
    "使用 ShardCard 配合 CardStyle.GLASS 替代",
    ReplaceWith("ShardCard(modifier = modifier, shape = shape, style = CardStyle.GLASS, onClick = onClick, border = true, content = content)")
)
@Composable
fun ShardGlassCard(
    modifier: Modifier = Modifier,
    onClick: (() -> Unit)? = null,
    shape: Shape = RoundedCornerShape(24.dp),
    content: @Composable ColumnScope.() -> Unit
) {
    ShardCard(
        modifier = modifier,
        shape = shape,
        style = CardStyle.GLASS,
        onClick = onClick,
        border = true,
        content = content
    )
}
