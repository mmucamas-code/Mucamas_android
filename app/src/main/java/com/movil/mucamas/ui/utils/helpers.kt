package com.movil.mucamas.ui.utils

import Collaborator
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.ui.graphics.vector.ImageVector
import com.google.firebase.firestore.FirebaseFirestore
import com.movil.mucamas.ui.models.Service
import java.text.NumberFormat
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

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


// Función para formatear el precio
fun formatCurrencyCOP(value: Long): String {
    val localeCO = Locale("es", "CO")
    val formatter = NumberFormat.getCurrencyInstance(localeCO)
    formatter.maximumFractionDigits = 0
    formatter.minimumFractionDigits = 0
    return formatter.format(value)
}

// NUEVA FUNCIÓN: Formatea la duración en minutos
fun formatDuration(minutes: Int): String {
    if (minutes <= 0) return ""
    val hours = minutes / 60
    val remainingMinutes = minutes % 60
    return when {
        hours > 0 && remainingMinutes > 0 -> "${hours}h ${remainingMinutes}m"
        hours > 0 -> "${hours}h"
        else -> "${remainingMinutes}m"
    }
}

// Función para obtener el icono de un servicio
fun getServiceIcon(iconName: String): ImageVector {
    return Icons.Default.Star
}

fun calculateSuggestedTime(collaborator: Collaborator): Long {
    return collaborator.availableAt
        ?: System.currentTimeMillis()
}

fun formatTime(timestamp: Long): String {
    val formatter = SimpleDateFormat("hh:mm a", Locale.getDefault())
    return formatter.format(Date(timestamp))
}
