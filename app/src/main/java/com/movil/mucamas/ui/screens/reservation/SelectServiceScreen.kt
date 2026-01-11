package com.movil.mucamas.ui.screens.reservation

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
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
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.DateRange
import androidx.compose.material.icons.filled.Place
import androidx.compose.material.icons.filled.Star
import androidx.compose.material.icons.filled.Warning
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.DatePicker
import androidx.compose.material3.DatePickerDialog
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.RadioButton
import androidx.compose.material3.RadioButtonDefaults
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.rememberDatePickerState
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
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.movil.mucamas.ui.models.Address
import com.movil.mucamas.ui.models.PaymentMethod
import com.movil.mucamas.ui.models.Reservation
import com.movil.mucamas.ui.models.Service
import com.movil.mucamas.ui.utils.AdaptiveTheme
import com.movil.mucamas.ui.utils.FormatsHelpers.formatCurrencyCOP
import com.movil.mucamas.ui.utils.FormatsHelpers.formatDuration
import com.movil.mucamas.ui.viewmodels.ReservationUiState
import com.movil.mucamas.ui.viewmodels.ReservationViewModel
import com.movil.mucamas.ui.viewmodels.SelectedServiceViewModel
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale
import kotlin.String

@Composable
fun SelectServiceScreen(
    selectedServiceViewModel: SelectedServiceViewModel = SelectedServiceViewModel(),
    reservationViewModel: ReservationViewModel = viewModel(),
    serviceName: String?,
    onContinueClick: (String) -> Unit = {}
) {
    val spacing = AdaptiveTheme.spacing
    val dimens = AdaptiveTheme.dimens
    val typography = AdaptiveTheme.typography

    var service by remember { mutableStateOf<Service?>(null) }
    val uiState by reservationViewModel.uiState.collectAsState()

    // Mock states
    var selectedPaymentMethod by remember { mutableStateOf("Efectivo") }
    var showDatePicker by remember { mutableStateOf(false) }
    var selectedDate by remember { mutableStateOf("Viernes, 15 Oct 2025") } // Default Mock Date
    val selectedTime = "15:00" // Formato 24h para lógica interna

    // State para la alerta
    var showAvailabilityAlert by remember { mutableStateOf(false) }
    var availableAt by remember { mutableStateOf("") }


    LaunchedEffect(serviceName) {
        if (serviceName != null) {
            service = selectedServiceViewModel.getServiceByName(serviceName)
        }
    }

    Scaffold(
        bottomBar = {
            Button(
                onClick = {
                    val newReservation = Reservation(
                        clientId = "", // TODO: Reemplazar con el ID del usuario actual
                        serviceId = service?.id ?: "",
                        serviceName = service?.nombre ?: "",
                        price = service?.precio?.toLong() ?: 0,
                        date = selectedDate,
                        startTime = selectedTime,
                        endTime = "", // TODO: Calcular endTime basado en la duración
                        address = Address(),
                        paymentMethod = if (selectedPaymentMethod == "Efectivo") PaymentMethod.CASH else PaymentMethod.CREDIT_CARD
                    )
                    reservationViewModel.createReservation(newReservation)
                },
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(spacing.large)
                    .height(dimens.buttonHeight),
                colors = ButtonDefaults.buttonColors(
                    containerColor = MaterialTheme.colorScheme.primary,
                    contentColor = MaterialTheme.colorScheme.onPrimary
                ),
                shape = RoundedCornerShape(dimens.cornerRadius),
                elevation = ButtonDefaults.buttonElevation(defaultElevation = 2.dp),
                enabled = uiState !is ReservationUiState.Loading
            ) {
                if (uiState is ReservationUiState.Loading) {
                    CircularProgressIndicator(modifier = Modifier.size(24.dp), color = MaterialTheme.colorScheme.onPrimary)
                } else {
                    Text(
                        text = "Confirmar y pagar",
                        fontSize = typography.button,
                        fontWeight = FontWeight.Bold
                    )
                }
            }
        },
        containerColor = MaterialTheme.colorScheme.background
    ) { innerPadding ->
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
                .padding(horizontal = spacing.large),
            verticalArrangement = Arrangement.Top
        ) {
            item {
                Spacer(modifier = Modifier.height(spacing.extraLarge))
                Text(
                    text = "Resumen y pago seguro",
                    style = MaterialTheme.typography.headlineMedium.copy(
                        fontSize = typography.headline,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.onBackground
                    )
                )
                Spacer(modifier = Modifier.height(spacing.large))
            }

            // 1) Card de Resumen
            item {
                SummaryCard(
                    serviceName = service?.nombre ?: "--",
                    date = selectedDate,
                    time = "3:00 pm",
                    address = "Calle 123 #45-67",
                    onDateClick = { showDatePicker = true },
                    onTimeClick = { /* TODO: Show Time Picker */ }
                )
                Spacer(modifier = Modifier.height(spacing.large))
            }

            item {
                Column(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalAlignment = Alignment.Start,
                    verticalArrangement = Arrangement.Center
                ) {
                    Text(
                        text = "Total a pagar",
                        style = MaterialTheme.typography.bodyLarge.copy(
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    )
                    Spacer(modifier = Modifier.height(4.dp))
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.Center
                    ) {
                        Text(
                            text = formatCurrencyCOP(service?.precio ?: 0),
                            style = MaterialTheme.typography.headlineSmall.copy(
                                fontWeight = FontWeight.Bold,
                                color = MaterialTheme.colorScheme.primary
                            )
                        )
                        Spacer(modifier = Modifier.width(spacing.small))
                        Text(
                            text = "/ ${formatDuration(service?.duracionMinutos?.toInt() ?: 0)}",
                            style = MaterialTheme.typography.bodyLarge.copy(
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        )
                    }
                }
                Spacer(modifier = Modifier.height(spacing.extraLarge))
            }

            item {
                Text(
                    text = "Método de pago",
                    style = MaterialTheme.typography.titleMedium.copy(
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.onSurface
                    )
                )
                Spacer(modifier = Modifier.height(spacing.medium))

                PaymentOptionRow(
                    text = "Efectivo",
                    selected = selectedPaymentMethod == "Efectivo",
                    onClick = { selectedPaymentMethod = "Efectivo" }
                )
                PaymentOptionRow(
                    text = "Tarjeta",
                    selected = selectedPaymentMethod == "Tarjeta",
                    onClick = { selectedPaymentMethod = "Tarjeta" }
                )

                Spacer(modifier = Modifier.height(80.dp))
            }
        }
    }

    // --- MANEJO DE ESTADOS DE LA UI ---
    when (val state = uiState) {
        is ReservationUiState.Loading -> { }

        is ReservationUiState.ReservationCreated -> {
            // Navega a la pantalla de éxito o confirmación
            LaunchedEffect(state.reservationId) {
                onContinueClick(state.reservationId)
            }
        }

        is ReservationUiState.ShowAvailabilityAlert -> {
            showAvailabilityAlert = true
            availableAt = state.availableAt
        }

        is ReservationUiState.Error -> {
            // Muestra un Snackbar o un dialogo de error
            // Por ahora, lo mostraremos en la consola para depuración
            LaunchedEffect(state.message) {
                println("Error en reserva: ${state.message} ")
            }
        }

        is ReservationUiState.Idle -> {
            // No se hace nada
        }
    }

    if (showAvailabilityAlert) {
        AlertDialog(
            onDismissRequest = { showAvailabilityAlert = false },
            title = { Text("Sin disponibilidad inmediata") },
            text = { Text("No tenemos un colaborador disponible a esa hora. El más cercano estará libre a las $availableAt. ¿Deseas crear la reserva y esperar a que se asigne un colaborador?") },
            confirmButton = {
                TextButton(
                    onClick = {
                        val newReservation = Reservation(
                            clientId = "", // TODO: Reemplazar con el ID del usuario actual
                            serviceId = service?.id ?: "",
                            serviceName = service?.nombre ?: "",
                            price = service?.precio?.toLong() ?: 0,
                            date = selectedDate,
                            startTime = selectedTime,
                            endTime = "", // TODO: Calcular endTime basado en la duración
                            address = Address(),
                            paymentMethod = if (selectedPaymentMethod == "Efectivo") PaymentMethod.CASH else PaymentMethod.CREDIT_CARD
                        )
                        reservationViewModel.proceedWithReservation(newReservation)
                        showAvailabilityAlert = false
                    }
                ) {
                    Text("Aceptar")
                }
            },
            dismissButton = {
                TextButton(onClick = { showAvailabilityAlert = false }) {
                    Text("Cancelar")
                }
            }
        )
    }

    if (showDatePicker) {
        SimpleDatePickerDialog(
            onDateSelected = { date ->
                selectedDate = date
                showDatePicker = false
            },
            onDismiss = { showDatePicker = false }
        )
    }
}


