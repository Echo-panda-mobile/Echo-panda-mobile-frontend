package com.example.echo_panda_mobile.presentation.viewsmodel

import android.content.Context
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.example.echo_panda_mobile.data.model.User
import com.example.echo_panda_mobile.data.remote.RetrofitClient
import com.example.echo_panda_mobile.data.repository.ArtistRepository
import com.example.echo_panda_mobile.data.repository.AuthRepository
import com.example.echo_panda_mobile.data.repository.AuthResult
import com.example.echo_panda_mobile.data.repository.TokenStorage
import com.example.echo_panda_mobile.util.S3UploadManager
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.io.File
import java.io.FileOutputStream
import android.net.Uri

data class ArtistProfileUiState(
    val isLoading: Boolean = true,
    val user: User? = null,
    val bio: String = "Independent artist sharing music on Echo Panda.",
    val followers: String = "0",
    val monthlyListeners: String = "0",
    val errorMessage: String? = null,
    val isUpdating: Boolean = false
)

class ArtistProfileViewModel(
    private val authRepository: AuthRepository,
    private val artistRepository: ArtistRepository? = null,
    private val tokenStorage: TokenStorage? = null,
    private val s3UploadManager: S3UploadManager = S3UploadManager()
) : ViewModel() {

    private val _uiState = MutableStateFlow(ArtistProfileUiState())
    val uiState: StateFlow<ArtistProfileUiState> = _uiState.asStateFlow()

    private val _updateSuccess = kotlinx.coroutines.flow.MutableSharedFlow<Unit>()
    val updateSuccess = _updateSuccess.asSharedFlow()

    init {
        loadProfile()
    }

    fun loadProfile() {
        viewModelScope.launch {
            _uiState.value = _uiState.value.copy(isLoading = true)
            try {
                val user = authRepository.getCachedUser()
                    ?: authRepository.getCurrentUserProfile()
                _uiState.value = _uiState.value.copy(
                    isLoading = false,
                    isUpdating = false,
                    user = user,
                    bio = currentBio,
                    errorMessage = if (user == null) "Failed to load profile" else null
                )
            } catch (e: Exception) {
                _uiState.value = _uiState.value.copy(
                    isLoading = false,
                    isUpdating = false,
                    errorMessage = e.message ?: "An unexpected error occurred"
                )
            }
        }
    }

    fun updateProfile(name: String, bio: String) {
        viewModelScope.launch {
            _uiState.value = _uiState.value.copy(isUpdating = true)
            val currentUser = uiState.value.user ?: return@launch
            
            // 1. Update Base User Profile
            val result = authRepository.updateUserProfile(
                name = name,
                email = currentUser.email,
                role = "artist",
                photoUrl = currentUser.photoUrl
            )

            // 2. Update Artist Specific Table (bio and name)
            val artistId = currentUser.artistId ?: tokenStorage?.getArtistId()?.takeIf { it != -1 }
            if (artistId != null && artistRepository != null) {
                artistRepository.updateArtistProfile(artistId, name, currentUser.photoUrl, bio)
            }
            
            when (result) {
                is AuthResult.Success -> {
                    _uiState.value = _uiState.value.copy(bio = bio, isUpdating = false)
                    _updateSuccess.emit(Unit)
                    loadProfile()
                }
                is AuthResult.Error -> {
                    _uiState.value = _uiState.value.copy(
                        isUpdating = false,
                        errorMessage = result.message
                    )
                }
                else -> Unit
            }
        }
    }

    fun updateProfileImage(context: Context, uri: Uri) {
        viewModelScope.launch {
            _uiState.value = _uiState.value.copy(isUpdating = true)
            val currentUser = uiState.value.user ?: return@launch
            
            try {
                // 1. Compress image
                val compressedFile = withContext(Dispatchers.IO) { compressImage(context, uri) }
                if (compressedFile == null) {
                    _uiState.value = _uiState.value.copy(isUpdating = false, errorMessage = "Failed to process image")
                    return@launch
                }

                var finalPhotoUrl = uri.toString()

                // 2. Upload to S3 if repository is available
                if (artistRepository != null) {
                    val presignedResult = artistRepository.getPresignedUrl(
                        purpose = "artist_profile",
                        fileName = compressedFile.name,
                        contentType = "image/jpeg",
                        size = compressedFile.length()
                    )

                    if (presignedResult.isSuccess) {
                        val presignedData = presignedResult.getOrThrow()
                        val uploadSuccess = s3UploadManager.uploadToS3(
                            uploadUrl = presignedData.uploadUrl,
                            file = compressedFile,
                            contentType = "image/jpeg",
                            headers = presignedData.headers
                        )

                        if (uploadSuccess) {
                            // Use the key returned by the backend
                            finalPhotoUrl = presignedData.key
                        }
                    }
                }

                // 3. Update profile with the new URL/Key
                val result = authRepository.updateUserProfile(
                    name = currentUser.name,
                    email = currentUser.email,
                    role = "artist",
                    photoUrl = finalPhotoUrl
                )
                
                // 4. Update Artist Table (cover_image_url)
                val artistId = currentUser.artistId ?: tokenStorage?.getArtistId()?.takeIf { it != -1 }
                if (artistId != null && artistRepository != null) {
                    artistRepository.updateArtistProfile(artistId, currentUser.name, finalPhotoUrl, uiState.value.bio)
                }

                when (result) {
                    is AuthResult.Success -> {
                        loadProfile()
                    }
                    is AuthResult.Error -> {
                        _uiState.value = _uiState.value.copy(
                            isUpdating = false,
                            errorMessage = result.message
                        )
                    }
                    else -> {
                        _uiState.value = _uiState.value.copy(isUpdating = false)
                    }
                }
            } catch (e: Exception) {
                _uiState.value = _uiState.value.copy(isUpdating = false, errorMessage = e.message)
            }
        }
    }

    private suspend fun compressImage(context: Context, uri: Uri): File? = withContext(Dispatchers.IO) {
        try {
            val inputStream = context.contentResolver.openInputStream(uri) ?: return@withContext null
            val bitmap = android.graphics.BitmapFactory.decodeStream(inputStream)
            inputStream.close()
            
            if (bitmap == null) return@withContext null

            // Resize to max 800px for profile
            val maxSize = 800
            val width = bitmap.width
            val height = bitmap.height
            val resizedBitmap = if (width > maxSize || height > maxSize) {
                val ratio = width.toFloat() / height.toFloat()
                val newWidth = if (width > height) maxSize else (maxSize * ratio).toInt()
                val newHeight = if (height > width) maxSize else (maxSize / ratio).toInt()
                android.graphics.Bitmap.createScaledBitmap(bitmap, newWidth, newHeight, true)
            } else {
                bitmap
            }

            val file = File(context.cacheDir, "profile_${System.currentTimeMillis()}.jpg")
            val outputStream = FileOutputStream(file)
            resizedBitmap.compress(android.graphics.Bitmap.CompressFormat.JPEG, 80, outputStream)
            outputStream.flush()
            outputStream.close()
            
            file
        } catch (e: Exception) {
            null
        }
    }
    
    fun clearError() {
        _uiState.value = _uiState.value.copy(errorMessage = null)
    }
}

class ArtistProfileViewModelFactory(private val context: Context) : ViewModelProvider.Factory {
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        if (modelClass.isAssignableFrom(ArtistProfileViewModel::class.java)) {
            val tokenStorage = TokenStorage.getInstance(context.applicationContext)
            val authRepository = AuthRepository(tokenStorage)
            val musicService = RetrofitClient.getMusicService(tokenStorage)
            val artistRepository = ArtistRepository(musicService, tokenStorage)
            @Suppress("UNCHECKED_CAST")
            return ArtistProfileViewModel(authRepository, artistRepository, tokenStorage) as T
        }
        throw IllegalArgumentException("Unknown ViewModel class")
    }
}
