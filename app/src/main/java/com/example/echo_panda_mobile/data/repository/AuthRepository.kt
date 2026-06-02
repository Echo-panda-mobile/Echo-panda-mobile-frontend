package com.example.echo_panda_mobile.data.repository

import android.content.Intent
import com.example.echo_panda_mobile.data.model.AuthResponse
import com.example.echo_panda_mobile.data.model.LoginRequest
import com.example.echo_panda_mobile.data.model.RegisterRequest
import com.example.echo_panda_mobile.data.model.User
import com.example.echo_panda_mobile.data.remote.FirebaseSessionRequest
import com.example.echo_panda_mobile.data.remote.ResetPasswordRequest
import com.example.echo_panda_mobile.data.remote.RetrofitClient
import androidx.core.net.toUri
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseAuthInvalidCredentialsException
import com.google.firebase.auth.FirebaseAuthInvalidUserException
import com.google.firebase.auth.FirebaseUser
import com.google.firebase.auth.GoogleAuthProvider
import com.google.firebase.firestore.SetOptions
import com.google.firebase.firestore.FirebaseFirestore
import kotlinx.coroutines.tasks.await
import retrofit2.HttpException

sealed class AuthResult<out T> {
    data class Success<T>(val data: T) : AuthResult<T>()
    data class Error(val message: String) : AuthResult<Nothing>()
    object Loading : AuthResult<Nothing>()
}

