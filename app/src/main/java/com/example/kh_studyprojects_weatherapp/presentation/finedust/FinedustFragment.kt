package com.example.kh_studyprojects_weatherapp.presentation.finedust

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import com.example.kh_studyprojects_weatherapp.R
import com.example.kh_studyprojects_weatherapp.databinding.FinedustFragmentBinding
import com.example.kh_studyprojects_weatherapp.presentation.common.base.BaseNavigationFragment

/**
 * 미세먼지 정보를 표시하는 Fragment
 */
class FinedustFragment : BaseNavigationFragment() {

    // 메인 레이아웃 바인딩
    private var _binding: FinedustFragmentBinding? = null
    private val binding get() = _binding!!

    /**
     * Fragment의 View를 생성하고 초기화하는 메서드
     */
    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        // 메인 레이아웃 바인딩 초기화
        _binding = FinedustFragmentBinding.inflate(inflater, container, false)

        // 하단 네비게이션 바인딩 초기화 (BaseNavigationFragment에서 제공)
        setupNavigationBinding(binding.root)

        // 네비게이션 클릭 리스너 설정
        setupBottomNavigation(
            weatherDestination = R.id.weatherFragment,
            settingDestination = R.id.settingFragment,
            finedustDestination = R.id.finedustFragment
        )

        return binding.root
    }

    /**
     * Fragment가 제거될 때 호출되는 메서드
     * 메모리 누수 방지를 위해 바인딩 객체를 null로 초기화
     */
    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}