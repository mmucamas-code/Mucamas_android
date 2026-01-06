package com.movil.mucamas.navigation

import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.navigation.NavHostController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import com.movil.mucamas.ui.screens.WelcomeScreen
import com.movil.mucamas.ui.screens.home.HomeScreen
import com.movil.mucamas.ui.screens.login.LoginScreen
import com.movil.mucamas.ui.screens.login.RegisterIdentityScreen
import com.movil.mucamas.ui.screens.login.RegisterLocationScreen
import com.movil.mucamas.ui.screens.myreservations.MyReservationsScreen
import com.movil.mucamas.ui.screens.profile.ProfileScreen
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
        startDestination = Screen.Welcome.route, // Nuevo punto de inicio
        modifier = modifier
    ) {
        // Welcome
        composable(Screen.Welcome.route) {
            WelcomeScreen(
                onLoginClick = { navController.navigate(Screen.Login.route) },
                onRegisterClick = { navController.navigate(Screen.RegisterIdentity.route) }
            )
        }

        // Login Flow
        composable(Screen.Login.route) {
            LoginScreen(
                onLoginSuccess = { 
                    navController.navigate(Screen.Home.route) {
                        popUpTo(Screen.Welcome.route) { inclusive = true }
                    }
                },
                onSignUpClick = { navController.navigate(Screen.RegisterIdentity.route) }
            )
        }
        
        // Registration Flow
        composable(Screen.RegisterIdentity.route) {
            RegisterIdentityScreen(
                onNextClick = {
                    // Al verificar, vamos al Home y limpiamos la pila de login
                    navController.navigate(Screen.Home.route) {
                        popUpTo(Screen.Welcome.route) { inclusive = true }
                    }
                },
                onLoginClick = { navController.navigate(Screen.Login.route) },
                //onScanClick = { /* TODO: Implement Scan logic */ },
                //onGalleryClick = { /* TODO: Implement Gallery logic */ }
            )
        }
        
        composable(Screen.RegisterLocation.route) {
            RegisterLocationScreen(
                onFinishClick = {
                    navController.navigate(Screen.Home.route) {
                        popUpTo(Screen.Welcome.route) { inclusive = true }
                    }
                },
                onUseCurrentLocationClick = { /* TODO: Implement location logic */ }
            )
        }

        // Main App Flow
        composable(Screen.Home.route) {
            HomeScreen(
                onServiceClick = { navController.navigate(Screen.SelectService.route) },
                onMyReservationsClick = { navController.navigate(Screen.MyReservations.route) },
                onProfileClick = { navController.navigate(Screen.Profile.route) }
            )
        }
        
        composable(Screen.Profile.route) {
            ProfileScreen(
                onLogoutClick = {
                    navController.navigate(Screen.Welcome.route) {
                        popUpTo(0) { inclusive = true }
                    }
                },
                onHomeClick = { navController.navigate(Screen.Home.route) },
                onReservationsClick = { navController.navigate(Screen.MyReservations.route) }
            )
        }

        composable(Screen.SelectService.route) { SelectServiceScreen() }
        composable(Screen.SelectDate.route) { SelectDateScreen() }
        composable(Screen.ConfirmReservation.route) { ConfirmReservationScreen() }
        composable(Screen.MyReservations.route) { MyReservationsScreen() }
        composable(Screen.RateService.route) { RateServiceScreen() }
    }
}
