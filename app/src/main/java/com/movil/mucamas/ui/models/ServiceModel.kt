package com.movil.mucamas.ui.models

data class Service(
    var id: String? = null,
    val nombre: String = "",
    val icono: String = "",
    val descripcion: String = "",
    val precio: Long = 0,
    val activo: Boolean = true,
    val duracionMinutos: Int = 0
){
    fun updateId(newId: String?) { id = newId }

    override fun toString(): String {
        return """
            id: $id
            nombre: $nombre
            icono: $icono
            descripcion: $descripcion
            precio: $precio
            activo: $activo
            duracionMinutos: $duracionMinutos
        """.trimIndent()
    }
}
