package com.ogken.safeorbit

import com.ogken.safeorbit.domain.GetOrbitCircleUseCase
import com.ogken.safeorbit.repository.InMemoryFamilyCircleRepository
import com.ogken.safeorbit.repository.InMemoryLocationSharingRepository
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.runBlocking
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test

class FamilyCircleRepositoryTest {

    @Test
    fun testDefaultLocationSharingIsOff() = runBlocking {
        val repository = InMemoryLocationSharingRepository()
        val sharingState = repository.getLocationSharingState().first()
        
        // SafeOrbit Stage 1 Guarantee: Sharing MUST default to OFF
        assertFalse("Location sharing must default to inactive", sharingState.isSharingActive)
        assertEquals("SHARING OFF", sharingState.statusLabel)
    }

    @Test
    fun testCircleMembersLoaded() = runBlocking {
        val repository = InMemoryFamilyCircleRepository()
        val useCase = GetOrbitCircleUseCase(repository)
        val members = useCase().first()

        assertTrue("Circle members should not be empty", members.isNotEmpty())
        assertEquals("Tanmay", members.first().name)
        assertTrue("First member should be current user", members.first().isCurrentUser)
        assertFalse("First member sharing should be inactive", members.first().isSharingActive)
    }
}
