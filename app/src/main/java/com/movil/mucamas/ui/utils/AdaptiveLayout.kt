package com.movil.mucamas.ui.utils

import androidx.compose.material3.windowsizeclass.WindowWidthSizeClass
import androidx.compose.runtime.Composable
import androidx.compose.runtime.ReadOnlyComposable
import androidx.compose.runtime.compositionLocalOf
import androidx.compose.runtime.remember
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.TextUnit
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp

/**
 * Sistema de diseño adaptativo centralizado.
 * Provee valores para Espaciado, Dimensiones y Tipografía basados en el tamaño de la ventana.
 */

// 1. Data Classes para configuración
data class AdaptiveSpecs(
    val windowWidth: WindowWidthSizeClass,
    val spacing: Spacing,
    val dimens: Dimens,
    val typography: AdaptiveTypography
)

data class Spacing(
    val small: Dp,
    val medium: Dp,
    val large: Dp,
    val extraLarge: Dp,
    val screenEdge: Dp
)

data class Dimens(
    val buttonHeight: Dp,
    val iconSmall: Dp,
    val iconMedium: Dp,
    val iconLarge: Dp,
    val cornerRadius: Dp,
    val maxContentWidth: Dp
)

data class AdaptiveTypography(
    val headline: TextUnit,
    val title: TextUnit,
    val body: TextUnit,
    val button: TextUnit
)

// 2. CompositionLocal
val LocalAdaptiveSpecs = compositionLocalOf<AdaptiveSpecs> { error("No AdaptiveSpecs provided") }

// 3. Objeto Singleton para acceso fácil (Sintaxis limpia: AdaptiveTheme.spacing.medium)
object AdaptiveTheme {
    val spacing: Spacing
        @Composable
        @ReadOnlyComposable
        get() = LocalAdaptiveSpecs.current.spacing

    val dimens: Dimens
        @Composable
        @ReadOnlyComposable
        get() = LocalAdaptiveSpecs.current.dimens

    val typography: AdaptiveTypography
        @Composable
        @ReadOnlyComposable
        get() = LocalAdaptiveSpecs.current.typography

    val widthSizeClass: WindowWidthSizeClass
        @Composable
        @ReadOnlyComposable
        get() = LocalAdaptiveSpecs.current.windowWidth
}

// 4. Función de cálculo basada en WindowSizeClass
@Composable
fun rememberAdaptiveSpecs(windowWidth: WindowWidthSizeClass): AdaptiveSpecs {
    return remember(windowWidth) {
        when (windowWidth) {
            WindowWidthSizeClass.Compact -> AdaptiveSpecs(
                windowWidth = windowWidth,
                spacing = Spacing(
                    small = 8.dp,
                    medium = 16.dp,
                    large = 24.dp,
                    extraLarge = 32.dp,
                    screenEdge = 24.dp // Márgenes laterales generosos en móvil
                ),
                dimens = Dimens(
                    buttonHeight = 56.dp,
                    iconSmall = 24.dp,
                    iconMedium = 32.dp,
                    iconLarge = 48.dp,
                    cornerRadius = 24.dp,
                    maxContentWidth = Dp.Infinity
                ),
                typography = AdaptiveTypography(
                    headline = 28.sp,
                    title = 20.sp,
                    body = 16.sp,
                    button = 16.sp
                )
            )
            WindowWidthSizeClass.Medium -> AdaptiveSpecs(
                windowWidth = windowWidth,
                spacing = Spacing(
                    small = 12.dp,
                    medium = 24.dp,
                    large = 32.dp,
                    extraLarge = 48.dp,
                    screenEdge = 48.dp
                ),
                dimens = Dimens(
                    buttonHeight = 64.dp,
                    iconSmall = 28.dp,
                    iconMedium = 40.dp,
                    iconLarge = 64.dp,
                    cornerRadius = 32.dp,
                    maxContentWidth = 600.dp
                ),
                typography = AdaptiveTypography(
                    headline = 34.sp,
                    title = 24.sp,
                    body = 18.sp,
                    button = 18.sp
                )
            )
            WindowWidthSizeClass.Expanded -> AdaptiveSpecs(
                windowWidth = windowWidth,
                spacing = Spacing(
                    small = 16.dp,
                    medium = 32.dp,
                    large = 48.dp,
                    extraLarge = 64.dp,
                    screenEdge = 64.dp
                ),
                dimens = Dimens(
                    buttonHeight = 72.dp,
                    iconSmall = 32.dp,
                    iconMedium = 48.dp,
                    iconLarge = 80.dp,
                    cornerRadius = 40.dp,
                    maxContentWidth = 800.dp
                ),
                typography = AdaptiveTypography(
                    headline = 40.sp,
                    title = 28.sp,
                    body = 20.sp,
                    button = 20.sp
                )
            )
            else -> AdaptiveSpecs(
                windowWidth = WindowWidthSizeClass.Compact,
                spacing = Spacing(8.dp, 16.dp, 24.dp, 32.dp, 16.dp),
                dimens = Dimens(56.dp, 24.dp, 32.dp, 48.dp, 24.dp, Dp.Infinity),
                typography = AdaptiveTypography(28.sp, 20.sp, 16.sp, 16.sp)
            )
        }
    }
}
