package com.repoflow.core.data.repository

import com.repoflow.core.data.local.datastore.DataStoreManager
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
    private val dataStoreManager: DataStoreManager
) : AuthRepository {

    override suspend fun loginWithOAuth(): Result<User> {
        return Result.failure(NotImplementedError("OAuth login not yet implemented"))
    }

    override suspend fun loginWithToken(token: String): Result<User> {
        return try {
            dataStoreManager.setAccessToken(token)
            val response = apiService.getCurrentUser()
            if (response.isSuccessful) {
                val user = response.body()?.toDomain()
                    ?: return Result.failure(Exception("Empty response"))
                Result.success(user)
            } else {
                Result.failure(Exception("Login failed: ${response.code()}"))
            }
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    override suspend fun loginWithDeviceFlow(): Result<User> {
        return Result.failure(NotImplementedError("Device flow login not yet implemented"))
    }

    override suspend fun logout() {
        dataStoreManager.setAccessToken(null)
    }

    override suspend fun getCurrentUser(): Result<User?> {
        return try {
            val response = apiService.getCurrentUser()
            if (response.isSuccessful) {
                Result.success(response.body()?.toDomain())
            } else {
                Result.failure(Exception("Failed to get user: ${response.code()}"))
            }
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    override fun isLoggedIn(): Flow<Boolean> {
        return dataStoreManager.accessToken.map { it != null }
    }

    override fun getAccessToken(): Flow<String?> {
        return dataStoreManager.accessToken
    }
}
