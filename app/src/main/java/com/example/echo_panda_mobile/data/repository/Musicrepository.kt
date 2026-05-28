package com.example.echo_panda_mobile.data.repository

import androidx.compose.ui.graphics.Color
import com.example.echo_panda_mobile.data.model.*
import com.example.echo_panda_mobile.data.remote.*
import kotlinx.coroutines.delay
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

// ─── Result wrapper (reuse pattern from AuthRepository) ───────────────────────
sealed class MusicResult<out T> {
    data class Success<T>(val data: T) : MusicResult<T>()
    data class Error(val message: String) : MusicResult<Nothing>()
    object Loading : MusicResult<Nothing>()
}

class MusicRepository(private val apiService: MusicApiService? = null) {

    private val defaultColors = listOf(Color(0xFF2C2C3A), Color(0xFF1A1A26))

    // Helper for deterministic colors
    private fun getColorsForId(id: String): List<Color> {
        val hash = id.hashCode()
        return listOf(
            Color((hash or 0xFF000000.toInt())),
            Color(((hash shr 8) or 0xFF000000.toInt()))
        )
    }

    // ── Playlists & Mixes ────────────────────────────────────────────────────

    suspend fun getRecentPlaylists(): MusicResult<List<Playlist>> = withContext(Dispatchers.IO) {
        if (apiService == null) return@withContext MusicResult.Error("API service not initialized")
        try {
            val response = apiService.getAlbums(sortBy = "latest")
            if (response.isSuccessful) {
                val playlists = response.body()?.data?.map {
                    Playlist(
                        id = it.id,
                        title = it.title,
                        imageUrl = it.coverUrl,
                        placeholderColors = getColorsForId(it.id)
                    )
                } ?: emptyList()
                MusicResult.Success(playlists)
            } else {
                MusicResult.Error("Failed to fetch playlists: ${response.code()}")
            }
        } catch (e: Exception) {
            MusicResult.Error(e.localizedMessage ?: "Network error")
        }
    }

    // ── Artists ───────────────────────────────────────────────────────────────

    suspend fun getPopularArtists(): MusicResult<List<Artist>> = withContext(Dispatchers.IO) {
        if (apiService == null) return@withContext MusicResult.Error("API service not initialized")
        try {
            val response = apiService.getArtists()
            if (response.isSuccessful) {
                val artists = response.body()?.data?.map { it.toDomain() } ?: emptyList()
                MusicResult.Success(artists)
            } else {
                MusicResult.Error("Failed to fetch artists: ${response.code()}")
            }
        } catch (e: Exception) {
            MusicResult.Error(e.localizedMessage ?: "Network error")
        }
    }

    suspend fun getArtistById(artistId: String): MusicResult<Artist> = withContext(Dispatchers.IO) {
        if (apiService == null) return@withContext MusicResult.Error("API service not initialized")
        try {
            val response = apiService.getArtistDetail(artistId)
            if (response.isSuccessful) {
                val artistDto = response.body() ?: return@withContext MusicResult.Error("Empty response body")
                MusicResult.Success(artistDto.toDomain())
            } else {
                MusicResult.Error("Failed to fetch artist details: ${response.code()}")
            }
        } catch (e: Exception) {
            MusicResult.Error(e.localizedMessage ?: "Network error")
        }
    }

    suspend fun getArtistAlbums(artistName: String): MusicResult<List<Album>> = withContext(Dispatchers.IO) {
        if (apiService == null) return@withContext MusicResult.Error("API service not initialized")
        try {
            val response = apiService.getAlbums(search = artistName)
            if (response.isSuccessful) {
                val albums = response.body()?.data?.map { it.toDomain() } ?: emptyList()
                MusicResult.Success(albums)
            } else {
                MusicResult.Error("Failed to fetch artist albums: ${response.code()}")
            }
        } catch (e: Exception) {
            MusicResult.Error(e.localizedMessage ?: "Network error")
        }
    }

    suspend fun getArtistSongs(artistName: String): MusicResult<List<Track>> = withContext(Dispatchers.IO) {
        if (apiService == null) return@withContext MusicResult.Error("API service not initialized")
        try {
            val response = apiService.getSongs(search = artistName)
            if (response.isSuccessful) {
                val tracks = response.body()?.data?.map { it.toDomain(artistName) } ?: emptyList()
                MusicResult.Success(tracks)
            } else {
                MusicResult.Error("Failed to fetch artist songs: ${response.code()}")
            }
        } catch (e: Exception) {
            MusicResult.Error(e.localizedMessage ?: "Network error")
        }
    }

    // ── Albums ────────────────────────────────────────────────────────────────

