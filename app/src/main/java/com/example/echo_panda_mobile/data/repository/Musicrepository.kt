package com.example.echo_panda_mobile.data.repository

import android.media.MediaMetadataRetriever
import androidx.compose.ui.graphics.Color
import com.example.echo_panda_mobile.data.model.*
import com.example.echo_panda_mobile.data.model.formatTrackDuration
import com.example.echo_panda_mobile.data.model.resolveDurationMs
import com.example.echo_panda_mobile.data.remote.*
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.sync.Semaphore
import kotlinx.coroutines.sync.withPermit
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

    /** DB often has 180s placeholder; player uses real ExoPlayer duration from the audio file. */
    private companion object {
        const val PLACEHOLDER_DURATION_MS = 180_000L
        const val MEDIA_DURATION_ENRICH_CONCURRENCY = 4
    }

    /** Only use URLs that are already absolute (e.g. fresh S3 presigned links from the API). */
    private fun directImageUrl(url: String?): String? =
        url?.takeIf { it.isNotBlank() && it.startsWith("http") }

    private suspend fun fetchSignedArtistImageUrl(artistId: String): String? {
        if (apiService == null) return null
        return try {
            val response = apiService.getArtistImageUrl(artistId)
            if (response.isSuccessful) {
                val raw = response.body()?.signedUrl ?: response.body()?.url
                directImageUrl(raw)
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
                val raw = response.body()?.signedUrl ?: response.body()?.url
                directImageUrl(raw)
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
                val raw = response.body()?.signedUrl ?: response.body()?.url
                directImageUrl(raw)
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
        fetchSignedSongCoverUrl(songId)?.let { return it }
        if (!coverUrl.isNullOrBlank() && coverUrl.startsWith("http")) return coverUrl
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
            val response = apiService.getMbFavorites()
            if (!response.isSuccessful) return tracks
            val favoriteIds = response.body()?.data?.mapNotNull { item ->
                item.song?.id
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
                val historyDtos = response.body()?.data.orEmpty()
                android.util.Log.d("MusicRepository", "Found ${historyDtos.size} continue items")
                if (historyDtos.isNotEmpty()) {
                    return@withContext MusicResult.Success(playHistoryToPlaylists(historyDtos))
                }
            } else {
                android.util.Log.w(
                    "MusicRepository",
                    "playback/continue failed (${response.code()}), using mb/playback/recent"
                )
            }
            val recent = fetchMbRecentPlaylists(limit = 6)
            MusicResult.Success(recent)
        } catch (e: Exception) {
            android.util.Log.e("MusicRepository", "getRecentPlaylists error", e)
            MusicResult.Success(fetchMbRecentPlaylists(limit = 6))
        }
    }

    private suspend fun playHistoryToPlaylists(historyDtos: List<PlayHistoryDto>): List<Playlist> =
        coroutineScope {
            historyDtos.map { history ->
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
                        labelOverlay = dto.artistName,
                        placeholderColors = getColorsForId(dto.resolvedId()),
                        resumePositionMs = history.progressSeconds * 1000L
                    )
                }
            }.awaitAll()
        }

    /** User-specific recently played (same source as Library → Recently). */
    private suspend fun fetchMbRecentPlaylists(limit: Int): List<Playlist> {
        if (apiService == null) return emptyList()
        return try {
            val response = apiService.getMbRecentlyPlayed(limit = limit)
            if (!response.isSuccessful) {
                android.util.Log.w("MusicRepository", "mb/playback/recent failed: ${response.code()}")
                return emptyList()
            }
            coroutineScope {
                response.body()?.data.orEmpty().map { item ->
                    async {
                        val song = item.song
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
                            labelOverlay = song.artistName,
                            placeholderColors = getColorsForId(song.resolvedId()),
                            resumePositionMs = (item.progressSeconds ?: 0).coerceAtLeast(0) * 1000L
                        )
                    }
                }.awaitAll()
            }.distinctBy { it.id }
        } catch (e: Exception) {
            android.util.Log.e("MusicRepository", "mb/playback/recent error", e)
            emptyList()
        }
    }

    // ── Artists ───────────────────────────────────────────────────────────────

    suspend fun getPopularArtists(limit: Int = 20): MusicResult<List<Artist>> = withContext(Dispatchers.IO) {
        if (apiService == null) return@withContext MusicResult.Error("API service not initialized")
        try {
            val response = apiService.getMbPopularArtists(limit = limit)
            if (response.isSuccessful) {
                val artists = mapArtistDtos(response.body()?.data.orEmpty())
                if (artists.isNotEmpty()) {
                    return@withContext MusicResult.Success(artists)
                }
            } else {
                android.util.Log.w(
                    "MusicRepository",
                    "mb/artists/popular failed (${response.code()}), falling back to /artists"
                )
            }
            fetchArtistsFromPublicCatalog()
        } catch (e: Exception) {
            android.util.Log.e("MusicRepository", "getPopularArtists error", e)
            fetchArtistsFromPublicCatalog()
        }
    }

    suspend fun getRandomArtists(limit: Int = 8): MusicResult<List<Artist>> = withContext(Dispatchers.IO) {
        if (apiService == null) return@withContext MusicResult.Error("API service not initialized")
        try {
            val response = apiService.getMbRandomArtists(limit = limit)
            if (response.isSuccessful) {
                val artists = mapArtistDtos(response.body()?.data.orEmpty())
                if (artists.isNotEmpty()) {
                    return@withContext MusicResult.Success(artists)
                }
            } else {
                android.util.Log.w(
                    "MusicRepository",
                    "mb/artists/random failed (${response.code()}), using local shuffle fallback"
                )
            }
            when (val popular = getPopularArtists(limit = 50)) {
                is MusicResult.Success -> MusicResult.Success(popular.data.shuffled().take(limit))
                is MusicResult.Error -> popular
                else -> MusicResult.Success(emptyList())
            }
        } catch (e: Exception) {
            android.util.Log.e("MusicRepository", "getRandomArtists error", e)
            when (val popular = getPopularArtists(limit = 50)) {
                is MusicResult.Success -> MusicResult.Success(popular.data.shuffled().take(limit))
                is MusicResult.Error -> popular
                else -> MusicResult.Success(emptyList())
            }
        }
    }

    private suspend fun fetchArtistsFromPublicCatalog(): MusicResult<List<Artist>> {
        val response = apiService?.getArtists()
            ?: return MusicResult.Error("API service not initialized")
        return if (response.isSuccessful) {
            MusicResult.Success(mapArtistDtos(response.body()?.data.orEmpty()))
        } else {
            MusicResult.Error("Failed to fetch artists: ${response.code()}")
        }
    }

    private suspend fun mapArtistDtos(artistDtos: List<ArtistDto>): List<Artist> =
        coroutineScope {
            artistDtos.map { dto ->
                async { enrichArtist(dto.toDomain()) }
            }.awaitAll()
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
            val response = api.getMostPlayedAlbums(limit = 10)
            if (response.isSuccessful) {
                val mostPlayedDtos = response.body()?.data ?: emptyList()
                val albums = mostPlayedDtos.map { dto ->
                    async { enrichAlbumDto(dto.album) }
                }.awaitAll()
                MusicResult.Success(albums)
            } else {
                getNewAlbums()
            }
        } catch (e: Exception) {
            getNewAlbums()
        }
    }

    suspend fun getNewAlbums(): MusicResult<List<Album>> = withContext(Dispatchers.IO) {
        val api = apiService ?: return@withContext MusicResult.Error("API service not initialized")
        try {
            val response = api.getAlbums(sortBy = "latest", perPage = 10)
            if (response.isSuccessful) {
                val albumDtos = response.body()?.data ?: emptyList()
                val albums = albumDtos.map { dto ->
                    async { enrichAlbumDto(dto) }
                }.awaitAll()
                MusicResult.Success(albums)
            } else {
                MusicResult.Error("Failed to fetch new albums")
            }
        } catch (e: Exception) {
            MusicResult.Error(e.localizedMessage ?: "Network error")
        }
    }

    suspend fun getAllAlbums(perPage: Int = 50): MusicResult<List<Album>> = withContext(Dispatchers.IO) {
        if (apiService == null) return@withContext MusicResult.Error("API service not initialized")
        try {
            val response = apiService.getAlbums(sortBy = "latest", perPage = perPage)
            if (response.isSuccessful) {
                val albumDtos = response.body()?.data ?: emptyList()
                val albums = albumDtos.map { dto ->
                    async { enrichAlbumDto(dto) }
                }.awaitAll()
                MusicResult.Success(albums)
            } else {
                MusicResult.Error("Failed to fetch albums")
            }
        } catch (e: Exception) {
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

            val resolvedAlbumCover = resolveAlbumCoverUrl(albumDto.id.toString(), albumDto.getDisplayCoverUrl())
            val songsResponse = api.getSongs(albumId = albumDto.id)
            
            val tracks = if (songsResponse.isSuccessful) {
                // Fetch all song covers in parallel to fix the 403 issue and show unique images
                songsResponse.body()?.data?.map { dto ->
                    async {
                        val domainTrack = dto.toDomain(albumDto.artist?.name ?: albumDto.artistName ?: "Unknown")
                        val coverUrl = resolveTrackCoverUrl(
                            songId = dto.id.toString(),
                            albumId = dto.albumId?.toString(),
                            coverUrl = dto.coverUrl,
                            albumCoverUrl = albumDto.getDisplayCoverUrl()
                        )
                        domainTrack.copy(imageUrl = coverUrl ?: resolvedAlbumCover)
                    }
                }?.awaitAll() ?: emptyList()
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
            val response = apiService.getSongDetail(id)
            if (response.isSuccessful) {
                val songDto = response.body() ?: return@withContext MusicResult.Error("Empty response body")
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
                MusicResult.Error("Failed to get stream ticket")
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
        imageUrl = directImageUrl(getDisplayCoverUrl()),
        placeholderColors = getColorsForId(id.toString())
    )

    private fun SongDto.resolvedTitle(): String =
        title?.takeIf { it.isNotBlank() } ?: name?.takeIf { it.isNotBlank() } ?: "Unknown Track"

    private fun SongDto.resolvedId(): String = (id ?: 0).toString()

    private fun SongDto.resolvedArtist(fallback: String = "Unknown"): String =
        artistName?.takeIf { it.isNotBlank() }
            ?: artist?.name?.takeIf { it.isNotBlank() && it != "Unknown" }
            ?: album?.artist?.name?.takeIf { it.isNotBlank() && it != "Unknown" }
            ?: album?.artistName?.takeIf { it.isNotBlank() }
            ?: fallback

    private fun SongDto.toDomain(defaultArtist: String) = Track(
        id = resolvedId(),
        title = resolvedTitle(),
        artist = resolvedArtist(defaultArtist),
        durationMs = resolveDurationMs(durationSeconds),
        imageUrl = directImageUrl(coverUrl ?: album?.coverUrl),
        album = album?.title?.takeIf { it.isNotBlank() },
        placeholderColors = getColorsForId(resolvedId()),
        isFavorite = isFavorite ?: false
    )

    private fun FavoriteItemDto.toTrack(): Track? {
        if (favoritableType != null && !favoritableType.contains("Song", ignoreCase = true)) {
            return null
        }
        val songDto = song ?: favoritable ?: return null
        return songDto.toDomain("Unknown").copy(isFavorite = true)
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
                        val albumId = song.albumId?.toString() ?: song.album?.id?.toString() ?: song.resolvedId()
                        val imageUrl = resolveTrackCoverUrl(
                            songId = song.resolvedId(),
                            albumId = albumId,
                            coverUrl = song.coverUrl,
                            albumCoverUrl = song.album?.coverUrl
                        )

                        Playlist(
                            id = albumId,
                            title = song.album?.title ?: song.resolvedTitle(),
                            imageUrl = imageUrl,
                            labelOverlay = song.artistName ?: song.artist?.name,
                            placeholderColors = getColorsForId(albumId)
                        )
                    }
                }.awaitAll().distinctBy { it.id }
                MusicResult.Success(playlists)
            } else {
                MusicResult.Success(fetchMbRecentPlaylists(limit = 10))
            }
        } catch (e: Exception) {
            MusicResult.Success(fetchMbRecentPlaylists(limit = 10))
        }
    }

    suspend fun getFeaturedArtist(): MusicResult<FeaturedArtist> = withContext(Dispatchers.IO) {
        val artists = getPopularArtists(limit = 1)
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
            val response = apiService.getMbFavorites()
            if (response.isSuccessful) {
                val items = response.body()?.data.orEmpty()
                val tracks = items.mapNotNull { item ->
                    val dto = item.song ?: return@mapNotNull null
                    val track = item.toTrack()
                    track?.let {
                        android.util.Log.d(
                            "MusicRepository",
                            "mb/favorites id=${dto.id} apiDuration=${dto.durationSeconds} " +
                                "durationMs=${it.durationMs} ui=${formatTrackDuration(it.durationMs)}"
                        )
                    }
                    track
                }
                MusicResult.Success(enrichTrackCovers(mergeFavoriteStatus(tracks)))
            } else {
                MusicResult.Error("Failed to fetch favorite tracks")
            }
        } catch (e: Exception) {
            MusicResult.Error(e.localizedMessage ?: "Network error")
        }
    }

    /**
     * Recently played for Library — mobile MB endpoint (listen-history + normalized songs).
     */
    suspend fun getRecentlyPlayedTracks(): MusicResult<List<Track>> = withContext(Dispatchers.IO) {
        if (apiService == null) return@withContext MusicResult.Error("API service not initialized")
        try {
            val response = apiService.getMbRecentlyPlayed(limit = 50)
            if (response.isSuccessful) {
                val tracks = response.body()?.data.orEmpty().map { item ->
                    val song = item.song
                    song.toDomain(song.resolvedArtist()).copy(
                        resumePositionMs = (item.progressSeconds ?: 0).coerceAtLeast(0) * 1000L,
                        isFavorite = song.isFavorite ?: false
                    )
                }.distinctBy { it.id }
                response.body()?.data.orEmpty().forEach { item ->
                    android.util.Log.d(
                        "MusicRepository",
                        "mb/playback/recent id=${item.song.id} apiDuration=${item.song.durationSeconds} " +
                            "durationMs=${item.song.let { resolveDurationMs(it.durationSeconds) }} " +
                            "ui=${formatTrackDuration(resolveDurationMs(item.song.durationSeconds))}"
                    )
                }
                MusicResult.Success(enrichTrackCovers(mergeFavoriteStatus(tracks)))
            } else {
                MusicResult.Error("Failed to fetch recently played")
            }
        } catch (e: Exception) {
            MusicResult.Error(e.localizedMessage ?: "Network error")
        }
    }

    suspend fun trackPlaybackProgress(
        songId: String,
        progressSeconds: Int,
        durationSeconds: Int
    ): MusicResult<Boolean> = withContext(Dispatchers.IO) {
        if (apiService == null) return@withContext MusicResult.Error("API service not initialized")
        try {
            val songIntId = songId.toIntOrNull()
                ?: return@withContext MusicResult.Error("Invalid song ID")
            val response = apiService.trackPlaybackProgress(
                PlaybackProgressRequest(
                    songId = songIntId,
                    progressSeconds = progressSeconds.coerceAtLeast(0),
                    durationSeconds = durationSeconds.coerceAtLeast(1),
                    source = "android"
                )
            )
            if (response.isSuccessful) {
                MusicResult.Success(true)
            } else {
                MusicResult.Error("Failed to track playback: ${response.code()}")
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

    /**
     * Reads duration from the streamed audio file (same source as ExoPlayer on the player screen).
     * Used when API/DB `songs.duration` is wrong or a placeholder (e.g. 180s for every row).
     */
    suspend fun enrichTracksDurationFromMedia(tracks: List<Track>): List<Track> =
        withContext(Dispatchers.IO) {
            if (tracks.isEmpty() || apiService == null) return@withContext tracks

            val semaphore = Semaphore(MEDIA_DURATION_ENRICH_CONCURRENCY)
            coroutineScope {
                tracks.map { track ->
                    async {
                        if (!track.needsMediaDurationEnrichment()) return@async track
                        semaphore.withPermit { enrichSingleTrackDurationFromMedia(track) }
                    }
                }.awaitAll()
            }
        }

    private fun Track.needsMediaDurationEnrichment(): Boolean =
        durationMs <= 0L || durationMs == PLACEHOLDER_DURATION_MS

    private suspend fun enrichSingleTrackDurationFromMedia(track: Track): Track {
        val ticketResult = getStreamTicket(track.id)
        if (ticketResult !is MusicResult.Success) return track

        val url = ticketResult.data.signedUrl
            ?: ticketResult.data.streamUrl
            ?: ticketResult.data.url
            ?: return track

        val mediaDurationMs = extractMediaDurationMs(url) ?: return track
        if (mediaDurationMs <= 0L) return track

        android.util.Log.d(
            "MusicRepository",
            "media duration id=${track.id} apiMs=${track.durationMs} mediaMs=$mediaDurationMs " +
                "ui=${formatTrackDuration(mediaDurationMs)}"
        )
        return track.copy(durationMs = mediaDurationMs)
    }

    private fun extractMediaDurationMs(url: String): Long? {
        val retriever = MediaMetadataRetriever()
        return try {
            retriever.setDataSource(url, emptyMap())
            retriever.extractMetadata(MediaMetadataRetriever.METADATA_KEY_DURATION)
                ?.toLongOrNull()
                ?.takeIf { it > 0L }
        } catch (e: Exception) {
            android.util.Log.w("MusicRepository", "MediaMetadataRetriever failed: ${e.message}")
            null
        } finally {
            try {
                retriever.release()
            } catch (_: Exception) {
            }
        }
    }

    private suspend fun enrichTrackCovers(tracks: List<Track>): List<Track> {
        if (tracks.isEmpty()) return tracks
        return tracks.map { track ->
            if (track.imageUrl.isNullOrBlank()) {
                track.copy(imageUrl = directImageUrl(null)) 
            } else track
        }
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
