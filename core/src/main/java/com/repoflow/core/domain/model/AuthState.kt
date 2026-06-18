package com.repoflow.core.domain.model

sealed interface AuthState {
    data object Loading : AuthState
    data object Unauthenticated : AuthState
    data class Authenticated(val user: User) : AuthState
    data class Error(val message: String) : AuthState
}

data class LoginState(
    val isLoading: Boolean = false,
    val error: String? = null,
    val isOAuthInProgress: Boolean = false,
    val deviceFlowCode: String? = null,
    val deviceFlowVerificationUri: String? = null
)

data class AccountState(
    val user: User? = null,
    val isLoading: Boolean = false,
    val error: String? = null,
    val isLoggingOut: Boolean = false
)
