package com.example.echo_panda_mobile.data.repository

import androidx.compose.ui.graphics.Color
import com.example.echo_panda_mobile.data.model.*
import com.example.echo_panda_mobile.data.remote.*
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import kotlinx.coroutines.*

sealed class MusicResult<out T> {
    data class Success<T>(val data: T) : MusicResult<T>()
    data class Error(val message: String) : MusicResult<Nothing>()
    object Loading : MusicResult<Nothing>()
}

class MusicRepository(
    private val apiService: MusicApiService? = null,
    private val tokenStorage: TokenStorage? = null
) {

    private val defaultColors = listOf(Color(0xFF2C2C3A), Color(0xFF1A1A26))

    private fun directImageUrl(url: String?): String? {
        val raw = url?.takeIf { it.isNotBlank() } ?: return null
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

    private suspend fun fetchSignedArtistImageUrl(artistId: String): String? {
        if (apiService == null) return null
        return try {
            val response = apiService.getArtistImageUrl(artistId)
            if (response.isSuccessful) {
                val signed = response.body()?.signedUrl ?: response.body()?.url
                if (!signed.isNullOrBlank() && signed.startsWith("http")) signed else null
            } else null
        } catch (e: Exception) {
            null
        }
    }

    private suspend fun fetchSignedAlbumCoverUrl(albumId: String): String? {
        if (apiService == null) return null
        return try {
            val response = apiService.getAlbumCoverUrl(albumId)
            if (response.isSuccessful) {
                val signed = response.body()?.signedUrl ?: response.body()?.url
                if (!signed.isNullOrBlank() && signed.startsWith("http")) signed else null
            } else null
        } catch (e: Exception) {
            null
        }
    }

    private suspend fun resolveAlbumCoverUrl(albumId: String, coverUrl: String?): String? {
        fetchSignedAlbumCoverUrl(albumId)?.let { return it }
        return directImageUrl(coverUrl)
    }

    private suspend fun enrichAlbum(album: Album): Album =
        album.copy(imageUrl = resolveAlbumCoverUrl(album.id, album.imageUrl))

    private suspend fun enrichAlbumDto(dto: AlbumDto): Album =
        enrichAlbum(dto.toDomain())

    private suspend fun fetchSignedSongCoverUrl(songId: String): String? {
        if (apiService == null) return null
        return try {
            val response = apiService.getSongCoverUrl(songId)
            if (response.isSuccessful) {
                val signed = response.body()?.signedUrl ?: response.body()?.url
                if (!signed.isNullOrBlank() && signed.startsWith("http")) signed else null
            } else null
        } catch (e: Exception) {
            null
        }
    }

    private suspend fun resolveTrackCoverUrl(
        songId: String,
        albumId: String?,
        coverUrl: String?,
        albumCoverUrl: String?
    ): String? {
        // If we already have a full URL, use it immediately to avoid extra network calls
        if (!coverUrl.isNullOrBlank() && coverUrl.startsWith("http")) return coverUrl
        if (!albumCoverUrl.isNullOrBlank() && albumCoverUrl.startsWith("http")) return albumCoverUrl

        // Try to fetch signed URL only if needed (this is slow if done in a loop)
        // fetchSignedSongCoverUrl(songId)?.let { return it }
        // albumId?.let { fetchSignedAlbumCoverUrl(it)?.let { signed -> return signed } }
        
        return directImageUrl(coverUrl ?: albumCoverUrl)
    }

    private suspend fun enrichArtist(artist: Artist): Artist {
        val signed = fetchSignedArtistImageUrl(artist.id)
        return if (signed != null) artist.copy(imageUrl = signed) else {
            artist.copy(imageUrl = directImageUrl(artist.imageUrl))
        }
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

    private fun getColorsForId(id: String): List<Color> {
        val hash = id.hashCode()
        return listOf(
            Color((hash or 0xFF000000.toInt())),
            Color(((hash shr 8) or 0xFF000000.toInt()))
        )
    }

    suspend fun getRecentPlaylists(): MusicResult<List<Playlist>> = withContext(Dispatchers.IO) {
        val api = apiService ?: return@withContext MusicResult.Error("API service not initialized")
        try {
            val response = api.getContinueListening()
            if (response.isSuccessful) {
                val historyDtos = response.body()?.data ?: emptyList()
                if (historyDtos.isEmpty()) return@withContext getNewAlbumsAsPlaylists()

                val playlists = historyDtos.map { history ->
                    async {
                        val dto = history.song
                        val imageUrl = resolveTrackCoverUrl(
                            songId = dto.resolvedId(),
                            albumId = dto.albumId?.toString() ?: dto.album?.id?.toString(),
                            coverUrl = dto.coverUrl,
                            albumCoverUrl = dto.album?.coverUrl
                        )

                        Playlist(
                            id = dto.resolvedId(),
                            title = dto.resolvedTitle(),
                            imageUrl = imageUrl,
                            labelOverlay = dto.artistName ?: dto.artist?.name,
                            placeholderColors = getColorsForId(dto.resolvedId()),
                            resumePositionMs = history.progressSeconds * 1000L
                        )
                    }
                }.awaitAll()
                MusicResult.Success(playlists)
            } else {
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
                        val albumId = dto.id.toString()
                        val imageUrl = resolveAlbumCoverUrl(albumId, dto.coverUrl)

                        Playlist(
                            id = albumId,
                            title = dto.title,
                            imageUrl = imageUrl,
                            placeholderColors = getColorsForId(albumId)
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

    suspend fun getPopularArtists(): MusicResult<List<Artist>> = withContext(Dispatchers.IO) {
        val api = apiService ?: return@withContext MusicResult.Error("API service not initialized")
        try {
            val response = api.getArtists()
            if (response.isSuccessful) {
                val artistDtos = response.body()?.data ?: emptyList()
                val artists = artistDtos.map { dto ->
                    async { enrichArtist(dto.toDomain()) }
                }.awaitAll()
                MusicResult.Success(artists)
            } else {
                MusicResult.Error("Failed to fetch artists")
            }
        } catch (e: Exception) {
            MusicResult.Error(e.localizedMessage ?: "Network error")
        }
    }

    suspend fun getArtistById(artistId: String): MusicResult<Artist> = withContext(Dispatchers.IO) {
        if (apiService == null) return@withContext MusicResult.Error("API service not initialized")
        try {
            var artist: Artist? = null
            val response = apiService.getArtistDetail(artistId)
            if (response.isSuccessful) {
                artist = response.body()!!.toDomain()
            } else if (response.code() == 404) {
                val allArtistsResult = getPopularArtists()
                if (allArtistsResult is MusicResult.Success) {
                    artist = allArtistsResult.data.find { it.id == artistId }
                }
            }

            if (artist != null) {
                MusicResult.Success(enrichArtist(artist))
            } else {
                MusicResult.Error("Artist not found")
            }
        } catch (e: Exception) {
            MusicResult.Error(e.localizedMessage ?: "Network error")
        }
    }

    suspend fun getArtistAlbums(artistName: String): MusicResult<List<Album>> = withContext(Dispatchers.IO) {
        val api = apiService ?: return@withContext MusicResult.Error("API service not initialized")
        try {
            val response = api.getAlbums(search = artistName)
            if (response.isSuccessful) {
                val albumDtos = response.body()?.data ?: emptyList()
                val albums = albumDtos.map { dto ->
                    async { enrichAlbumDto(dto) }
                }.awaitAll()
                MusicResult.Success(albums)
            } else {
                MusicResult.Error("Failed to fetch artist albums")
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
                        val track = dto.toDomain(dto.artistName ?: dto.artist?.name ?: "Unknown")
                        val imageUrl = resolveTrackCoverUrl(
                            songId = dto.resolvedId(),
                            albumId = dto.albumId?.toString() ?: dto.album?.id?.toString(),
                            coverUrl = dto.coverUrl,
                            albumCoverUrl = dto.album?.coverUrl
                        )
                        track.copy(imageUrl = imageUrl)
                    }
                }.awaitAll()
                MusicResult.Success(mergeFavoriteStatus(tracks))
            } else {
                MusicResult.Error("Failed to fetch playlist songs")
            }
        } catch (e: Exception) {
            MusicResult.Error(e.localizedMessage ?: "Network error")
        }
    }

    suspend fun getArtistSongs(artistName: String): MusicResult<List<Track>> = withContext(Dispatchers.IO) {
        val api = apiService ?: return@withContext MusicResult.Error("API service not initialized")
        try {
            val response = api.getSongs(search = artistName)
            if (response.isSuccessful) {
                val tracks = response.body()?.data?.map { it.toDomain(artistName) } ?: emptyList()
                MusicResult.Success(mergeFavoriteStatus(enrichTrackCovers(tracks)))
            } else {
                MusicResult.Error("Failed to fetch artist songs")
            }
        } catch (e: Exception) {
            MusicResult.Error(e.localizedMessage ?: "Network error")
        }
    }

    suspend fun getTopAlbums(): MusicResult<List<Album>> = withContext(Dispatchers.IO) {
        val api = apiService ?: return@withContext MusicResult.Error("API service not initialized")
        try {
            android.util.Log.d("ALBUM_API", "━━━ FETCH TOP ALBUMS START ━━━")
            val response = api.getMostPlayedAlbums(limit = 10)
            
            val request = response.raw().request
            android.util.Log.d("ALBUM_API", "Request URL: ${request.url}")
            android.util.Log.d("ALBUM_API", "Response Code: ${response.code()}")

            if (response.isSuccessful) {
                val mostPlayedDtos = response.body()?.data ?: emptyList()
                val albums = mostPlayedDtos.map { dto ->
                    async { enrichAlbumDto(dto.album) }
                }.awaitAll()
                MusicResult.Success(albums)
            } else {
                android.util.Log.w("ALBUM_API", "⚠️ Top albums failed (Code ${response.code()}), falling back to new albums.")
                getNewAlbums()
            }
        } catch (e: Exception) {
            android.util.Log.e("ALBUM_API", "❌ Top albums exception: ${e.message}")
            getNewAlbums()
        }
    }

    suspend fun getNewAlbums(): MusicResult<List<Album>> = withContext(Dispatchers.IO) {
        val api = apiService ?: return@withContext MusicResult.Error("API service not initialized")
        try {
            android.util.Log.d("ALBUM_API", "━━━ FETCH NEW ALBUMS START ━━━")
            val response = api.getAlbums(sortBy = "latest", perPage = 10)
            
            val request = response.raw().request
            val token = tokenStorage?.getToken()
            android.util.Log.d("ALBUM_API", "URL: ${request.url}")
            android.util.Log.d("ALBUM_API", "Status: ${response.code()}")
            android.util.Log.d("ALBUM_API", "Token from Storage: ${if (token.isNullOrBlank()) "MISSING" else "PRESENT"}")
            android.util.Log.d("ALBUM_API", "Auth Header: ${request.header("Authorization")}")

            if (response.isSuccessful) {
                val albumDtos = response.body()?.data ?: emptyList()
                val albums = albumDtos.map { dto ->
                    async { enrichAlbumDto(dto) }
                }.awaitAll()
                MusicResult.Success(albums)
            } else {
                val errorMsg = response.errorBody()?.string() ?: response.message()
                android.util.Log.e("ALBUM_API", "❌ Error: $errorMsg")
                MusicResult.Error("Failed to fetch new albums: $errorMsg")
            }
        } catch (e: Exception) {
            android.util.Log.e("ALBUM_API", "❌ Exception: ${e.message}", e)
            MusicResult.Error(e.localizedMessage ?: "Network error")
        }
    }

    suspend fun getAllAlbums(perPage: Int = 50): MusicResult<List<Album>> = withContext(Dispatchers.IO) {
        if (apiService == null) return@withContext MusicResult.Error("API service not initialized")
        try {
            android.util.Log.d("ALBUM_API", "━━━ FETCH ALL ALBUMS START ━━━")
            val response = apiService.getAlbums(sortBy = "latest", perPage = perPage)
            
            // Log Full URL
            val fullUrl = response.raw().request.url.toString()
            android.util.Log.d("ALBUM_API", "FULL URL REQUESTED: $fullUrl")
            android.util.Log.d("ALBUM_API", "Status Code: ${response.code()}")
            
            if (response.isSuccessful) {
                val body = response.body()
                android.util.Log.d("ALBUM_API", "✓ Success Body: $body")
                val albumDtos = body?.data ?: emptyList()
                val albums = albumDtos.map { dto ->
                    async { enrichAlbumDto(dto) }
                }.awaitAll()
                MusicResult.Success(albums)
            } else {
                val errorBody = response.errorBody()?.string()
                android.util.Log.e("ALBUM_API", "❌ Error Body: $errorBody")
                MusicResult.Error("Failed to fetch albums: ${response.code()} ${response.message()}")
            }
        } catch (e: Exception) {
            android.util.Log.e("ALBUM_API", "❌ Exception: ${e.message}", e)
            MusicResult.Error(e.localizedMessage ?: "Network error")
        }
    }

    suspend fun getPopularAlbums(): MusicResult<List<Album>> = withContext(Dispatchers.IO) {
        val api = apiService ?: return@withContext MusicResult.Error("API service not initialized")
        try {
            val response = api.getAlbums(perPage = 10)
            if (response.isSuccessful) {
                val albumDtos = response.body()?.data ?: emptyList()
                val albums = albumDtos.map { dto ->
                    async { enrichAlbumDto(dto) }
                }.awaitAll()
                MusicResult.Success(albums)
            } else {
                MusicResult.Error("Failed to fetch popular albums")
            }
        } catch (e: Exception) {
            MusicResult.Error(e.localizedMessage ?: "Network error")
        }
    }

    suspend fun getAlbumById(id: String): MusicResult<Album> = withContext(Dispatchers.IO) {
        val api = apiService ?: return@withContext MusicResult.Error("API service not initialized")
        try {
            val albumResponse = api.getAlbumDetail(id)
            if (!albumResponse.isSuccessful) return@withContext MusicResult.Error("Album not found")
            val albumDto = albumResponse.body() ?: return@withContext MusicResult.Error("Empty response body")

            val songsResponse = api.getSongs(albumId = albumDto.id)
            val tracks = if (songsResponse.isSuccessful) {
                songsResponse.body()?.data?.map {
                    it.toDomain(albumDto.artist?.name ?: albumDto.artistName ?: "Unknown")
                } ?: emptyList()
            } else emptyList()
            val tracksWithFavorites = mergeFavoriteStatus(tracks)

            val album = enrichAlbum(
                albumDto.toDomain().copy(
                    tracks = tracksWithFavorites,
                    totalDuration = "${tracksWithFavorites.sumOf { it.durationMs } / 60000}m"
                )
            )
            MusicResult.Success(album)
        } catch (e: Exception) {
            MusicResult.Error(e.localizedMessage ?: "Network error")
        }
    }

    suspend fun getAllSongs(search: String? = null): MusicResult<List<Track>> = withContext(Dispatchers.IO) {
        val api = apiService ?: return@withContext MusicResult.Error("API service not initialized")
        try {
            val response = api.getSongs(search = search)
            if (response.isSuccessful) {
                val tracks = response.body()?.data?.map { it.toDomain("Unknown") } ?: emptyList()
                MusicResult.Success(mergeFavoriteStatus(enrichTrackCovers(tracks)))
            } else {
                MusicResult.Error("Failed to fetch songs")
            }
        } catch (e: Exception) {
            MusicResult.Error(e.localizedMessage ?: "Network error")
        }
    }

    suspend fun getTrackById(id: String): MusicResult<Track> = withContext(Dispatchers.IO) {
        if (apiService == null) return@withContext MusicResult.Error("API service not initialized")
        try {
            android.util.Log.d("MusicRepository", "Fetching track details for ID: $id")
            val response = apiService.getSongDetail(id)
            android.util.Log.d("MusicRepository", "Response code: ${response.code()}")
            
            if (response.isSuccessful) {
                val songDto = response.body() ?: return@withContext MusicResult.Error("Empty response body")
                android.util.Log.d("MusicRepository", "Song DTO: $songDto")

                var track = songDto.toDomain("Unknown")
                val coverUrl = resolveTrackCoverUrl(
                    songId = id,
                    albumId = songDto.albumId?.toString() ?: songDto.album?.id?.toString(),
                    coverUrl = songDto.coverUrl,
                    albumCoverUrl = songDto.album?.coverUrl
                )
                if (coverUrl != null) {
                    track = track.copy(imageUrl = coverUrl)
                }

                id.toIntOrNull()?.let { songIntId ->
                    try {
                        val favResponse = apiService.checkIsFavorite(CheckFavoriteRequest(songIntId))
                        if (favResponse.isSuccessful) {
                            track = track.copy(isFavorite = favResponse.body()?.isFavorite ?: false)
                        }
                    } catch (_: Exception) { }
                }

                MusicResult.Success(track)
            } else {
                MusicResult.Error("Failed to fetch track")
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
            } else {
                MusicResult.Error("Failed to get stream ticket: ${response.code()} ${response.message()}")
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
                MusicResult.Error("Failed to add to history")
            }
        } catch (e: Exception) {
            MusicResult.Error(e.localizedMessage ?: "Network error")
        }
    }

    suspend fun getMostPlayedSongs(limit: Int = 10): MusicResult<List<Track>> = withContext(Dispatchers.IO) {
        val api = apiService ?: return@withContext MusicResult.Error("API service not initialized")
        try {
            val response = api.getMostPlayedSongs(limit = limit)
            if (response.isSuccessful) {
                val tracks = response.body()?.data?.map { it.song.toDomain("Unknown") } ?: emptyList()
                MusicResult.Success(mergeFavoriteStatus(enrichTrackCovers(tracks)))
            } else {
                MusicResult.Error("Failed to fetch most played songs")
            }
        } catch (e: Exception) {
            MusicResult.Error(e.localizedMessage ?: "Network error")
        }
    }

    private fun ArtistDto.toDomain() = Artist(
        id = id.toString(),
        name = name,
        imageUrl = directImageUrl(imageUrl ?: coverImageUrl),
        bio = bio,
        monthlyListeners = monthlyListeners ?: "0M",
        placeholderColors = getColorsForId(id.toString())
    )

    private fun AlbumDto.toDomain() = Album(
        id = id.toString(),
        title = title,
        artist = artist?.name ?: artistName ?: "Unknown",
        imageUrl = getDisplayCoverUrl(),
        placeholderColors = getColorsForId(id.toString())
    )

    private fun SongDto.resolvedTitle(): String =
        title?.takeIf { it.isNotBlank() } ?: name?.takeIf { it.isNotBlank() } ?: "Unknown Track"

    private fun SongDto.resolvedId(): String = (id ?: 0).toString()

    private fun SongDto.toDomain(defaultArtist: String): Track {
        val resolvedArtist = artist?.name?.takeIf { it != "Unknown" } ?: artistName ?: defaultArtist
        return Track(
            id = resolvedId(),
            title = resolvedTitle(),
            artist = resolvedArtist,
            durationMs = (durationSeconds ?: 0) * 1000L,
            imageUrl = getDisplayCoverUrl(),
            album = album?.title ?: (if (resolvedArtist != "Unknown") resolvedArtist else null),
            placeholderColors = getColorsForId(resolvedId()),
            isFavorite = isFavorite ?: false
        )
    }

    private fun FavoriteItemDto.toTrack(): Track? {
        song?.let { return it.toDomain("Unknown").copy(isFavorite = true) }
        val trackId = songId ?: id ?: return null
        val idStr = trackId.toString()
        val resolvedTitle = title?.takeIf { it.isNotBlank() } ?: name?.takeIf { it.isNotBlank() } ?: "Unknown Track"
        return Track(
            id = idStr,
            title = resolvedTitle,
            artist = artistName ?: "Unknown",
            durationMs = (durationSeconds ?: 0) * 1000L,
            imageUrl = getDisplayCoverUrl(),
            album = album?.title,
            placeholderColors = getColorsForId(idStr),
            isFavorite = true
        )
    }

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
                        val imageUrl = resolveTrackCoverUrl(
                            songId = song.resolvedId(),
                            albumId = song.albumId?.toString() ?: song.album?.id?.toString(),
                            coverUrl = song.coverUrl,
                            albumCoverUrl = song.album?.coverUrl
                        )

                        Playlist(
                            id = song.resolvedId(),
                            title = song.resolvedTitle(),
                            imageUrl = imageUrl,
                            labelOverlay = song.artistName ?: song.artist?.name,
                            placeholderColors = getColorsForId(song.resolvedId())
                        )
                    }
                }.awaitAll()
                MusicResult.Success(playlists)
            } else {
                getRecentPlaylists()
            }
        } catch (e: Exception) {
            getRecentPlaylists()
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
        val list = listOf(
            Genre("g1", "Rap Tracks", "Rap Songs", listOf(Color(0xFF3A1A2A), Color(0xFF1A0A12))),
            Genre("g2", "Pop Tracks", "Pop Songs", listOf(Color(0xFF1A2A4A), Color(0xFF0A1228)))
        )
        MusicResult.Success(list)
    }

    suspend fun getMoodPlaylists(): MusicResult<List<MoodPlaylist>> = withContext(Dispatchers.IO) {
        val list = listOf(
            MoodPlaylist("mo1", "Sad Songs", "Sad Songs", listOf(Color(0xFF1A2030), Color(0xFF0A1018))),
            MoodPlaylist("mo2", "Workout Songs", "Workout Songs", listOf(Color(0xFF3A2A10), Color(0xFF1A1208)))
        )
        MusicResult.Success(list)
    }

    suspend fun getNewReleases(): MusicResult<List<Track>> = getAllSongs()

    suspend fun getBrowseCategories(): MusicResult<List<BrowseCategory>> = withContext(Dispatchers.IO) {
        val list = listOf(
            BrowseCategory("bc1", "Made for You", Color(0xFF1565C0), listOf(Color(0xFF0D47A1), Color(0xFF1976D2))),
            BrowseCategory("bc2", "RELEASED", Color(0xFF6A1B9A), listOf(Color(0xFF4A148C), Color(0xFF8E24AA)))
        )
        MusicResult.Success(list)
    }

    suspend fun getFavoriteTracks(): MusicResult<List<Track>> = withContext(Dispatchers.IO) {
        if (apiService == null) return@withContext MusicResult.Error("API service not initialized")
        try {
            val response = apiService.getFavorites()
            if (response.isSuccessful) {
                val tracks = response.body()?.data?.mapNotNull { it.toTrack() } ?: emptyList()
                MusicResult.Success(enrichTrackCovers(tracks))
            } else {
                MusicResult.Error("Failed to fetch favorite tracks")
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
                MusicResult.Error("Failed to fetch recently played")
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
                                } catch (_: Exception) { 0 }
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
        // Skip heavy enrichment if we already have URLs or to avoid too many requests
        return tracks
    }

    suspend fun toggleFavorite(trackId: String, isFavorite: Boolean): MusicResult<Boolean> = withContext(Dispatchers.IO) {
        if (apiService == null) return@withContext MusicResult.Error("API service not initialized")
        try {
            val songIntId = trackId.toIntOrNull() ?: return@withContext MusicResult.Error("Invalid song ID")
            val response = if (isFavorite) {
                apiService.removeFavorite(CheckFavoriteRequest(songIntId))
            } else {
                apiService.toggleFavorite(CheckFavoriteRequest(songIntId))
            }
            if (response.isSuccessful) MusicResult.Success(!isFavorite) else MusicResult.Error("Failed")
        } catch (e: Exception) {
            MusicResult.Error(e.localizedMessage ?: "Network error")
        }
    }

    suspend fun downloadTrack(trackId: String): MusicResult<Boolean> = withContext(Dispatchers.IO) {
        MusicResult.Success(trackId.isNotBlank())
    }

    suspend fun addToPlaylist(trackId: String, playlistId: String): MusicResult<Boolean> = withContext(Dispatchers.IO) {
        if (apiService == null) return@withContext MusicResult.Error("API service not initialized")
        try {
            val songInt = trackId.toIntOrNull() ?: return@withContext MusicResult.Error("Invalid track ID")
            val response = apiService.addSongToPlaylist(playlistId, AddSongToPlaylistRequest(songInt))
            if (response.isSuccessful) MusicResult.Success(true) else MusicResult.Error("Failed")
        } catch (e: Exception) {
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
                        imageUrl = directImageUrl(dto.coverUrl),
                        labelOverlay = dto.label,
                        placeholderColors = getColorsForId(dto.id)
                    )
                }
                MusicResult.Success(playlists)
            } else MusicResult.Error("Failed to fetch playlists")
        } catch (e: Exception) {
            MusicResult.Error(e.localizedMessage ?: "Network error")
        }
    }

    suspend fun createPlaylist(title: String): MusicResult<Playlist> = withContext(Dispatchers.IO) {
        if (apiService == null) return@withContext MusicResult.Error("API service not initialized")
        try {
            val response = apiService.createPlaylist(CreatePlaylistRequest(title))
            if (response.isSuccessful) {
                val dto = response.body()?.data ?: return@withContext MusicResult.Error("Empty body")
                val playlist = Playlist(
                    id = dto.id,
                    title = dto.name ?: dto.title ?: "Untitled",
                    imageUrl = directImageUrl(dto.coverUrl),
                    labelOverlay = dto.label,
                    placeholderColors = getColorsForId(dto.id)
                )
                MusicResult.Success(playlist)
            } else MusicResult.Error("Server error")
        } catch (e: Exception) {
            MusicResult.Error(e.localizedMessage ?: "Network error")
        }
    }
}
