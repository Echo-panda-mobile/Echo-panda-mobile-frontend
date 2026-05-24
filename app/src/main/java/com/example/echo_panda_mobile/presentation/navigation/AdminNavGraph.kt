package com.example.echo_panda_mobile.presentation.navigation

import androidx.compose.foundation.layout.*
import androidx.compose.material3.Scaffold
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
import com.example.echo_panda_mobile.presentation.views.admin.AdminAddArtistScreen
import com.example.echo_panda_mobile.presentation.views.admin.AdminAlbumDetailScreen
import com.example.echo_panda_mobile.presentation.views.admin.AdminCategoryAlbumsScreen
import com.example.echo_panda_mobile.presentation.views.admin.AdminCategoryDetailScreen
import com.example.echo_panda_mobile.presentation.views.admin.AdminDashboardScreen
import com.example.echo_panda_mobile.presentation.views.admin.AdminLibraryScreen
import com.example.echo_panda_mobile.presentation.views.admin.AdminMusicScreen
import com.example.echo_panda_mobile.presentation.views.admin.AdminProfileScreen
import com.example.echo_panda_mobile.presentation.views.admin.AdminSongDetailScreen
import com.example.echo_panda_mobile.presentation.views.admin.AdminTagAlbumsScreen
import com.example.echo_panda_mobile.presentation.views.admin.AdminTagDetailScreen
import com.example.echo_panda_mobile.presentation.views.admin.AdminUserDetailScreen
import com.example.echo_panda_mobile.presentation.views.admin.AdminUserManagementScreen

fun NavGraphBuilder.adminNavGraph(
    navController: NavController,
    selectedNav: Int,
    onNavSelect: (Int) -> Unit
) {
    navigation(
        startDestination = Routes.ADMIN_DASHBOARD,
        route = "admin_graph"
    ) {
        composable(Routes.ADMIN_DASHBOARD) {
            val authRepo = remember { AuthRepository() }

            AdminDashboardScreen(
                selectedNav = selectedNav,
                onNavSelect = onNavSelect,
                onLogout = {
                    authRepo.logout()
                    navController.navigate(Routes.LOGIN) {
                        popUpTo(0) { inclusive = true }
                    }
                },
                onProfileClick = {
                    navController.navigate(Routes.ADMIN_PROFILE)
                }
            )
        }

        composable(Routes.ADMIN_USER_MANAGEMENT) {
            AdminUserManagementScreen(
                selectedNav = selectedNav,
                onNavSelect = onNavSelect,
                onNavigateToDetail = { userId, role ->
                    navController.navigate(
                        Routes.ADMIN_USER_DETAIL
                            .replace("{userId}", userId)
                            .replace("{role}", role)
                    )
                },
                onNavigateToAddArtist = {
                    navController.navigate(Routes.ADMIN_ADD_ARTIST)
                },
                onProfileClick = {
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
                onBack = { navController.popBackStack() }
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
                onBack = { navController.popBackStack() }
            )
        }

        composable(Routes.ADMIN_MUSIC) {
            AdminMusicScreen(
                selectedNav = selectedNav,
                onNavSelect = onNavSelect,
                onNavigateToSongDetail = { songId ->
                    navController.navigate(Routes.ADMIN_SONG_DETAIL.replace("{songId}", songId))
                },
                onNavigateToAlbumDetail = { albumId ->
                    // Assuming you will create AdminAlbumDetailScreen later
                    navController.navigate(Routes.ADMIN_ALBUM_DETAIL.replace("{albumId}", albumId))
                },
                onProfileClick = {
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
                    navController.navigate(Routes.ADMIN_TAG_DETAIL.replace("{tagId}", tagId))
                },
                onNavigateToTagAlbums = { tagId ->
                    navController.navigate(Routes.ADMIN_TAG_ALBUMS.replace("{tagId}", tagId))
                },
                onNavigateToCategoryDetail = { categoryId ->
                    navController.navigate(Routes.ADMIN_CATEGORY_DETAIL.replace("{categoryId}", categoryId))
                },
                onNavigateToCategoryAlbums = { categoryId ->
                    navController.navigate(Routes.ADMIN_CATEGORY_ALBUMS.replace("{categoryId}", categoryId))
                },
                onProfileClick = {
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
            val authRepo = remember { AuthRepository() }
            AdminProfileScreen(
                onBack = { navController.popBackStack() },
                onLogout = {
                    authRepo.logout()
                    navController.navigate(Routes.LOGIN) {
                        popUpTo(0) { inclusive = true }
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
