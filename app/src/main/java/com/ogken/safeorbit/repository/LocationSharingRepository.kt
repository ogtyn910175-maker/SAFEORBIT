package com.ogken.safeorbit.repository

import com.ogken.safeorbit.model.LocationSharingState
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update

/**
 * Repository interface for observing and controlling location-sharing state.
 */
interface LocationSharingRepository {
    fun getLocationSharingState(): Flow<LocationSharingState>
    fun toggleSharingWithConsent(consentAcknowledged: Boolean, enable: Boolean): Result<LocationSharingState>
    fun resetToDefaultOff()
}

/**
 * Stage 2 implementation ensuring location sharing defaults to OFF
 * and strictly prevents unconsented tracking.
 */
class InMemoryLocationSharingRepository : LocationSharingRepository {

    private val defaultState = LocationSharingState(
        isSharingActive = false,
        statusLabel = "SHARING OFF",
        description = "Your location is completely private. Stage 2 architecture runs in zero-tracking mode without GPS permissions.",
        privacyBadge = "Strict Consent Active",
        consentAcknowledged = false
    )

    private val _sharingState = MutableStateFlow(defaultState)

    override fun getLocationSharingState(): Flow<LocationSharingState> = _sharingState.asStateFlow()

    override fun toggleSharingWithConsent(consentAcknowledged: Boolean, enable: Boolean): Result<LocationSharingState> {
        if (enable && !consentAcknowledged) {
            return Result.failure(IllegalStateException("Explicit consent is required before location sharing can be activated"))
        }

        val updated = if (enable) {
            LocationSharingState(
                isSharingActive = true,
                statusLabel = "SHARING SIMULATED (STAGE 2)",
                description = "Simulation Mode Active. GPS sensors are not bound. In Stage 3, coordinates will be broadcast with end-to-end consent.",
                privacyBadge = "Consent Verified (Demo)",
                consentAcknowledged = true
            )
        } else {
            defaultState
        }

        _sharingState.update { updated }
        return Result.success(updated)
    }

    override fun resetToDefaultOff() {
        _sharingState.update { defaultState }
    }

    companion object {
        val instance: InMemoryLocationSharingRepository by lazy { InMemoryLocationSharingRepository() }
    }
}
