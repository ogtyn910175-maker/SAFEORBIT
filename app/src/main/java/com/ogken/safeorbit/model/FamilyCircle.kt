package com.ogken.safeorbit.model

/**
 * Represents a SafeOrbit family circle containing multiple members stored in Firestore.
 */
data class FamilyCircle(
    val id: String = "",
    val name: String = "",
    val description: String = "",
    val ownerId: String = "",
    val inviteCode: String = "",
    val inviteExpiresAt: Long = System.currentTimeMillis() + (48 * 60 * 60 * 1000L),
    val memberIds: List<String> = emptyList(),
    val members: List<CircleMember> = emptyList(),
    val isDemoCircle: Boolean = false,
    val createdAt: Long = System.currentTimeMillis()
) {
    fun toMap(): Map<String, Any?> = mapOf(
        "id" to id,
        "name" to name,
        "description" to description,
        "ownerId" to ownerId,
        "inviteCode" to inviteCode,
        "inviteExpiresAt" to inviteExpiresAt,
        "memberIds" to memberIds,
        "createdAt" to createdAt
    )

    companion object {
        fun fromMap(map: Map<String, Any?>, docId: String = ""): FamilyCircle {
            val rawMembers = map["memberIds"] as? List<*>
            val membersList = rawMembers?.filterIsInstance<String>() ?: emptyList()
            return FamilyCircle(
                id = (map["id"] as? String) ?: docId,
                name = (map["name"] as? String) ?: "Family Orbit",
                description = (map["description"] as? String) ?: "",
                ownerId = (map["ownerId"] as? String) ?: "",
                inviteCode = (map["inviteCode"] as? String) ?: "",
                inviteExpiresAt = (map["inviteExpiresAt"] as? Number)?.toLong() ?: 0L,
                memberIds = membersList,
                createdAt = (map["createdAt"] as? Number)?.toLong() ?: System.currentTimeMillis(),
                isDemoCircle = false
            )
        }
    }
}
