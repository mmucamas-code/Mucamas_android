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

    /**
     * Finds an available collaborator, locks them transactionally, and returns their User data.
     * This prevents race conditions where two users might be assigned the same collaborator.
     */
    suspend fun findAndLockAvailableCollaborator(): UserDto? {
        // Step 1: Find a few potential candidates outside the transaction.
        val candidatesQuery = collaboratorsCollection
            .whereEqualTo("isAvailable", true)
            .limit(5) // Fetch a few to reduce contention
            .get()
            .await()

        // Step 2: Iterate through candidates and try to lock one transactionally.
        for (doc in candidatesQuery.documents) {
            try {
                val lockedUser = firestore.runTransaction { transaction ->
                    val freshDoc = transaction.get(doc.reference)
                    if (freshDoc.exists() && freshDoc.getBoolean("isAvailable") == true) {
                        // Atomically lock the collaborator
                        transaction.update(doc.reference, "isAvailable", false)
                        val userId = freshDoc.getString("userId")
                        if (userId != null) {
                            val userDoc = transaction.get(usersCollection.document(userId))
                            userDoc.toObject(UserDto::class.java)
                        } else {
                            null
                        }
                    } else {
                        null // Collaborator was already locked
                    }
                }.await()

                if (lockedUser != null) {
                    return lockedUser // Success!
                }
            } catch (e: Exception) {
                // Transaction failed, try next candidate
                continue
            }
        }
        return null // No collaborator could be locked
    }

    /**
     * Sets the reservation ID for a collaborator who has just been assigned a task.
     */
    suspend fun setCollaboratorReservationId(collaboratorId: String, reservationId: String) {
        collaboratorsCollection.document(collaboratorId).update(
            "currentReservationId", reservationId,
            "lastUpdatedAt", System.currentTimeMillis()
        ).await()
    }

    /**
     * Gets the list of collaborators who are available to be manually assigned by an Admin.
     */
    suspend fun getAvailableCollaboratorsForAdmin(): List<UserDto> {
        val allCollaboratorUsers = usersCollection.whereEqualTo("role", UserRole.COLLABORATOR.name).get().await()
        if (allCollaboratorUsers.isEmpty) return emptyList()

        val userIds = allCollaboratorUsers.map { it.id }
        val busyCollaborators = collaboratorsCollection.whereIn("userId", userIds)
            .whereEqualTo("isAvailable", false).get().await()
        val busyIds = busyCollaborators.mapNotNull { it.getString("userId") }

        return allCollaboratorUsers.documents
            .filter { it.id !in busyIds }
            .mapNotNull { it.toObject(UserDto::class.java) }
    }

    /**
     * Updates a collaborator's status, typically to release them after a reservation is completed or cancelled.
     */
    suspend fun setCollaboratorAvailability(collaboratorId: String, isAvailable: Boolean) {
        val updates = mapOf(
            "isAvailable" to isAvailable,
            "currentReservationId" to null, // Always clear the reservation ID when availability changes
            "lastUpdatedAt" to System.currentTimeMillis()
        )
        collaboratorsCollection.document(collaboratorId).update(updates).await()
    }

    /**
     * Assigns a reservation to a collaborator, creating their collaborator document if it's their first time.
     * This is used for manual assignment by an Admin.
     */
    suspend fun assignReservationToCollaborator(collaboratorId: String, reservationId: String) {
        val collaboratorDocRef = collaboratorsCollection.document(collaboratorId)
        val docSnapshot = collaboratorDocRef.get().await()

        if (docSnapshot.exists()) {
            val updates = mapOf(
                "isAvailable" to false,
                "currentReservationId" to reservationId,
                "lastUpdatedAt" to System.currentTimeMillis()
            )
            collaboratorDocRef.update(updates).await()
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
