package com.example.echo_panda_mobile.presentation.viewsmodel

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.viewModelScope
import com.example.echo_panda_mobile.data.model.*
import com.example.echo_panda_mobile.data.remote.RetrofitClient
import com.example.echo_panda_mobile.data.repository.MusicRepository
import com.example.echo_panda_mobile.data.repository.MusicResult
import com.example.echo_panda_mobile.data.repository.TokenStorage
import com.example.echo_panda_mobile.presentation.navigation.BrowseSection
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

sealed class BrowseContent {
    data class Artists(val items: List<Artist>) : BrowseContent()
    data class Albums(val items: List<Album>) : BrowseContent()
    data class Tracks(val items: List<Track>) : BrowseContent()
    data class Genres(val items: List<Genre>) : BrowseContent()
    data class Moods(val items: List<MoodPlaylist>) : BrowseContent()
    data class ContinueListening(val items: List<Playlist>) : BrowseContent()
}

data class BrowseUiState(
    val isLoading: Boolean = true,
    val title: String = "",
    val content: BrowseContent? = null,
    val errorMessage: String? = null
)

class BrowseViewModel(
    application: Application,
    savedStateHandle: SavedStateHandle
) : AndroidViewModel(application) {

    private val section: String = savedStateHandle.get<String>("section").orEmpty()
    private val repository = MusicRepository(RetrofitClient.getMusicService(TokenStorage(application)))

    private val _uiState = MutableStateFlow(
        BrowseUiState(title = BrowseSection.title(section))
    )
    val uiState: StateFlow<BrowseUiState> = _uiState.asStateFlow()

    init {
        load()
    }

    private fun load() {
        viewModelScope.launch {
            _uiState.update { it.copy(isLoading = true, errorMessage = null) }
            try {
                val content = when (section) {
                    BrowseSection.POPULAR_ARTISTS -> loadArtists()
                    BrowseSection.TOP_ALBUMS, BrowseSection.TOP_PICKS ->
                        loadAlbums { repository.getTopAlbums() }
                    BrowseSection.ALL_ALBUMS ->
                        loadAlbums { repository.getAllAlbums(perPage = 50) }
                    BrowseSection.NEW_RELEASES -> loadTracks { repository.getNewReleases() }
                    BrowseSection.MOST_PLAYED -> loadTracks { repository.getMostPlayedSongs() }
                    BrowseSection.GENRES -> loadGenres()
                    BrowseSection.MOOD_PLAYLISTS -> loadMoods()
                    BrowseSection.CONTINUE_LISTENING -> loadContinue()
                    BrowseSection.RECENT_LISTENING -> loadRecentListening()
                    else -> null
                }
                if (content == null) {
                    _uiState.update {
                        it.copy(isLoading = false, errorMessage = "Unknown section")
                    }
                } else {
                    _uiState.update { it.copy(isLoading = false, content = content) }
                }
            } catch (e: Exception) {
                _uiState.update {
                    it.copy(
                        isLoading = false,
                        errorMessage = e.localizedMessage ?: "Failed to load"
                    )
                }
            }
        }
    }

    private suspend fun loadArtists(): BrowseContent? {
        return when (val result = repository.getPopularArtists()) {
            is MusicResult.Success -> BrowseContent.Artists(result.data)
            else -> null
        }
    }

    private suspend fun loadAlbums(fetch: suspend () -> MusicResult<List<Album>>): BrowseContent? {
        return when (val result = fetch()) {
            is MusicResult.Success -> BrowseContent.Albums(result.data)
            else -> null
        }
    }

    private suspend fun loadTracks(fetch: suspend () -> MusicResult<List<Track>>): BrowseContent? {
        return when (val result = fetch()) {
            is MusicResult.Success -> BrowseContent.Tracks(result.data)
            else -> null
        }
    }

    private suspend fun loadGenres(): BrowseContent? {
        return when (val result = repository.getGenres()) {
            is MusicResult.Success -> BrowseContent.Genres(result.data)
            else -> null
        }
    }

    private suspend fun loadMoods(): BrowseContent? {
        return when (val result = repository.getMoodPlaylists()) {
            is MusicResult.Success -> BrowseContent.Moods(result.data)
            else -> null
        }
    }

    private suspend fun loadContinue(): BrowseContent? {
        return when (val result = repository.getRecentPlaylists()) {
            is MusicResult.Success -> BrowseContent.ContinueListening(result.data)
            else -> null
        }
    }

    private suspend fun loadRecentListening(): BrowseContent? {
        return when (val result = repository.getRecentListening()) {
            is MusicResult.Success -> BrowseContent.ContinueListening(result.data)
            else -> null
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
        val current = _uiState.value.content as? BrowseContent.Tracks ?: return
        _uiState.update {
            it.copy(
                content = current.copy(
                    items = current.items.map { track ->
                        if (track.id == trackId) track.copy(isFavorite = isFavorite) else track
                    }
                )
            )
        }
    }
}
