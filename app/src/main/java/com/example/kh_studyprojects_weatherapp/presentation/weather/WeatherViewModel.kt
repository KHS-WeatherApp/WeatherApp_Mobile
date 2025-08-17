package com.example.kh_studyprojects_weatherapp.presentation.weather

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

/**
 * 날씨 메인 화면 ViewModel
 * * @author 김지윤
 * @since 2025.08.12
 * @version 1.0
 */
@HiltViewModel
class WeatherViewModel @Inject constructor() : ViewModel() {

    private val _isRefreshing = MutableStateFlow(false)
    val isRefreshing: StateFlow<Boolean> = _isRefreshing.asStateFlow()

    // 갱신 진행 상태를 위한 StateFlow
    private val _refreshStatus = MutableStateFlow("")
    val refreshStatus: StateFlow<String> = _refreshStatus.asStateFlow()

    // 갱신 카운터 (갱신 횟수 표시용)
    private val _refreshCount = MutableStateFlow(0)
    val refreshCount: StateFlow<Int> = _refreshCount.asStateFlow()

    /**
     * 날씨 데이터 새로고침
     */
    fun refreshWeatherData() {
        viewModelScope.launch {
            _isRefreshing.value = true
            _refreshCount.value = _refreshCount.value + 1

            try {
                // 갱신 시작을 알림
                _refreshStatus.value = "데이터 갱신 시작..."

                // 실제 데이터 갱신은 각 자식 프래그먼트에서 처리
                // 프로그레스바는 Fragment에서 직접 제어
                // 여기서는 갱신 시작만 알림

                // 갱신 진행 상태 업데이트
                kotlinx.coroutines.delay(500)
                _refreshStatus.value = "위치 정보 확인 중..."

                kotlinx.coroutines.delay(500)
                _refreshStatus.value = "날씨 데이터 수집 중..."

                kotlinx.coroutines.delay(500)
                _refreshStatus.value = "데이터 처리 중..."

            } catch (e: Exception) {
                _isRefreshing.value = false
                _refreshStatus.value = "갱신 중 오류 발생"
            }
        }
    }

    // 새로고침 완료 시 호출하여 프로그레스바를 숨김
    fun onRefreshComplete() {
        _isRefreshing.value = false
        _refreshStatus.value = "갱신 완료"
    }

    /**
     * 갱신 상태 초기화
     */
    fun resetRefreshStatus() {
        _refreshStatus.value = ""
    }
}