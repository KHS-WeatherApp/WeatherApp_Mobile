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
import androidx.lifecycle.lifecycleScope
import com.example.kh_studyprojects_weatherapp.databinding.WeatherAdditionalFragmentBinding
import com.example.kh_studyprojects_weatherapp.domain.model.weather.WeatherAdditional
import com.example.kh_studyprojects_weatherapp.presentation.common.base.RefreshableFragment
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.launch

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
        setupWeatherDataObserver()
    }

    /**
     * ViewModel의 UiState를 관찰하고 상태에 따라 UI를 업데이트
     */
    private fun setupWeatherDataObserver() {
        viewLifecycleOwner.lifecycleScope.launch {
            viewModel.uiState.collect { state ->
                when (state) {
                    is com.example.kh_studyprojects_weatherapp.presentation.common.base.UiState.Initial -> {
                        // 초기 상태 - 아무것도 하지 않음
                    }
                    is com.example.kh_studyprojects_weatherapp.presentation.common.base.UiState.Loading -> {
                        // 로딩 상태 처리 (필요시 로딩 UI 표시)
                    }
                    is com.example.kh_studyprojects_weatherapp.presentation.common.base.UiState.Success -> {
                        updateUI(state.data)
                    }
                    is com.example.kh_studyprojects_weatherapp.presentation.common.base.UiState.Error -> {
                        // 에러 처리 (필요시 에러 UI 표시)
                        Log.e("AdditionalWeather", "데이터 로드 실패: ${state.message}")
                    }
                }
            }
        }
    }

    @SuppressLint("DefaultLocale")
    private fun updateUI(additional: WeatherAdditional) {
        try {
            fun formatToAmPm(raw: String): String? = try {
                val timePart = raw.substringAfter('T', raw)
                val time = java.time.LocalTime.parse(timePart)
                val hour12 = if (time.hour % 12 == 0) 12 else time.hour % 12
                val amPm = if (time.hour < 12) "AM" else "PM"
                String.format("%02d:%02d%s", hour12, time.minute, amPm)
            } catch (_: Exception) {
                null
            }

            additional.sunrise?.let { formatToAmPm(it) }?.let { binding.sunriseTime.text = it }
            additional.sunset?.let { formatToAmPm(it) }?.let { binding.sunsetTime.text = it }

            additional.precipitation?.let { precipitation ->
                val precipitationText = updatePrecipitationProgress(precipitation)
                binding.precipitationLevel.text = String.format("%.1f mm (%s)", precipitation, precipitationText)
            }

            additional.windSpeed?.let { windSpeed ->
                binding.windSpeedLevel.text = String.format("%.1f km/h", windSpeed)
            }

            additional.pm10?.let { pm10 ->
                binding.fineDustLevel.text = "${pm10.toInt()} μg/m³"
                updateFineDustProgress(pm10)
            }

            additional.pm2_5?.let { pm25 ->
                binding.ultraFineDustLevel.text = "${pm25.toInt()} μg/m³"
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
     * 미세먼지(PM10) 수치에 따른 진행률과 상태를 업데이트
     * @param value 미세먼지 수치
     */
    private fun updateFineDustProgress(value: Double) {
        Log.d("AdditionalWeather", "미세먼지 값: $value")
        val progress = when {
            value <= 30 -> Triple(value, "좋음", "#0048c6")    // 파랑색
            value <= 80 -> Triple(value, "보통", "#90e990")    // 초록색
            value <= 150 -> Triple(value, "나쁨", "#fcb80c")   // 주황색
            else -> Triple(value, "매우나쁨", "#fc2407")      // 빨간색
        }
        
        // 프로그레스를 단계별 범위로 조정
        /*  적절한 진행률 계산: 실제 미세먼지 값을 0-100 범위로 매핑
            미세먼지: 0-30 → 0-25%, 30-80 → 25-50%, 80-150 → 50-75%, 150+ → 75-100%
            초미세먼지: 0-15 → 0-25%, 15-35 → 25-50%, 35-75 → 50-75%, 75+ → 75-100%
        */
        binding.fineDustProgressBar.max = 100
        val progressValue = when {
            value <= 15 -> (value / 15 * 25).toInt()      // 0-15 → 0-25
            value <= 35 -> (25 + (value - 15) / 20 * 25).toInt()  // 15-35 → 25-50
            value <= 75 -> (50 + (value - 35) / 40 * 25).toInt() // 35-75 → 50-75
            else -> (75 + (value - 75) / 25 * 25).toInt().coerceAtMost(100) // 75+ → 75-100
        }
        binding.fineDustProgressBar.progress = progressValue
        
        // 프로그레스 바의 색상 변경
        binding.fineDustProgressBar.progressTintList = ColorStateList.valueOf(
            Color.parseColor(
            progress.third
        ))
    }

    /**
     * 초미세먼지(PM2.5) 수치에 따른 진행률과 상태를 업데이트
     * @param value 초미세먼지 수치
     */
    private fun updateUltraFineDustProgress(value: Double) {
        Log.d("AdditionalWeather", "초미세먼지 값: $value")
        val progress = when {
            value <= 15 -> Triple(value, "좋음", "#0048c6")    // 파랑색
            value <= 35 -> Triple(value, "보통", "#90e990")    // 초록색
            value <= 75 -> Triple(value, "나쁨", "#fcb80c")   // 주황색
            else -> Triple(value, "매우나쁨", "#fc2407")      // 빨간색
        }
        
        //프로그레스 바의 진행률 변경 - 적절한 범위로 조정
        /*  적절한 진행률 계산: 실제 미세먼지 값을 0-100 범위로 매핑
            미세먼지: 0-30 → 0-25%, 30-80 → 25-50%, 80-150 → 50-75%, 150+ → 75-100%
            초미세먼지: 0-15 → 0-25%, 15-35 → 25-50%, 35-75 → 50-75%, 75+ → 75-100%
        */
        binding.ultraFineDustProgressBar.max = 100
        val progressValue = when {
            value <= 15 -> (value / 15 * 25).toInt()      // 0-15 → 0-25
            value <= 35 -> (25 + (value - 15) / 20 * 25).toInt()  // 15-35 → 25-50
            value <= 75 -> (50 + (value - 35) / 40 * 25).toInt() // 35-75 → 50-75
            else -> (75 + (value - 75) / 25 * 25).toInt().coerceAtMost(100) // 75+ → 75-100
        }
        binding.ultraFineDustProgressBar.progress = progressValue
        
        // 프로그레스 바의 색상 변경
        binding.ultraFineDustProgressBar.progressTintList = ColorStateList.valueOf(
            Color.parseColor(
                progress.third
            ))
    }

    /**
     * UV 지수에 따른 5단계 text를 업데이트
     * @param value UV 지수
     */
    private fun updateUVProgress(value: Double) {
        val progress = when {
            value <= 2 -> Pair(20, "낮음")     // 0-2: 낮음
            value <= 5 -> Pair(40, "보통")     // 3-5: 보통
            value <= 7 -> Pair(60, "높음")     // 6-7: 높음
            value <= 10 -> Pair(80, "매우높음") // 8-10: 매우높음
            else -> Pair(100, "위험")          // 11 이상: 위험
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
            value < 5 -> "매우 적음"    // 0~5 mm 미만
            value < 10 -> "적음"       // 5~10 mm
            value < 20 -> "보통"       // 10~20 mm
            value < 80 -> "많음"       // 20~80 mm
            else -> "집중호우"         // 80 mm 이상
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
