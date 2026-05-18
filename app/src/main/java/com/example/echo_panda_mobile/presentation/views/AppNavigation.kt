package com.example.echo_panda_mobile.presentation.views

import androidx.compose.runtime.Composable
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import com.example.echo_panda_mobile.presentation.views.auth.LoginScreen
import com.example.echo_panda_mobile.presentation.views.auth.SignUpScreen
import com.example.echo_panda_mobile.presentation.views.user.home.HomeScreen

@Composable
fun AppNavigation() {
    val navController = rememberNavController()

    NavHost(
        navController    = navController,
        startDestination = Routes.LOGIN
    ) {
        // ── Auth ──────────────────────────────────────────────────────────
        composable(Routes.LOGIN) {
            LoginScreen(
                onNavigateToSignUp = {
                    navController.navigate(Routes.SIGNUP)
                },
                onLoginSuccess = { route ->
                    navController.navigate(route) {
                        popUpTo(Routes.LOGIN) { inclusive = true }
                    }
                }
            )
        }

        composable(Routes.SIGNUP) {
            SignUpScreen(
                onNavigateToLogin = {
                    navController.popBackStack()
                },
                onRegisterSuccess = { route ->
                    navController.navigate(route) {
                        popUpTo(Routes.LOGIN) { inclusive = true }
                    }
                }
            )
        }

        // ── User routes ───────────────────────────────────────────────────
        composable(Routes.USER_HOME) {
            HomeScreen() // TODO: replace with real UserHomeScreen
        }

        // TODO: add USER_SEARCH, USER_PLAYER, USER_LIBRARY, USER_PROFILE

        // ── Artist routes ─────────────────────────────────────────────────
        composable(Routes.ARTIST_DASHBOARD) {
            // TODO: ArtistDashboardScreen()
        }

        // TODO: add ARTIST_MY_MUSIC, ARTIST_UPLOAD, ARTIST_PROFILE
    }
}