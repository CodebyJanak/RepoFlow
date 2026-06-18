package com.repoflow.core.data.repository

import com.repoflow.core.data.local.datastore.DataStoreManager
import com.repoflow.core.domain.repository.SettingsRepository
import kotlinx.coroutines.flow.Flow
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class SettingsRepositoryImpl @Inject constructor(
    private val dataStoreManager: DataStoreManager
) : SettingsRepository {

    override fun isDarkMode(): Flow<Boolean> = dataStoreManager.isDarkMode

    override suspend fun setDarkMode(enabled: Boolean) {
        dataStoreManager.setDarkMode(enabled)
    }

    override fun isDynamicColorEnabled(): Flow<Boolean> = dataStoreManager.isDynamicColorEnabled

    override suspend fun setDynamicColorEnabled(enabled: Boolean) {
        dataStoreManager.setDynamicColorEnabled(enabled)
    }

    override fun isBiometricEnabled(): Flow<Boolean> = dataStoreManager.isBiometricEnabled

    override suspend fun setBiometricEnabled(enabled: Boolean) {
        dataStoreManager.setBiometricEnabled(enabled)
    }

    override fun getSelectedTheme(): Flow<String> = dataStoreManager.selectedTheme

    override suspend fun setSelectedTheme(theme: String) {
        dataStoreManager.setSelectedTheme(theme)
    }

    override fun isNotificationEnabled(): Flow<Boolean> = dataStoreManager.isNotificationsEnabled

    override suspend fun setNotificationEnabled(enabled: Boolean) {
        dataStoreManager.setNotificationsEnabled(enabled)
    }
}
