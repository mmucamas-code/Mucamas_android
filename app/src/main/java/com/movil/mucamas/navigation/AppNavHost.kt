package com.movil.mucamas.navigation

import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.navigation.NavHostController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import com.movil.mucamas.ui.screens.SplashScreen
import com.movil.mucamas.ui.screens.WelcomeScreen
import com.movil.mucamas.ui.screens.home.HomeScreen
import com.movil.mucamas.ui.screens.login.LoginScreen
import com.movil.mucamas.ui.screens.login.RegisterIdentityScreen
import com.movil.mucamas.ui.screens.myreservations.MyReservationsScreen
import com.movil.mucamas.ui.screens.profile.ProfileScreen
import com.movil.mucamas.ui.screens.reservation.SelectServiceScreen

// Rutas principales del grafo de autenticación y de la app
const val AUTH_ROUTE = "auth_graph"
const val MAIN_ROUTE = "main_graph"

@Composable
fun AppNavHost(
    navController: NavHostController,
    modifier: Modifier = Modifier
) {
    NavHost(
        navController = navController,
        startDestination = "splash_screen", // Nuevo punto de inicio
        modifier = modifier
    ) {
        // SplashScreen para decidir el flujo inicial
        composable("splash_screen") {
            SplashScreen(
                onNavigateToHome = {
                    navController.navigate(MAIN_ROUTE) {
                        popUpTo("splash_screen") { inclusive = true }
                    }
                },
                onNavigateToAuth = {
                    navController.navigate(AUTH_ROUTE) {
                        popUpTo("splash_screen") { inclusive = true }
                    }
                }
            )
        }

        // Grafo de Autenticación
        composable(route = AUTH_ROUTE) {
            WelcomeScreen(
                onLoginClick = { navController.navigate(Screen.Login.route) },
                onRegisterClick = { navController.navigate(Screen.RegisterIdentity.route) }
            )
        }
        
        composable(Screen.Login.route) {
            LoginScreen(
                onLoginSuccess = {
                    navController.navigate(MAIN_ROUTE) {
                        popUpTo(AUTH_ROUTE) { inclusive = true }
                    }
                },
                onSignUpClick = { navController.navigate(Screen.RegisterIdentity.route) }
            )
        }
        
        composable(Screen.RegisterIdentity.route) {
            RegisterIdentityScreen(
                onRegistrationSuccess = {
                    navController.navigate(MAIN_ROUTE) {
                        popUpTo(AUTH_ROUTE) { inclusive = true }
                    }
                },
                onLoginClick = { navController.navigate(Screen.Login.route) }
            )
        }

        // Grafo Principal (Home y resto de la app)
        composable(route = MAIN_ROUTE) {
             // Aquí podrías tener otro NavHost si el grafo principal es complejo,
             // o simplemente lanzar la pantalla Home como punto de entrada.
             HomeScreen(
                onServiceClick = { serviceName ->
                    // Navegar a la pantalla de detalle/reserva con el nombre del servicio
                    navController.navigate("${Screen.SelectService.route}/$serviceName")
                }
             )
        }

        // Pantallas internas del grafo principal
        composable("${Screen.SelectService.route}/{serviceName}") { backStackEntry ->
            val serviceName = backStackEntry.arguments?.getString("serviceName") ?: ""
            SelectServiceScreen(onContinueClick = { 
                // Lógica de navegación después de la reserva
                navController.navigate(Screen.MyReservations.route)
            })
        }

        composable(Screen.MyReservations.route) {
            MyReservationsScreen()
        }

        composable(Screen.Profile.route) {
            ProfileScreen(
                onLogoutClick = {
                    navController.navigate(AUTH_ROUTE) {
                        popUpTo(MAIN_ROUTE) { inclusive = true }
                    }
                }
            )
        }
    }
}
