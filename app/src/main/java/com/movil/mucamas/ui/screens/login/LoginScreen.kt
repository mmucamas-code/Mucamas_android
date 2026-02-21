package com.movil.mucamas.ui.screens.login

import android.Manifest
import android.content.pm.PackageManager
import android.os.Build
import android.widget.Toast
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.background
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
import com.movil.mucamas.ui.viewmodels.LoginViewModel
import com.movil.mucamas.ui.viewmodels.OtpVerificationState

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun LoginScreen(
    onLoginSuccess: () -> Unit = {},
    onSignUpClick: () -> Unit = {},
    loginViewModel: LoginViewModel = viewModel()
) {
    var identification by remember { mutableStateOf("") }
    val loginState by loginViewModel.otpLoginState.collectAsState()
    val verifyState by loginViewModel.otpVerifyState.collectAsState()
    val context = LocalContext.current

    var showOtpDialog by remember { mutableStateOf(false) }

    // --- Observadores de Estado ---

    LaunchedEffect(loginState) {
        when (val state = loginState) {
            is OtpLoginState.OtpSent -> {
                showOtpDialog = true
                Toast.makeText(context, "Código OTP enviado a tu correo.", Toast.LENGTH_SHORT).show()
            }
            is OtpLoginState.InvalidId -> {
                Toast.makeText(context, "Usuario no encontrado. Por favor, regístrate.", Toast.LENGTH_LONG).show()
                loginViewModel.resetStartState()
                onSignUpClick()
            }
            is OtpLoginState.EmailSendError -> {
                Toast.makeText(context, "Error al enviar el correo. Intenta de nuevo.", Toast.LENGTH_LONG).show()
                loginViewModel.resetStartState()
            }
            is OtpLoginState.GenericError -> {
                Toast.makeText(context, state.message, Toast.LENGTH_LONG).show()
                loginViewModel.resetStartState()
            }
            else -> { /* No-op para Loading y null */ }
        }
    }

    LaunchedEffect(verifyState) {
        when (val state = verifyState) {
            is OtpVerificationState.Success -> {
                Toast.makeText(context, "¡Login Exitoso!", Toast.LENGTH_SHORT).show()
                showOtpDialog = false
                loginViewModel.resetVerifyState()
                loginViewModel.resetStartState()
                onLoginSuccess()
            }
            is OtpVerificationState.Error -> {
                Toast.makeText(context, state.message, Toast.LENGTH_SHORT).show()
                // No reseteamos el diálogo, para que el usuario pueda reintentar
            }
            else -> { /* No-op para Loading y null */ }
        }
    }

    // --- UI --- 
    
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
                Text("Login", style = MaterialTheme.typography.headlineLarge.copy(fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.onSurface, fontSize = typography.headline), textAlign = TextAlign.Center)
                Spacer(modifier = Modifier.height(spacing.small))
                Text("Welcome back, you've been missed!", style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Normal, color = MaterialTheme.colorScheme.onSurfaceVariant, fontSize = typography.body), textAlign = TextAlign.Center)
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
                    trailingIcon = { Icon(Icons.Default.Email, contentDescription = null, tint = MaterialTheme.colorScheme.onSurfaceVariant) }
                )
                Spacer(modifier = Modifier.height(spacing.large))

                Button(
                    onClick = { loginViewModel.startOtpLoginFlow(context,identification) },
                    modifier = Modifier.fillMaxWidth().height(dimens.buttonHeight),
                    enabled = loginState !is OtpLoginState.Loading,
                    colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.primary, contentColor = MaterialTheme.colorScheme.onPrimary),
                    shape = RoundedCornerShape(dimens.cornerRadius)
                ) {
                    if (loginState is OtpLoginState.Loading) {
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

        if (showOtpDialog) {
            val isLoading = verifyState is OtpVerificationState.Loading
            OtpDialog(
                isLoading = isLoading,
                onDismissRequest = { 
                    showOtpDialog = false
                    loginViewModel.resetStartState()
                },
                onVerify = { enteredOtp ->
                    loginViewModel.verifyOtp(identification, enteredOtp)
                }
            )
        }
    }
}

// El OtpDialog y otros componentes de diálogo se mantienen igual que antes.
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun OtpDialog(
    isLoading: Boolean,
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
                Text("Ingresa el código de 4 dígitos enviado a tu correo.", style = MaterialTheme.typography.bodyMedium.copy(color = MaterialTheme.colorScheme.onSurfaceVariant), textAlign = TextAlign.Center)
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
                        enabled = otpValues.all { it.isNotEmpty() } && !isLoading,
                        colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.primary, contentColor = MaterialTheme.colorScheme.onPrimary)
                    ) {
                        if (isLoading) {
                            CircularProgressIndicator(modifier = Modifier.size(20.dp), color = MaterialTheme.colorScheme.onPrimary)
                        } else {
                            Text("Verificar")
                        }
                    }
                }
            }
        }
    }
}
