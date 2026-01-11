package com.movil.mucamas.ui.viewmodels


import Collaborator
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.movil.mucamas.data.SessionManager
import com.movil.mucamas.data.SessionProvider
import com.movil.mucamas.ui.models.Reservation
import com.movil.mucamas.ui.models.ReservationStatus
import com.movil.mucamas.ui.repositories.ReservationRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import java.util.Calendar

sealed interface ReservationUiState {
    object Idle : ReservationUiState
    object Loading : ReservationUiState
    data class ReservationCreated(val reservationId: String) : ReservationUiState
    data class ShowAvailabilityAlert(
        val nextCollaborator: Collaborator,
        val suggestedStartTime: String,
        val originalReservation: Reservation
    ) : ReservationUiState
    data class Error(val message: String) : ReservationUiState
}

class ReservationViewModel(
    private val reservationRepository: ReservationRepository = ReservationRepository()
) : ViewModel() {

    private val _uiState = MutableStateFlow<ReservationUiState>(ReservationUiState.Idle)
    val uiState: StateFlow<ReservationUiState> = _uiState.asStateFlow()

    private val sessionManager = SessionProvider.get()

    fun createReservation(reservation: Reservation, serviceDurationMinutes: Int) {
        viewModelScope.launch {
            _uiState.value = ReservationUiState.Loading

            val session = sessionManager.userSessionFlow
                .first { it != null }

            if (session == null) {
                _uiState.value = ReservationUiState.Error("Sesión no disponible.")
                return@launch
            }

            val finalReservation = reservation.copy(
                clientId = session.userId,
                endTime = calculateEndTime(reservation.startTime, serviceDurationMinutes)
            )

            val availableCollaborator = reservationRepository.findAvailableCollaborator(
                date = finalReservation.date,
                startTime = finalReservation.startTime
            )

            if (availableCollaborator != null) {
                assignCollaboratorAndCreate(finalReservation, availableCollaborator.userId)
            } else {
                handleNoAvailableCollaborator(finalReservation)
            }
        }
    }

    fun createReservationForNextAvailable(
        baseReservation: Reservation, 
        nextCollaborator: Collaborator, 
        suggestedStartTime: String, 
        serviceDurationMinutes: Int
    ) {
        viewModelScope.launch {
            _uiState.value = ReservationUiState.Loading
            val newReservation = baseReservation.copy(
                startTime = suggestedStartTime,
                endTime = calculateEndTime(suggestedStartTime, serviceDurationMinutes)
            )
            assignCollaboratorAndCreate(newReservation, nextCollaborator.userId)
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

    private fun handleNoAvailableCollaborator(originalReservation: Reservation) {
        viewModelScope.launch {
            reservationRepository.getNextAvailableCollaborator().collect { nextCollaborator ->
                if (nextCollaborator != null) {
                    // TODO: Implementar lógica para calcular la hora sugerida real basada en nextCollaborator.availableAt
                    val suggestedTime = "18:00" 
                    _uiState.value = ReservationUiState.ShowAvailabilityAlert(nextCollaborator, suggestedTime, originalReservation)
                } else {
                    _uiState.value = ReservationUiState.Error("No hay colaboradores disponibles en este momento. Inténtalo más tarde.")
                }
            }
        }
    }

    private fun calculateEndTime(startTime: String, durationMinutes: Int): String {
        val calendar = Calendar.getInstance()
        val timeParts = startTime.split(":")
        if (timeParts.size == 2) {
            try {
                calendar.set(Calendar.HOUR_OF_DAY, timeParts[0].toInt())
                calendar.set(Calendar.MINUTE, timeParts[1].toInt())
                calendar.add(Calendar.MINUTE, durationMinutes)
                val hour = calendar.get(Calendar.HOUR_OF_DAY)
                val minute = calendar.get(Calendar.MINUTE)
                return String.format("%02d:%02d", hour, minute)
            } catch (e: NumberFormatException) {
                // Manejar error si el formato de startTime no es válido
                return ""
            }
        } 
        return ""
    }

    fun resetState() {
        _uiState.value = ReservationUiState.Idle
    }
}