package com.example.echo_panda_mobile.data.model

import androidx.compose.runtime.Immutable

enum class UserRole {
    USER,
    ARTIST,
    ADMIN,
    UNKNOWN
}

@Immutable
data class AuthState(
    val isAuthenticated: Boolean = false,
    val user: User? = null,
    val role: UserRole = UserRole.UNKNOWN,
    val token: String? = null,
    val isLoading: Boolean = false,
    val error: String? = null
) {
    val isUser: Boolean get() = role == UserRole.USER
    val isArtist: Boolean get() = role == UserRole.ARTIST
    val isAdmin: Boolean get() = role == UserRole.ADMIN
}

@Immutable
data class ArtistStats(
    val totalStreams: Long = 0L,
    val totalListeners: Long = 0L,
    val totalSongs: Int = 0,
    val monthlyRevenue: Double = 0.0,
    val averageStreamLength: Int = 0, // in seconds
    val topTrackId: String = "",
    val topTrackName: String = "",
    val topTrackStreams: Long = 0L,
    val monthlyGrowth: Double = 0.0 // percentage
)

@Immutable
data class UserStats(
    val totalListened: Long = 0L,
    val totalSongs: Int = 0,
    val totalPlaylists: Int = 0,
    val followingCount: Int = 0,
    val followersCount: Int = 0,
    val favoriteGenres: List<String> = emptyList()
)
