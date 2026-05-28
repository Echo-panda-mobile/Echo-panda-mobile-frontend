package com.example.echo_panda_mobile.data.model

import androidx.compose.ui.graphics.Color

// ─── Music Domain Models ───────────────────────────────────────────────────────

data class Artist(
    val id: String,
    val name: String,
    val imageUrl: String? = null,
    val monthlyListeners: String = "0",
    val placeholderColors: List<Color> = listOf(Color(0xFF2C2C3A), Color(0xFF1A1A26))
)

data class Album(
    val id: String,
    val title: String,
    val artist: String,
    val imageUrl: String? = null,
    val placeholderColors: List<Color> = listOf(Color(0xFF2C2C3A), Color(0xFF1A1A26)),
    val tracks: List<Track> = emptyList(),
    val totalDuration: String = "0m",
    val artistImageUrl: String? = null
)

data class Track(
    val id: String,
    val title: String,
    val artist: String,
    val album: String? = null,
    val durationMs: Long = 0,
    val imageUrl: String? = null,
    val placeholderColors: List<Color> = listOf(Color(0xFF2C2C3A), Color(0xFF1A1A26)),
    val lyrics: String? = null,
    val isFavorite: Boolean = false,
    val isDownloaded: Boolean = false
)

data class Playlist(
    val id: String,
    val title: String,
    val imageUrl: String? = null,
    val labelOverlay: String? = null,          // text shown on top of image
    val placeholderColors: List<Color> = listOf(Color(0xFF2C2C3A), Color(0xFF1A1A26))
)

data class Genre(
    val id: String,
    val name: String,
    val subLabel: String,
    val placeholderColors: List<Color>,
    val imageUrl: String? = null
)

data class MoodPlaylist(
    val id: String,
    val name: String,
    val subLabel: String,
    val placeholderColors: List<Color>,
    val imageUrl: String? = null
)

data class BrowseCategory(
    val id: String,
    val title: String,
    val bgColor: Color,
    val accentColors: List<Color>
)

data class FeaturedArtist(
    val artist: Artist,
    val description: String,
    val ctaLabel: String = "Listen Now"
)