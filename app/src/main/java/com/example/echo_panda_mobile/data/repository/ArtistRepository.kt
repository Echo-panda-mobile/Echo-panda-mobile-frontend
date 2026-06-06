package com.example.echo_panda_mobile.data.repository

import com.example.echo_panda_mobile.data.remote.*
import com.example.echo_panda_mobile.data.repository.TokenStorage
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import okhttp3.MediaType.Companion.toMediaTypeOrNull
import okhttp3.MultipartBody
import okhttp3.RequestBody.Companion.asRequestBody
import okhttp3.RequestBody.Companion.toRequestBody
import java.io.File

import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import kotlinx.coroutines.async
import kotlinx.coroutines.awaitAll
import kotlinx.coroutines.coroutineScope
import com.example.echo_panda_mobile.BuildConfig

class ArtistRepository(
    private val apiService: MusicApiService,
    private val tokenStorage: TokenStorage
) {
    // ─── Reactive Data Flows ────────────────────────────────────────────────
    private val _songs = MutableStateFlow<List<SongDto>>(emptyList())
    val songs: StateFlow<List<SongDto>> = _songs.asStateFlow()

    private val _albums = MutableStateFlow<List<AlbumDto>>(emptyList())
    val albums: StateFlow<List<AlbumDto>> = _albums.asStateFlow()

    suspend fun getMySongs(): Result<List<SongDto>> = withContext(Dispatchers.IO) {
        try {
            android.util.Log.d("ArtistRepository", "━━━ FETCH ARTIST SONGS START ━━━")
            val response = apiService.getMySongs(perPage = 500, sortBy = "latest")
            
            if (response.isSuccessful && response.body() != null) {
                val allSongs = response.body()!!.allSongs
                
                // Filter by logged-in artist ID or Name (matching web logic)
                val currentArtistId = tokenStorage.getArtistId()
                val currentUserName = tokenStorage.getName()?.trim()
                
                android.util.Log.d("ArtistRepository", "Filtering songs for Artist ID: $currentArtistId, Name: $currentUserName")

                val filteredSongs = allSongs.filter { song ->
                    songBelongsToLoggedInArtist(song, currentArtistId, currentUserName)
                }
                
                // Resolve Image URLs for each song
                val playCountBySongId = fetchTopListenedPlayCounts()

                val enrichedSongs = coroutineScope {
                    filteredSongs.map { song ->
                        async {
                            val songId = song.id?.toString() ?: return@async song
                            val resolvedUrl = resolveSongCoverUrl(songId, song.getDisplayCoverUrl())
                            val playCount = song.id?.let { playCountBySongId[it] } ?: song.playCount ?: 0
                            song.copy(coverUrl = resolvedUrl, playCount = playCount)
                        }
                    }.awaitAll()
                }
                
                android.util.Log.d("ArtistRepository", "✓ Successfully fetched ${enrichedSongs.size} filtered songs (out of ${allSongs.size})")
                _songs.value = enrichedSongs
                Result.success(enrichedSongs)
            } else {
                Result.failure(Exception("Error ${response.code()}"))
            }
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    /**
     * Uses existing mobile dashboard endpoint (max 20 songs) for listen + stream totals.
     * Other catalog songs fall back to [SongDto.playCount] from GET /api/songs.
     */
    private suspend fun fetchTopListenedPlayCounts(): Map<Int, Int> {
        return try {
            val response = apiService.getArtistTopListenedSongs(limit = 20)
            if (!response.isSuccessful) return emptyMap()
            response.body()?.data.orEmpty().mapNotNull { row ->
                val songId = row.song.id ?: return@mapNotNull null
                songId to (row.playCount ?: row.song.playCount ?: 0)
            }.toMap()
        } catch (e: Exception) {
            android.util.Log.w("ArtistRepository", "Top listened play counts unavailable: ${e.message}")
            emptyMap()
        }
    }

    private suspend fun resolveSongCoverUrl(songId: String, rawUrl: String?): String? {
        // Try to get signed URL first (standard app logic for private S3 images)
        try {
            val response = apiService.getSongCoverUrl(songId)
            if (response.isSuccessful) {
                val signed = response.body()?.signedUrl ?: response.body()?.url
                if (!signed.isNullOrBlank()) return directImageUrl(signed)
            }
        } catch (_: Exception) { }
        
        return directImageUrl(rawUrl)
    }

    private fun directImageUrl(url: String?): String? {
        val raw = url?.takeIf { it.isNotBlank() && it != "null" } ?: return null
        if (raw.startsWith("http") || raw.startsWith("content://") || raw.startsWith("file://")) {
            return raw
        }
        
        val apiBase = BuildConfig.API_BASE_URL
        val domainBase = apiBase.replace("/api/", "/")
        val cleanPath = if (raw.startsWith("/")) raw.substring(1) else raw
        
        return if (!cleanPath.contains("storage/") && !cleanPath.startsWith("http")) {
            "${domainBase}storage/$cleanPath"
        } else {
            "$domainBase$cleanPath"
        }
    }

    suspend fun getMyAlbums(): Result<List<AlbumDto>> = withContext(Dispatchers.IO) {
        try {
            android.util.Log.d("ALBUM_API", "━━━ FETCH ARTIST ALBUMS START ━━━")
            val response = apiService.getMyAlbums(perPage = 500, sortBy = "latest")

            if (response.isSuccessful && response.body() != null) {
                val body = response.body()!!
                val allAlbums = body.allAlbums

                // Filter by logged-in artist ID or Name (matching web logic)
                val currentArtistId = tokenStorage.getArtistId()
                val currentUserName = tokenStorage.getName()?.trim()

                val filteredAlbums = allAlbums.filter { album ->
                    albumBelongsToLoggedInArtist(album, currentArtistId, currentUserName)
                }

                android.util.Log.d("ALBUM_API", "✓ Successfully fetched ${filteredAlbums.size} filtered albums (out of ${allAlbums.size})")

                // UPDATE FLOW
                _albums.value = filteredAlbums
                Result.success(filteredAlbums)
            } else {
                val errorBody = response.errorBody()?.string()
                android.util.Log.e("ALBUM_API", " Failed: ${response.code()} - $errorBody")
                Result.failure(Exception("Failed to fetch albums: ${response.code()}"))
            }
        } catch (e: Exception) {
            android.util.Log.e("ALBUM_API", " Exception: ${e.message}")
            Result.failure(e)
        }
    }

    suspend fun createAlbum(
        title: String,
        artist: String,
        description: String?,
        coverKey: String?,
        releaseDate: String? = null,
        artistId: Int? = null,
        releaseStatus: String? = "published"
    ): Result<AlbumDto> {
        return try {
            android.util.Log.d("ArtistRepository", "━━━ CREATE ALBUM START ━━━")
            android.util.Log.d("ArtistRepository", "Title: $title, Date: $releaseDate, ID: $artistId")
            
            // Verify token is available before making request
            val token = tokenStorage.getToken()
            if (token.isNullOrBlank()) {
                android.util.Log.e("ArtistRepository", "⚠️ NO TOKEN! Request will fail with 401")
                return Result.failure(Exception("Unauthenticated: No token available. Please login again."))
            }
            
            val releaseDateValue = releaseDate ?: java.text.SimpleDateFormat("yyyy-MM-dd", java.util.Locale.getDefault()).format(java.util.Date())
            
            val request = CreateAlbumRequest(
                title = title,
                artist = artist,
                artistId = artistId,
                description = description,
                coverKey = coverKey,
                releaseDate = releaseDateValue,
                releaseStatus = releaseStatus
            )
            android.util.Log.d("ArtistRepository", "Making API call to: https://api.echopanda.me/api/albums")
            android.util.Log.d("ArtistRepository", "Request Body: $request")
            val response = apiService.createAlbum(request)
            
            android.util.Log.d("ArtistRepository", "Response status: ${response.code()}")
            if (response.isSuccessful && response.body() != null) {
                val createdAlbum = response.body()!!.data
                android.util.Log.d("ArtistRepository", "✓ CREATE ALBUM SUCCESS")
                android.util.Log.d("ArtistRepository", "Album ID: ${createdAlbum.id}")
                android.util.Log.d("ArtistRepository", "Cover URL: ${createdAlbum.coverUrl}")
                android.util.Log.d("ArtistRepository", "Cover Image: ${createdAlbum.coverImage}")
                android.util.Log.d("ArtistRepository", "Display Cover URL: ${createdAlbum.getDisplayCoverUrl()}")
                Result.success(createdAlbum)
            } else {
                val errorMsg = response.errorBody()?.string() ?: response.message()
                android.util.Log.e("ArtistRepository", "⚠️ API Error: $errorMsg")
                Result.failure(Exception("Failed to create album: $errorMsg"))
            }
        } catch (e: com.google.gson.JsonSyntaxException) {
            android.util.Log.e("ArtistRepository", "JSON parsing error: ${e.message}")
            // Return more helpful error for debugging
            Result.failure(Exception("Server returned invalid data format. If you see this, the server might be sending an HTML error page instead of JSON. Check your domain/URL."))
        } catch (e: Exception) {
            android.util.Log.e("ArtistRepository", "Exception: ${e.message}", e)
            Result.failure(e)
        }
    }

    suspend fun updateAlbum(
        albumId: String,
        title: String,
        artist: String,
        description: String?,
        coverKey: String?,
        releaseDate: String? = null,
        releaseStatus: String? = "published"
    ): Result<AlbumDto> {
        return try {
            val request = CreateAlbumRequest(
                title = title,
                artist = artist,
                description = description,
                coverKey = coverKey,
                releaseDate = releaseDate,
                releaseStatus = releaseStatus
            )
            val response = apiService.updateAlbum(albumId, request)
            if (response.isSuccessful && response.body() != null) {
                Result.success(response.body()!!.data)
            } else {
                Result.failure(Exception("Failed to update album: ${response.message()}"))
            }
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    suspend fun deleteAlbum(albumId: String): Result<Unit> {
        return try {
            val response = apiService.deleteAlbum(albumId)
            if (response.isSuccessful) {
                Result.success(Unit)
            } else {
                Result.failure(Exception("Failed to delete album"))
            }
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    suspend fun getSongsForAlbum(albumId: Int): Result<List<SongDto>> = withContext(Dispatchers.IO) {
        try {
            val response = apiService.getSongs(albumId = albumId, perPage = 500, sortBy = "track_number")
            if (!response.isSuccessful || response.body() == null) {
                return@withContext Result.failure(Exception("Failed to load album tracks (${response.code()})"))
            }

            val songs = response.body()!!.data
                .sortedBy { it.trackNumber ?: Int.MAX_VALUE }

            val enrichedSongs = coroutineScope {
                songs.map { song ->
                    async {
                        val songId = song.id?.toString() ?: return@async song
                        val resolvedUrl = resolveSongCoverUrl(songId, song.getDisplayCoverUrl())
                        song.copy(coverUrl = resolvedUrl)
                    }
                }.awaitAll()
            }

            Result.success(enrichedSongs)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    suspend fun uploadSong(
        albumId: Int?,
        title: String,
        duration: Int,
        trackNumber: Int,
        categoryId: String?,
        tagId: Int? = null,
        lyrics: String?,
        audioKey: String?,
        coverKey: String?
    ): Result<SongDto> {
        return try {
            // Validate required fields
            if (audioKey == null || audioKey.isBlank()) {
                return Result.failure(Exception("Audio file is required - upload failed"))
            }
            if (title.isBlank()) {
                return Result.failure(Exception("Song title is required"))
            }
            if (duration <= 0) {
                return Result.failure(Exception("Invalid audio duration"))
            }

            val request = CreateSongRequest(
                albumId = if (albumId != null && albumId > 0) albumId else null,
                title = title.trim(),
                duration = duration,
                trackNumber = trackNumber,
                categoryId = categoryId?.trim()?.takeIf { it.isNotBlank() },
                tagId = tagId,
                lyrics = lyrics?.trim(),
                originalKey = audioKey,
                coverKey = coverKey
            )
            val response = apiService.createSong(request)
            if (response.isSuccessful && response.body() != null) {
                // NOTE: If the backend doesn't automatically send FCM notifications,
                // you might need to call a specific notification endpoint here.
                Result.success(response.body()!!.data)
            } else {
                val errorMsg = response.body()?.message 
                    ?: response.errorBody()?.string() 
                    ?: response.message()
                Result.failure(Exception("Failed to save song to database: $errorMsg"))
            }
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    fun getLoggedInArtistDisplayName(): String? =
        tokenStorage.getName()?.trim()?.takeIf { it.isNotBlank() }

    suspend fun getSongFormOptions(): Result<Pair<List<MbGenreDto>, List<MbTagDto>>> = withContext(Dispatchers.IO) {
        try {
            android.util.Log.d(EDIT_SONG_LOG, "GET form options: /api/genres + /api/tags")
            val genresResponse = apiService.getPublicGenres()
            val tagsResponse = apiService.getPublicTags()
            android.util.Log.d(
                EDIT_SONG_LOG,
                "Form options HTTP genres=${genresResponse.code()} tags=${tagsResponse.code()} " +
                    "genresCount=${genresResponse.body()?.data?.size ?: 0} tagsCount=${tagsResponse.body()?.data?.size ?: 0}"
            )
            if (!genresResponse.isSuccessful || !tagsResponse.isSuccessful) {
                val err = buildString {
                    if (!genresResponse.isSuccessful) append("genres=${genresResponse.errorBody()?.string()} ")
                    if (!tagsResponse.isSuccessful) append("tags=${tagsResponse.errorBody()?.string()}")
                }
                android.util.Log.e(EDIT_SONG_LOG, "Form options failed: $err")
                return@withContext Result.failure(Exception(err.ifBlank { "Failed to load genres or tags" }))
            }
            Result.success(
                (genresResponse.body()?.data ?: emptyList()) to (tagsResponse.body()?.data ?: emptyList())
            )
        } catch (e: Exception) {
            android.util.Log.e(EDIT_SONG_LOG, "Form options exception", e)
            Result.failure(e)
        }
    }

    suspend fun getSongForEdit(songId: String): Result<SongDto> = withContext(Dispatchers.IO) {
        try {
            android.util.Log.d(EDIT_SONG_LOG, "GET song for edit: /api/songs/$songId")
            val response = apiService.getSongDetail(songId)
            val errorBody = response.errorBody()?.string()
            android.util.Log.d(
                EDIT_SONG_LOG,
                "GET song response code=${response.code()} success=${response.isSuccessful} errorBody=${errorBody?.take(500)}"
            )
            if (response.isSuccessful && response.body() != null) {
                val song = response.body()!!
                android.util.Log.d(
                    EDIT_SONG_LOG,
                    "GET song parsed id=${song.id} title=${song.title} albumId=${song.albumId} " +
                        "categoryId=${song.categoryId} tagId=${song.tagId} coverKey=${song.coverKey}"
                )
                val coverUrl = resolveSongCoverUrl(songId, song.getDisplayCoverUrl())
                Result.success(song.copy(coverUrl = coverUrl))
            } else {
                Result.failure(Exception(errorBody ?: "Failed to load song (HTTP ${response.code()})"))
            }
        } catch (e: Exception) {
            android.util.Log.e(EDIT_SONG_LOG, "GET song exception", e)
            Result.failure(e)
        }
    }

    suspend fun updateSongForEdit(
        songId: String,
        request: UpdateSongRequest
    ): Result<SongDto> = withContext(Dispatchers.IO) {
        try {
            val token = tokenStorage.getToken()
            if (token.isNullOrBlank()) {
                android.util.Log.e(EDIT_SONG_LOG, "PUT song aborted: no auth token in storage")
                return@withContext Result.failure(
                    Exception("Unauthenticated: No token available. Please log in again.")
                )
            }

            android.util.Log.d(
                EDIT_SONG_LOG,
                "PUT song edit: /api/songs/$songId body=" +
                    "album_id=${request.albumId}, title=${request.title}, artist=${request.artist}, " +
                    "duration=${request.duration}, track_number=${request.trackNumber}, " +
                    "category_id=${request.categoryId}, tag_id=${request.tagId}, " +
                    "cover_key=${request.coverKey}, original_key=${request.originalKey}, " +
                    "lyrics_len=${request.lyrics?.length ?: 0}"
            )
            val response = apiService.updateSong(songId, request)
            val errorBody = response.errorBody()?.string()
            android.util.Log.d(
                EDIT_SONG_LOG,
                "PUT song response code=${response.code()} success=${response.isSuccessful} " +
                    "message=${response.body()?.message} errorBody=${errorBody?.take(1000)}"
            )
            if (response.isSuccessful && response.body()?.data != null) {
                val updated = response.body()!!.data
                android.util.Log.d(
                    EDIT_SONG_LOG,
                    "PUT song success id=${updated.id} title=${updated.title} categoryId=${updated.categoryId} tagId=${updated.tagId}"
                )
                Result.success(updated)
            } else {
                val message = formatApiError(errorBody, response.code())
                android.util.Log.e(EDIT_SONG_LOG, "PUT song failed: $message")
                Result.failure(Exception(message))
            }
        } catch (e: Exception) {
            android.util.Log.e(EDIT_SONG_LOG, "PUT song exception", e)
            Result.failure(e)
        }
    }

    private fun formatApiError(errorBody: String?, httpCode: Int): String {
        if (errorBody.isNullOrBlank()) {
            return when (httpCode) {
                401 -> "Unauthenticated. Please log out and sign in again."
                403 -> "You do not have permission to edit this song."
                else -> "Failed to update song (HTTP $httpCode)"
            }
        }
        return try {
            val json = org.json.JSONObject(errorBody)
            val message = json.optString("message").takeIf { it.isNotBlank() }
            val errors = json.optJSONObject("errors")
            if (errors != null) {
                val details = buildList {
                    errors.keys().forEach { key ->
                        val arr = errors.optJSONArray(key)
                        if (arr != null && arr.length() > 0) add("$key: ${arr.getString(0)}")
                    }
                }
                if (details.isNotEmpty()) {
                    return listOfNotNull(message).plus(details).joinToString(" | ")
                }
            }
            message ?: errorBody
        } catch (_: Exception) {
            errorBody
        }
    }

    private fun songBelongsToLoggedInArtist(
        song: SongDto,
        currentArtistId: Int?,
        currentUserName: String?,
    ): Boolean {
        if (currentArtistId == null) return false

        val songArtistId = song.artistId ?: song.artist?.id?.toIntOrNull()
        if (songArtistId == currentArtistId) return true

        val albumArtistId = song.album?.artistId ?: song.album?.artist?.id?.toIntOrNull()
        if (albumArtistId == currentArtistId) return true

        val songArtistName = (song.artistName ?: song.artist?.name)?.trim()
        return !currentUserName.isNullOrBlank()
            && !songArtistName.isNullOrBlank()
            && currentUserName.equals(songArtistName, ignoreCase = true)
    }

    private fun albumBelongsToLoggedInArtist(
        album: AlbumDto,
        currentArtistId: Int?,
        currentUserName: String?,
    ): Boolean {
        if (currentArtistId == null) return false

        val albumArtistId = album.artistId ?: album.artist?.id?.toIntOrNull()
        if (albumArtistId == currentArtistId) return true

        val albumArtistName = (album.artistName ?: album.artist?.name)?.trim()
        return !currentUserName.isNullOrBlank()
            && !albumArtistName.isNullOrBlank()
            && currentUserName.equals(albumArtistName, ignoreCase = true)
    }

    companion object {
        private const val EDIT_SONG_LOG = "ArtistEditSong"
    }

    suspend fun deleteSong(songId: String): Result<Unit> {
        return try {
            val response = apiService.deleteSong(songId)
            if (response.isSuccessful) {
                Result.success(Unit)
            } else {
                Result.failure(Exception("Failed to delete song"))
            }
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    suspend fun getPresignedUrl(purpose: String, fileName: String, contentType: String, size: Long): Result<PresignedUrlResponse> {
        return try {
            android.util.Log.d("ArtistRepository", ">>> GET PRESIGNED URL: purpose=$purpose, file=$fileName, size=$size")
            
            val request = PresignUploadRequest(
                purpose = purpose,
                fileName = fileName,
                contentType = contentType,
                size = size
            )
            
            val response = apiService.getUploadUrl(request)
            
            if (response.isSuccessful && response.body() != null) {
                android.util.Log.d("ArtistRepository", "<<< PRESIGNED URL SUCCESS")
                Result.success(response.body()!!)
            } else {
                val errorBody = response.errorBody()?.string()
                android.util.Log.e("ArtistRepository", " ❌ PRESIGNED URL FAILED: ${response.code()} - $errorBody")
                Result.failure(Exception("HTTP ${response.code()}: $errorBody"))
            }
        } catch (e: Exception) {
            android.util.Log.e("ArtistRepository", "PRESIGNED URL EXCEPTION: ${e.message}")
            Result.failure(e)
        }
    }

    suspend fun uploadFile(file: File, purpose: String): Result<UploadResponse> {
        return try {
            android.util.Log.d("ArtistRepository", "━━━ UPLOAD FILE START ━━━")
            android.util.Log.d("ArtistRepository", "File: ${file.name}, Purpose: $purpose")
            
            // Verify token is available before making request
            val token = tokenStorage.getToken()
            if (token.isNullOrBlank()) {
                android.util.Log.e("ArtistRepository", "⚠️ NO TOKEN! Upload will fail with 401")
                return Result.failure(Exception("Unauthenticated: No token available. Please login again."))
            } else {
                android.util.Log.d("ArtistRepository", "✓ Token available: ${token.take(20)}...")
            }
            
            // Determine correct MIME type based on file extension and purpose
            val mimeType = when {
                purpose.contains("audio", ignoreCase = true) -> "application/octet-stream"
                purpose.contains("image", ignoreCase = true) || purpose.contains("cover", ignoreCase = true) -> {
                    when (file.extension.lowercase()) {
                        "jpg", "jpeg" -> "image/jpeg"
                        "png" -> "image/png"
                        "webp" -> "image/webp"
                        else -> "image/jpeg"
                    }
                }
                else -> "application/octet-stream"
            }
            
            android.util.Log.d("ArtistRepository", "MIME type: $mimeType")
            
            val requestFile = file.asRequestBody(mimeType.toMediaTypeOrNull())
            val body = MultipartBody.Part.createFormData("file", file.name, requestFile)
            val purposeBody = purpose.toRequestBody("text/plain".toMediaTypeOrNull())

            android.util.Log.d("ArtistRepository", "Making API call to upload file...")
            val response = apiService.uploadMedia(body, purposeBody)
            
            android.util.Log.d("ArtistRepository", "Response status: ${response.code()}")
            if (response.isSuccessful && response.body() != null) {
                android.util.Log.d("ArtistRepository", "✓ UPLOAD SUCCESS")
                Result.success(response.body()!!)
            } else {
                val errorBody = response.errorBody()?.string()
                android.util.Log.e("ArtistRepository", "⚠️ Upload API Error: $errorBody")
                Result.failure(Exception("Upload failed: ${response.code()}"))
            }
        } catch (e: com.google.gson.JsonSyntaxException) {
            android.util.Log.e("ArtistRepository", "JSON parsing error: ${e.message}")
            Result.failure(Exception("Upload failed: Server returned non-JSON response. Check your URL and token."))
        } catch (e: Exception) {
            android.util.Log.e("ArtistRepository", "Exception: ${e.message}", e)
            Result.failure(e)
        }
    }

    suspend fun resolveArtistImageUrl(artistId: String): String? = withContext(Dispatchers.IO) {
        try {
            val response = apiService.getArtistImageUrl(artistId)
            if (response.isSuccessful) {
                response.body()?.signedUrl?.takeIf { it.isNotBlank() }
                    ?: response.body()?.url?.takeIf { it.isNotBlank() }
            } else {
                null
            }
        } catch (_: Exception) {
            null
        }
    }

    /** Public catalog list includes bio; used when single-artist detail route is unavailable. */
    suspend fun getArtistFromCatalog(artistId: String): Result<ArtistDto> = withContext(Dispatchers.IO) {
        try {
            val response = apiService.getArtists()
            if (!response.isSuccessful || response.body() == null) {
                return@withContext Result.failure(Exception("Failed to fetch artist catalog"))
            }
            val artist = response.body()!!.data.find { it.id.toString() == artistId }
                ?: return@withContext Result.failure(Exception("Artist not found"))
            Result.success(artist)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    suspend fun getArtistProfile(artistId: String): Result<ArtistDto> = withContext(Dispatchers.IO) {
        try {
            val detailResponse = apiService.getArtistDetail(artistId)
            if (detailResponse.isSuccessful && detailResponse.body() != null) {
                return@withContext Result.success(detailResponse.body()!!)
            }
            getArtistFromCatalog(artistId)
        } catch (e: Exception) {
            getArtistFromCatalog(artistId)
        }
    }

    suspend fun updateArtistProfile(artistId: Int, name: String, imageUrl: String? = null, bio: String? = null): Result<ArtistDto> = withContext(Dispatchers.IO) {
        try {
            val request = ArtistDto(
                id = artistId,
                name = name,
                imageUrl = imageUrl,
                coverImageUrl = imageUrl, // Update both as per user request for "cover_imge _url"
                bio = bio
            )
            val response = apiService.updateArtistProfile(request)
            if (response.isSuccessful && response.body() != null) {
                Result.success(response.body()!!.data)
            } else {
                val errorBody = response.errorBody()?.string()
                Result.failure(Exception(errorBody ?: "Failed to update artist profile: ${response.message()}"))
            }
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
}
