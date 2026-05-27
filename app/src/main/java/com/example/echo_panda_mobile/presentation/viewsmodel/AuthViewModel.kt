package com.example.echo_panda_mobile.presentation.viewsmodel

import android.content.Context
import android.content.Intent
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.example.echo_panda_mobile.data.remote.FirebaseLoginRequest
import com.example.echo_panda_mobile.data.remote.RetrofitClient
import com.example.echo_panda_mobile.data.repository.FirebaseAuthManager
import com.example.echo_panda_mobile.data.repository.FirebaseAuthResult
import com.example.echo_panda_mobile.data.repository.TokenStorage
import com.google.firebase.auth.FirebaseAuth
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
    private val authManager: FirebaseAuthManager,
    private val tokenStorage: TokenStorage
) : ViewModel() {

    private val _authState = MutableStateFlow<AuthState>(AuthState.Idle)
    val authState: StateFlow<AuthState> = _authState

    fun signInWithGoogle(data: Intent?) {
        viewModelScope.launch {
            _authState.value = AuthState.Loading
            
            val firebaseResult = authManager.signInWithGoogle(data)
            
            when (firebaseResult) {
                is FirebaseAuthResult.Success -> {
                    val firebaseUser = firebaseResult.data
                    try {
                        // Call backend
                        val response = RetrofitClient.getInstance(tokenStorage).firebaseLogin(
                            FirebaseLoginRequest(
                                email = firebaseUser.email ?: "",
                                name = firebaseUser.displayName ?: "",
                                firebase_uid = firebaseUser.uid,
                                provider = firebaseUser.provider
                            )
                        )
                        
                        // Save token and role
                        tokenStorage.saveToken(response.token)
                        tokenStorage.saveRole(response.user.role)
                        
                        _authState.value = AuthState.Success(response.user.role)
                    } catch (e: Exception) {
                        _authState.value = AuthState.Error("Backend login failed: ${e.localizedMessage}")
                    }
                }
                is FirebaseAuthResult.Error -> {
                    _authState.value = AuthState.Error(firebaseResult.message)
                }
            }
        }
    }
    
    fun logout() {
        viewModelScope.launch {
            _authState.value = AuthState.Loading
            try {
                // 1. Call backend logout (AuthInterceptor will add the token)
                RetrofitClient.getInstance(tokenStorage).logout()
            } catch (e: Exception) {
                // Even if backend fails, we should proceed with local logout
                android.util.Log.e("AuthViewModel", "Backend logout failed", e)
            } finally {
                // 2. Firebase Sign Out
                FirebaseAuth.getInstance().signOut()
                
                // 3. Clear local storage
                tokenStorage.clear()
                
                // 4. Reset state to Idle (Login Screen)
                _authState.value = AuthState.Idle
            }
        }
    }
}

class AuthViewModelFactory(private val context: Context) : ViewModelProvider.Factory {
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        if (modelClass.isAssignableFrom(AuthViewModel::class.java)) {
            @Suppress("UNCHECKED_CAST")
            return AuthViewModel(
                FirebaseAuthManager(context.applicationContext),
                TokenStorage(context.applicationContext)
            ) as T
        }
        throw IllegalArgumentException("Unknown ViewModel class")
    }
}
