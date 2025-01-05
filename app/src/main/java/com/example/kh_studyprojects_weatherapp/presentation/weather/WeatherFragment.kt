package com.example.kh_studyprojects_weatherapp.presentation.weather

import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import android.widget.Toast
import androidx.constraintlayout.widget.ConstraintLayout
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.lifecycle.lifecycleScope
import androidx.navigation.findNavController
import com.example.kh_studyprojects_weatherapp.R
import kotlinx.coroutines.launch

// weather/WeatherFragment.kt
class WeatherFragment : Fragment() {
//    private val viewModel: WeatherViewModel by viewModels()
    private lateinit var weatherInfoTextView: TextView

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        val view = inflater.inflate(R.layout.weather_fragment, container, false)
        setupNavigation(view)
//        setupChildFragments(savedInstanceState)
        return view
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
                    // UI 업데이트
                }
                is WeatherState.Error -> {
                    Toast.makeText(context, state.message, Toast.LENGTH_SHORT).show()
                }
            }
        }

        // 날씨 데이터 요청
        viewModel.fetchWeatherData()
    }*/

    /*네비게이션*/
    private fun setupNavigation(view: View) {
        view.findViewById<ConstraintLayout>(R.id.clNav01).setOnClickListener {
            Toast.makeText(context, "날씨 화면으로 이동합니다.", Toast.LENGTH_SHORT).show()
            it.findNavController().navigate(R.id.action_finedustFragment_to_weatherFragment)
        }

        view.findViewById<ConstraintLayout>(R.id.clNavOval).setOnClickListener {
            Toast.makeText(context, "설정 화면으로 이동합니다.", Toast.LENGTH_SHORT).show()
            it.findNavController().navigate(R.id.action_finedustFragment_to_settingFragment)
        }

        view.findViewById<ConstraintLayout>(R.id.clNav03).setOnClickListener {
            Toast.makeText(context, "미세먼지 화면으로 이동합니다.", Toast.LENGTH_SHORT).show()
            it.findNavController().navigate(R.id.action_finedustFragment_self)
        }
    }

    /*프레그먼트 동기화*/
   /* private fun setupChildFragments(savedInstanceState: Bundle?) {
        if (savedInstanceState == null) {
            childFragmentManager.beginTransaction()
                .replace(R.id.weather_hourly_forecast_fragment, WeatherHourlyForecastFragment())
                .commit()

            childFragmentManager.beginTransaction()
                .replace(R.id.weather_daily_forecast_fragment, WeatherDailyForecastFragment())
                .commit()
        }
    }*/
}