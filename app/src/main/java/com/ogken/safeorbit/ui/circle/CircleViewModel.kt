package com.ogken.safeorbit.ui.circle

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.ogken.safeorbit.model.CircleMember
import com.ogken.safeorbit.model.FamilyCircle
import com.ogken.safeorbit.repository.FamilyCircleRepository
import com.ogken.safeorbit.repository.InMemoryFamilyCircleRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.launchIn
import kotlinx.coroutines.flow.onEach
import kotlinx.coroutines.flow.update

data class CircleUiState(
    val circle: FamilyCircle = FamilyCircle(),
    val members: List<CircleMember> = emptyList(),
    val createNameInput: String = "",
    val createDescInput: String = "",
    val createNameError: String? = null,
    val joinCodeInput: String = "",
    val joinCodeError: String? = null,
    val successMessage: String? = null,
    val errorMessage: String? = null
)

class CircleViewModel(
    private val circleRepository: FamilyCircleRepository = InMemoryFamilyCircleRepository.instance
) : ViewModel() {

    private val _uiState = MutableStateFlow(CircleUiState())
    val uiState: StateFlow<CircleUiState> = _uiState.asStateFlow()

    init {
        circleRepository.getCircle().onEach { circle ->
            _uiState.update { it.copy(circle = circle, members = circle.members) }
        }.launchIn(viewModelScope)
    }

    fun onCreateNameChanged(name: String) {
        val error = circleRepository.validateCircleName(name)
        _uiState.update {
            it.copy(
                createNameInput = name,
                createNameError = if (name.isNotBlank()) error else null,
                successMessage = null,
                errorMessage = null
            )
        }
    }

    fun onCreateDescChanged(desc: String) {
        _uiState.update { it.copy(createDescInput = desc) }
    }

    fun createCircle(): Boolean {
        val validation = circleRepository.validateCircleName(_uiState.value.createNameInput)
        if (validation != null) {
            _uiState.update { it.copy(createNameError = validation) }
            return false
        }

        val result = circleRepository.createCircle(
            name = _uiState.value.createNameInput,
            description = _uiState.value.createDescInput
        )

        return if (result.isSuccess) {
            _uiState.update {
                it.copy(
                    createNameInput = "",
                    createDescInput = "",
                    createNameError = null,
                    successMessage = "Circle '${result.getOrNull()?.name}' created successfully! Share your invite code to add members."
                )
            }
            true
        } else {
            _uiState.update {
                it.copy(errorMessage = result.exceptionOrNull()?.message ?: "Failed to create circle")
            }
            false
        }
    }

    fun onJoinCodeChanged(code: String) {
        val uppercase = code.uppercase()
        val error = circleRepository.validateInviteCode(uppercase)
        _uiState.update {
            it.copy(
                joinCodeInput = uppercase,
                joinCodeError = if (uppercase.isNotBlank()) error else null,
                successMessage = null,
                errorMessage = null
            )
        }
    }

    fun joinCircle(): Boolean {
        val validation = circleRepository.validateInviteCode(_uiState.value.joinCodeInput)
        if (validation != null) {
            _uiState.update { it.copy(joinCodeError = validation) }
            return false
        }

        val result = circleRepository.joinCircle(_uiState.value.joinCodeInput)
        return if (result.isSuccess) {
            _uiState.update {
                it.copy(
                    joinCodeInput = "",
                    joinCodeError = null,
                    successMessage = "Successfully connected to circle! (Demo Simulation)"
                )
            }
            true
        } else {
            _uiState.update {
                it.copy(errorMessage = result.exceptionOrNull()?.message ?: "Invalid invite code")
            }
            false
        }
    }

    fun addDemoMember(name: String, role: String): Boolean {
        val result = circleRepository.addDemoMember(name, role)
        return if (result.isSuccess) {
            _uiState.update {
                it.copy(successMessage = "Demo member '${name}' added to your circle!")
            }
            true
        } else {
            _uiState.update {
                it.copy(errorMessage = result.exceptionOrNull()?.message ?: "Failed to add member")
            }
            false
        }
    }

    fun clearMessages() {
        _uiState.update { it.copy(successMessage = null, errorMessage = null) }
    }
}
