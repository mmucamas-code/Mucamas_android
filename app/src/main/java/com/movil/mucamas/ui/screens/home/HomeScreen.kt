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
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.KeyboardArrowRight
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
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
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.movil.mucamas.data.model.SessionResult
import com.movil.mucamas.data.model.UserSession
import com.movil.mucamas.ui.models.Service
import com.movil.mucamas.ui.utils.AdaptiveTheme
import com.movil.mucamas.ui.utils.FormatsHelpers.formatCurrencyCOP
import com.movil.mucamas.ui.utils.FormatsHelpers.formatDuration
import com.movil.mucamas.ui.utils.FirebaseHelpers.getServiceIcon
import com.movil.mucamas.ui.viewmodels.HomeViewModel
import com.movil.mucamas.ui.viewmodels.MainViewModel
import com.movil.mucamas.ui.viewmodels.ServicesUiState

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun HomeScreen(
    onServiceClick: (String) -> Unit = {},
    mainViewModel: MainViewModel = viewModel(),
    homeViewModel: HomeViewModel = viewModel()
) {
    val spacing = AdaptiveTheme.spacing
    val sessionState by mainViewModel.sessionState.collectAsState()
    val servicesUiState by homeViewModel.servicesUiState.collectAsState()
    var userLogged by remember { mutableStateOf<UserSession?>(null) }

    var selectedService by remember { mutableStateOf<Service?>(null) }
    val sheetState = rememberModalBottomSheetState()

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

        item {
            when (val state = servicesUiState) {
                is ServicesUiState.Loading -> {
                    Box(modifier = Modifier.fillMaxWidth().padding(vertical = spacing.extraLarge), contentAlignment = Alignment.Center) {
                        CircularProgressIndicator(color = MaterialTheme.colorScheme.primary)
                    }
                }
                is ServicesUiState.Success -> {
                    Column {
                        for (service in state.services) {
                            ServiceListCard(
                                service = service,
                                onClick = { selectedService = service }
                            )
                            Spacer(modifier = Modifier.height(spacing.medium))
                        }
                    }
                }
                is ServicesUiState.Empty -> {
                    Text("No hay servicios disponibles.", textAlign = TextAlign.Center, modifier = Modifier.fillMaxWidth().padding(spacing.extraLarge))
                }
                is ServicesUiState.Error -> {
                    Text("Error: ${state.message}", color = MaterialTheme.colorScheme.error, textAlign = TextAlign.Center, modifier = Modifier.fillMaxWidth().padding(spacing.extraLarge))
                }
            }
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
                    // CORRECCIÓN: Pasar el ID del servicio en lugar del nombre
                    onServiceClick(selectedService!!.nombre)
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

@Composable
fun ServiceListCard(
    service: Service,
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
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
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
                Box(
                    modifier = Modifier
                        .size(dimens.iconLarge)
                        .background(MaterialTheme.colorScheme.primary.copy(alpha = 0.08f), CircleShape),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = getServiceIcon(service.icono),
                        contentDescription = null,
                        tint = MaterialTheme.colorScheme.primary,
                        modifier = Modifier.size(dimens.iconSmall)
                    )
                }

                Spacer(modifier = Modifier.width(spacing.medium))

                Text(
                    text = service.nombre,
                    style = MaterialTheme.typography.titleMedium.copy(
                        fontSize = typography.title,
                        fontWeight = FontWeight.SemiBold,
                        color = MaterialTheme.colorScheme.onSurface
                    )
                )
            }
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
    service: Service,
    onReserveClick: () -> Unit
) {
    val spacing = AdaptiveTheme.spacing
    val dimens = AdaptiveTheme.dimens
    val typography = AdaptiveTheme.typography

    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = spacing.large)
            .padding(bottom = spacing.extraLarge + 20.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Box(
            modifier = Modifier.size(80.dp).background(MaterialTheme.colorScheme.primary.copy(alpha = 0.1f), CircleShape),
            contentAlignment = Alignment.Center
        ) {
            Icon(
                imageVector = getServiceIcon(service.icono),
                contentDescription = null,
                modifier = Modifier.size(40.dp),
                tint = MaterialTheme.colorScheme.primary
            )
        }

        Spacer(modifier = Modifier.height(spacing.medium))

        Text(
            text = service.nombre,
            style = MaterialTheme.typography.headlineMedium.copy(fontSize = typography.headline, fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.onSurface),
            textAlign = TextAlign.Center
        )

        Spacer(modifier = Modifier.height(spacing.small))

        // Unir Precio y Duración
        Row {
             Text(
                text = formatCurrencyCOP(service.precio),
                style = MaterialTheme.typography.titleLarge.copy(fontSize = typography.title, fontWeight = FontWeight.SemiBold, color = MaterialTheme.colorScheme.primary),
                textAlign = TextAlign.Center
            )
            Spacer(modifier = Modifier.width(spacing.small))
            Text(
                text = "(${formatDuration(service.duracionMinutos.toInt())})",
                style = MaterialTheme.typography.titleMedium.copy(fontSize = typography.title, color = MaterialTheme.colorScheme.onSurfaceVariant),
                textAlign = TextAlign.Center
            )
        }

        Spacer(modifier = Modifier.height(spacing.large))
        HorizontalDivider(color = MaterialTheme.colorScheme.outline.copy(alpha = 0.2f))
        Spacer(modifier = Modifier.height(spacing.large))

        Text(
            text = service.descripcion,
            style = MaterialTheme.typography.bodyLarge.copy(fontSize = typography.body, color = MaterialTheme.colorScheme.onSurfaceVariant, lineHeight = 24.sp),
            textAlign = TextAlign.Center
        )

        Spacer(modifier = Modifier.height(spacing.extraLarge))

        Button(
            onClick = onReserveClick,
            modifier = Modifier.fillMaxWidth().height(dimens.buttonHeight),
            colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.primary, contentColor = MaterialTheme.colorScheme.onPrimary),
            shape = RoundedCornerShape(dimens.cornerRadius)
        ) {
            Text(
                text = "Reservar",
                fontSize = typography.button,
                fontWeight = FontWeight.Bold
            )
        }
    }
}
