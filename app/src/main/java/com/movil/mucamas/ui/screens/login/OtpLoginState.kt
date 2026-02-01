package com.movil.mucamas.ui.screens.login

/**
 * Representa los posibles estados de la UI durante el flujo de login con OTP.
 */
sealed class OtpLoginState {
    /**
     * Indica que una operación está en curso (ej: enviando OTP).
     */
    object Loading : OtpLoginState()

    /**
     * El OTP fue generado y enviado correctamente.
     */
    object OtpSent : OtpLoginState()

    /**
     * El ID de usuario ingresado no es válido o no fue encontrado.
     */
    object InvalidId : OtpLoginState()

    /**
     * Ocurrió un error específico al intentar enviar el email.
     */
    object EmailSendError : OtpLoginState()

    /**
     * Ocurrió un error genérico en el flujo.
     * @param message Un mensaje descriptivo del error.
     */
    data class GenericError(val message: String) : OtpLoginState()
}
