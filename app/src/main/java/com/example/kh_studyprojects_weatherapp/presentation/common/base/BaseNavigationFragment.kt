package com.example.kh_studyprojects_weatherapp.presentation.common.base

import android.view.View
import androidx.annotation.IdRes
import androidx.fragment.app.Fragment
import androidx.navigation.NavOptions
import androidx.navigation.findNavController
import com.example.kh_studyprojects_weatherapp.R
import com.example.kh_studyprojects_weatherapp.databinding.LayoutNavigationBottomBinding

/**
 * 하단 네비게이션을 포함하는 Fragment들의 Base 클래스
 *
 * 중복되는 네비게이션 설정 코드를 공통화하여
 * WeatherFragment, SettingFragment, FinedustFragment에서 재사용합니다.
 *
 * @author 김효동
 * @since 2025.09.30
 * @version 1.0
 */
abstract class BaseNavigationFragment : Fragment() {

    /**
     * 하단 네비게이션 바인딩
     * 자식 클래스에서 접근 가능
     */
    protected var _navigationBinding: LayoutNavigationBottomBinding? = null
    protected val navigationBinding: LayoutNavigationBottomBinding
        get() = _navigationBinding ?: throw IllegalStateException(
            "Navigation binding is accessed before setupNavigationBinding or after onDestroyView"
        )

    /**
     * 네비게이션 바인딩 초기화
     * 자식 Fragment의 onCreateView에서 호출해야 합니다.
     *
     * @param rootView Fragment의 root view
     */
    protected fun setupNavigationBinding(rootView: View) {
        _navigationBinding = LayoutNavigationBottomBinding.bind(
            rootView.findViewById(R.id.included_navigation_bottom)
        )
    }

    /**
     * 하단 네비게이션 버튼 클릭 이벤트 설정
     *
     * @param weatherDestination 날씨 화면 목적지 ID (R.id.weatherFragment)
     * @param settingDestination 설정 화면 목적지 ID (R.id.settingFragment)
     * @param finedustDestination 미세먼지 화면 목적지 ID (R.id.finedustFragment)
     */
    protected fun setupBottomNavigation(
        @IdRes weatherDestination: Int,
        @IdRes settingDestination: Int,
        @IdRes finedustDestination: Int
    ) {
        navigationBinding.apply {
            navWeather.setOnClickListener {
                navigateToDestination(it, weatherDestination)
            }
            navSetting.setOnClickListener {
                navigateToDestination(it, settingDestination)
            }
            navFindust.setOnClickListener {
                navigateToDestination(it, finedustDestination)
            }
        }
    }

    /**
     * 목적지로 안전하게 이동
     * - 현재 목적지와 동일하면 무시 (중복 방지)
     * - launchSingleTop으로 단일 인스턴스 유지
     *
     * @param view 클릭된 뷰
     * @param destinationId 이동할 목적지 ID
     */
    private fun navigateToDestination(view: View, @IdRes destinationId: Int) {
        val navController = view.findNavController()

        // 이미 현재 목적지면 무시
        if (navController.currentDestination?.id == destinationId) {
            return
        }

        val navOptions = NavOptions.Builder()
            .setLaunchSingleTop(true)
            .setPopUpTo(R.id.main_nav, inclusive = false, saveState = true)
            .setRestoreState(true)
            .build()

        try {
            navController.navigate(destinationId, null, navOptions)
        } catch (e: IllegalArgumentException) {
            // 목적지를 찾을 수 없는 경우 무시 (빠른 중복 클릭 방지)
        }
    }

    /**
     * Fragment가 제거될 때 바인딩 정리
     */
    override fun onDestroyView() {
        super.onDestroyView()
        _navigationBinding = null
    }
}