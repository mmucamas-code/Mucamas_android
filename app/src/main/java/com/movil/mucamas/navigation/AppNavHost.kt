package com.movil.mucamas.navigation

import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.navigation.NavHostController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import com.movil.mucamas.ui.screens.MainScreen
import com.movil.mucamas.ui.screens.SplashScreen
import com.movil.mucamas.ui.screens.WelcomeScreen
import com.movil.mucamas.ui.screens.login.LoginScreen
import com.movil.mucamas.ui.screens.login.RegisterIdentityScreen

const val AUTH_ROUTE = "auth_graph"
const val MAIN_ROUTE = "main_graph"

@Composable
fun AppNavHost(
    navController: NavHostController,
    startDestination: String,
    modifier: Modifier = Modifier
) {
    NavHost(
        navController = navController,
        startDestination = startDestination,
        modifier = modifier
    ) {
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

        // Grafo Principal: una única pantalla que gestiona las secciones
        composable(route = MAIN_ROUTE) {
            MainScreen(navController = navController)
        }

        // Aquí irían las pantallas que se abren POR ENCIMA del MainScreen
        // Ejemplo: composable("service_detail/{serviceId}") { ... }
    }
}
