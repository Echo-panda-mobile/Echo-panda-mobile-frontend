package com.example.echo_panda_mobile.player

import android.content.Context
import android.util.Log
import androidx.media3.common.MediaItem
import androidx.media3.common.MediaMetadata
import androidx.media3.common.Player
import androidx.media3.common.util.UnstableApi
import androidx.media3.common.util.Util
import androidx.media3.datasource.okhttp.OkHttpDataSource
import androidx.media3.exoplayer.ExoPlayer
import androidx.media3.exoplayer.source.DefaultMediaSourceFactory
import com.example.echo_panda_mobile.data.remote.RetrofitClient
import com.example.echo_panda_mobile.data.repository.TokenStorage
import kotlinx.coroutines.*
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow

@UnstableApi
class MusicPlayerManager(context: Context) {
    private val TAG = "MusicPlayerManager"
    private val tokenStorage = TokenStorage(context)
    
    // Use OkHttp for better compatibility and to leverage AuthInterceptor
    private val dataSourceFactory = OkHttpDataSource.Factory(
        RetrofitClient.getOkHttpClient(tokenStorage).newBuilder()
            .addInterceptor { chain ->
                val request = chain.request()
                
                // Add Range header if missing (required for some streaming backends)
                val newRequestBuilder = request.newBuilder()
                if (request.header("Range") == null) {
                    newRequestBuilder.header("Range", "bytes=0-")
                }
                
                chain.proceed(newRequestBuilder.build())
            }
            .build()
    ).setUserAgent(Util.getUserAgent(context, "EchoPanda"))

    private val exoPlayer = ExoPlayer.Builder(context)
        .setMediaSourceFactory(
            DefaultMediaSourceFactory(context)
                .setDataSourceFactory(dataSourceFactory)
        )
        .build()
    
    private val _isPlaying = MutableStateFlow(false)
    val isPlaying = _isPlaying.asStateFlow()
    
    private val _currentPosition = MutableStateFlow(0L)
    val currentPosition = _currentPosition.asStateFlow()
    
    private val _duration = MutableStateFlow(0L)
    val duration = _duration.asStateFlow()

    private val _currentTrack = MutableStateFlow<com.example.echo_panda_mobile.data.model.Track?>(null)
    val currentTrack = _currentTrack.asStateFlow()

    private var queue = mutableListOf<com.example.echo_panda_mobile.data.model.Track>()
    private var currentIndex = -1

    private val playerScope = CoroutineScope(Dispatchers.Main + SupervisorJob())

    init {
        exoPlayer.addListener(object : Player.Listener {
            override fun onIsPlayingChanged(playing: Boolean) {
                Log.d(TAG, "onIsPlayingChanged: $playing")
                _isPlaying.value = playing
                if (playing) {
                    startPositionUpdates()
                } else {
                    stopPositionUpdates()
                }
            }

            override fun onPlaybackStateChanged(state: Int) {
                val stateStr = when(state) {
                    Player.STATE_IDLE -> "IDLE"
                    Player.STATE_BUFFERING -> "BUFFERING"
                    Player.STATE_READY -> "READY"
                    Player.STATE_ENDED -> "ENDED"
                    else -> "UNKNOWN"
                }
                Log.d(TAG, "onPlaybackStateChanged: $stateStr")
                if (state == Player.STATE_READY) {
                    _duration.value = exoPlayer.duration
                }
                if (state == Player.STATE_ENDED) {
                    next()
                }
            }

            override fun onMediaItemTransition(mediaItem: MediaItem?, reason: Int) {
                super.onMediaItemTransition(mediaItem, reason)
                Log.d(TAG, "onMediaItemTransition: ${mediaItem?.mediaMetadata?.title}")
                // Update current track state if needed
            }

            override fun onPlayerError(error: androidx.media3.common.PlaybackException) {
                Log.e(TAG, "onPlayerError: ${error.errorCodeName} (${error.errorCode})", error)
                if (error.cause is androidx.media3.datasource.HttpDataSource.InvalidResponseCodeException) {
                    val httpError = error.cause as androidx.media3.datasource.HttpDataSource.InvalidResponseCodeException
                    Log.e(TAG, "HTTP Error Code: ${httpError.responseCode}")
                    Log.e(TAG, "HTTP Headers: ${httpError.headerFields}")
                }
            }
        })
    }

