package com.example.echo_panda_mobile.presentation.viewmodel

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.example.echo_panda_mobile.data.model.*
import com.example.echo_panda_mobile.data.remote.RetrofitClient
import com.example.echo_panda_mobile.data.repository.MusicRepository
import com.example.echo_panda_mobile.data.repository.MusicResult
import com.example.echo_panda_mobile.data.repository.TokenStorage
import kotlinx.coroutines.async
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

data class DiscoverUiState(
    val isLoading: Boolean                   = true,
    val genres: List<Genre>                  = emptyList(),
    val moodPlaylists: List<MoodPlaylist>     = emptyList(),
    val newReleases: List<Track>             = emptyList(),
    val mostPlayedSongs: List<Track>         = emptyList(),
    val popularArtists: List<Artist>         = emptyList(),
    val browseCategories: List<BrowseCategory> = emptyList(),
    val searchResults: List<Album>           = emptyList(), // For album search
    val searchQuery: String                  = "",
    val isSearchActive: Boolean              = false,
    val errorMessage: String?                = null
)

class DiscoverViewModel(application: Application) : AndroidViewModel(application) {
    private val tokenStorage = TokenStorage(application)
    private val repository = MusicRepository(RetrofitClient.getMusicService(tokenStorage))

    private val _uiState = MutableStateFlow(DiscoverUiState())
    val uiState: StateFlow<DiscoverUiState> = _uiState.asStateFlow()

    private var allGenres: List<Genre> = emptyList()
    private var allMoods: List<MoodPlaylist> = emptyList()
    private var allReleases: List<Track> = emptyList()

    init { loadAll() }

    private fun loadAll() {
        viewModelScope.launch {
            _uiState.value = _uiState.value.copy(isLoading = true)

            val genresDef    = async { repository.getGenres() }
            val moodsDef     = async { repository.getMoodPlaylists() }
            val releasesDef  = async { repository.getNewReleases() }
            val mostPlayedDef = async { repository.getMostPlayedSongs() }
            val artistsDef   = async { repository.getPopularArtists() }
            val browseDef    = async { repository.getBrowseCategories() }

            val genresResult = genresDef.await()
            val tagsResult = moodsDef.await()
            allGenres   = (genresResult as? MusicResult.Success)?.data ?: emptyList()
            allMoods    = (tagsResult as? MusicResult.Success)?.data ?: emptyList()
            val loadError = listOfNotNull(
                (genresResult as? MusicResult.Error)?.message,
                (tagsResult as? MusicResult.Error)?.message
            ).firstOrNull()
            allReleases = (releasesDef.await() as? MusicResult.Success)?.data ?: emptyList()
            val mostPlayed = (mostPlayedDef.await() as? MusicResult.Success)?.data ?: emptyList()
            val artists  = (artistsDef.await()  as? MusicResult.Success)?.data ?: emptyList()
            val browse   = (browseDef.await()   as? MusicResult.Success)?.data ?: emptyList()

            _uiState.value = _uiState.value.copy(
                isLoading        = false,
                genres           = allGenres,
                moodPlaylists    = allMoods,
                newReleases      = allReleases,
                mostPlayedSongs  = mostPlayed,
                popularArtists   = artists,
                browseCategories = browse,
                errorMessage     = loadError
            )
        }
    }

    fun onSearchQueryChange(query: String) {
        _uiState.update { it.copy(searchQuery = query) }
        
        if (query.isBlank()) {
            _uiState.update { state ->
                state.copy(
                    genres = allGenres,
                    moodPlaylists = allMoods,
                    newReleases = allReleases,
                    searchResults = emptyList()
                )
            }
        } else {
            val queryWords = query.trim().lowercase().split("\\s+".toRegex()).filter { it.isNotBlank() }
            
            _uiState.update { state ->
                state.copy(
                    genres = allGenres.filter { genre ->
                        val target = genre.name.lowercase()
                        queryWords.all { word -> target.contains(word) }
                    },
                    moodPlaylists = allMoods.filter { mood ->
                        val target = mood.name.lowercase()
                        queryWords.all { word -> target.contains(word) }
                    },
                    newReleases = allReleases.filter { track ->
                        val target = "${track.title} ${track.artist}".lowercase()
                        queryWords.all { word -> target.contains(word) }
                    }
                )
            }
            
            // Simulate album search
            viewModelScope.launch {
                val albumsResult = repository.getTopAlbums()
                if (albumsResult is MusicResult.Success) {
                    _uiState.update { state ->
                        state.copy(
                            searchResults = albumsResult.data.filter { album ->
                                val target = "${album.title} ${album.artist}".lowercase()
                                queryWords.all { word -> target.contains(word) }
                            }
                        )
                    }
                }
            }
        }
    }

    fun toggleSearch() {
        _uiState.update { state ->
            val nextActive = !state.isSearchActive
            state.copy(
                isSearchActive = nextActive,
                searchQuery = if (nextActive) state.searchQuery else ""
            )
        }
    }

    fun toggleFavorite(track: Track) {
        val wasFavorite = track.isFavorite
        updateTrackFavorite(track.id, !wasFavorite)
        viewModelScope.launch {
            val result = repository.toggleFavorite(track.id, wasFavorite)
            if (result is MusicResult.Error) {
                android.util.Log.e("DiscoverViewModel", "Toggle favorite error: ${result.message}")
                updateTrackFavorite(track.id, wasFavorite)
            }
        }
    }

    private fun updateTrackFavorite(trackId: String, isFavorite: Boolean) {
        _uiState.update { state ->
            state.copy(
                newReleases = state.newReleases.map {
                    if (it.id == trackId) it.copy(isFavorite = isFavorite) else it
                },
                mostPlayedSongs = state.mostPlayedSongs.map {
                    if (it.id == trackId) it.copy(isFavorite = isFavorite) else it
                }
            )
        }
        allReleases = allReleases.map {
            if (it.id == trackId) it.copy(isFavorite = isFavorite) else it
        }
    }

    fun refresh() = loadAll()
}
