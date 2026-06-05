package com.example.echo_panda_mobile.presentation.navigation

import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.navigation.NavController
import androidx.navigation.NavGraphBuilder
import androidx.navigation.compose.composable
import androidx.navigation.navigation
import com.example.echo_panda_mobile.data.model.User
import com.example.echo_panda_mobile.data.repository.AuthRepository
import com.example.echo_panda_mobile.data.repository.TokenStorage
import com.example.echo_panda_mobile.presentation.views.artist.*
import kotlinx.coroutines.launch

fun NavGraphBuilder.artistNavGraph(
    navController: NavController,
    tokenStorage: TokenStorage
) {
    navigation(
        startDestination = Routes.ARTIST_DASHBOARD,
        route = Routes.ARTIST_GRAPH
    ) {
        composable(Routes.ARTIST_DASHBOARD) {
            val authRepo = remember { AuthRepository(tokenStorage) }
            val scope = rememberCoroutineScope()
            var currentUser by remember { mutableStateOf<User?>(null) }

            LaunchedEffect(Unit) {
                currentUser = authRepo.getCurrentUserProfile()
            }

            ArtistDashboardScreen(
                currentUser = currentUser,
                onNavigate = { route -> 
                    android.util.Log.d("NAVIGATION", "ArtistDashboard navigating to: $route")
                    navController.navigate(route) 
                },
                onLogout = {
                    scope.launch {
                        authRepo.logout()
                        android.util.Log.d("NAVIGATION", "Artist Logout. Navigating to: ${Routes.LOGIN}")
                        navController.navigate(Routes.LOGIN) {
                            popUpTo(0) { inclusive = true }
                        }
                    }
                }
            )
        }

        composable(Routes.ARTIST_MY_MUSIC) {
            ArtistMusicScreen(onNavigate = { route -> 
                android.util.Log.d("NAVIGATION", "ArtistMusic navigating to: $route")
                navController.navigate(route) 
            })
        }

        composable(Routes.ARTIST_UPLOAD) {
            val authRepo = remember { AuthRepository(tokenStorage) }
            var currentUser by remember { mutableStateOf<User?>(null) }
            LaunchedEffect(Unit) {
                currentUser = authRepo.getCurrentUserProfile()
            }
            ArtistUploadScreen(
                currentUser = currentUser,
                onNavigate = { route -> 
                    android.util.Log.d("NAVIGATION", "ArtistUpload navigating to: $route")
                    navController.navigate(route) 
                }
            )
        }

        composable(Routes.ARTIST_ANALYTICS) {
            ArtistAnalyticsScreen(onNavigate = { route -> 
                android.util.Log.d("NAVIGATION", "ArtistAnalytics navigating to: $route")
                navController.navigate(route) 
            })
        }

        composable(Routes.ARTIST_PROFILE) {
            ArtistProfileScreen(onNavigate = { route -> 
                android.util.Log.d("NAVIGATION", "ArtistProfile navigating to: $route")
                if (route == Routes.LOGIN) {
                    navController.navigate(route) {
                        popUpTo(0) { inclusive = true }
                    }
                } else {
                    navController.navigate(route)
                }
            })
        }

        composable(Routes.ARTIST_ALBUMS) {
            ArtistAlbumManagementScreen(
                onBack = { navController.popBackStack() },
                onNavigateToCreate = { 
                    android.util.Log.d("NAVIGATION", "Navigating to: ${Routes.ARTIST_CREATE_ALBUM}")
                    navController.navigate(Routes.ARTIST_CREATE_ALBUM) 
                }
            )
        }

        composable(Routes.ARTIST_CREATE_ALBUM) {
            val authRepo = remember { AuthRepository(tokenStorage) }
            var currentUser by remember { mutableStateOf<User?>(null) }
            LaunchedEffect(Unit) {
                currentUser = authRepo.getCurrentUserProfile()
            }
            ArtistCreateAlbumScreen(
                currentUser = currentUser,
                onBack = { navController.popBackStack() }
            )
        }

        composable(Routes.ARTIST_NOTIFICATIONS) {
            ArtistNotificationScreen(onBack = { navController.popBackStack() })
        }

        composable(Routes.ARTIST_COMMENTS) {
            ArtistCommentsScreen(onBack = { navController.popBackStack() })
        }

        composable(Routes.ARTIST_PREFERENCES) {
            ArtistPreferencesScreen(onBack = { navController.popBackStack() })
        }

        composable(Routes.ARTIST_HELP_SUPPORT) {
            ArtistHelpSupportScreen(onBack = { navController.popBackStack() })
        }

        composable(Routes.ARTIST_SECURITY) {
            ArtistSecurityScreen(onBack = { navController.popBackStack() })
        }

        composable(Routes.ARTIST_EDIT_PROFILE) {
            ArtistEditProfileScreen(onBack = { navController.popBackStack() })
        }

        composable(
            route = Routes.ARTIST_EDIT_SONG,
            arguments = listOf(
                androidx.navigation.navArgument("trackId") { type = androidx.navigation.NavType.StringType }
            )
        ) { backStackEntry ->
            val trackId = backStackEntry.arguments?.getString("trackId") ?: ""
            ArtistEditSongScreen(
                songId = trackId,
                onBack = { navController.popBackStack() },
            )
        }

        composable(
            route = Routes.ARTIST_PLAYER,
            arguments = listOf(
                androidx.navigation.navArgument("trackId") { type = androidx.navigation.NavType.StringType }
            )
        ) { backStackEntry ->
            val trackId = backStackEntry.arguments?.getString("trackId") ?: ""
            ArtistPlayerScreen(
                trackId = trackId,
                onBack = { navController.popBackStack() }
            )
        }
    }
}
