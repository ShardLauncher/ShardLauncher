package com.lanrhyme.shardlauncher.ui.notification

import com.lanrhyme.shardlauncher.ui.notification.Notification
import com.lanrhyme.shardlauncher.ui.notification.NotificationType
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update

object NotificationManager {
    private val _notifications = MutableStateFlow<List<Notification>>(emptyList())
    val notifications = _notifications.asStateFlow()

    private val _seenPopupIds = MutableStateFlow<Set<String>>(emptySet())
    val seenPopupIds = _seenPopupIds.asStateFlow()

    fun show(notification: Notification) {
        _notifications.update { it + notification }
    }

    fun dismiss(notificationId: String) {
        _notifications.update { it.filterNot { n -> n.id == notificationId } }
    }

    fun clearAll() {
        _notifications.update { list -> list.filter { it.type == NotificationType.Temporary } }
    }

    fun updateProgress(notificationId: String, progress: Float) {
        val progressClamped = progress.coerceIn(0f, 1f)
        _notifications.update { list ->
            list.map {
                if (it.id == notificationId) it.copy(progress = progressClamped) else it
            }
        }
    }

    fun update(notification: Notification) {
        _notifications.update { list ->
            list.map {
                if (it.id == notification.id) notification else it
            }
        }
    }

    fun addSeenPopupId(id: String) {
        _seenPopupIds.update { it + id }
    }
}
