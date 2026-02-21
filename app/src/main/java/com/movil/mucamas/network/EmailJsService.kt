package com.movil.mucamas.network

import android.util.Log
import com.movil.mucamas.config.EmailJsConfig
import com.movil.mucamas.data.model.EmailJsRequest
import com.movil.mucamas.data.model.TemplateParams
import okhttp3.OkHttpClient
import okhttp3.logging.HttpLoggingInterceptor
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory

object EmailJsService {

    private const val BASE_URL = "https://api.mailersend.com/"

    // Configuración del Logcat para ver las peticiones
    private val logging = HttpLoggingInterceptor().apply {
        level = HttpLoggingInterceptor.Level.BODY
    }

    private val client = OkHttpClient.Builder()
        .addInterceptor(logging)
        .build()

    // Inicialización de Retrofit
    private val retrofitApi: EmailJsApi by lazy {
        Retrofit.Builder()
            .baseUrl(BASE_URL)
            .client(client)
            .addConverterFactory(GsonConverterFactory.create())
            .build()
            .create(EmailJsApi::class.java)
    }

    /**
     * Envía un OTP al email especificado usando la API de EmailJS.
     */
    suspend fun sendOtpEmail(email: String, otp: String): Boolean {
        val payload = EmailJsRequest(
            service_id = EmailJsConfig.SERVICE_ID,
            template_id = EmailJsConfig.TEMPLATE_ID,
            user_id = EmailJsConfig.API_TOKEN,
            template_params = TemplateParams(email, otp,"")
        )

        return try {
            val response = retrofitApi.sendEmail(payload)

            if (response.isSuccessful) {
                Log.d("EmailJsService", "Email enviado con éxito")
                true
            } else {
                // Como QA, aquí verás el error exacto (ej. 401 si el user_id está mal)
                Log.e("EmailJsService", "Error en la respuesta: ${response.code()} - ${response.errorBody()?.string()}")
                false
            }
        } catch (e: Exception) {
            Log.e("EmailJsService", "Fallo crítico en la petición", e)
            false
        }
    }
}
