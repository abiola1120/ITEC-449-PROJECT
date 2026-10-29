package com.eventexperts.eventandticketmanagementsystem.ui.events

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.eventexperts.eventandticketmanagementsystem.data.model.Category
import com.eventexperts.eventandticketmanagementsystem.data.model.Event
import com.eventexperts.eventandticketmanagementsystem.data.repository.CatalogRepository
import com.eventexperts.eventandticketmanagementsystem.data.repository.EventRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.launch

data class SearchUiState(
    val query: String = "",
    val selectedCategory: Category? = null,
    val selectedCity: String? = null,
    val maxPriceDollars: Double = 200.0,
    val allEvents: List<Event> = emptyList(),
    val filteredEvents: List<Event> = emptyList(),
    val categories: List<Category> = emptyList(),
    val isLoading: Boolean = false
)

class SearchViewModel(
    private val eventRepository: EventRepository,
    private val catalogRepository: CatalogRepository
) : ViewModel() {

    private val _uiState = MutableStateFlow(SearchUiState())
    val uiState: StateFlow<SearchUiState> = _uiState.asStateFlow()

    init {
        loadData()
    }

    private fun loadData() {
        viewModelScope.launch {
            launch {
                catalogRepository.observeCategories()
                    .catch { _uiState.value = _uiState.value.copy(isLoading = false) }
                    .collect { categories ->
                        _uiState.value = _uiState.value.copy(categories = categories)
                    }
            }
            launch {
                eventRepository.observePublishedEvents()
                    .catch { _uiState.value = _uiState.value.copy(isLoading = false) }
                    .collect { events ->
                        _uiState.value = _uiState.value.copy(allEvents = events)
                        applyFilters()
                    }
            }
        }
    }

    fun onQueryChange(newQuery: String) {
        _uiState.value = _uiState.value.copy(query = newQuery)
        applyFilters()
    }

    fun onCategorySelect(category: Category?) {
        _uiState.value = _uiState.value.copy(selectedCategory = category)
        applyFilters()
    }

    fun onCitySelect(city: String?) {
        _uiState.value = _uiState.value.copy(selectedCity = city)
        applyFilters()
    }

    fun onMaxPriceChange(maxPrice: Double) {
        _uiState.value = _uiState.value.copy(maxPriceDollars = maxPrice)
        applyFilters()
    }

    private fun applyFilters() {
        val state = _uiState.value
        val queryLower = state.query.trim().lowercase()

        val filtered = state.allEvents.filter { event ->
            val matchesQuery = queryLower.isBlank() ||
                    event.title.lowercase().contains(queryLower) ||
                    event.description.lowercase().contains(queryLower) ||
                    event.venueName.lowercase().contains(queryLower)

            val matchesCategory = state.selectedCategory == null || event.categoryId == state.selectedCategory.id
            val matchesCity = state.selectedCity.isNullOrBlank() || event.city.equals(state.selectedCity, ignoreCase = true)

            val priceDollars = event.basePrice / 100.0
            val matchesPrice = priceDollars <= state.maxPriceDollars

            matchesQuery && matchesCategory && matchesCity && matchesPrice
        }

        _uiState.value = state.copy(filteredEvents = filtered)
    }

    companion object {
        fun provideFactory(
            eventRepository: EventRepository,
            catalogRepository: CatalogRepository
        ): ViewModelProvider.Factory = object : ViewModelProvider.Factory {
            @Suppress("UNCHECKED_CAST")
            override fun <T : ViewModel> create(modelClass: Class<T>): T {
                return SearchViewModel(eventRepository, catalogRepository) as T
            }
        }
    }
}
