package com.lanrhyme.shardlauncher.ui.components.basic

import android.os.Build
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
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
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.MaterialTheme
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
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.graphics.toArgb
import androidx.compose.ui.platform.LocalView
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import androidx.compose.ui.window.DialogWindowProvider
import androidx.compose.ui.window.Popup
import androidx.compose.ui.window.PopupProperties
import androidx.core.view.WindowCompat
import androidx.compose.foundation.layout.width
import androidx.compose.animation.scaleIn
import androidx.compose.animation.scaleOut
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.core.view.WindowInsetsCompat
import androidx.core.view.WindowInsetsControllerCompat
import com.lanrhyme.shardlauncher.ui.components.layout.LocalCardLayoutConfig
import dev.chrisbanes.haze.hazeEffect
import kotlinx.coroutines.delay

/**
 * 对话框尺寸枚举
 */
enum class DialogSize {
    SMALL,      // 小型对话框 (280dp x 200dp)
    MEDIUM,     // 中型对话框 (350dp x 310dp) - 默认
    LARGE,      // 大型对话框 (560dp x 420dp)
    FULL        // 全屏对话框
}

/**
 * 统一的对话框组件 - ShardDialog
 *
 * 这是项目中所有对话框的基础组件，支持：
 * - 多种预设尺寸
 * - 毛玻璃模糊效果（Android 12+）
 * - 进入/退出动画
 * - 沉浸式全屏显示
 *
 * @param visible 控制对话框的可见性
 * @param onDismissRequest 当用户请求关闭对话框时调用的回调
 * @param modifier 应用于对话框内容表面的修饰符
 * @param size 对话框尺寸，参考 [DialogSize]
 * @param customWidth 自定义宽度（覆盖尺寸预设）
 * @param customHeight 自定义高度（覆盖尺寸预设）
 * @param dismissOnBackPress 是否允许返回键关闭
 * @param dismissOnClickOutside 是否允许点击外部关闭
 * @param content 对话框内部显示的内容
 */
@Composable
fun ShardDialog(
    visible: Boolean,
    onDismissRequest: () -> Unit,
    modifier: Modifier = Modifier,
    size: DialogSize = DialogSize.MEDIUM,
    customWidth: Dp? = null,
    customHeight: Dp? = null,
    dismissOnBackPress: Boolean = true,
    dismissOnClickOutside: Boolean = true,
    content: @Composable () -> Unit
) {
    val (isCardBlurEnabled, _, hazeState) = LocalCardLayoutConfig.current
    val tween = tween<Float>(durationMillis = 300)

    // 尺寸配置
    val (width, height) = when (size) {
        DialogSize.SMALL -> 280.dp to 200.dp
        DialogSize.MEDIUM -> 350.dp to 310.dp
        DialogSize.LARGE -> 560.dp to 420.dp
        DialogSize.FULL -> 600.dp to 600.dp
    }
    val finalWidth = customWidth ?: width
    val finalHeight = customHeight ?: height

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
            properties = DialogProperties(
                usePlatformDefaultWidth = false,
                decorFitsSystemWindows = false,
                dismissOnBackPress = dismissOnBackPress,
                dismissOnClickOutside = dismissOnClickOutside
            )
        ) {
            // 设置沉浸式窗口
            ShardDialogWindowSetup()

            AnimatedVisibility(
                visible = visible,
                enter = fadeIn(tween),
                exit = fadeOut(tween)
            ) {
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .clickable(
                            indication = null,
                            interactionSource = remember { MutableInteractionSource() },
                            onClick = { if (dismissOnClickOutside) onDismissRequest() }
                        ),
                    contentAlignment = Alignment.Center
                ) {
                    // 背景遮罩
                    val bgAlpha by animateFloatAsState(
                        targetValue = if (visible) 0.4f else 0f,
                        animationSpec = tween
                    )
                    Box(
                        modifier = Modifier
                            .fillMaxSize()
                            .graphicsLayer { alpha = bgAlpha }
                            .background(Color.Black)
                    )

                    // 对话框内容
                    val progress by animateFloatAsState(
                        targetValue = if (visible) 1f else 0f,
                        animationSpec = tween
                    )
                    val dialogShape = RoundedCornerShape(16.dp)

                    Surface(
                        modifier = modifier
                            .graphicsLayer {
                                alpha = progress
                                translationY = (1f - progress) * 80.dp.toPx()
                            }
                            .widthIn(min = 280.dp, max = finalWidth)
                            .heightIn(min = 200.dp, max = finalHeight)
                            .then(
                                if (isCardBlurEnabled && Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
                                    Modifier.clip(dialogShape).hazeEffect(state = hazeState)
                                } else Modifier
                            )
                            .clickable(
                                indication = null,
                                interactionSource = remember { MutableInteractionSource() },
                                onClick = {}
                            ),
                        shape = dialogShape,
                        color = if (isCardBlurEnabled) {
                            MaterialTheme.colorScheme.surface.copy(alpha = 0.9f)
                        } else {
                            MaterialTheme.colorScheme.surface
                        }
                    ) {
                        Box(contentAlignment = Alignment.Center) {
                            content()
                        }
                    }
                }
            }
        }
    }
}

