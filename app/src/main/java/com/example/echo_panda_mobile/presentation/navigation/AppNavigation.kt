package com.example.echo_panda_mobile.presentation.navigation

import android.content.Context
import androidx.annotation.OptIn
import androidx.compose.foundation.layout.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import androidx.core.content.edit
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.media3.common.util.UnstableApi
import androidx.navigation.NavGraph.Companion.findStartDestination
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.rememberNavController
import com.example.echo_panda_mobile.data.repository.AuthRepository
import com.example.echo_panda_mobile.data.repository.TokenStorage
import com.example.echo_panda_mobile.presentation.components.MiniPlayer
import com.example.echo_panda_mobile.presentation.viewmodel.GlobalPlayerViewModel
import com.example.echo_panda_mobile.presentation.views.auth.ForgotPasswordScreen
import com.example.echo_panda_mobile.presentation.views.auth.LoginScreen
import com.example.echo_panda_mobile.presentation.views.auth.SignUpScreen
import com.example.echo_panda_mobile.presentation.views.auth.SuccessAccountScreen
import com.example.echo_panda_mobile.presentation.views.intro.EchoPandaOnboardingView
import com.example.echo_panda_mobile.presentation.viewsmodel.ForgotPasswordViewModel
import com.google.firebase.auth.FirebaseAuth
import kotlinx.coroutines.launch