@Composable
fun SummaryCard(
    serviceName: String,
    date: String,
    time: String,
    address: String,
    onDateClick: () -> Unit,
    onTimeClick: () -> Unit
) {
    val dimens = AdaptiveTheme.dimens
    val spacing = AdaptiveTheme.spacing

    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(dimens.cornerRadius),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        Column(
            modifier = Modifier.padding(spacing.large)
        ) {
            SummaryRow(icon = Icons.Default.Star, text = "Servicio: $serviceName")
            Spacer(modifier = Modifier.height(spacing.medium))
            HorizontalDivider(color = MaterialTheme.colorScheme.outline.copy(alpha = 0.1f))
            Spacer(modifier = Modifier.height(spacing.medium))

            SummaryRow(
                icon = Icons.Default.DateRange,
                text = "Fecha: $date",
                isClickable = true,
                onClick = onDateClick
            )
            Spacer(modifier = Modifier.height(spacing.small))

            SummaryRow(
                icon = Icons.Default.Warning,
                text = "Hora: $time",
                isClickable = true,
                onClick = onTimeClick
            )

            Spacer(modifier = Modifier.height(spacing.medium))
            HorizontalDivider(color = MaterialTheme.colorScheme.outline.copy(alpha = 0.1f))
            Spacer(modifier = Modifier.height(spacing.medium))

            SummaryRow(icon = Icons.Default.Place, text = "Dirección: $address")
        }
    }
}

