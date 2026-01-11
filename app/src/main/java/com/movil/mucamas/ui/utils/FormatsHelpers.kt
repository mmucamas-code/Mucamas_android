package com.movil.mucamas.ui.utils

import java.text.NumberFormat
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

object FormatsHelpers {

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


    fun formatTime(timestamp: Long): String {
        val formatter = SimpleDateFormat("hh:mm a", Locale.getDefault())
        return formatter.format(Date(timestamp))
    }
}