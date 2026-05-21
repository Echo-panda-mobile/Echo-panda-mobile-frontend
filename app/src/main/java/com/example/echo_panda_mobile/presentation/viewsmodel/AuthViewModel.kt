package com.example.echo_panda_mobile.presentation.views.auth

import androidx.lifecycle.ViewModel
import com.google.firebase.auth.FirebaseAuth
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow

class AuthViewModel : ViewModel() {
    private val auth = FirebaseAuth.getInstance()

    private val _authState = MutableStateFlow<AuthState>(AuthState.Idle)
    val authState: StateFlow<AuthState> = _authState

    sealed class AuthState {
        object Idle : AuthState()
        object Loading : AuthState()
        object VerificationEmailSent : AuthState()
        object Success : AuthState()
        data class Error(val message: String) : AuthState()
    }

    fun signUp(email: String, pass: String) {
        _authState.value = AuthState.Loading
        auth.createUserWithEmailAndPassword(email, pass)
            .addOnCompleteListener { task ->
                if (task.isSuccessful) {
                    // Send Firebase Verification Link
                    auth.currentUser?.sendEmailVerification()
                    _authState.value = AuthState.VerificationEmailSent
                } else {
                    _authState.value = AuthState.Error(task.exception?.message ?: "Sign up failed")
                }
            }
    }

    fun checkEmailVerified() {
        auth.currentUser?.reload()?.addOnCompleteListener {
            if (auth.currentUser?.isEmailVerified == true) {
                _authState.value = AuthState.Success
            }
        }
    }
}