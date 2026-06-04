package com.example.echo_panda_mobile.presentation.navigation

object BrowseSection {
    const val CONTINUE_LISTENING = "continue_listening"
    const val POPULAR_ARTISTS = "popular_artists"
    const val TOP_ALBUMS = "top_albums"
    const val RECENT_LISTENING = "recent_listening"
    const val GENRES = "genres"
    const val MOOD_PLAYLISTS = "mood_playlists"
    const val NEW_RELEASES = "new_releases"
    const val MOST_PLAYED = "most_played"
    const val ALL_ALBUMS = "all_albums"
    const val TOP_PICKS = "top_picks"

    fun title(section: String): String = when (section) {
        CONTINUE_LISTENING -> "Continue Listening"
        POPULAR_ARTISTS -> "Popular Artists"
        TOP_ALBUMS -> "Top Albums"
        RECENT_LISTENING -> "Based on Your Listening"
        GENRES -> "Music Genres"
        MOOD_PLAYLISTS -> "Mood Playlists"
        NEW_RELEASES -> "New Release Songs"
        MOST_PLAYED -> "Most Played Songs"
        ALL_ALBUMS -> "All Albums"
        TOP_PICKS -> "Top Picks"
        else -> "Browse"
    }
}
