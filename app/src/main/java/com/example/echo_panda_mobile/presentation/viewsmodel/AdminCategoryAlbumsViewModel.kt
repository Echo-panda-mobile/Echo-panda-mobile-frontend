package com.example.echo_panda_mobile.presentation.viewsmodel

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.example.echo_panda_mobile.data.remote.AlbumDto
import com.example.echo_panda_mobile.data.repository.AdminRepository
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
            try {
                val categoryIntId = categoryId.toIntOrNull() ?: return@launch
                val albums = repository.getAlbumsByGenre(categoryIntId)
                _uiState.value = _uiState.value.copy(isLoading = false, albums = albums)
            } catch (e: Exception) {
                _uiState.value = _uiState.value.copy(isLoading = false, errorMessage = e.message)
            }
        }
    }
}
