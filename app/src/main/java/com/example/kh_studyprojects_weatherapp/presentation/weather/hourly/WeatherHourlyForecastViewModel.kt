package com.example.kh_studyprojects_weatherapp.presentation.weather.hourly

import android.util.Log
import com.example.kh_studyprojects_weatherapp.data.model.weather.WeatherHourlyForecastDto
import com.example.kh_studyprojects_weatherapp.data.model.weather.WeatherMappers
import com.example.kh_studyprojects_weatherapp.domain.repository.weather.WeatherRepository
import com.example.kh_studyprojects_weatherapp.presentation.common.base.BaseLoadViewModel
import com.example.kh_studyprojects_weatherapp.presentation.common.location.EffectiveLocationResolver
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import javax.inject.Inject

@HiltViewModel
/**
 * 시간별 예보 화면의 ViewModel
 * - 위치 해석(즐겨찾기 > GPS > 기본값) 후 레포지토리에서 응답을 받아
 *   WeatherMappers로 안전하게 파싱하여 UI 상태(StateFlow)로 제공합니다.
 * - BaseLoadViewModel의 loadInitial/load를 사용해 로딩/에러 상태를 관리합니다.
 */
class WeatherHourlyForecastViewModel @Inject constructor(
    private val weatherRepository: WeatherRepository,
    private val effectiveLocationResolver: EffectiveLocationResolver
) : BaseLoadViewModel() {

    // UI에 노출할 시간별 예보 목록 상태
    private val _hourlyForecastItems = MutableStateFlow<List<WeatherHourlyForecastDto>>(emptyList())
    val hourlyForecastItems: StateFlow<List<WeatherHourlyForecastDto>> = _hourlyForecastItems.asStateFlow()

    // 주소 + 위경도 표시용 상태(헤더 등에서 사용)
    private val _locationInfo = MutableStateFlow<String?>(null)
    val locationInfo: StateFlow<String?> = _locationInfo.asStateFlow()

    init {
        // 최초 진입 시 1회 로딩
        loadInitial { fetch() }
    }

    /** 외부에서 호출하는 새로고침 진입점 */
    fun refreshWeatherData() = load { fetch() }

    /**
     * 데이터 로딩 순서
     * 1) 유효 위치 계산(즐겨찾기 > GPS > 기본값)
     * 2) 레포지토리에서 날씨 응답 수신
     * 3) 매퍼로 파싱해 상태 업데이트(빈 결과는 경고 로그 후 빈 리스트)
     */
    private suspend fun fetch() {
        Log.d("WeatherHourlyForecast", "start fetch")
        
        // 1) 유효 위치 계산
        val loc = effectiveLocationResolver.resolve()
        // 2) 위치 정보 상태 업데이트(주소 + 위경도)
        _locationInfo.value = "${loc.address}\n위도: ${loc.latitude}, 경도: ${loc.longitude}"
        // 3) 레포지토리에서 날씨 응답 수신
        val result = weatherRepository.getWeatherInfo(loc.latitude, loc.longitude)
        // 4) 매퍼로 파싱해 상태 업데이트
        result.onSuccess { response ->
            val items = WeatherMappers.toHourlyForecastDtos(response)
            if (items.isEmpty()) {
                Log.w("WeatherHourlyForecast", "시간별 예보가 비어있습니다. 응답 포맷 또는 데이터 부족 가능")
                _hourlyForecastItems.value = emptyList()
            } else {
                _hourlyForecastItems.value = items
                Log.d("WeatherHourlyForecast", "parsed ${items.size} hourly items")
            }
        }.onFailure { throw it }
    }
}

