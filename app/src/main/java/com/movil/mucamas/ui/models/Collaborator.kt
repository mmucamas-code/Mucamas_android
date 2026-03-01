package com.movil.mucamas.ui.models

import com.google.firebase.firestore.DocumentId
import com.google.firebase.firestore.PropertyName

data class Collaborator(
    @DocumentId
    val id: String = "",
    
    @get:PropertyName("userId")
    @set:PropertyName("userId")
    var userId: String = "",

    @get:PropertyName("isAvailable")
    @set:PropertyName("isAvailable")
    var isAvailable: Boolean = true,

    @get:PropertyName("currentReservationId")
    @set:PropertyName("currentReservationId")
    var currentReservationId: String? = null,

    @get:PropertyName("availableAt")
    @set:PropertyName("availableAt")
    var availableAt: Long? = null,

    @get:PropertyName("lastUpdatedAt")
    @set:PropertyName("lastUpdatedAt")
    var lastUpdatedAt: Long = 0L
)
