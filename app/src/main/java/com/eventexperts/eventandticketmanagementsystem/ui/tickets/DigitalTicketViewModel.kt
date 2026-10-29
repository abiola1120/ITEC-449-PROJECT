package com.eventexperts.eventandticketmanagementsystem.ui.tickets

import android.graphics.Bitmap
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.eventexperts.eventandticketmanagementsystem.data.model.Booking
import com.eventexperts.eventandticketmanagementsystem.data.model.Event
import com.eventexperts.eventandticketmanagementsystem.data.model.Ticket
import com.eventexperts.eventandticketmanagementsystem.data.model.User
import com.eventexperts.eventandticketmanagementsystem.data.repository.BookingRepository
import com.eventexperts.eventandticketmanagementsystem.data.repository.EventRepository
import com.eventexperts.eventandticketmanagementsystem.util.QrCodeGenerator
import com.eventexperts.eventandticketmanagementsystem.util.Result
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharedFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.launch

sealed class DigitalTicketUiEvent {
    data class ShowSnackbar(val message: String) : DigitalTicketUiEvent()
    object BookingCancelled : DigitalTicketUiEvent()
}

data class TicketWithQr(
    val ticket: Ticket,
    val qrBitmap: Bitmap?
)

data class DigitalTicketUiState(
    val booking: Booking? = null,
    val event: Event? = null,
    val tickets: List<TicketWithQr> = emptyList(),
    val isLoading: Boolean = false
)

class DigitalTicketViewModel(
    private val bookingRepository: BookingRepository,
    private val eventRepository: EventRepository
) : ViewModel() {

    private val _uiState = MutableStateFlow(DigitalTicketUiState())
    val uiState: StateFlow<DigitalTicketUiState> = _uiState.asStateFlow()

    private val _uiEvent = MutableSharedFlow<DigitalTicketUiEvent>()
    val uiEvent: SharedFlow<DigitalTicketUiEvent> = _uiEvent.asSharedFlow()

    fun loadTicketDetails(bookingId: String) {
        viewModelScope.launch {
            _uiState.value = _uiState.value.copy(isLoading = true)
            val booking = try {
                bookingRepository.getBookingById(bookingId)
            } catch (e: Exception) {
                null
            }
            if (booking == null) {
                _uiState.value = _uiState.value.copy(isLoading = false)
                return@launch
            }

            val event = try {
                eventRepository.getEventById(booking.eventId)
            } catch (e: Exception) {
                null
            }
            _uiState.value = _uiState.value.copy(booking = booking, event = event)

            bookingRepository.observeBookingTickets(bookingId)
                .catch { _uiState.value = _uiState.value.copy(isLoading = false) }
                .collect { rawTickets ->
                    val ticketsWithQr = rawTickets.map { ticket ->
                        TicketWithQr(
                        ticket = ticket,
                        qrBitmap = QrCodeGenerator.generateQrCodeBitmap(ticket.ticketRef)
                    )
                }
                _uiState.value = _uiState.value.copy(tickets = ticketsWithQr, isLoading = false)
            }
        }
    }

    fun cancelBooking(bookingId: String, user: User) {
        viewModelScope.launch {
            when (val result = bookingRepository.cancelBooking(bookingId, user)) {
                is Result.Success -> {
                    _uiEvent.emit(DigitalTicketUiEvent.ShowSnackbar("Booking cancelled and seats released"))
                    _uiEvent.emit(DigitalTicketUiEvent.BookingCancelled)
                }
                is Result.Error -> {
                    _uiEvent.emit(DigitalTicketUiEvent.ShowSnackbar(result.message))
                }
                else -> {}
            }
        }
    }

    companion object {
        fun provideFactory(
            bookingRepository: BookingRepository,
            eventRepository: EventRepository
        ): ViewModelProvider.Factory = object : ViewModelProvider.Factory {
            @Suppress("UNCHECKED_CAST")
            override fun <T : ViewModel> create(modelClass: Class<T>): T {
                return DigitalTicketViewModel(bookingRepository, eventRepository) as T
            }
        }
    }
}
