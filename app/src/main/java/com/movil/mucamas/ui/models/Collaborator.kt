package com.movil.mucamas.ui.models


data class Collaborator(
    val userId: String = "",
    val isAvailable: Boolean = true,
    val currentReservationId: String? = null,
    val availableAt: Long? = null, // Timestamp for when they will be free
    val lastUpdatedAt: Long = 0L
)
