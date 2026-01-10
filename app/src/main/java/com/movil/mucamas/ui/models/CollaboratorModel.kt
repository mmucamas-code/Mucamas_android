data class Collaborator(
    val userId: String = "",

    // Disponibilidad rápida para queries
    val isAvailable: Boolean = true,

    // Reserva que está atendiendo actualmente (null = libre)
    val currentReservationId: String? = null,

    // Fecha/hora estimada en la que vuelve a estar disponible
    val availableAt: Long? = null, // Timestamp en millis

    // Auditoría
    val lastUpdatedAt: Long = System.currentTimeMillis()
)