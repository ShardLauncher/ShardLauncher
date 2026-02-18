package com.lanrhyme.shardlauncher.ui.developeroptions

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.navigation.NavController
import com.lanrhyme.shardlauncher.ui.components.layout.LocalCardLayoutConfig
import com.lanrhyme.shardlauncher.ui.components.basic.ButtonType
import com.lanrhyme.shardlauncher.ui.components.basic.ShardButtonWithIcon
import com.lanrhyme.shardlauncher.ui.components.layout.SliderLayoutCard
import com.lanrhyme.shardlauncher.ui.components.basic.SubPageNavigationBar
import com.lanrhyme.shardlauncher.ui.components.basic.TitleAndSummary
import com.lanrhyme.shardlauncher.ui.notification.Notification
import com.lanrhyme.shardlauncher.ui.notification.NotificationManager
import com.lanrhyme.shardlauncher.ui.notification.NotificationType

@Composable
fun DeveloperOptionsScreen(navController: NavController) {
    val cardLayoutConfig = LocalCardLayoutConfig.current
    val isCardBlurEnabled = cardLayoutConfig.isCardBlurEnabled
    val cardAlpha = cardLayoutConfig.cardAlpha
    val hazeState = cardLayoutConfig.hazeState
    LazyColumn(
            modifier = Modifier.fillMaxSize().padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        item {
            Card(modifier = Modifier.fillMaxWidth(), shape = RoundedCornerShape(22.dp)) {
                Column(modifier = Modifier.padding(18.dp)) {
                    SubPageNavigationBar(title = "开发者选项", onBack = { navController.navigateUp() })
                }
            }
        }
        item {
            Card(modifier = Modifier.fillMaxWidth(), shape = RoundedCornerShape(22.dp)) {
                Column(modifier = Modifier.padding(16.dp)) {
                    TitleAndSummary(title = "组件演示", summary = "查看所有可用组件")
                    Spacer(modifier = Modifier.height(16.dp))
                    ShardButtonWithIcon(
                            onClick = { navController.navigate("component_demo") },
                            modifier = Modifier.fillMaxWidth(),
                            text = "打开组件演示",
                            type = ButtonType.GRADIENT
                    )
                }
            }
        }
        item {
            Card(modifier = Modifier.fillMaxWidth(), shape = RoundedCornerShape(22.dp)) {
                TestNotificationSender()
            }
        }
        item {
            Card(modifier = Modifier.fillMaxWidth(), shape = RoundedCornerShape(22.dp)) {
                Column(modifier = Modifier.padding(16.dp)) {
                    TitleAndSummary(title = "实时日志", summary = "查看应用运行日志，用于调试")
                    Spacer(modifier = Modifier.height(16.dp))
                    ShardButtonWithIcon(
                            onClick = { navController.navigate("log_viewer") },
                            modifier = Modifier.fillMaxWidth(),
                            text = "打开日志查看器",
                            icon = Icons.Default.List,
                            type = ButtonType.GRADIENT
                    )
                }
            }
        }
        item {
            Card(modifier = Modifier.fillMaxWidth(), shape = RoundedCornerShape(22.dp)) {
                Column(modifier = Modifier.padding(16.dp)) {
                    TitleAndSummary(title = "模拟崩溃", summary = "测试崩溃报告功能")
                    Spacer(modifier = Modifier.height(16.dp))
                    ShardButtonWithIcon(
                            onClick = { throw RuntimeException("Test Crash") },
                            modifier = Modifier.fillMaxWidth(),
                            text = "模拟崩溃",
                            type = ButtonType.GRADIENT
                    )
                }
            }
        }
    }
}

@Composable
private fun TestNotificationSender() {
    val cardLayoutConfig = LocalCardLayoutConfig.current
    val isCardBlurEnabled = cardLayoutConfig.isCardBlurEnabled
    val cardAlpha = cardLayoutConfig.cardAlpha
    val hazeState = cardLayoutConfig.hazeState
    var progress by remember { mutableStateOf(0f) }
    var progressNotificationId by remember { mutableStateOf<String?>(null) }

    val buttons = remember {
        listOf(
                "Temporary" to
                        {
                            NotificationManager.show(
                                    Notification(
                                            title = "Temporary",
                                            message = "Disappears after 3s",
                                            type = NotificationType.Temporary
                                    )
                            )
                        },
                "Normal" to
                        {
                            NotificationManager.show(
                                    Notification(
                                            title = "Normal",
                                            message = "Stays in the list",
                                            type = NotificationType.Normal
                                    )
                            )
                        },
                "Warning" to
                        {
                            NotificationManager.show(
                                    Notification(
                                            title = "Warning",
                                            message = "A warning message",
                                            type = NotificationType.Warning
                                    )
                            )
                        },
                "Error" to
                        {
                            NotificationManager.show(
                                    Notification(
                                            title = "Error",
                                            message = "An error message",
                                            type = NotificationType.Error
                                    )
                            )
                        },
                "Clickable" to
                        {
                            NotificationManager.show(
                                    Notification(
                                            title = "Clickable",
                                            message = "Click me!",
                                            type = NotificationType.Normal,
                                            isClickable = true,
                                            onClick = {}
                                    )
                            )
                        },
                "Clickable Warning" to
                        {
                            NotificationManager.show(
                                    Notification(
                                            title = "Clickable Warning",
                                            message = "A clickable warning",
                                            type = NotificationType.Warning,
                                            isClickable = true,
                                            onClick = {}
                                    )
                            )
                        },
        )
    }

    Column(modifier = Modifier.padding(16.dp), horizontalAlignment = Alignment.CenterHorizontally) {
        TitleAndSummary(
                title = "Test Notifications",
                summary = "Send different types of notifications"
        )
        Spacer(modifier = Modifier.height(16.dp))

        LazyVerticalGrid(
                columns = GridCells.Fixed(2),
                horizontalArrangement = Arrangement.spacedBy(8.dp),
                verticalArrangement = Arrangement.spacedBy(8.dp),
                modifier =
                        Modifier.height(
                                200.dp
                        ) // LazyVerticalGrid needs a fixed height when nested in a LazyColumn
        ) {
            items(buttons) { (text, onClick) -> Button(onClick = onClick) { Text(text) } }
            item {
                Button(
                        onClick = {
                            val notification =
                                    Notification(
                                            title = "Progress",
                                            message = "Updating...",
                                            type = NotificationType.Progress,
                                            progress = progress,
                                            isClickable = true,
                                            onClick = {}
                                    )
                            progressNotificationId = notification.id
                            NotificationManager.show(notification)
                        }
                ) { Text("Progress") }
            }
        }

        Spacer(modifier = Modifier.height(16.dp))

        SliderLayoutCard(
                value = progress,
                onValueChange = {
                    progress = it
                    progressNotificationId?.let { id -> NotificationManager.updateProgress(id, it) }
                },
                title = "Update Progress",
                summary = "For the last progress notification sent",
                isGlowEffectEnabled = true
        )
    }
}
