
package com.movil.mucamas.ui.screens.home

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
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
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Search
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FilterChip
import androidx.compose.material3.FilterChipDefaults
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.OutlinedTextField
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
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.movil.mucamas.data.model.SessionResult
import com.movil.mucamas.data.model.UserSession
import com.movil.mucamas.ui.models.Service
import com.movil.mucamas.ui.models.UserRole
import com.movil.mucamas.ui.utils.AdaptiveTheme
import com.movil.mucamas.ui.utils.FirebaseHelpers.getServiceIcon
import com.movil.mucamas.ui.utils.FormatsHelpers
import com.movil.mucamas.ui.viewmodels.HomeViewModel
import com.movil.mucamas.ui.viewmodels.MainViewModel
import com.movil.mucamas.ui.viewmodels.ServicesUiState

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun HomeScreen(
    onServiceClick: (String) -> Unit = {},
    onAdminClick: () -> Unit = {},
    mainViewModel: MainViewModel = viewModel(),
    homeViewModel: HomeViewModel = viewModel()
) {
    val spacing = AdaptiveTheme.spacing
    val sessionState by mainViewModel.sessionState.collectAsState()
    val servicesUiState by homeViewModel.servicesUiState.collectAsState()
    var userLogged by remember { mutableStateOf<UserSession?>(null) }

    var selectedService by remember { mutableStateOf<Service?>(null) }
    val sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true)

    var searchQuery by remember { mutableStateOf("") }
    val categories = listOf("Todos", "Hogar", "Oficina", "Cocina", "Adicionales")
    var selectedCategory by remember { mutableStateOf(categories.first()) }

    LaunchedEffect(sessionState) {
        if (sessionState is SessionResult.Success) {
            userLogged = (sessionState as SessionResult.Success).user
        }
    }

    LaunchedEffect(Unit) {
        homeViewModel.refreshServices()
    }

    val filteredServices = remember(searchQuery, selectedCategory, servicesUiState) {
        if (servicesUiState is ServicesUiState.Success) {
            (servicesUiState as ServicesUiState.Success).services.filter {
                val matchesCategory = selectedCategory == "Todos" || it.categoria.equals(selectedCategory, ignoreCase = true)
                val matchesSearch = searchQuery.isBlank() ||
                        it.nombre.contains(searchQuery, ignoreCase = true) ||
                        it.descripcion.contains(searchQuery, ignoreCase = true)
                matchesCategory && matchesSearch
            }
        } else {
            emptyList()
        }
    }

    Column(modifier = Modifier
        .fillMaxSize()
        .background(MaterialTheme.colorScheme.surface))
    {
        LazyColumn(
            modifier = Modifier.weight(1f),
            contentPadding = PaddingValues(bottom = spacing.large)
        ) {
            item {
                Column(modifier = Modifier.padding(horizontal = spacing.large)) {
                    Spacer(modifier = Modifier.height(spacing.extraLarge))
                    HeaderSection(userName = userLogged?.fullName?.substringBefore(" ") ?: "Usuario")
                    Spacer(modifier = Modifier.height(spacing.large))
                    SearchBar(searchQuery = searchQuery, onQueryChange = { searchQuery = it })
                }
            }

            item {
                Spacer(modifier = Modifier.height(spacing.medium))
                CategoryFilters(categories, selectedCategory, onCategorySelected = { selectedCategory = it })
                Spacer(modifier = Modifier.height(spacing.large))
            }

            if (userLogged?.role == UserRole.ADMIN) {
                item {
                    Box(modifier = Modifier.padding(horizontal = spacing.large)){
                        Button(onClick = onAdminClick) {
                            Text("Administrar servicios")
                        }
                    }
                    Spacer(modifier = Modifier.height(spacing.large))
                }
            }

            when (val state = servicesUiState) {
                is ServicesUiState.Loading -> {
                    item {
                        Box(modifier = Modifier.fillParentMaxSize(), contentAlignment = Alignment.Center) {
                            CircularProgressIndicator()
                        }
                    }
                }
                is ServicesUiState.Success -> {
                    val comboServices = filteredServices.filter { it.esCombo }
                    val individualServices = filteredServices.filter { !it.esCombo }

                    if (comboServices.isNotEmpty()) {
                        item {
                            Text(
                                "Combos para ti",
                                style = MaterialTheme.typography.titleLarge.copy(fontWeight = FontWeight.Bold),
                                modifier = Modifier.padding(horizontal = spacing.large)
                            )
                            Spacer(modifier = Modifier.height(spacing.medium))
                            LazyRow(
                                contentPadding = PaddingValues(horizontal = spacing.large),
                                horizontalArrangement = Arrangement.spacedBy(spacing.medium)
                            ) {
                                items(comboServices) { service ->
                                    ComboCard(service = service, onClick = { selectedService = service })
                                }
                            }
                            Spacer(modifier = Modifier.height(spacing.large))
                        }
                    }

                    if (individualServices.isNotEmpty()) {
                        item {
                            Text(
                                "Servicios",
                                style = MaterialTheme.typography.titleLarge.copy(fontWeight = FontWeight.Bold),
                                modifier = Modifier.padding(horizontal = spacing.large)
                            )
                            Spacer(modifier = Modifier.height(spacing.medium))
                        }
                        items(individualServices) {
                            service ->
                            Box(modifier = Modifier.padding(horizontal = spacing.large, vertical = spacing.small)){
                                IndividualServiceCard(service = service, onClick = { selectedService = service })
                            }
                        }
                    }
                }
                is ServicesUiState.Empty -> {
                    item {
                        Text(
                            "No hay servicios disponibles.",
                            modifier = Modifier.padding(all = spacing.extraLarge), textAlign = TextAlign.Center
                        )
                    }
                }
                is ServicesUiState.Error -> {
                    item {
                        Text(
                            "Error: ${state.message}",
                            color = MaterialTheme.colorScheme.error,
                            modifier = Modifier.padding(all = spacing.extraLarge), textAlign = TextAlign.Center
                        )
                    }
                }
            }
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
                    onServiceClick(selectedService!!.nombre)
                    selectedService = null
                }
            )
        }
    }
}