/**
 * 警告/确认对话框组件
 *
 * 基于 ShardDialog 实现的便捷对话框，包含标题、正文和操作按钮
 *
 * @param visible 是否显示
 * @param title 对话框标题
 * @param text 对话框正文内容（字符串）
 * @param onDismiss 关闭对话框时的回调
 * @param onConfirm 点击确认按钮时的回调，为 null 时不显示确认按钮
 * @param onCancel 点击取消按钮时的回调，为 null 时复用 onDismiss
 * @param confirmText 确认按钮文本，默认为 "确定"
 * @param cancelText 取消按钮文本，默认为 "取消"
 * @param size 对话框尺寸
 */
@Composable
fun ShardAlertDialog(
    visible: Boolean,
    title: String,
    text: String,
    onDismiss: () -> Unit,
    onConfirm: (() -> Unit)? = null,
    onCancel: (() -> Unit)? = null,
    confirmText: String = "确定",
    cancelText: String = "取消",
    size: DialogSize = DialogSize.SMALL
) {
    ShardDialog(
        visible = visible,
        onDismissRequest = onDismiss,
        size = size
    ) {
        Column(
            modifier = Modifier.padding(24.dp)
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
                    Text(cancelText)
                }
                if (onConfirm != null) {
                    Spacer(modifier = Modifier.width(8.dp))
                    TextButton(onClick = onConfirm) {
                        Text(confirmText)
                    }
                }
            }
        }
    }
}

/**
 * 警告/确认对话框组件（支持自定义内容）
 *
 * @param visible 是否显示
 * @param title 对话框标题
 * @param onDismiss 关闭对话框时的回调
 * @param onConfirm 点击确认按钮时的回调，为 null 时不显示确认按钮
 * @param onCancel 点击取消按钮时的回调，为 null 时复用 onDismiss
 * @param confirmText 确认按钮文本，默认为 "确定"
 * @param cancelText 取消按钮文本，默认为 "取消"
 * @param size 对话框尺寸
 * @param content 自定义内容区域
 */
@Composable
fun ShardAlertDialog(
    visible: Boolean,
    title: String,
    onDismiss: () -> Unit,
    onConfirm: (() -> Unit)? = null,
    onCancel: (() -> Unit)? = null,
    confirmText: String = "确定",
    cancelText: String = "取消",
    size: DialogSize = DialogSize.SMALL,
    content: @Composable ColumnScope.() -> Unit
) {
    ShardDialog(
        visible = visible,
        onDismissRequest = onDismiss,
        size = size
    ) {
        Column(
            modifier = Modifier.padding(24.dp)
        ) {
            Text(
                text = title,
                style = MaterialTheme.typography.headlineSmall,
                color = MaterialTheme.colorScheme.onSurface
            )
            Spacer(modifier = Modifier.height(16.dp))
            content()
            Spacer(modifier = Modifier.height(24.dp))
            Row(
                modifier = Modifier.align(Alignment.End),
                horizontalArrangement = Arrangement.End
            ) {
                TextButton(onClick = onCancel ?: onDismiss) {
                    Text(cancelText)
                }
                if (onConfirm != null) {
                    Spacer(modifier = Modifier.width(8.dp))
                    TextButton(onClick = onConfirm) {
                        Text(confirmText)
                    }
                }
            }
        }
    }
}

