package com.ogken.safeorbit.repository

import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import com.ogken.safeorbit.model.User
import kotlinx.coroutines.channels.awaitClose
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.callbackFlow
import kotlinx.coroutines.tasks.await

interface UserRepository {
    fun getUser(): Flow<User>
    fun validateDisplayName(name: String): String?
    suspend fun updateProfile(name: String, role: String, emergencyPhone: String): Result<User>
    suspend fun updateCircleMembership(circleId: String?): Result<Unit>
}

class FirebaseUserRepository(
    private val auth: FirebaseAuth = FirebaseAuth.getInstance(),
    private val firestore: FirebaseFirestore = FirebaseFirestore.getInstance()
) : UserRepository {

    override fun getUser(): Flow<User> = callbackFlow {
        val uid = auth.currentUser?.uid
        if (uid == null) {
            trySend(User(name = "Guest User", isProfileSetupComplete = false))
            awaitClose { }
            return@callbackFlow
        }

        val registration = firestore.collection("users").document(uid)
            .addSnapshotListener { snapshot, error ->
                if (error != null) {
                    return@addSnapshotListener
                }
                if (snapshot != null && snapshot.exists()) {
                    val user = User.fromMap(snapshot.data ?: emptyMap(), uid)
                    trySend(user)
                } else {
                    val fallback = User(
                        id = uid,
                        name = auth.currentUser?.displayName ?: "SafeOrbit User",
                        email = auth.currentUser?.email ?: "",
                        isProfileSetupComplete = false
                    )
                    trySend(fallback)
                }
            }

        awaitClose { registration.remove() }
    }

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

    override suspend fun updateProfile(name: String, role: String, emergencyPhone: String): Result<User> {
        val error = validateDisplayName(name)
        if (error != null) {
            return Result.failure(IllegalArgumentException(error))
        }

        val uid = auth.currentUser?.uid ?: return Result.failure(IllegalStateException("User is not signed in"))
        val trimmedName = name.trim()

        return try {
            val userRef = firestore.collection("users").document(uid)
            val snapshot = userRef.get().await()
            val existing = if (snapshot.exists()) User.fromMap(snapshot.data ?: emptyMap(), uid) else User(id = uid)

            val updated = existing.copy(
                name = trimmedName,
                role = role.trim().ifBlank { "Member" },
                emergencyPhone = emergencyPhone.trim(),
                avatarInitial = trimmedName.take(1).uppercase(),
                isProfileSetupComplete = true
            )

            userRef.set(updated.toMap()).await()

            // Synchronize limited display fields (no email, no emergency phone) to active circle
            val circleId = existing.currentCircleId
            if (!circleId.isNullOrBlank()) {
                try {
                    firestore.collection("circles").document(circleId)
                        .collection("members").document(uid)
                        .update(
                            mapOf(
                                "name" to trimmedName,
                                "role" to updated.role,
                                "avatarInitial" to updated.avatarInitial
                            )
                        ).await()
                } catch (_: Exception) {
                    // Non-blocking if circle record has not yet been initialized
                }
            }

            Result.success(updated)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    override suspend fun updateCircleMembership(circleId: String?): Result<Unit> {
        val uid = auth.currentUser?.uid ?: return Result.failure(IllegalStateException("User is not signed in"))
        return try {
            firestore.collection("users").document(uid).update("currentCircleId", circleId).await()
            Result.success(Unit)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    companion object {
        val instance: UserRepository by lazy { FirebaseUserRepository() }
    }
}

class InMemoryUserRepository(
    initialUser: User = User(
        id = "user_001",
        name = "Tanmay",
        role = "Circle Creator",
        emergencyPhone = "+91 98765 43210",
        avatarInitial = "T",
        isProfileSetupComplete = true
    )
) : UserRepository {
    private val userFlow = MutableStateFlow(initialUser)

    override fun getUser(): Flow<User> = userFlow.asStateFlow()

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

    override suspend fun updateProfile(name: String, role: String, emergencyPhone: String): Result<User> {
        val error = validateDisplayName(name)
        if (error != null) return Result.failure(IllegalArgumentException(error))

        val trimmed = name.trim()
        val updated = userFlow.value.copy(
            name = trimmed,
            role = role.trim().ifBlank { "Member" },
            emergencyPhone = emergencyPhone.trim(),
            avatarInitial = trimmed.take(1).uppercase(),
            isProfileSetupComplete = true
        )
        userFlow.value = updated
        return Result.success(updated)
    }

    override suspend fun updateCircleMembership(circleId: String?): Result<Unit> {
        userFlow.value = userFlow.value.copy(currentCircleId = circleId)
        return Result.success(Unit)
    }

    companion object {
        val instance: InMemoryUserRepository by lazy { InMemoryUserRepository() }
    }
}
