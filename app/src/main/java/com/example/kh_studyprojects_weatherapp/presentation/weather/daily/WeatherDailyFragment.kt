package com.example.kh_studyprojects_weatherapp.presentation.weather.daily

import android.location.Geocoder
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
import com.example.kh_studyprojects_weatherapp.data.repository.weather.WeatherRepositoryImpl
import com.example.kh_studyprojects_weatherapp.databinding.WeatherDailyIncludeBinding
import com.example.kh_studyprojects_weatherapp.presentation.location.LocationManager
import com.google.android.material.snackbar.Snackbar
import kotlinx.coroutines.launch
import kotlinx.coroutines.flow.catch  // 이 import 추가
import dagger.hilt.android.AndroidEntryPoint
import java.util.Locale

@AndroidEntryPoint
class WeatherDailyFragment : Fragment() {
    private var _binding: WeatherDailyIncludeBinding? = null
    private val binding get() = _binding!!

    private val viewModel: WeatherDailyViewModel by viewModels {
        WeatherDailyViewModelFactory(
            WeatherRepositoryImpl.getInstance(),
            LocationManager(
                requireContext(),
                Geocoder(requireContext(), Locale.getDefault())
            )
        )
    }

    private val adapter = WeatherDailyAdapter()

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = WeatherDailyIncludeBinding.inflate(inflater, container, false)
        setupButtons()
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        // RecyclerView 설정
        binding.weatherDailyRecyclerView.apply {
            adapter = this@WeatherDailyFragment.adapter
            layoutManager = LinearLayoutManager(context)
        }

        // ViewModel의 데이터 관찰
        viewLifecycleOwner.lifecycleScope.launch {
            viewLifecycleOwner.repeatOnLifecycle(Lifecycle.State.STARTED) {
                try {
                    viewModel.weatherItems.collect { items ->
                        println("Submitting items to adapter: $items")
                        adapter.submitList(items)
                    }
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