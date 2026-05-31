package com.example.echo_panda_mobile.presentation.viewsmodel

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.example.echo_panda_mobile.data.model.Album
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

data class AlbumUiState(
    val isLoading: Boolean = true,
    val topAlbums: List<Album> = emptyList(),
    val newAlbums: List<Album> = emptyList(),
    val popularAlbums: List<Album> = emptyList(),
    val allAlbums: List<Album> = emptyList(),
    val filteredAlbums: List<Album> = emptyList(),
    val selectedCategory: String = "All",
    val errorMessage: String? = null,
    val searchQuery: String = "",
    val isSearchActive: Boolean = false
)

class AlbumViewModel(application: Application) : AndroidViewModel(application) {
    private val musicRepository = MusicRepository(
        RetrofitClient.getMusicService(TokenStorage(application))
    )

    private val _uiState = MutableStateFlow(AlbumUiState())
    val uiState: StateFlow<AlbumUiState> = _uiState.asStateFlow()

    init {
        loadAlbums()
    }

    fun loadAlbums() {
        viewModelScope.launch {
            _uiState.update { it.copy(isLoading = true, errorMessage = null) }

            val topDef = async { musicRepository.getTopAlbums() }
            val newDef = async { musicRepository.getNewAlbums() }
            val popularDef = async { musicRepository.getPopularAlbums() }
            val allDef = async { musicRepository.getAllAlbums(perPage = 50) }

            val topResult = topDef.await()
            val newResult = newDef.await()
            val popularResult = popularDef.await()
            val allResult = allDef.await()

            val top = (topResult as? MusicResult.Success)?.data ?: emptyList()
            val new = (newResult as? MusicResult.Success)?.data ?: emptyList()
            val popular = (popularResult as? MusicResult.Success)?.data ?: emptyList()
            val allFromApi = (allResult as? MusicResult.Success)?.data

            val all = allFromApi?.takeIf { it.isNotEmpty() }
                ?: (top + new + popular).distinctBy { it.id }

            val error = listOf(topResult, newResult, popularResult, allResult)
                .filterIsInstance<MusicResult.Error>()
                .firstOrNull()
                ?.message
                .takeIf { all.isEmpty() }

            _uiState.update { state ->
                state.copy(
                    isLoading = false,
                    topAlbums = top,
                    newAlbums = new,
                    popularAlbums = popular,
                    allAlbums = all,
                    filteredAlbums = all,
                    errorMessage = error
                )
            }
        }
    }

    fun onSearchQueryChange(query: String) {
        _uiState.update { state ->
            val source = state.allAlbums.ifEmpty { state.popularAlbums }
            val filtered = if (query.isBlank()) {
                source
            } else {
                source.filter { SearchMatcher.matches(query, it.title, it.artist) }
            }
            state.copy(searchQuery = query, filteredAlbums = filtered)
        }
    }

    fun setCategory(category: String) {
        _uiState.update { state ->
            val filtered = when (category) {
                "Trending" -> state.topAlbums.ifEmpty { state.allAlbums }
                "Newest" -> state.newAlbums.ifEmpty { state.allAlbums }
                "Pop", "Rock", "Hip-Hop" -> state.popularAlbums.ifEmpty { state.allAlbums }
                else -> state.allAlbums
            }
            state.copy(selectedCategory = category, filteredAlbums = filtered)
        }
    }

    fun toggleSearch() {
        _uiState.update { it.copy(isSearchActive = !it.isSearchActive, searchQuery = "") }
        if (!_uiState.value.isSearchActive) onSearchQueryChange("")
    }

    fun startSearchWithQuery(query: String) {
        _uiState.update { it.copy(isSearchActive = true) }
        onSearchQueryChange(query)
    }
}
