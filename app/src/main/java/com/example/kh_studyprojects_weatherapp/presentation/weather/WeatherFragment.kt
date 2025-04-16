package com.example.kh_studyprojects_weatherapp.presentation.weather

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.navigation.findNavController
import com.example.kh_studyprojects_weatherapp.R
import com.example.kh_studyprojects_weatherapp.databinding.WeatherFragmentBinding
import com.example.kh_studyprojects_weatherapp.databinding.LayoutNavigationBottomBinding
import com.example.kh_studyprojects_weatherapp.presentation.weather.daily.WeatherDailyFragment
import com.example.kh_studyprojects_weatherapp.presentation.weather.hourly.WeatherHourlyForecastFragment
import com.example.kh_studyprojects_weatherapp.presentation.weather.current.CurrentWeatherFragment
import com.example.kh_studyprojects_weatherapp.presentation.weather.additional.AdditionalWeatherFragment

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

//    private val viewModel: WeatherViewModel by viewModels()

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        // 메인 레이아웃 바인딩 초기화
        _binding = WeatherFragmentBinding.inflate(inflater, container, false)

        // 하단 네비게이션 바인딩 초기화 (메인 바인딩에서 가져오기)
        _navigationBinding = LayoutNavigationBottomBinding.bind(_binding!!.root)

        // 네비게이션 클릭 리스너 설정
        setupNavigation()
        setupChildFragments(savedInstanceState)
        return binding.root
    }



    /*override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        // 날씨 데이터 관찰
        viewModel.weatherState.observe(viewLifecycleOwner) { state ->
            when (state) {
                is WeatherState.Loading -> {
                    // 로딩 처리
                }
                is WeatherState.Success -> {
                    Toast.makeText(context, "날씨 정보를 성공적으로 가져왔습니다.", Toast.LENGTH_SHORT).show()
                }
                is WeatherState.Error -> {
                    Toast.makeText(context, state.message, Toast.LENGTH_SHORT).show()
                }
            }
        }

        viewModel.fetchWeatherData()
    }*/

    /**
     * 하단 네비게이션 버튼들의 클릭 이벤트를 설정하는 메서드
     */
    private fun setupNavigation() {
        with(navigationBinding) {
            // 날씨 화면 전환 버튼 (현재 화면 새로고침)
            navWeather.setOnClickListener {
                it.findNavController().navigate(R.id.action_weatherFragment_self)
//                Toast.makeText(context, "날씨 화면으로 이동합니다.", Toast.LENGTH_SHORT).show()
            }
            // 설정 화면 전환 버튼
            navSetting.setOnClickListener {
                it.findNavController().navigate(R.id.action_weatherFragment_to_settingFragment)
//                Toast.makeText(context, "설정 화면으로 이동합니다.", Toast.LENGTH_SHORT).show()
            }
            // 미세먼지 화면 전환 버튼
            navFindust.setOnClickListener {
                it.findNavController().navigate(R.id.action_weatherFragment_to_finedustFragment)
//                Toast.makeText(context, "미세먼지 화면으로 이동합니다.", Toast.LENGTH_SHORT).show()
            }
        }
    }

    // 하위 프래그먼트 설정
    private fun setupChildFragments(savedInstanceState: Bundle?) {
        if (savedInstanceState == null) {
            childFragmentManager.beginTransaction()
                .replace(R.id.weather_hourly_forecast_fragment, WeatherHourlyForecastFragment())
                .commit()

            // 현재 날씨 프래그먼트 추가
            childFragmentManager.beginTransaction()
                .replace(R.id.weather_current_container, CurrentWeatherFragment())
                .commit()

            // 일별 예보 프래그먼트 추가
            childFragmentManager.beginTransaction()
            .replace(R.id.weather_daily_container, WeatherDailyFragment())
            .commit()

            // 기타 예보 프래그먼트 추가
            childFragmentManager.beginTransaction()
            .replace(R.id.weather_additional_container, AdditionalWeatherFragment())
            .commit()
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