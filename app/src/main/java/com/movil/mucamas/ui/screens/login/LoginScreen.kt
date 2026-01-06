package com.movil.mucamas.ui.screens.login

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
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Email
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.ThumbUp
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
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
import com.movil.mucamas.ui.utils.AdaptiveTheme

@Composable
fun LoginScreen(
    onLoginClick: () -> Unit = {},
    onSignUpClick: () -> Unit = {}
) {
    var email by remember { mutableStateOf("") }
    
    // Acceso a utilidades adaptativas
    val spacing = AdaptiveTheme.spacing
    val dimens = AdaptiveTheme.dimens
    val typography = AdaptiveTheme.typography

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background), // Fondo desde el tema
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
                    text = "Login here",
                    style = MaterialTheme.typography.headlineLarge.copy(
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.primary, // Color primario (SoftBlack)
                        fontSize = typography.headline
                    ),
                    textAlign = TextAlign.Center
                )

                Spacer(modifier = Modifier.height(spacing.small))

                // Subtítulo
                Text(
                    text = "Welcome back you've been missed!",
                    style = MaterialTheme.typography.titleMedium.copy(
                        fontWeight = FontWeight.SemiBold,
                        color = MaterialTheme.colorScheme.onSurfaceVariant, // Texto secundario
                        fontSize = typography.body
                    ),
                    textAlign = TextAlign.Center
                )

                Spacer(modifier = Modifier.height(spacing.extraLarge))

                // Input único
                OutlinedTextField(
                    value = email,
                    onValueChange = { email = it },
                    label = { Text("Email or Phone") },
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(dimens.cornerRadius),
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedBorderColor = MaterialTheme.colorScheme.primary,
                        unfocusedBorderColor = MaterialTheme.colorScheme.outline,
                        focusedLabelColor = MaterialTheme.colorScheme.primary,
                        unfocusedLabelColor = MaterialTheme.colorScheme.onSurfaceVariant
                    ),
                    singleLine = true,
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Email),
                    trailingIcon = {
                        Icon(
                            Icons.Default.Email, 
                            contentDescription = null, 
                            tint = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                )

                Spacer(modifier = Modifier.height(spacing.large))

                // Botón Sign In
                Button(
                    onClick = onLoginClick,
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(dimens.buttonHeight),
                    colors = ButtonDefaults.buttonColors(
                        containerColor = MaterialTheme.colorScheme.primary,
                        contentColor = MaterialTheme.colorScheme.onPrimary
                    ),
                    shape = RoundedCornerShape(dimens.cornerRadius),
                    elevation = ButtonDefaults.buttonElevation(defaultElevation = 2.dp)
                ) {
                    Text(
                        text = "Sign in",
                        fontSize = typography.button,
                        fontWeight = FontWeight.Bold
                    )
                }

                Spacer(modifier = Modifier.height(spacing.medium))

                // Crear nueva cuenta
                TextButton(
                    onClick = onSignUpClick
                ) {
                    Text(
                        text = "Create new account",
                        style = MaterialTheme.typography.bodyLarge.copy(
                            fontWeight = FontWeight.SemiBold,
                            color = MaterialTheme.colorScheme.onSurface
                        )
                    )
                }

                Spacer(modifier = Modifier.height(spacing.medium))

                // Separador
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    HorizontalDivider(
                        modifier = Modifier.weight(1f), 
                        color = MaterialTheme.colorScheme.outline.copy(alpha = 0.5f)
                    )
                    Text(
                        text = "Or continue with",
                        modifier = Modifier.padding(horizontal = spacing.medium),
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                    HorizontalDivider(
                        modifier = Modifier.weight(1f), 
                        color = MaterialTheme.colorScheme.outline.copy(alpha = 0.5f)
                    )
                }

                Spacer(modifier = Modifier.height(spacing.large))

                // Social Icons
                Row(
                    horizontalArrangement = Arrangement.spacedBy(spacing.large),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    SocialIcon(Icons.Default.Person)
                    SocialIcon(Icons.Default.ThumbUp)
                }
            }
        }
    }
}

@Composable
fun SocialIcon(icon: ImageVector) {
    val dimens = AdaptiveTheme.dimens
    val spacing = AdaptiveTheme.spacing
    
    Box(
        modifier = Modifier
            .size(dimens.buttonHeight)
            .background(
                MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f), 
                RoundedCornerShape(dimens.cornerRadius)
            )
            .clickable { /* Social Login */ },
        contentAlignment = Alignment.Center
    ) {
        Icon(
            imageVector = icon,
            contentDescription = null,
            modifier = Modifier
                .size(dimens.iconSmall)
                .padding(spacing.small),
            tint = MaterialTheme.colorScheme.onSurfaceVariant
        )
    }
}
