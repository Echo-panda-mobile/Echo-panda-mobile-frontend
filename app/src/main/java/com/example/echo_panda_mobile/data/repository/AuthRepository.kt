package com.example.echo_panda_mobile.data.repository

import com.example.echo_panda_mobile.data.model.AuthResponse
import com.example.echo_panda_mobile.data.model.LoginRequest
import com.example.echo_panda_mobile.data.model.RegisterRequest
import com.example.echo_panda_mobile.data.model.User
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseAuthInvalidCredentialsException
import com.google.firebase.auth.FirebaseAuthInvalidUserException
import com.google.firebase.auth.GoogleAuthProvider
import com.google.firebase.firestore.DocumentSnapshot
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.SetOptions
import kotlinx.coroutines.tasks.await

sealed class AuthResult<out T> {
    data class Success<T>(val data: T) : AuthResult<T>()
    data class Error(val message: String) : AuthResult<Nothing>()
    object Loading : AuthResult<Nothing>()
}

class AuthRepository {
    private val firebaseAuth = FirebaseAuth.getInstance()
    private val firestore = FirebaseFirestore.getInstance()

    private suspend fun saveUserProfile(userId: String, name: String, email: String, role: String) {
        val profile = mapOf(
            "name" to name,
            "email" to email,
            "role" to role,
            "provider" to "password",
            "createdAt" to System.currentTimeMillis()
        )
        firestore.collection("users").document(userId)
            .set(profile)
            .await()
    }

    private suspend fun saveGoogleUserProfile(userId: String, name: String, email: String, role: String) {
        val profile = mapOf(
            "name" to name,
            "email" to email,
            "role" to role,
            "provider" to "google",
            "createdAt" to System.currentTimeMillis()
        )
        firestore.collection("users").document(userId)
            .set(profile)
            .await()
    }

    /**
     * Log in user with email and password using Firebase Authentication
     */
    suspend fun login(request: LoginRequest): AuthResult<AuthResponse> {
        val normalizedEmail = request.email.trim()
        val normalizedPassword = request.password.trim()

        if (normalizedEmail.isBlank() || normalizedPassword.isBlank()) {
            return AuthResult.Error("Email and password are required.")
        }

        val artistDocument = findArtistDocumentByEmail(normalizedEmail)
        if (artistDocument != null) {
            return loginArtist(artistDocument, normalizedEmail, normalizedPassword)
        }

        return loginFirebaseUser(normalizedEmail, normalizedPassword)
    }

    private suspend fun loginFirebaseUser(
        email: String,
        password: String
    ): AuthResult<AuthResponse> {
        return try {
            val result = firebaseAuth.signInWithEmailAndPassword(
                email,
                password
            ).await()

            val firebaseUser = result.user
                ?: return AuthResult.Error("Login failed. User data not available.")

            val token = firebaseUser.getIdToken(false).await().token
                ?: "firebase-token"

            val userDocument = firestore.collection("users").document(firebaseUser.uid).get().await()
            val userName = userDocument.getString("name") ?: firebaseUser.displayName ?: "User"
            val storedEmail = userDocument.getString("email") ?: firebaseUser.email ?: email
            val role = userDocument.getString("role") ?: if (isArtistEmail(storedEmail)) "artist" else "user"

            if (!userDocument.exists()) {
                saveUserProfile(firebaseUser.uid, userName, storedEmail, role)
            }

            AuthResult.Success(
                AuthResponse(
                    user = User(
                        id = firebaseUser.uid.hashCode(),
                        name = userName,
                        email = storedEmail,
                        role = role,
                        token = token
                    ),
                    token = token,
                    message = "Login successful."
                )
            )
        } catch (firebaseException: Exception) {
            val errorMessage = when {
                firebaseException is FirebaseAuthInvalidCredentialsException && firebaseException.errorCode == "ERROR_INVALID_EMAIL" ->
                    "Invalid email format."
                firebaseException is FirebaseAuthInvalidCredentialsException && firebaseException.errorCode == "ERROR_WRONG_PASSWORD" ->
                    "Incorrect password."
                firebaseException is FirebaseAuthInvalidCredentialsException && firebaseException.errorCode == "ERROR_INVALID_CUSTOM_TOKEN" ->
                    "Invalid or expired credentials."
                firebaseException is FirebaseAuthInvalidUserException ->
                    "User not found. Please sign up."
                firebaseException.message?.contains("no user record", ignoreCase = true) == true ->
                    "User not found. Please sign up."
                firebaseException.message?.contains("password is invalid", ignoreCase = true) == true ->
                    "Incorrect password."
                firebaseException.message?.contains("user has been disabled", ignoreCase = true) == true ->
                    "This account has been disabled."
                firebaseException.message?.contains("too many unsuccessful login attempts", ignoreCase = true) == true ->
                    "Too many login attempts. Try again later."
                else ->
                    firebaseException.message ?: "Login failed. Please try again."
            }
            AuthResult.Error(errorMessage)
        }
    }

