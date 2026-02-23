package com.movil.mucamas.ui.screens.reservation

import android.net.Uri
import android.widget.Toast
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
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
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.LocationOn
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.DatePicker
import androidx.compose.material3.DatePickerDialog
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.RadioButton
import androidx.compose.material3.RadioButtonDefaults
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.material3.rememberDatePickerState
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.movil.mucamas.ui.models.Address
import com.movil.mucamas.ui.models.PaymentMethod
import com.movil.mucamas.ui.models.Reservation
import com.movil.mucamas.ui.utils.FormatsHelpers
import com.movil.mucamas.ui.viewmodels.HomeViewModel
import com.movil.mucamas.ui.viewmodels.ReservationUiEvent
import com.movil.mucamas.ui.viewmodels.ReservationViewModel
import com.movil.mucamas.ui.viewmodels.ServicesUiState
import kotlinx.coroutines.launch
import java.text.Normalizer
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Locale

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SelectServiceScreen(
    serviceName: String?,
    onContinueClick: () -> Unit,
    homeViewModel: HomeViewModel = viewModel(),
    reservationViewModel: ReservationViewModel = viewModel()
) {
    val servicesState by homeViewModel.servicesUiState.collectAsState()
    val reservationUiState by reservationViewModel.uiState.collectAsState()
    val context = LocalContext.current

    var selectedService by remember { mutableStateOf(serviceName) }
    val service = remember(selectedService, servicesState) {
        if (servicesState is ServicesUiState.Success) {
            (servicesState as ServicesUiState.Success).services.find { it.nombre == selectedService }
        } else null
    }

    var showDatePicker by remember { mutableStateOf(false) }
    var showTimePicker by remember { mutableStateOf(false) }
    var showAddressSheet by remember { mutableStateOf(false) }

    var date by remember { mutableStateOf(Calendar.getInstance()) }
    var address by remember { mutableStateOf<Address?>(null) }
    var paymentMethod by remember { mutableStateOf(PaymentMethod.CREDIT_CARD) }

    val datePickerState = rememberDatePickerState(initialSelectedDateMillis = date.timeInMillis)
    val scope = rememberCoroutineScope()

    // Set default address from history
    LaunchedEffect(reservationUiState.addressHistory) {
        if (address == null && reservationUiState.addressHistory.isNotEmpty()) {
            address = reservationUiState.addressHistory.first()
        }
    }

    // Image Picker Launcher
    val imagePickerLauncher = rememberLauncherForActivityResult(ActivityResultContracts.GetContent()) { uri: Uri? ->
        uri?.let { reservationViewModel.uploadReceiptAndHoldUrl(it) }
    }

    // Event Collector
    LaunchedEffect(Unit) {
        reservationViewModel.eventFlow.collect { event ->
            when (event) {
                is ReservationUiEvent.ReservationCreated -> {
                    Toast.makeText(context, "Reserva creada con éxito", Toast.LENGTH_LONG).show()
                    onContinueClick()
                }
                is ReservationUiEvent.ShowError -> Toast.makeText(context, event.message, Toast.LENGTH_LONG).show()
                is ReservationUiEvent.ShowSuccess -> Toast.makeText(context, event.message, Toast.LENGTH_SHORT).show()
                else -> {}
            }
        }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Resumen y Pago") },
                navigationIcon = { IconButton(onClick = onContinueClick) { Icon(Icons.AutoMirrored.Filled.ArrowBack, "Atrás") } },
                colors = TopAppBarDefaults.topAppBarColors(containerColor = MaterialTheme.colorScheme.surface)
            )
        },
        containerColor = MaterialTheme.colorScheme.surface
    ) {
        padding ->
        if (service != null) {
            LazyColumn(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(padding)
                    .padding(horizontal = 16.dp),
                verticalArrangement = Arrangement.spacedBy(24.dp)
            ) {
                item { ServiceSummaryCard(service.nombre, service.precio) }
                item { DateTimeSelector(date, { showDatePicker = true }, { showTimePicker = true }) }
                item { AddressSelector(address, { showAddressSheet = true }) }
                item { PaymentMethodSelector(
                    selectedMethod = paymentMethod,
                    onMethodSelected = { paymentMethod = it },
                    onUploadReceipt = { imagePickerLauncher.launch("image/*") },
                    isUploading = reservationUiState.isLoading,
                    receiptUrl = reservationViewModel.uploadedReceiptUrl
                ) }

                item {
                    Button(
                        onClick = {
                            val (hour, minute) = date.get(Calendar.HOUR_OF_DAY) to date.get(Calendar.MINUTE)
                            val reservation = Reservation(
                                serviceName = service.nombre,
                                date = SimpleDateFormat("dd/MM/yyyy", Locale.getDefault()).format(date.time),
                                startTime = String.format("%02d:%02d", hour, minute),
                                address = address!!,
                                paymentMethod = paymentMethod,
                                paymentReceiptUrl = if (paymentMethod == PaymentMethod.TRANSFER) reservationViewModel.uploadedReceiptUrl else null,
                                //TODO: Calculate total price
                                price = service.precio.toLong(),
                            )
                            reservationViewModel.createReservation(reservation, service.duracionMinutos)
                         },
                        modifier = Modifier.fillMaxWidth().height(50.dp),
                        enabled = !reservationUiState.isLoading && address != null,
                        colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.onSurface)
                    ) {
                        if (reservationUiState.isLoading && reservationViewModel.uploadedReceiptUrl == null) {
                            CircularProgressIndicator(modifier = Modifier.size(24.dp), color = MaterialTheme.colorScheme.surface)
                        } else {
                            Text("Reservar Ahora", color = MaterialTheme.colorScheme.surface, fontWeight = FontWeight.Bold)
                        }
                    }
                }
            }
        } else {
            Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                Text("Servicio no encontrado")
            }
        }
    }

    // --- DIALOGS AND SHEETS ---

    if (showDatePicker) {
        DatePickerDialog(
            onDismissRequest = { showDatePicker = false },
            confirmButton = {
                TextButton(onClick = {
                    showDatePicker = false
                    datePickerState.selectedDateMillis?.let { millis ->
                        val newDate = Calendar.getInstance().apply { timeInMillis = millis }
                        // Keep original time
                        newDate.set(Calendar.HOUR_OF_DAY, date.get(Calendar.HOUR_OF_DAY))
                        newDate.set(Calendar.MINUTE, date.get(Calendar.MINUTE))
                        date = newDate
                    }
                    showTimePicker = true // Open time picker after date
                }) { Text("Aceptar") }
            },
            dismissButton = { TextButton(onClick = { showDatePicker = false }) { Text("Cancelar") } }
        ) {
            DatePicker(state = datePickerState)
        }
    }

    if (showTimePicker) {
        ModalBottomSheet(onDismissRequest = { showTimePicker = false }) {
            val currentHour = date.get(Calendar.HOUR_OF_DAY)
            val currentMinute = date.get(Calendar.MINUTE) / 5 // Assuming 5-minute intervals
            TimeSheetContent(
                initialHour = currentHour,
                initialMinuteIndex = currentMinute,
                onTimeSelected = {
                    hour, minute ->
                    date = date.clone() as Calendar
                    date.set(Calendar.HOUR_OF_DAY, hour)
                    date.set(Calendar.MINUTE, minute)
                    scope.launch { showTimePicker = false }
                }
            )
        }
    }

    if (showAddressSheet) {
        ModalBottomSheet(onDismissRequest = { showAddressSheet = false }, sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true)) {
            AddressSheetContent(
                history = reservationUiState.addressHistory,
                onAddressSelected = {
                    newAddress ->
                    address = newAddress
                    scope.launch { showAddressSheet = false }
                },
                onAddNewAddress = { /* TODO */ }
            )
        }
    }
}

