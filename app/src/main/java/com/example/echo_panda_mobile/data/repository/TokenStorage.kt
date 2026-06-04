package com.example.echo_panda_mobile.data.repository

import android.content.Context
import androidx.security.crypto.EncryptedSharedPreferences
import androidx.security.crypto.MasterKey

class TokenStorage(context: Context) {
    companion object {
        @Volatile
        private var instance: TokenStorage? = null

        fun getInstance(context: Context): TokenStorage {
            return instance ?: synchronized(this) {
                instance ?: TokenStorage(context.applicationContext).also { instance = it }
            }
        }
    }

    private val masterKey = MasterKey.Builder(context)
        .setKeyScheme(MasterKey.KeyScheme.AES256_GCM)
        .build()

    private val sharedPreferences = EncryptedSharedPreferences.create(
        context,
        "secure_prefs",
        masterKey,
        EncryptedSharedPreferences.PrefKeyEncryptionScheme.AES256_SIV,
        EncryptedSharedPreferences.PrefValueEncryptionScheme.AES256_GCM
    )

    fun saveToken(token: String) {
        if (token.isBlank()) {
            android.util.Log.e("TokenStorage", "⚠️ Attempted to save EMPTY token! Ignoring.")
            return
        }
        android.util.Log.d("TokenStorage", "━━━ SAVING TOKEN ━━━")
        android.util.Log.d("TokenStorage", "Token length: ${token.length}")
        sharedPreferences.edit().putString("auth_token", token).commit()
        android.util.Log.d("TokenStorage", "✓ SAVED_TOKEN: Committed to secure storage")
    }

    fun getToken(): String? {
        return sharedPreferences.getString("auth_token", null)
    }

    fun saveRole(role: String) {
        if (role.isNotBlank()) {
            sharedPreferences.edit().putString("user_role", role).commit()
            android.util.Log.d("TokenStorage", "Saved role: $role")
        }
    }

    fun getRole(): String? {
        return sharedPreferences.getString("user_role", null)
    }

    fun saveUserProfile(
        userId: Int,
        name: String,
        email: String,
        role: String,
        photoUrl: String? = null
    ) {
        sharedPreferences.edit()
            .putInt("user_id", userId)
            .putString("user_name", name)
            .putString("user_email", email)
            .putString("user_role", role)
            .apply {
                if (photoUrl != null) putString("user_photo_url", photoUrl)
                else remove("user_photo_url")
            }
    }

    fun getUserId(): Int? {
        val id = sharedPreferences.getInt("user_id", -1)
        return id.takeIf { it >= 0 }
    }

    fun getName(): String? = sharedPreferences.getString("user_name", null)

    fun getEmail(): String? = sharedPreferences.getString("user_email", null)

    fun getPhotoUrl(): String? = sharedPreferences.getString("user_photo_url", null)

    fun clearSession() {
        sharedPreferences.edit()
            .remove("auth_token")
            .remove("user_role")
            .remove("user_id")
            .remove("user_name")
            .remove("user_email")
            .remove("user_photo_url")
            .apply()
        UserSessionCache.invalidate()
    }

    fun clear() {
        sharedPreferences.edit().clear().commit()
        android.util.Log.d("TokenStorage", "✓ Cleared all stored data")
    }
}
