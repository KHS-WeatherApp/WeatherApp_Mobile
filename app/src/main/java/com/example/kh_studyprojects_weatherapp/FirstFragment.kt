package com.example.kh_studyprojects_weatherapp

import android.os.Bundle
import androidx.fragment.app.Fragment
import androidx.lifecycle.ViewModelProvider
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import androidx.recyclerview.widget.DividerItemDecoration
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.ImageButton
import android.util.Log
import androidx.navigation.findNavController
import androidx.core.view.ViewCompat
import com.example.kh_studyprojects_weatherapp.adapter.WeatherAdapter
import com.example.kh_studyprojects_weatherapp.R
import com.example.kh_studyprojects_weatherapp.WeatherViewModel

class FirstFragment : Fragment() {

    private lateinit var viewModel: WeatherViewModel
    private lateinit var adapter: WeatherAdapter

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        val view = inflater.inflate(R.layout.fragment_first, container, false)
        // Activity의 ViewModel 참조
        viewModel = (activity as MainActivity).weatherViewModel

        // RecyclerView 설정
        val recyclerView: RecyclerView = view.findViewById(R.id.recycler_view)
        recyclerView.apply {
            layoutManager = object : LinearLayoutManager(requireContext()) {
                override fun canScrollVertically(): Boolean = false
                override fun isAutoMeasureEnabled(): Boolean = true
                
                // 이 부분 추가
                override fun onLayoutChildren(recycler: RecyclerView.Recycler?, state: RecyclerView.State?) {
                    try {
                        super.onLayoutChildren(recycler, state)
                    } catch (e: IndexOutOfBoundsException) {
                        e.printStackTrace()
                    }
                }
            }
            
            // 기존 설정 유지
            setHasFixedSize(false)
            adapter = WeatherAdapter(mutableListOf()).also { adapter = it }
            addItemDecoration(DividerItemDecoration(context, LinearLayoutManager.VERTICAL))
            ViewCompat.setNestedScrollingEnabled(this, false)
        }


        adapter = WeatherAdapter(mutableListOf())
        recyclerView.adapter = adapter

        // ViewModel 초기화
        //viewModel = ViewModelProvider(this)[WeatherViewModel::class.java]

        // LiveData 관찰 부분도 수정
        viewModel.weatherData.observe(viewLifecycleOwner) { weatherData ->
            adapter.updateWeatherData(weatherData)
            // post 대신 직접 호출
            recyclerView.requestLayout()
        }

        // API 호출 파라미터 설정 - 전체 데이터를 한 번에 가져옴
        // val params: Map<String, Any> = mapOf(
        //     "latitude" to 37.55,
        //     "longitude" to 126.9375,
        //     "queryParam" to "current=temperature_2m,relative_humidity_2m,apparent_temperature,is_day,precipitation,weather_code&hourly=temperature_2m,apparent_temperature,precipitation_probability,precipitation,weather_code&daily=weather_code,temperature_2m_max,temperature_2m_min,apparent_temperature_max,apparent_temperature_min,sunrise,sunset,uv_index_max,precipitation_sum,precipitation_probability_max,wind_speed_10m_max&timezone=auto&past_days=1&forecast_days=14"
        // )
        // viewModel.fetchWeatherData(params)
        // 데이터가 없을 때만 API 호출
        if (viewModel.weatherData.value.isNullOrEmpty()) {
            val params: Map<String, Any> = mapOf(
                "latitude" to 37.55,
                "longitude" to 126.9375,
                "queryParam" to "current=temperature_2m,relative_humidity_2m,apparent_temperature,is_day,precipitation,weather_code&hourly=temperature_2m,apparent_temperature,precipitation_probability,precipitation,weather_code&daily=weather_code,temperature_2m_max,temperature_2m_min,apparent_temperature_max,apparent_temperature_min,sunrise,sunset,uv_index_max,precipitation_sum,precipitation_probability_max,wind_speed_10m_max&timezone=auto&past_days=1&forecast_days=15"
            )
            viewModel.fetchWeatherData(params)
        }

        // 어제 날씨 버튼 클릭 시
        view.findViewById<Button>(R.id.show_yesterday_button).setOnClickListener {
            viewModel.showYesterdayWeather()
        }

        // 15일 예보 보기 버튼 클릭 시 (토글 방식)
        view.findViewById<Button>(R.id.show_15_days_button).setOnClickListener {
            viewModel.toggle15DaysWeather()
        }

        // 네비게이션 버튼들 설정
        view.findViewById<ImageButton>(R.id.icon_center_button).setOnClickListener {
            it.findNavController().navigate(R.id.action_firstFragment_to_secondFragment)
        }

        view.findViewById<ImageButton>(R.id.icon_right).setOnClickListener {
            it.findNavController().navigate(R.id.action_firstFragment_to_thirdFragment)
        }

        return view
    }
}


