package com.movil.mucamas.ui.utils

import com.google.firebase.firestore.FieldValue
import android.app.NotificationChannel
import android.app.NotificationManager
import android.content.Context
import android.os.Build
import android.util.Log
import androidx.core.app.NotificationCompat
import com.google.firebase.firestore.ktx.firestore
import com.google.firebase.ktx.Firebase
import com.movil.mucamas.R
import com.movil.mucamas.data.model.OtpData
import kotlinx.coroutines.tasks.await
import java.util.concurrent.TimeUnit

sealed class OtpVerificationResult {
    object Success : OtpVerificationResult()
    sealed class Error(val message: String) : OtpVerificationResult() {
        object InvalidCode : Error("El código ingresado es incorrecto.")
        object Expired : Error("El código ha expirado.")
        object MaxAttemptsReached : Error("Se ha superado el número de intentos.")
        object OtpNotFound : Error("No se encontró un código OTP para este usuario.")
        data class FirestoreError(val exception: Exception) : Error("Ocurrió un error inesperado.")
    }
}

object OtpManager {

    private const val OTP_CHANNEL_ID = "otp_channel"

    fun generateAndNotifyOtp(context: Context): String {
        // 1. Generar OTP
        val otp = generateOtp()
        Log.d("OtpManager", "Generated OTP: $otp")

        // 2. Crear canal de notificación (si no existe)
        createNotificationChannel(context)

        // 3. Crear y mostrar notificación
        val notificationManager = context.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager

        val notification = NotificationCompat.Builder(context, OTP_CHANNEL_ID)
            .setSmallIcon(R.drawable.ic_launcher_foreground) // Reemplaza con tu ícono
            .setContentTitle("Código de Verificación")
            .setContentText("Tu código de acceso es: $otp")
            .setPriority(NotificationCompat.PRIORITY_HIGH)
            .setAutoCancel(true)
            .build()

        notificationManager.notify(1, notification)

        return otp
    }

    private fun createNotificationChannel(context: Context) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val name = "OTP Verification"
            val descriptionText = "Channel for OTP delivery"
            val importance = NotificationManager.IMPORTANCE_HIGH
            val channel = NotificationChannel(OTP_CHANNEL_ID, name, importance).apply {
                description = descriptionText
            }
            val notificationManager: NotificationManager =
                context.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
            notificationManager.createNotificationChannel(channel)
        }
    }

    fun generateOtp(): String {
        return (1000..9999).random().toString()
    }

    suspend fun generateAndSaveOtp(userId: String): String {
        val otp = generateOtp()
        val expiresAt = System.currentTimeMillis() + TimeUnit.MINUTES.toMillis(5)
        val otpData = OtpData(
            code = otp,
            expiresAt = expiresAt,
            attempts = 0
        )

        Firebase.firestore.collection("users").document(userId)
            .update("otp", otpData)
            .await()

        return otp
    }

    suspend fun verifyOtp(userId: String, enteredOtp: String): OtpVerificationResult {
        val userDocRef = Firebase.firestore.collection("users").document(userId)

        try {
            val document = userDocRef.get().await()
            val otpMap = document.get("otp") as? Map<String, Any>
                ?: return OtpVerificationResult.Error.OtpNotFound

            val otpData = OtpData(
                code = otpMap["code"] as? String ?: "",
                expiresAt = otpMap["expiresAt"] as? Long ?: 0L,
                attempts = (otpMap["attempts"] as? Long)?.toInt() ?: 0
            )

            if (otpData.attempts >= 3) {
                return OtpVerificationResult.Error.MaxAttemptsReached
            }

            if (System.currentTimeMillis() > otpData.expiresAt) {
                return OtpVerificationResult.Error.Expired
            }

            return if (otpData.code == enteredOtp) {
                OtpVerificationResult.Success
            } else {
                userDocRef.update("otp.attempts", otpData.attempts + 1).await()
                OtpVerificationResult.Error.InvalidCode
            }

        } catch (e: Exception) {
            Log.e("OtpManager", "Error verifying OTP", e)
            return OtpVerificationResult.Error.FirestoreError(e)
        }
    }

    suspend fun deleteOtp(userId: String) {
        try {
            val userDocRef = Firebase.firestore.collection("users").document(userId)
            userDocRef.update("otp", FieldValue.delete()).await()
        } catch (e: Exception) {
            Log.e("OtpManager", "Error deleting OTP for user $userId", e)
            // Opcional: Manejar el error, aunque en este punto la validación ya fue exitosa.
        }
    }
}
