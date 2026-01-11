package com.movil.mucamas.ui.viewmodels

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.google.firebase.firestore.FirebaseFirestore
import com.movil.mucamas.ui.models.Service
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch
import kotlinx.coroutines.tasks.await

sealed class ServicesUiState {
    object Loading : ServicesUiState()
    data class Success(val services: List<Service>) : ServicesUiState()
    data class Error(val message: String) : ServicesUiState()
    object Empty : ServicesUiState()
}

class HomeViewModel : ViewModel() {

    private val _servicesUiState = MutableStateFlow<ServicesUiState>(ServicesUiState.Loading)
    val servicesUiState: StateFlow<ServicesUiState> = _servicesUiState

    init {
        fetchServices()
    }

    fun refreshServices() {
        fetchServices()
    }

    private fun fetchServices() {
        viewModelScope.launch {
            try {
                val snapshot = FirebaseFirestore.getInstance().collection("services").get().await()
                if (snapshot.isEmpty) {
                    _servicesUiState.value = ServicesUiState.Empty
                } else {
                    val services = snapshot.toObjects(Service::class.java)
                    _servicesUiState.value = ServicesUiState.Success(services)
                }
            } catch (e: Exception) {
                _servicesUiState.value = ServicesUiState.Error("Error al cargar los servicios: ${e.message}")
            }
        }
    }
}
