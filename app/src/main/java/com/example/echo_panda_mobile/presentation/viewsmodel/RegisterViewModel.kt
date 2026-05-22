package com.example.echo_panda_mobile.presentation.viewsmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.echo_panda_mobile.data.model.AuthResponse
import com.example.echo_panda_mobile.data.model.RegisterRequest
import com.example.echo_panda_mobile.data.model.UserRole
import com.example.echo_panda_mobile.data.repository.AuthRepository
import com.example.echo_panda_mobile.data.repository.AuthResult
import com.example.echo_panda_mobile.presentation.navigation.Routes
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

data class RegisterUiState(
    val name: String               = "",
    val email: String              = "",
    val password: String           = "",
    val confirmPassword: String    = "",
    val selectedRole: String       = "user",    // "user" | "artist"
    val isLoading: Boolean         = false,
    val errorMessage: String?      = null,
    val isPasswordVisible: Boolean = false,
    val isConfirmPasswordVisible: Boolean = false,
    val navigateTo: String?        = null
)

class RegisterViewModel(
    private val repository: AuthRepository = AuthRepository()
) : ViewModel() {

    private val _uiState = MutableStateFlow(RegisterUiState())
    val uiState: StateFlow<RegisterUiState> = _uiState.asStateFlow()

    fun onNameChange(value: String) {
        _uiState.value = _uiState.value.copy(name = value, errorMessage = null)
    }

    fun onEmailChange(value: String) {
        _uiState.value = _uiState.value.copy(email = value, errorMessage = null)
    }

    fun onPasswordChange(value: String) {
        _uiState.value = _uiState.value.copy(password = value, errorMessage = null)
    }

    fun onConfirmPasswordChange(value: String) {
        _uiState.value = _uiState.value.copy(confirmPassword = value, errorMessage = null)
    }

    fun onRoleChange(role: String) {
        _uiState.value = _uiState.value.copy(selectedRole = role)
    }

    fun onTogglePasswordVisibility() {
        _uiState.value = _uiState.value.copy(
            isPasswordVisible = !_uiState.value.isPasswordVisible
        )
    }

    fun onToggleConfirmPasswordVisibility() {
        _uiState.value = _uiState.value.copy(
            isConfirmPasswordVisible = !_uiState.value.isConfirmPasswordVisible
        )
    }

    fun onRegisterClick(selectedRole: UserRole = UserRole.USER) {
        viewModelScope.launch {
            _uiState.value = _uiState.value.copy(isLoading = true, errorMessage = null)

            val state = _uiState.value
            val roleStr = when (selectedRole) {
                UserRole.ARTIST -> "artist"
                UserRole.USER -> "user"
                UserRole.UNKNOWN -> "user"
            }
            
            val result = repository.register(
                RegisterRequest(
                    name                 = state.name.trim(),
                    email                = state.email.trim(),
                    password             = state.password,
                    passwordConfirmation = state.confirmPassword,
                    role                 = roleStr
                )
            )

            when (result) {
                is AuthResult.Success<AuthResponse> -> {
                    _uiState.value = _uiState.value.copy(
                        isLoading  = false,
                        navigateTo = Routes.VERIFY_EMAIL
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

    fun onNavigationHandled() {
        _uiState.value = _uiState.value.copy(navigateTo = null)
    }
}