@Composable
fun WheelPicker(
    items: List<String>,
    initialIndex: Int,
    onItemSelected: (Int) -> Unit,
    modifier: Modifier = Modifier
) {
    val listState = rememberLazyListState(initialFirstVisibleItemIndex = initialIndex)
    LaunchedEffect(listState.isScrollInProgress) {
        if (!listState.isScrollInProgress) {
            val center = listState.layoutInfo.visibleItemsInfo.minByOrNull { kotlin.math.abs(it.offset) }?.index
            if (center != null) onItemSelected(center)
        }
    }
    LazyColumn(
        state = listState,
        modifier = modifier.height(120.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        items(items.size) { index ->
            Text(items[index], style = MaterialTheme.typography.titleLarge)
        }
    }
}

@Composable
fun TimeSheetContent(initialHour: Int, initialMinuteIndex: Int, onTimeSelected: (Int, Int) -> Unit) {
    var selectedHour by remember { mutableIntStateOf(initialHour) }
    var selectedMinute by remember { mutableIntStateOf(initialMinuteIndex * 5) }

    val hours = (0..23).map { it.toString().padStart(2, '0') }
    val minutes = (0..11).map { (it * 5).toString().padStart(2, '0') }

    Column(modifier = Modifier.padding(16.dp), horizontalAlignment = Alignment.CenterHorizontally) {
        Text("Seleccionar Hora", style = MaterialTheme.typography.headlineSmall, fontWeight = FontWeight.Bold)
        Spacer(Modifier.height(16.dp))
        Row(horizontalArrangement = Arrangement.Center, verticalAlignment = Alignment.CenterVertically) {
            WheelPicker(items = hours, initialIndex = initialHour, onItemSelected = { selectedHour = it }, modifier = Modifier.weight(1f))
            Text(":", style = MaterialTheme.typography.headlineMedium, modifier = Modifier.padding(horizontal = 8.dp))
            WheelPicker(items = minutes, initialIndex = initialMinuteIndex, onItemSelected = { selectedMinute = it * 5 }, modifier = Modifier.weight(1f))
        }
        Spacer(Modifier.height(16.dp))
        Button(onClick = { onTimeSelected(selectedHour, selectedMinute) }, modifier = Modifier.fillMaxWidth()) {
            Text("Confirmar Hora")
        }
    }
}

// --- Reusable & Updated Components from previous step ---

@Composable
fun ServiceSummaryCard(serviceName: String, price: Long) {
    Column {
        Text("Resumen del Servicio", style = MaterialTheme.typography.titleLarge, fontWeight = FontWeight.Bold)
        Spacer(modifier = Modifier.height(16.dp))
        Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween, verticalAlignment = Alignment.CenterVertically) {
            Text(serviceName, style = MaterialTheme.typography.bodyLarge)
            Text( FormatsHelpers.formatCurrencyCOP(price), style = MaterialTheme.typography.bodyLarge, fontWeight = FontWeight.SemiBold)
        }
        HorizontalDivider(modifier = Modifier.padding(vertical = 16.dp), color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.1f))
    }
}