    private suspend fun findArtistDocumentByEmail(email: String): DocumentSnapshot? = runCatching {
        val normalizedEmail = email.trim().lowercase()

        val exactMatch = firestore.collection("artists")
            .whereEqualTo("email", email.trim())
            .limit(1)
            .get()
            .await()
            .documents
            .firstOrNull()

        exactMatch ?: firestore.collection("artists")
            .get()
            .await()
            .documents
            .firstOrNull { document ->
                document.getString("email")?.trim()?.lowercase() == normalizedEmail
            }
    }.getOrNull()

    private suspend fun isArtistEmail(email: String): Boolean {
        return findArtistDocumentByEmail(email) != null
    }

    private suspend fun loginArtist(
        document: DocumentSnapshot,
        email: String,
        password: String
    ): AuthResult<AuthResponse> {
        return try {
            val storedPassword = document.getString("password")?.trim().orEmpty()
            if (storedPassword.isBlank()) {
                return AuthResult.Error("Artist account is missing a password in Firestore.")
            }

            if (storedPassword != password) {
                return AuthResult.Error("Incorrect password.")
            }

            val name = document.getString("name")?.takeIf { it.isNotBlank() }
                ?: email.substringBefore("@")
            val token = "artist-firestore-token"

            AuthResult.Success(
                AuthResponse(
                    user = User(
                        id = document.id.hashCode(),
                        name = name,
                        email = email,
                        role = "artist",
                        token = token
                    ),
                    token = token,
                    message = "Artist login successful."
                )
            )
        } catch (e: Exception) {
            AuthResult.Error(e.message ?: "Artist login failed. Please try again.")
        }
    }

    /**
     * Sign in user with Google using Firebase Authentication
     */
    suspend fun signInWithGoogle(idToken: String): AuthResult<AuthResponse> {
        return try {
            if (idToken.isBlank()) {
                return AuthResult.Error("Google sign-in token is required.")
            }

            val credential = GoogleAuthProvider.getCredential(idToken, null)
            val result = firebaseAuth.signInWithCredential(credential).await()
            val firebaseUser = result.user
                ?: return AuthResult.Error("Google sign-in failed. User data not available.")

            val token = firebaseUser.getIdToken(false).await().token
                ?: "firebase-token"

            val email = firebaseUser.email ?: ""
            val profileDoc = firestore.collection("users").document(firebaseUser.uid).get().await()
            val name = profileDoc.getString("name") ?: firebaseUser.displayName ?: "User"
            val role = profileDoc.getString("role") ?: if (email.isNotBlank() && isArtistEmail(email)) {
                "artist"
            } else {
                "user"
            }

            if (!profileDoc.exists()) {
                saveGoogleUserProfile(firebaseUser.uid, name, email, role)
            }

            val user = User(
                id    = firebaseUser.uid.hashCode(),
                name  = name,
                email = email,
                role  = role,
                token = token
            )

            AuthResult.Success(
                AuthResponse(
                    user    = user,
                    token   = token,
                    message = "Google sign-in successful."
                )
            )
        } catch (e: Exception) {
            val errorMessage = when {
                e.message?.contains("A network error") == true -> "Network error. Please try again."
                else -> e.message ?: "Google sign-in failed. Please try again."
            }
            AuthResult.Error(errorMessage)
        }
    }

