package com.ogken.safeorbit.model

/**
 * Represents the primary device user.
 */
data class User(
    val id: String = "user_tanmay_01",
    val name: String = "Tanmay",
    val role: String = "Circle Creator",
    val emergencyPhone: String = "+91 98765 43210",
    val avatarInitial: String = "T",
    val avatarColorHex: Long = 0xFF42D9B0,
    val isProfileSetupComplete: Boolean = true
)
