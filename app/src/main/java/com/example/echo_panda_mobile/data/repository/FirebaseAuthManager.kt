package com.example.echo_panda_mobile.data.repository

import android.content.Context
import android.content.Intent
import com.example.echo_panda_mobile.R
import com.google.android.gms.auth.api.signin.GoogleSignIn
import com.google.android.gms.auth.api.signin.GoogleSignInClient
import com.google.android.gms.auth.api.signin.GoogleSignInOptions
import com.google.android.gms.common.api.ApiException
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.GoogleAuthProvider
import kotlinx.coroutines.tasks.await

data class AuthUserData(
    val uid: String,
    val email: String?,
    val displayName: String?,
    val provider: String = "google"
)

sealed class FirebaseAuthResult<out T> {
    data class Success<T>(val data: T) : FirebaseAuthResult<T>()
    data class Error(val message: String) : FirebaseAuthResult<Nothing>()
}

class FirebaseAuthManager(context: Context) {
    private val appContext = context.applicationContext
    private val auth = FirebaseAuth.getInstance()

    fun getGoogleSignInClient(): GoogleSignInClient {
        val gso = GoogleSignInOptions.Builder(GoogleSignInOptions.DEFAULT_SIGN_IN)
            .requestIdToken(appContext.getString(R.string.default_web_client_id))
            .requestEmail()
            .build()
        return GoogleSignIn.getClient(appContext, gso)
    }

    suspend fun signInWithGoogle(data: Intent?): FirebaseAuthResult<AuthUserData> {
        return try {
            val task = GoogleSignIn.getSignedInAccountFromIntent(data)
            val account = task.getResult(ApiException::class.java) 
                ?: return FirebaseAuthResult.Error("Google account not found")
            
            val idToken = account.idToken ?: return FirebaseAuthResult.Error("ID Token is null")
            val credential = GoogleAuthProvider.getCredential(idToken, null)
            
            val authResult = auth.signInWithCredential(credential).await()
            val user = authResult.user ?: return FirebaseAuthResult.Error("Firebase user is null")

            FirebaseAuthResult.Success(
                AuthUserData(
                    uid = user.uid,
                    email = user.email,
                    displayName = user.displayName,
                    provider = "google"
                )
            )
        } catch (e: Exception) {
            FirebaseAuthResult.Error(e.localizedMessage ?: "Google Sign-In failed")
        }
    }
}
