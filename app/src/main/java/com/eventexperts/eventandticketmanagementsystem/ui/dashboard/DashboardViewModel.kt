package com.eventexperts.eventandticketmanagementsystem.ui.dashboard

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.eventexperts.eventandticketmanagementsystem.data.model.Booking
import com.eventexperts.eventandticketmanagementsystem.data.model.BookingStatus
import com.eventexperts.eventandticketmanagementsystem.data.model.Event
import com.eventexperts.eventandticketmanagementsystem.data.model.User
import com.eventexperts.eventandticketmanagementsystem.data.model.UserRole
import com.eventexperts.eventandticketmanagementsystem.data.repository.BookingRepository
import com.eventexperts.eventandticketmanagementsystem.data.repository.EventRepository
import com.eventexperts.eventandticketmanagementsystem.data.repository.UserRepository
import com.google.firebase.firestore.FirebaseFirestore
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import kotlinx.coroutines.tasks.await

data class EventStatRow(
    val eventTitle: String,
    val bookedCount: Int,
    val capacity: Int,
    val revenueCents: Int
) {
    val occupancyRate: Int get() = if (capacity == 0) 0 else ((bookedCount.toDouble() / capacity) * 100).toInt()
}

data class DashboardUiState(
    val totalUsers: Int = 0,
    val totalEvents: Int = 0,
    val totalBookings: Int = 0,
    val totalTicketsSold: Int = 0,
    val totalRevenueCents: Int = 0,
    val organizerEventRows: List<EventStatRow> = emptyList(),
    val isLoading: Boolean = false
)

class DashboardViewModel(
    private val firestore: FirebaseFirestore
) : ViewModel() {

    private val _uiState = MutableStateFlow(DashboardUiState())
    val uiState: StateFlow<DashboardUiState> = _uiState.asStateFlow()

    fun loadDashboardData(user: User?) {
        if (user == null) return

        viewModelScope.launch {
            _uiState.value = _uiState.value.copy(isLoading = true)
            try {
                when (user.userRole) {
                    UserRole.ADMIN -> loadAdminStats()
                    UserRole.ORGANIZER -> loadOrganizerStats(user.uid)
                    UserRole.ATTENDEE -> loadAttendeeStats(user.uid)
                }
            } catch (_: Exception) {
                _uiState.value = _uiState.value.copy(isLoading = false)
            }
        }
    }

    private suspend fun loadAdminStats() {
        val usersSnap = firestore.collection("users").get().await()
        val eventsSnap = firestore.collection("events").get().await()
        val bookingsSnap = firestore.collection("bookings").get().await()

        val allBookings = bookingsSnap.documents.mapNotNull { it.toObject(Booking::class.java) }
        val confirmedBookings = allBookings.filter { it.status == BookingStatus.CONFIRMED.value }

        val totalTicketsSold = confirmedBookings.sumOf { it.quantity }
        val totalRevenueCents = confirmedBookings.sumOf { it.totalAmount }

        _uiState.value = DashboardUiState(
            totalUsers = usersSnap.size(),
            totalEvents = eventsSnap.size(),
            totalBookings = confirmedBookings.size,
            totalTicketsSold = totalTicketsSold,
            totalRevenueCents = totalRevenueCents,
            isLoading = false
        )
    }

    private suspend fun loadOrganizerStats(organizerId: String) {
        val eventsSnap = firestore.collection("events")
            .whereEqualTo("organizerId", organizerId)
            .get()
            .await()

        val events = eventsSnap.documents.mapNotNull { it.toObject(Event::class.java) }

        val bookingsSnap = firestore.collection("bookings").get().await()
        val allBookings = bookingsSnap.documents.mapNotNull { it.toObject(Booking::class.java) }
        val organizerEventIds = events.map { it.eventId }.toSet()

        val confirmedBookings = allBookings.filter {
            it.status == BookingStatus.CONFIRMED.value && organizerEventIds.contains(it.eventId)
        }

        val statRows = events.map { event ->
            val eventBookings = confirmedBookings.filter { it.eventId == event.eventId }
            val eventRevenue = eventBookings.sumOf { it.totalAmount }
            EventStatRow(
                eventTitle = event.title,
                bookedCount = event.bookedCount,
                capacity = event.capacity,
                revenueCents = eventRevenue
            )
        }

        _uiState.value = DashboardUiState(
            totalEvents = events.size,
            totalBookings = confirmedBookings.size,
            totalTicketsSold = confirmedBookings.sumOf { it.quantity },
            totalRevenueCents = confirmedBookings.sumOf { it.totalAmount },
            organizerEventRows = statRows,
            isLoading = false
        )
    }

    private suspend fun loadAttendeeStats(userId: String) {
        val bookingsSnap = firestore.collection("bookings")
            .whereEqualTo("userId", userId)
            .get()
            .await()

        val bookings = bookingsSnap.documents.mapNotNull { it.toObject(Booking::class.java) }
        val confirmedBookings = bookings.filter { it.status == BookingStatus.CONFIRMED.value }

        _uiState.value = DashboardUiState(
            totalBookings = confirmedBookings.size,
            totalTicketsSold = confirmedBookings.sumOf { it.quantity },
            totalRevenueCents = confirmedBookings.sumOf { it.totalAmount },
            isLoading = false
        )
    }

    companion object {
        fun provideFactory(firestore: FirebaseFirestore): ViewModelProvider.Factory =
            object : ViewModelProvider.Factory {
                @Suppress("UNCHECKED_CAST")
                override fun <T : ViewModel> create(modelClass: Class<T>): T {
                    return DashboardViewModel(firestore) as T
                }
            }
    }
}
