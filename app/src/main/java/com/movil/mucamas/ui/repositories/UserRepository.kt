package com.movil.mucamas.ui.repositories

import com.google.firebase.firestore.FirebaseFirestore
import com.movil.mucamas.data.model.OtpData
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

    /**
     * Busca un usuario por su número de identificación (idNumber).
     * Retorna Result.success con el UserDto si lo encuentra.
     * Retorna Result.success con null si no existe.
     * Retorna Result.failure si ocurre un error.
     */
    suspend fun findUserByIdNumber(idNumber: String): Result<UserDto?> {
        return try {
            val querySnapshot = collection
                .whereEqualTo("idNumber", idNumber)
                .limit(1)
                .get()
                .await()

            if (querySnapshot.isEmpty) {
                Result.success(null) // Usuario no encontrado
            } else {
                // Lo encontramos, lo convertimos al DTO y lo retornamos
                val user = querySnapshot.documents.first().toObject(UserDto::class.java)
                Result.success(user)
            }
        } catch (e: Exception) {
            e.printStackTrace()
            Result.failure(e)
        }
    }

    suspend fun updateUserOtp(documentId: String, otpData: OtpData): Result<Boolean> {
        return try {
            collection.document(documentId)
                .update("otp", otpData)
                .await()
            Result.success(true)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    suspend fun lockOtp(documentId: String, lockDurationMillis: Long): Result<Boolean> {
        return try {
            val unlockTime = System.currentTimeMillis() + lockDurationMillis
            collection.document(documentId)
                .update("otp.lockedUntil", unlockTime)
                .await()
            Result.success(true)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    /**
     * Resetea el contador de intentos y limpia el tiempo de bloqueo.
     */
    suspend fun resetOtpAttempts(documentId: String): Result<Boolean> {
        return try {
            collection.document(documentId)
                .update(
                    "otp.attempts", 0,
                    "otp.lockedUntil", null // Eliminamos el bloqueo
                )
                .await()
            Result.success(true)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    /**
     * Incrementa el contador de intentos fallidos dentro del objeto otp.
     */
    suspend fun incrementOtpAttempts(documentId: String, currentAttempts: Int): Result<Boolean> {
        return try {
            collection.document(documentId)
                .update("otp.attempts", currentAttempts + 1)
                .await()
            Result.success(true)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    /**
     * Elimina el campo 'otp' del documento del usuario.
     */
    suspend fun clearOtpData(documentId: String): Result<Boolean> {
        return try {
            collection.document(documentId)
                .update("otp", com.google.firebase.firestore.FieldValue.delete())
                .await()
            Result.success(true)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
}