@Composable
fun DateTimeSelector(date: Calendar, onDateClick: () -> Unit, onTimeClick: () -> Unit) {
    val sdfDate = SimpleDateFormat("EEEE, dd MMMM", Locale.getDefault())
    val sdfTime = SimpleDateFormat("hh:mm a", Locale.getDefault())

    Column(modifier = Modifier.fillMaxWidth()) {
        Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween, verticalAlignment = Alignment.CenterVertically) {
            Text("Fecha y Hora", style = MaterialTheme.typography.titleLarge, fontWeight = FontWeight.Bold)
        }
        Spacer(modifier = Modifier.height(16.dp))
        Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
            InfoCard("Fecha", sdfDate.format(date.time), Modifier.weight(1f).clickable(onClick = onDateClick))
            InfoCard("Hora", sdfTime.format(date.time), Modifier.weight(0.7f).clickable(onClick = onTimeClick))
        }
    }
}

@Composable
fun AddressSelector(address: Address?, onAddressClick: () -> Unit) {
    Column {
        Text("Dirección de Servicio", style = MaterialTheme.typography.titleLarge, fontWeight = FontWeight.Bold)
        Spacer(modifier = Modifier.height(16.dp))
        InfoCard("Dirección", address?.fullAddress ?: "Seleccionar o agregar dirección", Modifier.clickable(onClick = onAddressClick))
    }
}

