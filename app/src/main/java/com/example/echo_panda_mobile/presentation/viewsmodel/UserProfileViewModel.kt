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
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

data class UserProfileUiState(
    val isLoading: Boolean = true,
    val user: User? = null,
    val playlistCount: Int = 0,
    val likedSongsCount: Int = 0,
    val errorMessage: String? = null
)

class UserProfileViewModel(application: Application) : AndroidViewModel(application) {

    private val tokenStorage = TokenStorage(application)
    private val authRepository = AuthRepository(tokenStorage)
    private val musicRepository = MusicRepository(RetrofitClient.getMusicService(tokenStorage))

    private val _uiState = MutableStateFlow(UserProfileUiState())
    val uiState: StateFlow<UserProfileUiState> = _uiState.asStateFlow()

    init {
        loadUserProfile()
    }

    fun loadUserProfile(forceRefreshUser: Boolean = false) {
        viewModelScope.launch {
            _uiState.update { it.copy(isLoading = true, errorMessage = null) }
            try {
                val userDef = async {
                    if (forceRefreshUser) {
                        authRepository.refreshCurrentUserProfile()
                    } else {
                        authRepository.getCachedUser()
                            ?: authRepository.getCurrentUserProfile()
                    }
                }
                val playlistsDef = async { musicRepository.getPlaylists(perPage = 100) }
                val favoritesDef = async { musicRepository.getFavoriteTracks() }

                val user = userDef.await()
                val playlistCount = when (val result = playlistsDef.await()) {
                    is MusicResult.Success -> result.data.size
                    else -> 0
                }
                val likedCount = when (val result = favoritesDef.await()) {
                    is MusicResult.Success -> result.data.size
                    else -> 0
                }

                _uiState.update {
                    it.copy(
                        isLoading = false,
                        user = user,
                        playlistCount = playlistCount,
                        likedSongsCount = likedCount,
                        errorMessage = if (user == null) "Could not load your profile." else null
                    )
                }
            } catch (e: Exception) {
                _uiState.update {
                    it.copy(
                        isLoading = false,
                        errorMessage = e.message ?: "Failed to load profile"
                    )
                }
            }
        }
    }

    fun updateProfile(name: String) {
        viewModelScope.launch {
            val email = _uiState.value.user?.email?.takeIf { it.isNotBlank() }
                ?: return@launch
            val result = authRepository.updateUserProfile(name, email)
            if (result is AuthResult.Success) {
                loadUserProfile(forceRefreshUser = true)
            } else if (result is AuthResult.Error) {
                _uiState.update { it.copy(errorMessage = result.message) }
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
            _uiState.update { it.copy(isLoading = true) }
            val result = authRepository.updateUserProfile(
                name = uiState.value.user?.name ?: "",
                email = uiState.value.user?.email ?: "",
                photoUrl = photoUrl
            )
            if (result is AuthResult.Success) {
                loadUserProfile(forceRefreshUser = true)
            } else if (result is AuthResult.Error) {
                _uiState.update {
                    it.copy(
                        isLoading = false,
                        errorMessage = result.message
                    )
                }
            }
        }
    }
}

class UserProfileViewModelFactory(private val context: android.content.Context) : androidx.lifecycle.ViewModelProvider.Factory {
    override fun <T : androidx.lifecycle.ViewModel> create(modelClass: Class<T>): T {
        if (modelClass.isAssignableFrom(UserProfileViewModel::class.java)) {
            val tokenStorage = TokenStorage.getInstance(context.applicationContext)
            val repository = AuthRepository(tokenStorage)
            @Suppress("UNCHECKED_CAST")
            return UserProfileViewModel(repository) as T
        }
        throw IllegalArgumentException("Unknown ViewModel class")
    }
}
