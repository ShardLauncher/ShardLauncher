package com.lanrhyme.shardlauncher.ui.components.basic

import android.os.Build
import com.lanrhyme.shardlauncher.ui.components.layout.LocalCardLayoutConfig
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.MutableTransitionState
import androidx.compose.animation.core.Spring
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.animateDpAsState
import androidx.compose.animation.core.spring
import androidx.compose.animation.core.tween
import androidx.compose.animation.core.updateTransition
import androidx.compose.animation.expandVertically
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.scaleIn
import androidx.compose.animation.scaleOut
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
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.offset
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
import androidx.compose.runtime.derivedStateOf
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
import androidx.compose.ui.focus.onFocusChanged
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Paint
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.graphics.drawscope.drawIntoCanvas
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.graphics.toArgb
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.Popup
import androidx.compose.ui.window.PopupProperties
import androidx.compose.foundation.gestures.Orientation
import androidx.compose.foundation.gestures.detectDragGestures
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.lazy.LazyListState
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.platform.LocalConfiguration
import androidx.compose.ui.platform.LocalDensity
import dev.chrisbanes.haze.hazeEffect
import kotlinx.coroutines.launch

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CollapsibleCard(
        modifier: Modifier = Modifier,
        title: String,
        summary: String? = null,
        animationSpeed: Float = 1.0f,
        content: @Composable () -> Unit
) {
        val (isCardBlurEnabled, cardAlpha, hazeState) = LocalCardLayoutConfig.current
        var isExpanded by remember { mutableStateOf(false) }
        val animationDuration = (300 / animationSpeed).toInt()
        val cardShape = RoundedCornerShape(16.dp)

        val cardModifier =
                if (isCardBlurEnabled && Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
                        modifier
                            .fillMaxWidth()
                            .clip(cardShape)
                            .hazeEffect(state = hazeState)
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
                                        Modifier
                                            .clickable { isExpanded = !isExpanded }
                                            .padding(horizontal = 16.dp, vertical = 12.dp),
                                verticalAlignment = Alignment.CenterVertically,
                        ) {
                                TitleAndSummary(
                                        modifier = Modifier.weight(1f),
                                        title = title,
                                        summary = summary
                                )
                                ExposedDropdownMenuDefaults.TrailingIcon(expanded = isExpanded)
                        }
                        AnimatedVisibility(
                                visible = isExpanded,
                                enter =
                                        expandVertically(
                                                animationSpec =
                                                        tween(
                                                                animationDuration,
                                                                easing = FastOutSlowInEasing
                                                        )
                                        ) + fadeIn(animationSpec = tween(animationDuration)),
                                exit =
                                        shrinkVertically(
                                                animationSpec =
                                                        tween(
                                                                animationDuration,
                                                                easing = FastOutSlowInEasing
                                                        )
                                        ) + fadeOut(animationSpec = tween(animationDuration))
                        ) {
                                Column(
                                        modifier =
                                                Modifier.padding(
                                                        start = 16.dp,
                                                        end = 16.dp,
                                                        bottom = 12.dp
                                                )
                                ) { content() }
                        }
                }
        }
}

/**
 * 一个组合卡片组件，包含标题、可选摘要和常驻内容区域
 * 支持卡片模糊效果配置
 *
 * @param modifier 应用于卡片的修饰符
 * @param title 卡片标题
 * @param summary 卡片摘要（可选）
 * @param content 卡片内容
 */