@Composable
fun SummaryRow(
    icon: ImageVector,
    text: String,
    isClickable: Boolean = false,
    onClick: () -> Unit = {}
) {
    val spacing = AdaptiveTheme.spacing

    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(8.dp))
            .clickable(enabled = isClickable, onClick = onClick)
            .padding(vertical = 8.dp, horizontal = 4.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Icon(
            imageVector = icon,
            contentDescription = null,
            tint = MaterialTheme.colorScheme.primary,
            modifier = Modifier.size(24.dp)
        )
        Spacer(modifier = Modifier.width(spacing.medium))
        Text(
            text = text,
            style = MaterialTheme.typography.bodyLarge.copy(
                color = MaterialTheme.colorScheme.onSurface,
                fontWeight = if (isClickable) FontWeight.SemiBold else FontWeight.Normal
            )
        )
    }
}

@Composable
fun PaymentOptionRow(
    text: String,
    selected: Boolean,
    onClick: () -> Unit
) {
    val spacing = AdaptiveTheme.spacing

    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(12.dp))
            .clickable(onClick = onClick)
            .padding(vertical = 4.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        RadioButton(
            selected = selected,
            onClick = onClick,
            colors = RadioButtonDefaults.colors(
                selectedColor = MaterialTheme.colorScheme.primary,
                unselectedColor = MaterialTheme.colorScheme.onSurfaceVariant
            )
        )
        Spacer(modifier = Modifier.width(spacing.small))
        Text(
            text = text,
            style = MaterialTheme.typography.bodyLarge.copy(
                color = MaterialTheme.colorScheme.onSurface
            )
        )
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SimpleDatePickerDialog(
    onDateSelected: (String) -> Unit,
    onDismiss: () -> Unit
) {
    val datePickerState = rememberDatePickerState()

    DatePickerDialog(
        onDismissRequest = onDismiss,
        confirmButton = {
            TextButton(
                onClick = {
                    val selectedMillis = datePickerState.selectedDateMillis
                    if (selectedMillis != null) {
                        val formatter = SimpleDateFormat("EEEE, d MMM yyyy", Locale.getDefault())
                        onDateSelected(formatter.format(Date(selectedMillis)))
                    }
                    onDismiss()
                }
            ) {
                Text("OK", color = MaterialTheme.colorScheme.primary)
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text("Cancel", color = MaterialTheme.colorScheme.onSurfaceVariant)
            }
        }
    ) {
        DatePicker(state = datePickerState)
    }
}