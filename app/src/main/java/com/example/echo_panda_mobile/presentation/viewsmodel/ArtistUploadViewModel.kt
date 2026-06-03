package com.example.echo_panda_mobile.presentation.viewsmodel

import android.content.Context
import android.net.Uri
import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.Intent
import android.os.Build
import androidx.core.app.NotificationCompat
import androidx.lifecycle.ViewModel
import com.example.echo_panda_mobile.MainActivity
import com.example.echo_panda_mobile.R
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.example.echo_panda_mobile.data.remote.AlbumDto
import com.example.echo_panda_mobile.data.remote.RetrofitClient
import com.example.echo_panda_mobile.data.remote.PresignedUrlResponse
import com.example.echo_panda_mobile.data.repository.ArtistRepository
import com.example.echo_panda_mobile.data.repository.TokenStorage
import com.example.echo_panda_mobile.util.AudioMetadataExtractor
import com.example.echo_panda_mobile.util.S3UploadManager
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.io.File
import java.io.FileOutputStream
import java.util.Locale

class ArtistUploadViewModel(
    private val repository: ArtistRepository,
    private val s3UploadManager: S3UploadManager = S3UploadManager()
) : ViewModel() {

    private val _uploadState = MutableStateFlow<UploadUiState>(UploadUiState.Idle)
    val uploadState: StateFlow<UploadUiState> = _uploadState.asStateFlow()

    private val _uploadProgress = MutableStateFlow(0)
    val uploadProgress: StateFlow<Int> = _uploadProgress.asStateFlow()

    private val _currentUploadingFile = MutableStateFlow<String?>(null)
    val currentUploadingFile: StateFlow<String?> = _currentUploadingFile.asStateFlow()

    sealed class UploadUiState {
        object Idle : UploadUiState()
        data class Uploading(val message: String) : UploadUiState()
        data class Success(val message: String) : UploadUiState()
        data class Error(val message: String) : UploadUiState()
    }

    val albums: StateFlow<List<AlbumDto>> = repository.albums

    private val _isAlbumsLoading = MutableStateFlow(false)
    val isAlbumsLoading: StateFlow<Boolean> = _isAlbumsLoading.asStateFlow()

    init {
        loadMyAlbums()
    }

    fun loadMyAlbums() {
        viewModelScope.launch {
            _isAlbumsLoading.value = true
            repository.getMyAlbums() // This updates the repository.albums flow
            _isAlbumsLoading.value = false
        }
    }

    fun resetState() {
        _uploadState.value = UploadUiState.Idle
        _uploadProgress.value = 0
        _currentUploadingFile.value = null
    }

    fun uploadSong(
        context: Context,
        title: String,
        albumId: Int?,
        genre: String?,
        lyrics: String?,
        audioUri: Uri?,
        coverUri: Uri?,
        trackNumber: Int = 1
    ) {
        viewModelScope.launch {
            // Validate inputs first
            if (title.isBlank()) {
                _uploadState.value = UploadUiState.Error("Song title is required")
                return@launch
            }
            
            if (audioUri == null) {
                _uploadState.value = UploadUiState.Error("Audio file is required - please select a music file")
                return@launch
            }
            
            _uploadState.value = UploadUiState.Uploading("Preparing upload...")
            _uploadProgress.value = 0
            
            try {
                // 1. Convert URIs to Files on background thread
                _currentUploadingFile.value = "Processing files..."
                val audioFile = withContext(Dispatchers.IO) { copyUriToFile(context, audioUri, "audio") }
                
                // Compress image if provided
                val coverFile = coverUri?.let { withContext(Dispatchers.IO) { compressImage(context, it) } }
                
                if (audioFile == null || !audioFile.exists()) {
                    _uploadState.value = UploadUiState.Error("Failed to process audio file.")
                    return@launch
                }

                // 2. File size validation
                val audioSizeMB = AudioMetadataExtractor.getFileSizeInMB(audioFile)
                android.util.Log.d("ArtistUploadViewModel", "Audio file size: ${String.format(Locale.getDefault(), "%.2f", audioSizeMB)} MB")
                
                if (audioSizeMB > 100.0) {
                    _uploadState.value = UploadUiState.Error("Audio file is too large (${String.format(Locale.getDefault(), "%.1f", audioSizeMB)} MB). Max allowed size is 100MB.")
                    return@launch
                }

                var audioKey: String? = null
                var coverKey: String? = null
                var duration = 0

                // 3. Get Presigned URL and Upload Audio DIRECTLY to S3
                _currentUploadingFile.value = "Requesting secure upload channel..."
                val audioUrlResult = repository.getPresignedUrl("song_audio", audioFile.name, "audio/mpeg", audioFile.length())
                
                if (audioUrlResult.isSuccess) {
                    val presignedData = audioUrlResult.getOrThrow()
                    audioKey = presignedData.key
                    
                    _currentUploadingFile.value = "Uploading audio directly to S3..."
                    val uploadSuccess = s3UploadManager.uploadToS3(
                        uploadUrl = presignedData.uploadUrl,
                        file = audioFile,
                        contentType = "audio/mpeg",
                        headers = presignedData.headers,
                        onProgress = { _uploadProgress.value = (it * 0.7).toInt() }
                    )
                    
                    if (!uploadSuccess) {
                        _uploadState.value = UploadUiState.Error("Failed to upload audio file to storage.")
                        return@launch
                    }
                    
                    // Extract duration
                    duration = AudioMetadataExtractor.getAudioDuration(audioFile)
                } else {
                    val errorMsg = audioUrlResult.exceptionOrNull()?.message ?: "Unknown error"
                    _uploadState.value = UploadUiState.Error("Failed to prepare audio upload: $errorMsg")
                    return@launch
                }

                // 4. Upload cover if exists
                coverFile?.let {
                    _currentUploadingFile.value = "Uploading cover image..."
                    val coverUrlResult = repository.getPresignedUrl("album_cover", it.name, "image/jpeg", it.length())
                    
                    if (coverUrlResult.isSuccess) {
                        val presignedData = coverUrlResult.getOrThrow()
                        coverKey = presignedData.key
                        
                        val uploadSuccess = s3UploadManager.uploadToS3(
                            uploadUrl = presignedData.uploadUrl,
                            file = it,
                            contentType = "image/jpeg",
                            headers = presignedData.headers,
                            onProgress = { _uploadProgress.value = 70 + (it * 0.2).toInt() }
                        )
                        
                        if (!uploadSuccess) {
                            android.util.Log.w("ArtistUploadViewModel", "⚠️ Cover image upload failed")
                        }
                    }
                }

                // 5. Create song record in database
                _currentUploadingFile.value = "Saving song to database..."
                val result = repository.uploadSong(
                    albumId = albumId,
                    title = title.trim(),
                    duration = duration,
                    trackNumber = trackNumber,
                    genre = genre?.trim(),
                    lyrics = lyrics?.trim(),
                    audioKey = audioKey,
                    coverKey = coverKey
                )

                if (result.isSuccess) {
                    _uploadProgress.value = 100
                    _uploadState.value = UploadUiState.Success("Song uploaded and saved successfully!")
                    
                    // Show a local notification for the artist that their upload is live
                    showUploadSuccessNotification(context, title)

                    loadMyAlbums()
                    // Cleanup cache files
                    withContext(Dispatchers.IO) {
                        audioFile.delete()
                        coverFile?.delete()
                    }
                } else {
                    _uploadState.value = UploadUiState.Error(
                        "Database save failed: ${result.exceptionOrNull()?.message ?: "Unknown error"}"
                    )
                }
            } catch (e: Exception) {
                android.util.Log.e("ArtistUploadViewModel", "Upload error: ${e.message}", e)
                _uploadState.value = UploadUiState.Error("Upload error: ${e.message ?: "Unknown error occurred"}")
            } finally {
                _currentUploadingFile.value = null
            }
        }
    }

    private fun copyUriToFile(context: Context, uri: Uri, prefix: String): File? {
        return try {
            val inputStream = context.contentResolver.openInputStream(uri) ?: return null
            val mimeType = context.contentResolver.getType(uri)
            val extension = when {
                mimeType?.contains("audio/mpeg") == true -> "mp3"
                mimeType?.contains("audio/wav") == true -> "wav"
                mimeType?.contains("image/jpeg") == true -> "jpg"
                mimeType?.contains("image/png") == true -> "png"
                else -> mimeType?.split("/")?.lastOrNull() ?: "bin"
            }
            val file = File(context.cacheDir, "${prefix}_${System.currentTimeMillis()}.$extension")
            val outputStream = FileOutputStream(file)
            inputStream.use { input ->
                outputStream.use { output ->
                    input.copyTo(output)
                }
            }
            file
        } catch (e: Exception) {
            android.util.Log.e("ArtistUploadViewModel", "Error copying URI to file: ${e.message}")
            null
        }
    }

    /**
     * Compress and resize image before upload to save bandwidth and storage
     */
    private suspend fun compressImage(context: Context, uri: Uri): File? = withContext(Dispatchers.IO) {
        try {
            val inputStream = context.contentResolver.openInputStream(uri) ?: return@withContext null
            val options = android.graphics.BitmapFactory.Options().apply {
                inJustDecodeBounds = true
            }
            android.graphics.BitmapFactory.decodeStream(inputStream, null, options)
            inputStream.close()

            // Calculate sample size to load a smaller version into memory
            val maxSize = 1200
            var inSampleSize = 1
            if (options.outHeight > maxSize || options.outWidth > maxSize) {
                val halfHeight: Int = options.outHeight / 2
                val halfWidth: Int = options.outWidth / 2
                while (halfHeight / inSampleSize >= maxSize && halfWidth / inSampleSize >= maxSize) {
                    inSampleSize *= 2
                }
            }

            // Decode with inSampleSize
            val finalInputStream = context.contentResolver.openInputStream(uri) ?: return@withContext null
            val decodeOptions = android.graphics.BitmapFactory.Options().apply {
                inSampleSize = inSampleSize
            }
            val bitmap = android.graphics.BitmapFactory.decodeStream(finalInputStream, null, decodeOptions)
            finalInputStream.close()
            
            if (bitmap == null) return@withContext null

            val file = File(context.cacheDir, "compressed_${System.currentTimeMillis()}.jpg")
            val outputStream = FileOutputStream(file)
            bitmap.compress(android.graphics.Bitmap.CompressFormat.JPEG, 80, outputStream)
            outputStream.flush()
            outputStream.close()
            bitmap.recycle()
            
            file
        } catch (e: Exception) {
            android.util.Log.e("ArtistUploadViewModel", "Error compressing image: ${e.message}")
            null
        }
    }

    fun createAlbum(
        context: Context,
        title: String,
        artist: String,
        description: String?,
        coverUri: Uri?
    ) {
        viewModelScope.launch {
            _uploadState.value = UploadUiState.Uploading("Creating album...")
            _uploadProgress.value = 0
            try {
                android.util.Log.d("ArtistUploadViewModel", "━━━ CREATE ALBUM START ━━━")
                android.util.Log.d("ArtistUploadViewModel", "Title: $title, Artist: $artist")
                
                var coverKey: String? = null
                
                // 1. Process and compress cover image
                val coverFile = coverUri?.let { withContext(Dispatchers.IO) { compressImage(context, it) } }
                
                coverFile?.let {
                    _currentUploadingFile.value = "Uploading cover image..."
                    android.util.Log.d("ArtistUploadViewModel", "Uploading cover image via Presigned URL...")
                    val urlResult = repository.getPresignedUrl("album_cover", it.name, "image/jpeg", it.length())
                    
                    if (urlResult.isSuccess) {
                        val presignedData = urlResult.getOrThrow()
                        val uploadSuccess = s3UploadManager.uploadToS3(
                            uploadUrl = presignedData.uploadUrl,
                            file = it,
                            contentType = "image/jpeg",
                            headers = presignedData.headers,
                            onProgress = { _uploadProgress.value = (it * 0.5).toInt() }
                        )
                        
                        if (uploadSuccess) {
                            coverKey = presignedData.key
                            android.util.Log.d("ArtistUploadViewModel", "✓ Cover image uploaded: $coverKey")
                        } else {
                            android.util.Log.e("ArtistUploadViewModel", "❌ Cover upload failed")
                        }
                    } else {
                        android.util.Log.e("ArtistUploadViewModel", "❌ Failed to get cover upload URL")
                    }
                    _uploadProgress.value = 50
                }

                _currentUploadingFile.value = "Creating album record..."
                android.util.Log.d("ArtistUploadViewModel", "Calling repository.createAlbum...")
                
                val result = repository.createAlbum(
                    title = title,
                    artist = artist,
                    description = description,
                    coverKey = coverKey,
                    releaseDate = java.text.SimpleDateFormat("yyyy-MM-dd", java.util.Locale.getDefault()).format(java.util.Date())
                )
                
                if (result.isSuccess) {
                    android.util.Log.d("ArtistUploadViewModel", "✓ CREATE ALBUM SUCCESS")
                    _uploadProgress.value = 100
                    _uploadState.value = UploadUiState.Success("Album created successfully!")
                    loadMyAlbums()
                } else {
                    val errorMsg = result.exceptionOrNull()?.message ?: "Unknown error"
                    android.util.Log.e("ArtistUploadViewModel", "⚠️ CREATE ALBUM FAILED: $errorMsg")
                    _uploadState.value = UploadUiState.Error(errorMsg)
                }
            } catch (e: Exception) {
                android.util.Log.e("ArtistUploadViewModel", "Exception: ${e.message}", e)
                _uploadState.value = UploadUiState.Error(e.message ?: "Failed to create album")
            } finally {
                _currentUploadingFile.value = null
            }
        }
    }

    fun getAudioDuration(file: File): Int {
        return AudioMetadataExtractor.getAudioDuration(file)
    }

    private fun showUploadSuccessNotification(context: Context, songTitle: String) {
        val channelId = "upload_channel"
        val notificationManager = context.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val channel = NotificationChannel(channelId, "Uploads", NotificationManager.IMPORTANCE_HIGH)
            notificationManager.createNotificationChannel(channel)
        }

        val notification = NotificationCompat.Builder(context, channelId)
            .setSmallIcon(R.mipmap.ic_launcher)
            .setContentTitle("Echo-Panda: New Release!")
            .setContentText("Your song '$songTitle' is now live for everyone to hear.")
            .setPriority(NotificationCompat.PRIORITY_HIGH)
            .setAutoCancel(true)
            .build()

        notificationManager.notify(System.currentTimeMillis().toInt(), notification)
    }
}

class ArtistUploadViewModelFactory(private val context: Context) : ViewModelProvider.Factory {
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        if (modelClass.isAssignableFrom(ArtistUploadViewModel::class.java)) {
            val tokenStorage = TokenStorage.getInstance(context.applicationContext)
            val apiService = RetrofitClient.getMusicService(tokenStorage)
            val repository = ArtistRepository(apiService, tokenStorage)
            val s3Manager = S3UploadManager()
            @Suppress("UNCHECKED_CAST")
            return ArtistUploadViewModel(repository, s3Manager) as T
        }
        throw IllegalArgumentException("Unknown ViewModel class")
    }
}
