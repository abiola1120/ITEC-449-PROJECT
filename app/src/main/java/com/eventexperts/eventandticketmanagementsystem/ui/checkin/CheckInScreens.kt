package com.eventexperts.eventandticketmanagementsystem.ui.checkin

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
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Error
import androidx.compose.material.icons.filled.Group
import androidx.compose.material.icons.filled.QrCodeScanner
import androidx.compose.material.icons.filled.Search
import androidx.compose.material.icons.filled.Warning
import androidx.compose.material3.Badge
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.FilterChip
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
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
import com.eventexperts.eventandticketmanagementsystem.data.repository.CheckInResult
import com.eventexperts.eventandticketmanagementsystem.ui.components.EmptyView
import com.eventexperts.eventandticketmanagementsystem.ui.components.LoadingView
import com.eventexperts.eventandticketmanagementsystem.ui.components.PrimaryButton

@Composable
fun CheckInScreen(
    eventId: String,
    viewModel: CheckInViewModel,
    currentUser: User?,
    onAttendeesListClick: (String) -> Unit
) {
    val uiState by viewModel.uiState.collectAsState()

    LaunchedEffect(eventId) {
        viewModel.loadEventData(eventId)
    }

    if (uiState.isLoading) {
        LoadingView(message = "Loading check-in system...")
        return
    }

    val event = uiState.event

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(24.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Text(
            text = "Organizer Ticket Check-In",
            style = MaterialTheme.typography.headlineSmall,
            fontWeight = FontWeight.Bold
        )
        if (event != null) {
            Text(
                text = event.title,
                style = MaterialTheme.typography.titleMedium,
                color = MaterialTheme.colorScheme.primary
            )
        }
        Spacer(modifier = Modifier.height(24.dp))

        // Check-in Input Card
        Card(
            modifier = Modifier.fillMaxWidth(),
            elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
        ) {
            Column(modifier = Modifier.padding(16.dp)) {
                Text("Enter Ticket Reference:", style = MaterialTheme.typography.labelLarge)
                Spacer(modifier = Modifier.height(8.dp))
                OutlinedTextField(
                    value = uiState.manualInput,
                    onValueChange = viewModel::onManualInputChange,
                    label = { Text("e.g. EVT-ABC123XYZ") },
                    leadingIcon = { Icon(Icons.Default.QrCodeScanner, contentDescription = null) },
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth()
                )
                Spacer(modifier = Modifier.height(16.dp))
                PrimaryButton(
                    text = "Validate & Check In",
                    onClick = { if (currentUser != null) viewModel.submitCheckIn(currentUser) },
                    isLoading = uiState.isSubmitting
                )
            }
        }
        Spacer(modifier = Modifier.height(24.dp))

        // Result Banner
        when (val result = uiState.checkInResult) {
            is CheckInResult.Success -> {
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.primaryContainer)
                ) {
                    Row(
                        modifier = Modifier.padding(16.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Icon(Icons.Default.CheckCircle, contentDescription = null, tint = MaterialTheme.colorScheme.primary)
                        Spacer(modifier = Modifier.width(12.dp))
                        Column {
                            Text("✅ VALID TICKET", fontWeight = FontWeight.Bold)
                            Text("Attendee: ${result.attendeeName} (${result.ticketTypeName})")
                        }
                    }
                }
            }
            is CheckInResult.AlreadyUsed -> {
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.errorContainer)
                ) {
                    Row(
                        modifier = Modifier.padding(16.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Icon(Icons.Default.Warning, contentDescription = null, tint = MaterialTheme.colorScheme.error)
                        Spacer(modifier = Modifier.width(12.dp))
                        Column {
                            Text("⚠️ ALREADY USED", fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.error)
                            Text("Ticket '${result.ticketRef}' was already checked in!")
                        }
                    }
                }
            }
            is CheckInResult.WrongEvent -> {
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.errorContainer)
                ) {
                    Row(modifier = Modifier.padding(16.dp), verticalAlignment = Alignment.CenterVertically) {
                        Icon(Icons.Default.Error, contentDescription = null, tint = MaterialTheme.colorScheme.error)
                        Spacer(modifier = Modifier.width(12.dp))
                        Text("❌ WRONG EVENT - Ticket belongs to another event", fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.error)
                    }
                }
            }
            is CheckInResult.CancelledTicket -> {
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.errorContainer)
                ) {
                    Row(modifier = Modifier.padding(16.dp), verticalAlignment = Alignment.CenterVertically) {
                        Icon(Icons.Default.Error, contentDescription = null, tint = MaterialTheme.colorScheme.error)
                        Spacer(modifier = Modifier.width(12.dp))
                        Text("❌ CANCELLED TICKET - Ticket has been cancelled", fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.error)
                    }
                }
            }
            is CheckInResult.NotFound -> {
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.errorContainer)
                ) {
                    Row(modifier = Modifier.padding(16.dp), verticalAlignment = Alignment.CenterVertically) {
                        Icon(Icons.Default.Error, contentDescription = null, tint = MaterialTheme.colorScheme.error)
                        Spacer(modifier = Modifier.width(12.dp))
                        Text("❌ NOT FOUND - Invalid ticket reference", fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.error)
                    }
                }
            }
            else -> {}
        }

        Spacer(modifier = Modifier.weight(1f))

        OutlinedButton(
            onClick = { onAttendeesListClick(eventId) },
            modifier = Modifier.fillMaxWidth().height(50.dp)
        ) {
            Icon(Icons.Default.Group, contentDescription = null)
            Spacer(modifier = Modifier.width(8.dp))
            Text("View Attendee List (${uiState.checkedInCount}/${uiState.totalTickets} Checked In)")
        }
    }
}

