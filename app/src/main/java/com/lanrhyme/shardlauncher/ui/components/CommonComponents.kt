package com.lanrhyme.shardlauncher.ui.components

import android.os.Build
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.Spring
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.spring
import androidx.compose.animation.core.tween
import androidx.compose.animation.core.updateTransition
import androidx.compose.animation.expandVertically
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.shrinkVertically
import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.interaction.collectIsPressedAsState
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
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
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Search
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ExposedDropdownMenuDefaults
import androidx.compose.material3.FilterChip
import androidx.compose.material3.FilterChipDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.PrimaryTabRow
import androidx.compose.material3.Tab
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.composed
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.drawBehind
import androidx.compose.ui.draw.scale
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Paint
import androidx.compose.ui.graphics.drawscope.drawIntoCanvas
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.graphics.toArgb
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import dev.chrisbanes.haze.HazeState
import dev.chrisbanes.haze.hazeChild

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CollapsibleCard(
        modifier: Modifier = Modifier,
        title: String,
        summary: String? = null,
        animationSpeed: Float = 1.0f,
        isCardBlurEnabled: Boolean = false,
        cardAlpha: Float = 0.6f,
        hazeState: HazeState? = null,
        content: @Composable () -> Unit
) {
    var isExpanded by remember { mutableStateOf(false) }
    val animationDuration = (300 / animationSpeed).toInt()
    val cardShape = RoundedCornerShape(16.dp)

    val cardModifier =
            if (isCardBlurEnabled &&
                            hazeState != null &&
                            Build.VERSION.SDK_INT >= Build.VERSION_CODES.S
            ) {
                modifier.fillMaxWidth().hazeChild(state = hazeState, shape = cardShape)
            } else {
                modifier.fillMaxWidth()
            }

    Card(
            modifier = cardModifier,
            shape = cardShape,
            colors =
                    CardDefaults.cardColors(
                            containerColor =
                                    MaterialTheme.colorScheme.surface.copy(alpha = cardAlpha)
                    ),
    ) {
        Column {
            Row(
                    modifier =
                            Modifier.clickable { isExpanded = !isExpanded }
                                    .padding(horizontal = 16.dp, vertical = 12.dp),
                    verticalAlignment = Alignment.CenterVertically,
            ) {
                TitleAndSummary(modifier = Modifier.weight(1f), title = title, summary = summary)
                ExposedDropdownMenuDefaults.TrailingIcon(expanded = isExpanded)
            }
            AnimatedVisibility(
                    visible = isExpanded,
                    enter =
                            expandVertically(
                                    animationSpec =
                                            tween(animationDuration, easing = FastOutSlowInEasing)
                            ) + fadeIn(animationSpec = tween(animationDuration)),
                    exit =
                            shrinkVertically(
                                    animationSpec =
                                            tween(animationDuration, easing = FastOutSlowInEasing)
                            ) + fadeOut(animationSpec = tween(animationDuration))
            ) {
                Column(modifier = Modifier.padding(start = 16.dp, end = 16.dp, bottom = 12.dp)) {
                    content()
                }
            }
        }
    }
}

@Composable
fun CombinedCard(
        modifier: Modifier = Modifier,
        title: String,
        summary: String? = null,
        isCardBlurEnabled: Boolean = false,
        cardAlpha: Float = 0.6f,
        hazeState: HazeState? = null,
        content: @Composable () -> Unit
) {
    val cardShape = RoundedCornerShape(16.dp)
    val cardModifier =
            if (isCardBlurEnabled &&
                            hazeState != null &&
                            Build.VERSION.SDK_INT >= Build.VERSION_CODES.S
            ) {
                modifier.fillMaxWidth().hazeChild(state = hazeState, shape = cardShape)
            } else {
                modifier.fillMaxWidth()
            }
    Card(
            modifier = cardModifier,
            shape = cardShape,
            colors =
                    CardDefaults.cardColors(
                            containerColor =
                                    MaterialTheme.colorScheme.surface.copy(alpha = cardAlpha)
                    ),
    ) {
        Column(modifier = Modifier.padding(12.dp)) {
            TitleAndSummary(title = title, summary = summary)
            Spacer(modifier = Modifier.height(12.dp))
            content()
        }
    }
}

