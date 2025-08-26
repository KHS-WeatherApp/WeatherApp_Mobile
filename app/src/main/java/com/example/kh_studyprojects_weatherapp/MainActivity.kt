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
import com.example.kh_studyprojects_weatherapp.presentation.common.sideMenu.adapter.SmFavoriteLocationAdapter
import com.example.kh_studyprojects_weatherapp.presentation.common.sideMenu.adapter.SmSearchResultAdapter
import com.example.kh_studyprojects_weatherapp.presentation.common.sideMenu.SmManager
import dagger.hilt.android.AndroidEntryPoint

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
    private lateinit var favoriteLocationAdapter: SmFavoriteLocationAdapter
    // 검색 결과 어댑터
    private lateinit var searchResultAdapter: SmSearchResultAdapter

    // 사이드메뉴 관리자
    private lateinit var sideMenuManager: SmManager




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
            statusBarColor = Color.TRANSPARENT      // 상단 상태바 배경 투명
            navigationBarColor = Color.WHITE        // 하단 네비게이션 바 배경 흰색
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

        // 어댑터들 초기화
        setupAdapters()
        
        // 사이드메뉴 관리자 초기화
        sideMenuManager = SmManager(
            context = this,
            binding = binding,
            lifecycleScope = lifecycleScope,
            favoriteLocationAdapter = favoriteLocationAdapter,
            searchResultAdapter = searchResultAdapter,
            navController = navController,
            activity = this
        )

        // 사이드 메뉴 설정
        sideMenuManager.setupSideMenu()
        
        // 어댑터 콜백 설정 (sideMenuManager 초기화 후)
        favoriteLocationAdapter = SmFavoriteLocationAdapter(
            onLocationClick = { location ->
                sideMenuManager.handleFavoriteLocationClick(location)
            },
            onDeleteClick = { location ->
                sideMenuManager.handleFavoriteLocationDelete(location)
            }
        )



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
     * 어댑터들 초기화
     */
    private fun setupAdapters() {
        // 즐겨찾기 지역 어댑터 초기화 (빈 콜백으로)
        favoriteLocationAdapter = SmFavoriteLocationAdapter(
            onLocationClick = { },
            onDeleteClick = { }
        )
        
        // 검색 결과 어댑터 초기화
        searchResultAdapter = SmSearchResultAdapter { document ->
            // document는 SearchDocument 타입
            Toast.makeText(
                this,
                "${document.addressName} 선택됨",  // addressName 필드 사용
                Toast.LENGTH_SHORT
            ).show()
        }
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

    /*
     * 뷰바인딩 해제(메모리 누수방지)
     */
    override fun onDestroy() {
        super.onDestroy()
        _binding = null
    }
}