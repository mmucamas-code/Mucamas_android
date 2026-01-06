package com.movil.mucamas.ui.screens.home

import androidx.compose.foundation.background
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
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.DateRange
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.Star
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.NavigationBarItemDefaults
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp

data class ServiceItem(
    val name: String,
    val icon: ImageVector,
    val color: Color? = null // Hacemos el color opcional para usar el tema si es null
)

@Composable
fun HomeScreen(
    onServiceClick: (String) -> Unit = {},
    onMyReservationsClick: () -> Unit = {},
    onProfileClick: () -> Unit = {}
) {
    val services = listOf(
        ServiceItem("Limpieza", Icons.Default.Home, null), // Usará primary color
        ServiceItem("Cocina", Icons.Default.Star, null), // Usará secondary color
        ServiceItem("Planchado", Icons.Default.DateRange, Color(0xFF6C63FF))
    )

    Scaffold(
        bottomBar = {
            BottomNavigationBar(
                onHomeClick = {}, // Ya estamos en home
                onReservationsClick = onMyReservationsClick,
                onProfileClick = onProfileClick
            )
        }
    ) { innerPadding ->
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
                .padding(horizontal = 24.dp), // Padding generoso
            verticalArrangement = Arrangement.Top
        ) {
            item {
                Spacer(modifier = Modifier.height(32.dp)) // Más espacio arriba
                HeaderSection()
                Spacer(modifier = Modifier.height(40.dp)) // Más espacio antes de las cards
            }

            items(services) { service ->
                // Determinar el color: si es null, usar primary/secondary del tema
                val itemColor = service.color ?: if (service.name == "Limpieza") MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.secondary

                ServiceCard(
                    serviceItem = service.copy(color = itemColor),
                    onClick = { onServiceClick(service.name) }
                )
                Spacer(modifier = Modifier.height(20.dp)) // Más espacio entre cards
            }
        }
    }
}

@Composable
fun HeaderSection() {
    Column {
        Text(
            text = "Hola, Usuario",
            style = MaterialTheme.typography.titleLarge,
            color = MaterialTheme.colorScheme.onBackground.copy(alpha = 0.6f)
        )
        Spacer(modifier = Modifier.height(8.dp))
        Text(
            text = "¿Qué necesitas hoy?",
            style = MaterialTheme.typography.displaySmall.copy( // Texto más grande e impactante
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.onBackground
            )
        )
    }
}

@Composable
fun ServiceCard(
    serviceItem: ServiceItem,
    onClick: () -> Unit
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .height(140.dp) // Cards más grandes
            .clickable(onClick = onClick),
        shape = RoundedCornerShape(28.dp), // Bordes más redondeados
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surface
        ),
        elevation = CardDefaults.cardElevation(defaultElevation = 4.dp) // Un poco más de elevación
    ) {
        Row(
            modifier = Modifier
                .fillMaxSize()
                .padding(32.dp), // Padding interno mayor
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.Start
        ) {
            // Icon Container
            Box(
                modifier = Modifier
                    .size(72.dp) // Icon container más grande
                    .background(serviceItem.color?.copy(alpha = 0.1f) ?: MaterialTheme.colorScheme.primary.copy(alpha = 0.1f), CircleShape),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = serviceItem.icon,
                    contentDescription = null,
                    tint = serviceItem.color ?: MaterialTheme.colorScheme.primary,
                    modifier = Modifier.size(36.dp) // Icono más grande
                )
            }

            Spacer(modifier = Modifier.width(24.dp))

            Text(
                text = serviceItem.name,
                fontSize = 22.sp, // Texto más grande
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.onSurface
            )
        }
    }
}

@Composable
fun BottomNavigationBar(
    onHomeClick: () -> Unit,
    onReservationsClick: () -> Unit,
    onProfileClick: () -> Unit
) {
    NavigationBar(
        containerColor = MaterialTheme.colorScheme.surface,
        tonalElevation = 8.dp
    ) {
        NavigationBarItem(
            selected = true,
            onClick = onHomeClick,
            icon = { Icon(Icons.Default.Home, contentDescription = "Inicio") },
            label = { Text("Inicio", fontWeight = FontWeight.SemiBold) },
            colors = NavigationBarItemDefaults.colors(
                selectedIconColor = MaterialTheme.colorScheme.primary,
                selectedTextColor = MaterialTheme.colorScheme.primary,
                indicatorColor = MaterialTheme.colorScheme.primary.copy(alpha = 0.1f)
            )
        )
        NavigationBarItem(
            selected = false,
            onClick = onReservationsClick,
            icon = { Icon(Icons.Default.DateRange, contentDescription = "Reservas") },
            label = { Text("Reservas", fontWeight = FontWeight.SemiBold) }
        )
        NavigationBarItem(
            selected = false,
            onClick = onProfileClick,
            icon = { Icon(Icons.Default.Person, contentDescription = "Perfil") },
            label = { Text("Perfil", fontWeight = FontWeight.SemiBold) }
        )
    }
}
