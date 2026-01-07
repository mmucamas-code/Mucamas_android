package com.movil.mucamas.ui.repositories

import com.google.firebase.firestore.FirebaseFirestore
import com.movil.mucamas.ui.models.UserDto
import kotlinx.coroutines.tasks.await

class UserRepository {

    private val firestore = FirebaseFirestore.getInstance()
    private val collection = firestore.collection("users")

    /**
     * Guarda un usuario en Firestore.
     * Retorna Result.success con el ID del documento generado si funciona.
     * Retorna Result.failure si falla.
     */
    suspend fun saveUser(user: UserDto): Result<String> {
        return try {
            // .add() genera el documentId automáticamente
            val documentReference = collection.add(user).await()
            Result.success(documentReference.id)
        } catch (e: Exception) {
            e.printStackTrace()
            Result.failure(e)
        }
    }
}