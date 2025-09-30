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
import com.example.kh_studyprojects_weatherapp.presentation.weather.current.WeatherCurrentFragment
import com.example.kh_studyprojects_weatherapp.presentation.weather.daily.WeatherDailyFragment
import com.example.kh_studyprojects_weatherapp.presentation.weather.hourly.WeatherHourlyForecastFragment
import com.example.kh_studyprojects_weatherapp.presentation.weather.additional.WeatherAdditionalFragment
import dagger.hilt.android.AndroidEntryPoint
import androidx.lifecycle.lifecycleScope
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.launch
import kotlinx.coroutines.delay
import kotlinx.coroutines.cancel

/**
 * 날씨 메인 프래그먼트 화면입니다.
 * @since 2024.01.01
 * @version 1.0
 */
@AndroidEntryPoint
class WeatherFragment : Fragment() {

    private var _binding: WeatherFragmentBinding? = null
    private val binding: WeatherFragmentBinding
        get() = _binding ?: throw IllegalStateException("Fragment binding is accessed before onCreateView or after onDestroyView")

    private var _navigationBinding: LayoutNavigationBottomBinding? = null
    private val navigationBinding: LayoutNavigationBottomBinding
        get() = _navigationBinding ?: throw IllegalStateException("Navigation binding is accessed before onCreateView or after onDestroyView")

    private val viewModel: WeatherViewModel by activityViewModels()

    private lateinit var loadingOverlay: View

    // 자식 Fragment 캐시
    private var cachedCurrentFragment: WeatherCurrentFragment? = null
    private var cachedDailyFragment: WeatherDailyFragment? = null
    private var cachedHourlyFragment: WeatherHourlyForecastFragment? = null
    private var cachedAdditionalFragment: WeatherAdditionalFragment? = null

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = WeatherFragmentBinding.inflate(inflater, container, false)

        // 포함된 하단 내비게이션 레이아웃을 바인딩
        _navigationBinding = LayoutNavigationBottomBinding.bind(binding.includedNavigationBottom.root)

        // 로딩 오버레이 뷰를 캐싱
        loadingOverlay = binding.root.findViewById(R.id.loadingOverlay)

        // 하단 네비게이션 동작을 설정
        setupNavigation()
        // 최초 생성 시 자식 프래그먼트를 붙임
        setupChildFragments(savedInstanceState)

        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        // 사용자가 당겨서 새로고침할 때 데이터를 갱신
        binding.swipeRefreshLayout.setOnRefreshListener {
            refreshWeatherData()
        }

        // 최초 진입 시에만 오버레이를 표시
        if (!viewModel.hasShownInitialOverlay.value) {
            loadingOverlay.visibility = View.VISIBLE
        }

        // 공유 ViewModel에서 새로고침 상태를 관찰
        viewLifecycleOwner.lifecycleScope.launch {
            viewModel.isRefreshing.collect { isRefreshing ->
                binding.swipeRefreshLayout.isRefreshing = isRefreshing
            }
        }

        // 초기 데이터 갱신을 즉시 실행
        viewLifecycleOwner.lifecycleScope.launch {
            refreshWeatherData()
        }

        // 모든 섹션의 데이터 준비 완료를 확인
        observeInitialLoadingCompletion()
        
