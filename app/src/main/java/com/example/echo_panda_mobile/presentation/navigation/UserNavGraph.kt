package com.example.echo_panda_mobile.presentation.navigation

import androidx.navigation.NavController
import androidx.navigation.NavGraphBuilder
import androidx.navigation.compose.composable
import androidx.navigation.navigation
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
                onNavigateToProfile = { navController.navigate(Routes.USER_PROFILE) },
                onNavigateToAlbum = { albumId ->
                    navController.navigate(Routes.USER_ALBUM_DETAIL.replace("{albumId}", albumId))
                },
                onNavigateToPlayer = { trackId, resumeMs ->
                    navController.navigateToPlayer(trackId, resumeMs)
                },
                onNavigateToArtist = { artistId ->
                    navController.navigate(Routes.ARTIST_VIEW.replace("{artistId}", artistId))
                }
            )
        }

        composable(Routes.USER_DISCOVER) {
            DiscoverScreen(
                selectedNav = selectedNav,
                onNavSelect = onNavSelect,
                onNavigateToArtist = { artistId ->
                    navController.navigate(Routes.ARTIST_VIEW.replace("{artistId}", artistId))
                },
                onNavigateToAlbum = { albumId ->
                    navController.navigate(Routes.USER_ALBUM_DETAIL.replace("{albumId}", albumId))
                },
                onNavigateToSong = { trackId, resumeMs ->
                    navController.navigateToPlayer(trackId, resumeMs)
                }
            )
        }

        composable(Routes.USER_LIBRARY) {
            LibraryScreen(
                selectedNav = selectedNav,
                onNavSelect = onNavSelect,
                onNavigateToFavorites = {
                    navController.navigate(Routes.USER_FAVORITES)
                },
                onNavigateToArtist = { artistId ->
                    navController.navigate(Routes.ARTIST_VIEW.replace("{artistId}", artistId))
                },
                onNavigateToAlbum = { albumId ->
                    navController.navigate(Routes.USER_ALBUM_DETAIL.replace("{albumId}", albumId))
                },
                onNavigateToPlaylist = { playlistId ->
                    navController.navigate(Routes.USER_PLAYLIST_DETAIL.replace("{playlistId}", playlistId))
                },
                onNavigateToPlayer = { trackId, resumeMs ->
                    navController.navigateToPlayer(trackId, resumeMs)
                }
            )
        }

        composable(Routes.USER_FAVORITES) {
            FavoritesScreen(
                selectedNav = selectedNav,
                onNavSelect = onNavSelect,
                onBack = { navController.popBackStack() },
                onNavigateToPlayer = { trackId, resumeMs ->
                    navController.navigateToPlayer(trackId, resumeMs)
                }
            )
        }

        composable(Routes.USER_ALBUMS) {
            AlbumScreen(
                selectedNav = selectedNav,
                onNavSelect = onNavSelect,
                onNavigateToDetail = { albumId ->
                    navController.navigate(Routes.USER_ALBUM_DETAIL.replace("{albumId}", albumId))
                },
                onSearch = { /* TODO */ },
                onVoiceSearch = { /* TODO */ }
            )
        }

        composable(Routes.USER_ALBUM_DETAIL) { backStackEntry ->
            val albumId = backStackEntry.arguments?.getString("albumId").orEmpty()
                .also { if (it.isBlank()) return@composable }
            AlbumDetailScreen(
                albumId = albumId,
                selectedNav = selectedNav,
                onNavSelect = onNavSelect,
                onBack = { navController.popBackStack() },
                onNavigateToPlayer = { trackId, resumeMs ->
                    navController.navigateToPlayer(trackId, resumeMs)
                }
            )
        }

        composable(Routes.USER_PLAYLIST_DETAIL) { backStackEntry ->
            val playlistId = backStackEntry.arguments?.getString("playlistId") ?: ""
            PlaylistDetailScreen(
                playlistId = playlistId,
                selectedNav = selectedNav,
                onNavSelect = onNavSelect,
                onBack = { navController.popBackStack() },
                onNavigateToPlayer = { trackId, resumeMs ->
                    navController.navigateToPlayer(trackId, resumeMs)
                }
            )
        }

        composable(Routes.USER_PLAYER) { backStackEntry ->
            val trackId = backStackEntry.arguments?.getString("trackId") ?: ""
            val resumeMs = backStackEntry.arguments?.getString("resumeMs")?.toLongOrNull()
            PlayerScreen(
                trackId = trackId,
                resumePositionMs = resumeMs,
                onBack = { navController.popBackStack() }
            )
        }

        composable(Routes.USER_PROFILE) {
            UserProfileScreen(
                onBack = { navController.popBackStack() },
                onSettings = { navController.navigate(Routes.USER_SETTINGS) },
                onEditProfile = { /* TODO */ },
                onLogoutSuccess = {
                    navController.navigate(Routes.LOGIN) {
                        popUpTo(0) { inclusive = true }
                    }
                }
            )
        }

        composable(Routes.USER_SETTINGS) {
            UserSettingsScreen(onBack = { navController.popBackStack() })
        }

        composable(Routes.ARTIST_VIEW) { backStackEntry ->
            val artistId = backStackEntry.arguments?.getString("artistId") ?: ""
            ArtistDetailScreen(
                artistId = artistId,
                onBack = { navController.popBackStack() },
                onNavigateToAlbum = { albumId ->
                    navController.navigate(Routes.USER_ALBUM_DETAIL.replace("{albumId}", albumId))
                },
                onNavigateToPlayer = { trackId, resumeMs ->
                    navController.navigateToPlayer(trackId, resumeMs)
                },
                onNavigateToDashboard = {
                    navController.navigate(Routes.ARTIST_GRAPH)
                }
            )
        }
    }
}
