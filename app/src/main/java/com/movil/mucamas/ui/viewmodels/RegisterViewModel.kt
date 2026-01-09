package com.movil.mucamas.ui.viewmodels

import android.content.Context
import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.movil.mucamas.data.SessionManager
import com.movil.mucamas.data.model.UserSession // Ruta corregida
import com.movil.mucamas.ui.models.UserDto
import com.movil.mucamas.ui.repositories.UserRepository
import com.movil.mucamas.ui.utils.OtpManager
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

sealed class RegistrationUiState {
    data object Idle : RegistrationUiState()
    data object Loading : RegistrationUiState()
    data class UserAlreadyExists(val message: String) : RegistrationUiState()
    data class RegistrationSuccess(val otp: String) : RegistrationUiState()
    data class Error(val message: String) : RegistrationUiState()
}

class RegisterViewModel(
    private val repository: UserRepository = UserRepository()
) : ViewModel() {

    private val _uiState = MutableStateFlow<RegistrationUiState>(RegistrationUiState.Idle)
    val uiState: StateFlow<RegistrationUiState> = _uiState.asStateFlow()
    
    private var savedUser: UserDto? = null
    private var generatedOtp: String? = null

    fun registerUser(
        idNumber: String,
        fullName: String,
        phone: String,
        email: String,
        address: String,
        context: Context
    ) {
        _uiState.value = RegistrationUiState.Loading

        viewModelScope.launch {
            // 1. Validar que el usuario no exista
            val userExistsResult = repository.findUserByIdNumber(idNumber)

            if (userExistsResult.isSuccess) {
                if (userExistsResult.getOrNull() != null) {
                    // Usuario ya existe
                    _uiState.value = RegistrationUiState.UserAlreadyExists("Esta identificación ya está registrada.")
                } else {
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
                        savedUser = newUser // Guardamos el usuario para la sesión
                        val otp = OtpManager.generateAndNotifyOtp(context)
                        generatedOtp = otp
                        _uiState.value = RegistrationUiState.RegistrationSuccess(otp)
                    } else {
                        _uiState.value = RegistrationUiState.Error("No se pudo completar el registro.")
                    }
                }
            } else {
                _uiState.value = RegistrationUiState.Error("Error de base de datos.")
            }
        }
    }

    fun verifyOtpAndLogin(enteredOtp: String, context: Context): Boolean {
        if (enteredOtp == generatedOtp) {
            viewModelScope.launch {
                savedUser?.let {
                    // Aquí necesitaríamos el documentId, que no tenemos. 
                    // Lo ideal sería volver a buscar al usuario para obtenerlo.
                    // Por simplicidad del flujo, usaremos el idNumber como userId temporal
                    val session = UserSession(userId = it.idNumber, fullName = it.fullName, idNumber = it.idNumber, role = it.role)
                    Log.i("session", "SessionState: ${session.toString()}")
                    SessionManager(context).saveUserSession(session)
                }
            }
            return true
        }
        return false
    }

    fun resetState() {
        _uiState.value = RegistrationUiState.Idle
        generatedOtp = null
        savedUser = null
    }
}
