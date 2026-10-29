package com.eventexperts.eventandticketmanagementsystem.ui.organizer

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.eventexperts.eventandticketmanagementsystem.data.model.Event
import com.eventexperts.eventandticketmanagementsystem.data.model.TicketType
import com.eventexperts.eventandticketmanagementsystem.data.repository.EventRepository
import com.eventexperts.eventandticketmanagementsystem.data.repository.TicketTypeRepository
import com.eventexperts.eventandticketmanagementsystem.util.Result
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharedFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.launch

sealed class TicketTypesUiEvent {
    data class ShowSnackbar(val message: String) : TicketTypesUiEvent()
}

data class TicketTypesUiState(
    val event: Event? = null,
    val ticketTypes: List<TicketType> = emptyList(),
    val isLoading: Boolean = false
)

class TicketTypesViewModel(
    private val eventRepository: EventRepository,
    private val ticketTypeRepository: TicketTypeRepository
) : ViewModel() {

    private val _uiState = MutableStateFlow(TicketTypesUiState())
    val uiState: StateFlow<TicketTypesUiState> = _uiState.asStateFlow()

    private val _uiEvent = MutableSharedFlow<TicketTypesUiEvent>()
    val uiEvent: SharedFlow<TicketTypesUiEvent> = _uiEvent.asSharedFlow()

    fun loadTicketTypesForEvent(eventId: String) {
        viewModelScope.launch {
            _uiState.value = _uiState.value.copy(isLoading = true)
            try {
                val event = eventRepository.getEventById(eventId)
                _uiState.value = _uiState.value.copy(event = event)
            } catch (e: Exception) {
                // handle error
            }

            ticketTypeRepository.observeTicketTypesForEvent(eventId)
                .catch { _uiState.value = _uiState.value.copy(isLoading = false) }
                .collect { types ->
                    _uiState.value = _uiState.value.copy(ticketTypes = types, isLoading = false)
                }
        }
    }

    fun addTicketType(eventId: String, name: String, priceCents: Int, quantityLimit: Int) {
        viewModelScope.launch {
            when (val result = ticketTypeRepository.saveTicketType(null, eventId, name, priceCents, quantityLimit)) {
                is Result.Success -> _uiEvent.emit(TicketTypesUiEvent.ShowSnackbar("Ticket type '${result.data.name}' created"))
                is Result.Error -> _uiEvent.emit(TicketTypesUiEvent.ShowSnackbar(result.message))
                else -> {}
            }
        }
    }

    fun toggleActive(typeId: String, currentActive: Boolean) {
        viewModelScope.launch {
            when (val result = ticketTypeRepository.toggleTicketTypeActive(typeId, !currentActive)) {
                is Result.Success -> _uiEvent.emit(TicketTypesUiEvent.ShowSnackbar("Ticket type status updated"))
                is Result.Error -> _uiEvent.emit(TicketTypesUiEvent.ShowSnackbar(result.message))
                else -> {}
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
                return TicketTypesViewModel(eventRepository, ticketTypeRepository) as T
            }
        }
    }
}
