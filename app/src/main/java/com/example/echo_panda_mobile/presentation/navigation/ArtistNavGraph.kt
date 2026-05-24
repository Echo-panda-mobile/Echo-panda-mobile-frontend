package com.example.echo_panda_mobile.presentation.navigation

import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.navigation.NavController
import androidx.navigation.NavGraphBuilder
import androidx.navigation.compose.composable
import androidx.navigation.navigation
import com.example.echo_panda_mobile.data.model.User
import com.example.echo_panda_mobile.data.repository.AuthRepository
import com.example.echo_panda_mobile.presentation.views.user.artist.*

fun NavGraphBuilder.artistNavGraph(
    navController: NavController
) {
    navigation(
        startDestination = Routes.ARTIST_DASHBOARD,
        route = Routes.ARTIST_GRAPH
    ) {
        composable(Routes.ARTIST_DASHBOARD) {
            val authRepo = remember { AuthRepository() }
            var currentUser by remember { mutableStateOf<User?>(null) }

            LaunchedEffect(Unit) {
                currentUser = authRepo.getCurrentUserProfile()
            }

            ArtistDashboardScreen(
                currentUser = currentUser,
                onNavigate = { route -> navController.navigate(route) },
                onLogout = {
                    navController.navigate(Routes.LOGIN) {
                        popUpTo(0) { inclusive = true }
                    }
                }
            )
        }

        composable(Routes.ARTIST_MY_MUSIC) {
            ArtistMusicScreen(onNavigate = { route -> navController.navigate(route) })
        }

        composable(Routes.ARTIST_UPLOAD) {
            ArtistUploadScreen(onNavigate = { route -> navController.navigate(route) })
        }

        composable(Routes.ARTIST_ANALYTICS) {
            ArtistAnalyticsScreen(onNavigate = { route -> navController.navigate(route) })
        }

        composable(Routes.ARTIST_PROFILE) {
            ArtistProfileScreen(onNavigate = { route -> navController.navigate(route) })
        }
    }
}
