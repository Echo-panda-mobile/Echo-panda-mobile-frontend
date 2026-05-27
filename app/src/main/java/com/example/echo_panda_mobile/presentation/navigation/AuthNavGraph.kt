package com.example.echo_panda_mobile.presentation.navigation

import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import com.example.echo_panda_mobile.presentation.viewsmodel.AuthState
import com.example.echo_panda_mobile.presentation.viewsmodel.AuthViewModel

@Composable
fun AuthNavGraph(viewModel: AuthViewModel) {
    val authState by viewModel.authState.collectAsState()
    val navController = rememberNavController()

    when (val state = authState) {
        is AuthState.Success -> {
            NavHost(navController = navController, startDestination = state.role) {
                composable("admin") { AdminScreen() }
                composable("artist") { ArtistScreen() }
                composable("user") { UserScreen() }
            }
        }
        is AuthState.Loading -> {
            Text("Logging in...")
        }
        is AuthState.Error -> {
            Text("Error: ${state.message}")
        }
        else -> {
            // Show Login Screen (not implemented in this NavHost for brevity, 
            // but usually you'd have a "login" destination here)
            Text("Please Login")
        }
    }
}

@Composable
fun AdminScreen() {
    Text("Welcome to Admin Screen")
}

@Composable
fun ArtistScreen() {
    Text("Welcome to Artist Screen")
}

@Composable
fun UserScreen() {
    Text("Welcome to User Screen")
}
