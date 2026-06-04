package com.example.echo_panda_mobile.presentation.viewsmodel

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.example.echo_panda_mobile.data.model.Playlist
import com.example.echo_panda_mobile.data.model.Track
import com.example.echo_panda_mobile.data.remote.RetrofitClient
import com.example.echo_panda_mobile.data.repository.MusicRepository
import com.example.echo_panda_mobile.data.repository.MusicResult
import com.example.echo_panda_mobile.data.repository.TokenStorage
import kotlinx.coroutines.async
import kotlinx.coroutines.coroutineScope
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

data class PlaylistState(
    val isLoading: Boolean = false,
    val playlist: Playlist? = null,
    val tracks: List<Track> = emptyList(),
    val error: String? = null
)

class PlaylistViewModel(application: Application) : AndroidViewModel(application) {
    private val musicRepository = MusicRepository(RetrofitClient.getMusicService(TokenStorage(application)))

    private val _uiState = MutableStateFlow(PlaylistState())
    val uiState: StateFlow<PlaylistState> = _uiState.asStateFlow()

    fun loadPlaylist(playlistId: String) {
        viewModelScope.launch {
            _uiState.update { it.copy(isLoading = true, error = null) }
            try {
                coroutineScope {
                    val metaDef = async { musicRepository.findPlaylistById(playlistId) }
                    val songsDef = async { musicRepository.getPlaylistSongs(playlistId) }

                    val playlist = (metaDef.await() as? MusicResult.Success)?.data
                    when (val songsResult = songsDef.await()) {
                        is MusicResult.Success -> {
                            _uiState.update {
                                it.copy(
                                    isLoading = false,
                                    playlist = playlist,
                                    tracks = songsResult.data
                                )
                            }
                        }
                        is MusicResult.Error -> {
                            _uiState.update {
                                it.copy(
                                    isLoading = false,
                                    playlist = playlist,
                                    error = songsResult.message
                                )
                            }
                        }
                        else -> {
                            _uiState.update {
                                it.copy(isLoading = false, playlist = playlist, error = "Unknown error")
                            }
                        }
                    }
                }
            } catch (e: Exception) {
                _uiState.update {
                    it.copy(
                        isLoading = false,
                        error = e.localizedMessage ?: "Failed to load playlist"
                    )
                }
            }
        }
    }

    fun toggleFavorite(track: Track) {
        val wasFavorite = track.isFavorite
        _uiState.update { state ->
            state.copy(
                tracks = state.tracks.map {
                    if (it.id == track.id) it.copy(isFavorite = !wasFavorite) else it
                }
            )
        }
        viewModelScope.launch {
            val result = musicRepository.toggleFavorite(track.id, wasFavorite)
            if (result is MusicResult.Error) {
                _uiState.update { state ->
                    state.copy(
                        tracks = state.tracks.map {
                            if (it.id == track.id) it.copy(isFavorite = wasFavorite) else it
                        }
                    )
                }
            }
        }
    }

    fun removeFromPlaylist(trackId: String) {
        val playlistId = _uiState.value.playlist?.id ?: return
        
        // Optimistic UI update
        val previousTracks = _uiState.value.tracks
        _uiState.update { state ->
            state.copy(tracks = state.tracks.filter { it.id != trackId })
        }

        viewModelScope.launch {
            when (val result = musicRepository.removeFromPlaylist(trackId, playlistId)) {
                is MusicResult.Error -> {
                    // Rollback on error
                    _uiState.update { it.copy(tracks = previousTracks, error = result.message) }
                }
                else -> {
                    // Success, maybe show a toast or message
                }
            }
        }
    }
}
