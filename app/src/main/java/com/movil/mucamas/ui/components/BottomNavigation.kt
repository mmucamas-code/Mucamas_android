package com.movil.mucamas.ui.components

import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.navigationBars
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.windowInsetsPadding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.DateRange
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.Person
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.NavigationBarItemDefaults
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.windowsizeclass.WindowWidthSizeClass
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.shadow
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
    data object Home : BottomNavItem(Screen.Home.route, "Home", Icons.Default.Home)
    data object Reservations : BottomNavItem(Screen.MyReservations.route, "Reservas", Icons.Default.DateRange)
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
        val spacing = AdaptiveTheme.spacing
        
        // Ajustamos la altura para que sea compacta y elegante
        val bottomBarHeight: Dp = when (AdaptiveTheme.widthSizeClass) {
            WindowWidthSizeClass.Compact -> 64.dp // Reducido levemente para elegancia
            else -> 72.dp
        }

        val iconSize: Dp = when (AdaptiveTheme.widthSizeClass) {
            WindowWidthSizeClass.Compact -> 24.dp
            else -> 26.dp
        }

        // Contenedor Flotante (Pill)
        Surface(
            modifier = modifier
                // Padding horizontal adaptativo
                .padding(horizontal = spacing.large)
                // Padding inferior base (margen estético) + Insets del sistema (seguridad)
                .padding(bottom = spacing.medium) 
                .windowInsetsPadding(WindowInsets.navigationBars) // Respeta la "rayita" o botones
                .height(bottomBarHeight)
                .shadow(elevation = 6.dp, shape = RoundedCornerShape(50)),
            shape = RoundedCornerShape(50),
            color = MaterialTheme.colorScheme.surface
        ) {
            NavigationBar(
                containerColor = MaterialTheme.colorScheme.surface,
                tonalElevation = 0.dp,
                // Anulamos los insets internos del NavigationBar porque ya los manejamos externamente en el Surface.
                // Esto centra perfectamente el contenido dentro de la píldora.
                windowInsets = WindowInsets(0, 0, 0, 0),
                modifier = Modifier.padding(horizontal = spacing.small)
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
                                    fontSize = 10.sp, // Texto fino para no competir con el icono
                                    fontWeight = if (selected) FontWeight.Bold else FontWeight.Medium
                                )
                            )
                        },
                        colors = NavigationBarItemDefaults.colors(
                            selectedIconColor = MaterialTheme.colorScheme.onPrimary,
                            selectedTextColor = MaterialTheme.colorScheme.primary,
                            indicatorColor = MaterialTheme.colorScheme.primary,
                            unselectedIconColor = MaterialTheme.colorScheme.onSurfaceVariant,
                            unselectedTextColor = MaterialTheme.colorScheme.onSurfaceVariant
                        ),
                        // Asegura que el ítem siempre tenga etiqueta para mantener el centrado vertical consistente
                        alwaysShowLabel = true 
                    )
                }
            }
        }
    }
}
