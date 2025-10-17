package com.example.kh_studyprojects_weatherapp.presentation.weather

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.kh_studyprojects_weatherapp.domain.repository.weather.WeatherRepository
import com.example.kh_studyprojects_weatherapp.presentation.common.location.EffectiveLocationResolver
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

/**
 * 날씨 메인 화면 ViewModel
 * @author 김지윤
 * @since 2025.08.12
 * @version 2.0 - 실제 데이터 갱신 로직 추가
 */
@HiltViewModel
class WeatherViewModel @Inject constructor(
    private val weatherRepository: WeatherRepository,
    private val locationResolver: EffectiveLocationResolver
) : ViewModel() {

    private val _isRefreshing = MutableStateFlow(false)
    val isRefreshing: StateFlow<Boolean> = _isRefreshing.asStateFlow()

    // 갱신 진행 상태
    private val _refreshStatus = MutableStateFlow("")
    val refreshStatus: StateFlow<String> = _refreshStatus.asStateFlow()

    // 갱신 카운터
    private val _refreshCount = MutableStateFlow(0)
    val refreshCount: StateFlow<Int> = _refreshCount.asStateFlow()

    // 갱신 성공 이벤트 (자식 프래그먼트에서 구독)
    private val _refreshTrigger = MutableStateFlow(0)
    val refreshTrigger: StateFlow<Int> = _refreshTrigger.asStateFlow()

    // 초기 로딩 오버레이 표시 여부
    private val _hasShownInitialOverlay = MutableStateFlow(false)
    val hasShownInitialOverlay: StateFlow<Boolean> = _hasShownInitialOverlay.asStateFlow()

    fun markInitialOverlayShown() {
        _hasShownInitialOverlay.value = true
    }

    /**
     * 실제 날씨 데이터 새로고침
     * - 위치 확인 → 날씨 데이터 조회 → 자식 프래그먼트에 알림
     */
    fun refreshWeatherData() {
        if (_isRefreshing.value) return // 중복 실행 방지

        viewModelScope.launch {
            _isRefreshing.value = true
            _refreshCount.value = _refreshCount.value + 1

            try {
                // 1. 위치 정보 확인
                _refreshStatus.value = "위치 정보 확인 중..."
                val location = locationResolver.resolve()

                // 2. 날씨 데이터 조회
                _refreshStatus.value = "날씨 데이터 수집 중..."
                val result = weatherRepository.getWeatherInfo(
                    latitude = location.latitude,
                    longitude = location.longitude
                )

                result.fold(
                    onSuccess = { weatherData ->
                        // 3. 데이터 처리 완료
                        _refreshStatus.value = "데이터 처리 완료"

                        // 4. 자식 프래그먼트들에게 갱신 알림
                        _refreshTrigger.value = _refreshTrigger.value + 1

                        _refreshStatus.value = "갱신 완료"
                    },
                    onFailure = { error ->
                        _refreshStatus.value = "갱신 실패: ${error.message}"
                    }
                )

            } catch (e: Exception) {
                _refreshStatus.value = "갱신 중 오류 발생: ${e.message}"
            } finally {
                _isRefreshing.value = false

                // 2초 후 상태 메시지 초기화
                kotlinx.coroutines.delay(2000)
                if (_refreshStatus.value.contains("완료") || _refreshStatus.value.contains("실패")) {
                    _refreshStatus.value = ""
                }
            }
        }
    }

    // 새로고침 완료 시 호출 (레거시 호환)
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