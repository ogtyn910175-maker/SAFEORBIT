package com.ogken.safeorbit.repository

import com.ogken.safeorbit.model.LocationSharingState
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow

/**
 * Repository interface for observing and controlling location-sharing state.
 */
interface LocationSharingRepository {
    fun getLocationSharingState(): Flow<LocationSharingState>
}

/**
 * Stage 1 implementation ensuring location sharing defaults to OFF
 * and strictly prevents unconsented tracking.
 */
class InMemoryLocationSharingRepository : LocationSharingRepository {

    private val _sharingState = MutableStateFlow(
        LocationSharingState(
            isSharingActive = false,
            statusLabel = "SHARING OFF",
            description = "Your location is completely private. Stage 1 architecture runs in zero-tracking mode without GPS permissions.",
            privacyBadge = "Strict Consent Active"
        )
    )

    override fun getLocationSharingState(): Flow<LocationSharingState> = _sharingState.asStateFlow()
}
