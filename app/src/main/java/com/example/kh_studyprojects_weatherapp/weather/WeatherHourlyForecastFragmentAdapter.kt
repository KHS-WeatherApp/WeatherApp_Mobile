package com.example.kh_studyprojects_weatherapp.weather

import android.content.Context
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.example.kh_studyprojects_weatherapp.databinding.WeatherHourlyForecastFragmentBinding
import com.example.kh_studyprojects_weatherapp.databinding.WeatherHourlyForecastItemBinding

class WeatherHourlyForecastFragmentAdapter(val context: Context) :
    RecyclerView.Adapter<WeatherHourlyForecastFragmentAdapter.ViewHolder>(){

//    private var items: ArrayList<WeatherHourlyForecastFragmentDto>? = null

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
        var binding = WeatherHourlyForecastItemBinding.inflate(LayoutInflater.from(parent.context), parent, false)
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
        view: WeatherHourlyForecastItemBinding,
    ) : RecyclerView.ViewHolder(view.root) {
        var tvPmPa : TextView = view.tvPmPa                 // AM,PM
        var tvHour : TextView = view.tvHour                 // 시간
//        var weatherIcon : TextView = view.tvHour                 // 날씨icon
        var probability : TextView = view.probability       // 강수확률
        var precipitation : TextView = view.precipitation   // 강수량
//        var clothesIcon : TextView = view.clothesIcon       // 옷icon
        var temperature : TextView = view.temperature       // 온도

        var parent: View = view.root
        fun bindItems(item: WeatherHourlyForecastFragmentDto, pos: Int) {
            tvPmPa.text = item.tvPmPa               // AM,PM
            tvHour.text = item.tvHour               // 시간
            probability.text = item.probability     // 강수확률
            precipitation.text = item.precipitation // 강수량
            temperature.text = item.temperature     // 온도
        }



    }
}