@Composable
fun PaymentMethodSelector(
    selectedMethod: PaymentMethod,
    onMethodSelected: (PaymentMethod) -> Unit,
    onUploadReceipt: () -> Unit,
    isUploading: Boolean,
    receiptUrl: String?
) {
    val paymentMethods = PaymentMethod.values().toList()

    Column {
        Text("Método de Pago", style = MaterialTheme.typography.titleLarge, fontWeight = FontWeight.Bold)
        Spacer(modifier = Modifier.height(8.dp))

        paymentMethods.forEach { method ->
            Row(verticalAlignment = Alignment.CenterVertically, modifier = Modifier.fillMaxWidth().clickable { onMethodSelected(method) }) {
                RadioButton(
                    selected = selectedMethod == method,
                    onClick = { onMethodSelected(method) },
                    colors = RadioButtonDefaults.colors(selectedColor = MaterialTheme.colorScheme.onSurface)
                )
                Text(method.name)
            }
        }

        if (selectedMethod == PaymentMethod.TRANSFER) {
            Spacer(modifier = Modifier.height(16.dp))
            if (isUploading) {
                CircularProgressIndicator(modifier = Modifier.size(24.dp))
            } else if (receiptUrl != null) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(Icons.Default.CheckCircle, "Comprobante subido", tint = MaterialTheme.colorScheme.primary)
                    Spacer(modifier = Modifier.width(8.dp))
                    Text("Comprobante cargado", color = MaterialTheme.colorScheme.primary, fontWeight = FontWeight.SemiBold)
                }
            } else {
                OutlinedButton(onClick = onUploadReceipt, modifier = Modifier.fillMaxWidth()) {
                    Text("Adjuntar Comprobante")
                }
            }
        }
    }
}

@Composable
fun AddressSheetContent(history: List<Address>, onAddressSelected: (Address) -> Unit, onAddNewAddress: () -> Unit) {
    var newAddressField by remember { mutableStateOf("") }

    LazyColumn(modifier = Modifier.padding(16.dp)) {
        item {
            Text("Seleccionar Dirección", style = MaterialTheme.typography.headlineSmall, fontWeight = FontWeight.Bold)
            Spacer(Modifier.height(16.dp))
            Box(
                modifier = Modifier.fillMaxWidth().height(150.dp).clip(RoundedCornerShape(12.dp)).background(MaterialTheme.colorScheme.surfaceVariant),
                contentAlignment = Alignment.Center
            ) {
                Icon(Icons.Default.LocationOn, "Mapa", modifier = Modifier.size(48.dp), tint = MaterialTheme.colorScheme.onSurfaceVariant)
            }
            Spacer(Modifier.height(24.dp))
        }

        if (history.isNotEmpty()){
            item {
                Text("Historial de Direcciones", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.SemiBold)
                Spacer(Modifier.height(8.dp))
            }
            items(history) { address ->
                Text(address.fullAddress, modifier = Modifier.fillMaxWidth().clickable { onAddressSelected(address) }.padding(vertical = 12.dp))
            }
            item { HorizontalDivider(modifier = Modifier.padding(vertical = 16.dp)) }
        }


        item {
            Text("Nueva Dirección", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.SemiBold)
            Spacer(Modifier.height(8.dp))
            OutlinedTextField(value = newAddressField, onValueChange = {newAddressField = it}, label = { Text("Escribe la dirección completa")}, modifier = Modifier.fillMaxWidth())
            Spacer(Modifier.height(8.dp))
            Button(onClick = {
                // TODO: Add full address object creation with City, etc.
                if(newAddressField.isNotBlank()) onAddressSelected(Address(fullAddress = newAddressField))
            }, modifier = Modifier.fillMaxWidth()) {
                Text("Usar esta dirección")
            }
        }
    }
}

@Composable
fun InfoCard(label: String, value: String, modifier: Modifier = Modifier) {
    Box(modifier.fillMaxWidth().then(modifier)) {
        Column {
            Text(label, style = MaterialTheme.typography.labelMedium, color = MaterialTheme.colorScheme.onSurfaceVariant, fontWeight = FontWeight.Normal)
            Spacer(Modifier.height(4.dp))
            Box(modifier = Modifier
                .fillMaxWidth()
                .clip(RoundedCornerShape(12.dp))
                .background(MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f))
                .padding(16.dp)) {
                Text(value, style = MaterialTheme.typography.bodyLarge, fontWeight = FontWeight.SemiBold, textAlign = TextAlign.Center, modifier=Modifier.fillMaxWidth())
            }
        }
    }
}
