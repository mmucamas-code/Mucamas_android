package com.movil.mucamas.ui.screens.login

import android.Manifest
import android.content.pm.PackageManager
import android.os.Build
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
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Email
import androidx.compose.material3.BasicAlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
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
import androidx.compose.ui.focus.FocusRequester
import androidx.compose.ui.focus.focusRequester
import androidx.compose.ui.input.key.Key
import androidx.compose.ui.input.key.key
import androidx.compose.ui.input.key.onKeyEvent
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.core.content.ContextCompat
import androidx.lifecycle.viewmodel.compose.viewModel
import com.movil.mucamas.ui.utils.AdaptiveTheme
import com.movil.mucamas.ui.viewmodels.LoginUiState
import com.movil.mucamas.ui.viewmodels.LoginViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun LoginScreen(
    onLoginSuccess: () -> Unit = {},
    onSignUpClick: () -> Unit = {},
    viewModel: LoginViewModel = viewModel()
) {
    var identification by remember { mutableStateOf("") }
    val uiState by viewModel.uiState.collectAsState()
    val context = LocalContext.current

    // Launcher para solicitar el permiso de notificaciones
    val notificationPermissionLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.RequestPermission(),
        onResult = { isGranted ->
            if (isGranted) {
                // Permiso concedido, proceder a buscar usuario
                viewModel.findUserById(identification, context)
            } else {
                // Permiso denegado, mostrar mensaje
                Toast.makeText(context, "El permiso de notificaciones es necesario para el login", Toast.LENGTH_LONG).show()
            }
        }
    )

    // Lanzamos efecto cuando cambia el estado para mostrar OTP o errores
    LaunchedEffect(uiState) {
        if (uiState is LoginUiState.UserFound) {
            // El OTP se muestra como alerta, el estado lo controla `showOtpDialog`
        }
    }

    // Estado local para la visibilidad de los diálogos
    var showOtpDialog by remember { mutableStateOf(false) }
    var showErrorDialog by remember { mutableStateOf(false) }

    // Observador para abrir los diálogos
    LaunchedEffect(uiState) {
        showOtpDialog = uiState is LoginUiState.UserFound
        showErrorDialog = uiState is LoginUiState.UserNotFound
    }
    
    val spacing = AdaptiveTheme.spacing
    val dimens = AdaptiveTheme.dimens
    val typography = AdaptiveTheme.typography

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background),
        contentAlignment = Alignment.Center
    ) {
        Card(
            modifier = Modifier
                .fillMaxWidth()
                .padding(spacing.large),
            shape = RoundedCornerShape(dimens.cornerRadius),
            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
            elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
        ) {
            Column(
                modifier = Modifier.padding(spacing.extraLarge),
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.Center
            ) {
                // Contenido de la UI sin cambios...
                Text(
                    text = "Login",
                    style = MaterialTheme.typography.headlineLarge.copy(fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.onSurface, fontSize = typography.headline),
                    textAlign = TextAlign.Center
                )
                Spacer(modifier = Modifier.height(spacing.small))
                Text(
                    text = "Welcome back, you've been missed!",
                    style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Normal, color = MaterialTheme.colorScheme.onSurfaceVariant, fontSize = typography.body),
                    textAlign = TextAlign.Center
                )
                Spacer(modifier = Modifier.height(spacing.extraLarge))
                OutlinedTextField(
                    value = identification,
                    onValueChange = { identification = it },
                    label = { Text("Identificación") },
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(dimens.cornerRadius),
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedBorderColor = MaterialTheme.colorScheme.primary,
                        unfocusedBorderColor = MaterialTheme.colorScheme.outline,
                        focusedLabelColor = MaterialTheme.colorScheme.primary,
                        unfocusedLabelColor = MaterialTheme.colorScheme.onSurfaceVariant
                    ),
                    singleLine = true,
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                    trailingIcon = {
                        Icon(Icons.Default.Email, contentDescription = null, tint = MaterialTheme.colorScheme.onSurfaceVariant)
                    }
                )
                Spacer(modifier = Modifier.height(spacing.large))

                // Botón "Continuar" que ahora solicita permiso si es necesario
                Button(
                    onClick = {
                        // Verificar si el permiso ya está concedido
                        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
                            when (ContextCompat.checkSelfPermission(context, Manifest.permission.POST_NOTIFICATIONS)) {
                                PackageManager.PERMISSION_GRANTED -> {
                                    // Permiso ya concedido, buscar usuario
                                    viewModel.findUserById(identification, context)
                                }
                                else -> {
                                    // Solicitar permiso
                                    notificationPermissionLauncher.launch(Manifest.permission.POST_NOTIFICATIONS)
                                }
                            }
                        } else {
                            // Versiones anteriores a Android 13 no necesitan permiso
                            viewModel.findUserById(identification, context)
                        }
                    },
                    modifier = Modifier.fillMaxWidth().height(dimens.buttonHeight),
                    enabled = uiState !is LoginUiState.Loading,
                    colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.primary, contentColor = MaterialTheme.colorScheme.onPrimary),
                    shape = RoundedCornerShape(dimens.cornerRadius)
                ) {
                    if (uiState is LoginUiState.Loading) {
                        CircularProgressIndicator(modifier = Modifier.size(24.dp), color = MaterialTheme.colorScheme.onPrimary)
                    } else {
                        Text("Continuar", fontSize = typography.button, fontWeight = FontWeight.Bold)
                    }
                }

                Spacer(modifier = Modifier.height(spacing.medium))
                TextButton(onClick = onSignUpClick) {
                    Text("Create new account", style = MaterialTheme.typography.bodyLarge.copy(fontWeight = FontWeight.Medium, color = MaterialTheme.colorScheme.onSurface))
                }
            }
        }

        // --- DIÁLOGOS --- 

        // Diálogo de OTP
        if (showOtpDialog) {
            OtpDialog(
                onDismissRequest = { viewModel.resetState() },
                onVerify = { enteredOtp ->
                    if (viewModel.verifyOtp(enteredOtp)) {
                        Toast.makeText(context, "Login Exitoso", Toast.LENGTH_SHORT).show()
                        viewModel.resetState()
                        onLoginSuccess()
                    } else {
                        Toast.makeText(context, "Código OTP incorrecto", Toast.LENGTH_SHORT).show()
                    }
                }
            )
        }

        // Diálogo de Usuario No Encontrado
        if (showErrorDialog) {
            UserNotFoundDialog(
                onDismiss = { viewModel.resetState() },
                onRegister = {
                    viewModel.resetState()
                    onSignUpClick()
                }
            )
        }
    }
}

