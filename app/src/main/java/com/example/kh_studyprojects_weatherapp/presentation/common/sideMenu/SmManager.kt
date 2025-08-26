package com.example.kh_studyprojects_weatherapp.presentation.common.sideMenu

import android.content.Context
import android.view.View
import androidx.lifecycle.LifecycleCoroutineScope
import com.example.kh_studyprojects_weatherapp.databinding.ActivityMainBinding
import com.example.kh_studyprojects_weatherapp.presentation.common.sideMenu.adapter.SmFavoriteLocationAdapter
import com.example.kh_studyprojects_weatherapp.presentation.common.sideMenu.adapter.SmSearchResultAdapter
import com.example.kh_studyprojects_weatherapp.domain.model.location.FavoriteLocation
import com.example.kh_studyprojects_weatherapp.domain.model.weather.WeatherCommon
import com.example.kh_studyprojects_weatherapp.presentation.weather.WeatherFragment
import com.example.kh_studyprojects_weatherapp.presentation.weather.current.CurrentWeatherFragment
import com.example.kh_studyprojects_weatherapp.presentation.weather.daily.WeatherDailyFragment
import com.example.kh_studyprojects_weatherapp.presentation.weather.hourly.WeatherHourlyForecastFragment
import com.example.kh_studyprojects_weatherapp.presentation.weather.additional.AdditionalWeatherFragment
import androidx.fragment.app.FragmentActivity
import androidx.navigation.NavController
import android.widget.Toast
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import android.view.ViewGroup
import android.app.AlertDialog

/**
 * 사이드메뉴 전체를 관리하는 Manager 클래스
 * 
 * 검색, 즐겨찾기, 애니메이션 등의 기능을 담당하는 하위 Manager들을 조율합니다.
 * MainActivity와 하위 Manager들 간의 중재자 역할을 수행하며, 전체적인 사이드메뉴 동작을 제어합니다.
 * 
 * 주요 기능:
 * - 하위 Manager들의 초기화 및 의존성 설정
 * - 윈도우 인셋 처리
 * - 메뉴 아이템 클릭 리스너 설정
 * - 현재 위치 날씨 정보 업데이트
 * - 즐겨찾기 지역 클릭/삭제 처리
 * 
 * @author 김효동
 * @since 2025.08.26
 * @version 1.0
 */
