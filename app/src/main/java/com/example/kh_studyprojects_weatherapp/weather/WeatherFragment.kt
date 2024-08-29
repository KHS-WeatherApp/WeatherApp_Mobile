package com.example.kh_studyprojects_weatherapp.weather

import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.LinearLayout
import android.widget.Switch
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

/*날씨 화면 최상단 네비게이션 연결된 메인 */
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

        view.findViewById<ConstraintLayout>(R.id.clNav01).setOnClickListener {
                Toast.makeText(context, "날씨 화면으로 이동합니다.", Toast.LENGTH_SHORT).show()
                it.findNavController().navigate(R.id.action_weatherFragment_self2)
        }

        view.findViewById<ConstraintLayout>(R.id.clNavOval).setOnClickListener {
                Toast.makeText(context, "설정 화면으로 이동합니다.", Toast.LENGTH_SHORT).show()
               it.findNavController().navigate(R.id.action_weatherFragment_to_settingFragment)
        }

        view.findViewById<ConstraintLayout>(R.id.clNav03).setOnClickListener {
                Toast.makeText(context, "미세먼지 화면으로 이동합니다.", Toast.LENGTH_SHORT).show()
                it.findNavController().navigate(R.id.action_weatherFragment_to_particulateMatterFragment)
        }

        //  동적 추가
        if (savedInstanceState == null) {
            // Hourly Forecast Fragment 추가
            val hourlyFragment = WeatherHourlyForecastFragment()
            childFragmentManager.beginTransaction()
                .replace(R.id.weather_hourly_forecast_fragment, hourlyFragment)
                .commit()

            // Daily Forecast Fragment 추가
            val dailyFragment = WeatherDailyForecastFragment()
            childFragmentManager.beginTransaction()
                .replace(R.id.weather_daily_forecast_fragment, dailyFragment)
                .commit()
        }


        return view

    }
}
