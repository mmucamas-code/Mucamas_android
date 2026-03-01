package com.movil.mucamas.ui.repositories

import com.google.firebase.firestore.FieldValue
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.Query
import com.movil.mucamas.ui.models.Address
import com.movil.mucamas.ui.models.Reservation
import com.movil.mucamas.ui.models.ReservationRating
import com.movil.mucamas.ui.models.ReservationStatus
import com.movil.mucamas.ui.models.UserRole
import kotlinx.coroutines.channels.awaitClose
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.callbackFlow
import kotlinx.coroutines.tasks.await
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Date
import java.util.Locale

class ReservationRepository(
    private val serviceRepository: ServiceRepository,
    private val collaboratorRepository: CollaboratorRepository
) {
    private val firestore = FirebaseFirestore.getInstance()
    private val reservations = firestore.collection("reservations")

    suspend fun createReservation(reservation: Reservation): String {
        val service = serviceRepository.getServiceById(reservation.serviceId)
            ?: throw Exception("Service not found")

        val startTime = Calendar.getInstance().apply {
            time = Date() // O la fecha/hora de inicio que elijas
        }

        val endTime = startTime.apply {
            add(Calendar.MINUTE, service.duracionMinutos)
        }

        val doc = reservations.document()
        doc.set(
            reservation.copy(
                id = doc.id,
                createdAt = Date(),
                updatedAt = Date(),
                endTime = endTime.time,
                duracionMinutos = service.duracionMinutos
            )
        ).await()
        return doc.id
    }

    fun getReservations(
        userId: String, 
        role: UserRole, 
        onlyToday: Boolean = false,
        statusFilter: ReservationStatus? = null
    ): Flow<List<Reservation>> = callbackFlow {
        var query: Query = reservations

        query = when (role) {
            UserRole.CLIENT -> query.whereEqualTo("clientId", userId)
            UserRole.COLLABORATOR -> query
                .whereEqualTo("collaboratorId", userId)
                .whereIn(
                    "status", listOf(
                        ReservationStatus.CONFIRMED.name,
                        ReservationStatus.IN_PROGRESS.name,
                        ReservationStatus.COMPLETED.name
                    )
                )
            UserRole.ADMIN -> query
        }

        if (onlyToday) {
            val today = SimpleDateFormat("dd/MM/yyyy", Locale.getDefault()).format(Date())
            query = query.whereEqualTo("date", today)
        }

        if (statusFilter != null) {
            query = query.whereEqualTo("status", statusFilter.name)
        }

        val listener = query.orderBy("createdAt", Query.Direction.DESCENDING)
            .addSnapshotListener { snapshot, error ->
                if (error != null) {
                    close(error)
                    return@addSnapshotListener
                }
                if (snapshot != null) {
                    val reservations = snapshot.toObjects(Reservation::class.java)
                    trySend(reservations).isSuccess
                }
            }
        awaitClose { listener.remove() }
    }

    suspend fun updateStatus(reservationId: String, status: ReservationStatus) {
        reservations.document(reservationId).update("status", status.name, "updatedAt", Date()).await()
    }

    suspend fun updatePaymentStatus(reservationId: String, receiptUrl: String?) {
        val updates = mutableMapOf<String, Any>(
            "status" to ReservationStatus.PENDING_CONFIRMATION.name,
            "updatedAt" to Date()
        )
        receiptUrl?.let { updates["paymentReceiptUrl"] = it }
        reservations.document(reservationId).update(updates).await()
    }

    suspend fun assignCollaborator(reservationId: String, collaboratorId: String) {
        val reservation = reservations.document(reservationId).get().await().toObject(Reservation::class.java)
            ?: throw Exception("Reservation not found")

        val endTime = Calendar.getInstance().apply {
            time = reservation.createdAt ?: Date()
            add(Calendar.MINUTE, reservation.duracionMinutos)
        }

        collaboratorRepository.assignReservationToCollaborator(collaboratorId, reservationId, endTime.timeInMillis)

        reservations.document(reservationId).update(
            mapOf(
                "collaboratorId" to collaboratorId,
                "status" to ReservationStatus.PENDING_PAYMENT.name,
                "updatedAt" to Date()
            )
        ).await()
    }

    suspend fun rateReservation(reservationId: String, rating: ReservationRating) {
        val reservationRef = reservations.document(reservationId)
        firestore.runTransaction { transaction ->
            val snapshot = transaction.get(reservationRef)
            val reservation = snapshot.toObject(Reservation::class.java)
                ?: throw Exception("Reservation not found")

            if (reservation.status != ReservationStatus.COMPLETED) {
                throw Exception("Reservation not completed")
            }

            val existingRating = reservation.ratings.find { it.role == rating.role }
            if (existingRating != null) {
                return@runTransaction
            }

            transaction.update(reservationRef, "ratings", FieldValue.arrayUnion(rating))
            
            // Si es la calificación del cliente, actualizamos el rating del colaborador
            if (rating.role == UserRole.CLIENT && reservation.collaboratorId != null) {
                // Esto requeriría actualizar el rating del colaborador en su documento
                // Se podría manejar con un Cloud Function o aquí mismo si tenemos acceso
            }
        }.await()
    }

    suspend fun getAddressHistoryForUser(userId: String): List<Address> {
        val snapshot = reservations
            .whereEqualTo("clientId", userId)
            .orderBy("createdAt", Query.Direction.DESCENDING)
            .limit(5)
            .get()
            .await()

        return snapshot.toObjects(Reservation::class.java)
            .mapNotNull { it.address }
            .distinctBy { it.fullAddress }
    }
}
