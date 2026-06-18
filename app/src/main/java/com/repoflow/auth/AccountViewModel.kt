package com.repoflow.auth

import androidx.lifecycle.viewModelScope
import com.repoflow.core.domain.model.AccountState
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
class AccountViewModel @Inject constructor(
    private val authRepository: AuthRepository
) : BaseViewModel() {

    private val _accountState = MutableStateFlow(AccountState())
    val accountState: StateFlow<AccountState> = _accountState.asStateFlow()

    private val _logoutComplete = MutableStateFlow(false)
    val logoutComplete: StateFlow<Boolean> = _logoutComplete.asStateFlow()

    init {
        loadUser()
    }

    private fun loadUser() {
        viewModelScope.launch {
            _accountState.value = _accountState.value.copy(isLoading = true)
            showLoading()

            val result = authRepository.getCurrentUser()
            result.fold(
                onSuccess = { user ->
                    _accountState.value = _accountState.value.copy(
                        user = user,
                        isLoading = false
                    )
                },
                onFailure = { error ->
                    _accountState.value = _accountState.value.copy(
                        isLoading = false,
                        error = error.message
                    )
                }
            )

            hideLoading()
        }
    }

    fun logout() {
        viewModelScope.launch {
            _accountState.value = _accountState.value.copy(isLoggingOut = true)
            showLoading()

            authRepository.logout()

            _accountState.value = AccountState()
            _logoutComplete.value = true

            hideLoading()
        }
    }
}
