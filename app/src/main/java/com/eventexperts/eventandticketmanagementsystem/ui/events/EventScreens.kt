package com.eventexperts.eventandticketmanagementsystem.ui.events

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.CalendarToday
import androidx.compose.material.icons.filled.ConfirmationNumber
import androidx.compose.material.icons.filled.LocationOn
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.Search
import androidx.compose.material3.Badge
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.FilterChip
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Slider
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.eventexperts.eventandticketmanagementsystem.ui.components.EmptyView
import com.eventexperts.eventandticketmanagementsystem.ui.components.EventCard
import com.eventexperts.eventandticketmanagementsystem.ui.components.LoadingView
import com.eventexperts.eventandticketmanagementsystem.ui.components.PrimaryButton
import java.util.Locale

@Composable
fun SearchScreen(
    viewModel: SearchViewModel,
    onEventClick: (String) -> Unit
) {
    val uiState by viewModel.uiState.collectAsState()

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp)
    ) {
        OutlinedTextField(
            value = uiState.query,
            onValueChange = viewModel::onQueryChange,
            placeholder = { Text("Search by title, venue, or keyword...") },
            leadingIcon = { Icon(Icons.Default.Search, contentDescription = null) },
            singleLine = true,
            modifier = Modifier.fillMaxWidth()
        )
        Spacer(modifier = Modifier.height(12.dp))

        Text("Category Filter:", style = MaterialTheme.typography.labelLarge)
        Spacer(modifier = Modifier.height(6.dp))
        LazyRow(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
            item {
                FilterChip(
                    selected = uiState.selectedCategory == null,
                    onClick = { viewModel.onCategorySelect(null) },
                    label = { Text("All Categories") }
                )
            }
            items(uiState.categories) { category ->
                FilterChip(
                    selected = uiState.selectedCategory?.id == category.id,
                    onClick = { viewModel.onCategorySelect(category) },
                    label = { Text(category.name) }
                )
            }
        }
        Spacer(modifier = Modifier.height(12.dp))

        Row(verticalAlignment = Alignment.CenterVertically) {
            Text(
                text = "Max Price: $${uiState.maxPriceDollars.toInt()}",
                style = MaterialTheme.typography.labelLarge
            )
            Spacer(modifier = Modifier.weight(1f))
        }
        Slider(
            value = uiState.maxPriceDollars.toFloat(),
            onValueChange = { viewModel.onMaxPriceChange(it.toDouble()) },
            valueRange = 0f..200f
        )
        Spacer(modifier = Modifier.height(16.dp))

        if (uiState.filteredEvents.isEmpty()) {
            EmptyView(
                title = "No Matching Events",
                message = "Try clearing search keywords or adjusting your category and price filters."
            )
        } else {
            LazyColumn(
                modifier = Modifier.fillMaxSize(),
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                items(uiState.filteredEvents, key = { it.eventId }) { event ->
                    EventCard(
                        title = event.title,
                        dateText = event.startAt?.toDate()?.toString() ?: "Upcoming",
                        venueName = "${event.venueName}, ${event.city}",
                        priceText = if (event.basePrice == 0) "Free" else "$${String.format(Locale.US, "%.2f", event.basePrice / 100.0)}",
                        seatsLeftText = "${event.seatsRemaining} seats left",
                        onClick = { onEventClick(event.eventId) }
                    )
                }
            }
        }
    }
}

