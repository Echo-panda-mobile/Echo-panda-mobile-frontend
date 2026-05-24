package com.example.echo_panda_mobile.presentation.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.echo_panda_mobile.data.model.*
import com.example.echo_panda_mobile.data.repository.AuthRepository
import com.example.echo_panda_mobile.data.repository.MusicRepository
import com.example.echo_panda_mobile.data.repository.MusicResult
import kotlinx.coroutines.async
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import kotlinx.coroutines.withTimeoutOrNull

data class HomeUiState(
    val isLoading: Boolean          = true,
    val recentPlaylists: List<Playlist>    = emptyList(),
    val popularArtists: List<Artist>       = emptyList(),
    val topAlbums: List<Album>             = emptyList(),
    val topMixes: List<Playlist>           = emptyList(),
    val recentListening: List<Playlist>    = emptyList(),
    val featuredArtist: FeaturedArtist?    = null,
    val userName: String                   = "User",
    val errorMessage: String?              = null
)

class HomeViewModel(
    private val musicRepository: MusicRepository = MusicRepository(),
    private val authRepository: AuthRepository = AuthRepository()
) : ViewModel() {

    private val _uiState = MutableStateFlow(HomeUiState())
    val uiState: StateFlow<HomeUiState> = _uiState.asStateFlow()

    init { loadAll() }

    private fun loadAll() {
        viewModelScope.launch {
            _uiState.update { it.copy(isLoading = true) }

            try {
                // Load user profile with a timeout to prevent hanging on Firestore issues
                val user = withTimeoutOrNull(3000) {
                    authRepository.getCurrentUser()
                }

                // Load music data in parallel
                val recentDef     = async { musicRepository.getRecentPlaylists() }
                val artistsDef    = async { musicRepository.getPopularArtists() }
                val albumsDef     = async { musicRepository.getTopAlbums() }
                val featuredDef   = async { musicRepository.getFeaturedArtist() }
                val recentListenDef = async { musicRepository.getRecentListening() }
                
                val recent        = (recentDef.await()       as? MusicResult.Success)?.data ?: emptyList()
                val artists       = (artistsDef.await()      as? MusicResult.Success)?.data ?: emptyList()
                val albums        = (albumsDef.await()       as? MusicResult.Success)?.data ?: emptyList()
                val featured      = (featuredDef.await()     as? MusicResult.Success)?.data
                val recentListen  = (recentListenDef.await() as? MusicResult.Success)?.data ?: emptyList()

                _uiState.update { 
                    it.copy(
                        isLoading        = false,
                        userName         = user?.name ?: "User",
                        recentPlaylists  = recent,
                        popularArtists   = artists,
                        topAlbums        = albums,
                        featuredArtist   = featured,
                        recentListening  = recentListen
                    )
                }
            } catch (e: Exception) {
                _uiState.update { 
                    it.copy(isLoading = false, errorMessage = "Failed to load: ${e.message}") 
                }
            } finally {
                _uiState.update { it.copy(isLoading = false) }
            }
        }
    }

    fun refresh() = loadAll()
}
