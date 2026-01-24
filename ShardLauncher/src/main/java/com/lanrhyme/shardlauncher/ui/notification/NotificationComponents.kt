package com.lanrhyme.shardlauncher.ui.notification

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Close
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp

@Composable
fun NotificationItem(
    notification: Notification,
    modifier: Modifier = Modifier,
    durationProgress: Float? = null
) {
    val backgroundColor = when (notification.type) {
        NotificationType.Warning -> androidx.compose.ui.graphics.Color(0xFFFFD54F) // Yellow 300
        NotificationType.Error -> MaterialTheme.colorScheme.errorContainer
        else -> MaterialTheme.colorScheme.surfaceVariant
    }
    
    val contentColor = when (notification.type) {
        NotificationType.Warning -> androidx.compose.ui.graphics.Color(0xFF3E2723) // Brown 900
        NotificationType.Error -> MaterialTheme.colorScheme.onErrorContainer
        else -> MaterialTheme.colorScheme.onSurfaceVariant
    }
    
    val shape = RoundedCornerShape(22.dp)

    Surface(
        modifier = modifier.clickable(enabled = notification.isClickable, onClick = { notification.onClick?.invoke() }),
        shape = shape,
        color = backgroundColor,
        contentColor = contentColor,
        tonalElevation = 3.dp,
        shadowElevation = 3.dp
    ) {
        Column {
            Row(
                modifier = Modifier.padding(16.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column(modifier = Modifier.weight(1f)) {
                    Text(text = notification.title, style = MaterialTheme.typography.titleMedium)
                    Spacer(modifier = Modifier.height(4.dp))
                    Text(text = notification.message, style = MaterialTheme.typography.bodyMedium)
                    if (notification.type == NotificationType.Progress && notification.progress != null) {
                        Spacer(modifier = Modifier.height(8.dp))
                        LinearProgressIndicator(
                            progress = notification.progress,
                            modifier = Modifier.fillMaxWidth()
                        )
                    }
                }
            }
            if (durationProgress != null) {
                LinearProgressIndicator(
                    progress = durationProgress,
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(4.dp),
                    color = contentColor.copy(alpha = 0.5f),
                    trackColor = androidx.compose.ui.graphics.Color.Transparent
                )
            }
        }
    }
}
