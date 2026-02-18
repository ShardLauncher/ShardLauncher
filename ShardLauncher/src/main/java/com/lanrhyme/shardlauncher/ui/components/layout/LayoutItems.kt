package com.lanrhyme.shardlauncher.ui.components.layout

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.expandVertically
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.shrinkVertically
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.interaction.collectIsDraggedAsState
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ExposedDropdownMenuDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ProvideTextStyle
import androidx.compose.material3.RadioButton
import androidx.compose.material3.Slider
import androidx.compose.material3.SliderDefaults
import androidx.compose.material3.Switch
import androidx.compose.material3.SwitchDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.ExperimentalComposeUiApi
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.scale
import androidx.compose.ui.focus.FocusRequester
import androidx.compose.ui.focus.focusRequester
import androidx.compose.ui.graphics.Shape
import androidx.compose.ui.platform.LocalSoftwareKeyboardController
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.DpSize
import androidx.compose.ui.unit.dp
import com.lanrhyme.shardlauncher.ui.components.basic.ShardInputField
import com.lanrhyme.shardlauncher.ui.components.basic.TitleAndSummary
import com.lanrhyme.shardlauncher.ui.components.basic.glow

// ==================== 开关类布局项 ====================

/**
 * 带开关的布局项组件（无卡片包装）
 *
 * @param checked 开关是否选中
 * @param onCheckedChange 开关状态改变时的回调
 * @param modifier 应用于组件的修饰符
 * @param title 标题文本
 * @param summary 摘要文本（可选）
 * @param enabled 是否启用
 */
