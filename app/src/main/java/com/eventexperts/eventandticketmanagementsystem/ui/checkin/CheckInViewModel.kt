package com.eventexperts.eventandticketmanagementsystem.ui.checkin

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.eventexperts.eventandticketmanagementsystem.data.model.Event
import com.eventexperts.eventandticketmanagementsystem.data.model.Ticket
import com.eventexperts.eventandticketmanagementsystem.data.model.User
import com.eventexperts.eventandticketmanagementsystem.data.repository.CheckInRepository
import com.eventexperts.eventandticketmanagementsystem.data.repository.CheckInResult
import com.eventexperts.eventandticketmanagementsystem.data.repository.EventRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.launch

data class CheckInUiState(
    val event: Event? = null,
    val manualInput: String = "",
    val checkInResult: CheckInResult? = null,
    val tickets: List<Ticket> = emptyList(),
    val filteredTickets: List<Ticket> = emptyList(),
    val searchQuery: String = "",
    val statusFilter: String = "all", // "all", "checked_in", "not_checked_in"
    val isSubmitting: Boolean = false,
    val isLoading: Boolean = false
) {
    val totalTickets: Int get() = tickets.size
    val checkedInCount: Int get() = tickets.count { it.status == "used" }
}

class CheckInViewModel(
    private val checkInRepository: CheckInRepository,
    private val eventRepository: EventRepository
) : ViewModel() {

    private val _uiState = MutableStateFlow(CheckInUiState())
    val uiState: StateFlow<CheckInUiState> = _uiState.asStateFlow()

    fun loadEventData(eventId: String) {
        viewModelScope.launch {
            _uiState.value = _uiState.value.copy(isLoading = true)
            val event = try {
                eventRepository.getEventById(eventId)
            } catch (e: Exception) {
                null
            }
            _uiState.value = _uiState.value.copy(event = event)

            checkInRepository.observeEventTickets(eventId)
                .catch { _uiState.value = _uiState.value.copy(isLoading = false) }
                .collect { tickets ->
                    _uiState.value = _uiState.value.copy(tickets = tickets, isLoading = false)
                    applyFilters()
                }
        }
    }

    fun onManualInputChange(input: String) {
        _uiState.value = _uiState.value.copy(manualInput = input, checkInResult = null)
    }

    fun submitCheckIn(user: User) {
        val state = _uiState.value
        val eventId = state.event?.eventId ?: return
        val ref = state.manualInput.trim()

        if (ref.isBlank()) return

        _uiState.value = state.copy(isSubmitting = true, checkInResult = null)

        viewModelScope.launch {
            val result = checkInRepository.checkInTicket(ref, eventId, user)
            _uiState.value = _uiState.value.copy(
                isSubmitting = false,
                checkInResult = result,
                manualInput = if (result is CheckInResult.Success) "" else state.manualInput
            )
        }
    }

    fun onSearchQueryChange(query: String) {
        _uiState.value = _uiState.value.copy(searchQuery = query)
        applyFilters()
    }

    fun onStatusFilterChange(filter: String) {
        _uiState.value = _uiState.value.copy(statusFilter = filter)
        applyFilters()
    }

    private fun applyFilters() {
        val state = _uiState.value
        val queryLower = state.searchQuery.trim().lowercase()

        val filtered = state.tickets.filter { ticket ->
            val matchesQuery = queryLower.isBlank() ||
                    ticket.attendeeName.lowercase().contains(queryLower) ||
                    ticket.ticketRef.lowercase().contains(queryLower)

            val matchesStatus = when (state.statusFilter) {
                "checked_in" -> ticket.status == "used"
                "not_checked_in" -> ticket.status == "valid"
                else -> true
            }

            matchesQuery && matchesStatus
        }

        _uiState.value = state.copy(filteredTickets = filtered)
    }

    companion object {
        fun provideFactory(
            checkInRepository: CheckInRepository,
            eventRepository: EventRepository
        ): ViewModelProvider.Factory = object : ViewModelProvider.Factory {
            @Suppress("UNCHECKED_CAST")
            override fun <T : ViewModel> create(modelClass: Class<T>): T {
                return CheckInViewModel(checkInRepository, eventRepository) as T
            }
        }
    }
}
