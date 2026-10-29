package com.eventexperts.eventandticketmanagementsystem.ui.notifications

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.DoneAll
import androidx.compose.material.icons.filled.Notifications
import androidx.compose.material3.Badge
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.eventexperts.eventandticketmanagementsystem.data.model.User
import com.eventexperts.eventandticketmanagementsystem.ui.components.EmptyView
import com.eventexperts.eventandticketmanagementsystem.ui.components.LoadingView

@Composable
fun NotificationCenterScreen(
    viewModel: NotificationViewModel,
    currentUser: User?,
    onNavigateToEvent: (String) -> Unit,
    onNavigateToTicket: (String) -> Unit
) {
    val uiState by viewModel.uiState.collectAsState()

    LaunchedEffect(currentUser) {
        if (currentUser != null) {
            viewModel.loadNotifications(currentUser.uid)
        }
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp)
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = "Notifications",
                style = MaterialTheme.typography.headlineSmall,
                fontWeight = FontWeight.Bold,
                modifier = Modifier.weight(1f)
            )
            if (uiState.unreadCount > 0 && currentUser != null) {
                OutlinedButton(onClick = { viewModel.markAllAsRead(currentUser.uid) }) {
                    Icon(Icons.Default.DoneAll, contentDescription = null)
                    Spacer(modifier = Modifier.width(4.dp))
                    Text("Mark All Read")
                }
            }
        }
        Spacer(modifier = Modifier.height(16.dp))

        if (uiState.isLoading) {
            LoadingView(message = "Loading notifications...")
        } else if (uiState.notifications.isEmpty()) {
            EmptyView(
                title = "No Notifications",
                message = "You have no unread or recent notifications. Booking confirmations and reminders will appear here.",
                icon = Icons.Default.Notifications
            )
        } else {
            LazyColumn(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                items(uiState.notifications, key = { it.id }) { item ->
                    Card(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clickable {
                                viewModel.markAsRead(item.id)
                                if (!item.eventId.isNullOrBlank()) {
                                    onNavigateToEvent(item.eventId)
                                } else if (!item.bookingId.isNullOrBlank()) {
                                    onNavigateToTicket(item.bookingId)
                                }
                            },
                        colors = CardDefaults.cardColors(
                            containerColor = if (!item.read)
                                MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.4f)
                            else MaterialTheme.colorScheme.surface
                        ),
                        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
                    ) {
                        Column(modifier = Modifier.padding(16.dp)) {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Text(
                                    text = item.title,
                                    style = MaterialTheme.typography.titleMedium,
                                    fontWeight = FontWeight.Bold,
                                    modifier = Modifier.weight(1f)
                                )
                                if (!item.read) {
                                    Badge {
                                        Text("NEW")
                                    }
                                }
                            }
                            Spacer(modifier = Modifier.height(4.dp))
                            Text(
                                text = item.body,
                                style = MaterialTheme.typography.bodyMedium
                            )
                            Spacer(modifier = Modifier.height(6.dp))
                            Text(
                                text = item.createdAt?.toDate()?.toString() ?: "",
                                style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                    }
                }
            }
        }
    }
}
