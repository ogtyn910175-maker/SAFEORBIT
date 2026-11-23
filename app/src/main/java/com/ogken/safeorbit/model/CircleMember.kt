package com.ogken.safeorbit.model

/**
 * Represents a member of the user's family orbit circle.
 */
data class CircleMember(
    val id: String = "",
    val userId: String = id,
    val name: String = "",
    val role: String = "Member",
    val isSharingActive: Boolean = false,
    val avatarInitial: String = if (name.isNotBlank()) name.take(1).uppercase() else "M",
    val avatarColorHex: Long = 0xFF1B4456,
    val isCurrentUser: Boolean = false,
    val statusText: String = "Location Sharing OFF",
    val batteryPercent: Int = 85,
    val lastSeenText: String = "Active",
    val joinedAt: Long = System.currentTimeMillis()
) {
    fun toMap(): Map<String, Any?> = mapOf(
        "id" to id,
        "userId" to userId,
        "name" to name,
        "role" to role,
        "isSharingActive" to isSharingActive,
        "avatarInitial" to avatarInitial,
        "avatarColorHex" to avatarColorHex,
        "batteryPercent" to batteryPercent,
        "statusText" to statusText,
        "joinedAt" to joinedAt
    )

    companion object {
        fun fromMap(map: Map<String, Any?>, currentUserId: String = ""): CircleMember {
            val memberId = (map["id"] as? String) ?: (map["userId"] as? String) ?: ""
            val nameStr = (map["name"] as? String) ?: "Member"
            return CircleMember(
                id = memberId,
                userId = memberId,
                name = nameStr,
                role = (map["role"] as? String) ?: "Member",
                isSharingActive = (map["isSharingActive"] as? Boolean) ?: false,
                avatarInitial = (map["avatarInitial"] as? String) ?: (if (nameStr.isNotBlank()) nameStr.take(1).uppercase() else "M"),
                avatarColorHex = (map["avatarColorHex"] as? Number)?.toLong() ?: 0xFF1B4456,
                isCurrentUser = (memberId == currentUserId && currentUserId.isNotBlank()),
                statusText = (map["statusText"] as? String) ?: "Location Sharing OFF",
                batteryPercent = (map["batteryPercent"] as? Number)?.toInt() ?: 85,
                joinedAt = (map["joinedAt"] as? Number)?.toLong() ?: System.currentTimeMillis()
            )
        }
    }
}
