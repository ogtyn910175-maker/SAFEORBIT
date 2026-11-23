package com.ogken.safeorbit.repository

import com.ogken.safeorbit.model.User
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update

interface UserRepository {
    fun getUser(): Flow<User>
    fun getCurrentUser(): User
    fun updateProfile(name: String, role: String, emergencyPhone: String): Result<User>
    fun validateDisplayName(name: String): String?
}

class InMemoryUserRepository : UserRepository {

    private val _userState = MutableStateFlow(
        User(
            id = "user_tanmay_01",
            name = "Tanmay",
            role = "Circle Creator",
            emergencyPhone = "+91 98765 43210",
            avatarInitial = "T",
            avatarColorHex = 0xFF42D9B0,
            isProfileSetupComplete = true
        )
    )

    override fun getUser(): Flow<User> = _userState.asStateFlow()

    override fun getCurrentUser(): User = _userState.value

    override fun validateDisplayName(name: String): String? {
        val trimmed = name.trim()
        return when {
            trimmed.isEmpty() -> "Display name cannot be empty"
            trimmed.length < 2 -> "Display name must be at least 2 characters"
            trimmed.length > 30 -> "Display name cannot exceed 30 characters"
            !trimmed.matches(Regex("^[a-zA-Z0-9 ]+$")) -> "Name can only contain letters, numbers, and spaces"
            else -> null
        }
    }

    override fun updateProfile(name: String, role: String, emergencyPhone: String): Result<User> {
        val error = validateDisplayName(name)
        if (error != null) {
            return Result.failure(IllegalArgumentException(error))
        }

        val updated = _userState.value.copy(
            name = name.trim(),
            role = role.trim(),
            emergencyPhone = emergencyPhone.trim(),
            avatarInitial = name.trim().take(1).uppercase(),
            isProfileSetupComplete = true
        )
        _userState.update { updated }
        return Result.success(updated)
    }

    companion object {
        val instance: InMemoryUserRepository by lazy { InMemoryUserRepository() }
    }
}
