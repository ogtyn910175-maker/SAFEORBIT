package com.ogken.safeorbit.ui.home

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.ogken.safeorbit.domain.GetOrbitCircleUseCase
import com.ogken.safeorbit.model.CircleMember
import com.ogken.safeorbit.model.LocationSharingState
import com.ogken.safeorbit.model.User
import com.ogken.safeorbit.repository.FamilyCircleRepository
import com.ogken.safeorbit.repository.InMemoryFamilyCircleRepository
import com.ogken.safeorbit.repository.InMemoryLocationSharingRepository
import com.ogken.safeorbit.repository.LocationSharingRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.launchIn
import kotlinx.coroutines.flow.onEach
import kotlinx.coroutines.flow.update

data class HomeUiState(
    val user: User = User(),
    val circleName: String = "SafeOrbit Core Circle",
    val members: List<CircleMember> = emptyList(),
    val sharingState: LocationSharingState = LocationSharingState(),
    val isSettingsOpen: Boolean = false
)

class HomeViewModel(
    private val familyCircleRepository: FamilyCircleRepository = InMemoryFamilyCircleRepository(),
    private val locationSharingRepository: LocationSharingRepository = InMemoryLocationSharingRepository(),
    private val getOrbitCircleUseCase: GetOrbitCircleUseCase = GetOrbitCircleUseCase(familyCircleRepository)
) : ViewModel() {

    private val _uiState = MutableStateFlow(HomeUiState())
    val uiState: StateFlow<HomeUiState> = _uiState.asStateFlow()

    init {
        loadData()
    }

    private fun loadData() {
        _uiState.update { it.copy(circleName = getOrbitCircleUseCase.getCircleName()) }

        getOrbitCircleUseCase().onEach { members ->
            _uiState.update { it.copy(members = members) }
        }.launchIn(viewModelScope)

        locationSharingRepository.getLocationSharingState().onEach { sharingState ->
            _uiState.update { it.copy(sharingState = sharingState) }
        }.launchIn(viewModelScope)
    }

    fun openSettings() {
        _uiState.update { it.copy(isSettingsOpen = true) }
    }

    fun closeSettings() {
        _uiState.update { it.copy(isSettingsOpen = false) }
    }
}
