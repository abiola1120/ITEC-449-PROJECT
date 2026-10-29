package com.eventexperts.eventandticketmanagementsystem.ui.booking

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
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.ConfirmationNumber
import androidx.compose.material.icons.filled.Email
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.Phone
import androidx.compose.material.icons.filled.Remove
import androidx.compose.material3.Badge
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.RadioButton
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import com.eventexperts.eventandticketmanagementsystem.data.model.User
import com.eventexperts.eventandticketmanagementsystem.ui.components.EmptyView
import com.eventexperts.eventandticketmanagementsystem.ui.components.LoadingView
import com.eventexperts.eventandticketmanagementsystem.ui.components.PrimaryButton
import java.util.Locale

@Composable
fun BookingScreen(
    eventId: String,
    viewModel: BookingViewModel,
    currentUser: User?,
    onBookingSuccess: (String) -> Unit
) {
    val uiState by viewModel.bookingState.collectAsState()
    val snackbarHostState = remember { SnackbarHostState() }

    LaunchedEffect(eventId, currentUser) {
        viewModel.loadBookingData(eventId, currentUser)
    }

    if (uiState.isLoading) {
        LoadingView(message = "Preparing checkout...")
        return
    }

    val event = uiState.event
    if (event == null) {
        EmptyView(title = "Event Not Found", message = "Unable to load event checkout.")
        return
    }

    if (uiState.completedBooking != null) {
        val booking = uiState.completedBooking!!
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(24.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {
            Icon(
                imageVector = Icons.Default.CheckCircle,
                contentDescription = null,
                tint = MaterialTheme.colorScheme.primary,
                modifier = Modifier.height(72.dp)
            )
            Spacer(modifier = Modifier.height(16.dp))
            Text(
                text = "Booking Confirmed!",
                style = MaterialTheme.typography.headlineMedium,
                fontWeight = FontWeight.Bold
            )
            Spacer(modifier = Modifier.height(8.dp))
            Text(
                text = "Reference: ${booking.bookingRef}",
                style = MaterialTheme.typography.titleMedium,
                color = MaterialTheme.colorScheme.primary,
                fontWeight = FontWeight.SemiBold
            )
            Spacer(modifier = Modifier.height(16.dp))
            Text(
                text = "Your booking for ${booking.quantity} ticket(s) to '${booking.eventTitle}' has been confirmed. In-app notification sent.",
                style = MaterialTheme.typography.bodyMedium,
                textAlign = TextAlign.Center
            )
            Spacer(modifier = Modifier.height(32.dp))
            PrimaryButton(
                text = "View Digital Ticket",
                onClick = { onBookingSuccess(booking.bookingId) }
            )
        }
        return
    }

    Scaffold(
        snackbarHost = { SnackbarHost(snackbarHostState) }
    ) { innerPadding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .verticalScroll(rememberScrollState())
                .padding(innerPadding)
                .padding(16.dp)
        ) {
            Text(
                text = "Checkout & Booking",
                style = MaterialTheme.typography.headlineSmall,
                fontWeight = FontWeight.Bold
            )
            Text(
                text = event.title,
                style = MaterialTheme.typography.titleMedium,
                color = MaterialTheme.colorScheme.primary
            )
            Spacer(modifier = Modifier.height(16.dp))

            if (uiState.errorMessage != null) {
                Text(
                    text = uiState.errorMessage!!,
                    color = MaterialTheme.colorScheme.error,
                    style = MaterialTheme.typography.bodySmall,
                    modifier = Modifier.padding(bottom = 12.dp)
                )
            }

            Text("Select Ticket Type:", style = MaterialTheme.typography.labelLarge)
            Spacer(modifier = Modifier.height(8.dp))

            if (uiState.ticketTypes.isEmpty()) {
                Text(text = "General Admission ($${String.format(Locale.US, "%.2f", event.basePrice / 100.0)})", style = MaterialTheme.typography.bodyMedium)
            } else {
                uiState.ticketTypes.forEach { type ->
                    Card(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(vertical = 4.dp)
                            .clickable { viewModel.onTicketTypeSelect(type) },
                        colors = CardDefaults.cardColors(
                            containerColor = if (uiState.selectedTicketType?.typeId == type.typeId)
                                MaterialTheme.colorScheme.primaryContainer
                            else MaterialTheme.colorScheme.surface
                        )
                    ) {
                        Row(
                            modifier = Modifier.padding(12.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            RadioButton(
                                selected = uiState.selectedTicketType?.typeId == type.typeId,
                                onClick = { viewModel.onTicketTypeSelect(type) }
                            )
                            Spacer(modifier = Modifier.width(8.dp))
                            Column(modifier = Modifier.weight(1f)) {
                                Text(text = type.name, style = MaterialTheme.typography.bodyLarge, fontWeight = FontWeight.SemiBold)
                                Text(text = "Available: ${type.availableQuantity}", style = MaterialTheme.typography.bodySmall)
                            }
                            Text(
                                text = "$${String.format(Locale.US, "%.2f", type.price / 100.0)}",
                                style = MaterialTheme.typography.titleMedium,
                                fontWeight = FontWeight.Bold
                            )
                        }
                    }
                }
            }
            Spacer(modifier = Modifier.height(16.dp))

            Text("Quantity:", style = MaterialTheme.typography.labelLarge)
            Row(
                verticalAlignment = Alignment.CenterVertically,
                modifier = Modifier.padding(vertical = 8.dp)
            ) {
                IconButton(
                    onClick = { viewModel.onQuantityChange(uiState.quantity - 1) },
                    enabled = uiState.quantity > 1
                ) {
                    Icon(Icons.Default.Remove, contentDescription = "Decrease")
                }
                Text(
                    text = "${uiState.quantity}",
                    style = MaterialTheme.typography.titleLarge,
                    fontWeight = FontWeight.Bold,
                    modifier = Modifier.padding(horizontal = 16.dp)
                )
                IconButton(
                    onClick = { viewModel.onQuantityChange(uiState.quantity + 1) },
                    enabled = uiState.quantity < 10
                ) {
                    Icon(Icons.Default.Add, contentDescription = "Increase")
                }
            }
            Spacer(modifier = Modifier.height(16.dp))

            Text("Attendee Contact Info:", style = MaterialTheme.typography.labelLarge)
            Spacer(modifier = Modifier.height(8.dp))
            OutlinedTextField(
                value = uiState.attendeeName,
                onValueChange = viewModel::onAttendeeNameChange,
                label = { Text("Full Name") },
                leadingIcon = { Icon(Icons.Default.Person, contentDescription = null) },
                singleLine = true,
                modifier = Modifier.fillMaxWidth()
            )
            Spacer(modifier = Modifier.height(8.dp))
            OutlinedTextField(
                value = uiState.attendeeEmail,
                onValueChange = viewModel::onAttendeeEmailChange,
                label = { Text("Email Address") },
                leadingIcon = { Icon(Icons.Default.Email, contentDescription = null) },
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Email),
                singleLine = true,
                modifier = Modifier.fillMaxWidth()
            )
            Spacer(modifier = Modifier.height(8.dp))
            OutlinedTextField(
                value = uiState.attendeePhone,
                onValueChange = viewModel::onAttendeePhoneChange,
                label = { Text("Phone Number (Optional)") },
                leadingIcon = { Icon(Icons.Default.Phone, contentDescription = null) },
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Phone),
                singleLine = true,
                modifier = Modifier.fillMaxWidth()
            )
            Spacer(modifier = Modifier.height(16.dp))

            // Promo Code Section
            Text("Promo Code:", style = MaterialTheme.typography.labelLarge)
            Spacer(modifier = Modifier.height(4.dp))
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically
            ) {
                OutlinedTextField(
                    value = uiState.promoCodeInput,
                    onValueChange = viewModel::onPromoCodeChange,
                    label = { Text("Enter promo code") },
                    singleLine = true,
                    modifier = Modifier.weight(1f),
                    enabled = uiState.appliedPromoCode == null
                )
                Spacer(modifier = Modifier.width(8.dp))
                if (uiState.appliedPromoCode == null) {
                    OutlinedButton(
                        onClick = viewModel::applyPromoCode,
                        modifier = Modifier.height(56.dp)
                    ) {
                        Text("Apply")
                    }
                } else {
                    OutlinedButton(
                        onClick = viewModel::removePromoCode,
                        modifier = Modifier.height(56.dp)
                    ) {
                        Text("Remove")
                    }
                }
            }
            if (!uiState.promoError.isNullOrBlank()) {
                Text(text = uiState.promoError!!, color = MaterialTheme.colorScheme.error, style = MaterialTheme.typography.bodySmall)
            }
            if (!uiState.promoSuccess.isNullOrBlank()) {
                Text(text = uiState.promoSuccess!!, color = MaterialTheme.colorScheme.primary, style = MaterialTheme.typography.bodySmall)
            }
            Spacer(modifier = Modifier.height(24.dp))

            // Cost Summary Card
            Card(
                modifier = Modifier.fillMaxWidth(),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant)
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Text("Payment Summary", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)
                    Spacer(modifier = Modifier.height(8.dp))
                    Row {
                        Text("Subtotal (${uiState.quantity} ticket(s))")
                        Spacer(modifier = Modifier.weight(1f))
                        Text("$${String.format(Locale.US, "%.2f", uiState.subtotal / 100.0)}")
                    }
                    if (uiState.discountAmount > 0) {
                        Spacer(modifier = Modifier.height(4.dp))
                        Row {
                            Text("Discount (${uiState.appliedPromoCode?.code})", color = MaterialTheme.colorScheme.primary)
                            Spacer(modifier = Modifier.weight(1f))
                            Text("-$${String.format(Locale.US, "%.2f", uiState.discountAmount / 100.0)}", color = MaterialTheme.colorScheme.primary)
                        }
                    }
                    Spacer(modifier = Modifier.height(4.dp))
                    Row {
                        Text("Service Fee (5%)")
                        Spacer(modifier = Modifier.weight(1f))
                        Text("$${String.format(Locale.US, "%.2f", uiState.feeAmount / 100.0)}")
                    }
                    HorizontalDivider(modifier = Modifier.padding(vertical = 8.dp))
                    Row {
                        Text("Total Amount", fontWeight = FontWeight.Bold)
                        Spacer(modifier = Modifier.weight(1f))
                        Text(
                            text = "$${String.format(Locale.US, "%.2f", uiState.totalAmount / 100.0)}",
                            fontWeight = FontWeight.Bold,
                            style = MaterialTheme.typography.titleLarge,
                            color = MaterialTheme.colorScheme.primary
                        )
                    }
                }
            }
            Spacer(modifier = Modifier.height(24.dp))

            PrimaryButton(
                text = "Confirm & Book Tickets",
                onClick = { if (currentUser != null) viewModel.submitBooking(currentUser) },
                isLoading = uiState.isSubmitting
            )
        }
    }
}

