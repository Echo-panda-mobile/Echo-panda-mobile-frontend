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
                val currentUserId = tokenStorage.getUserId()
                val currentArtistId = tokenStorage.getArtistId()
                val currentUserName = tokenStorage.getName()?.lowercase()?.trim()
                
                android.util.Log.d("ArtistRepository", "Filtering for User ID: $currentUserId, Artist ID: $currentArtistId, Name: $currentUserName")

                val filteredSongs = allSongs.filter { song ->
                    val songArtistId = song.artist?.id?.toIntOrNull()
                    val songArtistName = (song.artistName ?: song.artist?.name)?.lowercase()?.trim()
                    
                    // Match by Artist ID, User ID or Name
                    (songArtistId != null && (songArtistId == currentArtistId || songArtistId == currentUserId)) || 
                    (currentUserName != null && songArtistName != null && (songArtistName.contains(currentUserName) || currentUserName.contains(songArtistName)))
                }
                
                android.util.Log.d("ArtistRepository", "✓ Successfully fetched ${filteredSongs.size} filtered songs (out of ${allSongs.size})")
                _songs.value = filteredSongs
                Result.success(filteredSongs)
            } else {
                Result.failure(Exception("Error ${response.code()}"))
            }
        } catch (e: Exception) {
            Result.failure(e)
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
                val currentUserId = tokenStorage.getUserId()
                val currentArtistId = tokenStorage.getArtistId()
                val currentUserName = tokenStorage.getName()?.lowercase()?.trim()

                val filteredAlbums = allAlbums.filter { album ->
                    val songArtistId = album.artist?.id?.toIntOrNull()
                    val albumArtistName = (album.artistName ?: album.artist?.name)?.lowercase()?.trim()
                    
                    (songArtistId != null && (songArtistId == currentArtistId || songArtistId == currentUserId)) ||
                    (currentUserName != null && albumArtistName != null && (albumArtistName.contains(currentUserName) || currentUserName.contains(albumArtistName)))
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

    suspend fun uploadSong(
        albumId: Int?,
        title: String,
        duration: Int,
        trackNumber: Int,
        genre: String?,
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
                genre = genre?.trim(),
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

    suspend fun updateSong(
        songId: String,
        albumId: Int?,
        title: String,
        duration: Int,
        trackNumber: Int,
        genre: String?,
        lyrics: String?,
        audioKey: String?,
        coverKey: String?
    ): Result<SongDto> {
        return try {
            val request = CreateSongRequest(
                albumId = if (albumId != null && albumId > 0) albumId else null,
                title = title,
                duration = duration,
                trackNumber = trackNumber,
                genre = genre,
                lyrics = lyrics,
                originalKey = audioKey,
                coverKey = coverKey
            )
            val response = apiService.updateSong(songId, request)
            if (response.isSuccessful && response.body() != null) {
                Result.success(response.body()!!.data)
            } else {
                Result.failure(Exception("Failed to update song"))
            }
        } catch (e: Exception) {
            Result.failure(e)
        }
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
