package com.movil.mucamas.ui.models

import com.google.firebase.firestore.ServerTimestamp
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
    val city: String = "",
    val neighborhood: String = "",
    val street: String = "",
    val notes: String = ""
)

enum class ReservationStatus {
    PENDING_ASSIGNMENT,
    PENDING_PAYMENT,
    CONFIRMED,
    IN_PROGRESS,
    COMPLETED,
    CANCELLED
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
