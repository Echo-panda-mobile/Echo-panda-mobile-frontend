package com.example.echo_panda_mobile.presentation.viewsmodel

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.example.echo_panda_mobile.data.remote.GenreData
import com.example.echo_panda_mobile.data.repository.AdminRepository
import com.example.echo_panda_mobile.data.repository.AdminResult
import com.example.echo_panda_mobile.data.repository.TokenStorage
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

data class AdminCategoryDetailUiState(
    val isLoading: Boolean = false,
    val genre: GenreData? = null,
    val errorMessage: String? = null,
    val successMessage: String? = null,
    val isDeleted: Boolean = false
)

class AdminCategoryDetailViewModel(application: Application) : AndroidViewModel(application) {
    private val repository = AdminRepository(TokenStorage(application))
    
    private val _uiState = MutableStateFlow(AdminCategoryDetailUiState())
    val uiState: StateFlow<AdminCategoryDetailUiState> = _uiState.asStateFlow()

    fun loadCategory(categoryId: String) {
        viewModelScope.launch {
            _uiState.value = _uiState.value.copy(isLoading = true, errorMessage = null)
            when (val result = repository.getGenres()) {
                is AdminResult.Success -> {
                    val genre = result.data.find { it.id.toString() == categoryId }
                    if (genre != null) {
                        _uiState.value = _uiState.value.copy(isLoading = false, genre = genre)
                    } else {
                        _uiState.value = _uiState.value.copy(isLoading = false, errorMessage = "Category not found")
                    }
                }
                is AdminResult.Error -> {
                    _uiState.value = _uiState.value.copy(isLoading = false, errorMessage = result.message)
                }
            }
        }
    }

    fun updateCategory(id: Int, name: String) {
        viewModelScope.launch {
            _uiState.value = _uiState.value.copy(isLoading = true, errorMessage = null, successMessage = null)
            when (val result = repository.updateGenre(id, name)) {
                is AdminResult.Success -> {
                    _uiState.value = _uiState.value.copy(
                        isLoading = false,
                        genre = result.data,
                        successMessage = "Category updated successfully"
                    )
                }
                is AdminResult.Error -> {
                    _uiState.value = _uiState.value.copy(isLoading = false, errorMessage = result.message)
                }
            }
        }
    }

    fun deleteCategory(id: Int) {
        viewModelScope.launch {
            _uiState.value = _uiState.value.copy(isLoading = true, errorMessage = null)
            when (val result = repository.deleteGenre(id)) {
                is AdminResult.Success -> {
                    _uiState.value = _uiState.value.copy(isLoading = false, isDeleted = true)
                }
                is AdminResult.Error -> {
                    _uiState.value = _uiState.value.copy(isLoading = false, errorMessage = result.message)
                }
            }
        }
    }
}
