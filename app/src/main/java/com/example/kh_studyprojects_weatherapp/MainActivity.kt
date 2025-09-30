package com.example.kh_studyprojects_weatherapp

import android.Manifest
import android.content.pm.PackageManager
import android.graphics.Color
import android.os.Bundle
import android.view.View
import android.widget.Toast
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.app.AppCompatDelegate
import androidx.core.content.ContextCompat
import androidx.core.view.GravityCompat
import androidx.core.view.WindowCompat
import androidx.core.view.WindowInsetsCompat
import androidx.core.view.ViewCompat
import androidx.drawerlayout.widget.DrawerLayout
import androidx.lifecycle.lifecycleScope
import androidx.navigation.NavController
import androidx.navigation.fragment.NavHostFragment
import com.example.kh_studyprojects_weatherapp.R
import com.example.kh_studyprojects_weatherapp.databinding.ActivityMainBinding
import com.example.kh_studyprojects_weatherapp.domain.repository.common.geocoding.GeocodingRepository
import com.example.kh_studyprojects_weatherapp.domain.repository.common.sidemenu.SmFavoriteLocationRepository
import com.example.kh_studyprojects_weatherapp.domain.repository.weather.WeatherRepository
import com.example.kh_studyprojects_weatherapp.presentation.common.location.LocationManager
import com.example.kh_studyprojects_weatherapp.presentation.common.location.LocationSelectionStore
import com.example.kh_studyprojects_weatherapp.presentation.common.sidemenu.SmManager
import com.example.kh_studyprojects_weatherapp.presentation.common.sidemenu.adapter.SmFavoriteLocationAdapter
import com.example.kh_studyprojects_weatherapp.presentation.common.sidemenu.adapter.SmSearchResultAdapter
import dagger.hilt.android.AndroidEntryPoint
import javax.inject.Inject

import com.example.kh_studyprojects_weatherapp.domain.model.weather.WeatherData
@AndroidEntryPoint
/**
 * MainActivity
 * - 네비게이션 호스트와 사이드메뉴(SmManager)를 초기화하고,
 *   위치 권한 흐름 및 공통 UI 설정을 담당합니다.
 */
class MainActivity : AppCompatActivity() {

    private lateinit var binding: ActivityMainBinding
    private lateinit var navController: NavController
    private lateinit var drawerLayout: DrawerLayout
    private lateinit var sideMenuManager: SmManager

    @Inject lateinit var favoriteLocationRepository: SmFavoriteLocationRepository
    @Inject lateinit var weatherRepository: WeatherRepository
    @Inject lateinit var geocodingRepository: GeocodingRepository
    @Inject lateinit var locationSelectionStore: LocationSelectionStore
    @Inject lateinit var locationManager: LocationManager

    /** 위치 권한 요청 콜백 등록 (FINE/COARSE) */
    private val locationPermissionRequest = registerForActivityResult(
        ActivityResultContracts.RequestMultiplePermissions()
    ) { permissions ->
        when {
            permissions.getOrDefault(Manifest.permission.ACCESS_FINE_LOCATION, false) ->
                Toast.makeText(this, "정확한 위치 권한이 허용되었습니다.", Toast.LENGTH_SHORT).show()
            permissions.getOrDefault(Manifest.permission.ACCESS_COARSE_LOCATION, false) ->
                Toast.makeText(this, "대략적 위치 권한이 허용되었습니다.", Toast.LENGTH_SHORT).show()
            else -> Toast.makeText(this, "위치 권한이 거부되었습니다.", Toast.LENGTH_SHORT).show()
        }
    }

    /**
     * 액티비티 진입점: E2E UI 세팅, 뷰 바인딩, NavController 준비, 사이드메뉴 구성
     */
    override fun onCreate(savedInstanceState: Bundle?) {
        AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_YES)
        super.onCreate(savedInstanceState)

        // 투명 상태바/내비게이션바 설정
        WindowCompat.setDecorFitsSystemWindows(window, false)
        window.apply {
            statusBarColor = Color.TRANSPARENT
            navigationBarColor = Color.WHITE
            decorView.systemUiVisibility = View.SYSTEM_UI_FLAG_LIGHT_NAVIGATION_BAR
        }

