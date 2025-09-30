package com.example.kh_studyprojects_weatherapp.presentation.common.sidemenu

import android.content.Context
import android.view.View
import android.view.ViewGroup
import android.view.animation.DecelerateInterpolator
import android.animation.ValueAnimator
import android.animation.AnimatorListenerAdapter
import android.animation.Animator
import android.view.MotionEvent
import android.view.inputmethod.EditorInfo
import android.view.inputmethod.InputMethodManager
import android.text.Editable
import android.text.TextWatcher
import android.widget.Toast
import androidx.lifecycle.LifecycleCoroutineScope
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import androidx.drawerlayout.widget.DrawerLayout
import androidx.fragment.app.FragmentActivity
import androidx.navigation.NavController
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.kh_studyprojects_weatherapp.databinding.ActivityMainBinding
import com.example.kh_studyprojects_weatherapp.presentation.common.sidemenu.adapter.SmFavoriteLocationAdapter
import com.example.kh_studyprojects_weatherapp.presentation.common.sidemenu.adapter.SmSearchResultAdapter
import com.example.kh_studyprojects_weatherapp.domain.model.location.FavoriteLocation
import com.example.kh_studyprojects_weatherapp.domain.model.weather.WeatherCommon
import com.example.kh_studyprojects_weatherapp.presentation.weather.WeatherFragment
import com.example.kh_studyprojects_weatherapp.presentation.weather.current.WeatherCurrentFragment
import com.example.kh_studyprojects_weatherapp.presentation.weather.daily.WeatherDailyFragment
import com.example.kh_studyprojects_weatherapp.presentation.weather.hourly.WeatherHourlyForecastFragment
import com.example.kh_studyprojects_weatherapp.presentation.weather.additional.WeatherAdditionalFragment
import com.example.kh_studyprojects_weatherapp.domain.repository.common.geocoding.GeocodingRepository
import com.example.kh_studyprojects_weatherapp.data.api.kakao.SearchDocument
import com.example.kh_studyprojects_weatherapp.domain.repository.common.sidemenu.SmFavoriteLocationRepository
import com.example.kh_studyprojects_weatherapp.domain.repository.weather.WeatherRepository
import com.example.kh_studyprojects_weatherapp.util.DeviceIdUtil
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import kotlinx.coroutines.CancellationException
import retrofit2.HttpException
import java.io.IOException
import android.app.AlertDialog
import android.util.Log
import com.example.kh_studyprojects_weatherapp.presentation.common.location.LocationSelectionStore
import com.example.kh_studyprojects_weatherapp.presentation.common.location.SelectedLocation
import com.example.kh_studyprojects_weatherapp.presentation.common.location.LocationManager

import kotlin.math.roundToInt

import com.example.kh_studyprojects_weatherapp.domain.model.weather.WeatherData
/**
 * 사이드메뉴 전체를 관리하는 통합 Manager 클래스
 * 
 * 검색, 즐겨찾기, 애니메이션 등의 모든 기능을 담당합니다.
 * MainActivity와 하위 컴포넌트들 간의 중재자 역할을 수행합니다.
 * 
 * 주요 기능:
 * - 검색 기능 (카카오 API 연동, 무한 스크롤)
 * - 즐겨찾기 관리 (추가, 삭제, 편집)
 * - 애니메이션 (검색창 확장/축소, 드래그)
 * - 윈도우 인셋 처리
 * - 날씨 데이터 동기화
 * 
 * @author 김효동
 * @since 2025.08.26
 * @version 2.0 (통합 버전)
 */
