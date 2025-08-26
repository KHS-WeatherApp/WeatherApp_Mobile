package com.example.kh_studyprojects_weatherapp.presentation.common.sidemenu

import android.view.View
import android.view.ViewGroup
import android.view.animation.DecelerateInterpolator
import android.animation.ValueAnimator
import android.animation.AnimatorListenerAdapter
import android.animation.Animator
import android.view.MotionEvent
import android.view.inputmethod.InputMethodManager
import com.example.kh_studyprojects_weatherapp.databinding.ActivityMainBinding
import android.util.Log

/**
 * 사이드메뉴 애니메이션을 관리하는 Manager 클래스
 * 
 * 검색창의 확장/축소, 드래그 제스처, 높이 애니메이션 등을 담당합니다.
 * 사용자 경험을 향상시키는 부드러운 UI 전환을 제공합니다.
 * 
 * @author 김효동
 * @since 2025.08.26
 * @version 1.0
 */
class SmAnimationManager(
    private val binding: ActivityMainBinding
) {
    /**
     * 검색창을 위로 확장하는 애니메이션을 실행합니다.
     * 검색창이 즐겨찾기 헤더를 완전히 덮을 때까지 부드럽게 확장됩니다.
     * 
     * @param searchContainer 확장할 검색창 컨테이너
     * @param favoriteHeader 덮을 즐겨찾기 헤더
     */
    fun animateSearchContainerUp(searchContainer: View, favoriteHeader: View) {
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
                        val imm = binding.root.context.getSystemService(InputMethodManager::class.java)
                        imm.showSoftInput(binding.sideMenuContent.etSearchLocation, InputMethodManager.SHOW_IMPLICIT)
                    }, 100)
                }
            })

            heightAnimator.start()
        }
    }

    /**
     * 검색창 드래그 리스너를 설정합니다.
     * 사용자가 검색창을 드래그하여 높이를 조절할 수 있게 합니다.
     * 
     * 주요 기능:
     * - 드래그 시작/이동/종료 감지
     * - 실시간 높이 조절
     * - 키보드 자동 숨김/표시
     * - 드래그 방향에 따른 최종 상태 결정
     * 
     * @param searchContainer 드래그할 검색창 컨테이너
     * @param favoriteHeader 즐겨찾기 헤더 (높이 계산용)
     */
    fun setupSearchDragListener(searchContainer: View, favoriteHeader: View) {
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
                            val imm = binding.root.context.getSystemService(InputMethodManager::class.java)
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

    fun animateToMaxHeight(searchContainer: View, favoriteHeader: View) {
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
                    val imm = binding.root.context.getSystemService(InputMethodManager::class.java)
                    imm.showSoftInput(binding.sideMenuContent.etSearchLocation, InputMethodManager.SHOW_IMPLICIT)
                }, 100)
            }
        })

        heightAnimator.start()
    }

    fun animateToMinHeight(searchContainer: View) {
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
                val imm = binding.root.context.getSystemService(InputMethodManager::class.java)
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

    fun animateToOriginalHeight(searchContainer: View, originalHeight: Int) {
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

    fun resetSearchContainer(searchContainer: View, favoriteHeader: View) {
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
                val imm = binding.root.context.getSystemService(InputMethodManager::class.java)
                imm.hideSoftInputFromWindow(binding.sideMenuContent.etSearchLocation.windowToken, 0)
            }
        })

        heightAnimator.start()
    }
}
