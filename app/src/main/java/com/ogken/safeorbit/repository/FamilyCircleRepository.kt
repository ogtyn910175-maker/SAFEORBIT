package com.ogken.safeorbit.repository

import com.ogken.safeorbit.model.CircleMember
import com.ogken.safeorbit.model.FamilyCircle
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.update
import kotlin.random.Random

/**
 * Repository interface for managing family circles and their members.
 */
interface FamilyCircleRepository {
    fun getCircle(): Flow<FamilyCircle>
    fun getCircleMembers(): Flow<List<CircleMember>>
    fun getCircleName(): String
    fun getInviteCode(): String
    fun createCircle(name: String, description: String): Result<FamilyCircle>
    fun joinCircle(inviteCode: String): Result<Boolean>
    fun addDemoMember(name: String, role: String): Result<CircleMember>
    fun validateInviteCode(code: String): String?
    fun validateCircleName(name: String): String?
}

/**
 * In-Memory implementation of FamilyCircleRepository for Stage 2.
 * Clearly marks demo members and simulates circle operations
 * without network dependencies.
 */
class InMemoryFamilyCircleRepository : FamilyCircleRepository {

    private val initialMembers = listOf(
        CircleMember(
            id = "member_01",
            name = "Tanmay",
            role = "Circle Owner (You)",
            isSharingActive = false,
            avatarInitial = "T",
            avatarColorHex = 0xFF42D9B0,
            isCurrentUser = true,
            statusText = "Local Device • Sharing OFF",
            batteryPercent = 94,
            lastSeenText = "Active"
        ),
        CircleMember(
            id = "member_02",
            name = "Mom",
            role = "Parent (Demo)",
            isSharingActive = false,
            avatarInitial = "M",
            avatarColorHex = 0xFF72E9C4,
            isCurrentUser = false,
            statusText = "Standby • Sharing OFF",
            batteryPercent = 78,
            lastSeenText = "12m ago"
        ),
        CircleMember(
            id = "member_03",
            name = "Dad",
            role = "Parent (Demo)",
            isSharingActive = false,
            avatarInitial = "D",
            avatarColorHex = 0xFF143949,
            isCurrentUser = false,
            statusText = "Standby • Sharing OFF",
            batteryPercent = 65,
            lastSeenText = "45m ago"
        )
    )

    private val _circleState = MutableStateFlow(
        FamilyCircle(
            id = "circle_core_01",
            name = "SafeOrbit Core Circle",
            description = "Primary Family Orbit • Stage 2 Demo",
            inviteCode = "SO-ORBIT-729",
            members = initialMembers,
            isDemoCircle = true
        )
    )

    override fun getCircle(): Flow<FamilyCircle> = _circleState.asStateFlow()

    override fun getCircleMembers(): Flow<List<CircleMember>> = _circleState.map { it.members }

    override fun getCircleName(): String = _circleState.value.name

    override fun getInviteCode(): String = _circleState.value.inviteCode

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
            normalized.length > 16 -> "Invite code is too long"
            !normalized.matches(Regex("^[A-Z0-9-]+$")) -> "Code can only contain uppercase letters, numbers, and hyphens"
            else -> null
        }
    }

    override fun createCircle(name: String, description: String): Result<FamilyCircle> {
        val error = validateCircleName(name)
        if (error != null) {
            return Result.failure(IllegalArgumentException(error))
        }

        val randomCode = "SO-${Random.nextInt(100, 999)}-${Random.nextInt(100, 999)}"
        val owner = _circleState.value.members.firstOrNull { it.isCurrentUser } ?: CircleMember(
            id = "member_01",
            name = "Tanmay",
            role = "Circle Owner (You)",
            isCurrentUser = true,
            avatarInitial = "T"
        )

        val newCircle = FamilyCircle(
            id = "circle_${System.currentTimeMillis()}",
            name = name.trim(),
            description = description.ifBlank { "Private Family Circle" },
            inviteCode = randomCode,
            members = listOf(owner),
            isDemoCircle = true
        )

        _circleState.update { newCircle }
        return Result.success(newCircle)
    }

    override fun joinCircle(inviteCode: String): Result<Boolean> {
        val error = validateInviteCode(inviteCode)
        if (error != null) {
            return Result.failure(IllegalArgumentException(error))
        }

        // Demo simulation: Adds mock joined circle members
        val normalized = inviteCode.trim().uppercase()
        val owner = _circleState.value.members.firstOrNull { it.isCurrentUser } ?: CircleMember(
            id = "member_01",
            name = "Tanmay",
            role = "Member (You)",
            isCurrentUser = true,
            avatarInitial = "T"
        )

        val joinedMembers = listOf(
            owner,
            CircleMember(
                id = "demo_host",
                name = "Circle Admin",
                role = "Circle Host (Demo)",
                avatarInitial = "A",
                avatarColorHex = 0xFF72E9C4,
                statusText = "Standby • Sharing OFF",
                batteryPercent = 88
            ),
            CircleMember(
                id = "demo_child",
                name = "Junior",
                role = "Member (Demo)",
                avatarInitial = "J",
                avatarColorHex = 0xFF143949,
                statusText = "Standby • Sharing OFF",
                batteryPercent = 70
            )
        )

        _circleState.update {
            it.copy(
                name = "Joined Orbit ($normalized)",
                description = "Connected via invite code $normalized",
                inviteCode = normalized,
                members = joinedMembers,
                isDemoCircle = true
            )
        }
        return Result.success(true)
    }

    override fun addDemoMember(name: String, role: String): Result<CircleMember> {
        val trimmedName = name.trim()
        if (trimmedName.isEmpty()) {
            return Result.failure(IllegalArgumentException("Member name cannot be empty"))
        }

        val newMember = CircleMember(
            id = "member_${System.currentTimeMillis()}",
            name = trimmedName,
            role = role.ifBlank { "Member (Demo)" },
            isSharingActive = false,
            avatarInitial = trimmedName.take(1).uppercase(),
            avatarColorHex = 0xFF1B4456,
            isCurrentUser = false,
            statusText = "Standby • Sharing OFF",
            batteryPercent = Random.nextInt(50, 99),
            lastSeenText = "Just joined"
        )

        _circleState.update {
            it.copy(members = it.members + newMember)
        }
        return Result.success(newMember)
    }

    companion object {
        val instance: InMemoryFamilyCircleRepository by lazy { InMemoryFamilyCircleRepository() }
    }
}
