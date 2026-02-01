package com.movil.mucamas.network

import android.util.Log
import com.movil.mucamas.config.EmailJsConfig
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import org.json.JSONObject
import java.io.OutputStreamWriter
import java.net.HttpURLConnection
import java.net.URL

object EmailJsService {

    private const val API_URL = "https://api.emailjs.com/api/v1.0/email/send"

    /**
     * Envía un OTP al email especificado usando la API de EmailJS.
     * Debe ser llamada desde una corrutina.
     *
     * @param email La dirección de email del destinatario.
     * @param otp El código OTP a enviar.
     * @return true si el email se envió correctamente, false en caso contrario.
     */
    suspend fun sendOtpEmail(email: String, otp: String): Boolean {
        if (EmailJsConfig.SERVICE_ID.contains("YOUR_")) {
            Log.e("EmailJsService", "Credenciales de EmailJS no configuradas.")
            return false
        }

        return withContext(Dispatchers.IO) {
            try {
                val url = URL(API_URL)
                val conn = url.openConnection() as HttpURLConnection
                conn.requestMethod = "POST"
                conn.setRequestProperty("Content-Type", "application/json")
                conn.doOutput = true

                val templateParams = JSONObject().apply {
                    put("to_email", email)
                    put("otp_code", otp)
                }
                val jsonPayload = JSONObject().apply {
                    put("service_id", EmailJsConfig.SERVICE_ID)
                    put("template_id", EmailJsConfig.TEMPLATE_ID)
                    put("user_id", EmailJsConfig.PUBLIC_KEY)
                    put("template_params", templateParams)
                }

                OutputStreamWriter(conn.outputStream).use { it.write(jsonPayload.toString()) }

                val responseCode = conn.responseCode
                Log.d("EmailJsService", "EmailJS Response: $responseCode")
                responseCode == HttpURLConnection.HTTP_OK
            } catch (e: Exception) {
                Log.e("EmailJsService", "Error enviando email con EmailJS", e)
                false
            }
        }
    }
}
