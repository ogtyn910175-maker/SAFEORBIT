package com.ogken.safeorbit.model

/**
 * Status of Android runtime location permissions.
 */
enum class LocationPermissionStatus {
    NOT_REQUESTED,
    PRECISE_GRANTED,
    APPROXIMATE_GRANTED,
    DENIED
}

/**
 * High-level state of location sharing in SafeOrbit Stage 4A.
 */
enum class SharingStatusSummary {
    OFF,
    ON,
    PERMISSION_DENIED,
    LOCATION_SERVICES_DISABLED,
    NO_FAMILY_CIRCLE,
    NETWORK_UNAVAILABLE,
    LOCATION_UNAVAILABLE
}

/**
 * Encapsulates the complete consent-based location sharing status.
 *
 * Guaranteed Defaults:
 * - isSharingActive is strictly FALSE by default.
 * - Consent is required before any sharing state can be activated.
 * - No background service or GPS hardware coordinate harvesting is executed.
 */
data class LocationSharingState(
    val isSharingActive: Boolean = false,
    val permissionStatus: LocationPermissionStatus = LocationPermissionStatus.NOT_REQUESTED,
    val isLocationServicesEnabled: Boolean = true,
    val isNetworkAvailable: Boolean = true,
    val isLocationAvailable: Boolean = true,
    val hasFamilyCircle: Boolean = false,
    val circleName: String = "",
    val consentAcknowledged: Boolean = false,
    val explanationWhatIsShared: String = "When active, your location coordinates (approximate or precise based on permission) and battery status are shared with authorized members.",
    val explanationWhoCanSee: String = "Only verified members of your selected family circle can see your location. Outsiders, advertisers, and third parties cannot access this data."
) {
    /**
     * Resolves the primary sharing status summary based on current system and app conditions.
     */
    val statusSummary: SharingStatusSummary
        get() = when {
            isSharingActive -> SharingStatusSummary.ON
            !hasFamilyCircle -> SharingStatusSummary.NO_FAMILY_CIRCLE
            permissionStatus == LocationPermissionStatus.DENIED -> SharingStatusSummary.PERMISSION_DENIED
            !isLocationServicesEnabled -> SharingStatusSummary.LOCATION_SERVICES_DISABLED
            !isNetworkAvailable -> SharingStatusSummary.NETWORK_UNAVAILABLE
            !isLocationAvailable -> SharingStatusSummary.LOCATION_UNAVAILABLE
            else -> SharingStatusSummary.OFF
        }

    val statusLabel: String
        get() = if (isSharingActive) "SHARING ACTIVE" else "SHARING OFF"

    val privacyBadge: String
        get() = if (isSharingActive) "Consent Verified" else "Strict Consent Active"

    val statusBadgeText: String
        get() = when (statusSummary) {
            SharingStatusSummary.ON -> "Location Sharing ON"
            SharingStatusSummary.OFF -> "Location Sharing OFF"
            SharingStatusSummary.PERMISSION_DENIED -> "Permission Denied"
            SharingStatusSummary.LOCATION_SERVICES_DISABLED -> "Location Services Disabled"
            SharingStatusSummary.NO_FAMILY_CIRCLE -> "No Family Circle"
            SharingStatusSummary.NETWORK_UNAVAILABLE -> "Network Unavailable"
            SharingStatusSummary.LOCATION_UNAVAILABLE -> "Location Unavailable"
        }

    val permissionDescription: String
        get() = when (permissionStatus) {
            LocationPermissionStatus.PRECISE_GRANTED -> "Precise Location (Exact coordinates enabled)"
            LocationPermissionStatus.APPROXIMATE_GRANTED -> "Approximate Location (Area-level sharing enabled)"
            LocationPermissionStatus.DENIED -> "Location Permission Denied by User"
            LocationPermissionStatus.NOT_REQUESTED -> "Permission Not Requested Yet"
        }
}
