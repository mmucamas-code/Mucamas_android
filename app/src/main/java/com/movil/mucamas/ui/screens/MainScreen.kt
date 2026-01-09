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
import com.movil.mucamas.navigation.Screen
import com.movil.mucamas.navigation.Screen.ConfirmReservation.createRoute
import com.movil.mucamas.ui.components.BottomNavItem
import com.movil.mucamas.ui.components.MucamasBottomBar
import com.movil.mucamas.ui.screens.home.HomeScreen
import com.movil.mucamas.ui.screens.myreservations.MyReservationsScreen
import com.movil.mucamas.ui.screens.profile.ProfileScreen

val bottomNavItemSaver = Saver<BottomNavItem, String>(
    save = { it.label },
    restore = { label ->
        when (label) {
            BottomNavItem.Home.label -> BottomNavItem.Home
            BottomNavItem.Reservations.label -> BottomNavItem.Reservations
            BottomNavItem.Profile.label -> BottomNavItem.Profile
            else -> BottomNavItem.Home
        }
    }
)

@Composable
fun MainScreen(navController: NavController) {
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
            when (currentSection) {
                BottomNavItem.Home -> HomeScreen(
                    onServiceClick = { serviceName -> // Ahora recibimos el ID
                        // CORRECCIÓN: Usar el ID para construir una ruta segura
                        navController.navigate(Screen.SelectService.createRoute(serviceName))
                    }
                )
                BottomNavItem.Reservations -> MyReservationsScreen()
                BottomNavItem.Profile -> ProfileScreen(
                    onLogoutClick = {
                        navController.navigate("auth_graph") {
                            popUpTo("main_graph") { inclusive = true }
                        }
                    }
                )
            }
        }
    }
}
