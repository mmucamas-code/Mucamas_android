package com.movil.mucamas.ui.screens.myreservations

import android.util.Log
import android.widget.Toast
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
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.movil.mucamas.ui.components.EmptyStateView
import com.movil.mucamas.ui.components.FullScreenLoading
import com.movil.mucamas.ui.models.Reservation
import com.movil.mucamas.ui.models.ReservationStatus
import com.movil.mucamas.ui.models.UserDto
import com.movil.mucamas.ui.models.UserRole
import com.movil.mucamas.ui.screens.rate.RateServiceScreen
import com.movil.mucamas.ui.utils.AdaptiveTheme
import com.movil.mucamas.ui.utils.FormatsHelpers
import com.movil.mucamas.ui.viewmodels.ReservationUiEvent
import com.movil.mucamas.ui.viewmodels.ReservationViewModel

@Composable
fun MyReservationsScreen(
    viewModel: ReservationViewModel = viewModel()
) {
    val uiState by viewModel.uiState.collectAsState()
    val userSession by viewModel.userSession.collectAsState()

    var showRateModal by remember { mutableStateOf(false) }
    var selectedServiceToRate by remember { mutableStateOf<Reservation?>(null) }

    var showCollaboratorSelector by remember { mutableStateOf(false) }
    var reservationToAssign by remember { mutableStateOf<Reservation?>(null) }
    val context = LocalContext.current

    LaunchedEffect(key1 = true) {
        viewModel.eventFlow.collect { event ->
            when (event) {
                is ReservationUiEvent.ShowError -> {
                    Log.d("error","${event.message}")
                    Toast.makeText(context, event.message, Toast.LENGTH_LONG).show()
                }
                is ReservationUiEvent.ReservationCreated -> {
                    Toast.makeText(context, "Reserva creada con éxito", Toast.LENGTH_SHORT).show()
                }
                is ReservationUiEvent.ReservationRated -> {
                    Toast.makeText(context, "Calificación enviada con éxito", Toast.LENGTH_SHORT).show()
                    showRateModal = false
                }
                is ReservationUiEvent.ReservationUpdated -> {
                    Toast.makeText(context, "Reserva actualizada", Toast.LENGTH_SHORT).show()
                }
                is ReservationUiEvent.ShowCollaboratorSelector -> {
                    showCollaboratorSelector = true
                }
            }
        }
    }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .padding(AdaptiveTheme.spacing.large),
        contentAlignment = Alignment.Center
    ) {
        if (uiState.isLoading) {
            FullScreenLoading()
        }

        if (!uiState.isLoading && uiState.isEmpty) {
            EmptyStateView()
        } else {
            ReservationsList(
                reservations = uiState.reservations,
                userRole = userSession?.role,
                onRateClick = {
                    selectedServiceToRate = it
                    showRateModal = true
                },
                onActionClick = { reservation, action ->
                    reservationToAssign = reservation
                    when (action) {
                        "assign" -> viewModel.onAssignCollaboratorClicked()
                        "pay" -> viewModel.processPayment(reservation.id)
                        "confirm" -> viewModel.confirmReservation(reservation.id)
                        "start" -> viewModel.startReservation(reservation.id)
                        "complete" -> viewModel.completeReservation(reservation)
                        "cancel" -> viewModel.cancelReservation(reservation)
                        else -> {}
                    }
                }
            )
        }

        if (showRateModal && selectedServiceToRate != null) {
            RateServiceScreen(
                serviceName = selectedServiceToRate!!.serviceName,
                onDismissRequest = { showRateModal = false },
                onSubmit = { rating, comment ->
                    viewModel.rateReservation(selectedServiceToRate!!.id, rating, comment)
                }
            )
        }

        if (showCollaboratorSelector && reservationToAssign != null) {
            CollaboratorSelectionDialog(
                collaborators = uiState.availableCollaborators,
                onDismiss = { showCollaboratorSelector = false },
                onConfirm = { collaboratorId ->
                    viewModel.assignCollaboratorToReservation(reservationToAssign!!.id, collaboratorId)
                    showCollaboratorSelector = false
                }
            )
        }
    }
}

