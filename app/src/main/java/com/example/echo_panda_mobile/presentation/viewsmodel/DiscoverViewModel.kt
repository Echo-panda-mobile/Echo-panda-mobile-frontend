package com.example.echo_panda_mobile.presentation.viewmodel

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.example.echo_panda_mobile.data.model.*
import com.example.echo_panda_mobile.data.remote.RetrofitClient
import com.example.echo_panda_mobile.data.repository.MusicRepository
import com.example.echo_panda_mobile.data.repository.MusicResult
import com.example.echo_panda_mobile.data.repository.TokenStorage
import com.example.echo_panda_mobile.data.util.SearchMatcher
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
    private var allMostPlayed: List<Track> = emptyList()

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

            allGenres   = (genresDef.await()   as? MusicResult.Success)?.data ?: emptyList()
            allMoods    = (moodsDef.await()    as? MusicResult.Success)?.data ?: emptyList()
            allReleases = (releasesDef.await() as? MusicResult.Success)?.data ?: emptyList()
            allMostPlayed = (mostPlayedDef.await() as? MusicResult.Success)?.data ?: emptyList()
            val artists  = (artistsDef.await()  as? MusicResult.Success)?.data ?: emptyList()
            val browse   = (browseDef.await()   as? MusicResult.Success)?.data ?: emptyList()

            _uiState.value = _uiState.value.copy(
                isLoading        = false,
                genres           = allGenres,
                moodPlaylists    = allMoods,
                newReleases      = allReleases,
                mostPlayedSongs  = allMostPlayed,
                popularArtists   = artists,
                browseCategories = browse
            )

            enrichDiscoverTrackDurations()
        }
    }

    private suspend fun enrichDiscoverTrackDurations() {
        val combined = (allReleases + allMostPlayed).distinctBy { it.id }
        if (combined.isEmpty()) return

        val enriched = repository.enrichTracksDurationFromMedia(combined)
        val byId = enriched.associateBy { it.id }
        allReleases = allReleases.map { byId[it.id] ?: it }
        allMostPlayed = allMostPlayed.map { byId[it.id] ?: it }
        refreshTrackSectionsInState()
    }

    private fun refreshTrackSectionsInState() {
        val query = _uiState.value.searchQuery
        val visibleReleases = if (query.isBlank()) {
            allReleases
        } else {
            allReleases.filter { SearchMatcher.matches(query, it.title, it.artist) }
        }
        val visibleMostPlayed = if (query.isBlank()) {
            allMostPlayed
        } else {
            allMostPlayed.filter { SearchMatcher.matches(query, it.title, it.artist) }
        }
        _uiState.update { state ->
            state.copy(
                newReleases = visibleReleases,
                mostPlayedSongs = visibleMostPlayed
            )
        }
    }

    fun onSearchQueryChange(query: String) {
        _uiState.value = _uiState.value.copy(searchQuery = query)
        if (query.isBlank()) {
            _uiState.value = _uiState.value.copy(
                genres = allGenres,
                moodPlaylists = allMoods,
                searchResults = emptyList()
            )
            refreshTrackSectionsInState()
        } else {
            _uiState.value = _uiState.value.copy(
                genres = allGenres.filter { SearchMatcher.matches(query, it.name, it.subLabel) },
                moodPlaylists = allMoods.filter { SearchMatcher.matches(query, it.name) },
                newReleases = allReleases.filter { SearchMatcher.matches(query, it.title, it.artist) },
                mostPlayedSongs = allMostPlayed.filter { SearchMatcher.matches(query, it.title, it.artist) }
            )
            viewModelScope.launch {
                val albumsResult = repository.getTopAlbums()
                if (albumsResult is MusicResult.Success) {
                    _uiState.value = _uiState.value.copy(
                        searchResults = albumsResult.data.filter {
                            SearchMatcher.matches(query, it.title, it.artist)
                        }
                    )
                }
            }
        }
    }

    fun toggleSearch() {
        val nextActive = !_uiState.value.isSearchActive
        _uiState.value = _uiState.value.copy(
            isSearchActive = nextActive,
            searchQuery = if (!nextActive) "" else _uiState.value.searchQuery
        )
        if (!nextActive) {
            onSearchQueryChange("")
        }
    }

    fun startSearchWithQuery(query: String) {
        _uiState.update { it.copy(isSearchActive = true) }
        onSearchQueryChange(query)
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
        allMostPlayed = allMostPlayed.map {
            if (it.id == trackId) it.copy(isFavorite = isFavorite) else it
        }
    }

    fun refresh() = loadAll()
}
