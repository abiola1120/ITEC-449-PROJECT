package com.eventexperts.eventandticketmanagementsystem.ui.events

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.eventexperts.eventandticketmanagementsystem.data.model.Event
import com.eventexperts.eventandticketmanagementsystem.data.model.TicketType
import com.eventexperts.eventandticketmanagementsystem.data.repository.EventRepository
import com.eventexperts.eventandticketmanagementsystem.data.repository.TicketTypeRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.launch

data class EventDetailsUiState(
    val event: Event? = null,
    val ticketTypes: List<TicketType> = emptyList(),
    val isLoading: Boolean = false,
    val errorMessage: String? = null
)

class EventDetailsViewModel(
    private val eventRepository: EventRepository,
    private val ticketTypeRepository: TicketTypeRepository
) : ViewModel() {

    private val _uiState = MutableStateFlow(EventDetailsUiState())
    val uiState: StateFlow<EventDetailsUiState> = _uiState.asStateFlow()

    fun loadEventDetails(eventId: String) {
        viewModelScope.launch {
            _uiState.value = _uiState.value.copy(isLoading = true)
            val event = try {
                eventRepository.getEventById(eventId)
            } catch (e: Exception) {
                null
            }
            if (event == null) {
                _uiState.value = _uiState.value.copy(isLoading = false, errorMessage = "Event not found")
                return@launch
            }

            ticketTypeRepository.observeTicketTypesForEvent(eventId)
                .catch { _uiState.value = _uiState.value.copy(isLoading = false) }
                .collect { types ->
                    _uiState.value = EventDetailsUiState(
                        event = event,
                        ticketTypes = types,
                        isLoading = false
                    )
                }
        }
    }

    companion object {
        fun provideFactory(
            eventRepository: EventRepository,
            ticketTypeRepository: TicketTypeRepository
        ): ViewModelProvider.Factory = object : ViewModelProvider.Factory {
            @Suppress("UNCHECKED_CAST")
            override fun <T : ViewModel> create(modelClass: Class<T>): T {
                return EventDetailsViewModel(eventRepository, ticketTypeRepository) as T
            }
        }
    }
}
