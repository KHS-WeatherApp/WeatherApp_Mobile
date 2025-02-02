package com.example.kh_studyprojects_weatherapp.presentation.weather

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.navigation.findNavController
import com.example.kh_studyprojects_weatherapp.R
import com.example.kh_studyprojects_weatherapp.databinding.WeatherFragmentBinding
import com.example.kh_studyprojects_weatherapp.databinding.LayoutNavigationBottomBinding

/**
 * 날씨 정보를 표시하는 Fragment
 */
class WeatherFragment : Fragment() {

    // 메인 레이아웃 바인딩
    private var _binding: WeatherFragmentBinding? = null
    private val binding get() = _binding!!
    
    // 하단 네비게이션 레이아웃 바인딩
    private var _navigationBinding: LayoutNavigationBottomBinding? = null
    private val navigationBinding get() = _navigationBinding!!

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        // 메인 레이아웃 바인딩 초기화
        _binding = WeatherFragmentBinding.inflate(inflater, container, false)
        
        // 하단 네비게이션 바인딩 초기화
        _navigationBinding = LayoutNavigationBottomBinding.bind(
            binding.root.findViewById(R.id.layout_navigation_bottom)
        )
        
        // 네비게이션 클릭 리스너 설정
        setupNavigation()

        return binding.root
    }

    /**
     * 하단 네비게이션 버튼들의 클릭 이벤트를 설정하는 메서드
     */
    private fun setupNavigation() {
        // 날씨 화면 전환 버튼 (현재 화면 새로고침)
        navigationBinding.navWeather.setOnClickListener {
            it.findNavController().navigate(R.id.action_weatherFragment_self)
            Toast.makeText(context, "날씨 화면으로 이동합니다.", Toast.LENGTH_SHORT).show()
        }

        // 설정 화면 전환 버튼
        navigationBinding.navSetting.setOnClickListener {
            it.findNavController().navigate(R.id.action_weatherFragment_to_settingFragment)
            Toast.makeText(context, "설정 화면으로 이동합니다.", Toast.LENGTH_SHORT).show()
        }

        // 미세먼지 화면 전환 버튼
        navigationBinding.navFindust.setOnClickListener {
            it.findNavController().navigate(R.id.action_weatherFragment_to_finedustFragment)
            Toast.makeText(context, "미세먼지 화면으로 이동합니다.", Toast.LENGTH_SHORT).show()
        }
    }

    /**
     * Fragment가 제거될 때 호출되는 메서드
     * 메모리 누수 방지를 위해 바인딩 객체들을 null로 초기화
     */
    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
        _navigationBinding = null
    }
}