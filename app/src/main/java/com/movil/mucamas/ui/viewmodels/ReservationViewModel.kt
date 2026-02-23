package com.movil.mucamas.ui.viewmodels

import android.net.Uri
import android.util.Log
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.Query
import com.movil.mucamas.data.SessionProvider
import com.movil.mucamas.data.model.UserSession
import com.movil.mucamas.ui.models.Address
import com.movil.mucamas.ui.models.PaymentMethod
import com.movil.mucamas.ui.models.Reservation
import com.movil.mucamas.ui.models.ReservationRating
import com.movil.mucamas.ui.models.ReservationStatus
import com.movil.mucamas.ui.models.UserDto
import com.movil.mucamas.ui.models.UserRole
import com.movil.mucamas.ui.repositories.CollaboratorRepository
import com.movil.mucamas.ui.repositories.ReservationRepository
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import kotlinx.coroutines.tasks.await
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Locale

data class ReservationUiState(
    val isLoading: Boolean = false,
    val reservations: List<Reservation> = emptyList(),
    val isEmpty: Boolean = false,
    val availableCollaborators: List<UserDto> = emptyList(),
    val addressHistory: List<Address> = emptyList(),
    val isCollaboratorAvailable: Boolean? = null,
    val estimatedAvailability: String? = null
)

sealed interface ReservationUiEvent {
    data class ShowError(val message: String) : ReservationUiEvent
    data class ReservationCreated(val reservationId: String, val reservationData: Reservation) : ReservationUiEvent
    object ReservationRated : ReservationUiEvent
    object ReservationUpdated : ReservationUiEvent
    object ShowCollaboratorSelector : ReservationUiEvent
}

