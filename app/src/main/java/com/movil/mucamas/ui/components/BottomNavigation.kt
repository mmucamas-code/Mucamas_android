package com.movil.mucamas.ui.components

import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.DateRange
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.Person
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarDefaults
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.NavigationBarItemDefaults
import androidx.compose.material3.Text
import androidx.compose.material3.windowsizeclass.WindowWidthSizeClass
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavController
import androidx.navigation.NavDestination.Companion.hierarchy
import androidx.navigation.NavGraph.Companion.findStartDestination
import androidx.navigation.compose.currentBackStackEntryAsState
import com.movil.mucamas.navigation.Screen
import com.movil.mucamas.ui.utils.AdaptiveTheme

sealed class BottomNavItem(val route: String, val label: String, val icon: ImageVector) {
    data object Home : BottomNavItem(Screen.Home.route, "Inicio", Icons.Default.Home)
    data object Reservations : BottomNavItem(Screen.MyReservations.route, "Mis Reservas", Icons.Default.DateRange)
    data object Profile : BottomNavItem(Screen.Profile.route, "Perfil", Icons.Default.Person)
}

@Composable
fun MucamasBottomBar(
    navController: NavController,
    modifier: Modifier = Modifier
) {
    val navBackStackEntry by navController.currentBackStackEntryAsState()
    val currentDestination = navBackStackEntry?.destination
    
    val items = listOf(
        BottomNavItem.Home,
        BottomNavItem.Reservations,
        BottomNavItem.Profile
    )

    val currentRoute = currentDestination?.route
    val showBottomBar = items.any { it.route == currentRoute }

    if (showBottomBar) {
        // Tamaños responsivos
        val iconSize: Dp = when (AdaptiveTheme.widthSizeClass) {
            WindowWidthSizeClass.Compact -> 24.dp
            else -> 26.dp
        }
        
        val textSize = when (AdaptiveTheme.widthSizeClass) {
            WindowWidthSizeClass.Compact -> 11.sp
            else -> 12.sp
        }

        // Barra de navegación integrada
        NavigationBar(
            modifier = modifier,
            containerColor = MaterialTheme.colorScheme.surface,
            contentColor = MaterialTheme.colorScheme.onSurfaceVariant,
            tonalElevation = 8.dp, // Elevación sutil estándar de M3 para separar del contenido
            windowInsets = NavigationBarDefaults.windowInsets // Respeta automáticamente los insets del sistema
        ) {
            items.forEach { item ->
                val selected = currentDestination?.hierarchy?.any { it.route == item.route } == true

                NavigationBarItem(
                    selected = selected,
                    onClick = {
                        navController.navigate(item.route) {
                            popUpTo(navController.graph.findStartDestination().id) {
                                saveState = true
                            }
                            launchSingleTop = true
                            restoreState = true
                        }
                    },
                    icon = {
                        Icon(
                            imageVector = item.icon,
                            contentDescription = item.label,
                            modifier = Modifier.size(iconSize)
                        )
                    },
                    label = {
                        Text(
                            text = item.label,
                            style = MaterialTheme.typography.labelSmall.copy(
                                fontSize = textSize,
                                fontWeight = if (selected) FontWeight.Bold else FontWeight.Medium
                            )
                        )
                    },
                    colors = NavigationBarItemDefaults.colors(
                        // Colores estrictos del Theme
                        selectedIconColor = MaterialTheme.colorScheme.primary,
                        selectedTextColor = MaterialTheme.colorScheme.primary,
                        // Hacemos transparente el indicador para un estilo "plano" moderno sin la píldora de fondo
                        indicatorColor = Color.Transparent, 
                        unselectedIconColor = MaterialTheme.colorScheme.onSurfaceVariant,
                        unselectedTextColor = MaterialTheme.colorScheme.onSurfaceVariant
                    ),
                    alwaysShowLabel = true // Mantiene la estructura Icono Arriba / Texto Abajo siempre
                )
            }
        }
    }
}
