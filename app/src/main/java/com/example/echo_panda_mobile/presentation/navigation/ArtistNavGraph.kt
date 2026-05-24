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
import com.example.echo_panda_mobile.presentation.views.artist.ArtistDashboardScreen

fun NavGraphBuilder.artistNavGraph(
    navController: NavController
) {
    navigation(
        startDestination = Routes.ARTIST_DASHBOARD,
        route = "artist_graph"
    ) {
        composable(Routes.ARTIST_DASHBOARD) {
            ArtistDashboardScreen(
                onNavigateToMyMusic = { navController.navigate(Routes.ARTIST_MY_MUSIC) },
                onNavigateToUpload = { navController.navigate(Routes.ARTIST_UPLOAD) },
                onNavigateToProfile = { navController.navigate(Routes.ARTIST_PROFILE) },
                onLogout = {
                    val repo = com.example.echo_panda_mobile.data.repository.AuthRepository()
                    repo.logout()
                    navController.navigate(Routes.LOGIN) {
                        popUpTo(0) { inclusive = true }
                    }
                }
            )
        }
        composable(Routes.ARTIST_MY_MUSIC) {
            Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                Text("My Music Screen")
            }
        }
        composable(Routes.ARTIST_UPLOAD) {
            Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                Text("Upload Music Screen")
            }
        }
        composable(Routes.ARTIST_PROFILE) {
            Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                Text("Artist Profile Management")
            }
        }
    }
}
