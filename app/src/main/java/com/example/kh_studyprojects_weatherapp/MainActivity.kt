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
import androidx.drawerlayout.widget.DrawerLayout
import androidx.lifecycle.lifecycleScope
import androidx.navigation.NavController
import androidx.navigation.fragment.NavHostFragment
import com.example.kh_studyprojects_weatherapp.databinding.ActivityMainBinding
import com.example.kh_studyprojects_weatherapp.presentation.common.sidemenu.adapter.SmFavoriteLocationAdapter
import com.example.kh_studyprojects_weatherapp.presentation.common.sidemenu.adapter.SmSearchResultAdapter
import com.example.kh_studyprojects_weatherapp.presentation.common.sidemenu.SmManager
import com.example.kh_studyprojects_weatherapp.domain.repository.common.sidemenu.SmFavoriteLocationRepository
import dagger.hilt.android.AndroidEntryPoint
import javax.inject.Inject
import android.util.Log

@AndroidEntryPoint
class MainActivity : AppCompatActivity() {

    // 뷰 바인딩
    private lateinit var binding: ActivityMainBinding

    // 네비게이션 컨트롤러
    private lateinit var navController: NavController
    
    // 드로어 레이아웃
    private lateinit var drawerLayout: DrawerLayout
    
    // 사이드메뉴 관리자
    private lateinit var sideMenuManager: SmManager
    
    @Inject
    lateinit var favoriteLocationRepository: SmFavoriteLocationRepository

    /**
     * 위치 권한 요청을 위한 런처
     */
    private val locationPermissionRequest = registerForActivityResult(
        ActivityResultContracts.RequestMultiplePermissions()
    ) { permissions ->
        when {
            permissions.getOrDefault(Manifest.permission.ACCESS_FINE_LOCATION, false) -> {
                Toast.makeText(this, "정확한 위치 권한이 승인되었습니다.", Toast.LENGTH_SHORT).show()
            }
            permissions.getOrDefault(Manifest.permission.ACCESS_COARSE_LOCATION, false) -> {
                Toast.makeText(this, "대략적인 위치 권한이 승인되었습니다.", Toast.LENGTH_SHORT).show()
            }
            else -> {
                Toast.makeText(this, "위치 권한이 거부되었습니다.", Toast.LENGTH_SHORT).show()
            }
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        // 다크 모드 강제 적용
        AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_YES)

        super.onCreate(savedInstanceState)

        // Edge-to-edge 설정
        WindowCompat.setDecorFitsSystemWindows(window, false)

        // 상태바와 네비게이션 바 설정
        val windowInsetsController = WindowCompat.getInsetsController(window, window.decorView)
        windowInsetsController.apply {
            isAppearanceLightStatusBars = false
            isAppearanceLightNavigationBars = true
        }

        window.apply {
            statusBarColor = Color.TRANSPARENT
            navigationBarColor = Color.WHITE
            decorView.systemUiVisibility = View.SYSTEM_UI_FLAG_LIGHT_NAVIGATION_BAR
        }

        // 뷰 바인딩 초기화
        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)

        // 드로어 레이아웃 초기화
        drawerLayout = binding.drawerLayout

        // 네비게이션 컨트롤러 설정
        val navHostFragment = supportFragmentManager
            .findFragmentById(R.id.fragmentContainerView) as NavHostFragment
        navController = navHostFragment.navController

        // 사이드메뉴 설정
        setupSideMenu()
    }

    /**
     * 위치 권한 상태를 확인하고 필요한 경우 권한을 요청하는 함수
     */
    private fun checkLocationPermission() {
        when {
            hasLocationPermission() -> {
                Toast.makeText(this, "위치 권한이 이미 승인되어 있습니다.", Toast.LENGTH_SHORT).show()
            }
            else -> {
                requestLocationPermission()
            }
        }
    }

    /**
     * 현재 앱이 위치 권한을 가지고 있는지 확인하는 함수
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
        sideMenuManager.updateCurrentWeatherData(weatherData)
    }

    private fun setupSideMenu() {
        // 어댑터들 생성
        val favoriteLocationAdapter = SmFavoriteLocationAdapter(
            onLocationClick = { location ->
                // SmManager가 초기화된 후에 실제 콜백으로 교체
            },
            onDeleteClick = { location ->
                // SmManager가 초기화된 후에 실제 콜백으로 교체
            }
        )

        val searchResultAdapter = SmSearchResultAdapter()
        
        // 사이드 메뉴 매니저 초기화
        sideMenuManager = SmManager(
            context = this,
            binding = binding,
            lifecycleScope = lifecycleScope,
            favoriteLocationAdapter = favoriteLocationAdapter,
            searchResultAdapter = searchResultAdapter,
            navController = navController,
            activity = this,
            favoriteLocationRepository = favoriteLocationRepository
        )

        // 사이드 메뉴 설정
        sideMenuManager.setupSideMenu()
        
        // SmManager 초기화 후 어댑터 콜백을 실제 구현으로 교체
        favoriteLocationAdapter.updateOnLocationClick { location ->
            sideMenuManager.handleFavoriteLocationClick(location)
        }
        
        favoriteLocationAdapter.updateOnDeleteClick { location ->
            sideMenuManager.handleFavoriteLocationDelete(location)
        }
        
        // 사이드메뉴 열림/닫힘 리스너 설정
        setupDrawerListener()
        
        // 위치 권한 확인 및 요청
        checkLocationPermission()
        
        // 최종 어댑터 상태 확인
        Log.d("MainActivity", "최종 어댑터 상태 - 아이템 개수: ${favoriteLocationAdapter.itemCount}")
    }

    /**
     * 사이드메뉴 열림/닫힘 리스너를 설정합니다.
     */
    private fun setupDrawerListener() {
        drawerLayout.addDrawerListener(object : DrawerLayout.DrawerListener {
            override fun onDrawerSlide(drawerView: View, slideOffset: Float) {
                // 슬라이드 중일 때는 아무것도 하지 않음
            }

            override fun onDrawerOpened(drawerView: View) {
                // 사이드메뉴가 열렸을 때 즐겨찾기 목록 새로고침
                sideMenuManager.onSideMenuOpened()
            }

            override fun onDrawerClosed(drawerView: View) {
                // 사이드메뉴가 닫혔을 때는 아무것도 하지 않음
            }

            override fun onDrawerStateChanged(newState: Int) {
                // 상태 변경 시에는 아무것도 하지 않음
            }
        })
    }

    override fun onDestroy() {
        super.onDestroy()
        // lateinit 변수는 null로 설정할 수 없음
    }
}