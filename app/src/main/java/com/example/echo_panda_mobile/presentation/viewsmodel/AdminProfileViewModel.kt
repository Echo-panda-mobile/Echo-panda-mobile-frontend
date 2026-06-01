package com.example.echo_panda_mobile.presentation.viewsmodel

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.example.echo_panda_mobile.data.model.User
import com.example.echo_panda_mobile.data.repository.AuthRepository
import com.example.echo_panda_mobile.data.repository.TokenStorage
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

data class AdminProfileUiState(
    val isLoading: Boolean = true,
    val user: User? = null,
    val errorMessage: String? = null
)

class AdminProfileViewModel(application: Application) : AndroidViewModel(application) {
    private val authRepository = AuthRepository(TokenStorage(application))

    private val _uiState = MutableStateFlow(AdminProfileUiState())
    val uiState: StateFlow<AdminProfileUiState> = _uiState.asStateFlow()

    init {
        loadAdminProfile()
    }

    fun loadAdminProfile() {
        viewModelScope.launch {
            _uiState.value = AdminProfileUiState(isLoading = true)
            try {
                val user = authRepository.getCurrentUser()
                _uiState.value = AdminProfileUiState(
                    isLoading = false,
                    user = user,
                    errorMessage = if (user == null) "Unable to load profile." else null
                )
            } catch (e: Exception) {
                _uiState.value = AdminProfileUiState(
                    isLoading = false,
                    errorMessage = e.message ?: "Failed to load profile"
                )
            }
        }
    }
}
