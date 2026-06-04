package com.example.echo_panda_mobile.data.repository

import com.example.echo_panda_mobile.data.model.*
import androidx.core.net.toUri
import com.example.echo_panda_mobile.data.remote.RetrofitClient
import com.example.echo_panda_mobile.data.remote.FirebaseSessionRequest
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseAuthInvalidCredentialsException
import com.google.firebase.auth.FirebaseAuthInvalidUserException
import com.google.firebase.auth.FirebaseUser
import com.google.firebase.auth.GoogleAuthProvider
import com.google.firebase.firestore.SetOptions
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.DocumentSnapshot
import kotlinx.coroutines.tasks.await

sealed class AuthResult<out T> {
    data class Success<out T>(val data: T) : AuthResult<T>()
    data class Error(val message: String) : AuthResult<Nothing>()
    object Loading : AuthResult<Nothing>()
}

class AuthRepository(private val tokenStorage: TokenStorage) {
    private val firebaseAuth = FirebaseAuth.getInstance()
    private val firestore = FirebaseFirestore.getInstance()
    private val authApi get() = RetrofitClient.getAuthService(tokenStorage)

    suspend fun artistLogin(request: LoginRequest): AuthResult<AuthResponse> {
        return try {
            android.util.Log.d("AuthRepository", "━━━ ARTIST LOGIN START ━━━")
            android.util.Log.d("AuthRepository", "Email: ${request.email}")
            
            val response = authApi.login(request)
            android.util.Log.d("AuthRepository", "✓ Login API response received")
            
            if (response.token.isBlank()) {
                return AuthResult.Error("Login failed: Server returned no authentication token")
            }
            
            val finalRole = if (response.user.role.isBlank() || response.user.role.lowercase().trim() == "user") {
                "artist"
            } else {
                response.user.role.lowercase().trim()
            }
            
            val updatedUser = response.user.copy(role = finalRole)
            val finalResponse = response.copy(user = updatedUser)

            tokenStorage.saveToken(finalResponse.token)
            tokenStorage.saveRole(finalRole)
            tokenStorage.saveEmail(finalResponse.user.email)
            tokenStorage.saveName(finalResponse.user.name)
            tokenStorage.saveUserId(finalResponse.user.id)
            
            RetrofitClient.resetAll()
            
            android.util.Log.d("AuthRepository", "━━━ ARTIST LOGIN SUCCESS ━━━")
            AuthResult.Success(finalResponse)
        } catch (e: Exception) {
            android.util.Log.e("AuthRepository", "Artist login failed: ${e.message}")
            AuthResult.Error(e.message ?: "Artist login failed")
        }
    }

    suspend fun login(request: LoginRequest): AuthResult<AuthResponse> {
        val email = request.email.trim()
        val password = request.password.trim()

        if (email.isBlank() || password.isBlank()) {
            return AuthResult.Error("Email and password are required.")
        }

        logoutLocally()

        // 1. Admin check — Firestore "admins" collection
        val adminDoc = findDocumentByEmail("admins", email)
        if (adminDoc != null) {
            val storedPassword = adminDoc.getString("password")?.trim().orEmpty()
            if (storedPassword == password) {
                return artistLogin(request) 
            } else {
                return AuthResult.Error("Incorrect password for Admin account.")
            }
        }

        // 2. Artist check — Firestore "artists" collection
        val artistDoc = findDocumentByEmail("artists", email)
        if (artistDoc != null) {
            val storedPassword = artistDoc.getString("password")?.trim().orEmpty()
            if (storedPassword == password) {
                return artistLogin(request)
            } else {
                return AuthResult.Error("Incorrect password for Artist account.")
            }
        }

        // 3. Regular user — Firebase Auth
        return loginFirebaseUser(email, password)
    }

    private suspend fun loginFirebaseUser(email: String, password: String): AuthResult<AuthResponse> {
        return try {
            val result = firebaseAuth.signInWithEmailAndPassword(email, password).await()
            val firebaseUser = result.user ?: return AuthResult.Error("Login failed. User data not available.")
            
            syncBackendSession(firebaseUser, provider = "password")
        } catch (e: Exception) {
            AuthResult.Error(mapFirebaseAuthError(e))
        }
    }