class AuthRepository(
    private val tokenStorage: TokenStorage,
    private val firebaseAuthManager: FirebaseAuthManager? = null
) {
    private val firebaseAuth = FirebaseAuth.getInstance()
    private val firestore = FirebaseFirestore.getInstance()
    private val authApi get() = RetrofitClient.getAuthService(tokenStorage)

    suspend fun login(request: LoginRequest): AuthResult<AuthResponse> {
        val email = request.email.trim()
        val password = request.password.trim()

        if (email.isBlank() || password.isBlank()) {
            return AuthResult.Error("Email and password are required.")
        }

        tokenStorage.clearSession()

        return try {
            // 1. Try normal Firebase Auth login
            val result = firebaseAuth.signInWithEmailAndPassword(email, password).await()
            val firebaseUser = result.user
                ?: return AuthResult.Error("Login failed. User data not available.")

            syncBackendSession(firebaseUser, provider = "password")
        } catch (e: Exception) {
            // 2. If login fails, check if the password was changed via our "Forgot Password" (Firestore)
            try {
                val querySnapshot = firestore.collection("users")
                    .whereEqualTo("email", email)
                    .get()
                    .await()
                
                if (!querySnapshot.isEmpty) {
                    val dbPassword = querySnapshot.documents[0].getString("password")
                    if (dbPassword != null && dbPassword == password) {
                        // The user entered the new password we saved in Firestore!
                        // We must update the actual Firebase Auth password to match it so they can log in.
                        // This only works if we can sign them in or if we have a way to update it.
                        // For demo: We'll show a helpful message.
                        return AuthResult.Error("Password reset successful in DB. Please use the 'Reset Link' method for real Firebase Auth sync, or contact admin.")
                    }
                }
            } catch (firestoreError: Exception) {
                // Ignore firestore errors and return the original auth error
            }

            AuthResult.Error(mapFirebaseAuthError(e))
        }
    }

    suspend fun signInWithGoogle(idToken: String): AuthResult<AuthResponse> {
        tokenStorage.clearSession()

        return try {
            if (idToken.isBlank()) {
                return AuthResult.Error("Google sign-in token is required.")
            }

            val credential = GoogleAuthProvider.getCredential(idToken, null)
            val result = firebaseAuth.signInWithCredential(credential).await()
            val firebaseUser = result.user
                ?: return AuthResult.Error("Google sign-in failed. User data not available.")

            syncBackendSession(firebaseUser, provider = "google")
        } catch (e: Exception) {
            AuthResult.Error(e.message ?: "Google sign-in failed. Please try again.")
        }
    }

    suspend fun signInWithGoogle(data: Intent?): AuthResult<AuthResponse> {
        val manager = firebaseAuthManager
            ?: return AuthResult.Error("Google sign-in is not configured.")
        return when (val result = manager.signInWithGoogle(data)) {
            is FirebaseAuthResult.Success -> {
                val firebaseUser = firebaseAuth.currentUser
                    ?: return AuthResult.Error("Google sign-in failed. User data not available.")
                syncBackendSession(firebaseUser, provider = result.data.provider)
            }
            is FirebaseAuthResult.Error -> AuthResult.Error(result.message)
        }
    }

    suspend fun register(request: RegisterRequest): AuthResult<AuthResponse> {
        tokenStorage.clearSession()

        return try {
            when {
                request.name.isBlank() -> return AuthResult.Error("Name is required.")
                request.email.isBlank() -> return AuthResult.Error("Email is required.")
                request.password.length < 8 -> return AuthResult.Error("Password must be at least 8 characters.")
                request.password != request.passwordConfirmation -> return AuthResult.Error("Passwords do not match.")
            }

            val normalizedEmail = request.email.trim()

            // 1. Create in Firebase Auth first (This is the most important part)
            val result = firebaseAuth.createUserWithEmailAndPassword(
                normalizedEmail,
                request.password
            ).await()
            val firebaseUser = result.user
                ?: return AuthResult.Error("Sign-up failed. User creation unsuccessful.")

            val profileUpdates = com.google.firebase.auth.UserProfileChangeRequest.Builder()
                .setDisplayName(request.name)
                .build()
            firebaseUser.updateProfile(profileUpdates).await()

            // 2. Try to save to Firestore, but DON'T fail the whole registration if rules block it
            try {
                saveUserProfile(firebaseUser.uid, request.name, normalizedEmail, "user")
            } catch (firestoreError: Exception) {
                android.util.Log.e("AuthRepository", "Firestore save failed (Rules?), but Auth succeeded", firestoreError)
            }
            
            try {
                firebaseUser.sendEmailVerification().await()
            } catch (_: Exception) {}

            // 3. Sync with Laravel Backend
            syncBackendSession(firebaseUser, provider = "password")
        } catch (e: Exception) {
            val msg = when {
                e.message?.contains("already in use") == true -> "Email is already registered. Please log in."
                e.message?.contains("invalid email") == true -> "Invalid email format."
                else -> e.message ?: "Sign-up failed. Please try again."
            }
            AuthResult.Error(msg)
        }
    }

    suspend fun getCurrentUserProfile(): User? {
        val firebaseUser = firebaseAuth.currentUser ?: return null

        // Always re-sync through Firebase so role/token match this Firebase account
        // (avoids a stale Sanctum token from an older session keeping you on the user app).
        return when (val sync = syncBackendSession(firebaseUser, provider = "session_restore")) {
            is AuthResult.Success -> sync.data.user
            else -> null
        }
    }

    suspend fun getCurrentUser(): User? = getCurrentUserProfile()

    suspend fun getUserLikedSongs(): List<String> {
        val firebaseUser = firebaseAuth.currentUser ?: return emptyList()
        val doc = firestore.collection("users").document(firebaseUser.uid).get().await()
        @Suppress("UNCHECKED_CAST")
        return doc.get("likedSongs") as? List<String> ?: emptyList()
    }

    suspend fun getUserPlaylists(): List<String> {
        val firebaseUser = firebaseAuth.currentUser ?: return emptyList()
        val doc = firestore.collection("users").document(firebaseUser.uid).get().await()
        @Suppress("UNCHECKED_CAST")
        return doc.get("playlists") as? List<String> ?: emptyList()
    }

    suspend fun updateUserProfile(
        name: String,
        email: String,
        role: String? = null,
        photoUrl: String? = null
    ): AuthResult<Unit> {
        val firebaseUser = firebaseAuth.currentUser
            ?: return AuthResult.Error("User not signed in.")
        return try {
            val normalizedName = name.trim()
            val normalizedEmail = email.trim().ifBlank { firebaseUser.email.orEmpty() }

            val profileUpdatesBuilder = com.google.firebase.auth.UserProfileChangeRequest.Builder()
                .setDisplayName(normalizedName)

            photoUrl?.let {
                profileUpdatesBuilder.setPhotoUri(it.toUri())
            }

            firebaseUser.updateProfile(profileUpdatesBuilder.build()).await()

            if (normalizedEmail.isNotBlank() && normalizedEmail != firebaseUser.email) {
                firebaseUser.updateEmail(normalizedEmail).await()
            }

            val data = mutableMapOf<String, Any>("name" to normalizedName)
            if (normalizedEmail.isNotBlank()) data["email"] = normalizedEmail
            role?.takeIf { it.isNotBlank() }?.let { data["role"] = it }
            photoUrl?.let { data["photoUrl"] = it }

            firestore.collection("users").document(firebaseUser.uid)
                .set(data, SetOptions.merge()).await()

            AuthResult.Success(Unit)
        } catch (e: Exception) {
            val msg = when {
                e.message?.contains("permission denied", ignoreCase = true) == true ->
                    "Unable to save profile. Please check permissions."
                e.message?.contains("requires-recent-login", ignoreCase = true) == true ->
                    "Please log in again before changing your email."
                else -> e.message ?: "Could not update profile. Please try again."
            }
            AuthResult.Error(msg)
        }
    }

    fun isLoggedIn(): Boolean =
        firebaseAuth.currentUser != null && !tokenStorage.getToken().isNullOrBlank()

    suspend fun logout() {
        try {
            authApi.logout()
        } catch (_: Exception) {
            // Proceed with local logout even if the backend call fails.
        } finally {
            firebaseAuth.signOut()
            tokenStorage.clearSession()
        }
    }

    suspend fun updatePassword(newPassword: String): AuthResult<Unit> {
        return try {
            val user = firebaseAuth.currentUser
                ?: return AuthResult.Error("No user signed in. Please log in again.")
            
            user.updatePassword(newPassword).await()
            AuthResult.Success(Unit)
        } catch (e: Exception) {
            val msg = when {
                e.message?.contains("requires-recent-login", ignoreCase = true) == true ->
                    "For security, please log in again before changing your password."
                else -> e.message ?: "Failed to update password."
            }
            AuthResult.Error(msg)
        }
    }

    /**
     * Stores the new password in Firestore for demo purposes.
     * Note: In a real app, you would use a backend Admin SDK to update Firebase Auth.
     */
    suspend fun storeNewPasswordInFirestore(email: String, newPassword: String): AuthResult<Unit> {
        return try {
            val trimmedEmail = email.trim()
            
            // 1. Find the user document by email
            val querySnapshot = firestore.collection("users")
                .whereEqualTo("email", trimmedEmail)
                .get()
                .await()

            if (querySnapshot.isEmpty) {
                return AuthResult.Error("User not found in database.")
            }

            val docId = querySnapshot.documents[0].id

            // 2. Update the password field in Firestore
            // We store it here so the login flow can potentially check it 
            // if you customize your login to use Firestore passwords.
            firestore.collection("users").document(docId)
                .update("password", newPassword)
                .await()

            AuthResult.Success(Unit)
        } catch (e: Exception) {
            AuthResult.Error(e.message ?: "Failed to save new password to database.")
        }
    }

    /**
     * Resets password using backend API (Admin SDK required on server).
     * This bypasses the need for a token/link for demo purposes.
     */
    suspend fun resetPasswordWithoutToken(email: String, newPassword: String): AuthResult<Unit> {
        return try {
            val response = authApi.resetPassword(
                ResetPasswordRequest(
                    email = email.trim(),
                    password = newPassword,
                    passwordConfirmation = newPassword
                )
            )

            if (response.isSuccessful) {
                AuthResult.Success(Unit)
            } else {
                val errorMsg = response.errorBody()?.string() ?: "Failed to reset password."
                AuthResult.Error(errorMsg)
            }
        } catch (e: Exception) {
            AuthResult.Error(e.message ?: "Could not connect to server to reset password.")
        }
    }

    suspend fun checkEmailExistsInFirestore(email: String): Boolean {
        val trimmedEmail = email.trim()
        return try {
            // Check in "users" collection (case-sensitive)
            val querySnapshot = firestore.collection("users")
                .whereEqualTo("email", trimmedEmail)
                .get()
                .await()
            
            if (!querySnapshot.isEmpty) return true

            // Fallback: Check in Authentication if Firestore check fails or returns empty
            // This handles cases where the user exists in Auth but hasn't been synced to Firestore yet.
            val methods = firebaseAuth.fetchSignInMethodsForEmail(trimmedEmail).await().signInMethods
            !methods.isNullOrEmpty()
        } catch (e: Exception) {
            android.util.Log.e("AuthRepository", "Verification failed for $trimmedEmail", e)
            // If the error is about enumeration protection, we fallback to a "best guess" 
            // by attempting to send a reset email. If it doesn't throw "no user record", it exists.
            try {
                firebaseAuth.sendPasswordResetEmail(trimmedEmail).await()
                true
            } catch (resetError: Exception) {
                !resetError.message?.contains("no user record", ignoreCase = true)!!
            }
        }
    }

    suspend fun sendPasswordResetEmail(email: String): AuthResult<Unit> {
        return try {
            val trimmedEmail = email.trim()
            if (trimmedEmail.isBlank()) return AuthResult.Error("Email is required.")
            
            android.util.Log.d("AuthRepository", "DEBUG: Requesting reset for |$trimmedEmail|")
            
            firebaseAuth.sendPasswordResetEmail(trimmedEmail).await()
            
            android.util.Log.d("AuthRepository", "DEBUG: Firebase reported SUCCESS for |$trimmedEmail|")
            AuthResult.Success(Unit)
        } catch (e: Exception) {
            // DETAILED LOGGING to find the hidden cause
            val errorCode = (e as? com.google.firebase.auth.FirebaseAuthException)?.errorCode ?: "Unknown"
            android.util.Log.e("AuthRepository", "DEBUG: Reset FAILED for |$email|", e)
            android.util.Log.e("AuthRepository", "DEBUG: Error Code: $errorCode | Message: ${e.message}")

            val msg = when {
                errorCode == "ERROR_USER_NOT_FOUND" -> "This email is not registered in the 'Users' list."
                errorCode == "ERROR_TOO_MANY_REQUESTS" -> "Too many attempts. Wait 10 minutes."
                else -> e.message ?: "Failed to send reset email. Check console settings."
            }
            AuthResult.Error(msg)
        }
    }

    private suspend fun syncBackendSession(
        firebaseUser: FirebaseUser,
        provider: String
    ): AuthResult<AuthResponse> {
        return try {
            val idToken = firebaseUser.getIdToken(true).await().token
                ?: return AuthResult.Error("Could not obtain Firebase session token.")

            val response = authApi.firebaseSession(
                FirebaseSessionRequest(
                    id_token = idToken,
                    email = firebaseUser.email,
                    name = firebaseUser.displayName,
                    provider = provider
                )
            )

            val normalizedRole = response.user.role.trim().lowercase()

            tokenStorage.saveToken(response.token)
            tokenStorage.saveRole(normalizedRole)

            val user = response.user.copy(role = normalizedRole).toUser(firebaseUser)
            AuthResult.Success(
                AuthResponse(
                    user = user,
                    token = response.token,
                    message = response.message ?: "Login successful.",
                    redirectTo = response.redirect_to
                )
            )
        } catch (e: HttpException) {
            val body = e.response()?.errorBody()?.string()
            AuthResult.Error(
                body?.takeIf { it.isNotBlank() }
                    ?: "Backend login failed (${e.code()})."
            )
        } catch (e: Exception) {
            AuthResult.Error(
                e.message ?: "Could not connect to the server. Check your connection."
            )
        }
    }

    private suspend fun saveUserProfile(
        userId: String,
        name: String,
        email: String,
        role: String,
        photoUrl: String? = null
    ) {
        val data = mutableMapOf(
            "name" to name,
            "email" to email,
            "role" to role,
            "provider" to "password",
            "createdAt" to System.currentTimeMillis()
        )
        photoUrl?.let { data["photoUrl"] = it }
        firestore.collection("users").document(userId).set(data).await()
    }

    private fun mapFirebaseAuthError(e: Exception): String = when {
        e is FirebaseAuthInvalidCredentialsException &&
            e.errorCode == "ERROR_INVALID_EMAIL" -> "Invalid email format."
        e is FirebaseAuthInvalidCredentialsException &&
            e.errorCode == "ERROR_WRONG_PASSWORD" -> incorrectFirebaseCredentialsMessage()
        e is FirebaseAuthInvalidCredentialsException &&
            (e.errorCode == "ERROR_INVALID_CREDENTIAL" ||
                e.message?.contains("incorrect, malformed or has expired", ignoreCase = true) == true) ->
            incorrectFirebaseCredentialsMessage()
        e is FirebaseAuthInvalidCredentialsException &&
            e.errorCode == "ERROR_INVALID_CUSTOM_TOKEN" -> "Invalid or expired credentials."
        e is FirebaseAuthInvalidUserException -> incorrectFirebaseCredentialsMessage()
        e.message?.contains("no user record", ignoreCase = true) == true -> incorrectFirebaseCredentialsMessage()
        e.message?.contains("password is invalid", ignoreCase = true) == true -> "Incorrect password."
        e.message?.contains("incorrect, malformed or has expired", ignoreCase = true) == true ->
            incorrectFirebaseCredentialsMessage()
        e.message?.contains("user has been disabled", ignoreCase = true) == true ->
            "This account has been disabled."
        e.message?.contains("too many unsuccessful", ignoreCase = true) == true ->
            "Too many login attempts. Try again later."
        else -> e.message ?: "Login failed. Please try again."
    }

    private fun incorrectFirebaseCredentialsMessage(): String =
        "Wrong email or password for Firebase. The web admin password is separate — " +
            "create this email in Firebase Console (project echo-panda-auth) or reset the password there."

    private fun com.example.echo_panda_mobile.data.remote.BackendUser.toUser(
        firebaseUser: FirebaseUser
    ): User = User(
        id = id,
        name = name,
        email = email,
        role = role,
        token = tokenStorage.getToken().orEmpty(),
        photoUrl = firebaseUser.photoUrl?.toString()
    )
}
