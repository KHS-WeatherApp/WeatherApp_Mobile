package com.example.kh_studyprojects_weatherapp.presentation.common.base

/**
 * UI 상태를 표현하는 공통 sealed interface
 * - 로딩, 성공, 에러, 초기 상태를 타입 안전하게 관리
 */
sealed interface UiState<out T> {
    /**
     * 초기 상태 (데이터 로드 전)
     */
    data object Initial : UiState<Nothing>

    /**
     * 로딩 중 상태
     */
    data object Loading : UiState<Nothing>

    /**
     * 성공 상태
     * @param data 로드된 데이터
     */
    data class Success<T>(val data: T) : UiState<T>

    /**
     * 에러 상태
     * @param message 사용자에게 표시할 에러 메시지
     * @param throwable 원본 예외 (로깅/디버깅용)
     */
    data class Error(
        val message: String,
        val throwable: Throwable? = null
    ) : UiState<Nothing>
}

/**
 * UiState 확장 함수들
 */

/**
 * 성공 상태인지 확인
 */
fun <T> UiState<T>.isSuccess(): Boolean = this is UiState.Success

/**
 * 로딩 중인지 확인
 */
fun <T> UiState<T>.isLoading(): Boolean = this is UiState.Loading

/**
 * 에러 상태인지 확인
 */
fun <T> UiState<T>.isError(): Boolean = this is UiState.Error

/**
 * 데이터 가져오기 (Success 상태일 때만)
 */
fun <T> UiState<T>.getDataOrNull(): T? = (this as? UiState.Success)?.data