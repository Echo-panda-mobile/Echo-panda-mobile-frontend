package com.example.echo_panda_mobile.data.repository

import com.example.echo_panda_mobile.data.model.AuthResponse
import com.example.echo_panda_mobile.data.model.LoginRequest
import com.example.echo_panda_mobile.data.model.RegisterRequest
import com.example.echo_panda_mobile.data.model.User
import kotlinx.coroutines.delay

sealed class AuthResult<out T> {
    data class Success<T>(val data: T) : AuthResult<T>()
    data class Error(val message: String) : AuthResult<Nothing>()
    object Loading : AuthResult<Nothing>()
}

class AuthRepository {

    // ─────────────────────────────────────────────
    // TODO: Replace with real Retrofit API calls
    //       when backend is ready
    // ─────────────────────────────────────────────

    suspend fun login(request: LoginRequest): AuthResult<AuthResponse> {
        return try {
            delay(1000) // Simulate network call

            // Fake validation
            if (request.email.isBlank() || request.password.isBlank()) {
                return AuthResult.Error("Email and password are required.")
            }

            // Fake users — remove when real API is connected
            val fakeUsers = mapOf(
                "user@test.com"   to Pair("password123", "user"),
                "artist@test.com" to Pair("password123", "artist")
            )

            val match = fakeUsers[request.email]
            if (match == null || match.first != request.password) {
                return AuthResult.Error("Invalid email or password.")
            }

            val user = User(
                id    = 1,
                name  = if (match.second == "artist") "Test Artist" else "Test User",
                email = request.email,
                role  = match.second,
                token = "fake-jwt-token-${System.currentTimeMillis()}"
            )

            AuthResult.Success(
                AuthResponse(user = user, token = user.token, message = "Login successful.")
            )
        } catch (e: Exception) {
            AuthResult.Error("Something went wrong. Please try again.")
        }
    }

    suspend fun register(request: RegisterRequest): AuthResult<AuthResponse> {
        return try {
            delay(1000) // Simulate network call

            // Fake validation
            when {
                request.name.isBlank()                          -> return AuthResult.Error("Name is required.")
                request.email.isBlank()                         -> return AuthResult.Error("Email is required.")
                request.password.length < 8                     -> return AuthResult.Error("Password must be at least 8 characters.")
                request.password != request.passwordConfirmation -> return AuthResult.Error("Passwords do not match.")
            }

            val user = User(
                id    = 2,
                name  = request.name,
                email = request.email,
                role  = request.role,
                token = "fake-jwt-token-${System.currentTimeMillis()}"
            )

            AuthResult.Success(
                AuthResponse(user = user, token = user.token, message = "Account created successfully.")
            )
        } catch (e: Exception) {
            AuthResult.Error("Something went wrong. Please try again.")
        }
    }
}