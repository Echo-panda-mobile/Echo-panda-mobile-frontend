package com.example.echo_panda_mobile.presentation.navigation

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Text
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavGraph.Companion.findStartDestination
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.rememberNavController
import com.example.echo_panda_mobile.presentation.components.MiniPlayer
import com.example.echo_panda_mobile.presentation.viewmodel.GlobalPlayerViewModel
import com.example.echo_panda_mobile.presentation.views.auth.LoginScreen
import com.example.echo_panda_mobile.presentation.views.auth.SignUpScreen
import com.example.echo_panda_mobile.presentation.views.user.album.AlbumDetailScreen
import com.example.echo_panda_mobile.presentation.views.user.album.AlbumScreen
import com.example.echo_panda_mobile.presentation.views.user.artist.ArtistDetailScreen
import com.example.echo_panda_mobile.presentation.views.user.discover.DiscoverScreen
import com.example.echo_panda_mobile.presentation.views.user.home.HomeScreen
import com.example.echo_panda_mobile.presentation.views.user.player.PlayerScreen
import com.example.echo_panda_mobile.presentation.views.user.profile.UserProfileScreen
import com.example.echo_panda_mobile.presentation.views.user.profile.UserSettingsScreen
import com.example.echo_panda_mobile.presentation.views.intro.EchoPandaOnboardingView
import com.google.firebase.auth.FirebaseAuth
import android.content.Context

