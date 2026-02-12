package com.lanrhyme.shardlauncher.ui.components.tiles

import android.os.Build
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.spring
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.interaction.collectIsPressedAsState
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ColumnScope
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
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
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import com.lanrhyme.shardlauncher.ui.components.basic.glow
import com.lanrhyme.shardlauncher.ui.components.layout.LocalCardLayoutConfig
import dev.chrisbanes.haze.hazeEffect

/**
 * 磁贴尺寸枚举
 */
enum class TileSize {
    SMALL,  // 1x1 - 小型磁贴
    MEDIUM, // 2x1 - 中型磁贴 (默认)
    LARGE,  // 2x2 - 大型磁贴
    WIDE    // 4x1 - 宽幅磁贴
}

/**
 * 磁贴样式枚举
 */
enum class TileStyle {
    DEFAULT,    // 默认样式 - 使用Surface颜色
    ACCENT,     // 强调色 - 使用主色
    GRADIENT,   // 渐变样式
    GLASS       // 毛玻璃效果
}

/**
 * 现代化磁贴卡片组件
 *
 * @param modifier 修饰符
 * @param size 磁贴尺寸
 * @param style 磁贴样式
 * @param onClick 点击回调
 * @param enabled 是否启用
 * @param shape 形状
 * @param content 内容
 */
@Composable
fun TileCard(
    modifier: Modifier = Modifier,
    size: TileSize = TileSize.MEDIUM,
    style: TileStyle = TileStyle.DEFAULT,
    onClick: (() -> Unit)? = null,
    enabled: Boolean = true,
    shape: Shape = RoundedCornerShape(16.dp),
    content: @Composable ColumnScope.() -> Unit
) {
    val (isCardBlurEnabled, cardAlpha, hazeState) = LocalCardLayoutConfig.current
    val interactionSource = remember { MutableInteractionSource() }
    val isPressed by interactionSource.collectIsPressedAsState()
    
    val scale by animateFloatAsState(
        targetValue = if (isPressed && onClick != null) 0.96f else 1f,
        animationSpec = tween(durationMillis = 100),
        label = "tile_scale"
    )

    // 根据样式确定背景
    val backgroundModifier = when (style) {
        TileStyle.ACCENT -> {
            Modifier.background(
                brush = Brush.linearGradient(
                    colors = listOf(
                        MaterialTheme.colorScheme.primary,
                        MaterialTheme.colorScheme.tertiary
                    )
                ),
                shape = shape
            )
        }
        TileStyle.GRADIENT -> {
            Modifier.background(
                brush = Brush.linearGradient(
                    colors = listOf(
                        MaterialTheme.colorScheme.primary.copy(alpha = 0.8f),
                        MaterialTheme.colorScheme.secondary.copy(alpha = 0.6f),
                        MaterialTheme.colorScheme.tertiary.copy(alpha = 0.4f)
                    )
                ),
                shape = shape
            )
        }
        else -> Modifier
    }

    val cardModifier = modifier
        .scale(scale)
        .clip(shape)
        .then(
            if (style == TileStyle.GLASS && isCardBlurEnabled && Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
                Modifier.hazeEffect(state = hazeState)
            } else Modifier
        )
        .then(
            if (onClick != null) {
                Modifier.clickable(
                    interactionSource = interactionSource,
                    indication = null,
                    enabled = enabled,
                    onClick = onClick
                )
            } else Modifier
        )

    Card(
        modifier = cardModifier,
        shape = shape,
        colors = CardDefaults.cardColors(
            containerColor = when (style) {
                TileStyle.DEFAULT -> MaterialTheme.colorScheme.surface.copy(alpha = cardAlpha)
                TileStyle.GLASS -> MaterialTheme.colorScheme.surface.copy(alpha = 0.7f)
                else -> Color.Transparent
            }
        ),
        elevation = CardDefaults.cardElevation(defaultElevation = 0.dp)
    ) {
        Box(
            modifier = Modifier
                .fillMaxSize()
                .then(backgroundModifier)
                .padding(16.dp)
        ) {
            Column(
                modifier = Modifier.fillMaxSize(),
                content = content
            )
        }
    }
}