    private var positionJob: Job? = null

    private fun startPositionUpdates() {
        positionJob?.cancel()
        positionJob = playerScope.launch {
            while (isActive) {
                _currentPosition.value = exoPlayer.currentPosition
                delay(500) // Update more frequently for smoother UI
            }
        }
    }

    private fun stopPositionUpdates() {
        positionJob?.cancel()
    }

    fun play(url: String?, title: String?, artist: String?, album: String? = null, resumePositionMs: Long = 0) {
        if (url.isNullOrBlank()) {
            Log.e(TAG, "Cannot play: URL is null or empty")
            return
        }
        Log.d(TAG, "Attempting to play: $url at position $resumePositionMs")
        
        // Stop any current playback and clear state
        exoPlayer.stop()
        exoPlayer.clearMediaItems()
        
        val mediaItem = MediaItem.Builder()
            .setUri(url)
            .setMediaId(url)
            .setMediaMetadata(
                MediaMetadata.Builder()
                    .setTitle(title ?: "Unknown Title")
                    .setArtist(artist ?: "Unknown Artist")
                    .setAlbumTitle(album ?: "Unknown Album")
                    .build()
            )
            .build()
        
        exoPlayer.setMediaItem(mediaItem)
        if (resumePositionMs > 0) {
            exoPlayer.seekTo(resumePositionMs)
        }
        exoPlayer.prepare()
        exoPlayer.play()
    }

    fun setQueue(tracks: List<com.example.echo_panda_mobile.data.model.Track>, startAtId: String? = null) {
        queue = tracks.toMutableList()
        currentIndex = if (startAtId != null) {
            queue.indexOfFirst { it.id == startAtId }.coerceAtLeast(0)
        } else {
            0
        }
        if (queue.isNotEmpty()) {
            _currentTrack.value = queue[currentIndex]
        }
    }

    fun next() {
        if (queue.isEmpty()) return
        currentIndex = (currentIndex + 1) % queue.size
        _currentTrack.value = queue[currentIndex]
        // This will trigger the PlayerViewModel or whoever observes currentTrack to load the new URL
    }

    fun previous() {
        if (queue.isEmpty()) return
        
        // If we're more than 3 seconds into the song, just restart it
        if (exoPlayer.currentPosition > 3000) {
            exoPlayer.seekTo(0)
            return
        }
        
        currentIndex = if (currentIndex <= 0) queue.size - 1 else currentIndex - 1
        _currentTrack.value = queue[currentIndex]
    }

    fun togglePlayPause() {
        if (exoPlayer.isPlaying) {
            exoPlayer.pause()
        } else {
            exoPlayer.play()
        }
    }

    fun seekTo(positionMs: Long) {
        exoPlayer.seekTo(positionMs)
    }

    fun stop() {
        exoPlayer.stop()
        exoPlayer.clearMediaItems()
        _currentTrack.value = null
        _isPlaying.value = false
        _currentPosition.value = 0L
        _duration.value = 0L
        queue.clear()
        currentIndex = -1
    }

    fun release() {
        exoPlayer.release()
        playerScope.cancel()
    }

    companion object {
        @Volatile
        private var INSTANCE: MusicPlayerManager? = null

        fun getInstance(context: Context): MusicPlayerManager {
            return INSTANCE ?: synchronized(this) {
                INSTANCE ?: MusicPlayerManager(context.applicationContext).also { INSTANCE = it }
            }
        }
    }
}
