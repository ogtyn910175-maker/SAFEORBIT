package com.ogken.safeorbit

import com.ogken.safeorbit.model.LocationPermissionStatus
import com.ogken.safeorbit.model.SharingStatusSummary
import com.ogken.safeorbit.repository.InMemoryLocationSharingRepository
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.runBlocking
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test

class LocationConsentEnforcementTest {

    private val locationRepository = InMemoryLocationSharingRepository()

    @Test
    fun testDefaultStateIsStrictlyOff() = runBlocking {
        val state = locationRepository.getLocationSharingState().first()
        assertFalse("Sharing must be strictly OFF by default", state.isSharingActive)
        assertEquals("SHARING OFF", state.statusLabel)
        assertEquals(SharingStatusSummary.NO_FAMILY_CIRCLE, state.statusSummary)
        assertFalse(state.consentAcknowledged)
    }

    @Test
    fun testEnableWithoutConsentFails() {
        val result = locationRepository.toggleSharingWithConsent(consentAcknowledged = false, enable = true)
        assertTrue(result.isFailure)
        assertTrue(result.exceptionOrNull() is IllegalStateException)
    }

    @Test
    fun testEnableWithConsentSucceeds() = runBlocking {
        locationRepository.updateCircleInfo(hasCircle = true, circleName = "The SafeOrbit Family")
        val result = locationRepository.toggleSharingWithConsent(consentAcknowledged = true, enable = true)
        assertTrue(result.isSuccess)

        val state = locationRepository.getLocationSharingState().first()
        assertTrue(state.isSharingActive)
        assertTrue(state.consentAcknowledged)
        assertEquals("Location Sharing ON", state.statusBadgeText)
        assertEquals(SharingStatusSummary.ON, state.statusSummary)
    }

    @Test
    fun testDisableResetsToOff() = runBlocking {
        locationRepository.updateCircleInfo(hasCircle = true, circleName = "The SafeOrbit Family")
        locationRepository.toggleSharingWithConsent(consentAcknowledged = true, enable = true)
        locationRepository.stopSharing()

        val state = locationRepository.getLocationSharingState().first()
        assertFalse(state.isSharingActive)
        assertEquals("Location Sharing OFF", state.statusBadgeText)
        assertEquals(SharingStatusSummary.OFF, state.statusSummary)
    }

    @Test
    fun testSupportApproximateAndPrecisePermissions() = runBlocking {
        locationRepository.updatePermissionStatus(LocationPermissionStatus.APPROXIMATE_GRANTED)
        var state = locationRepository.getLocationSharingState().first()
        assertEquals(LocationPermissionStatus.APPROXIMATE_GRANTED, state.permissionStatus)
        assertTrue(state.permissionDescription.contains("Approximate"))

        locationRepository.updatePermissionStatus(LocationPermissionStatus.PRECISE_GRANTED)
        state = locationRepository.getLocationSharingState().first()
        assertEquals(LocationPermissionStatus.PRECISE_GRANTED, state.permissionStatus)
        assertTrue(state.permissionDescription.contains("Precise"))
    }

    @Test
    fun testPermissionDeniedTurnsSharingOff() = runBlocking {
        locationRepository.updateCircleInfo(hasCircle = true, circleName = "Family")
        locationRepository.startSharingWithConsent(consentAcknowledged = true)
        assertTrue(locationRepository.getLocationSharingState().first().isSharingActive)

        // Simulate permission revoked or denied
        locationRepository.updatePermissionStatus(LocationPermissionStatus.DENIED)
        val state = locationRepository.getLocationSharingState().first()
        assertFalse("Sharing must be terminated if permission is denied", state.isSharingActive)
        assertEquals(SharingStatusSummary.PERMISSION_DENIED, state.statusSummary)
        assertEquals("Permission Denied", state.statusBadgeText)
    }

    @Test
    fun testLocationServicesDisabledState() = runBlocking {
        locationRepository.updateCircleInfo(hasCircle = true, circleName = "Family")
        locationRepository.updatePermissionStatus(LocationPermissionStatus.PRECISE_GRANTED)
        locationRepository.updateSystemConditions(
            isLocationServicesEnabled = false,
            isNetworkAvailable = true,
            isLocationAvailable = false
        )

        val state = locationRepository.getLocationSharingState().first()
        assertEquals(SharingStatusSummary.LOCATION_SERVICES_DISABLED, state.statusSummary)
        assertEquals("Location Services Disabled", state.statusBadgeText)
    }

    @Test
    fun testNetworkUnavailableState() = runBlocking {
        locationRepository.updateCircleInfo(hasCircle = true, circleName = "Family")
        locationRepository.updatePermissionStatus(LocationPermissionStatus.PRECISE_GRANTED)
        locationRepository.updateSystemConditions(
            isLocationServicesEnabled = true,
            isNetworkAvailable = false,
            isLocationAvailable = true
        )

        val state = locationRepository.getLocationSharingState().first()
        assertEquals(SharingStatusSummary.NETWORK_UNAVAILABLE, state.statusSummary)
        assertEquals("Network Unavailable", state.statusBadgeText)
    }
}
