package com.movil.mucamas.ui.screens

import android.util.Log
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.lifecycle.viewmodel.compose.viewModel
import com.movil.mucamas.data.model.SessionResult
import com.movil.mucamas.ui.viewmodels.MainViewModel

@Composable
fun SplashScreen(
    onNavigateToHome: () -> Unit,
    onNavigateToAuth: () -> Unit,
    viewModel: MainViewModel = viewModel()
) {
    val sessionState by viewModel.sessionState.collectAsState()


    /*
    // Sirve para subir los servicios a la base de datos
    LaunchedEffect(Unit) {
        uploadServicesToFirestore(
            onSuccess = { println("Servicios cargados") },
            onError = { println("Error: ${it.message}") }
        )
    }
    */

    LaunchedEffect(sessionState) {
        Log.d("Session","$sessionState")
        when (sessionState) {
            is SessionResult.Success -> onNavigateToHome()
            is SessionResult.Empty -> onNavigateToAuth()
            is SessionResult.Loading -> {
                // No hacemos nada, seguimos mostrando el logo o un loader
                Log.i("session", "Cargando datos del disco...")
            }
        }
    }

    // UI simple de splash screen
    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background),
        contentAlignment = Alignment.Center
    ) {
        CircularProgressIndicator(color = MaterialTheme.colorScheme.primary)
    }
}
