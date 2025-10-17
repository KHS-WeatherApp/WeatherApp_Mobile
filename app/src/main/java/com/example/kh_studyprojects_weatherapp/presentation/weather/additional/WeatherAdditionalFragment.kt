package com.example.kh_studyprojects_weatherapp.presentation.weather.additional

import android.annotation.SuppressLint
import android.content.res.ColorStateList
import android.graphics.Color
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import com.example.kh_studyprojects_weatherapp.databinding.WeatherAdditionalFragmentBinding
import com.example.kh_studyprojects_weatherapp.domain.model.weather.WeatherAdditional
import com.example.kh_studyprojects_weatherapp.presentation.common.base.RefreshableFragment
import com.example.kh_studyprojects_weatherapp.presentation.common.base.collectUiState
import dagger.hilt.android.AndroidEntryPoint

/**
 * 대기질 상태별 색상 코드 (환경부 기준)
 */
private object AirQualityColors {
    const val GOOD = "#0048c6"         // 파랑 - 좋음
    const val MODERATE = "#90e990"     // 초록 - 보통
    const val UNHEALTHY = "#fcb80c"    // 주황 - 나쁨
    const val HAZARDOUS = "#fc2407"    // 빨강 - 매우나쁨
}

/**
 * 미세먼지(PM10) 농도 기준 (환경부 고시)
 */
private object PM10Standards {
    const val GOOD = 30.0          // 좋음
    const val MODERATE = 80.0      // 보통
    const val UNHEALTHY = 150.0    // 나쁨
}

/**
 * 초미세먼지(PM2.5) 농도 기준 (환경부 고시)
 */
private object PM25Standards {
    const val GOOD = 15.0          // 좋음
    const val MODERATE = 35.0      // 보통
    const val UNHEALTHY = 75.0     // 나쁨
}

/**
 * UV 지수 기준 (WHO)
 */
private object UVIndexStandards {
    const val LOW = 2.0            // 낮음
    const val MODERATE = 5.0       // 보통
    const val HIGH = 7.0           // 높음
    const val VERY_HIGH = 10.0     // 매우높음
}

/**
 * 강수량 기준 (기상청)
 */
private object PrecipitationStandards {
    const val VERY_LIGHT = 5.0     // 매우 적음
    const val LIGHT = 10.0         // 적음
    const val MODERATE = 20.0      // 보통
    const val HEAVY = 80.0         // 많음
}

/**
 * 추가 날씨 정보를 표시하는 프래그먼트
 * - 미세먼지, 초미세먼지, UV 지수, 강수량, 일출/일몰 시간을 노출
 */
@AndroidEntryPoint
class WeatherAdditionalFragment : Fragment(), RefreshableFragment {
    private var _binding: WeatherAdditionalFragmentBinding? = null
    private val binding: WeatherAdditionalFragmentBinding
        get() = _binding ?: throw IllegalStateException("Fragment binding is accessed before onCreateView or after onDestroyView")

    private val viewModel: WeatherAdditionalViewModel by viewModels()
    
    // 외부에서 접근 가능하도록 viewModel 속성 추가
    val viewModelInstance: WeatherAdditionalViewModel
        get() = viewModel

