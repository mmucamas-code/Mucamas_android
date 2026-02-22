package com.movil.mucamas.ui.models

import com.google.firebase.firestore.DocumentId

data class Service(
    @DocumentId
    var id: String = "",
    val nombre: String = "",
    val icono: String = "",
    val descripcion: String = "",
    val precio: Long = 0,
    val activo: Boolean = true,
    val duracionMinutos: Int = 0,
    // --- Nuevos campos para la estructura propuesta ---
    val tipo: String = "",           // Ej: "combo", "individual", "adicional"
    val categoria: String = "",      // Ej: "hogar", "oficina", "mudanza", "comida"
    val esCombo: Boolean = false,    // Para identificar rápidamente si es un paquete
    val caracteristicas: List<String> = emptyList() // El desglose de lo que incluye cada servicio
) {

    override fun toString(): String {
        return """
            id: $id
            nombre: $nombre
            tipo: $tipo
            categoria: $categoria
            precio: $precio
            duracionMinutos: $duracionMinutos
            esCombo: $esCombo
            activo: $activo
            caracteristicas: ${caracteristicas.joinToString(", ")}
        """.trimIndent()
    }
}
