package com.movil.mucamas.navigation

import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.navigation.NavHostController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import com.movil.mucamas.ui.screens.home.HomeScreen
import com.movil.mucamas.ui.screens.login.LoginScreen
import com.movil.mucamas.ui.screens.login.RegisterIdentityScreen
import com.movil.mucamas.ui.screens.login.RegisterLocationScreen
import com.movil.mucamas.ui.screens.myreservations.MyReservationsScreen
import com.movil.mucamas.ui.screens.rate.RateServiceScreen
import com.movil.mucamas.ui.screens.reservation.ConfirmReservationScreen
import com.movil.mucamas.ui.screens.reservation.SelectDateScreen
import com.movil.mucamas.ui.screens.reservation.SelectServiceScreen

@Composable
fun AppNavHost(
    navController: NavHostController,
    modifier: Modifier = Modifier
) {
    NavHost(
        navController = navController,
        startDestination = Screen.Login.route,
        modifier = modifier
    ) {
        // Login Flow
        composable(Screen.Login.route) {
            LoginScreen(
                onLoginClick = { navController.navigate(Screen.Home.route) },
                onSignUpClick = { navController.navigate(Screen.RegisterIdentity.route) }
            )
        }
        
        // Registration Flow
        composable(Screen.RegisterIdentity.route) {
            RegisterIdentityScreen(
                onNextClick = { navController.navigate(Screen.RegisterLocation.route) },
                onScanClick = { /* TODO: Implement Scan logic */ },
                onGalleryClick = { /* TODO: Implement Gallery logic */ }
            )
        }
        
        composable(Screen.RegisterLocation.route) {
            RegisterLocationScreen(
                onFinishClick = {
                    // Navega al Home y limpia la pila de navegación para que no pueda volver al registro
                    navController.navigate(Screen.Home.route) {
                        popUpTo(Screen.Login.route) { inclusive = true }
                    }
                },
                onUseCurrentLocationClick = { /* TODO: Implement location logic */ }
            )
        }

        // Main App Flow
        composable(Screen.Home.route) {
            HomeScreen(
                onServiceClick = { navController.navigate(Screen.SelectService.route) },
                onMyReservationsClick = { navController.navigate(Screen.MyReservations.route) }
            )
        }
        composable(Screen.SelectService.route) { SelectServiceScreen() }
        composable(Screen.SelectDate.route) { SelectDateScreen() }
        composable(Screen.ConfirmReservation.route) { ConfirmReservationScreen() }
        composable(Screen.MyReservations.route) { MyReservationsScreen() }
        composable(Screen.RateService.route) { RateServiceScreen() }
    }
}
