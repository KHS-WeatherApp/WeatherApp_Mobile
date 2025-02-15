package com.example.kh_studyprojects_weatherapp.presentation.weather.daily

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.DiffUtil
import com.example.kh_studyprojects_weatherapp.databinding.ItemWeatherDailyOtherBinding
import com.example.kh_studyprojects_weatherapp.databinding.ItemWeatherDailyTodayBinding
import com.example.kh_studyprojects_weatherapp.databinding.ItemWeatherDailyYesterdayBinding
import com.example.kh_studyprojects_weatherapp.domain.model.weather.WeatherDailyItem

class WeatherDailyAdapter : ListAdapter<WeatherDailyItem, WeatherDailyViewHolder>(DiffCallback()) {
    
    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): WeatherDailyViewHolder {
        val inflater = LayoutInflater.from(parent.context)
        return when (WeatherDailyItem.Type.values()[viewType]) {
            WeatherDailyItem.Type.TODAY -> WeatherDailyViewHolder.Today(
                ItemWeatherDailyTodayBinding.inflate(inflater, parent, false)
            )
            WeatherDailyItem.Type.YESTERDAY -> WeatherDailyViewHolder.Yesterday(
                ItemWeatherDailyYesterdayBinding.inflate(inflater, parent, false)
            )
            WeatherDailyItem.Type.OTHER -> WeatherDailyViewHolder.Other(
                ItemWeatherDailyOtherBinding.inflate(inflater, parent, false)
            )
        }
    }

    override fun onBindViewHolder(holder: WeatherDailyViewHolder, position: Int) {
        holder.bind(getItem(position))
    }

    override fun getItemViewType(position: Int) = getItem(position).type.ordinal

    private class DiffCallback : DiffUtil.ItemCallback<WeatherDailyItem>() {
        override fun areItemsTheSame(oldItem: WeatherDailyItem, newItem: WeatherDailyItem) =
            oldItem.date == newItem.date && oldItem.type == newItem.type

        override fun areContentsTheSame(oldItem: WeatherDailyItem, newItem: WeatherDailyItem) =
            oldItem == newItem
    }
}