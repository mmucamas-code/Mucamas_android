package com.movil.mucamas.ui.models

import androidx.compose.ui.graphics.Color
import com.google.firebase.firestore.ServerTimestamp
import com.movil.mucamas.ui.theme.OrangeAccent
import com.movil.mucamas.ui.theme.TurquoiseMain
import java.util.Date

data class Reservation(
    val id: String = "",
    val clientId: String = "",

    val serviceId: String = "",
    val serviceName: String = "",
    val price: Long = 0,

    val date: String = "",
    val startTime: String = "",
    val endTime: String = "",

    val address: Address = Address(),

    val collaboratorId: String? = null,

    val status: ReservationStatus = ReservationStatus.PENDING_ASSIGNMENT,
    val paymentStatus: PaymentStatus = PaymentStatus.PENDING,
    val paymentMethod: PaymentMethod? = null,

    @ServerTimestamp
    val createdAt: Date? = null,

    @ServerTimestamp
    val updatedAt: Date? = null
)

data class Address(
    val city: String = "Chigorodó",
    val neighborhood: String = "Kennedy",
    val street: String = "Calle 123 #45-67",
    val notes: String = "Servicio adomicilio"
)

enum class ReservationStatus(
    val label: String,
    val color: Color
) {

    PENDING_ASSIGNMENT(
        label = "Pendiente de asignación",
        color = OrangeAccent
    ),

    PENDING_PAYMENT(
        label = "Pendiente de pago",
        color = Color(0xFFFFA726) // naranja suave
    ),

    CONFIRMED(
        label = "Confirmada",
        color = TurquoiseMain
    ),

    IN_PROGRESS(
        label = "En progreso",
        color = Color.Blue
    ),

    COMPLETED(
        label = "Completada",
        color = Color(0xFF4CAF50) // verde éxito
    ),

    CANCELLED(
        label = "Cancelada",
        color = Color(0xFFE53935) // rojo error
    )
}

enum class PaymentStatus {
    PENDING,
    PAID,
    FAILED
}

enum class PaymentMethod {
    CASH,
    CREDIT_CARD,
    DEBIT_CARD
}
