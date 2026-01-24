package com.lanrhyme.shardlauncher.ui.notification

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.CubicBezierEasing
import androidx.compose.animation.core.FastOutLinearInEasing
import androidx.compose.animation.core.LinearEasing
import androidx.compose.animation.core.LinearOutSlowInEasing
import androidx.compose.animation.core.tween
import androidx.compose.animation.expandVertically
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.shrinkVertically
import androidx.compose.animation.slideInHorizontally
import androidx.compose.animation.slideInVertically
import androidx.compose.animation.slideOutHorizontally
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Close
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.SwipeToDismissBox
import androidx.compose.material3.SwipeToDismissBoxValue
import androidx.compose.material3.Text
import androidx.compose.material3.rememberSwipeToDismissBoxState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.key
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.lanrhyme.shardlauncher.common.SidebarPosition
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch

@Composable
fun NotificationPanel(
    isVisible: Boolean,
    sidebarPosition: SidebarPosition
) {
    // 自定义缓动曲线，效果更显著
    val enterEasing = remember { CubicBezierEasing(0.05f, 0.7f, 0.1f, 1.0f) } // 极快启动，缓慢结束
    val exitEasing = remember { CubicBezierEasing(0.3f, 0.0f, 0.8f, 0.15f) } // 缓慢启动，极快结束

    val panelAlignment = if (sidebarPosition == SidebarPosition.Left) Alignment.CenterEnd else Alignment.CenterStart
    val enterAnimation = if (sidebarPosition == SidebarPosition.Left) slideInHorizontally(initialOffsetX = { it }) else slideInHorizontally(initialOffsetX = { -it })
    val exitAnimation = if (sidebarPosition == SidebarPosition.Left) slideOutHorizontally(targetOffsetX = { it }) else slideOutHorizontally(targetOffsetX = { -it })

    val allNotifications by NotificationManager.notifications.collectAsState()
    var dialogNotification by remember { mutableStateOf<Notification?>(null) }
    val persistentNotifications = remember(allNotifications) {
        allNotifications.filter { it.type != NotificationType.Temporary }
    }

    if (dialogNotification != null) {
        NotificationDialog(notification = dialogNotification!!, onDismiss = { dialogNotification = null })
    }

    val visibleItems = remember { mutableStateListOf<String>() }

    LaunchedEffect(persistentNotifications, isVisible) {
        if (isVisible) {
            val coroutineScope = this
            visibleItems.clear()
            persistentNotifications.forEachIndexed { index, item ->
                coroutineScope.launch {
                    delay(index * 50L + 100L) // 错峰延迟
                    visibleItems.add(item.id)
                }
            }
        } else {
            visibleItems.clear()
        }
    }

    AnimatedVisibility(
        visible = isVisible,
        enter = enterAnimation,
        exit = exitAnimation
    ) {
        Box(modifier = Modifier.fillMaxSize(), contentAlignment = panelAlignment) {
            val shape = if (sidebarPosition == SidebarPosition.Left) {
                RoundedCornerShape(topStart = 22.dp, bottomStart = 22.dp)
            } else {
                RoundedCornerShape(topEnd = 22.dp, bottomEnd = 22.dp)
            }

            Surface(
                modifier = Modifier
                    .fillMaxHeight()
                    .fillMaxWidth(0.4f),
                shape = shape,
                color = MaterialTheme.colorScheme.surface.copy(alpha = 0.95f),
                tonalElevation = 12.dp
            ) {
                Box(modifier = Modifier.fillMaxSize()) {
                    if (persistentNotifications.isEmpty()) {
                        Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                            Text("没有通知")
                        }
                    } else {
                        LazyColumn(
                            modifier = Modifier.fillMaxSize(),
                            contentPadding = PaddingValues(top = 48.dp, start = 16.dp, end = 16.dp, bottom = 16.dp),
                            verticalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            items(persistentNotifications, key = { it.id }) { notification ->
                                AnimatedVisibility(
                                    visible = notification.id in visibleItems,
                                    enter = fadeIn(animationSpec = tween(durationMillis = 800, easing = enterEasing)) + slideInVertically(animationSpec = tween(durationMillis = 800, easing = enterEasing), initialOffsetY = { it / 2 }),
                                    exit = fadeOut(animationSpec = tween(durationMillis = 800, easing = exitEasing))
                                ) {
                                    val dismissState = rememberSwipeToDismissBoxState(
                                        confirmValueChange = {
                                            if (it != SwipeToDismissBoxValue.Settled) {
                                                NotificationManager.dismiss(notification.id)
                                                true
                                            } else false
                                        }
                                    )
                                    SwipeToDismissBox(
                                        state = dismissState,
                                        backgroundContent = {},
                                        content = {
                                            NotificationItem(
                                                notification = notification.copy(onClick = {
                                                    dialogNotification = notification
                                                }),
                                                modifier = Modifier.fillMaxWidth()
                                            )
                                        }
                                    )
                                }
                            }
                        }
                        // 全部清除按钮
                        IconButton(
                            onClick = { NotificationManager.clearAll() },
                            modifier = Modifier
                                .align(Alignment.TopEnd)
                                .padding(8.dp)
                        ) {
                            Icon(Icons.Default.Close, contentDescription = "全部清除")
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun NotificationPopupHost() {
    val allNotifications by NotificationManager.notifications.collectAsState()
    val seenPopupIds by NotificationManager.seenPopupIds.collectAsState()
    var dialogNotification by remember { mutableStateOf<Notification?>(null) }

    val notificationsToShowAsPopup = allNotifications.filter { it.id !in seenPopupIds }

    if (dialogNotification != null) {
        NotificationDialog(notification = dialogNotification!!, onDismiss = { dialogNotification = null })
    }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .padding(top = 16.dp, end = 16.dp),
        contentAlignment = Alignment.TopEnd
    ) {
        Column(horizontalAlignment = Alignment.End, verticalArrangement = Arrangement.spacedBy(8.dp)) {
            notificationsToShowAsPopup.forEachIndexed { index, notification ->
                key(notification.id) {
                    PopupNotificationItem(
                        notification = notification,
                        isTop = index == 0,
                        onDismiss = { id, type, remove ->
                            NotificationManager.addSeenPopupId(id)
                            if (remove) {
                                NotificationManager.dismiss(id)
                            }
                        },
                        onClick = {
                            dialogNotification = notification
                        }
                    )
                }
            }
        }
    }
}

@Composable
private fun PopupNotificationItem(
    notification: Notification,
    isTop: Boolean,
    onDismiss: (String, NotificationType, Boolean) -> Unit,
    onClick: () -> Unit
) {
    var visible by remember { mutableStateOf(false) }
    val durationAnim = remember { Animatable(1f) }

    // 逻辑：默认自动消失时间为5秒。
    // 进度通知如果 keepOnScreen = true (或默认) 则不会自动消失。
    val isSticky = notification.keepOnScreen ?: (notification.type == NotificationType.Progress)
    val shouldAutoDismiss = !isSticky

    LaunchedEffect(notification.id) {
        visible = true
        if (shouldAutoDismiss) {
            val totalDuration = 5000L
            val animDuration = 2100L
            // 引入 300ms 缓冲时间，提前开始退出动画，防止因系统延迟导致视觉上超过 5s
            val bufferTime = 300L 
            val staticDuration = totalDuration - animDuration - bufferTime // 2600ms

            // 独立启动进度条动画，使其跨越完整的5秒时长 (直到通知彻底消失)
            val progressJob = launch {
                durationAnim.animateTo(0f, animationSpec = tween(totalDuration.toInt(), easing = LinearEasing))
            }

            // 等待静止阶段结束。
            // 注意：visible=true 触发进入动画 (2.1s)。
            // 我们希望通知在 T = 2.9s 时开始退出。
            // 由于 `delay` 是顺序执行的，我们只需等待静止时间。
            delay(staticDuration)
            
            visible = false // 开始退出动画 (2.1s)
            
            delay(animDuration) // 等待退出动画完成
            
            // 自动消失时，如果是临时通知则移除，否则只隐藏弹窗（保留在列表中）
            val remove = notification.type == NotificationType.Temporary
            onDismiss(notification.id, notification.type, remove)
            progressJob.cancel()
        }
    }
    
    // 粘性进度通知在进度完成时自动消失
    LaunchedEffect(notification.progress) {
        if (notification.type == NotificationType.Progress && isSticky && notification.progress == 1.0f) {
            delay(1000)
            visible = false
            delay(2100)
            // 进度完成后自动消失，保留在列表中（除非是临时通知）
            val remove = notification.type == NotificationType.Temporary
            onDismiss(notification.id, notification.type, remove)
        }
    }

    val dismissState = rememberSwipeToDismissBoxState(
        confirmValueChange = {
            if (it != SwipeToDismissBoxValue.Settled) {
                // 手动滑动删除时，如果是临时通知则移除，否则只隐藏弹窗（保留在列表中）
                val remove = notification.type == NotificationType.Temporary
                onDismiss(notification.id, notification.type, remove)
                true
            } else false
        }
    )
    
    // 自定义缓动曲线
    val enterEasing = remember { CubicBezierEasing(0.05f, 0.7f, 0.1f, 1.0f) }
    val exitEasing = remember { CubicBezierEasing(0.3f, 0.0f, 0.8f, 0.15f) }

    AnimatedVisibility(
        visible = visible,
        enter = slideInHorizontally(animationSpec = tween(2100, easing = enterEasing)) { it } + fadeIn(animationSpec = tween(2100, easing = enterEasing)),
        exit = slideOutHorizontally(animationSpec = tween(2100, easing = exitEasing)) { it } + fadeOut(animationSpec = tween(2100, easing = exitEasing)) + if (isTop) shrinkVertically(animationSpec = tween(2100, easing = exitEasing)) else androidx.compose.animation.ExitTransition.None
    ) {
        SwipeToDismissBox(
            state = dismissState,
            backgroundContent = {},
            content = {
                NotificationItem(
                    notification = notification.copy(onClick = notification.onClick ?: onClick),
                    durationProgress = if (shouldAutoDismiss) durationAnim.value else null,
                    modifier = Modifier.width(350.dp)
                )
            }
        )
    }
}
