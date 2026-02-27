package com.movil.mucamas.ui.repositories

import android.util.Log
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.Query
import com.movil.mucamas.ui.models.Collaborator
import com.movil.mucamas.ui.models.UserDto
import com.movil.mucamas.ui.models.UserRole
import kotlinx.coroutines.tasks.await

class CollaboratorRepository {

    private val firestore = FirebaseFirestore.getInstance()
    private val usersCollection = firestore.collection("users")
    private val collaboratorsCollection = firestore.collection("collaborators")

    suspend fun findAndLockAvailableCollaborator(): Collaborator? {
        val candidatesQuery = collaboratorsCollection
            .whereEqualTo("isAvailable", true)
            .orderBy("lastUpdatedAt", Query.Direction.ASCENDING)
            .limit(1)
            .get()
            .await()

        if (candidatesQuery.isEmpty) return null

        val doc = candidatesQuery.documents.first()

        return try {
            firestore.runTransaction { transaction ->
                val freshDoc = transaction.get(doc.reference)
                if (freshDoc.exists() && freshDoc.getBoolean("isAvailable") == true) {
                    val collaboratorObject = freshDoc.toObject(Collaborator::class.java)
                    if (collaboratorObject != null) {
                        val now = System.currentTimeMillis()
                        transaction.update(doc.reference, "isAvailable", false, "lastUpdatedAt", now)
                        collaboratorObject.copy(isAvailable = false, lastUpdatedAt = now)
                    } else {
                        null
                    }
                } else {
                    null
                }
            }.await()
        } catch (e: Exception) {
            Log.e("FirestoreError", "Error en transacción findAndLock: ${e.message}")
            null
        }
    }

    suspend fun getAllCollaborators(): List<Pair<UserDto, Collaborator?>> {
        val usersSnapshot = usersCollection.whereEqualTo("role", UserRole.COLLABORATOR.name).get().await()
        val users = usersSnapshot.toObjects(UserDto::class.java)

        val collaboratorDetails = collaboratorsCollection.get().await().toObjects(Collaborator::class.java)
        val detailsMap = collaboratorDetails.associateBy { it.userId }

        return users.map { user ->
            Pair(user, detailsMap[user.idNumber])
        }
    }

    suspend fun setCollaboratorAvailability(collaboratorId: String, isAvailable: Boolean) {
        val updates = mutableMapOf<String, Any?>(
            "isAvailable" to isAvailable,
            "currentReservationId" to null,
            "lastUpdatedAt" to System.currentTimeMillis()
        )
        if (isAvailable) {
            updates["availableAt"] = null
        }

        collaboratorsCollection.document(collaboratorId).update(updates).await()
    }

    suspend fun assignReservationToCollaborator(collaboratorId: String, reservationId: String, availableAt: Long) {
        val collaboratorDocRef = collaboratorsCollection.document(collaboratorId)
        val updates = mapOf(
            "isAvailable" to false,
            "currentReservationId" to reservationId,
            "availableAt" to availableAt,
            "lastUpdatedAt" to System.currentTimeMillis()
        )
        collaboratorDocRef.update(updates).await()
    }

    suspend fun setCollaboratorReservationId(collaboratorId: String, reservationId: String) {
        collaboratorsCollection.document(collaboratorId).update(
            "currentReservationId", reservationId,
            "lastUpdatedAt", System.currentTimeMillis()
        ).await()
    }
}
