package com.example.kh_studyprojects_weatherapp.presentation.weather

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.fragment.app.activityViewModels
import com.example.kh_studyprojects_weatherapp.R
import com.example.kh_studyprojects_weatherapp.databinding.WeatherFragmentBinding
import com.example.kh_studyprojects_weatherapp.presentation.common.base.BaseNavigationFragment
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
class WeatherFragment : BaseNavigationFragment() {

    private var _binding: WeatherFragmentBinding? = null
    private val binding: WeatherFragmentBinding
        get() = _binding ?: throw IllegalStateException("Fragment binding is accessed before onCreateView or after onDestroyView")

    private val viewModel: WeatherViewModel by activityViewModels()

    private lateinit var loadingOverlay: View

    // 자식 Fragment 캐시
    private var cachedCurrentFragment: WeatherCurrentFragment? = null
    private var cachedDailyFragment: WeatherDailyFragment? = null
    private var cachedHourlyFragment: WeatherHourlyForecastFragment? = null
    private var cachedAdditionalFragment: WeatherAdditionalFragment? = null

    /**
     * Fragment를 캐시에서 가져오거나, 없으면 childFragmentManager에서 찾아서 반환합니다.
     *
     * @param T Fragment 타입
     * @param containerId Fragment가 위치한 컨테이너 ID
     * @param cachedFragment 캐시된 Fragment (nullable)
     * @param updateCache 캐시를 업데이트하는 람다 함수
     * @return 찾은 Fragment 또는 null
     */
    private inline fun <reified T : Fragment> getOrCacheFragment(
        containerId: Int,
        cachedFragment: T?,
        updateCache: (T) -> Unit
    ): T? {
        return cachedFragment ?: (childFragmentManager.findFragmentById(containerId) as? T)?.also(updateCache)
    }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = WeatherFragmentBinding.inflate(inflater, container, false)

        // 하단 네비게이션 바인딩 초기화 (BaseNavigationFragment에서 제공)
        setupNavigationBinding(binding.root)

        // 로딩 오버레이 뷰를 캐싱
        loadingOverlay = binding.root.findViewById(R.id.loadingOverlay)

        // 네비게이션 클릭 리스너 설정
        setupBottomNavigation(
            weatherDestination = R.id.weatherFragment,
            settingDestination = R.id.settingFragment,
            finedustDestination = R.id.finedustFragment
        )

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
        val current = getOrCacheFragment(R.id.weather_current_container, cachedCurrentFragment) { cachedCurrentFragment = it }
        val daily = getOrCacheFragment(R.id.weather_daily_container, cachedDailyFragment) { cachedDailyFragment = it }
        val hourly = getOrCacheFragment(R.id.weather_hourly_forecast_fragment, cachedHourlyFragment) { cachedHourlyFragment = it }

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
        val current = getOrCacheFragment(R.id.weather_current_container, cachedCurrentFragment) { cachedCurrentFragment = it }
        val daily = getOrCacheFragment(R.id.weather_daily_container, cachedDailyFragment) { cachedDailyFragment = it }
        val hourly = getOrCacheFragment(R.id.weather_hourly_forecast_fragment, cachedHourlyFragment) { cachedHourlyFragment = it }
        val addi = getOrCacheFragment(R.id.weather_additional_container, cachedAdditionalFragment) { cachedAdditionalFragment = it }

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
        // 캐시 초기화
        cachedCurrentFragment = null
        cachedDailyFragment = null
        cachedHourlyFragment = null
        cachedAdditionalFragment = null
    }
}