@Composable
fun AppNavigation() {
    val navController = rememberNavController()
    val backStack by navController.currentBackStackEntryAsState()
    val currentRoute = backStack?.destination?.route
    
    val globalPlayerViewModel: GlobalPlayerViewModel = viewModel()
    val playerState by globalPlayerViewModel.playerState.collectAsState()

    // Reactive check for Firebase user state
    val auth = remember { FirebaseAuth.getInstance() }
    var currentUser by remember { mutableStateOf(auth.currentUser) }
    var hasSeenIntro by remember { mutableStateOf(true) }

    DisposableEffect(auth) {
        val listener = FirebaseAuth.AuthStateListener {
            currentUser = it.currentUser
        }
        auth.addAuthStateListener(listener)
        onDispose { auth.removeAuthStateListener(listener) }
    }

    // Check if user has seen intro (on first login)
    LaunchedEffect(currentUser) {
        if (currentUser != null) {
            val preferences = navController.context.getSharedPreferences("app_prefs", Context.MODE_PRIVATE)
            hasSeenIntro = preferences.getBoolean("has_seen_intro_${currentUser?.uid}", false)
        }
    }

    val startRoute = remember(currentUser, hasSeenIntro) {
        when {
            currentUser == null -> Routes.LOGIN
            !hasSeenIntro -> Routes.INTRO
            else -> Routes.USER_HOME
        }
    }

    // Derive selected bottom nav tab from current route
    val selectedNav = Routes.bottomNavIndex(currentRoute)

    // Shared bottom nav handler
    val onNavSelect: (Int) -> Unit = { index ->
        val route = Routes.bottomNavRoute(index)
        navController.navigate(route) {
            popUpTo(navController.graph.findStartDestination().id) { saveState = true }
            launchSingleTop = true
            restoreState    = true
        }
    }

    Box(modifier = Modifier.fillMaxSize()) {
        NavHost(
            navController    = navController,
            startDestination = startRoute
        ) {
            composable(Routes.LOGIN) {
                LoginScreen(
                    onBack = { navController.popBackStack() },
                    onAuthenticateSuccess = { destination ->
                        navController.navigate(destination) {
                            popUpTo(Routes.LOGIN) { inclusive = true }
                            launchSingleTop = true
                        }
                    },
                    onNavigateToSignUp = { navController.navigate(Routes.SIGNUP) },
                    onNavigateToForgotPassword = { navController.navigate(Routes.FORGOT_PASSWORD) }
                )
            }

            composable(Routes.SIGNUP) {
                SignUpScreen(
                    onBack = { navController.popBackStack() },
                    onSignUpSuccess = { route ->
                        navController.navigate(route) {
                            popUpTo(Routes.LOGIN) { inclusive = true }
                        }
                    }
                )
            }

            composable(Routes.USER_HOME) {
                HomeScreen(
                    selectedNav = selectedNav, 
                    onNavSelect = onNavSelect,
                    onNavigateToProfile = { navController.navigate(Routes.USER_PROFILE) },
                    onNavigateToAlbum = { albumId ->
                        navController.navigate(Routes.USER_ALBUM_DETAIL.replace("{albumId}", albumId))
                    },
                    onNavigateToPlayer = { trackId ->
                        navController.navigate(Routes.USER_PLAYER.replace("{trackId}", trackId))
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
                    }
                )
            }

            composable(Routes.USER_LIBRARY) {
                // Placeholder for Library Screen
                Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                    Text("Library Screen (Coming Soon)", color = androidx.compose.ui.graphics.Color.White)
                }
            }

            composable(Routes.USER_ALBUMS) {
                AlbumScreen(
                    selectedNav = selectedNav, 
                    onNavSelect = onNavSelect,
                    onNavigateToDetail = { albumId -> 
                        navController.navigate(Routes.USER_ALBUM_DETAIL.replace("{albumId}", albumId))
                    },
                    onSearch = { query ->
                        // Implement search logic or navigate to search results
                    },
                    onVoiceSearch = {
                        // Trigger Voice Search Intent
                    }
                )
            }

            composable(Routes.USER_ALBUM_DETAIL) { backStackEntry ->
                val albumId = backStackEntry.arguments?.getString("albumId") ?: ""
                AlbumDetailScreen(
                    albumId = albumId,
                    selectedNav = selectedNav,
                    onNavSelect = onNavSelect,
                    onBack = { navController.popBackStack() },
                    onNavigateToPlayer = { trackId ->
                        navController.navigate(Routes.USER_PLAYER.replace("{trackId}", trackId))
                    }
                )
            }

            composable(Routes.USER_PLAYER) { backStackEntry ->
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

            // Artist View Route (for clicking on artist cards)
            composable(Routes.ARTIST_VIEW) { backStackEntry ->
                val artistId = backStackEntry.arguments?.getString("artistId") ?: ""
                ArtistDetailScreen(
                    artistId = artistId,
                    onBack = { navController.popBackStack() },
                    onNavigateToAlbum = { albumId ->
                        navController.navigate(Routes.USER_ALBUM_DETAIL.replace("{albumId}", albumId))
                    },
                    onNavigateToPlayer = { trackId ->
                        navController.navigate(Routes.USER_PLAYER.replace("{trackId}", trackId))
                    }
                )
            }

            // Intro/Onboarding Route
            composable(Routes.INTRO) {
                EchoPandaOnboardingView(
                    onFinished = {
                        // Mark intro as seen
                        if (currentUser != null) {
                            val preferences = navController.context.getSharedPreferences("app_prefs", Context.MODE_PRIVATE)
                            preferences.edit().putBoolean("has_seen_intro_${currentUser?.uid}", true).apply()
                        }
                        navController.navigate(Routes.USER_HOME) {
                            popUpTo(Routes.INTRO) { inclusive = true }
                            launchSingleTop = true
                        }
                    }
                )
            }
        }

        // Global Mini Player - appears everywhere except login/signup and full player
        val isPlayerScreen = currentRoute?.startsWith("user/player") == true
        val isAuthScreen = currentRoute == Routes.LOGIN || currentRoute == Routes.SIGNUP

        if (playerState.currentTrack != null && !isPlayerScreen && !isAuthScreen) {
            Box(
                modifier = Modifier
                    .align(Alignment.BottomCenter)
                    .padding(bottom = 80.dp) // Height of BottomBar approx
            ) {
                MiniPlayer(
                    track = playerState.currentTrack!!,
                    isPlaying = playerState.isPlaying,
                    progress = playerState.progress,
                    onTogglePlay = { globalPlayerViewModel.togglePlayPause() },
                    onNext = { globalPlayerViewModel.nextTrack() },
                    onPrevious = { globalPlayerViewModel.previousTrack() },
                    onClick = {
                        navController.navigate(
                            Routes.USER_PLAYER.replace("{trackId}", playerState.currentTrack!!.id)
                        )
                    }
                )
            }
        }
    }
}
