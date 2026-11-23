package com.ogken.safeorbit.repository

import com.ogken.safeorbit.model.LocationPermissionStatus
import com.ogken.safeorbit.model.LocationSharingState
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update

/**
 * Repository interface for observing and controlling consent-based location sharing.
 */
interface LocationSharingRepository {
    fun getLocationSharingState(): Flow<LocationSharingState>
    fun toggleSharingWithConsent(consentAcknowledged: Boolean, enable: Boolean): Result<LocationSharingState>
    fun startSharingWithConsent(consentAcknowledged: Boolean): Result<LocationSharingState>
    fun stopSharing(): Result<LocationSharingState>
    fun updatePermissionStatus(status: LocationPermissionStatus)
    fun updateSystemConditions(isLocationServicesEnabled: Boolean, isNetworkAvailable: Boolean, isLocationAvailable: Boolean)
    fun updateCircleInfo(hasCircle: Boolean, circleName: String)
    fun resetToDefaultOff()
}

/**
 * Stage 4A In-Memory implementation ensuring location sharing defaults to OFF
 * and strictly prevents unconsented tracking.
 */
class InMemoryLocationSharingRepository : LocationSharingRepository {

    private val defaultState = LocationSharingState(
        isSharingActive = false,
        permissionStatus = LocationPermissionStatus.NOT_REQUESTED,
        isLocationServicesEnabled = true,
        isNetworkAvailable = true,
        isLocationAvailable = true,
        hasFamilyCircle = false,
        circleName = "",
        consentAcknowledged = false
    )

    private val _sharingState = MutableStateFlow(defaultState)

    override fun getLocationSharingState(): Flow<LocationSharingState> = _sharingState.asStateFlow()

    override fun toggleSharingWithConsent(consentAcknowledged: Boolean, enable: Boolean): Result<LocationSharingState> {
        return if (enable) {
            startSharingWithConsent(consentAcknowledged)
        } else {
            stopSharing()
        }
    }

    override fun startSharingWithConsent(consentAcknowledged: Boolean): Result<LocationSharingState> {
        if (!consentAcknowledged) {
            return Result.failure(IllegalStateException("Explicit consent is required before location sharing can be activated."))
        }
        val current = _sharingState.value
        if (current.permissionStatus == LocationPermissionStatus.DENIED) {
            return Result.failure(IllegalStateException("Location permission has been denied. Please grant location access in device settings."))
        }

        val updated = current.copy(
            isSharingActive = true,
            consentAcknowledged = true
        )
        _sharingState.update { updated }
        return Result.success(updated)
    }

    override fun stopSharing(): Result<LocationSharingState> {
        val updated = _sharingState.value.copy(
            isSharingActive = false
        )
        _sharingState.update { updated }
        return Result.success(updated)
    }

    override fun updatePermissionStatus(status: LocationPermissionStatus) {
        _sharingState.update { current ->
            // If permission is denied while sharing was active, automatically turn sharing OFF
            val active = if (status == LocationPermissionStatus.DENIED) false else current.isSharingActive
            current.copy(
                permissionStatus = status,
                isSharingActive = active
            )
        }
    }

    override fun updateSystemConditions(
        isLocationServicesEnabled: Boolean,
        isNetworkAvailable: Boolean,
        isLocationAvailable: Boolean
    ) {
        _sharingState.update { current ->
            current.copy(
                isLocationServicesEnabled = isLocationServicesEnabled,
                isNetworkAvailable = isNetworkAvailable,
                isLocationAvailable = isLocationAvailable
            )
        }
    }

    override fun updateCircleInfo(hasCircle: Boolean, circleName: String) {
        _sharingState.update { current ->
            current.copy(
                hasFamilyCircle = hasCircle,
                circleName = circleName
            )
        }
    }

    override fun resetToDefaultOff() {
        _sharingState.update { defaultState }
    }

    companion object {
        val instance: LocationSharingRepository by lazy { InMemoryLocationSharingRepository() }
    }
}
