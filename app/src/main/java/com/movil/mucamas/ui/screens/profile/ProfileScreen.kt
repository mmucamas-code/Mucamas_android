package com.movil.mucamas.ui.screens.profile

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ExitToApp
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.movil.mucamas.ui.models.UserAddress
import com.movil.mucamas.ui.utils.AdaptiveTheme
import com.movil.mucamas.ui.viewmodels.MainViewModel
import com.movil.mucamas.ui.viewmodels.ProfileViewModel

@Composable
fun ProfileScreen(
    onLogoutClick: () -> Unit = {},
    mainViewModel: MainViewModel = viewModel(),
    profileViewModel: ProfileViewModel = viewModel()
) {
    val spacing = AdaptiveTheme.spacing
    val typography = AdaptiveTheme.typography
    val uiState by profileViewModel.uiState.collectAsState()
    
    var showAddressDialog by remember { mutableStateOf(false) }
    var selectedAddress by remember { mutableStateOf<UserAddress?>(null) }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(horizontal = spacing.large)
            .verticalScroll(rememberScrollState())
    ) {
        // Header con Foto, Nombre y Rating
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .padding(top = 40.dp, bottom = 32.dp),
            contentAlignment = Alignment.Center
        ) {
            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                Box(
                    modifier = Modifier
                        .size(100.dp)
                        .background(MaterialTheme.colorScheme.primary.copy(alpha = 0.1f), CircleShape)
                        .border(2.dp, MaterialTheme.colorScheme.primary, CircleShape),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = Icons.Default.Person,
                        contentDescription = null,
                        modifier = Modifier.size(60.dp),
                        tint = MaterialTheme.colorScheme.primary
                    )
                }
                
                Spacer(modifier = Modifier.height(16.dp))
                
                Text(
                    text = uiState.user?.fullName ?: "Cargando...",
                    style = MaterialTheme.typography.headlineMedium.copy(
                        fontSize = typography.headline,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.onBackground
                    )
                )
                
                Spacer(modifier = Modifier.height(8.dp))
                
                RatingStars(rating = uiState.user?.rating ?: 5.0)
                
                Spacer(modifier = Modifier.height(4.dp))
                
                Text(
                    text = "Rol: ${uiState.user?.role ?: "-"} | ID: ${uiState.user?.idNumber ?: "-"}",
                    style = MaterialTheme.typography.bodyMedium.copy(
                        fontSize = typography.body,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                )
            }
        }

        Spacer(modifier = Modifier.height(24.dp))

        // Opciones de Menú
        Column {
            Text(
                text = "Cuenta",
                style = MaterialTheme.typography.titleMedium.copy(
                    fontSize = typography.title,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.onSurface
                ),
                modifier = Modifier.padding(bottom = 12.dp)
            )

            MenuOptionItem(
                icon = Icons.Default.Home, 
                title = "Mis direcciones", 
                onClick = { showAddressDialog = true }
            )
            
            Spacer(modifier = Modifier.height(12.dp))
            
            MenuOptionItem(icon = Icons.Default.Info, title = "Historial de servicios", onClick = { /* Navegar a historial (que reutiliza MyReservationsScreen) */ })

            Spacer(modifier = Modifier.height(32.dp))
            
            Text(
                text = "General",
                style = MaterialTheme.typography.titleMedium.copy(
                    fontSize = typography.title,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.onSurface
                ),
                modifier = Modifier.padding(bottom = 12.dp)
            )

            MenuOptionItem(icon = Icons.Default.Settings, title = "Soporte y Ayuda", onClick = { /* TODO */ })
            
            Spacer(modifier = Modifier.height(40.dp))

            // Botón Cerrar Sesión
            Button(
                onClick = onLogoutClick,
                modifier = Modifier.fillMaxWidth().height(56.dp),
                colors = ButtonDefaults.buttonColors(
                    containerColor = MaterialTheme.colorScheme.surface,
                    contentColor = MaterialTheme.colorScheme.error
                ),
                shape = RoundedCornerShape(28.dp),
                elevation = ButtonDefaults.buttonElevation(defaultElevation = 0.dp),
                border = BorderStroke(1.dp, MaterialTheme.colorScheme.error.copy(alpha = 0.2f))
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(Icons.AutoMirrored.Filled.ExitToApp, contentDescription = null)
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        text = "Cerrar sesión",
                        fontSize = typography.button,
                        fontWeight = FontWeight.Bold
                    )
                }
            }
        }
        
        Spacer(modifier = Modifier.height(80.dp))
    }

    if (showAddressDialog) {
        AddressManagementDialog(
            addresses = uiState.addresses,
            onDismiss = { showAddressDialog = false },
            onAdd = { profileViewModel.addAddress(it) },
            onUpdate = { profileViewModel.updateAddress(it) },
            onDelete = { profileViewModel.deleteAddress(it) },
            onSetDefault = { profileViewModel.setDefaultAddress(it) }
        )
    }
}