@Composable
fun SearchBar(searchQuery: String, onQueryChange: (String) -> Unit) {
    OutlinedTextField(
        value = searchQuery,
        onValueChange = onQueryChange,
        modifier = Modifier.fillMaxWidth(),
        placeholder = { Text("¿Qué necesitas limpiar hoy?") },
        leadingIcon = { Icon(Icons.Default.Search, contentDescription = "Buscar", tint = MaterialTheme.colorScheme.onSurface) },
        shape = RoundedCornerShape(50)
    )
}

@Composable
fun CategoryFilters(categories: List<String>, selectedCategory: String, onCategorySelected: (String) -> Unit) {
    LazyRow(
        contentPadding = PaddingValues(horizontal = AdaptiveTheme.spacing.large),
        horizontalArrangement = Arrangement.spacedBy(AdaptiveTheme.spacing.small)
    ) {
        items(categories) { category ->
            FilterChip(
                selected = category == selectedCategory,
                onClick = { onCategorySelected(category) },
                label = { Text(category) },
                colors = FilterChipDefaults.filterChipColors(
                    selectedContainerColor = MaterialTheme.colorScheme.onSurface,
                    selectedLabelColor = MaterialTheme.colorScheme.surface,
                    containerColor = MaterialTheme.colorScheme.surface
                ),
                border = FilterChipDefaults.filterChipBorder(
                    borderColor = MaterialTheme.colorScheme.outline.copy(alpha = 0.5f),
                    selectedBorderColor = Color.Transparent,
                    disabledBorderColor = MaterialTheme.colorScheme.outline.copy(alpha = 0.12f),
                    disabledSelectedBorderColor = Color.Transparent,
                    borderWidth = 1.dp,
                    selectedBorderWidth = 1.dp,
                    enabled = true,
                    selected =  category == selectedCategory
                )
            )
        }
    }
}

@Composable
fun ComboCard(service: Service, onClick: () -> Unit) {
    Card(
        modifier = Modifier
            .width(200.dp)
            .clickable(onClick = onClick),
        elevation = CardDefaults.cardElevation(2.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
    ) {
        Column {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(100.dp)
                    .background(
                        brush = Brush.verticalGradient(
                            colors = listOf(
                                MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.3f),
                                Color.Transparent
                            )
                        )
                    ),
                contentAlignment = Alignment.Center
            ) {
                Icon(getServiceIcon(service.icono), contentDescription = null, modifier = Modifier.size(48.dp), tint = MaterialTheme.colorScheme.onSurface)
            }
            Column(modifier = Modifier.padding(AdaptiveTheme.spacing.medium)) {
                Text(service.nombre, style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold, maxLines = 1)
                Spacer(modifier = Modifier.height(AdaptiveTheme.spacing.small))
                Text(
                    FormatsHelpers.formatCurrencyCOP(service.precio),
                    style = MaterialTheme.typography.bodyLarge,
                    fontWeight = FontWeight.SemiBold,
                    color = MaterialTheme.colorScheme.onSurface
                )
            }
        }
    }
}

