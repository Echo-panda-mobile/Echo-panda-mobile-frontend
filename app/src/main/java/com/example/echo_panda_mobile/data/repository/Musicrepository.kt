package com.example.echo_panda_mobile.data.repository

import androidx.compose.ui.graphics.Color
import com.example.echo_panda_mobile.data.model.*
import com.example.echo_panda_mobile.data.remote.*
import kotlinx.coroutines.delay
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import kotlinx.coroutines.*

// ─── Result wrapper (reuse pattern from AuthRepository) ───────────────────────
sealed class MusicResult<out T> {
    data class Success<T>(val data: T) : MusicResult<T>()
    data class Error(val message: String) : MusicResult<Nothing>()
    object Loading : MusicResult<Nothing>()
}

class MusicRepository(private val apiService: MusicApiService? = null) {

    private val IMAGE_BASE_URL = "https://api.echopanda.me/storage/"
    private val defaultColors = listOf(Color(0xFF2C2C3A), Color(0xFF1A1A26))

    private fun ensureFullUrl(url: String?): String? {
        if (url.isNullOrBlank()) return null
        
        // If it's already an absolute URL (like an S3 signed URL), use it as is
        if (url.startsWith("http")) return url
        
        // If it's a relative path, it's a legacy/fallback path that we know will fail 
        // with 403 unless we use the signed-url endpoint. 
        // However, we'll keep the construction logic just in case the backend 
        // makes the storage public in the future.
        val cleanPath = if (url.startsWith("/")) url.substring(1) else url
        return "$IMAGE_BASE_URL$cleanPath"
    }

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
                val albumDtos = response.body()?.data ?: emptyList()
                
                // Fetch signed URLs for album covers because direct storage access returns 403
                val playlists = albumDtos.map { dto ->
                    async {
                        var imageUrl = ensureFullUrl(dto.coverUrl)
                        try {
                            val imgResponse = apiService.getAlbumCoverUrl(dto.id)
                            if (imgResponse.isSuccessful) {
                                val signedUrl = imgResponse.body()?.signedUrl ?: imgResponse.body()?.url
                                if (!signedUrl.isNullOrBlank()) imageUrl = signedUrl
                            }
                        } catch (e: Exception) {
                            android.util.Log.e("MusicRepository", "Failed to fetch signed URL for album ${dto.id}", e)
                        }

                        Playlist(
                            id = dto.id,
                            title = dto.title,
                            imageUrl = imageUrl,
                            placeholderColors = getColorsForId(dto.id)
                        )
                    }
                }.awaitAll()
                
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
                val artistDtos = response.body()?.data ?: emptyList()
                
                // Fetch signed URLs for artist images
                val artists = artistDtos.map { dto ->
                    async {
                        var artist = dto.toDomain()
                        try {
                            val imgResponse = apiService.getArtistImageUrl(dto.id)
                            if (imgResponse.isSuccessful) {
                                val signedUrl = imgResponse.body()?.signedUrl ?: imgResponse.body()?.url
                                if (!signedUrl.isNullOrBlank()) {
                                    artist = artist.copy(imageUrl = signedUrl)
                                }
                            }
                        } catch (e: Exception) {
                            android.util.Log.e("MusicRepository", "Failed to fetch signed URL for artist ${dto.id}", e)
                        }
                        artist
                    }
                }.awaitAll()
                
