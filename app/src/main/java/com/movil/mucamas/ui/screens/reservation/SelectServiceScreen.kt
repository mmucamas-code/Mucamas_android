package com.movil.mucamas.ui.screens.reservation

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.Star
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.movil.mucamas.ui.theme.OrangeAccent
import com.movil.mucamas.ui.theme.TurquoiseMain

data class SelectableService(
    val id: String,
    val name: String,
    val description: String,
    val icon: ImageVector,
    val color: Color
)

@Composable
fun SelectServiceScreen(
    onContinueClick: () -> Unit = {}
) {
    var selectedServiceId by remember { mutableStateOf<String?>(null) }

    val mainServices = listOf(
        SelectableService("cleaning", "Limpieza", "Limpieza profunda de hogar", Icons.Default.Home, TurquoiseMain),
        SelectableService("cooking", "Cocina", "Preparación de alimentos", Icons.Default.Star, OrangeAccent)
    )

    val additionalServices = listOf(
        SelectableService("ironing", "Planchado", "Cuidado de ropa", Icons.Default.Add, Color(0xFF6C63FF)),
        SelectableService("pets", "Cuidado de mascotas", "Paseo y alimentación", Icons.Default.Add, Color(0xFFFF6B6B))
    )

    Scaffold(
        bottomBar = {
            Button(
                onClick = onContinueClick,
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(16.dp)
                    .height(56.dp),
                colors = ButtonDefaults.buttonColors(
                    containerColor = if (selectedServiceId != null) OrangeAccent else Color.Gray
                ),
                enabled = selectedServiceId != null,
                shape = RoundedCornerShape(28.dp)
            ) {
                Text(
                    text = "Continuar",
                    fontSize = 18.sp,
                    fontWeight = FontWeight.Bold
                )
            }
        }
    ) { innerPadding ->
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
                .padding(horizontal = 24.dp),
            verticalArrangement = Arrangement.Top
        ) {
            item {
                Spacer(modifier = Modifier.height(24.dp))
                Text(
                    text = "Encuentra el servicio perfecto",
                    style = MaterialTheme.typography.headlineMedium.copy(
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.onBackground
                    )
                )
                Spacer(modifier = Modifier.height(32.dp))
            }

            items(mainServices) { service ->
                SelectableServiceCard(
                    service = service,
                    isSelected = selectedServiceId == service.id,
                    onClick = { selectedServiceId = service.id }
                )
                Spacer(modifier = Modifier.height(16.dp))
            }

            item {
                Spacer(modifier = Modifier.height(16.dp))
                Text(
                    text = "Servicios adicionales",
                    style = MaterialTheme.typography.titleLarge.copy(
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.onBackground
                    )
                )
                Spacer(modifier = Modifier.height(16.dp))
            }

            items(additionalServices) { service ->
                SelectableServiceCard(
                    service = service,
                    isSelected = selectedServiceId == service.id,
                    onClick = { selectedServiceId = service.id },
                    isCompact = true
                )
                Spacer(modifier = Modifier.height(12.dp))
            }
            
            item {
                Spacer(modifier = Modifier.height(16.dp))
            }
        }
    }
}

@Composable
fun SelectableServiceCard(
    service: SelectableService,
    isSelected: Boolean,
    onClick: () -> Unit,
    isCompact: Boolean = false
) {
    val borderColor = if (isSelected) TurquoiseMain else Color.Transparent
    val backgroundColor = if (isSelected) TurquoiseMain.copy(alpha = 0.05f) else Color(0xFFF8F9FA) // Fondo claro

    Box(
        modifier = Modifier
            .fillMaxWidth()
            .height(if (isCompact) 80.dp else 140.dp)
            .clip(RoundedCornerShape(24.dp))
            .background(backgroundColor)
            .border(2.dp, borderColor, RoundedCornerShape(24.dp))
            .clickable(onClick = onClick)
            .padding(20.dp),
        contentAlignment = Alignment.CenterStart
    ) {
        Row(
            verticalAlignment = Alignment.CenterVertically
        ) {
            Box(
                modifier = Modifier
                    .size(if (isCompact) 48.dp else 64.dp)
                    .background(service.color.copy(alpha = 0.15f), CircleShape),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = service.icon,
                    contentDescription = null,
                    tint = service.color,
                    modifier = Modifier.size(if (isCompact) 24.dp else 32.dp)
                )
            }
            
            Spacer(modifier = Modifier.size(16.dp))

            Column {
                Text(
                    text = service.name,
                    fontSize = if (isCompact) 16.sp else 20.sp,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.onBackground
                )
                if (!isCompact) {
                    Spacer(modifier = Modifier.height(4.dp))
                    Text(
                        text = service.description,
                        style = MaterialTheme.typography.bodyMedium,
                        color = Color.Gray
                    )
                }
            }
        }
    }
}
