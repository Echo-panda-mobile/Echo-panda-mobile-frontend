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

    fun saveEmail(email: String) {
        if (email.isNotBlank()) {
            sharedPreferences.edit().putString("user_email", email).apply()
            android.util.Log.d("TokenStorage", "Saved email: $email")
        }
    }

    fun getEmail(): String? {
        return sharedPreferences.getString("user_email", null)
    }

    fun saveName(name: String) {
        if (name.isNotBlank()) {
            sharedPreferences.edit().putString("user_name", name).apply()
            android.util.Log.d("TokenStorage", "Saved name: $name")
        }
    }

    fun getName(): String? {
        return sharedPreferences.getString("user_name", null)
    }

    fun saveUserId(userId: Int) {
        sharedPreferences.edit().putInt("user_id", userId).apply()
        android.util.Log.d("TokenStorage", "Saved user_id: $userId")
    }

    fun getUserId(): Int {
        return sharedPreferences.getInt("user_id", -1)
    }

    fun saveArtistId(artistId: Int) {
        sharedPreferences.edit().putInt("artist_id", artistId).apply()
        android.util.Log.d("TokenStorage", "Saved artist_id: $artistId")
    }

    fun getArtistId(): Int {
        return sharedPreferences.getInt("artist_id", -1)
    }

    fun clearSession() {
        sharedPreferences.edit()
            .remove("auth_token")
            .remove("user_role")
            .remove("user_email")
            .remove("user_name")
            .remove("user_id")
            .remove("artist_id")
            .commit()
        android.util.Log.d("TokenStorage", "✓ Cleared session")
    }

    fun clear() {
        sharedPreferences.edit().clear().commit()
        android.util.Log.d("TokenStorage", "✓ Cleared all stored data")
    }
}
