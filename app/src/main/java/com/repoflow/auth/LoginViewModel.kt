package com.repoflow.auth

import androidx.lifecycle.viewModelScope
import com.repoflow.core.domain.model.LoginState
import com.repoflow.core.domain.model.User
import com.repoflow.core.domain.repository.AuthRepository
import com.repoflow.core.ui.BaseViewModel
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class LoginViewModel @Inject constructor(
    private val authRepository: AuthRepository
) : BaseViewModel() {

    private val _loginState = MutableStateFlow(LoginState())
    val loginState: StateFlow<LoginState> = _loginState.asStateFlow()

    private val _loginSuccess = MutableStateFlow<User?>(null)
    val loginSuccess: StateFlow<User?> = _loginSuccess.asStateFlow()

    fun loginWithToken(token: String) {
        if (token.isBlank()) {
            _loginState.value = _loginState.value.copy(error = "Token cannot be empty")
            return
        }

        viewModelScope.launch {
            _loginState.value = _loginState.value.copy(isLoading = true, error = null)
            showLoading()

            val result = authRepository.loginWithToken(token.trim())

            result.fold(
                onSuccess = { user ->
                    _loginState.value = _loginState.value.copy(isLoading = false)
                    _loginSuccess.value = user
                },
                onFailure = { error ->
                    _loginState.value = _loginState.value.copy(
                        isLoading = false,
                        error = error.message ?: "Login failed"
                    )
                }
            )

            hideLoading()
        }
    }

    fun loginWithOAuth() {
        viewModelScope.launch {
            _loginState.value = _loginState.value.copy(
                isLoading = true,
                error = null,
                isOAuthInProgress = true
            )
            showLoading()

            val result = authRepository.loginWithOAuth()

            result.fold(
                onSuccess = { user ->
                    _loginState.value = _loginState.value.copy(
                        isLoading = false,
                        isOAuthInProgress = false
                    )
                    _loginSuccess.value = user
                },
                onFailure = { error ->
                    _loginState.value = _loginState.value.copy(
                        isLoading = false,
                        isOAuthInProgress = false,
                        error = error.message ?: "OAuth login failed"
                    )
                }
            )

            hideLoading()
        }
    }

    fun loginWithDeviceFlow() {
        viewModelScope.launch {
            _loginState.value = _loginState.value.copy(isLoading = true, error = null)
            showLoading()

            val result = authRepository.loginWithDeviceFlow()

            result.fold(
                onSuccess = { user ->
                    _loginState.value = _loginState.value.copy(isLoading = false)
                    _loginSuccess.value = user
                },
                onFailure = { error ->
                    _loginState.value = _loginState.value.copy(
                        isLoading = false,
                        error = error.message ?: "Device flow login failed"
                    )
                }
            )

            hideLoading()
        }
    }

    fun clearError() {
        _loginState.value = _loginState.value.copy(error = null)
    }
}
