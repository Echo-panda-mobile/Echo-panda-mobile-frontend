package com.example.echo_panda_mobile.presentation.navigation

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
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
    var userRole by remember { mutableStateOf<String?>(null) }

    DisposableEffect(auth) {
        val listener = FirebaseAuth.AuthStateListener {
            currentUser = it.currentUser
        }
        auth.addAuthStateListener(listener)
        onDispose { auth.removeAuthStateListener(listener) }
    }

    // Check if user has seen intro and fetch role
    LaunchedEffect(currentUser) {
        if (currentUser != null) {
            val preferences = navController.context.getSharedPreferences("app_prefs", Context.MODE_PRIVATE)
            hasSeenIntro = preferences.getBoolean("has_seen_intro_${currentUser?.uid}", false)
            
            // Fetch role from AuthRepository or Firestore
            // For now, let's assume we have a way to get it. 
            // We can use AuthRepository().getCurrentUserProfile()
            val repo = com.example.echo_panda_mobile.data.repository.AuthRepository()
            val profile = repo.getCurrentUserProfile()
            userRole = profile?.role?.uppercase()
        } else {
            userRole = null
        }
    }

    val startRoute = remember(currentUser, hasSeenIntro, userRole) {
        when {
            currentUser == null -> Routes.LOGIN
            !hasSeenIntro -> Routes.INTRO
            else -> Routes.getHomeRoute(userRole)
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

            composable(Routes.FORGOT_PASSWORD) {
                Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                    androidx.compose.material3.Text("Forgot Password Screen")
                }
            }

            composable(Routes.VERIFY_EMAIL) {
                Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                    androidx.compose.material3.Text("Verify Email Screen")
                }
            }

            // Nested Navigation Graphs
            userNavGraph(
                navController = navController,
                selectedNav = selectedNav,
                onNavSelect = onNavSelect
            )

            artistNavGraph(
                navController = navController
            )

            adminNavGraph(
                navController = navController
            )

            // Intro/Onboarding Route
            composable(Routes.INTRO) {
                EchoPandaOnboardingView(
                    onFinished = {
                        // Mark intro as seen
                        if (currentUser != null) {
                            val preferences = navController.context.getSharedPreferences("app_prefs", Context.MODE_PRIVATE)
                            preferences.edit().putBoolean("has_seen_intro_${currentUser?.uid}", true).apply()
                        }
                        navController.navigate(Routes.getHomeRoute(userRole)) {
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
