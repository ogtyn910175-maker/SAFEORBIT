package com.ogken.safeorbit.model

/**
 * Represents a member of the user's family orbit circle.
 * In Stage 2, members are clearly identified with demo badges to reflect
 * that real backend device pairing will arrive in Stage 3.
 */
data class CircleMember(
    val id: String,
    val name: String,
    val role: String = "Member",
    val isSharingActive: Boolean = false,
    val avatarInitial: String = name.take(1).uppercase(),
    val avatarColorHex: Long = 0xFF1B4456,
    val isCurrentUser: Boolean = false,
    val statusText: String = "Location Sharing OFF",
    val batteryPercent: Int = 85,
    val lastSeenText: String = "Just now"
)
