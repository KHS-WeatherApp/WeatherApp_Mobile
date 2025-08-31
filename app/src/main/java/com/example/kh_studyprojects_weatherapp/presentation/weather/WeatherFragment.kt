package com.example.kh_studyprojects_weatherapp.presentation.weather

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.fragment.app.activityViewModels
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
import com.example.kh_studyprojects_weatherapp.presentation.weather.additional.AdditionalWeatherViewModel
import com.example.kh_studyprojects_weatherapp.presentation.weather.current.CurrentWeatherViewModel
import com.example.kh_studyprojects_weatherapp.presentation.weather.daily.WeatherDailyViewModel
import com.example.kh_studyprojects_weatherapp.presentation.weather.hourly.WeatherHourlyForecastViewModel
import kotlinx.coroutines.flow.collect
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.launch
import kotlinx.coroutines.delay
import kotlinx.coroutines.async
import kotlinx.coroutines.awaitAll
import kotlinx.coroutines.cancel

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

    private val viewModel: WeatherViewModel by activityViewModels()

    private lateinit var loadingOverlay: View

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = WeatherFragmentBinding.inflate(inflater, container, false)

        // include된 네비게이션 바인딩
        _navigationBinding = LayoutNavigationBottomBinding.bind(binding.includedNavigationBottom.root)

        // 로딩 오버레이 뷰 참조
        loadingOverlay = binding.root.findViewById(R.id.loadingOverlay)

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

        // 초기 진입 로딩 오버레이 표시 (한 번만 보여줌)
        if (!viewModel.hasShownInitialOverlay.value) {
            loadingOverlay.visibility = View.VISIBLE
        }

        // ViewModel의 갱신 상태 관찰
        viewLifecycleOwner.lifecycleScope.launch {
            viewModel.isRefreshing.collect { isRefreshing ->
                binding.swipeRefreshLayout.isRefreshing = isRefreshing
            }
        }

        // 초기 데이터 로드 트리거
        viewLifecycleOwner.lifecycleScope.launch {
            refreshWeatherData()
        }

        // 모든 섹션 데이터가 준비되면 로딩 오버레이 숨김
        observeInitialLoadingCompletion()
        
        // 초기 로딩 오버레이 상태 관찰
        viewLifecycleOwner.lifecycleScope.launch {
            viewModel.hasShownInitialOverlay.collect { hasShown ->
                if (hasShown) {
                    loadingOverlay.visibility = View.GONE
                }
            }
        }
    }

    private fun observeInitialLoadingCompletion() {
        // 자식 프래그먼트들이 트랜잭션을 마치도록 보장
        childFragmentManager.executePendingTransactions()

        val current = childFragmentManager
            .findFragmentById(R.id.weather_current_container) as? CurrentWeatherFragment
        val daily = childFragmentManager
            .findFragmentById(R.id.weather_daily_container) as? WeatherDailyFragment
        val hourly = childFragmentManager
            .findFragmentById(R.id.weather_hourly_forecast_fragment) as? WeatherHourlyForecastFragment

        if (current == null || daily == null || hourly == null) {
            // 프래그먼트가 아직 준비되지 않은 경우 조금 후 다시 시도
            viewLifecycleOwner.lifecycleScope.launch {
                delay(100)
                observeInitialLoadingCompletion()
            }
            return
        }

        viewLifecycleOwner.lifecycleScope.launch {
            // 각 뷰모델의 데이터 준비 여부를 Boolean Flow로 변환
            val currentReady = current.viewModelInstance.weatherState.map { it.isNotEmpty() }
            val dailyReady = daily.viewModelInstance.weatherItems.map { it.isNotEmpty() }
            val hourlyReady = hourly.viewModelInstance.hourlyForecastItems.map { it.isNotEmpty() }

            combine(currentReady, dailyReady, hourlyReady) { c, d, h -> c && d && h }
                .collect { allReady ->
                    if (allReady) {
                        loadingOverlay.visibility = View.GONE
                        viewModel.markInitialOverlayShown()   // 이제부터 다시는 안 보이게 플래그 세팅
                        // 관찰 종료를 위해 this coroutine 취소
                        this.cancel()
                    }
                }
        }
    }

    /**
     * 날씨 데이터 새로고침
     */
    private fun refreshWeatherData() {
        childFragmentManager.executePendingTransactions()

        val current = (childFragmentManager.findFragmentById(R.id.weather_current_container) as? CurrentWeatherFragment)?.viewModelInstance
        val daily   = (childFragmentManager.findFragmentById(R.id.weather_daily_container) as? WeatherDailyFragment)?.viewModelInstance
        val hourly  = (childFragmentManager.findFragmentById(R.id.weather_hourly_forecast_fragment) as? WeatherHourlyForecastFragment)?.viewModelInstance
        val addi    = (childFragmentManager.findFragmentById(R.id.weather_additional_container) as? AdditionalWeatherFragment)?.viewModelInstance

        // 1) 실제 새로고침 트리거
        (current as? CurrentWeatherViewModel)?.refreshWeatherData()
        (daily   as? WeatherDailyViewModel)?.refreshWeatherData()
        (hourly  as? WeatherHourlyForecastViewModel)?.refreshWeatherData()
        (addi    as? AdditionalWeatherViewModel)?.refreshWeatherData()


        // 2) 로딩 묶어서 스피너/오버레이 제어
        viewLifecycleOwner.lifecycleScope.launch {
            combine(
                current?.isLoading ?: kotlinx.coroutines.flow.flowOf(false),
                daily?.isLoading ?: kotlinx.coroutines.flow.flowOf(false),
                hourly?.isLoading ?: kotlinx.coroutines.flow.flowOf(false),
                addi?.isLoading ?: kotlinx.coroutines.flow.flowOf(false),
            ) { arr -> arr.any { it } }
             .collect { anyLoading ->
                binding.swipeRefreshLayout.isRefreshing = anyLoading
             }
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