    /**
     * 프래그먼트의 뷰를 생성하고 초기화
     * @param inflater 레이아웃 XML을 View 객체로 변환하는 인플레이터
     * @param container 부모 뷰
     * @param savedInstanceState 이전 상태가 저장된 번들
     * @return 생성된 뷰
     */
    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = WeatherAdditionalFragmentBinding.inflate(inflater, container, false)
        return binding.root
    }

    /**
     * 뷰가 생성된 후 호출되는 메서드
     * 날씨 데이터 옵저버를 설정
     */
    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        initializeProgressBars()
        setupWeatherDataObserver()
    }

    /**
     * 프로그레스 바 초기 설정
     */
    private fun initializeProgressBars() {
        binding.fineDustProgressBar.max = 100
        binding.ultraFineDustProgressBar.max = 100
    }

    /**
     * ViewModel의 UiState를 관찰하고 상태에 따라 UI를 업데이트
     */
    private fun setupWeatherDataObserver() {
        collectUiState(viewModel) { data ->
            updateUI(data)
        }
    }

    @SuppressLint("DefaultLocale")
    private fun updateUI(additional: WeatherAdditional) {
        try {
            fun formatToAmPm(raw: String): String = try {
                val timePart = raw.substringAfter('T', raw)
                val time = java.time.LocalTime.parse(timePart)
                val hour12 = if (time.hour % 12 == 0) 12 else time.hour % 12
                val amPm = if (time.hour < 12) "AM" else "PM"
                String.format("%02d:%02d%s", hour12, time.minute, amPm)
            } catch (e: Exception) {
                Log.w("AdditionalWeather", "시간 포맷팅 실패: $raw", e)
                "--:--"
            }

            binding.sunriseTime.text = additional.sunrise?.let { formatToAmPm(it) } ?: "--:--"
            binding.sunsetTime.text = additional.sunset?.let { formatToAmPm(it) } ?: "--:--"

            additional.precipitation?.let { precipitation ->
                val precipitationText = updatePrecipitationProgress(precipitation)
                binding.precipitationLevel.text = String.format("%.1f mm (%s)", precipitation, precipitationText)
            }

            additional.windSpeed?.let { windSpeed ->
                binding.windSpeedLevel.text = String.format("%.1f km/h", windSpeed)
            }

            additional.pm10?.let { pm10 ->
                updateFineDustProgress(pm10)
            }

            additional.pm2_5?.let { pm25 ->
                updateUltraFineDustProgress(pm25)
            }

            additional.uvIndex?.let { uvIndex ->
                updateUVProgress(uvIndex)
            }
        } catch (e: Exception) {
            Log.e("AdditionalWeather", "추가 날씨 데이터 처리 중 오류", e)
        }
    }


    /**
     * 대기질 수치에 따른 진행률, 상태, 색상을 계산하는 공통 함수
     * @param value 측정값
     * @param goodThreshold 좋음 기준값
     * @param moderateThreshold 보통 기준값
     * @param unhealthyThreshold 나쁨 기준값
     * @return Pair<진행률(0-100), 색상>
     */
    private fun calculateAirQualityProgress(
        value: Double,
        goodThreshold: Double,
        moderateThreshold: Double,
        unhealthyThreshold: Double
    ): Pair<Int, String> {
        // 상태별 색상 결정
        val color = when {
            value <= goodThreshold -> AirQualityColors.GOOD
            value <= moderateThreshold -> AirQualityColors.MODERATE
            value <= unhealthyThreshold -> AirQualityColors.UNHEALTHY
            else -> AirQualityColors.HAZARDOUS
        }

        // 4단계 범위로 진행률 계산 (각 단계마다 25%씩 할당)
        val progressValue = when {
            value <= goodThreshold ->
                (value / goodThreshold * 25).toInt()
            value <= moderateThreshold ->
                (25 + (value - goodThreshold) / (moderateThreshold - goodThreshold) * 25).toInt()
            value <= unhealthyThreshold ->
                (50 + (value - moderateThreshold) / (unhealthyThreshold - moderateThreshold) * 25).toInt()
            else ->
                (75 + (value - unhealthyThreshold) / (unhealthyThreshold * 0.5) * 25).toInt().coerceAtMost(100)
        }

        return Pair(progressValue, color)
    }

    /**
     * 미세먼지(PM10) 수치에 따른 진행률과 상태를 업데이트
     * @param value 미세먼지 수치
     */
    private fun updateFineDustProgress(value: Double) {
        Log.d("AdditionalWeather", "미세먼지 값: $value")

        val (progressValue, color) = calculateAirQualityProgress(
            value,
            PM10Standards.GOOD,
            PM10Standards.MODERATE,
            PM10Standards.UNHEALTHY
        )

        // 상태 텍스트 결정
        val statusText = when {
            value <= PM10Standards.GOOD -> "좋음"
            value <= PM10Standards.MODERATE -> "보통"
            value <= PM10Standards.UNHEALTHY -> "나쁨"
            else -> "매우나쁨"
        }

        binding.fineDustProgressBar.progress = progressValue
        binding.fineDustProgressBar.progressTintList = ColorStateList.valueOf(Color.parseColor(color))
        binding.fineDustProgressBar.contentDescription = "미세먼지 ${value.toInt()} μg/m³, $statusText"
        binding.fineDustLevel.text = "${value.toInt()} μg/m³ ($statusText)"
    }

    /**
     * 초미세먼지(PM2.5) 수치에 따른 진행률과 상태를 업데이트
     * @param value 초미세먼지 수치
     */
    private fun updateUltraFineDustProgress(value: Double) {
        Log.d("AdditionalWeather", "초미세먼지 값: $value")

        val (progressValue, color) = calculateAirQualityProgress(
            value,
            PM25Standards.GOOD,
            PM25Standards.MODERATE,
            PM25Standards.UNHEALTHY
        )

        // 상태 텍스트 결정
        val statusText = when {
            value <= PM25Standards.GOOD -> "좋음"
            value <= PM25Standards.MODERATE -> "보통"
            value <= PM25Standards.UNHEALTHY -> "나쁨"
            else -> "매우나쁨"
        }

        binding.ultraFineDustProgressBar.progress = progressValue
        binding.ultraFineDustProgressBar.progressTintList = ColorStateList.valueOf(Color.parseColor(color))
        binding.ultraFineDustProgressBar.contentDescription = "초미세먼지 ${value.toInt()} μg/m³, $statusText"
        binding.ultraFineDustLevel.text = "${value.toInt()} μg/m³ ($statusText)"
    }

    /**
     * UV 지수에 따른 5단계 text를 업데이트
     * @param value UV 지수
     */
    private fun updateUVProgress(value: Double) {
        val progress = when {
            value <= UVIndexStandards.LOW -> Pair(20, "낮음")
            value <= UVIndexStandards.MODERATE -> Pair(40, "보통")
            value <= UVIndexStandards.HIGH -> Pair(60, "높음")
            value <= UVIndexStandards.VERY_HIGH -> Pair(80, "매우높음")
            else -> Pair(100, "위험")
        }
        binding.uvIndexLevel.text = progress.second

    }

    /**
     * 강수량에 따른 텍스트를 반환하는 함수
     * @param value 강수량 (mm)
     * @return 강수량에 따른 텍스트
     */
    private fun updatePrecipitationProgress(value: Double): String {
        return when {
            value < PrecipitationStandards.VERY_LIGHT -> "매우 적음"
            value < PrecipitationStandards.LIGHT -> "적음"
            value < PrecipitationStandards.MODERATE -> "보통"
            value < PrecipitationStandards.HEAVY -> "많음"
            else -> "집중호우"
        }
    }

    /**
     * 날씨 데이터 새로고침
     */
    override fun refreshWeatherData() {
        viewModel.refreshWeatherData()
    }

    /**
     * 프래그먼트의 뷰가 제거될 때 호출
     * 메모리 누수를 방지하기 위해 바인딩을 null로 설정
     */
    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}
