package com.example.echo_panda_mobile.presentation.viewsmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.echo_panda_mobile.data.model.Artist
import com.example.echo_panda_mobile.data.model.Playlist
import com.example.echo_panda_mobile.data.model.Album
import com.example.echo_panda_mobile.data.repository.LibraryRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

data class LibraryState(
    val isLoading: Boolean = false,
    val allItems: List<LibraryItem> = emptyList(),
    val filteredItems: List<LibraryItem> = emptyList(),
    val selectedFilter: String = "All",
    val searchQuery: String = "",
    val isCreatePlaylistOpen: Boolean = false,
    val isFollowArtistOpen: Boolean = false,
    val newPlaylistName: String = "",
    val newArtistName: String = "",
    val likedSongsCount: Int = 0
)

sealed class LibraryItem {
    data class ArtistItem(val artist: Artist) : LibraryItem()
    data class PlaylistItem(val playlist: Playlist, val trackCount: Int? = null, val subtitle: String? = null) : LibraryItem()
    data class AlbumItem(val album: Album) : LibraryItem()
}

class LibraryViewModel(application: android.app.Application) : androidx.lifecycle.AndroidViewModel(application) {
    private val tokenStorage = com.example.echo_panda_mobile.data.repository.TokenStorage(application)
    private val musicRepository = com.example.echo_panda_mobile.data.repository.MusicRepository(
        com.example.echo_panda_mobile.data.remote.RetrofitClient.getMusicService(tokenStorage)
    )

    private val _uiState = MutableStateFlow(LibraryState())
    val uiState: StateFlow<LibraryState> = _uiState.asStateFlow()

    init {
        viewModelScope.launch {
            LibraryRepository.playlists.collect { _ ->
                refreshLibrary()
            }
        }
        viewModelScope.launch {
            LibraryRepository.followedArtists.collect { _ ->
                refreshLibrary()
            }
        }
        viewModelScope.launch {
            LibraryRepository.favoriteTracks.collect { tracks ->
                _uiState.update { it.copy(likedSongsCount = tracks.size) }
            }
        }
        loadLibrary()
    }

    private fun loadLibrary() {
        viewModelScope.launch {
            _uiState.update { it.copy(isLoading = true) }

            // 1. Get followed/local items
            val localItems = buildLibraryItems()
            
            // 2. Supplement with some global data from API to make it look "full"
            val globalArtists = (musicRepository.getPopularArtists() as? com.example.echo_panda_mobile.data.repository.MusicResult.Success)?.data ?: emptyList()
            val globalAlbums = (musicRepository.getPopularAlbums() as? com.example.echo_panda_mobile.data.repository.MusicResult.Success)?.data ?: emptyList()

            val allItems = (localItems + 
                globalArtists.map { LibraryItem.ArtistItem(it) } + 
                globalAlbums.map { LibraryItem.AlbumItem(it) }
            ).distinctBy {
                when (it) {
                    is LibraryItem.ArtistItem -> "artist_${it.artist.id}"
                    is LibraryItem.PlaylistItem -> "playlist_${it.playlist.id}"
                    is LibraryItem.AlbumItem -> "album_${it.album.id}"
                }
            }

            _uiState.update {
                it.copy(
                    isLoading = false,
                    allItems = allItems,
                    filteredItems = applyFilter(it.selectedFilter, it.searchQuery, allItems)
                )
            }
        }
    }

