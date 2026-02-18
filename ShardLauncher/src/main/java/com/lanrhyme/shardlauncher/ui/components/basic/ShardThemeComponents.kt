package com.lanrhyme.shardlauncher.ui.components.basic

import android.os.Build
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ColumnScope
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.ButtonColors
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Shape
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.VisualTransformation
import androidx.compose.ui.unit.DpOffset
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.PopupProperties
import com.lanrhyme.shardlauncher.ui.components.layout.LocalCardLayoutConfig
import dev.chrisbanes.haze.hazeEffect

/**
 * 这里是ShardTheme的基础组件
 * 使用时优先使用这些，而不是md3的
 *
 * 注意：卡片和按钮组件已迁移至：
 * - Cards.kt: ShardCard, ExpandableCard, InfoCard, ActionCard
 * - Buttons.kt: ShardButton, ShardIconButton
 * - Dialogs.kt: ShardDialog, ShardAlertDialog
 */

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
 * 符合 ShardTheme 风格的下拉菜单项
 *
 * @param text 菜单项文本
 * @param onClick 点击回调
 * @param modifier 修饰符
 * @param leadingIcon 前置图标
 * @param trailingIcon 后置图标
 * @param enabled 是否启用
 */
@Composable
fun ShardDropdownMenuItem(
    text: String,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    leadingIcon: @Composable (() -> Unit)? = null,
    trailingIcon: @Composable (() -> Unit)? = null,
    enabled: Boolean = true
) {
    DropdownMenuItem(
        text = { Text(text) },
        onClick = onClick,
        modifier = modifier,
        leadingIcon = leadingIcon,
        trailingIcon = trailingIcon,
        enabled = enabled
    )
}

/**
 * 一个符合 ShardTheme 风格的通用输入框组件
 *
 * 该组件提供了一个带有圆角背景和提示文本的输入框，用于用户输入文本
 * 它使用 [BasicTextField] 作为基础，并遵循 ShardTheme 的视觉规范，圆角大小为 16dp
 *
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
                        color = if (isError) {
                            MaterialTheme.colorScheme.error
                        } else {
                            MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.3f)
                        },
                        shape = shape
                    )
                    .background(
                        if (isError) {
                            MaterialTheme.colorScheme.error.copy(alpha = 0.1f)
                        } else {
                            MaterialTheme.colorScheme.surfaceVariant.copy(
                                alpha = (cardAlpha * 0.6f).coerceAtLeast(0.1f)
                            )
                        },
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
                            color = if (isError) {
                                MaterialTheme.colorScheme.error
                            } else {
                                MaterialTheme.colorScheme.primary
                            }
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

// ==================== 废弃组件 ====================

/**
 * @deprecated 使用 [ShardCard] 替代，位于 Cards.kt
 */
@Deprecated(
    "使用 ShardCard 替代，位于 Cards.kt",
    ReplaceWith("ShardCard(modifier, enabled, shape, style, onClick, border, contentPadding, content)",
        "com.lanrhyme.shardlauncher.ui.components.basic.ShardCard",
        "com.lanrhyme.shardlauncher.ui.components.basic.CardStyle")
)
@Composable
fun ShardCard(
    modifier: Modifier = Modifier,
    enabled: Boolean = true,
    shape: Shape = RoundedCornerShape(20.dp),
    border: Boolean = true,
    content: @Composable ColumnScope.() -> Unit
) {
    // 转发到新的实现
    com.lanrhyme.shardlauncher.ui.components.basic.ShardCard(
        modifier = modifier,
        enabled = enabled,
        shape = shape,
        style = CardStyle.DEFAULT,
        border = border,
        content = content
    )
}

/**
 * @deprecated 使用 [ShardButton] 替代，位于 Buttons.kt
 */
