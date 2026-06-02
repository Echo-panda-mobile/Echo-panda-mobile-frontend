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
            try {
                val tagIntId = tagId.toIntOrNull() ?: return@launch
                val albums = repository.getAlbumsByTag(tagIntId)
                _uiState.value = _uiState.value.copy(isLoading = false, albums = albums)
            } catch (e: Exception) {
                _uiState.value = _uiState.value.copy(isLoading = false, errorMessage = e.message)
            }
        }
    }
}
