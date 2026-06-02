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
        sharedPreferences.edit().putString("auth_token", token).apply()
    }

    fun getToken(): String? {
        return sharedPreferences.getString("auth_token", null)
    }

    fun saveRole(role: String) {
        sharedPreferences.edit().putString("user_role", role).apply()
    }

    fun getRole(): String? {
        return sharedPreferences.getString("user_role", null)
    }

    fun clearSession() {
        sharedPreferences.edit()
            .remove("auth_token")
            .remove("user_role")
            .apply()
    }

    fun clear() {
        sharedPreferences.edit().clear().apply()
    }
}
