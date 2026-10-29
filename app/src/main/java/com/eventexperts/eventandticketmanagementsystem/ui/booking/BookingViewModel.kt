package com.eventexperts.eventandticketmanagementsystem.ui.booking

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.eventexperts.eventandticketmanagementsystem.data.model.Booking
import com.eventexperts.eventandticketmanagementsystem.data.model.Event
import com.eventexperts.eventandticketmanagementsystem.data.model.PromoCode
import com.eventexperts.eventandticketmanagementsystem.data.model.TicketType
import com.eventexperts.eventandticketmanagementsystem.data.model.User
import com.eventexperts.eventandticketmanagementsystem.data.repository.BookingRepository
import com.eventexperts.eventandticketmanagementsystem.data.repository.EventRepository
import com.eventexperts.eventandticketmanagementsystem.data.repository.PromoCodeRepository
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
import java.util.UUID

sealed class BookingUiEvent {
    data class BookingSuccess(val bookingId: String, val bookingRef: String) : BookingUiEvent()
    data class ShowSnackbar(val message: String) : BookingUiEvent()
}

data class BookingUiState(
    val event: Event? = null,
    val ticketTypes: List<TicketType> = emptyList(),
    val selectedTicketType: TicketType? = null,
    val quantity: Int = 1,
    val attendeeName: String = "",
    val attendeeEmail: String = "",
    val attendeePhone: String = "",
    val promoCodeInput: String = "",
    val appliedPromoCode: PromoCode? = null,
    val discountAmount: Int = 0,
    val promoError: String? = null,
    val promoSuccess: String? = null,
    val idempotencyKey: String = UUID.randomUUID().toString(),
    val isLoading: Boolean = false,
    val isSubmitting: Boolean = false,
    val completedBooking: Booking? = null,
    val errorMessage: String? = null
) {
    val subtotal: Int get() = (selectedTicketType?.price ?: 0) * quantity
    val discount: Int get() = discountAmount
    val feeAmount: Int get() = ((subtotal - discount).coerceAtLeast(0) * 0.05).toInt()
    val totalAmount: Int get() = (subtotal - discount).coerceAtLeast(0) + feeAmount
}

data class MyBookingsUiState(
    val bookings: List<Booking> = emptyList(),
    val isLoading: Boolean = false
)

