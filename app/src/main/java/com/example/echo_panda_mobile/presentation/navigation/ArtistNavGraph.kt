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

fun NavGraphBuilder.artistNavGraph(
    navController: NavController
) {
    navigation(
        startDestination = Routes.ARTIST_DASHBOARD,
        route = "artist_graph"
    ) {
        composable(Routes.ARTIST_DASHBOARD) {
            Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                Text("Artist Dashboard")
            }
        }
        composable(Routes.ARTIST_MY_MUSIC) {
            Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                Text("Artist My Music")
            }
        }
        composable(Routes.ARTIST_UPLOAD) {
            Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                Text("Artist Upload")
            }
        }
        composable(Routes.ARTIST_PROFILE) {
            Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                Text("Artist Profile")
            }
        }
    }
}
