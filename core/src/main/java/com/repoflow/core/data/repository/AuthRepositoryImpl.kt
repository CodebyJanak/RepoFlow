package com.repoflow.core.data.repository

import com.repoflow.core.data.local.datastore.SecureStorage
import com.repoflow.core.data.remote.ApiService
import com.repoflow.core.data.mapper.RepositoryMapper.toDomain
import com.repoflow.core.domain.model.User
import com.repoflow.core.domain.repository.AuthRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class AuthRepositoryImpl @Inject constructor(
    private val apiService: ApiService,
    private val secureStorage: SecureStorage
) : AuthRepository {

    override suspend fun loginWithOAuth(): Result<User> {
        return try {
            val token = performOAuthFlow()
                ?: return Result.failure(Exception("OAuth flow cancelled"))
            return loginWithToken(token)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    override suspend fun loginWithToken(token: String): Result<User> {
        return try {
            secureStorage.setAccessToken(token)
            val response = apiService.getCurrentUser()
            if (response.isSuccessful) {
                val user = response.body()?.toDomain()
                    ?: return Result.failure(Exception("Empty user response"))
                secureStorage.setString(KEY_USER_LOGIN, user.login)
                secureStorage.setString(KEY_USER_NAME, user.name)
                secureStorage.setString(KEY_USER_AVATAR, user.avatarUrl)
                Result.success(user)
            } else {
                secureStorage.setAccessToken(null)
                val errorBody = response.errorBody()?.string()
                Result.failure(Exception(errorBody ?: "Login failed: ${response.code()}"))
            }
        } catch (e: Exception) {
            secureStorage.setAccessToken(null)
            Result.failure(e)
        }
    }

    override suspend fun loginWithDeviceFlow(): Result<User> {
        return try {
            val deviceCode = requestDeviceCode()
            val token = pollForDeviceFlowToken(deviceCode)
                ?: return Result.failure(Exception("Device flow cancelled"))
            return loginWithToken(token)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    override suspend fun logout() {
        secureStorage.clear()
    }

    override suspend fun getCurrentUser(): Result<User?> {
        val token = secureStorage.getAccessTokenSync()
        if (token == null) {
            return Result.success(null)
        }
        return try {
            val response = apiService.getCurrentUser()
            if (response.isSuccessful) {
                Result.success(response.body()?.toDomain())
            } else if (response.code() == 401) {
                secureStorage.setAccessToken(null)
                Result.success(null)
            } else {
                Result.failure(Exception("Failed to get user: ${response.code()}"))
            }
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    override fun isLoggedIn(): Flow<Boolean> {
        return secureStorage.accessToken.map { it != null }
    }

    override fun getAccessToken(): Flow<String?> {
        return secureStorage.accessToken
    }

    suspend fun getCachedUser(): User? {
        val login = secureStorage.getString(KEY_USER_LOGIN) ?: return null
        return User(
            id = 0,
            login = login,
            avatarUrl = secureStorage.getString(KEY_USER_AVATAR) ?: "",
            name = secureStorage.getString(KEY_USER_NAME),
            email = null,
            bio = null,
            publicRepos = 0
        )
    }

    private suspend fun performOAuthFlow(): String? {
        return null
    }

    private suspend fun requestDeviceCode(): String? {
        return null
    }

    private suspend fun pollForDeviceFlowToken(deviceCode: String?): String? {
        return null
    }

    companion object {
        private const val KEY_USER_LOGIN = "cached_user_login"
        private const val KEY_USER_NAME = "cached_user_name"
        private const val KEY_USER_AVATAR = "cached_user_avatar"
    }
}
