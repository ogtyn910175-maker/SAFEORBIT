package com.ogken.safeorbit

import com.ogken.safeorbit.repository.InMemoryAuthRepository
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.runBlocking
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Test

class AuthValidationTest {

    private val authRepository = InMemoryAuthRepository()

    @Test
    fun testValidEmail() {
        assertNull(authRepository.validateEmail("tanmay@safeorbit.com"))
        assertNull(authRepository.validateEmail("user.name+filter@domain.co.in"))
    }

    @Test
    fun testInvalidEmailFails() {
        assertNotNull(authRepository.validateEmail(""))
        assertNotNull(authRepository.validateEmail("notanemail"))
        assertNotNull(authRepository.validateEmail("missing@domain"))
        assertNotNull(authRepository.validateEmail("@nodomain.com"))
    }

    @Test
    fun testStrongPassword() {
        assertNull(authRepository.validatePassword("SafeOrbit2026!"))
        assertNull(authRepository.validatePassword("P@ssw0rdSecure"))
    }

    @Test
    fun testWeakPasswordFails() {
        assertNotNull("Too short password should fail", authRepository.validatePassword("Pass1!"))
        assertNotNull("Missing uppercase should fail", authRepository.validatePassword("password123"))
        assertNotNull("Missing lowercase should fail", authRepository.validatePassword("PASSWORD123"))
        assertNotNull("Missing number should fail", authRepository.validatePassword("PasswordOnly"))
    }

    @Test
    fun testSignUpAndSignOutFlow() = runBlocking {
        val result = authRepository.signUp(
            email = "tanmay@safeorbit.com",
            password = "SecurePassword1",
            displayName = "Tanmay",
            role = "Creator"
        )
        assertTrue(result.isSuccess)

        val currentUser = authRepository.authState.first()
        assertNotNull(currentUser)
        assertEquals("Tanmay", currentUser?.name)
        assertEquals("tanmay@safeorbit.com", currentUser?.email)

        authRepository.signOut()
        val signedOutUser = authRepository.authState.first()
        assertNull(signedOutUser)
    }

    @Test
    fun testSignInFlow() = runBlocking {
        val result = authRepository.signIn(
            email = "parent@safeorbit.com",
            password = "ParentPassword1"
        )
        assertTrue(result.isSuccess)

        val currentUser = authRepository.authState.first()
        assertNotNull(currentUser)
        assertEquals("parent@safeorbit.com", currentUser?.email)
    }

    @Test
    fun testPasswordResetValidation() = runBlocking {
        val validResult = authRepository.sendPasswordReset("valid@safeorbit.com")
        assertTrue(validResult.isSuccess)

        val invalidResult = authRepository.sendPasswordReset("not-an-email")
        assertTrue(invalidResult.isFailure)
    }
}
