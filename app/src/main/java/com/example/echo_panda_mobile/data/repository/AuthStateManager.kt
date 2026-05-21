package com.example.echo_panda_mobile.data.repository

import android.content.Context
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.stringPreferencesKey
import androidx.datastore.preferences.preferencesDataStore
import com.example.echo_panda_mobile.data.model.AuthState
import com.example.echo_panda_mobile.data.model.User
import com.example.echo_panda_mobile.data.model.UserRole
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.map

private val Context.dataStore by preferencesDataStore(name = "auth_preferences")

object AuthPreferencesKeys {
    val USER_ID = stringPreferencesKey("user_id")
    val USER_NAME = stringPreferencesKey("user_name")
    val USER_EMAIL = stringPreferencesKey("user_email")
    val USER_ROLE = stringPreferencesKey("user_role")
    val USER_TOKEN = stringPreferencesKey("user_token")
    val IS_AUTHENTICATED = stringPreferencesKey("is_authenticated")
}

class AuthStateManager(private val context: Context) {
    private val _authState = MutableStateFlow(AuthState())
    val authState: StateFlow<AuthState> = _authState.asStateFlow()

    /**
     * Load authentication state from DataStore on app startup
     */
    suspend fun loadAuthState() {
        val preferences = context.dataStore.data.map { pref ->
            val isAuth = pref[AuthPreferencesKeys.IS_AUTHENTICATED]?.toBoolean() ?: false
            
            if (isAuth) {
                val userId = pref[AuthPreferencesKeys.USER_ID] ?: ""
                val name = pref[AuthPreferencesKeys.USER_NAME] ?: ""
                val email = pref[AuthPreferencesKeys.USER_EMAIL] ?: ""
                val roleStr = pref[AuthPreferencesKeys.USER_ROLE] ?: "USER"
                val token = pref[AuthPreferencesKeys.USER_TOKEN] ?: ""
                
                val role = try {
                    UserRole.valueOf(roleStr)
                } catch (e: Exception) {
                    UserRole.UNKNOWN
                }
                
                val user = User(
                    id = userId.hashCode(),
                    name = name,
                    email = email,
                    role = roleStr.lowercase(),
                    token = token
                )
                
                AuthState(
                    isAuthenticated = true,
                    user = user,
                    role = role,
                    token = token
                )
            } else {
                AuthState()
            }
        }
        
        preferences.collect { newState ->
            _authState.value = newState
        }
    }

    /**
     * Save authentication state to DataStore
     */
    suspend fun saveAuthState(user: User, role: UserRole, token: String) {
        context.dataStore.edit { preferences ->
            preferences[AuthPreferencesKeys.USER_ID] = user.id.toString()
            preferences[AuthPreferencesKeys.USER_NAME] = user.name
            preferences[AuthPreferencesKeys.USER_EMAIL] = user.email
            preferences[AuthPreferencesKeys.USER_ROLE] = role.name
            preferences[AuthPreferencesKeys.USER_TOKEN] = token
            preferences[AuthPreferencesKeys.IS_AUTHENTICATED] = "true"
        }
        
        _authState.value = AuthState(
            isAuthenticated = true,
            user = user,
            role = role,
            token = token
        )
    }

    /**
     * Clear authentication state (logout)
     */
    suspend fun clearAuthState() {
        context.dataStore.edit { preferences ->
            preferences.clear()
        }
        
        _authState.value = AuthState()
    }

    /**
     * Update loading state
     */
    fun setLoading(isLoading: Boolean) {
        _authState.value = _authState.value.copy(isLoading = isLoading)
    }

    /**
     * Update error state
     */
    fun setError(error: String?) {
        _authState.value = _authState.value.copy(error = error)
    }
}
