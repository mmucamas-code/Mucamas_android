package com.movil.mucamas.ui.viewmodels

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.movil.mucamas.ui.models.Reservation
import com.movil.mucamas.ui.models.ReservationStatus
import com.movil.mucamas.ui.repositories.ReservationRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

sealed interface ReservationUiState {
    object Idle : ReservationUiState
    object Loading : ReservationUiState
    data class ReservationCreated(val reservationId: String) : ReservationUiState
    data class ShowAvailabilityAlert(val availableAt: String) : ReservationUiState
    data class Error(val message: String) : ReservationUiState
}

class ReservationViewModel(
    private val reservationRepository: ReservationRepository = ReservationRepository()
) : ViewModel() {

    private val _uiState = MutableStateFlow<ReservationUiState>(ReservationUiState.Idle)
    val uiState: StateFlow<ReservationUiState> = _uiState.asStateFlow()

    fun createReservation(reservation: Reservation) {
        viewModelScope.launch {
            _uiState.value = ReservationUiState.Loading

            val availableCollaborator = reservationRepository.findAvailableCollaborator(
                date = reservation.date,
                startTime = reservation.startTime
            )

            if (availableCollaborator != null) {
                assignCollaboratorAndCreate(reservation, availableCollaborator.userId)
            } else {
                handleNoAvailableCollaborator(reservation)
            }
        }
    }

    fun proceedWithReservation(reservation: Reservation) {
        viewModelScope.launch {
            try {
                val reservationId = reservationRepository.createReservation(reservation)
                _uiState.value = ReservationUiState.ReservationCreated(reservationId)
            } catch (e: Exception) {
                _uiState.value = ReservationUiState.Error("Error al crear la reserva: ${e.message}")
            }
        }
    }

    private suspend fun assignCollaboratorAndCreate(reservation: Reservation, collaboratorId: String) {
        val reservationWithCollaborator = reservation.copy(
            collaboratorId = collaboratorId,
            status = ReservationStatus.PENDING_PAYMENT
        )
        try {
            val reservationId = reservationRepository.createReservation(reservationWithCollaborator)
            reservationRepository.assignCollaborator(reservationId, collaboratorId)
            _uiState.value = ReservationUiState.ReservationCreated(reservationId)
        } catch (e: Exception) {
            _uiState.value = ReservationUiState.Error("Error al asignar colaborador y crear reserva: ${e.message}")
        }
    }

    private fun handleNoAvailableCollaborator(reservation: Reservation) {
        viewModelScope.launch {
            reservationRepository.getNextAvailableCollaborator().collect { nextCollaborator ->
                if (nextCollaborator != null) {
                    // val suggestedTime = calculateSuggestedTime(nextCollaborator) // Asumiendo que tienes una función para calcular esto
                    // val formattedTime = formatTime(suggestedTime) // Y una para formatear
                    _uiState.value = ReservationUiState.ShowAvailabilityAlert("ahora") // TODO: format time
                } else {
                    // Si no hay ninguno disponible próximamente, crear la reserva pendiente
                    proceedWithReservation(reservation)
                }
            }
        }
    }
}