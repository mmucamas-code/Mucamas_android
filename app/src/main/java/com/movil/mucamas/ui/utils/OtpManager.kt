package com.movil.mucamas.ui.utils


import android.app.NotificationChannel
import android.app.NotificationManager
import android.content.Context
import android.os.Build
import android.util.Log
import androidx.core.app.NotificationCompat
import com.movil.mucamas.R
import com.movil.mucamas.data.model.OtpData
import com.movil.mucamas.data.model.UserSession
import com.movil.mucamas.ui.repositories.UserRepository
import java.util.concurrent.TimeUnit

sealed class OtpVerificationResult() {
    data class Success(val userSession: UserSession) : OtpVerificationResult()
    sealed class Error(val message: String) : OtpVerificationResult() {
        object InvalidCode : Error("El código ingresado es incorrecto.")
        object Expired : Error("El código ha expirado.")
        object MaxAttemptsReached : Error("Se ha superado el número de intentos.")
        object OtpNotFound : Error("No se encontró un código OTP para este usuario.")
        data class TemporarilyBlocked(val remainingMinutes: Long = 15) : Error("El código ha sido bloqueado temporalmente. Inténtalo de nuevo en $remainingMinutes minutos.")
        object UnBlocked : Error("La validación por OTP ha sido desbloqueada, Intenta de nuevo.")
        data class FirestoreError(val exception: Exception) : Error("Ocurrió un error inesperado.")
    }
}

object OtpManager {

    private const val OTP_CHANNEL_ID = "otp_channel"

    val repository = UserRepository()

    fun generateAndNotifyOtp(context: Context, otp: String): String {
        // 1. Generar OTP
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

    suspend fun generateAndSaveOtp(documentID: String): String {
        val otp = generateOtp()
        val expiresAt = System.currentTimeMillis() + TimeUnit.MINUTES.toMillis(5)
        val otpData = OtpData(
            code = otp,
            expiresAt = expiresAt,
            attempts = 0
        )

        repository.updateUserOtp(documentID, otpData)

        return otp
    }

    suspend fun verifyOtp(userId: String, enteredOtp: String): OtpVerificationResult {
        try {
            //TODO: DEBO ORGANIZAR EL USER para que se traiga por id
            //OBTENEMOS EL USER DOCUMENT
            // Paso 1: Verificar que el usuario existe usando el repositorio
            val userCollectionResult = repository.findUserByIdNumber(userId)

            // 2. Bloque de fallo corregido
            if (userCollectionResult.isFailure) {

                val exception = Exception( userCollectionResult.exceptionOrNull()!!)

                // Si isFailure es true, exceptionOrNull() no será null.
                return OtpVerificationResult.Error.FirestoreError(exception)
            }

            val user = userCollectionResult.getOrNull() ?: return OtpVerificationResult.Error.OtpNotFound

            val otpData = user.otp ?: return OtpVerificationResult.Error.OtpNotFound

            val currentTime = System.currentTimeMillis()

            // 1. Verificar si está bloqueado temporalmente
            otpData.lockedUntil?.let { lockedUntil ->
                if (currentTime < lockedUntil) {
                    val remainingMinutes = TimeUnit.MILLISECONDS.toMinutes(lockedUntil - currentTime) + 1
                    // Podrías crear un nuevo tipo de error: OtpVerificationResult.Error.TemporarilyBlocked(remainingMinutes)
                    return OtpVerificationResult.Error.TemporarilyBlocked(remainingMinutes)
                } else {
                    // ¡IMPORTANTE! El tiempo ya pasó, así que reseteamos los intentos en la DB
                    // para que pueda seguir validando.
                    repository.resetOtpAttempts(user.documentId)

                    return OtpVerificationResult.Error.UnBlocked
                }
            }

            // 2. Verificar intentos máximos (si no estaba bloqueado pero ya llegó al límite)
            if (otpData.attempts >= 3) {
                // Si llega aquí es porque el tiempo de bloqueo anterior ya pasó,
                // pero el contador sigue en 3. Podríamos resetearlo o pedir nuevo OTP.
                return OtpVerificationResult.Error.MaxAttemptsReached
            }

            // 3. Verificar expiración del código
            if (currentTime > otpData.expiresAt) {
                return OtpVerificationResult.Error.Expired
            }

            // 4. Validar el código
            return if (otpData.code == enteredOtp) {
                OtpVerificationResult.Success(user.userSession())
            } else {
                val newAttempts = otpData.attempts + 1
                if (newAttempts >= 3) {
                    // Bloqueo por 15 minutos si falla el 3er intento
                    repository.lockOtp(user.documentId, TimeUnit.MINUTES.toMillis(15))
                    repository.incrementOtpAttempts(user.documentId, otpData.attempts) // para marcar el 3ero
                    OtpVerificationResult.Error.MaxAttemptsReached
                } else {
                    repository.incrementOtpAttempts(user.documentId, otpData.attempts)
                    OtpVerificationResult.Error.InvalidCode
                }
            }

        } catch (e: Exception) {
            Log.e("OtpManager", "Error verifying OTP", e)
            return OtpVerificationResult.Error.FirestoreError(e)
        }
    }

    suspend fun deleteOtp(userId: String) {
        try {
            //TODO: DEBO ORGANIZAR EL USER para que se traiga por id
            //OBTENEMOS EL USER DOCUMENT
            // Paso 1: Verificar que el usuario existe usando el repositorio
            val userCollectionResult = repository.findUserByIdNumber(userId)

            // 2. Bloque de fallo corregido
            if (userCollectionResult.isFailure) {

                val exception = Exception( userCollectionResult.exceptionOrNull()!!)
                Log.e("error",exception.message ?: "Error al obtener el usuario")

                // Si isFailure es true, exceptionOrNull() no será null.
                return
            }

            val user = userCollectionResult.getOrNull() ?: return

            // Usamos la nueva función del repositorio
            val result = repository.clearOtpData(user.documentId)
            if (result.isFailure) {
                Log.e("OtpManager", "Error deleting OTP for user $userId")
            }


        } catch (e: Exception) {
            Log.e("OtpManager", "Error verifying OTP", e)
        }
    }
}
