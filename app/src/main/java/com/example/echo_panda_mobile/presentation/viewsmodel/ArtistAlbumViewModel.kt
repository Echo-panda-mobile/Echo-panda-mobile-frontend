package com.example.echo_panda_mobile.presentation.viewsmodel

import android.content.Context
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.example.echo_panda_mobile.data.remote.AlbumDto
import com.example.echo_panda_mobile.data.remote.RetrofitClient
import com.example.echo_panda_mobile.data.repository.ArtistRepository
import com.example.echo_panda_mobile.data.repository.TokenStorage
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

class ArtistAlbumViewModel(
    private val repository: ArtistRepository
) : ViewModel() {

    sealed class AlbumUiState {
        object Loading : AlbumUiState()
        data class Success(val albums: List<AlbumDto>) : AlbumUiState()
        data class Error(val message: String) : AlbumUiState()
    }

    private val _loading = MutableStateFlow(false)
    private val _error = MutableStateFlow<String?>(null)

    val uiState: StateFlow<AlbumUiState> = combine(
        repository.albums,
        _loading,
        _error
    ) { albums, loading, error ->
        when {
            loading -> AlbumUiState.Loading
            error != null -> AlbumUiState.Error(error)
            else -> AlbumUiState.Success(albums)
        }
    }.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5000),
        initialValue = AlbumUiState.Loading
    )

    init {
        loadMyAlbums()
    }

    fun loadMyAlbums() {
        viewModelScope.launch {
            _loading.value = true
            _error.value = null
            val result = repository.getMyAlbums()
            if (result.isFailure) {
                _error.value = result.exceptionOrNull()?.message ?: "Unknown error"
            }
            _loading.value = false
        }
    }

    fun deleteAlbum(albumId: String) {
        viewModelScope.launch {
            val result = repository.deleteAlbum(albumId)
            if (result.isSuccess) {
                loadMyAlbums()
            }
        }
    }
}

class ArtistAlbumViewModelFactory(private val context: Context) : ViewModelProvider.Factory {
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        if (modelClass.isAssignableFrom(ArtistAlbumViewModel::class.java)) {
            val tokenStorage = TokenStorage.getInstance(context.applicationContext)
            val apiService = RetrofitClient.getMusicService(tokenStorage)
            val repository = ArtistRepository(apiService, tokenStorage)
            @Suppress("UNCHECKED_CAST")
            return ArtistAlbumViewModel(repository) as T
        }
        throw IllegalArgumentException("Unknown ViewModel class")
    }
}
