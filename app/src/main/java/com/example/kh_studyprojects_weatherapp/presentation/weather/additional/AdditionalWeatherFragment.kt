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
import com.example.kh_studyprojects_weatherapp.databinding.FragmentAdditionalWeatherBinding
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.launch

/**
 * 추가 날씨 정보를 표시하는 프래그먼트
 * - 미세먼지, 초미세먼지, UV 지수, 강수량, 풍속, 일출/일몰 시간 등을 표시
 */
@AndroidEntryPoint
class AdditionalWeatherFragment : Fragment() {
    private var _binding: FragmentAdditionalWeatherBinding? = null
    private val binding get() = _binding!!

    private val viewModel: AdditionalWeatherViewModel by viewModels()

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
        _binding = FragmentAdditionalWeatherBinding.inflate(inflater, container, false)
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
     * ViewModel의 날씨 데이터를 관찰하고 UI를 업데이트하는 옵저버를 설정
     * 데이터가 변경될 때마다 updateUI를 호출
     */
    private fun setupWeatherDataObserver() {
        viewLifecycleOwner.lifecycleScope.launch {
            viewModel.weatherState.collect { combinedData ->
                if (combinedData.isNotEmpty()) {
                    updateUI(combinedData)
                }
            }
        }
    }

    @SuppressLint("DefaultLocale")
    private fun updateUI(combinedData: Map<String, Any>) {
        try {
            /*
                💚 additional에서 필요한 변수
                (1) 미세먼지    => '대기질' current 변수
                (2) 초미세먼지  => '대기질' current 변수
                (3) 자외선지수  => '대기질'   current 변수
                (4) 강수량	   => '기본'   current 변수
                (5) 풍속       => '기본'   current 변수
                (6) 일출/일몰	=> '기본'  daily 변수

            */

            // 1. 기본 날씨 데이터 처리 - daily
            val daily = combinedData["daily"] as? Map<*, *>
            if (daily == null) {
                android.util.Log.e("AdditionalWeather", "daily 데이터가 null입니다.")
                return
            }
            daily.let {
                val timeList = it["time"] as? List<*>
                val sunriseList = it["sunrise"] as? List<*>
                val sunsetList = it["sunset"] as? List<*>

                // 현재 날짜 구하기 (YYYY-MM-DD 형식)
                val currentDate = java.time.LocalDate.now().toString()

                // 오늘 날짜와 일치하는 인덱스 찾기
                val todayIndex = timeList?.indexOfFirst { date ->
                    date.toString() == currentDate
                } ?: -1

                if (todayIndex == -1) {
                    android.util.Log.e("AdditionalWeather", "오늘 날짜에 해당하는 인덱스를 찾을 수 없습니다.")
                    return
                }

                // (6) 일출/일몰 시간
                val sunrise = sunriseList?.get(todayIndex)?.toString()
                val sunset = sunsetList?.get(todayIndex)?.toString()

                if (sunrise == null || sunset == null) {
                    android.util.Log.e("AdditionalWeather", "일출/일몰 시간 데이터가 null입니다.")
                } else {
                    // "2025-04-12T06:01" 형식에서 시간만 추출하고 AM/PM 형식으로 변환
                    val sunriseTime = java.time.LocalTime.parse(sunrise.substringAfter("T"))
                    val sunsetTime = java.time.LocalTime.parse(sunset.substringAfter("T"))

                    // 시간을 AM/PM 형식으로 변환
                    val sunriseFormatted = String.format("%02d:%02d%s",
                        if (sunriseTime.hour % 12 == 0) 12 else sunriseTime.hour % 12,
                        sunriseTime.minute,
                        if (sunriseTime.hour < 12) "AM" else "PM"
                    )

                    val sunsetFormatted = String.format("%02d:%02d%s",
                        if (sunsetTime.hour % 12 == 0) 12 else sunsetTime.hour % 12,
                        sunsetTime.minute,
                        if (sunsetTime.hour < 12) "AM" else "PM"
                    )

                    binding.sunriseTime.text = sunriseFormatted
                    binding.sunsetTime.text = sunsetFormatted
                }
            }

            //2. 기본 날씨 데이터 처리 - current
            val current = combinedData["current"] as? Map<*, *>
            if (current == null) {
                android.util.Log.e("AdditionalWeather", "current 데이터가 null입니다.")
                return
            }
            current.let {

                // (4) 강수량
                val precipitation = it["precipitation"] as? Double
                if (precipitation == null) {
                    android.util.Log.e("AdditionalWeather", "강수량 데이터가 null입니다.")
                } else {
                    val precipitationText = updatePrecipitationProgress(precipitation)
                    binding.precipitationLevel.text = "${precipitation} mm ($precipitationText)"
                }

                // (5) 풍속
                val windSpeed = it["wind_speed_10m"] as? Double
                if (windSpeed == null) {
                    android.util.Log.e("AdditionalWeather", "풍속 데이터가 null입니다.")
                } else {
                    binding.windSpeedLevel.text = "${windSpeed} km/h"
                }
            }

            // 3. 대기질 데이터 처리 - air_current
            val airCurrent = combinedData["air_current"] as? Map<*, *>
            if (airCurrent == null) {
                android.util.Log.e("AdditionalWeather", "air_current 데이터가 null입니다.")
                return
            }
            airCurrent.let {
                // (1) 미세먼지 (PM10)
                val pm10 = it["pm10"] as? Double
                if (pm10 == null) {
                    android.util.Log.e("AdditionalWeather", "미세먼지(PM10) 데이터가 null입니다.")
                } else {
                    binding.fineDustLevel.text = "${pm10.toInt()} μg/m³"
                    updateFineDustProgress(pm10)
                }

                // (2) 초미세먼지 (PM2.5)
                val pm25 = it["pm2_5"] as? Double
                if (pm25 == null) {
                    android.util.Log.e("AdditionalWeather", "초미세먼지(PM2.5) 데이터가 null입니다.")
                } else {
                    binding.ultraFineDustLevel.text = "${pm25.toInt()} μg/m³"
                    updateUltraFineDustProgress(pm25)
                }

                // (3) UV 지수
                val uvi = it["uv_index"] as? Double
                if (uvi == null) {
                    android.util.Log.e("AdditionalWeather", "uv지수 데이터가 null입니다.")
                } else {
                    updateUVProgress(uvi)
                }
            }
        } catch (e: Exception) {
            android.util.Log.e("AdditionalWeather", "날씨 데이터 처리 중 오류 발생: ${e.message}")
            e.printStackTrace()
        }
    }

