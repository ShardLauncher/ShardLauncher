package com.lanrhyme.shardlauncher.ui.components.basic

import android.os.Build
import com.lanrhyme.shardlauncher.ui.components.layout.LocalCardLayoutConfig
import dev.chrisbanes.haze.hazeEffect
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.draw.clip
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ColumnScope
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.interaction.collectIsPressedAsState
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.RowScope
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonColors
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.SideEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Shape
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.graphics.toArgb
import androidx.compose.ui.platform.LocalView
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.VisualTransformation
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.DpOffset
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.PopupProperties
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import androidx.compose.ui.window.DialogWindowProvider
import androidx.core.view.WindowCompat
import androidx.core.view.WindowInsetsCompat
import androidx.core.view.WindowInsetsControllerCompat
import kotlinx.coroutines.delay


/**
 * 这里是ShardTheme的基础组件
 * 使用时优先使用这些，而不是md3的
 */


/**
 * 一个符合 ShardTheme 风格的基础卡片组件
 *
 * 该组件提供了一个带有圆角背景的卡片容器，支持模糊效果（如果 [LocalCardLayoutConfig.isCardBlurEnabled] 为 true 且 Android 版本 >= S），
 * 并根据主题颜色和卡片透明度进行渲染
 *
 * @param modifier 应用于卡片容器的修饰符
 * @param enabled 控制卡片是否启用，影响其透明度
 * @param shape 卡片的形状，默认为 20dp 的圆角
 * @param border 是否显示边框
 * @param content 卡片内部显示的内容
 */
@Composable
fun ShardCard(
    modifier: Modifier = Modifier,
    enabled: Boolean = true,
    shape: Shape = RoundedCornerShape(20.dp),
    border: Boolean = true,
    content: @Composable ColumnScope.() -> Unit
) {
    val (isCardBlurEnabled, cardAlpha, hazeState) = LocalCardLayoutConfig.current
    val cardModifier =
        if (isCardBlurEnabled && Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
            modifier
                .fillMaxWidth()
                .alpha(if (enabled) 1f else 0.5f)
                .clip(shape)
                .hazeEffect(state = hazeState)
        } else {
            modifier.fillMaxWidth().alpha(if (enabled) 1f else 0.5f)
        }

    Card(
        modifier = cardModifier,
        shape = shape,
        border = if (border) BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.3f)) else null,
        colors =
            CardDefaults.cardColors(
                containerColor =
                    MaterialTheme.colorScheme.surface.copy(alpha = cardAlpha)
            ),
        content = content
    )
}

/**
 * 极其高级的玻璃拟态卡片，带有细微的发光边框和增强的模糊感
 */
@Composable
fun ShardGlassCard(
    modifier: Modifier = Modifier,
    onClick: (() -> Unit)? = null,
    shape: Shape = RoundedCornerShape(24.dp),
    content: @Composable ColumnScope.() -> Unit
) {
    val (isCardBlurEnabled, cardAlpha, hazeState) = LocalCardLayoutConfig.current
    
    val baseModifier = modifier
        .fillMaxWidth()
        .then(
            if (onClick != null) Modifier.clickable(onClick = onClick) else Modifier
        )
        .clip(shape)

    val finalModifier = if (isCardBlurEnabled && Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
        baseModifier.hazeEffect(state = hazeState)
    } else baseModifier

    Surface(
        modifier = finalModifier,
        shape = shape,
        color = MaterialTheme.colorScheme.surface.copy(alpha = cardAlpha),
        border = BorderStroke(
            0.5.dp, 
            Brush.verticalGradient(
                listOf(
                    Color.White.copy(alpha = 0.2f),
                    Color.White.copy(alpha = 0.05f)
                )
            )
        )
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            content()
        }
    }
}

/**
 * 高级部分标题，通常用于列表或设置页面的分组
 */
