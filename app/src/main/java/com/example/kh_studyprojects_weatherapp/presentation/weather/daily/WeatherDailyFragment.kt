package com.example.kh_studyprojects_weatherapp.presentation.weather.daily

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.lifecycleScope
import androidx.lifecycle.repeatOnLifecycle
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.kh_studyprojects_weatherapp.databinding.WeatherDailyIncludeBinding
import com.example.kh_studyprojects_weatherapp.presentation.common.base.RefreshableFragment
import kotlinx.coroutines.launch
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.flow.collectLatest

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
            //adapter = this@WeatherDailyFragment.weatherDailyAdapter
            layoutManager = LinearLayoutManager(context)
            adapter = weatherDailyAdapter
        }

        // ViewModel의 데이터 관찰 - combine으로 두 Flow를 효율적으로 수집
        viewLifecycleOwner.lifecycleScope.launch {
            viewLifecycleOwner.repeatOnLifecycle(Lifecycle.State.STARTED) {
                kotlinx.coroutines.flow.combine(
                    viewModel.weatherItems,
                    viewModel.currentApiTime
                ) { items, apiTime ->
                    items to apiTime
                }.collect { (items, apiTime) ->
                    apiTime?.let { time ->
                        weatherDailyAdapter.submitListWithTime(items, time)
                    }
                }
            }
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