    /**
     * 미세먼지(PM10) 수치에 따른 진행률과 상태를 업데이트
     * @param value 미세먼지 수치
     */
    private fun updateFineDustProgress(value: Double) {
        android.util.Log.d("AdditionalWeather", "🧡🧡🧡미세먼지❤❤❤"+value)
        val progress = when {
            value <= 30 -> Triple(value, "좋음", "#0048c6")    // 파랑색
            value <= 80 -> Triple(value, "보통", "#90e990")    // 초록색
            value <= 150 -> Triple(value, "나쁨", "#fcb80c")   // 주황색
            else -> Triple(value, "매우나쁨", "#fc2407")      // 빨간색
        }
        //프로그레스 바의 진행률 변경(최대 200으로 넣음)
        binding.fineDustProgressBar.max = 200
        binding.fineDustProgressBar.progress = progress.first.toInt()
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
        android.util.Log.d("AdditionalWeather", "🧡🧡🧡초미세먼지❤❤❤"+value)
        val progress = when {
            value <= 15 -> Triple(value, "좋음", "#0048c6")    // 파랑색
            value <= 35 -> Triple(value, "보통", "#90e990")    // 초록색
            value <= 75 -> Triple(value, "나쁨", "#fcb80c")   // 주황색
            else -> Triple(value, "매우나쁨", "#fc2407")      // 빨간색
        }
        //프로그레스 바의 진행률 변경(최대 200으로 넣음)
        binding.ultraFineDustProgressBar.max = 200
        binding.ultraFineDustProgressBar.progress = progress.first.toInt()
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
     * 프래그먼트의 뷰가 제거될 때 호출
     * 메모리 누수를 방지하기 위해 바인딩을 null로 설정
     */
    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}