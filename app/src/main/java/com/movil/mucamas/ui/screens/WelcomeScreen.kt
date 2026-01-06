package com.movil.mucamas.ui.screens

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
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Home
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import com.movil.mucamas.ui.utils.AdaptiveTheme

@Composable
fun WelcomeScreen(
    onLoginClick: () -> Unit = {},
    onRegisterClick: () -> Unit = {}
) {
    // Uso simplificado de utilidades adaptativas
    val spacing = AdaptiveTheme.spacing
    val dimens = AdaptiveTheme.dimens
    val typography = AdaptiveTheme.typography

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background),
        contentAlignment = Alignment.Center
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .widthIn(max = dimens.maxContentWidth)
                .padding(horizontal = spacing.screenEdge, vertical = spacing.medium),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {
            Spacer(modifier = Modifier.weight(1f))

            // Ilustración Placeholder
            Box(
                modifier = Modifier
                    .size(dimens.iconLarge * 2.5f) // Escala proporcional
                    .background(MaterialTheme.colorScheme.primary.copy(alpha = 0.1f), CircleShape),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = Icons.Default.Home,
                    contentDescription = "Welcome Illustration",
                    modifier = Modifier.size(dimens.iconLarge * 1.5f),
                    tint = MaterialTheme.colorScheme.primary
                )
            }

            Spacer(modifier = Modifier.height(spacing.extraLarge))

            // Título Principal
            Text(
                text = "Tu hogar,\nen buenas manos",
                style = MaterialTheme.typography.displaySmall.copy(
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.onBackground,
                    fontSize = typography.headline
                ),
                textAlign = TextAlign.Center
            )

            Spacer(modifier = Modifier.height(spacing.medium))

            // Texto Descriptivo
            Text(
                text = "Encuentra profesionales de confianza para el cuidado y mantenimiento de tu hogar.",
                style = MaterialTheme.typography.bodyLarge.copy(
                    fontSize = typography.body
                ),
                textAlign = TextAlign.Center,
                color = MaterialTheme.colorScheme.onBackground.copy(alpha = 0.7f)
            )

            Spacer(modifier = Modifier.weight(1f))

            // Botones Contenedor (para limitar ancho en pantallas grandes)
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .widthIn(max = 400.dp), // Max width para botones en tablets
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                // Botón Login (Primario)
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
                    elevation = ButtonDefaults.buttonElevation(
                        defaultElevation = 2.dp,
                        pressedElevation = 4.dp
                    )
                ) {
                    Text(
                        text = "Iniciar Sesión",
                        fontSize = typography.button,
                        fontWeight = FontWeight.Bold
                    )
                }

                Spacer(modifier = Modifier.height(spacing.medium))

                // Botón Register (Secundario/Texto)
                TextButton(
                    onClick = onRegisterClick,
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(dimens.buttonHeight),
                    shape = RoundedCornerShape(dimens.cornerRadius)
                ) {
                    Text(
                        text = "Registrarme",
                        fontSize = typography.button,
                        fontWeight = FontWeight.SemiBold,
                        color = MaterialTheme.colorScheme.primary
                    )
                }
            }

            Spacer(modifier = Modifier.height(spacing.large))
        }
    }
}
