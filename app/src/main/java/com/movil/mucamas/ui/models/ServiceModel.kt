package com.movil.mucamas.ui.models

data class Service(
    val nombre: String = "",
    val icono: String = "",
    val descripcion: String = "",
    val precio: Long = 0,
    val activo: Boolean = true
)
