package com.example.echo_panda_mobile.presentation.viewsmodel

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.example.echo_panda_mobile.data.model.Album
import com.example.echo_panda_mobile.data.model.Track
import com.example.echo_panda_mobile.data.remote.BackendUser
import com.example.echo_panda_mobile.data.remote.RetrofitClient
import com.example.echo_panda_mobile.data.repository.AdminRepository
import com.example.echo_panda_mobile.data.repository.AdminResult
import com.example.echo_panda_mobile.data.repository.MusicRepository
import com.example.echo_panda_mobile.data.repository.MusicResult
import com.example.echo_panda_mobile.data.repository.TokenStorage
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

data class AdminUserDetailUiState(
    val isLoading: Boolean = true,
    val errorMessage: String? = null,
    val user: BackendUser? = null,
    val profileImageUrl: String? = null,
    val albums: List<Album> = emptyList(),
    val songs: List<Track> = emptyList(),
    val isArtistContentLoading: Boolean = false
)

class AdminUserDetailViewModel(
    application: Application,
    private val userId: String,
    private val role: String
) : AndroidViewModel(application) {
    private val tokenStorage = TokenStorage(application)
    private val repository = AdminRepository(tokenStorage)
    private val musicRepository = MusicRepository(RetrofitClient.getMusicService(tokenStorage))

    private val _uiState = MutableStateFlow(AdminUserDetailUiState())
    val uiState: StateFlow<AdminUserDetailUiState> = _uiState.asStateFlow()

    init {
        loadUser()
    }

    private fun loadUser() {
        viewModelScope.launch {
            when (val result = repository.findUserByIdAndRole(userId, role)) {
                is AdminResult.Success -> {
                    if (result.data != null) {
                        val user = result.data
                        val profileImageUrl = resolveProfileImageUrl(user)
                        _uiState.value = AdminUserDetailUiState(
                            isLoading = false,
                            user = user,
                            profileImageUrl = profileImageUrl
                        )
                        if (user.role.trim().lowercase() == "artist" || user.artist != null) {
                            loadArtistContent(user.artist?.name ?: user.name)
                        }
                    } else {
                        _uiState.value = AdminUserDetailUiState(
                            isLoading = false,
                            errorMessage = "No matching $role record was found."
                        )
                    }
                }
                is AdminResult.Error -> {
                    _uiState.value = AdminUserDetailUiState(
                        isLoading = false,
                        errorMessage = result.message
                    )
                }
            }
        }
    }

    private suspend fun resolveProfileImageUrl(user: BackendUser): String? {
        val catalogByName = musicRepository.fetchArtistCatalogByName()
        return musicRepository.resolveArtistProfileImageForUser(
            userName = user.name,
            linkedArtist = user.artist,
            catalogByName = catalogByName
        )
    }

    private fun loadArtistContent(artistName: String) {
        viewModelScope.launch {
            _uiState.value = _uiState.value.copy(isArtistContentLoading = true)
            
            val albumsResult = musicRepository.getArtistAlbums(artistName)
            val songsResult = musicRepository.getArtistSongs(artistName)

            val albums = if (albumsResult is MusicResult.Success) albumsResult.data else emptyList()
            val songs = if (songsResult is MusicResult.Success) songsResult.data else emptyList()

            _uiState.value = _uiState.value.copy(
                albums = albums,
                songs = songs,
                isArtistContentLoading = false
            )
        }
    }
}

class AdminUserDetailViewModelFactory(
    private val application: Application,
    private val userId: String,
    private val role: String
) : ViewModelProvider.Factory {
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        if (modelClass.isAssignableFrom(AdminUserDetailViewModel::class.java)) {
            @Suppress("UNCHECKED_CAST")
            return AdminUserDetailViewModel(application, userId, role) as T
        }
        throw IllegalArgumentException("Unknown ViewModel class")
    }
}