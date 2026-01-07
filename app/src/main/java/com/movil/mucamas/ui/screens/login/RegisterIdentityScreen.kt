package com.movil.mucamas.ui.screens.login

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AccountCircle
import androidx.compose.material.icons.filled.Email
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.Phone
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
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
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.movil.mucamas.ui.utils.AdaptiveTheme
import com.movil.mucamas.ui.viewmodels.RegisterViewModel
import com.movil.mucamas.ui.viewmodels.RegistrationUiState

@Composable
fun RegisterIdentityScreen(
    onNextClick: () -> Unit = {},
    onLoginClick: () -> Unit = {},
    viewModel: RegisterViewModel = viewModel()
) {
    // Observamos el estado del ViewModel
    val uiState by viewModel.uiState.collectAsState()

    var identification by remember { mutableStateOf("") }
    var fullName by remember { mutableStateOf("") }
    var phone by remember { mutableStateOf("") }
    // Campo adicional para email, necesario para el registro
    var email by remember { mutableStateOf("") }
    // Campo adicional para dirección, necesario para el registro (temporalmente vacío o agregar input)
    var address by remember { mutableStateOf("") }
    
    var showOtpDialog by remember { mutableStateOf(false) }

    // Reacción a cambios de estado del registro
    LaunchedEffect(uiState) {
        if (uiState is RegistrationUiState.Success) {
            onNextClick() // Navegar al Home
            viewModel.resetState()
        }
    }

    // Acceso a utilidades adaptativas
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
            elevation = CardDefaults.cardElevation(defaultElevation = 4.dp)
        ) {
            Column(
                modifier = Modifier.padding(spacing.extraLarge),
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.Center
            ) {
                // Título
                Text(
                    text = "Create Account",
                    style = MaterialTheme.typography.headlineLarge.copy(
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.primary,
                        fontSize = typography.headline
                    ),
                    textAlign = TextAlign.Center
                )

                Spacer(modifier = Modifier.height(spacing.small))

                // Texto descriptivo
                Text(
                    text = "Create an account so you can explore all the existing jobs",
                    style = MaterialTheme.typography.titleMedium.copy(
                        fontWeight = FontWeight.SemiBold,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        fontSize = typography.body
                    ),
                    textAlign = TextAlign.Center
                )

                Spacer(modifier = Modifier.height(spacing.extraLarge))

                // Inputs
                RegisterInput(
                    value = identification,
                    onValueChange = { identification = it },
                    label = "Identification",
                    icon = Icons.Default.AccountCircle,
                    keyboardType = KeyboardType.Number
                )
                Spacer(modifier = Modifier.height(spacing.medium))
                
                RegisterInput(
                    value = fullName,
                    onValueChange = { fullName = it },
                    label = "Full Name",
                    icon = Icons.Default.Person,
                    keyboardType = KeyboardType.Text
                )
                Spacer(modifier = Modifier.height(spacing.medium))
                
                RegisterInput(
                    value = phone,
                    onValueChange = { phone = it },
                    label = "Phone",
                    icon = Icons.Default.Phone,
                    keyboardType = KeyboardType.Phone
                )
                Spacer(modifier = Modifier.height(spacing.medium))

                RegisterInput(
                    value = email,
                    onValueChange = { email = it },
                    label = "Email",
                    icon = Icons.Default.Email,
                    keyboardType = KeyboardType.Email
                )

                Spacer(modifier = Modifier.height(spacing.large))

                // Botón Sign Up (Abre OTP Dialog)
                Button(
                    onClick = { showOtpDialog = true },
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(dimens.buttonHeight),
                    colors = ButtonDefaults.buttonColors(
                        containerColor = MaterialTheme.colorScheme.primary,
                        contentColor = MaterialTheme.colorScheme.onPrimary
                    ),
                    shape = RoundedCornerShape(dimens.cornerRadius),
                    elevation = ButtonDefaults.buttonElevation(defaultElevation = 2.dp),
                    enabled = uiState !is RegistrationUiState.Loading
                ) {
                    if (uiState is RegistrationUiState.Loading) {
                        CircularProgressIndicator(
                            modifier = Modifier.size(24.dp),
                            color = MaterialTheme.colorScheme.onPrimary
                        )
                    } else {
                        Text(
                            text = "Sign up",
                            fontSize = typography.button,
                            fontWeight = FontWeight.Bold
                        )
                    }
                }
                
                if (uiState is RegistrationUiState.Error) {
                    Spacer(modifier = Modifier.height(spacing.small))
                    Text(
                        text = (uiState as RegistrationUiState.Error).message,
                        color = MaterialTheme.colorScheme.error,
                        style = MaterialTheme.typography.bodySmall,
                        textAlign = TextAlign.Center
                    )
                }

                Spacer(modifier = Modifier.height(spacing.medium))

                // Already have an account
                TextButton(
                    onClick = onLoginClick,
                    enabled = uiState !is RegistrationUiState.Loading
                ) {
                    Text(
                        text = "Already have an account",
                        style = MaterialTheme.typography.bodyLarge.copy(
                            fontWeight = FontWeight.SemiBold,
                            color = MaterialTheme.colorScheme.onSurface
                        )
                    )
                }
            }
        }
        
        if (showOtpDialog) {
            OtpDialog(
                onDismissRequest = { showOtpDialog = false },
                onVerify = {
                    showOtpDialog = false
                    // Al verificar OTP, llamamos al ViewModel para registrar
                    viewModel.registerUser(
                        idNumber = identification,
                        fullName = fullName,
                        phone = phone,
                        email = email,
                        address = address
                    )
                }
            )
        }
    }
}

@Composable
fun RegisterInput(
    value: String,
    onValueChange: (String) -> Unit,
    label: String,
    icon: ImageVector,
    keyboardType: KeyboardType = KeyboardType.Text
) {
    val dimens = AdaptiveTheme.dimens
    OutlinedTextField(
        value = value,
        onValueChange = onValueChange,
        label = { Text(label) },
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(dimens.cornerRadius),
        colors = OutlinedTextFieldDefaults.colors(
            focusedBorderColor = MaterialTheme.colorScheme.primary,
            unfocusedBorderColor = MaterialTheme.colorScheme.outline,
            focusedLabelColor = MaterialTheme.colorScheme.primary,
            unfocusedLabelColor = MaterialTheme.colorScheme.onSurfaceVariant
        ),
        singleLine = true,
        keyboardOptions = KeyboardOptions(keyboardType = keyboardType),
        trailingIcon = {
            Icon(icon, contentDescription = null, tint = MaterialTheme.colorScheme.onSurfaceVariant)
        }
    )
}