@Composable
fun IndividualServiceCard(service: Service, onClick: () -> Unit) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .clickable(onClick = onClick),
        shape = RoundedCornerShape(AdaptiveTheme.dimens.cornerRadius),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.3f)),
        elevation = CardDefaults.cardElevation(0.dp)
    ) {
        Row(
            modifier = Modifier
                .fillMaxSize()
                .padding(AdaptiveTheme.spacing.medium),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Row(verticalAlignment = Alignment.CenterVertically, modifier = Modifier.weight(1f)) {
                Icon(getServiceIcon(service.icono), contentDescription = service.nombre, tint = MaterialTheme.colorScheme.onSurface)
                Spacer(modifier = Modifier.width(AdaptiveTheme.spacing.medium))
                Text(service.nombre, style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.SemiBold)
            }
            Text(FormatsHelpers.formatCurrencyCOP(service.precio), style = MaterialTheme.typography.bodyMedium, fontWeight = FontWeight.Medium)
        }
    }
}

@Composable
fun HeaderSection(userName: String) {
    Column {
        Text(
            text = "Hola, $userName!",
            style = MaterialTheme.typography.titleLarge.copy(color = MaterialTheme.colorScheme.onBackground.copy(alpha = 0.7f))
        )
        Text(
            text = "¿Qué necesitas hoy?",
            style = MaterialTheme.typography.headlineMedium.copy(fontWeight = FontWeight.Bold)
        )
    }
}

@Composable
fun ServiceDetailContent(service: Service, onReserveClick: () -> Unit) {
    val spacing = AdaptiveTheme.spacing
    val dimens = AdaptiveTheme.dimens

    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = spacing.large)
            .padding(bottom = spacing.extraLarge + 20.dp),
    ) {
        Column(modifier = Modifier.fillMaxWidth(), horizontalAlignment = Alignment.CenterHorizontally) {
            Box(
                modifier = Modifier
                    .size(80.dp)
                    .background(MaterialTheme.colorScheme.onSurface.copy(alpha = 0.05f), CircleShape),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = getServiceIcon(service.icono),
                    contentDescription = null,
                    modifier = Modifier.size(40.dp),
                    tint = MaterialTheme.colorScheme.onSurface
                )
            }
            Spacer(modifier = Modifier.height(spacing.medium))
            Text(
                text = service.nombre,
                style = MaterialTheme.typography.headlineSmall.copy(fontWeight = FontWeight.Bold),
                textAlign = TextAlign.Center
            )
            Spacer(modifier = Modifier.height(spacing.small))

            Row(verticalAlignment = Alignment.CenterVertically) {
                Text(
                    text = FormatsHelpers.formatCurrencyCOP(service.precio),
                    style = MaterialTheme.typography.titleLarge.copy(fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.onSurface)
                )
                Spacer(modifier = Modifier.width(spacing.small))
                Text(
                    text = "(${FormatsHelpers.formatDuration(service.duracionMinutos.toInt())})",
                    style = MaterialTheme.typography.bodyLarge.copy(color = MaterialTheme.colorScheme.onSurfaceVariant)
                )
            }
        }

        Spacer(modifier = Modifier.height(spacing.large))
        HorizontalDivider(color = MaterialTheme.colorScheme.outline.copy(alpha = 0.2f))
        Spacer(modifier = Modifier.height(spacing.large))

        Text(text = service.descripcion, style = MaterialTheme.typography.bodyLarge, lineHeight = 24.sp)

        Spacer(modifier = Modifier.height(spacing.large))

        Text("Incluye:", style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold))
        Spacer(modifier = Modifier.height(spacing.medium))
        Column(verticalArrangement = Arrangement.spacedBy(spacing.small)) {
            service.caracteristicas.forEach { item ->
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(
                        Icons.Default.CheckCircle,
                        contentDescription = null,
                        tint = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.8f),
                        modifier = Modifier.size(20.dp)
                    )
                    Spacer(modifier = Modifier.width(spacing.medium))
                    Text(text = item, style = MaterialTheme.typography.bodyLarge, color = MaterialTheme.colorScheme.onSurfaceVariant)
                }
            }
        }

        Spacer(modifier = Modifier.height(spacing.extraLarge))

        Button(
            onClick = onReserveClick,
            modifier = Modifier
                .fillMaxWidth()
                .height(dimens.buttonHeight),
            colors = ButtonDefaults.buttonColors(
                containerColor = MaterialTheme.colorScheme.onSurface, // Black
                contentColor = MaterialTheme.colorScheme.surface // White
            ),
            shape = RoundedCornerShape(dimens.cornerRadius)
        ) {
            Text("Reservar este servicio", style = MaterialTheme.typography.labelLarge, fontWeight = FontWeight.Bold)
        }
    }
}