    suspend fun signInWithGoogle(idToken: String): AuthResult<AuthResponse> {
        logoutLocally()
        return try {
            if (idToken.isBlank()) return AuthResult.Error("Google sign-in token is required.")

            val credential = GoogleAuthProvider.getCredential(idToken, null)
            val result = firebaseAuth.signInWithCredential(credential).await()
            val firebaseUser = result.user ?: return AuthResult.Error("Google sign-in failed. User data not available.")

            syncBackendSession(firebaseUser, provider = "google")
        } catch (e: Exception) {
            AuthResult.Error(e.message ?: "Google sign-in failed. Please try again.")
        }
    }

    suspend fun register(request: RegisterRequest): AuthResult<AuthResponse> {
        return try {
            when {
                request.name.isBlank() -> return AuthResult.Error("Name is required.")
                request.email.isBlank() -> return AuthResult.Error("Email is required.")
                request.password.length < 8 -> return AuthResult.Error("Password must be at least 8 characters.")
                request.password != request.passwordConfirmation -> return AuthResult.Error("Passwords do not match.")
            }

            if (request.role.equals("artist", ignoreCase = true) || request.role.equals("admin", ignoreCase = true)) {
                return AuthResult.Error("This role cannot be self-registered. Please contact support.")
            }

            val result = firebaseAuth.createUserWithEmailAndPassword(request.email.trim(), request.password).await()
            val firebaseUser = result.user ?: return AuthResult.Error("Sign-up failed. User creation unsuccessful.")

            val profileUpdates = com.google.firebase.auth.UserProfileChangeRequest.Builder()
                .setDisplayName(request.name)
                .build()
            firebaseUser.updateProfile(profileUpdates).await()

            saveUserProfile(firebaseUser.uid, request.name, request.email.trim(), "user")
            firebaseUser.sendEmailVerification().await()

            syncBackendSession(firebaseUser, provider = "password")
        } catch (e: Exception) {
            AuthResult.Error(e.message ?: "Sign-up failed.")
        }
    }

    /**
     * Returns the signed-in user from memory/disk when available.
     * Does not call the backend — safe for tab switches and screen recomposition.
     */
    fun getCachedUser(): User? {
        val firebaseUser = firebaseAuth.currentUser ?: return null
        UserSessionCache.getIfFresh()?.let { return it }
        return UserSessionCache.fromStorage(tokenStorage, firebaseUser)
    }

    /**
     * Resolves the user profile, syncing with the backend only when cache is missing or stale.
     */
    suspend fun getCurrentUserProfile(forceRefresh: Boolean = false): User? {
        val firebaseUser = firebaseAuth.currentUser ?: return null

        if (!forceRefresh) {
            getCachedUser()?.let { return it }
        }

        return when (val sync = syncBackendSession(firebaseUser, provider = "session_restore")) {
            is AuthResult.Success -> {
                val user = sync.data.user
                UserSessionCache.persist(tokenStorage, user)
                user
            }
            else -> getCachedUser()
        }
    }

    suspend fun refreshCurrentUserProfile(): User? =
        getCurrentUserProfile(forceRefresh = true)

    suspend fun getCurrentUser(): User? = getCachedUser() ?: getCurrentUserProfile()

    /**
     * Profile photo is not on the Laravel user model — resolve from encrypted prefs,
     * Firebase Auth, then Firestore `users/{uid}.photoUrl`.
     */
    suspend fun resolveProfilePhotoUrl(): String? {
        tokenStorage.getPhotoUrl()?.let { return it }
        val firebaseUser = firebaseAuth.currentUser ?: return null
        firebaseUser.photoUrl?.toString()?.let { return it }
        return try {
            firestore.collection("users").document(firebaseUser.uid).get().await()
                .getString("photoUrl")
                ?.takeIf { it.isNotBlank() }
        } catch (_: Exception) {
            null
        }
    }

