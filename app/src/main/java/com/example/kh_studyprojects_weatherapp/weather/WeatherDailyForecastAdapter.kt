package com.example.kh_studyprojects_weatherapp.weather

import android.content.Context
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.example.kh_studyprojects_weatherapp.databinding.WeatherDailyForecastFragmentBinding
import com.example.kh_studyprojects_weatherapp.databinding.WeatherDailyForecastItemHorizontalBinding
import com.example.kh_studyprojects_weatherapp.databinding.WeatherHourlyForecastItemVerticalBinding

class WeatherDailyForecastAdapter(val context:Context) :
    RecyclerView.Adapter<WeatherDailyForecastAdapter.ViewHolder>() {

    var items: MutableList<WeatherDailyForecastDto> = mutableListOf(
        WeatherDailyForecastDto("어제","8.26","30%","0.2mm","10","20"),
        WeatherDailyForecastDto("오늘","8.26","30%","0.2mm","10","20"),
        WeatherDailyForecastDto("금","8.26","30%","0.2mm","10","20"),
        WeatherDailyForecastDto("토","8.26","30%","0.2mm","10","20"),
        WeatherDailyForecastDto("일","8.26","30%","0.2mm","10","20"),
        WeatherDailyForecastDto("월","8.26","30%","0.2mm","10","20"),
        WeatherDailyForecastDto("화","8.26","30%","0.2mm","10","20"),
        WeatherDailyForecastDto("수","8.26","30%","0.2mm","10","20"),
        WeatherDailyForecastDto("목","8.26","30%","0.2mm","10","20"),
        WeatherDailyForecastDto("금","8.26","30%","0.2mm","10","20"),
    )

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val binding = WeatherDailyForecastItemHorizontalBinding.inflate(LayoutInflater.from(parent.context),parent, false)
        return ViewHolder(binding)
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        items?.let {
            val item = it[position]
            holder.apply {
                bindItems(item, position)
                itemView.tag = item
            }
        }
    }

    override fun getItemCount(): Int {
        items?.let {
            return it.size
        }
        return 0
    }

    class ViewHolder(
        view: WeatherDailyForecastItemHorizontalBinding,
    ) : RecyclerView.ViewHolder(view.root) {
        var tvPmPa : TextView = view.tvPmPa
        var tvDate : TextView = view.tvDate
//        var tvHour : TextView = view.tvHour
        var probability : TextView = view.probability
        var precipitation : TextView = view.precipitation
//        var temperature  : TextView = view.temperature
        var minTemperature  : TextView = view.minTemperature
        var maxTemperature  : TextView = view.maxTemperature

        var parent: View = view.root

        fun bindItems(item: WeatherDailyForecastDto, pos: Int) {
            tvPmPa.text = item.tvPmPa
            tvDate.text = item.tvDate
//            tvHour.text = item.tvHour
            probability.text = item.probability
            precipitation.text = item.precipitation
//            temperature.text = item.temperature
            minTemperature.text=item.minTemperature
            maxTemperature.text=item.maxTemperature
        }
    }

}