    private fun buildLibraryItems(): List<LibraryItem> {
        val staticItems = listOf(
            LibraryItem.PlaylistItem(
                Playlist(id = "2", title = "3:00am vibes"),
                trackCount = LibraryRepository.getPlaylistTrackCount("2")
            ),
            LibraryItem.AlbumItem(Album(id = "3", title = "Wiped Out!", artist = "The Neighbourhood")),
            LibraryItem.PlaylistItem(
                Playlist(id = "4", title = "Extra Dynamic"),
                subtitle = "Updated Aug 10 • ur mom ashley",
                trackCount = LibraryRepository.getPlaylistTrackCount("4")
            )
        )

        val artistItems = LibraryRepository.followedArtists.value.map { LibraryItem.ArtistItem(it) }
        val playlistItems = LibraryRepository.playlists.value.map { playlist ->
            LibraryItem.PlaylistItem(
                playlist,
                trackCount = LibraryRepository.getPlaylistTrackCount(playlist.id)
            )
        }

        return (artistItems + playlistItems + staticItems).distinctBy {
            when (it) {
                is LibraryItem.ArtistItem -> "artist_${it.artist.id}"
                is LibraryItem.PlaylistItem -> "playlist_${it.playlist.id}"
                is LibraryItem.AlbumItem -> "album_${it.album.id}"
            }
        }
    }

    private fun refreshLibrary() {
        _uiState.update { currentState ->
            val items = buildLibraryItems()
            val filtered = applyFilter(currentState.selectedFilter, currentState.searchQuery, items)
            currentState.copy(
                allItems = items,
                filteredItems = filtered
            )
        }
    }

    private fun applyFilter(filter: String, searchQuery: String, items: List<LibraryItem>): List<LibraryItem> {
        val query = searchQuery.trim().lowercase()
        if (query.isNotBlank()) {
            return items.filter { item ->
                when (item) {
                    is LibraryItem.ArtistItem -> item.artist.name.lowercase().contains(query)
                    is LibraryItem.PlaylistItem -> item.playlist.title.lowercase().contains(query)
                    is LibraryItem.AlbumItem -> item.album.title.lowercase().contains(query) || item.album.artist.lowercase().contains(query)
                }
            }
        }

        return when (filter) {
            "All", "Recently" -> items
            "Playlists" -> items.filter { it is LibraryItem.PlaylistItem }
            "Artists" -> items.filter { it is LibraryItem.ArtistItem }
            "Albums" -> items.filter { it is LibraryItem.AlbumItem }
            else -> items
        }
    }

    fun setFilter(filter: String) {
        _uiState.update { currentState ->
            val newFilter = if (currentState.selectedFilter == filter) "All" else filter
            val filtered = applyFilter(newFilter, currentState.searchQuery, currentState.allItems)
            currentState.copy(
                selectedFilter = newFilter,
                filteredItems = filtered
            )
        }
    }

    fun onSearchQueryChange(query: String) {
        _uiState.update { currentState ->
            val filtered = applyFilter(currentState.selectedFilter, query, currentState.allItems)
            currentState.copy(
                searchQuery = query,
                filteredItems = filtered
            )
        }
    }

    fun toggleCreatePlaylistDialog(open: Boolean) {
        _uiState.update { it.copy(isCreatePlaylistOpen = open, newPlaylistName = if (!open) "" else it.newPlaylistName) }
    }

    fun toggleFollowArtistDialog(open: Boolean) {
        _uiState.update { it.copy(isFollowArtistOpen = open, newArtistName = if (!open) "" else it.newArtistName) }
    }

    fun onNewPlaylistNameChange(value: String) {
        _uiState.update { it.copy(newPlaylistName = value) }
    }

    fun onNewArtistNameChange(value: String) {
        _uiState.update { it.copy(newArtistName = value) }
    }

    fun followArtist() {
        val name = _uiState.value.newArtistName.trim()
        if (name.isBlank()) return

        LibraryRepository.followArtist(name)
        _uiState.update { it.copy(isFollowArtistOpen = false, newArtistName = "") }
    }

    fun createPlaylist() {
        val name = _uiState.value.newPlaylistName.trim()
        if (name.isBlank()) return

        LibraryRepository.createPlaylist(name)
        _uiState.update { currentState ->
            val items = buildLibraryItems()
            val filtered = applyFilter(currentState.selectedFilter, currentState.searchQuery, items)
            currentState.copy(
                allItems = items,
                filteredItems = filtered,
                isCreatePlaylistOpen = false,
                newPlaylistName = ""
            )
        }
    }
}
