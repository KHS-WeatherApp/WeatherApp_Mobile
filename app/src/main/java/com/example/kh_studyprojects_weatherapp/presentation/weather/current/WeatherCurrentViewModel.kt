package com.example.kh_studyprojects_weatherapp.presentation.weather.current

import com.example.kh_studyprojects_weatherapp.domain.model.weather.WeatherCurrent
import com.example.kh_studyprojects_weatherapp.domain.repository.weather.WeatherRepository
import com.example.kh_studyprojects_weatherapp.presentation.common.base.BaseLoadViewModel
import com.example.kh_studyprojects_weatherapp.presentation.common.location.EffectiveLocationResolver
import dagger.hilt.android.lifecycle.HiltViewModel
import javax.inject.Inject

@HiltViewModel
class WeatherCurrentViewModel @Inject constructor(
    private val weatherRepository: WeatherRepository,
    private val effectiveLocationResolver: EffectiveLocationResolver
) : BaseLoadViewModel<WeatherCurrent>() {

    init {
        loadInitial { fetch() }
    }

    private suspend fun fetch(): Result<WeatherCurrent> {
        val loc = effectiveLocationResolver.resolve()
        val lat = loc.latitude
        val lon = loc.longitude
        val locationText = loc.address

        return weatherRepository.getCurrentWeather(lat, lon)
            .map { model -> model.copy(location = locationText) }
    }

    fun refreshWeatherData() = load { fetch() }
}
