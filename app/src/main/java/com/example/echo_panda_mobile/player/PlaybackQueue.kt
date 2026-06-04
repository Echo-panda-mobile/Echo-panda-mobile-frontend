package com.example.echo_panda_mobile.player

import com.example.echo_panda_mobile.data.model.Track
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow

/**
 * In-memory play queue for skip next / previous. Set when the user starts playback
 * from a list (album, playlist, favorites, etc.).
 */
object PlaybackQueue {

    private var tracks: List<Track> = emptyList()
    private var currentIndex: Int = 0

    private val _currentTrack = MutableStateFlow<Track?>(null)
    val currentTrack: StateFlow<Track?> = _currentTrack.asStateFlow()

    fun setQueue(newTracks: List<Track>, startTrackId: String) {
        if (newTracks.isEmpty()) return
        tracks = newTracks
        currentIndex = newTracks.indexOfFirst { it.id == startTrackId }.let { idx ->
            if (idx >= 0) idx else 0
        }
        publishCurrent()
    }

    fun setQueue(newTracks: List<Track>, startIndex: Int) {
        if (newTracks.isEmpty()) return
        tracks = newTracks
        currentIndex = startIndex.coerceIn(0, newTracks.lastIndex)
        publishCurrent()
    }

    fun setSingle(track: Track) {
        setQueue(listOf(track), 0)
    }

    fun contains(trackId: String): Boolean = tracks.any { it.id == trackId }

    fun syncToTrack(trackId: String) {
        val idx = tracks.indexOfFirst { it.id == trackId }
        if (idx >= 0) {
            currentIndex = idx
            publishCurrent()
        }
    }

    fun current(): Track? = tracks.getOrNull(currentIndex)

    fun hasNext(): Boolean = currentIndex < tracks.lastIndex

    fun hasPrevious(): Boolean = currentIndex > 0

    fun next(): Track? {
        if (!hasNext()) return null
        currentIndex++
        return publishCurrent()
    }

    fun previous(): Track? {
        if (!hasPrevious()) return null
        currentIndex--
        return publishCurrent()
    }

    fun clear() {
        tracks = emptyList()
        currentIndex = 0
        _currentTrack.value = null
    }

    private fun publishCurrent(): Track? =
        tracks.getOrNull(currentIndex)?.also { _currentTrack.value = it }
}
