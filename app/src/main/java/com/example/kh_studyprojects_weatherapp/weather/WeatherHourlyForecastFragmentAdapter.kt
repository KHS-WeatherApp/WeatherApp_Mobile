package com.example.kh_studyprojects_weatherapp.weather

import android.content.Context
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import android.widget.Toast
import androidx.recyclerview.widget.RecyclerView
import com.example.kh_studyprojects_weatherapp.databinding.WeatherHourlyForecastItemHorizontalBinding
import com.example.kh_studyprojects_weatherapp.databinding.WeatherHourlyForecastItemVerticalBinding

class WeatherHourlyForecastFragmentAdapter(val context: Context) :
    RecyclerView.Adapter<WeatherHourlyForecastFragmentAdapter.ViewHolder>(){

//    private var items: ArrayList<WeatherHourlyForecastFragmentDto>? = null

    // 아이템 리스트 초기화
    var items: MutableList<WeatherHourlyForecastFragmentDto> = mutableListOf(
        WeatherHourlyForecastFragmentDto("AM", "1시", "75%", "1.1mm", "75"),
        WeatherHourlyForecastFragmentDto("AM", "2시", "75%", "1.1mm", "75"),
        WeatherHourlyForecastFragmentDto("AM", "3시", "75%", "1.1mm", "75"),
        WeatherHourlyForecastFragmentDto("AM", "4시", "75%", "1.1mm", "75"),
        WeatherHourlyForecastFragmentDto("AM", "5시", "75%", "1.1mm", "75"),
        WeatherHourlyForecastFragmentDto("AM", "6시", "75%", "1.1mm", "75"),
        WeatherHourlyForecastFragmentDto("AM", "7시", "75%", "1.1mm", "75")
    )
    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        Log.d("RecyclerView", "Creating view holder")
        val binding = WeatherHourlyForecastItemVerticalBinding.inflate(LayoutInflater.from(parent.context), parent, false)
        return ViewHolder(binding)
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        Log.d("RecyclerView", "Binding view holder for position $position")
        items?.let {
            val item = it[position]
            holder.apply {
                bindItems(item, position)
                itemView.tag = item
            }
        }
    }

    override fun getItemCount(): Int {
        Log.d("RecyclerView", "Item count: ${items.size}")
        items?.let {
            return it.size
        }
        return 0
    }


//    fun setItem(items:ArrayList<WeatherHourlyForecastFragmentDto>){
//        this.items = items
//    }


    class ViewHolder(
        view:WeatherHourlyForecastItemVerticalBinding,
    ) : RecyclerView.ViewHolder(view.root) {
        var tvPmPa : TextView = view.tvPmPa
        var tvHour : TextView = view.tvHour
        var probability : TextView = view.probability
        var precipitation : TextView = view.precipitation
        var temperature  : TextView = view.temperature

        var parent:View = view.root

        fun bindItems(item: WeatherHourlyForecastFragmentDto, pos: Int) {
            tvPmPa.text = item.tvPmPa
            tvHour.text = item.tvHour
            probability.text = item.probability
            precipitation.text = item.precipitation
            temperature.text = item.temperature
        }
    }
}