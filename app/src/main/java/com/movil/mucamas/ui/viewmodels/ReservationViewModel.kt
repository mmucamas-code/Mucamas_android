package com.movil.mucamas.ui.viewmodels

import Collaborator
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.movil.mucamas.data.SessionProvider
import com.movil.mucamas.ui.models.Reservation
import com.movil.mucamas.ui.models.ReservationRating
import com.movil.mucamas.ui.models.ReservationStatus
import com.movil.mucamas.ui.repositories.ReservationRepository
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import java.util.Calendar
import java.util.Date

data class ReservationUiState(
    val isLoading: Boolean = false,
    val reservations: List<Reservation> = emptyList(),
    val isEmpty: Boolean = false
)

sealed interface ReservationUiEvent {
    data class ShowError(val message: String) : ReservationUiEvent
    data class ReservationCreated(val reservationId: String) : ReservationUiEvent
    object ReservationRated : ReservationUiEvent
    data class ShowAvailabilityAlert(
        val nextCollaborator: Collaborator,
        val suggestedStartTime: String,
        val originalReservation: Reservation
    ) : ReservationUiEvent
}

class ReservationViewModel(
    private val reservationRepository: ReservationRepository = ReservationRepository()
) : ViewModel() {

    private val _uiState = MutableStateFlow(ReservationUiState())
    val uiState = _uiState.asStateFlow()

    private val _eventFlow = MutableSharedFlow<ReservationUiEvent>()
    val eventFlow = _eventFlow.asSharedFlow()

    private val sessionManager = SessionProvider.get()

    init {
        loadReservations()
    }

    private fun loadReservations() {
        viewModelScope.launch {
            _uiState.update { it.copy(isLoading = true) }
            sessionManager.userSessionFlow.collect { userSession ->
                if (userSession != null) {
                    reservationRepository.getReservationsByUserId(userSession.userId)
                        .catch { e ->
                            _eventFlow.emit(ReservationUiEvent.ShowError("Error al cargar las reservas: ${e.message}"))
                            _uiState.update { it.copy(isLoading = false) }
                        }
                        .collect { reservations ->
                            _uiState.update {
                                it.copy(
                                    isLoading = false,
                                    reservations = reservations,
                                    isEmpty = reservations.isEmpty()
                                )
                            }
                        }
                } else {
                    _eventFlow.emit(ReservationUiEvent.ShowError("No se pudo obtener la sesión del usuario."))
                    _uiState.update { it.copy(isLoading = false, isEmpty = true) }
                }
            }
        }
    }

    fun createReservation(reservation: Reservation, serviceDurationMinutes: Int) {
        viewModelScope.launch {
            _uiState.update { it.copy(isLoading = true) }

            val session = sessionManager.userSessionFlow.first()

            if (session == null) {
                _eventFlow.emit(ReservationUiEvent.ShowError("Sesión no disponible."))
                _uiState.update { it.copy(isLoading = false) }
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
            _uiState.update { it.copy(isLoading = true) }
            val newReservation = baseReservation.copy(
                startTime = suggestedStartTime,
                endTime = calculateEndTime(suggestedStartTime, serviceDurationMinutes)
            )
            assignCollaboratorAndCreate(newReservation, nextCollaborator.userId)
        }
    }

    private suspend fun assignCollaboratorAndCreate(
        reservation: Reservation,
        collaboratorId: String
    ) {
        try {
            val reservationWithCollaborator = reservation.copy(
                collaboratorId = collaboratorId,
                status = ReservationStatus.PENDING_PAYMENT
            )
            val reservationId = reservationRepository.createReservation(reservationWithCollaborator)
            reservationRepository.assignCollaborator(reservationId, collaboratorId)
            _eventFlow.emit(ReservationUiEvent.ReservationCreated(reservationId))
        } catch (e: Exception) {
            _eventFlow.emit(ReservationUiEvent.ShowError("Error al asignar colaborador y crear reserva: ${e.message}"))
        } finally {
            _uiState.update { it.copy(isLoading = false) }
        }
    }

    private fun handleNoAvailableCollaborator(originalReservation: Reservation) {
        viewModelScope.launch {
            try {
                val nextCollaborator = reservationRepository.getNextAvailableCollaborator().first()
                if (nextCollaborator != null) {
                    // TODO: Implementar lógica para calcular la hora sugerida real basada en nextCollaborator.availableAt
                    val suggestedTime = "18:00"
                    _eventFlow.emit(
                        ReservationUiEvent.ShowAvailabilityAlert(
                            nextCollaborator,
                            suggestedTime,
                            originalReservation
                        )
                    )
                } else {
                    _eventFlow.emit(ReservationUiEvent.ShowError("No hay colaboradores disponibles en este momento. Inténtalo más tarde."))
                }
            } catch (e: Exception) {
                _eventFlow.emit(ReservationUiEvent.ShowError("Error buscando disponibilidad: ${e.message}"))
            } finally {
                _uiState.update { it.copy(isLoading = false) }
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

    fun rateReservation(reservationId: String, score: Int, comment: String) {
        viewModelScope.launch {
            _uiState.update { it.copy(isLoading = true) }

            try {
                val userSession = sessionManager.userSessionFlow.first()
                if (userSession == null) {
                    _eventFlow.emit(ReservationUiEvent.ShowError("User not logged in"))
                    _uiState.update { it.copy(isLoading = false) }
                    return@launch
                }

                val rating = ReservationRating(
                    userId = userSession.userId,
                    role = userSession.role,
                    score = score,
                    comment = comment,
                    createdAt = Date()
                )

                reservationRepository.rateReservation(reservationId, rating)
                _eventFlow.emit(ReservationUiEvent.ReservationRated)

            } catch (e: Exception) {
                _eventFlow.emit(ReservationUiEvent.ShowError(e.message ?: "Unknown error"))
            } finally {
                _uiState.update { it.copy(isLoading = false) }
            }
        }
    }

    fun resetState() {
        _uiState.value = ReservationUiState()
    }
}
