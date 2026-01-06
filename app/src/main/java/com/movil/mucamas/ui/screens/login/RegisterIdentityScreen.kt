package com.movil.mucamas.ui.screens.login

import androidx.compose.foundation.background
import androidx.compose.foundation.border
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
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Face
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.movil.mucamas.ui.theme.OrangeAccent
import com.movil.mucamas.ui.theme.TurquoiseMain

@Composable
fun RegisterIdentityScreen(
    onNextClick: () -> Unit = {},
    onScanClick: () -> Unit = {},
    onGalleryClick: () -> Unit = {}
) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(24.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Top
    ) {
        Spacer(modifier = Modifier.height(40.dp))

        // Icono ilustrativo
        Box(
            modifier = Modifier
                .size(80.dp)
                .background(TurquoiseMain.copy(alpha = 0.1f), RoundedCornerShape(20.dp)),
            contentAlignment = Alignment.Center
        ) {
            Icon(
                imageVector = Icons.Default.Face,
                contentDescription = null,
                modifier = Modifier.size(40.dp),
                tint = TurquoiseMain
            )
        }

        Spacer(modifier = Modifier.height(24.dp))

        Text(
            text = "Verifica tu identidad",
            style = MaterialTheme.typography.headlineMedium.copy(
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.onBackground
            )
        )

        Spacer(modifier = Modifier.height(8.dp))

        Text(
            text = "Para seguridad de todos, necesitamos verificar tu documento de identidad oficial.",
            style = MaterialTheme.typography.bodyLarge,
            textAlign = TextAlign.Center,
            color = Color.Gray
        )

        Spacer(modifier = Modifier.height(48.dp))

        // Opción Escanear
        ActionCard(
            text = "Escanear cédula",
            onClick = onScanClick,
            isPrimary = true
        )

        Spacer(modifier = Modifier.height(16.dp))

        // Opción Galería
        ActionCard(
            text = "Subir desde galería",
            onClick = onGalleryClick,
            isPrimary = false
        )

        Spacer(modifier = Modifier.weight(1f))

        // Botón Siguiente
        Button(
            onClick = onNextClick,
            modifier = Modifier
                .fillMaxWidth()
                .height(56.dp),
            colors = ButtonDefaults.buttonColors(containerColor = OrangeAccent),
            shape = RoundedCornerShape(28.dp)
        ) {
            Text(
                text = "Siguiente",
                fontSize = 18.sp,
                fontWeight = FontWeight.Bold
            )
        }
        Spacer(modifier = Modifier.height(24.dp))
    }
}

@Composable
fun ActionCard(
    text: String,
    onClick: () -> Unit,
    isPrimary: Boolean
) {
    val borderColor = if (isPrimary) TurquoiseMain else Color.LightGray
    val iconColor = if (isPrimary) TurquoiseMain else Color.Gray
    val textColor = if (isPrimary) TurquoiseMain else Color.Gray

    Row(
        modifier = Modifier
            .fillMaxWidth()
            .height(70.dp)
            .clip(RoundedCornerShape(16.dp))
            .border(2.dp, borderColor, RoundedCornerShape(16.dp))
            .clickable(onClick = onClick)
            .padding(horizontal = 20.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.SpaceBetween
    ) {
        Text(
            text = text,
            fontSize = 16.sp,
            fontWeight = FontWeight.Medium,
            color = textColor
        )
        Icon(
            imageVector = Icons.Default.Add, // Placeholder icon
            contentDescription = null,
            tint = iconColor
        )
    }
}