@Composable
fun MyBookingsScreen(
    viewModel: BookingViewModel,
    currentUser: User?,
    onTicketClick: (String) -> Unit
) {
    val uiState by viewModel.myBookingsState.collectAsState()

    LaunchedEffect(currentUser) {
        if (currentUser != null) {
            viewModel.loadMyBookings(currentUser.uid)
        }
    }

    Column(modifier = Modifier.fillMaxSize().padding(16.dp)) {
        Text("My Bookings & Tickets", style = MaterialTheme.typography.headlineSmall, fontWeight = FontWeight.Bold)
        Spacer(modifier = Modifier.height(16.dp))

        if (uiState.isLoading) {
            LoadingView(message = "Loading your bookings...")
        } else if (uiState.bookings.isEmpty()) {
            EmptyView(
                title = "No Bookings Found",
                message = "You haven't booked any tickets yet. Explore upcoming events on the Home tab!",
                icon = Icons.Default.ConfirmationNumber
            )
        } else {
            LazyColumn(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                items(uiState.bookings, key = { it.bookingId }) { booking ->
                    Card(
                        modifier = Modifier.fillMaxWidth().clickable { onTicketClick(booking.bookingId) },
                        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
                    ) {
                        Column(modifier = Modifier.padding(16.dp)) {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Text(
                                    text = booking.eventTitle,
                                    style = MaterialTheme.typography.titleMedium,
                                    fontWeight = FontWeight.Bold,
                                    modifier = Modifier.weight(1f)
                                )
                                Badge {
                                    Text(booking.bookingRef)
                                }
                            }
                            Spacer(modifier = Modifier.height(6.dp))
                            Text(
                                text = "${booking.quantity}x ${booking.ticketTypeName} • Total: $${String.format(Locale.US, "%.2f", booking.totalAmount / 100.0)}",
                                style = MaterialTheme.typography.bodyMedium,
                                color = MaterialTheme.colorScheme.primary
                            )
                            Spacer(modifier = Modifier.height(8.dp))
                            Row {
                                OutlinedButton(onClick = { onTicketClick(booking.bookingId) }) {
                                    Text("View E-Ticket")
                                }
                            }
                        }
                    }
                }
            }
        }
    }
}
