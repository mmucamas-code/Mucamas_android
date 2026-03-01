package com.movil.mucamas.ui.screens.myreservations

import android.net.Uri
import android.util.Log
import android.widget.Toast
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AccessTime
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.ContentCopy
import androidx.compose.material.icons.filled.FilterList
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalClipboardManager
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.AnnotatedString
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.movil.mucamas.ui.components.EmptyStateView
import com.movil.mucamas.ui.components.FullScreenLoading
import com.movil.mucamas.ui.models.*
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
    var showPaymentModal by remember { mutableStateOf(false) }
    var reservationToHandle by remember { mutableStateOf<Reservation?>(null) }
    
    val context = LocalContext.current

    LaunchedEffect(key1 = true) {
        viewModel.eventFlow.collect { event ->
            when (event) {
                is ReservationUiEvent.ShowError -> {
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
                is ReservationUiEvent.PaymentProcessed -> {
                    Toast.makeText(context, "Pago enviado a revisión", Toast.LENGTH_SHORT).show()
                    showPaymentModal = false
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
        Column(modifier = Modifier.fillMaxSize()) {
            Text(
                text = "Mis Reservas",
                style = MaterialTheme.typography.headlineMedium.copy(
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.onBackground
                )
            )
            
            FilterSection(
                showOnlyToday = uiState.showOnlyToday,
                onTodayToggle = { viewModel.toggleTodayFilter() },
                selectedStatus = uiState.statusFilter,
                onStatusSelected = { viewModel.setStatusFilter(it) }
            )

            if (uiState.isLoading) {
                Box(Modifier.weight(1f).fillMaxWidth()) { FullScreenLoading() }
            } else if (uiState.isEmpty) {
                Box(Modifier.weight(1f).fillMaxWidth()) { EmptyStateView() }
            } else {
                ReservationsList(
                    modifier = Modifier.weight(1f),
                    reservations = uiState.reservations,
                    userRole = userSession?.role,
                    onRateClick = {
                        selectedServiceToRate = it
                        showRateModal = true
                    },
                    onActionClick = { reservation, action ->
                        reservationToHandle = reservation
                        when (action) {
                            "assign" -> viewModel.onAssignCollaboratorClicked()
                            "pay" -> showPaymentModal = true
                            "confirm" -> viewModel.confirmReservation(reservation.id)
                            "start" -> viewModel.startReservation(reservation.id)
                            "complete" -> viewModel.completeReservation(reservation)
                            "cancel" -> viewModel.cancelReservation(reservation)
                            else -> {}
                        }
                    }
                )
            }
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

        if (showCollaboratorSelector && reservationToHandle != null) {
            CollaboratorSelectionDialog(
                collaborators = uiState.collaborators,
                onDismiss = { showCollaboratorSelector = false },
                onConfirm = { collaboratorId ->
                    viewModel.assignCollaboratorToReservation(reservationToHandle!!.id, collaboratorId)
                    showCollaboratorSelector = false
                }
            )
        }

        if (showPaymentModal && reservationToHandle != null) {
            PaymentModal(
                reservation = reservationToHandle!!,
                onDismiss = { showPaymentModal = false },
                onReceiptAttached = { uri ->
                    viewModel.processPayment(reservationToHandle!!.id, uri)
                }
            )
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun FilterSection(
    showOnlyToday: Boolean,
    onTodayToggle: () -> Unit,
    selectedStatus: ReservationStatus?,
    onStatusSelected: (ReservationStatus?) -> Unit
) {
    Column(modifier = Modifier.padding(vertical = 8.dp)) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            FilterChip(
                selected = showOnlyToday,
                onClick = onTodayToggle,
                label = { Text("Hoy") }
            )
            
            var expanded by remember { mutableStateOf(false) }
            Box {
                FilterChip(
                    selected = selectedStatus != null,
                    onClick = { expanded = true },
                    label = { Text(selectedStatus?.label ?: "Todos los estados") },
                    trailingIcon = { 
                        Icon(
                            imageVector = Icons.Default.FilterList, 
                            contentDescription = null, 
                            modifier = Modifier.size(18.dp)
                        ) 
                    }
                )
                DropdownMenu(expanded = expanded, onDismissRequest = { expanded = false }) {
                    DropdownMenuItem(
                        text = { Text("Todos") },
                        onClick = { onStatusSelected(null); expanded = false }
                    )
                    ReservationStatus.values().forEach { status ->
                        DropdownMenuItem(
                            text = { Text(status.label) },
                            onClick = { onStatusSelected(status); expanded = false }
                        )
                    }
                }
            }
        }
    }
}

@Composable
fun PaymentModal(
    reservation: Reservation,
    onDismiss: () -> Unit,
    onReceiptAttached: (Uri) -> Unit
) {
    val clipboardManager = LocalClipboardManager.current
    val accountManager = "0123456789 (Bancolombia)"
    val launcher = rememberLauncherForActivityResult(ActivityResultContracts.GetContent()) { uri ->
        uri?.let { onReceiptAttached(uri) }
    }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("Finalizar Pago") },
        text = {
            Column(
                modifier = Modifier.fillMaxWidth(),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Box(
                    Modifier
                        .size(180.dp)
                        .background(Color.LightGray, RoundedCornerShape(8.dp)),
                    contentAlignment = Alignment.Center
                ) {
                    Text("QR de Pago", color = Color.DarkGray)
                }
                
                Spacer(Modifier.height(16.dp))
                
                Card(
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Row(
                        modifier = Modifier.padding(12.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Column(modifier = Modifier.weight(1f)) {
                            Text("Número de cuenta:", style = MaterialTheme.typography.labelSmall)
                            Text(accountManager, fontWeight = FontWeight.Bold)
                        }
                        IconButton(onClick = { clipboardManager.setText(AnnotatedString(accountManager.split(" ")[0])) }) {
                            Icon(Icons.Default.ContentCopy, "Copiar")
                        }
                    }
                }
                
                Spacer(Modifier.height(16.dp))
                
                Button(
                    onClick = { launcher.launch("image/*") },
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Text("Adjuntar Comprobante")
                }
            }
        },
        confirmButton = {
            TextButton(onClick = onDismiss) { Text("Cancelar") }
        }
    )
}

@Composable
fun ReservationsList(
    reservations: List<Reservation>,
    userRole: UserRole?,
    modifier: Modifier = Modifier,
    onRateClick: (Reservation) -> Unit = {},
    onActionClick: (Reservation, String) -> Unit
) {
    LazyColumn(
        modifier = modifier.fillMaxSize(),
        verticalArrangement = Arrangement.spacedBy(AdaptiveTheme.spacing.medium)
    ) {
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
            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                Text(
                    text = reservation.serviceName,
                    style = MaterialTheme.typography.titleLarge.copy(
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.onSurface
                    )
                )
                Text(
                    text = FormatsHelpers.formatCurrencyCOP(reservation.price),
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.primary
                )
            }
            
            Spacer(modifier = Modifier.height(AdaptiveTheme.spacing.small))
            
            Text(
                text = "Fecha: ${reservation.date} a las ${reservation.startTime}",
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
            
            if (userRole == UserRole.ADMIN || userRole == UserRole.COLLABORATOR) {
                Text(
                    text = "Cliente: ${reservation.clientName}",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }

            Spacer(modifier = Modifier.height(AdaptiveTheme.spacing.medium))
            
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.Start,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Box(
                    modifier = Modifier
                        .background(
                            (reservation.status.color).copy(alpha = 0.1f),
                            RoundedCornerShape(8.dp)
                        )
                        .padding(horizontal = 12.dp, vertical = 6.dp)
                ) {
                    Text(
                        text = reservation.status.label,
                        color = reservation.status.color,
                        style = MaterialTheme.typography.bodyMedium,
                        fontWeight = FontWeight.SemiBold,
                    )
                }
                
                if (reservation.paymentStatus == PaymentStatus.PAID) {
                    Spacer(Modifier.width(8.dp))
                    Badge(containerColor = Color(0xFF4CAF50)) { Text("PAGADO", color = Color.White) }
                }
            }
            
            Spacer(modifier = Modifier.height(AdaptiveTheme.spacing.medium))
            
            ReservationActionButtons(
                reservation = reservation,
                userRole = userRole,
                onRateClick = onRateClick,
                onActionClick = onActionClick,
                onDetailClick = { /* Implementar navegación a detalle */ }
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
        horizontalArrangement = Arrangement.spacedBy(8.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        val hasRated = reservation.ratings.any { it.role == userRole }

        when (userRole) {
            UserRole.CLIENT -> {
                if (reservation.status == ReservationStatus.PENDING_PAYMENT) {
                    ActionButton(text = "Pagar") { onActionClick("pay") }
                }
                if (reservation.status in listOf(ReservationStatus.PENDING_ASSIGNMENT, ReservationStatus.PENDING_PAYMENT, ReservationStatus.PENDING_CONFIRMATION, ReservationStatus.CONFIRMED)) {
                    CancelButton { onActionClick("cancel") }
                }
                if (reservation.status == ReservationStatus.COMPLETED) {
                    RateButton(onClick = onRateClick, enabled = !hasRated)
                }
            }
            UserRole.COLLABORATOR -> {
                when (reservation.status) {
                    ReservationStatus.CONFIRMED -> ActionButton(text = "Empezar") { onActionClick("start") }
                    ReservationStatus.IN_PROGRESS -> ActionButton(text = "Finalizar") { onActionClick("complete") }
                    ReservationStatus.COMPLETED -> RateButton(onClick = onRateClick, enabled = !hasRated)
                    else -> {}
                }
            }
            UserRole.ADMIN -> {
                if (reservation.status == ReservationStatus.PENDING_ASSIGNMENT) {
                    ActionButton(text = "Asignar") { onActionClick("assign") }
                }
                if (reservation.status == ReservationStatus.PENDING_CONFIRMATION) {
                    ActionButton(text = "Confirmar Pago") { onActionClick("confirm") }
                }
                if (reservation.status == ReservationStatus.COMPLETED) {
                    RateButton(onClick = onRateClick, enabled = !hasRated)
                }
                if (reservation.status != ReservationStatus.COMPLETED && reservation.status != ReservationStatus.CANCELLED) {
                    CancelButton { onActionClick("cancel") }
                }
            }
            else -> {}
        }

        Spacer(Modifier.weight(1f))
        
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
            color = MaterialTheme.colorScheme.error
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
            text = "Detalle",
            style = MaterialTheme.typography.labelSmall,
            color = MaterialTheme.colorScheme.primary
        )
    }
}

@Composable
fun CollaboratorSelectionDialog(
    collaborators: List<Pair<UserDto, Collaborator?>>,
    onDismiss: () -> Unit,
    onConfirm: (String) -> Unit
) {
    AlertDialog(
        onDismissRequest = onDismiss,
        title = { 
            Text(
                "Asignar Colaborador",
                style = MaterialTheme.typography.headlineSmall,
                fontWeight = FontWeight.Bold
            ) 
        },
        text = {
            Box(Modifier.heightIn(max = 400.dp)) {
                LazyColumn(
                    verticalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    items(collaborators) { (user, collaborator) ->
                        CollaboratorItem(
                            user = user,
                            collaborator = collaborator,
                            onClick = { onConfirm(user.idNumber) }
                        )
                    }
                }
            }
        },
        confirmButton = {
            TextButton(onClick = onDismiss) { Text("Cerrar") }
        }
    )
}

@Composable
fun CollaboratorItem(
    user: UserDto,
    collaborator: Collaborator?,
    onClick: () -> Unit
) {
    val isAvailable = collaborator?.isAvailable ?: true
    val availableAt = collaborator?.availableAt
    
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .clickable { if(isAvailable){ onClick()} },
        colors = CardDefaults.cardColors(
            containerColor = if (isAvailable) 
                MaterialTheme.colorScheme.surface 
            else 
                MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f)
        ),
        border = BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant)
    ) {
        Row(
            modifier = Modifier
                .padding(16.dp)
                .fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically
        ) {
            // Avatar Placeholder con inicial
            Box(
                modifier = Modifier
                    .size(40.dp)
                    .background(MaterialTheme.colorScheme.primaryContainer, CircleShape),
                contentAlignment = Alignment.Center
            ) {
                Text(
                    user.fullName.take(1).uppercase(),
                    color = MaterialTheme.colorScheme.onPrimaryContainer,
                    fontWeight = FontWeight.Bold
                )
            }
            
            Spacer(Modifier.width(16.dp))
            
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = user.fullName,
                    style = MaterialTheme.typography.bodyLarge,
                    fontWeight = FontWeight.SemiBold
                )
                
                Row(verticalAlignment = Alignment.CenterVertically) {
                    val statusColor = if (isAvailable) Color(0xFF4CAF50) else Color(0xFFFFA000)
                    val statusIcon = if (isAvailable) Icons.Default.CheckCircle else Icons.Default.AccessTime
                    
                    Icon(
                        imageVector = statusIcon,
                        contentDescription = null,
                        tint = statusColor,
                        modifier = Modifier.size(14.dp)
                    )
                    
                    Spacer(Modifier.width(4.dp))
                    
                    Text(
                        text = if (isAvailable) "Disponible ahora" else "Ocupado",
                        style = MaterialTheme.typography.labelSmall,
                        color = statusColor
                    )
                }
                
                if (!isAvailable && availableAt != null) {
                    Text(
                        text = "Disponible aprox: ${FormatsHelpers.formatTimestamp(availableAt)}",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        fontSize = 11.sp
                    )
                }
            }
            
            if (isAvailable) {
                Icon(
                    imageVector = Icons.Default.CheckCircle,
                    contentDescription = null,
                    tint = MaterialTheme.colorScheme.primary,
                    modifier = Modifier.size(24.dp)
                )
            }
        }
    }
}
