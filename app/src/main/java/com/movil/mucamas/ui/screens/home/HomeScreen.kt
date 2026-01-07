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
import androidx.compose.material.icons.filled.Star
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.movil.mucamas.ui.utils.AdaptiveTheme

data class ServiceItem(
    val name: String,
    val icon: ImageVector,
    val color: Color? = null
)

@Composable
fun HomeScreen(
    onServiceClick: (String) -> Unit = {},
    onMyReservationsClick: () -> Unit = {},
    onProfileClick: () -> Unit = {}
) {
    // Utilidades adaptativas
    val spacing = AdaptiveTheme.spacing
    val dimens = AdaptiveTheme.dimens
    val typography = AdaptiveTheme.typography

    val services = listOf(
        ServiceItem("Limpieza", Icons.Default.Home, null), 
        ServiceItem("Cocina", Icons.Default.Star, null), 
        ServiceItem("Planchado", Icons.Default.DateRange, null)
    )

    LazyColumn(
        modifier = Modifier
            .fillMaxSize()
            .padding(horizontal = spacing.large),
        verticalArrangement = Arrangement.Top
    ) {
        item {
            Spacer(modifier = Modifier.height(spacing.extraLarge))
            HeaderSection()
            Spacer(modifier = Modifier.height(spacing.extraLarge))
        }

        items(services) { service ->
            // Usar colores del tema
            val itemColor = service.color ?: MaterialTheme.colorScheme.primary

            ServiceCard(
                serviceItem = service.copy(color = itemColor),
                onClick = { onServiceClick(service.name) }
            )
            Spacer(modifier = Modifier.height(spacing.medium))
        }
        
        // Espacio extra para el BottomBar flotante
        item {
            Spacer(modifier = Modifier.height(80.dp))
        }
    }
}

@Composable
fun HeaderSection() {
    val typography = AdaptiveTheme.typography
    val spacing = AdaptiveTheme.spacing

    Column {
        Text(
            text = "Hola, Usuario",
            style = MaterialTheme.typography.titleLarge.copy(
                fontSize = typography.title,
                color = MaterialTheme.colorScheme.onBackground.copy(alpha = 0.6f)
            )
        )
        Spacer(modifier = Modifier.height(spacing.small))
        Text(
            text = "¿Qué necesitas hoy?",
            style = MaterialTheme.typography.displaySmall.copy(
                fontSize = typography.headline,
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
    val dimens = AdaptiveTheme.dimens
    val spacing = AdaptiveTheme.spacing
    val typography = AdaptiveTheme.typography

    Card(
        modifier = Modifier
            .fillMaxWidth()
            .height(140.dp) // Mantener tamaño grande para impacto
            .clickable(onClick = onClick),
        shape = RoundedCornerShape(dimens.cornerRadius),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surface
        ),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        Row(
            modifier = Modifier
                .fillMaxSize()
                .padding(spacing.large),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.Start
        ) {
            // Icon Container
            Box(
                modifier = Modifier
                    .size(dimens.iconLarge * 1.5f)
                    .background(
                        serviceItem.color?.copy(alpha = 0.1f) ?: MaterialTheme.colorScheme.primary.copy(alpha = 0.1f), 
                        CircleShape
                    ),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = serviceItem.icon,
                    contentDescription = null,
                    tint = serviceItem.color ?: MaterialTheme.colorScheme.primary,
                    modifier = Modifier.size(dimens.iconMedium)
                )
            }

            Spacer(modifier = Modifier.width(spacing.large))

            Text(
                text = serviceItem.name,
                style = MaterialTheme.typography.titleLarge.copy(
                    fontSize = typography.title,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.onSurface
                )
            )
        }
    }
}
