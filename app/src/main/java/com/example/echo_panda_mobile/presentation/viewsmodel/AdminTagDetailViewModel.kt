package com.example.echo_panda_mobile.presentation.viewsmodel

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.example.echo_panda_mobile.data.remote.TagData
import com.example.echo_panda_mobile.data.repository.AdminRepository
import com.example.echo_panda_mobile.data.repository.AdminResult
import com.example.echo_panda_mobile.data.repository.TokenStorage
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

data class AdminTagDetailUiState(
    val isLoading: Boolean = false,
    val tag: TagData? = null,
    val errorMessage: String? = null,
    val successMessage: String? = null,
    val isDeleted: Boolean = false
)

class AdminTagDetailViewModel(application: Application) : AndroidViewModel(application) {
    private val repository = AdminRepository(TokenStorage(application))
    
    private val _uiState = MutableStateFlow(AdminTagDetailUiState())
    val uiState: StateFlow<AdminTagDetailUiState> = _uiState.asStateFlow()

    fun loadTag(tagId: String) {
        viewModelScope.launch {
            _uiState.value = _uiState.value.copy(isLoading = true, errorMessage = null)
            when (val result = repository.getTags()) {
                is AdminResult.Success -> {
                    val tag = result.data.find { it.id.toString() == tagId }
                    if (tag != null) {
                        _uiState.value = _uiState.value.copy(isLoading = false, tag = tag)
                    } else {
                        _uiState.value = _uiState.value.copy(isLoading = false, errorMessage = "Tag not found")
                    }
                }
                is AdminResult.Error -> {
                    _uiState.value = _uiState.value.copy(isLoading = false, errorMessage = result.message)
                }
            }
        }
    }

    fun updateTag(id: Int, name: String) {
        viewModelScope.launch {
            _uiState.value = _uiState.value.copy(isLoading = true, errorMessage = null, successMessage = null)
            when (val result = repository.updateTag(id, name)) {
                is AdminResult.Success -> {
                    _uiState.value = _uiState.value.copy(
                        isLoading = false,
                        tag = result.data,
                        successMessage = "Tag updated successfully"
                    )
                }
                is AdminResult.Error -> {
                    _uiState.value = _uiState.value.copy(isLoading = false, errorMessage = result.message)
                }
            }
        }
    }

    fun deleteTag(id: Int) {
        viewModelScope.launch {
            _uiState.value = _uiState.value.copy(isLoading = true, errorMessage = null)
            when (val result = repository.deleteTag(id)) {
                is AdminResult.Success -> {
                    _uiState.value = _uiState.value.copy(isLoading = false, isDeleted = true)
                }
                is AdminResult.Error -> {
                    _uiState.value = _uiState.value.copy(isLoading = false, errorMessage = result.message)
                }
            }
        }
    }

    fun setTagActive(isActive: Boolean) {
        val tag = _uiState.value.tag ?: return
        viewModelScope.launch {
            val previous = tag
            _uiState.value = _uiState.value.copy(
                tag = tag.copy(isActive = isActive),
                errorMessage = null,
                successMessage = null
            )
            when (val result = repository.setTagActive(tag.id, isActive)) {
                is AdminResult.Success -> {
                    _uiState.value = _uiState.value.copy(tag = result.data)
                }
                is AdminResult.Error -> {
                    _uiState.value = _uiState.value.copy(tag = previous, errorMessage = result.message)
                }
            }
        }
    }

    fun setTagShowAsRow(showAsRow: Boolean) {
        val tag = _uiState.value.tag ?: return
        viewModelScope.launch {
            val previous = tag
            _uiState.value = _uiState.value.copy(
                tag = tag.copy(showAsRow = showAsRow),
                errorMessage = null,
                successMessage = null
            )
            when (val result = repository.setTagShowAsRow(tag.id, showAsRow)) {
                is AdminResult.Success -> {
                    _uiState.value = _uiState.value.copy(tag = result.data)
                }
                is AdminResult.Error -> {
                    _uiState.value = _uiState.value.copy(tag = previous, errorMessage = result.message)
                }
            }
        }
    }
}
