package com.example.echo_panda_mobile.presentation.viewsmodel

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
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

data class FavoritesUiState(
    val isLoading: Boolean = false,
    val favoriteTracks: List<Track> = emptyList(),
    val totalDuration: String = "0 min",
    val searchQuery: String = "",
    val errorMessage: String? = null
)

class FavoritesViewModel(application: Application) : AndroidViewModel(application) {
    private val musicRepository = MusicRepository(RetrofitClient.getMusicService(TokenStorage(application)))

    private val _uiState = MutableStateFlow(FavoritesUiState())
    val uiState: StateFlow<FavoritesUiState> = _uiState.asStateFlow()

    private var allFavoriteTracks: List<Track> = emptyList()

    init {
        loadFavorites()
    }

    fun loadFavorites() {
        viewModelScope.launch {
            _uiState.update { it.copy(isLoading = true, errorMessage = null) }

            when (val result = musicRepository.getFavoriteTracks()) {
                is MusicResult.Success -> {
                    allFavoriteTracks = result.data
                    applyTracksToState(allFavoriteTracks)
                }
                is MusicResult.Error -> {
                    _uiState.update {
                        it.copy(isLoading = false, errorMessage = result.message)
                    }
                }
                else -> {
                    _uiState.update { it.copy(isLoading = false) }
                }
            }
        }
    }

    private fun applyTracksToState(tracks: List<Track>) {
        _uiState.update {
            it.copy(
                isLoading = false,
                favoriteTracks = tracks,
                totalDuration = "${tracks.sumOf { track -> track.durationMs } / 60000} min"
            )
        }
    }

    fun onSearchQueryChange(query: String) {
        _uiState.update { it.copy(searchQuery = query) }
        val lowerQuery = query.trim().lowercase()
        if (lowerQuery.isBlank()) {
            applyTracksToState(allFavoriteTracks)
        } else {
            applyTracksToState(
                allFavoriteTracks.filter {
                    it.title.lowercase().contains(lowerQuery) ||
                        it.artist.lowercase().contains(lowerQuery)
                }
            )
        }
    }

    fun toggleFavorite(track: Track) {
        val wasFavorite = track.isFavorite
        if (wasFavorite) {
            allFavoriteTracks = allFavoriteTracks.filter { it.id != track.id }
            applyTracksToState(
                _uiState.value.favoriteTracks.filter { it.id != track.id }
            )
        }
        viewModelScope.launch {
            val result = musicRepository.toggleFavorite(track.id, wasFavorite)
            if (result is MusicResult.Error) {
                loadFavorites()
            } else if (!wasFavorite) {
                loadFavorites()
            }
        }
    }
}
