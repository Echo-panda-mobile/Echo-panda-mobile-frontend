package com.example.echo_panda_mobile.presentation.viewsmodel

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.example.echo_panda_mobile.data.model.Album
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

data class AlbumUiState(
    val isLoading: Boolean = true,
    val topAlbums: List<Album> = emptyList(),
    val newAlbums: List<Album> = emptyList(),
    val popularAlbums: List<Album> = emptyList(),
    val filteredAlbums: List<Album> = emptyList(),
    val selectedCategory: String = "All",
    val errorMessage: String? = null,
    val searchQuery: String = "",
    val isSearchActive: Boolean = false
)

class AlbumViewModel(application: Application) : AndroidViewModel(application) {
    private val tokenStorage = TokenStorage(application)
    private val musicRepository = MusicRepository(RetrofitClient.getMusicService(tokenStorage))

    private val _uiState = MutableStateFlow(AlbumUiState())
    val uiState: StateFlow<AlbumUiState> = _uiState.asStateFlow()

    init {
        loadAlbums()
    }

    fun loadAlbums() {
        viewModelScope.launch {
            _uiState.update { it.copy(isLoading = true) }
            
            val topDef = async { musicRepository.getTopAlbums() }
            val newDef = async { musicRepository.getNewAlbums() }
            val popularDef = async { musicRepository.getPopularAlbums() }
            
            val topResult = topDef.await()
            val newResult = newDef.await()
            val popularResult = popularDef.await()
            
            _uiState.update { state ->
                val top = (topResult as? MusicResult.Success)?.data ?: emptyList()
                val new = (newResult as? MusicResult.Success)?.data ?: emptyList()
                val popular = (popularResult as? MusicResult.Success)?.data ?: emptyList()
                state.copy(
                    isLoading = false,
                    topAlbums = top,
                    newAlbums = new,
                    popularAlbums = popular,
                    filteredAlbums = popular // Default to popular for "All"
                )
            }
        }
    }

    fun onSearchQueryChange(query: String) {
        _uiState.update { state ->
            val filtered = if (query.isBlank()) {
                state.popularAlbums
            } else {
                (state.topAlbums + state.newAlbums + state.popularAlbums)
                    .distinctBy { it.id }
                    .filter { 
                        it.title.contains(query, ignoreCase = true) || 
                        it.artist.contains(query, ignoreCase = true) 
                    }
            }
            state.copy(searchQuery = query, filteredAlbums = filtered)
        }
    }

    fun setCategory(category: String) {
        _uiState.update { state ->
            val filtered = when (category) {
                "Trending" -> state.topAlbums
                "Newest" -> state.newAlbums
                "Pop", "Rock", "Hip-Hop" -> state.popularAlbums // Simulated
                else -> state.popularAlbums
            }
            state.copy(selectedCategory = category, filteredAlbums = filtered)
        }
    }

    fun toggleSearch() {
        _uiState.update { it.copy(isSearchActive = !it.isSearchActive, searchQuery = "") }
        if (!_uiState.value.isSearchActive) onSearchQueryChange("")
    }
}
