package com.example.echo_panda_mobile.presentation.views

import androidx.compose.runtime.Composable
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import com.example.echo_panda_mobile.presentation.views.auth.ForgotPasswordScreen
import com.example.echo_panda_mobile.presentation.views.auth.LoginScreen
import com.example.echo_panda_mobile.presentation.views.auth.SignUpScreen
import com.example.echo_panda_mobile.presentation.views.auth.VerifyEmailScreen
import com.example.echo_panda_mobile.presentation.views.intro.EchoPandaOnboardingView
import com.example.echo_panda_mobile.presentation.views.user.artist.ArtistDashboardScreen
import com.example.echo_panda_mobile.presentation.views.user.artist.ArtistProfileScreen
import com.example.echo_panda_mobile.presentation.views.user.artist.ArtistSettingsScreen
import com.example.echo_panda_mobile.presentation.views.user.home.HomeScreen
import com.example.echo_panda_mobile.presentation.views.user.profile.UserProfileScreen
import com.example.echo_panda_mobile.presentation.views.user.profile.UserSettingsScreen

@Composable
fun AppNavigation(
    isDarkMode: Boolean = true,
    language: String = "en"
) {
    val navController = rememberNavController()

    NavHost(
        navController = navController,
        startDestination = Routes.INTRO_SPLASH
    ) {
        // ── INTRO SPLASH ──────────────────────────────────────────────
        composable(Routes.INTRO_SPLASH) {
            EchoPandaOnboardingView(
                onFinished = {
                    navController.navigate(Routes.ONBOARDING) {
                        popUpTo(Routes.INTRO_SPLASH) { inclusive = true }
                    }
                }
            )
        }

        // ── ONBOARDING ────────────────────────────────────────────────
        composable(Routes.ONBOARDING) {
            EchoPandaOnboardingView(
                onFinished = {
                    navController.navigate(Routes.LOGIN) {
                        popUpTo(Routes.ONBOARDING) { inclusive = true }
                    }
                }
            )
        }

        // ── LOGIN ─────────────────────────────────────────────────────
        composable(Routes.LOGIN) {
            LoginScreen(
                onBack = { navController.popBackStack() },
                onAuthenticateSuccess = { destination ->
                    navController.navigate(destination) {
                        popUpTo(Routes.LOGIN) { inclusive = true }
                        launchSingleTop = true
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

        // ── FORGOT PASSWORD ───────────────────────────────────────────
        composable(Routes.FORGOT_PASSWORD) {
            ForgotPasswordScreen(
                onBack = {
                    navController.popBackStack()
                },
                onPasswordResetSent = {
                    navController.popBackStack()
                }
            )
        }

        // ── SIGN UP ───────────────────────────────────────────────────
        composable(Routes.SIGNUP) {
            SignUpScreen(
                onBack = {
                    navController.navigate(Routes.LOGIN) {
                        popUpTo(Routes.SIGNUP) { inclusive = true }
                    }
                },
                onSignUpSuccess = { destination ->
                    if (destination == Routes.VERIFY_EMAIL) {
                        navController.navigate(Routes.VERIFY_EMAIL) {
                            popUpTo(Routes.SIGNUP) { inclusive = true }
                        }
                    } else {
                        navController.navigate(destination) {
                            popUpTo(Routes.SIGNUP) { inclusive = true }
                            launchSingleTop = true
                        }
                    }
                }
            )
        }

        // ── VERIFY EMAIL ─────────────────────────────────────────────────
        composable(Routes.VERIFY_EMAIL) {
            VerifyEmailScreen(
                onCancel = {
                    navController.navigate(Routes.LOGIN) {
                        popUpTo(Routes.VERIFY_EMAIL) { inclusive = true }
                    }
                },
                onNext = {
                    navController.navigate(Routes.LOGIN) {
                        popUpTo(Routes.VERIFY_EMAIL) { inclusive = true }
                    }
                }
            )
        }

        // ── HOME ──────────────────────────────────────────────────────
        composable(Routes.USER_HOME) {
            HomeScreen(
                onNavigateToUserProfile = { navController.navigate(Routes.USER_PROFILE) },
                onNavigateToArtistProfile = { navController.navigate(Routes.ARTIST_PROFILE) }
            )
        }

        // ── USER PROFILE ──────────────────────────────────────────────────
        composable(Routes.USER_PROFILE) {
            UserProfileScreen(
                onBack = { navController.popBackStack() },
                onSettings = { navController.navigate(Routes.USER_SETTINGS) },
                onEditProfile = { navController.navigate(Routes.USER_SETTINGS) },
                onLogoutSuccess = {
                    navController.navigate(Routes.LOGIN) {
                        popUpTo(0) { inclusive = true }
                    }
                }
            )
        }

        // ── USER SETTINGS ─────────────────────────────────────────────────
        composable(Routes.USER_SETTINGS) {
            UserSettingsScreen(onBack = { navController.popBackStack() })
        }

        // ── ARTIST DASHBOARD ──────────────────────────────────────────────
        composable(Routes.ARTIST_DASHBOARD) {
            ArtistDashboardScreen(
                onNavigateToProfile = { navController.navigate(Routes.ARTIST_PROFILE) },
                onNavigateToSettings = { navController.navigate(Routes.ARTIST_SETTINGS) }
            )
        }

        // ── ARTIST PROFILE ────────────────────────────────────────────────
        composable(Routes.ARTIST_PROFILE) {
            ArtistProfileScreen(
                onBack = { navController.popBackStack() },
                onSettings = { navController.navigate(Routes.ARTIST_SETTINGS) }
            )
        }

        // ── ARTIST SETTINGS ───────────────────────────────────────────────
        composable(Routes.ARTIST_SETTINGS) {
            ArtistSettingsScreen(
                onBack = { navController.popBackStack() },
                onLogoutSuccess = {
                    navController.navigate(Routes.LOGIN) {
                        popUpTo(0) { inclusive = true }
                    }
                }
            )
        }
    }
}