/**
 * 弹出式容器组件
 *
 * 用于显示简单的弹出内容，比 Dialog 更轻量
 *
 * @param visible 控制弹出容器的可见性
 * @param onDismissRequest 当用户请求关闭时调用的回调
 * @param modifier 应用于弹出内容的修饰符
 * @param width 弹出容器宽度
 * @param height 弹出容器高度
 * @param alignment 弹出容器在屏幕上的对齐方式
 * @param content 弹出容器内部显示的内容
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
    val (isCardBlurEnabled, _, hazeState) = LocalCardLayoutConfig.current
    var showPopup by remember { mutableStateOf(false) }

    LaunchedEffect(visible) {
        if (visible) {
            showPopup = true
        } else {
            delay(200)
            showPopup = false
        }
    }

    if (showPopup) {
        Popup(
            onDismissRequest = onDismissRequest,
            properties = PopupProperties(focusable = true),
            alignment = alignment
        ) {
            AnimatedVisibility(
                visible = visible,
                enter = fadeIn(tween(200)) + androidx.compose.animation.scaleIn(tween(200), 0.9f),
                exit = fadeOut(tween(200)) + androidx.compose.animation.scaleOut(tween(200), 0.9f)
            ) {
                val popupShape = RoundedCornerShape(16.dp)
                Surface(
                    modifier = modifier
                        .width(width)
                        .height(height)
                        .then(
                            if (isCardBlurEnabled && Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
                                Modifier.clip(popupShape).hazeEffect(state = hazeState)
                            } else Modifier
                        ),
                    shape = popupShape,
                    color = if (isCardBlurEnabled) {
                        MaterialTheme.colorScheme.surface.copy(alpha = 0.8f)
                    } else {
                        MaterialTheme.colorScheme.surface
                    }
                ) {
                    content()
                }
            }
        }
    }
}

/**
 * 编辑对话框组件
 *
 * 基于 ShardDialog 实现，提供标题与输入框，适用于简单的文本编辑场景
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
    ShardDialog(
        visible = true,
        onDismissRequest = onDismissRequest,
        size = DialogSize.SMALL
    ) {
        Column(
            modifier = Modifier.padding(24.dp)
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
            supportingText?.let {
                Spacer(modifier = Modifier.height(4.dp))
                it()
            }
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

/**
 * 任务执行对话框组件
 *
 * 执行 [task] 时显示进度指示，成功后触发 [onDismiss]，失败时触发 [onError]
 *
 * @param title 对话框标题
 * @param task 需要执行的挂起任务
 * @param onDismiss 任务完成后关闭对话框的回调
 * @param onError 任务失败时的回调
 */
@Composable
fun ShardTaskDialog(
    title: String,
    task: suspend () -> Unit,
    onDismiss: () -> Unit,
    onError: (Throwable) -> Unit
) {
    var isRunning by remember { mutableStateOf(true) }
    var errorMessage by remember { mutableStateOf<String?>(null) }

    LaunchedEffect(Unit) {
        try {
            task()
            onDismiss()
        } catch (e: Throwable) {
            isRunning = false
            errorMessage = e.message
            onError(e)
        }
    }

    val currentErrorMessage = errorMessage
    if (isRunning || currentErrorMessage != null) {
        ShardDialog(
            visible = true,
            onDismissRequest = { if (currentErrorMessage != null) onDismiss() },
            size = DialogSize.SMALL,
            dismissOnBackPress = currentErrorMessage != null,
            dismissOnClickOutside = currentErrorMessage != null
        ) {
            Column(
                modifier = Modifier.padding(24.dp)
            ) {
                Text(
                    text = title,
                    style = MaterialTheme.typography.headlineSmall,
                    color = MaterialTheme.colorScheme.onSurface
                )
                Spacer(modifier = Modifier.height(16.dp))

                if (currentErrorMessage != null) {
                    Text(
                        text = currentErrorMessage,
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.error
                    )
                    Spacer(modifier = Modifier.height(16.dp))
                    TextButton(
                        onClick = onDismiss,
                        modifier = Modifier.align(Alignment.End)
                    ) {
                        Text("关闭")
                    }
                } else {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        CircularProgressIndicator(
                            modifier = Modifier.size(24.dp),
                            strokeWidth = 2.dp
                        )
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

/**
 * 对话框窗口设置（内部使用）
 * 配置沉浸式全屏显示
 */
@Composable
private fun ShardDialogWindowSetup() {
    val view = LocalView.current
    val darkTheme = androidx.compose.foundation.isSystemInDarkTheme()
    if (!view.isInEditMode) {
        SideEffect {
            @Suppress("DEPRECATION")
            val window = (view.parent as? DialogWindowProvider)?.window
            if (window != null) {
                WindowCompat.setDecorFitsSystemWindows(window, false)
                val insetsController = WindowCompat.getInsetsController(window, view)
                insetsController.hide(WindowInsetsCompat.Type.systemBars())
                insetsController.systemBarsBehavior =
                    WindowInsetsControllerCompat.BEHAVIOR_SHOW_TRANSIENT_BARS_BY_SWIPE
                @Suppress("DEPRECATION")
                window.statusBarColor = Color.Transparent.toArgb()
                @Suppress("DEPRECATION")
                window.navigationBarColor = Color.Transparent.toArgb()
                insetsController.isAppearanceLightStatusBars = !darkTheme
            }
        }
    }
}
