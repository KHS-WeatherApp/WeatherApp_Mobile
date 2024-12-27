package com.example.kh_studyprojects_weatherapp.adapter

import android.view.LayoutInflater
import android.view.View.GONE
import android.view.View.VISIBLE
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.example.kh_studyprojects_weatherapp.R
import com.example.kh_studyprojects_weatherapp.data.WeatherData
import com.example.kh_studyprojects_weatherapp.type.ViewType
import com.example.kh_studyprojects_weatherapp.viewholder.OtherViewHolder
import com.example.kh_studyprojects_weatherapp.viewholder.TodayViewHolder
import com.example.kh_studyprojects_weatherapp.viewholder.YesterdayViewHolder

class WeatherAdapter(private var weatherList: MutableList<WeatherData>) : RecyclerView.Adapter<RecyclerView.ViewHolder>() {

    override fun getItemViewType(position: Int): Int {
        return weatherList[position].type.typeValue
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): RecyclerView.ViewHolder {
        val inflater = LayoutInflater.from(parent.context)
        val viewTypeEnum = ViewType.fromOrdinal(viewType)
        return when (viewTypeEnum) {
            ViewType.YESTERDAY -> {
                val view = inflater.inflate(R.layout.yesterday_box, parent, false)
                YesterdayViewHolder(view)
            }
            ViewType.TODAY -> {
                val view = inflater.inflate(R.layout.today_box, parent, false)
                TodayViewHolder(view)
            }
            else -> {
                val view = inflater.inflate(R.layout.other_box, parent, false)
                OtherViewHolder(view)
            }
        }
    }

    override fun onBindViewHolder(holder: RecyclerView.ViewHolder, position: Int) {
        val weatherItem = weatherList[position]

        if (weatherItem.isVisible) {
            holder.itemView.visibility = VISIBLE
            holder.itemView.layoutParams = RecyclerView.LayoutParams(
                ViewGroup.LayoutParams.MATCH_PARENT,
                ViewGroup.LayoutParams.WRAP_CONTENT
            )
        } else {
            holder.itemView.visibility = GONE
            holder.itemView.layoutParams = RecyclerView.LayoutParams(0, 0)
        }

        when (ViewType.fromOrdinal(holder.itemViewType)) {
            ViewType.YESTERDAY -> (holder as YesterdayViewHolder).bind(weatherItem)
            ViewType.TODAY -> (holder as TodayViewHolder).bind(weatherItem)
            ViewType.OTHER -> (holder as OtherViewHolder).bind(weatherItem)
        }
    }

    override fun getItemCount(): Int {
        return weatherList.size
    }

    fun toggleYesterdayWeather() {
        weatherList.forEach { weatherItem ->
            if (weatherItem.type == ViewType.YESTERDAY) {
                weatherItem.isVisible = !weatherItem.isVisible
                val index = weatherList.indexOf(weatherItem)
                notifyItemChanged(index)
            }
        }
    }

    fun updateWeatherData(newWeatherList: List<WeatherData>) {
        val updatedList = newWeatherList.mapIndexed { index, newItem ->
            val existingItem = weatherList.getOrNull(index)
            newItem.isVisible = existingItem?.isVisible ?: true
            newItem
        }
        weatherList.clear()
        weatherList.addAll(updatedList)
        notifyDataSetChanged()
    }

    fun addWeatherData(newWeatherData: WeatherData) {
        weatherList.add(newWeatherData)
        notifyItemInserted(weatherList.size - 1)
    }

    fun removeWeatherData(position: Int) {
        if (position >= 0 && position < weatherList.size) {
            weatherList.removeAt(position)
            notifyItemRemoved(position)
        }
    }
}
