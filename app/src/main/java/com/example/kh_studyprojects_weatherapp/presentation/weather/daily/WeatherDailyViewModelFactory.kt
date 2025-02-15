package com.example.kh_studyprojects_weatherapp.presentation.weather.daily

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import com.example.kh_studyprojects_weatherapp.domain.repository.weather.WeatherRepository

class WeatherDailyViewModelFactory(
    private val weatherRepository: WeatherRepository
) : ViewModelProvider.Factory {
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        if (modelClass.isAssignableFrom(WeatherDailyViewModel::class.java)) {
            @Suppress("UNCHECKED_CAST")
            return WeatherDailyViewModel(weatherRepository) as T
        }
        throw IllegalArgumentException("Unknown ViewModel class")
    }
} 