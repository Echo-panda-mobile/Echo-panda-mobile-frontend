package com.example.echo_panda_mobile.presentation.viewsmodel

import android.app.Application
import android.content.Intent
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.example.echo_panda_mobile.data.model.AuthResponse
import com.example.echo_panda_mobile.data.model.LoginRequest
import com.example.echo_panda_mobile.data.model.UserRole
import com.example.echo_panda_mobile.data.repository.AuthRepository
import com.example.echo_panda_mobile.data.repository.AuthResult
import com.example.echo_panda_mobile.data.repository.FirebaseAuthManager
import com.example.echo_panda_mobile.data.repository.TokenStorage
import com.example.echo_panda_mobile.presentation.navigation.Routes
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

data class LoginUiState(
    val email: String = "",
    val password: String = "",
    val isLoading: Boolean = false,
    val errorMessage: String? = null,
    val isPasswordVisible: Boolean = false,
    val navigateTo: String? = null
)

class LoginViewModel(application: Application) : AndroidViewModel(application) {

    private val repository = AuthRepository(
        tokenStorage = TokenStorage(application),
        firebaseAuthManager = FirebaseAuthManager(application)
    )

    private val _uiState = MutableStateFlow(LoginUiState())
    val uiState: StateFlow<LoginUiState> = _uiState.asStateFlow()

    fun onEmailChange(value: String) {
        _uiState.value = _uiState.value.copy(email = value, errorMessage = null)
    }

    fun onPasswordChange(value: String) {
        _uiState.value = _uiState.value.copy(password = value, errorMessage = null)
    }

    fun onTogglePasswordVisibility() {
        _uiState.value = _uiState.value.copy(
            isPasswordVisible = !_uiState.value.isPasswordVisible
        )
    }

    fun onLoginClick(selectedRole: UserRole = UserRole.USER) {
        viewModelScope.launch {
            _uiState.value = _uiState.value.copy(isLoading = true, errorMessage = null)

            val result = repository.login(
                LoginRequest(
                    email = _uiState.value.email.trim(),
                    password = _uiState.value.password
                )
            )

            handleAuthResult(result)
        }
    }

    fun signInWithGoogle(data: Intent?) {
        viewModelScope.launch {
            _uiState.value = _uiState.value.copy(isLoading = true, errorMessage = null)
            handleAuthResult(repository.signInWithGoogle(data))
        }
    }

    fun onGoogleSignIn(idToken: String) {
        viewModelScope.launch {
            _uiState.value = _uiState.value.copy(isLoading = true, errorMessage = null)
            handleAuthResult(repository.signInWithGoogle(idToken))
        }
    }

    private fun handleAuthResult(result: AuthResult<*>) {
        when (result) {
            is AuthResult.Success<*> -> {
                val authResponse = result.data as? AuthResponse
                val destination = Routes.getHomeRoute(authResponse?.user?.role)
                _uiState.value = _uiState.value.copy(
                    isLoading = false,
                    navigateTo = destination
                )
            }
            is AuthResult.Error -> {
                _uiState.value = _uiState.value.copy(
                    isLoading = false,
                    errorMessage = result.message
                )
            }
            else -> Unit
        }
    }

    fun onGoogleSignInFailed(message: String) {
        _uiState.value = _uiState.value.copy(isLoading = false, errorMessage = message)
    }

    fun onNavigationHandled() {
        _uiState.value = _uiState.value.copy(navigateTo = null)
    }
}
