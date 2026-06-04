package com.example.echo_panda_mobile.presentation.viewsmodel

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.example.echo_panda_mobile.data.model.Album
import com.example.echo_panda_mobile.data.model.Artist
import com.example.echo_panda_mobile.data.model.Playlist
import com.example.echo_panda_mobile.data.model.Track
import com.example.echo_panda_mobile.data.remote.RetrofitClient
import com.example.echo_panda_mobile.data.repository.MusicRepository
import com.example.echo_panda_mobile.data.repository.MusicResult
import com.example.echo_panda_mobile.data.util.SearchMatcher
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
    val isRefreshing: Boolean = false,
    val allItems: List<LibraryItem> = emptyList(),
    val filteredItems: List<LibraryItem> = emptyList(),
    val selectedFilter: String = "All",
    val searchQuery: String = "",
    val isCreatePlaylistOpen: Boolean = false,
    val isFollowArtistOpen: Boolean = false,
    val newPlaylistName: String = "",
    val newArtistName: String = "",
    val likedSongsCount: Int = 0,
    val errorMessage: String? = null,
    val followArtistError: String? = null
)

sealed class LibraryItem {
    data class ArtistItem(val artist: Artist) : LibraryItem()
    data class PlaylistItem(
        val playlist: Playlist,
        val trackCount: Int? = null,
        val subtitle: String? = null
    ) : LibraryItem()
    data class AlbumItem(val album: Album) : LibraryItem()
    data class RecentTrackItem(val track: Track) : LibraryItem()
}

class LibraryViewModel(application: Application) : AndroidViewModel(application) {
    private val musicRepository = MusicRepository(
        RetrofitClient.getMusicService(TokenStorage(application))
    )

    private val _uiState = MutableStateFlow(LibraryState())
    val uiState: StateFlow<LibraryState> = _uiState.asStateFlow()

    private var cachedPlaylists: List<LibraryItem.PlaylistItem> = emptyList()
    private var cachedArtists: List<LibraryItem.ArtistItem> = emptyList()
    private var cachedAlbums: List<LibraryItem.AlbumItem> = emptyList()
    private var cachedRecent: List<LibraryItem.RecentTrackItem> = emptyList()
    private val pinnedArtists = mutableListOf<Artist>()

    init {
        loadLibrary()
    }

    fun refresh() {
        loadLibrary(isRefresh = true)
    }

