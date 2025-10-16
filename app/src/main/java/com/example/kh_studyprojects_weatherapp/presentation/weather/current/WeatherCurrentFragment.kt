package com.example.kh_studyprojects_weatherapp.presentation.weather.current

import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import com.example.kh_studyprojects_weatherapp.databinding.WeatherCurrentFragmentBinding
import com.example.kh_studyprojects_weatherapp.domain.model.weather.WeatherCurrent
import com.example.kh_studyprojects_weatherapp.domain.model.weather.WeatherCommon
import com.example.kh_studyprojects_weatherapp.presentation.common.base.RefreshableFragment
import com.example.kh_studyprojects_weatherapp.presentation.common.base.collectUiState
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
class WeatherCurrentFragment : Fragment(), RefreshableFragment {
    private var _binding: WeatherCurrentFragmentBinding? = null
    private val binding: WeatherCurrentFragmentBinding
        get() = _binding ?: throw IllegalStateException("Fragment binding is accessed before onCreateView or after onDestroyView")

    private val viewModel: WeatherCurrentViewModel by viewModels()
    val viewModelInstance: WeatherCurrentViewModel get() = viewModel

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
        collectUiState(viewModel) { data ->
            updateUI(data)
        }
    }

    private fun updateUI(model: WeatherCurrent) {
        try {
            updateLocation(model.location)
            updateTemperature(model)
            updateYesterdayComparison(model)
            updateTodayMinMax(model.todayMaxTemp, model.todayMinTemp)
        } catch (e: Exception) {
            Log.e("WeatherCurrent", "날씨 데이터 처리 중 오류", e)
        }
    }

    /**
     * 위치 정보를 간략하게 표시
     * 주소의 마지막 부분만 표시 (예: "서울특별시 강남구 역삼동" → "역삼동")
     */
    private fun updateLocation(location: String) {
        val thoroughfare = location.split(" ").lastOrNull() ?: location
        binding.location.text = thoroughfare
    }

    /**
     * 현재 온도, 체감온도, 날씨 아이콘, 옷 추천 표시
     */
    private fun updateTemperature(model: WeatherCurrent) {
        model.apparentTemperature?.let { temp ->
            binding.apparentTemperature.text = "체감온도 : ${temp}°"
            binding.RecommendClothesIcon.setImageResource(WeatherCommon.getClothingIcon(temp.toDouble()))
            binding.RecommendClothes.text = "추천 옷: ${WeatherCommon.getClothingText(temp.toDouble())}"
        }
        binding.CurrentTemp.text = model.temperature?.let { "${it}°" } ?: "N/A"
        binding.currentWeatherIcon.setImageResource(WeatherCommon.getWeatherIcon(model.weatherCode))
    }

    /**
     * 어제 같은 시간 대비 온도 차이 표시
     */
    private fun updateYesterdayComparison(model: WeatherCurrent) {
        val currentTime = model.currentTimeIso ?: return
        if (model.hourlyTimes.isEmpty() || model.hourlyTemperatures.isEmpty()) return

        val currentTemp = model.temperature?.toDouble() ?: return
        val yesterdayTemp = findYesterdayTemperature(currentTime, model.hourlyTimes, model.hourlyTemperatures) ?: return

        val diff = currentTemp - yesterdayTemp
        binding.WeatherDiff.text = when {
            diff > 0 -> "어제보다 ${diff.toInt()}° 따뜻해요"
            diff < 0 -> "어제보다 ${-diff.toInt()}° 추워요"
            else -> "어제와 비슷해요"
        }
    }

    /**
     * 어제 같은 시간의 온도를 찾아 반환
     *
     * 시간별 데이터 배열에서 현재 시간으로부터 24시간 전(인덱스 -24)의 온도를 반환합니다.
     *
     * @param currentTimeIso 현재 시간 ISO 형식 (예: "2024-01-15T14:00")
     * @param hourlyTimes 시간별 타임스탬프 리스트
     * @param hourlyTemperatures 시간별 온도 리스트
     * @return 어제 온도, 찾지 못하면 null
     */
    private fun findYesterdayTemperature(
        currentTimeIso: String,
        hourlyTimes: List<String>,
        hourlyTemperatures: List<Double>
    ): Double? {
        // 현재 시간의 인덱스 찾기
        val currentIndex = hourlyTimes.indexOfFirst { it.startsWith(currentTimeIso.substring(0, 13)) }
        if (currentIndex == -1) return null

        // 24시간 전 = 인덱스 -24
        val yesterdayIndex = currentIndex - 24
        return hourlyTemperatures.getOrNull(yesterdayIndex)
    }

    /**
     * 오늘의 최고/최저 온도 표시
     */
    private fun updateTodayMinMax(maxTemp: Double?, minTemp: Double?) {
        if (maxTemp != null && minTemp != null) {
            binding.dailyMinMaxTemp.text = "최고 : ${maxTemp.toInt()}° / 최저 : ${minTemp.toInt()}°"
        }
    }

    private fun setupMenuButton() {
        binding.ivMenu.setOnClickListener {
            // 메뉴 열기(드로어)
            (activity as? com.example.kh_studyprojects_weatherapp.MainActivity)?.openDrawer()
        }
    }

    override fun refreshWeatherData() {
        viewModel.refreshWeatherData()
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}