class BookingViewModel(
    private val eventRepository: EventRepository,
    private val ticketTypeRepository: TicketTypeRepository,
    private val bookingRepository: BookingRepository,
    private val promoCodeRepository: PromoCodeRepository
) : ViewModel() {

    private val _bookingState = MutableStateFlow(BookingUiState())
    val bookingState: StateFlow<BookingUiState> = _bookingState.asStateFlow()

    private val _myBookingsState = MutableStateFlow(MyBookingsUiState())
    val myBookingsState: StateFlow<MyBookingsUiState> = _myBookingsState.asStateFlow()

    private val _uiEvent = MutableSharedFlow<BookingUiEvent>()
    val uiEvent: SharedFlow<BookingUiEvent> = _uiEvent.asSharedFlow()

    fun loadBookingData(eventId: String, user: User?) {
        viewModelScope.launch {
            _bookingState.value = _bookingState.value.copy(
                isLoading = true,
                attendeeName = user?.name ?: "",
                attendeeEmail = user?.email ?: ""
            )

            val event = try {
                eventRepository.getEventById(eventId)
            } catch (e: Exception) {
                null
            }
            _bookingState.value = _bookingState.value.copy(event = event)

            ticketTypeRepository.observeTicketTypesForEvent(eventId)
                .catch { _bookingState.value = _bookingState.value.copy(isLoading = false) }
                .collect { types ->
                    val activeTypes = types.filter { it.active }
                    val selected = _bookingState.value.selectedTicketType ?: activeTypes.firstOrNull()
                    _bookingState.value = _bookingState.value.copy(
                        ticketTypes = activeTypes,
                        selectedTicketType = selected,
                        isLoading = false
                    )
                }
        }
    }

    fun loadMyBookings(userId: String) {
        viewModelScope.launch {
            _myBookingsState.value = _myBookingsState.value.copy(isLoading = true)
            bookingRepository.observeUserBookings(userId)
                .catch { _myBookingsState.value = _myBookingsState.value.copy(isLoading = false) }
                .collect { bookings ->
                    _myBookingsState.value = MyBookingsUiState(
                        bookings = bookings.sortedByDescending { it.createdAt },
                        isLoading = false
                    )
                }
        }
    }

    fun onTicketTypeSelect(ticketType: TicketType) {
        _bookingState.value = _bookingState.value.copy(selectedTicketType = ticketType, errorMessage = null)
    }

    fun onQuantityChange(newQuantity: Int) {
        if (newQuantity in 1..10) {
            _bookingState.value = _bookingState.value.copy(quantity = newQuantity, errorMessage = null)
        }
    }

    fun onAttendeeNameChange(name: String) {
        _bookingState.value = _bookingState.value.copy(attendeeName = name, errorMessage = null)
    }

    fun onAttendeeEmailChange(email: String) {
        _bookingState.value = _bookingState.value.copy(attendeeEmail = email, errorMessage = null)
    }

    fun onAttendeePhoneChange(phone: String) {
        _bookingState.value = _bookingState.value.copy(attendeePhone = phone, errorMessage = null)
    }

    fun onPromoCodeChange(code: String) {
        _bookingState.value = _bookingState.value.copy(promoCodeInput = code, promoError = null, promoSuccess = null)
    }

    fun applyPromoCode() {
        val state = _bookingState.value
        val code = state.promoCodeInput
        val eventId = state.event?.eventId ?: return

        if (code.isBlank()) {
            _bookingState.value = state.copy(promoError = "Enter a promo code")
            return
        }

        viewModelScope.launch {
            when (val result = promoCodeRepository.validateAndCalculatePromo(code, eventId, state.subtotal)) {
                is Result.Success -> {
                    val (promo, discount) = result.data
                    _bookingState.value = state.copy(
                        appliedPromoCode = promo,
                        discountAmount = discount,
                        promoSuccess = "Promo applied: -$${String.format(java.util.Locale.US, "%.2f", discount / 100.0)}",
                        promoError = null
                    )
                }
                is Result.Error -> {
                    _bookingState.value = state.copy(
                        promoError = result.message,
                        promoSuccess = null
                    )
                }
                else -> {}
            }
        }
    }

    fun removePromoCode() {
        _bookingState.value = _bookingState.value.copy(
            appliedPromoCode = null,
            discountAmount = 0,
            promoCodeInput = "",
            promoSuccess = null,
            promoError = null
        )
    }

    fun submitBooking(user: User) {
        val state = _bookingState.value
        val selectedType = state.selectedTicketType

        if (selectedType == null) {
            _bookingState.value = state.copy(errorMessage = "Please select a ticket type")
            return
        }

        _bookingState.value = state.copy(isSubmitting = true, errorMessage = null)

        viewModelScope.launch {
            val result = bookingRepository.createBooking(
                eventId = state.event?.eventId ?: "",
                ticketTypeId = selectedType.typeId,
                quantity = state.quantity,
                attendeeName = state.attendeeName,
                attendeeEmail = state.attendeeEmail,
                attendeePhone = state.attendeePhone,
                idempotencyKey = state.idempotencyKey,
                user = user
            )

            when (result) {
                is Result.Success -> {
                    _bookingState.value = _bookingState.value.copy(
                        isSubmitting = false,
                        completedBooking = result.data
                    )
                    _uiEvent.emit(BookingUiEvent.BookingSuccess(result.data.bookingId, result.data.bookingRef))
                }
                is Result.Error -> {
                    _bookingState.value = _bookingState.value.copy(
                        isSubmitting = false,
                        errorMessage = result.message
                    )
                }
                else -> {
                    _bookingState.value = _bookingState.value.copy(isSubmitting = false)
                }
            }
        }
    }

    companion object {
        fun provideFactory(
            eventRepository: EventRepository,
            ticketTypeRepository: TicketTypeRepository,
            bookingRepository: BookingRepository,
            promoCodeRepository: PromoCodeRepository
        ): ViewModelProvider.Factory = object : ViewModelProvider.Factory {
            @Suppress("UNCHECKED_CAST")
            override fun <T : ViewModel> create(modelClass: Class<T>): T {
                return BookingViewModel(eventRepository, ticketTypeRepository, bookingRepository, promoCodeRepository) as T
            }
        }
    }
}
