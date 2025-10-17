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
        executeLoad(block)
    }

    /**
     * 일반 로드 (재로드 가능)
     * @param block 데이터 로드 로직
     */
    protected fun load(block: suspend () -> Result<T>) {
        executeLoad(block)
    }

    /**
     * 공통 로드 로직
     * - loadInitial과 load에서 공유하는 에러 처리 로직
     * @param block 데이터 로드 로직
     */
    private fun executeLoad(block: suspend () -> Result<T>) {
        viewModelScope.launch {
            _uiState.value = UiState.Loading

            runCatching { block() }
                .onSuccess { result ->
                    _uiState.value = result.fold(
                        onSuccess = { data -> UiState.Success(data) },
                        onFailure = { throwable -> createErrorState(throwable) }
                    )
                }
                .onFailure { throwable ->
                    _uiState.value = createErrorState(throwable)
                }
        }
    }

    /**
     * 에러 상태 생성 헬퍼
     */
    private fun createErrorState(throwable: Throwable) = UiState.Error(
        message = throwable.message ?: "알 수 없는 오류가 발생했습니다",
        throwable = throwable
    )

    /**
     * 에러 상태 초기화
     */
    protected fun clearError() {
        if (_uiState.value is UiState.Error) {
            _uiState.value = UiState.Initial
        }
    }
}
