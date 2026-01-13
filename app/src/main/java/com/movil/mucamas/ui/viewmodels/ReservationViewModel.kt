package com.movil.mucamas.ui.viewmodels

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.movil.mucamas.data.SessionProvider
import com.movil.mucamas.data.model.UserSession
import com.movil.mucamas.ui.models.Reservation
import com.movil.mucamas.ui.models.ReservationRating
import com.movil.mucamas.ui.models.ReservationStatus
import com.movil.mucamas.ui.models.UserRole
import com.movil.mucamas.ui.repositories.ReservationRepository
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import java.util.Calendar

data class ReservationUiState(
    val isLoading: Boolean = false,
    val reservations: List<Reservation> = emptyList(),
    val isEmpty: Boolean = false
)

sealed interface ReservationUiEvent {
    data class ShowError(val message: String) : ReservationUiEvent
    data class ReservationCreated(val reservationId: String) : ReservationUiEvent
    object ReservationRated : ReservationUiEvent
    object ReservationUpdated : ReservationUiEvent
}

class ReservationViewModel(
    private val reservationRepository: ReservationRepository = ReservationRepository()
) : ViewModel() {

    private val _uiState = MutableStateFlow(ReservationUiState())
    val uiState = _uiState.asStateFlow()

    private val _eventFlow = MutableSharedFlow<ReservationUiEvent>()
    val eventFlow = _eventFlow.asSharedFlow()

    private val _userSession = MutableStateFlow<UserSession?>(null)
    val userSession = _userSession.asStateFlow()

    private val sessionManager = SessionProvider.get()

    init {
        loadReservations()
    }

    private fun loadReservations() {
        viewModelScope.launch {
            _uiState.update { it.copy(isLoading = true) }
            sessionManager.userSessionFlow.collect { userSession ->
                _userSession.value = userSession
                if (userSession != null) {
                    reservationRepository.getReservations(userSession.userId, userSession.role)
                        .catch { e -> _eventFlow.emit(ReservationUiEvent.ShowError("Error al cargar las reservas: ${e.message}")) }
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
            try {
                val session = _userSession.value ?: throw IllegalStateException("Sesión no disponible.")

                var newReservation = reservation.copy(
                    clientId = session.userId,
                    endTime = calculateEndTime(reservation.startTime, serviceDurationMinutes)
                )

                val availableCollaborator = reservationRepository.findAvailableCollaborator()
                newReservation = if (availableCollaborator != null) {
                    newReservation.copy(
                        collaboratorId = availableCollaborator.userId,
                        status = ReservationStatus.PENDING_PAYMENT
                    )
                } else {
                    newReservation.copy(status = ReservationStatus.PENDING_ASSIGNMENT)
                }

                val reservationId = reservationRepository.createReservation(newReservation)
                _eventFlow.emit(ReservationUiEvent.ReservationCreated(reservationId))
            } catch (e: Exception) {
                _eventFlow.emit(ReservationUiEvent.ShowError(e.message ?: "Error creando la reserva."))
            } finally {
                _uiState.update { it.copy(isLoading = false) }
            }
        }
    }

    fun assignAvailableCollaborator(reservationId: String) {
        viewModelScope.launch {
            if (_userSession.value?.role != UserRole.ADMIN) {
                _eventFlow.emit(ReservationUiEvent.ShowError("No tienes permisos para asignar un colaborador."))
                return@launch
            }
            _uiState.update { it.copy(isLoading = true) }
            try {
                val collaborator = reservationRepository.findAvailableCollaborator()
                if (collaborator != null) {
                    reservationRepository.assignCollaborator(reservationId, collaborator.userId)
                    _eventFlow.emit(ReservationUiEvent.ReservationUpdated)
                } else {
                    _eventFlow.emit(ReservationUiEvent.ShowError("No hay colaboradores disponibles para asignar."))
                }
            } catch (e: Exception) {
                _eventFlow.emit(ReservationUiEvent.ShowError(e.message ?: "Error asignando colaborador."))
            } finally {
                _uiState.update { it.copy(isLoading = false) }
            }
        }
    }

    fun processPayment(reservationId: String) {
        updateReservationStatus(reservationId, ReservationStatus.PENDING_CONFIRMATION, listOf(UserRole.CLIENT))
    }

    fun confirmReservation(reservationId: String) {
        updateReservationStatus(reservationId, ReservationStatus.CONFIRMED, listOf(UserRole.ADMIN))
    }

    fun startReservation(reservationId: String) {
        updateReservationStatus(reservationId, ReservationStatus.IN_PROGRESS, listOf(UserRole.COLLABORATOR))
    }

    fun completeReservation(reservationId: String) {
        updateReservationStatus(reservationId, ReservationStatus.COMPLETED, listOf(UserRole.COLLABORATOR))
    }

    fun cancelReservation(reservationId: String) {
        updateReservationStatus(reservationId, ReservationStatus.CANCELLED, listOf(UserRole.CLIENT, UserRole.ADMIN))
    }

    private fun updateReservationStatus(reservationId: String, newStatus: ReservationStatus, allowedRoles: List<UserRole>) {
        viewModelScope.launch {
            val userSession = _userSession.value
            if (userSession == null || userSession.role !in allowedRoles) {
                _eventFlow.emit(ReservationUiEvent.ShowError("No tienes permisos para realizar esta acción."))
                return@launch
            }

            _uiState.update { it.copy(isLoading = true) }
            try {
                reservationRepository.updateStatus(reservationId, newStatus)
                _eventFlow.emit(ReservationUiEvent.ReservationUpdated)
            } catch (e: Exception) {
                _eventFlow.emit(ReservationUiEvent.ShowError(e.message ?: "Error actualizando la reserva."))
            } finally {
                _uiState.update { it.copy(isLoading = false) }
            }
        }
    }

    fun rateReservation(reservationId: String, score: Int, comment: String) {
        viewModelScope.launch {
            _uiState.update { it.copy(isLoading = true) }
            try {
                val userSession = _userSession.value ?: throw IllegalStateException("User not logged in")
                val reservation = _uiState.value.reservations.find { it.id == reservationId } ?: throw IllegalStateException("Reservation not found")

                if (reservation.status != ReservationStatus.COMPLETED) {
                    _eventFlow.emit(ReservationUiEvent.ShowError("Solo puedes calificar reservas completadas."))
                    return@launch
                }
                if (reservation.ratings.any { it.role == userSession.role }) {
                    _eventFlow.emit(ReservationUiEvent.ShowError("Ya has calificado esta reserva."))
                    return@launch
                }

                val rating = ReservationRating(
                    userId = userSession.userId,
                    role = userSession.role,
                    score = score,
                    comment = comment,
                    createdAt = System.currentTimeMillis()
                )
                reservationRepository.rateReservation(reservationId, rating)
                _eventFlow.emit(ReservationUiEvent.ReservationRated)
            } catch (e: Exception) {
                _eventFlow.emit(ReservationUiEvent.ShowError(e.message ?: "Error al calificar."))
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
            } catch (e: NumberFormatException) { return "" }
        }
        return ""
    }
}
