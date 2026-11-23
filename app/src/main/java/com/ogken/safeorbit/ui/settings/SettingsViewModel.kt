package com.ogken.safeorbit.ui.settings

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.ogken.safeorbit.model.LocationSharingState
import com.ogken.safeorbit.model.User
import com.ogken.safeorbit.repository.AuthRepository
import com.ogken.safeorbit.repository.FirebaseAuthRepository
import com.ogken.safeorbit.repository.FirebaseUserRepository
import com.ogken.safeorbit.repository.InMemoryLocationSharingRepository
import com.ogken.safeorbit.repository.LocationSharingRepository
import com.ogken.safeorbit.repository.UserRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.launchIn
import kotlinx.coroutines.flow.onEach
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

data class SettingsUiState(
    val user: User = User(),
    val sharingState: LocationSharingState = LocationSharingState(),
    val isConsentDialogOpen: Boolean = false,
    val feedbackMessage: String? = null
)

class SettingsViewModel(
    private val userRepository: UserRepository = FirebaseUserRepository.instance,
    private val authRepository: AuthRepository = FirebaseAuthRepository.instance,
    private val locationSharingRepository: LocationSharingRepository = InMemoryLocationSharingRepository.instance
) : ViewModel() {

    private val _uiState = MutableStateFlow(SettingsUiState())
    val uiState: StateFlow<SettingsUiState> = _uiState.asStateFlow()

    init {
        userRepository.getUser().onEach { user ->
            _uiState.update { it.copy(user = user) }
        }.launchIn(viewModelScope)

        locationSharingRepository.getLocationSharingState().onEach { state ->
            _uiState.update { it.copy(sharingState = state) }
        }.launchIn(viewModelScope)
    }

    fun requestEnableSharing() {
        // Open consent modal; never enable silently
        _uiState.update { it.copy(isConsentDialogOpen = true) }
    }

    fun confirmConsentAndEnableSharing() {
        locationSharingRepository.toggleSharingWithConsent(consentAcknowledged = true, enable = true)
        _uiState.update {
            it.copy(
                isConsentDialogOpen = false,
                feedbackMessage = "Location sharing simulation enabled. In Stage 4, coordinates will be sent only with active consent."
            )
        }
    }

    fun disableSharing() {
        locationSharingRepository.toggleSharingWithConsent(consentAcknowledged = true, enable = false)
        _uiState.update {
            it.copy(
                isConsentDialogOpen = false,
                feedbackMessage = "Location sharing is completely OFF. No GPS tracking active."
            )
        }
    }

    fun dismissConsentDialog() {
        _uiState.update { it.copy(isConsentDialogOpen = false) }
    }

    fun clearFeedback() {
        _uiState.update { it.copy(feedbackMessage = null) }
    }

    fun signOut(onSignedOut: () -> Unit) {
        viewModelScope.launch {
            authRepository.signOut()
            onSignedOut()
        }
    }
}