    suspend fun getTopAlbums(): MusicResult<List<Album>> = withContext(Dispatchers.IO) {
        if (apiService == null) return@withContext MusicResult.Error("API service not initialized")
        try {
            val response = apiService.getMostPlayedAlbums(limit = 10)
            if (response.isSuccessful) {
                val albums = response.body()?.data?.map { it.album.toDomain() } ?: emptyList()
                MusicResult.Success(albums)
            } else {
                // Fallback to regular albums if stats fail
                val fallback = apiService.getAlbums(sortBy = "latest")
                if (fallback.isSuccessful) {
                    val albums = fallback.body()?.data?.map { it.toDomain() } ?: emptyList()
                    MusicResult.Success(albums)
                } else {
                    MusicResult.Error("Failed to fetch top albums: ${response.code()}")
                }
            }
        } catch (e: Exception) {
            MusicResult.Error(e.localizedMessage ?: "Network error")
        }
    }

    suspend fun getNewAlbums(): MusicResult<List<Album>> = withContext(Dispatchers.IO) {
        if (apiService == null) return@withContext MusicResult.Error("API service not initialized")
        try {
            val response = apiService.getAlbums(sortBy = "latest", perPage = 10)
            if (response.isSuccessful) {
                val albums = response.body()?.data?.map { it.toDomain() } ?: emptyList()
                MusicResult.Success(albums)
            } else {
                MusicResult.Error("Failed to fetch new albums: ${response.code()}")
            }
        } catch (e: Exception) {
            MusicResult.Error(e.localizedMessage ?: "Network error")
        }
    }

    suspend fun getPopularAlbums(): MusicResult<List<Album>> = withContext(Dispatchers.IO) {
        if (apiService == null) return@withContext MusicResult.Error("API service not initialized")
        try {
            val response = apiService.getAlbums(perPage = 10)
            if (response.isSuccessful) {
                val albums = response.body()?.data?.map { it.toDomain() } ?: emptyList()
                MusicResult.Success(albums)
            } else {
                MusicResult.Error("Failed to fetch popular albums: ${response.code()}")
            }
        } catch (e: Exception) {
            MusicResult.Error(e.localizedMessage ?: "Network error")
        }
    }

    suspend fun getAlbumById(id: String): MusicResult<Album> = withContext(Dispatchers.IO) {
        if (apiService == null) return@withContext MusicResult.Error("API service not initialized")
        try {
            // 1. Fetch Album Detail
            val albumResponse = apiService.getAlbumDetail(id)
            if (!albumResponse.isSuccessful) return@withContext MusicResult.Error("Album not found")
            val albumDto = albumResponse.body() ?: return@withContext MusicResult.Error("Empty response body")

            // 2. Fetch Album Songs
            val songsResponse = apiService.getSongs(albumId = albumDto.id.toIntOrNull())
            val tracks = if (songsResponse.isSuccessful) {
                songsResponse.body()?.data?.map { it.toDomain(albumDto.artist) } ?: emptyList()
            } else emptyList()

            MusicResult.Success(
                albumDto.toDomain().copy(
                    tracks = tracks,
                    totalDuration = "${tracks.sumOf { it.durationMs } / 60000}m"
                )
            )
        } catch (e: Exception) {
            MusicResult.Error(e.localizedMessage ?: "Network error")
        }
    }

    // ── Songs ─────────────────────────────────────────────────────────────────

    suspend fun getAllSongs(search: String? = null): MusicResult<List<Track>> = withContext(Dispatchers.IO) {
        if (apiService == null) return@withContext MusicResult.Error("API service not initialized")
        try {
            val response = apiService.getSongs(search = search)
            if (response.isSuccessful) {
                val tracks = response.body()?.data?.map { it.toDomain("Unknown") } ?: emptyList()
                MusicResult.Success(tracks)
            } else {
                MusicResult.Error("Failed to fetch songs: ${response.code()}")
            }
        } catch (e: Exception) {
            MusicResult.Error(e.localizedMessage ?: "Network error")
        }
    }

    suspend fun getTrackById(id: String): MusicResult<Track> = withContext(Dispatchers.IO) {
        if (apiService == null) return@withContext MusicResult.Error("API service not initialized")
        try {
            val response = apiService.getSongDetail(id)
            if (response.isSuccessful) {
                val songDto = response.body() ?: return@withContext MusicResult.Error("Empty response body")
                MusicResult.Success(songDto.toDomain("Unknown"))
            } else {
                MusicResult.Error("Failed to fetch track: ${response.code()}")
            }
        } catch (e: Exception) {
            MusicResult.Error(e.localizedMessage ?: "Network error")
        }
    }

