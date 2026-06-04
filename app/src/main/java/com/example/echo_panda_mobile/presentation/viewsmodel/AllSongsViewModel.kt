package com.example.echo_panda_mobile.presentation.viewmodel

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.SavedStateHandle
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

data class AllSongsUiState(
    val isLoading: Boolean = true,
    val title: String = "Songs",
    val tracks: List<Track> = emptyList(),
    val errorMessage: String? = null
)

class AllSongsViewModel(
    application: Application,
    savedStateHandle: SavedStateHandle
) : AndroidViewModel(application) {

    private val tokenStorage = TokenStorage(application)
    private val repository = MusicRepository(RetrofitClient.getMusicService(tokenStorage))

    private val filterType = savedStateHandle.get<String>("type") ?: "Songs"
    private val filterTitle = savedStateHandle.get<String>("title").orEmpty()

    private val _uiState = MutableStateFlow(AllSongsUiState())
    val uiState: StateFlow<AllSongsUiState> = _uiState.asStateFlow()

    init {
        load()
    }

    private fun load() {
        viewModelScope.launch {
            _uiState.value = _uiState.value.copy(isLoading = true, errorMessage = null)

            val result = when {
                filterType == "New Releases" -> {
                    _uiState.update { it.copy(title = "New Releases") }
                    repository.getNewReleases()
                }
                filterType == "Most Played" -> {
                    _uiState.update { it.copy(title = "Most Played") }
                    repository.getMostPlayedSongs(limit = 100)
                }
                filterType.startsWith("genre-") -> {
                    val genreId = filterType.removePrefix("genre-")
                    _uiState.update { it.copy(title = filterTitle.ifBlank { "Genre" }) }
                    repository.getSongsByGenre(genreId)
                }
                filterType.startsWith("tag-") -> {
                    val tagId = filterType.removePrefix("tag-")
                    _uiState.update { it.copy(title = filterTitle.ifBlank { "Tag" }) }
                    repository.getSongsByTag(tagId)
                }
                else -> {
                    _uiState.update { it.copy(title = filterType) }
                    repository.getNewReleases()
                }
            }

            when (result) {
                is MusicResult.Success -> {
                    _uiState.update { it.copy(isLoading = false, tracks = result.data) }
                }
                is MusicResult.Error -> {
                    _uiState.update {
                        it.copy(isLoading = false, errorMessage = result.message)
                    }
                }
            }
        }
    }

    fun toggleFavorite(track: Track) {
        val wasFavorite = track.isFavorite
        updateTrackFavorite(track.id, !wasFavorite)
        viewModelScope.launch {
            val result = repository.toggleFavorite(track.id, wasFavorite)
            if (result is MusicResult.Error) {
                updateTrackFavorite(track.id, wasFavorite)
            }
        }
    }

    private fun updateTrackFavorite(trackId: String, isFavorite: Boolean) {
        _uiState.update { state ->
            state.copy(
                tracks = state.tracks.map {
                    if (it.id == trackId) it.copy(isFavorite = isFavorite) else it
                }
            )
        }
    }

    fun refresh() = load()
}
