package com.movil.mucamas.ui.repositories

import com.google.firebase.firestore.FieldValue
import com.google.firebase.firestore.FirebaseFirestore
import com.movil.mucamas.data.model.OtpData
import com.movil.mucamas.ui.models.UserAddress
import com.movil.mucamas.ui.models.UserDto
import kotlinx.coroutines.channels.awaitClose
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.callbackFlow
import kotlinx.coroutines.tasks.await

class UserRepository {

    private val firestore = FirebaseFirestore.getInstance()
    private val collection = firestore.collection("users")

    suspend fun saveUser(user: UserDto): Result<String> {
        return try {
            val documentReference = collection.add(user).await()
            Result.success(documentReference.id)
        } catch (e: Exception) {
            e.printStackTrace()
            Result.failure(e)
        }
    }

    suspend fun findUserByIdNumber(idNumber: String): Result<UserDto?> {
        return try {
            val querySnapshot = collection
                .whereEqualTo("idNumber", idNumber)
                .limit(1)
                .get()
                .await()

            if (querySnapshot.isEmpty) {
                Result.success(null)
            } else {
                val user = querySnapshot.documents.first().toObject(UserDto::class.java)
                Result.success(user)
            }
        } catch (e: Exception) {
            e.printStackTrace()
            Result.failure(e)
        }
    }

    fun getUserData(documentId: String): Flow<UserDto?> = callbackFlow {
        val listener = collection.document(documentId).addSnapshotListener { snapshot, error ->
            if (error != null) {
                close(error)
                return@addSnapshotListener
            }
            trySend(snapshot?.toObject(UserDto::class.java)).isSuccess
        }
        awaitClose { listener.remove() }
    }

    // --- CRUD de Direcciones ---
    
    fun getUserAddresses(documentId: String): Flow<List<UserAddress>> = callbackFlow {
        val listener = collection.document(documentId).collection("addresses")
            .addSnapshotListener { snapshot, error ->
                if (error != null) {
                    close(error)
                    return@addSnapshotListener
                }
                val addresses = snapshot?.toObjects(UserAddress::class.java) ?: emptyList()
                trySend(addresses).isSuccess
            }
        awaitClose { listener.remove() }
    }

    suspend fun addAddress(documentId: String, address: UserAddress) {
        collection.document(documentId).collection("addresses").add(address).await()
    }

    suspend fun updateAddress(documentId: String, address: UserAddress) {
        collection.document(documentId).collection("addresses").document(address.id).set(address).await()
    }

    suspend fun deleteAddress(documentId: String, addressId: String) {
        collection.document(documentId).collection("addresses").document(addressId).delete().await()
    }

    suspend fun setDefaultAddress(documentId: String, addressId: String) {
        val batch = firestore.batch()
        val addressesRef = collection.document(documentId).collection("addresses")
        
        val all = addressesRef.get().await()
        all.documents.forEach { doc ->
            batch.update(doc.reference, "isDefault", false)
        }
        batch.update(addressesRef.document(addressId), "isDefault", true)
        batch.commit().await()
    }

    // --- OTP y Otros ---

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

    suspend fun resetOtpAttempts(documentId: String): Result<Boolean> {
        return try {
            collection.document(documentId)
                .update(
                    "otp.attempts", 0,
                    "otp.lockedUntil", null
                )
                .await()
            Result.success(true)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

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

    suspend fun clearOtpData(documentId: String): Result<Boolean> {
        return try {
            collection.document(documentId)
                .update("otp", FieldValue.delete())
                .await()
            Result.success(true)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
}
