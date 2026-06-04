package com.example.echo_panda_mobile.presentation.navigation

import androidx.navigation.NavController
import androidx.navigation.NavGraphBuilder
import androidx.navigation.compose.composable
import androidx.navigation.navigation
import androidx.navigation.NavType
import androidx.navigation.navArgument
import com.example.echo_panda_mobile.presentation.views.user.home.HomeScreen
import com.example.echo_panda_mobile.presentation.views.user.discover.DiscoverScreen
import com.example.echo_panda_mobile.presentation.views.user.library.LibraryScreen
import com.example.echo_panda_mobile.presentation.views.user.library.FavoritesScreen
import com.example.echo_panda_mobile.presentation.views.user.library.PlaylistDetailScreen
import com.example.echo_panda_mobile.presentation.views.user.album.AlbumScreen
import com.example.echo_panda_mobile.presentation.views.user.album.AlbumDetailScreen
import com.example.echo_panda_mobile.presentation.views.user.player.PlayerScreen
import com.example.echo_panda_mobile.presentation.views.user.profile.UserProfileScreen
import com.example.echo_panda_mobile.presentation.views.user.profile.UserSettingsScreen
import com.example.echo_panda_mobile.presentation.views.user.artist.ArtistDetailScreen

fun NavGraphBuilder.userNavGraph(
    navController: NavController,
    selectedNav: Int,
    onNavSelect: (Int) -> Unit
) {
    navigation(
        startDestination = Routes.USER_HOME,
        route = "user_graph"
    ) {
        composable(Routes.USER_HOME) {
            HomeScreen(
                selectedNav = selectedNav,
                onNavSelect = onNavSelect,
                onNavigateToProfile = { 
                    android.util.Log.d("NAVIGATION", "Navigating to: ${Routes.USER_PROFILE}")
                    navController.navigate(Routes.USER_PROFILE) 
                },
                onNavigateToAlbum = { albumId ->
                    android.util.Log.d("NAVIGATION", "━━━━ NAVIGATING TO ALBUM DETAIL ━━━━")
                    android.util.Log.d("NAVIGATION", "Album ID: $albumId")
                    val route = Routes.USER_ALBUM_DETAIL.replace("{albumId}", albumId)
                    android.util.Log.d("NAVIGATION", "Route: $route")
                    navController.navigate(route)
                },
                onNavigateToPlayer = { trackId, _ ->
                    val route = Routes.USER_PLAYER.replace("{trackId}", trackId)
                    android.util.Log.d("NAVIGATION", "Navigating to: $route")
                    navController.navigate(route)
                },
                onNavigateToArtist = { artistId ->
                    val route = Routes.ARTIST_VIEW.replace("{artistId}", artistId)
                    android.util.Log.d("NAVIGATION", "Navigating to: $route")
                    navController.navigate(route)
                }
            )
        }

        composable(Routes.USER_DISCOVER) {
            DiscoverScreen(
                selectedNav = selectedNav,
                onNavSelect = onNavSelect,
                onNavigateToArtist = { artistId ->
                    val route = Routes.ARTIST_VIEW.replace("{artistId}", artistId)
                    android.util.Log.d("NAVIGATION", "Navigating to: $route")
                    navController.navigate(route)
                },
                onNavigateToAlbum = { albumId ->
                    android.util.Log.d("NAVIGATION", "━━━━ NAVIGATING TO ALBUM DETAIL ━━━━")
                    android.util.Log.d("NAVIGATION", "Album ID: $albumId")
                    val route = Routes.USER_ALBUM_DETAIL.replace("{albumId}", albumId)
                    android.util.Log.d("NAVIGATION", "Route: $route")
                    navController.navigate(route)
                },
                onNavigateToSong = { trackId, _ ->
                    val route = Routes.USER_PLAYER.replace("{trackId}", trackId)
                    android.util.Log.d("NAVIGATION", "Navigating to: $route")
                    navController.navigate(route)
                }
            )
        }

        composable(Routes.USER_LIBRARY) {
            LibraryScreen(
                selectedNav = selectedNav,
                onNavSelect = onNavSelect,
                onNavigateToFavorites = {
                    android.util.Log.d("NAVIGATION", "Navigating to: ${Routes.USER_FAVORITES}")
                    navController.navigate(Routes.USER_FAVORITES)
                },
                onNavigateToPlaylist = { playlistId ->
                    val route = Routes.USER_PLAYLIST_DETAIL.replace("{playlistId}", playlistId)
                    android.util.Log.d("NAVIGATION", "Navigating to: $route")
                    navController.navigate(route)
                },
                onNavigateToPlayer = { trackId, _ ->
                    val route = Routes.USER_PLAYER.replace("{trackId}", trackId)
                    android.util.Log.d("NAVIGATION", "Navigating to: $route")
                    navController.navigate(route)
                }
            )
        }

        composable(
            route = Routes.USER_PLAYLIST_DETAIL,
            arguments = listOf(navArgument("playlistId") { type = NavType.StringType })
        ) { backStackEntry ->
            val playlistId = backStackEntry.arguments?.getString("playlistId") ?: ""
            PlaylistDetailScreen(
                playlistId = playlistId,
                selectedNav = selectedNav,
                onNavSelect = onNavSelect,
                onBack = { navController.popBackStack() },
                onNavigateToPlayer = { trackId, _ ->
                    val route = Routes.USER_PLAYER.replace("{trackId}", trackId)
                    android.util.Log.d("NAVIGATION", "Navigating to: $route")
                    navController.navigate(route)
                }
            )
        }

        composable(Routes.USER_FAVORITES) {
            FavoritesScreen(
                selectedNav = selectedNav,
                onNavSelect = onNavSelect,
                onBack = { navController.popBackStack() },
                onNavigateToPlayer = { trackId, _ ->
                    val route = Routes.USER_PLAYER.replace("{trackId}", trackId)
                    android.util.Log.d("NAVIGATION", "Navigating to: $route")
                    navController.navigate(route)
                }
            )
        }

        composable(Routes.USER_ALBUMS) {
            AlbumScreen(
                selectedNav = selectedNav,
                onNavSelect = onNavSelect,
                onNavigateToDetail = { albumId ->
                    val route = Routes.USER_ALBUM_DETAIL.replace("{albumId}", albumId)
                    android.util.Log.d("NAVIGATION", "Navigating to: $route")
                    navController.navigate(route)
                },
                onSearch = { /* TODO */ },
                onVoiceSearch = { /* TODO */ }
            )
        }

        composable(
            route = Routes.USER_ALBUM_DETAIL,
            arguments = listOf(navArgument("albumId") { type = NavType.StringType })
        ) { backStackEntry ->
            val albumId = backStackEntry.arguments?.getString("albumId") ?: ""
            AlbumDetailScreen(
                albumId = albumId,
                selectedNav = selectedNav,
                onNavSelect = onNavSelect,
                onBack = { navController.popBackStack() },
                onNavigateToPlayer = { trackId, _ ->
                    val route = Routes.USER_PLAYER.replace("{trackId}", trackId)
                    android.util.Log.d("NAVIGATION", "Navigating to: $route")
                    navController.navigate(route)
                }
            )
        }

        composable(
            route = Routes.USER_PLAYER,
            arguments = listOf(navArgument("trackId") { type = NavType.StringType })
        ) { backStackEntry ->
            val trackId = backStackEntry.arguments?.getString("trackId") ?: ""
            PlayerScreen(
                trackId = trackId,
                onBack = { navController.popBackStack() }
            )
        }

        composable(Routes.USER_PROFILE) {
            UserProfileScreen(
                onBack = { navController.popBackStack() },
                onSettings = { 
                    android.util.Log.d("NAVIGATION", "Navigating to: ${Routes.USER_SETTINGS}")
                    navController.navigate(Routes.USER_SETTINGS) 
                },
                onEditProfile = { /* TODO */ },
                onLogoutSuccess = {
                    android.util.Log.d("NAVIGATION", "Logout success. Navigating to: ${Routes.LOGIN}")
                    navController.navigate(Routes.LOGIN) {
                        popUpTo(0) { inclusive = true }
                    }
                }
            )
        }

        composable(Routes.USER_SETTINGS) {
            UserSettingsScreen(onBack = { navController.popBackStack() })
        }

        composable(
            route = Routes.ARTIST_VIEW,
            arguments = listOf(navArgument("artistId") { type = NavType.StringType })
        ) { backStackEntry ->
            val artistId = backStackEntry.arguments?.getString("artistId") ?: ""
            ArtistDetailScreen(
                artistId = artistId,
                onBack = { navController.popBackStack() },
                onNavigateToAlbum = { albumId ->
                    android.util.Log.d("NAVIGATION", "━━━━ NAVIGATING TO ALBUM DETAIL ━━━━")
                    android.util.Log.d("NAVIGATION", "Album ID: $albumId")
                    val route = Routes.USER_ALBUM_DETAIL.replace("{albumId}", albumId)
                    android.util.Log.d("NAVIGATION", "Route: $route")
                    navController.navigate(route)
                },
                onNavigateToPlayer = { trackId, _ ->
                    val route = Routes.USER_PLAYER.replace("{trackId}", trackId)
                    android.util.Log.d("NAVIGATION", "Navigating to: $route")
                    navController.navigate(route)
                },
                onNavigateToDashboard = {
                    android.util.Log.d("NAVIGATION", "Navigating to: ${Routes.ARTIST_GRAPH}")
                    navController.navigate(Routes.ARTIST_GRAPH)
                }
            )
        }
    }
}
