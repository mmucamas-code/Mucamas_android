package com.movil.mucamas.ui.models

import com.google.firebase.firestore.DocumentId

data class Collaborator(
    @DocumentId
    val id: String = "",
    val userId: String = "",
    val isAvailable: Boolean = true,
    val currentReservationId: String? = null,
    val availableAt: Long? = null, // Timestamp for when they will be free
    val lastUpdatedAt: Long = 0L
)