@Composable
fun CombinedCard(
        modifier: Modifier = Modifier,
        title: String,
        summary: String? = null,
        content: @Composable () -> Unit
) {
        val (isCardBlurEnabled, cardAlpha, hazeState) = LocalCardLayoutConfig.current
        val cardShape = RoundedCornerShape(16.dp)
        val cardModifier =
                if (isCardBlurEnabled && Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
                        modifier
                            .fillMaxWidth()
                            .clip(cardShape)
                            .hazeEffect(state = hazeState)
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

        val buttonModifier = modifier
            .scale(scale)
            .background(backgroundBrush, shape = buttonShape)

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
                modifier = modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp, vertical = 8.dp),
                verticalAlignment = Alignment.CenterVertically
        ) {
                Text(
                    text = title,
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.onPrimary,
                    modifier =
                        Modifier
                            .glow(
                                color = MaterialTheme.colorScheme.primary,
                                cornerRadius = 16.dp
                            )
                            .clip(RoundedCornerShape(16.dp))
                            .background(
                                Brush.horizontalGradient(
                                    colors =
                                        listOf(
                                            MaterialTheme.colorScheme
                                                .primary,
                                            MaterialTheme.colorScheme
                                                .tertiary,
                                        )
                                )
                            )
                            .padding(horizontal = 16.dp, vertical = 4.dp)
                )
                Spacer(modifier = Modifier.width(16.dp))
                PrimaryTabRow(
                        selectedTabIndex = pages.indexOf(selectedPage),
                        modifier = Modifier
                            .weight(1f)
                            .clip(RoundedCornerShape(16.dp)),
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

/**
 * 子页面导航栏组件
 * 包含返回按钮、标题和可选的描述文本
 *
 * @param title 导航栏标题，默认为 "返回"
 * @param description 标题旁边的描述文本（可选）
 * @param onBack 返回按钮点击回调
 * @param modifier 应用于组件的修饰符
 */
@Composable
fun SubPageNavigationBar(
        title: String = "返回",
        description: String? = null,
        onBack: () -> Unit,
        modifier: Modifier = Modifier
) {
        Row(verticalAlignment = Alignment.CenterVertically, modifier = modifier.fillMaxWidth()) {
                IconButton(onClick = onBack) {
                        Icon(
                                imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                                contentDescription = "Back"
                        )
                }
            Text(text = title, style = MaterialTheme.typography.titleLarge)
                if (description != null) {
                        Spacer(modifier = Modifier.width(8.dp))
                    Text(text = description, style = MaterialTheme.typography.bodyMedium)
                }
        }
}

/**
 * 样式化的过滤芯片组件，用于显示可选的标签或选项
 * 选中时使用主色调背景
 *
 * @param selected 是否处于选中状态
 * @param onClick 点击时的回调
 * @param label 芯片显示的标签内容
 * @param modifier 应用于组件的修饰符
 */
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
                        animationSpec =
                                tween(durationMillis = animationDuration, delayMillis = delay),
                        label = "alpha"
                )
        val scale by
                animateFloatAsState(
                        targetValue = if (animated) 1f else 0.95f,
                        animationSpec =
                                tween(durationMillis = animationDuration, delayMillis = delay),
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
                                        SelectableState.Idle isTransitioningTo
                                                SelectableState.Pressed ||
                                                SelectableState.Selected isTransitioningTo
                                                        SelectableState.Pressed ->
                                                tween(durationMillis = 100)
                                        // 弹回
                                        else ->
                                                spring(
                                                        dampingRatio =
                                                                Spring.DampingRatioMediumBouncy,
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
                                        Modifier
                                            .background(
                                                MaterialTheme.colorScheme.surfaceVariant
                                                    .copy(alpha = 0.6f),
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
                                        modifier = Modifier
                                            .padding(start = 4.dp)
                                            .weight(1f),
                                        contentAlignment = Alignment.CenterStart
                                ) {
                                        if (value.isEmpty()) {
                                            Text(
                                                hint,
                                                style = MaterialTheme.typography.bodySmall,
                                                color =
                                                    MaterialTheme.colorScheme
                                                        .onSurfaceVariant
                                            )
                                        }
                                        innerTextField()
                                }
                        }
                }
        )
}

/**
 * 一个胶囊风格的输入框组件
 *
 * @param value 输入框的值
 * @param onValueChange 当值改变时的回调
 * @param label 左侧显示的标签
 * @param modifier 修饰符
 * @param hint 输入框为空时的提示文本
 */