/**
 * 一个带动画的按钮，在按下时会缩放以提供触觉反馈，并带有默认的渐变背景
 *
 * @param onClick 当按钮被点击时执行的操作
 * @param modifier 应用于按钮的修饰符
 * @param icon 在按钮中显示的图标
 * @param text 在按钮中显示的文本
 * @param enabled 控制按钮的启用状态 当为 `false` 时，此按钮将不可点击，并向用户显示为禁用状态
 * @param animationSpeed 缩放动画的速度
 */
@Composable
fun ScalingActionButton(
        onClick: () -> Unit,
        modifier: Modifier = Modifier,
        icon: ImageVector? = null,
        text: String? = null,
        enabled: Boolean = true,
        animationSpeed: Float = 1.0f,
        contentPadding: PaddingValues = PaddingValues(horizontal = 24.dp, vertical = 4.dp)
) {
    val interactionSource = remember { MutableInteractionSource() }
    val isPressed by interactionSource.collectIsPressedAsState()
    val animationDuration = (150 / animationSpeed).toInt()
    val scale by
            animateFloatAsState(
                    targetValue = if (isPressed) 0.95f else 1f,
                    label = "buttonScale",
                    animationSpec = tween(durationMillis = animationDuration)
            )

    val backgroundBrush =
            Brush.horizontalGradient(
                    colors =
                            listOf(
                                    MaterialTheme.colorScheme.primary,
                                    MaterialTheme.colorScheme.tertiary,
                            )
            )
    val buttonShape = RoundedCornerShape(100.dp)

    val buttonModifier = modifier.scale(scale).background(backgroundBrush, shape = buttonShape)

    Button(
            onClick = onClick,
            modifier = buttonModifier,
            enabled = enabled,
            interactionSource = interactionSource,
            contentPadding = contentPadding,
            colors =
                    ButtonDefaults.buttonColors(
                            containerColor = Color.Transparent,
                            contentColor = MaterialTheme.colorScheme.onPrimary
                    ),
            elevation =
                    ButtonDefaults.buttonElevation(
                            defaultElevation = 0.dp,
                            pressedElevation = 0.dp
                    ),
            shape = buttonShape
    ) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            icon?.let {
                Icon(
                        imageVector = it,
                        contentDescription = null, // Decorative icon
                        modifier = Modifier.size(24.dp)
                )
                if (text != null) {
                    Spacer(Modifier.size(8.dp))
                }
            }
            text?.let { Text(it) }
        }
    }
}

/**
 * 一个可组合项，用于显示标题及其下方较小的半透明摘要
 *
 * @param title 主标题文本
 * @param summary 摘要文本，显示在标题下方
 * @param modifier 应用于布局的修饰符
 */
