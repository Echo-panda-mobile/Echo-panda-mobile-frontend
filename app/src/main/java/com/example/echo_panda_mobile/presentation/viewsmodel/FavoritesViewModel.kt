package com.example.echo_panda_mobile.presentation.viewsmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.echo_panda_mobile.data.model.Track
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

data class FavoritesUiState(
    val isLoading: Boolean = false,
    val favoriteTracks: List<Track> = emptyList(),
    val totalDuration: String = "0 min"
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
            
            // Mock favorite tracks
            val tracks = listOf(
                Track(id = "f1", title = "Heather", artist = "Conan Gray", durationMs = 198000),
                Track(id = "f2", title = "Maniac", artist = "Conan Gray", durationMs = 185000),
                Track(id = "f3", title = "Softcore", artist = "The Neighbourhood", durationMs = 210000),
                Track(id = "f4", title = "Daddy Issues", artist = "The Neighbourhood", durationMs = 260000),
                Track(id = "f5", title = "Sweater Weather", artist = "The Neighbourhood", durationMs = 240000)
            )
            
            _uiState.update { 
                it.copy(
                    isLoading = false,
                    favoriteTracks = tracks,
                    totalDuration = "${tracks.size * 3} min"
                )
            }
        }
    }
}
