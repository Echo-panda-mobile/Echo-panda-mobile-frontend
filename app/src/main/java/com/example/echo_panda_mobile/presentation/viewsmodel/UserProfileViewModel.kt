package com.example.echo_panda_mobile.presentation.viewsmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.echo_panda_mobile.data.model.User
import com.example.echo_panda_mobile.data.remote.RetrofitClient
import com.example.echo_panda_mobile.data.repository.AuthRepository
import com.example.echo_panda_mobile.data.repository.AuthResult
import com.example.echo_panda_mobile.data.repository.MusicRepository
import com.example.echo_panda_mobile.data.repository.MusicResult
import com.example.echo_panda_mobile.data.repository.TokenStorage
import kotlinx.coroutines.async
import kotlinx.coroutines.coroutineScope
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

data class UserProfileUiState(
    val isLoading: Boolean = true,
    val user: User? = null,
    val playlists: List<String> = emptyList(),
    val likedSongsCount: Int = 0,
    val errorMessage: String? = null
)

class UserProfileViewModel(
    private val authRepository: AuthRepository,
    private val musicRepository: MusicRepository
) : ViewModel() {

    private val _uiState = MutableStateFlow(UserProfileUiState())
    val uiState: StateFlow<UserProfileUiState> = _uiState.asStateFlow()

    init {
        loadUserProfile()
    }

    fun loadUserProfile() {
        viewModelScope.launch {
            _uiState.value = _uiState.value.copy(isLoading = true)
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
                    } else emptyList()

                    val favoritesCount = if (favoritesResult is MusicResult.Success) {
                        favoritesResult.data.size
                    } else 0

                    _uiState.value = _uiState.value.copy(
                        isLoading = false,
                        user = user,
                        playlists = playlistIds,
                        likedSongsCount = favoritesCount,
                        errorMessage = if (user == null) "Failed to load profile" else null
                    )
                }
            } catch (e: Exception) {
                _uiState.value = _uiState.value.copy(
                    isLoading = false,
                    errorMessage = e.message ?: "Failed to load profile"
                )
            }
        }
    }

    fun updateProfile(name: String, email: String) {
        viewModelScope.launch {
            val result = authRepository.updateUserProfile(name, email)
            if (result is AuthResult.Success) {
                loadUserProfile()
            } else if (result is AuthResult.Error) {
                _uiState.value = _uiState.value.copy(errorMessage = result.message)
            }
        }
    }

    fun logout(onComplete: () -> Unit) {
        viewModelScope.launch {
            authRepository.logout()
            onComplete()
        }
    }

    fun updateProfileImage(photoUrl: String) {
        viewModelScope.launch {
            _uiState.value = _uiState.value.copy(isLoading = true)
            val result = authRepository.updateUserProfile(
                name = uiState.value.user?.name ?: "",
                email = uiState.value.user?.email ?: "",
                photoUrl = photoUrl
            )
            if (result is AuthResult.Success) {
                loadUserProfile()
            } else if (result is AuthResult.Error) {
                _uiState.value = _uiState.value.copy(
                    isLoading = false,
                    errorMessage = result.message
                )
            }
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
