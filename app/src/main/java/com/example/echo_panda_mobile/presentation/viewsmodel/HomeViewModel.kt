package com.example.echo_panda_mobile.presentation.viewmodel

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.example.echo_panda_mobile.data.model.*
import com.example.echo_panda_mobile.data.remote.RetrofitClient
import com.example.echo_panda_mobile.data.repository.AuthRepository
import com.example.echo_panda_mobile.data.repository.MusicRepository
import com.example.echo_panda_mobile.data.repository.MusicResult
import com.example.echo_panda_mobile.data.repository.TokenStorage
import kotlinx.coroutines.async
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

data class HomeUiState(
    val isLoading: Boolean          = true,
    val recentPlaylists: List<Playlist>    = emptyList(),
    val popularArtists: List<Artist>       = emptyList(),
    val topAlbums: List<Album>             = emptyList(),
    val topMixes: List<Playlist>           = emptyList(),
    val recentListening: List<Playlist>    = emptyList(),
    val featuredArtist: FeaturedArtist?    = null,
    val userName: String                   = "User",
    val userPhotoUrl: String?              = null,
    val errorMessage: String?              = null
)

class HomeViewModel(application: Application) : AndroidViewModel(application) {
    private val tokenStorage = TokenStorage(application)
    private val musicRepository = MusicRepository(RetrofitClient.getMusicService(tokenStorage))
    private val authRepository = AuthRepository(TokenStorage(application))

    private val _uiState = MutableStateFlow(HomeUiState())
    val uiState: StateFlow<HomeUiState> = _uiState.asStateFlow()

    init { loadAll() }

    private fun loadAll() {
        viewModelScope.launch {
            _uiState.update { it.copy(isLoading = true) }

            try {
                val user = authRepository.getCachedUser()
                val profilePhotoUrl = authRepository.resolveProfilePhotoUrl()

                // Load music data in parallel
                val recentDef     = async { musicRepository.getRecentPlaylists() }
                val artistsDef    = async { musicRepository.getPopularArtists() }
                val albumsDef     = async { musicRepository.getTopAlbums() }
                val featuredDef   = async { musicRepository.getFeaturedArtist() }
                val recentListenDef = async { musicRepository.getRecentListening() }
                
                val recent        = (recentDef.await()       as? MusicResult.Success<*>?)?.data as? List<Playlist> ?: emptyList()
                val artists       = (artistsDef.await()      as? MusicResult.Success<*>?)?.data as? List<Artist>   ?: emptyList()
                val albums        = (albumsDef.await()       as? MusicResult.Success<*>?)?.data as? List<Album>    ?: emptyList()
                val featured      = (featuredDef.await()     as? MusicResult.Success<*>?)?.data as? FeaturedArtist
                val recentListen  = (recentListenDef.await() as? MusicResult.Success<*>?)?.data as? List<Playlist> ?: emptyList()

                _uiState.update { 
                    it.copy(
                        isLoading        = false,
                        userName         = user?.name ?: "User",
                        userPhotoUrl     = profilePhotoUrl ?: user?.photoUrl,
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

    fun refreshContinueListening() {
        viewModelScope.launch {
            try {
                val recentDef = async { musicRepository.getRecentPlaylists() }
                val recentListenDef = async { musicRepository.getRecentListening() }
                val recent = (recentDef.await() as? MusicResult.Success<*>?)?.data as? List<Playlist> ?: emptyList()
                val recentListen =
                    (recentListenDef.await() as? MusicResult.Success<*>?)?.data as? List<Playlist> ?: emptyList()
                _uiState.update { it.copy(recentPlaylists = recent, recentListening = recentListen) }
            } catch (_: Exception) {
                // Keep existing lists on transient failure
            }
        }
    }

    fun refreshUserHeader() {
        viewModelScope.launch {
            val user = authRepository.getCachedUser()
            val profilePhotoUrl = authRepository.resolveProfilePhotoUrl()
            _uiState.update {
                it.copy(
                    userName = user?.name ?: it.userName,
                    userPhotoUrl = profilePhotoUrl ?: user?.photoUrl
                )
            }
        }
    }
}