@Composable
fun TitleAndSummary(title: String, summary: String?, modifier: Modifier = Modifier) {
    Column(modifier = modifier.fillMaxWidth()) {
        Text(text = title, style = MaterialTheme.typography.titleSmall)
        summary?.let {
            Spacer(Modifier.height(4.dp))
            Text(
                    text = it,
                    style = MaterialTheme.typography.labelSmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun <T> SegmentedNavigationBar(
        modifier: Modifier = Modifier,
        title: String,
        selectedPage: T,
        onPageSelected: (T) -> Unit,
        pages: List<T>,
        getTitle: (T) -> String
) {
    Row(
            modifier = modifier.fillMaxWidth().padding(horizontal = 16.dp, vertical = 8.dp),
            verticalAlignment = Alignment.CenterVertically
    ) {
        Text(
                text = title,
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.onPrimary,
                modifier =
                        Modifier.glow(
                                        color = MaterialTheme.colorScheme.primary,
                                        cornerRadius = 16.dp
                                )
                                .clip(RoundedCornerShape(16.dp))
                                .background(
                                        Brush.horizontalGradient(
                                                colors =
                                                        listOf(
                                                                MaterialTheme.colorScheme.primary,
                                                                MaterialTheme.colorScheme.tertiary,
                                                        )
                                        )
                                )
                                .padding(horizontal = 16.dp, vertical = 4.dp)
        )
        Spacer(modifier = Modifier.width(16.dp))
        PrimaryTabRow(
                selectedTabIndex = pages.indexOf(selectedPage),
                modifier = Modifier.weight(1f).clip(RoundedCornerShape(16.dp)),
                divider = {},
        ) {
            pages.forEach { page ->
                Tab(
                        modifier = Modifier.height(40.dp),
                        selected = selectedPage == page,
                        onClick = { onPageSelected(page) },
                        text = { Text(text = getTitle(page)) }
                )
            }
        }
    }
}

@Composable
fun SubPageNavigationBar(
        title: String = "返回",
        description: String? = null,
        onBack: () -> Unit,
        modifier: Modifier = Modifier
) {
    Row(verticalAlignment = Alignment.CenterVertically, modifier = modifier.fillMaxWidth()) {
        IconButton(onClick = onBack) {
            Icon(imageVector = Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back")
        }
        Text(text = title, style = MaterialTheme.typography.titleLarge)
        if (description != null) {
            Spacer(modifier = Modifier.width(8.dp))
            Text(text = description, style = MaterialTheme.typography.bodyMedium)
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun StyledFilterChip(
        selected: Boolean,
        onClick: () -> Unit,
        label: @Composable () -> Unit,
        modifier: Modifier = Modifier
) {
    FilterChip(
            selected = selected,
            onClick = onClick,
            label = label,
            modifier = modifier,
            shape = RoundedCornerShape(16.dp),
            colors =
                    FilterChipDefaults.filterChipColors(
                            selectedContainerColor = MaterialTheme.colorScheme.primary,
                            selectedLabelColor = MaterialTheme.colorScheme.onPrimary
                    )
    )
}

/**
 * 一个修饰符，用于在可组合项的边界周围应用发光效果
 *
 * @param color 发光的颜色
 * @param cornerRadius 发光形状的圆角半径
 * @param blurRadius 发光效果的模糊半径
 * @param enabled 切换发光效果的开或关
 */
fun Modifier.glow(
        color: Color,
        cornerRadius: Dp = 0.dp,
        blurRadius: Dp = 12.dp,
        enabled: Boolean = true
): Modifier = composed {
    if (!enabled) return@composed this

    val shadowColor = color.copy(alpha = 0.7f).toArgb()
    val transparent = color.copy(alpha = 0f).toArgb()

    this.drawBehind {
        this.drawIntoCanvas {
            val paint = Paint()
            val frameworkPaint = paint.asFrameworkPaint()
            frameworkPaint.color = transparent
            frameworkPaint.setShadowLayer(blurRadius.toPx(), 0f, 0f, shadowColor)
            it.drawRoundRect(
                    0f,
                    0f,
                    this.size.width,
                    this.size.height,
                    cornerRadius.toPx(),
                    cornerRadius.toPx(),
                    paint
            )
        }
    }
}

/**
 * 为任意 Composable 添加入场动画效果
 *
 * 动画内容：
 * 1. 透明度从 0 → 1（淡入）
 * 2. 缩放从 0.95 → 1（轻微放大）
 *
 * 当多条目（如 LazyColumn/LazyRow）使用时，通过 [index] 错开启动时刻，形成瀑布流效果 单条目调用时 [index] 传 0 即可
 *
 * @param index 条目在列表中的位置，用于计算延迟（越靠后越晚开始）,单条目场景直接传 0
 * @param animationSpeed 整体速度系数
 */
@OptIn(ExperimentalFoundationApi::class)
fun Modifier.animatedAppearance(index: Int, animationSpeed: Float): Modifier = composed {
    var animated by remember { mutableStateOf(false) }
    val animationDuration = (300 / animationSpeed).toInt()
    val delay = (60 * index / animationSpeed).toInt()

    val alpha by
            animateFloatAsState(
                    targetValue = if (animated) 1f else 0f,
                    animationSpec = tween(durationMillis = animationDuration, delayMillis = delay),
                    label = "alpha"
            )
    val scale by
            animateFloatAsState(
                    targetValue = if (animated) 1f else 0.95f,
                    animationSpec = tween(durationMillis = animationDuration, delayMillis = delay),
                    label = "scale"
            )

    LaunchedEffect(Unit) { animated = true }

    this.graphicsLayer(alpha = alpha, scaleX = scale, scaleY = scale)
}

/**
 * 为可选择的卡片提供选择动画效果 带有边框和弹动动画
 *
 * 使用时请在卡片调用处传入以下参数
 * ```
 * border = if (isSelected) BorderStroke(1.5.dp, MaterialTheme.colorScheme.primary) else null
 * ```
 * @param isSelected 卡片是否被选中
 * @param isPressed 卡片是否被按下
 */
private enum class SelectableState {
    Pressed,
    Selected,
    Idle
}

fun Modifier.selectableCard(
        isSelected: Boolean,
        isPressed: Boolean,
): Modifier = composed {
    val transition =
            updateTransition(
                    targetState =
                            when {
                                isPressed -> SelectableState.Pressed
                                isSelected -> SelectableState.Selected
                                else -> SelectableState.Idle
                            },
                    label = "selectableCard-transition"
            )

    val scale by
            transition.animateFloat(
                    transitionSpec = {
                        when {
                            // 按下
                            SelectableState.Idle isTransitioningTo SelectableState.Pressed ||
                                    SelectableState.Selected isTransitioningTo
                                            SelectableState.Pressed -> tween(durationMillis = 100)
                            // 弹回
                            else ->
                                    spring(
                                            dampingRatio = Spring.DampingRatioMediumBouncy,
                                            stiffness = Spring.StiffnessLow
                                    )
                        }
                    },
                    label = "selectableCard-scale"
            ) { state ->
                when (state) {
                    SelectableState.Pressed -> 0.97f
                    SelectableState.Selected -> 1.03f
                    SelectableState.Idle -> 1f
                }
            }

    this.graphicsLayer {
        scaleX = scale
        scaleY = scale
    }
}

@Composable
fun SearchTextField(
        value: String,
        onValueChange: (String) -> Unit,
        hint: String,
        modifier: Modifier = Modifier,
) {
    BasicTextField(
            value = value,
            onValueChange = onValueChange,
            modifier = modifier.fillMaxSize(),
            textStyle =
                    MaterialTheme.typography.bodySmall.copy(
                            color = MaterialTheme.colorScheme.onSurface
                    ),
            singleLine = true,
            decorationBox = { innerTextField ->
                Row(
                        modifier =
                                Modifier.background(
                                                MaterialTheme.colorScheme.surfaceVariant.copy(
                                                        alpha = 0.6f
                                                ),
                                                RoundedCornerShape(22.dp)
                                        )
                                        .padding(horizontal = 8.dp)
                                        .fillMaxSize(),
                        verticalAlignment = Alignment.CenterVertically
                ) {
                    Icon(
                            Icons.Default.Search,
                            contentDescription = "Search",
                            tint = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                    Box(
                            modifier = Modifier.padding(start = 4.dp).weight(1f),
                            contentAlignment = Alignment.CenterStart
                    ) {
                        if (value.isEmpty()) {
                            Text(
                                    hint,
                                    style = MaterialTheme.typography.bodySmall,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                        innerTextField()
                    }
                }
            }
    )
}
