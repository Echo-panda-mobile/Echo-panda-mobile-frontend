package com.example.echo_panda_mobile.presentation.navigation

import android.content.Context
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.CircularProgressIndicator
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
import com.example.echo_panda_mobile.data.repository.AuthRepository
import com.example.echo_panda_mobile.presentation.components.MiniPlayer
import com.example.echo_panda_mobile.presentation.viewmodel.GlobalPlayerViewModel
import com.example.echo_panda_mobile.presentation.views.auth.LoginScreen
import com.example.echo_panda_mobile.presentation.views.auth.SignUpScreen
import com.example.echo_panda_mobile.presentation.views.intro.EchoPandaOnboardingView
import com.google.firebase.auth.FirebaseAuth

@Composable
fun AppNavigation() {
    val navController = rememberNavController()
    val backStack by navController.currentBackStackEntryAsState()
    val currentRoute = backStack?.destination?.route

    val globalPlayerViewModel: GlobalPlayerViewModel = viewModel()
    val playerState by globalPlayerViewModel.playerState.collectAsState()

    // Single stable instances — never recreated on recomposition
    val auth = remember { FirebaseAuth.getInstance() }
    val authRepo = remember { AuthRepository() }

    // ── Auth state ────────────────────────────────────────────────────────────
    var currentUser by remember { mutableStateOf(auth.currentUser) }

    // null  = still resolving (show spinner)
    // non-null = ready, hand off to NavHost
    var startRoute by remember { mutableStateOf<String?>(null) }
    var userRole by remember { mutableStateOf<String?>(null) }

    // Keep currentUser in sync with Firebase (handles logout from other screens)
    DisposableEffect(auth) {
        val listener = FirebaseAuth.AuthStateListener { currentUser = it.currentUser }
        auth.addAuthStateListener(listener)
        onDispose { auth.removeAuthStateListener(listener) }
    }

    // Resolve the correct start destination once per session.
    // Re-runs only when currentUser changes (login / logout).
    LaunchedEffect(currentUser) {
        startRoute = null  // show spinner while resolving

        val destination = if (currentUser == null) {
            userRole = null
            Routes.LOGIN
        } else {
            val prefs = navController.context
                .getSharedPreferences("app_prefs", Context.MODE_PRIVATE)
            val hasSeenIntro = prefs.getBoolean("has_seen_intro_${currentUser!!.uid}", false)

            // ALWAYS fetch profile to ensure userRole state is populated for bottom nav logic
            val profile = authRepo.getCurrentUserProfile()
            userRole = profile?.role

            if (!hasSeenIntro) {
                Routes.INTRO
            } else {
                Routes.getHomeRoute(profile?.role?.uppercase())
            }
        }

        startRoute = destination
    }

    // Block rendering until we know where to send the user
    if (startRoute == null) {
        Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
            CircularProgressIndicator()
        }
        return
    }

    // ── Bottom nav helpers ────────────────────────────────────────────────────
    val selectedNav = Routes.bottomNavIndex(currentRoute)
    val onNavSelect: (Int) -> Unit = { index ->
        val isAdminRoute = currentRoute?.startsWith("admin/") == true
        val route = if (isAdminRoute || userRole?.uppercase() == "ADMIN") {
            Routes.adminBottomNavRoute(index)
        } else {
            Routes.bottomNavRoute(index)
        }
        
        navController.navigate(route) {
            // Use findStartDestination().id to pop back to the role-based graph root
            popUpTo(navController.graph.findStartDestination().id) { 
                saveState = true 
            }
            launchSingleTop = true
            restoreState = true
        }
    }

    // ── Main nav graph ────────────────────────────────────────────────────────
    Box(modifier = Modifier.fillMaxSize()) {
        NavHost(
            navController = navController,
            startDestination = startRoute!!   // guaranteed non-null here
        ) {

            // ── Auth ──────────────────────────────────────────────────────────
            composable(Routes.LOGIN) {
                LoginScreen(
                    onBack = { navController.popBackStack() },
                    onAuthenticateSuccess = { destination ->
                        // destination is already the role-based route from LoginViewModel
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
                Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                    androidx.compose.material3.Text("Forgot Password Screen")
                }
            }

            composable(Routes.VERIFY_EMAIL) {
                Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                    androidx.compose.material3.Text("Verify Email Screen")
                }
            }

            // ── Role-based graphs ─────────────────────────────────────────────
            userNavGraph(navController, selectedNav, onNavSelect)
            artistNavGraph(navController)
            adminNavGraph(navController, selectedNav, onNavSelect)

            // ── Onboarding ────────────────────────────────────────────────────
            composable(Routes.INTRO) {
                EchoPandaOnboardingView(
                    onFinished = {
                        currentUser?.uid?.let { uid ->
                            navController.context
                                .getSharedPreferences("app_prefs", Context.MODE_PRIVATE)
                                .edit()
                                .putBoolean("has_seen_intro_$uid", true)
                                .apply()
                        }
                        // Navigate to whatever the role-based home is
                        // startRoute at this point holds INTRO, so re-derive home
                        val home = startRoute
                            ?.takeIf { it != Routes.INTRO }
                            ?: Routes.USER_HOME
                        navController.navigate(home) {
                            popUpTo(Routes.INTRO) { inclusive = true }
                            launchSingleTop = true
                        }
                    }
                )
            }
        }

        // ── Global Mini Player ────────────────────────────────────────────────
        val isPlayerScreen = currentRoute?.startsWith("user/player") == true
        val isAuthScreen   = currentRoute == Routes.LOGIN || currentRoute == Routes.SIGNUP

        if (playerState.currentTrack != null && !isPlayerScreen && !isAuthScreen) {
            Box(
                modifier = Modifier
                    .align(Alignment.BottomCenter)
                    .padding(bottom = 80.dp)
            ) {
                MiniPlayer(
                    track        = playerState.currentTrack!!,
                    isPlaying    = playerState.isPlaying,
                    progress     = playerState.progress,
                    onTogglePlay = { globalPlayerViewModel.togglePlayPause() },
                    onNext       = { globalPlayerViewModel.nextTrack() },
                    onPrevious   = { globalPlayerViewModel.previousTrack() },
                    onClick      = {
                        navController.navigate(
                            Routes.USER_PLAYER.replace("{trackId}", playerState.currentTrack!!.id)
                        )
                    }
                )
            }
        }
    }
}