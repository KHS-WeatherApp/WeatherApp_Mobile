package com.example.kh_studyprojects_weatherapp.presentation.weather.current

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.lifecycle.lifecycleScope
import com.example.kh_studyprojects_weatherapp.databinding.WeatherCurrentFragmentBinding
import com.example.kh_studyprojects_weatherapp.domain.model.weather.WeatherCurrent
import com.example.kh_studyprojects_weatherapp.domain.model.weather.WeatherCommon
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.launch

@AndroidEntryPoint
class WeatherCurrentFragment : Fragment() {
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
                        // Toast나 Snackbar로 에러 메시지 표시 가능
                    }
                }
            }
        }
    }

    private fun updateUI(model: WeatherCurrent) {
        try {
            // 위치 표시(간략 표기)
            val address = model.location
            val thoroughfare = address.split(" ").lastOrNull() ?: address
            binding.location.text = thoroughfare

            // 현재 기온/체감온도/아이콘
            model.apparentTemperature?.let { temp ->
                binding.apparentTemperature.text = "체감온도 : ${temp}°"
                binding.RecommendClothesIcon.setImageResource(WeatherCommon.getClothingIcon(temp.toDouble()))
                binding.RecommendClothes.text = "추천 옷: ${getClothingText(temp.toDouble())}"
            }
            binding.CurrentTemp.text = model.temperature?.let { "${it}°" } ?: "N/A"
            binding.currentWeatherIcon.setImageResource(WeatherCommon.getWeatherIcon(model.weatherCode))

            // 어제 대비 비교
            val currentTime = model.currentTimeIso
            if (currentTime != null && model.hourlyTimes.isNotEmpty() && model.hourlyTemperatures.isNotEmpty()) {
                val currentHour = currentTime.substring(0, 13) + ":00"
                val yesterdayHour = currentTime.substring(0, 8) +
                        (currentTime.substring(8, 10).toInt() - 1).toString().padStart(2, '0') +
                        currentTime.substring(10, 13) + ":00"
                val times = model.hourlyTimes
                val temps = model.hourlyTemperatures
                val currentIndex = times.indexOfFirst { it.startsWith(currentHour) }
                val yesterdayIndex = times.indexOfFirst { it.startsWith(yesterdayHour) }
                if (currentIndex != -1 && yesterdayIndex != -1) {
                    val yesterdayTemp = temps.getOrNull(yesterdayIndex) ?: 0.0
                    val tempNow = model.temperature?.toDouble() ?: 0.0
                    val diff = tempNow - yesterdayTemp
                    binding.WeatherDiff.text = when {
                        diff > 0 -> "어제보다 ${diff.toInt()}° 따뜻해요"
                        diff < 0 -> "어제보다 ${-diff.toInt()}° 추워요"
                        else -> "어제와 비슷해요"
                    }
                }
            }

            // 오늘 최고/최저
            val tMax = model.todayMaxTemp?.toInt()
            val tMin = model.todayMinTemp?.toInt()
            if (tMax != null && tMin != null) {
                binding.dailyMinMaxTemp.text = "최고 : ${tMax}° / 최저 : ${tMin}°"
            }
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }

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

    private fun setupMenuButton() {
        binding.ivMenu.setOnClickListener {
            // 메뉴 열기(드로어)
            (activity as? com.example.kh_studyprojects_weatherapp.MainActivity)?.openDrawer()
        }
    }

    fun refreshWeatherData() {
        viewModel.refreshWeatherData()
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}
