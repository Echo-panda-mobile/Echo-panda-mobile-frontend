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
    private val firestore    = FirebaseFirestore.getInstance()

    // ─────────────────────────────────────────────────────────────────────────
    // LOGIN — single entry point, role-aware
    // Flow:
    //   1. Check "admins" collection  → plain-text password match (Firestore-only)
    //   2. Check "artists" collection → plain-text password match (Firestore-only)
    //   3. Fall through to Firebase Auth (regular users + Google accounts)
    // ─────────────────────────────────────────────────────────────────────────
    suspend fun login(request: LoginRequest): AuthResult<AuthResponse> {
        val email    = request.email.trim()
        val password = request.password.trim()

        if (email.isBlank() || password.isBlank()) {
            return AuthResult.Error("Email and password are required.")
        }

        // 1. Admin check — Firestore "admins" collection, plain-text password
        val adminDoc = findDocumentByEmail("admins", email)
        if (adminDoc != null) {
            return loginFromFirestoreDoc(
                document = adminDoc,
                email    = email,
                password = password,
                role     = "admin",
                errorPrefix = "Admin"
            )
        }

        // 2. Artist check — Firestore "artists" collection, plain-text password
        val artistDoc = findDocumentByEmail("artists", email)
        if (artistDoc != null) {
            return loginFromFirestoreDoc(
                document = artistDoc,
                email    = email,
                password = password,
                role     = "artist",
                errorPrefix = "Artist"
            )
        }

        // 3. Regular user — Firebase Auth + Firestore "users" collection
        return loginFirebaseUser(email, password)
    }

    // ─────────────────────────────────────────────────────────────────────────
    // Shared Firestore-only login (admin + artist)
    // Both collections store a plain-text "password" field
    // ─────────────────────────────────────────────────────────────────────────
    private fun loginFromFirestoreDoc(
        document    : DocumentSnapshot,
        email       : String,
        password    : String,
        role        : String,
        errorPrefix : String
    ): AuthResult<AuthResponse> {
        val storedPassword = document.getString("password")?.trim().orEmpty()

        if (storedPassword.isBlank()) {
            return AuthResult.Error("$errorPrefix account has no password configured.")
        }
        if (storedPassword != password) {
            return AuthResult.Error("Incorrect password.")
        }

        val name  = document.getString("name")?.takeIf { it.isNotBlank() }
            ?: email.substringBefore("@")
        val token = "$role-firestore-token"

        return AuthResult.Success(
            AuthResponse(
                user = User(
                    id    = document.id.hashCode(),
                    name  = name,
                    email = email,
                    role  = role,
                    token = token
                ),
                token   = token,
                message = "$errorPrefix login successful."
            )
        )
    }

    // ─────────────────────────────────────────────────────────────────────────
    // Firebase Auth login — regular users only
    // login() already filtered out admins and artists before calling this,
    // but we add a final guard here so a misconfigured account can never
    // accidentally create a duplicate doc in the users collection.
    // ─────────────────────────────────────────────────────────────────────────
    private suspend fun loginFirebaseUser(
        email    : String,
        password : String
    ): AuthResult<AuthResponse> {
        return try {
            val result       = firebaseAuth.signInWithEmailAndPassword(email, password).await()
            val firebaseUser = result.user
                ?: return AuthResult.Error("Login failed. User data not available.")

            val token       = firebaseUser.getIdToken(false).await().token ?: "firebase-token"
            val userDoc     = firestore.collection("users").document(firebaseUser.uid).get().await()

            val name        = userDoc.getString("name")  ?: firebaseUser.displayName ?: "User"
            val storedEmail = userDoc.getString("email") ?: firebaseUser.email ?: email

            // Role comes exclusively from the existing Firestore doc.
            // Default is "user" — never "admin" or "artist" via this path.
            val role = userDoc.getString("role")?.lowercase() ?: "user"

            // Final guard: if this email belongs to admins/artists, do NOT
            // create a users doc — just return the role from the correct collection.
            if (!userDoc.exists()) {
                val isPrivileged = findDocumentByEmail("admins", email) != null
                        || findDocumentByEmail("artists", email) != null
                if (!isPrivileged) {
                    saveUserProfile(firebaseUser.uid, name, storedEmail, role)
                }
            }

            AuthResult.Success(
                AuthResponse(
                    user = User(
                        id    = firebaseUser.uid.hashCode(),
                        name  = name,
                        email = storedEmail,
                        role  = role,
                        token = token
                    ),
                    token   = token,
                    message = "Login successful."
                )
            )
        } catch (e: Exception) {
            AuthResult.Error(mapFirebaseAuthError(e))
        }
    }

    // ─────────────────────────────────────────────────────────────────────────
    // Google Sign-In
    // MUST check admins → artists BEFORE touching the users collection,
    // otherwise a Google-linked admin/artist gets a duplicate "user" doc.
    // ─────────────────────────────────────────────────────────────────────────
    suspend fun signInWithGoogle(idToken: String): AuthResult<AuthResponse> {
        return try {
            if (idToken.isBlank()) return AuthResult.Error("Google sign-in token is required.")

            val credential   = GoogleAuthProvider.getCredential(idToken, null)
            val result       = firebaseAuth.signInWithCredential(credential).await()
            val firebaseUser = result.user
                ?: return AuthResult.Error("Google sign-in failed. User data not available.")

<<<<<<< HEAD
            val token = firebaseUser.getIdToken(false).await().token ?: "firebase-token"
            val email = firebaseUser.email ?: ""

            // ── Guard: check privileged collections first ─────────────────────
            // If this Google email belongs to an admin or artist, return their
            // Firestore role and NEVER write anything to the users collection.
            if (email.isNotBlank()) {
                val adminDoc = findDocumentByEmail("admins", email)
                if (adminDoc != null) {
                    val name = adminDoc.getString("name") ?: firebaseUser.displayName ?: "Admin"
                    return AuthResult.Success(
                        AuthResponse(
                            user = User(
                                id    = adminDoc.id.hashCode(),
                                name  = name,
                                email = email,
                                role  = "admin",
                                token = token
                            ),
                            token   = token,
                            message = "Admin Google sign-in successful."
                        )
                    )
                }

                val artistDoc = findDocumentByEmail("artists", email)
                if (artistDoc != null) {
                    val name = artistDoc.getString("name") ?: firebaseUser.displayName ?: "Artist"
                    return AuthResult.Success(
                        AuthResponse(
                            user = User(
                                id    = artistDoc.id.hashCode(),
                                name  = name,
                                email = email,
                                role  = "artist",
                                token = token
                            ),
                            token   = token,
                            message = "Artist Google sign-in successful."
                        )
                    )
                }
            }

            // ── Regular user — safe to read/write users collection ────────────
            val profileDoc = firestore.collection("users").document(firebaseUser.uid).get().await()
            val name       = profileDoc.getString("name") ?: firebaseUser.displayName ?: "User"
            val role       = profileDoc.getString("role")?.lowercase() ?: "user"
=======
            val token      = firebaseUser.getIdToken(false).await().token ?: "firebase-token"
            val email      = firebaseUser.email ?: ""

            // ── Role Enforcement: Google is for "user" role only ──
            // Prevent conflicts if this email is already an Admin or Artist
            val adminDoc = findDocumentByEmail("admins", email)
            if (adminDoc != null) {
                return AuthResult.Error("This email is registered as an Admin. Please use email/password login.")
            }

            val artistDoc = findDocumentByEmail("artists", email)
            if (artistDoc != null) {
                return AuthResult.Error("This email is registered as an Artist. Please use email/password login.")
            }

            // Normal user flow
            val profileDoc = firestore.collection("users").document(firebaseUser.uid).get().await()
            val name = profileDoc.getString("name") ?: firebaseUser.displayName ?: "User"
            
            // Force role to "user" for all Google accounts
            val role = "user"
>>>>>>> d33b944 (Initial commit)

            if (!profileDoc.exists()) {
                saveGoogleUserProfile(firebaseUser.uid, name, email, role)
            } else if (profileDoc.getString("role") != "user") {
                // Update existing user doc if it somehow has a different role (optional safety)
                firestore.collection("users").document(firebaseUser.uid)
                    .update("role", "user").await()
            }

            AuthResult.Success(
                AuthResponse(
                    user = User(
                        id    = firebaseUser.uid.hashCode(),
                        name  = name,
                        email = email,
                        role  = role,
                        token = token
                    ),
                    token   = token,
                    message = "Google sign-in successful."
                )
            )
        } catch (e: Exception) {
            AuthResult.Error(e.message ?: "Google sign-in failed. Please try again.")
        }
    }

    // ─────────────────────────────────────────────────────────────────────────
    // Register — email/password users only, never creates admin/artist
    // ─────────────────────────────────────────────────────────────────────────
    suspend fun register(request: RegisterRequest): AuthResult<AuthResponse> {
        return try {
            when {
                request.name.isBlank()                           -> return AuthResult.Error("Name is required.")
                request.email.isBlank()                          -> return AuthResult.Error("Email is required.")
                request.password.length < 8                      -> return AuthResult.Error("Password must be at least 8 characters.")
                request.password != request.passwordConfirmation -> return AuthResult.Error("Passwords do not match.")
            }

            if (request.role.equals("artist", ignoreCase = true) ||
                request.role.equals("admin",  ignoreCase = true)) {
                return AuthResult.Error(
                    "This role cannot be self-registered. Please contact support."
                )
            }

            val result       = firebaseAuth.createUserWithEmailAndPassword(
                request.email.trim(), request.password
            ).await()
            val firebaseUser = result.user
                ?: return AuthResult.Error("Sign-up failed. User creation unsuccessful.")

            val profileUpdates = com.google.firebase.auth.UserProfileChangeRequest.Builder()
                .setDisplayName(request.name)
                .build()
            firebaseUser.updateProfile(profileUpdates).await()

            saveUserProfile(firebaseUser.uid, request.name, request.email.trim(), "user")

            val token = firebaseUser.getIdToken(false).await().token ?: "firebase-token"

            firebaseUser.sendEmailVerification().await()

            AuthResult.Success(
                AuthResponse(
                    user = User(
                        id    = firebaseUser.uid.hashCode(),
                        name  = request.name,
                        email = request.email,
                        role  = "user",
                        token = token
                    ),
                    token   = token,
                    message = "Account created successfully. Please verify your email."
                )
            )
        } catch (e: Exception) {
            val msg = when {
                e.message?.contains("already in use")       == true -> "Email is already registered. Please log in."
                e.message?.contains("invalid email")        == true -> "Invalid email format."
                e.message?.contains("password is too weak") == true -> "Password must be at least 8 characters."
                else -> e.message ?: "Sign-up failed. Please try again."
            }
            AuthResult.Error(msg)
        }
    }

    // ─────────────────────────────────────────────────────────────────────────
    // getCurrentUserProfile — used by AppNavigation on cold launch
    // Checks all three collections in order so cold-launch role is always correct
    // ─────────────────────────────────────────────────────────────────────────
    suspend fun getCurrentUserProfile(): User? {
        val firebaseUser = firebaseAuth.currentUser
        val email        = firebaseUser?.email ?: ""

        // Firestore-only accounts (admin / artist) don't have a Firebase UID in users collection.
        // Check those collections first so cold-launch navigation is correct.
        if (email.isNotBlank()) {
            val adminDoc = findDocumentByEmail("admins", email)
            if (adminDoc != null) {
                return User(
                    id    = adminDoc.id.hashCode(),
                    name  = adminDoc.getString("name") ?: "Admin",
                    email = email,
                    role  = "admin",
                    token = ""
                )
            }

            val artistDoc = findDocumentByEmail("artists", email)
            if (artistDoc != null) {
                return User(
                    id    = artistDoc.id.hashCode(),
                    name  = artistDoc.getString("name") ?: "Artist",
                    email = email,
                    role  = "artist",
                    token = ""
                )
            }
        }

        // Regular Firebase user
        firebaseUser ?: return null
        val userDoc = firestore.collection("users").document(firebaseUser.uid).get().await()
        val name    = userDoc.getString("name")  ?: firebaseUser.displayName ?: "User"
        val role    = userDoc.getString("role")?.lowercase() ?: "user"

        return User(
            id    = firebaseUser.uid.hashCode(),
            name  = name,
            email = email,
            role  = role,
            token = ""
        )
    }

    // ─────────────────────────────────────────────────────────────────────────
    // Helpers
    // ─────────────────────────────────────────────────────────────────────────

    /**
     * Generic email lookup across any Firestore collection.
     *
     * CRITICAL: This runs BEFORE the user is authenticated with Firebase Auth.
     * Firestore Security Rules MUST allow unauthenticated reads on "admins"
     * and "artists", otherwise this silently returns null and the user falls
     * through to loginFirebaseUser() which creates a duplicate users doc
     * with role:"user".
     *
     * Required Firestore rules for admins + artists:
     *   allow read: if true;
     *   allow write: if false;
     */
    private suspend fun findDocumentByEmail(
        collection : String,
        email      : String
    ): DocumentSnapshot? {
        val normalizedEmail = email.trim().lowercase()
        return try {
            val exactMatch = firestore.collection(collection)
                .whereEqualTo("email", email.trim())
                .limit(1)
                .get()
                .await()
                .documents
                .firstOrNull()

            exactMatch ?: firestore.collection(collection)
                .get()
                .await()
                .documents
                .firstOrNull { it.getString("email")?.trim()?.lowercase() == normalizedEmail }
        } catch (e: Exception) {
            // If logcat shows PERMISSION_DENIED here — your Firestore rules are blocking
            // the email lookup before auth. Fix: allow read: if true on admins + artists.
            android.util.Log.e(
                "AuthRepository",
                "findDocumentByEmail($collection) failed — " +
                        "likely PERMISSION_DENIED before auth. Error: ${e.message}"
            )
            null
        }
    }

    private suspend fun saveUserProfile(
        userId : String,
        name   : String,
        email  : String,
        role   : String
    ) {
        firestore.collection("users").document(userId).set(
            mapOf(
                "name"      to name,
                "email"     to email,
                "role"      to role,
                "provider"  to "password",
                "createdAt" to System.currentTimeMillis()
            )
        ).await()
    }

    private suspend fun saveGoogleUserProfile(
        userId : String,
        name   : String,
        email  : String,
        role   : String
    ) {
        firestore.collection("users").document(userId).set(
            mapOf(
                "name"         to name,
                "email"        to email,
                "role"         to role,
                "provider"     to "google",
                "registeredAt" to System.currentTimeMillis(),
                "lastLogin"    to System.currentTimeMillis(),
                "status"       to "active"
            )
        ).await()
    }

    private fun mapFirebaseAuthError(e: Exception): String = when {
        e is FirebaseAuthInvalidCredentialsException &&
                e.errorCode == "ERROR_INVALID_EMAIL"        -> "Invalid email format."
        e is FirebaseAuthInvalidCredentialsException &&
                e.errorCode == "ERROR_WRONG_PASSWORD"       -> "Incorrect password."
        e is FirebaseAuthInvalidCredentialsException &&
                e.errorCode == "ERROR_INVALID_CUSTOM_TOKEN" -> "Invalid or expired credentials."
        e is FirebaseAuthInvalidUserException               -> "User not found. Please sign up."
        e.message?.contains("no user record",        ignoreCase = true) == true -> "User not found. Please sign up."
        e.message?.contains("password is invalid",   ignoreCase = true) == true -> "Incorrect password."
        e.message?.contains("user has been disabled",ignoreCase = true) == true -> "This account has been disabled."
        e.message?.contains("too many unsuccessful", ignoreCase = true) == true -> "Too many login attempts. Try again later."
        else -> e.message ?: "Login failed. Please try again."
    }

    // ─────────────────────────────────────────────────────────────────────────
    // Remaining methods — unchanged
    // ─────────────────────────────────────────────────────────────────────────

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
        name  : String,
        email : String,
        role  : String? = null
    ): AuthResult<Unit> {
        val firebaseUser = firebaseAuth.currentUser
            ?: return AuthResult.Error("User not signed in.")
        return try {
            val normalizedName  = name.trim()
            val normalizedEmail = email.trim().ifBlank { firebaseUser.email.orEmpty() }

            val profileUpdates = com.google.firebase.auth.UserProfileChangeRequest.Builder()
                .setDisplayName(normalizedName)
                .build()
            firebaseUser.updateProfile(profileUpdates).await()

            if (normalizedEmail.isNotBlank() && normalizedEmail != firebaseUser.email) {
                firebaseUser.updateEmail(normalizedEmail).await()
            }

            val data = mutableMapOf<String, Any>("name" to normalizedName)
            if (normalizedEmail.isNotBlank()) data["email"] = normalizedEmail
            role?.takeIf { it.isNotBlank() }?.let { data["role"] = it }

            firestore.collection("users").document(firebaseUser.uid)
                .set(data, SetOptions.merge()).await()

            AuthResult.Success(Unit)
        } catch (e: Exception) {
            val msg = when {
                e.message?.contains("permission denied",      ignoreCase = true) == true ->
                    "Unable to save profile. Please check permissions."
                e.message?.contains("requires-recent-login", ignoreCase = true) == true ->
                    "Please log in again before changing your email."
                else -> e.message ?: "Could not update profile. Please try again."
            }
            AuthResult.Error(msg)
        }
    }

    suspend fun getCurrentUser(): User? {
        val firebaseUser = firebaseAuth.currentUser ?: return null
        val email        = firebaseUser.email ?: ""
        val doc          = firestore.collection("users").document(firebaseUser.uid).get().await()
        val name         = doc.getString("name") ?: firebaseUser.displayName ?: "User"
        val role         = doc.getString("role")?.lowercase() ?: "user"
        return User(
            id    = firebaseUser.uid.hashCode(),
            name  = name,
            email = email,
            role  = role,
            token = ""
        )
    }

    fun isLoggedIn(): Boolean = firebaseAuth.currentUser != null

    fun logout() = firebaseAuth.signOut()

    suspend fun sendPasswordResetEmail(email: String): AuthResult<Unit> {
        return try {
            if (email.isBlank()) return AuthResult.Error("Email is required.")
            firebaseAuth.sendPasswordResetEmail(email.trim()).await()
            AuthResult.Success(Unit)
        } catch (e: Exception) {
            val msg = when {
                e.message?.contains("no user record")    == true -> "No account found with this email."
                e.message?.contains("invalid email")     == true -> "Invalid email format."
                e.message?.contains("too many requests") == true -> "Too many reset requests. Try again later."
                else -> e.message ?: "Failed to send reset email. Please try again."
            }
            AuthResult.Error(msg)
        }
    }
}