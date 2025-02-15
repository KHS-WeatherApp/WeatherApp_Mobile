package com.example.kh_studyprojects_weatherapp.presentation.weather.daily

import androidx.recyclerview.widget.RecyclerView
import com.example.kh_studyprojects_weatherapp.R
import com.example.kh_studyprojects_weatherapp.databinding.ItemWeatherDailyTodayBinding
import com.example.kh_studyprojects_weatherapp.databinding.ItemWeatherDailyYesterdayBinding
import com.example.kh_studyprojects_weatherapp.databinding.ItemWeatherDailyOtherBinding
import com.example.kh_studyprojects_weatherapp.domain.model.weather.WeatherDailyDto

sealed class WeatherDailyViewHolder(
    private val binding: androidx.viewbinding.ViewBinding
) : RecyclerView.ViewHolder(binding.root) {

    abstract fun bind(item: WeatherDailyDto)

    class Today(
        private val binding: ItemWeatherDailyTodayBinding
    ) : WeatherDailyViewHolder(binding) {
        override fun bind(item: WeatherDailyDto) {
            binding.apply {
                textWeek.text = item.week
                textPrecipitation.text = item.precipitation
                textHumidity.text = item.humidity
                textMinTemp.text = item.minTemp
                textMaxTemp.text = item.maxTemp
                imageWeather.setImageResource(getWeatherIcon(item.weatherCode))
                // 옷 이미지는 온도에 따라 설정하는 로직 추가 필요
            }
        }
    }

    class Other(
        private val binding: ItemWeatherDailyOtherBinding
    ) : WeatherDailyViewHolder(binding) {
        override fun bind(item: WeatherDailyDto) {
            binding.apply {
                textWeek.text = item.week
                textDate.text = item.date
                textPrecipitation.text = item.precipitation
                textHumidity.text = item.humidity
                textMinTemp.text = item.minTemp
                textMaxTemp.text = item.maxTemp
                imageWeather.setImageResource(getWeatherIcon(item.weatherCode))
                // 옷 이미지는 온도에 따라 설정하는 로직 추가 필요
            }
        }
    }

    class Yesterday(
        private val binding: ItemWeatherDailyYesterdayBinding
    ) : WeatherDailyViewHolder(binding) {
        override fun bind(item: WeatherDailyDto) {
            binding.apply {
                textWeek.text = item.week
                textMinTemp.text = item.minTemp
                textMaxTemp.text = item.maxTemp
            }
        }
    }

    protected fun getWeatherIcon(weatherCode: Int): Int {
        return when (weatherCode) {
            0 -> R.drawable.weather_icon_sun
            1, 2, 3 -> R.drawable.weather_icon_cloudy
            45, 48 -> R.drawable.weather_icon_fog
            51, 53, 55, 56, 57 -> R.drawable.weather_icon_raining
            61, 63, 65, 66, 67 -> R.drawable.weather_icon_shower
            71, 73, 75, 77 -> R.drawable.weather_icon_snow
            95, 96, 99 -> R.drawable.weather_icon_thunder
            else -> R.drawable.weather_icon_unknown
        }
    }

    protected fun getClothingIcon(temperature: Double): Int {
        return when {
            temperature >= 28 -> R.drawable.clothing_icon_hawaiianshirt
            temperature >= 23 -> R.drawable.clothing_icon_hawaiianshirt
            temperature >= 20 -> R.drawable.clothing_icon_hawaiianshirt
            temperature >= 17 -> R.drawable.clothing_icon_hawaiianshirt
            temperature >= 12 -> R.drawable.clothing_icon_hawaiianshirt
            temperature >= 9 -> R.drawable.clothing_icon_hawaiianshirt
            else -> R.drawable.clothing_icon_hawaiianshirt
        }
    }
}