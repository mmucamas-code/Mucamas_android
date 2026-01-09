package com.movil.mucamas.ui.viewmodels

import android.content.Context
import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.movil.mucamas.data.SessionManager
import com.movil.mucamas.data.model.UserSession
import com.movil.mucamas.ui.models.UserDto
import com.movil.mucamas.ui.repositories.UserRepository
import com.movil.mucamas.ui.utils.OtpManager
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

sealed class LoginUiState {
    data object Idle : LoginUiState()
    data object Loading : LoginUiState()
    data class UserFound(val otp: String) : LoginUiState() // Estado para cuando se encuentra el usuario y se genera el OTP
    data object UserNotFound : LoginUiState()
    data class Error(val message: String) : LoginUiState()
}

class LoginViewModel(
    private val repository: UserRepository = UserRepository()
) : ViewModel() {

    private val _uiState = MutableStateFlow<LoginUiState>(LoginUiState.Idle)
    val uiState: StateFlow<LoginUiState> = _uiState.asStateFlow()

    private var savedUser: UserDto? = null
    private var generatedOtp: String? = null

    fun findUserById(idNumber: String, context: Context) {
        _uiState.value = LoginUiState.Loading

        viewModelScope.launch {
            val result = repository.findUserByIdNumber(idNumber)
            
            if (result.isSuccess) {
                val user = result.getOrNull()
                if (user != null) {
                    // Usuario encontrado: generar y notificar OTP
                    val otp = OtpManager.generateAndNotifyOtp(context)
                    generatedOtp = otp
                    savedUser = user
                    _uiState.value = LoginUiState.UserFound(otp)
                } else {
                    // Usuario no encontrado
                    _uiState.value = LoginUiState.UserNotFound
                }
            } else {
                // Error en la consulta
                _uiState.value = LoginUiState.Error(
                    result.exceptionOrNull()?.message ?: "Error en la base de datos"
                )
            }
        }
    }

    /**
     * Verifica si el código ingresado coincide con el generado.
     */
    fun verifyOtpAndLogin(enteredOtp: String, context: Context): Boolean {
        if (enteredOtp == generatedOtp) {
            viewModelScope.launch {
                savedUser?.let {
                    // Aquí necesitaríamos el documentId, que no tenemos.
                    // Lo ideal sería volver a buscar al usuario para obtenerlo.
                    // Por simplicidad del flujo, usaremos el idNumber como userId temporal
                    val session = UserSession(userId = it.idNumber, fullName = it.fullName, idNumber = it.idNumber, role = it.role)
                    Log.i("session", "SessionState save from login: ${session.toString()}")
                    SessionManager(context).saveUserSession(session)
                }
            }
            return true
        }
        return false
    }

    /**
     * Resetea el estado de la UI para volver al estado inicial.
     */
    fun resetState() {
        _uiState.value = LoginUiState.Idle
        generatedOtp = null
        savedUser = null
    }
}
