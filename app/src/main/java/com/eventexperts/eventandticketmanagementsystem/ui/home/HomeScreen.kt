package com.eventexperts.eventandticketmanagementsystem.ui.home

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Event
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.eventexperts.eventandticketmanagementsystem.ui.components.EmptyView
import com.eventexperts.eventandticketmanagementsystem.ui.components.EventCard
import com.eventexperts.eventandticketmanagementsystem.ui.components.LoadingView

@Composable
fun HomeScreen(
    viewModel: HomeViewModel,
    onEventClick: (String) -> Unit
) {
    val uiState by viewModel.uiState.collectAsState()

    Column(modifier = Modifier.fillMaxSize()) {
        Text(
            text = "Upcoming Published Events",
            style = MaterialTheme.typography.titleLarge,
            fontWeight = FontWeight.Bold,
            modifier = Modifier.padding(start = 16.dp, top = 16.dp, end = 16.dp, bottom = 8.dp)
        )

        if (uiState.isLoading) {
            LoadingView(message = "Loading upcoming events...")
        } else if (uiState.publishedEvents.isEmpty()) {
            EmptyView(
                title = "No Upcoming Events",
                message = "There are currently no published upcoming events available. Check back soon!",
                icon = Icons.Default.Event
            )
        } else {
            LazyColumn(
                modifier = Modifier.fillMaxSize(),
                contentPadding = PaddingValues(16.dp),
                verticalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                items(uiState.publishedEvents, key = { it.eventId }) { event ->
                    EventCard(
                        title = event.title,
                        dateText = event.startAt?.toDate()?.toString() ?: "Upcoming",
                        venueName = "${event.venueName}, ${event.city}",
                        priceText = if (event.basePrice == 0) "Free" else "From $${String.format("%.2f", event.basePrice / 100.0)}",
                        seatsLeftText = "${event.seatsRemaining} seats left",
                        imageUrl = event.imageUrls.firstOrNull(),
                        categoryName = event.categoryName,
                        startAt = event.startAt,
                        onClick = { onEventClick(event.eventId) }
                    )
                }
            }
        }
    }
}