@Composable
fun SwitchLayoutItem(
    checked: Boolean,
    onCheckedChange: () -> Unit,
    modifier: Modifier = Modifier,
    title: String,
    summary: String? = null,
    enabled: Boolean = true
) {
    val interactionSource = remember { MutableInteractionSource() }
    Row(
        modifier = modifier
            .fillMaxWidth()
            .clickable(
                interactionSource = interactionSource,
                indication = null,
                enabled = enabled,
                onClick = onCheckedChange
            )
            .padding(horizontal = 16.dp, vertical = 12.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.SpaceBetween
    ) {
        TitleAndSummary(
            modifier = Modifier.weight(1f),
            title = title,
            summary = summary
        )
        Spacer(modifier = Modifier.width(8.dp))
        Switch(
            checked = checked,
            onCheckedChange = { onCheckedChange() },
            enabled = enabled,
            colors = SwitchDefaults.colors(
                checkedThumbColor = MaterialTheme.colorScheme.secondary,
                checkedTrackColor = MaterialTheme.colorScheme.secondaryContainer,
                checkedIconColor = MaterialTheme.colorScheme.onSecondary,
                uncheckedThumbColor = MaterialTheme.colorScheme.outline,
                uncheckedTrackColor = MaterialTheme.colorScheme.surfaceVariant,
                uncheckedBorderColor = MaterialTheme.colorScheme.outline
            )
        )
    }
}

/**
 * 带图标和开关的布局项组件（无卡片包装）
 *
 * @param checked 开关是否选中
 * @param onCheckedChange 开关状态改变时的回调
 * @param onIconClick 图标点击时的回调
 * @param modifier 应用于组件的修饰符
 * @param icon 图标组件
 * @param title 标题文本
 * @param summary 摘要文本（可选）
 * @param enabled 是否启用
 */
@Composable
fun IconSwitchLayoutItem(
    checked: Boolean,
    onCheckedChange: () -> Unit,
    onIconClick: () -> Unit,
    modifier: Modifier = Modifier,
    icon: @Composable () -> Unit,
    title: String,
    summary: String? = null,
    enabled: Boolean = true
) {
    val interactionSource = remember { MutableInteractionSource() }
    Row(
        modifier = modifier
            .fillMaxWidth()
            .clickable(
                interactionSource = interactionSource,
                indication = null,
                enabled = enabled,
                onClick = onCheckedChange
            )
            .padding(horizontal = 16.dp, vertical = 12.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.SpaceBetween
    ) {
        TitleAndSummary(
            modifier = Modifier.weight(1f),
            title = title,
            summary = summary
        )
        Spacer(modifier = Modifier.width(8.dp))
        androidx.compose.material3.IconButton(onClick = onIconClick, enabled = enabled) { icon() }
        Switch(
            checked = checked,
            onCheckedChange = { onCheckedChange() },
            enabled = enabled,
            colors = SwitchDefaults.colors(
                checkedThumbColor = MaterialTheme.colorScheme.secondary,
                checkedTrackColor = MaterialTheme.colorScheme.secondaryContainer,
                checkedIconColor = MaterialTheme.colorScheme.onSecondary,
                uncheckedThumbColor = MaterialTheme.colorScheme.outline,
                uncheckedTrackColor = MaterialTheme.colorScheme.surfaceVariant,
                uncheckedBorderColor = MaterialTheme.colorScheme.outline
            )
        )
    }
}

// ==================== 列表选择类布局项 ====================

/**
 * 简单列表选择布局项组件（无卡片包装）
 *
 * @param modifier 应用于组件的修饰符
 * @param items 选项列表
 * @param selectedItem 当前选中的项
 * @param title 标题文本
 * @param getItemText 获取选项显示文本的函数
 * @param onValueChange 选中项改变时的回调
 * @param summary 摘要文本（可选），默认为选中项的文本
 * @param getItemSummary 获取选项摘要的函数（可选）
 * @param enabled 是否启用
 * @param autoCollapse 选中后是否自动收起
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun <E> SimpleListLayoutItem(
    modifier: Modifier = Modifier,
    items: List<E>,
    selectedItem: E,
    title: String,
    getItemText: @Composable (E) -> String,
    onValueChange: (E) -> Unit,
    summary: String? = null,
    getItemSummary: (@Composable (E) -> Unit)? = null,
    enabled: Boolean = true,
    autoCollapse: Boolean = true
) {
    var expanded by remember { mutableStateOf(false) }

    Column(modifier = modifier.fillMaxWidth()) {
        Row(
            modifier = Modifier
                .clickable(enabled = enabled) { expanded = !expanded }
                .padding(horizontal = 16.dp, vertical = 12.dp)
                .fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            TitleAndSummary(
                modifier = Modifier.weight(1f),
                title = title,
                summary = summary ?: getItemText(selectedItem)
            )
            ExposedDropdownMenuDefaults.TrailingIcon(expanded = expanded)
        }

        AnimatedVisibility(
            visible = expanded,
            enter = expandVertically() + fadeIn(),
            exit = shrinkVertically() + fadeOut()
        ) {
            Column(
                modifier = Modifier.padding(
                    start = 16.dp,
                    end = 16.dp,
                    bottom = 8.dp
                )
            ) {
                items.forEach { item ->
                    Row(
                        Modifier
                            .fillMaxWidth()
                            .clickable(enabled = enabled) {
                                onValueChange(item)
                                if (autoCollapse) {
                                    expanded = false
                                }
                            }
                            .padding(vertical = 8.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        RadioButton(
                            selected = (item == selectedItem),
                            onClick = null,
                            enabled = enabled
                        )
                        Spacer(Modifier.width(16.dp))
                        Column {
                            Text(
                                text = getItemText(item),
                                style = MaterialTheme.typography.bodyLarge
                            )
                            getItemSummary?.let {
                                Spacer(Modifier.height(2.dp))
                                ProvideTextStyle(
                                    value = MaterialTheme.typography.bodySmall.copy(
                                        color = MaterialTheme.colorScheme.onSurfaceVariant
                                    )
                                ) { it(item) }
                            }
                        }
                    }
                }
            }
        }
    }
}

// ==================== 滑块类布局项 ====================

/**
 * 滑块布局项组件（无卡片包装）
 *
 * @param value 当前值
 * @param onValueChange 值改变时的回调
 * @param modifier 应用于组件的修饰符
 * @param title 标题文本
 * @param summary 摘要文本（可选）
 * @param valueRange 值范围
 * @param enabled 是否启用
 * @param displayValue 显示值（可能与实际值不同）
 * @param isGlowEffectEnabled 是否启用发光效果
 */
@OptIn(ExperimentalMaterial3Api::class, ExperimentalComposeUiApi::class)
@Composable
fun SliderLayoutItem(
    value: Float,
    onValueChange: (Float) -> Unit,
    modifier: Modifier = Modifier,
    title: String,
    summary: String? = null,
    valueRange: ClosedFloatingPointRange<Float> = 0f..1f,
    enabled: Boolean = true,
    displayValue: Float = value,
    isGlowEffectEnabled: Boolean = true
) {
    Column(
        modifier = modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp, vertical = 12.dp)
    ) {
        TitleAndSummary(title = title, summary = summary)
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(top = 8.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            val interactionSource = remember { MutableInteractionSource() }
            val isDragged by interactionSource.collectIsDraggedAsState()
            val scale by animateFloatAsState(
                targetValue = if (isDragged) 1.2f else 1.0f,
                label = "thumb-scale"
            )

            Slider(
                value = value,
                onValueChange = onValueChange,
                modifier = Modifier.weight(1f),
                valueRange = valueRange,
                enabled = enabled,
                interactionSource = interactionSource,
                thumb = {
                    SliderDefaults.Thumb(
                        interactionSource = interactionSource,
                        modifier = Modifier
                            .scale(scale)
                            .glow(
                                color = MaterialTheme.colorScheme.primary,
                                enabled = isGlowEffectEnabled && enabled,
                                cornerRadius = 20.dp,
                                blurRadius = 12.dp
                            ),
                        thumbSize = DpSize(20.dp, 20.dp)
                    )
                }
            )

            Spacer(Modifier.width(16.dp))

            var isEditing by remember { mutableStateOf(false) }
            var textFieldValue by remember(displayValue) {
                mutableStateOf(String.format("%.1f", displayValue))
            }
            val focusRequester = remember { FocusRequester() }
            val keyboardController = LocalSoftwareKeyboardController.current

            Box(
                modifier = Modifier.widthIn(min = 40.dp),
                contentAlignment = Alignment.CenterEnd
            ) {
                if (isEditing) {
                    BasicTextField(
                        value = textFieldValue,
                        onValueChange = {
                            if (it.count { c -> c == '.' } <= 1) {
                                textFieldValue = it
                                    .filter { c -> c.isDigit() || c == '.' }
                                    .take(4)
                            }
                        },
                        textStyle = MaterialTheme.typography.bodyLarge.copy(
                            color = MaterialTheme.colorScheme.onSurface,
                            textAlign = TextAlign.End
                        ),
                        modifier = Modifier
                            .focusRequester(focusRequester)
                            .width(50.dp),
                        keyboardOptions = KeyboardOptions(
                            keyboardType = KeyboardType.Number,
                            imeAction = ImeAction.Done
                        ),
                        keyboardActions = KeyboardActions(
                            onDone = {
                                val newValue = textFieldValue.toFloatOrNull()
                                if (newValue != null) {
                                    onValueChange(newValue.coerceIn(valueRange))
                                }
                                isEditing = false
                                keyboardController?.hide()
                            }
                        ),
                        singleLine = true
                    )

                    LaunchedEffect(Unit) {
                        focusRequester.requestFocus()
                    }
                } else {
                    Text(
                        text = String.format("%.1fx", displayValue),
                        style = MaterialTheme.typography.bodyLarge,
                        modifier = Modifier.clickable(enabled = enabled) { isEditing = true }
                    )
                }
            }
        }
    }
}

// ==================== 输入类布局项 ====================

/**
 * 文本输入布局项组件（无卡片包装）
 *
 * @param value 输入框的值
 * @param onValueChange 值改变时的回调
 * @param modifier 应用于组件的修饰符
 * @param title 标题文本
 * @param summary 摘要文本（可选）
 * @param placeholder 输入框占位符
 * @param enabled 是否启用
 */
@Composable
fun TextInputLayoutItem(
    value: String,
    onValueChange: (String) -> Unit,
    modifier: Modifier = Modifier,
    title: String,
    summary: String? = null,
    placeholder: String? = null,
    enabled: Boolean = true
) {
    Column(
        modifier = modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp, vertical = 12.dp)
    ) {
        TitleAndSummary(title = title, summary = summary)
        Spacer(modifier = Modifier.height(8.dp))
        ShardInputField(
            value = value,
            onValueChange = onValueChange,
            modifier = Modifier.fillMaxWidth(),
            placeholder = placeholder,
            enabled = enabled,
            singleLine = true,
            keyboardOptions = KeyboardOptions(imeAction = ImeAction.Done),
        )
    }
}

