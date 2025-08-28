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
import kotlinx.coroutines.launch
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.flow.collectLatest

@AndroidEntryPoint
class WeatherDailyFragment : Fragment() {
    private var _binding: WeatherDailyIncludeBinding? = null
    private val binding get() = _binding!!

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

        // ViewModel의 데이터 관찰
        viewLifecycleOwner.lifecycleScope.launch {
            viewLifecycleOwner.repeatOnLifecycle(Lifecycle.State.STARTED) {
                try {
                    // 🚀 1. weatherItems와 currentApiTime을 모두 관찰
                    // 두 Flow를 zip() 등으로 묶어서 한 번에 처리하는 것이 효율적입니다.
                    viewModel.weatherItems.collectLatest { dailyItems ->
                        // 🚀 2. `currentApiTime`이 업데이트될 때까지 대기
                        viewModel.currentApiTime.collectLatest { currentApiTime ->
                            if (currentApiTime != null) {
                                // 🚀 3. 어댑터에 데이터와 함께 API 시간을 전달
                                weatherDailyAdapter.submitListWithTime(dailyItems, currentApiTime)
                            }
                        }
                    }
//                    viewModel.weatherItems.collect { items ->
//                        println("Submitting items to adapter: $items")
//                        adapter.submitList(items)
//                    }
                } catch (e: Exception) {
                    println("Error collecting weather items: ${e.message}")
                    e.printStackTrace()
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
    fun refreshWeatherData() {
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