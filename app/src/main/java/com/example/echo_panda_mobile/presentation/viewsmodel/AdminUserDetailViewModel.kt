package com.example.echo_panda_mobile.presentation.viewsmodel

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.example.echo_panda_mobile.data.remote.BackendUser
import com.example.echo_panda_mobile.data.repository.AdminRepository
import com.example.echo_panda_mobile.data.repository.AdminResult
import com.example.echo_panda_mobile.data.repository.TokenStorage
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

data class AdminUserDetailUiState(
    val isLoading: Boolean = true,
    val errorMessage: String? = null,
    val user: BackendUser? = null
)

class AdminUserDetailViewModel(
    application: Application,
    private val userId: String,
    private val role: String
) : AndroidViewModel(application) {
    private val repository = AdminRepository(TokenStorage(application))

    private val _uiState = MutableStateFlow(AdminUserDetailUiState())
    val uiState: StateFlow<AdminUserDetailUiState> = _uiState.asStateFlow()

    init {
        loadUser()
    }

    private fun loadUser() {
        viewModelScope.launch {
            when (val result = repository.findUserByIdAndRole(userId, role)) {
                is AdminResult.Success -> {
                    _uiState.value = if (result.data != null) {
                        AdminUserDetailUiState(isLoading = false, user = result.data)
                    } else {
                        AdminUserDetailUiState(
                            isLoading = false,
                            errorMessage = "No matching $role record was found."
                        )
                    }
                }
                is AdminResult.Error -> {
                    _uiState.value = AdminUserDetailUiState(
                        isLoading = false,
                        errorMessage = result.message
                    )
                }
            }
        }
    }
}

class AdminUserDetailViewModelFactory(
    private val application: Application,
    private val userId: String,
    private val role: String
) : ViewModelProvider.Factory {
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        if (modelClass.isAssignableFrom(AdminUserDetailViewModel::class.java)) {
            @Suppress("UNCHECKED_CAST")
            return AdminUserDetailViewModel(application, userId, role) as T
        }
        throw IllegalArgumentException("Unknown ViewModel class")
    }
}