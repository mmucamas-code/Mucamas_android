package com.movil.mucamas.ui.repositories

import Collaborator
import com.google.firebase.firestore.FieldValue
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.Query
import com.google.firebase.firestore.snapshots
import com.movil.mucamas.ui.models.Reservation
import com.movil.mucamas.ui.models.ReservationRating
import com.movil.mucamas.ui.models.ReservationStatus
import com.movil.mucamas.ui.models.UserRole
import kotlinx.coroutines.channels.awaitClose
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.callbackFlow
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

    fun getReservations(userId: String, role: UserRole): Flow<List<Reservation>> = callbackFlow {
        val query = when (role) {
            UserRole.CLIENT -> reservations.whereEqualTo("clientId", userId)
            UserRole.COLLABORATOR -> reservations.whereEqualTo("collaboratorId", userId)
            UserRole.ADMIN -> reservations
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
                throw Exception("Rating already submitted for this role")
            }

            transaction.update(
                reservationRef,
                "ratings",
                FieldValue.arrayUnion(rating)
            )
        }.await()
    }

}
