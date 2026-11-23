package com.ogken.safeorbit.ui.profile

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.ogken.safeorbit.model.User
import com.ogken.safeorbit.repository.InMemoryUserRepository
import com.ogken.safeorbit.repository.UserRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.launchIn
import kotlinx.coroutines.flow.onEach
import kotlinx.coroutines.flow.update

data class ProfileUiState(
    val user: User = User(),
    val nameInput: String = "Tanmay",
    val roleInput: String = "Circle Creator",
    val emergencyPhoneInput: String = "+91 98765 43210",
    val nameError: String? = null,
    val isSavedSuccess: Boolean = false,
    val feedbackMessage: String? = null
)

class ProfileViewModel(
    private val userRepository: UserRepository = InMemoryUserRepository.instance
) : ViewModel() {

    private val _uiState = MutableStateFlow(ProfileUiState())
    val uiState: StateFlow<ProfileUiState> = _uiState.asStateFlow()

    init {
        userRepository.getUser().onEach { user ->
            _uiState.update { current ->
                current.copy(
                    user = user,
                    nameInput = user.name,
                    roleInput = user.role,
                    emergencyPhoneInput = user.emergencyPhone
                )
            }
        }.launchIn(viewModelScope)
    }

    fun onNameChanged(name: String) {
        val error = userRepository.validateDisplayName(name)
        _uiState.update {
            it.copy(
                nameInput = name,
                nameError = if (name.isNotBlank()) error else null,
                isSavedSuccess = false,
                feedbackMessage = null
            )
        }
    }

    fun onRoleChanged(role: String) {
        _uiState.update { it.copy(roleInput = role, isSavedSuccess = false) }
    }

    fun onEmergencyPhoneChanged(phone: String) {
        _uiState.update { it.copy(emergencyPhoneInput = phone, isSavedSuccess = false) }
    }

    fun saveProfile(): Boolean {
        val nameValidation = userRepository.validateDisplayName(_uiState.value.nameInput)
        if (nameValidation != null) {
            _uiState.update { it.copy(nameError = nameValidation) }
            return false
        }

        val result = userRepository.updateProfile(
            name = _uiState.value.nameInput,
            role = _uiState.value.roleInput.ifBlank { "Member" },
            emergencyPhone = _uiState.value.emergencyPhoneInput
        )

        return if (result.isSuccess) {
            _uiState.update {
                it.copy(
                    nameError = null,
                    isSavedSuccess = true,
                    feedbackMessage = "Profile updated successfully!"
                )
            }
            true
        } else {
            _uiState.update {
                it.copy(nameError = result.exceptionOrNull()?.message ?: "Failed to save profile")
            }
            false
        }
    }

    fun clearFeedback() {
        _uiState.update { it.copy(feedbackMessage = null, isSavedSuccess = false) }
    }
}
