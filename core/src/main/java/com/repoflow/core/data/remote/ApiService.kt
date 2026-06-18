package com.repoflow.core.data.remote

import com.repoflow.core.data.remote.dto.GithubUserDto
import retrofit2.Response
import retrofit2.http.GET

interface ApiService {

    @GET("user")
    suspend fun getCurrentUser(): Response<GithubUserDto>

    @GET("user/repos")
    suspend fun getRepositories(): Response<List<GithubUserDto>>
}
