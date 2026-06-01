package com.example.echo_panda_mobile.presentation.viewsmodel

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.example.echo_panda_mobile.data.model.Album
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

data class AdminAlbumDetailUiState(
    val isLoading: Boolean = true,
    val errorMessage: String? = null,
    val album: Album? = null,
    val actionMessage: String? = null
)

class AdminAlbumDetailViewModel(
    application: Application,
    private val albumId: String
) : AndroidViewModel(application) {
    private val tokenStorage = TokenStorage(application)
    private val musicRepository = MusicRepository(RetrofitClient.getMusicService(tokenStorage))
    private val adminRepository = AdminRepository(tokenStorage)

    private val _uiState = MutableStateFlow(AdminAlbumDetailUiState())
    val uiState: StateFlow<AdminAlbumDetailUiState> = _uiState.asStateFlow()

    init {
        loadAlbum()
    }

    fun loadAlbum() {
        viewModelScope.launch {
            _uiState.value = _uiState.value.copy(isLoading = true, errorMessage = null, actionMessage = null)
            when (val result = musicRepository.getAlbumById(albumId)) {
                is MusicResult.Success -> {
                    _uiState.value = AdminAlbumDetailUiState(isLoading = false, album = result.data)
                }
                is MusicResult.Error -> {
                    _uiState.value = AdminAlbumDetailUiState(isLoading = false, errorMessage = result.message)
                }
                else -> Unit
            }
        }
    }

    fun approveAlbum() = moderate { adminRepository.approveAlbum(albumId) }

    fun hideAlbum() = moderate { adminRepository.hideAlbum(albumId) }

    fun reportAlbum() = moderate { adminRepository.reportAlbum(albumId, "Reported from admin panel") }

    private fun moderate(action: suspend () -> AdminResult<Boolean>) {
        viewModelScope.launch {
            when (val result = action()) {
                is AdminResult.Success -> _uiState.value = _uiState.value.copy(actionMessage = "Action completed successfully.")
                is AdminResult.Error -> _uiState.value = _uiState.value.copy(errorMessage = result.message)
            }
        }
    }
}

class AdminAlbumDetailViewModelFactory(
    private val application: Application,
    private val albumId: String
) : ViewModelProvider.Factory {
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        if (modelClass.isAssignableFrom(AdminAlbumDetailViewModel::class.java)) {
            @Suppress("UNCHECKED_CAST")
            return AdminAlbumDetailViewModel(application, albumId) as T
        }
        throw IllegalArgumentException("Unknown ViewModel class")
    }
}