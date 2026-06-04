package com.example.echo_panda_mobile.presentation.viewsmodel

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.example.echo_panda_mobile.data.remote.AlbumDto
import com.example.echo_panda_mobile.data.repository.AdminRepository
import com.example.echo_panda_mobile.data.repository.AdminResult
import com.example.echo_panda_mobile.data.repository.TokenStorage
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

data class AdminTagAlbumsUiState(
    val isLoading: Boolean = false,
    val albums: List<AlbumDto> = emptyList(),
    val errorMessage: String? = null
)

class AdminTagAlbumsViewModel(application: Application) : AndroidViewModel(application) {
    private val repository = AdminRepository(TokenStorage(application))
    
    private val _uiState = MutableStateFlow(AdminTagAlbumsUiState())
    val uiState: StateFlow<AdminTagAlbumsUiState> = _uiState.asStateFlow()

    fun loadAlbums(tagId: String) {
        viewModelScope.launch {
            _uiState.value = _uiState.value.copy(isLoading = true, errorMessage = null)
            val tagIntId = tagId.toIntOrNull() ?: return@launch
            when (val result = repository.getAlbumsByTag(tagIntId)) {
                is AdminResult.Success -> {
                    _uiState.value = _uiState.value.copy(isLoading = false, albums = result.data)
                }
                is AdminResult.Error -> {
                    _uiState.value = _uiState.value.copy(isLoading = false, errorMessage = result.message)
                }
            }
        }
    }

    fun createTag(name: String) {
        viewModelScope.launch {
            if (name.isBlank()) return@launch
            _uiState.value = _uiState.value.copy(isLoading = true, errorMessage = null)
            when (val result = repository.createTag(name.trim())) {
                is AdminResult.Success -> {
                    _uiState.value = _uiState.value.copy(isLoading = false)
                }
                is AdminResult.Error -> {
                    _uiState.value = _uiState.value.copy(isLoading = false, errorMessage = result.message)
                }
            }
        }
    }

    fun removeFromTag(albumId: Int, tagId: Int) {
        viewModelScope.launch {
            _uiState.value = _uiState.value.copy(isLoading = true, errorMessage = null)
            when (val result = repository.removeAlbumFromTag(albumId, tagId)) {
                is AdminResult.Success -> {
                    // reload
                    _uiState.value = _uiState.value.copy(isLoading = false)
                    loadAlbums(tagId.toString())
                }
                is AdminResult.Error -> {
                    _uiState.value = _uiState.value.copy(isLoading = false, errorMessage = result.message)
                }
            }
        }
    }
}
