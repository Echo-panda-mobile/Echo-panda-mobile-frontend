package com.example.echo_panda_mobile.presentation.viewsmodel

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.example.echo_panda_mobile.data.model.*
import com.example.echo_panda_mobile.data.remote.RetrofitClient
import com.example.echo_panda_mobile.data.repository.AuthRepository
import com.example.echo_panda_mobile.data.repository.LibraryRepository
import com.example.echo_panda_mobile.data.repository.MusicRepository
import com.example.echo_panda_mobile.data.repository.MusicResult
import com.example.echo_panda_mobile.data.repository.TokenStorage
import kotlinx.coroutines.async
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

class ArtistDetailViewModel(application: Application) : AndroidViewModel(application) {
    private val tokenStorage = TokenStorage(application)
    private val musicRepository = MusicRepository(RetrofitClient.getMusicService(tokenStorage))
    private val authRepository = AuthRepository()

    private val _uiState = MutableStateFlow(ArtistDetailUiState())
    val uiState: StateFlow<ArtistDetailUiState> = _uiState.asStateFlow()

    fun loadArtist(artistId: String) {
        viewModelScope.launch {
            _uiState.update { it.copy(isLoading = true, errorMessage = null) }
            
            val user = authRepository.getCurrentUser()
            
            // 1. Get artist detail directly from API
            val artistResult = musicRepository.getArtistById(artistId)
            
            if (artistResult is MusicResult.Success) {
                val artist = artistResult.data
                
                // 2. Load albums and songs for this artist in parallel
                val albumsDef = async { musicRepository.getArtistAlbums(artist.name) }
                val tracksDef = async { musicRepository.getArtistSongs(artist.name) }
                
                val albums = (albumsDef.await() as? MusicResult.Success)?.data ?: emptyList()
                val tracks = (tracksDef.await() as? MusicResult.Success)?.data ?: emptyList()

                _uiState.update { 
                    it.copy(
                        isLoading = false,
                        artist = artist,
                        popularTracks = tracks,
                        albums = albums,
                        singles = tracks.filter { t -> t.album == null || t.album == "null" },
                        currentUser = user,
                        isFollowing = LibraryRepository.followedArtists.value.any { followed -> followed.id == artistId }
                    )
                }
            } else {
                _uiState.update { 
                    it.copy(
                        isLoading = false, 
                        errorMessage = (artistResult as? MusicResult.Error)?.message ?: "Failed to load artist"
                    ) 
                }
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
