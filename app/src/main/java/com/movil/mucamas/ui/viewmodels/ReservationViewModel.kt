package com.movil.mucamas.ui.viewmodels

import android.net.Uri
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.google.firebase.storage.FirebaseStorage
import com.movil.mucamas.data.SessionProvider
import com.movil.mucamas.data.model.UserSession
import com.movil.mucamas.ui.models.Address
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
import java.util.Calendar

data class ReservationUiState(
    val isLoading: Boolean = false,
    val reservations: List<Reservation> = emptyList(),
    val isEmpty: Boolean = false,
    val availableCollaborators: List<UserDto> = emptyList(),
    val addressHistory: List<Address> = emptyList() // Added
)

sealed interface ReservationUiEvent {
    data class ShowError(val message: String) : ReservationUiEvent
    data class ReservationCreated(val reservationId: String) : ReservationUiEvent
    object ReservationRated : ReservationUiEvent
    object ReservationUpdated : ReservationUiEvent
    object ShowCollaboratorSelector : ReservationUiEvent
    data class ShowSuccess(val message: String) : ReservationUiEvent // Added
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
    private val storage = FirebaseStorage.getInstance() // Added
    var uploadedReceiptUrl by mutableStateOf<String?>(null) // Added

    init {
        loadReservations()
        loadAddressHistory() // Added
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

    // --- ADDED FUNCTIONS ---

    fun loadAddressHistory() {
        viewModelScope.launch {
            try {
                val session = _userSession.value ?: return@launch
                // Assuming repository has this method for a clean architecture
                val history = reservationRepository.getAddressHistoryForUser(session.userId)
                _uiState.update { it.copy(addressHistory = history) }
            } catch (e: Exception) {
                _eventFlow.emit(ReservationUiEvent.ShowError("Error al cargar historial: ${e.message}"))
            }
        }
    }

    fun uploadReceiptAndHoldUrl(uri: Uri) {
        viewModelScope.launch {
            _uiState.update { it.copy(isLoading = true) }
            uploadedReceiptUrl = null // Reset before new upload
            try {
                val session = _userSession.value ?: throw IllegalStateException("Usuario no ha iniciado sesión")
                val receiptRef = storage.reference.child("payment_receipts/${session.userId}/${System.currentTimeMillis()}")
                val downloadUrl = receiptRef.putFile(uri).await().storage.downloadUrl.await().toString()
                uploadedReceiptUrl = downloadUrl
                _eventFlow.emit(ReservationUiEvent.ShowSuccess("Comprobante subido con éxito"))
            } catch (e: Exception) {
                _eventFlow.emit(ReservationUiEvent.ShowError("Error al subir comprobante: ${e.message}"))
            } finally {
                _uiState.update { it.copy(isLoading = false) }
            }
        }
    }

    // --- EXISTING FUNCTIONS (UNCHANGED) ---

    fun createReservation(reservation: Reservation, serviceDurationMinutes: Int) {
        viewModelScope.launch {
            _uiState.update { it.copy(isLoading = true) }
            try {
                val session = _userSession.value ?: throw IllegalStateException("Sesión no disponible.")

                val newReservationStub = reservation.copy(
                    clientId = session.userId,
                    endTime = calculateEndTime(reservation.startTime, serviceDurationMinutes)
                )

                val availableCollaborator = collaboratorRepository.findAndLockAvailableCollaborator()

                val finalReservation: Reservation
                if (availableCollaborator != null) {
                    finalReservation = newReservationStub.copy(
                        collaboratorId = availableCollaborator.idNumber,
                        status = ReservationStatus.PENDING_PAYMENT
                    )
                } else {
                    finalReservation = newReservationStub.copy(status = ReservationStatus.PENDING_ASSIGNMENT)
                }

                val reservationId = reservationRepository.createReservation(finalReservation)

                if (availableCollaborator != null) {
                    collaboratorRepository.setCollaboratorReservationId(availableCollaborator.idNumber, reservationId)
                }

                _eventFlow.emit(ReservationUiEvent.ReservationCreated(reservationId))

            } catch (e: Exception) {
                _eventFlow.emit(ReservationUiEvent.ShowError(e.message ?: "Error creando la reserva."))
            } finally {
                _uiState.update { it.copy(isLoading = false) }
            }
        }
    }

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
