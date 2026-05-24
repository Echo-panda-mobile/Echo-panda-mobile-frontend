package com.example.echo_panda_mobile.presentation.viewsmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.echo_panda_mobile.data.model.User
import com.example.echo_panda_mobile.data.repository.AuthRepository
import com.example.echo_panda_mobile.data.repository.AuthResult
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

data class UserProfileUiState(
    val isLoading: Boolean = true,
    val user: User? = null,
    val playlists: List<String> = emptyList(),
    val likedSongsCount: Int = 0,
    val followingCount: Int = 0, // Placeholder for future feature
    val errorMessage: String? = null
)

class UserProfileViewModel(
    private val authRepository: AuthRepository = AuthRepository()
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
                val user = authRepository.getCurrentUser()
                val playlists = authRepository.getUserPlaylists()
                val likedSongs = authRepository.getUserLikedSongs()
                
                _uiState.value = _uiState.value.copy(
                    isLoading = false,
                    user = user,
                    playlists = playlists,
                    likedSongsCount = likedSongs.size,
                    followingCount = 0 // Update this when following logic is implemented
                )
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
}
