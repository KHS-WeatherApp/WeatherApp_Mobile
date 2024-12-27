package com.example.kh_studyprojects_weatherapp.viewholder

import android.view.View
import android.widget.ImageView
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.example.kh_studyprojects_weatherapp.R
import com.example.kh_studyprojects_weatherapp.data.WeatherData

// OtherViewHolder에서 bind 메서드 수정
class OtherViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
    fun bind(data: WeatherData) {
        itemView.findViewById<TextView>(R.id.text_week).text = data.week
        itemView.findViewById<TextView>(R.id.text_date).text = data.date
        itemView.findViewById<TextView>(R.id.text_precipitation).text = data.precipitation
        itemView.findViewById<TextView>(R.id.text_humidity).text = data.humidity
        itemView.findViewById<TextView>(R.id.text_min_temp).text = data.minTemp
        itemView.findViewById<TextView>(R.id.text_max_temp).text = data.maxTemp

        // 날씨 이모지 적용
        itemView.findViewById<ImageView>(R.id.image_weather).setImageResource(getDrawableForEmoji(data.weatherEmoji))
    }

    private fun getDrawableForEmoji(emoji: String): Int {
        // 이모지에 맞는 드로어블 리소스 반환 (리소스에 맞춰서 수정)
        return when (emoji) {
            "\uD83C\uDF1E" -> R.drawable.weather_icon_sun
            "\u26C5" -> R.drawable.weather_icon_cloudy
            "\uD83C\uDF2B" -> R.drawable.weather_icon_fog
            "\u2614" -> R.drawable.weather_icon_raining
            "\uD83C\uDF27" -> R.drawable.weather_icon_shower
            "\uD83C\uDF28" -> R.drawable.weather_icon_snow
            "\u26A1" -> R.drawable.weather_icon_thunder
            else -> R.drawable.weather_icon_unknown
        }
    }
}

