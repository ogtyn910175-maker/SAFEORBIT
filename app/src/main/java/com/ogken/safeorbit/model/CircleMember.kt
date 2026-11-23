package com.ogken.safeorbit.model

/**
 * Represents a member of the user's family orbit circle.
 * In Stage 1, this provides architectural structure and preview state
 * without requiring Firebase or live device pairing.
 */
data class CircleMember(
    val id: String,
    val name: String,
    val relation: String,
    val isSharingActive: Boolean = false,
    val avatarInitial: String,
    val isCurrentUser: Boolean = false,
    val statusText: String = "Location Sharing OFF"
)
