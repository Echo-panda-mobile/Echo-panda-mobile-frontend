package com.example.echo_panda_mobile.presentation.viewsmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import android.content.Context
import androidx.lifecycle.viewModelScope
import com.example.echo_panda_mobile.data.model.AuthResponse
import com.example.echo_panda_mobile.data.model.LoginRequest
import com.example.echo_panda_mobile.data.repository.AuthRepository
import com.example.echo_panda_mobile.data.repository.AuthResult
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

class LoginViewModel(
    private val repository: AuthRepository
) : ViewModel() {

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

    fun onLoginClick() {
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

    fun onGoogleSignIn(idToken: String) {
        viewModelScope.launch {
            _uiState.value = _uiState.value.copy(isLoading = true, errorMessage = null)
            handleAuthResult(repository.signInWithGoogle(idToken))
        }
    }

    private fun handleAuthResult(result: AuthResult<AuthResponse>) {
        when (result) {
            is AuthResult.Success -> {
                val authResponse = result.data
                
                // Sanitize redirectTo from backend (remove leading slashes)
                val rawRedirect = authResponse.redirectTo?.trim()?.removePrefix("/")
                
                // If it's empty or just "/", we use the role-based graph
                val destination = if (rawRedirect.isNullOrBlank()) {
                    Routes.getHomeRoute(authResponse.user.role)
                } else {
                    // Map common backend routes to Compose routes
                    when (rawRedirect) {
                        "admin/dashboard" -> Routes.ADMIN_GRAPH
                        "artist/dashboard" -> Routes.ARTIST_GRAPH
                        else -> Routes.getHomeRoute(authResponse.user.role)
                    }
                }

                android.util.Log.d("LoginViewModel", "Navigating to: $destination (from backend: ${authResponse.redirectTo})")

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

class LoginViewModelFactory(private val context: Context) : ViewModelProvider.Factory {
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        if (modelClass.isAssignableFrom(LoginViewModel::class.java)) {
            val tokenStorage = TokenStorage.getInstance(context.applicationContext)
            val repository = AuthRepository(tokenStorage)
            @Suppress("UNCHECKED_CAST")
            return LoginViewModel(repository) as T
        }
        throw IllegalArgumentException("Unknown ViewModel class")
    }
}
