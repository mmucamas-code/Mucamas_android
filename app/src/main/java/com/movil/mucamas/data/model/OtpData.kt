package com.movil.mucamas.data.model

import com.google.firebase.firestore.IgnoreExtraProperties

@IgnoreExtraProperties
data class OtpData(
    val code: String = "",
    val expiresAt: Long = 0L,
    val attempts: Int = 0,
    val lockedUntil: Long? = null // Nuevo campo: timestamp de cuándo se desbloquea
)
