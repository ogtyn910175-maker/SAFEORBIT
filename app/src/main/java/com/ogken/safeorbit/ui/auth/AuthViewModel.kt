package com.ogken.safeorbit.ui.auth

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.ogken.safeorbit.model.User
import com.ogken.safeorbit.repository.AuthRepository
import com.ogken.safeorbit.repository.FirebaseAuthRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

data class AuthUiState(
    val emailInput: String = "",
    val passwordInput: String = "",
    val confirmPasswordInput: String = "",
    val displayNameInput: String = "",
    val roleInput: String = "Member",
    val isLoginMode: Boolean = true,
    val emailError: String? = null,
    val passwordError: String? = null,
    val confirmPasswordError: String? = null,
    val nameError: String? = null,
    val isLoading: Boolean = false,
    val errorMessage: String? = null,
    val successMessage: String? = null,
    val isResetDialogOpen: Boolean = false,
    val resetEmailInput: String = ""
)

class AuthViewModel(
    private val authRepository: AuthRepository = FirebaseAuthRepository.instance
) : ViewModel() {

    private val _uiState = MutableStateFlow(AuthUiState())
    val uiState: StateFlow<AuthUiState> = _uiState.asStateFlow()

    val currentUser: StateFlow<User?> = authRepository.authState.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5000),
        initialValue = null
    )

    fun onEmailChanged(email: String) {
        val error = if (email.isNotBlank()) authRepository.validateEmail(email) else null
        _uiState.update { it.copy(emailInput = email, emailError = error, errorMessage = null) }
    }

    fun onPasswordChanged(password: String) {
        val error = if (password.isNotBlank()) authRepository.validatePassword(password) else null
        _uiState.update { it.copy(passwordInput = password, passwordError = error, errorMessage = null) }
    }

    fun onConfirmPasswordChanged(password: String) {
        val error = if (password != _uiState.value.passwordInput) "Passwords do not match" else null
        _uiState.update { it.copy(confirmPasswordInput = password, confirmPasswordError = error, errorMessage = null) }
    }

    fun onDisplayNameChanged(name: String) {
        _uiState.update { it.copy(displayNameInput = name, nameError = null, errorMessage = null) }
    }

    fun onRoleChanged(role: String) {
        _uiState.update { it.copy(roleInput = role) }
    }

    fun toggleMode() {
        _uiState.update {
            it.copy(
                isLoginMode = !it.isLoginMode,
                errorMessage = null,
                successMessage = null,
                emailError = null,
                passwordError = null
            )
        }
    }

    fun openResetDialog() {
        _uiState.update { it.copy(isResetDialogOpen = true, resetEmailInput = it.emailInput) }
    }

    fun dismissResetDialog() {
        _uiState.update { it.copy(isResetDialogOpen = false) }
    }

    fun onResetEmailChanged(email: String) {
        _uiState.update { it.copy(resetEmailInput = email) }
    }

    fun sendPasswordReset() {
        val email = _uiState.value.resetEmailInput.trim()
        val error = authRepository.validateEmail(email)
        if (error != null) {
            _uiState.update { it.copy(errorMessage = error) }
            return
        }

        viewModelScope.launch {
            _uiState.update { it.copy(isLoading = true, errorMessage = null) }
            val result = authRepository.sendPasswordReset(email)
            if (result.isSuccess) {
                _uiState.update {
                    it.copy(
                        isLoading = false,
                        isResetDialogOpen = false,
                        successMessage = "Password reset email sent to $email."
                    )
                }
            } else {
                _uiState.update {
                    it.copy(
                        isLoading = false,
                        errorMessage = result.exceptionOrNull()?.localizedMessage ?: "Failed to send reset email"
                    )
                }
            }
        }
    }

    fun submitAuth(onSuccess: () -> Unit) {
        val state = _uiState.value
        val emailError = authRepository.validateEmail(state.emailInput)
        val passwordError = authRepository.validatePassword(state.passwordInput)

        if (emailError != null || passwordError != null) {
            _uiState.update { it.copy(emailError = emailError, passwordError = passwordError) }
            return
        }

        if (!state.isLoginMode) {
            if (state.displayNameInput.trim().length < 2) {
                _uiState.update { it.copy(nameError = "Display name must be at least 2 characters") }
                return
            }
            if (state.passwordInput != state.confirmPasswordInput) {
                _uiState.update { it.copy(confirmPasswordError = "Passwords do not match") }
                return
            }
        }

        viewModelScope.launch {
            _uiState.update { it.copy(isLoading = true, errorMessage = null, successMessage = null) }

            val result = if (state.isLoginMode) {
                authRepository.signIn(state.emailInput, state.passwordInput)
            } else {
                authRepository.signUp(
                    email = state.emailInput,
                    password = state.passwordInput,
                    displayName = state.displayNameInput,
                    role = state.roleInput
                )
            }

            if (result.isSuccess) {
                _uiState.update { it.copy(isLoading = false, successMessage = "Welcome to SafeOrbit!") }
                onSuccess()
            } else {
                val errorMsg = result.exceptionOrNull()?.localizedMessage ?: "Authentication failed"
                _uiState.update { it.copy(isLoading = false, errorMessage = errorMsg) }
            }
        }
    }

    fun signOut(onSignedOut: () -> Unit) {
        viewModelScope.launch {
            authRepository.signOut()
            onSignedOut()
        }
    }

    fun clearMessages() {
        _uiState.update { it.copy(errorMessage = null, successMessage = null) }
    }
}