        // 뷰 바인딩 설정
        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)

        // 윈도우 인셋 처리 설정
        ViewCompat.setOnApplyWindowInsetsListener(binding.fragmentContainerView) { view, windowInsets ->
            val navigationBars = windowInsets.getInsets(WindowInsetsCompat.Type.navigationBars())
            val statusBars = windowInsets.getInsets(WindowInsetsCompat.Type.statusBars())
            val params = view.layoutParams as androidx.constraintlayout.widget.ConstraintLayout.LayoutParams
            params.bottomMargin = navigationBars.bottom
            params.topMargin = statusBars.top
            view.layoutParams = params
            WindowInsetsCompat.CONSUMED
        }

        // 드로어 레이아웃 참조
        drawerLayout = binding.drawerLayout

        // NavController 초기화
        val navHost = supportFragmentManager.findFragmentById(R.id.fragmentContainerView) as NavHostFragment
        navController = navHost.navController

        // 사이드메뉴 구성
        setupSideMenu()
    }

    /** 위치 권한이 없으면 즉시 요청 */
    private fun checkLocationPermission() {
        if (!hasLocationPermission()) {
            requestLocationPermission()
        }
    }

    /** 위치 권한 보유 여부 */
    private fun hasLocationPermission(): Boolean {
        return ContextCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED ||
            ContextCompat.checkSelfPermission(this, Manifest.permission.ACCESS_COARSE_LOCATION) == PackageManager.PERMISSION_GRANTED
    }

    /** 위치 권한 요청 트리거 */
    private fun requestLocationPermission() {
        locationPermissionRequest.launch(
            arrayOf(
                Manifest.permission.ACCESS_FINE_LOCATION,
                Manifest.permission.ACCESS_COARSE_LOCATION
            )
        )
    }

    /** 드로어 열기 */
    fun openDrawer() {
        if (!drawerLayout.isDrawerOpen(GravityCompat.START)) drawerLayout.openDrawer(GravityCompat.START)
    }

    /** 드로어 닫기 */
    fun closeDrawer() {
        if (drawerLayout.isDrawerOpen(GravityCompat.START)) drawerLayout.closeDrawer(GravityCompat.START)
    }

    /**
     * 현재 날씨 데이터가 갱신되었을 때(프래그먼트에서 콜백)
     * 사이드메뉴 헤더 등의 표시를 업데이트하기 위해 SmManager로 전달
     */
    fun updateCurrentWeatherData(weatherData: WeatherData) {
        sideMenuManager.updateCurrentWeatherData(weatherData)
    }

    /**
     * 사이드메뉴 구성: 어댑터 생성/설정, SmManager 초기화, 클릭 콜백 연결, 권한 확인
     */
    private fun setupSideMenu() {
        val favoriteLocationAdapter = SmFavoriteLocationAdapter(
            onLocationClick = { },
            onDeleteClick = { }
        )
        // 어댑터에 WeatherRepository 주입
        favoriteLocationAdapter.setWeatherRepository(weatherRepository)

        // 검색 결과 어댑터 생성
        val searchResultAdapter = SmSearchResultAdapter()

        // SmManager 초기화
        sideMenuManager = SmManager(
            context = this,
            binding = binding,
            lifecycleScope = lifecycleScope,
            favoriteLocationAdapter = favoriteLocationAdapter,
            searchResultAdapter = searchResultAdapter,
            navController = navController,
            activity = this,
            favoriteLocationRepository = favoriteLocationRepository,
            weatherRepository = weatherRepository,
            geocodingRepository = geocodingRepository,
            locationSelectionStore = locationSelectionStore,
            locationManager = locationManager
        )
        
        // 사이드메뉴 초기화
        sideMenuManager.setupSideMenu()
        
        // 즐겨찾기 클릭 콜백 설정
        favoriteLocationAdapter.updateOnLocationClick { loc ->
            sideMenuManager.handleFavoriteLocationClick(loc)
        }
        
        // 즐겨찾기 삭제 콜백 설정
        favoriteLocationAdapter.updateOnDeleteClick { loc ->
            sideMenuManager.handleFavoriteLocationDelete(loc)
        }
        // 위치 권한 확인 및 요청
        checkLocationPermission()
    }
}

