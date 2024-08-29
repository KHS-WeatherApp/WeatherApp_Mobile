package com.example.kh_studyprojects_weatherapp.weather

import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.kh_studyprojects_weatherapp.R
import com.example.kh_studyprojects_weatherapp.databinding.WeatherDailyForecastFragmentBinding
import com.example.kh_studyprojects_weatherapp.databinding.WeatherHourlyForecastFragmentBinding


class WeatherDailyForecastFragment : Fragment() {

    private var _binding: WeatherDailyForecastFragmentBinding? = null
    private val binding get() = _binding!!
    private lateinit var adapter: WeatherDailyForecastAdapter

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        _binding = WeatherDailyForecastFragmentBinding.inflate(inflater, container, false)
        val root: View = binding.root

        adapter = WeatherDailyForecastAdapter(requireContext())
        binding.clDaily02.layoutManager = LinearLayoutManager(context)
        binding.clDaily02.adapter = adapter

        return root
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}