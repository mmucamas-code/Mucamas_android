package com.movil.mucamas.domain.usecase

import android.content.Context
import android.util.Log
import com.movil.mucamas.network.EmailJsService
import com.movil.mucamas.ui.repositories.UserRepository
import com.movil.mucamas.ui.utils.OtpManager

sealed class StartOtpFlowResult {
    object Success : StartOtpFlowResult()
    sealed class Error(val message: String) : StartOtpFlowResult() {
        object UserNotFound : Error("El usuario no existe en la base de datos.")
        object OtpGenerationError : Error("No se pudo generar y guardar el código OTP.")
        object EmailSendError : Error("No se pudo enviar el correo con el código.")
        // 1. Corregido para aceptar cualquier tipo de Throwable
        data class UnknownError(val exception: Throwable) : Error("Ocurrió un error inesperado.")
    }
}

class StartOtpFlowUseCase(
    private val userRepository: UserRepository = UserRepository() // Inyectamos el repositorio
) {

    suspend operator fun invoke(context: Context,userId: String): StartOtpFlowResult {
        return try {
            // Paso 1: Verificar que el usuario existe usando el repositorio
            val userResult = userRepository.findUserByIdNumber(userId)

            // 2. Bloque de fallo corregido
            if (userResult.isFailure) {
                // Si isFailure es true, exceptionOrNull() no será null.
                return StartOtpFlowResult.Error.UnknownError(userResult.exceptionOrNull()!!)
            }

            val user = userResult.getOrNull()
            if (user == null || user.email.isBlank()) {
                return StartOtpFlowResult.Error.UserNotFound
            }

            // Paso 2: Generar y guardar el OTP en Firestore
            // El idNumber (userId) sigue siendo la clave para el documento OTP
            val generatedOtp = OtpManager.generateAndSaveOtp(user.documentId)
            if (generatedOtp.isBlank()) {
                return StartOtpFlowResult.Error.OtpGenerationError
            }

            // TODO:  Paso 3 -> Enviar el OTP por email usando EmailJS (PENDING)
            //val emailSent = EmailJsService.sendOtpEmail(user.email, generatedOtp)
            //if (!emailSent) {
            //    return StartOtpFlowResult.Error.EmailSendError
            //}
            OtpManager.generateAndNotifyOtp(context = context,generatedOtp)

            StartOtpFlowResult.Success
        } catch (e: Exception) {
            Log.e("StartOtpFlowUseCase", "Error en el flujo de OTP", e)
            StartOtpFlowResult.Error.UnknownError(e)
        }
    }
}
