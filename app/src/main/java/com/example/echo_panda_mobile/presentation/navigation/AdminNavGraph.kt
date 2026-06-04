package com.example.echo_panda_mobile.presentation.navigation

import androidx.compose.foundation.layout.*
import androidx.compose.material3.Text
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import androidx.navigation.NavController
import androidx.navigation.NavGraphBuilder
import androidx.navigation.compose.composable
import androidx.navigation.navigation
import com.example.echo_panda_mobile.data.repository.AuthRepository
import com.example.echo_panda_mobile.data.repository.TokenStorage
import com.example.echo_panda_mobile.presentation.views.admin.*
import kotlinx.coroutines.launch

fun NavGraphBuilder.adminNavGraph(
    navController: NavController,
    selectedNav: Int,
    onNavSelect: (Int) -> Unit,
    tokenStorage: TokenStorage
) {
    navigation(
        startDestination = Routes.ADMIN_DASHBOARD,
        route = "admin_graph"
    ) {
        composable(Routes.ADMIN_DASHBOARD) {
            val authRepo = remember { AuthRepository(tokenStorage) }
            val scope = rememberCoroutineScope()
            AdminDashboardScreen(
                selectedNav = selectedNav,
                onNavSelect = onNavSelect,
                onLogout = {
                    scope.launch {
                        authRepo.logout()
                        android.util.Log.d("NAVIGATION", "Admin Logout. Navigating to: ${Routes.LOGIN}")
                        navController.navigate(Routes.LOGIN) {
                            popUpTo(0) { inclusive = true }
                        }
                    }
                },
                onProfileClick = {
                    android.util.Log.d("NAVIGATION", "Navigating to: ${Routes.ADMIN_PROFILE}")
                    navController.navigate(Routes.ADMIN_PROFILE)
                }
            )
        }

        composable(Routes.ADMIN_USER_MANAGEMENT) {
            AdminUserManagementScreen(
                selectedNav = selectedNav,
                onNavSelect = onNavSelect,
                onNavigateToDetail = { userId, role ->
                    val route = Routes.ADMIN_USER_DETAIL
                            .replace("{userId}", userId)
                            .replace("{role}", role)
                    android.util.Log.d("NAVIGATION", "Navigating to: $route")
                    navController.navigate(route)
                },
                onNavigateToAddArtist = {
                    android.util.Log.d("NAVIGATION", "Navigating to: ${Routes.ADMIN_ADD_ARTIST}")
                    navController.navigate(Routes.ADMIN_ADD_ARTIST)
                },
                onProfileClick = {
                    android.util.Log.d("NAVIGATION", "Navigating to: ${Routes.ADMIN_PROFILE}")
                    navController.navigate(Routes.ADMIN_PROFILE)
                }
            )
        }

        composable(Routes.ADMIN_ADD_ARTIST) {
            AdminAddArtistScreen(
                onBack = { navController.popBackStack() }
            )
        }

        composable(Routes.ADMIN_USER_DETAIL) { backStackEntry ->
            val userId = backStackEntry.arguments?.getString("userId") ?: ""
            val role = backStackEntry.arguments?.getString("role") ?: "User"
            AdminUserDetailScreen(
                userId = userId,
                role = role,
                onBack = { navController.popBackStack() },
                onNavigateToAlbumDetail = { albumId ->
                    val route = Routes.ADMIN_ALBUM_DETAIL.replace("{albumId}", albumId)
                    android.util.Log.d("NAVIGATION", "Navigating from user detail to: $route")
                    navController.navigate(route)
                }
            )
        }

        composable(Routes.ADMIN_SONG_DETAIL) { backStackEntry ->
            val songId = backStackEntry.arguments?.getString("songId") ?: ""
            AdminSongDetailScreen(
                songId = songId,
                onBack = { navController.popBackStack() }
            )
        }

        composable(Routes.ADMIN_ALBUM_DETAIL) { backStackEntry ->
            val albumId = backStackEntry.arguments?.getString("albumId") ?: ""
            AdminAlbumDetailScreen(
                albumId = albumId,
                onBack = { navController.popBackStack() },
                onNavigateToSongDetail = { songId ->
                    val route = Routes.ADMIN_SONG_DETAIL.replace("{songId}", songId)
                    android.util.Log.d("NAVIGATION", "Navigating from album detail to: $route")
                    navController.navigate(route)
                }
            )
        }

        composable(Routes.ADMIN_MUSIC) {
            AdminMusicScreen(
                selectedNav = selectedNav,
                onNavSelect = onNavSelect,
                onNavigateToSongDetail = { songId ->
                    val route = Routes.ADMIN_SONG_DETAIL.replace("{songId}", songId)
                    android.util.Log.d("NAVIGATION", "Navigating to: $route")
                    navController.navigate(route)
                },
                onNavigateToAlbumDetail = { albumId ->
                    val route = Routes.ADMIN_ALBUM_DETAIL.replace("{albumId}", albumId)
                    android.util.Log.d("NAVIGATION", "Navigating to: $route")
                    navController.navigate(route)
                },
                onProfileClick = {
                    android.util.Log.d("NAVIGATION", "Navigating to: ${Routes.ADMIN_PROFILE}")
                    navController.navigate(Routes.ADMIN_PROFILE)
                },
                onBack = { navController.popBackStack() }
            )
        }

        composable(Routes.ADMIN_LIBRARY) {
            AdminLibraryScreen(
                selectedNav = selectedNav,
                onNavSelect = onNavSelect,
                onNavigateToTagDetail = { tagId ->
                    val route = Routes.ADMIN_TAG_DETAIL.replace("{tagId}", tagId)
                    android.util.Log.d("NAVIGATION", "Navigating to: $route")
                    navController.navigate(route)
                },
                onNavigateToTagAlbums = { tagId ->
                    val route = Routes.ADMIN_TAG_ALBUMS.replace("{tagId}", tagId)
                    android.util.Log.d("NAVIGATION", "Navigating to: $route")
                    navController.navigate(route)
                },
                onNavigateToCategoryDetail = { categoryId ->
                    val route = Routes.ADMIN_CATEGORY_DETAIL.replace("{categoryId}", categoryId)
                    android.util.Log.d("NAVIGATION", "Navigating to: $route")
                    navController.navigate(route)
                },
                onNavigateToCategoryAlbums = { categoryId ->
                    val route = Routes.ADMIN_CATEGORY_ALBUMS.replace("{categoryId}", categoryId)
                    android.util.Log.d("NAVIGATION", "Navigating to: $route")
                    navController.navigate(route)
                },
                onProfileClick = {
                    android.util.Log.d("NAVIGATION", "Navigating to: ${Routes.ADMIN_PROFILE}")
                    navController.navigate(Routes.ADMIN_PROFILE)
                },
                onBack = { navController.popBackStack() }
            )
        }

        composable(Routes.ADMIN_TAG_DETAIL) { backStackEntry ->
            val tagId = backStackEntry.arguments?.getString("tagId") ?: ""
            AdminTagDetailScreen(
                tagId = tagId,
                onBack = { navController.popBackStack() }
            )
        }

        composable(Routes.ADMIN_TAG_ALBUMS) { backStackEntry ->
            val tagId = backStackEntry.arguments?.getString("tagId") ?: ""
            AdminTagAlbumsScreen(
                tagId = tagId,
                onBack = { navController.popBackStack() }
            )
        }

        composable(Routes.ADMIN_CATEGORY_DETAIL) { backStackEntry ->
            val categoryId = backStackEntry.arguments?.getString("categoryId") ?: ""
            // Reusing Tag Detail UI but labeled for Category
            AdminCategoryDetailScreen(
                categoryId = categoryId,
                onBack = { navController.popBackStack() }
            )
        }

        composable(Routes.ADMIN_CATEGORY_ALBUMS) { backStackEntry ->
            val categoryId = backStackEntry.arguments?.getString("categoryId") ?: ""
            // Reusing Tag Albums UI but labeled for Category
            AdminCategoryAlbumsScreen(
                categoryId = categoryId,
                onBack = { navController.popBackStack() }
            )
        }

        composable(Routes.ADMIN_PROFILE) {
            val authRepo = remember { AuthRepository(tokenStorage) }
            val scope = rememberCoroutineScope()
            AdminProfileScreen(
                onBack = { navController.popBackStack() },
                onLogout = {
                    scope.launch {
                        authRepo.logout()
                        android.util.Log.d("NAVIGATION", "Admin Logout from Profile. Navigating to: ${Routes.LOGIN}")
                        navController.navigate(Routes.LOGIN) {
                            popUpTo(0) { inclusive = true }
                        }
                    }
                }
            )
        }

        composable(Routes.ADMIN_CONTENT_MODERATION) {
            Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                Text("Content Moderation")
            }
        }
    }
}

// ── Placeholder Screens ──────────────────────────────────────────────────
