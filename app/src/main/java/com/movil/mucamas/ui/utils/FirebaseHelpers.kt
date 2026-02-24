package com.movil.mucamas.ui.utils

import android.util.Log
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Star
import androidx.compose.ui.graphics.vector.ImageVector
import com.google.firebase.firestore.FirebaseFirestore
import com.movil.mucamas.ui.models.Service
import kotlin.collections.forEach

object FirebaseHelpers {
    // Función para subir los servicios a Firestore
    fun uploadServicesToFirestore(
        onSuccess: () -> Unit,
        onError: (Exception) -> Unit
    ) {
        val db = FirebaseFirestore.getInstance()
        val services = getSampleServices()

        services.forEach { service ->
            db.collection("services")
                .add(service)
                .addOnFailureListener { e ->
                    onError(e)
                }
        }

        onSuccess()
    }

    // Función para obtener los servicios de ejemplo
    fun getSampleServices() : List<Service> {
        return listOf(
            Service(
                nombre = "Limpieza general del hogar",
                icono = "cleaning",
                descripcion = "Servicio de limpieza básica para mantener tu hogar en orden. Incluye barrido, trapeado, limpieza de polvo, baños y cocina. Se espera que la vivienda esté en condiciones normales, sin acumulación extrema de suciedad.",
                precio = 60000,
                duracionMinutos = 120,
                activo = true
            ),
            // ... (resto de servicios)
        )
    }


    // Función para obtener el icono de un servicio
    fun getServiceIcon(iconName: String): ImageVector {
        // Aseguramos que el nombre empiece por Mayúscula (ej: "home" -> "Home")
        val formattedName = iconName.replaceFirstChar { it.uppercase() }

        return try {
            // Buscamos en el catálogo de Material Icons usando reflexión
            val clazz = Class.forName("androidx.compose.material.icons.filled.${formattedName}Kt")
            val method = clazz.declaredMethods.first { it.name.startsWith("get") }
            method.invoke(null, Icons.Filled) as ImageVector
        } catch (e: Exception) {
            // Log del error para debugging
            Log.e("IconError", "No se encontró el icono: $formattedName, usando respaldo.")
            Icons.Default.Star // Icono de respaldo
        }
    }
}