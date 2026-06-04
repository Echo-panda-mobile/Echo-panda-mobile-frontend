package com.example.echo_panda_mobile.presentation.viewsmodel

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.example.echo_panda_mobile.data.model.Playlist
import com.example.echo_panda_mobile.data.model.Track
import com.example.echo_panda_mobile.data.remote.RetrofitClient
import com.example.echo_panda_mobile.data.repository.MusicRepository
import com.example.echo_panda_mobile.data.repository.MusicResult
import com.example.echo_panda_mobile.data.repository.TokenStorage
import kotlinx.coroutines.async
import kotlinx.coroutines.coroutineScope
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

data class LibraryState(
    val isLoading: Boolean = false,
    val allItems: List<LibraryItem> = emptyList(),
    val filteredItems: List<LibraryItem> = emptyList(),
    val selectedFilter: String = "Recently",
    val searchQuery: String = "",
    val isCreatePlaylistOpen: Boolean = false,
    val newPlaylistName: String = "",
    val likedSongsCount: Int = 0,
    val errorMessage: String? = null
)

sealed class LibraryItem {
    data class PlaylistItem(
        val playlist: Playlist,
        val trackCount: Int? = null,
        val subtitle: String? = null
    ) : LibraryItem()
    data class RecentTrackItem(val track: Track) : LibraryItem()
}

class LibraryViewModel(application: Application) : AndroidViewModel(application) {
    private val musicRepository = MusicRepository(
        RetrofitClient.getMusicService(TokenStorage(application))
    )

    private val _uiState = MutableStateFlow(LibraryState())
    val uiState: StateFlow<LibraryState> = _uiState.asStateFlow()

    private var cachedPlaylists: List<LibraryItem.PlaylistItem> = emptyList()
    private var cachedRecent: List<LibraryItem.RecentTrackItem> = emptyList()

    init {
        loadLibrary()
    }

    fun loadLibrary() {
        viewModelScope.launch {
            _uiState.update { it.copy(isLoading = true, errorMessage = null) }
            try {
                coroutineScope {
                    val playlistsDef = async { musicRepository.getPlaylistsWithSongCounts(perPage = 50) }
                    val recentDef = async { musicRepository.getRecentlyPlayedTracks() }
                    val favoritesDef = async { musicRepository.getFavoriteTracks() }

                    cachedPlaylists = when (val result = playlistsDef.await()) {
                        is MusicResult.Success -> result.data.map { (playlist, count) ->
                            LibraryItem.PlaylistItem(
                                playlist = playlist,
                                trackCount = count,
                                subtitle = "$count songs"
                            )
                        }
                        else -> emptyList()
                    }

                    cachedRecent = (recentDef.await() as? MusicResult.Success)?.data?.map {
                        LibraryItem.RecentTrackItem(it)
                    } ?: emptyList()

                    val favoritesCount = (favoritesDef.await() as? MusicResult.Success)?.data?.size ?: 0

                    val allItems = buildAllItems()
                    _uiState.update { state ->
                        state.copy(
                            isLoading = false,
                            likedSongsCount = favoritesCount,
                            allItems = allItems,
                            filteredItems = applyFilter(state.selectedFilter, state.searchQuery, allItems)
                        )
                    }
                }
            } catch (e: Exception) {
                _uiState.update {
                    it.copy(
                        isLoading = false,
                        errorMessage = e.localizedMessage ?: "Failed to load library"
                    )
                }
            }
        }
    }

    private fun buildAllItems(): List<LibraryItem> {
        return (cachedRecent.take(8) + cachedPlaylists)
            .distinctBy { itemKey(it) }
    }

    private fun itemKey(item: LibraryItem): String = when (item) {
        is LibraryItem.PlaylistItem -> "playlist_${item.playlist.id}"
        is LibraryItem.RecentTrackItem -> "recent_${item.track.id}"
    }

    private fun applyFilter(filter: String, searchQuery: String, items: List<LibraryItem>): List<LibraryItem> {
        val base = when (filter) {
            "Recently" -> cachedRecent
            "Playlists" -> cachedPlaylists
            else -> items
        }

        val query = searchQuery.trim().lowercase()
        if (query.isBlank()) return base

        return base.filter { item ->
            when (item) {
                is LibraryItem.PlaylistItem -> item.playlist.title.lowercase().contains(query)
                is LibraryItem.RecentTrackItem ->
                    item.track.title.lowercase().contains(query) ||
                        item.track.artist.lowercase().contains(query)
            }
        }
    }

    fun setFilter(filter: String) {
        _uiState.update { currentState ->
            val allItems = buildAllItems()
            currentState.copy(
                selectedFilter = filter,
                allItems = allItems,
                filteredItems = applyFilter(filter, currentState.searchQuery, allItems)
            )
        }
    }

    fun onSearchQueryChange(query: String) {
        _uiState.update { currentState ->
            val allItems = buildAllItems()
            currentState.copy(
                searchQuery = query,
                allItems = allItems,
                filteredItems = applyFilter(currentState.selectedFilter, query, allItems)
            )
        }
    }

    fun sectionTitle(filter: String): String = when (filter) {
        "Recently" -> "Recently played"
        "Playlists" -> "Your playlists"
        else -> "Your library"
    }

    fun toggleCreatePlaylistDialog(open: Boolean) {
        _uiState.update { it.copy(isCreatePlaylistOpen = open, newPlaylistName = if (!open) "" else it.newPlaylistName) }
    }

    fun onNewPlaylistNameChange(value: String) {
        _uiState.update { it.copy(newPlaylistName = value) }
    }

    fun createPlaylist() {
        val name = _uiState.value.newPlaylistName.trim()
        if (name.isBlank()) return

        viewModelScope.launch {
            _uiState.update { it.copy(isLoading = true) }
            when (val result = musicRepository.createPlaylist(name)) {
                is MusicResult.Success -> {
                    loadLibrary()
                    _uiState.update { it.copy(isCreatePlaylistOpen = false, newPlaylistName = "") }
                }
                is MusicResult.Error -> {
                    _uiState.update {
                        it.copy(isLoading = false, errorMessage = result.message)
                    }
                }
                else -> {
                    _uiState.update { it.copy(isLoading = false) }
                }
            }
        }
    }

    fun clearError() {
        _uiState.update { it.copy(errorMessage = null) }
    }
}