// ==================== 按钮类布局项 ====================

/**
 * 带按钮的布局项组件（无卡片包装）
 *
 * @param onClick 按钮点击时的回调
 * @param modifier 应用于组件的修饰符
 * @param title 标题文本
 * @param summary 摘要文本（可选）
 * @param buttonText 按钮显示的文本
 * @param enabled 是否启用
 */
@Composable
fun ButtonLayoutItem(
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    title: String,
    summary: String? = null,
    buttonText: String = "操作",
    enabled: Boolean = true
) {
    Row(
        modifier = modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp, vertical = 12.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.SpaceBetween
    ) {
        TitleAndSummary(
            modifier = Modifier.weight(1f),
            title = title,
            summary = summary
        )
        Spacer(modifier = Modifier.width(8.dp))
        androidx.compose.material3.Button(
            onClick = onClick,
            enabled = enabled,
            colors = androidx.compose.material3.ButtonDefaults.buttonColors(
                containerColor = MaterialTheme.colorScheme.primary,
                contentColor = MaterialTheme.colorScheme.onPrimary
            )
        ) {
            Text(buttonText)
        }
    }
}

// ==================== 纯文本信息项 ====================

/**
 * 信息展示布局项（无卡片包装）
 *
 * @param title 标题文本
 * @param value 值文本
 * @param modifier 应用于组件的修饰符
 * @param summary 摘要文本（可选）
 */