@Composable
fun CapsuleTextField(
    value: String,
    onValueChange: (String) -> Unit,
    label: String,
    modifier: Modifier = Modifier,
    hint: String = ""
) {
    val (isCardBlurEnabled, cardAlpha, hazeState) = LocalCardLayoutConfig.current
    var isFocused by remember { mutableStateOf(false) }
    var textWidth by remember { mutableStateOf(0f) }
    
    // 目标宽度：只有在获取到焦点且有文字时才显示
    val targetWidth = if (isFocused && value.isNotEmpty()) textWidth else 0f
    
    val indicatorWidth by animateFloatAsState(
        targetValue = targetWidth,
        label = "IndicatorWidth",
        animationSpec = spring(stiffness = Spring.StiffnessMediumLow)
    )

    // 文字垂直偏移：聚焦时上移，未聚焦时居中（0）
    val textVerticalOffset by animateDpAsState(
        targetValue = if (isFocused) (-2).dp else 0.dp,
        label = "TextVerticalOffset",
        animationSpec = spring(stiffness = Spring.StiffnessMediumLow)
    )
    
    val indicatorColor = MaterialTheme.colorScheme.primary
    val containerShape = RoundedCornerShape(16.dp)

    Row(
        modifier = modifier
            .height(52.dp)
            .clip(containerShape)
            .then(
                if (isCardBlurEnabled && Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
                    Modifier.hazeEffect(state = hazeState)
                } else Modifier
            )
            .background(
                MaterialTheme.colorScheme.surfaceVariant.copy(
                    alpha = (cardAlpha * 0.4f).coerceAtLeast(0.1f)
                )
            ),
        verticalAlignment = Alignment.CenterVertically
    ) {
        // 左侧标签部分
        Text(
            text = label,
            style = MaterialTheme.typography.titleMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            fontWeight = FontWeight.Bold,
            modifier = Modifier.padding(horizontal = 16.dp)
        )

        // 右侧输入部分
        Box(
            modifier = Modifier
                .clip(RoundedCornerShape(16.dp))
                .weight(1f)
                .fillMaxHeight()
                .background(
                    MaterialTheme.colorScheme.surface.copy(
                        alpha = (cardAlpha * 0.9f).coerceAtLeast(0.3f)
                    )
                )
                .onFocusChanged { isFocused = it.isFocused }
                .padding(horizontal = 16.dp),
            contentAlignment = Alignment.CenterStart
        ) {
            BasicTextField(
                value = value,
                onValueChange = onValueChange,
                modifier = Modifier.fillMaxWidth(),
                textStyle = MaterialTheme.typography.bodyMedium.copy(
                    color = MaterialTheme.colorScheme.onSurface
                ),
                singleLine = true,
                cursorBrush = SolidColor(MaterialTheme.colorScheme.primary),
                onTextLayout = { textLayoutResult ->
                    // 核心修复：使用 getLineRight(0) 获取第一行文字的实际结束位置，
                    // 而不是使用 textLayoutResult.size.width，后者在 fillMaxWidth 下会返回整个输入框宽度。
                    if (value.isNotEmpty()) {
                        textWidth = textLayoutResult.getLineRight(0)
                    } else {
                        textWidth = 0f
                    }
                },
                decorationBox = { innerTextField ->
                    Box(
                        modifier = Modifier.fillMaxSize(),
                        contentAlignment = Alignment.CenterStart
                    ) {
                        if (value.isEmpty() && !isFocused) {
                            Text(
                                text = hint,
                                style = MaterialTheme.typography.bodyMedium,
                                color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.5f)
                            )
                        }
                        
                        // 文字主体带上位移动画
                        Box(
                            modifier = Modifier
                                .fillMaxWidth()
                                .offset(y = textVerticalOffset),
                            contentAlignment = Alignment.CenterStart
                        ) {
                            innerTextField()
                            
                            // 指示线：相对于文字底部定位
                            if (indicatorWidth > 0.1f) {
                                Box(
                                    modifier = Modifier
                                        .align(Alignment.BottomStart)
                                        .offset(y = 2.dp) // 这里的 offset 是相对于 innerTextField 底部的
                                        .width(with(LocalDensity.current) { indicatorWidth.toDp() })
                                        .height(2.dp)
                                        .background(indicatorColor, RoundedCornerShape(100))
                                )
                            }
                        }
                    }
                }
            )
        }
    }
}

/**
 * 一个带背景的文字标签
 *
 * @param title 标签文本
 * @param icon 标签图标，默认为空
 * @param backgroundColor 背景颜色，默认为主题色
 * @param modifier 修饰符
 */
