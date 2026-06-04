package com.example.echo_panda_mobile.data.repository

import com.example.echo_panda_mobile.data.model.User
import com.google.firebase.auth.FirebaseUser

/**
 * In-memory + encrypted-prefs cache for the signed-in user.
 * Avoids hitting /firebase/session on every screen or tab switch.
 */
object UserSessionCache {

    private const val TTL_MS = 5 * 60 * 1000L

    @Volatile
    private var memoryUser: User? = null

    @Volatile
    private var memoryCachedAt: Long = 0L

    fun invalidate() {
        memoryUser = null
        memoryCachedAt = 0L
    }

    fun put(user: User) {
        memoryUser = user
        memoryCachedAt = System.currentTimeMillis()
    }

    fun getIfFresh(nowMs: Long = System.currentTimeMillis()): User? {
        val user = memoryUser ?: return null
        if (nowMs - memoryCachedAt > TTL_MS) return null
        return user
    }

    fun getRole(tokenStorage: TokenStorage): String? =
        getIfFresh()?.role ?: tokenStorage.getRole()

    fun fromStorage(tokenStorage: TokenStorage, firebaseUser: FirebaseUser?): User? {
        val role = tokenStorage.getRole() ?: return null
        val email = tokenStorage.getEmail() ?: firebaseUser?.email ?: return null
        val name = tokenStorage.getName()
            ?: firebaseUser?.displayName
            ?: email.substringBefore("@")
        val id = tokenStorage.getUserId() ?: 0
        val token = tokenStorage.getToken().orEmpty()
        return User(
            id = id,
            name = name,
            email = email,
            role = role,
            token = token,
            photoUrl = tokenStorage.getPhotoUrl() ?: firebaseUser?.photoUrl?.toString()
        )
    }

    fun persist(tokenStorage: TokenStorage, user: User) {
        tokenStorage.saveUserProfile(
            userId = user.id,
            name = user.name,
            email = user.email,
            role = user.role,
            photoUrl = user.photoUrl
        )
        put(user)
    }
}