class ReservationViewModel(
    private val reservationRepository: ReservationRepository = ReservationRepository(),
    private val collaboratorRepository: CollaboratorRepository = CollaboratorRepository()
) : ViewModel() {

    private val _uiState = MutableStateFlow(ReservationUiState())
    val uiState = _uiState.asStateFlow()

    private val _eventFlow = MutableSharedFlow<ReservationUiEvent>()
    val eventFlow = _eventFlow.asSharedFlow()

    private val _userSession = MutableStateFlow<UserSession?>(null)
    val userSession = _userSession.asStateFlow()

    private val sessionManager = SessionProvider.get()
    private val db = FirebaseFirestore.getInstance()

    init {
        loadReservations()
        loadAddressHistory()
    }

    fun checkAvailability() {
        viewModelScope.launch {
            _uiState.update { it.copy(isLoading = true, isCollaboratorAvailable = null, estimatedAvailability = null) }
            try {
                // Read-only query for availability
                val availableNowQuery = db.collection("collaborators").whereEqualTo("isAvailable", true).limit(1).get().await()
                if (!availableNowQuery.isEmpty) {
                    _uiState.update { it.copy(isCollaboratorAvailable = true, isLoading = false) }
                } else {
                    // Find next available if no one is free now
                    val nextAvailableQuery = db.collection("collaborators").orderBy("availableAt", Query.Direction.ASCENDING).limit(1).get().await()
                    if (!nextAvailableQuery.isEmpty) {
                        val availableAtTimestamp = nextAvailableQuery.documents.first().getTimestamp("availableAt")?.toDate()
                        val estimatedTime = availableAtTimestamp?.let { SimpleDateFormat("hh:mm a", Locale.getDefault()).format(it) }
                        _uiState.update { it.copy(isCollaboratorAvailable = false, estimatedAvailability = estimatedTime, isLoading = false) }
                    } else {
                        _uiState.update { it.copy(isCollaboratorAvailable = false, isLoading = false) } // No collaborators exist
                    }
                }
            } catch (e: Exception) {
                _eventFlow.emit(ReservationUiEvent.ShowError("Error al verificar disponibilidad: ${e.message}"))
                _uiState.update { it.copy(isLoading = false, isCollaboratorAvailable = false) }
            }
        }
    }

    private fun loadReservations() {
        viewModelScope.launch {
            sessionManager.userSessionFlow.collect { userSession ->
                _userSession.value = userSession
                if (userSession != null) {
                    reservationRepository.getReservations(userSession.userId, userSession.role)
                        .catch { e -> _eventFlow.emit(ReservationUiEvent.ShowError("Error al cargar las reservas: ${e.message}")) }
                        .collect { reservations -> _uiState.update { it.copy(reservations = reservations, isEmpty = reservations.isEmpty()) } }
                } else {
                    _uiState.update { it.copy(isEmpty = true) }
                }
            }
        }
    }

    fun loadAddressHistory() {
        viewModelScope.launch {
            try {
                val session = _userSession.value ?: return@launch
                _uiState.update { it.copy(addressHistory = reservationRepository.getAddressHistoryForUser(session.userId)) }
            } catch (e: Exception) {
                _eventFlow.emit(ReservationUiEvent.ShowError("Error al cargar historial: ${e.message}"))
            }
        }
    }

    fun createReservation(reservation: Reservation, serviceDurationMinutes: Int) {
        viewModelScope.launch {
            _uiState.update { it.copy(isLoading = true) }
            try {
                val session = _userSession.value ?: throw IllegalStateException("Sesión no disponible.")

                var finalReservation = reservation.copy(
                    clientId = session.userId,
                    clientName = session.fullName,
                    endTime = calculateEndTime(reservation.startTime, serviceDurationMinutes)
                )

                // The definitive lock and assignment happens here, and only here.
                val availableCollaborator = collaboratorRepository.findAndLockAvailableCollaborator()

                Log.d("DEBUG_COLLAB", "Documentos encontrados: ${availableCollaborator?.userId}")

                if (availableCollaborator != null) {
                    finalReservation = finalReservation.copy(
                        collaboratorId = availableCollaborator.userId, // Correctly assign the collaborator's userId
                        status = if (finalReservation.paymentMethod == PaymentMethod.TRANSFER) ReservationStatus.PENDING_CONFIRMATION else ReservationStatus.PENDING_CONFIRMATION
                    )
                } else {
                    finalReservation = finalReservation.copy(
                        collaboratorId = null, // Ensure collaboratorId is null
                        status = ReservationStatus.PENDING_ASSIGNMENT
                    )
                }

                val reservationId = reservationRepository.createReservation(finalReservation)

                // Set the reservation ID on the collaborator, which should also handle setting isAvailable to false
                if (availableCollaborator != null) {
                    collaboratorRepository.setCollaboratorReservationId(availableCollaborator.userId, reservationId)
                }

                _eventFlow.emit(ReservationUiEvent.ReservationCreated(reservationId, finalReservation))

            } catch (e: Exception) {
                _eventFlow.emit(ReservationUiEvent.ShowError(e.message ?: "Error creando la reserva."))
            } finally {
                _uiState.update { it.copy(isLoading = false) }
            }
        }
    }
    
    // --- OTHER UNCHANGED FUNCTIONS ---
    fun onAssignCollaboratorClicked() {
        viewModelScope.launch {
            if (_userSession.value?.role != UserRole.ADMIN) return@launch

            _uiState.update { it.copy(isLoading = true) }
            try {
                val collaborators = collaboratorRepository.getAvailableCollaboratorsForAdmin()
                if (collaborators.isNotEmpty()) {
                    _uiState.update { it.copy(availableCollaborators = collaborators) }
                    _eventFlow.emit(ReservationUiEvent.ShowCollaboratorSelector)
                } else {
                    _eventFlow.emit(ReservationUiEvent.ShowError("No hay colaboradores disponibles."))
                }
            } catch (e: Exception) {
                _eventFlow.emit(ReservationUiEvent.ShowError("Error al buscar colaboradores: ${e.message}"))
            } finally {
                _uiState.update { it.copy(isLoading = false) }
            }
        }
    }

    fun assignCollaboratorToReservation(reservationId: String, collaboratorId: String) {
        viewModelScope.launch {
            if (_userSession.value?.role != UserRole.ADMIN) return@launch
            _uiState.update { it.copy(isLoading = true) }
            try {
                reservationRepository.assignCollaborator(reservationId, collaboratorId)
                collaboratorRepository.assignReservationToCollaborator(collaboratorId, reservationId)
                _eventFlow.emit(ReservationUiEvent.ReservationUpdated)
            } catch (e: Exception) {
                _eventFlow.emit(ReservationUiEvent.ShowError(e.message ?: "Error asignando colaborador."))
            } finally {
                _uiState.update { it.copy(isLoading = false) }
            }
        }
    }

    fun processPayment(reservationId: String) {
        updateReservationStatus(reservationId, ReservationStatus.PENDING_CONFIRMATION)
    }

    fun confirmReservation(reservationId: String) {
        updateReservationStatus(reservationId, ReservationStatus.CONFIRMED)
    }

    fun startReservation(reservationId: String) {
        updateReservationStatus(reservationId, ReservationStatus.IN_PROGRESS)
    }

    fun completeReservation(reservation: Reservation) {
        viewModelScope.launch {
            updateReservationStatus(reservation.id, ReservationStatus.COMPLETED)
            reservation.collaboratorId?.let { collaboratorRepository.setCollaboratorAvailability(it, true) }
        }
    }

    fun cancelReservation(reservation: Reservation) {
        viewModelScope.launch {
            updateReservationStatus(reservation.id, ReservationStatus.CANCELLED)
            if (reservation.status != ReservationStatus.PENDING_ASSIGNMENT) {
                reservation.collaboratorId?.let { collaboratorRepository.setCollaboratorAvailability(it, true) }
            }
        }
    }

    private fun updateReservationStatus(reservationId: String, newStatus: ReservationStatus) {
        viewModelScope.launch {
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
                    return@launch // No action if already rated
                }

                val rating = ReservationRating(userId = userSession.userId, role = userSession.role, score = score, comment = comment, createdAt = System.currentTimeMillis())
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
