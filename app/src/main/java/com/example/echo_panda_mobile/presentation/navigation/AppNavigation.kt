package com.example.echo_panda_mobile.presentation.navigation

import androidx.compose.runtime.*
import androidx.navigation.NavGraph.Companion.findStartDestination
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.rememberNavController
import com.example.echo_panda_mobile.presentation.views.user.discover.DiscoverScreen
import com.example.echo_panda_mobile.presentation.views.user.home.HomeScreen
import com.example.echo_panda_mobile.presentation.views.auth.LoginScreen
import com.example.echo_panda_mobile.presentation.views.auth.SignUpScreen
import com.example.echo_panda_mobile.presentation.views.user.profile.UserProfileScreen
import com.example.echo_panda_mobile.presentation.views.user.profile.UserSettingsScreen
import com.example.echo_panda_mobile.presentation.views.user.library.LibraryScreen
import com.google.firebase.auth.FirebaseAuth

@Composable
fun AppNavigation() {
    val navController = rememberNavController()
    val backStack by navController.currentBackStackEntryAsState()
    val currentRoute = backStack?.destination?.route

    // Reactive check for Firebase user state
    val auth = remember { FirebaseAuth.getInstance() }
    var currentUser by remember { mutableStateOf(auth.currentUser) }

    DisposableEffect(auth) {
        val listener = FirebaseAuth.AuthStateListener {
            currentUser = it.currentUser
        }
        auth.addAuthStateListener(listener)
        onDispose { auth.removeAuthStateListener(listener) }
    }

    val startRoute = remember(currentUser) {
        if (currentUser != null) Routes.USER_HOME else Routes.LOGIN
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
                onNavigateToProfile = { navController.navigate(Routes.USER_PROFILE) }
            )
        }

        composable(Routes.USER_DISCOVER) {
            DiscoverScreen(selectedNav = selectedNav, onNavSelect = onNavSelect)
        }

        composable(Routes.USER_LIBRARY) {
            LibraryScreen(selectedNav = selectedNav, onNavSelect = onNavSelect)
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
    }
}
