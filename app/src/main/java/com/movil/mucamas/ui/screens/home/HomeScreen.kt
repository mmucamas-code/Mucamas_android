package com.movil.mucamas.ui.screens.home

import android.util.Log
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
import androidx.compose.material.icons.automirrored.filled.KeyboardArrowRight
import androidx.compose.material.icons.filled.DateRange
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.Star
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.Text
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.movil.mucamas.data.model.SessionResult
import com.movil.mucamas.data.model.UserSession
import com.movil.mucamas.ui.utils.AdaptiveTheme
import com.movil.mucamas.ui.viewmodels.MainViewModel

data class ServiceItem(
    val name: String,
    val icon: ImageVector,
    val description: String,
    val price: String
)

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun HomeScreen(
    onServiceClick: (String) -> Unit = {},
    mainViewModel: MainViewModel = viewModel()
) {
    val spacing = AdaptiveTheme.spacing
    val sessionState by mainViewModel.sessionState.collectAsState()
    var userLogged by remember { mutableStateOf<UserSession?>(null) }
    
    var selectedService by remember { mutableStateOf<ServiceItem?>(null) }
    val sheetState = rememberModalBottomSheetState()

    val services = listOf(
        ServiceItem("Limpieza", Icons.Default.Home, "Servicio completo de limpieza y desinfección.", "$35.00 / 4h"),
        ServiceItem("Cocina", Icons.Default.Star, "Preparación de alimentos saludables.", "$45.00 / día"),
        ServiceItem("Planchado", Icons.Default.DateRange, "Cuidado experto para tu ropa.", "$25.00 / canasta")
    )

    LaunchedEffect(sessionState) {
        when (val result = sessionState) {
            is SessionResult.Success -> { userLogged = result.user }
            is SessionResult.Empty -> { }
            is SessionResult.Loading -> {}
        }
    }

    LazyColumn(
        modifier = Modifier
            .fillMaxSize()
            .padding(horizontal = spacing.large),
        verticalArrangement = Arrangement.Top
    ) {
        item {
            Spacer(modifier = Modifier.height(spacing.extraLarge))
            // Pasamos el primer nombre al Header
            HeaderSection(userName = userLogged?.fullName?.substringBefore(" ") ?: "Usuario")
            Spacer(modifier = Modifier.height(spacing.extraLarge))
        }

        items(services) { service ->
            ServiceListCard(
                serviceItem = service,
                onClick = { selectedService = service }
            )
            Spacer(modifier = Modifier.height(spacing.medium))
        }
        
        item {
            Spacer(modifier = Modifier.height(80.dp))
        }
    }

    if (selectedService != null) {
        ModalBottomSheet(
            onDismissRequest = { selectedService = null },
            sheetState = sheetState,
            containerColor = MaterialTheme.colorScheme.surface
        ) {
            ServiceDetailContent(
                service = selectedService!!,
                onReserveClick = {
                    onServiceClick(selectedService!!.name)
                    selectedService = null
                }
            )
        }
    }
}

@Composable
fun HeaderSection(userName: String) {
    val typography = AdaptiveTheme.typography
    val spacing = AdaptiveTheme.spacing

    Column {
        Text(
            text = "Hola, $userName!",
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

// El resto de los componentes (ServiceListCard, ServiceDetailContent) se mantienen igual...

@Composable
fun ServiceListCard(
    serviceItem: ServiceItem,
    onClick: () -> Unit
) {
    val dimens = AdaptiveTheme.dimens
    val spacing = AdaptiveTheme.spacing
    val typography = AdaptiveTheme.typography

    Card(
        modifier = Modifier
            .fillMaxWidth()
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
                .padding(spacing.medium),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                // Icon Container
                Box(
                    modifier = Modifier
                        .size(dimens.iconLarge)
                        .background(
                            MaterialTheme.colorScheme.primary.copy(alpha = 0.08f),
                            CircleShape
                        ),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = serviceItem.icon,
                        contentDescription = null,
                        tint = MaterialTheme.colorScheme.primary,
                        modifier = Modifier.size(dimens.iconSmall)
                    )
                }

                Spacer(modifier = Modifier.width(spacing.medium))

                Text(
                    text = serviceItem.name,
                    style = MaterialTheme.typography.titleMedium.copy(
                        fontSize = typography.title,
                        fontWeight = FontWeight.SemiBold,
                        color = MaterialTheme.colorScheme.onSurface
                    )
                )
            }
            
            // Flecha indicadora sutil
            Icon(
                imageVector = Icons.AutoMirrored.Filled.KeyboardArrowRight,
                contentDescription = null,
                tint = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.5f)
            )
        }
    }
}

@Composable
fun ServiceDetailContent(
    service: ServiceItem,
    onReserveClick: () -> Unit
) {
    val spacing = AdaptiveTheme.spacing
    val dimens = AdaptiveTheme.dimens
    val typography = AdaptiveTheme.typography

    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = spacing.large)
            .padding(bottom = spacing.extraLarge + 20.dp), // Padding extra para safe area
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        // Icono Grande Centrado
        Box(
            modifier = Modifier
                .size(80.dp)
                .background(MaterialTheme.colorScheme.primary.copy(alpha = 0.1f), CircleShape),
            contentAlignment = Alignment.Center
        ) {
            Icon(
                imageVector = service.icon,
                contentDescription = null,
                modifier = Modifier.size(40.dp),
                tint = MaterialTheme.colorScheme.primary
            )
        }

        Spacer(modifier = Modifier.height(spacing.medium))

        // Título
        Text(
            text = service.name,
            style = MaterialTheme.typography.headlineMedium.copy(
                fontSize = typography.headline,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.onSurface
            ),
            textAlign = TextAlign.Center
        )

        Spacer(modifier = Modifier.height(spacing.small))

        // Precio
        Text(
            text = service.price,
            style = MaterialTheme.typography.titleLarge.copy(
                fontSize = typography.title,
                fontWeight = FontWeight.SemiBold,
                color = MaterialTheme.colorScheme.primary
            ),
            textAlign = TextAlign.Center
        )

        Spacer(modifier = Modifier.height(spacing.large))
        
        HorizontalDivider(color = MaterialTheme.colorScheme.outline.copy(alpha = 0.2f))
        
        Spacer(modifier = Modifier.height(spacing.large))

        // Descripción
        Text(
            text = service.description,
            style = MaterialTheme.typography.bodyLarge.copy(
                fontSize = typography.body,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                lineHeight = 24.sp
            ),
            textAlign = TextAlign.Center
        )

        Spacer(modifier = Modifier.height(spacing.extraLarge))

        // Botón Reservar
        Button(
            onClick = onReserveClick,
            modifier = Modifier
                .fillMaxWidth()
                .height(dimens.buttonHeight),
            colors = ButtonDefaults.buttonColors(
                containerColor = MaterialTheme.colorScheme.primary,
                contentColor = MaterialTheme.colorScheme.onPrimary
            ),
            shape = RoundedCornerShape(dimens.cornerRadius),
            elevation = ButtonDefaults.buttonElevation(defaultElevation = 2.dp)
        ) {
            Text(
                text = "Reservar",
                fontSize = typography.button,
                fontWeight = FontWeight.Bold
            )
        }
    }
}
