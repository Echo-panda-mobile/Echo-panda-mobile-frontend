package com.example.echo_panda_mobile.presentation.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.echo_panda_mobile.data.model.Playlist
import com.example.echo_panda_mobile.data.model.Track
import com.example.echo_panda_mobile.data.repository.MusicRepository
import com.example.echo_panda_mobile.data.repository.MusicResult
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

data class PlayerUiState(
    val isLoading: Boolean = true,
    val track: Track? = null,
    val errorMessage: String? = null,
    val isPlaying: Boolean = false,
    val progress: Float = 0.3f, // Mock progress
    val showPlaylistDialog: Boolean = false,
    val userPlaylists: List<Playlist> = emptyList()
)

class PlayerViewModel(
    private val musicRepository: MusicRepository = MusicRepository()
) : ViewModel() {

    private val _uiState = MutableStateFlow(PlayerUiState())
    val uiState: StateFlow<PlayerUiState> = _uiState.asStateFlow()

    fun loadTrack(trackId: String) {
        viewModelScope.launch {
            _uiState.update { it.copy(isLoading = true) }
            when (val result = musicRepository.getTrackById(trackId)) {
                is MusicResult.Success -> {
                    _uiState.update { it.copy(isLoading = false, track = result.data) }
                }
                is MusicResult.Error -> {
                    _uiState.update { it.copy(isLoading = false, errorMessage = result.message) }
                }
                else -> {
                    _uiState.update { it.copy(isLoading = false) }
                }
            }
        }
    }

    fun togglePlayPause() {
        _uiState.update { it.copy(isPlaying = !it.isPlaying) }
    }

    fun updateProgress(value: Float) {
        _uiState.update { it.copy(progress = value) }
    }

    fun toggleFavorite() {
        val currentTrack = _uiState.value.track ?: return
        viewModelScope.launch {
            // Optimistic update
            _uiState.update { it.copy(track = currentTrack.copy(isFavorite = !currentTrack.isFavorite)) }
            
            // Call repository
            val result = musicRepository.toggleFavorite(currentTrack.id)
            if (result is MusicResult.Error) {
                // Rollback if error
                _uiState.update { it.copy(track = currentTrack) }
            }
        }
    }

    fun downloadTrack() {
        val currentTrack = _uiState.value.track ?: return
        if (currentTrack.isDownloaded) return
        
        viewModelScope.launch {
            val result = musicRepository.downloadTrack(currentTrack.id)
            if (result is MusicResult.Success) {
                _uiState.update { it.copy(track = it.track?.copy(isDownloaded = true)) }
            }
        }
    }

    fun openPlaylistDialog() {
        viewModelScope.launch {
            _uiState.update { it.copy(showPlaylistDialog = true) }
            val playlists = musicRepository.getRecentPlaylists()
            if (playlists is MusicResult.Success) {
                _uiState.update { it.copy(userPlaylists = playlists.data) }
            }
        }
    }

    fun closePlaylistDialog() {
        _uiState.update { it.copy(showPlaylistDialog = false) }
    }

    fun addToPlaylist(playlistId: String) {
        val currentTrack = _uiState.value.track ?: return
        viewModelScope.launch {
            val result = musicRepository.addToPlaylist(currentTrack.id, playlistId)
            if (result is MusicResult.Success) {
                closePlaylistDialog()
            }
        }
    }
}
