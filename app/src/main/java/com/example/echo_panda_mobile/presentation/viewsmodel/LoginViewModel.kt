package com.example.echo_panda_mobile.presentation.viewsmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.echo_panda_mobile.data.model.AuthResponse
import com.example.echo_panda_mobile.data.model.LoginRequest
import com.example.echo_panda_mobile.data.model.UserRole
import com.example.echo_panda_mobile.data.repository.AuthRepository
import com.example.echo_panda_mobile.data.repository.AuthResult
import com.example.echo_panda_mobile.presentation.navigation.Routes
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

data class LoginUiState(
    val email: String          = "",
    val password: String       = "",
    val isLoading: Boolean     = false,
    val errorMessage: String?  = null,
    val isPasswordVisible: Boolean = false,
    val navigateTo: String?    = null   // route to navigate after success
)

class LoginViewModel(
    private val repository: AuthRepository = AuthRepository()
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

    fun onLoginClick(selectedRole: UserRole = UserRole.USER) {
        viewModelScope.launch {
            _uiState.value = _uiState.value.copy(isLoading = true, errorMessage = null)

            val result = repository.login(
                LoginRequest(
                    email    = _uiState.value.email.trim(),
                    password = _uiState.value.password
                )
            )

            when (result) {
                is AuthResult.Success<*> -> {
                    val authResponse = result.data as? AuthResponse
                    // Check if user's actual role matches selected role
                    val userRole = authResponse?.user?.role?.uppercase()
                    val selectedRoleStr = selectedRole.name
                    
                    val destination = if (selectedRoleStr == "ARTIST" && userRole == "ARTIST") {
                        Routes.ARTIST_DASHBOARD
                    } else if (selectedRoleStr == "USER" && userRole != "ARTIST") {
                        Routes.USER_HOME
                    } else {
                        // If role mismatch, still route based on actual user role
                        if (authResponse?.user?.role == "artist") {
                            Routes.ARTIST_DASHBOARD
                        } else {
                            Routes.USER_HOME
                        }
                    }
                    
                    _uiState.value = _uiState.value.copy(
                        isLoading  = false,
                        navigateTo = destination
                    )
                }
                is AuthResult.Error -> {
                    _uiState.value = _uiState.value.copy(
                        isLoading    = false,
                        errorMessage = result.message
                    )
                }
                else -> Unit
            }
        }
    }

    fun onGoogleSignIn(idToken: String) {
        viewModelScope.launch {
            _uiState.value = _uiState.value.copy(isLoading = true, errorMessage = null)

            val result = repository.signInWithGoogle(idToken)
            when (result) {
                is AuthResult.Success<*> -> {
                    val authResponse = result.data as? AuthResponse
                    val destination = when (authResponse?.user?.role?.lowercase()) {
                        "artist" -> Routes.ARTIST_DASHBOARD
                        else -> Routes.USER_HOME
                    }
                    _uiState.value = _uiState.value.copy(
                        isLoading  = false,
                        navigateTo = destination
                    )
                }
                is AuthResult.Error -> {
                    _uiState.value = _uiState.value.copy(
                        isLoading    = false,
                        errorMessage = result.message
                    )
                }
                else -> Unit
            }
        }
    }

    fun onGoogleSignInFailed(message: String) {
        _uiState.value = _uiState.value.copy(
            isLoading = false,
            errorMessage = message
        )
    }

    fun onNavigationHandled() {
        _uiState.value = _uiState.value.copy(navigateTo = null)
    }
}