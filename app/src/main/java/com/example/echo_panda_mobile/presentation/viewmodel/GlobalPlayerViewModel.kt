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
import com.example.echo_panda_mobile.player.PlaybackQueue
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

data class GlobalPlayerState(
    val currentTrack: Track? = null,
    val isPlaying: Boolean = false,
    val progress: Float = 0f
)

class GlobalPlayerViewModel(application: Application) : AndroidViewModel(application) {

    private val tokenStorage = TokenStorage(application)
    private val musicRepository = MusicRepository(RetrofitClient.getMusicService(tokenStorage))
    private val playerManager = MusicPlayerManager.getInstance(application)

    private val _playerState = MutableStateFlow(GlobalPlayerState())
    val playerState: StateFlow<GlobalPlayerState> = _playerState.asStateFlow()

    init {
        viewModelScope.launch {
            PlaybackQueue.currentTrack.collect { track ->
                if (track != null) {
                    _playerState.update { it.copy(currentTrack = track) }
                }
            }
        }
        viewModelScope.launch {
            combine(
                playerManager.isPlaying,
                playerManager.currentPosition,
                playerManager.duration
            ) { playing, position, duration ->
                Triple(playing, position, duration)
            }.collect { (playing, position, duration) ->
                _playerState.update {
                    it.copy(
                        isPlaying = playing,
                        progress = if (duration > 0) position.toFloat() / duration else 0f
                    )
                }
            }
        }
    }

    fun playTrack(track: Track) {
        playQueue(listOf(track), startTrackId = track.id)
    }

    fun playQueue(tracks: List<Track>, startTrackId: String) {
        if (tracks.isEmpty()) return
        PlaybackQueue.setQueue(tracks, startTrackId)
        PlaybackQueue.current()?.let { track ->
            _playerState.update { it.copy(currentTrack = track, isPlaying = true) }
        }
    }

    fun playQueue(tracks: List<Track>, startIndex: Int) {
        if (tracks.isEmpty()) return
        PlaybackQueue.setQueue(tracks, startIndex)
        PlaybackQueue.current()?.let { track ->
            _playerState.update { it.copy(currentTrack = track, isPlaying = true) }
        }
    }

    fun togglePlayPause() {
        playerManager.togglePlayPause()
    }

    fun nextTrack() {
        viewModelScope.launch {
            val next = PlaybackQueue.next() ?: return@launch
            startPlayback(next)
        }
    }

    fun previousTrack() {
        viewModelScope.launch {
            if (playerManager.currentPosition.value > 3_000L) {
                playerManager.seekTo(0)
                return@launch
            }
            val previous = PlaybackQueue.previous()
            if (previous != null) {
                startPlayback(previous)
            } else {
                playerManager.seekTo(0)
            }
        }
    }

    private suspend fun startPlayback(track: Track, resumePositionMs: Long = 0L) {
        _playerState.update { it.copy(currentTrack = track) }
        when (val ticketResult = musicRepository.getStreamTicket(track.id)) {
            is MusicResult.Success -> {
                val audioUrl = ticketResult.data.signedUrl
                    ?: ticketResult.data.streamUrl
                    ?: ticketResult.data.url
                if (audioUrl != null) {
                    playerManager.play(audioUrl, track.title, track.artist, resumePositionMs)
                }
            }
            else -> Unit
        }
    }
}
