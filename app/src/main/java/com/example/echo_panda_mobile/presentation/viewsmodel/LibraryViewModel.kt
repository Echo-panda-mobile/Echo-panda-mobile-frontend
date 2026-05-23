package com.example.echo_panda_mobile.presentation.viewsmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.echo_panda_mobile.data.model.Artist
import com.example.echo_panda_mobile.data.model.Playlist
import com.example.echo_panda_mobile.data.model.Album
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

data class LibraryState(
    val isLoading: Boolean = false,
    val allItems: List<LibraryItem> = emptyList(),
    val filteredItems: List<LibraryItem> = emptyList(),
    val selectedFilter: String = "All"
)

sealed class LibraryItem {
    data class ArtistItem(val artist: Artist) : LibraryItem()
    data class PlaylistItem(val playlist: Playlist, val trackCount: Int? = null, val subtitle: String? = null) : LibraryItem()
    data class AlbumItem(val album: Album) : LibraryItem()
}

class LibraryViewModel : ViewModel() {
    private val _uiState = MutableStateFlow(LibraryState())
    val uiState: StateFlow<LibraryState> = _uiState.asStateFlow()

    init {
        loadLibrary()
    }

    private fun loadLibrary() {
        viewModelScope.launch {
            _uiState.update { it.copy(isLoading = true) }
            
            // Mock data based on the screenshot
            val items = listOf(
                LibraryItem.ArtistItem(Artist(id = "1", name = "Conan Gray")),
                LibraryItem.PlaylistItem(Playlist(id = "2", title = "3:00am vibes"), trackCount = 18),
                LibraryItem.AlbumItem(Album(id = "3", title = "Wiped Out!", artist = "The Neighbourhood")),
                LibraryItem.PlaylistItem(Playlist(id = "4", title = "Extra Dynamic"), subtitle = "Updated Aug 10 • ur mom ashley")
            )
            
            _uiState.update { 
                it.copy(
                    isLoading = false,
                    allItems = items,
                    filteredItems = items // Initially show all
                )
            }
        }
    }

    fun setFilter(filter: String) {
        _uiState.update { currentState ->
            val newFilter = if (currentState.selectedFilter == filter) "All" else filter
            val filtered = when (newFilter) {
                "All", "Recently" -> currentState.allItems
                "Playlists" -> currentState.allItems.filter { it is LibraryItem.PlaylistItem }
                "Artists" -> currentState.allItems.filter { it is LibraryItem.ArtistItem }
                "Albums" -> currentState.allItems.filter { it is LibraryItem.AlbumItem }
                else -> currentState.allItems
            }
            currentState.copy(
                selectedFilter = newFilter,
                filteredItems = filtered
            )
        }
    }
}
