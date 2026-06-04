package com.example.echo_panda_mobile.presentation.viewsmodel

import android.content.Context
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.example.echo_panda_mobile.data.remote.RetrofitClient
import com.example.echo_panda_mobile.data.remote.SongDto
import com.example.echo_panda_mobile.data.repository.ArtistRepository
import com.example.echo_panda_mobile.data.repository.TokenStorage
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

class ArtistMusicViewModel(
    private val repository: ArtistRepository
) : ViewModel() {

    sealed class MusicUiState {
        object Loading : MusicUiState()
        data class Success(val songs: List<SongDto>) : MusicUiState()
        data class Error(val message: String) : MusicUiState()
    }

    private val _loading = MutableStateFlow(false)
    private val _error = MutableStateFlow<String?>(null)

    val uiState: StateFlow<MusicUiState> = combine(
        repository.songs,
        _loading,
        _error
    ) { songs, loading, error ->
        when {
            loading -> MusicUiState.Loading
            error != null -> MusicUiState.Error(error)
            else -> MusicUiState.Success(songs)
        }
    }.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5000),
        initialValue = MusicUiState.Loading
    )

    init {
        loadMyMusic()
    }

    fun loadMyMusic() {
        viewModelScope.launch {
            _loading.value = true
            _error.value = null
            val result = repository.getMySongs()
            if (result.isFailure) {
                _error.value = result.exceptionOrNull()?.message ?: "Unknown error"
            }
            _loading.value = false
        }
    }

    fun deleteSong(songId: String) {
        viewModelScope.launch {
            val result = repository.deleteSong(songId)
            if (result.isSuccess) {
                loadMyMusic()
            }
        }
    }
}

class ArtistMusicViewModelFactory(private val context: Context) : ViewModelProvider.Factory {
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        if (modelClass.isAssignableFrom(ArtistMusicViewModel::class.java)) {
            val tokenStorage = TokenStorage.getInstance(context.applicationContext)
            val apiService = RetrofitClient.getMusicService(tokenStorage)
            val repository = ArtistRepository(apiService, tokenStorage)
            @Suppress("UNCHECKED_CAST")
            return ArtistMusicViewModel(repository) as T
        }
        throw IllegalArgumentException("Unknown ViewModel class")
    }
}
