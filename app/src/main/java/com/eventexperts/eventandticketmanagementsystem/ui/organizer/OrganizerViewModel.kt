package com.eventexperts.eventandticketmanagementsystem.ui.organizer

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.eventexperts.eventandticketmanagementsystem.data.model.Category
import com.eventexperts.eventandticketmanagementsystem.data.model.Event
import com.eventexperts.eventandticketmanagementsystem.data.model.EventStatus
import com.eventexperts.eventandticketmanagementsystem.data.model.User
import com.eventexperts.eventandticketmanagementsystem.data.model.Venue
import com.eventexperts.eventandticketmanagementsystem.data.repository.CatalogRepository
import com.eventexperts.eventandticketmanagementsystem.data.repository.EventRepository
import com.eventexperts.eventandticketmanagementsystem.util.Result
import com.google.firebase.Timestamp
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharedFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.launch

sealed class OrganizerUiEvent {
    object NavigateBack : OrganizerUiEvent()
    data class ShowSnackbar(val message: String) : OrganizerUiEvent()
}

data class OrganizerDashboardUiState(
    val events: List<Event> = emptyList(),
    val isLoading: Boolean = false
)

data class EventEditUiState(
    val eventId: String? = null,
    val title: String = "",
    val description: String = "",
    val imageUrlInput: String = "",
    val videoUrlInput: String = "",
    val selectedCategory: Category? = null,
    val selectedVenue: Venue? = null,
    val capacityInput: String = "",
    val priceInput: String = "",
    val status: EventStatus = EventStatus.DRAFT,
    val categories: List<Category> = emptyList(),
    val venues: List<Venue> = emptyList(),
    val isSaving: Boolean = false,
    val errorMessage: String? = null
)

