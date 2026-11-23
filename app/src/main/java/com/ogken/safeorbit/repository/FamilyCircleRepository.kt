package com.ogken.safeorbit.repository

import com.ogken.safeorbit.model.CircleMember
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flowOf

/**
 * Repository interface for managing family circle members.
 */
interface FamilyCircleRepository {
    fun getCircleMembers(): Flow<List<CircleMember>>
    fun getCircleName(): String
}

/**
 * Stage 1 In-Memory implementation providing architectural structure
 * and realistic UI state without requiring Firebase or network backends.
 */
class InMemoryFamilyCircleRepository : FamilyCircleRepository {

    private val members = listOf(
        CircleMember(
            id = "member_01",
            name = "Tanmay",
            relation = "You (Circle Owner)",
            isSharingActive = false,
            avatarInitial = "T",
            isCurrentUser = true,
            statusText = "Local Device • Sharing OFF"
        ),
        CircleMember(
            id = "member_02",
            name = "Mom",
            relation = "Family Member",
            isSharingActive = false,
            avatarInitial = "M",
            isCurrentUser = false,
            statusText = "Standby • Sharing OFF"
        ),
        CircleMember(
            id = "member_03",
            name = "Dad",
            relation = "Family Member",
            isSharingActive = false,
            avatarInitial = "D",
            isCurrentUser = false,
            statusText = "Standby • Sharing OFF"
        )
    )

    override fun getCircleMembers(): Flow<List<CircleMember>> = flowOf(members)

    override fun getCircleName(): String = "SafeOrbit Core Circle"
}
