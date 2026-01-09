package com.movil.mucamas.ui.viewmodels

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.movil.mucamas.ui.models.Service
import com.movil.mucamas.ui.repositories.ServiceRepository
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn

// Estados de la UI para la lista de servicios
sealed interface ServicesUiState {
    data object Loading : ServicesUiState
    data class Success(val services: List<Service>) : ServicesUiState
    data object Empty : ServicesUiState // Para cuando la lista viene vacía
    data class Error(val message: String) : ServicesUiState
}

class HomeViewModel(
    private val repository: ServiceRepository = ServiceRepository()
) : ViewModel() {

    // Exponemos el estado de la UI como un StateFlow
    val servicesUiState: StateFlow<ServicesUiState> = repository.getActiveServicesStream()
        .map { services ->
            if (services.isEmpty()) {
                ServicesUiState.Empty
            } else {
                ServicesUiState.Success(services)
            }
        }
        .catch { e ->
            // En caso de un error en el Flow, emitimos el estado de Error
            emit(ServicesUiState.Error(e.message ?: "Error desconocido"))
        }
        .stateIn(
            scope = viewModelScope,
            // El Flow se mantiene activo mientras la UI esté visible (Started)
            // y se detiene 5 segundos después para ahorrar recursos (WhileSubscribed).
            // Firestore gestiona la cancelación del listener automáticamente.
            started = SharingStarted.WhileSubscribed(5000L),
            initialValue = ServicesUiState.Loading // Estado inicial mientras se conecta
        )
}
