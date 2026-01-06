package com.movil.mucamas.ui.screens.myreservations

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.DateRange
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.Star
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.movil.mucamas.ui.screens.rate.RateServiceScreen
import com.movil.mucamas.ui.theme.OrangeAccent
import com.movil.mucamas.ui.theme.TurquoiseMain

enum class ReservationStatus(val label: String, val color: Color?) {
    PENDING("Pendiente", OrangeAccent),
    IN_PROGRESS("En Progreso", TurquoiseMain),
    COMPLETED("Completado", Color.Gray)
}

data class Reservation(
    val id: String,
    val serviceName: String,
    val date: String,
    val time: String,
    val status: ReservationStatus,
    val icon: ImageVector
)

@Composable
fun MyReservationsScreen() {
    val reservations = listOf(
        Reservation("1", "Limpieza de Hogar", "15 Nov 2023", "10:00 AM", ReservationStatus.PENDING, Icons.Default.Home),
        Reservation("2", "Cocina", "10 Nov 2023", "02:00 PM", ReservationStatus.COMPLETED, Icons.Default.Star),
        Reservation("3", "Planchado", "18 Nov 2023", "09:00 AM", ReservationStatus.IN_PROGRESS, Icons.Default.DateRange)
    )
    
    var showRateModal by remember { mutableStateOf(false) }
    var selectedServiceToRate by remember { mutableStateOf<Reservation?>(null) }

    Box(modifier = Modifier.fillMaxSize()) {
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .background(Color(0xFFF8F9FA)), // Fondo suave
            contentPadding = PaddingValues(24.dp), // Padding generoso
            verticalArrangement = Arrangement.spacedBy(20.dp) // Mayor separación
        ) {
            item {
                Spacer(modifier = Modifier.height(24.dp))
                Text(
                    text = "Mis Reservas",
                    style = MaterialTheme.typography.headlineMedium.copy(
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.onBackground
                    )
                )
                Spacer(modifier = Modifier.height(16.dp))
            }

            items(reservations) { reservation ->
                ReservationCard(
                    reservation = reservation,
                    onRateClick = {
                        selectedServiceToRate = reservation
                        showRateModal = true
                    }
                )
            }
            
            item {
                 Spacer(modifier = Modifier.height(24.dp))
            }
        }

        if (showRateModal && selectedServiceToRate != null) {
            RateServiceScreen(
                serviceName = selectedServiceToRate!!.serviceName,
                onDismissRequest = { 
                    showRateModal = false
                    selectedServiceToRate = null
                },
                onSubmit = { rating, comment ->
                    // TODO: Handle rating submission
                    showRateModal = false
                    selectedServiceToRate = null
                }
            )
        }
    }
}

@Composable
fun ReservationCard(
    reservation: Reservation,
    onCancelClick: () -> Unit = {},
    onDetailClick: () -> Unit = {},
    onRateClick: () -> Unit = {}
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(24.dp), // Bordes más redondeados
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        Column(
            modifier = Modifier.padding(24.dp) // Mayor padding interno
        ) {
            // Header con Icono y Estado
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Box(
                        modifier = Modifier
                            .size(48.dp) // Icono más grande
                            .background(
                                (reservation.status.color ?: MaterialTheme.colorScheme.outline).copy(alpha = 0.1f), 
                                CircleShape
                            ),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            imageVector = reservation.icon,
                            contentDescription = null,
                            tint = reservation.status.color ?: MaterialTheme.colorScheme.outline,
                            modifier = Modifier.size(24.dp)
                        )
                    }
                    Spacer(modifier = Modifier.width(16.dp))
                    Text(
                        text = reservation.serviceName,
                        style = MaterialTheme.typography.titleLarge, // Texto más grande
                        fontWeight = FontWeight.Bold,
                         color = MaterialTheme.colorScheme.onSurface
                    )
                }
            }

            Spacer(modifier = Modifier.height(12.dp))
            
            // Chip de estado alineado a la izquierda (debajo del título si es largo, o en nueva fila)
             Box(
                modifier = Modifier
                    .background(
                        (reservation.status.color ?: MaterialTheme.colorScheme.outline).copy(alpha = 0.1f), 
                        RoundedCornerShape(8.dp)
                    )
                    .padding(horizontal = 12.dp, vertical = 6.dp)
            ) {
                Text(
                    text = reservation.status.label,
                    color = reservation.status.color ?: MaterialTheme.colorScheme.outline,
                    style = MaterialTheme.typography.labelMedium,
                    fontWeight = FontWeight.Bold
                )
            }

            Spacer(modifier = Modifier.height(20.dp))
            
            // Detalles de Fecha
            Row(verticalAlignment = Alignment.CenterVertically) {
                Icon(
                    Icons.Default.DateRange, 
                    contentDescription = null, 
                    tint = MaterialTheme.colorScheme.onSurfaceVariant,
                    modifier = Modifier.size(20.dp)
                )
                Spacer(modifier = Modifier.width(12.dp))
                Text(
                    text = "${reservation.date} • ${reservation.time}",
                    style = MaterialTheme.typography.bodyLarge, // Texto más legible
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }

            Spacer(modifier = Modifier.height(24.dp))

            // Botones de Acción
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                if (reservation.status == ReservationStatus.PENDING) {
                    OutlinedButton(
                        onClick = onCancelClick,
                        modifier = Modifier
                            .weight(1f)
                            .height(48.dp), // Botón más alto
                        shape = RoundedCornerShape(24.dp),
                        colors = ButtonDefaults.outlinedButtonColors(
                            contentColor = MaterialTheme.colorScheme.error
                        ),
                        border = androidx.compose.foundation.BorderStroke(1.dp, MaterialTheme.colorScheme.error.copy(alpha = 0.5f))
                    ) {
                        Text("Cancelar", fontSize = 14.sp, fontWeight = FontWeight.SemiBold)
                    }
                } else if (reservation.status == ReservationStatus.COMPLETED) {
                    Button(
                        onClick = onRateClick,
                        modifier = Modifier
                            .weight(1f)
                            .height(48.dp),
                        shape = RoundedCornerShape(24.dp),
                        colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.secondary)
                    ) {
                        Text("Calificar", fontSize = 14.sp, fontWeight = FontWeight.SemiBold)
                    }
                }
                
                Button(
                    onClick = onDetailClick,
                    modifier = Modifier
                        .weight(1f)
                        .height(48.dp),
                    shape = RoundedCornerShape(24.dp),
                    colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.primary)
                ) {
                    Text("Ver detalle", fontSize = 14.sp, fontWeight = FontWeight.SemiBold)
                }
            }
        }
    }
}
