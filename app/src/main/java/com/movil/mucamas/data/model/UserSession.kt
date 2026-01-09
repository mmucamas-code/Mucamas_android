package com.movil.mucamas.data.model

data class UserSession(
    val userId: String,
    val fullName: String,
    val idNumber: String,
    val role: String
){
    override fun toString(): String {
        return """
            Usuario: $fullName
            ID: $userId
            Documento: $idNumber
            Rol: $role
        """.trimIndent()
    }
}

sealed class SessionResult {
    object Loading : SessionResult()
    data class Success(val user: UserSession) : SessionResult()
    object Empty : SessionResult()
}
