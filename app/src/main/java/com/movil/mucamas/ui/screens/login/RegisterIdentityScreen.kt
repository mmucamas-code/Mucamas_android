package com.movil.mucamas.ui.screens.login

import android.widget.Toast
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
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.movil.mucamas.ui.utils.AdaptiveTheme
import com.movil.mucamas.ui.viewmodels.RegisterViewModel
import com.movil.mucamas.ui.viewmodels.RegistrationUiState

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun RegisterIdentityScreen(
    onRegistrationSuccess: () -> Unit = {},
    onLoginClick: () -> Unit = {},
    viewModel: RegisterViewModel = viewModel()
) {
    val uiState by viewModel.uiState.collectAsState()
    val context = LocalContext.current

    var identification by remember { mutableStateOf("") }
    var fullName by remember { mutableStateOf("") }
    var phone by remember { mutableStateOf("") }
    var email by remember { mutableStateOf("") }
    var address by remember { mutableStateOf("") }
    
    var showOtpDialog by remember { mutableStateOf(false) }
    var showUserExistsDialog by remember { mutableStateOf(false) }

    LaunchedEffect(uiState) {
        val state = uiState
        showOtpDialog = state is RegistrationUiState.RegistrationSuccess
        showUserExistsDialog = state is RegistrationUiState.UserAlreadyExists
        
        if (state is RegistrationUiState.RegistrationSuccess) {
            // El diálogo OTP se mostrará automáticamente
        } else if (state is RegistrationUiState.UserAlreadyExists) {
            Toast.makeText(context, state.message, Toast.LENGTH_LONG).show()
            viewModel.resetState()
        }
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
            elevation = CardDefaults.cardElevation(defaultElevation = 4.dp)
        ) {
            Column(
                modifier = Modifier.padding(spacing.extraLarge),
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.Center
            ) {
                Text(
                    text = "Create Account",
                    style = MaterialTheme.typography.headlineLarge.copy(fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.primary, fontSize = typography.headline),
                    textAlign = TextAlign.Center
                )
                Spacer(modifier = Modifier.height(spacing.small))
                Text(
                    text = "Create an account so you can explore all the existing jobs",
                    style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.SemiBold, color = MaterialTheme.colorScheme.onSurfaceVariant, fontSize = typography.body),
                    textAlign = TextAlign.Center
                )
                Spacer(modifier = Modifier.height(spacing.extraLarge))

                RegisterInput(value = identification, onValueChange = { identification = it }, label = "Identification", icon = Icons.Default.AccountCircle, keyboardType = KeyboardType.Number)
                Spacer(modifier = Modifier.height(spacing.medium))
                RegisterInput(value = fullName, onValueChange = { fullName = it }, label = "Full Name", icon = Icons.Default.Person, keyboardType = KeyboardType.Text)
                Spacer(modifier = Modifier.height(spacing.medium))
                RegisterInput(value = phone, onValueChange = { phone = it }, label = "Phone", icon = Icons.Default.Phone, keyboardType = KeyboardType.Phone)
                Spacer(modifier = Modifier.height(spacing.medium))
                RegisterInput(value = email, onValueChange = { email = it }, label = "Email", icon = Icons.Default.Email, keyboardType = KeyboardType.Email)

                Spacer(modifier = Modifier.height(spacing.large))

                Button(
                    onClick = { 
                        viewModel.registerUser(identification, fullName, phone, email, address, context)
                    },
                    modifier = Modifier.fillMaxWidth().height(dimens.buttonHeight),
                    colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.primary, contentColor = MaterialTheme.colorScheme.onPrimary),
                    shape = RoundedCornerShape(dimens.cornerRadius),
                    enabled = uiState !is RegistrationUiState.Loading
                ) {
                    if (uiState is RegistrationUiState.Loading) {
                        CircularProgressIndicator(modifier = Modifier.size(24.dp), color = MaterialTheme.colorScheme.onPrimary)
                    } else {
                        Text("Sign up", fontSize = typography.button, fontWeight = FontWeight.Bold)
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

                TextButton(
                    onClick = onLoginClick,
                    enabled = uiState !is RegistrationUiState.Loading
                ) {
                    Text(
                        text = "Already have an account",
                        style = MaterialTheme.typography.bodyLarge.copy(fontWeight = FontWeight.SemiBold, color = MaterialTheme.colorScheme.onSurface)
                    )
                }
            }
        }
        
        if (showOtpDialog) {
            OtpDialog(
                onDismissRequest = { 
                    viewModel.resetState()
                    onLoginClick() // Si cancela, lo mandamos a Login
                },
                onVerify = { enteredOtp ->
                    if (viewModel.verifyOtpAndLogin(enteredOtp, context)) {
                        Toast.makeText(context, "Registro y Login exitosos!", Toast.LENGTH_SHORT).show()
                        viewModel.resetState()
                        onRegistrationSuccess() // Navega al Home
                    } else {
                        Toast.makeText(context, "Código OTP incorrecto", Toast.LENGTH_SHORT).show()
                    }
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
