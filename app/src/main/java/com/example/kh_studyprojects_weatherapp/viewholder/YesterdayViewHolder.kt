package com.example.kh_studyprojects_weatherapp.viewholder

import android.view.View
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.example.kh_studyprojects_weatherapp.R
import com.example.kh_studyprojects_weatherapp.data.WeatherData

class YesterdayViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {

    fun bind(data: WeatherData) {
        itemView.findViewById<TextView>(R.id.text_week).text = data.week
        itemView.findViewById<TextView>(R.id.text_min_temp).text = data.minTemp
        itemView.findViewById<TextView>(R.id.text_max_temp).text = data.maxTemp
    }
}
