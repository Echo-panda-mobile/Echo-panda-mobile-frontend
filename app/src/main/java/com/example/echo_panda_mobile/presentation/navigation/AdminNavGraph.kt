package com.example.echo_panda_mobile.presentation.navigation

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.Text
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.navigation.NavController
import androidx.navigation.NavGraphBuilder
import androidx.navigation.compose.composable
import androidx.navigation.navigation
import com.example.echo_panda_mobile.presentation.views.admin.AdminDashboardScreen

fun NavGraphBuilder.adminNavGraph(
    navController: NavController
) {
    navigation(
        startDestination = Routes.ADMIN_DASHBOARD,
        route = "admin_graph"
    ) {
        composable(Routes.ADMIN_DASHBOARD) {
            AdminDashboardScreen(
                onNavigateToUsers = { navController.navigate(Routes.ADMIN_USER_MANAGEMENT) },
                onNavigateToContent = { navController.navigate(Routes.ADMIN_CONTENT_MODERATION) },
                onLogout = {
                    val repo = com.example.echo_panda_mobile.data.repository.AuthRepository()
                    repo.logout()
                    navController.navigate(Routes.LOGIN) {
                        popUpTo(0) { inclusive = true }
                    }
                }
            )
        }
        composable(Routes.ADMIN_USER_MANAGEMENT) {
            Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                Text("User Management")
            }
        }
        composable(Routes.ADMIN_CONTENT_MODERATION) {
            Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                Text("Content Moderation")
            }
        }
    }
}