@Composable
fun CollaboratorSelectionDialog(
    collaborators: List<UserDto>,
    onDismiss: () -> Unit,
    onConfirm: (String) -> Unit
) {
    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("Asignar Colaborador") },
        text = {
            LazyColumn {
                items(collaborators) { collaborator ->
                    Text(
                        text = collaborator.fullName,
                        modifier = Modifier
                            .fillMaxWidth()
                            .clickable { onConfirm(collaborator.idNumber) }
                            .padding(vertical = 16.dp)
                    )
                }
            }
        },
        confirmButton = {
            TextButton(onClick = onDismiss) {
                Text("Cancelar")
            }
        }
    )
}

@Composable
fun ReservationsList(
    reservations: List<Reservation>,
    userRole: UserRole?,
    onRateClick: (Reservation) -> Unit = {},
    onActionClick: (Reservation, String) -> Unit
) {
    LazyColumn(
        modifier = Modifier.fillMaxSize(),
        verticalArrangement = Arrangement.spacedBy(AdaptiveTheme.spacing.medium)
    ) {
        item {
            Text(
                text = "Mis Reservas",
                style = MaterialTheme.typography.headlineMedium.copy(
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.onBackground
                )
            )
            Spacer(modifier = Modifier.height(AdaptiveTheme.spacing.large))
        }
        items(reservations) { reservation ->
            ReservationCard(
                reservation = reservation,
                userRole = userRole,
                onRateClick = { onRateClick(reservation) },
                onActionClick = {
                    onActionClick(reservation, it)
                }
            )
        }
        item {
            Spacer(modifier = Modifier.height(24.dp))
        }
    }
}

@Composable
fun ReservationCard(
    reservation: Reservation,
    userRole: UserRole?,
    onRateClick: () -> Unit = {},
    onActionClick: (String) -> Unit
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
    ) {
        Column(modifier = Modifier.padding(AdaptiveTheme.spacing.large)) {
            Text(
                text = reservation.serviceName,
                style = MaterialTheme.typography.titleLarge.copy(
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.onSurface
                )
            )
            Spacer(modifier = Modifier.height(AdaptiveTheme.spacing.small))
            Text(
                text = "Fecha: ${reservation.date} a las ${reservation.startTime}",
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
            Spacer(modifier = Modifier.height(AdaptiveTheme.spacing.medium))
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.Start,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "Estado: ",
                    style = MaterialTheme.typography.bodyMedium,
                    fontWeight = FontWeight.SemiBold,
                    color = MaterialTheme.colorScheme.primary
                )
                Box(
                    modifier = Modifier
                        .background(
                            (reservation.status.color
                                ?: MaterialTheme.colorScheme.outline).copy(alpha = 0.1f),
                            RoundedCornerShape(8.dp)
                        )
                        .padding(horizontal = 12.dp, vertical = 6.dp)
                ) {
                    Text(
                        text = reservation.status.label,
                        color = reservation.status.color
                            ?: MaterialTheme.colorScheme.outline,
                        style = MaterialTheme.typography.bodyMedium,
                        fontWeight = FontWeight.SemiBold,
                    )
                }
            }
            Spacer(modifier = Modifier.height(AdaptiveTheme.spacing.medium))
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.Start,
            ) {
                Text(
                    text = "Valor: ",
                    style = MaterialTheme.typography.bodyMedium,
                    fontWeight = FontWeight.SemiBold,
                    color = MaterialTheme.colorScheme.primary
                )

                Text(
                    text = FormatsHelpers.formatCurrencyCOP(reservation.price),
                    style = MaterialTheme.typography.bodyLarge,
                    fontWeight = FontWeight.Bold
                )
            }
            Spacer(modifier = Modifier.height(AdaptiveTheme.spacing.medium))
            ReservationActionButtons(
                reservation = reservation,
                userRole = userRole,
                onRateClick = onRateClick,
                onActionClick = onActionClick,
                onDetailClick = { /* ver detalle */ }
            )

        }
    }
}

