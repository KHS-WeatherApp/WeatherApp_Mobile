package com.example.kh_studyprojects_weatherapp.presentation.weather.daily

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.DiffUtil
import com.example.kh_studyprojects_weatherapp.databinding.ItemWeatherDailyOtherBinding
import com.example.kh_studyprojects_weatherapp.databinding.ItemWeatherDailyTodayBinding
import com.example.kh_studyprojects_weatherapp.databinding.ItemWeatherDailyYesterdayBinding
import com.example.kh_studyprojects_weatherapp.data.model.weather.WeatherDailyDto

class WeatherDailyAdapter : ListAdapter<WeatherDailyDto, WeatherDailyViewHolder>(DiffCallback()) {
    
    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): WeatherDailyViewHolder {
        val inflater = LayoutInflater.from(parent.context)
        return when (WeatherDailyDto.Type.values()[viewType]) {
            WeatherDailyDto.Type.TODAY -> WeatherDailyViewHolder.Today(
                ItemWeatherDailyTodayBinding.inflate(inflater, parent, false)
            )
            WeatherDailyDto.Type.YESTERDAY -> WeatherDailyViewHolder.Yesterday(
                ItemWeatherDailyYesterdayBinding.inflate(inflater, parent, false)
            )
            WeatherDailyDto.Type.OTHER -> WeatherDailyViewHolder.Other(
                ItemWeatherDailyOtherBinding.inflate(inflater, parent, false)
            )
        }
    }

    // 🚀 1. API 시간을 저장할 변수 추가
    private var currentApiTime: String = ""

    // 🚀 2. List와 시간을 함께 받는 새로운 submitList 함수 추가
    fun submitListWithTime(list: List<WeatherDailyDto>, currentApiTime: String) {
        this.currentApiTime = currentApiTime
        submitList(list)
    }

    override fun onBindViewHolder(holder: WeatherDailyViewHolder, position: Int) {
        //holder.bind(getItem(position))
        holder.bind(getItem(position), currentApiTime)
    }

    override fun getItemViewType(position: Int) = getItem(position).type.ordinal

    private class DiffCallback : DiffUtil.ItemCallback<WeatherDailyDto>() {
        override fun areItemsTheSame(oldItem: WeatherDailyDto, newItem: WeatherDailyDto) =
            oldItem.date == newItem.date && oldItem.type == newItem.type

        override fun areContentsTheSame(oldItem: WeatherDailyDto, newItem: WeatherDailyDto) =
            oldItem == newItem
    }
}