package com.ogken.safeorbit

import com.ogken.safeorbit.repository.InMemoryUserRepository
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.runBlocking
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Test

class UserProfileValidationTest {

    private val userRepository = InMemoryUserRepository()

    @Test
    fun testValidDisplayName() {
        assertNull(userRepository.validateDisplayName("Tanmay"))
        assertNull(userRepository.validateDisplayName("Sarah Connor"))
        assertNull(userRepository.validateDisplayName("Alex123"))
    }

    @Test
    fun testEmptyDisplayNameFails() {
        assertNotNull(userRepository.validateDisplayName(""))
        assertNotNull(userRepository.validateDisplayName("   "))
    }

    @Test
    fun testTooShortDisplayNameFails() {
        assertNotNull(userRepository.validateDisplayName("A"))
    }

    @Test
    fun testInvalidCharactersFail() {
        assertNotNull(userRepository.validateDisplayName("Tanmay@Home!"))
    }

    @Test
    fun testUpdateProfileSucceeds() = runBlocking {
        val result = userRepository.updateProfile(
            name = "Tanmay Developer",
            role = "Family Admin",
            emergencyPhone = "+1 555-0199"
        )

        assertTrue(result.isSuccess)
        val user = userRepository.getUser().first()
        assertEquals("Tanmay Developer", user.name)
        assertEquals("Family Admin", user.role)
        assertEquals("+1 555-0199", user.emergencyPhone)
        assertEquals("T", user.avatarInitial)
    }
}