class SmManager(
    private val context: Context,
    private val binding: ActivityMainBinding,
    private val lifecycleScope: LifecycleCoroutineScope,
    private val favoriteLocationAdapter: SmFavoriteLocationAdapter,
    private val searchResultAdapter: SmSearchResultAdapter,
    private val navController: NavController,
    private val activity: FragmentActivity
) {
    private lateinit var sideMenuSearchManager: SmSearchManager
    private lateinit var sideMenuFavoriteManager: SmFavoriteManager
    private lateinit var sideMenuAnimationManager: SmAnimationManager

    // 현재 날씨 데이터 콜백
    private var onWeatherDataUpdated: ((Map<String, Any>) -> Unit)? = null

    fun setupSideMenu() {
        // Manager 클래스들 먼저 초기화
        initializeManagers()
        
        // 사이드메뉴의 윈도우 인셋 처리 설정
        setupWindowInsets(binding.sideMenuContent.root, false, true)

        // 즐겨찾기 지역 RecyclerView 설정
        sideMenuFavoriteManager.setupFavoriteLocationsRecyclerView()

        // 검색 결과 RecyclerView 설정
        sideMenuSearchManager.setupSearchResultsRecyclerView()

        // 메뉴 아이템 클릭 리스너 설정
        setupMenuClickListeners()

        // 현재 날씨 데이터 구독
        setupCurrentWeatherObserver()
    }

    private fun initializeManagers() {
        sideMenuSearchManager = SmSearchManager(context, binding, lifecycleScope, searchResultAdapter)
        sideMenuFavoriteManager = SmFavoriteManager(context, binding, favoriteLocationAdapter)
        sideMenuAnimationManager = SmAnimationManager(binding)
        
        // Manager 간 의존성 설정
        sideMenuSearchManager.setAnimationManager(sideMenuAnimationManager)
    }

    /**
     * 윈도우 인셋 처리 공통 함수
     */
    private fun setupWindowInsets(view: View, useTopMargin: Boolean, useBottomMargin: Boolean) {
        ViewCompat.setOnApplyWindowInsetsListener(view) { _, windowInsets ->
            // 현재 기기의 네비게이션 바와 상태 바의 크기 정보를 가져옴
            val navigationBars = windowInsets.getInsets(WindowInsetsCompat.Type.navigationBars())
            val statusBars = windowInsets.getInsets(WindowInsetsCompat.Type.statusBars())

            // 레이아웃 파라미터를 가져와서 마진 설정 (안전한 캐스팅)
            val params = view.layoutParams
            if (params is ViewGroup.MarginLayoutParams) {
                // 설정에 따라 마진 적용
                if (useBottomMargin) {
                    params.bottomMargin = navigationBars.bottom
                }
                if (useTopMargin) {
                    params.topMargin = statusBars.top
                }
                // 변경된 레이아웃 파라미터 적용
                view.layoutParams = params
            }

            // 인셋이 처리되었음을 시스템에 알림
            WindowInsetsCompat.CONSUMED
        }
    }

    /**
     * 현재 날씨 데이터 구독
     */
    private fun setupCurrentWeatherObserver() {
        // 콜백 설정
        onWeatherDataUpdated = { weatherData ->
            updateCurrentLocationInSideMenu(weatherData)
        }
    }

    /**
     * 사이드메뉴의 현재 위치 아이템 업데이트
     */
    private fun updateCurrentLocationInSideMenu(weatherData: Map<String, Any>) {
        try {
            // 위치 정보 표시
            weatherData["location"]?.let { location ->
                val address = location.toString()
                val thoroughfare = address.split(" ").lastOrNull() ?: address
                binding.sideMenuContent.tvCurrentLocationName.text = thoroughfare
                binding.sideMenuContent.tvCurrentLocationAddress.text = address
            }

            // 현재 날씨 데이터 처리
            val current = weatherData["current"] as? Map<*, *>
            current?.let {
                // 현재 온도
                val temperature = it["temperature_2m"] as? Double
                binding.sideMenuContent.tvCurrentTemperature.text = "${temperature?.toInt()}°"

                // 날씨 아이콘
                val weatherCode = (it["weather_code"] as? Number)?.toInt() ?: 0
                binding.sideMenuContent.ivCurrentWeatherIcon.setImageResource(WeatherCommon.getWeatherIcon(weatherCode))
            }
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }

    /**
     * 메뉴 아이템 클릭 리스너 설정
     */
    private fun setupMenuClickListeners() {
        // 현재 위치 클릭
        binding.sideMenuContent.llCurrentLocation.setOnClickListener {
            handleCurrentLocationClick()
        }

        // 앱 정보 메뉴 클릭
        binding.sideMenuContent.llAbout.setOnClickListener {
            showAboutDialog()
        }

        // 편집 버튼 클릭
        binding.sideMenuContent.llEditFavorite.setOnClickListener {
            sideMenuFavoriteManager.handleEditFavoriteClick()
        }

        // 검색창 포커스 리스너 설정
        sideMenuSearchManager.setupSearchFocusListeners()
    }

    /**
     * 현재 위치 클릭 처리
     */
    private fun handleCurrentLocationClick() {
        // 현재 위치의 날씨 정보를 새로 가져와서 표시
        refreshCurrentLocationWeather()
    }

    /**
     * 현재 위치 날씨 정보 새로고침
     */
    private fun refreshCurrentLocationWeather() {
        // 현재 활성화된 프래그먼트가 WeatherFragment인지 확인
        val currentFragment = navController.currentDestination?.label?.toString()

        if (currentFragment == "weatherFragment") {
            // WeatherFragment의 하위 프래그먼트들에 새로고침 신호 전달
            val weatherFragment = activity.supportFragmentManager
                .findFragmentById(com.example.kh_studyprojects_weatherapp.R.id.fragmentContainerView)
                ?.childFragmentManager
                ?.fragments
                ?.firstOrNull { it is WeatherFragment }

            // 각 하위 프래그먼트의 ViewModel에 새로고침 신호 전달
            weatherFragment?.childFragmentManager?.fragments?.forEach { fragment ->
                when (fragment) {
                    is CurrentWeatherFragment -> {
                        fragment.refreshWeatherData()
                    }
                    is WeatherHourlyForecastFragment -> {
                        fragment.refreshWeatherData()
                    }
                    is WeatherDailyFragment -> {
                        fragment.refreshWeatherData()
                    }
                    is AdditionalWeatherFragment -> {
                        fragment.refreshWeatherData()
                    }
                }
            }

            Toast.makeText(
                context,
                "현재 위치의 날씨 정보를 새로고침합니다.",
                Toast.LENGTH_SHORT
            ).show()
        } else {
            // WeatherFragment가 아닌 경우 WeatherFragment로 이동
            navController.navigate(com.example.kh_studyprojects_weatherapp.R.id.weatherFragment)
            Toast.makeText(
                context,
                "날씨 화면으로 이동합니다.",
                Toast.LENGTH_SHORT
            ).show()
        }
    }

    /**
     * 앱 정보 다이얼로그 표시
     */
    private fun showAboutDialog() {
        AlertDialog.Builder(context)
            .setTitle("앱 정보")
            .setMessage("날씨 앱 v1.0\n\n날씨 정보와 미세먼지 정보를 제공하는 앱입니다.")
            .setPositiveButton("확인") { dialog, _ ->
                dialog.dismiss()
            }
            .show()
    }

    /**
     * 현재 날씨 데이터 업데이트 (Fragment에서 호출)
     */
    fun updateCurrentWeatherData(weatherData: Map<String, Any>) {
        onWeatherDataUpdated?.invoke(weatherData)
    }

    /**
     * 즐겨찾기 지역 클릭 처리 (MainActivity에서 호출)
     */
    fun handleFavoriteLocationClick(location: FavoriteLocation) {
        sideMenuFavoriteManager.handleFavoriteLocationClick(location)
    }

    /**
     * 즐겨찾기 지역 삭제 처리 (MainActivity에서 호출)
     */
    fun handleFavoriteLocationDelete(location: FavoriteLocation) {
        sideMenuFavoriteManager.handleFavoriteLocationDelete(location)
    }
}
