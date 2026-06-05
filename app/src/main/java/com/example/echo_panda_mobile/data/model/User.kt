package com.example.echo_panda_mobile.data.model

import com.google.gson.annotations.SerializedName

data class User(
    val id: Int,
    val name: String,
    val email: String,
    val role: String,
    val token: String,
    @SerializedName("image_url") val photoUrl: String? = null,
    val artistId: Int? = null
) {
    fun getDisplayPhotoUrl(): String? {
        val raw = photoUrl ?: return null
        if (raw.startsWith("http") || raw.startsWith("content://") || raw.startsWith("file://")) return raw
        
        val apiBase = com.example.echo_panda_mobile.BuildConfig.API_BASE_URL
        val domainBase = apiBase.replace("/api/", "/")
        
        val cleanPath = if (raw.startsWith("/")) raw.substring(1) else raw
        
        return if (!cleanPath.contains("storage/") && !cleanPath.startsWith("http")) {
            "${domainBase}storage/$cleanPath"
        } else {
            "$domainBase$cleanPath"
        }
    }
}

data class LoginRequest(
    val email: String,
    val password: String
)

data class RegisterRequest(
    val name: String,
    val email: String,
    val password: String,
    val passwordConfirmation: String,
    val role: String
)

data class AuthResponse(
    val user: User,
    val token: String,
    val message: String,
    val redirectTo: String? = null
)
