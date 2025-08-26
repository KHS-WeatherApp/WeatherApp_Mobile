package com.example.kh_studyprojects_weatherapp.presentation.common.sideMenu

import android.content.Context
import android.view.View
import android.view.ViewGroup
import android.view.inputmethod.EditorInfo
import android.view.inputmethod.InputMethodManager
import android.widget.Toast
import androidx.lifecycle.LifecycleCoroutineScope
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.kh_studyprojects_weatherapp.databinding.ActivityMainBinding
import com.example.kh_studyprojects_weatherapp.presentation.weather.adapter.SearchResultAdapter
import com.example.kh_studyprojects_weatherapp.data.api.ExternalApiRetrofitInstance
import com.example.kh_studyprojects_weatherapp.presentation.common.sideMenu.SideMenuAnimationManager
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import retrofit2.HttpException
import java.io.IOException
import android.text.Editable
import android.text.TextWatcher
import androidx.drawerlayout.widget.DrawerLayout

class SideMenuSearchManager(
    private val context: Context,
    private val binding: ActivityMainBinding,
    private val lifecycleScope: LifecycleCoroutineScope,
    private val searchResultAdapter: SearchResultAdapter
) {
    private var searchJob: Job? = null
    private var currentSearchQuery: String = ""
    private var currentSearchPage: Int = 1
    private var isSearchLoading: Boolean = false
    private var isSearchEnd: Boolean = false

    private lateinit var sideMenuAnimationManager: SideMenuAnimationManager

    fun setAnimationManager(animationManager: SideMenuAnimationManager) {
        this.sideMenuAnimationManager = animationManager
    }

    fun setupSearchFocusListeners() {
        val searchEditText = binding.sideMenuContent.etSearchLocation
        val searchContainer = binding.sideMenuContent.llSearchContainer
        val favoriteHeader = binding.sideMenuContent.llFavoriteHeader
        val clearButton = binding.sideMenuContent.ivClearSearch

        // 검색창 포커스 리스너
        searchEditText.setOnFocusChangeListener { _, hasFocus ->
            if (hasFocus) {
                // 포커스 획득 시: 검색창을 확장 애니메이션
                sideMenuAnimationManager.animateSearchContainerUp(searchContainer, favoriteHeader)
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

    fun setupSearchResultsRecyclerView() {
        val recyclerView = binding.sideMenuContent.rvSearchResults

        recyclerView.apply {
            layoutManager = LinearLayoutManager(context)
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

    fun performSearch(query: String) {
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
                }
            } catch (e: HttpException) {
                val code = e.code()
                val msg = e.response()?.errorBody()?.string().orEmpty()
                android.util.Log.e("KakaoAddressSearch", "HTTP $code: $msg", e)
                showLoadingIndicator(false)
                showErrorMessage("검색 오류 (HTTP $code)")
            } catch (e: IOException) {
                android.util.Log.e("KakaoAddressSearch", "네트워크 오류", e)
                showLoadingIndicator(false)
                showErrorMessage("네트워크 오류가 발생했습니다")
            } catch (e: Exception) {
                android.util.Log.e("KakaoAddressSearch", "기타 오류", e)
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

    fun loadNextSearchPage() {
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
                android.util.Log.e("KakaoAddressSearch", "HTTP $code: $msg", e)
                showLoadingIndicator(false)
                showErrorMessage("검색 오류 (HTTP $code)")
            } catch (e: IOException) {
                android.util.Log.e("KakaoAddressSearch", "네트워크 오류", e)
                showLoadingIndicator(false)
                showErrorMessage("네트워크 오류가 발생했습니다")
            } catch (e: Exception) {
                android.util.Log.e("KakaoAddressSearch", "기타 오류", e)
                showLoadingIndicator(false)
                showErrorMessage("검색 중 오류가 발생했습니다")
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

    private fun setupSearchDragListener(searchContainer: View, favoriteHeader: View) {
        // 이 메서드는 SideMenuAnimationManager에서 처리하므로 여기서는 호출만
        sideMenuAnimationManager.setupSearchDragListener(searchContainer, favoriteHeader)
    }

    private fun setupDrawerListener(searchContainer: View, favoriteHeader: View) {
        // DrawerLayout 리스너 설정
        val drawerLayout = binding.drawerLayout
        drawerLayout.addDrawerListener(object : DrawerLayout.DrawerListener {
            override fun onDrawerSlide(drawerView: View, slideOffset: Float) {
                // 드로어가 슬라이드되는 동안
            }

            override fun onDrawerOpened(drawerView: View) {
                // 드로어가 열렸을 때
            }

            override fun onDrawerClosed(drawerView: View) {
                // 드로어가 닫혔을 때 검색창 초기화
                sideMenuAnimationManager.resetSearchContainer(searchContainer, favoriteHeader)
            }

            override fun onDrawerStateChanged(newState: Int) {
                // 드로어 상태 변경
            }
        })
    }
}
