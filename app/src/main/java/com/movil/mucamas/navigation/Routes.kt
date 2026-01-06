package com.movil.mucamas.navigation

sealed class Screen(val route: String) {
    data object Login : Screen("login")
    data object RegisterIdentity : Screen("register_identity")
    data object RegisterLocation : Screen("register_location")
    data object Home : Screen("home")
    data object SelectService : Screen("select_service")
    data object SelectDate : Screen("select_date")
    data object ConfirmReservation : Screen("confirm_reservation")
    data object MyReservations : Screen("my_reservations")
    data object RateService : Screen("rate_service")
    data object Profile : Screen("profile")
}