@Composable
fun BackgroundTextTag(
    title: String,
    icon: ImageVector? = null,
    backgroundColor: Color = MaterialTheme.colorScheme.primary.copy(alpha = 0.1f),
    modifier: Modifier = Modifier
) {
    Row(
        modifier =
            modifier
                .clip(RoundedCornerShape(6.dp))
                .background(backgroundColor)
                .padding(horizontal = 10.dp, vertical = 6.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        if (icon != null) {
            Icon(
                imageVector = icon,
                contentDescription = null,
                modifier = Modifier.size(14.dp),
                tint = MaterialTheme.colorScheme.primary
            )
            Spacer(modifier = Modifier.width(2.dp))
        }
        Text(
            text = title,
            style = MaterialTheme.typography.labelMedium,
            color =MaterialTheme.colorScheme.primary,
            fontWeight = FontWeight.Medium
        )
    }
}

/**
 * 一个带标题的分割线
 *
 * @param title 标题文本
 * @param modifier 修饰符
 */
@Composable
fun TitledDivider(title: String, modifier: Modifier = Modifier) {
        Row(modifier = modifier.fillMaxWidth(), verticalAlignment = Alignment.CenterVertically) {
                Text(
                    text = title,
                    style = MaterialTheme.typography.titleSmall,
                    color = MaterialTheme.colorScheme.primary,
                    fontWeight = FontWeight.Bold
                )
                Spacer(modifier = Modifier.width(16.dp))
                Box(
                        modifier =
                                Modifier
                                    .weight(1f)
                                    .height(2.dp)
                                    .clip(RoundedCornerShape(1.dp))
                                    .background(MaterialTheme.colorScheme.outlineVariant)
                )
        }
}

/**
 * 一个弹出式的容器卡片组件
 *
 * 该组件提供了一个可动画的弹出式卡片，支持模糊效果（如果 [LocalCardLayoutConfig.isCardBlurEnabled] 为 true 且 Android 版本 >= S），
 * 并且在显示/隐藏时带有弹簧动画效果
 *
 * @param visible 控制弹出卡片的可见性
 * @param onDismissRequest 当用户请求关闭弹出卡片时调用的回调
 * @param modifier 应用于弹出卡片内容的修饰符
 * @param alignment 弹出卡片在屏幕上的对齐方式
 * @param content 弹出卡片内部显示的内容
 */
@Composable
fun PopupContainer(
    visible: Boolean,
    onDismissRequest: () -> Unit,
    modifier: Modifier = Modifier,
    width: Dp = 300.dp,
    height: Dp = 300.dp,
    alignment: Alignment = Alignment.Center,
    content: @Composable () -> Unit
) {
    val (isCardBlurEnabled, cardAlpha, hazeState) = LocalCardLayoutConfig.current
    val visibleState = remember { MutableTransitionState(false) }

    LaunchedEffect(visible) { visibleState.targetState = visible }

    if (visibleState.currentState || visibleState.targetState) {
        Popup(
            onDismissRequest = onDismissRequest,
            properties = PopupProperties(focusable = true),
            alignment = alignment
        ) {
            AnimatedVisibility(
                visibleState = visibleState,
                enter =
                    fadeIn(
                        animationSpec =
                            spring(
                                stiffness =
                                    Spring.StiffnessMediumLow
                            )
                    ) +
                            scaleIn(
                                animationSpec =
                                    spring(
                                        dampingRatio =
                                            Spring.DampingRatioMediumBouncy,
                                        stiffness =
                                            Spring.StiffnessLow
                                    ),
                                initialScale = 0.8f
                            ),
                exit =
                    fadeOut(
                        animationSpec =
                            spring(
                                stiffness =
                                    Spring.StiffnessMediumLow
                            )
                    ) +
                            scaleOut(
                                animationSpec =
                                    spring(
                                        stiffness =
                                            Spring.StiffnessMediumLow
                                    ),
                                targetScale = 0.8f
                            )
            ) {
                val popupShape = RoundedCornerShape(16.dp)
                Card(
                    modifier =
                        modifier.width(width).height(height)
                            .then(
                            if (isCardBlurEnabled &&
                                Build.VERSION
                                    .SDK_INT >=
                                Build.VERSION_CODES
                                    .S
                            ) {
                                Modifier
                                    .clip(popupShape)
                                    .hazeEffect(
                                        state =
                                            hazeState
                                    )
                            } else Modifier
                        ),
                    shape = popupShape,
                    colors =
                        CardDefaults.cardColors(
                            containerColor = if (isCardBlurEnabled) {
                                MaterialTheme.colorScheme.surface
                                    .copy(alpha = 0.8f)
                            } else {
                                MaterialTheme.colorScheme.surface
                            }
                        )
                ) { content() }
            }
        }
    }
}

/**
 * 一个滚动条指示器，用于在可滑动列表中指示当前位置
 *
 * @param listState 列表的状态，用于获取滚动位置
 * @param modifier 修饰符
 * @param orientation 滚动方向，默认为竖向
 * @param indicatorLength 指示器的固定长度，默认为屏幕的一半不到
 */
@Composable
fun ScrollIndicator(
    listState: LazyListState,
    modifier: Modifier = Modifier,
    orientation: Orientation = Orientation.Vertical,
    indicatorLength: Dp? = null
) {
    val (isCardBlurEnabled, _, hazeState) = LocalCardLayoutConfig.current
    val scope = rememberCoroutineScope()
    val config = LocalConfiguration.current
    val density = LocalDensity.current

    // 计算指示器的总长度
    val totalLength = indicatorLength ?: if (orientation == Orientation.Vertical) {
        (config.screenHeightDp.dp / 2.5f)
    } else {
        (config.screenWidthDp.dp / 2.5f)
    }

    val totalLengthPx = with(density) { totalLength.toPx() }

    // 滚动与交互状态管理
    var isVisible by remember { mutableStateOf(false) }
    var isInteracting by remember { mutableStateOf(false) }
    // 拖动时的临时进度，用于解决拖动时滑块抖动的问题
    var dragProgress by remember { mutableStateOf(0f) }

    LaunchedEffect(listState.isScrollInProgress, isInteracting) {
        val canScroll = listState.canScrollForward || listState.canScrollBackward
        if (canScroll && (listState.isScrollInProgress || isInteracting)) {
            isVisible = true
        } else {
            // 当停止滚动且没有交互时，延迟 1 秒消失
            kotlinx.coroutines.delay(1000)
            isVisible = false
        }
    }

    // 如果列表为空，则不显示滚动条
    if (listState.layoutInfo.totalItemsCount == 0) return

    // 计算实际列表位置的进度
    val listProgress by remember {
        derivedStateOf {
            val layoutInfo = listState.layoutInfo
            val totalItemsCount = layoutInfo.totalItemsCount
            if (totalItemsCount == 0) return@derivedStateOf 0f

            val visibleItems = layoutInfo.visibleItemsInfo
            if (visibleItems.isEmpty()) return@derivedStateOf 0f

            val avgItemSize = visibleItems.sumOf { it.size } / visibleItems.size.toFloat()
            val estimatedTotalHeight = avgItemSize * totalItemsCount
            val viewportHeight = if (orientation == Orientation.Vertical) layoutInfo.viewportSize.height else layoutInfo.viewportSize.width
            
            val scrollableHeight = (estimatedTotalHeight - viewportHeight).coerceAtLeast(1f)
            val currentOffset = listState.firstVisibleItemIndex * avgItemSize + listState.firstVisibleItemScrollOffset
            
            (currentOffset / scrollableHeight).coerceIn(0f, 1f)
        }
    }

    // 确定当前应该显示的进度：交互时使用拖动进度（无抖动），否则使用列表进度
    // 使用 animateFloatAsState 平滑切换，避免状态切换时跳变
    val progress by animateFloatAsState(
        targetValue = if (isInteracting) dragProgress else listProgress,
        label = "ProgressAnimation",
        animationSpec = spring(stiffness = Spring.StiffnessMediumLow) 
    )

    // 动画显示
    AnimatedVisibility(
        visible = isVisible,
        enter = fadeIn(animationSpec = tween(500)),
        exit = fadeOut(animationSpec = tween(500)),
        modifier = modifier
    ) {
        // 弹动放大的动画状态
        val scale by animateFloatAsState(
            targetValue = if (isInteracting) 1.2f else 1f,
            animationSpec = spring(dampingRatio = Spring.DampingRatioNoBouncy),
            label = "ExpandAnimation"
        )

        // 尺寸定义
        val trackWidth = 8.dp * scale
        val thumbLength = 32.dp

        Box(
            modifier = Modifier
                .padding(8.dp)
                .then(
                    if (orientation == Orientation.Vertical)
                        Modifier.height(totalLength).width(48.dp) // 增大触控区域
                    else
                        Modifier.width(totalLength).height(48.dp)
                )
                .pointerInput(orientation, totalLengthPx) {
                    detectDragGestures(
                        onDragStart = { 
                            isInteracting = true 
                            // 开始拖动时，同步当前列表进度到拖动进度，避免跳变
                            dragProgress = listProgress
                        },
                        onDragEnd = { isInteracting = false },
                        onDragCancel = { isInteracting = false },
                        onDrag = { change, _ ->
                            change.consume()
                            val dragPosition = if (orientation == Orientation.Vertical) change.position.y else change.position.x
                            
                            // 更新拖动进度
                            val dragPercent = (dragPosition / totalLengthPx).coerceIn(0f, 1f)
                            dragProgress = dragPercent

                            val layoutInfo = listState.layoutInfo
                            val totalItems = layoutInfo.totalItemsCount
                            if (totalItems > 0) {
                                val visibleItems = layoutInfo.visibleItemsInfo
                                val avgSize = if (visibleItems.isEmpty()) 1f else visibleItems.sumOf { it.size } / visibleItems.size.toFloat()
                                val viewportHeight = if (orientation == Orientation.Vertical) layoutInfo.viewportSize.height else layoutInfo.viewportSize.width
                                val estimatedHeight = avgSize * totalItems
                                val targetOffset = dragPercent * (estimatedHeight - viewportHeight)
                                
                                val targetIndex = (targetOffset / avgSize).toInt().coerceIn(0, totalItems - 1)
                                val targetOffsetInItem = (targetOffset % avgSize).toInt()
                                
                                scope.launch {
                                    listState.scrollToItem(targetIndex, targetOffsetInItem)
                                }
                            }
                        }
                    )
                }
                .pointerInput(orientation, totalLengthPx) {
                    detectTapGestures { offset ->
                        val tapPosition = if (orientation == Orientation.Vertical) offset.y else offset.x
                        val tapPercent = (tapPosition / totalLengthPx).coerceIn(0f, 1f)
                        
                        val layoutInfo = listState.layoutInfo
                        val totalItems = layoutInfo.totalItemsCount
                        if (totalItems > 0) {
                            val visibleItems = layoutInfo.visibleItemsInfo
                            val avgSize = if (visibleItems.isEmpty()) 1f else visibleItems.sumOf { it.size } / visibleItems.size.toFloat()
                            val viewportHeight = if (orientation == Orientation.Vertical) layoutInfo.viewportSize.height else layoutInfo.viewportSize.width
                            val estimatedHeight = avgSize * totalItems
                            val targetOffset = tapPercent * (estimatedHeight - viewportHeight)
                            
                            val targetIndex = (targetOffset / avgSize).toInt().coerceIn(0, totalItems - 1)
                            val targetOffsetInItem = (targetOffset % avgSize).toInt()
                            
                            scope.launch {
                                listState.animateScrollToItem(targetIndex, targetOffsetInItem)
                            }
                        }
                    }
                }
        ) {
            // 轨道背景
            Box(
                modifier = Modifier
                    .align(Alignment.Center)
                    .then(
                        if (orientation == Orientation.Vertical)
                            Modifier.height(totalLength).width(trackWidth)
                        else
                            Modifier.width(totalLength).height(trackWidth)
                    )
                    .clip(RoundedCornerShape(100))
                    .then(
                        if (isCardBlurEnabled && Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
                            Modifier
                                .hazeEffect(state = hazeState)
                                .background(MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.3f))
                        } else {
                            Modifier.background(MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f))
                        }
                    )
            ) {
                // 滑块 (Thumb)
                val maxOffsetPx = with(density) { (totalLength - thumbLength).toPx() }
                
                Box(
                    modifier = Modifier
                        .align(Alignment.TopStart)
                        .graphicsLayer {
                            val offset = maxOffsetPx * progress
                            if (orientation == Orientation.Vertical) {
                                translationY = offset
                            } else {
                                translationX = offset
                            }
                        }
                        .size(
                            width = if (orientation == Orientation.Vertical) trackWidth else thumbLength,
                            height = if (orientation == Orientation.Vertical) thumbLength else trackWidth
                        )
                        .glow(
                            color = MaterialTheme.colorScheme.primary,
                            cornerRadius = 100.dp,
                            blurRadius = if (isInteracting) 16.dp else 8.dp,
                            enabled = true
                        )
                        .background(
                            color = MaterialTheme.colorScheme.primary,
                            shape = RoundedCornerShape(100)
                        )
                )
            }
        }
    }
}

// 辅助Modifier，用于定义形状
private fun Modifier.contentShape(orientation: Orientation, length: Dp, thickness: Dp): Modifier = composed {
    if (orientation == Orientation.Vertical) {
        this.size(thickness, length)
    } else {
        this.size(length, thickness)
    }
}