                MusicResult.Success(artists)
            } else {
                MusicResult.Error("Failed to fetch artists: ${response.code()}")
            }
        } catch (e: Exception) {
            MusicResult.Error(e.localizedMessage ?: "Network error")
        }
    }

    suspend fun getArtistById(artistId: String): MusicResult<Artist> = withContext(Dispatchers.IO) {
        android.util.Log.d("MusicRepository", "getArtistById entry for ID: $artistId")
        if (apiService == null) return@withContext MusicResult.Error("API service not initialized")
        try {
            var artist: Artist? = null
            
            // 1. Try detail endpoint
            val response = apiService.getArtistDetail(artistId)
            android.util.Log.d("MusicRepository", "getArtistDetail response code: ${response.code()}")
            
            if (response.isSuccessful) {
                artist = response.body()?.toDomain()
            } else if (response.code() == 404) {
                android.util.Log.w("MusicRepository", "Artist detail 404, attempting fallback to list search")
                // Fallback: search in popular artists list if detail endpoint is missing
                val allArtistsResult = getPopularArtists()
                if (allArtistsResult is MusicResult.Success) {
                    artist = allArtistsResult.data.find { it.id == artistId }
                    android.util.Log.d("MusicRepository", "Fallback search result: ${if (artist != null) "Found" else "Not Found"}")
                }
            }

            if (artist != null) {
                // 2. Fetch SIGNED image URL (Relative paths often 403)
                try {
                    android.util.Log.d("MusicRepository", "Fetching signed image URL for artist: $artistId")
                    val imageResponse = apiService.getArtistImageUrl(artistId)
                    if (imageResponse.isSuccessful) {
                        val body = imageResponse.body()
                        val signedUrl = body?.signedUrl ?: body?.url
                        if (!signedUrl.isNullOrBlank()) {
                            android.util.Log.d("MusicRepository", "Found artist signed URL: $signedUrl")
                            artist = artist.copy(imageUrl = signedUrl)
                        }
                    }
                } catch (e: Exception) {
                    android.util.Log.e("MusicRepository", "Error fetching artist signed image", e)
                }
                
                MusicResult.Success(artist)
            } else {
                MusicResult.Error("Artist not found (404)")
            }
        } catch (e: Exception) {
            android.util.Log.e("MusicRepository", "Network error in getArtistById", e)
            MusicResult.Error(e.localizedMessage ?: "Network error")
        }
    }

    suspend fun getArtistAlbums(artistName: String): MusicResult<List<Album>> = withContext(Dispatchers.IO) {
        if (apiService == null) return@withContext MusicResult.Error("API service not initialized")
        try {
            val response = apiService.getAlbums(search = artistName)
            if (response.isSuccessful) {
                val albumDtos = response.body()?.data ?: emptyList()
                val albums = albumDtos.map { dto ->
                    async {
                        var album = dto.toDomain()
                        try {
                            val imgResponse = apiService.getAlbumCoverUrl(dto.id)
                            if (imgResponse.isSuccessful) {
                                val signedUrl = imgResponse.body()?.signedUrl ?: imgResponse.body()?.url
                                if (!signedUrl.isNullOrBlank()) album = album.copy(imageUrl = signedUrl)
                            }
                        } catch (e: Exception) {}
                        album
                    }
                }.awaitAll()
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
                val albumDtos = response.body()?.data?.map { it.album } ?: emptyList()
                val albums = albumDtos.map { dto ->
                    async {
                        var album = dto.toDomain()
                        try {
                            val imgResponse = apiService.getAlbumCoverUrl(dto.id)
                            if (imgResponse.isSuccessful) {
                                val signedUrl = imgResponse.body()?.signedUrl ?: imgResponse.body()?.url
                                if (!signedUrl.isNullOrBlank()) album = album.copy(imageUrl = signedUrl)
                            }
                        } catch (e: Exception) {}
                        album
                    }
                }.awaitAll()
                MusicResult.Success(albums)
            } else {
                // Fallback to regular albums if stats fail
                val fallback = apiService.getAlbums(sortBy = "latest")
                if (fallback.isSuccessful) {
                    val albumDtos = fallback.body()?.data ?: emptyList()
                    val albums = albumDtos.map { dto ->
                        async {
                            var album = dto.toDomain()
                            try {
                                val imgResponse = apiService.getAlbumCoverUrl(dto.id)
                                if (imgResponse.isSuccessful) {
                                    val signedUrl = imgResponse.body()?.signedUrl ?: imgResponse.body()?.url
                                    if (!signedUrl.isNullOrBlank()) album = album.copy(imageUrl = signedUrl)
                                }
                            } catch (e: Exception) {}
                            album
                        }
                    }.awaitAll()
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
                val albumDtos = response.body()?.data ?: emptyList()
                val albums = albumDtos.map { dto ->
                    async {
                        var album = dto.toDomain()
                        try {
                            val imgResponse = apiService.getAlbumCoverUrl(dto.id)
                            if (imgResponse.isSuccessful) {
                                val signedUrl = imgResponse.body()?.signedUrl ?: imgResponse.body()?.url
                                if (!signedUrl.isNullOrBlank()) album = album.copy(imageUrl = signedUrl)
                            }
                        } catch (e: Exception) {}
                        album
                    }
                }.awaitAll()
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
                val albumDtos = response.body()?.data ?: emptyList()
                val albums = albumDtos.map { dto ->
                    async {
                        var album = dto.toDomain()
                        try {
                            val imgResponse = apiService.getAlbumCoverUrl(dto.id)
                            if (imgResponse.isSuccessful) {
                                val signedUrl = imgResponse.body()?.signedUrl ?: imgResponse.body()?.url
                                if (!signedUrl.isNullOrBlank()) album = album.copy(imageUrl = signedUrl)
                            }
                        } catch (e: Exception) {}
                        album
                    }
                }.awaitAll()
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
        android.util.Log.d("MusicRepository", "getTrackById entry for ID: $id")
        if (apiService == null) return@withContext MusicResult.Error("API service not initialized")
        try {
            val response = apiService.getSongDetail(id)
            android.util.Log.d("MusicRepository", "getSongDetail response code: ${response.code()}")
            if (response.isSuccessful) {
                val songDto = response.body() ?: return@withContext MusicResult.Error("Empty response body")
                
                // CRITICAL: We start with a version of the track where the image is NULL 
                // to FORCE the fallback logic below to fetch a Signed URL.
                // The coverUrl in the main DTO is a relative path that causes a 403.
                var track = songDto.toDomain("Unknown").copy(imageUrl = null)
                
                // Fetch extra details for the detail screen
                try {
                    val songIntId = id.toIntOrNull()
                    if (songIntId != null) {
                        // 1. Favorite status check
                        val favResponse = apiService.checkIsFavorite(CheckFavoriteRequest(songIntId))
                        if (favResponse.isSuccessful) {
                            track = track.copy(isFavorite = favResponse.body()?.isFavorite ?: false)
                        }
                    }
                    
                    // 2. Fetch SIGNED cover URL (Relative paths in metadata are forbidden)
                    android.util.Log.d("MusicRepository", "Attempting signed song cover fetch for ID: $id")
                    val songCoverResponse = apiService.getSongCoverUrl(id)
                    if (songCoverResponse.isSuccessful) {
                        val body = songCoverResponse.body()
                        val signedUrl = body?.signedUrl ?: body?.url
                        if (!signedUrl.isNullOrBlank() && signedUrl.startsWith("http")) {
                            android.util.Log.d("MusicRepository", "Found signed song cover: $signedUrl")
                            track = track.copy(imageUrl = signedUrl)
                        }
                    }
                    
                    // 3. Fallback to SIGNED album cover URL
                    if (track.imageUrl.isNullOrBlank() && songDto.albumId != null) {
                        val albumId = songDto.albumId.toString()
                        android.util.Log.d("MusicRepository", "Song signed cover null, attempting album signed cover fetch for ID: $albumId")
                        val albumCoverResponse = apiService.getAlbumCoverUrl(albumId)
                        if (albumCoverResponse.isSuccessful) {
                            val body = albumCoverResponse.body()
                            val signedUrl = body?.signedUrl ?: body?.url
                            if (!signedUrl.isNullOrBlank() && signedUrl.startsWith("http")) {
                                android.util.Log.d("MusicRepository", "Found signed album cover: $signedUrl")
                                track = track.copy(imageUrl = signedUrl)
                            }
                        }
                    }
                    
                    // 4. Last resort: If no signed URL found, use the relative path (will likely 403)
                    if (track.imageUrl.isNullOrBlank()) {
                        val fallbackUrl = ensureFullUrl(songDto.coverUrl ?: songDto.album?.coverUrl)
                        android.util.Log.w("MusicRepository", "No signed URL found. Falling back to relative: $fallbackUrl")
                        track = track.copy(imageUrl = fallbackUrl)
                    }
                } catch (e: Exception) {
                    android.util.Log.e("MusicRepository", "Error fetching extra track details", e)
                }

                MusicResult.Success(track)
            } else {
                MusicResult.Error("Failed to fetch track: ${response.code()}")
            }
        } catch (e: Exception) {
            MusicResult.Error(e.localizedMessage ?: "Network error")
        }
    }

    suspend fun getStreamTicket(songId: String): MusicResult<StreamTicketResponse> = withContext(Dispatchers.IO) {
        if (apiService == null) return@withContext MusicResult.Error("API service not initialized")
        try {
            val response = apiService.getStreamTicket(songId)
            if (response.isSuccessful) {
                val body = response.body() ?: return@withContext MusicResult.Error("Empty body")
                MusicResult.Success(body)
            } else if (response.code() == 404) {
                MusicResult.Error("Song unavailable")
            } else {
                MusicResult.Error("Failed to get stream ticket: ${response.code()}")
            }
        } catch (e: Exception) {
            MusicResult.Error(e.localizedMessage ?: "Network error")
        }
    }

    suspend fun addToListenHistory(songId: String, duration: Int? = null, completed: Boolean? = null): MusicResult<Boolean> = withContext(Dispatchers.IO) {
        if (apiService == null) return@withContext MusicResult.Error("API service not initialized")
        try {
            val songIntId = songId.toIntOrNull() ?: return@withContext MusicResult.Error("Invalid song ID")
            val response = apiService.addToListenHistory(ListenHistoryRequest(songIntId, duration, completed))
            if (response.isSuccessful) {
                MusicResult.Success(true)
            } else {
                MusicResult.Error("Failed to add to history: ${response.code()}")
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
        imageUrl = ensureFullUrl(imageUrl),
        monthlyListeners = monthlyListeners ?: "0M",
        placeholderColors = getColorsForId(id)
    )

    private fun AlbumDto.toDomain() = Album(
        id = id,
        title = title,
        artist = artist,
        imageUrl = ensureFullUrl(coverUrl),
        placeholderColors = getColorsForId(id)
    )

    private fun SongDto.toDomain(defaultArtist: String) = Track(
        id = id.toString(),
        title = title,
        artist = artistName ?: defaultArtist,
        durationMs = durationSeconds * 1000L,
        imageUrl = ensureFullUrl(coverUrl ?: album?.coverUrl),
        album = album?.title,
        placeholderColors = getColorsForId(id.toString())
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

    suspend fun toggleFavorite(trackId: String): MusicResult<Boolean> = withContext(Dispatchers.IO) {
        if (apiService == null) return@withContext MusicResult.Error("API service not initialized")
        try {
            val songIntId = trackId.toIntOrNull() ?: return@withContext MusicResult.Error("Invalid song ID")
            val response = apiService.toggleFavorite(CheckFavoriteRequest(songIntId))
            if (response.isSuccessful) {
                MusicResult.Success(true)
            } else {
                MusicResult.Error("Failed to toggle favorite: ${response.code()}")
            }
        } catch (e: Exception) {
            MusicResult.Error(e.localizedMessage ?: "Network error")
        }
    }
    suspend fun downloadTrack(trackId: String): MusicResult<Boolean> = MusicResult.Success(true)
    suspend fun addToPlaylist(trackId: String, playlistId: String): MusicResult<Boolean> = MusicResult.Success(true)
}
