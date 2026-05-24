package com.example.echo_panda_mobile.presentation.viewsmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.echo_panda_mobile.data.model.Track
import com.example.echo_panda_mobile.data.repository.LibraryRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

data class FavoritesUiState(
    val isLoading: Boolean = false,
    val favoriteTracks: List<Track> = emptyList(),
    val totalDuration: String = "0 min",
    val searchQuery: String = ""
)

class FavoritesViewModel : ViewModel() {
    private val _uiState = MutableStateFlow(FavoritesUiState())
    val uiState: StateFlow<FavoritesUiState> = _uiState.asStateFlow()

    init {
        loadFavorites()
    }

    private fun loadFavorites() {
        viewModelScope.launch {
            _uiState.update { it.copy(isLoading = true) }
            val tracks = LibraryRepository.favoriteTracks.value
            _uiState.update {
                it.copy(
                    isLoading = false,
                    favoriteTracks = tracks,
                    totalDuration = "${tracks.size * 3} min"
                )
            }
        }
    }

    fun onSearchQueryChange(query: String) {
        _uiState.update { currentState ->
            val filtered = LibraryRepository.searchFavorites(query)
            currentState.copy(
                searchQuery = query,
                favoriteTracks = filtered
            )
        }
    }

    fun addTrackToFavorites(track: Track) {
        LibraryRepository.addTrackToFavorites(track)
        loadFavorites()
    }
}
