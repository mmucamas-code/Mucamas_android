package com.movil.mucamas

import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.navigation.NavHostController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable

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
        composable(Screen.Login.route) { LoginScreen() }
        composable(Screen.Home.route) { HomeScreen() }
        composable(Screen.SelectService.route) { SelectServiceScreen() }
        composable(Screen.SelectDate.route) { SelectDateScreen() }
        composable(Screen.ConfirmReservation.route) { ConfirmReservationScreen() }
        composable(Screen.MyReservations.route) { MyReservationsScreen() }
        composable(Screen.RateService.route) { RateServiceScreen() }
    }
}

@Composable
fun LoginScreen() {
    // TODO: Implement Login
}

@Composable
fun HomeScreen() {
    // TODO: Implement Home
}

@Composable
fun SelectServiceScreen() {
    // TODO: Implement Select Service
}

@Composable
fun SelectDateScreen() {
    // TODO: Implement Select Date
}

@Composable
fun ConfirmReservationScreen() {
    // TODO: Implement Confirm Reservation
}

@Composable
fun MyReservationsScreen() {
    // TODO: Implement My Reservations
}

@Composable
fun RateServiceScreen() {
    // TODO: Implement Rate Service
}
