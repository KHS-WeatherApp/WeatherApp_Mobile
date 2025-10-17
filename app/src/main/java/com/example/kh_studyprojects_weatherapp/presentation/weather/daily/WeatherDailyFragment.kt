package com.example.kh_studyprojects_weatherapp.presentation.weather.daily

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.kh_studyprojects_weatherapp.databinding.WeatherDailyIncludeBinding
import com.example.kh_studyprojects_weatherapp.presentation.common.base.RefreshableFragment
import com.example.kh_studyprojects_weatherapp.presentation.common.base.collectUiState
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
class WeatherDailyFragment : Fragment(), RefreshableFragment {
    private var _binding: WeatherDailyIncludeBinding? = null
    private val binding: WeatherDailyIncludeBinding
        get() = _binding ?: throw IllegalStateException("Fragment binding is accessed before onCreateView or after onDestroyView")

    private val viewModel: WeatherDailyViewModel by viewModels()
    
    // 외부에서 접근 가능하도록 viewModel 속성 추가
    val viewModelInstance: WeatherDailyViewModel
        get() = viewModel

    //private val adapter = WeatherDailyAdapter()
    private lateinit var weatherDailyAdapter: WeatherDailyAdapter

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = WeatherDailyIncludeBinding.inflate(inflater, container, false)
        
        // 어댑터 초기화
        weatherDailyAdapter = WeatherDailyAdapter()
        
        setupButtons()
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        // RecyclerView 설정
        binding.weatherDailyRecyclerView.apply {
            layoutManager = LinearLayoutManager(context)
            adapter = weatherDailyAdapter
        }

        // UiState 기반 데이터 관찰
        setupWeatherDataObserver()
    }

    /**
     * ViewModel의 UiState를 관찰하고 상태에 따라 UI를 업데이트
     */
    private fun setupWeatherDataObserver() {
        collectUiState(viewModel) { data ->
            weatherDailyAdapter.submitListWithTime(data.weatherItems, data.currentApiTime)
        }
    }

    private fun setupButtons() {
        binding.apply {
            btnWeatherYesterday.setOnClickListener {
                viewModel.toggleYesterdayWeather()
            }
            btnWeatherForecast.setOnClickListener {
                viewModel.toggle15DaysWeather()
            }
        }
    }

    /**
     * 날씨 데이터 새로고침
     */
    override fun refreshWeatherData() {
        viewModel.refreshWeatherData()
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }

    companion object {
        fun newInstance() = WeatherDailyFragment()
    }
} 