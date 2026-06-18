package com.repoflow.core.data.remote

import com.repoflow.core.data.remote.dto.GithubBranchDto
import com.repoflow.core.data.remote.dto.GithubCommitDto
import com.repoflow.core.data.remote.dto.GithubContributorDto
import com.repoflow.core.data.remote.dto.GithubReleaseDto
import com.repoflow.core.data.remote.dto.GithubRepoDto
import com.repoflow.core.data.remote.dto.GithubUserDto
import retrofit2.Response
import retrofit2.http.GET
import retrofit2.http.Path
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

    @GET("repos/{owner}/{repo}")
    suspend fun getRepository(
        @Path("owner") owner: String,
        @Path("repo") repo: String
    ): Response<GithubRepoDto>

    @GET("repos/{owner}/{repo}/branches")
    suspend fun getBranches(
        @Path("owner") owner: String,
        @Path("repo") repo: String,
        @Query("per_page") perPage: Int = 100
    ): Response<List<GithubBranchDto>>

    @GET("repos/{owner}/{repo}/commits")
    suspend fun getCommits(
        @Path("owner") owner: String,
        @Path("repo") repo: String,
        @Query("sha") sha: String? = null,
        @Query("per_page") perPage: Int = 30
    ): Response<List<GithubCommitDto>>

    @GET("repos/{owner}/{repo}/contributors")
    suspend fun getContributors(
        @Path("owner") owner: String,
        @Path("repo") repo: String,
        @Query("per_page") perPage: Int = 30
    ): Response<List<GithubContributorDto>>

    @GET("repos/{owner}/{repo}/releases")
    suspend fun getReleases(
        @Path("owner") owner: String,
        @Path("repo") repo: String,
        @Query("per_page") perPage: Int = 10
    ): Response<List<GithubReleaseDto>>
}

data class GitHubSearchResponse(
    val items: List<GithubRepoDto>
)
