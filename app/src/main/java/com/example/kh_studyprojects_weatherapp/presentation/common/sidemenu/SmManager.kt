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
import androidx.core.view.GravityCompat
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
import com.example.kh_studyprojects_weatherapp.presentation.weather.current.CurrentWeatherFragment
import com.example.kh_studyprojects_weatherapp.presentation.weather.daily.WeatherDailyFragment
import com.example.kh_studyprojects_weatherapp.presentation.weather.hourly.WeatherHourlyForecastFragment
import com.example.kh_studyprojects_weatherapp.presentation.weather.additional.AdditionalWeatherFragment
import com.example.kh_studyprojects_weatherapp.data.api.ApiServiceProvider
import com.example.kh_studyprojects_weatherapp.data.api.kakao.SearchDocument
import com.example.kh_studyprojects_weatherapp.domain.repository.common.sidemenu.SmFavoriteLocationRepository
import com.example.kh_studyprojects_weatherapp.domain.repository.weather.WeatherRepository
import com.example.kh_studyprojects_weatherapp.util.DeviceIdUtil
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import retrofit2.HttpException
import java.io.IOException
import android.app.AlertDialog
import android.util.Log

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
    private val weatherRepository: WeatherRepository
) {
    // 검색 관련 변수
    private var searchJob: Job? = null
    private var currentSearchQuery: String = ""
    private var currentSearchPage: Int = 1
    private var isSearchLoading: Boolean = false
    private var isSearchEnd: Boolean = false

    // 현재 날씨 데이터 콜백
    private var onWeatherDataUpdated: ((Map<String, Any>) -> Unit)? = null

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
        
        // 초기 즐겨찾기 목록 로드는 별도로 처리
        lifecycleScope.launch {
            delay(500) // 기본 설정 완료 후 0.5초 대기
            Log.d("SmManager", "초기 즐겨찾기 목록 로드 시작")
            refreshFavoriteLocations()
        }
        
        Log.d("SmManager", "사이드메뉴 설정 완료")
    }

    // ==================== 즐겨찾기 관리 ====================

    /**
     * 즐겨찾기 지역 RecyclerView를 설정합니다.
     */
    private fun setupFavoriteLocationsRecyclerView() {
        val recyclerView = binding.sideMenuContent.rvFavoriteLocations
        recyclerView.apply {
            layoutManager = LinearLayoutManager(context)
            adapter = favoriteLocationAdapter
        }
    }

    /**
     * 즐겨찾기 지역 클릭을 처리합니다.
     */
    fun handleFavoriteLocationClick(location: FavoriteLocation) {
        Toast.makeText(context, "${location.addressName}의 날씨 정보를 가져옵니다.", Toast.LENGTH_SHORT).show()
        // TODO: 해당 지역의 날씨 정보를 가져와서 표시
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
                val locations = favoriteLocationRepository.getFavoriteLocations(deviceId)
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
                    ApiServiceProvider.kakaoApiService.searchByAddress(
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
            } catch (e: HttpException) {
                val code = e.code()
                val msg = e.response()?.errorBody()?.string().orEmpty()
                Log.e("KakaoAddressSearch", "HTTP $code: $msg", e)
                showLoadingIndicator(false)
                val errorMessage = e.message ?: "검색 오류 (HTTP $code)"
                showErrorMessage(errorMessage)
            } catch (e: IOException) {
                Log.e("KakaoAddressSearch", "네트워크 오류", e)
                showLoadingIndicator(false)
                val errorMessage = e.message ?: "네트워크 오류가 발생했습니다"
                showErrorMessage(errorMessage)
            } catch (e: Exception) {
                Log.e("KakaoAddressSearch", "기타 오류", e)
                showLoadingIndicator(false)
                val errorMessage = e.message ?: "검색 중 오류가 발생했습니다"
                showErrorMessage(errorMessage)
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
                    ApiServiceProvider.kakaoApiService.searchByAddress(
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

            } catch (e: HttpException) {
                val code = e.code()
                val msg = e.response()?.errorBody()?.string().orEmpty()
                Log.e("KakaoAddressSearch", "HTTP $code: $msg", e)
                showLoadingIndicator(false)
                val errorMessage = e.message ?: "검색 오류 (HTTP $code)"
                showErrorMessage(errorMessage)
            } catch (e: IOException) {
                Log.e("KakaoAddressSearch", "네트워크 오류", e)
                showLoadingIndicator(false)
                val errorMessage = e.message ?: "네트워크 오류가 발생했습니다"
                showErrorMessage(errorMessage)
            } catch (e: Exception) {
                Log.e("KakaoAddressSearch", "기타 오류", e)
                showLoadingIndicator(false)
                val errorMessage = e.message ?: "검색 중 오류가 발생했습니다"
                showErrorMessage(errorMessage)
            } finally {
                isSearchLoading = false
            }
        }
    }

    private fun showLoadingIndicator(show: Boolean) {
        searchResultAdapter.setLoading(show)
    }

    private fun showErrorMessage(message: String) {
        Toast.makeText(context, message, Toast.LENGTH_SHORT).show()
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
        var parentHeightAtStart = 0
        var startTop = 0
        var startBottom = 0

        searchContainer.setOnTouchListener { _, event ->
            when (event.action) {
                MotionEvent.ACTION_DOWN -> {
                    startY = event.rawY
                    startHeight = searchContainer.height
                    isDragging = true
                    parentHeightAtStart = (searchContainer.parent as? View)?.height ?: 0
                    startTop = searchContainer.top
                    startBottom = searchContainer.bottom
                    Log.d(
                        "SmManagerDrag",
                        "drag DOWN: startY=${startY}, startH=${startHeight}, sc.top=${startTop}, sc.bottom=${startBottom}, parent.h=${parentHeightAtStart}, ref.top=${favoriteHeader.top}, ref.h=${favoriteHeader.height}"
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
                        Log.d(
                            "SmManagerDrag",
                            "drag MOVE: deltaY=${deltaY}, newH=${newHeight}, clamp[${minHeight}..${maxHeight}]=${clampedHeight}, sc.top=${searchContainer.top}, sc.bottom=${searchContainer.bottom}, parent.h=${parentHeight}"
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
                }, 100)
            }
        })

        heightAnimator.start()
    }

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
            }
        })

        heightAnimator.start()
    }

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

    private fun resetSearchContainer(searchContainer: View, favoriteHeader: View) {
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
        onWeatherDataUpdated = { weatherData ->
            updateCurrentLocationInSideMenu(weatherData)
        }
    }

    /**
     * 사이드메뉴의 현재 위치 아이템 업데이트
     */
    private fun updateCurrentLocationInSideMenu(weatherData: Map<String, Any>) {
        try {
            weatherData["location"]?.let { location ->
                val address = location.toString()
                val thoroughfare = address.split(" ").lastOrNull() ?: address
                binding.sideMenuContent.tvCurrentLocationName.text = thoroughfare
                binding.sideMenuContent.tvCurrentLocationAddress.text = address
            }

            val current = weatherData["current"] as? Map<*, *>
            current?.let {
                val temperature = it["temperature_2m"] as? Double
                binding.sideMenuContent.tvCurrentTemperature.text = "${temperature?.toInt()}°"

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
            handleEditFavoriteClick()
        }
    }

    /**
     * 현재 위치 클릭 처리
     */
    private fun handleCurrentLocationClick() {
        refreshCurrentLocationWeather()
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
                    is CurrentWeatherFragment -> fragment.refreshWeatherData()
                    is WeatherHourlyForecastFragment -> fragment.refreshWeatherData()
                    is WeatherDailyFragment -> fragment.refreshWeatherData()
                    is AdditionalWeatherFragment -> fragment.refreshWeatherData()
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
    fun updateCurrentWeatherData(weatherData: Map<String, Any>) {
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
    }
}
