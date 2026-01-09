package com.movil.mucamas.ui.repositories

import Collaborator
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.snapshots
import com.movil.mucamas.ui.models.Reservation
import com.movil.mucamas.ui.models.ReservationStatus
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.tasks.await
import java.util.Date

class ReservationRepository {
    private val firestore = FirebaseFirestore.getInstance()
    private val reservations = firestore.collection("reservations")

    suspend fun createReservation(reservation: Reservation): String {
        val doc = reservations.document()
        doc.set(
            reservation.copy(
                id = doc.id,
                createdAt = Date(),
                updatedAt = Date()
            )
        ).await()
        return doc.id
    }

    fun getReservationsByClient(clientId: String): Flow<List<Reservation>> {
        return reservations
            .whereEqualTo("clientId", clientId)
            .snapshots()
            .map { it.toObjects(Reservation::class.java) }
    }

    suspend fun updateStatus(
        reservationId: String,
        status: ReservationStatus
    ) {
        reservations.document(reservationId)
            .update(
                mapOf(
                    "status" to status.name,
                    "updatedAt" to Date()
                )
            ).await()
    }

    suspend fun assignCollaborator(
        reservationId: String,
        collaboratorId: String
    ) {
        reservations.document(reservationId)
            .update(
                mapOf(
                    "collaboratorId" to collaboratorId,
                    "status" to ReservationStatus.PENDING_PAYMENT.name,
                    "updatedAt" to Date()
                )
            ).await()
    }

    suspend fun findAvailableCollaborator(
        date: String,
        startTime: String
    ): Collaborator? {

        val snapshot = firestore.collection("collaborators")
            .whereEqualTo("isAvailable", true)
            .limit(1)
            .get()
            .await()

        return snapshot.documents.firstOrNull()
            ?.toObject(Collaborator::class.java)
    }

    fun getNextAvailableCollaborator(): Flow<Collaborator?> {
        val now = System.currentTimeMillis()

        return firestore.collection("collaborators")
            .whereEqualTo("isAvailable", false)
            .whereGreaterThan("availableAt", now)
            .orderBy("availableAt")
            .limit(1)
            .snapshots()
            .map { snapshot ->
                snapshot.documents.firstOrNull()?.toObject(Collaborator::class.java)
            }
    }

}
