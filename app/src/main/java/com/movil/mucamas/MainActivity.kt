package com.movil.mucamas

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Scaffold
import androidx.compose.material3.windowsizeclass.ExperimentalMaterial3WindowSizeClassApi
import androidx.compose.material3.windowsizeclass.calculateWindowSizeClass
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.ui.Modifier
import androidx.navigation.compose.rememberNavController
import com.movil.mucamas.navigation.AppNavHost
import com.movil.mucamas.ui.theme.MucamasTheme
import com.movil.mucamas.ui.utils.LocalAdaptiveSpecs
import com.movil.mucamas.ui.utils.rememberAdaptiveSpecs

class MainActivity : ComponentActivity() {
    @OptIn(ExperimentalMaterial3WindowSizeClassApi::class)
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            val windowSizeClass = calculateWindowSizeClass(this)
            val adaptiveSpecs = rememberAdaptiveSpecs(windowSizeClass.widthSizeClass)

            MucamasTheme {
                // Proveemos las specs usando el CompositionLocal importado de utils
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
    Scaffold(modifier = modifier.fillMaxSize()) { innerPadding ->
        AppNavHost(
            navController = navController,
            modifier = Modifier.padding(innerPadding)
        )
    }
}
