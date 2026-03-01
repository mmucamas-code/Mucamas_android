package com.movil.mucamas.ui.viewmodels

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.movil.mucamas.data.SessionProvider
import com.movil.mucamas.ui.models.UserAddress
import com.movil.mucamas.ui.models.UserDto
import com.movil.mucamas.ui.repositories.UserRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.firstOrNull
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

data class ProfileUiState(
    val user: UserDto? = null,
    val addresses: List<UserAddress> = emptyList(),
    val isLoading: Boolean = false,
    val error: String? = null
)

class ProfileViewModel(
    private val userRepository: UserRepository = UserRepository()
) : ViewModel() {

    private val _uiState = MutableStateFlow(ProfileUiState())
    val uiState = _uiState.asStateFlow()

    private val sessionManager = SessionProvider.get()

    init {
        loadProfileData()
    }

    private fun loadProfileData() {
        viewModelScope.launch {
            sessionManager.userSessionFlow.collect { session ->
                if (session != null) {
                    _uiState.update { it.copy(isLoading = true) }
                    
                    // Cargar datos de usuario en tiempo real
                    launch {
                        userRepository.getUserData(session.documentId).collect { user ->
                            _uiState.update { it.copy(user = user) }
                        }
                    }

                    // Cargar direcciones en tiempo real
                    launch {
                        userRepository.getUserAddresses(session.documentId).collect { addresses ->
                            _uiState.update { it.copy(addresses = addresses, isLoading = false) }
                        }
                    }
                }
            }
        }
    }

    fun addAddress(address: UserAddress) {
        viewModelScope.launch {
            val session = sessionManager.userSessionFlow.firstOrNull() ?: return@launch
            userRepository.addAddress(session.documentId, address)
        }
    }

    fun updateAddress(address: UserAddress) {
        viewModelScope.launch {
            val session = sessionManager.userSessionFlow.firstOrNull()  ?: return@launch
            userRepository.updateAddress(session.documentId, address)
        }
    }

    fun deleteAddress(addressId: String) {
        viewModelScope.launch {
            val session = sessionManager.userSessionFlow.firstOrNull()  ?: return@launch
            userRepository.deleteAddress(session.documentId, addressId)
        }
    }

    fun setDefaultAddress(addressId: String) {
        viewModelScope.launch {
            val session = sessionManager.userSessionFlow.firstOrNull()  ?: return@launch
            userRepository.setDefaultAddress(session.documentId, addressId)
        }
    }
}