class SmManager(
    private val context: Context,
    private val binding: ActivityMainBinding,
    private val lifecycleScope: LifecycleCoroutineScope,
    private val favoriteLocationAdapter: SmFavoriteLocationAdapter,
    private val searchResultAdapter: SmSearchResultAdapter,
    private val navController: NavController,
    private val activity: FragmentActivity,
    private val favoriteLocationRepository: SmFavoriteLocationRepository,
    private val weatherRepository: WeatherRepository,
    private val geocodingRepository: GeocodingRepository,
    private val locationSelectionStore: LocationSelectionStore,
    private val locationManager: LocationManager
) {
    // 검색 관련 변수
    private var searchJob: Job? = null
    private var currentSearchQuery: String = ""
    private var currentSearchPage: Int = 1
    private var isSearchLoading: Boolean = false
    private var isSearchEnd: Boolean = false
    private var isSearchExpanded: Boolean = false
    private var isSearchContainerExpanded: Boolean = false // 검색창이 확장되었는지 추적

    // 현재 날씨 데이터 콜백
    private var onWeatherDataUpdated: ((WeatherData) -> Unit)? = null

    fun setupSideMenu() {
        // 즐겨찾기 지역 RecyclerView 설정
        setupFavoriteLocationsRecyclerView()

        // 검색 결과 RecyclerView 설정
        setupSearchResultsRecyclerView()

        // 검색창 포커스 리스너 설정
        setupSearchFocusListeners()

        // 메뉴 아이템 클릭 리스너 설정
        setupMenuClickListeners()

        // 사이드메뉴의 윈도우 인셋 처리 설정
        setupWindowInsets(binding.sideMenuContent.root, false, true)

        // 현재 날씨 데이터 구독
        setupCurrentWeatherObserver()
        
        // 검색 결과 클릭 시 즐겨찾기에 추가하는 기능 연결
        searchResultAdapter.setOnItemClickListener { document ->
            handleSearchResultClick(document)
        }

        Log.d("SmManager", "사이드메뉴 기본 설정 완료")
        
        // 초기 즐겨찾기 목록 즉시 로드 (불필요한 지연 제거)
        lifecycleScope.launch {
            Log.d("SmManager", "초기 즐겨찾기 목록 로드 시작")
            refreshFavoriteLocations()
        }
        
        Log.d("SmManager", "사이드메뉴 설정 완료")

        // GPS 현재 위치로 사이드메뉴 상단 헤더 갱신
        refreshGpsHeader()
        refreshGpsWeatherInHeader()
    }

    // ==================== 즐겨찾기 관리 ====================

    /**
     * 즐겨찾기 지역 RecyclerView를 설정합니다.
     */
    private fun setupFavoriteLocationsRecyclerView() {
        val recyclerView = binding.sideMenuContent.rvFavoriteLocations
        // 안정적인 드래그/재활용을 위해 StableIds를 어댑터 부착 전에 활성화
        favoriteLocationAdapter.setHasStableIds(true)
        recyclerView.apply {
            layoutManager = LinearLayoutManager(context)
            adapter = favoriteLocationAdapter
            // 스크롤 성능 최적화
            setHasFixedSize(true)
            setItemViewCacheSize(20)
            // 변경 애니메이션으로 인한 깜빡임 방지 (목록 갱신 시 체인지 애니메이션 제거)
            itemAnimator = null
        }

        // 어댑터에 WeatherRepository 주입 (아이템 날씨 표시 캐시/지연 로딩 지원)
        favoriteLocationAdapter.setWeatherRepository(weatherRepository)

        // 드래그 앤 드롭을 위한 ItemTouchHelper 설정 (편집 모드에서만 동작)
        val touchHelper = androidx.recyclerview.widget.ItemTouchHelper(
            object : androidx.recyclerview.widget.ItemTouchHelper.SimpleCallback(
                androidx.recyclerview.widget.ItemTouchHelper.UP or androidx.recyclerview.widget.ItemTouchHelper.DOWN,
                0
            ) {
                override fun onMove(
                    recyclerView: RecyclerView,
                    viewHolder: RecyclerView.ViewHolder,
                    target: RecyclerView.ViewHolder
                ): Boolean {
                    if (!favoriteLocationAdapter.isEditModeEnabled()) return false
                    val from = viewHolder.bindingAdapterPosition
                    val to = target.bindingAdapterPosition
                    favoriteLocationAdapter.onItemMove(from, to)
                    return true
                }

                override fun onSwiped(viewHolder: RecyclerView.ViewHolder, direction: Int) {
                    // 스와이프 사용 안 함
                }

                override fun isLongPressDragEnabled(): Boolean {
                    // 편집 모드에서만 롱프레스 드래그 허용
                    return favoriteLocationAdapter.isEditModeEnabled()
                }

                override fun clearView(recyclerView: RecyclerView, viewHolder: RecyclerView.ViewHolder) {
                    super.clearView(recyclerView, viewHolder)
                    // 드래그 종료 시 현재 순서를 서버에 반영
                    if (!favoriteLocationAdapter.isEditModeEnabled()) return
                    persistFavoriteSortOrder()
                }
            }
        )
        touchHelper.attachToRecyclerView(recyclerView)
    }

    /**
     * 현재 어댑터 순서를 DB SORT_ORDER로 저장합니다.
     */
    private fun persistFavoriteSortOrder() {
        val current = favoriteLocationAdapter.getCurrentLocations()
        if (current.isEmpty()) return

        lifecycleScope.launch(Dispatchers.IO) {
            try {
                val deviceId = getDeviceId()
                current.forEachIndexed { index, location ->
                    try {
                        favoriteLocationRepository.updateSortOrder(
                            latitude = location.latitude,
                            longitude = location.longitude,
                            deviceId = deviceId,
                            sortOrder = index
                        )
                    } catch (e: Exception) {
                        Log.e("SmManager", "정렬 순서 업데이트 실패: ${location.addressName}", e)
                    }
                }
            } catch (e: Exception) {
                Log.e("SmManager", "정렬 순서 일괄 업데이트 실패", e)
            }
        }
    }

    /**
     * 즐겨찾기 지역 클릭을 처리합니다.
     */
    fun handleFavoriteLocationClick(location: FavoriteLocation) {
        // 선택 위치를 전역 Store에 반영 -> ViewModel의 EffectiveLocationResolver가 이를 사용
        locationSelectionStore.setSelectedLocation(
            SelectedLocation(
                latitude = location.latitude,
                longitude = location.longitude,
                address = location.addressName
            )
        )

        // 메인 화면(날씨 화면) 새로고침
        refreshCurrentLocationWeather()

        // 사이드메뉴 닫기 및 안내
        binding.drawerLayout.closeDrawers()
        Toast.makeText(context, "${location.addressName}의 날씨로 이동합니다.", Toast.LENGTH_SHORT).show()
    }

    /**
     * 즐겨찾기 지역 삭제를 처리합니다.
     */
    fun handleFavoriteLocationDelete(location: FavoriteLocation) {
        lifecycleScope.launch {
            try {
                val result = favoriteLocationRepository.deleteFavoriteLocation(
                    latitude = location.latitude,
                    longitude = location.longitude,
                    deviceId = location.deviceId
                )
                
                val isSuccess = result.first
                val message = result.second
                
                if (isSuccess) {
                    Toast.makeText(context, message, Toast.LENGTH_SHORT).show()
                    
                    // 현재 리스트에서 해당 지역 제거
                    val currentLocations = favoriteLocationAdapter.getCurrentLocations().toMutableList()
                    currentLocations.remove(location)
                    favoriteLocationAdapter.updateLocations(currentLocations)
                } else {
                    Toast.makeText(context, message, Toast.LENGTH_SHORT).show()
                }
            } catch (e: Exception) {
                Log.e("SmManager", "즐겨찾기 삭제 실패", e)
                val errorMessage = e.message ?: "삭제 중 오류가 발생했습니다."
                Toast.makeText(context, errorMessage, Toast.LENGTH_SHORT).show()
            }
        }
    }

    /**
     * 편집 모드를 토글합니다.
     */
    fun handleEditFavoriteClick() {
        Toast.makeText(context, "편집 모드가 활성화되었습니다.", Toast.LENGTH_SHORT).show()
        favoriteLocationAdapter.toggleEditMode()
    }

    /**
     * 즐겨찾기 목록을 새로고침합니다.
     */
    fun refreshFavoriteLocations() {
        lifecycleScope.launch {
            try {
                Log.d("SmManager", "즐겨찾기 목록 새로고침 시작")
                val deviceId = getDeviceId()
                Log.d("SmManager", "디바이스 ID: $deviceId")
                
                // 어댑터 참조 확인
                val recyclerView = binding.sideMenuContent.rvFavoriteLocations
                Log.d("SmManager", "RecyclerView 어댑터 해시코드: ${recyclerView.adapter?.hashCode()}")
                Log.d("SmManager", "SmManager 어댑터 해시코드: ${favoriteLocationAdapter.hashCode()}")
                Log.d("SmManager", "어댑터 참조 일치 여부: ${recyclerView.adapter === favoriteLocationAdapter}")
                
                Log.d("SmManager", "Repository 호출 시작")
                val locations = withContext(Dispatchers.IO) {
                    favoriteLocationRepository.getFavoriteLocations(deviceId)
                }
                Log.d("SmManager", "Repository 호출 완료")
                Log.d("SmManager", "즐겨찾기 목록 조회 결과: ${locations?.size ?: 0}개")
                
                if (locations != null) {
                    if (locations.isNotEmpty()) {
                        Log.d("SmManager", "즐겨찾기 목록 업데이트: ${locations.map { it.addressName }}")
                        favoriteLocationAdapter.updateLocations(locations)
                        
                        // 업데이트 후 어댑터 상태 확인 (UI 스레드에서 실행)
                        withContext(Dispatchers.Main) {
                            delay(100) // 어댑터 업데이트 완료 대기
                            Log.d("SmManager", "업데이트 후 어댑터 아이템 개수: ${favoriteLocationAdapter.itemCount}")
                            Log.d("SmManager", "업데이트 후 RecyclerView 어댑터 아이템 개수: ${recyclerView.adapter?.itemCount}")
                        }
                    } else {
                        Log.d("SmManager", "즐겨찾기 목록이 비어있음")
                        favoriteLocationAdapter.updateLocations(emptyList())
                    }
                } else {
                    Log.w("SmManager", "즐겨찾기 목록이 null - 빈 리스트로 설정")
                    favoriteLocationAdapter.updateLocations(emptyList())
                }
            } catch (e: Exception) {
                Log.e("SmManager", "즐겨찾기 목록 새로고침 실패", e)
                Log.e("SmManager", "오류 스택 트레이스", e)
                favoriteLocationAdapter.updateLocations(emptyList())
            }
        }
    }

    // ==================== 검색 기능 ====================

    /**
     * 검색 결과 RecyclerView를 설정합니다.
     */
    private fun setupSearchResultsRecyclerView() {
        val recyclerView = binding.sideMenuContent.rvSearchResults

        recyclerView.apply {
            layoutManager = LinearLayoutManager(context)
            adapter = searchResultAdapter

            // 스크롤 성능 최적화
            setHasFixedSize(true)
            setItemViewCacheSize(20)

            // 무한 스크롤 리스너 추가
            addOnScrollListener(object : RecyclerView.OnScrollListener() {
                private var isScrolling = false

                override fun onScrollStateChanged(recyclerView: RecyclerView, newState: Int) {
                    super.onScrollStateChanged(recyclerView, newState)
                    isScrolling = newState == RecyclerView.SCROLL_STATE_IDLE
                }

                override fun onScrolled(recyclerView: RecyclerView, dx: Int, dy: Int) {
                    super.onScrolled(recyclerView, dx, dy)

                    if (dy <= 0) return
                    if (isSearchLoading || isSearchEnd) return

                    val layoutManager = recyclerView.layoutManager as LinearLayoutManager
                    val totalItemCount = layoutManager.itemCount
                    val lastVisibleItemPosition = layoutManager.findLastVisibleItemPosition()

                    if (lastVisibleItemPosition >= totalItemCount - 5 && !isScrolling) {
                        loadNextSearchPage()
                    }
                }
            })
        }
    }

    /**
     * 검색 결과 아이템 클릭을 처리합니다.
     */
    fun handleSearchResultClick(document: SearchDocument) {
        lifecycleScope.launch {
            try {
                // SearchDocument를 FavoriteLocation으로 변환
                val favoriteLocation = FavoriteLocation(
                    deviceId = getDeviceId(),
                    latitude = document.y.toDouble(),
                    longitude = document.x.toDouble(),
                    addressName = document.addressName,
                    region1depthName = document.address?.region1depthName ?: "",
                    region2depthName = document.address?.region2depthName ?: "",
                    region3depthName = document.address?.region3depthName ?: "",
                    region3depthHName = document.address?.region3depthHName ?: "",
                    sortOrder = 0
                )

                // 즐겨찾기에 추가 (서버에서 중복 체크 수행)
                val result = favoriteLocationRepository.addFavoriteLocation(favoriteLocation)
                val isSuccess = result.first
                val message = result.second
                
                if (isSuccess) {
                    Toast.makeText(context,message,Toast.LENGTH_SHORT).show()
                    refreshFavoriteLocations() // 즐겨찾기 목록 새로고침
                } else {
                    Toast.makeText(context,message,Toast.LENGTH_SHORT).show()
                }
                
            } catch (e: Exception) {
                Log.e("SmManager", "즐겨찾기 추가 실패", e)
                val errorMessage = e.message ?: "즐겨찾기 추가 중 오류가 발생했습니다."
                Toast.makeText(context, errorMessage, Toast.LENGTH_SHORT).show()
            }
        }
    }

    /**
     * 검색창 포커스 리스너를 설정합니다.
     */
    private fun setupSearchFocusListeners() {
        val searchEditText = binding.sideMenuContent.etSearchLocation
        val searchContainer = binding.sideMenuContent.llSearchContainer
        val favoriteHeader = binding.sideMenuContent.llFavoriteHeader
        val clearButton = binding.sideMenuContent.ivClearSearch

        // 검색창 포커스 리스너
        searchEditText.setOnFocusChangeListener { _, hasFocus ->
            if (hasFocus) {
                animateSearchContainerUp(searchContainer, favoriteHeader)
                clearButton.visibility = View.VISIBLE
            }
        }

        // X 버튼 클릭 리스너
        clearButton.setOnClickListener {
            searchEditText.text.clear()
            clearButton.visibility = View.GONE
        }

        // 검색창 텍스트 변경 리스너
        searchEditText.addTextChangedListener(object : TextWatcher {
            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {}
            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {}
            override fun afterTextChanged(s: Editable?) {
                clearButton.visibility = if (s.isNullOrEmpty()) View.GONE else View.VISIBLE
                val query = s?.toString()?.trim().orEmpty()
                triggerDebouncedSearch(query)
            }
        })

        // 검색창 클릭 리스너
        binding.sideMenuContent.llSearchBar.setOnClickListener {
            searchEditText.requestFocus()
        }

        // 검색창 키보드 액션 리스너
        searchEditText.setOnEditorActionListener { _, actionId, _ ->
            if (actionId == EditorInfo.IME_ACTION_SEARCH) {
                performSearch(searchEditText.text.toString())
                return@setOnEditorActionListener true
            }
            return@setOnEditorActionListener false
        }

        setupSearchDragListener(searchContainer, favoriteHeader) // 검색창 드래그 리스너 설정
        setupDrawerListener(searchContainer, favoriteHeader)    // 사이드 메뉴 닫힐 때 검색창 초기화
    }

    /**
     * 검색을 수행합니다.
     */
    private fun performSearch(query: String) {
        if (query.isBlank()) {
            searchResultAdapter.clearSearchResults()
            binding.sideMenuContent.llSearchResultsContainer.visibility = View.GONE
            return
        }

        if (query.length < 2) {
            searchResultAdapter.clearSearchResults()
            binding.sideMenuContent.llSearchResultsContainer.visibility = View.GONE
            return
        }

        val trimmedQuery = if (query.length > 30) query.substring(0, 30) else query

        currentSearchQuery = trimmedQuery
        currentSearchPage = 1
        isSearchEnd = false
        isSearchLoading = false

        searchResultAdapter.setLoading(false)

        searchJob?.cancel()
        searchJob = lifecycleScope.launch {
            try {
                val response = withContext(Dispatchers.IO) {
                    geocodingRepository.searchByAddress(
                        query = trimmedQuery,
                        page = 1,
                        size = 30
                    )
                }

                if (response.documents.isEmpty()) {
                    searchResultAdapter.clearSearchResults()
                    binding.sideMenuContent.llSearchResultsContainer.visibility = View.GONE
                } else {
                    searchResultAdapter.updateSearchResults(response.documents)
                    binding.sideMenuContent.llSearchResultsContainer.visibility = View.VISIBLE
                    currentSearchPage = 2
                    isSearchEnd = response.meta.isEnd
                }
            } catch (e: Exception) {
                showLoadingIndicator(false)
                val errorMessage = e.message ?: "검색 중 오류가 발생했습니다"
                showErrorMessage(errorMessage)
                Log.e("KakaoAddressSearch", "Search error", e)
            }
        }
    }

    /**
     * 디바운스 검색을 트리거합니다.
     */
    private fun triggerDebouncedSearch(query: String) {
        searchJob?.cancel()
        searchJob = lifecycleScope.launch {
            delay(350)
            performSearch(query)
        }
    }

    /**
     * 다음 검색 페이지를 로드합니다.
     */
    private fun loadNextSearchPage() {
        if (currentSearchQuery.isBlank()) return
        if (isSearchLoading || isSearchEnd) return

        isSearchLoading = true
        showLoadingIndicator(true)

        lifecycleScope.launch {
            try {
                val response = withContext(Dispatchers.IO) {
                    geocodingRepository.searchByAddress(
                        query = currentSearchQuery,
                        page = currentSearchPage,
                        size = 30
                    )
                }

                if (response.documents.isNotEmpty()) {
                    showLoadingIndicator(false)
                    searchResultAdapter.appendSearchResults(response.documents)
                    currentSearchPage += 1
                    isSearchEnd = response.meta.isEnd
                } else {
                    isSearchEnd = true
                    showLoadingIndicator(false)
                }
            } catch (e: Exception) {
                showLoadingIndicator(false)
                val errorMessage = e.message ?: "검색 중 오류가 발생했습니다"
                showErrorMessage(errorMessage)
                Log.e("KakaoAddressSearch", "loadNextSearchPage error", e)
            } finally {
                isSearchLoading = false
            }
        }
    }

    // ==================== 윈도우 인셋 처리 ====================
    // (상단 상태바, 하단 내비게이션바 등 시스템 UI와의 간섭 방지)
    // ViewCompat.setOnApplyWindowInsetsListener를 사용하여
    // 뷰의 패딩을 동적으로 조정합니다.
    // ==================================================
    // isPaddingLeft, isPaddingTop, isPaddingRight, isPaddingBottom 중
    // true로 설정된 방향에 대해서만 패딩을 적용합니다.
    // ==================================================
    // isMarginLeft, isMarginTop, isMarginRight, isMarginBottom 중
    // true로 설정된 방향에 대해서만 마진을 적용합니다.
    // ==================================================
    private fun showLoadingIndicator(show: Boolean) {
        searchResultAdapter.setLoading(show)
    }

    // 윈도우 인셋 처리 설정
    private fun showErrorMessage(message: String) {
        Toast.makeText(context, message, Toast.LENGTH_SHORT).show()
    }

    // 디바이스 ID 가져오기
    private fun setDrawerLockForSearch(expanded: Boolean) {
        val drawerLayout = binding.drawerLayout
        val mode = if (expanded) DrawerLayout.LOCK_MODE_LOCKED_OPEN else DrawerLayout.LOCK_MODE_UNLOCKED
        drawerLayout.setDrawerLockMode(mode)
        isSearchExpanded = expanded
        Log.d("SmManager", "Drawer lock changed: expanded=${expanded}")
    }

    /**
     * 검색창이 확장되었는지 확인하여 DrawerLayout의 터치 이벤트를 제어합니다.
     */
    private fun setDrawerTouchEnabled(enabled: Boolean) {
        val drawerLayout = binding.drawerLayout
        if (enabled) {
            // 검색창이 접혀있을 때만 사이드메뉴 터치 허용
            drawerLayout.setDrawerLockMode(DrawerLayout.LOCK_MODE_UNLOCKED)
        } else {
            // 검색창이 확장되었을 때는 사이드메뉴 터치 차단
            drawerLayout.setDrawerLockMode(DrawerLayout.LOCK_MODE_LOCKED_OPEN)
        }
        Log.d("SmManager", "Drawer touch enabled: ${enabled}")
    }

    // ==================== 애니메이션 기능 ====================

    /**
     * 검색창을 위로 확장하는 애니메이션을 실행합니다.
     */
    private fun animateSearchContainerUp(searchContainer: View, favoriteHeader: View) {
        val currentHeight = searchContainer.height
        val searchContainerTop = searchContainer.top
        val favoriteHeaderTop = favoriteHeader.top
        val distanceToCover = searchContainerTop - favoriteHeaderTop
        val rawTarget = currentHeight + distanceToCover + favoriteHeader.height
        val parentHeight = (searchContainer.parent as? View)?.height ?: rawTarget
        val minHeightPx = maxOf(searchContainer.minimumHeight, 60)
        val targetHeight = rawTarget.coerceIn(minHeightPx, parentHeight)

        Log.d(
            "SmManagerDrag",
            "animateUp start: sc.h=${currentHeight}, sc.top=${searchContainerTop}, sc.bottom=${searchContainer.bottom}, parent.h=${parentHeight}, ref.top=${favoriteHeaderTop}, ref.h=${favoriteHeader.height}, dist=${distanceToCover}, rawTarget=${rawTarget}, clampedTarget=${targetHeight}"
        )

        searchContainer.post {
            val heightAnimator = ValueAnimator.ofInt(currentHeight, targetHeight)
            heightAnimator.duration = 300
            heightAnimator.interpolator = DecelerateInterpolator()

            heightAnimator.addUpdateListener { animator ->
                val params = searchContainer.layoutParams
                if (params is ViewGroup.LayoutParams) {
                    val newHeight = animator.animatedValue as Int
                    params.height = newHeight
                    searchContainer.layoutParams = params
                    searchContainer.requestLayout()
                }
            }

                    heightAnimator.addListener(object : AnimatorListenerAdapter() {
            override fun onAnimationEnd(animation: Animator) {
                binding.sideMenuContent.llSearchResultsContainer.visibility = View.VISIBLE
                binding.sideMenuContent.llSearchResultsContainer.alpha = 0f
                binding.sideMenuContent.llSearchResultsContainer.animate()
                    .alpha(1f)
                    .setDuration(200)
                    .start()

                binding.sideMenuContent.etSearchLocation.requestFocus()

                binding.sideMenuContent.etSearchLocation.postDelayed({
                    val imm = binding.root.context.getSystemService(InputMethodManager::class.java)
                    imm.showSoftInput(binding.sideMenuContent.etSearchLocation, InputMethodManager.SHOW_IMPLICIT)
                    Log.d(
                        "SmManagerDrag",
                        "animateUp end: sc.h=${searchContainer.height}, sc.top=${searchContainer.top}, sc.bottom=${searchContainer.bottom}, results.vis=${binding.sideMenuContent.llSearchResultsContainer.visibility}"
                    )
                    setDrawerLockForSearch(true)
                    isSearchContainerExpanded = true
                    setDrawerTouchEnabled(false) // 확장된 상태에서는 터치 차단
                }, 100)
            }
        })

            heightAnimator.start()
        }
    }

    /**
     * 검색창 드래그 리스너를 설정합니다.
     */
    private fun setupSearchDragListener(searchContainer: View, favoriteHeader: View) {
        var startY = 0f
        var startHeight = 0
        var isDragging = false
        val originalHeight = searchContainer.height // 원본 높이 저장

        searchContainer.setOnTouchListener { _, event ->
            when (event.action) {
                MotionEvent.ACTION_DOWN -> {
                    startY = event.rawY
                    startHeight = searchContainer.height
                    isDragging = true
                    Log.d(
                        "SmManagerDrag",
                        "drag DOWN: startY=${startY}, startH=${startHeight}, sc.top=${searchContainer.top}, sc.bottom=${searchContainer.bottom}, parent.h=${(searchContainer.parent as? View)?.height ?: 0}, ref.top=${favoriteHeader.top}, ref.h=${favoriteHeader.height}"
                    )
                    true
                }
                MotionEvent.ACTION_MOVE -> {
                    if (isDragging) {
                        val deltaY = startY - event.rawY
                        val newHeight = (startHeight + deltaY).toInt()
                        val minHeight = 100
                        val parentHeight = (searchContainer.parent as? View)?.height ?: Int.MAX_VALUE
                        val maxHeight = parentHeight
                        val clampedHeight = newHeight.coerceIn(minHeight, maxHeight)
                        
                        // 검색창이 원본 높이보다 늘어났는지 확인
                        val isExpanded = clampedHeight > originalHeight + 50 // 50px 여유값
                        if (isExpanded != isSearchContainerExpanded) {
                            isSearchContainerExpanded = isExpanded
                            setDrawerTouchEnabled(!isExpanded) // 확장되었으면 터치 차단, 접혔으면 터치 허용
                        }
                        
                        Log.d(
                            "SmManagerDrag",
                            "drag MOVE: deltaY=${deltaY}, newH=${newHeight}, clamp[${minHeight}..${maxHeight}]=${clampedHeight}, sc.top=${searchContainer.top}, sc.bottom=${searchContainer.bottom}, parent.h=${parentHeight}, isExpanded=${isExpanded}"
                        )

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
                        val deltaY = startY - event.rawY
                        val threshold = 100f
                        Log.d(
                            "SmManagerDrag",
                            "drag UP: deltaY=${deltaY}, sc.h.now=${searchContainer.height}, sc.top=${searchContainer.top}, sc.bottom=${searchContainer.bottom}"
                        )

                        if (deltaY > threshold) {
                            animateToMaxHeight(searchContainer, favoriteHeader)
                        } else if (deltaY < -threshold) {
                            animateToMinHeight(searchContainer)
                        } else {
                            animateToOriginalHeight(searchContainer, startHeight)
                        }
                    }
                    true
                }
                else -> false
            }
        }
    }

    // 검색창을 최대 높이로 확장하는 애니메이션
    private fun animateToMaxHeight(searchContainer: View, favoriteHeader: View) {
        val currentHeight = searchContainer.height
        val searchContainerTop = searchContainer.top
        val favoriteHeaderTop = favoriteHeader.top
        val distanceToCover = searchContainerTop - favoriteHeaderTop
        val rawTarget = currentHeight + distanceToCover + favoriteHeader.height
        val parentHeight = (searchContainer.parent as? View)?.height ?: rawTarget
        val minHeightPx = maxOf(searchContainer.minimumHeight, 60)
        val targetHeight = rawTarget.coerceIn(minHeightPx, parentHeight)
        Log.d(
            "SmManagerDrag",
            "animateToMax start: sc.h=${currentHeight}, sc.top=${searchContainerTop}, sc.bottom=${searchContainer.bottom}, parent.h=${parentHeight}, ref.top=${favoriteHeaderTop}, ref.h=${favoriteHeader.height}, dist=${distanceToCover}, rawTarget=${rawTarget}, clampedTarget=${targetHeight}"
        )

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
                binding.sideMenuContent.llSearchResultsContainer.visibility = View.VISIBLE
                binding.sideMenuContent.llSearchResultsContainer.alpha = 0f
                binding.sideMenuContent.llSearchResultsContainer.animate()
                    .alpha(1f)
                    .setDuration(200)
                    .start()

                binding.sideMenuContent.etSearchLocation.requestFocus()

                binding.sideMenuContent.etSearchLocation.postDelayed({
                    val imm = binding.root.context.getSystemService(InputMethodManager::class.java)
                    imm.showSoftInput(binding.sideMenuContent.etSearchLocation, InputMethodManager.SHOW_IMPLICIT)
                    Log.d(
                        "SmManagerDrag",
                        "animateToMax end: sc.h=${searchContainer.height}, sc.top=${searchContainer.top}, sc.bottom=${searchContainer.bottom}, results.vis=${binding.sideMenuContent.llSearchResultsContainer.visibility}"
                    )
                    setDrawerLockForSearch(true)
                    isSearchContainerExpanded = true
                    setDrawerTouchEnabled(false) // 확장된 상태에서는 터치 차단
                }, 100)
            }
        })

        heightAnimator.start()
    }

    // 검색창을 최소 높이로 축소하는 애니메이션
    private fun animateToMinHeight(searchContainer: View) {
        val currentHeight = searchContainer.height
        val minHeight = 100

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
                binding.sideMenuContent.llSearchResultsContainer.visibility = View.GONE

                val imm = binding.root.context.getSystemService(InputMethodManager::class.java)
                imm.hideSoftInputFromWindow(binding.sideMenuContent.etSearchLocation.windowToken, 0)

                binding.sideMenuContent.etSearchLocation.clearFocus()

                val params = searchContainer.layoutParams
                if (params is ViewGroup.LayoutParams) {
                    params.height = ViewGroup.LayoutParams.WRAP_CONTENT
                    searchContainer.layoutParams = params
                    searchContainer.requestLayout()
                }
                setDrawerLockForSearch(false)
                isSearchContainerExpanded = false
                setDrawerTouchEnabled(true) // 접힌 상태에서는 터치 허용
            }
        })

        heightAnimator.start()
    }

    // 검색창을 원본 높이로 복원하는 애니메이션
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

        heightAnimator.addListener(object : AnimatorListenerAdapter() {
            override fun onAnimationEnd(animation: Animator) {
                // 원본 높이로 돌아갔을 때 상태 업데이트
                val isExpanded = searchContainer.height > originalHeight + 50
                if (isExpanded != isSearchContainerExpanded) {
                    isSearchContainerExpanded = isExpanded
                    setDrawerTouchEnabled(!isExpanded)
                }
            }
        })

        heightAnimator.start()
    }

    /**
     * 사이드 메뉴 닫힐 때 검색창 초기화 및 편집모드 초기화
     */
    private fun setupDrawerListener(searchContainer: View, favoriteHeader: View) {
        val drawerLayout = binding.drawerLayout
        drawerLayout.addDrawerListener(object : DrawerLayout.DrawerListener {
            override fun onDrawerSlide(drawerView: View, slideOffset: Float) {}
            override fun onDrawerOpened(drawerView: View) {}
            override fun onDrawerStateChanged(newState: Int) {}
            
            override fun onDrawerClosed(drawerView: View) {
                resetSearchContainer(searchContainer, favoriteHeader)
                // 편집모드 초기화
                favoriteLocationAdapter.resetEditMode()
            }
        })
    }

    // 검색창 초기화 애니메이션
    private fun resetSearchContainer(searchContainer: View, @Suppress("UNUSED_PARAMETER") favoriteHeader: View) {
        val currentHeight = searchContainer.height
        val originalHeight = ViewGroup.LayoutParams.WRAP_CONTENT

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
                binding.sideMenuContent.llSearchResultsContainer.visibility = View.GONE
                binding.sideMenuContent.etSearchLocation.setText("")
                binding.sideMenuContent.etSearchLocation.clearFocus()

                val params = searchContainer.layoutParams
                if (params is ViewGroup.LayoutParams) {
                    params.height = originalHeight
                    searchContainer.layoutParams = params
                    searchContainer.requestLayout()
                }

                val imm = binding.root.context.getSystemService(InputMethodManager::class.java)
                imm.hideSoftInputFromWindow(binding.sideMenuContent.etSearchLocation.windowToken, 0)
                setDrawerLockForSearch(false)
                isSearchContainerExpanded = false
                setDrawerTouchEnabled(true) // 사이드메뉴가 닫힐 때 터치 허용
            }
        })

        heightAnimator.start()
    }

    // ==================== 기타 기능 ====================

    /**
     * 윈도우 인셋 처리 공통 함수
     */
    private fun setupWindowInsets(view: View, useTopMargin: Boolean, useBottomMargin: Boolean) {
        ViewCompat.setOnApplyWindowInsetsListener(view) { _, windowInsets ->
            val navigationBars = windowInsets.getInsets(WindowInsetsCompat.Type.navigationBars())
            val statusBars = windowInsets.getInsets(WindowInsetsCompat.Type.statusBars())

            val params = view.layoutParams
            if (params is ViewGroup.MarginLayoutParams) {
                if (useBottomMargin) {
                    params.bottomMargin = navigationBars.bottom
                }
                if (useTopMargin) {
                    params.topMargin = statusBars.top
                }
                view.layoutParams = params
            }

            WindowInsetsCompat.CONSUMED
        }
    }

    /**
     * 현재 날씨 데이터 구독
     */
    private fun setupCurrentWeatherObserver() {
        onWeatherDataUpdated = { _ ->
            // 메인 화면 데이터와 무관하게, 헤더는 항상 GPS 기준으로 갱신
            refreshGpsWeatherInHeader()
        }
    }

    /**
     * 사이드메뉴의 현재 위치 아이템 업데이트
     */
    private fun updateCurrentLocationInSideMenu(@Suppress("UNUSED_PARAMETER") weatherData: WeatherData) {
        // 헤더의 온도/아이콘은 GPS 기준으로만 갱신
        refreshGpsWeatherInHeader()
    }

    /**
     * GPS 현재 위치로 사이드메뉴 상단 헤더(주소)를 갱신합니다.
     */
    private fun refreshGpsHeader() {
        lifecycleScope.launch {
            try {
                val loc = locationManager.getCurrentLocation()
                if (loc != null) {
                    val address = loc.address
                    val thoroughfare = address.split(" ").lastOrNull() ?: address
                    binding.sideMenuContent.tvCurrentLocationName.text = thoroughfare
                    binding.sideMenuContent.tvCurrentLocationAddress.text = address
                }
            } catch (e: Exception) {
                Log.e("SmManager", "GPS 헤더 갱신 실패", e)
            }
        }
    }

    /**
     * 사이드메뉴 헤더의 날씨(온도/아이콘)를 GPS 위치 기준으로 갱신합니다.
     */
    private fun refreshGpsWeatherInHeader() {
        lifecycleScope.launch {
            try {
                val loc = locationManager.getCurrentLocation()
                if (loc != null) {
                    val result = withContext(Dispatchers.IO) {
                        weatherRepository.getWeatherInfo(loc.latitude, loc.longitude)
                    }
                    result.onSuccess { weatherData ->
                        try {
                            val temperature = weatherData.current.temperature2m?.roundToInt()
                            binding.sideMenuContent.tvCurrentTemperature.text = temperature?.let { "${it}°" } ?: "N/A"

                            val weatherCode = weatherData.current.weatherCode ?: 0
                            binding.sideMenuContent.ivCurrentWeatherIcon.setImageResource(WeatherCommon.getWeatherIcon(weatherCode))
                        } catch (e: Exception) {
                            Log.e("SmManager", "기본 날씨 파싱 실패", e)
                        }
                    }.onFailure {
                        binding.sideMenuContent.tvCurrentTemperature.text = "N/A"
                    }
                }
            } catch (e: Exception) {
                Log.e("SmManager", "GPS 헤더 날씨 갱신 실패", e)
            }
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
            handleEditFavoriteClick()
        }
    }

    /**
     * 현재 위치 클릭 처리
     */
    private fun handleCurrentLocationClick() {
        lifecycleScope.launch {
            try {
                val loc = locationManager.getCurrentLocation()
                if (loc != null) {
                    locationSelectionStore.setSelectedLocation(
                        SelectedLocation(
                            latitude = loc.latitude,
                            longitude = loc.longitude,
                            address = loc.address
                        )
                    )
                    binding.drawerLayout.closeDrawers()
                    Toast.makeText(context, "현재 위치의 날씨로 이동합니다.", Toast.LENGTH_SHORT).show()
                } else {
                    Toast.makeText(context, "현재 위치를 가져올 수 없습니다.", Toast.LENGTH_SHORT).show()
                }
            } catch (e: Exception) {
                Log.e("SmManager", "현재 위치 선택 실패", e)
                Toast.makeText(context, "현재 위치 선택 중 오류가 발생했습니다.", Toast.LENGTH_SHORT).show()
            } finally {
                refreshCurrentLocationWeather()
            }
        }
    }

    /**
     * 현재 위치 날씨 정보 새로고침
     */
    private fun refreshCurrentLocationWeather() {
        val currentFragment = navController.currentDestination?.label?.toString()

        if (currentFragment == "weatherFragment") {
            val weatherFragment = activity.supportFragmentManager
                .findFragmentById(com.example.kh_studyprojects_weatherapp.R.id.fragmentContainerView)
                ?.childFragmentManager
                ?.fragments
                ?.firstOrNull { it is WeatherFragment }

            weatherFragment?.childFragmentManager?.fragments?.forEach { fragment ->
                when (fragment) {
                    is WeatherCurrentFragment -> fragment.refreshWeatherData()
                    is WeatherHourlyForecastFragment -> fragment.refreshWeatherData()
                    is WeatherDailyFragment -> fragment.refreshWeatherData()
                    is WeatherAdditionalFragment -> fragment.refreshWeatherData()
                }
            }

            Toast.makeText(context, "현재 위치의 날씨 정보를 새로고침합니다.", Toast.LENGTH_SHORT).show()
        } else {
            navController.navigate(com.example.kh_studyprojects_weatherapp.R.id.weatherFragment)
            Toast.makeText(context, "날씨 화면으로 이동합니다.", Toast.LENGTH_SHORT).show()
        }
    }

    /**
     * 앱 정보 다이얼로그 표시
     */
    private fun showAboutDialog() {
        AlertDialog.Builder(context)
            .setTitle("앱 정보")
            .setMessage("날씨 앱 v2.0\n\n날씨 정보와 미세먼지 정보를 제공하는 앱입니다.")
            .setPositiveButton("확인") { dialog, _ -> dialog.dismiss() }
            .show()
    }

    /**
     * 현재 날씨 데이터 업데이트 (Fragment에서 호출)
     */
    fun updateCurrentWeatherData(weatherData: WeatherData) {
        onWeatherDataUpdated?.invoke(weatherData)
    }

    /**
     * 디바이스 ID를 가져옵니다.
     */
    private fun getDeviceId(): String {
        return DeviceIdUtil.getDeviceId(context)
    }

    /**
     * 사이드메뉴가 열릴 때 호출되어 즐겨찾기 목록을 새로고침합니다.
     */
    fun onSideMenuOpened() {
        Log.d("SmManager", "사이드메뉴 열림 - 즐겨찾기 목록 새로고침")
        refreshFavoriteLocations()
        refreshGpsHeader()
        refreshGpsWeatherInHeader()
    }
}


