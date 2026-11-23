package com.ogken.safeorbit

import com.ogken.safeorbit.repository.InMemoryFamilyCircleRepository
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.runBlocking
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Test

class FamilyCircleValidationTest {

    private val circleRepository = InMemoryFamilyCircleRepository()

    @Test
    fun testValidCircleName() {
        assertNull(circleRepository.validateCircleName("The Sharma Family"))
        assertNull(circleRepository.validateCircleName("Orbit Alpha"))
    }

    @Test
    fun testEmptyOrShortCircleNameFails() {
        assertNotNull(circleRepository.validateCircleName(""))
        assertNotNull(circleRepository.validateCircleName("AB"))
    }

    @Test
    fun testValidInviteCode() {
        assertNull(circleRepository.validateInviteCode("SO-ORBIT-729"))
        assertNull(circleRepository.validateInviteCode("ORBIT123"))
    }

    @Test
    fun testInvalidInviteCodeFails() {
        assertNotNull(circleRepository.validateInviteCode(""))
        assertNotNull(circleRepository.validateInviteCode("SO"))
        assertNotNull(circleRepository.validateInviteCode("CODE with spaces!"))
    }

    @Test
    fun testCreateCircleGeneratesCodeAndStoresOwner() = runBlocking {
        val result = circleRepository.createCircle("Tanmay Family", "Primary home safety")
        assertTrue(result.isSuccess)

        val circle = circleRepository.getCircle().first()
        assertEquals("Tanmay Family", circle.name)
        assertTrue(circle.inviteCode.startsWith("SO-"))
        assertTrue(circle.members.isNotEmpty())
        assertTrue(circle.members.first().isCurrentUser)
    }

    @Test
    fun testJoinCircleSimulatesConnectingMembers() = runBlocking {
        val result = circleRepository.joinCircle("SO-TEST-999")
        assertTrue(result.isSuccess)

        val circle = circleRepository.getCircle().first()
        assertEquals("SO-TEST-999", circle.inviteCode)
        assertTrue("Joined circle should contain multiple members in demo mode", circle.members.size >= 2)
    }
}
