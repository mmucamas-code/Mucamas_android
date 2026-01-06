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
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.movil.mucamas.ui.theme.OrangeAccent
import com.movil.mucamas.ui.theme.TurquoiseMain

enum class ReservationStatus(val label: String, val color: Color) {
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

    LazyColumn(
        modifier = Modifier
            .fillMaxSize()
            .background(Color(0xFFF8F9FA)), // Fondo suave
        contentPadding = PaddingValues(24.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
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
            ReservationCard(reservation = reservation)
        }
        
        item {
             Spacer(modifier = Modifier.height(24.dp))
        }
    }
}

@Composable
fun ReservationCard(
    reservation: Reservation,
    onCancelClick: () -> Unit = {},
    onDetailClick: () -> Unit = {}
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(20.dp),
        colors = CardDefaults.cardColors(containerColor = Color.White),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        Column(
            modifier = Modifier.padding(20.dp)
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
                            .size(40.dp)
                            .background(reservation.status.color.copy(alpha = 0.1f), CircleShape),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            imageVector = reservation.icon,
                            contentDescription = null,
                            tint = reservation.status.color,
                            modifier = Modifier.size(20.dp)
                        )
                    }
                    Spacer(modifier = Modifier.width(12.dp))
                    Text(
                        text = reservation.serviceName,
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold
                    )
                }
                
                // Chip de estado
                Box(
                    modifier = Modifier
                        .background(reservation.status.color.copy(alpha = 0.1f), RoundedCornerShape(8.dp))
                        .padding(horizontal = 10.dp, vertical = 4.dp)
                ) {
                    Text(
                        text = reservation.status.label,
                        color = reservation.status.color,
                        style = MaterialTheme.typography.labelSmall,
                        fontWeight = FontWeight.Bold
                    )
                }
            }

            Spacer(modifier = Modifier.height(16.dp))
            
            // Detalles de Fecha
            Row(verticalAlignment = Alignment.CenterVertically) {
                Icon(
                    Icons.Default.DateRange, 
                    contentDescription = null, 
                    tint = Color.Gray,
                    modifier = Modifier.size(16.dp)
                )
                Spacer(modifier = Modifier.width(8.dp))
                Text(
                    text = "${reservation.date} • ${reservation.time}",
                    style = MaterialTheme.typography.bodyMedium,
                    color = Color.Gray
                )
            }

            Spacer(modifier = Modifier.height(20.dp))

            // Botones de Acción
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                if (reservation.status == ReservationStatus.PENDING) {
                    OutlinedButton(
                        onClick = onCancelClick,
                        modifier = Modifier
                            .weight(1f)
                            .height(40.dp),
                        shape = RoundedCornerShape(20.dp),
                        colors = ButtonDefaults.outlinedButtonColors(contentColor = Color.Red.copy(alpha = 0.7f))
                    ) {
                        Text("Cancelar", fontSize = 12.sp)
                    }
                }
                
                Button(
                    onClick = onDetailClick,
                    modifier = Modifier
                        .weight(1f)
                        .height(40.dp),
                    shape = RoundedCornerShape(20.dp),
                    colors = ButtonDefaults.buttonColors(containerColor = TurquoiseMain)
                ) {
                    Text("Ver detalle", fontSize = 12.sp)
                }
            }
        }
    }
}
