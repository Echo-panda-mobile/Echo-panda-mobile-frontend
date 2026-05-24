package com.example.echo_panda_mobile.presentation.viewsmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.echo_panda_mobile.data.model.*
import com.example.echo_panda_mobile.data.repository.AuthRepository
import com.example.echo_panda_mobile.data.repository.LibraryRepository
import com.example.echo_panda_mobile.data.repository.MusicRepository
import com.example.echo_panda_mobile.data.repository.MusicResult
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

data class ArtistDetailUiState(
    val isLoading: Boolean = true,
    val artist: Artist? = null,
    val popularTracks: List<Track> = emptyList(),
    val albums: List<Album> = emptyList(),
    val singles: List<Track> = emptyList(),
    val isFollowing: Boolean = false,
    val currentUser: User? = null,
    val errorMessage: String? = null
)

class ArtistDetailViewModel(
    private val musicRepository: MusicRepository = MusicRepository(),
    private val authRepository: AuthRepository = AuthRepository()
) : ViewModel() {

    private val _uiState = MutableStateFlow(ArtistDetailUiState())
    val uiState: StateFlow<ArtistDetailUiState> = _uiState.asStateFlow()

    fun loadArtist(artistId: String) {
        viewModelScope.launch {
            _uiState.update { it.copy(isLoading = true) }
            
            val user = authRepository.getCurrentUser()
            val artistsResult = musicRepository.getPopularArtists()
            val albumsResult = musicRepository.getTopAlbums()
            val singlesResult = musicRepository.getNewReleases()

            if (artistsResult is MusicResult.Success) {
                val artist = artistsResult.data.find { it.id == artistId } ?: artistsResult.data.first()
                
                // Mocking tracks for the artist dynamically
                val mockTracks = if (artist.name.lowercase().contains("jennie")) {
                    listOf(
                        Track("p1", "Dracula - JENNIE Remix", artist.name, placeholderColors = artist.placeholderColors),
                        Track("p2", "One Of The Girls", artist.name, placeholderColors = artist.placeholderColors),
                        Track("p3", "like JENNIE", artist.name, placeholderColors = artist.placeholderColors),
                        Track("p4", "Mantra", artist.name, placeholderColors = artist.placeholderColors),
                        Track("p5", "Solo", artist.name, placeholderColors = artist.placeholderColors)
                    )
                } else {
                    listOf(
                        Track("tr1", "Popular Song 1", artist.name, placeholderColors = artist.placeholderColors),
                        Track("tr2", "Hit Single 2", artist.name, placeholderColors = artist.placeholderColors),
                        Track("tr3", "Trending Track 3", artist.name, placeholderColors = artist.placeholderColors),
                        Track("tr4", "Classic Album Cut 4", artist.name, placeholderColors = artist.placeholderColors),
                        Track("tr5", "Fan Favorite 5", artist.name, placeholderColors = artist.placeholderColors)
                    )
                }

                _uiState.update { 
                    it.copy(
                        isLoading = false,
                        artist = artist,
                        popularTracks = mockTracks,
                        albums = if (albumsResult is MusicResult.Success) albumsResult.data else emptyList(),
                        singles = if (singlesResult is MusicResult.Success) singlesResult.data else emptyList(),
                        currentUser = user,
                        isFollowing = LibraryRepository.followedArtists.value.any { it.id == artistId }
                    )
                }
            } else {
                _uiState.update { it.copy(isLoading = false, errorMessage = "Failed to load artist") }
            }
        }
    }

    fun toggleFollow() {
        val artist = _uiState.value.artist ?: return
        viewModelScope.launch {
            if (_uiState.value.isFollowing) {
                // LibraryRepository should ideally have an unfollow
                // For now we just follow (Add Favorite) as requested
            } else {
                LibraryRepository.followArtist(artist.name)
            }
            _uiState.update { it.copy(isFollowing = !it.isFollowing) }
        }
    }

    fun addToFavorites(track: Track) {
        LibraryRepository.addTrackToFavorites(track)
    }

    fun addToPlaylist(track: Track, playlistId: String) {
        LibraryRepository.addTrackToPlaylist(track, playlistId)
    }
}