@Composable
fun ReservationActionButtons(
    reservation: Reservation,
    userRole: UserRole?,
    onRateClick: () -> Unit,
    onDetailClick: () -> Unit,
    onActionClick: (String) -> Unit,
    modifier: Modifier = Modifier
) {
    Row(
        modifier = modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        val hasRated = reservation.ratings.any { it.role == userRole }

        when (userRole) {
            UserRole.CLIENT -> {
                if (reservation.status in listOf(
                        ReservationStatus.PENDING_ASSIGNMENT,
                        ReservationStatus.PENDING_PAYMENT,
                        ReservationStatus.PENDING_CONFIRMATION,
                        ReservationStatus.CONFIRMED
                    )) {
                    CancelButton { onActionClick("cancel") }
                }
                if (reservation.status == ReservationStatus.PENDING_PAYMENT) {
                    ActionButton(text = "Pagar") { onActionClick("pay") }
                }
                if (reservation.status == ReservationStatus.COMPLETED) {
                    RateButton(onClick = onRateClick, enabled = !hasRated)
                }
            }
            UserRole.COLLABORATOR -> {
                when (reservation.status) {
                    ReservationStatus.CONFIRMED -> ActionButton(text = "Empezar") { onActionClick("start") }
                    ReservationStatus.IN_PROGRESS -> ActionButton(text = "Completar") { onActionClick("complete") }
                    ReservationStatus.COMPLETED -> RateButton(onClick = onRateClick, enabled = !hasRated)
                    else -> {}
                }
            }
            UserRole.ADMIN -> {
                if (reservation.status == ReservationStatus.PENDING_ASSIGNMENT) {
                    ActionButton(text = "Asignar") { onActionClick("assign") }
                }
                if (reservation.status == ReservationStatus.PENDING_CONFIRMATION) {
                    ActionButton(text = "Confirmar") { onActionClick("confirm") }
                }
                if (reservation.status in listOf(ReservationStatus.PENDING_PAYMENT, ReservationStatus.CONFIRMED, ReservationStatus.IN_PROGRESS)) {
                    CancelButton { onActionClick("cancel") }
                }
                if (reservation.status == ReservationStatus.COMPLETED) {
                    RateButton(onClick = onRateClick, enabled = !hasRated)
                }
            }
            else -> {}
        }

        DetailButton(onClick = onDetailClick)
    }
}

@Composable
private fun CancelButton(onClick: () -> Unit) {
    TextButton(
        onClick = onClick,
        contentPadding = PaddingValues(horizontal = 8.dp, vertical = 4.dp)
    ) {
        Text(
            text = "Cancelar",
            style = MaterialTheme.typography.labelSmall,
            color = MaterialTheme.colorScheme.error.copy(alpha = 0.8f)
        )
    }
}

@Composable
private fun RateButton(onClick: () -> Unit, enabled: Boolean) {
    Button(
        onClick = onClick,
        enabled = enabled,
        shape = RoundedCornerShape(12.dp),
        contentPadding = PaddingValues(horizontal = 12.dp, vertical = 6.dp),
        colors = ButtonDefaults.buttonColors(
            containerColor = MaterialTheme.colorScheme.secondary.copy(alpha = 0.15f),
            contentColor = MaterialTheme.colorScheme.secondary
        ),
        elevation = ButtonDefaults.buttonElevation(defaultElevation = 0.dp)
    ) {
        Text(
            text = "Calificar",
            style = MaterialTheme.typography.labelSmall,
            fontWeight = FontWeight.Medium
        )
    }
}

@Composable
private fun ActionButton(text: String, onClick: () -> Unit) {
    Button(
        onClick = onClick,
        shape = RoundedCornerShape(12.dp),
        contentPadding = PaddingValues(horizontal = 12.dp, vertical = 6.dp)
    ) {
        Text(
            text = text,
            style = MaterialTheme.typography.labelSmall,
            fontWeight = FontWeight.Medium
        )
    }
}

@Composable
private fun DetailButton(
    onClick: () -> Unit
) {
    TextButton(
        onClick = onClick,
        contentPadding = PaddingValues(horizontal = 8.dp, vertical = 4.dp)
    ) {
        Text(
            text = "Ver detalle",
            style = MaterialTheme.typography.labelSmall,
            color = MaterialTheme.colorScheme.primary
        )
    }
}
