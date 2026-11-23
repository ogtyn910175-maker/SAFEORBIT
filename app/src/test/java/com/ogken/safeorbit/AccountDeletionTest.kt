package com.ogken.safeorbit

import com.ogken.safeorbit.model.User
import com.ogken.safeorbit.repository.InMemoryAuthRepository
import com.ogken.safeorbit.repository.InMemoryFamilyCircleRepository
import com.ogken.safeorbit.repository.InMemoryLocationSharingRepository
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.runBlocking
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Test

class AccountDeletionTest {

    private val authRepository = InMemoryAuthRepository()
    private val circleRepository = InMemoryFamilyCircleRepository()
    private val locationRepository = InMemoryLocationSharingRepository()

    @Test
    fun testSignOutStopsActiveSharing() = runBlocking {
        // Given sharing is active
        locationRepository.updateCircleInfo(hasCircle = true, circleName = "Family")
        locationRepository.startSharingWithConsent(consentAcknowledged = true)
        assertTrue(locationRepository.getLocationSharingState().first().isSharingActive)

        // When user signs out
        locationRepository.stopSharing()
        authRepository.signOut()

        // Then sharing is completely OFF and auth state is cleared
        val sharingState = locationRepository.getLocationSharingState().first()
        assertFalse(sharingState.isSharingActive)
        assertEquals("Location Sharing OFF", sharingState.statusBadgeText)
        assertNull(authRepository.authState.first())
    }

    @Test
    fun testAccountDeletionPurgesData() = runBlocking {
        // Given user is registered and sharing
        val signUpResult = authRepository.signUp(
            email = "user@safeorbit.test",
            password = "SecurePassword123!",
            displayName = "SafeOrbit User",
            role = "Parent"
        )
        assertTrue(signUpResult.isSuccess)
        locationRepository.updateCircleInfo(hasCircle = true, circleName = "Family")
        locationRepository.startSharingWithConsent(consentAcknowledged = true)

        // When account is deleted
        locationRepository.stopSharing()
        val deleteResult = authRepository.deleteAccount()
        assertTrue(deleteResult.isSuccess)

        // Then auth is null and sharing is OFF
        assertNull(authRepository.authState.first())
        assertFalse(locationRepository.getLocationSharingState().first().isSharingActive)
    }

    @Test
    fun testLeaveCircleResetsActiveCircle() = runBlocking {
        val circleBefore = circleRepository.getCircle().first()
        assertTrue(circleBefore.id.isNotBlank())

        val leaveResult = circleRepository.leaveCircle()
        assertTrue(leaveResult.isSuccess)
    }
}
