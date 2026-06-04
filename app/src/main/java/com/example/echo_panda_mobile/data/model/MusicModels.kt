package com.example.echo_panda_mobile.data.model

import androidx.compose.ui.graphics.Color

data class Artist(
    val id: String,
    val name: String,
    val imageUrl: String? = null,
    val bio: String? = null,
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

/** API `duration` may be seconds or, if mis-stored, milliseconds. */
private const val DURATION_SECONDS_VS_MS_THRESHOLD = 10_000

fun resolveDurationMs(durationSeconds: Int?): Long {
    val raw = durationSeconds ?: return 0L
    return if (raw > DURATION_SECONDS_VS_MS_THRESHOLD) raw.toLong() else raw * 1000L
}

fun formatTrackDuration(durationMs: Long): String {
    if (durationMs <= 0L) return "--:--"
    val totalSeconds = durationMs / 1000
    val minutes = totalSeconds / 60
    val seconds = totalSeconds % 60
    return String.format(java.util.Locale.getDefault(), "%d:%02d", minutes, seconds)
}

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
    val isDownloaded: Boolean = false,
    val resumePositionMs: Long? = null
)

data class Playlist(
    val id: String,
    val title: String,
    val imageUrl: String? = null,
    val labelOverlay: String? = null,          // text shown on top of image
    val placeholderColors: List<Color> = listOf(Color(0xFF2C2C3A), Color(0xFF1A1A26)),
    val resumePositionMs: Long? = null,
    val trackId: String? = null
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
