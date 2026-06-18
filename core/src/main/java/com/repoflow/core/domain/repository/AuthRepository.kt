package com.repoflow.core.domain.repository

import com.repoflow.core.domain.model.User
import kotlinx.coroutines.flow.Flow

interface AuthRepository {
    suspend fun loginWithOAuth(): Result<User>
    suspend fun loginWithToken(token: String): Result<User>
    suspend fun loginWithDeviceFlow(): Result<User>
    suspend fun logout()
    suspend fun getCurrentUser(): Result<User?>
    fun isLoggedIn(): Flow<Boolean>
    fun getAccessToken(): Flow<String?>
}
