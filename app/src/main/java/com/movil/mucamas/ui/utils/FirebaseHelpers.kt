package com.movil.mucamas.ui.utils

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
        return Icons.Default.Star
    }
}