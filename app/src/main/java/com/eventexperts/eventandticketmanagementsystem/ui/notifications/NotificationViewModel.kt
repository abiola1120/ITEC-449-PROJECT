package com.eventexperts.eventandticketmanagementsystem.ui.notifications

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.eventexperts.eventandticketmanagementsystem.data.model.InAppNotification
import com.eventexperts.eventandticketmanagementsystem.data.repository.NotificationRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.launch

data class NotificationsUiState(
    val notifications: List<InAppNotification> = emptyList(),
    val isLoading: Boolean = false
) {
    val unreadCount: Int get() = notifications.count { !it.read }
}

class NotificationViewModel(
    private val notificationRepository: NotificationRepository
) : ViewModel() {

    private val _uiState = MutableStateFlow(NotificationsUiState())
    val uiState: StateFlow<NotificationsUiState> = _uiState.asStateFlow()

    fun loadNotifications(userId: String) {
        viewModelScope.launch {
            _uiState.value = _uiState.value.copy(isLoading = true)
            notificationRepository.observeUserNotifications(userId)
                .catch { _uiState.value = _uiState.value.copy(isLoading = false) }
                .collect { list ->
                    val sorted = list.sortedByDescending { it.createdAt }
                    _uiState.value = NotificationsUiState(notifications = sorted, isLoading = false)
                }
        }
    }

    fun markAsRead(notificationId: String) {
        viewModelScope.launch {
            try {
                notificationRepository.markAsRead(notificationId)
            } catch (e: Exception) {
                // handle error
            }
        }
    }

    fun markAllAsRead(userId: String) {
        viewModelScope.launch {
            try {
                notificationRepository.markAllAsRead(userId)
            } catch (e: Exception) {
                // handle error
            }
        }
    }

    companion object {
        fun provideFactory(notificationRepository: NotificationRepository): ViewModelProvider.Factory =
            object : ViewModelProvider.Factory {
                @Suppress("UNCHECKED_CAST")
                override fun <T : ViewModel> create(modelClass: Class<T>): T {
                    return NotificationViewModel(notificationRepository) as T
                }
            }
    }
}
