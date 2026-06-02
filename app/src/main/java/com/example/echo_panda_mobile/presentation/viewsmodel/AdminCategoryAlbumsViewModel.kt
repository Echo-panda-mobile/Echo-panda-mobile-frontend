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

data class AdminCategoryAlbumsUiState(
    val isLoading: Boolean = false,
    val albums: List<AlbumDto> = emptyList(),
    val errorMessage: String? = null
)

class AdminCategoryAlbumsViewModel(application: Application) : AndroidViewModel(application) {
    private val repository = AdminRepository(TokenStorage(application))
    
    private val _uiState = MutableStateFlow(AdminCategoryAlbumsUiState())
    val uiState: StateFlow<AdminCategoryAlbumsUiState> = _uiState.asStateFlow()

    fun loadAlbums(categoryId: String) {
        viewModelScope.launch {
            _uiState.value = _uiState.value.copy(isLoading = true, errorMessage = null)
            val categoryIntId = categoryId.toIntOrNull() ?: return@launch
            when (val result = repository.getAlbumsByGenre(categoryIntId)) {
                is AdminResult.Success -> {
                    _uiState.value = _uiState.value.copy(isLoading = false, albums = result.data)
                }
                is AdminResult.Error -> {
                    _uiState.value = _uiState.value.copy(isLoading = false, errorMessage = result.message)
                }
            }
        }
    }

    fun createCategory(name: String) {
        viewModelScope.launch {
            if (name.isBlank()) return@launch
            _uiState.value = _uiState.value.copy(isLoading = true, errorMessage = null)
            when (val result = repository.createGenre(name.trim())) {
                is AdminResult.Success -> {
                    _uiState.value = _uiState.value.copy(isLoading = false)
                }
                is AdminResult.Error -> {
                    _uiState.value = _uiState.value.copy(isLoading = false, errorMessage = result.message)
                }
            }
        }
    }

    fun removeFromCategory(albumId: Int, categoryId: Int) {
        viewModelScope.launch {
            _uiState.value = _uiState.value.copy(isLoading = true, errorMessage = null)
            when (val result = repository.removeAlbumFromGenre(albumId, categoryId)) {
                is AdminResult.Success -> {
                    _uiState.value = _uiState.value.copy(isLoading = false)
                    loadAlbums(categoryId.toString())
                }
                is AdminResult.Error -> {
                    _uiState.value = _uiState.value.copy(isLoading = false, errorMessage = result.message)
                }
            }
        }
    }
}
