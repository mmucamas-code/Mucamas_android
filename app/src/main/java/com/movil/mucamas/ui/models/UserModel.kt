package com.movil.mucamas.ui.models

import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.ServerTimestamp
import kotlinx.coroutines.tasks.await
import java.util.Date

data class UserDto(
    val idNumber: String = "",
    val fullName: String = "",
    val phone: String = "",
    val email: String = "",
    val mainAddress: String = "",
    val quickRegistration: Boolean = true,

    // @ServerTimestamp le dice a Firestore que use la hora del servidor al escribir
    @ServerTimestamp
    val createdAt: Date? = null,

    @ServerTimestamp
    val lastAccess: Date? = null,

    val verificationStatus: String = "pending",
    val biometricsEnabled: Boolean = false,
    val role: String = "customer",
    val loginMethods: LoginMethodsDto = LoginMethodsDto(),
    val metadata: MetadataDto = MetadataDto()
)

data class LoginMethodsDto(
    val otp: Boolean = false,
    val password: Boolean = false,
    val biometric: Boolean = false
)

data class MetadataDto(
    val version: Int = 1,
    val platform: String = "android"
)