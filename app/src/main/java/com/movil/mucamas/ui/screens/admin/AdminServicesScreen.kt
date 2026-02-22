package com.movil.mucamas.ui.screens.admin

import android.net.Uri
import android.widget.Toast
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.tween
import androidx.compose.animation.expandVertically
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.shrinkVertically
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
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.movil.mucamas.ui.models.Service
import com.movil.mucamas.ui.utils.FormatsHelpers
import com.movil.mucamas.ui.viewmodels.AdminUiEvent
import com.movil.mucamas.ui.viewmodels.AdminUiState
import com.movil.mucamas.ui.viewmodels.AdminViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AdminServicesScreen(adminViewModel: AdminViewModel = viewModel()) {
    val uiState by adminViewModel.uiState.collectAsState()
    val context = LocalContext.current

    var serviceToEdit by remember { mutableStateOf<Service?>(null) }
    val editSheetState = rememberModalBottomSheetState()

    val csvPickerLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.GetContent(),
        onResult = { uri: Uri? ->
            uri?.let {
                adminViewModel.importServicesFromCSV(it, context.contentResolver)
            }
        }
    )

    LaunchedEffect(key1 = true) {
        adminViewModel.eventFlow.collect { event ->
            val message = when (event) {
                is AdminUiEvent.ShowError -> event.message
                is AdminUiEvent.ShowSuccess -> event.message
            }
            Toast.makeText(context, message, Toast.LENGTH_SHORT).show()
        }
    }

    Scaffold(
        floatingActionButton = {
            FloatingActionButton(
                onClick = { csvPickerLauncher.launch("text/csv") },
                containerColor = MaterialTheme.colorScheme.primary
            ) {
                Icon(Icons.Default.Add, contentDescription = "Importar CSV", tint = MaterialTheme.colorScheme.onPrimary)
            }
        }
    ) { paddingValues ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
                .padding(16.dp)
        ) {
            Text("Gestión de Servicios", style = MaterialTheme.typography.headlineMedium)
            Spacer(modifier = Modifier.height(16.dp))

            AdminServicesContent(
                uiState = uiState,
                viewModel = adminViewModel,
                onEditClick = { service -> serviceToEdit = service }
            )
        }
    }

    // Modal para editar servicio
    if (serviceToEdit != null) {
        ModalBottomSheet(
            onDismissRequest = { serviceToEdit = null },
            sheetState = editSheetState
        ) {
            EditServiceSheetContent(
                service = serviceToEdit!!,
                adminViewModel = adminViewModel,
                onDismiss = { serviceToEdit = null }
            )
        }
    }
}

@Composable
fun AdminServicesContent(uiState: AdminUiState, viewModel: AdminViewModel, onEditClick: (Service) -> Unit) {
    if (uiState.isLoading) {
        Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
            CircularProgressIndicator()
        }
    } else {
        LazyColumn(verticalArrangement = Arrangement.spacedBy(12.dp)) {
            items(uiState.services, key = { it.id }) { service ->
                ServiceListItem(service = service, viewModel = viewModel, onEditClick = { onEditClick(service) })
            }
        }
    }
}

@Composable
fun ServiceListItem(service: Service, viewModel: AdminViewModel, onEditClick: () -> Unit) {
    var expanded by remember { mutableStateOf(false) }

    Card(
        modifier = Modifier
            .fillMaxWidth()
            .clickable { expanded = !expanded },
        elevation = CardDefaults.cardElevation(2.dp)
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Text(service.nombre, modifier = Modifier.weight(1f), style = MaterialTheme.typography.titleMedium)
                Text(FormatsHelpers.formatCurrencyCOP(service.precio), fontWeight = FontWeight.SemiBold)
            }

            AnimatedVisibility(
                visible = expanded,
                enter = fadeIn() + expandVertically(animationSpec = tween(durationMillis = 300)),
                exit = fadeOut() + shrinkVertically(animationSpec = tween(durationMillis = 300))
            ) {
                ServiceDetails(service = service, viewModel = viewModel, onEditClick = onEditClick)
            }
        }
    }
}

