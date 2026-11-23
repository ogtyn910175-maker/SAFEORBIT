package com.ogken.safeorbit.domain

import com.ogken.safeorbit.model.CircleMember
import com.ogken.safeorbit.repository.FamilyCircleRepository
import kotlinx.coroutines.flow.Flow

/**
 * Domain use case to retrieve family circle members.
 * Keeps business logic decoupled from UI and ViewModel layers.
 */
class GetOrbitCircleUseCase(
    private val familyCircleRepository: FamilyCircleRepository
) {
    operator fun invoke(): Flow<List<CircleMember>> {
        return familyCircleRepository.getCircleMembers()
    }

    fun getCircleName(): String {
        return familyCircleRepository.getCircleName()
    }
}
