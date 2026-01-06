package com.movil.mucamas.ui.theme

import android.app.Activity
import android.os.Build
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.dynamicDarkColorScheme
import androidx.compose.material3.dynamicLightColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext

// Esquema Claro - Elegante y aireado
private val LightColorScheme = lightColorScheme(
    primary = SoftBlack,
    onPrimary = PureWhite,
    secondary = DarkGray,
    onSecondary = PureWhite,
    tertiary = MidGray,
    onTertiary = SoftBlack,
    background = LightGray,
    onBackground = SoftBlack,
    surface = PureWhite,
    onSurface = SoftBlack,
    surfaceVariant = LightGray,
    onSurfaceVariant = DarkGray,
    outline = MidGray,
    error = Color(0xFFD32F2F), // Mantenemos el error en rojo estándar por usabilidad
    onError = PureWhite
)

// Esquema Oscuro - Sofisticado y descansado
private val DarkColorScheme = darkColorScheme(
    primary = PureWhite,
    onPrimary = SoftBlack,
    secondary = MidGray,
    onSecondary = SoftBlack,
    tertiary = DarkGray,
    onTertiary = PureWhite,
    background = PureBlack,
    onBackground = PureWhite,
    surface = SoftBlack,
    onSurface = PureWhite,
    surfaceVariant = DarkGray,
    onSurfaceVariant = LightGray,
    outline = MidGray,
    error = Color(0xFFEF5350),
    onError = PureBlack
)

@Composable
fun MucamasTheme(
    darkTheme: Boolean = isSystemInDarkTheme(),
    // Desactivamos dynamicColor para mantener estrictamente nuestra identidad visual
    dynamicColor: Boolean = false, 
    content: @Composable () -> Unit
) {
    val colorScheme = when {
        dynamicColor && Build.VERSION.SDK_INT >= Build.VERSION_CODES.S -> {
            val context = LocalContext.current
            if (darkTheme) dynamicDarkColorScheme(context) else dynamicLightColorScheme(context)
        }
        darkTheme -> DarkColorScheme
        else -> LightColorScheme
    }

    MaterialTheme(
        colorScheme = colorScheme,
        typography = Typography,
        content = content
    )
}