@OptIn(UnstableApi::class)
@Composable
fun AppNavigation() {
    val navController = rememberNavController()
    val backStack by navController.currentBackStackEntryAsState()
    val currentRoute = backStack?.destination?.route
    val scope = rememberCoroutineScope()

    val globalPlayerViewModel: GlobalPlayerViewModel = viewModel()

    // Single stable instances — never recreated on recomposition
    val context = navController.context
    val tokenStorage = remember { TokenStorage.getInstance(context) }
    val auth = remember { FirebaseAuth.getInstance() }
    val authRepo = remember { AuthRepository(tokenStorage) }

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
    LaunchedEffect(currentUser) {
        android.util.Log.d("AppNavigation", "━━━ AUTH STATE CHECK ━━━")
        
        val prefs = navController.context
            .getSharedPreferences("app_prefs", Context.MODE_PRIVATE)

        val hasSeenIntroGlobal = prefs.getBoolean("has_seen_intro", false)
        val localToken = tokenStorage.getToken()
        val localRole = tokenStorage.getRole()
        
        android.util.Log.d("AppNavigation", "Firebase User: ${currentUser?.email ?: "NULL"}")
        android.util.Log.d("AppNavigation", "Local Token: ${if (localToken.isNullOrBlank()) "MISSING" else "PRESENT"}")
        android.util.Log.d("AppNavigation", "Local Role: $localRole")

        if (currentUser != null || !localToken.isNullOrBlank()) {
            val profile = authRepo.getCurrentUserProfile()
            userRole = profile?.role ?: localRole
            android.util.Log.d("AppNavigation", "Authenticated as: ${profile?.email ?: "Local User"}, Role: $userRole")
        } else {
            userRole = null
            android.util.Log.d("AppNavigation", "Not authenticated")
        }

        // Only compute the initial NavHost destination once per cold start.
        if (startRoute != null) {
            android.util.Log.d("AppNavigation", "Start route already set to: $startRoute")
            return@LaunchedEffect
        }

        val destination = if (!hasSeenIntroGlobal) {
            android.util.Log.d("AppNavigation", "Navigation: Showing Intro")
            Routes.INTRO
        } else if (currentUser == null && localToken.isNullOrBlank()) {
            android.util.Log.d("AppNavigation", "Navigation: Showing Login")
            Routes.LOGIN
        } else {
            val route = Routes.getHomeRoute(userRole)
            android.util.Log.d("AppNavigation", "Navigation: Auto-login to $route")
            route
        }

        startRoute = destination
    }

    // ── Bottom nav helpers ────────────────────────────────────────────────────
    val selectedNav = Routes.bottomNavIndex(currentRoute)
    val onNavSelect: (Int) -> Unit = { index ->
        val isAdminRoute = currentRoute?.startsWith("admin/") == true
        val route = if (isAdminRoute || Routes.isAdminRole(userRole)) {
            Routes.adminBottomNavRoute(index)
        } else {
            Routes.bottomNavRoute(index)
        }

        android.util.Log.d("NAVIGATION", "Bottom nav interaction: index $index -> target route: $route")

        navController.navigate(route) {
            popUpTo(navController.graph.findStartDestination().id) {
                saveState = true
            }
            launchSingleTop = true
            restoreState = true
        }
    }

    // ── Main nav graph ────────────────────────────────────────────────────────
    if (startRoute != null) {
        Box(modifier = Modifier.fillMaxSize()) {
            NavHost(
                navController = navController,
                startDestination = startRoute!!
            ) {

                // ── Auth ──────────────────────────────────────────────────────────
                composable(Routes.LOGIN) {
                    LoginScreen(
                        onBack = { navController.popBackStack() },
                        onAuthenticateSuccess = { destination ->
                            scope.launch {
                                val profile = authRepo.getCurrentUserProfile()
                                val role = profile?.role ?: tokenStorage.getRole()
                                userRole = role

                                android.util.Log.d("NAVIGATION", "━━━ LOGIN SUCCESS ━━━")
                                val finalDest = Routes.resolveRoute(destination, role)
                                navController.navigate(finalDest) {
                                    popUpTo(navController.graph.id) { inclusive = true }
                                    launchSingleTop = true
                                }
                            }
                        },
                        onNavigateToSignUp = {
                            navController.navigate(Routes.SIGNUP)
                        },
                        onNavigateToForgotPassword = {
                            navController.navigate(Routes.FORGOT_PASSWORD)
                        }
                    )
                }

                composable(Routes.SIGNUP) {
                    SignUpScreen(
                        onBack = { navController.popBackStack() },
                        onSignUpSuccess = { route ->
                            scope.launch {
                                val profile = authRepo.getCurrentUserProfile()
                                val role = profile?.role
                                userRole = role
                                val finalDest = Routes.resolveRoute(route, role)
                                navController.navigate(finalDest) {
                                    popUpTo(navController.graph.id) { inclusive = true }
                                    launchSingleTop = true
                                }
                            }
                        }
                    )
                }

                composable(Routes.VERIFY_EMAIL) {
                    SuccessAccountScreen(
                        onGoHome = {
                            scope.launch {
                                val profile = authRepo.getCurrentUserProfile()
                                val nextRoute = Routes.getHomeRoute(profile?.role)
                                navController.navigate(nextRoute) {
                                    popUpTo(Routes.VERIFY_EMAIL) { inclusive = true }
                                    launchSingleTop = true
                                }
                            }
                        }
                    )
                }

                composable(Routes.FORGOT_PASSWORD) {
                    val forgotPasswordViewModel: ForgotPasswordViewModel = viewModel(
                        factory = com.example.echo_panda_mobile.presentation.viewsmodel.ForgotPasswordViewModelFactory(LocalContext.current)
                    )
                    val uiState by forgotPasswordViewModel.uiState.collectAsState()

                    LaunchedEffect(uiState.navigateTo) {
                        uiState.navigateTo?.let { route ->
                            navController.navigate(route)
                            forgotPasswordViewModel.onNavigationHandled()
                        }
                    }

                    ForgotPasswordScreen(
                        onBack = { navController.popBackStack() },
                        viewModel = forgotPasswordViewModel
                    )
                }

                // ── Role-based graphs ─────────────────────────────────────────────
                userNavGraph(navController, selectedNav, onNavSelect)
                artistNavGraph(navController, tokenStorage)
                adminNavGraph(navController, selectedNav, onNavSelect, tokenStorage)

                // ── Onboarding ────────────────────────────────────────────────────
                composable(Routes.INTRO) {
                    EchoPandaOnboardingView(
                        onFinished = {
                            navController.context
                                .getSharedPreferences("app_prefs", Context.MODE_PRIVATE)
                                .edit { putBoolean("has_seen_intro", true) }

                            if (currentUser == null && tokenStorage.getToken().isNullOrBlank()) {
                                navController.navigate(Routes.LOGIN) {
                                    popUpTo(Routes.INTRO) { inclusive = true }
                                    launchSingleTop = true
                                }
                            } else {
                                scope.launch {
                                    val profile = authRepo.getCurrentUserProfile()
                                    val nextRoute = Routes.getHomeRoute(profile?.role)
                                    navController.navigate(nextRoute) {
                                        popUpTo(Routes.INTRO) { inclusive = true }
                                        launchSingleTop = true
                                    }
                                }
                            }
                        }
                    )
                }
            }

            // ── Global Mini Player ────────────────────────────────────────────────
            val isPlayerScreen = currentRoute?.contains("player") == true
            val isAuthScreen = currentRoute == Routes.LOGIN || currentRoute == Routes.SIGNUP

            if (!isPlayerScreen && !isAuthScreen) {
                val playerState by globalPlayerViewModel.playerState.collectAsState()
                
                if (playerState.currentTrack != null) {
                    Box(
                        modifier = Modifier
                            .align(Alignment.BottomCenter)
                            .padding(bottom = 80.dp)
                    ) {
                        MiniPlayer(
                            track = playerState.currentTrack!!,
                            isPlaying = playerState.isPlaying,
                            progress = playerState.progress,
                            onTogglePlay = { globalPlayerViewModel.togglePlayPause() },
                            onNext = { globalPlayerViewModel.nextTrack() },
                            onPrevious = { globalPlayerViewModel.previousTrack() },
                            onClose = { globalPlayerViewModel.stopPlayback() },
                            onClick = {
                                val route = Routes.USER_PLAYER.replace(
                                    "{trackId}",
                                    playerState.currentTrack!!.id
                                )
                                navController.navigate(route)
                            }
                        )
                    }
                }
            }
        }
    }
}
