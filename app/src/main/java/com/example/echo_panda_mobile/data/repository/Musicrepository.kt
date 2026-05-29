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

    private suspend fun mergeFavoriteStatus(tracks: List<Track>): List<Track> {
        if (tracks.isEmpty() || apiService == null) return tracks
        return try {
            val response = apiService.getFavorites()
            if (!response.isSuccessful) return tracks
            val favoriteIds = response.body()?.data?.mapNotNull { item ->
                item.song?.id ?: item.songId ?: item.id
            }?.map { it.toString() }?.toSet() ?: emptySet()
            tracks.map { it.copy(isFavorite = it.id in favoriteIds) }
        } catch (_: Exception) {
            tracks
        }
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
            android.util.Log.d("MusicRepository", "Fetching continue listening history...")
            val response = apiService.getContinueListening()
            if (response.isSuccessful) {
                val historyDtos = response.body()?.data ?: emptyList()
                android.util.Log.d("MusicRepository", "Found ${historyDtos.size} history items")
                
                if (historyDtos.isEmpty()) {
                    android.util.Log.d("MusicRepository", "History empty, falling back to latest albums")
                    return@withContext getNewAlbumsAsPlaylists()
                }

                val playlists = historyDtos.map { history ->
                    async {
                        val dto = history.song
                        var imageUrl = ensureFullUrl(dto.coverUrl ?: dto.album?.coverUrl)
                        try {
                            // Try to get a signed URL for the song cover
                            val imgResponse = apiService.getSongCoverUrl(dto.resolvedId())
                            if (imgResponse.isSuccessful) {
                                val signedUrl = imgResponse.body()?.signedUrl ?: imgResponse.body()?.url
                                if (!signedUrl.isNullOrBlank()) imageUrl = signedUrl
                            }
                        } catch (e: Exception) {}

                        Playlist(
                            id = dto.resolvedId(),
                            title = dto.resolvedTitle(),
                            imageUrl = imageUrl,
                            labelOverlay = dto.artistName,
                            placeholderColors = getColorsForId(dto.resolvedId()),
                            resumePositionMs = history.progressSeconds * 1000L
                        )
                    }
                }.awaitAll()
                
                MusicResult.Success(playlists)
            } else {
                // Fallback to latest albums if nothing to continue
                getNewAlbumsAsPlaylists()
            }
        } catch (e: Exception) {
            getNewAlbumsAsPlaylists()
        }
    }

    private suspend fun getNewAlbumsAsPlaylists(): MusicResult<List<Playlist>> = withContext(Dispatchers.IO) {
        if (apiService == null) return@withContext MusicResult.Error("API service not initialized")
        try {
            val response = apiService.getAlbums(sortBy = "latest")
            if (response.isSuccessful) {
                val albumDtos = response.body()?.data ?: emptyList()
                val playlists = albumDtos.map { dto ->
                    async {
                        var imageUrl = ensureFullUrl(dto.coverUrl)
                        try {
                            val imgResponse = apiService.getAlbumCoverUrl(dto.id)
                            if (imgResponse.isSuccessful) {
                                val signedUrl = imgResponse.body()?.signedUrl ?: imgResponse.body()?.url
                                if (!signedUrl.isNullOrBlank()) imageUrl = signedUrl
                            }
                        } catch (e: Exception) {}

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
                MusicResult.Error("Failed to fetch fallback playlists")
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
                artist = response.body()!!.toDomain()
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
                
                MusicResult.Success(artist!!)
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

    suspend fun getPlaylistSongs(playlistId: String): MusicResult<List<Track>> = withContext(Dispatchers.IO) {
        if (apiService == null) return@withContext MusicResult.Error("API service not initialized")
        try {
            val response = apiService.getPlaylistSongs(playlistId)
            if (response.isSuccessful) {
                val songDtos = response.body()?.data ?: emptyList()
                val tracks = songDtos.map { dto ->
                    async {
                        val track = dto.toDomain(dto.artistName ?: "Unknown")
                        // attempt to get signed cover
                        try {
                            val imgResponse = apiService.getSongCoverUrl(dto.resolvedId())
                            if (imgResponse.isSuccessful) {
                                val signedUrl = imgResponse.body()?.signedUrl ?: imgResponse.body()?.url
                                if (!signedUrl.isNullOrBlank()) {
                                    return@async track.copy(imageUrl = signedUrl)
                                }
                            }
                        } catch (e: Exception) {}
                        track
                    }
                }.awaitAll()
                MusicResult.Success(mergeFavoriteStatus(tracks))
            } else {
                MusicResult.Error("Failed to fetch playlist songs: ${response.code()}")
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
                MusicResult.Success(mergeFavoriteStatus(tracks))
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
                songsResponse.body()?.data?.map {
                    it.toDomain(albumDto.artist?.name ?: "Unknown")
                } ?: emptyList()
            } else emptyList()
            val tracksWithFavorites = mergeFavoriteStatus(tracks)

            MusicResult.Success(
                albumDto.toDomain().copy(
                    tracks = tracksWithFavorites,
                    totalDuration = "${tracksWithFavorites.sumOf { it.durationMs } / 60000}m"
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
                MusicResult.Success(mergeFavoriteStatus(tracks))
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
                MusicResult.Success(mergeFavoriteStatus(tracks))
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
        artist = artist?.name ?: "Unknown",
        imageUrl = ensureFullUrl(coverUrl),
        placeholderColors = getColorsForId(id)
    )

    private fun SongDto.resolvedTitle(): String =
        title?.takeIf { it.isNotBlank() }
            ?: name?.takeIf { it.isNotBlank() }
            ?: "Unknown Track"

    private fun SongDto.resolvedId(): String = (id ?: 0).toString()

    private fun SongDto.toDomain(defaultArtist: String) = Track(
        id = resolvedId(),
        title = resolvedTitle(),
        artist = artistName?.takeIf { it.isNotBlank() } ?: defaultArtist,
        durationMs = (durationSeconds ?: 0) * 1000L,
        imageUrl = ensureFullUrl(coverUrl ?: album?.coverUrl),
        album = album?.title?.takeIf { it.isNotBlank() },
        placeholderColors = getColorsForId(resolvedId()),
        isFavorite = isFavorite ?: false
    )

    private fun FavoriteItemDto.toTrack(): Track? {
        song?.let { return it.toDomain("Unknown").copy(isFavorite = true) }
        val trackId = songId ?: id ?: return null
        val idStr = trackId.toString()
        val resolvedTitle = title?.takeIf { it.isNotBlank() }
            ?: name?.takeIf { it.isNotBlank() }
            ?: "Unknown Track"
        return Track(
            id = idStr,
            title = resolvedTitle,
            artist = artistName?.takeIf { it.isNotBlank() } ?: "Unknown",
            durationMs = (durationSeconds ?: 0) * 1000L,
            imageUrl = ensureFullUrl(coverUrl ?: album?.coverUrl),
            album = album?.title?.takeIf { it.isNotBlank() },
            placeholderColors = getColorsForId(idStr),
            isFavorite = true
        )
    }

    // ── Placeholders ──────────────────────────────────────────────────────────

    suspend fun getTopMixes(): MusicResult<List<Playlist>> = withContext(Dispatchers.IO) {
        MusicResult.Success(listOf(
            Playlist("m1", "Trending Music", labelOverlay = "TRENDING", placeholderColors = listOf(Color(0xFF1565C0), Color(0xFF0D47A1))),
            Playlist("m2", "Weekly Top Songs", labelOverlay = "WEEKLY TOP", placeholderColors = listOf(Color(0xFFF9A825), Color(0xFFF57F17)))
        ))
    }

    suspend fun getRecentListening(): MusicResult<List<Playlist>> = withContext(Dispatchers.IO) {
        if (apiService == null) return@withContext MusicResult.Error("API service not initialized")
        try {
            val response = apiService.getListenHistory(perPage = 10)
            if (response.isSuccessful) {
                val historyDtos = response.body()?.data ?: emptyList()
                val playlists = historyDtos.map { dto ->
                    async {
                        val song = dto.song
                        var imageUrl = ensureFullUrl(song.coverUrl ?: song.album?.coverUrl)
                        try {
                            val imgResponse = apiService.getSongCoverUrl(song.resolvedId())
                            if (imgResponse.isSuccessful) {
                                val signedUrl = imgResponse.body()?.signedUrl ?: imgResponse.body()?.url
                                if (!signedUrl.isNullOrBlank()) imageUrl = signedUrl
                            }
                        } catch (e: Exception) {}

                        Playlist(
                            id = song.resolvedId(),
                            title = song.resolvedTitle(),
                            imageUrl = imageUrl,
                            labelOverlay = song.artistName,
                            placeholderColors = getColorsForId(song.resolvedId())
                        )
                    }
                }.awaitAll()
                MusicResult.Success(playlists)
            } else {
                getRecentPlaylists() // Fallback
            }
        } catch (e: Exception) {
            getRecentPlaylists() // Fallback
        }
    }

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

    suspend fun getFavoriteTracks(): MusicResult<List<Track>> = withContext(Dispatchers.IO) {
        if (apiService == null) return@withContext MusicResult.Error("API service not initialized")
        try {
            val response = apiService.getFavorites()
            if (response.isSuccessful) {
                val tracks = response.body()?.data?.mapNotNull { it.toTrack() } ?: emptyList()
                MusicResult.Success(enrichTrackCovers(tracks))
            } else {
                MusicResult.Error("Failed to fetch favorite tracks: ${response.code()}")
            }
        } catch (e: Exception) {
            MusicResult.Error(e.localizedMessage ?: "Network error")
        }
    }

    suspend fun getRecentlyPlayedTracks(): MusicResult<List<Track>> = withContext(Dispatchers.IO) {
        if (apiService == null) return@withContext MusicResult.Error("API service not initialized")
        try {
            val response = apiService.getRecentlyPlayed()
            if (response.isSuccessful) {
                val tracks = response.body()?.data?.map { it.toDomain("Unknown") } ?: emptyList()
                MusicResult.Success(enrichTrackCovers(mergeFavoriteStatus(tracks)))
            } else {
                MusicResult.Error("Failed to fetch recently played: ${response.code()}")
            }
        } catch (e: Exception) {
            MusicResult.Error(e.localizedMessage ?: "Network error")
        }
    }

    suspend fun getPlaylistsWithSongCounts(perPage: Int = 50): MusicResult<List<Pair<Playlist, Int>>> =
        withContext(Dispatchers.IO) {
            if (apiService == null) return@withContext MusicResult.Error("API service not initialized")
            val service = apiService
            when (val playlistsResult = getPlaylists(perPage = perPage)) {
                is MusicResult.Success -> {
                    val withCounts = coroutineScope {
                        playlistsResult.data.map { playlist ->
                            async {
                                val count = try {
                                    val songsResponse = service.getPlaylistSongs(playlist.id, perPage = 1)
                                    if (songsResponse.isSuccessful) {
                                        songsResponse.body()?.total ?: songsResponse.body()?.data?.size ?: 0
                                    } else 0
                                } catch (_: Exception) {
                                    0
                                }
                                playlist to count
                            }
                        }.awaitAll()
                    }
                    MusicResult.Success(withCounts)
                }
                is MusicResult.Error -> playlistsResult
                else -> MusicResult.Error("Failed to load playlists")
            }
        }

    suspend fun findPlaylistById(playlistId: String): MusicResult<Playlist?> = withContext(Dispatchers.IO) {
        when (val result = getPlaylists(perPage = 100)) {
            is MusicResult.Success -> MusicResult.Success(result.data.find { it.id == playlistId })
            is MusicResult.Error -> result
            else -> MusicResult.Error("Failed to load playlist")
        }
    }

    private suspend fun enrichTrackCovers(tracks: List<Track>): List<Track> {
        if (tracks.isEmpty() || apiService == null) return tracks
        val service = apiService
        return coroutineScope {
            tracks.map { track ->
                async {
                    var enriched = track
                    try {
                        val imgResponse = service.getSongCoverUrl(track.id)
                        if (imgResponse.isSuccessful) {
                            val signedUrl = imgResponse.body()?.signedUrl ?: imgResponse.body()?.url
                            if (!signedUrl.isNullOrBlank()) {
                                enriched = enriched.copy(imageUrl = signedUrl)
                            }
                        }
                    } catch (_: Exception) {
                    }
                    enriched
                }
            }.awaitAll()
        }
    }

    suspend fun toggleFavorite(trackId: String, isFavorite: Boolean): MusicResult<Boolean> = withContext(Dispatchers.IO) {
        if (apiService == null) return@withContext MusicResult.Error("API service not initialized")
        try {
            val songIntId = trackId.toIntOrNull() 
                ?: return@withContext MusicResult.Error("Invalid song ID: $trackId")
            
            val response = if (isFavorite) {
                apiService.removeFavorite(CheckFavoriteRequest(songIntId))
            } else {
                apiService.toggleFavorite(CheckFavoriteRequest(songIntId))
            }
            
            if (response.isSuccessful) {
                MusicResult.Success(!isFavorite)
            } else {
                MusicResult.Error("Failed: ${response.code()}")
            }
        } catch (e: Exception) {
            MusicResult.Error(e.localizedMessage ?: "Network error")
        }
    }
    suspend fun downloadTrack(trackId: String): MusicResult<Boolean> = withContext(Dispatchers.IO) {
        // stub: pretend success if id looks non-empty
        MusicResult.Success(trackId.isNotBlank())
    }

    suspend fun addToPlaylist(trackId: String, playlistId: String): MusicResult<Boolean> = withContext(Dispatchers.IO) {
        if (apiService == null) return@withContext MusicResult.Error("API service not initialized")
        try {
            val songInt = trackId.toIntOrNull() ?: return@withContext MusicResult.Error("Invalid track ID: $trackId")
            android.util.Log.d("MusicRepository", "Adding song $songInt to playlist $playlistId")
            val response = apiService.addSongToPlaylist(playlistId, AddSongToPlaylistRequest(songInt))
            if (response.isSuccessful) {
                android.util.Log.d("MusicRepository", "Add to playlist success")
                MusicResult.Success(true)
            } else {
                val errorBody = response.errorBody()?.string()
                android.util.Log.e("MusicRepository", "Failed to add to playlist: ${response.code()} - $errorBody")
                MusicResult.Error("Failed to add to playlist: ${response.code()}")
            }
        } catch (e: Exception) {
            android.util.Log.e("MusicRepository", "Network error in addToPlaylist", e)
            MusicResult.Error(e.localizedMessage ?: "Network error")
        }
    }

    suspend fun getPlaylists(perPage: Int? = 20): MusicResult<List<Playlist>> = withContext(Dispatchers.IO) {
        if (apiService == null) return@withContext MusicResult.Error("API service not initialized")
        try {
            val response = apiService.getPlaylists(perPage = perPage)
            if (response.isSuccessful) {
                val dtos = response.body()?.data ?: emptyList()
                val playlists = dtos.map { dto ->
                    Playlist(
                        id = dto.id,
                        title = dto.name ?: dto.title ?: "Untitled",
                        imageUrl = ensureFullUrl(dto.coverUrl),
                        labelOverlay = dto.label,
                        placeholderColors = getColorsForId(dto.id)
                    )
                }
                MusicResult.Success(playlists)
            } else {
                MusicResult.Error("Failed to fetch playlists: ${response.code()}")
            }
        } catch (e: Exception) {
            MusicResult.Error(e.localizedMessage ?: "Network error")
        }
    }

    suspend fun createPlaylist(title: String): MusicResult<Playlist> = withContext(Dispatchers.IO) {
        if (apiService == null) return@withContext MusicResult.Error("API service not initialized")
        try {
            android.util.Log.d("MusicRepository", "Creating playlist: $title")
            val response = apiService.createPlaylist(CreatePlaylistRequest(title))
            if (response.isSuccessful) {
                val dto = response.body()?.data ?: return@withContext MusicResult.Error("Empty response body")
                android.util.Log.d("MusicRepository", "Playlist created: ${dto.id}")
                val playlist = Playlist(
                    id = dto.id,
                    title = dto.name ?: dto.title ?: "Untitled",
                    imageUrl = ensureFullUrl(dto.coverUrl),
                    labelOverlay = dto.label,
                    placeholderColors = getColorsForId(dto.id)
                )
                MusicResult.Success(playlist)
            } else {
                val errorMsg = response.errorBody()?.string() ?: "Unknown error"
                android.util.Log.e("MusicRepository", "Failed to create playlist: ${response.code()} - $errorMsg")
                MusicResult.Error("Server error ${response.code()}")
            }
        } catch (e: Exception) {
            android.util.Log.e("MusicRepository", "Network error in createPlaylist", e)
            MusicResult.Error(e.localizedMessage ?: "Network error")
        }
    }
}
