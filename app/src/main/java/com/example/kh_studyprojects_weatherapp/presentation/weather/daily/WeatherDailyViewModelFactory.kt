package com.example.kh_studyprojects_weatherapp.presentation.weather.daily

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import com.example.kh_studyprojects_weatherapp.domain.repository.weather.WeatherRepository
import com.example.kh_studyprojects_weatherapp.presentation.common.location.LocationManager

class WeatherDailyViewModelFactory(
    private val weatherRepository: WeatherRepository,
    private val locationManager: LocationManager
) : ViewModelProvider.Factory {
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        if (modelClass.isAssignableFrom(WeatherDailyViewModel::class.java)) {
            @Suppress("UNCHECKED_CAST")
            return WeatherDailyViewModel(weatherRepository, locationManager) as T
        }
        throw IllegalArgumentException("Unknown ViewModel class")
    }
} 