@Deprecated(
    "使用 ShardButton 替代，位于 Buttons.kt",
    ReplaceWith("ShardButton(onClick, modifier, type, size, enabled, shape, colors, contentPadding, interactionSource, content)",
        "com.lanrhyme.shardlauncher.ui.components.basic.ShardButton",
        "com.lanrhyme.shardlauncher.ui.components.basic.ButtonType")
)
@Composable
fun ShardButton(
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    enabled: Boolean = true,
    shape: Shape = RoundedCornerShape(16.dp),
    colors: ButtonColors? = null,
    contentPadding: PaddingValues = PaddingValues(horizontal = 24.dp, vertical = 8.dp),
    content: @Composable androidx.compose.foundation.layout.RowScope.() -> Unit
) {
    com.lanrhyme.shardlauncher.ui.components.basic.ShardButton(
        onClick = onClick,
        modifier = modifier,
        type = ButtonType.FILLED,
        size = ButtonSize.MEDIUM,
        enabled = enabled,
        shape = shape,
        colors = colors,
        contentPadding = contentPadding
    ) {
        content()
    }
}

/**
 * @deprecated 使用 [ShardDialog] 替代，位于 Dialogs.kt
 */
@Deprecated(
    "使用 ShardDialog 替代，位于 Dialogs.kt",
    ReplaceWith("ShardDialog(visible, onDismissRequest, modifier, size, customWidth, customHeight, dismissOnBackPress, dismissOnClickOutside, content)",
        "com.lanrhyme.shardlauncher.ui.components.basic.ShardDialog",
        "com.lanrhyme.shardlauncher.ui.components.basic.DialogSize")
)
@Composable
fun ShardDialog(
    visible: Boolean,
    onDismissRequest: () -> Unit,
    modifier: Modifier = Modifier,
    width: androidx.compose.ui.unit.Dp = 600.dp,
    height: androidx.compose.ui.unit.Dp = 380.dp,
    content: @Composable () -> Unit
) {
    com.lanrhyme.shardlauncher.ui.components.basic.ShardDialog(
        visible = visible,
        onDismissRequest = onDismissRequest,
        modifier = modifier,
        size = DialogSize.MEDIUM,
        customWidth = width,
        customHeight = height,
        content = content
    )
}

/**
 * @deprecated 使用 [ShardAlertDialog] 替代，位于 Dialogs.kt
 */
@Deprecated(
    "使用 ShardAlertDialog 替代，位于 Dialogs.kt",
    ReplaceWith("ShardAlertDialog(visible, title, text, onDismiss, onConfirm, onCancel, confirmText, cancelText, size)",
        "com.lanrhyme.shardlauncher.ui.components.basic.ShardAlertDialog")
)
@Composable
fun ShardAlertDialog(
    title: String,
    text: String,
    onDismiss: () -> Unit,
    onConfirm: (() -> Unit)? = null,
    onCancel: (() -> Unit)? = null,
    onDismissRequest: () -> Unit = onDismiss
) {
    androidx.compose.ui.window.Dialog(
        onDismissRequest = onDismissRequest,
        properties = androidx.compose.ui.window.DialogProperties(
            usePlatformDefaultWidth = false,
            decorFitsSystemWindows = false
        )
    ) {
        // 简化的兼容实现
        Box(
            modifier = Modifier.fillMaxWidth(),
            contentAlignment = Alignment.Center
        ) {
            Surface(
                modifier = Modifier.padding(24.dp),
                shape = RoundedCornerShape(16.dp),
                color = MaterialTheme.colorScheme.surface
            ) {
                Column(modifier = Modifier.padding(24.dp)) {
                    Text(
                        text = title,
                        style = MaterialTheme.typography.headlineSmall
                    )
                    Spacer(modifier = Modifier.height(16.dp))
                    Text(
                        text = text,
                        style = MaterialTheme.typography.bodyMedium
                    )
                    Spacer(modifier = Modifier.height(24.dp))
                    Row(
                        modifier = Modifier.align(Alignment.End),
                        horizontalArrangement = Arrangement.End
                    ) {
                        androidx.compose.material3.TextButton(onClick = onCancel ?: onDismiss) {
                            Text("取消")
                        }
                        if (onConfirm != null) {
                            Spacer(modifier = Modifier.width(8.dp))
                            androidx.compose.material3.TextButton(onClick = onConfirm) {
                                Text("确定")
                            }
                        }
                    }
                }
            }
        }
    }
}