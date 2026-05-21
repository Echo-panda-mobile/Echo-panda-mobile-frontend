package com.example.echo_panda_mobile.data.repository

import android.content.Context
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.booleanPreferencesKey
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.stringPreferencesKey
import androidx.datastore.preferences.preferencesDataStore
import com.example.echo_panda_mobile.data.model.AppSettings
import com.example.echo_panda_mobile.data.model.Languages
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.map

private val Context.settingsDataStore by preferencesDataStore(name = "app_settings")

class SettingsRepository(private val context: Context) {

    private object Keys {
        val IS_DARK_MODE = booleanPreferencesKey("is_dark_mode")
        val LANGUAGE = stringPreferencesKey("language")
        val NOTIFICATIONS_ENABLED = booleanPreferencesKey("notifications_enabled")
        val PRIVATE_ACCOUNT = booleanPreferencesKey("private_account")
        val EXPLICIT_CONTENT_ENABLED = booleanPreferencesKey("explicit_content_enabled")
    }

    val settingsFlow: Flow<AppSettings> = context.settingsDataStore.data.map { preferences ->
        preferences.toAppSettings()
    }

    suspend fun setDarkMode(isDark: Boolean) {
        context.settingsDataStore.edit { preferences ->
            preferences[Keys.IS_DARK_MODE] = isDark
        }
    }

    suspend fun setLanguage(language: String) {
        context.settingsDataStore.edit { preferences ->
            preferences[Keys.LANGUAGE] = language
        }
    }

    suspend fun saveSettings(settings: AppSettings) {
        context.settingsDataStore.edit { preferences ->
            preferences[Keys.IS_DARK_MODE] = settings.isDarkMode
            preferences[Keys.LANGUAGE] = settings.language
            preferences[Keys.NOTIFICATIONS_ENABLED] = settings.notificationsEnabled
            preferences[Keys.PRIVATE_ACCOUNT] = settings.privateAccount
            preferences[Keys.EXPLICIT_CONTENT_ENABLED] = settings.explicitContentEnabled
        }
    }

    suspend fun updateSettings(settings: AppSettings) {
        saveSettings(settings)
    }

    suspend fun getSettings(): AppSettings {
        return context.settingsDataStore.data.first().toAppSettings()
    }

    suspend fun resetToDefaults() {
        context.settingsDataStore.edit { preferences ->
            preferences.clear()
        }
    }

    private fun Preferences.toAppSettings(): AppSettings {
        return AppSettings(
            isDarkMode = this[Keys.IS_DARK_MODE] ?: true,
            language = this[Keys.LANGUAGE] ?: Languages.ENGLISH,
            notificationsEnabled = this[Keys.NOTIFICATIONS_ENABLED] ?: true,
            privateAccount = this[Keys.PRIVATE_ACCOUNT] ?: false,
            explicitContentEnabled = this[Keys.EXPLICIT_CONTENT_ENABLED] ?: true
        )
    }
}
