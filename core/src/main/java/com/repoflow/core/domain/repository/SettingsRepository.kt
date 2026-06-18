package com.repoflow.core.domain.repository

import kotlinx.coroutines.flow.Flow

interface SettingsRepository {
    fun isDarkMode(): Flow<Boolean>
    suspend fun setDarkMode(enabled: Boolean)
    fun isDynamicColorEnabled(): Flow<Boolean>
    suspend fun setDynamicColorEnabled(enabled: Boolean)
    fun isBiometricEnabled(): Flow<Boolean>
    suspend fun setBiometricEnabled(enabled: Boolean)
    fun getSelectedTheme(): Flow<String>
    suspend fun setSelectedTheme(theme: String)
    fun isNotificationEnabled(): Flow<Boolean>
    suspend fun setNotificationEnabled(enabled: Boolean)
}
