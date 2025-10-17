package com.example.kh_studyprojects_weatherapp.presentation.weather.hourly

import com.example.kh_studyprojects_weatherapp.util.DebugLogger
import com.example.kh_studyprojects_weatherapp.data.model.weather.WeatherMappers
import com.example.kh_studyprojects_weatherapp.domain.model.weather.WeatherHourlyForecastUiData
import com.example.kh_studyprojects_weatherapp.domain.repository.weather.WeatherRepository
import com.example.kh_studyprojects_weatherapp.presentation.common.base.BaseLoadViewModel
import com.example.kh_studyprojects_weatherapp.presentation.common.location.EffectiveLocationResolver
import dagger.hilt.android.lifecycle.HiltViewModel
import javax.inject.Inject

/**
 * 시간별 예보 화면의 ViewModel
 * - 위치 해석(즐겨찾기 > GPS > 기본값) 후 레포지토리에서 응답을 받아
 *   WeatherMappers로 안전하게 파싱하여 UI 상태(UiState)로 제공합니다.
 * - BaseLoadViewModel을 상속해 로딩/성공/에러 상태를 자동 관리합니다.
 */
@HiltViewModel
class WeatherHourlyForecastViewModel @Inject constructor(
    private val weatherRepository: WeatherRepository,
    private val effectiveLocationResolver: EffectiveLocationResolver
) : BaseLoadViewModel<WeatherHourlyForecastUiData>() {

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
     * 3) 매퍼로 파싱해 WeatherHourlyForecastUiData 모델로 반환
     */
    private suspend fun fetch(): Result<WeatherHourlyForecastUiData> {
        DebugLogger.d("WeatherHourlyForecast", "start fetch")

        // 1) 유효 위치 계산
        val loc = effectiveLocationResolver.resolve()
        val locationInfo = "${loc.address}\n위도: ${loc.latitude}, 경도: ${loc.longitude}"

        // 2) 레포지토리에서 날씨 응답 수신
        val result = weatherRepository.getWeatherInfo(loc.latitude, loc.longitude)

        // 3) 매퍼로 파싱해 WeatherHourlyForecastUiData 모델로 반환
        return result.map { response ->
            val items = WeatherMappers.toHourlyForecastDtos(response)
            if (items.isEmpty()) {
                DebugLogger.w("WeatherHourlyForecast", "시간별 예보가 비어있습니다. 응답 포맷 또는 데이터 부족 가능")
            } else {
                DebugLogger.d("WeatherHourlyForecast", "parsed ${items.size} hourly items")
            }
            WeatherHourlyForecastUiData(
                hourlyForecastItems = items,
                locationInfo = locationInfo
            )
        }
    }
}