@Composable
fun ServiceDetails(service: Service, viewModel: AdminViewModel, onEditClick: () -> Unit) {
    Column(
        modifier = Modifier
            .padding(top = 16.dp)
            .fillMaxWidth()
    ) {
        Text(service.descripcion, style = MaterialTheme.typography.bodyMedium)
        Spacer(modifier = Modifier.height(8.dp))
        Text("Duración: ${service.duracionMinutos} min.", style = MaterialTheme.typography.bodySmall)
        Spacer(modifier = Modifier.height(8.dp))
        Text("Características:", fontWeight = FontWeight.Bold)
        service.caracteristicas.forEach {
            Text(" • $it")
        }
        Spacer(modifier = Modifier.height(16.dp))
        Row(horizontalArrangement = Arrangement.End, modifier = Modifier.fillMaxWidth()) {
            IconButton(onClick = onEditClick) {
                Icon(Icons.Default.Edit, contentDescription = "Editar")
            }
            Spacer(modifier = Modifier.width(8.dp))
            IconButton(onClick = { viewModel.deleteService(service.id, service.nombre) }) {
                Icon(Icons.Default.Delete, contentDescription = "Eliminar", tint = MaterialTheme.colorScheme.error)
            }
        }
    }
}

@Composable
fun EditServiceSheetContent(
    service: Service,
    adminViewModel: AdminViewModel,
    onDismiss: () -> Unit
) {
    var nombre by remember { mutableStateOf(service.nombre) }
    var descripcion by remember { mutableStateOf(service.descripcion) }
    var precio by remember { mutableStateOf(service.precio.toString()) }
    var duracion by remember { mutableStateOf(service.duracionMinutos.toString()) }
    val caracteristicas = remember { mutableStateListOf<String>().also { it.addAll(service.caracteristicas) } }
    var newCaracteristica by remember { mutableStateOf("") }

    LazyColumn(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp, vertical = 24.dp)
    ) {
        item {
            Text("Editar Servicio", style = MaterialTheme.typography.headlineSmall, fontWeight = FontWeight.Bold)
            Spacer(modifier = Modifier.height(16.dp))
        }

        item {
            MucamasTextField(label = "Nombre del Servicio", value = nombre, onValueChange = { nombre = it })
            Spacer(modifier = Modifier.height(8.dp))
            MucamasTextField(label = "Descripción", value = descripcion, onValueChange = { descripcion = it })
            Spacer(modifier = Modifier.height(8.dp))
            MucamasTextField(
                label = "Precio (COP)",
                value = precio,
                onValueChange = { newValue -> precio = newValue.filter { it.isDigit() } },
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number)
            )
            Spacer(modifier = Modifier.height(8.dp))
            MucamasTextField(
                label = "Duración (minutos)",
                value = duracion,
                onValueChange = { newValue -> duracion = newValue.filter { it.isDigit() } },
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number)
            )
            Spacer(modifier = Modifier.height(16.dp))
        }

        item {
            Text("Características", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.SemiBold)
            Spacer(modifier = Modifier.height(8.dp))
        }

        items(caracteristicas) { item ->
            Row(verticalAlignment = Alignment.CenterVertically, modifier = Modifier.padding(bottom = 4.dp)) {
                Text(item, modifier = Modifier.weight(1f))
                IconButton(onClick = { caracteristicas.remove(item) }) {
                    Icon(Icons.Default.Delete, contentDescription = "Eliminar característica", tint = MaterialTheme.colorScheme.error)
                }
            }
        }

        item {
            Row(verticalAlignment = Alignment.CenterVertically, modifier = Modifier.padding(top = 8.dp)) {
                OutlinedTextField(
                    value = newCaracteristica,
                    onValueChange = { newCaracteristica = it },
                    label = { Text("Nueva característica") },
                    modifier = Modifier.weight(1f)
                )
                Spacer(modifier = Modifier.width(8.dp))
                Button(onClick = {
                    if (newCaracteristica.isNotBlank()) {
                        caracteristicas.add(newCaracteristica)
                        newCaracteristica = ""
                    }
                }) {
                    Text("Añadir")
                }
            }
            Spacer(modifier = Modifier.height(24.dp))
        }

        item {
            Button(
                onClick = {
                    val updatedService = service.copy(
                        nombre = nombre,
                        descripcion = descripcion,
                        precio = precio.toLongOrNull() ?: service.precio,
                        duracionMinutos = duracion.toIntOrNull() ?: service.duracionMinutos,
                        caracteristicas = caracteristicas
                    )
                    adminViewModel.updateService(updatedService)
                    onDismiss()
                },
                modifier = Modifier.fillMaxWidth()
            ) {
                Text("Guardar Cambios")
            }
        }
    }
}

@Composable
fun MucamasTextField(
    label: String,
    value: String,
    onValueChange: (String) -> Unit,
    keyboardOptions: KeyboardOptions = KeyboardOptions.Default
) {
    OutlinedTextField(
        value = value,
        onValueChange = onValueChange,
        label = { Text(label) },
        modifier = Modifier.fillMaxWidth(),
        keyboardOptions = keyboardOptions
    )
}
