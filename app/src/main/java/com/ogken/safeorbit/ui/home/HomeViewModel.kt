package com.ogken.safeorbit.ui.home

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.ogken.safeorbit.domain.GetOrbitCircleUseCase
import com.ogken.safeorbit.model.CircleMember
import com.ogken.safeorbit.model.LocationSharingState
import com.ogken.safeorbit.model.User
import com.ogken.safeorbit.repository.FamilyCircleRepository
import com.ogken.safeorbit.repository.FirebaseFamilyCircleRepository
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

data class HomeUiState(
    val user: User = User(),
    val circleName: String = "SafeOrbit Core Circle",
    val inviteCode: String = "SO-ORBIT-729",
    val members: List<CircleMember> = emptyList(),
    val sharingState: LocationSharingState = LocationSharingState(),
    val isConsentDialogOpen: Boolean = false,
    val isSettingsOpen: Boolean = false
)

class HomeViewModel(
    private val userRepository: UserRepository = FirebaseUserRepository.instance,
    private val familyCircleRepository: FamilyCircleRepository = FirebaseFamilyCircleRepository.instance,
    private val locationSharingRepository: LocationSharingRepository = InMemoryLocationSharingRepository.instance,
    private val getOrbitCircleUseCase: GetOrbitCircleUseCase = GetOrbitCircleUseCase(familyCircleRepository)
) : ViewModel() {

    private val _uiState = MutableStateFlow(HomeUiState())
    val uiState: StateFlow<HomeUiState> = _uiState.asStateFlow()

    init {
        loadData()
    }

    private fun loadData() {
        userRepository.getUser().onEach { user ->
            _uiState.update { it.copy(user = user) }
        }.launchIn(viewModelScope)

        familyCircleRepository.getCircle().onEach { circle ->
            _uiState.update {
                it.copy(
                    circleName = circle.name,
                    inviteCode = circle.inviteCode,
                    members = circle.members
                )
            }
        }.launchIn(viewModelScope)

        locationSharingRepository.getLocationSharingState().onEach { sharingState ->
            _uiState.update { it.copy(sharingState = sharingState) }
        }.launchIn(viewModelScope)
    }

    fun requestToggleSharing() {
        if (_uiState.value.sharingState.isSharingActive) {
            locationSharingRepository.toggleSharingWithConsent(consentAcknowledged = true, enable = false)
        } else {
            _uiState.update { it.copy(isConsentDialogOpen = true) }
        }
    }

    fun confirmConsentAndEnableSharing() {
        locationSharingRepository.toggleSharingWithConsent(consentAcknowledged = true, enable = true)
        _uiState.update { it.copy(isConsentDialogOpen = false) }
    }

    fun dismissConsentDialog() {
        _uiState.update { it.copy(isConsentDialogOpen = false) }
    }

    fun openSettings() {
        _uiState.update { it.copy(isSettingsOpen = true) }
    }

    fun closeSettings() {
        _uiState.update { it.copy(isSettingsOpen = false) }
    }
}
