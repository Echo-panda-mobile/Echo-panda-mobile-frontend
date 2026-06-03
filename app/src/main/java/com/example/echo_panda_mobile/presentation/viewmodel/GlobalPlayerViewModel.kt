package com.example.echo_panda_mobile.presentation.viewmodel

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

data class GlobalPlayerState(
    val currentTrack: Track? = null,
    val isPlaying: Boolean = false,
    val progress: Float = 0f
)

@UnstableApi
class GlobalPlayerViewModel(application: Application) : AndroidViewModel(application) {
    private val tokenStorage = TokenStorage(application)
    private val musicRepository = MusicRepository(RetrofitClient.getMusicService(tokenStorage))
    private val playerManager = MusicPlayerManager.getInstance(application)
    
    private val _playerState = MutableStateFlow(GlobalPlayerState())
    val playerState: StateFlow<GlobalPlayerState> = _playerState.asStateFlow()

    private var lastLoadedTrackId: String? = null

    init {
        viewModelScope.launch {
            combine(
                playerManager.currentTrack,
                playerManager.isPlaying,
                playerManager.currentPosition,
                playerManager.duration
            ) { track, playing, position, duration ->
                GlobalPlayerState(
                    currentTrack = track,
                    isPlaying = playing,
                    progress = if (duration > 0) position.toFloat() / duration else 0f
                )
            }.collect { state ->
                _playerState.value = state
            }
        }

        // Auto-load and play when track changes in the manager (queue skip, etc.)
        viewModelScope.launch {
            playerManager.currentTrack.collect { track ->
                if (track != null && track.id != lastLoadedTrackId) {
                    loadAndPlay(track)
                }
            }
        }
    }

    private suspend fun loadAndPlay(track: Track) {
        lastLoadedTrackId = track.id
        
        // 1. Get fresh stream ticket
        val ticketResult = musicRepository.getStreamTicket(track.id)
        if (ticketResult is MusicResult.Success) {
            val ticket = ticketResult.data
            val audioUrl = ticket.signedUrl ?: ticket.streamUrl ?: ticket.url
            
            if (audioUrl != null) {
                // 2. Play in manager
                playerManager.play(audioUrl, track.title, track.artist, track.album)
                
                // 3. Log to history
                musicRepository.addToListenHistory(track.id)
            }
        }
    }

    fun playTrack(track: Track) {
        // Just set as single item queue and start
        playerManager.setQueue(listOf(track), track.id)
    }

    fun setQueue(tracks: List<Track>, startIndex: Int) {
        playerManager.setQueue(tracks, tracks.getOrNull(startIndex)?.id)
    }

    fun togglePlayPause() {
        playerManager.togglePlayPause()
    }

    fun nextTrack() {
        playerManager.next()
    }

    fun previousTrack() {
        playerManager.previous()
    }

    fun stopPlayback() {
        playerManager.stop()
        lastLoadedTrackId = null
    }
}