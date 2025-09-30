package com.example.kh_studyprojects_weatherapp.presentation.common.base

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

/**
 * UiState 기반 BaseViewModel
 * - 제네릭 타입을 통해 타입 안전성 보장
 * - Initial, Loading, Success, Error 상태를 명확히 관리
 */
abstract class BaseLoadViewModel<T> : ViewModel() {
    private val _uiState = MutableStateFlow<UiState<T>>(UiState.Initial)
    val uiState: StateFlow<UiState<T>> = _uiState.asStateFlow()

    /**
     * 초기 로드 (중복 실행 방지)
     * @param block 데이터 로드 로직
     */
    protected fun loadInitial(block: suspend () -> Result<T>) {
        if (_uiState.value !is UiState.Initial) return

        viewModelScope.launch {
            _uiState.value = UiState.Loading

            runCatching { block() }
                .onSuccess { result ->
                    result.fold(
                        onSuccess = { data -> _uiState.value = UiState.Success(data) },
                        onFailure = { throwable ->
                            _uiState.value = UiState.Error(
                                message = throwable.message ?: "알 수 없는 오류가 발생했습니다",
                                throwable = throwable
                            )
                        }
                    )
                }
                .onFailure { throwable ->
                    _uiState.value = UiState.Error(
                        message = throwable.message ?: "알 수 없는 오류가 발생했습니다",
                        throwable = throwable
                    )
                }
        }
    }

    /**
     * 일반 로드 (재로드 가능)
     * @param block 데이터 로드 로직
     */
    protected fun load(block: suspend () -> Result<T>) {
        viewModelScope.launch {
            _uiState.value = UiState.Loading

            runCatching { block() }
                .onSuccess { result ->
                    result.fold(
                        onSuccess = { data -> _uiState.value = UiState.Success(data) },
                        onFailure = { throwable ->
                            _uiState.value = UiState.Error(
                                message = throwable.message ?: "알 수 없는 오류가 발생했습니다",
                                throwable = throwable
                            )
                        }
                    )
                }
                .onFailure { throwable ->
                    _uiState.value = UiState.Error(
                        message = throwable.message ?: "알 수 없는 오류가 발생했습니다",
                        throwable = throwable
                    )
                }
        }
    }

    /**
     * 에러 상태 초기화
     */
    protected fun clearError() {
        if (_uiState.value is UiState.Error) {
            _uiState.value = UiState.Initial
        }
    }
}

/**
 * 레거시 BaseViewModel (제네릭 없는 버전)
 * - 복잡한 상태 관리가 필요한 ViewModel을 위한 호환 클래스
 * - WeatherDailyViewModel, WeatherHourlyForecastViewModel 등에서 사용
 */
abstract class BaseLegacyViewModel : ViewModel() {
    private val _isInitialLoading = MutableStateFlow(true)
    val isInitialLoading: StateFlow<Boolean> = _isInitialLoading.asStateFlow()

    private val _isLoading = MutableStateFlow(false)
    val isLoading: StateFlow<Boolean> = _isLoading.asStateFlow()

    private val _error = MutableStateFlow<String?>(null)
    val error: StateFlow<String?> = _error.asStateFlow()

    /**
     * 초기 로드 (중복 실행 방지)
     */
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

    /**
     * 일반 로드 (재로드 가능)
     */
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
