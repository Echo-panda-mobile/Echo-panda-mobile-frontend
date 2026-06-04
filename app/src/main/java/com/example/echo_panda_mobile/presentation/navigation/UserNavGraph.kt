package com.example.echo_panda_mobile.presentation.navigation

import androidx.navigation.NavController
import androidx.navigation.NavGraphBuilder
import androidx.navigation.NavType
import androidx.navigation.compose.composable
import androidx.navigation.navArgument
import androidx.navigation.navigation
import com.example.echo_panda_mobile.presentation.views.user.browse.BrowseScreen
import com.example.echo_panda_mobile.presentation.views.user.home.HomeScreen
import com.example.echo_panda_mobile.presentation.views.user.discover.DiscoverScreen
import com.example.echo_panda_mobile.presentation.views.user.library.LibraryScreen
import com.example.echo_panda_mobile.presentation.views.user.library.FavoritesScreen
import com.example.echo_panda_mobile.presentation.views.user.album.AlbumScreen
import com.example.echo_panda_mobile.presentation.views.user.album.AlbumDetailScreen
import com.example.echo_panda_mobile.presentation.views.user.player.PlayerScreen
import com.example.echo_panda_mobile.presentation.views.user.profile.UserProfileScreen
import com.example.echo_panda_mobile.presentation.views.user.profile.UserSettingsScreen
import com.example.echo_panda_mobile.presentation.views.user.artist.ArtistDetailScreen
import com.example.echo_panda_mobile.presentation.views.user.library.PlaylistDetailScreen

fun NavController.navigateToBrowse(section: String) {
    navigate(Routes.USER_BROWSE.replace("{section}", section))
}

fun NavController.navigateToPlayer(trackId: String, resumeMs: Long? = null) {
    var route = Routes.USER_PLAYER.replace("{trackId}", trackId)
    if (resumeMs != null) {
        route = route.replace("{resumeMs}", resumeMs.toString())
    } else {
        route = route.substringBefore("?")
    }
    this.navigate(route)
}

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
                    navController.navigate(Routes.ARTIST_VIEW.replace("{artistId}", artistId))
                },
                onNavigateToBrowse = { section -> navController.navigateToBrowse(section) }
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
                onNavigateToSong = { trackId, resumeMs ->
                    navController.navigateToPlayer(trackId, resumeMs)
                },
                onNavigateToBrowse = { section -> navController.navigateToBrowse(section) }
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
                onSearch = { },
                onNavigateToBrowse = { section -> navController.navigateToBrowse(section) }
            )
        }

        composable(
            route = Routes.USER_BROWSE,
            arguments = listOf(navArgument("section") { type = NavType.StringType })
        ) {
            BrowseScreen(
                onBack = { navController.popBackStack() },
                onNavigateToArtist = { artistId ->
                    navController.navigate(Routes.ARTIST_VIEW.replace("{artistId}", artistId))
                },
                onNavigateToAlbum = { albumId ->
                    navController.navigate(Routes.USER_ALBUM_DETAIL.replace("{albumId}", albumId))
                },
                onNavigateToPlayer = { trackId, resumeMs ->
                    navController.navigateToPlayer(trackId, resumeMs)
                }
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
                onSettings = { navController.navigate(Routes.USER_SETTINGS) },
                onNavigateToLikedSongs = { navController.navigate(Routes.USER_FAVORITES) },
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
