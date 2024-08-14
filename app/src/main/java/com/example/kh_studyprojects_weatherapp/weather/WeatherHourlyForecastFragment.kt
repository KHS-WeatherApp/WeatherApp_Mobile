package com.example.kh_studyprojects_weatherapp.weather

import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.constraintlayout.helper.widget.Carousel.Adapter
import com.example.kh_studyprojects_weatherapp.R
import com.example.kh_studyprojects_weatherapp.databinding.WeatherHourlyForecastFragmentBinding

class WeatherHourlyForecastFragment : Fragment() {

    private var _binding : WeatherHourlyForecastFragmentBinding?= null
    private  val binding get() = _binding!!
    private lateinit var adapter: WeatherHourlyForecastFragmentAdapter

//    private lateinit var adapter:


    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = WeatherHourlyForecastFragmentBinding.inflate(inflater, container, false)
        val root: View = binding.root

        adapter = WeatherHourlyForecastFragmentAdapter(requireContext())
        binding.clHourly02.adapter = adapter

        return root

    }
}