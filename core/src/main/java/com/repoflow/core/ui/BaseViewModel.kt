package com.repoflow.core.ui

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow

abstract class BaseViewModel : ViewModel() {

    protected val coroutineScope: CoroutineScope = viewModelScope

    private val _isLoading = MutableStateFlow(false)
    val isLoading: StateFlow<Boolean> = _isLoading.asStateFlow()

    protected fun showLoading() {
        _isLoading.value = true
    }

    protected fun hideLoading() {
        _isLoading.value = false
    }
}