@Composable
fun ShardSectionHeader(
    title: String,
    modifier: Modifier = Modifier,
    trailing: @Composable (() -> Unit)? = null
) {
    Row(
        modifier = modifier
            .fillMaxWidth()
            .padding(horizontal = 8.dp, vertical = 12.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.SpaceBetween
    ) {
        Text(
            text = title,
            style = MaterialTheme.typography.titleMedium,
            fontWeight = FontWeight.ExtraBold,
            color = MaterialTheme.colorScheme.primary
        )
        trailing?.invoke()
    }
}

/**
 * 高级标签/徽章组件
 */
@Composable
fun ShardTag(
    text: String,
    modifier: Modifier = Modifier,
    containerColor: Color = MaterialTheme.colorScheme.secondaryContainer.copy(alpha = 0.5f),
    contentColor: Color = MaterialTheme.colorScheme.onSecondaryContainer
) {
    Surface(
        color = containerColor,
        contentColor = contentColor,
        shape = RoundedCornerShape(8.dp),
        modifier = modifier
    ) {
        Text(
            text = text,
            modifier = Modifier.padding(horizontal = 8.dp, vertical = 2.dp),
            style = MaterialTheme.typography.labelSmall,
            fontWeight = FontWeight.Bold
        )
    }
}

/**
 * 按钮类型枚举
 * 定义了 ShardButton 支持的三种样式
 */
enum class ButtonType {
    FILLED,
    OUTLINED,
    TEXT
}

/**
 * 符合 ShardTheme 风格的按钮组件
 * 带有触觉反馈缩放动画
 *
 * @param onClick 点击回调
 * @param modifier 修饰符
 * @param type 按钮类型，参考 [ButtonType]
 * @param enabled 是否启用
 * @param shape 按钮形状
 * @param colors 按钮颜色配置
 * @param contentPadding 内容内边距
 * @param content 按钮内容
 */
@Composable
fun ShardButton(
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    type: ButtonType = ButtonType.FILLED,
    enabled: Boolean = true,
    shape: Shape = RoundedCornerShape(16.dp),
    colors: ButtonColors? = null,
    contentPadding: PaddingValues = ButtonDefaults.ContentPadding,
    content: @Composable RowScope.() -> Unit
) {
    val (isCardBlurEnabled, _, hazeState) = LocalCardLayoutConfig.current
    val interactionSource = remember { MutableInteractionSource() }
    val isPressed by interactionSource.collectIsPressedAsState()
    
    val scale by animateFloatAsState(
        targetValue = if (isPressed) 0.96f else 1f,
        label = "ButtonScale"
    )

    val buttonModifier = Modifier
        .graphicsLayer(scaleX = scale, scaleY = scale)
        .then(
            if (isCardBlurEnabled && Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
                modifier.clip(shape).hazeEffect(state = hazeState)
            } else {
                modifier
            }
        )

    val contentWithWeight: @Composable RowScope.() -> Unit = {
        content()
    }

    when (type) {
        ButtonType.FILLED -> {
            val defaultColors = if (isCardBlurEnabled) {
                ButtonDefaults.buttonColors(
                    containerColor = MaterialTheme.colorScheme.primary.copy(alpha = 0.85f),
                    contentColor = MaterialTheme.colorScheme.onPrimary
                )
            } else {
                ButtonDefaults.buttonColors()
            }
            Button(
                onClick = onClick,
                modifier = buttonModifier,
                enabled = enabled,
                shape = shape,
                colors = colors ?: defaultColors,
                contentPadding = contentPadding,
                interactionSource = interactionSource,
                content = contentWithWeight
            )
        }
        ButtonType.OUTLINED -> {
            OutlinedButton(
                onClick = onClick,
                modifier = buttonModifier,
                enabled = enabled,
                shape = shape,
                colors = colors ?: ButtonDefaults.outlinedButtonColors(),
                contentPadding = contentPadding,
                interactionSource = interactionSource,
                content = contentWithWeight
            )
        }
        ButtonType.TEXT -> {
            TextButton(
                onClick = onClick,
                modifier = buttonModifier,
                enabled = enabled,
                shape = shape,
                colors = colors ?: ButtonDefaults.textButtonColors(),
                contentPadding = contentPadding,
                interactionSource = interactionSource,
                content = contentWithWeight
            )
        }
    }
}

/**
 * ShardTheme 对话框组件
 * 在复杂布局下具有较高性能
 *
 * @param visible 控制对话框的可见性
 * @param onDismissRequest 当用户请求关闭对话框时（例如点击对话框外部）调用的回调
 * @param modifier 应用于对话框内容表面的修饰符
 * @param width 对话框的最大宽度
 * @param height 对话框的最大高度
 * @param content 对话框内部显示的内容
 */
@Composable
fun ShardDialog(
    visible: Boolean,
    onDismissRequest: () -> Unit,
    modifier: Modifier = Modifier,
    width : Dp = 600.dp,
    height : Dp = 380.dp,
    content: @Composable () -> Unit
) {
    val (isCardBlurEnabled, _, hazeState) = LocalCardLayoutConfig.current
    val tween = tween<Float>(durationMillis = 300)

    var showDialog by remember { mutableStateOf(visible) }
    LaunchedEffect(visible) {
        if (visible) {
            showDialog = true
        } else {
            delay(300)
            showDialog = false
        }
    }

    if (showDialog) {
        Dialog(
            onDismissRequest = onDismissRequest,
            properties =
                DialogProperties(
                    usePlatformDefaultWidth = false,
                    decorFitsSystemWindows = false
                )
        ) {
            val view = LocalView.current
            val darkTheme = isSystemInDarkTheme()
            if (!view.isInEditMode) {
                SideEffect {
                    @Suppress("DEPRECATION")
                    val window = (view.parent as? DialogWindowProvider)?.window
                    if (window != null) {
                        WindowCompat.setDecorFitsSystemWindows(
                            window,
                            false
                        )
                        val insetsController =
                            WindowCompat.getInsetsController(
                                window,
                                view
                            )
                        insetsController.hide(
                            WindowInsetsCompat.Type.systemBars()
                        )
                        insetsController.systemBarsBehavior =
                            WindowInsetsControllerCompat
                                .BEHAVIOR_SHOW_TRANSIENT_BARS_BY_SWIPE
                        @Suppress("DEPRECATION")
                        window.statusBarColor = Color.Transparent.toArgb()
                        @Suppress("DEPRECATION")
                        window.navigationBarColor =
                            Color.Transparent.toArgb()
                        insetsController.isAppearanceLightStatusBars =
                            !darkTheme
                    }
                }
            }

            AnimatedVisibility(
                visible = visible,
                enter = fadeIn(tween),
                exit = fadeOut(tween)
            ) {
                Box(
                    modifier =
                        Modifier.fillMaxSize()
                            .clickable(
                                indication = null,
                                interactionSource =
                                    remember {
                                        MutableInteractionSource()
                                    },
                                onClick = onDismissRequest
                            ),
                    contentAlignment = Alignment.Center
                ) {
                    val bgAlpha by
                    animateFloatAsState(
                        targetValue = if (visible) 0.4f else 0f,
                        animationSpec = tween,
                    )
                    Box(
                        modifier =
                            Modifier.fillMaxSize()
                                .graphicsLayer { alpha = bgAlpha }
                                .background(Color.Black)
                    )

                    val progress by
                    animateFloatAsState(
                        targetValue = if (visible) 1f else 0f,
                        animationSpec = tween,
                    )
                    val dialogShape = RoundedCornerShape(16.dp)
                    Surface(
                        modifier =
                            modifier
                                .graphicsLayer {
                                    alpha = progress
                                    translationY =
                                        (1f - progress) *
                                                80.dp.toPx()
                                }
                                .widthIn(max = width)
                                .heightIn(max = height)
                                .then(
                                    if (isCardBlurEnabled &&
                                        Build.VERSION
                                            .SDK_INT >=
                                        Build.VERSION_CODES
                                            .S
                                    ) {
                                        Modifier.clip(
                                            dialogShape
                                        )
                                            .hazeEffect(
                                                state =
                                                    hazeState
                                            )
                                    } else Modifier
                                )
                                .clickable(
                                    indication = null,
                                    interactionSource =
                                        remember {
                                            MutableInteractionSource()
                                        },
                                    onClick = {}
                                ),
                        shape = dialogShape,
                        color = if (isCardBlurEnabled) {
                            MaterialTheme.colorScheme.surface.copy(
                                alpha = 0.9f
                            )
                        } else {
                            MaterialTheme.colorScheme.surface
                        }
                    ) { Box(contentAlignment = Alignment.Center) { content() } }
                }
            }
        }
    }
}

/**
 * 符合 ShardTheme 风格的下拉菜单组件
 *
 * @param expanded 是否展开
 * @param onDismissRequest 关闭请求回调
 * @param modifier 修饰符
 * @param offset 偏移量
 * @param shape 菜单形状
 * @param border 边框
 * @param properties 弹出窗口属性
 * @param content 菜单内容
 */
@Composable
fun ShardDropdownMenu(
    expanded: Boolean,
    onDismissRequest: () -> Unit,
    modifier: Modifier = Modifier,
    offset: DpOffset = DpOffset(0.dp, 0.dp),
    shape: Shape = RoundedCornerShape(16.dp),
    border: BorderStroke? = null,
    properties: PopupProperties = PopupProperties(focusable = true),
    content: @Composable ColumnScope.() -> Unit
) {
    val (isCardBlurEnabled, _, hazeState) = LocalCardLayoutConfig.current
    val resolvedColor =
        if (isCardBlurEnabled && Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
            MaterialTheme.colorScheme.surface.copy(alpha = 0.9f)
        } else {
            MaterialTheme.colorScheme.surface
        }
    val menuModifier =
        if (isCardBlurEnabled && Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
            modifier.clip(shape).hazeEffect(state = hazeState)
        } else {
            modifier
        }
    DropdownMenu(
        expanded = expanded,
        onDismissRequest = onDismissRequest,
        modifier = menuModifier,
        offset = offset,
        shape = shape,
        containerColor = resolvedColor,
        tonalElevation = 0.dp,
        shadowElevation = 0.dp,
        border = border,
        properties = properties,
        content = content
    )
}


/**
 * 一个符合 ShardTheme 风格的通用输入框组件
 *
 * 该组件提供了一个带有圆角背景和提示文本的输入框，用于用户输入文本
 * 它使用 [BasicTextField] 作为基础，并遵循 ShardTheme 的视觉规范，圆角大小为 16dp

 * @param value 当前文本字段的值
 * @param onValueChange 当文本字段的值发生变化时调用的回调
 * @param modifier 应用于整个输入框的修饰符
 * @param enabled 控制输入框是否启用
 * @param readOnly 控制输入框是否只读
 * @param textStyle 用于文本字段的 [TextStyle]
 * @param label 可选的标签
 * @param placeholder 可选的占位符
 * @param leadingIcon 可选的前置图标 Composable
 * @param trailingIcon 可选的后置图标 Composable
 * @param isError 是否显示错误状态
 * @param visualTransformation 用于视觉转换输入文本，例如密码点
 * @param keyboardOptions 用于配置键盘行为的选项
 * @param keyboardActions 用于处理键盘动作的回调
 * @param singleLine 如果为 true，则文本字段将限制为单行
 * @param maxLines 文本字段允许的最大行数
 * @param shape 输入框的形状
 */
@Composable
fun ShardInputField(
    value: String,
    onValueChange: (String) -> Unit,
    modifier: Modifier = Modifier,
    enabled: Boolean = true,
    readOnly: Boolean = false,
    textStyle: TextStyle = MaterialTheme.typography.bodyMedium.copy(color = MaterialTheme.colorScheme.onSurface),
    label: String? = null,
    placeholder: String? = null,
    leadingIcon: @Composable (() -> Unit)? = null,
    trailingIcon: @Composable (() -> Unit)? = null,
    isError: Boolean = false,
    visualTransformation: VisualTransformation = VisualTransformation.None,
    keyboardOptions: KeyboardOptions = KeyboardOptions.Default,
    keyboardActions: KeyboardActions = KeyboardActions.Default,
    singleLine: Boolean = true,
    maxLines: Int = if (singleLine) 1 else Int.MAX_VALUE,
    shape: Shape = RoundedCornerShape(16.dp),
) {
    val (isCardBlurEnabled, cardAlpha, hazeState) = LocalCardLayoutConfig.current
    BasicTextField(
        value = value,
        onValueChange = onValueChange,
        modifier = modifier,
        enabled = enabled,
        readOnly = readOnly,
        textStyle = textStyle,
        cursorBrush = SolidColor(MaterialTheme.colorScheme.primary),
        visualTransformation = visualTransformation,
        keyboardOptions = keyboardOptions,
        keyboardActions = keyboardActions,
        singleLine = singleLine,
        maxLines = maxLines,
        decorationBox = { innerTextField ->
            Row(
                modifier =
                    Modifier
                        .then(
                            if (isCardBlurEnabled && Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
                                Modifier
                                    .clip(shape)
                                     .hazeEffect(state = hazeState)
                             } else Modifier
                         )
                         .border(
                             width = 1.dp,
                             color = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.3f),
                             shape = shape
                         )
                         .background(
                             MaterialTheme.colorScheme.surfaceVariant.copy(
                                 alpha = (cardAlpha * 0.6f).coerceAtLeast(0.1f)
                             ),
                             shape
                         )
                         .padding(horizontal = 16.dp, vertical = 12.dp)
                        .fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically
            ) {
                if (leadingIcon != null) {
                    leadingIcon()
                    Spacer(modifier = Modifier.width(8.dp))
                }
                Column(
                    modifier = Modifier.weight(1f),
                    verticalArrangement = Arrangement.Center
                ) {
                    if (label != null) {
                        Text(
                            text = label,
                            style = MaterialTheme.typography.labelMedium,
                            color = MaterialTheme.colorScheme.primary
                        )
                    }
                    Box(
                        contentAlignment = Alignment.CenterStart
                    ) {
                        if (value.isEmpty() && placeholder != null) {
                            Text(
                                text = placeholder,
                                style = textStyle,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                        innerTextField()
                    }
                }
                if (trailingIcon != null) {
                    Spacer(modifier = Modifier.width(8.dp))
                    trailingIcon()
                }
            }
        }
    )
}

@Composable
private fun ShardDialogWindowSetup() {
    val view = LocalView.current
    val darkTheme = isSystemInDarkTheme()
    if (!view.isInEditMode) {
        SideEffect {
            @Suppress("DEPRECATION")
            val window = (view.parent as? DialogWindowProvider)?.window
            if (window != null) {
                WindowCompat.setDecorFitsSystemWindows(
                    window,
                    false
                )
                val insetsController =
                    WindowCompat.getInsetsController(
                        window,
                        view
                    )
                insetsController.hide(
                    WindowInsetsCompat.Type.systemBars()
                )
                insetsController.systemBarsBehavior =
                    WindowInsetsControllerCompat
                        .BEHAVIOR_SHOW_TRANSIENT_BARS_BY_SWIPE
                @Suppress("DEPRECATION")
                window.statusBarColor = Color.Transparent.toArgb()
                @Suppress("DEPRECATION")
                window.navigationBarColor =
                    Color.Transparent.toArgb()
                insetsController.isAppearanceLightStatusBars =
                    !darkTheme
            }
        }
    }
}

@Composable
private fun ShardDialogContentWrapper(
    onDismissRequest: () -> Unit,
    modifier: Modifier = Modifier,
    content: @Composable () -> Unit
) {
    ShardDialogWindowSetup()

    var visible by remember { mutableStateOf(false) }
    LaunchedEffect(Unit) { visible = true }
    val tween = tween<Float>(durationMillis = 300)

    Box(
        modifier =
        Modifier.fillMaxSize()
            .clickable(
                indication = null,
                interactionSource =
                remember {
                    MutableInteractionSource()
                },
                onClick = onDismissRequest
            ),
        contentAlignment = Alignment.Center
    ) {
        val bgAlpha by
        animateFloatAsState(
            targetValue = if (visible) 0.4f else 0f,
            animationSpec = tween,
        )
        Box(
            modifier =
            Modifier.fillMaxSize()
                .graphicsLayer { alpha = bgAlpha }
                .background(Color.Black)
        )

        val progress by
        animateFloatAsState(
            targetValue = if (visible) 1f else 0f,
            animationSpec = tween,
        )
        val dialogShape = RoundedCornerShape(16.dp)
        val (isCardBlurEnabled, _, hazeState) = LocalCardLayoutConfig.current

        Surface(
            modifier = Modifier
                .padding(24.dp)
                .then(modifier)
                .graphicsLayer {
                    alpha = progress
                    translationY =
                        (1f - progress) *
                                80.dp.toPx()
                }
                .then(
                    if (isCardBlurEnabled &&
                        Build.VERSION
                            .SDK_INT >=
                        Build.VERSION_CODES
                            .S
                    ) {
                        Modifier.clip(
                            dialogShape
                        )
                            .hazeEffect(
                                state =
                                hazeState
                            )
                    } else Modifier
                )
                .clickable(
                    indication = null,
                    interactionSource =
                    remember {
                        MutableInteractionSource()
                    },
                    onClick = {}
                ),
            shape = dialogShape,
            color = if (isCardBlurEnabled) {
                MaterialTheme.colorScheme.surface.copy(
                    alpha = 0.9f
                )
            } else {
                MaterialTheme.colorScheme.surface
            }
        ) { Box(contentAlignment = Alignment.Center) { content() } }
    }
}

/**
 * 一个符合 ShardTheme 风格的通用提示对话框
 *
 * 基于 [ShardDialog] 实现，提供标题、正文与确认/取消操作区域
 *
 * @param title 对话框标题
 * @param text 对话框正文内容
 * @param onDismiss 关闭对话框时的回调
 * @param onConfirm 点击确认按钮时的回调，为 null 时不显示确认按钮
 * @param onCancel 点击取消按钮时的回调，为 null 时复用 [onDismiss]
 * @param onDismissRequest 外部关闭请求回调，默认等同于 [onDismiss]
 */
@Composable
fun ShardAlertDialog(
    title: String,
    text: String,
    onDismiss: () -> Unit,
    onConfirm: (() -> Unit)? = null,
    onCancel: (() -> Unit)? = null,
    onDismissRequest: () -> Unit = onDismiss
) {
    Dialog(
        onDismissRequest = onDismissRequest,
        properties =
            DialogProperties(
                usePlatformDefaultWidth = false,
                decorFitsSystemWindows = false
            )
    ) {
        ShardDialogContentWrapper(
            onDismissRequest = onDismissRequest,
            modifier = Modifier.widthIn(min = 280.dp, max = 560.dp)
        ) {
            Column(
                modifier = Modifier
                    .padding(24.dp)
            ) {
                Text(
                    text = title,
                    style = MaterialTheme.typography.headlineSmall,
                    color = MaterialTheme.colorScheme.onSurface
                )
                Spacer(modifier = Modifier.height(16.dp))
                Text(
                    text = text,
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
                Spacer(modifier = Modifier.height(24.dp))
                Row(
                    modifier = Modifier.align(Alignment.End),
                    horizontalArrangement = Arrangement.End
                ) {
                    TextButton(onClick = onCancel ?: onDismiss) {
                        Text("取消")
                    }
                    if (onConfirm != null) {
                        Spacer(modifier = Modifier.width(8.dp))
                        TextButton(onClick = onConfirm) {
                            Text("确定")
                        }
                    }
                }
            }
        }
    }
}


/**
 * 一个符合 ShardTheme 风格的通用提示对话框
 *
 * 基于 [ShardDialog] 实现，提供标题、正文与确认/取消操作区域
 *
 * @param title 对话框标题
 * @param text 对话框正文内容
 * @param onDismiss 关闭对话框时的回调
 * @param onConfirm 点击确认按钮时的回调，为 null 时不显示确认按钮
 * @param onCancel 点击取消按钮时的回调，为 null 时复用 [onDismiss]
 * @param onDismissRequest 外部关闭请求回调，默认等同于 [onDismiss]
 */
@Composable
fun ShardAlertDialog(
    title: String,
    text: @Composable (() -> Unit),
    onDismiss: () -> Unit,
    onConfirm: (() -> Unit)? = null,
    onCancel: (() -> Unit)? = null,
    onDismissRequest: () -> Unit = onDismiss
) {
    Dialog(
        onDismissRequest = onDismissRequest,
        properties =
            DialogProperties(
                usePlatformDefaultWidth = false,
                decorFitsSystemWindows = false
            )
    ) {
        ShardDialogContentWrapper(
            onDismissRequest = onDismissRequest,
            modifier = Modifier.widthIn(min = 280.dp, max = 560.dp)
        ) {
            Column(
                modifier = Modifier
                    .padding(24.dp)
            ) {
                Text(
                    text = title,
                    style = MaterialTheme.typography.headlineSmall,
                    color = MaterialTheme.colorScheme.onSurface
                )
                Spacer(modifier = Modifier.height(16.dp))
                text()
                Spacer(modifier = Modifier.height(24.dp))
                Row(
                    modifier = Modifier.align(Alignment.End),
                    horizontalArrangement = Arrangement.End
                ) {
                    TextButton(onClick = onCancel ?: onDismiss) {
                        Text("取消")
                    }
                    if (onConfirm != null) {
                        Spacer(modifier = Modifier.width(8.dp))
                        TextButton(onClick = onConfirm) {
                            Text("确定")
                        }
                    }
                }
            }
        }
    }
}

/**
 * 一个符合 ShardTheme 风格的输入对话框
 *
 * 基于 [ShardDialog] 实现，提供标题与输入框，适用于简单的文本编辑场景
 *
 * @param title 对话框标题
 * @param value 输入框当前值
 * @param onValueChange 输入内容变化时的回调
 * @param label 输入框标签，可选
 * @param isError 是否显示错误状态
 * @param supportingText 输入框辅助文本，可选
 * @param singleLine 是否限制为单行输入
 * @param onDismissRequest 关闭对话框时的回调
 * @param onConfirm 点击确认按钮时的回调
 */
@Composable
fun ShardEditDialog(
    title: String,
    value: String,
    onValueChange: (String) -> Unit,
    label: String? = null,
    isError: Boolean = false,
    supportingText: @Composable (() -> Unit)? = null,
    singleLine: Boolean = false,
    onDismissRequest: () -> Unit,
    onConfirm: () -> Unit
) {
    Dialog(
        onDismissRequest = onDismissRequest,
        properties =
            DialogProperties(
                usePlatformDefaultWidth = false,
                decorFitsSystemWindows = false
            )
    ) {
        ShardDialogContentWrapper(
            onDismissRequest = onDismissRequest,
            modifier = Modifier.widthIn(min = 280.dp, max = 560.dp)
        ) {
            Column(
                modifier = Modifier
                    .padding(24.dp)
            ) {
                Text(
                    text = title,
                    style = MaterialTheme.typography.headlineSmall,
                    color = MaterialTheme.colorScheme.onSurface
                )
                Spacer(modifier = Modifier.height(16.dp))
                ShardInputField(
                    value = value,
                    onValueChange = onValueChange,
                    label = label,
                    isError = isError,
                    singleLine = singleLine,
                    modifier = Modifier.fillMaxWidth()
                )
                Spacer(modifier = Modifier.height(24.dp))
                Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.End) {
                    TextButton(onClick = onDismissRequest) {
                        Text("取消")
                    }
                    Spacer(modifier = Modifier.width(8.dp))
                    TextButton(
                        onClick = onConfirm,
                        enabled = !isError
                    ) {
                        Text("确定")
                    }
                }
            }
        }
    }
}

/**
 * 一个符合 ShardTheme 风格的任务执行对话框
 *
 * 执行 [task] 时显示进度指示，成功后触发 [onDismiss]，失败时触发 [onError]
 *
 * @param title 对话框标题
 * @param task 需要执行的挂起任务
 * @param context 外部协程上下文
 * @param onDismiss 任务完成后关闭对话框的回调
 * @param onError 任务失败时的回调
 */
@Composable
fun ShardTaskDialog(
    title: String,
    task: suspend () -> Unit,
    context: kotlinx.coroutines.CoroutineScope,
    onDismiss: () -> Unit,
    onError: (Throwable) -> Unit
) {
    var isRunning by remember { mutableStateOf(true) }

    LaunchedEffect(Unit) {
        try {
            task()
            onDismiss()
        } catch (e: Throwable) {
            isRunning = false
            onError(e)
        }
    }

    if (isRunning) {
        Dialog(
            onDismissRequest = { },
            properties =
                DialogProperties(
                    usePlatformDefaultWidth = false,
                    decorFitsSystemWindows = false
                )
        ) {
            ShardDialogContentWrapper(
                onDismissRequest = { },
                modifier = Modifier.widthIn(min = 280.dp, max = 560.dp)
            ) {
                Column(
                    modifier = Modifier
                        .padding(24.dp)
                ) {
                    Text(
                        text = title,
                        style = MaterialTheme.typography.headlineSmall,
                        color = MaterialTheme.colorScheme.onSurface
                    )
                    Spacer(modifier = Modifier.height(16.dp))
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        CircularProgressIndicator(modifier = Modifier.size(24.dp))
                        Spacer(modifier = Modifier.width(16.dp))
                        Text(
                            text = "正在执行...",
                            style = MaterialTheme.typography.bodyMedium,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }
            }
        }
    }
}