        // 초기 오버레이 표시 여부를 추적
        viewLifecycleOwner.lifecycleScope.launch {
            viewModel.hasShownInitialOverlay.collect { hasShown ->
                if (hasShown) {
                    loadingOverlay.visibility = View.GONE
                }
            }
        }
    }

    private fun observeInitialLoadingCompletion() {
        // 상태 확인 전에 대기 중인 프래그먼트 트랜잭션을 처리
        childFragmentManager.executePendingTransactions()

        // 캐시된 Fragment가 없으면 조회 후 캐싱
        val current = cachedCurrentFragment ?: (childFragmentManager
            .findFragmentById(R.id.weather_current_container) as? WeatherCurrentFragment)
            ?.also { cachedCurrentFragment = it }

        val daily = cachedDailyFragment ?: (childFragmentManager
            .findFragmentById(R.id.weather_daily_container) as? WeatherDailyFragment)
            ?.also { cachedDailyFragment = it }

        val hourly = cachedHourlyFragment ?: (childFragmentManager
            .findFragmentById(R.id.weather_hourly_forecast_fragment) as? WeatherHourlyForecastFragment)
            ?.also { cachedHourlyFragment = it }

        if (current == null || daily == null || hourly == null) {
            // 프래그먼트가 준비되지 않았으면 잠시 후 다시 시도
            viewLifecycleOwner.lifecycleScope.launch {
                delay(100)
                observeInitialLoadingCompletion()
            }
            return
        }

        // 각 자식 ViewModel의 상태를 관찰
        viewLifecycleOwner.lifecycleScope.launch {
            val currentReady = current.viewModelInstance.uiState.map {
                it is com.example.kh_studyprojects_weatherapp.presentation.common.base.UiState.Success
            }
            val dailyReady = daily.viewModelInstance.weatherItems.map { it.isNotEmpty() }
            val hourlyReady = hourly.viewModelInstance.hourlyForecastItems.map { it.isNotEmpty() }

            combine(currentReady, dailyReady, hourlyReady) { c, d, h -> c && d && h }
                .collect { allReady ->
                    if (allReady) {
                        loadingOverlay.visibility = View.GONE
                        viewModel.markInitialOverlayShown()   // 초기 오버레이가 표시되었음을 기록
                        this.cancel()
                    }
                }
        }
    }

    /**
     * 모든 섹션의 날씨 데이터를 새로고침합니다.
     */
    private fun refreshWeatherData() {
        childFragmentManager.executePendingTransactions()

        // 캐시된 Fragment 사용 (없으면 조회 후 캐싱)
        val current = cachedCurrentFragment ?: (childFragmentManager
            .findFragmentById(R.id.weather_current_container) as? WeatherCurrentFragment)
            ?.also { cachedCurrentFragment = it }

        val daily = cachedDailyFragment ?: (childFragmentManager
            .findFragmentById(R.id.weather_daily_container) as? WeatherDailyFragment)
            ?.also { cachedDailyFragment = it }

        val hourly = cachedHourlyFragment ?: (childFragmentManager
            .findFragmentById(R.id.weather_hourly_forecast_fragment) as? WeatherHourlyForecastFragment)
            ?.also { cachedHourlyFragment = it }

        val addi = cachedAdditionalFragment ?: (childFragmentManager
            .findFragmentById(R.id.weather_additional_container) as? WeatherAdditionalFragment)
            ?.also { cachedAdditionalFragment = it }

        current?.viewModelInstance?.refreshWeatherData()
        daily?.viewModelInstance?.refreshWeatherData()
        hourly?.viewModelInstance?.refreshWeatherData()
        addi?.viewModelInstance?.refreshWeatherData()


        // 각 섹션의 로딩 상태를 합쳐 새로고침 인디케이터를 제어
        viewLifecycleOwner.lifecycleScope.launch {
            combine(
                current?.viewModelInstance?.uiState?.map { it is com.example.kh_studyprojects_weatherapp.presentation.common.base.UiState.Loading }
                    ?: kotlinx.coroutines.flow.flowOf(false),
                daily?.viewModelInstance?.isLoading ?: kotlinx.coroutines.flow.flowOf(false),
                hourly?.viewModelInstance?.isLoading ?: kotlinx.coroutines.flow.flowOf(false),
                addi?.viewModelInstance?.uiState?.map { it is com.example.kh_studyprojects_weatherapp.presentation.common.base.UiState.Loading }
                    ?: kotlinx.coroutines.flow.flowOf(false),
            ) { arr -> arr.any { it } }
             .collect { anyLoading ->
                binding.swipeRefreshLayout.isRefreshing = anyLoading
             }
        }
    }

    /**
     * 하단 네비게이션 버튼을 설정합니다.
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
     * 최초 생성 시 자식 프래그먼트를 추가합니다.
     */
    private fun setupChildFragments(savedInstanceState: Bundle?) {
        if (savedInstanceState == null) {
            childFragmentManager.beginTransaction()
                .replace(R.id.weather_current_container, WeatherCurrentFragment())
                .replace(R.id.weather_hourly_forecast_fragment, WeatherHourlyForecastFragment())
                .replace(R.id.weather_daily_container, WeatherDailyFragment())
                .replace(R.id.weather_additional_container, WeatherAdditionalFragment())
                .commit()
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
        _navigationBinding = null
        // 캐시 초기화
        cachedCurrentFragment = null
        cachedDailyFragment = null
        cachedHourlyFragment = null
        cachedAdditionalFragment = null
    }
}
