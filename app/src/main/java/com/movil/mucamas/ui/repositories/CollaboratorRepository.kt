package com.movil.mucamas.ui.repositories

import com.google.firebase.firestore.FirebaseFirestore
import com.movil.mucamas.ui.models.Collaborator
import com.movil.mucamas.ui.models.UserDto
import com.movil.mucamas.ui.models.UserRole
import kotlinx.coroutines.tasks.await

class CollaboratorRepository {

    private val firestore = FirebaseFirestore.getInstance()
    private val usersCollection = firestore.collection("users")
    private val collaboratorsCollection = firestore.collection("collaborators")

    suspend fun getAvailableCollaborators(): List<UserDto> {
        val collaboratorUsers = usersCollection.whereEqualTo("role", UserRole.COLLABORATOR.name).get().await()
        val collaboratorUserIds = collaboratorUsers.map { it.id }

        val busyCollaborators = collaboratorsCollection.whereIn("userId", collaboratorUserIds).whereEqualTo("isAvailable", false).get().await()
        val busyCollaboratorIds = busyCollaborators.map { it.getString("userId") }

        return collaboratorUsers.filter { it.id !in busyCollaboratorIds }.map { it.toObject(UserDto::class.java) }
    }

    suspend fun findAvailableCollaborator(): UserDto? {
        val collaborators = getAvailableCollaborators()
        return collaborators.firstOrNull()
    }

    suspend fun updateCollaboratorStatus(collaboratorId: String, reservationId: String?, isAvailable: Boolean) {
        val collaboratorDocRef = collaboratorsCollection.document(collaboratorId)
        val updates = mapOf(
            "isAvailable" to isAvailable,
            "currentReservationId" to reservationId,
            "lastUpdatedAt" to System.currentTimeMillis(),
            "availableAt" to if (isAvailable) System.currentTimeMillis() else null
        )
        collaboratorDocRef.update(updates).await()
    }

    suspend fun createOrUpdateCollaborator(collaboratorId: String, reservationId: String) {
        val collaboratorDocRef = collaboratorsCollection.document(collaboratorId)
        val docSnapshot = collaboratorDocRef.get().await()

        if (docSnapshot.exists()) {
            updateCollaboratorStatus(collaboratorId, reservationId, false)
        } else {
            val newCollaborator = Collaborator(
                userId = collaboratorId,
                isAvailable = false,
                currentReservationId = reservationId,
                lastUpdatedAt = System.currentTimeMillis()
            )
            collaboratorDocRef.set(newCollaborator).await()
        }
    }
}
