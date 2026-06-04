package com.example.echo_panda_mobile.presentation.viewsmodel

import android.content.Context
import android.content.Intent
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.example.echo_panda_mobile.data.model.AuthResponse
import com.example.echo_panda_mobile.data.repository.AuthRepository
import com.example.echo_panda_mobile.data.repository.AuthResult
import com.example.echo_panda_mobile.data.repository.TokenStorage
import com.google.android.gms.auth.api.signin.GoogleSignIn
import com.google.android.gms.common.api.ApiException
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
    private val authRepository: AuthRepository,
    private val tokenStorage: TokenStorage
) : ViewModel() {

    private val _authState = MutableStateFlow<AuthState>(AuthState.Idle)
    val authState: StateFlow<AuthState> = _authState

    fun signInWithGoogle(data: Intent?) {
        viewModelScope.launch {
            _authState.value = AuthState.Loading
            
            try {
                val task = GoogleSignIn.getSignedInAccountFromIntent(data)
                val account = task.getResult(ApiException::class.java)
                val idToken = account?.idToken

                if (idToken == null) {
                    _authState.value = AuthState.Error("Google ID Token not found.")
                    return@launch
                }

                val result = authRepository.signInWithGoogle(idToken)
                
                when (result) {
                    is AuthResult.Success -> {
                        val authResponse = result.data
                        val role = authResponse.user.role
                        android.util.Log.d("AuthViewModel", "Google sign-in success with role: $role")
                        _authState.value = AuthState.Success(role)
                    }
                    is AuthResult.Error -> {
                        _authState.value = AuthState.Error(result.message)
                    }
                    else -> _authState.value = AuthState.Idle
                }
            } catch (e: Exception) {
                _authState.value = AuthState.Error("Google sign-in failed: ${e.localizedMessage}")
            }
        }
    }

    fun logout() {
        viewModelScope.launch {
            _authState.value = AuthState.Loading
            try {
                authRepository.logout()
            } catch (e: Exception) {
                android.util.Log.e("AuthViewModel", "Logout failed", e)
            } finally {
                _authState.value = AuthState.Idle
            }
        }
    }
}

class AuthViewModelFactory(private val context: Context) : ViewModelProvider.Factory {
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        if (modelClass.isAssignableFrom(AuthViewModel::class.java)) {
            val tokenStorage = TokenStorage.getInstance(context.applicationContext)
            @Suppress("UNCHECKED_CAST")
            return AuthViewModel(
                AuthRepository(tokenStorage),
                tokenStorage
            ) as T
        }
        throw IllegalArgumentException("Unknown ViewModel class")
    }
}
