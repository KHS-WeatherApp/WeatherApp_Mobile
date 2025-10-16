package com.example.kh_studyprojects_weatherapp.presentation.common.base

import androidx.fragment.app.Fragment
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.lifecycleScope
import androidx.lifecycle.repeatOnLifecycle
import kotlinx.coroutines.launch

/**
 * Fragment에서 UiState를 수집하는 확장 함수
 *
 * BaseLoadViewModel의 UiState를 관찰하고 상태에 따라 콜백을 실행합니다.
 * repeatOnLifecycle을 사용하여 메모리 누수를 방지합니다.
 *
 * @param T UiState의 데이터 타입
 * @param viewModel UiState를 제공하는 ViewModel
 * @param onSuccess Success 상태일 때 실행될 콜백 (데이터를 받아 UI 업데이트)
 *
 * 사용 예시:
 * ```
 * collectUiState(viewModel) { data ->
 *     updateUI(data)
 * }
 * ```
 */
fun <T> Fragment.collectUiState(
    viewModel: BaseLoadViewModel<T>,
    onSuccess: (T) -> Unit
) {
    viewLifecycleOwner.lifecycleScope.launch {
        viewLifecycleOwner.repeatOnLifecycle(Lifecycle.State.STARTED) {
            viewModel.uiState.collect { state ->
                when (state) {
                    is UiState.Initial -> {
                        // 초기 상태 - 아무것도 하지 않음
                    }
                    is UiState.Loading -> {
                        // 로딩 상태 처리 (필요시 로딩 UI 표시)
                    }
                    is UiState.Success -> {
                        onSuccess(state.data)
                    }
                    is UiState.Error -> {
                        // 에러 처리 (필요시 에러 UI 표시)
                        // 각 Fragment에서 필요시 별도 처리 가능
                    }
                }
            }
        }
    }
}
