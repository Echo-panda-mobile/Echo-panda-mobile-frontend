package com.example.echo_panda_mobile.presentation.viewsmodel

import android.content.Context
import android.content.Intent
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.example.echo_panda_mobile.data.repository.AuthRepository
import com.example.echo_panda_mobile.data.repository.AuthResult
import com.example.echo_panda_mobile.data.repository.FirebaseAuthManager
import com.example.echo_panda_mobile.data.repository.TokenStorage
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch

sealed class AuthState {
    object Idle : AuthState()
    object Loading : AuthState()
    data class Success(val role: String) : AuthState()
    data class Error(val message: String) : AuthState()
}

class AuthViewModel(
    private val authRepository: AuthRepository
) : ViewModel() {

    private val _authState = MutableStateFlow<AuthState>(AuthState.Idle)
    val authState: StateFlow<AuthState> = _authState

    fun signInWithGoogle(data: Intent?) {
        viewModelScope.launch {
            _authState.value = AuthState.Loading
            when (val result = authRepository.signInWithGoogle(data)) {
                is AuthResult.Success -> {
                    _authState.value = AuthState.Success(result.data.user.role)
                }
                is AuthResult.Error -> {
                    _authState.value = AuthState.Error(result.message)
                }
                else -> Unit
            }
        }
    }

    fun logout() {
        viewModelScope.launch {
            _authState.value = AuthState.Loading
            authRepository.logout()
            _authState.value = AuthState.Idle
        }
    }
}

class AuthViewModelFactory(private val context: Context) : ViewModelProvider.Factory {
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        if (modelClass.isAssignableFrom(AuthViewModel::class.java)) {
            @Suppress("UNCHECKED_CAST")
            return AuthViewModel(
                AuthRepository(
                    tokenStorage = TokenStorage(context.applicationContext),
                    firebaseAuthManager = FirebaseAuthManager(context.applicationContext)
                )
            ) as T
        }
        throw IllegalArgumentException("Unknown ViewModel class")
    }
}
