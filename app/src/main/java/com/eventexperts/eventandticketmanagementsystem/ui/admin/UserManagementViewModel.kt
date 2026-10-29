package com.eventexperts.eventandticketmanagementsystem.ui.admin

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.eventexperts.eventandticketmanagementsystem.data.model.User
import com.eventexperts.eventandticketmanagementsystem.data.model.UserRole
import com.eventexperts.eventandticketmanagementsystem.data.model.UserStatus
import com.eventexperts.eventandticketmanagementsystem.data.repository.UserRepository
import com.eventexperts.eventandticketmanagementsystem.util.Result
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharedFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.launch

sealed class UserManagementUiEvent {
    data class ShowSnackbar(val message: String) : UserManagementUiEvent()
}

data class UserManagementUiState(
    val users: List<User> = emptyList(),
    val filteredUsers: List<User> = emptyList(),
    val selectedUser: User? = null,
    val searchQuery: String = "",
    val selectedRoleFilter: UserRole? = null,
    val selectedStatusFilter: UserStatus? = null,
    val isLoading: Boolean = false
)

class UserManagementViewModel(
    private val userRepository: UserRepository
) : ViewModel() {

    private val _uiState = MutableStateFlow(UserManagementUiState())
    val uiState: StateFlow<UserManagementUiState> = _uiState.asStateFlow()

    private val _uiEvent = MutableSharedFlow<UserManagementUiEvent>()
    val uiEvent: SharedFlow<UserManagementUiEvent> = _uiEvent.asSharedFlow()

    init {
        observeUsers()
    }

    private fun observeUsers() {
        viewModelScope.launch {
            _uiState.value = _uiState.value.copy(isLoading = true)
            userRepository.observeAllUsers()
                .catch { _uiState.value = _uiState.value.copy(isLoading = false) }
                .collect { users ->
                    _uiState.value = _uiState.value.copy(users = users, isLoading = false)
                    applyFilters()
                }
        }
    }

    fun loadUserDetails(uid: String) {
        viewModelScope.launch {
            try {
                val user = userRepository.getUserById(uid)
                _uiState.value = _uiState.value.copy(selectedUser = user)
            } catch (e: Exception) {
                // handle error
            }
        }
    }

    fun onSearchQueryChange(query: String) {
        _uiState.value = _uiState.value.copy(searchQuery = query)
        applyFilters()
    }

    fun onRoleFilterSelect(role: UserRole?) {
        _uiState.value = _uiState.value.copy(selectedRoleFilter = role)
        applyFilters()
    }

    fun onStatusFilterSelect(status: UserStatus?) {
        _uiState.value = _uiState.value.copy(selectedStatusFilter = status)
        applyFilters()
    }

    fun updateUserRole(targetUid: String, newRole: UserRole, adminUser: User) {
        viewModelScope.launch {
            when (val result = userRepository.updateUserRole(targetUid, newRole, adminUser)) {
                is Result.Success -> {
                    _uiEvent.emit(UserManagementUiEvent.ShowSnackbar("User role updated to ${newRole.value.uppercase()}"))
                    loadUserDetails(targetUid)
                }
                is Result.Error -> {
                    _uiEvent.emit(UserManagementUiEvent.ShowSnackbar(result.message))
                }
                else -> {}
            }
        }
    }

    fun updateUserStatus(targetUid: String, newStatus: UserStatus, adminUser: User) {
        viewModelScope.launch {
            when (val result = userRepository.updateUserStatus(targetUid, newStatus, adminUser)) {
                is Result.Success -> {
                    _uiEvent.emit(UserManagementUiEvent.ShowSnackbar("User status updated to ${newStatus.value.uppercase()}"))
                    loadUserDetails(targetUid)
                }
                is Result.Error -> {
                    _uiEvent.emit(UserManagementUiEvent.ShowSnackbar(result.message))
                }
                else -> {}
            }
        }
    }

    private fun applyFilters() {
        val state = _uiState.value
        val queryLower = state.searchQuery.trim().lowercase()

        val filtered = state.users.filter { user ->
            val matchesQuery = queryLower.isBlank() ||
                    user.name.lowercase().contains(queryLower) ||
                    user.email.lowercase().contains(queryLower) ||
                    user.username.lowercase().contains(queryLower)

            val matchesRole = state.selectedRoleFilter == null || user.role == state.selectedRoleFilter.value
            val matchesStatus = state.selectedStatusFilter == null || user.status == state.selectedStatusFilter.value

            matchesQuery && matchesRole && matchesStatus
        }

        _uiState.value = state.copy(filteredUsers = filtered)
    }

    companion object {
        fun provideFactory(userRepository: UserRepository): ViewModelProvider.Factory =
            object : ViewModelProvider.Factory {
                @Suppress("UNCHECKED_CAST")
                override fun <T : ViewModel> create(modelClass: Class<T>): T {
                    return UserManagementViewModel(userRepository) as T
                }
            }
    }
}
