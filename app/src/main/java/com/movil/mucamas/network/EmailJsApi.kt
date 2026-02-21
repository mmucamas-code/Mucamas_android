package com.movil.mucamas.network

import com.movil.mucamas.data.model.EmailJsRequest
import retrofit2.Response
import retrofit2.http.Body
import retrofit2.http.POST

interface EmailJsApi {
    @POST("v1/email") // Ajusta según la URL real de EmailJS
    suspend fun sendEmail(@Body payload: EmailJsRequest): Response<Unit>
}