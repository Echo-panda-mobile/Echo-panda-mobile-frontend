package com.example.echo_panda_mobile.presentation.viewsmodel

import android.content.Context
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.example.echo_panda_mobile.data.repository.AuthRepository
import com.example.echo_panda_mobile.data.repository.AuthResult
import com.example.echo_panda_mobile.data.repository.TokenStorage
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

data class ForgotPasswordUiState(
    val email: String = "",
    val isLoading: Boolean = false,
    val successMessage: String? = null,
    val errorMessage: String? = null,
    val isEmailSent: Boolean = false,
    val navigateTo: String? = null
)

class ForgotPasswordViewModel(
    private val repository: AuthRepository,
    private val tokenStorage: TokenStorage? = null
) : ViewModel() {

    private val _uiState = MutableStateFlow(ForgotPasswordUiState())
    val uiState: StateFlow<ForgotPasswordUiState> = _uiState.asStateFlow()

    fun onEmailChange(value: String) {
        _uiState.value = _uiState.value.copy(email = value, errorMessage = null)
    }

    fun onResetPasswordClick() {
        val email = _uiState.value.email.trim()
        if (email.isBlank()) {
            _uiState.value = _uiState.value.copy(errorMessage = "Please enter your email address.")
            return
        }

        viewModelScope.launch {
            _uiState.value = _uiState.value.copy(isLoading = true, errorMessage = null, successMessage = null)

            // Step 1: Check if the email exists in our system
            val emailExists = repository.checkEmailExistsInFirestore(email)
            
            if (!emailExists) {
                _uiState.value = _uiState.value.copy(
                    isLoading = false,
                    errorMessage = "This email is not registered in our system."
                )
                return@launch
            }

            // Step 2: Send the REAL Firebase Password Reset Email
            val result = repository.sendPasswordResetEmail(email)
            
            if (result is AuthResult.Success) {
                _uiState.value = _uiState.value.copy(
                    isLoading = false,
                    successMessage = "A secure reset link has been sent to your email. Please check your inbox.",
                    isEmailSent = true
                )
            } else if (result is AuthResult.Error) {
                _uiState.value = _uiState.value.copy(
                    isLoading = false,
                    errorMessage = result.message
                )
            }
        }
    }

    fun onUpdatePasswordClick(newPassword: String, onSuccess: () -> Unit) {
        val email = _uiState.value.email.trim()
        
        viewModelScope.launch {
            _uiState.value = _uiState.value.copy(isLoading = true, errorMessage = null)
            
            val result = repository.storeNewPasswordInFirestore(email, newPassword)
            
            if (result is AuthResult.Success) {
                _uiState.value = _uiState.value.copy(
                    isLoading = false,
                    successMessage = "New password saved successfully!"
                )
                delay(1500)
                onSuccess()
            } else if (result is AuthResult.Error) {
                _uiState.value = _uiState.value.copy(
                    isLoading = false,
                    errorMessage = result.message
                )
            }
        }
    }

    fun onNavigationHandled() {
        _uiState.value = _uiState.value.copy(navigateTo = null)
    }

    fun onBackToLogin() {
        _uiState.value = ForgotPasswordUiState()
    }
}

class ForgotPasswordViewModelFactory(private val context: Context) : ViewModelProvider.Factory {
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        if (modelClass.isAssignableFrom(ForgotPasswordViewModel::class.java)) {
            val tokenStorage = TokenStorage.getInstance(context.applicationContext)
            val repository = AuthRepository(tokenStorage)
            @Suppress("UNCHECKED_CAST")
            return ForgotPasswordViewModel(repository, tokenStorage) as T
        }
        throw IllegalArgumentException("Unknown ViewModel class")
    }
}
