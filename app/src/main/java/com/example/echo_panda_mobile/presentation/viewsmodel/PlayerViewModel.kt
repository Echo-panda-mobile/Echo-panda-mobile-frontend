package com.example.echo_panda_mobile.presentation.viewsmodel

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.example.echo_panda_mobile.data.model.Track
import com.example.echo_panda_mobile.data.remote.RetrofitClient
import com.example.echo_panda_mobile.data.repository.MusicRepository
import com.example.echo_panda_mobile.data.repository.MusicResult
import com.example.echo_panda_mobile.data.repository.TokenStorage
import com.example.echo_panda_mobile.player.MusicPlayerManager
import androidx.media3.common.util.UnstableApi
import kotlin.OptIn
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch

data class PlayerUiState(
    val isLoading: Boolean = true,
    val track: Track? = null,
    val errorMessage: String? = null,
    val isPlaying: Boolean = false,
    val progress: Float = 0f,
    val streamUrl: String? = null,
    val streamExpiresAt: Long = 0,
    val currentPositionMs: Long = 0,
    val durationMs: Long = 0
)

@OptIn(UnstableApi::class)
class PlayerViewModel(application: Application) : AndroidViewModel(application) {
    private val tokenStorage = TokenStorage(application)
    private val musicRepository = MusicRepository(RetrofitClient.getMusicService(tokenStorage))
    private val playerManager = MusicPlayerManager.getInstance(application)

    private val _uiState = MutableStateFlow(PlayerUiState())
    val uiState: StateFlow<PlayerUiState> = _uiState.asStateFlow()

    init {
        // Observe player manager state
        viewModelScope.launch {
            combine(
                playerManager.isPlaying,
                playerManager.currentPosition,
                playerManager.duration
            ) { playing, position, duration ->
                Triple(playing, position, duration)
            }.collect { (playing, position, duration) ->
                _uiState.update { it.copy(
                    isPlaying = playing,
                    currentPositionMs = position,
                    durationMs = duration,
                    progress = if (duration > 0) position.toFloat() / duration else 0f
                ) }
            }
        }
    }

    fun loadTrack(trackId: String, resumePositionMs: Long? = null) {
        android.util.Log.d("PlayerViewModel", "loadTrack called with ID: $trackId, resume: $resumePositionMs")
        viewModelScope.launch {
            _uiState.update { it.copy(isLoading = true, errorMessage = null) }
            
            // 1. Get track metadata
            val trackResult = musicRepository.getTrackById(trackId)
            if (trackResult is MusicResult.Success) {
                val track = trackResult.data
                // Use provided resume point if available, otherwise check if metadata has one
                val resumeAt = resumePositionMs ?: track.resumePositionMs ?: 0L
                
                _uiState.update { it.copy(track = track) }
                
                // 2. Get stream ticket
                val ticketResult = musicRepository.getStreamTicket(trackId)
                if (ticketResult is MusicResult.Success) {
                    val ticket = ticketResult.data
                    val audioUrl = ticket.signedUrl ?: ticket.streamUrl ?: ticket.url
                    
                    if (audioUrl != null) {
                        _uiState.update { it.copy(
                            streamUrl = audioUrl,
                            streamExpiresAt = System.currentTimeMillis() + ((ticket.expiresInSeconds ?: 300) * 1000)
                        ) }
                        
                        // Start playing automatically with resume point
                        playerManager.play(audioUrl, track.title, track.artist, resumeAt)
                        
                        // 3. Add to listen history
                        musicRepository.addToListenHistory(trackId)
                    } else {
                        _uiState.update { it.copy(errorMessage = "Stream URL not found in response") }
                    }
                } else if (ticketResult is MusicResult.Error) {
                    _uiState.update { it.copy(errorMessage = ticketResult.message) }
                }
                
                _uiState.update { it.copy(isLoading = false) }
            } else if (trackResult is MusicResult.Error) {
                _uiState.update { it.copy(isLoading = false, errorMessage = trackResult.message) }
            }
        }
    }

    fun togglePlayPause() {
        val track = _uiState.value.track ?: return
        
        viewModelScope.launch {
            // Check if we need a new stream ticket or if we haven't started playing yet
            val isExpired = _uiState.value.streamUrl == null || 
                           System.currentTimeMillis() > (_uiState.value.streamExpiresAt - 10000)
            
            if (isExpired) {
                val result = musicRepository.getStreamTicket(track.id)
                if (result is MusicResult.Success) {
                    val ticket = result.data
                    val audioUrl = ticket.signedUrl ?: ticket.streamUrl ?: ticket.url
                    if (audioUrl != null) {
                        _uiState.update { it.copy(
                            streamUrl = audioUrl,
                            streamExpiresAt = System.currentTimeMillis() + ((ticket.expiresInSeconds ?: 300) * 1000)
                        ) }
                        playerManager.play(audioUrl, track.title, track.artist)
                    } else {
                        _uiState.update { it.copy(errorMessage = "Stream URL not found") }
                    }
                } else {
                    _uiState.update { it.copy(errorMessage = "Could not refresh stream ticket") }
                }
            } else {
                playerManager.togglePlayPause()
            }
        }
    }

    fun updateProgress(value: Float) {
        val duration = _uiState.value.durationMs
        if (duration > 0) {
            val seekPos = (value * duration).toLong()
            playerManager.seekTo(seekPos)
        }
    }

    fun toggleFavorite() {
        val currentTrack = _uiState.value.track ?: return
        viewModelScope.launch {
            // Optimistic update
            _uiState.update { it.copy(track = currentTrack.copy(isFavorite = !currentTrack.isFavorite)) }
            
            // Call repository
            val result = musicRepository.toggleFavorite(currentTrack.id, currentTrack.isFavorite)
            if (result is MusicResult.Error) {
                // Rollback if error
                _uiState.update { it.copy(track = currentTrack) }
            }
        }
    }

    fun downloadTrack() {
        val currentTrack = _uiState.value.track ?: return
        if (currentTrack.isDownloaded) return
        
        viewModelScope.launch {
            val result = musicRepository.downloadTrack(currentTrack.id)
            if (result is MusicResult.Success) {
                _uiState.update { it.copy(track = it.track?.copy(isDownloaded = true)) }
            }
        }
    }

}
