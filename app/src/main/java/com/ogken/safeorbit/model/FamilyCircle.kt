package com.ogken.safeorbit.model

/**
 * Represents a SafeOrbit family circle containing multiple members.
 */
data class FamilyCircle(
    val id: String = "circle_core_01",
    val name: String = "SafeOrbit Core Circle",
    val description: String = "Primary family safety circle",
    val inviteCode: String = "SO-ORBIT-729",
    val members: List<CircleMember> = emptyList(),
    val isDemoCircle: Boolean = true
)
