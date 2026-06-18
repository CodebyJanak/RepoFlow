package com.repoflow.core.data.remote

import com.repoflow.core.data.remote.dto.GithubRepoDto
import com.repoflow.core.data.remote.dto.GithubUserDto
import retrofit2.Response
import retrofit2.http.GET
import retrofit2.http.Header
import retrofit2.http.Query

interface ApiService {

    @GET("user")
    suspend fun getCurrentUser(): Response<GithubUserDto>

    @GET("user/repos")
    suspend fun getRepositories(
        @Query("sort") sort: String = "updated",
        @Query("direction") direction: String = "desc",
        @Query("per_page") perPage: Int = 100
    ): Response<List<GithubRepoDto>>

    @GET("search/repositories")
    suspend fun searchRepositories(
        @Query("q") query: String,
        @Query("sort") sort: String = "stars",
        @Query("per_page") perPage: Int = 50
    ): Response<GitHubSearchResponse>
}

data class GitHubSearchResponse(
    val items: List<GithubRepoDto>
)
