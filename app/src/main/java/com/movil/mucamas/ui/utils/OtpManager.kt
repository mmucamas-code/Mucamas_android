package com.movil.mucamas.ui.utils

import android.app.NotificationChannel
import android.app.NotificationManager
import android.content.Context
import android.os.Build
import android.util.Log
import androidx.core.app.NotificationCompat
import com.movil.mucamas.R

object OtpManager {

    private const val OTP_CHANNEL_ID = "otp_channel"

    fun generateAndNotifyOtp(context: Context): String {
        // 1. Generar OTP
        val otp = (1000..9999).random().toString()
        Log.d("OtpManager", "Generated OTP: $otp")

        // 2. Crear canal de notificación (si no existe)
        createNotificationChannel(context)

        // 3. Crear y mostrar notificación
        val notificationManager = context.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager

        val notification = NotificationCompat.Builder(context, OTP_CHANNEL_ID)
            .setSmallIcon(R.drawable.ic_launcher_foreground) // Reemplaza con tu ícono
            .setContentTitle("Código de Verificación")
            .setContentText("Tu código de acceso es: $otp")
            .setPriority(NotificationCompat.PRIORITY_HIGH)
            .setAutoCancel(true)
            .build()

        notificationManager.notify(1, notification)

        return otp
    }

    private fun createNotificationChannel(context: Context) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val name = "OTP Verification"
            val descriptionText = "Channel for OTP delivery"
            val importance = NotificationManager.IMPORTANCE_HIGH
            val channel = NotificationChannel(OTP_CHANNEL_ID, name, importance).apply {
                description = descriptionText
            }
            val notificationManager: NotificationManager =
                context.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
            notificationManager.createNotificationChannel(channel)
        }
    }
}
