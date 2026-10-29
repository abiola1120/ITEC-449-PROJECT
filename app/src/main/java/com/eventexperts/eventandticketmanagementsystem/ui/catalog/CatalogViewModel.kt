package com.eventexperts.eventandticketmanagementsystem.ui.catalog

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.eventexperts.eventandticketmanagementsystem.data.model.Category
import com.eventexperts.eventandticketmanagementsystem.data.model.User
import com.eventexperts.eventandticketmanagementsystem.data.model.Venue
import com.eventexperts.eventandticketmanagementsystem.data.repository.CatalogRepository
import com.eventexperts.eventandticketmanagementsystem.util.Result
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharedFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.launch

sealed class CatalogUiEvent {
    data class ShowSnackbar(val message: String) : CatalogUiEvent()
}

data class CatalogUiState(
    val categories: List<Category> = emptyList(),
    val venues: List<Venue> = emptyList(),
    val isLoading: Boolean = false
)

class CatalogViewModel(
    private val catalogRepository: CatalogRepository
) : ViewModel() {

    private val _uiState = MutableStateFlow(CatalogUiState())
    val uiState: StateFlow<CatalogUiState> = _uiState.asStateFlow()

    private val _uiEvent = MutableSharedFlow<CatalogUiEvent>()
    val uiEvent: SharedFlow<CatalogUiEvent> = _uiEvent.asSharedFlow()

    init {
        observeCatalogData()
    }

    private fun observeCatalogData() {
        viewModelScope.launch {
            catalogRepository.observeCategories()
                .catch { _uiState.value = _uiState.value.copy(isLoading = false) }
                .collect { categories ->
                    _uiState.value = _uiState.value.copy(categories = categories)
                }
        }
        viewModelScope.launch {
            catalogRepository.observeVenues()
                .catch { _uiState.value = _uiState.value.copy(isLoading = false) }
                .collect { venues ->
                _uiState.value = _uiState.value.copy(venues = venues)
            }
        }
    }

    fun addCategory(name: String, user: User?) {
        viewModelScope.launch {
            when (val result = catalogRepository.addCategory(name, user)) {
                is Result.Success -> _uiEvent.emit(CatalogUiEvent.ShowSnackbar("Category '${result.data.name}' added"))
                is Result.Error -> _uiEvent.emit(CatalogUiEvent.ShowSnackbar(result.message))
                else -> {}
            }
        }
    }

    fun editCategory(id: String, newName: String, user: User?) {
        viewModelScope.launch {
            when (val result = catalogRepository.editCategory(id, newName, user)) {
                is Result.Success -> _uiEvent.emit(CatalogUiEvent.ShowSnackbar("Category updated"))
                is Result.Error -> _uiEvent.emit(CatalogUiEvent.ShowSnackbar(result.message))
                else -> {}
            }
        }
    }

    fun deleteCategory(id: String, user: User?) {
        viewModelScope.launch {
            when (val result = catalogRepository.deleteCategory(id, user)) {
                is Result.Success -> _uiEvent.emit(CatalogUiEvent.ShowSnackbar("Category deleted"))
                is Result.Error -> _uiEvent.emit(CatalogUiEvent.ShowSnackbar(result.message))
                else -> {}
            }
        }
    }

    fun addVenue(name: String, address: String, city: String, capacity: Int, user: User?) {
        viewModelScope.launch {
            when (val result = catalogRepository.addVenue(name, address, city, capacity, null, null, user)) {
                is Result.Success -> _uiEvent.emit(CatalogUiEvent.ShowSnackbar("Venue '${result.data.name}' added"))
                is Result.Error -> _uiEvent.emit(CatalogUiEvent.ShowSnackbar(result.message))
                else -> {}
            }
        }
    }

    fun deleteVenue(id: String, user: User?) {
        viewModelScope.launch {
            when (val result = catalogRepository.deleteVenue(id, user)) {
                is Result.Success -> _uiEvent.emit(CatalogUiEvent.ShowSnackbar("Venue deleted"))
                is Result.Error -> _uiEvent.emit(CatalogUiEvent.ShowSnackbar(result.message))
                else -> {}
            }
        }
    }

    companion object {
        fun provideFactory(catalogRepository: CatalogRepository): ViewModelProvider.Factory =
            object : ViewModelProvider.Factory {
                @Suppress("UNCHECKED_CAST")
                override fun <T : ViewModel> create(modelClass: Class<T>): T {
                    return CatalogViewModel(catalogRepository) as T
                }
            }
    }
}