    suspend fun getMostPlayedSongs(limit: Int = 10): MusicResult<List<Track>> = withContext(Dispatchers.IO) {
        if (apiService == null) return@withContext MusicResult.Error("API service not initialized")
        try {
            val response = apiService.getMostPlayedSongs(limit = limit)
            if (response.isSuccessful) {
                val tracks = response.body()?.data?.map { it.song.toDomain("Unknown") } ?: emptyList()
                MusicResult.Success(tracks)
            } else {
                MusicResult.Error("Failed to fetch most played songs: ${response.code()}")
            }
        } catch (e: Exception) {
            MusicResult.Error(e.localizedMessage ?: "Network error")
        }
    }

    // ── Extensions for Mapping ────────────────────────────────────────────────

    private fun ArtistDto.toDomain() = Artist(
        id = id,
        name = name,
        imageUrl = imageUrl,
        monthlyListeners = monthlyListeners ?: "0M",
        placeholderColors = getColorsForId(id)
    )

    private fun AlbumDto.toDomain() = Album(
        id = id,
        title = title,
        artist = artist,
        imageUrl = coverUrl,
        placeholderColors = getColorsForId(id)
    )

    private fun SongDto.toDomain(defaultArtist: String) = Track(
        id = id,
        title = title,
        artist = artist ?: defaultArtist,
        durationMs = durationSeconds * 1000L,
        imageUrl = coverUrl,
        placeholderColors = getColorsForId(id)
    )

    // ── Placeholders ──────────────────────────────────────────────────────────

    suspend fun getTopMixes(): MusicResult<List<Playlist>> = withContext(Dispatchers.IO) {
        MusicResult.Success(listOf(
            Playlist("m1", "Trending Music", labelOverlay = "TRENDING", placeholderColors = listOf(Color(0xFF1565C0), Color(0xFF0D47A1))),
            Playlist("m2", "Weekly Top Songs", labelOverlay = "WEEKLY TOP", placeholderColors = listOf(Color(0xFFF9A825), Color(0xFFF57F17)))
        ))
    }

    suspend fun getRecentListening(): MusicResult<List<Playlist>> = getRecentPlaylists()

    suspend fun getFeaturedArtist(): MusicResult<FeaturedArtist> = withContext(Dispatchers.IO) {
        val artists = getPopularArtists()
        if (artists is MusicResult.Success && artists.data.isNotEmpty()) {
            val artist = artists.data.first()
            MusicResult.Success(FeaturedArtist(
                artist = artist,
                description = "Discover the latest tracks from ${artist.name}.",
                ctaLabel = "Listen Now"
            ))
        } else {
            MusicResult.Error("No featured artist found")
        }
    }

    suspend fun getGenres(): MusicResult<List<Genre>> = withContext(Dispatchers.IO) {
        MusicResult.Success(listOf(
            Genre("g1", "Rap Tracks", "Rap Songs", listOf(Color(0xFF3A1A2A), Color(0xFF1A0A12))),
            Genre("g2", "Pop Tracks", "Pop Songs", listOf(Color(0xFF1A2A4A), Color(0xFF0A1228)))
        ))
    }

    suspend fun getMoodPlaylists(): MusicResult<List<MoodPlaylist>> = withContext(Dispatchers.IO) {
        MusicResult.Success(listOf(
            MoodPlaylist("mo1", "Sad Songs", "Sad Songs", listOf(Color(0xFF1A2030), Color(0xFF0A1018))),
            MoodPlaylist("mo2", "Workout Songs", "Workout Songs", listOf(Color(0xFF3A2A10), Color(0xFF1A1208)))
        ))
    }

    suspend fun getNewReleases(): MusicResult<List<Track>> = getAllSongs()

    suspend fun getBrowseCategories(): MusicResult<List<BrowseCategory>> = withContext(Dispatchers.IO) {
        MusicResult.Success(listOf(
            BrowseCategory("bc1", "Made for You", Color(0xFF1565C0), listOf(Color(0xFF0D47A1), Color(0xFF1976D2))),
            BrowseCategory("bc2", "RELEASED", Color(0xFF6A1B9A), listOf(Color(0xFF4A148C), Color(0xFF8E24AA)))
        ))
    }

    suspend fun toggleFavorite(trackId: String): MusicResult<Boolean> = MusicResult.Success(true)
    suspend fun downloadTrack(trackId: String): MusicResult<Boolean> = MusicResult.Success(true)
    suspend fun addToPlaylist(trackId: String, playlistId: String): MusicResult<Boolean> = MusicResult.Success(true)
}