    /**
     * Register new user with email and password using Firebase Authentication
     */
    suspend fun register(request: RegisterRequest): AuthResult<AuthResponse> {
        return try {
            // Validate input
            when {
                request.name.isBlank()                          -> return AuthResult.Error("Name is required.")
                request.email.isBlank()                         -> return AuthResult.Error("Email is required.")
                request.password.length < 8                     -> return AuthResult.Error("Password must be at least 8 characters.")
                request.password != request.passwordConfirmation -> return AuthResult.Error("Passwords do not match.")
            }

            if (request.role.equals("artist", ignoreCase = true)) {
                return AuthResult.Error(
                    "Artist accounts must be created by an administrator. Please register as a listener or contact support."
                )
            }

            // Create user in Firebase
            val result = firebaseAuth.createUserWithEmailAndPassword(
                request.email.trim(),
                request.password
            ).await()

            val firebaseUser = result.user
                ?: return AuthResult.Error("Sign-up failed. User creation unsuccessful.")

            // Update user profile with name
            val profileUpdates = com.google.firebase.auth.UserProfileChangeRequest.Builder()
                .setDisplayName(request.name)
                .build()
            firebaseUser.updateProfile(profileUpdates).await()

            // Persist profile in Firestore
            saveUserProfile(firebaseUser.uid, request.name, request.email.trim(), request.role)

            // Get Firebase ID token
            val token = firebaseUser.getIdToken(false).await().token
                ?: "firebase-token"

            // Create user object
            val user = User(
                id    = firebaseUser.uid.hashCode(),
                name  = request.name,
                email = request.email,
                role  = request.role,
                token = token
            )

            // Send email verification
            firebaseUser.sendEmailVerification().await()

            AuthResult.Success(
                AuthResponse(
                    user    = user,
                    token   = token,
                    message = "Account created successfully. Please verify your email."
                )
            )
        } catch (e: Exception) {
            val errorMessage = when {
                e.message?.contains("already in use") == true -> "Email is already registered. Please log in."
                e.message?.contains("invalid email") == true -> "Invalid email format."
                e.message?.contains("password is too weak") == true -> "Password is too weak. Use at least 8 characters."
                else -> e.message ?: "Sign-up failed. Please try again."
            }
            AuthResult.Error(errorMessage)
        }
    }

    suspend fun getCurrentUserProfile(): User? {
        val firebaseUser = firebaseAuth.currentUser ?: return null
        val email = firebaseUser.email ?: ""
        val userDoc = firestore.collection("users").document(firebaseUser.uid).get().await()
        val artistDoc = if (email.isNotBlank()) findArtistDocumentByEmail(email) else null

        val name = userDoc.getString("name")
            ?: artistDoc?.getString("name")
            ?: firebaseUser.displayName
            ?: "User"

        val role = userDoc.getString("role")
            ?: artistDoc?.getString("role")
            ?: if (artistDoc != null) "artist" else "user"

        return User(
            id = firebaseUser.uid.hashCode(),
            name = name,
            email = email,
            role = role,
            token = ""
        )
    }

    suspend fun getUserLikedSongs(): List<String> {
        val firebaseUser = firebaseAuth.currentUser ?: return emptyList()
        val profileDoc = firestore.collection("users").document(firebaseUser.uid).get().await()
        @Suppress("UNCHECKED_CAST")
        return profileDoc.get("likedSongs") as? List<String> ?: emptyList()
    }

