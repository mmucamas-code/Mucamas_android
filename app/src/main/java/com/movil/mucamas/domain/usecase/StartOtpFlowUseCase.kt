package com.movil.mucamas.domain.usecase

import android.util.Log
import com.google.firebase.firestore.ktx.firestore
import com.google.firebase.ktx.Firebase
import com.movil.mucamas.network.EmailJsService
import com.movil.mucamas.ui.utils.OtpManager
import kotlinx.coroutines.tasks.await

// 1. Sealed class para representar los resultados del flujo
sealed class StartOtpFlowResult {
    object Success : StartOtpFlowResult()
    sealed class Error(val message: String) : StartOtpFlowResult() {
        object UserNotFound : Error("El usuario no existe en la base de datos.")
        object OtpGenerationError : Error("No se pudo generar y guardar el código OTP.")
        object EmailSendError : Error("No se pudo enviar el correo con el código.")
        data class UnknownError(val exception: Exception) :
            Error("Ocurrió un error inesperado.")
    }
}

// 2. Caso de uso para encapsular la lógica
class StartOtpFlowUseCase {

    /**
     * Orquesta el flujo completo de inicio de sesión con OTP.
     * @param userId El ID del usuario (documento en Firestore).
     * @return Un [StartOtpFlowResult] que indica el éxito o el tipo de error.
     */
    suspend operator fun invoke(userId: String): StartOtpFlowResult {
        return try {
            // Paso 1: Verificar que el usuario existe y obtener su email
            val userDocument = Firebase.firestore.collection("users").document(userId).get().await()
            if (!userDocument.exists()) {
                return StartOtpFlowResult.Error.UserNotFound
            }
            val userEmail = userDocument.getString("email")
            if (userEmail.isNullOrBlank()) {
                // Si el email no existe en el documento, no podemos continuar.
                return StartOtpFlowResult.Error.UserNotFound
            }

            // Paso 2: Generar y guardar el OTP en Firestore
            val generatedOtp = OtpManager.generateAndSaveOtp(userId)
            if (generatedOtp.isBlank()) {
                return StartOtpFlowResult.Error.OtpGenerationError
            }

            // Paso 3: Enviar el OTP por email usando EmailJS
            val emailSent = EmailJsService.sendOtpEmail(userEmail, generatedOtp)
            if (!emailSent) {
                return StartOtpFlowResult.Error.EmailSendError
            }

            StartOtpFlowResult.Success
        } catch (e: Exception) {
            Log.e("StartOtpFlowUseCase", "Error en el flujo de OTP", e)
            StartOtpFlowResult.Error.UnknownError(e)
        }
    }
}
