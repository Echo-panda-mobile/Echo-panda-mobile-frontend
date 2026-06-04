package com.example.echo_panda_mobile.presentation.viewsmodel

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.example.echo_panda_mobile.data.model.Album
import com.example.echo_panda_mobile.data.model.Track
import com.example.echo_panda_mobile.data.repository.AdminModerationAction
import com.example.echo_panda_mobile.data.repository.AdminRepository
import com.example.echo_panda_mobile.data.repository.AdminResult
import com.example.echo_panda_mobile.data.repository.MusicRepository
import com.example.echo_panda_mobile.data.repository.MusicResult
import com.example.echo_panda_mobile.data.repository.TokenStorage
import com.example.echo_panda_mobile.data.remote.RetrofitClient
import kotlinx.coroutines.async
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

data class AdminMusicUiState(
    val isLoading: Boolean = true,
    val errorMessage: String? = null,
    val songs: List<Track> = emptyList(),
    val albums: List<Album> = emptyList()
)

class AdminMusicViewModel(application: Application) : AndroidViewModel(application) {
    private val tokenStorage = TokenStorage(application)
    private val musicRepository = MusicRepository(RetrofitClient.getMusicService(tokenStorage))
    private val adminRepository = AdminRepository(tokenStorage)

    private val _uiState = MutableStateFlow(AdminMusicUiState())
    val uiState: StateFlow<AdminMusicUiState> = _uiState.asStateFlow()

    init {
        refresh()
    }

    fun refresh() {
        viewModelScope.launch {
            _uiState.value = _uiState.value.copy(isLoading = true, errorMessage = null)

            val songsDeferred = async { musicRepository.getAllSongs() }
            val albumsDeferred = async { musicRepository.getAllAlbums() }

            val songsResult = songsDeferred.await()
            val albumsResult = albumsDeferred.await()

            when {
                songsResult is MusicResult.Success && albumsResult is MusicResult.Success -> {
                    _uiState.value = AdminMusicUiState(
                        isLoading = false,
                        songs = songsResult.data,
                        albums = albumsResult.data
                    )
                }
                songsResult is MusicResult.Error -> {
                    _uiState.value = AdminMusicUiState(
                        isLoading = false,
                        errorMessage = songsResult.message
                    )
                }
                albumsResult is MusicResult.Error -> {
                    _uiState.value = AdminMusicUiState(
                        isLoading = false,
                        errorMessage = albumsResult.message
                    )
                }
            }
        }
    }

    fun approveSong(songId: String, onDone: (String?) -> Unit = {}) {
        viewModelScope.launch {
            when (val result = adminRepository.approveSong(songId)) {
                is AdminResult.Success -> onDone(null)
                is AdminResult.Error -> onDone(result.message)
            }
        }
    }

    fun hideSong(songId: String, onDone: (String?) -> Unit = {}) {
        viewModelScope.launch {
            when (val result = adminRepository.hideSong(songId)) {
                is AdminResult.Success -> onDone(null)
                is AdminResult.Error -> onDone(result.message)
            }
        }
    }

    fun reportSong(songId: String, onDone: (String?) -> Unit = {}) {
        viewModelScope.launch {
            when (val result = adminRepository.reportSong(songId, "Reported from admin panel")) {
                is AdminResult.Success -> onDone(null)
                is AdminResult.Error -> onDone(result.message)
            }
        }
    }

    fun approveAlbum(albumId: String, onDone: (String?) -> Unit = {}) {
        viewModelScope.launch {
            when (val result = adminRepository.approveAlbum(albumId)) {
                is AdminResult.Success -> onDone(null)
                is AdminResult.Error -> onDone(result.message)
            }
        }
    }

    fun hideAlbum(albumId: String, onDone: (String?) -> Unit = {}) {
        viewModelScope.launch {
            when (val result = adminRepository.hideAlbum(albumId)) {
                is AdminResult.Success -> onDone(null)
                is AdminResult.Error -> onDone(result.message)
            }
        }
    }

    fun reportAlbum(albumId: String, onDone: (String?) -> Unit = {}) {
        viewModelScope.launch {
            when (val result = adminRepository.reportAlbum(albumId, "Reported from admin panel")) {
                is AdminResult.Success -> onDone(null)
                is AdminResult.Error -> onDone(result.message)
            }
        }
    }
}
