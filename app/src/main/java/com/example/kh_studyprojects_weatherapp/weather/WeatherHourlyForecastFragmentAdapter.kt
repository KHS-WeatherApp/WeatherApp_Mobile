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

class WeatherHourlyForecastFragmentAdapter(val context: Context,var isVertical: Boolean = false) :
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
        val inflater = LayoutInflater.from(context)
        val binding = if (isVertical) {
            WeatherHourlyForecastItemVerticalBinding.inflate(inflater, parent, false)
        } else {
            WeatherHourlyForecastItemHorizontalBinding.inflate(inflater, parent, false)
        }
        return ViewHolder(binding)
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        Log.d("RecyclerView", "Binding view holder for position $position")
        holder.bindItems(items[position], position)
    }

    override fun getItemCount(): Int {
        Log.d("RecyclerView", "Item count: ${items.size}")
        return items.size
    }


//    fun setItem(items:ArrayList<WeatherHourlyForecastFragmentDto>){
//        this.items = items
//    }


    class ViewHolder(
        private val binding: Any // 두 종류의 바인딩을 처리하기 위한 일반적인 타입
    ) : RecyclerView.ViewHolder((binding as? WeatherHourlyForecastItemHorizontalBinding)?.root ?: (binding as WeatherHourlyForecastItemVerticalBinding).root) {

        fun bindItems(item: WeatherHourlyForecastFragmentDto, pos: Int) {
            when (binding) {
                is WeatherHourlyForecastItemHorizontalBinding -> {
                    bindHorizontal(binding, item)
                }
                is WeatherHourlyForecastItemVerticalBinding -> {
                    bindVertical(binding, item)
                }
            }
        }

        private fun bindHorizontal(binding: WeatherHourlyForecastItemHorizontalBinding, item: WeatherHourlyForecastFragmentDto) {
            binding.apply {
                tvPmPa.text = item.tvPmPa
                tvHour.text = item.tvHour
                probability.text = item.probability
                precipitation.text = item.precipitation
                temperature.text = item.temperature
            }
        }

        private fun bindVertical(binding: WeatherHourlyForecastItemVerticalBinding, item: WeatherHourlyForecastFragmentDto) {
            binding.apply {
                tvPmPa.text = item.tvPmPa               // AM,PM
                tvHour.text = item.tvHour               // 시간
                probability.text = item.probability     // 강수확률
                precipitation.text = item.precipitation // 강수량
                temperature.text = item.temperature     // 온도
            }
        }
    }
}