@Composable
fun RatingStars(rating: Double, modifier: Modifier = Modifier) {
    Row(modifier = modifier, verticalAlignment = Alignment.CenterVertically) {
        repeat(5) { index ->
            val color = if (index < rating.toInt()) Color(0xFFFFD700) else Color.LightGray
            Icon(
                imageVector = Icons.Default.Star,
                contentDescription = null,
                tint = color,
                modifier = Modifier.size(20.dp)
            )
        }
        Spacer(Modifier.width(8.dp))
        Text(text = "($rating)", style = MaterialTheme.typography.bodyMedium, fontWeight = FontWeight.Bold)
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AddressManagementDialog(
    addresses: List<UserAddress>,
    onDismiss: () -> Unit,
    onAdd: (UserAddress) -> Unit,
    onUpdate: (UserAddress) -> Unit,
    onDelete: (String) -> Unit,
    onSetDefault: (String) -> Unit
) {
    var showForm by remember { mutableStateOf(false) }
    var editingAddress by remember { mutableStateOf<UserAddress?>(null) }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("Mis Direcciones") },
        text = {
            Column(modifier = Modifier.fillMaxWidth()) {
                if (showForm) {
                    AddressForm(
                        address = editingAddress,
                        onSave = {
                            if (editingAddress == null) onAdd(it) else onUpdate(it.copy(id = editingAddress!!.id))
                            showForm = false
                            editingAddress = null
                        },
                        onCancel = { showForm = false; editingAddress = null }
                    )
                } else {
                    Button(
                        onClick = { showForm = true; editingAddress = null },
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Icon(Icons.Default.Add, contentDescription = null)
                        Spacer(Modifier.width(8.dp))
                        Text("Agregar Nueva")
                    }
                    
                    Spacer(Modifier.height(16.dp))
                    
                    Box(Modifier.heightIn(max = 300.dp)) {
                        Column(modifier = Modifier.verticalScroll(rememberScrollState())) {
                            addresses.forEach { addr ->
                                AddressItem(
                                    address = addr,
                                    onEdit = { editingAddress = addr; showForm = true },
                                    onDelete = { onDelete(addr.id) },
                                    onSetDefault = { onSetDefault(addr.id) }
                                )
                                Divider(Modifier.padding(vertical = 8.dp))
                            }
                        }
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
fun AddressItem(
    address: UserAddress,
    onEdit: () -> Unit,
    onDelete: () -> Unit,
    onSetDefault: () -> Unit
) {
    Column(modifier = Modifier.fillMaxWidth()) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            Icon(
                if (address.isDefault) Icons.Default.RadioButtonChecked else Icons.Default.RadioButtonUnchecked,
                contentDescription = null,
                modifier = Modifier.clickable { onSetDefault() },
                tint = if (address.isDefault) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.outline
            )
            Spacer(Modifier.width(12.dp))
            Column(Modifier.weight(1f)) {
                Text(address.street, fontWeight = FontWeight.Bold)
                Text("${address.neighborhood}, ${address.city}", style = MaterialTheme.typography.bodySmall)
            }
            IconButton(onClick = onEdit) { Icon(Icons.Default.Edit, contentDescription = null,  modifier = Modifier.size(20.dp) ) }
            IconButton(onClick = onDelete) { Icon(Icons.Default.Delete, contentDescription = null,  modifier = Modifier.size(20.dp), tint = Color.Red) }
        }
    }
}

@Composable
fun AddressForm(
    address: UserAddress?,
    onSave: (UserAddress) -> Unit,
    onCancel: () -> Unit
) {
    var street by remember { mutableStateOf(address?.street ?: "") }
    var neighborhood by remember { mutableStateOf(address?.neighborhood ?: "") }
    var notes by remember { mutableStateOf(address?.notes ?: "") }

    Column {
        TextField(value = street, onValueChange = { street = it }, label = { Text("Calle / Carrera") }, modifier = Modifier.fillMaxWidth())
        Spacer(Modifier.height(8.dp))
        TextField(value = neighborhood, onValueChange = { neighborhood = it }, label = { Text("Barrio") }, modifier = Modifier.fillMaxWidth())
        Spacer(Modifier.height(8.dp))
        TextField(value = notes, onValueChange = { notes = it }, label = { Text("Notas (Ej: Apt 201)") }, modifier = Modifier.fillMaxWidth())
        
        Spacer(Modifier.height(16.dp))
        
        Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.End) {
            TextButton(onClick = onCancel) { Text("Cancelar") }
            Button(onClick = { onSave(UserAddress(street = street, neighborhood = neighborhood, notes = notes)) }) {
                Text("Guardar")
            }
        }
    }
}

@Composable
fun MenuOptionItem(
    icon: ImageVector,
    title: String,
    onClick: () -> Unit
) {
    val typography = AdaptiveTheme.typography
    
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(16.dp))
            .background(MaterialTheme.colorScheme.surface)
            .clickable(onClick = onClick)
            .padding(16.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.SpaceBetween
    ) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            Box(
                modifier = Modifier
                    .size(40.dp)
                    .background(MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.3f), CircleShape),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = icon,
                    contentDescription = null,
                    tint = MaterialTheme.colorScheme.onSurfaceVariant,
                    modifier = Modifier.size(20.dp)
                )
            }
            Spacer(modifier = Modifier.width(16.dp))
            Text(
                text = title,
                style = MaterialTheme.typography.bodyLarge.copy(
                    fontSize = typography.body,
                    fontWeight = FontWeight.Medium,
                    color = MaterialTheme.colorScheme.onSurface
                )
            )
        }
        Icon(
            imageVector = Icons.Default.KeyboardArrowRight,
            contentDescription = null,
            tint = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.5f)
        )
    }
}
