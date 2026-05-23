package com.example.echo_panda_mobile.presentation.viewmodel

import androidx.lifecycle.ViewModel
import com.example.echo_panda_mobile.data.model.Track
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update

data class GlobalPlayerState(
    val currentTrack: Track? = null,
    val isPlaying: Boolean = false,
    val progress: Float = 0.4f
)

class GlobalPlayerViewModel : ViewModel() {
    private val _playerState = MutableStateFlow(GlobalPlayerState())
    val playerState: StateFlow<GlobalPlayerState> = _playerState.asStateFlow()

    fun playTrack(track: Track) {
        _playerState.update { it.copy(currentTrack = track, isPlaying = true) }
    }

    fun togglePlayPause() {
        _playerState.update { it.copy(isPlaying = !it.isPlaying) }
    }

    fun nextTrack() {
        // Logic for next track
    }

    fun previousTrack() {
        // Logic for previous track
    }
}