@Composable
fun AttendeeListScreen(
    eventId: String,
    viewModel: CheckInViewModel,
    currentUser: User?
) {
    val uiState by viewModel.uiState.collectAsState()

    LaunchedEffect(eventId) {
        viewModel.loadEventData(eventId)
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp)
    ) {
        Card(
            modifier = Modifier.fillMaxWidth().padding(bottom = 12.dp),
            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.primaryContainer)
        ) {
            Column(modifier = Modifier.padding(16.dp)) {
                Text("Check-In Progress", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)
                Spacer(modifier = Modifier.height(4.dp))
                Text(
                    text = "${uiState.checkedInCount} of ${uiState.totalTickets} attendees checked in (${if (uiState.totalTickets == 0) 0 else (uiState.checkedInCount * 100 / uiState.totalTickets)}%)",
                    style = MaterialTheme.typography.bodyMedium
                )
            }
        }

        OutlinedTextField(
            value = uiState.searchQuery,
            onValueChange = viewModel::onSearchQueryChange,
            placeholder = { Text("Search attendee name or ticket ref...") },
            leadingIcon = { Icon(Icons.Default.Search, contentDescription = null) },
            singleLine = true,
            modifier = Modifier.fillMaxWidth()
        )
        Spacer(modifier = Modifier.height(8.dp))

        Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
            FilterChip(
                selected = uiState.statusFilter == "all",
                onClick = { viewModel.onStatusFilterChange("all") },
                label = { Text("All (${uiState.tickets.size})") }
            )
            FilterChip(
                selected = uiState.statusFilter == "checked_in",
                onClick = { viewModel.onStatusFilterChange("checked_in") },
                label = { Text("Checked In (${uiState.checkedInCount})") }
            )
            FilterChip(
                selected = uiState.statusFilter == "not_checked_in",
                onClick = { viewModel.onStatusFilterChange("not_checked_in") },
                label = { Text("Not Checked In (${uiState.totalTickets - uiState.checkedInCount})") }
            )
        }
        Spacer(modifier = Modifier.height(12.dp))

        if (uiState.filteredTickets.isEmpty()) {
            EmptyView(
                title = "No Attendees Found",
                message = "No tickets match the search query or selected filter.",
                icon = Icons.Default.Group
            )
        } else {
            LazyColumn(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                items(uiState.filteredTickets, key = { it.ticketRef }) { ticket ->
                    Card(
                        modifier = Modifier.fillMaxWidth(),
                        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
                    ) {
                        Row(
                            modifier = Modifier.padding(16.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Column(modifier = Modifier.weight(1f)) {
                                Text(text = ticket.attendeeName, style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)
                                Spacer(modifier = Modifier.height(2.dp))
                                Text(text = "${ticket.ticketTypeName} • Ref: ${ticket.ticketRef}", style = MaterialTheme.typography.bodySmall)
                            }
                            if (ticket.status == "used") {
                                Badge {
                                    Text("CHECKED IN")
                                }
                            } else if (ticket.status == "valid") {
                                OutlinedButton(
                                    onClick = {
                                        if (currentUser != null) {
                                            viewModel.onManualInputChange(ticket.ticketRef)
                                            viewModel.submitCheckIn(currentUser)
                                        }
                                    }
                                ) {
                                    Icon(Icons.Default.Check, contentDescription = null)
                                    Spacer(modifier = Modifier.width(4.dp))
                                    Text("Check In")
                                }
                            } else {
                                Badge {
                                    Text(ticket.status.uppercase())
                                }
                            }
                        }
                    }
                }
            }
        }
    }
}
