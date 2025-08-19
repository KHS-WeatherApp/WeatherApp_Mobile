package com.example.kh_studyprojects_weatherapp

import android.Manifest
import android.animation.Animator
import android.animation.AnimatorListenerAdapter
import android.animation.ValueAnimator
import android.app.AlertDialog
import android.content.pm.PackageManager
import android.graphics.Color
import android.os.Bundle
import android.text.Editable
import android.text.TextWatcher
import android.util.Log
import android.view.MotionEvent
import android.view.View
import android.view.ViewGroup
import android.view.animation.DecelerateInterpolator
import android.view.inputmethod.EditorInfo
import android.view.inputmethod.InputMethodManager
import android.widget.Toast
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.app.AppCompatDelegate
import androidx.core.content.ContextCompat
import androidx.core.view.GravityCompat
import androidx.core.view.ViewCompat
import androidx.core.view.WindowCompat
import androidx.core.view.WindowInsetsCompat
import androidx.drawerlayout.widget.DrawerLayout
import androidx.lifecycle.lifecycleScope
import androidx.navigation.NavController
import androidx.navigation.fragment.NavHostFragment
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.kh_studyprojects_weatherapp.data.api.ExternalApiRetrofitInstance
import com.example.kh_studyprojects_weatherapp.databinding.ActivityMainBinding
import com.example.kh_studyprojects_weatherapp.domain.model.location.FavoriteLocation
import com.example.kh_studyprojects_weatherapp.domain.model.weather.WeatherCommon
import com.example.kh_studyprojects_weatherapp.presentation.weather.WeatherFragment
import com.example.kh_studyprojects_weatherapp.presentation.weather.adapter.FavoriteLocationAdapter
import com.example.kh_studyprojects_weatherapp.presentation.weather.adapter.SearchResultAdapter
import com.example.kh_studyprojects_weatherapp.presentation.weather.additional.AdditionalWeatherFragment
import com.example.kh_studyprojects_weatherapp.presentation.weather.current.CurrentWeatherFragment
import com.example.kh_studyprojects_weatherapp.presentation.weather.daily.WeatherDailyFragment
import com.example.kh_studyprojects_weatherapp.presentation.weather.hourly.WeatherHourlyForecastFragment
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import retrofit2.HttpException
import java.io.IOException
import kotlin.collections.get

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
    // 검색 결과 어댑터
    private lateinit var searchResultAdapter: SearchResultAdapter

    // 현재 날씨 데이터 콜백
    private var onWeatherDataUpdated: ((Map<String, Any>) -> Unit)? = null
    // 검색 디바운스용 잡
    private var searchJob: Job? = null
    // 검색 페이징 상태
    private var currentSearchQuery: String = ""
    private var currentSearchPage: Int = 1
    private var isSearchLoading: Boolean = false
    private var isSearchEnd: Boolean = false

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

        // 사이드 메뉴 설정
        setupSideMenu()

        // 시스템 UI(상태 바, 네비게이션 바)와 앱 콘텐츠가 겹치지 않도록 여백 자동 조정
        setupWindowInsets(findViewById(R.id.fragmentContainerView), true, true)

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
        // 사이드 메뉴의 윈도우 인셋 처리 설정
        setupWindowInsets(binding.sideMenuContent.root, false, true)

        // 즐겨찾기 지역 RecyclerView 설정
        setupFavoriteLocationsRecyclerView()

        // 검색 결과 RecyclerView 설정
        setupSearchResultsRecyclerView()

        // 메뉴 아이템 클릭 리스너 설정
        setupMenuClickListeners()

        // 현재 날씨 데이터 구독
        setupCurrentWeatherObserver()

        // 임시 즐겨찾기 데이터 추가 (테스트용)
        addTestFavoriteLocations()
    }

    /**
     * 윈도우 인셋 처리 공통 함수
     *
     * @param view 처리할 뷰
     * @param useTopMargin 상단 마진 사용 여부 (상태바)
     * @param useBottomMargin 하단 마진 사용 여부 (네비게이션바)
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
     * 검색 결과 RecyclerView 설정
     */
    private fun setupSearchResultsRecyclerView() {
        val recyclerView = binding.sideMenuContent.rvSearchResults
        searchResultAdapter = SearchResultAdapter { document ->
            Toast.makeText(
                this,
                "${document.placeName.ifEmpty { document.addressName }} 선택됨",
                Toast.LENGTH_SHORT
            ).show()
        }
        recyclerView.apply {
            layoutManager = LinearLayoutManager(this@MainActivity)
            adapter = searchResultAdapter

            // 스크롤 성능 최적화
            setHasFixedSize(true)
            setItemViewCacheSize(20)

            // 무한 스크롤 리스너 추가 (안전한 방식)
            addOnScrollListener(object : RecyclerView.OnScrollListener() {
                private var isScrolling = false

                override fun onScrollStateChanged(recyclerView: RecyclerView, newState: Int) {
                    super.onScrollStateChanged(recyclerView, newState)
                    isScrolling = newState == RecyclerView.SCROLL_STATE_IDLE
                }

                override fun onScrolled(recyclerView: RecyclerView, dx: Int, dy: Int) {
                    super.onScrolled(recyclerView, dx, dy)

                    // 위로 스크롤할 때는 무시
                    if (dy <= 0) return

                    // 이미 로딩 중이거나 마지막 페이지면 무시
                    if (isSearchLoading || isSearchEnd) return

                    val layoutManager = recyclerView.layoutManager as LinearLayoutManager
                    val totalItemCount = layoutManager.itemCount
                    val lastVisibleItemPosition = layoutManager.findLastVisibleItemPosition()

                    // 마지막 아이템에서 5개 전에 도달하면 다음 페이지 로드 (더 안전한 임계값)
                    if (lastVisibleItemPosition >= totalItemCount - 5 && !isScrolling) {
                        loadNextSearchPage()
                    }
                }
            })
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

        // 검색창 포커스 리스너 설정
        setupSearchFocusListeners()
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
                this,
                "현재 위치의 날씨 정보를 새로고침합니다.",
                Toast.LENGTH_SHORT
            ).show()
        } else {
            // WeatherFragment가 아닌 경우 WeatherFragment로 이동
            navController.navigate(R.id.weatherFragment)
            Toast.makeText(
                this,
                "날씨 화면으로 이동합니다.",
                Toast.LENGTH_SHORT
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
        Toast.makeText(
            this,
            "${location.name}의 날씨 정보를 가져옵니다.",
            Toast.LENGTH_SHORT
        ).show()
        drawerLayout.closeDrawers()
    }

    /**
     * 즐겨찾기 지역 삭제 처리
     */
    private fun handleFavoriteLocationDelete(location: FavoriteLocation) {
        // TODO: DB에서 즐겨찾기 지역 삭제
        Toast.makeText(
            this,
            "${location.name}을(를) 즐겨찾기에서 삭제합니다.",
            Toast.LENGTH_SHORT
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
        Toast.makeText(
            this,
            "편집 모드가 활성화되었습니다.",
            Toast.LENGTH_SHORT
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
     * 검색창 포커스 리스너 설정
     */
    private fun setupSearchFocusListeners() {
        val searchEditText = binding.sideMenuContent.etSearchLocation
        val searchContainer = binding.sideMenuContent.llSearchContainer
        val favoriteHeader = binding.sideMenuContent.llFavoriteHeader
        val clearButton = binding.sideMenuContent.ivClearSearch

        // 검색창 포커스 리스너
        searchEditText.setOnFocusChangeListener { _, hasFocus ->
            if (hasFocus) {
                // 포커스 획득 시: 검색창을 확장 애니메이션
                animateSearchContainerUp(searchContainer, favoriteHeader)
                clearButton.visibility = View.VISIBLE
            }
            // 포커스 상실 시 애니메이션 제거
        }

        // X 버튼 클릭 리스너
        clearButton.setOnClickListener {
            searchEditText.text.clear()
            // 포커스 해제하지 않음 (검색창 확장 상태 유지)
            clearButton.visibility = View.GONE
        }

        // 검색창 텍스트 변경 리스너
        searchEditText.addTextChangedListener(object : TextWatcher {
            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {}
            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {}
            override fun afterTextChanged(s: Editable?) {
                // 텍스트가 있으면 X 버튼 표시, 없으면 숨김
                clearButton.visibility = if (s.isNullOrEmpty()) View.GONE else View.VISIBLE

                val query = s?.toString()?.trim().orEmpty()
                triggerDebouncedSearch(query)
            }
        })

        // 검색창 클릭 리스너 (포커스 획득)
        binding.sideMenuContent.llSearchBar.setOnClickListener {
            searchEditText.requestFocus()
        }

        // 검색창 키보드 액션 리스너
        searchEditText.setOnEditorActionListener { _, actionId, _ ->
            if (actionId == EditorInfo.IME_ACTION_SEARCH) {
                // 검색 실행
                performSearch(searchEditText.text.toString())
                return@setOnEditorActionListener true
            }
            return@setOnEditorActionListener false
        }

        // 검색창 드래그 리스너 설정
        setupSearchDragListener(searchContainer, favoriteHeader)

        // 사이드 메뉴 닫힐 때 검색창 초기화
        setupDrawerListener(searchContainer, favoriteHeader)
    }

    /**
     * 검색창을 확장 애니메이션 (포커스 획득 시)
     */
    private fun animateSearchContainerUp(searchContainer: View, favoriteHeader: View) {
        // 검색창을 제자리에서 확장하여 즐겨찾기 헤더를 완전히 덮음
        val currentHeight = searchContainer.height

        // 즐겨찾기 헤더의 위치를 기준으로 검색창이 덮을 수 있는 높이 계산
        val searchContainerTop = searchContainer.top
        val favoriteHeaderTop = favoriteHeader.top
        val distanceToCover = searchContainerTop - favoriteHeaderTop

        // 검색창이 즐겨찾기 헤더까지 덮을 수 있는 정확한 높이
        val targetHeight = currentHeight + distanceToCover + favoriteHeader.height

        // 디버깅을 위한 로그
        Log.d("SearchAnimation", "현재 높이: $currentHeight, 헤더 높이: ${favoriteHeader.height}, 목표 높이: $targetHeight")
        Log.d("SearchAnimation", "검색창 top: $searchContainerTop, 즐겨찾기 헤더 top: $favoriteHeaderTop, 덮을 거리: $distanceToCover")

        // UI 스레드에서 안전하게 높이 변경
        searchContainer.post {
            // 높이만 애니메이션
            val heightAnimator = ValueAnimator.ofInt(currentHeight, targetHeight)
            heightAnimator.duration = 300
            heightAnimator.interpolator = DecelerateInterpolator()

            heightAnimator.addUpdateListener { animator ->
                val params = searchContainer.layoutParams
                if (params is ViewGroup.LayoutParams) {
                    val newHeight = animator.animatedValue as Int
                    params.height = newHeight
                    searchContainer.layoutParams = params
                    // 레이아웃 재계산 강제
                    searchContainer.requestLayout()
                }
            }

            heightAnimator.addListener(object : AnimatorListenerAdapter() {
                override fun onAnimationEnd(animation: Animator) {
                    // 검색 결과 영역 표시
                    binding.sideMenuContent.llSearchResultsContainer.visibility = View.VISIBLE
                    binding.sideMenuContent.llSearchResultsContainer.alpha = 0f
                    binding.sideMenuContent.llSearchResultsContainer.animate()
                        .alpha(1f)
                        .setDuration(200)
                        .start()

                    // 최대 확장 상태에서 키보드 표시 및 포커스 설정
                    binding.sideMenuContent.etSearchLocation.requestFocus()

                    // 키보드 표시 (약간의 지연 후)
                    binding.sideMenuContent.etSearchLocation.postDelayed({
                        val imm = getSystemService(INPUT_METHOD_SERVICE) as InputMethodManager
                        imm.showSoftInput(binding.sideMenuContent.etSearchLocation, InputMethodManager.SHOW_IMPLICIT)
                    }, 100)
                }
            })

            heightAnimator.start()
        }
    }

    /**
     * 검색창 드래그 리스너 설정
     */
    private fun setupSearchDragListener(searchContainer: View, favoriteHeader: View) {
        var startY = 0f
        var startHeight = 0
        var isDragging = false

        // 검색창 영역에 터치 리스너 설정
        searchContainer.setOnTouchListener { _, event ->
            when (event.action) {
                MotionEvent.ACTION_DOWN -> {
                    startY = event.rawY
                    startHeight = searchContainer.height
                    isDragging = true
                    true
                }
                MotionEvent.ACTION_MOVE -> {
                    if (isDragging) {
                        val deltaY = startY - event.rawY
                        val newHeight = (startHeight + deltaY).toInt()

                        // 안전한 최소/최대 높이 계산
                        val minHeight = 100 // 최소 높이를 고정값으로 설정
                        val currentTop = searchContainer.top
                        val headerTop = favoriteHeader.top
                        val headerHeight = favoriteHeader.height

                        // 최대 높이 계산 (즐겨찾기 헤더까지 덮을 수 있는 높이)
                        val maxHeight = if (currentTop > headerTop) {
                            startHeight + (currentTop - headerTop) + headerHeight
                        } else {
                            startHeight + headerHeight + 200 // 안전한 기본값
                        }

                        // 최소값이 최대값보다 크지 않도록 보장
                        val safeMinHeight = minOf(minHeight, maxHeight - 1)
                        val safeMaxHeight = maxOf(maxHeight, safeMinHeight + 1)

                        val clampedHeight = newHeight.coerceIn(safeMinHeight, safeMaxHeight)

                        // 디버깅 로그
                        Log.d("DragDebug", "현재 높이: $newHeight, 최소: $safeMinHeight, 최대: $safeMaxHeight, 클램프: $clampedHeight")

                        // 키보드 자동 숨김 (검색창이 30% 이상 줄어들면)
                        val heightReduction = startHeight - clampedHeight
                        val reductionPercentage = (heightReduction.toFloat() / startHeight.toFloat()) * 100

                        if (reductionPercentage >= 30f) {
                            // 키보드 숨기기
                            val imm = getSystemService(INPUT_METHOD_SERVICE) as InputMethodManager
                            imm.hideSoftInputFromWindow(binding.sideMenuContent.etSearchLocation.windowToken, 0)

                            // 포커스 해제
                            binding.sideMenuContent.etSearchLocation.clearFocus()
                        }

                        // 실시간으로 높이 변경
                        val params = searchContainer.layoutParams
                        if (params is ViewGroup.LayoutParams) {
                            params.height = clampedHeight
                            searchContainer.layoutParams = params
                            searchContainer.requestLayout()
                        }
                    }
                    true
                }
                MotionEvent.ACTION_UP -> {
                    if (isDragging) {
                        isDragging = false

                        // 드래그 방향에 따라 최종 상태 결정
                        val deltaY = startY - event.rawY
                        val threshold = 100f // 드래그 임계값

                        if (deltaY > threshold) {
                            // 위로 드래그: 최대 확장
                            animateToMaxHeight(searchContainer, favoriteHeader)
                        } else if (deltaY < -threshold) {
                            // 아래로 드래그: 최소 축소
                            animateToMinHeight(searchContainer)
                        } else {
                            // 임계값 미달: 원래 상태로 복원
                            animateToOriginalHeight(searchContainer, startHeight)
                        }
                    }
                    true
                }
                else -> false
            }
        }
    }

    /**
     * 최대 높이로 애니메이션
     */
    private fun animateToMaxHeight(searchContainer: View, favoriteHeader: View) {
        val currentHeight = searchContainer.height
        val searchContainerTop = searchContainer.top
        val favoriteHeaderTop = favoriteHeader.top
        val distanceToCover = searchContainerTop - favoriteHeaderTop
        val targetHeight = currentHeight + distanceToCover + favoriteHeader.height

        val heightAnimator = ValueAnimator.ofInt(currentHeight, targetHeight)
        heightAnimator.duration = 200
        heightAnimator.interpolator = DecelerateInterpolator()

        heightAnimator.addUpdateListener { animator ->
            val params = searchContainer.layoutParams
            if (params is ViewGroup.LayoutParams) {
                params.height = animator.animatedValue as Int
                searchContainer.layoutParams = params
                searchContainer.requestLayout()
            }
        }

        heightAnimator.addListener(object : AnimatorListenerAdapter() {
            override fun onAnimationEnd(animation: Animator) {
                // 검색 결과 영역 표시
                binding.sideMenuContent.llSearchResultsContainer.visibility = View.VISIBLE
                binding.sideMenuContent.llSearchResultsContainer.alpha = 0f
                binding.sideMenuContent.llSearchResultsContainer.animate()
                    .alpha(1f)
                    .setDuration(200)
                    .start()

                // 최대 확장 상태에서 키보드 표시 및 포커스 설정
                binding.sideMenuContent.etSearchLocation.requestFocus()

                // 키보드 표시 (약간의 지연 후)
                binding.sideMenuContent.etSearchLocation.postDelayed({
                    val imm = getSystemService(INPUT_METHOD_SERVICE) as InputMethodManager
                    imm.showSoftInput(binding.sideMenuContent.etSearchLocation, InputMethodManager.SHOW_IMPLICIT)
                }, 100)
            }
        })

        heightAnimator.start()
    }

    /**
     * 최소 높이로 애니메이션
     */
    private fun animateToMinHeight(searchContainer: View) {
        val currentHeight = searchContainer.height
        val minHeight = 100 // 최소 높이를 안전한 값으로 설정

        val heightAnimator = ValueAnimator.ofInt(currentHeight, minHeight)
        heightAnimator.duration = 200
        heightAnimator.interpolator = DecelerateInterpolator()

        heightAnimator.addUpdateListener { animator ->
            val params = searchContainer.layoutParams
            if (params is ViewGroup.LayoutParams) {
                params.height = animator.animatedValue as Int
                searchContainer.layoutParams = params
                searchContainer.requestLayout()
            }
        }

        heightAnimator.addListener(object : AnimatorListenerAdapter() {
            override fun onAnimationEnd(animation: Animator) {
                // 검색 결과 영역 숨김
                binding.sideMenuContent.llSearchResultsContainer.visibility = View.GONE

                // 키보드 숨기기 (검색창이 완전히 내려갔을 때)
                val imm = getSystemService(INPUT_METHOD_SERVICE) as InputMethodManager
                imm.hideSoftInputFromWindow(binding.sideMenuContent.etSearchLocation.windowToken, 0)

                // 포커스 해제
                binding.sideMenuContent.etSearchLocation.clearFocus()

                // 높이를 WRAP_CONTENT로 복원
                val params = searchContainer.layoutParams
                if (params is ViewGroup.LayoutParams) {
                    params.height = ViewGroup.LayoutParams.WRAP_CONTENT
                    searchContainer.layoutParams = params
                    searchContainer.requestLayout()
                }
            }
        })

        heightAnimator.start()
    }

    /**
     * 원래 높이로 애니메이션
     */
    private fun animateToOriginalHeight(searchContainer: View, originalHeight: Int) {
        val currentHeight = searchContainer.height

        val heightAnimator = ValueAnimator.ofInt(currentHeight, originalHeight)
        heightAnimator.duration = 200
        heightAnimator.interpolator = DecelerateInterpolator()

        heightAnimator.addUpdateListener { animator ->
            val params = searchContainer.layoutParams
            if (params is ViewGroup.LayoutParams) {
                params.height = animator.animatedValue as Int
                searchContainer.layoutParams = params
                searchContainer.requestLayout()
            }
        }

        heightAnimator.start()
    }

    /**
     * 사이드 메뉴 닫힐 때 검색창 초기화
     */
    private fun setupDrawerListener(searchContainer: View, favoriteHeader: View) {
        binding.drawerLayout.addDrawerListener(object : DrawerLayout.DrawerListener {
            override fun onDrawerSlide(drawerView: View, slideOffset: Float) {
                // 드로어가 슬라이드되는 동안
            }

            override fun onDrawerOpened(drawerView: View) {
                // 드로어가 열렸을 때
            }

            override fun onDrawerClosed(drawerView: View) {
                // 드로어가 닫혔을 때 검색창 초기화
                resetSearchContainer(searchContainer, favoriteHeader)
            }

            override fun onDrawerStateChanged(newState: Int) {
                // 드로어 상태 변경
            }
        })
    }

    /**
     * 검색창을 초기 상태로 복원
     */
    private fun resetSearchContainer(searchContainer: View, favoriteHeader: View) {
        // 검색창 높이를 원래대로 복원
        val currentHeight = searchContainer.height
        val originalHeight = ViewGroup.LayoutParams.WRAP_CONTENT

        // 높이 애니메이션으로 부드럽게 복원
        val heightAnimator = ValueAnimator.ofInt(currentHeight, 0)
        heightAnimator.duration = 300
        heightAnimator.interpolator = DecelerateInterpolator()

        heightAnimator.addUpdateListener { animator ->
            val params = searchContainer.layoutParams
            if (params is ViewGroup.LayoutParams) {
                params.height = animator.animatedValue as Int
                searchContainer.layoutParams = params
                searchContainer.requestLayout()
            }
        }

        heightAnimator.addListener(object : AnimatorListenerAdapter() {
            override fun onAnimationEnd(animation: Animator) {
                // 검색 결과 영역 숨김
                binding.sideMenuContent.llSearchResultsContainer.visibility = View.GONE

                // 검색창 텍스트 초기화
                binding.sideMenuContent.etSearchLocation.setText("")

                // 검색창 포커스 해제
                binding.sideMenuContent.etSearchLocation.clearFocus()

                // 높이를 WRAP_CONTENT로 복원
                val params = searchContainer.layoutParams
                if (params is ViewGroup.LayoutParams) {
                    params.height = originalHeight
                    searchContainer.layoutParams = params
                    searchContainer.requestLayout()
                }

                // 키보드 숨기기
                val imm = getSystemService(INPUT_METHOD_SERVICE) as InputMethodManager
                imm.hideSoftInputFromWindow(binding.sideMenuContent.etSearchLocation.windowToken, 0)
            }
        })

        heightAnimator.start()
    }

    /**
     * 검색 실행
     */
    private fun performSearch(query: String) {
        if (query.isBlank()) {
            searchResultAdapter.clearSearchResults()
            binding.sideMenuContent.llSearchResultsContainer.visibility = View.GONE
            return
        }

        // 카카오 주소 검색은 1글자/공백 등 잘못된 파라미터로 400이 발생할 수 있어 2자 이상일 때만 호출
        if (query.length < 2) {
            searchResultAdapter.clearSearchResults()
            binding.sideMenuContent.llSearchResultsContainer.visibility = View.GONE
            return
        }

        // 카카오 API 제한: 검색어 최대 30자
        val trimmedQuery = if (query.length > 30) query.substring(0, 30) else query

        // 검색 상태 초기화
        currentSearchQuery = trimmedQuery
        currentSearchPage = 1
        isSearchEnd = false
        isSearchLoading = false

        // 로딩 상태 초기화
        searchResultAdapter.setLoading(false)

        // 첫 페이지 호출
        searchJob?.cancel()
        searchJob = lifecycleScope.launch {
            try {
                val response = withContext(Dispatchers.IO) {
                    ExternalApiRetrofitInstance.kakaoApiService.searchByAddress(
                        query = trimmedQuery,
                        page = 1,
                        size = 30
                    )
                }
                
                if (response.documents.isEmpty()) {
                    searchResultAdapter.clearSearchResults()
                    binding.sideMenuContent.llSearchResultsContainer.visibility = View.GONE
                } else {
                    // 첫 페이지 결과 표시
                    searchResultAdapter.updateSearchResults(response.documents)
                    binding.sideMenuContent.llSearchResultsContainer.visibility = View.VISIBLE
                    
                    // 다음 페이지 준비 (무한 스크롤을 위해)
                    currentSearchPage = 2
                    isSearchEnd = response.meta.isEnd
                    
                    Log.d("Search", "첫 페이지 로드 완료: ${response.documents.size}개, 다음 페이지: $currentSearchPage, 마지막 페이지: $isSearchEnd")
                }
            } catch (e: HttpException) {
                val code = e.code()
                val msg = e.response()?.errorBody()?.string().orEmpty()
                Log.e("KakaoAddressSearch", "HTTP $code: $msg", e)
                showLoadingIndicator(false)
                showErrorMessage("검색 오류 (HTTP $code)")
            } catch (e: IOException) {
                Log.e("KakaoAddressSearch", "네트워크 오류", e)
                showLoadingIndicator(false)
                showErrorMessage("네트워크 오류가 발생했습니다")
            } catch (e: Exception) {
                Log.e("KakaoAddressSearch", "기타 오류", e)
                showLoadingIndicator(false)
                showErrorMessage("검색 중 오류가 발생했습니다")
            }
        }
    }

    private fun triggerDebouncedSearch(query: String) {
        searchJob?.cancel()
        searchJob = lifecycleScope.launch {
            delay(350)
            performSearch(query)
        }
    }

    /**
     * 다음 페이지 로드
     */
    private fun loadNextSearchPage() {
        if (currentSearchQuery.isBlank()) return
        if (isSearchLoading || isSearchEnd) return

        // 중복 호출 방지
        isSearchLoading = true
        showLoadingIndicator(true)

        lifecycleScope.launch {
            try {
                val response = withContext(Dispatchers.IO) {
                    ExternalApiRetrofitInstance.kakaoApiService.searchByAddress(
                        query = currentSearchQuery,
                        page = currentSearchPage,
                        size = 30
                    )
                }

                if (response.documents.isNotEmpty()) {
                    // 데이터 추가 전에 로딩 상태 해제
                    showLoadingIndicator(false)

                    searchResultAdapter.appendSearchResults(response.documents)
                    currentSearchPage += 1
                    isSearchEnd = response.meta.isEnd
                } else {
                    isSearchEnd = true
                    showLoadingIndicator(false)
                }

            } catch (e: HttpException) {
                val code = e.code()
                val msg = e.response()?.errorBody()?.string().orEmpty()
                Log.e("KakaoAddressSearch", "HTTP $code: $msg", e)
                showLoadingIndicator(false)
                showErrorMessage("검색 오류 (HTTP $code)")
            } catch (e: IOException) {
                Log.e("KakaoAddressSearch", "네트워크 오류", e)
                showLoadingIndicator(false)
                showErrorMessage("네트워크 오류가 발생했습니다")
            } catch (e: Exception) {
                Log.e("KakaoAddressSearch", "기타 오류", e)
                showLoadingIndicator(false)
                showErrorMessage("검색 중 오류가 발생했습니다")
            } finally {
                isSearchLoading = false
            }
        }
    }

    /**
     * 로딩 인디케이터 표시/숨김
     */
    private fun showLoadingIndicator(show: Boolean) {
        searchResultAdapter.setLoading(show)
    }

    /**
     * 에러 메시지 표시
     */
    private fun showErrorMessage(message: String) {
        Toast.makeText(this@MainActivity, message, Toast.LENGTH_SHORT).show()
    }

    /**
     * 앱 정보 다이얼로그 표시
     */
    private fun showAboutDialog() {
        AlertDialog.Builder(this)
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