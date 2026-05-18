package com.example.echo_panda_mobile.data.model

data class User(
    val id: Int,
    val name: String,
    val email: String,
    val role: String,       // "user" | "artist"
    val token: String
)

data class LoginRequest(
    val email: String,
    val password: String
)

data class RegisterRequest(
    val name: String,
    val email: String,
    val password: String,
    val passwordConfirmation: String,
    val role: String        // "user" | "artist"
)

data class AuthResponse(
    val user: User,
    val token: String,
    val message: String
)