package com.ogken.safeorbit.model

/**
 * Encapsulates the location sharing status of the local device.
 * Per Stage 1 requirements, sharing is strictly initialized to OFF
 * and no background GPS or network transmission is performed.
 */
data class LocationSharingState(
    val isSharingActive: Boolean = false,
    val statusLabel: String = "SHARING OFF",
    val description: String = "Your location is completely private. No GPS signals or coordinates are being recorded or transmitted.",
    val privacyBadge: String = "Zero-Surveillance Mode"
)
