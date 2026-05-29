package com.example.echo_panda_mobile.presentation.viewmodel

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.example.echo_panda_mobile.data.model.Album
import com.example.echo_panda_mobile.data.model.Track
import com.example.echo_panda_mobile.data.remote.RetrofitClient
import com.example.echo_panda_mobile.data.repository.MusicRepository
import com.example.echo_panda_mobile.data.repository.MusicResult
import com.example.echo_panda_mobile.data.repository.TokenStorage
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

data class AlbumDetailUiState(
    val isLoading: Boolean = true,
    val album: Album? = null,
    val errorMessage: String? = null
)

class AlbumDetailViewModel(application: Application) : AndroidViewModel(application) {
    private val tokenStorage = TokenStorage(application)
    private val musicRepository = MusicRepository(RetrofitClient.getMusicService(tokenStorage))

    private val _uiState = MutableStateFlow(AlbumDetailUiState())
    val uiState: StateFlow<AlbumDetailUiState> = _uiState.asStateFlow()

    fun loadAlbum(albumId: String) {
        viewModelScope.launch {
            _uiState.update { it.copy(isLoading = true) }
            when (val result = musicRepository.getAlbumById(albumId)) {
                is MusicResult.Success -> {
                    _uiState.update { it.copy(isLoading = false, album = result.data) }
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

    fun toggleFavorite(track: Track) {
        val wasFavorite = track.isFavorite
        updateTrackFavorite(track.id, !wasFavorite)
        viewModelScope.launch {
            val result = musicRepository.toggleFavorite(track.id, wasFavorite)
            if (result is MusicResult.Error) {
                updateTrackFavorite(track.id, wasFavorite)
            }
        }
    }

    private fun updateTrackFavorite(trackId: String, isFavorite: Boolean) {
        _uiState.update { state ->
            val updatedAlbum = state.album?.copy(
                tracks = state.album.tracks.map {
                    if (it.id == trackId) it.copy(isFavorite = isFavorite) else it
                }
            )
            state.copy(album = updatedAlbum)
        }
    }
}
