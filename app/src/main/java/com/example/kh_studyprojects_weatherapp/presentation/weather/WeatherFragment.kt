package com.example.kh_studyprojects_weatherapp.presentation.weather

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.navigation.findNavController
import com.example.kh_studyprojects_weatherapp.R
import com.example.kh_studyprojects_weatherapp.databinding.LayoutNavigationBottomBinding
import com.example.kh_studyprojects_weatherapp.databinding.WeatherFragmentBinding
import com.example.kh_studyprojects_weatherapp.presentation.weather.current.CurrentWeatherFragment
import com.example.kh_studyprojects_weatherapp.presentation.weather.daily.WeatherDailyFragment
import com.example.kh_studyprojects_weatherapp.presentation.weather.hourly.WeatherHourlyForecastFragment
import com.example.kh_studyprojects_weatherapp.presentation.weather.additional.AdditionalWeatherFragment
import com.example.kh_studyprojects_weatherapp.presentation.weather.WeatherViewModel
import dagger.hilt.android.AndroidEntryPoint
import androidx.lifecycle.lifecycleScope
import kotlinx.coroutines.flow.collect
import kotlinx.coroutines.launch
import kotlinx.coroutines.delay
import kotlinx.coroutines.async
import kotlinx.coroutines.awaitAll

/**
 * 날씨 메인 프래그먼트
 * * @author 개발자명
 * @since 2024.01.01
 * @version 1.0
 */
@AndroidEntryPoint
class WeatherFragment : Fragment() {

    private var _binding: WeatherFragmentBinding? = null
    private val binding get() = _binding!!

    private var _navigationBinding: LayoutNavigationBottomBinding? = null
    private val navigationBinding get() = _navigationBinding!!

    private val viewModel: WeatherViewModel by viewModels()

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = WeatherFragmentBinding.inflate(inflater, container, false)

        // include된 네비게이션 바인딩
        _navigationBinding = LayoutNavigationBottomBinding.bind(binding.includedNavigationBottom.root)

        setupNavigation()
        setupChildFragments(savedInstanceState)

        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        // 당겨서 새로고침 동작
        binding.swipeRefreshLayout.setOnRefreshListener {
            refreshWeatherData()
        }

        // ViewModel의 갱신 상태 관찰
        viewLifecycleOwner.lifecycleScope.launch {
            viewModel.isRefreshing.collect { isRefreshing ->
                binding.swipeRefreshLayout.isRefreshing = isRefreshing
            }
        }
    }

    /**
     * 날씨 데이터 새로고침
     */
    private fun refreshWeatherData() {
        // ViewModel을 통해 갱신 시작 알림
        viewModel.refreshWeatherData()
        
        // 각 자식 프래그먼트의 ViewModel에 데이터 갱신 요청
        try {
            // 현재 날씨 데이터 갱신
            val currentWeatherFragment = childFragmentManager
                .findFragmentById(R.id.weather_current_container) as? CurrentWeatherFragment
            currentWeatherFragment?.let { fragment ->
                fragment.viewModelInstance.refreshWeatherData()
                // UI 강제 갱신을 위한 지연 처리
                viewLifecycleOwner.lifecycleScope.launch {
                    kotlinx.coroutines.delay(100)
                    fragment.viewModelInstance.forceUIUpdate()
                }
            }
            
            // 일별 날씨 데이터 갱신
            val dailyWeatherFragment = childFragmentManager
                .findFragmentById(R.id.weather_daily_container) as? WeatherDailyFragment
            dailyWeatherFragment?.let { fragment ->
                fragment.viewModelInstance.refreshWeatherData()
                // UI 강제 갱신을 위한 지연 처리
                viewLifecycleOwner.lifecycleScope.launch {
                    kotlinx.coroutines.delay(100)
                    fragment.viewModelInstance.forceUIUpdate()
                }
            }
            
            // 시간별 날씨 데이터 갱신
            val hourlyWeatherFragment = childFragmentManager
                .findFragmentById(R.id.weather_hourly_forecast_fragment) as? WeatherHourlyForecastFragment
            hourlyWeatherFragment?.let { fragment ->
                fragment.viewModelInstance.refreshWeatherData()
                // UI 강제 갱신을 위한 지연 처리
                viewLifecycleOwner.lifecycleScope.launch {
                    kotlinx.coroutines.delay(100)
                    fragment.viewModelInstance.forceUIUpdate()
                }
            }
            
            // 추가 날씨 데이터 갱신
            val additionalWeatherFragment = childFragmentManager
                .findFragmentById(R.id.weather_additional_container) as? AdditionalWeatherFragment
            additionalWeatherFragment?.let { fragment ->
                fragment.viewModelInstance.refreshWeatherData()
                // UI 강제 갱신을 위한 지연 처리
                viewLifecycleOwner.lifecycleScope.launch {
                    kotlinx.coroutines.delay(100)
                    fragment.viewModelInstance.forceUIUpdate()
                }
            }
            
            // 실제 데이터 갱신 완료를 기다리는 대신, 
            // 적절한 시간 후 프로그레스바 종료 (API 호출 시간 고려)
            // 이는 임시 해결책이며, 실제로는 각 ViewModel의 갱신 완료 상태를 관찰해야 함
            viewLifecycleOwner.lifecycleScope.launch {
                kotlinx.coroutines.delay(1500) // 1.5초 대기 (실제 API 호출 시간에 맞춤)
                binding.swipeRefreshLayout.isRefreshing = false
                println("데이터 갱신 완료 (타이머 기반)")
            }
            
        } catch (e: Exception) {
            // 오류 발생 시 로그 출력 및 프로그레스바 종료
            println("자식 프래그먼트 데이터 갱신 중 오류: ${e.message}")
            binding.swipeRefreshLayout.isRefreshing = false
        }
    }

    /**
     * 하단 네비게이션 설정
     */
    private fun setupNavigation() {
        with(navigationBinding) {
            navWeather.setOnClickListener {
                it.findNavController().navigate(R.id.action_weatherFragment_self)
            }
            navSetting.setOnClickListener {
                it.findNavController().navigate(R.id.action_weatherFragment_to_settingFragment)
            }
            navFindust.setOnClickListener {
                it.findNavController().navigate(R.id.action_weatherFragment_to_finedustFragment)
            }
        }
    }

    /**
     * 자식 프래그먼트 설정
     */
    private fun setupChildFragments(savedInstanceState: Bundle?) {
        if (savedInstanceState == null) {
            childFragmentManager.beginTransaction()
                .replace(R.id.weather_current_container, CurrentWeatherFragment())
                .replace(R.id.weather_hourly_forecast_fragment, WeatherHourlyForecastFragment())
                .replace(R.id.weather_daily_container, WeatherDailyFragment())
                .replace(R.id.weather_additional_container, AdditionalWeatherFragment())
                .commit()
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
        _navigationBinding = null
    }
}