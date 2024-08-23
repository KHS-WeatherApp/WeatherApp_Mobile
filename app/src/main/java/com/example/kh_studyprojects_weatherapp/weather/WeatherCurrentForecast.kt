package com.example.kh_studyprojects_weatherapp.weather

import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import com.example.kh_studyprojects_weatherapp.R

/*날씨 화면 현재 날씨 및 온도, 추천 옷*/
class WeatherCurrentForecast : Fragment() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        // Inflate the layout for this fragment
        return inflater.inflate(R.layout.weather_current_forecast_fragment, container, false)
    }
}