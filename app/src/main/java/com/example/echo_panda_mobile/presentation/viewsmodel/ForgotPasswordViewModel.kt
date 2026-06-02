package com.example.echo_panda_mobile.presentation.viewsmodel

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.example.echo_panda_mobile.data.repository.AuthRepository
import com.example.echo_panda_mobile.data.repository.AuthResult
import com.example.echo_panda_mobile.data.repository.FirebaseAuthManager
import com.example.echo_panda_mobile.data.repository.TokenStorage
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

class ForgotPasswordViewModel(application: Application) : AndroidViewModel(application) {

    private val repository = AuthRepository(
        tokenStorage = TokenStorage(application),
        firebaseAuthManager = FirebaseAuthManager(application)
    )

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
            
            // This will now store the REAL password in your Firebase Firestore Database
            val result = repository.storeNewPasswordInFirestore(email, newPassword)
            
            if (result is AuthResult.Success) {
                _uiState.value = _uiState.value.copy(
                    isLoading = false,
                    successMessage = "New password saved to Firebase successfully!"
                )
                kotlinx.coroutines.delay(1500)
                onSuccess()
            } else if (result is AuthResult.Error) {
                // If you see "PERMISSION_DENIED" here, follow the instructions I gave you 
                // to update your Rules in the Firebase Console.
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
