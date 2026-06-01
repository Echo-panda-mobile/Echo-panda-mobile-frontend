package com.example.echo_panda_mobile.presentation.viewsmodel

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.example.echo_panda_mobile.data.model.Track
import com.example.echo_panda_mobile.data.repository.AdminRepository
import com.example.echo_panda_mobile.data.repository.AdminResult
import com.example.echo_panda_mobile.data.repository.MusicRepository
import com.example.echo_panda_mobile.data.repository.MusicResult
import com.example.echo_panda_mobile.data.repository.TokenStorage
import com.example.echo_panda_mobile.data.remote.RetrofitClient
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

data class AdminSongDetailUiState(
    val isLoading: Boolean = true,
    val errorMessage: String? = null,
    val song: Track? = null,
    val actionMessage: String? = null
)

class AdminSongDetailViewModel(
    application: Application,
    private val songId: String
) : AndroidViewModel(application) {
    private val tokenStorage = TokenStorage(application)
    private val musicRepository = MusicRepository(RetrofitClient.getMusicService(tokenStorage))
    private val adminRepository = AdminRepository(tokenStorage)

    private val _uiState = MutableStateFlow(AdminSongDetailUiState())
    val uiState: StateFlow<AdminSongDetailUiState> = _uiState.asStateFlow()

    init {
        loadSong()
    }

    fun loadSong() {
        viewModelScope.launch {
            _uiState.value = _uiState.value.copy(isLoading = true, errorMessage = null, actionMessage = null)
            when (val result = musicRepository.getTrackById(songId)) {
                is MusicResult.Success -> {
                    _uiState.value = AdminSongDetailUiState(isLoading = false, song = result.data)
                }
                is MusicResult.Error -> {
                    _uiState.value = AdminSongDetailUiState(isLoading = false, errorMessage = result.message)
                }
                else -> Unit
            }
        }
    }

    fun approveSong() = moderate { adminRepository.approveSong(songId) }

    fun hideSong() = moderate { adminRepository.hideSong(songId) }

    fun reportSong() = moderate { adminRepository.reportSong(songId, "Reported from admin panel") }

    private fun moderate(action: suspend () -> AdminResult<Boolean>) {
        viewModelScope.launch {
            when (val result = action()) {
                is AdminResult.Success -> _uiState.value = _uiState.value.copy(actionMessage = "Action completed successfully.")
                is AdminResult.Error -> _uiState.value = _uiState.value.copy(errorMessage = result.message)
            }
        }
    }
}

class AdminSongDetailViewModelFactory(
    private val application: Application,
    private val songId: String
) : ViewModelProvider.Factory {
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        if (modelClass.isAssignableFrom(AdminSongDetailViewModel::class.java)) {
            @Suppress("UNCHECKED_CAST")
            return AdminSongDetailViewModel(application, songId) as T
        }
        throw IllegalArgumentException("Unknown ViewModel class")
    }
}
