package com.example.echo_panda_mobile.presentation.viewsmodel

import android.app.Application
import android.net.Uri
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.example.echo_panda_mobile.data.remote.GenreData
import com.example.echo_panda_mobile.data.repository.AdminRepository
import com.example.echo_panda_mobile.data.repository.AdminResult
import com.example.echo_panda_mobile.data.repository.TokenStorage
import com.example.echo_panda_mobile.util.ImageCompressor
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

data class AdminCategoryDetailUiState(
    val isLoading: Boolean = false,
    val isUploadingImage: Boolean = false,
    val genre: GenreData? = null,
    val displayImageUrl: String? = null,
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
                        val imageUrl = repository.resolveGenreImageUrl(genre.id, genre.imageUrl)
                        _uiState.value = _uiState.value.copy(
                            isLoading = false,
                            genre = genre,
                            displayImageUrl = imageUrl
                        )
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

    fun uploadGenreImage(uri: Uri) {
        val genre = _uiState.value.genre ?: return
        viewModelScope.launch {
            _uiState.value = _uiState.value.copy(
                isUploadingImage = true,
                errorMessage = null,
                successMessage = null
            )

            val compressedFile = withContext(Dispatchers.IO) {
                ImageCompressor.compress(getApplication(), uri, "genre_image")
            }
            if (compressedFile == null) {
                _uiState.value = _uiState.value.copy(
                    isUploadingImage = false,
                    errorMessage = "Failed to process image"
                )
                return@launch
            }

            when (val result = repository.uploadGenreImage(genre.id, compressedFile)) {
                is AdminResult.Success -> {
                    val imageUrl = repository.resolveGenreImageUrl(result.data.id, result.data.imageUrl)
                    _uiState.value = _uiState.value.copy(
                        isUploadingImage = false,
                        genre = result.data,
                        displayImageUrl = imageUrl,
                        successMessage = "Category image updated successfully"
                    )
                }
                is AdminResult.Error -> {
                    _uiState.value = _uiState.value.copy(
                        isUploadingImage = false,
                        errorMessage = result.message
                    )
                }
            }
        }
    }

    fun updateCategory(id: Int, name: String) {
        viewModelScope.launch {
            _uiState.value = _uiState.value.copy(isLoading = true, errorMessage = null, successMessage = null)
            when (val result = repository.updateGenre(id, name)) {
                is AdminResult.Success -> {
                    val imageUrl = repository.resolveGenreImageUrl(result.data.id, result.data.imageUrl)
                    _uiState.value = _uiState.value.copy(
                        isLoading = false,
                        genre = result.data,
                        displayImageUrl = imageUrl,
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

    fun setCategoryActive(isActive: Boolean) {
        val genre = _uiState.value.genre ?: return
        viewModelScope.launch {
            val previous = genre
            _uiState.value = _uiState.value.copy(
                genre = genre.copy(isActive = isActive),
                errorMessage = null,
                successMessage = null
            )
            when (val result = repository.setGenreActive(genre.id, isActive)) {
                is AdminResult.Success -> {
                    val imageUrl = repository.resolveGenreImageUrl(result.data.id, result.data.imageUrl)
                    _uiState.value = _uiState.value.copy(genre = result.data, displayImageUrl = imageUrl)
                }
                is AdminResult.Error -> {
                    _uiState.value = _uiState.value.copy(genre = previous, errorMessage = result.message)
                }
            }
        }
    }

    fun setCategoryShowAsRow(showAsRow: Boolean) {
        val genre = _uiState.value.genre ?: return
        viewModelScope.launch {
            val previous = genre
            _uiState.value = _uiState.value.copy(
                genre = genre.copy(showAsRow = showAsRow),
                errorMessage = null,
                successMessage = null
            )
            when (val result = repository.setGenreShowAsRow(genre.id, showAsRow)) {
                is AdminResult.Success -> {
                    val imageUrl = repository.resolveGenreImageUrl(result.data.id, result.data.imageUrl)
                    _uiState.value = _uiState.value.copy(genre = result.data, displayImageUrl = imageUrl)
                }
                is AdminResult.Error -> {
                    _uiState.value = _uiState.value.copy(genre = previous, errorMessage = result.message)
                }
            }
        }
    }
}
