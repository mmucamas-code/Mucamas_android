package com.movil.mucamas.ui.viewmodels

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.movil.mucamas.ui.models.UserDto
import com.movil.mucamas.ui.repositories.UserRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

// Estados simples para la UI
sealed class RegistrationUiState {
    data object Idle : RegistrationUiState()
    data object Loading : RegistrationUiState()
    data object Success : RegistrationUiState()
    data class Error(val message: String) : RegistrationUiState()
}

class RegisterViewModel(
    // En una app real inyectarías esto. Aquí lo instanciamos directo por simplicidad si no usas DI.
    private val repository: UserRepository = UserRepository()
) : ViewModel() {

    private val _uiState = MutableStateFlow<RegistrationUiState>(RegistrationUiState.Idle)
    val uiState: StateFlow<RegistrationUiState> = _uiState.asStateFlow()

    fun registerUser(
        idNumber: String,
        fullName: String,
        phone: String,
        email: String,
        address: String
    ) {
        _uiState.value = RegistrationUiState.Loading

        // Construimos el objeto con los datos del formulario y los defaults del requerimiento
        val newUser = UserDto(
            idNumber = idNumber,
            fullName = fullName,
            phone = phone,
            email = email,
            mainAddress = address
            // Los demás campos (createdAt, metadata, loginMethods) se llenan solos con los defaults del Data Class
        )

        viewModelScope.launch {
            val result = repository.saveUser(newUser)

            if (result.isSuccess) {
                _uiState.value = RegistrationUiState.Success
                // Aquí podrías loguear el ID: result.getOrNull()
            } else {
                _uiState.value = RegistrationUiState.Error(
                    result.exceptionOrNull()?.message ?: "Error desconocido guardando en Firestore"
                )
            }
        }
    }

    fun resetState() {
        _uiState.value = RegistrationUiState.Idle
    }
}