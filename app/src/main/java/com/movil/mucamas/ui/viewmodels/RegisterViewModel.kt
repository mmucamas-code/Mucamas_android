package com.movil.mucamas.ui.viewmodels

import android.content.Context
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.movil.mucamas.domain.usecase.StartOtpFlowResult
import com.movil.mucamas.domain.usecase.StartOtpFlowUseCase
import com.movil.mucamas.ui.models.UserDto
import com.movil.mucamas.ui.repositories.UserRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

sealed class RegistrationUiState {
    data object Idle : RegistrationUiState()
    data object Loading : RegistrationUiState()
    data class UserAlreadyExists(val message: String) : RegistrationUiState()
    data class RegistrationSuccess(val userId: String) : RegistrationUiState()
    data class Error(val message: String) : RegistrationUiState()
}

class RegisterViewModel(
    private val repository: UserRepository = UserRepository()
) : ViewModel() {

    private val _uiState = MutableStateFlow<RegistrationUiState>(RegistrationUiState.Idle)
    val uiState: StateFlow<RegistrationUiState> = _uiState.asStateFlow()

    private val startOtpFlowUseCase = StartOtpFlowUseCase()

    fun registerUser(
        context: Context,
        idNumber: String,
        fullName: String,
        phone: String,
        email: String,
        address: String
    ) {
        _uiState.value = RegistrationUiState.Loading

        viewModelScope.launch {
            val userExistsResult = repository.findUserByIdNumber(idNumber)

            if (!userExistsResult.isSuccess) {
                _uiState.value = RegistrationUiState.Error("Error de base de datos al verificar el usuario.")
                return@launch
            }

            if (userExistsResult.getOrNull() != null) {
                _uiState.value = RegistrationUiState.UserAlreadyExists("Esta identificación ya está registrada.")
                return@launch
            }

            // Usuario no existe, proceder a crear
            val newUser = UserDto(
                idNumber = idNumber,
                fullName = fullName,
                phone = phone,
                email = email,
                mainAddress = address
            )

            val saveResult = repository.saveUser(newUser)
            if (saveResult.isSuccess) {
                // User created, now start the OTP flow using the use case
                when (val result = startOtpFlowUseCase(context,idNumber)) {
                    is StartOtpFlowResult.Success -> {
                        _uiState.value = RegistrationUiState.RegistrationSuccess(userId = idNumber)
                    }
                    is StartOtpFlowResult.Error.EmailSendError -> {
                        _uiState.value = RegistrationUiState.Error("Se creó el usuario, pero no se pudo enviar el correo de verificación.")
                    }
                    else -> { // Covers OtpGenerationError and UnknownError
                        _uiState.value = RegistrationUiState.Error("Se creó el usuario, pero ocurrió un error al generar el código de verificación.")
                    }
                }
            } else {
                _uiState.value = RegistrationUiState.Error("No se pudo completar el registro.")
            }
        }
    }

    fun resetState() {
        _uiState.value = RegistrationUiState.Idle
    }
}
