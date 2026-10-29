package com.eventexperts.eventandticketmanagementsystem.ui.home

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.eventexperts.eventandticketmanagementsystem.data.model.Event
import com.eventexperts.eventandticketmanagementsystem.data.repository.EventRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.launch

data class HomeUiState(
    val publishedEvents: List<Event> = emptyList(),
    val isLoading: Boolean = false
)

class HomeViewModel(
    private val eventRepository: EventRepository
) : ViewModel() {

    private val _uiState = MutableStateFlow(HomeUiState())
    val uiState: StateFlow<HomeUiState> = _uiState.asStateFlow()

    init {
        observeEvents()
    }

    private fun observeEvents() {
        viewModelScope.launch {
            _uiState.value = _uiState.value.copy(isLoading = true)
            eventRepository.observePublishedEvents()
                .catch { _uiState.value = _uiState.value.copy(isLoading = false) }
                .collect { events ->
                    _uiState.value = HomeUiState(publishedEvents = events, isLoading = false)
                }
        }
    }

    companion object {
        fun provideFactory(eventRepository: EventRepository): ViewModelProvider.Factory =
            object : ViewModelProvider.Factory {
                @Suppress("UNCHECKED_CAST")
                override fun <T : ViewModel> create(modelClass: Class<T>): T {
                    return HomeViewModel(eventRepository) as T
                }
            }
    }
}
