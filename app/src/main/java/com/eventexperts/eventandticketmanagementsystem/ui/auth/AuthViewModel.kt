package com.eventexperts.eventandticketmanagementsystem.ui.auth

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.eventexperts.eventandticketmanagementsystem.data.model.User
import com.eventexperts.eventandticketmanagementsystem.data.model.UserRole
import com.eventexperts.eventandticketmanagementsystem.data.repository.AuthRepository
import com.eventexperts.eventandticketmanagementsystem.util.Result
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharedFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

sealed class AuthUiEvent {
    object NavigateHome : AuthUiEvent()
    object NavigateSuspended : AuthUiEvent()
    data class ShowSnackbar(val message: String) : AuthUiEvent()
}

data class LoginUiState(
    val emailOrUsernameInput: String = "",
    val passwordInput: String = "",
    val isLoading: Boolean = false,
    val errorMessage: String? = null
)

data class RegisterUiState(
    val nameInput: String = "",
    val usernameInput: String = "",
    val emailInput: String = "",
    val passwordInput: String = "",
    val selectedRole: UserRole = UserRole.ATTENDEE,
    val isLoading: Boolean = false,
    val errorMessage: String? = null
)

data class ResetPasswordUiState(
    val emailInput: String = "",
    val isLoading: Boolean = false,
    val isSuccess: Boolean = false,
    val errorMessage: String? = null
)

class AuthViewModel(
    private val authRepository: AuthRepository
) : ViewModel() {

    val currentUser: StateFlow<User?> = authRepository.currentUser
    val isSessionInitialized: StateFlow<Boolean> = authRepository.isSessionInitialized

    private val _loginUiState = MutableStateFlow(LoginUiState())
    val loginUiState: StateFlow<LoginUiState> = _loginUiState.asStateFlow()

    private val _registerUiState = MutableStateFlow(RegisterUiState())
    val registerUiState: StateFlow<RegisterUiState> = _registerUiState.asStateFlow()

    private val _resetUiState = MutableStateFlow(ResetPasswordUiState())
    val resetUiState: StateFlow<ResetPasswordUiState> = _resetUiState.asStateFlow()

    private val _uiEvent = MutableSharedFlow<AuthUiEvent>()
    val uiEvent: SharedFlow<AuthUiEvent> = _uiEvent.asSharedFlow()

    init {
        viewModelScope.launch {
            authRepository.initializeSession()
        }
    }

    fun onLoginEmailOrUsernameChange(input: String) {
        _loginUiState.value = _loginUiState.value.copy(emailOrUsernameInput = input, errorMessage = null)
    }

    fun onLoginPasswordChange(input: String) {
        _loginUiState.value = _loginUiState.value.copy(passwordInput = input, errorMessage = null)
    }

    fun login() {
        val state = _loginUiState.value
        _loginUiState.value = state.copy(isLoading = true, errorMessage = null)

        viewModelScope.launch {
            when (val result = authRepository.login(state.emailOrUsernameInput, state.passwordInput)) {
                is Result.Success -> {
                    _loginUiState.value = LoginUiState()
                    val user = result.data
                    if (user.isSuspended) {
                        _uiEvent.emit(AuthUiEvent.NavigateSuspended)
                    } else {
                        _uiEvent.emit(AuthUiEvent.NavigateHome)
                    }
                }
                is Result.Error -> {
                    _loginUiState.value = _loginUiState.value.copy(isLoading = false, errorMessage = result.message)
                }
                else -> {
                    _loginUiState.value = _loginUiState.value.copy(isLoading = false)
                }
            }
        }
    }

    fun onRegisterNameChange(input: String) {
        _registerUiState.value = _registerUiState.value.copy(nameInput = input, errorMessage = null)
    }

    fun onRegisterUsernameChange(input: String) {
        _registerUiState.value = _registerUiState.value.copy(usernameInput = input, errorMessage = null)
    }

    fun onRegisterEmailChange(input: String) {
        _registerUiState.value = _registerUiState.value.copy(emailInput = input, errorMessage = null)
    }

    fun onRegisterPasswordChange(input: String) {
        _registerUiState.value = _registerUiState.value.copy(passwordInput = input, errorMessage = null)
    }

    fun onRegisterRoleChange(role: UserRole) {
        _registerUiState.value = _registerUiState.value.copy(selectedRole = role, errorMessage = null)
    }

    fun register() {
        val state = _registerUiState.value
        _registerUiState.value = state.copy(isLoading = true, errorMessage = null)

        viewModelScope.launch {
            val result = authRepository.register(
                name = state.nameInput,
                username = state.usernameInput,
                email = state.emailInput,
                password = state.passwordInput,
                role = state.selectedRole
            )
            when (result) {
                is Result.Success -> {
                    _registerUiState.value = RegisterUiState()
                    _uiEvent.emit(AuthUiEvent.NavigateHome)
                }
                is Result.Error -> {
                    _registerUiState.value = _registerUiState.value.copy(isLoading = false, errorMessage = result.message)
                }
                else -> {
                    _registerUiState.value = _registerUiState.value.copy(isLoading = false)
                }
            }
        }
    }

    fun onResetEmailChange(input: String) {
        _resetUiState.value = _resetUiState.value.copy(emailInput = input, errorMessage = null, isSuccess = false)
    }

    fun resetPassword() {
        val state = _resetUiState.value
        _resetUiState.value = state.copy(isLoading = true, errorMessage = null)

        viewModelScope.launch {
            when (val result = authRepository.resetPassword(state.emailInput)) {
                is Result.Success -> {
                    _resetUiState.value = _resetUiState.value.copy(isLoading = false, isSuccess = true)
                    _uiEvent.emit(AuthUiEvent.ShowSnackbar("Password reset link sent to your email"))
                }
                is Result.Error -> {
                    _resetUiState.value = _resetUiState.value.copy(isLoading = false, errorMessage = result.message)
                }
                else -> {
                    _resetUiState.value = _resetUiState.value.copy(isLoading = false)
                }
            }
        }
    }

    fun logout() {
        viewModelScope.launch {
            authRepository.logout()
        }
    }

    companion object {
        fun provideFactory(authRepository: AuthRepository): ViewModelProvider.Factory =
            object : ViewModelProvider.Factory {
                @Suppress("UNCHECKED_CAST")
                override fun <T : ViewModel> create(modelClass: Class<T>): T {
                    return AuthViewModel(authRepository) as T
                }
            }
    }
}
