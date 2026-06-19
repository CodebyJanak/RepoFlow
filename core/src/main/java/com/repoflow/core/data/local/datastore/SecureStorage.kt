package com.repoflow.core.data.local.datastore

import android.content.Context
import android.content.SharedPreferences
import androidx.security.crypto.EncryptedSharedPreferences
import androidx.security.crypto.MasterKey
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class SecureStorage @Inject constructor(
    @ApplicationContext private val context: Context
) {
    private val prefs: SharedPreferences by lazy {
        val masterKey = MasterKey.Builder(context)
            .setKeyScheme(MasterKey.KeyScheme.AES256_GCM)
            .build()
        EncryptedSharedPreferences.create(
            context,
            "repoflow_secure_prefs",
            masterKey,
            EncryptedSharedPreferences.PrefKeyEncryptionScheme.AES256_SIV,
            EncryptedSharedPreferences.PrefValueEncryptionScheme.AES256_GCM
        )
    }

    private val _accessToken = MutableStateFlow<String?>(null)
    val accessToken: Flow<String?> = _accessToken.asStateFlow()

    init {
        _accessToken.value = prefs.getString(KEY_ACCESS_TOKEN, null)
    }

    fun getAccessTokenSync(): String? = prefs.getString(KEY_ACCESS_TOKEN, null)

    fun setAccessToken(token: String?) {
        prefs.edit().apply {
            if (token != null) putString(KEY_ACCESS_TOKEN, token)
            else remove(KEY_ACCESS_TOKEN)
            apply()
        }
        _accessToken.value = token
    }

    fun getRefreshToken(): String? = prefs.getString(KEY_REFRESH_TOKEN, null)

    fun setRefreshToken(token: String?) {
        prefs.edit().apply {
            if (token != null) putString(KEY_REFRESH_TOKEN, token)
            else remove(KEY_REFRESH_TOKEN)
            apply()
        }
    }

    fun getString(key: String, default: String? = null): String? =
        prefs.getString(key, default)

    fun setString(key: String, value: String?) {
        prefs.edit().apply {
            if (value != null) putString(key, value)
            else remove(key)
            apply()
        }
    }

    fun clear() {
        prefs.edit().clear().apply()
        _accessToken.value = null
    }

    companion object {
        private const val KEY_ACCESS_TOKEN = "github_access_token"
        private const val KEY_REFRESH_TOKEN = "github_refresh_token"
    }
}
