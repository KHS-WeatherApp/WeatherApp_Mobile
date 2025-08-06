package com.example.kh_studyprojects_weatherapp.presentation

import android.Manifest
import android.content.pm.PackageManager
import android.os.Bundle
import android.view.View
import android.view.WindowInsetsController
import android.widget.Toast
import androidx.activity.enableEdgeToEdge
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.app.AppCompatDelegate
import androidx.core.view.ViewCompat
import androidx.core.view.WindowCompat
import androidx.core.view.WindowInsetsCompat
import androidx.core.view.WindowInsetsControllerCompat
import androidx.core.content.ContextCompat
import androidx.core.view.GravityCompat
import androidx.drawerlayout.widget.DrawerLayout
import androidx.navigation.NavController
import androidx.navigation.fragment.NavHostFragment
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.lifecycle.lifecycleScope
import com.example.kh_studyprojects_weatherapp.R
import com.example.kh_studyprojects_weatherapp.databinding.ActivityMainBinding
import com.example.kh_studyprojects_weatherapp.domain.model.location.FavoriteLocation
import com.example.kh_studyprojects_weatherapp.domain.model.weather.WeatherCommon
import com.example.kh_studyprojects_weatherapp.presentation.weather.adapter.FavoriteLocationAdapter
import com.example.kh_studyprojects_weatherapp.presentation.weather.current.CurrentWeatherViewModel
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.launch
import javax.inject.Inject

// 사용하지 않는 변수, 변경된 파라미터 이름 등에 대한 컴파일러 경고를 억제합니다.
@AndroidEntryPoint
class MainActivity : AppCompatActivity() {

    // 뷰 바인딩을 위한 널 가능한(private) 변수를 선언합니다.
    private var _binding: ActivityMainBinding? = null
    // _binding 변수에 대한 널이 아닌 접근자를 제공합니다.
    private val binding get() = _binding!!
    
    // 네비게이션 컨트롤러
    private lateinit var navController: NavController
    // 드로어 레이아웃
    private lateinit var drawerLayout: DrawerLayout
    // 즐겨찾기 지역 어댑터
    private lateinit var favoriteLocationAdapter: FavoriteLocationAdapter
    
    // 현재 날씨 데이터 콜백
    private var onWeatherDataUpdated: ((Map<String, Any>) -> Unit)? = null

    /**
     * 위치 권한 요청을 위한 런처
     * ActivityResultContracts.RequestMultiplePermissions()를 사용하여 여러 권한을 한 번에 요청
     * 권한 요청 결과에 따라 다른 동작을 수행
     */
    private val locationPermissionRequest = registerForActivityResult(
        ActivityResultContracts.RequestMultiplePermissions()
    ) { permissions ->
        when {
            // 정확한 위치 권한이 승인된 경우 (GPS 사용 가능)
            permissions.getOrDefault(Manifest.permission.ACCESS_FINE_LOCATION, false) -> {
//                Toast.makeText(this, "정확한 위치 권한이 승인되었습니다.", Toast.LENGTH_SHORT).show()
            }
            // 대략적인 위치 권한이 승인된 경우 (네트워크 기반 위치)
            permissions.getOrDefault(Manifest.permission.ACCESS_COARSE_LOCATION, false) -> {
//                Toast.makeText(this, "대략적인 위치 권한이 승인되었습니다.", Toast.LENGTH_SHORT).show()
            }
            // 모든 위치 권한이 거부된 경우
            else -> {
//                Toast.makeText(this, "위치 권한이 거부되었습니다. 날씨 정보를 가져올 수 없습니다.", Toast.LENGTH_LONG).show()
            }
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        // 다크 모드 강제 적용
        AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_YES)
        
        super.onCreate(savedInstanceState)
        
        // Edge-to-edge 설정 - 앱이 시스템 UI 영역까지 그려지도록 함
        WindowCompat.setDecorFitsSystemWindows(window, false)
        
        // 상태바와 네비게이션 바의 아이콘 색상 설정
        val windowInsetsController = WindowCompat.getInsetsController(window, window.decorView)
        windowInsetsController.apply {
            isAppearanceLightStatusBars = false      // 상단 상태바 아이콘 색상 (false: 흰색)
            isAppearanceLightNavigationBars = true   // 하단 네비게이션 바 아이콘 색상 (true: 검정색)
        }

        // 상태바와 네비게이션 바의 배경 색상 설정
        window.apply {
            statusBarColor = android.graphics.Color.TRANSPARENT      // 상단 상태바 배경 투명
            navigationBarColor = android.graphics.Color.WHITE        // 하단 네비게이션 바 배경 흰색
            decorView.systemUiVisibility = View.SYSTEM_UI_FLAG_LIGHT_NAVIGATION_BAR  // 네비게이션 바 아이콘을 어두운 색상으로 설정
        }
        
        // 뷰 바인딩 초기화
        _binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)
        
