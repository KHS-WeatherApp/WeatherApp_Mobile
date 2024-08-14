package com.example.kh_studyprojects_weatherapp.weather

import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.LinearLayout
import android.widget.TextView
import android.widget.Toast
import androidx.constraintlayout.widget.ConstraintLayout
import androidx.fragment.app.Fragment
import androidx.navigation.findNavController
import androidx.navigation.fragment.findNavController
import com.example.kh_studyprojects_weatherapp.R

/*
클래스명
weather_current forecast01
weather_hourly forecast02
weather_daily forecast03
weather_detailed 04
*/
class WeatherFragment : Fragment() {
    private lateinit var weatherInfoTextView: TextView

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {

        val view = inflater.inflate(R.layout.weather_fragment, container, false)

        view.findViewById<ConstraintLayout>(R.id.cl01).setOnClickListener {
                Toast.makeText(context, "날씨 화면으로 이동합니다.", Toast.LENGTH_SHORT).show()
                it.findNavController().navigate(R.id.action_weatherFragment_self2)
        }

        view.findViewById<ConstraintLayout>(R.id.cl02_01).setOnClickListener {
                Toast.makeText(context, "설정 화면으로 이동합니다.", Toast.LENGTH_SHORT).show()
               it.findNavController().navigate(R.id.action_weatherFragment_to_settingFragment)
        }

        view.findViewById<ConstraintLayout>(R.id.cl03).setOnClickListener {
                Toast.makeText(context, "미세먼지 화면으로 이동합니다.", Toast.LENGTH_SHORT).show()
                it.findNavController().navigate(R.id.action_weatherFragment_to_particulateMatterFragment)
        }
        return view

    }
}
