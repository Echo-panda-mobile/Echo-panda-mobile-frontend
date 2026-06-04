package com.example.echo_panda_mobile.data.repository

import android.util.Log
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
        val raw = url?.takeIf { it.isNotBlank() && it != "null" } ?: return null
        if (raw.startsWith("http") || raw.startsWith("content://") || raw.startsWith("file://")) {
            return raw
        }
        
        val apiBase = com.example.echo_panda_mobile.BuildConfig.API_BASE_URL
        val domainBase = apiBase.replace("/api/", "/")
        val cleanPath = if (raw.startsWith("/")) raw.substring(1) else raw
        
        val finalUrl = if (!cleanPath.contains("storage/") && !cleanPath.startsWith("http")) {
            "${domainBase}storage/$cleanPath"
        } else {
            "$domainBase$cleanPath"
        }
        
        Log.d("MusicRepository", "Generated URL: $finalUrl (from raw: $raw)")
        return finalUrl
    }

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
            val response = apiService.getFavorites()
            if (!response.isSuccessful) return tracks
            val favoriteIds = response.body()?.data?.mapNotNull { item ->
                item.favoritable?.id ?: item.song?.id ?: item.songId ?: item.favoritableId ?: item.id
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
                        val albumId = dto.albumId?.toString() ?: dto.album?.id?.toString() ?: dto.resolvedId()
                        
                        val imageUrl = resolveTrackCoverUrl(
                            songId = dto.resolvedId(),
                            albumId = albumId,
                            coverUrl = dto.coverUrl,
                            albumCoverUrl = dto.album?.coverUrl
                        )

                        Playlist(
                            id = albumId,
                            title = dto.resolvedTitle(),
                            imageUrl = imageUrl,
                            labelOverlay = dto.artistName ?: dto.artist?.name,
                            placeholderColors = getColorsForId(albumId),
                            resumePositionMs = history.progressSeconds * 1000L,
                            trackId = dto.resolvedId()
                        )
                    }
                }.awaitAll().distinctBy { it.trackId ?: it.id }
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

    suspend fun getPopularArtists(limit: Int = 30): MusicResult<List<Artist>> = withContext(Dispatchers.IO) {
        val api = apiService ?: return@withContext MusicResult.Error("API service not initialized")
        try {
            // 1. Try public popular artists endpoint (ranked by play count)
            val popularResponse = api.getPopularArtists(limit = limit)
            if (popularResponse.isSuccessful) {
                val popularDtos = popularResponse.body()?.data ?: emptyList()
                if (popularDtos.isNotEmpty()) {
                    val artists = popularDtos.map { dto ->
                        async { enrichArtist(dto.toDomain()) }
                    }.awaitAll()
                    return@withContext MusicResult.Success(artists)
                }
            }

            // 2. Fallback: public artists list
            val response = api.getArtists()
            if (response.isSuccessful) {
                val artistDtos = response.body()?.data ?: emptyList()
                if (artistDtos.isNotEmpty()) {
                    val artists = artistDtos.take(limit).map { dto ->
                        async { enrichArtist(dto.toDomain()) }
                    }.awaitAll()
                    return@withContext MusicResult.Success(artists)
                }
            }

            // 3. Fallback: derive from albums and songs
            Log.d("MusicRepository", "Popular artists endpoint empty, falling back to derivation")
            val albumsDef = async { api.getAlbums(perPage = 50) }
            val songsDef = async { api.getSongs() }

            val albumDtos = (albumsDef.await().body()?.data ?: emptyList())
            val songDtos = (songsDef.await().body()?.data ?: emptyList())

            val artistMap = mutableMapOf<String, Artist>()

            fun addArtist(name: String?, id: String?, imageUrl: String?) {
                val artistName = name?.trim() ?: return
                if (artistName.isBlank() || artistName.lowercase() == "unknown") return
                
                val key = artistName.lowercase()
                if (!artistMap.containsKey(key)) {
                    val resolvedId = id ?: java.net.URLEncoder.encode(artistName, "UTF-8")
                    artistMap[key] = Artist(
                        id = resolvedId,
                        name = artistName,
                        imageUrl = directImageUrl(imageUrl),
                        placeholderColors = getColorsForId(resolvedId)
                    )
                }
            }

            songDtos.forEach { song ->
                addArtist(song.artistName ?: song.artist?.name, song.artist?.id, null)
            }
            albumDtos.forEach { album ->
                addArtist(album.artistName ?: album.artist?.name, album.artist?.id, null)
            }

            val derived = artistMap.values.toList().take(limit)
            MusicResult.Success(derived)
        } catch (e: Exception) {
            Log.e("MusicRepository", "Error fetching popular artists", e)
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
                val data = response.body()?.data ?: emptyList()
                val tracks = coroutineScope {
                    data.map { dto ->
                        async {
                            val track = dto.toDomain(artistName)
                            val imageUrl = resolveTrackCoverUrl(
                                songId = dto.resolvedId(),
                                albumId = dto.albumId?.toString() ?: dto.album?.id?.toString(),
                                coverUrl = dto.coverUrl,
                                albumCoverUrl = dto.album?.coverUrl
                            )
                            track.copy(imageUrl = imageUrl)
                        }
                    }.awaitAll()
                }
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
                val data = response.body()?.data ?: emptyList()
                val tracks = coroutineScope {
                    data.map { dto ->
                        async {
                            val track = dto.toDomain("Unknown")
                            val imageUrl = resolveTrackCoverUrl(
                                songId = dto.resolvedId(),
                                albumId = dto.albumId?.toString() ?: dto.album?.id?.toString(),
                                coverUrl = dto.coverUrl,
                                albumCoverUrl = dto.album?.coverUrl
                            )
                            track.copy(imageUrl = imageUrl)
                        }
                    }.awaitAll()
                }
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
                val data = response.body()?.data ?: emptyList()
                val tracks = coroutineScope {
                    data.map { dto ->
                        async {
                            val songDto = dto.song
                            val track = songDto.toDomain("Unknown")
                            val imageUrl = resolveTrackCoverUrl(
                                songId = songDto.resolvedId(),
                                albumId = songDto.albumId?.toString() ?: songDto.album?.id?.toString(),
                                coverUrl = songDto.coverUrl,
                                albumCoverUrl = songDto.album?.coverUrl
                            )
                            track.copy(imageUrl = imageUrl)
                        }
                    }.awaitAll()
                }
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

    private fun MbArtistDto.toDomain() = Artist(
        id = id,
        name = name,
        imageUrl = directImageUrl(imageUrl),
        bio = bio,
        monthlyListeners = monthlyListeners ?: "0",
        placeholderColors = getColorsForId(id)
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

    private fun SongDto.toDomain(defaultArtist: String): Track {
        val resolvedArtist = artist?.name?.takeIf { it != "Unknown" } ?: artistName ?: defaultArtist
        return Track(
            id = resolvedId(),
            title = resolvedTitle(),
            artist = resolvedArtist,
            durationMs = (durationSeconds ?: 0) * 1000L,
            imageUrl = directImageUrl(getDisplayCoverUrl()),
            album = album?.title ?: (if (resolvedArtist != "Unknown") resolvedArtist else null),
            placeholderColors = getColorsForId(resolvedId()),
            isFavorite = isFavorite ?: false
        )
    }

    private fun FavoriteItemDto.toTrack(): Track? {
        favoritable?.let { return it.toDomain("Unknown").copy(isFavorite = true) }
        song?.let { return it.toDomain("Unknown").copy(isFavorite = true) }
        val trackId = songId ?: favoritableId ?: id ?: return null
        val idStr = trackId.toString()
        val resolvedTitle = title?.takeIf { it.isNotBlank() } ?: name?.takeIf { it.isNotBlank() } ?: "Unknown Track"
        return Track(
            id = idStr,
            title = resolvedTitle,
            artist = artistName ?: "Unknown",
            durationMs = (durationSeconds ?: 0) * 1000L,
            imageUrl = directImageUrl(getDisplayCoverUrl()),
            album = album?.title,
            placeholderColors = getColorsForId(idStr),
            isFavorite = true
        )
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
                val data = response.body()?.data ?: emptyList()
                val tracks = coroutineScope {
                    data.map { dto ->
                        async {
                            val track = dto.toTrack() ?: return@async null
                            val songDto = dto.favoritable ?: dto.song
                            val imageUrl = if (songDto != null) {
                                resolveTrackCoverUrl(
                                    songId = songDto.resolvedId(),
                                    albumId = songDto.albumId?.toString() ?: songDto.album?.id?.toString(),
                                    coverUrl = songDto.coverUrl,
                                    albumCoverUrl = songDto.album?.coverUrl
                                )
                            } else {
                                resolveTrackCoverUrl(
                                    songId = track.id,
                                    albumId = null,
                                    coverUrl = null,
                                    albumCoverUrl = null
                                )
                            }
                            track.copy(imageUrl = imageUrl)
                        }
                    }.awaitAll().filterNotNull()
                }
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
            val response = apiService.getListenHistory(perPage = 25)
            if (response.isSuccessful) {
                val data = response.body()?.data ?: emptyList()
                val tracks = coroutineScope {
                    data.map { history ->
                        async {
                            val dto = history.song
                            val track = dto.toDomain("Unknown")
                            val imageUrl = resolveTrackCoverUrl(
                                songId = dto.resolvedId(),
                                albumId = dto.albumId?.toString() ?: dto.album?.id?.toString(),
                                coverUrl = dto.coverUrl,
                                albumCoverUrl = dto.album?.coverUrl
                            )
                            track.copy(imageUrl = imageUrl)
                        }
                    }.awaitAll()
                }
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

    suspend fun removeFromPlaylist(trackId: String, playlistId: String): MusicResult<Boolean> = withContext(Dispatchers.IO) {
        if (apiService == null) return@withContext MusicResult.Error("API service not initialized")
        try {
            val response = apiService.removeSongFromPlaylist(playlistId, trackId)
            if (response.isSuccessful) MusicResult.Success(true) else MusicResult.Error("Failed to remove song")
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
