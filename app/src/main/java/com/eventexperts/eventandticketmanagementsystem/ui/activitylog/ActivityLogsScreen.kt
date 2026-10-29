package com.eventexperts.eventandticketmanagementsystem.ui.activitylog

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.History
import androidx.compose.material.icons.filled.Search
import androidx.compose.material3.Badge
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.FilterChip
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.eventexperts.eventandticketmanagementsystem.ui.components.EmptyView
import com.eventexperts.eventandticketmanagementsystem.ui.components.LoadingView

@Composable
fun ActivityLogsScreen(
    viewModel: ActivityLogsViewModel
) {
    val uiState by viewModel.uiState.collectAsState()

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp)
    ) {
        Text("Admin System Activity Audit Logs", style = MaterialTheme.typography.headlineSmall, fontWeight = FontWeight.Bold)
        Spacer(modifier = Modifier.height(12.dp))

        OutlinedTextField(
            value = uiState.searchQuery,
            onValueChange = viewModel::onSearchQueryChange,
            placeholder = { Text("Search logs by user, action, or details...") },
            leadingIcon = { Icon(Icons.Default.Search, contentDescription = null) },
            singleLine = true,
            modifier = Modifier.fillMaxWidth()
        )
        Spacer(modifier = Modifier.height(8.dp))

        val filters = listOf("All", "login", "booking", "event", "user", "category")
        LazyRow(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
            items(filters) { filter ->
                val selected = if (filter == "All") uiState.selectedActionFilter == null else uiState.selectedActionFilter == filter
                FilterChip(
                    selected = selected,
                    onClick = { viewModel.onActionFilterSelect(if (filter == "All") null else filter) },
                    label = { Text(filter.uppercase()) }
                )
            }
        }
        Spacer(modifier = Modifier.height(12.dp))

        if (uiState.isLoading) {
            LoadingView(message = "Loading activity audit trail...")
        } else if (uiState.filteredLogs.isEmpty()) {
            EmptyView(
                title = "No Activity Logs Found",
                message = "No system audit logs match your search and filter criteria.",
                icon = Icons.Default.History
            )
        } else {
            LazyColumn(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                items(uiState.filteredLogs, key = { it.id }) { log ->
                    Card(
                        modifier = Modifier.fillMaxWidth(),
                        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
                    ) {
                        Column(modifier = Modifier.padding(16.dp)) {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Text(
                                    text = "${log.userName} (${log.role.uppercase()})",
                                    style = MaterialTheme.typography.titleMedium,
                                    fontWeight = FontWeight.Bold,
                                    modifier = Modifier.weight(1f)
                                )
                                Badge {
                                    Text(log.action.uppercase())
                                }
                            }
                            Spacer(modifier = Modifier.height(4.dp))
                            if (!log.details.isNullOrBlank()) {
                                Text(text = log.details, style = MaterialTheme.typography.bodyMedium)
                                Spacer(modifier = Modifier.height(4.dp))
                            }
                            Text(
                                text = log.createdAt?.toDate()?.toString() ?: "",
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
