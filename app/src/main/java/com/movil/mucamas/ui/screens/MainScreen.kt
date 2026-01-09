package com.movil.mucamas.ui.screens

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Scaffold
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.saveable.Saver
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.navigation.NavController
import com.movil.mucamas.ui.components.BottomNavItem
import com.movil.mucamas.ui.components.MucamasBottomBar
import com.movil.mucamas.ui.screens.home.HomeScreen
import com.movil.mucamas.ui.screens.myreservations.MyReservationsScreen
import com.movil.mucamas.ui.screens.profile.ProfileScreen

// Saver para poder usar rememberSaveable con un objeto no-parcelable como BottomNavItem
val bottomNavItemSaver = Saver<BottomNavItem, String>(
    save = { it.label }, // Guardamos el label, que es único
    restore = { label ->
        when (label) {
            BottomNavItem.Home.label -> BottomNavItem.Home
            BottomNavItem.Reservations.label -> BottomNavItem.Reservations
            BottomNavItem.Profile.label -> BottomNavItem.Profile
            else -> BottomNavItem.Home // Fallback seguro
        }
    }
)

@Composable
fun MainScreen(navController: NavController) {
    // CORRECCIÓN: Se especifica el tipo explícitamente y se usa el Saver personalizado.
    var currentSection: BottomNavItem by rememberSaveable(stateSaver = bottomNavItemSaver) { 
        mutableStateOf(BottomNavItem.Home) 
    }

    Scaffold(
        bottomBar = {
            MucamasBottomBar(
                currentSection = currentSection,
                onSectionSelected = { section -> currentSection = section }
            )
        }
    ) { innerPadding ->
        Box(modifier = Modifier.padding(innerPadding)) {
            // Muestra el contenido de la sección seleccionada
            when (currentSection) {
                BottomNavItem.Home -> HomeScreen(
                    // Pasamos el NavController para la navegación secundaria (ej: ir a detalles)
                    onServiceClick = { serviceName ->
                        // Aquí sí usamos NavController para ir a una PANTALLA, no a una SECCIÓN
                        navController.navigate("service_detail/$serviceName")
                    }
                )
                BottomNavItem.Reservations -> MyReservationsScreen()
                BottomNavItem.Profile -> ProfileScreen(
                    onLogoutClick = {
                        // La lógica de logout navega fuera del MainScreen
                        navController.navigate("auth_graph") {
                            popUpTo("main_graph") { inclusive = true }
                        }
                    }
                )
            }
        }
    }
}