@Composable
fun EventDetailsScreen(
    eventId: String,
    viewModel: EventDetailsViewModel,
    onBookClick: (String) -> Unit,
    onReviewsClick: (String) -> Unit
) {
    val uiState by viewModel.uiState.collectAsState()

    LaunchedEffect(eventId) {
        viewModel.loadEventDetails(eventId)
    }

    if (uiState.isLoading) {
        LoadingView(message = "Loading event details...")
        return
    }

    val event = uiState.event
    if (event == null) {
        EmptyView(title = "Event Not Found", message = "The requested event could not be found.")
        return
    }

    Scaffold(
        bottomBar = {
            Card(
                modifier = Modifier.fillMaxWidth(),
                elevation = CardDefaults.cardElevation(defaultElevation = 8.dp)
            ) {
                Row(
                    modifier = Modifier.padding(16.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column(modifier = Modifier.weight(1f)) {
                        Text(text = "Price from", style = MaterialTheme.typography.labelMedium)
                        Text(
                            text = if (event.basePrice == 0) "Free" else "$${String.format(Locale.US, "%.2f", event.basePrice / 100.0)}",
                            style = MaterialTheme.typography.titleLarge,
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.primary
                        )
                    }
                    PrimaryButton(
                        text = if (event.isSoldOut) "Sold Out" else "Book Now",
                        enabled = !event.isSoldOut && event.isPublished,
                        onClick = { onBookClick(event.eventId) },
                        modifier = Modifier.width(160.dp)
                    )
                }
            }
        }
    ) { innerPadding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .verticalScroll(rememberScrollState())
                .padding(innerPadding)
                .padding(16.dp)
        ) {
            Text(
                text = event.title,
                style = MaterialTheme.typography.headlineSmall,
                fontWeight = FontWeight.Bold
            )
            Spacer(modifier = Modifier.height(8.dp))

            Row(verticalAlignment = Alignment.CenterVertically) {
                Badge { Text(event.categoryName) }
                Spacer(modifier = Modifier.width(8.dp))
                Badge { Text("${event.seatsRemaining} seats remaining") }
            }
            Spacer(modifier = Modifier.height(16.dp))

            Card(
                modifier = Modifier.fillMaxWidth(),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant)
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(Icons.Default.CalendarToday, contentDescription = null, tint = MaterialTheme.colorScheme.primary)
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(text = event.startAt?.toDate()?.toString() ?: "Upcoming", style = MaterialTheme.typography.bodyMedium)
                    }
                    Spacer(modifier = Modifier.height(8.dp))
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(Icons.Default.LocationOn, contentDescription = null, tint = MaterialTheme.colorScheme.primary)
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(text = "${event.venueName}, ${event.address}, ${event.city}", style = MaterialTheme.typography.bodyMedium)
                    }
                    Spacer(modifier = Modifier.height(8.dp))
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(Icons.Default.Person, contentDescription = null, tint = MaterialTheme.colorScheme.primary)
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(text = "Hosted by ${event.organizerName}", style = MaterialTheme.typography.bodyMedium)
                    }
                }
            }
            Spacer(modifier = Modifier.height(24.dp))

            Text("About Event", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)
            Spacer(modifier = Modifier.height(8.dp))
            Text(text = event.description, style = MaterialTheme.typography.bodyMedium)
            Spacer(modifier = Modifier.height(24.dp))

            Text("Available Ticket Options", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)
            Spacer(modifier = Modifier.height(8.dp))

            if (uiState.ticketTypes.isEmpty()) {
                Text("General Admission ($${String.format(Locale.US, "%.2f", event.basePrice / 100.0)})", style = MaterialTheme.typography.bodyMedium)
            } else {
                uiState.ticketTypes.forEach { type ->
                    Card(
                        modifier = Modifier.fillMaxWidth().padding(vertical = 4.dp)
                    ) {
                        Row(
                            modifier = Modifier.padding(12.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Icon(Icons.Default.ConfirmationNumber, contentDescription = null, tint = MaterialTheme.colorScheme.primary)
                            Spacer(modifier = Modifier.width(8.dp))
                            Column(modifier = Modifier.weight(1f)) {
                                Text(text = type.name, style = MaterialTheme.typography.bodyLarge, fontWeight = FontWeight.SemiBold)
                                Text(
                                    text = "Available: ${type.availableQuantity}",
                                    style = MaterialTheme.typography.bodySmall,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant
                                )
                            }
                            Text(
                                text = "$${String.format(Locale.US, "%.2f", type.price / 100.0)}",
                                style = MaterialTheme.typography.titleMedium,
                                fontWeight = FontWeight.Bold,
                                color = MaterialTheme.colorScheme.primary
                            )
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun ReviewsScreen(eventId: String) {
    Column(
        modifier = Modifier.fillMaxSize().padding(16.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        Text("Ratings & Reviews for Event: $eventId")
    }
}
