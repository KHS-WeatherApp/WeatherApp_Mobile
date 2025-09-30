package com.example.kh_studyprojects_weatherapp.presentation.common.base

import android.view.View
import androidx.annotation.IdRes
import androidx.fragment.app.Fragment
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
     * @param weatherAction 날씨 화면으로 이동하는 Navigation Action ID
     * @param settingAction 설정 화면으로 이동하는 Navigation Action ID
     * @param finedustAction 미세먼지 화면으로 이동하는 Navigation Action ID
     */
    protected fun setupBottomNavigation(
        @IdRes weatherAction: Int,
        @IdRes settingAction: Int,
        @IdRes finedustAction: Int
    ) {
        navigationBinding.apply {
            navWeather.setOnClickListener {
                it.findNavController().navigate(weatherAction)
            }
            navSetting.setOnClickListener {
                it.findNavController().navigate(settingAction)
            }
            navFindust.setOnClickListener {
                it.findNavController().navigate(finedustAction)
            }
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