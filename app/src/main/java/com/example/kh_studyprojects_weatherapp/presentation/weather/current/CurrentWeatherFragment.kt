package com.example.kh_studyprojects_weatherapp.presentation.weather.current

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.lifecycle.lifecycleScope
import com.example.kh_studyprojects_weatherapp.R
import com.example.kh_studyprojects_weatherapp.databinding.WeatherCurrentFragmentBinding
import com.example.kh_studyprojects_weatherapp.domain.model.weather.WeatherCommon
import com.example.kh_studyprojects_weatherapp.presentation.MainActivity
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.launch

@AndroidEntryPoint
class CurrentWeatherFragment : Fragment() {
    private var _binding: WeatherCurrentFragmentBinding? = null
    private val binding get() = _binding!!

    private val viewModel: CurrentWeatherViewModel by viewModels()
    
    // 외부에서 접근 가능하도록 viewModel 속성 추가
    val viewModelInstance: CurrentWeatherViewModel
        get() = viewModel

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = WeatherCurrentFragmentBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        setupWeatherDataObserver()
        setupMenuButton()
    }

    private fun setupWeatherDataObserver() {
        viewLifecycleOwner.lifecycleScope.launch {
            viewModel.weatherState.collect { weatherData ->
                if (weatherData.isNotEmpty()) {
                    updateUI(weatherData)
                }
            }
        }
    }

    private fun updateUI(weatherData: Map<String, Any>) {
        try {
            // MainActivity에 데이터 업데이트 알림
            (activity as? MainActivity)?.updateCurrentWeatherData(weatherData)
            
            // 위치 정보 표시
            weatherData["location"]?.let { location ->
                // 주소에서 thoroughfare(동/읍/면) 정보만 추출
                val address = location.toString()
                val thoroughfare = address.split(" ").lastOrNull() ?: address
                binding.location.text = thoroughfare
            }

            // 현재 날씨 데이터 처리
            val current = weatherData["current"] as? Map<*, *>
            current?.let {
                // 1. 체감온도
                val apparentTemperature = it["apparent_temperature"] as? Double
                binding.apparentTemperature.text = "체감온도 : ${apparentTemperature?.toInt()}°"

                // 2. 현재 온도
                val temperature = it["temperature_2m"] as? Double
                binding.CurrentTemp.text = "${temperature?.toInt()}°"

                // 3. 날씨 코드 - 안씀(2025.05.31 변경)
                val weatherCode = (it["weather_code"] as? Number)?.toInt() ?: 0
//                binding.weatherCode.text = getWeatherText(weatherCode)
                
                // 4. 날씨 아이콘
                binding.currentWeatherIcon.setImageResource(WeatherCommon.getWeatherIcon(weatherCode))

                // 5. 옷차림 추천(아이콘)
                apparentTemperature?.let { temp ->
                    binding.RecommendClothesIcon.setImageResource(WeatherCommon.getClothingIcon(temp))
                }
                // 6. 옷차림 추천(텍스트)
                apparentTemperature?.let { temp ->
                    binding.RecommendClothes.text = "추천 옷 : ${getClothingText(temp)}"
                }
                // 7. 어제와의 온도 비교
                val currentTime = it["time"] as? String // 현재 시간
                val hourly = weatherData["hourly"] as? Map<*, *>
                
                hourly?.let { hourlyData ->
                    val times = hourlyData["time"] as? List<*>
                    val temps = hourlyData["temperature_2m"] as? List<*>
                    
                    if (times != null && temps != null && currentTime != null) {
                        // 현재 시간에서 날짜와 시간만 추출 (분은 제외)
                        // 예: "2025-03-22T01:45" -> "2025-03-22T01:00"
                        val currentHour = currentTime.substring(0, 13) + ":00"
                        
                        // 어제 같은 시간 계산
                        // 예: "2025-03-22T01:00" -> "2025-03-21T01:00"
                        val yesterdayHour = currentTime.substring(0, 8) + 
                            (currentTime.substring(8, 10).toInt() - 1).toString().padStart(2, '0') +
                            currentTime.substring(10, 13) + ":00"
                        
                        // 현재 시간과 어제 시간의 인덱스 찾기
                        val currentIndex = times.indexOfFirst { it.toString().startsWith(currentHour) }
                        val yesterdayIndex = times.indexOfFirst { it.toString().startsWith(yesterdayHour) }
                        
                        if (currentIndex != -1 && yesterdayIndex != -1) {
                            val yesterdayTemp = (temps[yesterdayIndex] as? Double) ?: 0.0
                            val tempDiff = (temperature ?: 0.0) - yesterdayTemp
                            
                            binding.WeatherDiff.text = when {
                                tempDiff > 0 -> "어제보다 ${tempDiff.toInt()}° 높아요"
                                tempDiff < 0 -> "어제보다 ${-tempDiff.toInt()}° 낮아요"
                                else -> "어제와 같아요"
                            }
                        }
                    }
                }
            }

            //8.  일별 날씨 데이터 처리 - 최저&최고 온도
            val daily = weatherData["daily"] as? Map<*, *>
            daily?.let {
                val maxTemps = it["temperature_2m_max"] as? List<*>
                val minTemps = it["temperature_2m_min"] as? List<*>
                
                // 오늘 날짜의 데이터는 리스트의 첫 번째 요소...맞나? 2번째 요소 아님?
                if (maxTemps != null && minTemps != null && maxTemps.isNotEmpty() && minTemps.isNotEmpty()) {
                    val todayMaxTemp = maxTemps[1] as? Double
                    val todayMinTemp = minTemps[1] as? Double
                    binding.dailyMinMaxTemp.text = "최고 : ${todayMaxTemp?.toInt()}° / 최저 : ${todayMinTemp?.toInt()}°"
                }
            }
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }

       //현재온도에 따른 옷차림 추천 text 함수
    private fun getClothingText(temperature: Double): String {
        return when {
            temperature >= 28 -> "반팔"
            temperature >= 23 -> "얇은 셔츠"
            temperature >= 20 -> "얇은 가디건"
            temperature >= 17 -> "맨투맨"
            temperature >= 12 -> "자켓"
            temperature >= 9 -> "코트"
            else -> "패딩"
        }
    }

    //날씨코드에 따른 날씨text 함수
    private fun getWeatherText(weatherCode: Int): String {
        return when (weatherCode) {
            0 -> "맑음"
            1 -> "대체로 맑음"
            2 -> "약간 흐림"
            3 -> "흐림"
            45, 48 -> "안개"
            51, 53, 55 -> "이슬비"
            56, 57 -> "얼어붙은 이슬비"
            61, 63, 65 -> "비"
            66, 67 -> "얼어붙은 비"
            71, 73, 75 -> "눈"
            77 -> "눈날림"
            95 -> "약한 뇌우"
            96, 99 -> "강한 뇌우"
            else -> "알 수 없음"
        }
    }

    /**
     * 메뉴 버튼 설정
     */
    private fun setupMenuButton() {
        binding.ivMenu.setOnClickListener {
            // MainActivity의 사이드 메뉴 열기
            (activity as? MainActivity)?.openDrawer()
        }
    }
    
    /**
     * 날씨 데이터 새로고침
     */
    fun refreshWeatherData() {
        viewModel.refreshWeatherData()
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}