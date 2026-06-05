package com.example.echo_panda_mobile.presentation.viewsmodel

import android.content.Context
import android.graphics.BitmapFactory
import android.net.Uri
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.example.echo_panda_mobile.data.remote.MbGenreDto
import com.example.echo_panda_mobile.data.remote.MbTagDto
import com.example.echo_panda_mobile.data.remote.RetrofitClient
import com.example.echo_panda_mobile.data.remote.SongDto
import com.example.echo_panda_mobile.data.remote.UpdateSongRequest
import com.example.echo_panda_mobile.data.repository.ArtistRepository
import com.example.echo_panda_mobile.data.repository.TokenStorage
import com.example.echo_panda_mobile.util.S3UploadManager
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.io.File
import java.io.FileOutputStream

data class ArtistEditSongUiState(
    val isLoading: Boolean = true,
    val isSaving: Boolean = false,
    val song: SongDto? = null,
    val genres: List<MbGenreDto> = emptyList(),
    val tags: List<MbTagDto> = emptyList(),
    val errorMessage: String? = null,
)

class ArtistEditSongViewModel(
    private val repository: ArtistRepository,
    private val s3UploadManager: S3UploadManager = S3UploadManager(),
) : ViewModel() {

    companion object {
        private const val TAG = "ArtistEditSong"
    }

    private val _uiState = MutableStateFlow(ArtistEditSongUiState())
    val uiState: StateFlow<ArtistEditSongUiState> = _uiState.asStateFlow()

    private val _saveSuccess = MutableSharedFlow<Unit>()
    val saveSuccess = _saveSuccess.asSharedFlow()

    fun load(songId: String) {
        android.util.Log.d(TAG, "load() songId=$songId")
        viewModelScope.launch {
            _uiState.value = _uiState.value.copy(isLoading = true, errorMessage = null)
            val optionsResult = repository.getSongFormOptions()
            val songResult = repository.getSongForEdit(songId)

            if (optionsResult.isFailure) {
                val msg = optionsResult.exceptionOrNull()?.message ?: "Failed to load categories"
                android.util.Log.e(TAG, "load() form options failed: $msg")
                _uiState.value = _uiState.value.copy(isLoading = false, errorMessage = msg)
                return@launch
            }

            if (songResult.isFailure) {
                val msg = songResult.exceptionOrNull()?.message ?: "Failed to load song"
                android.util.Log.e(TAG, "load() song fetch failed: $msg")
                _uiState.value = _uiState.value.copy(isLoading = false, errorMessage = msg)
                return@launch
            }

            val (genres, tags) = optionsResult.getOrNull() ?: (emptyList<MbGenreDto>() to emptyList())
            val song = songResult.getOrNull()
            android.util.Log.d(
                TAG,
                "load() success songId=${song?.id} albumId=${song?.albumId} categoryId=${song?.categoryId} " +
                    "genres=${genres.size} tags=${tags.size}"
            )
            _uiState.value = _uiState.value.copy(
                isLoading = false,
                song = song,
                genres = genres,
                tags = tags,
            )
        }
    }

    fun save(
        context: Context,
        songId: String,
        title: String,
        categoryId: Int,
        tagId: Int?,
        lyrics: String,
        coverUri: Uri?,
    ) {
        android.util.Log.d(
            TAG,
            "save() called songId=$songId title='$title' categoryId=$categoryId tagId=$tagId " +
                "hasNewCover=${coverUri != null} lyricsLen=${lyrics.length}"
        )
        viewModelScope.launch {
            val current = _uiState.value.song
            if (current == null) {
                android.util.Log.w(TAG, "save() aborted: song not loaded")
                _uiState.value = _uiState.value.copy(
                    errorMessage = "Song details are not loaded yet. Pull back and try again.",
                )
                return@launch
            }
            val albumId = current.albumId
            if (albumId == null || albumId <= 0) {
                android.util.Log.w(TAG, "save() aborted: missing album_id on song id=${current.id}")
                _uiState.value = _uiState.value.copy(errorMessage = "Song album is missing. Cannot save changes.")
                return@launch
            }

            _uiState.value = _uiState.value.copy(isSaving = true, errorMessage = null)
            android.util.Log.d(
                TAG,
                "save() starting albumId=$albumId duration=${current.durationSeconds} " +
                    "track=${current.trackNumber} existingCoverKey=${current.coverKey}"
            )

            try {
                var coverKey = current.coverKey
                if (coverUri != null) {
                    android.util.Log.d(TAG, "save() compressing new cover uri=$coverUri")
                    val compressed = withContext(Dispatchers.IO) { compressImage(context, coverUri) }
                    if (compressed == null) {
                        android.util.Log.e(TAG, "save() cover compress failed")
                        _uiState.value = _uiState.value.copy(
                            isSaving = false,
                            errorMessage = "Failed to process cover image",
                        )
                        return@launch
                    }
                    android.util.Log.d(TAG, "save() cover compressed size=${compressed.length()} name=${compressed.name}")

                    val presigned = repository.getPresignedUrl(
                        purpose = "song_cover",
                        fileName = compressed.name,
                        contentType = "image/jpeg",
                        size = compressed.length(),
                    )
                    if (presigned.isFailure) {
                        val msg = presigned.exceptionOrNull()?.message ?: "Failed to prepare cover upload"
                        android.util.Log.e(TAG, "save() presign failed: $msg")
                        _uiState.value = _uiState.value.copy(isSaving = false, errorMessage = msg)
                        return@launch
                    }

                    val presignedData = presigned.getOrThrow()
                    android.util.Log.d(TAG, "save() presign ok key=${presignedData.key}")
                    val uploaded = s3UploadManager.uploadToS3(
                        uploadUrl = presignedData.uploadUrl,
                        file = compressed,
                        contentType = "image/jpeg",
                        headers = presignedData.headers,
                    )
                    if (!uploaded) {
                        android.util.Log.e(TAG, "save() S3 upload failed")
                        _uiState.value = _uiState.value.copy(
                            isSaving = false,
                            errorMessage = "Failed to upload cover image",
                        )
                        return@launch
                    }
                    coverKey = presignedData.key
                    android.util.Log.d(TAG, "save() cover uploaded key=$coverKey")
                }

                val artistName = current.artistName
                    ?: current.artist?.name?.takeIf { it.isNotBlank() && it != "Unknown" }
                    ?: repository.getLoggedInArtistDisplayName()

                val request = UpdateSongRequest(
                    albumId = albumId,
                    title = title.trim(),
                    artist = artistName?.takeIf { it.isNotBlank() },
                    duration = current.durationSeconds ?: 1,
                    trackNumber = current.trackNumber ?: 1,
                    lyrics = lyrics.trim().ifBlank { null },
                    categoryId = categoryId.toString(),
                    tagId = tagId,
                    coverKey = coverKey,
                    originalKey = current.originalKey,
                )

                android.util.Log.d(TAG, "save() calling PUT /api/songs/$songId")
                val result = repository.updateSongForEdit(songId, request)
                if (result.isSuccess) {
                    android.util.Log.d(TAG, "save() success")
                    repository.getMySongs()
                    _uiState.value = _uiState.value.copy(isSaving = false)
                    _saveSuccess.emit(Unit)
                } else {
                    val msg = result.exceptionOrNull()?.message ?: "Failed to update song"
                    android.util.Log.e(TAG, "save() failed: $msg")
                    _uiState.value = _uiState.value.copy(isSaving = false, errorMessage = msg)
                }
            } catch (e: Exception) {
                android.util.Log.e(TAG, "save() exception", e)
                _uiState.value = _uiState.value.copy(
                    isSaving = false,
                    errorMessage = e.message ?: "Failed to update song",
                )
            }
        }
    }

    fun clearError() {
        _uiState.value = _uiState.value.copy(errorMessage = null)
    }

    private suspend fun compressImage(context: Context, uri: Uri): File? = withContext(Dispatchers.IO) {
        try {
            val inputStream = context.contentResolver.openInputStream(uri) ?: return@withContext null
            val bitmap = BitmapFactory.decodeStream(inputStream)
            inputStream.close()
            if (bitmap == null) return@withContext null

            val maxSize = 1200
            val width = bitmap.width
            val height = bitmap.height
            val resized = if (width > maxSize || height > maxSize) {
                val ratio = width.toFloat() / height.toFloat()
                val newWidth = if (width > height) maxSize else (maxSize * ratio).toInt()
                val newHeight = if (height > width) maxSize else (maxSize / ratio).toInt()
                android.graphics.Bitmap.createScaledBitmap(bitmap, newWidth, newHeight, true)
            } else {
                bitmap
            }

            val file = File(context.cacheDir, "song_cover_${System.currentTimeMillis()}.jpg")
            FileOutputStream(file).use { output ->
                resized.compress(android.graphics.Bitmap.CompressFormat.JPEG, 80, output)
            }
            if (resized !== bitmap) resized.recycle()
            bitmap.recycle()
            file
        } catch (_: Exception) {
            null
        }
    }
}

class ArtistEditSongViewModelFactory(private val context: Context) : ViewModelProvider.Factory {
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        if (modelClass.isAssignableFrom(ArtistEditSongViewModel::class.java)) {
            val tokenStorage = TokenStorage.getInstance(context.applicationContext)
            val apiService = RetrofitClient.getMusicService(tokenStorage)
            val repository = ArtistRepository(apiService, tokenStorage)
            @Suppress("UNCHECKED_CAST")
            return ArtistEditSongViewModel(repository) as T
        }
        throw IllegalArgumentException("Unknown ViewModel class")
    }
}
