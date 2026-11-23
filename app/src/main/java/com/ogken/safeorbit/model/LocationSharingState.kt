package com.ogken.safeorbit.model

/**
 * Encapsulates the location sharing status of the local device.
 * Per Stage 2 requirements, sharing defaults strictly to OFF.
 * Users must knowingly agree through explicit consent verification before any sharing status changes.
 */
data class LocationSharingState(
    val isSharingActive: Boolean = false,
    val statusLabel: String = "SHARING OFF",
    val description: String = "Your location is completely private. SafeOrbit Stage 2 runs in zero-tracking mode without GPS permissions.",
    val privacyBadge: String = "Strict Consent Active",
    val consentAcknowledged: Boolean = false
)
