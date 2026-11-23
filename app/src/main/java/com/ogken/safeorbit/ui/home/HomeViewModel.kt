package com.ogken.safeorbit.ui.home

import android.content.Context
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.ogken.safeorbit.domain.GetOrbitCircleUseCase
import com.ogken.safeorbit.model.CircleMember
import com.ogken.safeorbit.model.LocationPermissionStatus
import com.ogken.safeorbit.model.LocationSharingState
import com.ogken.safeorbit.model.User
import com.ogken.safeorbit.repository.FamilyCircleRepository
import com.ogken.safeorbit.repository.FirebaseFamilyCircleRepository
import com.ogken.safeorbit.repository.FirebaseUserRepository
import com.ogken.safeorbit.repository.InMemoryLocationSharingRepository
import com.ogken.safeorbit.repository.LocationSharingRepository
import com.ogken.safeorbit.repository.UserRepository
import com.ogken.safeorbit.util.LocationSystemStateHelper
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.launchIn
import kotlinx.coroutines.flow.onEach
import kotlinx.coroutines.flow.update

data class HomeUiState(
    val user: User = User(),
    val circleName: String = "",
    val inviteCode: String = "",
    val members: List<CircleMember> = emptyList(),
    val sharingState: LocationSharingState = LocationSharingState(),
    val isConsentDialogOpen: Boolean = false,
    val shouldRequestPermission: Boolean = false,
    val isSettingsOpen: Boolean = false,
    val userFeedbackMessage: String? = null
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
            val hasCircle = circle.id.isNotBlank() && circle.name.isNotBlank()
            locationSharingRepository.updateCircleInfo(hasCircle = hasCircle, circleName = circle.name)
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

    /**
     * Checks current permission, GPS service, and network status from the Android system.
     */
    fun refreshSystemConditions(context: Context) {
        val permStatus = LocationSystemStateHelper.checkPermissionStatus(context)
        val gpsEnabled = LocationSystemStateHelper.isLocationServicesEnabled(context)
        val networkAvailable = LocationSystemStateHelper.isNetworkAvailable(context)

        // Only overwrite permission if already granted or explicitly checked
        if (permStatus != LocationPermissionStatus.NOT_REQUESTED ||
            _uiState.value.sharingState.permissionStatus == LocationPermissionStatus.NOT_REQUESTED
        ) {
            locationSharingRepository.updatePermissionStatus(permStatus)
        }

        locationSharingRepository.updateSystemConditions(
            isLocationServicesEnabled = gpsEnabled,
            isNetworkAvailable = networkAvailable,
            isLocationAvailable = gpsEnabled
        )
    }

    /**
     * User initiates location sharing. Always checks consent and permissions first.
     */
    fun requestStartSharing(context: Context) {
        val current = _uiState.value.sharingState
        if (!current.hasFamilyCircle) {
            _uiState.update { it.copy(userFeedbackMessage = "Please join or create a family circle first.") }
            return
        }

        // Before requesting permission or enabling sharing: display consent explanation
        _uiState.update { it.copy(isConsentDialogOpen = true) }
    }

    /**
     * User explicitly acknowledges the pre-permission consent notice.
     */
    fun onConsentConfirmed(context: Context) {
        _uiState.update { it.copy(isConsentDialogOpen = false) }

        val currentPerm = LocationSystemStateHelper.checkPermissionStatus(context)
        if (currentPerm == LocationPermissionStatus.PRECISE_GRANTED ||
            currentPerm == LocationPermissionStatus.APPROXIMATE_GRANTED
        ) {
            // Permission already granted; activate sharing immediately
            locationSharingRepository.updatePermissionStatus(currentPerm)
            locationSharingRepository.startSharingWithConsent(consentAcknowledged = true)
            val permLabel = if (currentPerm == LocationPermissionStatus.PRECISE_GRANTED) "Precise" else "Approximate"
            _uiState.update { it.copy(userFeedbackMessage = "Location sharing started ($permLabel location).") }
        } else {
            // Trigger Android system permission request dialog
            _uiState.update { it.copy(shouldRequestPermission = true) }
        }
    }

    /**
     * Handles the outcome of the Android runtime permission launcher.
     */
    fun onPermissionResult(fineGranted: Boolean, coarseGranted: Boolean) {
        _uiState.update { it.copy(shouldRequestPermission = false) }

        val status = when {
            fineGranted -> LocationPermissionStatus.PRECISE_GRANTED
            coarseGranted -> LocationPermissionStatus.APPROXIMATE_GRANTED
            else -> LocationPermissionStatus.DENIED
        }

        locationSharingRepository.updatePermissionStatus(status)

        if (status == LocationPermissionStatus.PRECISE_GRANTED || status == LocationPermissionStatus.APPROXIMATE_GRANTED) {
            val res = locationSharingRepository.startSharingWithConsent(consentAcknowledged = true)
            if (res.isSuccess) {
                val mode = if (status == LocationPermissionStatus.PRECISE_GRANTED) "Precise" else "Approximate"
                _uiState.update { it.copy(userFeedbackMessage = "Location sharing activated ($mode).") }
            } else {
                _uiState.update { it.copy(userFeedbackMessage = res.exceptionOrNull()?.message ?: "Failed to start sharing.") }
            }
        } else {
            _uiState.update {
                it.copy(userFeedbackMessage = "Location permission was denied. Location sharing remains OFF.")
            }
        }
    }

    /**
     * User stops sharing. Always accessible and immediately terminates sharing.
     */
    fun stopSharing() {
        locationSharingRepository.stopSharing()
        _uiState.update {
            it.copy(userFeedbackMessage = "Location sharing is OFF. Your location is completely private.")
        }
    }

    fun dismissConsentDialog() {
        _uiState.update { it.copy(isConsentDialogOpen = false) }
    }

    fun clearFeedbackMessage() {
        _uiState.update { it.copy(userFeedbackMessage = null) }
    }

    fun openSettings() {
        _uiState.update { it.copy(isSettingsOpen = true) }
    }

    fun closeSettings() {
        _uiState.update { it.copy(isSettingsOpen = false) }
    }
}
