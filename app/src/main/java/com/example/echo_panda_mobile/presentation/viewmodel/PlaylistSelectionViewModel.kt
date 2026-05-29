package com.example.echo_panda_mobile.presentation.viewmodel

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.example.echo_panda_mobile.data.model.Playlist
import com.example.echo_panda_mobile.data.remote.RetrofitClient
import com.example.echo_panda_mobile.data.repository.MusicRepository
import com.example.echo_panda_mobile.data.repository.MusicResult
import com.example.echo_panda_mobile.data.repository.TokenStorage
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

data class PlaylistSelectionUiState(
    val isLoading: Boolean = false,
    val playlists: List<Playlist> = emptyList(),
    val errorMessage: String? = null,
    val successMessage: String? = null,
    val isActionInProgress: Boolean = false,
    val showCreateInput: Boolean = false,
    val dismissTrigger: Boolean = false
)

class PlaylistSelectionViewModel(application: Application) : AndroidViewModel(application) {
    private val tokenStorage = TokenStorage(application)
    private val musicRepository = MusicRepository(RetrofitClient.getMusicService(tokenStorage))

    private val _uiState = MutableStateFlow(PlaylistSelectionUiState())
    val uiState: StateFlow<PlaylistSelectionUiState> = _uiState.asStateFlow()

    fun loadPlaylists() {
        viewModelScope.launch {
            _uiState.update { it.copy(isLoading = true, errorMessage = null) }
            when (val result = musicRepository.getPlaylists(perPage = 50)) {
                is MusicResult.Success -> {
                    _uiState.update { it.copy(isLoading = false, playlists = result.data) }
                }
                is MusicResult.Error -> {
                    _uiState.update { it.copy(isLoading = false, errorMessage = result.message) }
                }
                else -> {}
            }
        }
    }

    fun addSongToPlaylist(trackId: String, playlistId: String) {
        viewModelScope.launch {
            _uiState.update { it.copy(isActionInProgress = true, errorMessage = null, successMessage = null) }
            when (val result = musicRepository.addToPlaylist(trackId, playlistId)) {
                is MusicResult.Success -> {
                    _uiState.update { 
                        it.copy(
                            isActionInProgress = false, 
                            successMessage = "Song added to playlist",
                            dismissTrigger = true 
                        ) 
                    }
                }
                is MusicResult.Error -> {
                    val errorMsg = parseErrorCode(result.message)
                    _uiState.update { it.copy(isActionInProgress = false, errorMessage = errorMsg) }
                }
                else -> {}
            }
        }
    }

    fun createAndAdd(trackId: String, playlistName: String) {
        if (playlistName.isBlank()) return
        
        viewModelScope.launch {
            _uiState.update { it.copy(isActionInProgress = true, errorMessage = null, successMessage = null) }
            
            // 1. Create Playlist
            when (val createResult = musicRepository.createPlaylist(playlistName)) {
                is MusicResult.Success -> {
                    val newPlaylistId = createResult.data.id
                    
                    // 2. Add Song (Inlined logic)
                    when (val addResult = musicRepository.addToPlaylist(trackId, newPlaylistId)) {
                        is MusicResult.Success -> {
                            _uiState.update { 
                                it.copy(
                                    isActionInProgress = false, 
                                    successMessage = "Song added to new playlist",
                                    dismissTrigger = true 
                                ) 
                            }
                        }
                        is MusicResult.Error -> {
                            val errorMsg = parseErrorCode(addResult.message)
                            _uiState.update { it.copy(isActionInProgress = false, errorMessage = errorMsg) }
                        }
                        else -> {
                            _uiState.update { it.copy(isActionInProgress = false) }
                        }
                    }
                }
                is MusicResult.Error -> {
                    val errorMsg = parseErrorCode(createResult.message)
                    _uiState.update { it.copy(isActionInProgress = false, errorMessage = errorMsg) }
                }
                else -> {
                    _uiState.update { it.copy(isActionInProgress = false) }
                }
            }
        }
    }

    private fun parseErrorCode(message: String): String {
        return when {
            message.contains("409") -> "Song already in playlist"
            message.contains("401") -> "Unauthenticated. Please login again."
            message.contains("403") -> "Forbidden action."
            message.contains("422") -> "Invalid data provided."
            else -> message
        }
    }

    fun toggleCreateInput(show: Boolean) {
        _uiState.update { it.copy(showCreateInput = show) }
    }
    
    fun clearMessages() {
        _uiState.update { it.copy(errorMessage = null, successMessage = null) }
    }

    fun resetDismissTrigger() {
        _uiState.update { it.copy(dismissTrigger = false) }
    }
}