// --- COMPONENTES DE DIÁLOGO --- 

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun OtpDialog(
    onDismissRequest: () -> Unit,
    onVerify: (String) -> Unit
) {
    val spacing = AdaptiveTheme.spacing
    val dimens = AdaptiveTheme.dimens
    val otpValues = remember { mutableStateListOf("", "", "", "") }
    val focusRequesters = remember { List(4) { FocusRequester() } }

    BasicAlertDialog(onDismissRequest = onDismissRequest) {
        Card(modifier = Modifier.fillMaxWidth().padding(spacing.medium), shape = RoundedCornerShape(dimens.cornerRadius), colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)) {
            Column(modifier = Modifier.padding(spacing.large), horizontalAlignment = Alignment.CenterHorizontally) {
                Text("Verificación", style = MaterialTheme.typography.headlineSmall.copy(fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.onSurface))
                Spacer(modifier = Modifier.height(spacing.small))
                Text("Ingresa el código de 4 dígitos enviado a tus notificaciones.", style = MaterialTheme.typography.bodyMedium.copy(color = MaterialTheme.colorScheme.onSurfaceVariant), textAlign = TextAlign.Center)
                Spacer(modifier = Modifier.height(spacing.large))
                Row(horizontalArrangement = Arrangement.Center, verticalAlignment = Alignment.CenterVertically, modifier = Modifier.fillMaxWidth()) {
                    for (i in 0 until 4) {
                        OutlinedTextField(
                            value = otpValues[i],
                            onValueChange = {
                                if (it.length <= 1 && it.all { char -> char.isDigit() }) {
                                    otpValues[i] = it
                                    if (it.isNotEmpty() && i < 3) focusRequesters[i + 1].requestFocus()
                                }
                            },
                            modifier = Modifier.size(56.dp).padding(horizontal = 4.dp).focusRequester(focusRequesters[i]).onKeyEvent {
                                if (it.key == Key.Backspace && otpValues[i].isEmpty() && i > 0) {
                                    focusRequesters[i - 1].requestFocus()
                                    true
                                } else false
                            },
                            textStyle = MaterialTheme.typography.headlineSmall.copy(textAlign = TextAlign.Center, fontWeight = FontWeight.Bold),
                            singleLine = true,
                            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                            shape = RoundedCornerShape(12.dp),
                            colors = OutlinedTextFieldDefaults.colors(focusedBorderColor = MaterialTheme.colorScheme.primary, unfocusedBorderColor = MaterialTheme.colorScheme.outline)
                        )
                    }
                }
                LaunchedEffect(Unit) { focusRequesters[0].requestFocus() }
                Spacer(modifier = Modifier.height(spacing.large))
                Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.End) {
                    TextButton(onClick = onDismissRequest) { Text("Cancelar", color = MaterialTheme.colorScheme.onSurfaceVariant) }
                    Spacer(modifier = Modifier.width(spacing.small))
                    Button(
                        onClick = { onVerify(otpValues.joinToString("")) },
                        enabled = otpValues.all { it.isNotEmpty() },
                        colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.primary, contentColor = MaterialTheme.colorScheme.onPrimary)
                    ) {
                        Text("Verify")
                    }
                }
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun UserNotFoundDialog(
    onDismiss: () -> Unit,
    onRegister: () -> Unit
) {
    BasicAlertDialog(onDismissRequest = onDismiss) {
        Card(
            modifier = Modifier.fillMaxWidth().padding(AdaptiveTheme.spacing.medium),
            shape = RoundedCornerShape(AdaptiveTheme.dimens.cornerRadius),
            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
        ) {
            Column(modifier = Modifier.padding(AdaptiveTheme.spacing.large), horizontalAlignment = Alignment.CenterHorizontally) {
                Text("Usuario no encontrado", style = MaterialTheme.typography.headlineSmall.copy(fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.onSurface))
                Spacer(modifier = Modifier.height(AdaptiveTheme.spacing.medium))
                Text("El número de identificación no está registrado. Por favor, crea una cuenta.", style = MaterialTheme.typography.bodyMedium.copy(color = MaterialTheme.colorScheme.onSurfaceVariant), textAlign = TextAlign.Center)
                Spacer(modifier = Modifier.height(AdaptiveTheme.spacing.large))
                Button(onClick = onRegister, modifier = Modifier.fillMaxWidth()) {
                    Text("Crear Cuenta")
                }
            }
        }
    }
}