/**
 * 信息磁贴组件 - 显示标题、数值和图标
 *
 * @param title 标题
 * @param value 主要数值/文字
 * @param icon 图标
 * @param modifier 修饰符
 * @param subtitle 副标题
 * @param style 磁贴样式
 * @param onClick 点击回调
 */
@Composable
fun InfoTile(
    title: String,
    value: String,
    icon: ImageVector,
    modifier: Modifier = Modifier,
    subtitle: String? = null,
    style: TileStyle = TileStyle.DEFAULT,
    onClick: (() -> Unit)? = null
) {
    val contentColor = when (style) {
        TileStyle.ACCENT, TileStyle.GRADIENT -> MaterialTheme.colorScheme.onPrimary
        else -> MaterialTheme.colorScheme.onSurface
    }

    TileCard(
        modifier = modifier,
        style = style,
        onClick = onClick
    ) {
        Column(
            modifier = Modifier.fillMaxSize(),
            verticalArrangement = Arrangement.SpaceBetween
        ) {
            // 顶部图标
            Surface(
                modifier = Modifier.size(40.dp),
                shape = RoundedCornerShape(12.dp),
                color = if (style == TileStyle.DEFAULT) {
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
                        tint = if (style == TileStyle.DEFAULT) {
                            MaterialTheme.colorScheme.primary
                        } else contentColor
                    )
                }
            }

            // 底部文字
            Column {
                Text(
                    text = value,
                    style = MaterialTheme.typography.titleLarge,
                    fontWeight = FontWeight.Bold,
                    color = contentColor,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
                if (subtitle != null) {
                    Spacer(modifier = Modifier.height(2.dp))
                    Text(
                        text = subtitle,
                        style = MaterialTheme.typography.labelSmall,
                        color = contentColor.copy(alpha = 0.7f),
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
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
 * 操作磁贴组件 - 用于显示可点击的操作按钮
 *
 * @param title 标题
 * @param icon 图标
 * @param onClick 点击回调
 * @param modifier 修饰符
 * @param subtitle 副标题
 * @param style 磁贴样式
 */
@Composable
fun ActionTile(
    title: String,
    icon: ImageVector,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    subtitle: String? = null,
    style: TileStyle = TileStyle.DEFAULT
) {
    val contentColor = when (style) {
        TileStyle.ACCENT, TileStyle.GRADIENT -> MaterialTheme.colorScheme.onPrimary
        else -> MaterialTheme.colorScheme.onSurface
    }

    TileCard(
        modifier = modifier,
        style = style,
        onClick = onClick
    ) {
        Column(
            modifier = Modifier.fillMaxSize(),
            verticalArrangement = Arrangement.Center,
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Icon(
                imageVector = icon,
                contentDescription = null,
                modifier = Modifier.size(32.dp),
                tint = if (style == TileStyle.DEFAULT) {
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
 * 宽幅内容磁贴 - 用于显示较大的内容区域
 *
 * @param title 标题
 * @param modifier 修饰符
 * @param icon 图标（可选）
 * @param style 磁贴样式
 * @param onClick 点击回调（可选）
 * @param content 内容区域
 */
@Composable
fun ContentTile(
    title: String,
    modifier: Modifier = Modifier,
    icon: ImageVector? = null,
    style: TileStyle = TileStyle.DEFAULT,
    onClick: (() -> Unit)? = null,
    content: @Composable ColumnScope.() -> Unit
) {
    val contentColor = when (style) {
        TileStyle.ACCENT, TileStyle.GRADIENT -> MaterialTheme.colorScheme.onPrimary
        else -> MaterialTheme.colorScheme.onSurface
    }

    TileCard(
        modifier = modifier,
        style = style,
        onClick = onClick
    ) {
        Column(
            modifier = Modifier.fillMaxSize()
        ) {
            // 标题栏
            Row(
                verticalAlignment = Alignment.CenterVertically,
                modifier = Modifier.padding(bottom = 12.dp)
            ) {
                if (icon != null) {
                    Icon(
                        imageVector = icon,
                        contentDescription = null,
                        modifier = Modifier.size(20.dp),
                        tint = if (style == TileStyle.DEFAULT) {
                            MaterialTheme.colorScheme.primary
                        } else contentColor
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                }
                Text(
                    text = title,
                    style = MaterialTheme.typography.titleSmall,
                    fontWeight = FontWeight.SemiBold,
                    color = contentColor
                )
            }

            // 内容
            content()
        }
    }
}

/**
 * 磁贴网格布局
 *
 * @param columns 列数
 * @param modifier 修饰符
 * @param horizontalSpacing 水平间距
 * @param verticalSpacing 垂直间距
 * @param content 网格内容
 */
@Composable
fun TileGrid(
    columns: Int = 4,
    modifier: Modifier = Modifier,
    horizontalSpacing: Dp = 12.dp,
    verticalSpacing: Dp = 12.dp,
    content: @Composable TileGridScope.() -> Unit
) {
    val scope = remember { TileGridScopeImpl(columns, horizontalSpacing, verticalSpacing) }
    scope.content()

    Column(
        modifier = modifier.fillMaxWidth(),
        verticalArrangement = Arrangement.spacedBy(verticalSpacing)
    ) {
        scope.rows.forEach { rowItems ->
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(horizontalSpacing)
            ) {
                rowItems.forEach { item ->
                    Box(
                        modifier = Modifier.weight(item.weight)
                    ) {
                        item.content()
                    }
                }
            }
        }
    }
}

/**
 * 磁贴网格作用域
 */
interface TileGridScope {
    fun item(weight: Float = 1f, content: @Composable () -> Unit)
    fun row(content: TileGridScope.() -> Unit)
}

private class TileGridItem(
    val weight: Float,
    val content: @Composable () -> Unit
)

private class TileGridScopeImpl(
    private val columns: Int,
    private val horizontalSpacing: Dp,
    private val verticalSpacing: Dp
) : TileGridScope {
    val rows = mutableListOf<List<TileGridItem>>()
    private var currentRow = mutableListOf<TileGridItem>()

    override fun item(weight: Float, content: @Composable () -> Unit) {
        currentRow.add(TileGridItem(weight, content))
        if (currentRow.sumOf { it.weight.toDouble() } >= columns) {
            rows.add(currentRow.toList())
            currentRow = mutableListOf()
        }
    }

    override fun row(content: TileGridScope.() -> Unit) {
        if (currentRow.isNotEmpty()) {
            rows.add(currentRow.toList())
            currentRow = mutableListOf()
        }
        content()
        if (currentRow.isNotEmpty()) {
            rows.add(currentRow.toList())
            currentRow = mutableListOf()
        }
    }
}

/**
 * 磁贴按钮 - 小型圆形按钮，常用于磁贴内部
 *
 * @param onClick 点击回调
 * @param icon 图标
 * @param modifier 修饰符
 * @param enabled 是否启用
 * @param size 按钮大小
 */
@Composable
fun TileButton(
    onClick: () -> Unit,
    icon: ImageVector,
    modifier: Modifier = Modifier,
    enabled: Boolean = true,
    size: Dp = 36.dp
) {
    val interactionSource = remember { MutableInteractionSource() }
    val isPressed by interactionSource.collectIsPressedAsState()
    val scale by animateFloatAsState(
        targetValue = if (isPressed) 0.9f else 1f,
        animationSpec = spring(stiffness = 400f),
        label = "button_scale"
    )

    Surface(
        modifier = modifier
            .size(size)
            .scale(scale)
            .clickable(
                interactionSource = interactionSource,
                indication = null,
                enabled = enabled,
                onClick = onClick
            )
            .glow(
                color = MaterialTheme.colorScheme.primary,
                cornerRadius = size / 2,
                blurRadius = 8.dp,
                enabled = isPressed
            ),
        shape = RoundedCornerShape(size / 2),
        color = MaterialTheme.colorScheme.surface.copy(alpha = 0.8f)
    ) {
        Box(
            modifier = Modifier.fillMaxSize(),
            contentAlignment = Alignment.Center
        ) {
            Icon(
                imageVector = icon,
                contentDescription = null,
                modifier = Modifier.size(size * 0.5f),
                tint = MaterialTheme.colorScheme.primary
            )
        }
    }
}
