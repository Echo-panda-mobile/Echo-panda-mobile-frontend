package com.example.echo_panda_mobile.data.repository

import androidx.compose.ui.graphics.Color
import com.example.echo_panda_mobile.data.model.Artist
import com.example.echo_panda_mobile.data.model.Playlist
import com.example.echo_panda_mobile.data.model.Track
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import java.util.UUID

object LibraryRepository {
    private val initialArtists = listOf(
        Artist(id = "a1", name = "Conan Gray", placeholderColors = listOf(Color(0xFF3A1A2A), Color(0xFF1A0A12))),
        Artist(id = "a2", name = "The Neighbourhood", placeholderColors = listOf(Color(0xFF1A2A4A), Color(0xFF0A1228)))
    )

    private val initialPlaylists = listOf(
        Playlist(
            id = "p1",
            title = "Chill Vibes",
            placeholderColors = listOf(Color(0xFF0D1F2A), Color(0xFF11304A))
        ),
        Playlist(
            id = "p2",
            title = "Top Hits",
            placeholderColors = listOf(Color(0xFF2A1F33), Color(0xFF3B2A51))
        ),
        Playlist(
            id = "p3",
            title = "Workout",
            placeholderColors = listOf(Color(0xFF2A2A1E), Color(0xFF3A3A24))
        )
    )

    private val _playlists = MutableStateFlow(initialPlaylists)
    val playlists: StateFlow<List<Playlist>> = _playlists.asStateFlow()

    private val _followedArtists = MutableStateFlow(initialArtists)
    val followedArtists: StateFlow<List<Artist>> = _followedArtists.asStateFlow()

    private val _favoriteTracks = MutableStateFlow<List<Track>>(emptyList())
    val favoriteTracks: StateFlow<List<Track>> = _favoriteTracks.asStateFlow()

    private val playlistTracks: MutableMap<String, MutableSet<String>> = initialPlaylists
        .associate { it.id to mutableSetOf<String>() }
        .toMutableMap()

    fun createPlaylist(title: String): Playlist {
        val trimmedTitle = title.trim().takeIf { it.isNotBlank() } ?: return initialPlaylists.first()
        val newPlaylist = Playlist(
            id = UUID.randomUUID().toString(),
            title = trimmedTitle,
            placeholderColors = listOf(Color(0xFF233142), Color(0xFF1B2B3F))
        )
        _playlists.value = _playlists.value + newPlaylist
        playlistTracks[newPlaylist.id] = mutableSetOf()
        return newPlaylist
    }

    fun followArtist(name: String): Artist {
        val trimmedName = name.trim().takeIf { it.isNotBlank() } ?: "Unknown Artist"
        val newArtist = Artist(
            id = UUID.randomUUID().toString(),
            name = trimmedName,
            placeholderColors = listOf(Color(0xFF1A3A2A), Color(0xFF0A1A12))
        )
        _followedArtists.value = _followedArtists.value + newArtist
        return newArtist
    }

    fun addTrackToPlaylist(track: Track, playlistId: String) {
        val trackSet = playlistTracks.getOrPut(playlistId) { mutableSetOf() }
        trackSet += track.id
        // Emit a new list to refresh collectors
        _playlists.value = _playlists.value.toList()
    }

    /**
     * Upsert a playlist from server into the local state. If the playlist already exists (by id)
     * it will be replaced, otherwise appended.
     */
    fun addOrUpdatePlaylist(playlist: Playlist) {
        val existing = _playlists.value.indexOfFirst { it.id == playlist.id }
        _playlists.value = if (existing >= 0) {
            _playlists.value.toMutableList().also { it[existing] = playlist }
        } else {
            _playlists.value + playlist
        }
        playlistTracks.putIfAbsent(playlist.id, mutableSetOf())
    }

    fun getPlaylistTrackCount(playlistId: String): Int {
        return playlistTracks[playlistId]?.size ?: 0
    }

    fun addTrackToFavorites(track: Track) {
        if (_favoriteTracks.value.none { it.id == track.id }) {
            _favoriteTracks.value = _favoriteTracks.value + track
        }
    }

    fun searchFavorites(query: String): List<Track> {
        val lowerQuery = query.trim().lowercase()
        if (lowerQuery.isBlank()) return _favoriteTracks.value
        return _favoriteTracks.value.filter {
            it.title.lowercase().contains(lowerQuery) || it.artist.lowercase().contains(lowerQuery)
        }
    }
}
