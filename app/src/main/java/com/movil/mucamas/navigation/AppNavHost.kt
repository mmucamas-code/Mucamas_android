package com.movil.mucamas.navigation

import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.navigation.NavHostController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import com.movil.mucamas.ui.screens.home.HomeScreen
import com.movil.mucamas.ui.screens.login.LoginScreen
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
        composable(Screen.Login.route) {
            LoginScreen(
                onLoginClick = { navController.navigate(Screen.Home.route) },
                onSignUpClick = { /* TODO: Implement navigation to sign up */ }
            )
        }
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
