package com.eventexperts.eventandticketmanagementsystem.ui.activitylog

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.eventexperts.eventandticketmanagementsystem.data.model.ActivityLog
import com.eventexperts.eventandticketmanagementsystem.data.repository.ActivityLogRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.launch

data class ActivityLogsUiState(
    val logs: List<ActivityLog> = emptyList(),
    val filteredLogs: List<ActivityLog> = emptyList(),
    val searchQuery: String = "",
    val selectedActionFilter: String? = null,
    val isLoading: Boolean = false
)

class ActivityLogsViewModel(
    private val activityLogRepository: ActivityLogRepository
) : ViewModel() {

    private val _uiState = MutableStateFlow(ActivityLogsUiState())
    val uiState: StateFlow<ActivityLogsUiState> = _uiState.asStateFlow()

    init {
        observeLogs()
    }

    private fun observeLogs() {
        viewModelScope.launch {
            _uiState.value = _uiState.value.copy(isLoading = true)
            activityLogRepository.observeActivityLogs()
                .catch { _uiState.value = _uiState.value.copy(isLoading = false) }
                .collect { logs ->
                    _uiState.value = _uiState.value.copy(logs = logs, isLoading = false)
                    applyFilters()
                }
        }
    }

    fun onSearchQueryChange(query: String) {
        _uiState.value = _uiState.value.copy(searchQuery = query)
        applyFilters()
    }

    fun onActionFilterSelect(action: String?) {
        _uiState.value = _uiState.value.copy(selectedActionFilter = action)
        applyFilters()
    }

    private fun applyFilters() {
        val state = _uiState.value
        val queryLower = state.searchQuery.trim().lowercase()

        val filtered = state.logs.filter { log ->
            val matchesQuery = queryLower.isBlank() ||
                    log.userName.lowercase().contains(queryLower) ||
                    log.action.lowercase().contains(queryLower) ||
                    (log.details?.lowercase()?.contains(queryLower) == true)

            val matchesAction = state.selectedActionFilter == null || log.action.contains(state.selectedActionFilter, ignoreCase = true)

            matchesQuery && matchesAction
        }

        _uiState.value = state.copy(filteredLogs = filtered)
    }

    companion object {
        fun provideFactory(activityLogRepository: ActivityLogRepository): ViewModelProvider.Factory =
            object : ViewModelProvider.Factory {
                @Suppress("UNCHECKED_CAST")
                override fun <T : ViewModel> create(modelClass: Class<T>): T {
                    return ActivityLogsViewModel(activityLogRepository) as T
                }
            }
    }
}
