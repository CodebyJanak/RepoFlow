package com.repoflow.core.data.local.datastore

import android.content.Context
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.booleanPreferencesKey
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.stringPreferencesKey
import androidx.datastore.preferences.preferencesDataStore
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import javax.inject.Inject
import javax.inject.Singleton

private val Context.dataStore: DataStore<Preferences> by preferencesDataStore(name = "repoflow_preferences")

@Singleton
class DataStoreManager @Inject constructor(
    @ApplicationContext private val context: Context
) {
    private object Keys {
        val DARK_MODE = booleanPreferencesKey("dark_mode")
        val DYNAMIC_COLOR = booleanPreferencesKey("dynamic_color")
        val BIOMETRIC_ENABLED = booleanPreferencesKey("biometric_enabled")
        val SELECTED_THEME = stringPreferencesKey("selected_theme")
        val NOTIFICATIONS_ENABLED = booleanPreferencesKey("notifications_enabled")
        val ACCESS_TOKEN = stringPreferencesKey("access_token")
        val REFRESH_TOKEN = stringPreferencesKey("refresh_token")
    }

    val isDarkMode: Flow<Boolean> = context.dataStore.data.map { preferences ->
        preferences[Keys.DARK_MODE] ?: false
    }

    val isDynamicColorEnabled: Flow<Boolean> = context.dataStore.data.map { preferences ->
        preferences[Keys.DYNAMIC_COLOR] ?: true
    }

    val isBiometricEnabled: Flow<Boolean> = context.dataStore.data.map { preferences ->
        preferences[Keys.BIOMETRIC_ENABLED] ?: false
    }

    val selectedTheme: Flow<String> = context.dataStore.data.map { preferences ->
        preferences[Keys.SELECTED_THEME] ?: "system"
    }

    val isNotificationsEnabled: Flow<Boolean> = context.dataStore.data.map { preferences ->
        preferences[Keys.NOTIFICATIONS_ENABLED] ?: true
    }

    val accessToken: Flow<String?> = context.dataStore.data.map { preferences ->
        preferences[Keys.ACCESS_TOKEN]
    }

    suspend fun setDarkMode(enabled: Boolean) {
        context.dataStore.edit { preferences ->
            preferences[Keys.DARK_MODE] = enabled
        }
    }

    suspend fun setDynamicColorEnabled(enabled: Boolean) {
        context.dataStore.edit { preferences ->
            preferences[Keys.DYNAMIC_COLOR] = enabled
        }
    }

    suspend fun setBiometricEnabled(enabled: Boolean) {
        context.dataStore.edit { preferences ->
            preferences[Keys.BIOMETRIC_ENABLED] = enabled
        }
    }

    suspend fun setSelectedTheme(theme: String) {
        context.dataStore.edit { preferences ->
            preferences[Keys.SELECTED_THEME] = theme
        }
    }

    suspend fun setNotificationsEnabled(enabled: Boolean) {
        context.dataStore.edit { preferences ->
            preferences[Keys.NOTIFICATIONS_ENABLED] = enabled
        }
    }

    suspend fun setAccessToken(token: String?) {
        context.dataStore.edit { preferences ->
            if (token != null) {
                preferences[Keys.ACCESS_TOKEN] = token
            } else {
                preferences.remove(Keys.ACCESS_TOKEN)
            }
        }
    }

    suspend fun clearAll() {
        context.dataStore.edit { preferences ->
            preferences.clear()
        }
    }
}
