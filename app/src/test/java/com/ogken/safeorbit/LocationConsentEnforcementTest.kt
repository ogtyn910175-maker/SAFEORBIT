package com.ogken.safeorbit

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
        assertFalse(state.isSharingActive)
        assertEquals("SHARING OFF", state.statusLabel)
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
        val result = locationRepository.toggleSharingWithConsent(consentAcknowledged = true, enable = true)
        assertTrue(result.isSuccess)

        val state = locationRepository.getLocationSharingState().first()
        assertTrue(state.isSharingActive)
        assertTrue(state.consentAcknowledged)
        assertTrue(state.statusLabel.contains("SHARING"))
    }

    @Test
    fun testDisableResetsToOff() = runBlocking {
        locationRepository.toggleSharingWithConsent(consentAcknowledged = true, enable = true)
        locationRepository.toggleSharingWithConsent(consentAcknowledged = true, enable = false)

        val state = locationRepository.getLocationSharingState().first()
        assertFalse(state.isSharingActive)
        assertEquals("SHARING OFF", state.statusLabel)
    }
}
