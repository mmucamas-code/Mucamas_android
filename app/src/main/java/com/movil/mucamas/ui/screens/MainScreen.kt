package com.movil.mucamas.ui.screens

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Scaffold
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.saveable.Saver
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.navigation.NavController
import com.movil.mucamas.data.SessionProvider
import com.movil.mucamas.navigation.Screen
import com.movil.mucamas.navigation.Screen.ConfirmReservation.createRoute
import com.movil.mucamas.ui.components.BottomNavItem
import com.movil.mucamas.ui.components.MucamasBottomBar
import com.movil.mucamas.ui.models.UserRole
import com.movil.mucamas.ui.screens.home.HomeScreen
import com.movil.mucamas.ui.screens.myreservations.MyReservationsScreen
import com.movil.mucamas.ui.screens.profile.ProfileScreen
import kotlinx.coroutines.launch

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

    val sessionManager = SessionProvider.get()
    val userRole by sessionManager.userSessionFlow.collectAsState(initial = null)

    val bottomNavItems = when (userRole?.role) {
        UserRole.COLLABORATOR -> listOf(BottomNavItem.Reservations, BottomNavItem.Profile)
        else -> listOf(BottomNavItem.Home, BottomNavItem.Reservations, BottomNavItem.Profile)
    }

    var currentSection: BottomNavItem by rememberSaveable(stateSaver = bottomNavItemSaver) {
        mutableStateOf(bottomNavItems.first())
    }

    LaunchedEffect(bottomNavItems) {
        if (currentSection !in bottomNavItems) {
            currentSection = bottomNavItems.first()
        }
    }

    val scope = rememberCoroutineScope()

    LaunchedEffect(Unit) {
        navController.currentBackStackEntry?.savedStateHandle
            ?.getStateFlow<String?>("SELECT_TAB", null)
            ?.collect { tab ->
                tab?.let { currentSection = BottomNavItem.fromId(it) }
            }
    }

    Scaffold(
        bottomBar = {
            if (userRole != null) {
                MucamasBottomBar(
                    currentSection = currentSection,
                    onSectionSelected = { section -> currentSection = section },
                    items = bottomNavItems
                )
            }
        }
    ) { innerPadding ->
        Box(modifier = Modifier.padding(innerPadding)) {
            when (currentSection) {
                BottomNavItem.Home -> HomeScreen(
                    onServiceClick = { serviceName ->
                        navController.navigate(Screen.SelectService.createRoute(serviceName))
                    }
                )

                BottomNavItem.Reservations -> MyReservationsScreen()

                BottomNavItem.Profile -> ProfileScreen(
                    onLogoutClick = {
                        scope.launch {
                            sessionManager.clearSession()
                            navController.navigate("auth_graph") {
                                popUpTo("main_graph") { inclusive = true }
                            }
                        }
                    }
                )
            }
        }
    }
}
