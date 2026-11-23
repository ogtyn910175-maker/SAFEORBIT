package com.ogken.safeorbit.model

/**
 * Represents an authenticated SafeOrbit user profile stored in Firestore.
 */
data class User(
    val id: String = "",
    val name: String = "",
    val email: String = "",
    val role: String = "Member",
    val emergencyPhone: String = "",
    val avatarInitial: String = if (name.isNotBlank()) name.take(1).uppercase() else "S",
    val avatarColorHex: Long = 0xFF42D9B0,
    val isProfileSetupComplete: Boolean = false,
    val currentCircleId: String? = null,
    val createdAt: Long = System.currentTimeMillis()
) {
    fun toMap(): Map<String, Any?> = mapOf(
        "id" to id,
        "name" to name,
        "email" to email,
        "role" to role,
        "emergencyPhone" to emergencyPhone,
        "avatarInitial" to avatarInitial,
        "avatarColorHex" to avatarColorHex,
        "isProfileSetupComplete" to isProfileSetupComplete,
        "currentCircleId" to currentCircleId,
        "createdAt" to createdAt
    )

    companion object {
        fun fromMap(map: Map<String, Any?>, docId: String = ""): User {
            val nameStr = (map["name"] as? String) ?: ""
            return User(
                id = (map["id"] as? String) ?: docId,
                name = nameStr,
                email = (map["email"] as? String) ?: "",
                role = (map["role"] as? String) ?: "Member",
                emergencyPhone = (map["emergencyPhone"] as? String) ?: "",
                avatarInitial = (map["avatarInitial"] as? String) ?: (if (nameStr.isNotBlank()) nameStr.take(1).uppercase() else "S"),
                avatarColorHex = (map["avatarColorHex"] as? Number)?.toLong() ?: 0xFF42D9B0,
                isProfileSetupComplete = (map["isProfileSetupComplete"] as? Boolean) ?: false,
                currentCircleId = map["currentCircleId"] as? String,
                createdAt = (map["createdAt"] as? Number)?.toLong() ?: System.currentTimeMillis()
            )
        }
    }
}
