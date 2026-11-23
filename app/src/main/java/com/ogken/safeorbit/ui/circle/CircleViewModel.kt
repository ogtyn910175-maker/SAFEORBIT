package com.ogken.safeorbit.ui.circle

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.ogken.safeorbit.model.CircleMember
import com.ogken.safeorbit.model.FamilyCircle
import com.ogken.safeorbit.repository.FamilyCircleRepository
import com.ogken.safeorbit.repository.FirebaseFamilyCircleRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.launchIn
import kotlinx.coroutines.flow.onEach
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

data class CircleUiState(
    val circle: FamilyCircle = FamilyCircle(),
    val members: List<CircleMember> = emptyList(),
    val createNameInput: String = "",
    val createDescInput: String = "",
    val createNameError: String? = null,
    val joinCodeInput: String = "",
    val joinCodeError: String? = null,
    val isLoading: Boolean = false,
    val successMessage: String? = null,
    val errorMessage: String? = null
)

class CircleViewModel(
    private val circleRepository: FamilyCircleRepository = FirebaseFamilyCircleRepository.instance
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

    fun createCircle(onSuccess: () -> Unit = {}) {
        val validation = circleRepository.validateCircleName(_uiState.value.createNameInput)
        if (validation != null) {
            _uiState.update { it.copy(createNameError = validation) }
            return
        }

        viewModelScope.launch {
            _uiState.update { it.copy(isLoading = true, errorMessage = null, successMessage = null) }
            val result = circleRepository.createCircle(
                name = _uiState.value.createNameInput,
                description = _uiState.value.createDescInput
            )

            if (result.isSuccess) {
                _uiState.update {
                    it.copy(
                        isLoading = false,
                        createNameInput = "",
                        createDescInput = "",
                        createNameError = null,
                        successMessage = "Circle '${result.getOrNull()?.name}' created successfully!"
                    )
                }
                onSuccess()
            } else {
                _uiState.update {
                    it.copy(
                        isLoading = false,
                        errorMessage = result.exceptionOrNull()?.localizedMessage ?: "Failed to create circle"
                    )
                }
            }
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

    fun joinCircle(onSuccess: () -> Unit = {}) {
        val validation = circleRepository.validateInviteCode(_uiState.value.joinCodeInput)
        if (validation != null) {
            _uiState.update { it.copy(joinCodeError = validation) }
            return
        }

        viewModelScope.launch {
            _uiState.update { it.copy(isLoading = true, errorMessage = null, successMessage = null) }
            val result = circleRepository.joinCircle(_uiState.value.joinCodeInput)

            if (result.isSuccess) {
                _uiState.update {
                    it.copy(
                        isLoading = false,
                        joinCodeInput = "",
                        joinCodeError = null,
                        successMessage = "Successfully connected to circle!"
                    )
                }
                onSuccess()
            } else {
                _uiState.update {
                    it.copy(
                        isLoading = false,
                        errorMessage = result.exceptionOrNull()?.localizedMessage ?: "Invalid or expired invite code"
                    )
                }
            }
        }
    }

    fun leaveCircle(onSuccess: () -> Unit = {}) {
        viewModelScope.launch {
            _uiState.update { it.copy(isLoading = true) }
            val result = circleRepository.leaveCircle()
            _uiState.update { it.copy(isLoading = false) }
            if (result.isSuccess) {
                onSuccess()
            } else {
                _uiState.update { it.copy(errorMessage = result.exceptionOrNull()?.localizedMessage ?: "Failed to leave circle") }
            }
        }
    }

    fun removeMember(memberId: String) {
        viewModelScope.launch {
            val result = circleRepository.removeMember(memberId)
            if (!result.isSuccess) {
                _uiState.update { it.copy(errorMessage = result.exceptionOrNull()?.localizedMessage ?: "Failed to remove member") }
            }
        }
    }

    fun clearMessages() {
        _uiState.update { it.copy(successMessage = null, errorMessage = null) }
    }
}