    suspend fun getUserPlaylists(): List<String> {
        val firebaseUser = firebaseAuth.currentUser ?: return emptyList()
        val profileDoc = firestore.collection("users").document(firebaseUser.uid).get().await()
        @Suppress("UNCHECKED_CAST")
        return profileDoc.get("playlists") as? List<String> ?: emptyList()
    }

    suspend fun updateUserProfile(
        name: String,
        email: String,
        role: String? = null
    ): AuthResult<Unit> {
        val firebaseUser = firebaseAuth.currentUser ?: return AuthResult.Error("User not signed in.")
        return try {
            val normalizedName = name.trim()
            val normalizedEmail = email.trim().ifBlank { firebaseUser.email.orEmpty() }

            val profileUpdates = com.google.firebase.auth.UserProfileChangeRequest.Builder()
                .setDisplayName(normalizedName)
                .build()

            firebaseUser.updateProfile(profileUpdates).await()

            if (normalizedEmail.isNotBlank() && normalizedEmail != firebaseUser.email) {
                firebaseUser.updateEmail(normalizedEmail).await()
            }

            val profileData = mutableMapOf<String, Any>(
                "name" to normalizedName
            )
            if (normalizedEmail.isNotBlank()) {
                profileData["email"] = normalizedEmail
            }
            role?.takeIf { it.isNotBlank() }?.let { profileData["role"] = it }

            firestore.collection("users").document(firebaseUser.uid)
                .set(profileData, SetOptions.merge())
                .await()

            if (isArtistEmail(normalizedEmail)) {
                firestore.collection("artists")
                    .whereEqualTo("email", normalizedEmail)
                    .get()
                    .await()
                    .documents
                    .forEach { doc ->
                        doc.reference.set(
                            mapOf(
                                "name" to normalizedName,
                                "email" to normalizedEmail
                            ),
                            SetOptions.merge()
                        ).await()
                    }
            }

            AuthResult.Success(Unit)
        } catch (e: Exception) {
            val errorMessage = when {
                e.message?.contains("permission denied", ignoreCase = true) == true ->
                    "Unable to save profile. Please check permissions."
                e.message?.contains("requires-recent-login", ignoreCase = true) == true ->
                    "Please log in again before changing your email."
                else -> e.message ?: "Could not update profile. Please try again."
            }
            AuthResult.Error(errorMessage)
        }
    }

    /**
     * Get current logged-in user
     */
    suspend fun getCurrentUser(): User? {
        val firebaseUser = firebaseAuth.currentUser ?: return null
        val email = firebaseUser.email ?: ""

        // We call the suspend function here.
        // This is now allowed because the parent function is 'suspend'.
        val role = if (email.isNotBlank() && isArtistEmail(email)) "artist" else "user"

        return User(
            id = firebaseUser.uid.hashCode(),
            name = firebaseUser.displayName ?: "User",
            email = email,
            role = role, // Use the variable defined above
            token = ""
        )
    }

    /**
     * Check if user is logged in
     */
    fun isLoggedIn(): Boolean = firebaseAuth.currentUser != null

    /**
     * Log out current user
     */
    fun logout() {
        firebaseAuth.signOut()
    }

    /**
     * Send password reset email
     */
    suspend fun sendPasswordResetEmail(email: String): AuthResult<Unit> {
        return try {
            if (email.isBlank()) {
                return AuthResult.Error("Email is required.")
            }

            firebaseAuth.sendPasswordResetEmail(email.trim()).await()

            AuthResult.Success(Unit)
        } catch (e: Exception) {
            val errorMessage = when {
                e.message?.contains("no user record") == true -> "No account found with this email."
                e.message?.contains("invalid email") == true -> "Invalid email format."
                e.message?.contains("too many requests") == true -> "Too many reset requests. Try again later."
                else -> e.message ?: "Failed to send reset email. Please try again."
            }
            AuthResult.Error(errorMessage)
        }
    }
}