        // 드로어 레이아웃 초기화
        drawerLayout = binding.drawerLayout
        
        // 네비게이션 컨트롤러 설정
        val navHostFragment = supportFragmentManager
            .findFragmentById(R.id.fragmentContainerView) as NavHostFragment
        navController = navHostFragment.navController
        
        // 사이드 메뉴 설정
        setupSideMenu()
        
        // 시스템 UI(상태 바, 네비게이션 바)와 앱 콘텐츠가 겹치지 않도록 여백 자동 조정
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.fragmentContainerView)) { view, windowInsets ->
            // 현재 기기의 네비게이션 바와 상태 바의 크기 정보를 가져옴
            val navigationBars = windowInsets.getInsets(WindowInsetsCompat.Type.navigationBars())
            val statusBars = windowInsets.getInsets(WindowInsetsCompat.Type.statusBars())
            
            // 레이아웃 파라미터를 가져와서 마진 설정
            val params = view.layoutParams as androidx.constraintlayout.widget.ConstraintLayout.LayoutParams
            // 하단 시스템 네비게이션 바 높이만큼 여백 설정
            params.bottomMargin = navigationBars.bottom
            // 상단 상태 바 높이만큼 여백 설정
            params.topMargin = statusBars.top
            // 변경된 레이아웃 파라미터 적용
            view.layoutParams = params
            
            // 인셋이 처리되었음을 시스템에 알림
            WindowInsetsCompat.CONSUMED
        }

        // 위치 권한 확인 및 요청
        checkLocationPermission()
    }

    /**
     * 위치 권한 상태를 확인하고 필요한 경우 권한을 요청하는 함수
     * 이미 권한이 있는 경우와 없는 경우를 구분하여 처리
     */
    private fun checkLocationPermission() {
        when {
            // 이미 권한이 있는 경우( 디버깅용 Toast 메세지)
            hasLocationPermission() -> {
//                Toast.makeText(this, "위치 권한이 이미 승인되어 있습니다.", Toast.LENGTH_SHORT).show()
            }
            // 권한이 없는 경우 요청
            else -> {
                requestLocationPermission()
            }
        }
    }

    /**
     * 현재 앱이 위치 권한을 가지고 있는지 확인하는 함수
     * ACCESS_FINE_LOCATION(정확한 위치) 또는 ACCESS_COARSE_LOCATION(대략적인 위치) 중 하나라도 있으면 true 반환
     */
    private fun hasLocationPermission(): Boolean {
        return ContextCompat.checkSelfPermission(
            this,
            Manifest.permission.ACCESS_FINE_LOCATION
        ) == PackageManager.PERMISSION_GRANTED ||
        ContextCompat.checkSelfPermission(
            this,
            Manifest.permission.ACCESS_COARSE_LOCATION
        ) == PackageManager.PERMISSION_GRANTED
    }

    /**
     * 위치 권한을 요청하는 함수
     * ACCESS_FINE_LOCATION과 ACCESS_COARSE_LOCATION 두 가지 권한을 동시에 요청
     */
    private fun requestLocationPermission() {
        locationPermissionRequest.launch(
            arrayOf(
                Manifest.permission.ACCESS_FINE_LOCATION,
                Manifest.permission.ACCESS_COARSE_LOCATION
            )
        )
    }
    
    /**
     * 사이드 메뉴 설정
     */
    private fun setupSideMenu() {
        // 즐겨찾기 지역 RecyclerView 설정
        setupFavoriteLocationsRecyclerView()
        
        // 메뉴 아이템 클릭 리스너 설정
        setupMenuClickListeners()
        
        // 현재 날씨 데이터 구독
        setupCurrentWeatherObserver()
        
        // 임시 즐겨찾기 데이터 추가 (테스트용)
        addTestFavoriteLocations()
    }
    
    /**
     * 즐겨찾기 지역 RecyclerView 설정
     */
    private fun setupFavoriteLocationsRecyclerView() {
        val recyclerView = binding.sideMenuContent.rvFavoriteLocations
        
        favoriteLocationAdapter = FavoriteLocationAdapter(
            onLocationClick = { location ->
                // 즐겨찾기 지역 클릭 시 해당 지역의 날씨 정보 표시
                handleFavoriteLocationClick(location)
            },
            onDeleteClick = { location ->
                // 즐겨찾기 지역 삭제
                handleFavoriteLocationDelete(location)
            }
        )
        
        recyclerView.apply {
            layoutManager = LinearLayoutManager(this@MainActivity)
            adapter = favoriteLocationAdapter
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
            drawerLayout.closeDrawers()
        }
        
        // 편집 버튼 클릭
        binding.sideMenuContent.llEditFavorite.setOnClickListener {
            handleEditFavoriteClick()
        }
    }
    
    /**
     * 현재 위치 클릭 처리
     */
    private fun handleCurrentLocationClick() {
        // 현재 위치의 날씨 정보를 새로 가져와서 표시
        refreshCurrentLocationWeather()
        drawerLayout.closeDrawers()
    }
    
    /**
     * 현재 위치 날씨 정보 새로고침
     */
    private fun refreshCurrentLocationWeather() {
        // 현재 활성화된 프래그먼트가 WeatherFragment인지 확인
        val currentFragment = navController.currentDestination?.label?.toString()
        
        if (currentFragment == "weatherFragment") {
            // WeatherFragment의 하위 프래그먼트들에 새로고침 신호 전달
            val weatherFragment = supportFragmentManager
                .findFragmentById(R.id.fragmentContainerView)
                ?.childFragmentManager
                ?.fragments
                ?.firstOrNull { it is com.example.kh_studyprojects_weatherapp.presentation.weather.WeatherFragment }
            
            // 각 하위 프래그먼트의 ViewModel에 새로고침 신호 전달
            weatherFragment?.childFragmentManager?.fragments?.forEach { fragment ->
                when (fragment) {
                    is com.example.kh_studyprojects_weatherapp.presentation.weather.current.CurrentWeatherFragment -> {
                        fragment.refreshWeatherData()
                    }
                    is com.example.kh_studyprojects_weatherapp.presentation.weather.hourly.WeatherHourlyForecastFragment -> {
                        fragment.refreshWeatherData()
                    }
                    is com.example.kh_studyprojects_weatherapp.presentation.weather.daily.WeatherDailyFragment -> {
                        fragment.refreshWeatherData()
                    }
                    is com.example.kh_studyprojects_weatherapp.presentation.weather.additional.AdditionalWeatherFragment -> {
                        fragment.refreshWeatherData()
                    }
                }
            }
            
            android.widget.Toast.makeText(
                this,
                "현재 위치의 날씨 정보를 새로고침합니다.",
                android.widget.Toast.LENGTH_SHORT
            ).show()
        } else {
            // WeatherFragment가 아닌 경우 WeatherFragment로 이동
            navController.navigate(R.id.weatherFragment)
            android.widget.Toast.makeText(
                this,
                "날씨 화면으로 이동합니다.",
                android.widget.Toast.LENGTH_SHORT
            ).show()
        }
    }
    
    /**
     * 테스트용 즐겨찾기 지역 추가
     */
    private fun addTestFavoriteLocations() {
        val testLocations = listOf(
            FavoriteLocation(
                id = "1",
                name = "서울특별시",
                latitude = 37.5665,
                longitude = 126.9780,
                address = "서울특별시 강남구"
            ),
            FavoriteLocation(
                id = "2",
                name = "부산광역시",
                latitude = 35.1796,
                longitude = 129.0756,
                address = "부산광역시 해운대구"
            ),
            FavoriteLocation(
                id = "3",
                name = "대구광역시",
                latitude = 35.8714,
                longitude = 128.6014,
                address = "대구광역시 중구"
            )
        )
        
        favoriteLocationAdapter.updateLocations(testLocations)
    }
    
    /**
     * 즐겨찾기 지역 클릭 처리
     */
    private fun handleFavoriteLocationClick(location: FavoriteLocation) {
        // TODO: 해당 지역의 날씨 정보를 가져와서 표시
        // 현재는 Toast 메시지로 표시
        android.widget.Toast.makeText(
            this,
            "${location.name}의 날씨 정보를 가져옵니다.",
            android.widget.Toast.LENGTH_SHORT
        ).show()
        drawerLayout.closeDrawers()
    }
    
    /**
     * 즐겨찾기 지역 삭제 처리
     */
    private fun handleFavoriteLocationDelete(location: FavoriteLocation) {
        // TODO: DB에서 즐겨찾기 지역 삭제
        android.widget.Toast.makeText(
            this,
            "${location.name}을(를) 즐겨찾기에서 삭제합니다.",
            android.widget.Toast.LENGTH_SHORT
        ).show()
        
        // 현재는 테스트 데이터에서만 제거
        val currentLocations = favoriteLocationAdapter.getCurrentLocations().toMutableList()
        currentLocations.remove(location)
        favoriteLocationAdapter.updateLocations(currentLocations)
    }
    
    /**
     * 편집 버튼 클릭 처리
     */
    private fun handleEditFavoriteClick() {
        // TODO: 편집 모드 활성화
        // 1. 즐겨찾기 아이템에 삭제 버튼 표시
        // 2. 드래그 앤 드롭으로 순서 변경 가능
        // 3. 편집 완료 버튼으로 모드 종료
        android.widget.Toast.makeText(
            this,
            "편집 모드가 활성화되었습니다.",
            android.widget.Toast.LENGTH_SHORT
        ).show()
        
        // 편집 모드 토글
        toggleEditMode()
    }
    
    /**
     * 편집 모드 토글
     */
    private fun toggleEditMode() {
        // TODO: 편집 모드 상태 관리
        // 1. 어댑터에 편집 모드 상태 전달
        // 2. UI 업데이트 (삭제 버튼 표시/숨김)
        // 3. 드래그 앤 드롭 활성화/비활성화
        favoriteLocationAdapter.toggleEditMode()
    }
    

    
    /**
     * 앱 정보 다이얼로그 표시
     */
    private fun showAboutDialog() {
        android.app.AlertDialog.Builder(this)
            .setTitle("앱 정보")
            .setMessage("날씨 앱 v1.0\n\n날씨 정보와 미세먼지 정보를 제공하는 앱입니다.")
            .setPositiveButton("확인") { dialog, _ ->
                dialog.dismiss()
            }
            .show()
    }
    
    /**
     * 사이드 메뉴 열기
     */
    fun openDrawer() {
        if (!drawerLayout.isDrawerOpen(GravityCompat.START)) {
            drawerLayout.openDrawer(GravityCompat.START)
        }
    }
    
    /**
     * 사이드 메뉴 닫기
     */
    fun closeDrawer() {
        if (drawerLayout.isDrawerOpen(GravityCompat.START)) {
            drawerLayout.closeDrawer(GravityCompat.START)
        }
    }
    
    /**
     * 현재 날씨 데이터 업데이트 (Fragment에서 호출)
     */
    fun updateCurrentWeatherData(weatherData: Map<String, Any>) {
        onWeatherDataUpdated?.invoke(weatherData)
    }
    
    /* 
    * 뷰바인딩 해제(메모리 누수방지)   
    */
    override fun onDestroy() {
        super.onDestroy()
        _binding = null
    }
}