    private suspend fun findDocumentByEmail(collection: String, email: String): DocumentSnapshot? {
        val trimmedEmail = email.trim()
        val lowerEmail = trimmedEmail.lowercase()
        
        return try {
            val q1 = firestore.collection(collection).whereEqualTo("email", trimmedEmail).limit(1).get().await()
            if (!q1.isEmpty) return q1.documents.first()

            if (trimmedEmail != lowerEmail) {
                val q2 = firestore.collection(collection).whereEqualTo("email", lowerEmail).limit(1).get().await()
                if (!q2.isEmpty) return q2.documents.first()
            }
            
            val d1 = firestore.collection(collection).document(trimmedEmail).get().await()
            if (d1.exists()) return d1

            val d2 = firestore.collection(collection).document(lowerEmail).get().await()
            if (d2.exists()) return d2

            null
        } catch (e: Exception) {
            null
        }
    }

    private suspend fun saveUserProfile(userId: String, name: String, email: String, role: String, photoUrl: String? = null) {
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

    suspend fun updateUserProfile(name: String, email: String, role: String? = null, photoUrl: String? = null): AuthResult<Unit> {
        val firebaseUser = firebaseAuth.currentUser
        val currentEmail = firebaseUser?.email ?: tokenStorage.getEmail() ?: return AuthResult.Error("User not signed in.")
        
        return try {
            val normalizedName = name.trim()
            val normalizedEmail = email.trim().ifBlank { currentEmail }

            if (firebaseUser != null) {
                val profileUpdates = com.google.firebase.auth.UserProfileChangeRequest.Builder()
                    .setDisplayName(normalizedName)
                    .setPhotoUri(photoUrl?.toUri())
                    .build()
                firebaseUser.updateProfile(profileUpdates).await()
            }

            val actualRole = role ?: tokenStorage.getRole() ?: "user"
            val collection = when (actualRole.lowercase()) {
                "admin" -> "admins"
                "artist" -> "artists"
                else -> "users"
            }

            val data = mutableMapOf<String, Any>("name" to normalizedName)
            if (normalizedEmail.isNotBlank()) data["email"] = normalizedEmail
            photoUrl?.let { data["photoUrl"] = it }

            if (collection == "users" && firebaseUser != null) {
                firestore.collection("users").document(firebaseUser.uid).set(data, SetOptions.merge()).await()
            } else {
                val doc = findDocumentByEmail(collection, currentEmail)
                doc?.reference?.set(data, SetOptions.merge())?.await()
            }

            getCachedUser()?.let { current ->
                UserSessionCache.persist(
                    tokenStorage,
                    current.copy(
                        name = normalizedName,
                        email = normalizedEmail,
                        photoUrl = photoUrl ?: current.photoUrl ?: firebaseUser.photoUrl?.toString()
                    )
                )
            }

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

    fun isLoggedIn(): Boolean = firebaseAuth.currentUser != null && !tokenStorage.getToken().isNullOrBlank()

    suspend fun logout() {
        try {
            authApi.logout()
        } catch (_: Exception) {
            // Proceed with local logout even if the backend call fails.
        } finally {
            firebaseAuth.signOut()
            UserSessionCache.invalidate()
            tokenStorage.clearSession()
        }
    }

    private fun logoutLocally() {
        firebaseAuth.signOut()
        tokenStorage.clearSession()
        RetrofitClient.clearInstances()
    }

    suspend fun sendPasswordResetEmail(email: String): AuthResult<Unit> {
        return try {
            if (email.isBlank()) return AuthResult.Error("Email is required.")
            firebaseAuth.sendPasswordResetEmail(email.trim()).await()
            AuthResult.Success(Unit)
        } catch (e: Exception) {
            AuthResult.Error(e.message ?: "Failed to send reset email.")
        }
    }

    private suspend fun syncBackendSession(firebaseUser: FirebaseUser, provider: String): AuthResult<AuthResponse> {
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
            UserSessionCache.persist(tokenStorage, user)
            AuthResult.Success(
                AuthResponse(
                    user = user,
                    token = response.token,
                    message = response.message ?: "Login successful.",
                    redirectTo = response.redirect_to
                )
            )
        } catch (e: Exception) {
            AuthResult.Error(e.message ?: "Backend sync failed.")
        }
    }

    private fun mapFirebaseAuthError(e: Exception): String = when {
        e is FirebaseAuthInvalidCredentialsException -> "Invalid email or password."
        e is FirebaseAuthInvalidUserException -> incorrectFirebaseCredentialsMessage()
        else -> e.message ?: "Login failed."
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
        photoUrl = tokenStorage.getPhotoUrl() ?: firebaseUser.photoUrl?.toString()
    )
}
