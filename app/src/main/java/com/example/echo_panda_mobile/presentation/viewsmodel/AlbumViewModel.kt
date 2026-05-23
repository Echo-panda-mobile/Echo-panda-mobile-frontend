package com.example.echo_panda_mobile.presentation.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.echo_panda_mobile.data.model.Album
import com.example.echo_panda_mobile.data.repository.MusicRepository
import com.example.echo_panda_mobile.data.repository.MusicResult
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
    val errorMessage: String? = null,
    val searchQuery: String = ""
)

class AlbumViewModel(
    private val musicRepository: MusicRepository = MusicRepository()
) : ViewModel() {

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
                state.copy(
                    isLoading = false,
                    topAlbums = (topResult as? MusicResult.Success)?.data ?: emptyList(),
                    newAlbums = (newResult as? MusicResult.Success)?.data ?: emptyList(),
                    popularAlbums = (popularResult as? MusicResult.Success)?.data ?: emptyList()
                )
            }
        }
    }

    fun onSearchQueryChange(query: String) {
        _uiState.update { it.copy(searchQuery = query) }
    }
}
