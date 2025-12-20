package com.lanrhyme.shardlauncher.ui.components

import android.os.Build
import androidx.compose.animation.*
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.interaction.collectIsDraggedAsState
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.ExperimentalComposeUiApi
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
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
import dev.chrisbanes.haze.HazeState
import dev.chrisbanes.haze.hazeChild

@Composable
fun SwitchLayout(
        checked: Boolean,
        onCheckedChange: () -> Unit,
        modifier: Modifier = Modifier,
        title: String,
        summary: String? = null,
        enabled: Boolean = true,
        shape: Shape = RoundedCornerShape(16.dp),
        isCardBlurEnabled: Boolean,
        cardAlpha: Float,
        hazeState: HazeState
) {
    val interactionSource = remember { MutableInteractionSource() }
    val cardModifier =
            if (isCardBlurEnabled && Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
                modifier.fillMaxWidth()
                        .alpha(if (enabled) 1f else 0.5f)
                        .hazeChild(state = hazeState, shape = shape)
            } else {
                modifier.fillMaxWidth().alpha(if (enabled) 1f else 0.5f)
            }
    Card(
            modifier =
                    cardModifier.clickable(
                            interactionSource = interactionSource,
                            indication = null,
                            enabled = enabled,
                            onClick = onCheckedChange
                    ),
            shape = shape,
            colors =
                    CardDefaults.cardColors(
                            containerColor =
                                    MaterialTheme.colorScheme.surface.copy(alpha = cardAlpha)
                    ),
    ) {
        Row(
                modifier = Modifier.padding(horizontal = 16.dp, vertical = 12.dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
        ) {
            TitleAndSummary(modifier = Modifier.weight(1f), title = title, summary = summary)
            Spacer(modifier = Modifier.width(8.dp))
            Switch(
                    checked = checked,
                    onCheckedChange = { onCheckedChange() },
                    enabled = enabled,
                    colors =
                            SwitchDefaults.colors(
                                    checkedThumbColor = MaterialTheme.colorScheme.secondary,
                                    checkedTrackColor =
                                            MaterialTheme.colorScheme.secondaryContainer,
                                    checkedIconColor = MaterialTheme.colorScheme.onSecondary,
                                    uncheckedThumbColor = MaterialTheme.colorScheme.outline,
                                    uncheckedTrackColor = MaterialTheme.colorScheme.surfaceVariant,
                                    uncheckedBorderColor = MaterialTheme.colorScheme.outline
                            )
            )
        }
    }
}

@Composable
fun IconSwitchLayout(
        checked: Boolean,
        onCheckedChange: () -> Unit,
        onIconClick: () -> Unit,
        modifier: Modifier = Modifier,
        icon: @Composable () -> Unit,
        title: String,
        summary: String? = null,
        enabled: Boolean = true,
        shape: Shape = RoundedCornerShape(16.dp),
        isCardBlurEnabled: Boolean,
        cardAlpha: Float,
        hazeState: HazeState
) {
    val interactionSource = remember { MutableInteractionSource() }
    val cardModifier =
            if (isCardBlurEnabled && Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
                modifier.fillMaxWidth()
                        .alpha(if (enabled) 1f else 0.5f)
                        .hazeChild(state = hazeState, shape = shape)
            } else {
                modifier.fillMaxWidth().alpha(if (enabled) 1f else 0.5f)
            }
    Card(
            modifier =
                    cardModifier.clickable(
                            interactionSource = interactionSource,
                            indication = null,
                            enabled = enabled,
                            onClick = onCheckedChange
                    ),
            shape = shape,
            colors =
                    CardDefaults.cardColors(
                            containerColor =
                                    MaterialTheme.colorScheme.surface.copy(alpha = cardAlpha)
                    ),
    ) {
        Row(
                modifier = Modifier.padding(horizontal = 16.dp, vertical = 12.dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
        ) {
            TitleAndSummary(modifier = Modifier.weight(1f), title = title, summary = summary)
            Spacer(modifier = Modifier.width(8.dp))
            IconButton(onClick = onIconClick, enabled = enabled) { icon() }
            Switch(
                    checked = checked,
                    onCheckedChange = { onCheckedChange() },
                    enabled = enabled,
                    colors =
                            SwitchDefaults.colors(
                                    checkedThumbColor = MaterialTheme.colorScheme.secondary,
                                    checkedTrackColor =
                                            MaterialTheme.colorScheme.secondaryContainer,
                                    checkedIconColor = MaterialTheme.colorScheme.onSecondary,
                                    uncheckedThumbColor = MaterialTheme.colorScheme.outline,
                                    uncheckedTrackColor = MaterialTheme.colorScheme.surfaceVariant,
                                    uncheckedBorderColor = MaterialTheme.colorScheme.outline
                            )
            )
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun <E> SimpleListLayout(
        modifier: Modifier = Modifier,
        items: List<E>,
        selectedItem: E,
        title: String,
        getItemText: @Composable (E) -> String,
        onValueChange: (E) -> Unit,
        summary: String? = null,
        getItemSummary: (@Composable (E) -> Unit)? = null,
        enabled: Boolean = true,
        autoCollapse: Boolean = true,
        shape: Shape = RoundedCornerShape(16.dp),
        isCardBlurEnabled: Boolean,
        cardAlpha: Float,
        hazeState: HazeState
) {
    var expanded by remember { mutableStateOf(false) }
    val cardModifier =
            if (isCardBlurEnabled && Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
                modifier.fillMaxWidth()
                        .alpha(if (enabled) 1f else 0.5f)
                        .hazeChild(state = hazeState, shape = shape)
            } else {
                modifier.fillMaxWidth().alpha(if (enabled) 1f else 0.5f)
            }

    Card(
            modifier = cardModifier,
            shape = shape,
            colors =
                    CardDefaults.cardColors(
                            containerColor =
                                    MaterialTheme.colorScheme.surface.copy(alpha = cardAlpha)
                    ),
    ) {
        Column {
            Row(
                    modifier =
                            Modifier.clickable(
                                            enabled = enabled,
                                            onClick = { expanded = !expanded }
                                    )
                                    .padding(horizontal = 16.dp, vertical = 12.dp),
                    verticalAlignment = Alignment.CenterVertically
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
                Column(modifier = Modifier.padding(start = 16.dp, end = 16.dp, bottom = 8.dp)) {
                    items.forEach { item ->
                        Row(
                                Modifier.fillMaxWidth()
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
                                            value =
                                                    MaterialTheme.typography.bodySmall.copy(
                                                            color =
                                                                    MaterialTheme.colorScheme
                                                                            .onSurfaceVariant
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
}

@OptIn(ExperimentalMaterial3Api::class, ExperimentalComposeUiApi::class)
@Composable
fun SliderLayout(
        value: Float,
        onValueChange: (Float) -> Unit,
        modifier: Modifier = Modifier,
        title: String,
        summary: String? = null,
        valueRange: ClosedFloatingPointRange<Float> = 0f..1f,
        steps: Int = 0,
        enabled: Boolean = true,
        shape: Shape = RoundedCornerShape(16.dp),
        displayValue: Float = value,
        isGlowEffectEnabled: Boolean,
        isCardBlurEnabled: Boolean,
        cardAlpha: Float,
        hazeState: HazeState
) {
    val cardModifier =
            if (isCardBlurEnabled && Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
                modifier.fillMaxWidth()
                        .alpha(if (enabled) 1f else 0.5f)
                        .hazeChild(state = hazeState, shape = shape)
            } else {
                modifier.fillMaxWidth().alpha(if (enabled) 1f else 0.5f)
            }
    Card(
            modifier = cardModifier,
            shape = shape,
            colors =
                    CardDefaults.cardColors(
                            containerColor =
                                    MaterialTheme.colorScheme.surface.copy(alpha = cardAlpha)
                    ),
    ) {
        Column(modifier = Modifier.padding(horizontal = 16.dp, vertical = 12.dp)) {
            TitleAndSummary(title = title, summary = summary)
            Row(
                    modifier = Modifier.fillMaxWidth().padding(top = 8.dp),
                    verticalAlignment = Alignment.CenterVertically
            ) {
                val interactionSource = remember { MutableInteractionSource() }
                val isDragged by interactionSource.collectIsDraggedAsState()
                val scale by
                        animateFloatAsState(
                                targetValue = if (isDragged) 1.2f else 1.0f,
                                label = "thumb-scale"
                        )

                Slider(
                        value = value,
                        onValueChange = onValueChange,
                        modifier = Modifier.weight(1f),
                        valueRange = valueRange,
                        steps = 0,
                        enabled = enabled,
                        interactionSource = interactionSource,
                        thumb = {
                            SliderDefaults.Thumb(
                                    interactionSource = interactionSource,
                                    modifier =
                                            Modifier.scale(scale)
                                                    .glow(
                                                            color =
                                                                    MaterialTheme.colorScheme
                                                                            .primary,
                                                            enabled =
                                                                    isGlowEffectEnabled && enabled,
                                                            cornerRadius = 20.dp,
                                                            blurRadius = 12.dp
                                                    ),
                                    thumbSize = DpSize(20.dp, 20.dp)
                            )
                        }
                )

                Spacer(Modifier.width(16.dp))

                var isEditing by remember { mutableStateOf(false) }
                var textFieldValue by
                        remember(displayValue) {
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
                                        textFieldValue =
                                                it.filter { c -> c.isDigit() || c == '.' }.take(4)
                                    }
                                },
                                textStyle =
                                        MaterialTheme.typography.bodyLarge.copy(
                                                color = MaterialTheme.colorScheme.onSurface,
                                                textAlign = TextAlign.End
                                        ),
                                modifier = Modifier.focusRequester(focusRequester).width(50.dp),
                                keyboardOptions =
                                        KeyboardOptions(
                                                keyboardType = KeyboardType.Number,
                                                imeAction = ImeAction.Done
                                        ),
                                keyboardActions =
                                        KeyboardActions(
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

                        LaunchedEffect(Unit) { focusRequester.requestFocus() }
                    } else {
                        Text(
                                text = String.format("%.1fx", displayValue),
                                style = MaterialTheme.typography.bodyLarge,
                                modifier =
                                        Modifier.clickable(enabled = enabled) { isEditing = true }
                        )
                    }
                }
            }
        }
    }
}
