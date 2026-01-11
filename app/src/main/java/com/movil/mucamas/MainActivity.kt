package com.movil.mucamas

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.windowsizeclass.ExperimentalMaterial3WindowSizeClassApi
import androidx.compose.material3.windowsizeclass.calculateWindowSizeClass
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.ui.Modifier
import androidx.navigation.compose.rememberNavController
import com.movil.mucamas.data.SessionProvider
import com.movil.mucamas.navigation.AppNavHost
import com.movil.mucamas.navigation.Screen
import com.movil.mucamas.ui.theme.MucamasTheme
import com.movil.mucamas.ui.utils.LocalAdaptiveSpecs
import com.movil.mucamas.ui.utils.rememberAdaptiveSpecs

class MainActivity : ComponentActivity() {
    @OptIn(ExperimentalMaterial3WindowSizeClassApi::class)
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        SessionProvider.init(this)
        enableEdgeToEdge()
        setContent {
            val windowSizeClass = calculateWindowSizeClass(this)
            val adaptiveSpecs = rememberAdaptiveSpecs(windowSizeClass.widthSizeClass)
            
            MucamasTheme {
                CompositionLocalProvider(LocalAdaptiveSpecs provides adaptiveSpecs) {
                    MucamasApp()
                }
            }
        }
    }
}

@Composable
fun MucamasApp(modifier: Modifier = Modifier) {
    val navController = rememberNavController()

    // AppNavHost es ahora el componente raíz, sin Scaffold que lo envuelva.
    // La pantalla inicial siempre será el SplashScreen, que decidirá el flujo.
    AppNavHost(
        navController = navController,
        startDestination = "splash_screen", // Punto de entrada único
        modifier = modifier.fillMaxSize()
    )
}
