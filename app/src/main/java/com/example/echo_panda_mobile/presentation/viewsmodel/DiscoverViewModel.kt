package com.example.echo_panda_mobile.presentation.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.echo_panda_mobile.data.model.*
import com.example.echo_panda_mobile.data.repository.MusicRepository
import com.example.echo_panda_mobile.data.repository.MusicResult
import kotlinx.coroutines.async
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

data class DiscoverUiState(
    val isLoading: Boolean                   = true,
    val genres: List<Genre>                  = emptyList(),
    val moodPlaylists: List<MoodPlaylist>     = emptyList(),
    val newReleases: List<Track>             = emptyList(),
    val popularArtists: List<Artist>         = emptyList(),
    val browseCategories: List<BrowseCategory> = emptyList(),
    val searchQuery: String                  = "",
    val errorMessage: String?                = null
)

class DiscoverViewModel(
    private val repository: MusicRepository = MusicRepository()
) : ViewModel() {

    private val _uiState = MutableStateFlow(DiscoverUiState())
    val uiState: StateFlow<DiscoverUiState> = _uiState.asStateFlow()

    init { loadAll() }

    private fun loadAll() {
        viewModelScope.launch {
            _uiState.value = _uiState.value.copy(isLoading = true)

            val genresDef    = async { repository.getGenres() }
            val moodsDef     = async { repository.getMoodPlaylists() }
            val releasesDef  = async { repository.getNewReleases() }
            val artistsDef   = async { repository.getPopularArtists() }
            val browseDef    = async { repository.getBrowseCategories() }

            val genres   = (genresDef.await()   as? MusicResult.Success)?.data ?: emptyList()
            val moods    = (moodsDef.await()    as? MusicResult.Success)?.data ?: emptyList()
            val releases = (releasesDef.await() as? MusicResult.Success)?.data ?: emptyList()
            val artists  = (artistsDef.await()  as? MusicResult.Success)?.data ?: emptyList()
            val browse   = (browseDef.await()   as? MusicResult.Success)?.data ?: emptyList()

            _uiState.value = _uiState.value.copy(
                isLoading        = false,
                genres           = genres,
                moodPlaylists    = moods,
                newReleases      = releases,
                popularArtists   = artists,
                browseCategories = browse
            )
        }
    }

    fun onSearchQueryChange(query: String) {
        _uiState.value = _uiState.value.copy(searchQuery = query)
    }

    fun refresh() = loadAll()
}