    fun loadLibrary(isRefresh: Boolean = false) {
        viewModelScope.launch {
            _uiState.update { state ->
                state.copy(
                    isLoading = !isRefresh && state.allItems.isEmpty(),
                    isRefreshing = isRefresh,
                    errorMessage = null
                )
            }
            try {
                coroutineScope {
                    val playlistsDef = async { musicRepository.getPlaylistsWithSongCounts(perPage = 50) }
                    val artistsDef = async { musicRepository.getPopularArtists() }
                    val albumsDef = async { musicRepository.getPopularAlbums() }
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

                    val apiArtists = (artistsDef.await() as? MusicResult.Success)?.data ?: emptyList()
                    val mergedArtists = (pinnedArtists + apiArtists).distinctBy { it.id }
                    cachedArtists = mergedArtists.map { LibraryItem.ArtistItem(it) }

                    cachedAlbums = (albumsDef.await() as? MusicResult.Success)?.data?.map {
                        LibraryItem.AlbumItem(it)
                    } ?: emptyList()

                    cachedRecent = (recentDef.await() as? MusicResult.Success)?.data?.map {
                        LibraryItem.RecentTrackItem(it)
                    } ?: emptyList()

                    val favoritesCount = (favoritesDef.await() as? MusicResult.Success)?.data?.size ?: 0

                    val allItems = buildAllItems()
                    _uiState.update { state ->
                        state.copy(
                            isLoading = false,
                            isRefreshing = false,
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
                        isRefreshing = false,
                        errorMessage = e.localizedMessage ?: "Failed to load library"
                    )
                }
            }
        }
    }

    private fun buildAllItems(): List<LibraryItem> {
        return (cachedRecent.take(8) + cachedPlaylists + cachedArtists + cachedAlbums)
            .distinctBy { itemKey(it) }
    }

    private fun itemKey(item: LibraryItem): String = when (item) {
        is LibraryItem.ArtistItem -> "artist_${item.artist.id}"
        is LibraryItem.PlaylistItem -> "playlist_${item.playlist.id}"
        is LibraryItem.AlbumItem -> "album_${item.album.id}"
        is LibraryItem.RecentTrackItem -> "recent_${item.track.id}"
    }

    private fun applyFilter(filter: String, searchQuery: String, items: List<LibraryItem>): List<LibraryItem> {
        val base = when (filter) {
            "Recently" -> cachedRecent
            "Playlists" -> cachedPlaylists
            "Artists" -> cachedArtists
            "Albums" -> cachedAlbums
            else -> items
        }

        val query = searchQuery.trim()
        if (query.isBlank()) return base

        return base.filter { item ->
            when (item) {
                is LibraryItem.ArtistItem -> SearchMatcher.matches(query, item.artist.name)
                is LibraryItem.PlaylistItem -> SearchMatcher.matches(query, item.playlist.title)
                is LibraryItem.AlbumItem -> SearchMatcher.matches(query, item.album.title, item.album.artist)
                is LibraryItem.RecentTrackItem -> SearchMatcher.matches(query, item.track.title, item.track.artist)
            }
        }
    }

    fun setFilter(filter: String) {
        _uiState.update { currentState ->
            val newFilter = if (currentState.selectedFilter == filter) "All" else filter
            val allItems = buildAllItems()
            currentState.copy(
                selectedFilter = newFilter,
                allItems = allItems,
                filteredItems = applyFilter(newFilter, currentState.searchQuery, allItems)
            )
        }
    }

    fun toggleFavorite(track: Track) {
        val wasFavorite = track.isFavorite
        updateRecentFavorite(track.id, !wasFavorite)
        viewModelScope.launch {
            val result = musicRepository.toggleFavorite(track.id, wasFavorite)
            if (result is MusicResult.Error) {
                updateRecentFavorite(track.id, wasFavorite)
            }
        }
    }

    private fun updateRecentFavorite(trackId: String, isFavorite: Boolean) {
        cachedRecent = cachedRecent.map { item ->
            if (item.track.id == trackId) item.copy(track = item.track.copy(isFavorite = isFavorite)) else item
        }
        _uiState.update { state ->
            val allItems = buildAllItems()
            state.copy(
                allItems = allItems,
                filteredItems = applyFilter(state.selectedFilter, state.searchQuery, allItems)
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
        "Artists" -> "Artists"
        "Albums" -> "Albums"
        else -> "Your library"
    }

    fun toggleCreatePlaylistDialog(open: Boolean) {
        _uiState.update { it.copy(isCreatePlaylistOpen = open, newPlaylistName = if (!open) "" else it.newPlaylistName) }
    }

    fun toggleFollowArtistDialog(open: Boolean) {
        _uiState.update {
            it.copy(
                isFollowArtistOpen = open,
                newArtistName = if (!open) "" else it.newArtistName,
                followArtistError = if (!open) null else it.followArtistError
            )
        }
    }

    fun onNewPlaylistNameChange(value: String) {
        _uiState.update { it.copy(newPlaylistName = value) }
    }

    fun onNewArtistNameChange(value: String) {
        _uiState.update { it.copy(newArtistName = value, followArtistError = null) }
    }

    fun followArtist() {
        val name = _uiState.value.newArtistName.trim()
        if (name.isBlank()) return

        viewModelScope.launch {
            when (val result = musicRepository.getPopularArtists()) {
                is MusicResult.Success -> {
                    val match = result.data.firstOrNull { it.name.equals(name, ignoreCase = true) }
                        ?: result.data.firstOrNull { it.name.contains(name, ignoreCase = true) }
                    if (match != null) {
                        if (pinnedArtists.none { it.id == match.id }) {
                            pinnedArtists.add(0, match)
                        }
                        val apiArtists = result.data
                        cachedArtists = (pinnedArtists + apiArtists)
                            .distinctBy { it.id }
                            .map { LibraryItem.ArtistItem(it) }
                        val allItems = buildAllItems()
                        _uiState.update { state ->
                            state.copy(
                                isFollowArtistOpen = false,
                                newArtistName = "",
                                followArtistError = null,
                                allItems = allItems,
                                filteredItems = applyFilter(state.selectedFilter, state.searchQuery, allItems)
                            )
                        }
                    } else {
                        _uiState.update { it.copy(followArtistError = "Artist not found") }
                    }
                }
                is MusicResult.Error -> {
                    _uiState.update { it.copy(followArtistError = result.message) }
                }
                else -> {}
            }
        }
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
