package com.nexus.app

import androidx.compose.material3.Scaffold
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.AccountCircle
import androidx.compose.material.icons.outlined.CheckCircle
import androidx.compose.material.icons.outlined.Home
import androidx.compose.ui.Modifier
import androidx.navigation.NavDestination.Companion.hierarchy
import androidx.navigation.NavGraph.Companion.findStartDestination
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.rememberNavController
import com.nexus.feature.account.AccountScreen
import com.nexus.feature.checkin.CheckInScreen
import com.nexus.feature.dashboard.DashboardScreen
import com.nexus.ui.components.NexusBottomBar
import com.nexus.ui.components.NexusBottomBarItem

@Composable
fun NexusApp() {
    val navController = rememberNavController()
    val destinations = listOf(
        AppDestination("dashboard", "概览", Icons.Outlined.Home),
        AppDestination("checkin", "签到", Icons.Outlined.CheckCircle),
        AppDestination("accounts", "账号", Icons.Outlined.AccountCircle),
    )

    Scaffold(
        bottomBar = {
            val navBackStackEntry by navController.currentBackStackEntryAsState()
            val currentDestination = navBackStackEntry?.destination
            NexusBottomBar(
                items = destinations.map { destination ->
                    NexusBottomBarItem(
                        route = destination.route,
                        label = destination.label,
                        icon = destination.icon,
                    )
                },
                currentRoute = currentDestination
                    ?.hierarchy
                    ?.firstOrNull { destination -> destination.route != null }
                    ?.route,
                onSelected = { destination ->
                    navController.navigate(destination.route) {
                        popUpTo(navController.graph.findStartDestination().id) {
                            saveState = true
                        }
                        launchSingleTop = true
                        restoreState = true
                    }
                },
                modifier = Modifier,
            )
        },
    ) { innerPadding ->
        NavHost(
            navController = navController,
            startDestination = "dashboard",
        ) {
            composable("dashboard") { DashboardScreen(innerPadding) }
            composable("checkin") { CheckInScreen(innerPadding) }
            composable("accounts") { AccountScreen(innerPadding) }
        }
    }
}

private data class AppDestination(
    val route: String,
    val label: String,
    val icon: ImageVector,
)
