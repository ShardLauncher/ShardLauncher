package com.lanrhyme.shardlauncher.ui.notification

import androidx.compose.runtime.Immutable
import java.util.UUID

@Immutable
data class Notification(
    val id: String = UUID.randomUUID().toString(),
    val title: String,
    val message: String,
    val type: NotificationType,
    val progress: Float? = null,
    val isClickable: Boolean = false,
    val keepOnScreen: Boolean? = null,
    val onClick: (() -> Unit)? = null
)

enum class NotificationType {
    Temporary,
    Normal,
    Progress,
    Warning,
    Error
}