@Composable
fun InfoLayoutItem(
    title: String,
    value: String,
    modifier: Modifier = Modifier,
    summary: String? = null
) {
    Row(
        modifier = modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp, vertical = 12.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.SpaceBetween
    ) {
        TitleAndSummary(
            modifier = Modifier.weight(1f),
            title = title,
            summary = summary
        )
        Text(
            text = value,
            style = MaterialTheme.typography.bodyLarge,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )
    }
}

/**
 * 可点击信息布局项（无卡片包装）
 *
 * @param title 标题文本
 * @param value 值文本
 * @param onClick 点击回调
 * @param modifier 应用于组件的修饰符
 * @param summary 摘要文本（可选）
 * @param enabled 是否启用
 */
@Composable
fun ClickableInfoLayoutItem(
    title: String,
    value: String,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    summary: String? = null,
    enabled: Boolean = true
) {
    Row(
        modifier = modifier
            .fillMaxWidth()
            .clickable(enabled = enabled, onClick = onClick)
            .padding(horizontal = 16.dp, vertical = 12.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.SpaceBetween
    ) {
        TitleAndSummary(
            modifier = Modifier.weight(1f),
            title = title,
            summary = summary
        )
        Text(
            text = value,
            style = MaterialTheme.typography.bodyLarge,
            color = if (enabled) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onSurfaceVariant
        )
    }
}
