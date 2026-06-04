package com.example.echo_panda_mobile.presentation.viewsmodel

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.example.echo_panda_mobile.data.remote.GenreData
import com.example.echo_panda_mobile.data.remote.TagData
import com.example.echo_panda_mobile.data.repository.AdminRepository
import com.example.echo_panda_mobile.data.repository.AdminResult
import com.example.echo_panda_mobile.data.repository.TokenStorage
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

data class AdminLibraryUiState(
    val isLoading: Boolean = false,
    val errorMessage: String? = null,
    val successMessage: String? = null,
    val tags: List<TagData> = emptyList(),
    val genres: List<GenreData> = emptyList()
)

class AdminLibraryViewModel(application: Application) : AndroidViewModel(application) {
    private val tokenStorage = TokenStorage(application)
    private val adminRepository = AdminRepository(tokenStorage)

    private val _uiState = MutableStateFlow(AdminLibraryUiState(isLoading = true))
    val uiState: StateFlow<AdminLibraryUiState> = _uiState.asStateFlow()

    init {
        loadLibrary()
    }

    fun loadLibrary() {
        viewModelScope.launch {
            _uiState.value = _uiState.value.copy(isLoading = true, errorMessage = null)
            
            try {
                val tagsResult = adminRepository.getTags()
                val genresResult = adminRepository.getGenres()

                if (tagsResult is AdminResult.Success && genresResult is AdminResult.Success) {
                    _uiState.value = _uiState.value.copy(
                        isLoading = false,
                        tags = tagsResult.data,
                        genres = genresResult.data,
                        errorMessage = if (tagsResult.data.isEmpty() && genresResult.data.isEmpty()) "No data found on server." else null
                    )
                } else {
                    val error = when {
                        tagsResult is AdminResult.Error -> tagsResult.message
                        genresResult is AdminResult.Error -> genresResult.message
                        else -> "Unknown error"
                    }
                    _uiState.value = _uiState.value.copy(
                        isLoading = false,
                        errorMessage = error
                    )
                }
            } catch (e: Exception) {
                _uiState.value = _uiState.value.copy(
                    isLoading = false,
                    errorMessage = "Error connecting to server: ${e.localizedMessage}"
                )
            }
        }
    }

    private fun showResultMessage(message: String?) {
        _uiState.value = _uiState.value.copy(successMessage = message, errorMessage = null)
    }

    fun createTag(name: String) {
        viewModelScope.launch {
            if (name.isBlank()) {
                _uiState.value = _uiState.value.copy(errorMessage = "Tag name is required.")
                return@launch
            }
            _uiState.value = _uiState.value.copy(isLoading = true, errorMessage = null, successMessage = null)

            when (val result = adminRepository.createTag(name.trim())) {
                is AdminResult.Success -> {
                    showResultMessage("Tag created successfully.")
                    loadLibrary()
                }
                is AdminResult.Error -> {
                    _uiState.value = _uiState.value.copy(isLoading = false, errorMessage = result.message)
                }
            }
        }
    }

    fun createGenre(name: String) {
        viewModelScope.launch {
            if (name.isBlank()) {
                _uiState.value = _uiState.value.copy(errorMessage = "Category name is required.")
                return@launch
            }
            _uiState.value = _uiState.value.copy(isLoading = true, errorMessage = null, successMessage = null)

            when (val result = adminRepository.createGenre(name.trim())) {
                is AdminResult.Success -> {
                    showResultMessage("Category created successfully.")
                    loadLibrary()
                }
                is AdminResult.Error -> {
                    _uiState.value = _uiState.value.copy(isLoading = false, errorMessage = result.message)
                }
            }
        }
    }

    fun deleteTag(id: Int) {
        viewModelScope.launch {
            _uiState.value = _uiState.value.copy(isLoading = true, errorMessage = null, successMessage = null)
            when (val result = adminRepository.deleteTag(id)) {
                is AdminResult.Success -> {
                    showResultMessage("Tag deleted successfully.")
                    loadLibrary()
                }
                is AdminResult.Error -> {
                    _uiState.value = _uiState.value.copy(isLoading = false, errorMessage = result.message)
                }
            }
        }
    }

    fun deleteGenre(id: Int) {
        viewModelScope.launch {
            _uiState.value = _uiState.value.copy(isLoading = true, errorMessage = null, successMessage = null)
            when (val result = adminRepository.deleteGenre(id)) {
                is AdminResult.Success -> {
                    showResultMessage("Category deleted successfully.")
                    loadLibrary()
                }
                is AdminResult.Error -> {
                    _uiState.value = _uiState.value.copy(isLoading = false, errorMessage = result.message)
                }
            }
        }
    }

    fun updateTag(id: Int, name: String) {
        viewModelScope.launch {
            if (name.isBlank()) {
                _uiState.value = _uiState.value.copy(errorMessage = "Tag name is required.")
                return@launch
            }
            _uiState.value = _uiState.value.copy(isLoading = true, errorMessage = null, successMessage = null)

            when (val result = adminRepository.updateTag(id, name.trim())) {
                is AdminResult.Success -> {
                    showResultMessage("Tag updated successfully.")
                    loadLibrary()
                }
                is AdminResult.Error -> {
                    _uiState.value = _uiState.value.copy(isLoading = false, errorMessage = result.message)
                }
            }
        }
    }

    fun updateGenre(id: Int, name: String) {
        viewModelScope.launch {
            if (name.isBlank()) {
                _uiState.value = _uiState.value.copy(errorMessage = "Category name is required.")
                return@launch
            }
            _uiState.value = _uiState.value.copy(isLoading = true, errorMessage = null, successMessage = null)

            when (val result = adminRepository.updateGenre(id, name.trim())) {
                is AdminResult.Success -> {
                    showResultMessage("Category updated successfully.")
                    loadLibrary()
                }
                is AdminResult.Error -> {
                    _uiState.value = _uiState.value.copy(isLoading = false, errorMessage = result.message)
                }
            }
        }
    }

    fun refresh() {
        loadLibrary()
    }
}
