package com.example.kh_studyprojects_weatherapp.presentation.common.base

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

abstract class BaseLoadViewModel : ViewModel() {
    private val _isInitialLoading = MutableStateFlow(true)
    val isInitialLoading: StateFlow<Boolean> = _isInitialLoading.asStateFlow()

    private val _isLoading = MutableStateFlow(false)
    val isLoading: StateFlow<Boolean> = _isLoading.asStateFlow()

    private val _error = MutableStateFlow<String?>(null)
    val error: StateFlow<String?> = _error.asStateFlow()

    protected fun loadInitial(block: suspend () -> Unit) {
        if (!_isInitialLoading.value) return
        viewModelScope.launch {
            runCatching {
                _isLoading.value = true
                _error.value = null
                block()
            }.onFailure { _error.value = it.message }
            _isLoading.value = false
            _isInitialLoading.value = false
        }
    }

    protected fun load(block: suspend () -> Unit) {
        viewModelScope.launch {
            runCatching {
                _isLoading.value = true
                _error.value = null
                block()
            }.onFailure { _error.value = it.message }
            _isLoading.value = false
        }
    }
}
