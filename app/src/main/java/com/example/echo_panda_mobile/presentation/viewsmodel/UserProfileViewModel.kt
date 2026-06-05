package com.example.echo_panda_mobile.presentation.viewsmodel

import android.content.Context
import android.graphics.BitmapFactory
import android.net.Uri
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.echo_panda_mobile.data.model.User
import com.example.echo_panda_mobile.data.remote.RetrofitClient
import com.example.echo_panda_mobile.data.repository.AuthRepository
import com.example.echo_panda_mobile.data.repository.AuthResult
import com.example.echo_panda_mobile.data.repository.MusicRepository
import com.example.echo_panda_mobile.data.repository.MusicResult
import com.example.echo_panda_mobile.data.repository.TokenStorage
import com.example.echo_panda_mobile.util.S3UploadManager
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.async
import kotlinx.coroutines.coroutineScope
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.io.File
import java.io.FileOutputStream

data class UserProfileUiState(
    val isLoading: Boolean = true,
    val isUpdating: Boolean = false,
    val user: User? = null,
    val playlists: List<String> = emptyList(),
    val likedSongsCount: Int = 0,
    val errorMessage: String? = null
)

class UserProfileViewModel(
    private val authRepository: AuthRepository,
    private val musicRepository: MusicRepository,
    private val s3UploadManager: S3UploadManager = S3UploadManager()
) : ViewModel() {

    private val _uiState = MutableStateFlow(UserProfileUiState())
    val uiState: StateFlow<UserProfileUiState> = _uiState.asStateFlow()

    init {
        loadUserProfile()
    }

    fun loadUserProfile() {
        viewModelScope.launch {
            _uiState.value = _uiState.value.copy(isLoading = true, errorMessage = null)
            try {
                coroutineScope {
                    val userDef = async { authRepository.getCurrentUserProfile() }
                    val playlistsDef = async { musicRepository.getPlaylists(perPage = 100) }
                    val favoritesDef = async { musicRepository.getFavoriteTracks() }

                    val user = userDef.await()
                    val playlistsResult = playlistsDef.await()
                    val favoritesResult = favoritesDef.await()

                    val playlistIds = if (playlistsResult is MusicResult.Success) {
                        playlistsResult.data.map { it.id }
                    } else {
                        emptyList()
                    }

                    val favoritesCount = if (favoritesResult is MusicResult.Success) {
                        favoritesResult.data.size
                    } else {
                        0
                    }

                    _uiState.value = _uiState.value.copy(
                        isLoading = false,
                        isUpdating = false,
                        user = user,
                        playlists = playlistIds,
                        likedSongsCount = favoritesCount,
                        errorMessage = if (user == null) "Failed to load profile" else null
                    )
                }
            } catch (e: Exception) {
                _uiState.value = _uiState.value.copy(
                    isLoading = false,
                    isUpdating = false,
                    errorMessage = e.message ?: "Failed to load profile"
                )
            }
        }
    }

    fun updateProfile(name: String, email: String) {
        viewModelScope.launch {
            _uiState.value = _uiState.value.copy(isUpdating = true, errorMessage = null)
            val result = authRepository.updateUserProfile(name, email)
            when (result) {
                is AuthResult.Success -> loadUserProfile()
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
            _uiState.value = _uiState.value.copy(isUpdating = true, errorMessage = null)
            val currentUser = uiState.value.user ?: return@launch

            try {
                val compressedFile = withContext(Dispatchers.IO) { compressImage(context, uri) }
                if (compressedFile == null) {
                    _uiState.value = _uiState.value.copy(
                        isUpdating = false,
                        errorMessage = "Failed to process image"
                    )
                    return@launch
                }

                val presignedResult = authRepository.getUserImagePresignedUrl(
                    fileName = compressedFile.name,
                    contentType = "image/jpeg",
                    size = compressedFile.length()
                )

                if (presignedResult.isFailure) {
                    _uiState.value = _uiState.value.copy(
                        isUpdating = false,
                        errorMessage = presignedResult.exceptionOrNull()?.message ?: "Failed to prepare upload"
                    )
                    return@launch
                }

                val presignedData = presignedResult.getOrThrow()
                val uploadSuccess = s3UploadManager.uploadToS3(
                    uploadUrl = presignedData.uploadUrl,
                    file = compressedFile,
                    contentType = "image/jpeg",
                    headers = presignedData.headers
                )

                if (!uploadSuccess) {
                    _uiState.value = _uiState.value.copy(
                        isUpdating = false,
                        errorMessage = "Failed to upload image"
                    )
                    return@launch
                }

                val result = authRepository.updateUserProfile(
                    name = currentUser.name,
                    email = currentUser.email,
                    photoUrl = presignedData.key
                )

                when (result) {
                    is AuthResult.Success -> loadUserProfile()
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
                _uiState.value = _uiState.value.copy(
                    isUpdating = false,
                    errorMessage = e.message ?: "Failed to update profile image"
                )
            }
        }
    }

    private suspend fun compressImage(context: Context, uri: Uri): File? = withContext(Dispatchers.IO) {
        try {
            val inputStream = context.contentResolver.openInputStream(uri) ?: return@withContext null
            val bitmap = BitmapFactory.decodeStream(inputStream)
            inputStream.close()

            if (bitmap == null) return@withContext null

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

            val file = File(context.cacheDir, "user_profile_${System.currentTimeMillis()}.jpg")
            FileOutputStream(file).use { outputStream ->
                resizedBitmap.compress(android.graphics.Bitmap.CompressFormat.JPEG, 80, outputStream)
            }

            file
        } catch (_: Exception) {
            null
        }
    }

    fun logout(onComplete: () -> Unit) {
        viewModelScope.launch {
            authRepository.logout()
            onComplete()
        }
    }
}

class UserProfileViewModelFactory(private val context: android.content.Context) : androidx.lifecycle.ViewModelProvider.Factory {
    override fun <T : androidx.lifecycle.ViewModel> create(modelClass: Class<T>): T {
        if (modelClass.isAssignableFrom(UserProfileViewModel::class.java)) {
            val tokenStorage = TokenStorage.getInstance(context.applicationContext)
            val authRepository = AuthRepository(tokenStorage)
            val musicRepository = MusicRepository(
                RetrofitClient.getMusicService(tokenStorage),
                tokenStorage
            )
            @Suppress("UNCHECKED_CAST")
            return UserProfileViewModel(authRepository, musicRepository) as T
        }
        throw IllegalArgumentException("Unknown ViewModel class")
    }
}