class OrganizerViewModel(
    private val eventRepository: EventRepository,
    private val catalogRepository: CatalogRepository
) : ViewModel() {

    private val _dashboardState = MutableStateFlow(OrganizerDashboardUiState())
    val dashboardState: StateFlow<OrganizerDashboardUiState> = _dashboardState.asStateFlow()

    private val _editState = MutableStateFlow(EventEditUiState())
    val editState: StateFlow<EventEditUiState> = _editState.asStateFlow()

    private val _uiEvent = MutableSharedFlow<OrganizerUiEvent>()
    val uiEvent: SharedFlow<OrganizerUiEvent> = _uiEvent.asSharedFlow()

    fun loadOrganizerEvents(organizerId: String) {
        viewModelScope.launch {
            _dashboardState.value = _dashboardState.value.copy(isLoading = true)
            eventRepository.observeOrganizerEvents(organizerId)
                .catch { _dashboardState.value = _dashboardState.value.copy(isLoading = false) }
                .collect { events ->
                    _dashboardState.value = OrganizerDashboardUiState(events = events, isLoading = false)
                }
        }
    }

    fun loadEventForEdit(eventId: String?) {
        viewModelScope.launch {
            // Observe catalog options first
            launch {
                catalogRepository.observeCategories()
                    .catch { }
                    .collect { categories ->
                        _editState.value = _editState.value.copy(categories = categories)
                    }
            }
            launch {
                catalogRepository.observeVenues()
                    .catch { }
                    .collect { venues ->
                        _editState.value = _editState.value.copy(venues = venues)
                    }
            }

            if (!eventId.isNullOrBlank()) {
                try {
                    val event = eventRepository.getEventById(eventId)
                    if (event != null) {
                        _editState.value = _editState.value.copy(
                            eventId = event.eventId,
                            title = event.title,
                            description = event.description,
                            imageUrlInput = event.imageUrls.firstOrNull() ?: "",
                            videoUrlInput = event.videoUrl,
                            capacityInput = event.capacity.toString(),
                            priceInput = (event.basePrice / 100.0).toString(),
                            status = event.eventStatus
                        )
                    }
                } catch (e: Exception) {
                    // handle error
                }
            }
        }
    }

    fun onTitleChange(input: String) {
        _editState.value = _editState.value.copy(title = input, errorMessage = null)
    }

    fun onDescriptionChange(input: String) {
        _editState.value = _editState.value.copy(description = input, errorMessage = null)
    }

    fun onImageUrlChange(input: String) {
        _editState.value = _editState.value.copy(imageUrlInput = input, errorMessage = null)
    }

    fun onVideoUrlChange(input: String) {
        _editState.value = _editState.value.copy(videoUrlInput = input, errorMessage = null)
    }

    fun onCategorySelect(category: Category) {
        _editState.value = _editState.value.copy(selectedCategory = category, errorMessage = null)
    }

    fun onVenueSelect(venue: Venue) {
        val currentCapacity = _editState.value.capacityInput
        val updatedCapacity = if (currentCapacity.isBlank()) venue.capacity.toString() else currentCapacity
        _editState.value = _editState.value.copy(selectedVenue = venue, capacityInput = updatedCapacity, errorMessage = null)
    }

    fun onCapacityChange(input: String) {
        _editState.value = _editState.value.copy(capacityInput = input, errorMessage = null)
    }

    fun onPriceChange(input: String) {
        _editState.value = _editState.value.copy(priceInput = input, errorMessage = null)
    }

    fun onStatusChange(status: EventStatus) {
        _editState.value = _editState.value.copy(status = status, errorMessage = null)
    }

    fun saveEvent(user: User) {
        val state = _editState.value
        val category = state.selectedCategory
        val venue = state.selectedVenue

        if (category == null) {
            _editState.value = state.copy(errorMessage = "Please select a category")
            return
        }
        if (venue == null) {
            _editState.value = state.copy(errorMessage = "Please select a venue")
            return
        }

        val capacity = state.capacityInput.toIntOrNull() ?: 0
        val priceCents = ((state.priceInput.toDoubleOrNull() ?: 0.0) * 100).toInt()

        _editState.value = state.copy(isSaving = true, errorMessage = null)

        viewModelScope.launch {
            val result = eventRepository.saveEvent(
                existingEventId = state.eventId,
                title = state.title,
                description = state.description,
                categoryId = category.id,
                venueId = venue.id,
                capacity = capacity,
                basePrice = priceCents,
                startAt = Timestamp(System.currentTimeMillis() / 1000 + 86400 * 7, 0), // 7 days from now
                endAt = null,
                saleStartAt = null,
                saleEndAt = null,
                registrationDeadline = null,
                cancellationPolicyHours = 24,
                status = state.status,
                imageUrls = listOf(state.imageUrlInput).filter { it.isNotBlank() },
                videoUrl = state.videoUrlInput,
                user = user
            )

            when (result) {
                is Result.Success -> {
                    _editState.value = EventEditUiState()
                    _uiEvent.emit(OrganizerUiEvent.ShowSnackbar("Event saved successfully"))
                    _uiEvent.emit(OrganizerUiEvent.NavigateBack)
                }
                is Result.Error -> {
                    _editState.value = _editState.value.copy(isSaving = false, errorMessage = result.message)
                }
                else -> {
                    _editState.value = _editState.value.copy(isSaving = false)
                }
            }
        }
    }

    fun cancelEvent(eventId: String, user: User) {
        viewModelScope.launch {
            when (val result = eventRepository.cancelEvent(eventId, user)) {
                is Result.Success -> _uiEvent.emit(OrganizerUiEvent.ShowSnackbar("Event cancelled"))
                is Result.Error -> _uiEvent.emit(OrganizerUiEvent.ShowSnackbar(result.message))
                else -> {}
            }
        }
    }

    companion object {
        fun provideFactory(
            eventRepository: EventRepository,
            catalogRepository: CatalogRepository
        ): ViewModelProvider.Factory = object : ViewModelProvider.Factory {
            @Suppress("UNCHECKED_CAST")
            override fun <T : ViewModel> create(modelClass: Class<T>): T {
                return OrganizerViewModel(eventRepository, catalogRepository) as T
            }
        }
    }
}
