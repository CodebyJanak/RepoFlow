package com.repoflow.core.data.remote

import com.repoflow.core.data.remote.dto.CreateCommentRequest
import com.repoflow.core.data.remote.dto.CreateIssueRequest
import com.repoflow.core.data.remote.dto.CreatePullRequestCommentRequest
import com.repoflow.core.data.remote.dto.CreatePullRequestRequest
import com.repoflow.core.data.remote.dto.CreatePullRequestReviewRequest
import com.repoflow.core.data.remote.dto.EditIssueRequest
import com.repoflow.core.data.remote.dto.GithubBranchDto
import com.repoflow.core.data.remote.dto.GithubCommitDto
import com.repoflow.core.data.remote.dto.GithubContributorDto
import com.repoflow.core.data.remote.dto.GithubIssueCommentDto
import com.repoflow.core.data.remote.dto.GithubIssueDto
import com.repoflow.core.data.remote.dto.GithubArtifactDto
import com.repoflow.core.data.remote.dto.GithubArtifactListResponse
import com.repoflow.core.data.remote.dto.GithubPullRequestCommentDto
import com.repoflow.core.data.remote.dto.GithubPullRequestDto
import com.repoflow.core.data.remote.dto.GithubPullRequestReviewDto
import com.repoflow.core.data.remote.dto.GithubReleaseDto
import com.repoflow.core.data.remote.dto.GithubWorkflowDto
import com.repoflow.core.data.remote.dto.GithubWorkflowJobListResponse
import com.repoflow.core.data.remote.dto.GithubWorkflowRunDto
import com.repoflow.core.data.remote.dto.GithubWorkflowRunListResponse
import com.repoflow.core.data.remote.dto.GithubRepoDto
import com.repoflow.core.data.remote.dto.GithubUserDto
import com.repoflow.core.data.remote.dto.GithubPullRequestMergeDto
import com.repoflow.core.data.remote.dto.GithubWorkflowListResponse
import com.repoflow.core.data.remote.dto.MergePullRequestRequest
import com.repoflow.core.data.remote.dto.UpdatePullRequestRequest
import okhttp3.ResponseBody
import retrofit2.Response
import retrofit2.http.Streaming
import retrofit2.http.Body
import retrofit2.http.GET
import retrofit2.http.PATCH
import retrofit2.http.POST
import retrofit2.http.PUT
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

    @GET("repos/{owner}/{repo}/issues")
    suspend fun getIssues(
        @Path("owner") owner: String,
        @Path("repo") repo: String,
        @Query("state") state: String = "open",
        @Query("sort") sort: String = "created",
        @Query("direction") direction: String = "desc",
        @Query("per_page") perPage: Int = 30,
        @Query("page") page: Int = 1
    ): Response<List<GithubIssueDto>>

    @GET("repos/{owner}/{repo}/issues/{issue_number}")
    suspend fun getIssue(
        @Path("owner") owner: String,
        @Path("repo") repo: String,
        @Path("issue_number") issueNumber: Int
    ): Response<GithubIssueDto>

    @POST("repos/{owner}/{repo}/issues")
    suspend fun createIssue(
        @Path("owner") owner: String,
        @Path("repo") repo: String,
        @Body request: CreateIssueRequest
    ): Response<GithubIssueDto>

    @PATCH("repos/{owner}/{repo}/issues/{issue_number}")
    suspend fun editIssue(
        @Path("owner") owner: String,
        @Path("repo") repo: String,
        @Path("issue_number") issueNumber: Int,
        @Body request: EditIssueRequest
    ): Response<GithubIssueDto>

    @GET("repos/{owner}/{repo}/issues/{issue_number}/comments")
    suspend fun getIssueComments(
        @Path("owner") owner: String,
        @Path("repo") repo: String,
        @Path("issue_number") issueNumber: Int,
        @Query("per_page") perPage: Int = 30
    ): Response<List<GithubIssueCommentDto>>

    @POST("repos/{owner}/{repo}/issues/{issue_number}/comments")
    suspend fun createIssueComment(
        @Path("owner") owner: String,
        @Path("repo") repo: String,
        @Path("issue_number") issueNumber: Int,
        @Body request: CreateCommentRequest
    ): Response<GithubIssueCommentDto>

    @GET("repos/{owner}/{repo}/pulls")
    suspend fun getPullRequests(
        @Path("owner") owner: String,
        @Path("repo") repo: String,
        @Query("state") state: String = "open",
        @Query("sort") sort: String = "created",
        @Query("direction") direction: String = "desc",
        @Query("per_page") perPage: Int = 30,
        @Query("page") page: Int = 1
    ): Response<List<GithubPullRequestDto>>

    @GET("repos/{owner}/{repo}/pulls/{pull_number}")
    suspend fun getPullRequest(
        @Path("owner") owner: String,
        @Path("repo") repo: String,
        @Path("pull_number") pullNumber: Int
    ): Response<GithubPullRequestDto>

    @POST("repos/{owner}/{repo}/pulls")
    suspend fun createPullRequest(
        @Path("owner") owner: String,
        @Path("repo") repo: String,
        @Body request: CreatePullRequestRequest
    ): Response<GithubPullRequestDto>

    @PATCH("repos/{owner}/{repo}/pulls/{pull_number}")
    suspend fun updatePullRequest(
        @Path("owner") owner: String,
        @Path("repo") repo: String,
        @Path("pull_number") pullNumber: Int,
        @Body request: UpdatePullRequestRequest
    ): Response<GithubPullRequestDto>

    @GET("repos/{owner}/{repo}/pulls/{pull_number}/reviews")
    suspend fun getPullRequestReviews(
        @Path("owner") owner: String,
        @Path("repo") repo: String,
        @Path("pull_number") pullNumber: Int
    ): Response<List<GithubPullRequestReviewDto>>

    @POST("repos/{owner}/{repo}/pulls/{pull_number}/reviews")
    suspend fun createPullRequestReview(
        @Path("owner") owner: String,
        @Path("repo") repo: String,
        @Path("pull_number") pullNumber: Int,
        @Body request: CreatePullRequestReviewRequest
    ): Response<GithubPullRequestReviewDto>

    @GET("repos/{owner}/{repo}/pulls/{pull_number}/comments")
    suspend fun getPullRequestComments(
        @Path("owner") owner: String,
        @Path("repo") repo: String,
        @Path("pull_number") pullNumber: Int
    ): Response<List<GithubPullRequestCommentDto>>

    @POST("repos/{owner}/{repo}/pulls/{pull_number}/comments")
    suspend fun createPullRequestComment(
        @Path("owner") owner: String,
        @Path("repo") repo: String,
        @Path("pull_number") pullNumber: Int,
        @Body request: CreatePullRequestCommentRequest
    ): Response<GithubPullRequestCommentDto>

    @PUT("repos/{owner}/{repo}/pulls/{pull_number}/merge")
    suspend fun mergePullRequest(
        @Path("owner") owner: String,
        @Path("repo") repo: String,
        @Path("pull_number") pullNumber: Int,
        @Body request: MergePullRequestRequest
    ): Response<GithubPullRequestMergeDto>

    @GET("repos/{owner}/{repo}/actions/workflows")
    suspend fun getWorkflows(
        @Path("owner") owner: String,
        @Path("repo") repo: String
    ): Response<GithubWorkflowListResponse>

    @GET("repos/{owner}/{repo}/actions/workflows/{workflow_id}/runs")
    suspend fun getWorkflowRuns(
        @Path("owner") owner: String,
        @Path("repo") repo: String,
        @Path("workflow_id") workflowId: Long,
        @Query("per_page") perPage: Int = 20,
        @Query("page") page: Int = 1
    ): Response<GithubWorkflowRunListResponse>

    @GET("repos/{owner}/{repo}/actions/runs")
    suspend fun getRepositoryRuns(
        @Path("owner") owner: String,
        @Path("repo") repo: String,
        @Query("per_page") perPage: Int = 20,
        @Query("page") page: Int = 1
    ): Response<GithubWorkflowRunListResponse>

    @GET("repos/{owner}/{repo}/actions/runs/{run_id}")
    suspend fun getWorkflowRun(
        @Path("owner") owner: String,
        @Path("repo") repo: String,
        @Path("run_id") runId: Long
    ): Response<GithubWorkflowRunDto>

    @GET("repos/{owner}/{repo}/actions/runs/{run_id}/jobs")
    suspend fun getWorkflowRunJobs(
        @Path("owner") owner: String,
        @Path("repo") repo: String,
        @Path("run_id") runId: Long
    ): Response<GithubWorkflowJobListResponse>

    @Streaming
    @GET("repos/{owner}/{repo}/actions/runs/{run_id}/logs")
    suspend fun getWorkflowRunLogs(
        @Path("owner") owner: String,
        @Path("repo") repo: String,
        @Path("run_id") runId: Long
    ): Response<ResponseBody>

    @GET("repos/{owner}/{repo}/actions/artifacts")
    suspend fun getArtifacts(
        @Path("owner") owner: String,
        @Path("repo") repo: String,
        @Query("per_page") perPage: Int = 20
    ): Response<GithubArtifactListResponse>

    @GET("repos/{owner}/{repo}/actions/runs/{run_id}/artifacts")
    suspend fun getRunArtifacts(
        @Path("owner") owner: String,
        @Path("repo") repo: String,
        @Path("run_id") runId: Long
    ): Response<GithubArtifactListResponse>
}

data class GitHubSearchResponse(
    val items: List<GithubRepoDto>
)
