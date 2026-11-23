package com.ogken.safeorbit.repository

import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FieldValue
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.SetOptions
import com.ogken.safeorbit.model.CircleMember
import com.ogken.safeorbit.model.FamilyCircle
import com.ogken.safeorbit.model.User
import kotlinx.coroutines.channels.awaitClose
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.callbackFlow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.tasks.await
import java.security.SecureRandom

/**
 * Repository interface for managing family circles and their members.
 */
interface FamilyCircleRepository {
    fun getCircle(): Flow<FamilyCircle>
    fun getCircleMembers(): Flow<List<CircleMember>>
    fun getCircleName(): String
    fun getInviteCode(): String
    suspend fun createCircle(name: String, description: String): Result<FamilyCircle>
    suspend fun joinCircle(inviteCode: String): Result<Boolean>
    suspend fun leaveCircle(): Result<Boolean>
    suspend fun removeMember(memberId: String): Result<Boolean>
    fun validateInviteCode(code: String): String?
    fun validateCircleName(name: String): String?
}

/**
 * Production implementation powered by Cloud Firestore.
 */
class FirebaseFamilyCircleRepository(
    private val auth: FirebaseAuth = FirebaseAuth.getInstance(),
    private val firestore: FirebaseFirestore = FirebaseFirestore.getInstance()
) : FamilyCircleRepository {

    private var cachedCircleName: String = "SafeOrbit Circle"
    private var cachedInviteCode: String = ""

    override fun getCircle(): Flow<FamilyCircle> = callbackFlow {
        val uid = auth.currentUser?.uid
        if (uid == null) {
            trySend(FamilyCircle(name = "SafeOrbit Core Circle", isDemoCircle = true))
            awaitClose { }
            return@callbackFlow
        }

        // Listen to current user's document to get active currentCircleId
        val userListener = firestore.collection("users").document(uid)
            .addSnapshotListener { userDoc, _ ->
                val circleId = userDoc?.getString("currentCircleId")
                if (circleId.isNullOrBlank()) {
                    // Fallback to sample or empty state
                    val defaultCircle = FamilyCircle(
                        id = "",
                        name = "No Active Circle",
                        description = "Create or join a family circle to get started.",
                        inviteCode = "",
                        isDemoCircle = false
                    )
                    cachedCircleName = defaultCircle.name
                    cachedInviteCode = defaultCircle.inviteCode
                    trySend(defaultCircle)
                } else {
                    // Listen to circle document
                    firestore.collection("circles").document(circleId)
                        .addSnapshotListener { circleDoc, _ ->
                            if (circleDoc != null && circleDoc.exists()) {
                                val baseCircle = FamilyCircle.fromMap(circleDoc.data ?: emptyMap(), circleId)
                                cachedCircleName = baseCircle.name
                                cachedInviteCode = baseCircle.inviteCode

                                // Fetch members subcollection
                                firestore.collection("circles").document(circleId)
                                    .collection("members")
                                    .addSnapshotListener { membersSnapshot, _ ->
                                        val members = membersSnapshot?.documents?.mapNotNull { doc ->
                                            CircleMember.fromMap(doc.data ?: emptyMap(), uid)
                                        } ?: emptyList()

                                        trySend(baseCircle.copy(members = members))
                                    }
                            } else {
                                val fallback = FamilyCircle(name = "Family Orbit")
                                trySend(fallback)
                            }
                        }
                }
            }

        awaitClose { userListener.remove() }
    }

    override fun getCircleMembers(): Flow<List<CircleMember>> = getCircle().map { it.members }

    override fun getCircleName(): String = cachedCircleName

    override fun getInviteCode(): String = cachedInviteCode

    override fun validateCircleName(name: String): String? {
        val trimmed = name.trim()
        return when {
            trimmed.isEmpty() -> "Circle name cannot be empty"
            trimmed.length < 3 -> "Circle name must be at least 3 characters"
            trimmed.length > 35 -> "Circle name cannot exceed 35 characters"
            else -> null
        }
    }

    override fun validateInviteCode(code: String): String? {
        val normalized = code.trim().uppercase()
        return when {
            normalized.isEmpty() -> "Please enter an invite code"
            normalized.length < 6 -> "Invite code is too short"
            normalized.length > 18 -> "Invite code is too long"
            !normalized.matches(Regex("^[A-Z0-9-]+$")) -> "Code can only contain letters, numbers, and hyphens"
            else -> null
        }
    }

    private fun generateSecureInviteCode(): String {
        val random = SecureRandom()
        val part1 = random.nextInt(900) + 100
        val part2 = random.nextInt(900) + 100
        return "SO-$part1-$part2"
    }

    override suspend fun createCircle(name: String, description: String): Result<FamilyCircle> {
        val nameError = validateCircleName(name)
        if (nameError != null) return Result.failure(IllegalArgumentException(nameError))

        val currentUser = auth.currentUser ?: return Result.failure(IllegalStateException("You must be logged in to create a circle"))
        val circleId = "circle_${System.currentTimeMillis()}"
        val inviteCode = generateSecureInviteCode()
        val expiresAt = System.currentTimeMillis() + (48 * 60 * 60 * 1000L) // 48h validity

        return try {
            val circle = FamilyCircle(
                id = circleId,
                name = name.trim(),
                description = description.trim().ifBlank { "Private Family Orbit" },
                ownerId = currentUser.uid,
                inviteCode = inviteCode,
                inviteExpiresAt = expiresAt,
                memberIds = listOf(currentUser.uid),
                isDemoCircle = false
            )

            // 1. Create circle doc
            firestore.collection("circles").document(circleId).set(circle.toMap()).await()

            // 2. Add owner to members subcollection
            val ownerMember = CircleMember(
                id = currentUser.uid,
                userId = currentUser.uid,
                name = currentUser.displayName ?: "Circle Owner",
                role = "Circle Owner (You)",
                isCurrentUser = true,
                isSharingActive = false,
                statusText = "Location Sharing OFF"
            )
            firestore.collection("circles").document(circleId)
                .collection("members").document(currentUser.uid)
                .set(ownerMember.toMap()).await()

            // 3. Register invite in secure invites collection
            val inviteData = mapOf(
                "code" to inviteCode,
                "circleId" to circleId,
                "circleName" to circle.name,
                "createdBy" to currentUser.uid,
                "expiresAt" to expiresAt,
                "isActive" to true
            )
            firestore.collection("invites").document(inviteCode).set(inviteData).await()

            // 4. Update user's currentCircleId
            firestore.collection("users").document(currentUser.uid)
                .update("currentCircleId", circleId).await()

            Result.success(circle)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    override suspend fun joinCircle(inviteCode: String): Result<Boolean> {
        val codeError = validateInviteCode(inviteCode)
        if (codeError != null) return Result.failure(IllegalArgumentException(codeError))

        val currentUser = auth.currentUser ?: return Result.failure(IllegalStateException("You must be logged in to join a circle"))
        val normalizedCode = inviteCode.trim().uppercase()

        return try {
            // 1. Validate invite document in Firestore
            val inviteDoc = firestore.collection("invites").document(normalizedCode).get().await()
            if (!inviteDoc.exists()) {
                return Result.failure(IllegalArgumentException("Invite code '$normalizedCode' was not found or is invalid."))
            }

            val isActive = inviteDoc.getBoolean("isActive") ?: false
            val expiresAt = inviteDoc.getLong("expiresAt") ?: 0L
            val circleId = inviteDoc.getString("circleId") ?: ""

            if (!isActive) {
                return Result.failure(IllegalStateException("This invite code has been deactivated."))
            }
            if (expiresAt < System.currentTimeMillis()) {
                return Result.failure(IllegalStateException("This invite code has expired. Please request a new invite."))
            }

            // 2. Add member to circle
            val newMember = CircleMember(
                id = currentUser.uid,
                userId = currentUser.uid,
                name = currentUser.displayName ?: "Family Member",
                role = "Member",
                isCurrentUser = true,
                isSharingActive = false,
                statusText = "Location Sharing OFF"
            )

            firestore.collection("circles").document(circleId)
                .collection("members").document(currentUser.uid)
                .set(newMember.toMap()).await()

            // 3. Append to memberIds array
            firestore.collection("circles").document(circleId)
                .update("memberIds", FieldValue.arrayUnion(currentUser.uid)).await()

            // 4. Update user's currentCircleId
            firestore.collection("users").document(currentUser.uid)
                .update("currentCircleId", circleId).await()

            Result.success(true)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    override suspend fun leaveCircle(): Result<Boolean> {
        val currentUser = auth.currentUser ?: return Result.failure(IllegalStateException("User is not signed in"))
        return try {
            val userDoc = firestore.collection("users").document(currentUser.uid).get().await()
            val circleId = userDoc.getString("currentCircleId") ?: return Result.success(true)

            // Remove member doc
            firestore.collection("circles").document(circleId)
                .collection("members").document(currentUser.uid).delete().await()

            // Remove from memberIds
            firestore.collection("circles").document(circleId)
                .update("memberIds", FieldValue.arrayRemove(currentUser.uid)).await()

            // Clear currentCircleId
            firestore.collection("users").document(currentUser.uid)
                .update("currentCircleId", null).await()

            Result.success(true)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    override suspend fun removeMember(memberId: String): Result<Boolean> {
        val currentUser = auth.currentUser ?: return Result.failure(IllegalStateException("User is not signed in"))
        return try {
            val userDoc = firestore.collection("users").document(currentUser.uid).get().await()
            val circleId = userDoc.getString("currentCircleId") ?: return Result.failure(IllegalStateException("No active circle"))

            val circleDoc = firestore.collection("circles").document(circleId).get().await()
            val ownerId = circleDoc.getString("ownerId")

            if (ownerId != currentUser.uid) {
                return Result.failure(SecurityException("Only the circle owner can remove members."))
            }

            // Remove target member from circle
            firestore.collection("circles").document(circleId)
                .collection("members").document(memberId).delete().await()

            firestore.collection("circles").document(circleId)
                .update("memberIds", FieldValue.arrayRemove(memberId)).await()

            // Clear target user's currentCircleId if possible
            firestore.collection("users").document(memberId)
                .update("currentCircleId", null).await()

            Result.success(true)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    companion object {
        val instance: FamilyCircleRepository by lazy { FirebaseFamilyCircleRepository() }
    }
}

/**
 * In-memory test double for local JVM unit testing and preview mode.
 */
class InMemoryFamilyCircleRepository : FamilyCircleRepository {
    private val circleFlow = MutableStateFlow(
        FamilyCircle(
            id = "circle_default",
            name = "SafeOrbit Core Circle",
            description = "Primary family safety circle",
            ownerId = "user_001",
            inviteCode = "SO-ORBIT-729",
            members = listOf(
                CircleMember(
                    id = "member_1",
                    userId = "user_001",
                    name = "Tanmay",
                    role = "Circle Creator",
                    isCurrentUser = true,
                    isSharingActive = false,
                    statusText = "Location Sharing OFF"
                ),
                CircleMember(
                    id = "member_2",
                    userId = "user_002",
                    name = "Aarav",
                    role = "Brother",
                    isCurrentUser = false,
                    isSharingActive = false,
                    statusText = "Standby (OFF)"
                )
            ),
            isDemoCircle = true
        )
    )

    override fun getCircle(): Flow<FamilyCircle> = circleFlow.asStateFlow()

    override fun getCircleMembers(): Flow<List<CircleMember>> = circleFlow.map { it.members }

    override fun getCircleName(): String = circleFlow.value.name

    override fun getInviteCode(): String = circleFlow.value.inviteCode

    override fun validateCircleName(name: String): String? {
        val trimmed = name.trim()
        return when {
            trimmed.isEmpty() -> "Circle name cannot be empty"
            trimmed.length < 3 -> "Circle name must be at least 3 characters"
            trimmed.length > 35 -> "Circle name cannot exceed 35 characters"
            else -> null
        }
    }

    override fun validateInviteCode(code: String): String? {
        val normalized = code.trim().uppercase()
        return when {
            normalized.isEmpty() -> "Please enter an invite code"
            normalized.length < 6 -> "Invite code is too short"
            normalized.length > 18 -> "Invite code is too long"
            !normalized.matches(Regex("^[A-Z0-9-]+$")) -> "Code can only contain letters, numbers, and hyphens"
            else -> null
        }
    }

    override suspend fun createCircle(name: String, description: String): Result<FamilyCircle> {
        val error = validateCircleName(name)
        if (error != null) return Result.failure(IllegalArgumentException(error))

        val randomCode = "SO-${(100..999).random()}-${(100..999).random()}"
        val newCircle = FamilyCircle(
            id = "circle_${System.currentTimeMillis()}",
            name = name.trim(),
            description = description.trim(),
            ownerId = "user_001",
            inviteCode = randomCode,
            members = listOf(
                CircleMember(
                    id = "member_owner",
                    userId = "user_001",
                    name = "Tanmay",
                    role = "Circle Creator",
                    isCurrentUser = true,
                    isSharingActive = false,
                    statusText = "Location Sharing OFF"
                )
            ),
            isDemoCircle = false
        )
        circleFlow.value = newCircle
        return Result.success(newCircle)
    }

    override suspend fun joinCircle(inviteCode: String): Result<Boolean> {
        val error = validateInviteCode(inviteCode)
        if (error != null) return Result.failure(IllegalArgumentException(error))

        val updatedMembers = circleFlow.value.members.toMutableList()
        if (updatedMembers.none { it.id == "member_joined" }) {
            updatedMembers.add(
                CircleMember(
                    id = "member_joined",
                    userId = "user_joined",
                    name = "Connected Member",
                    role = "Member",
                    isCurrentUser = false,
                    isSharingActive = false,
                    statusText = "Standby (OFF)"
                )
            )
        }
        circleFlow.value = circleFlow.value.copy(
            inviteCode = inviteCode.trim().uppercase(),
            members = updatedMembers
        )
        return Result.success(true)
    }

    override suspend fun leaveCircle(): Result<Boolean> {
        circleFlow.value = FamilyCircle(name = "No Active Circle", isDemoCircle = false)
        return Result.success(true)
    }

    override suspend fun removeMember(memberId: String): Result<Boolean> {
        val current = circleFlow.value
        val updated = current.members.filterNot { it.id == memberId }
        circleFlow.value = current.copy(members = updated)
        return Result.success(true)
    }

    companion object {
        val instance: InMemoryFamilyCircleRepository by lazy { InMemoryFamilyCircleRepository() }
    }
}
