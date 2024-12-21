package com.example.kh_studyprojects_weatherapp.weather

import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.fragment.app.viewModels
import com.example.kh_studyprojects_weatherapp.R
import com.example.kh_studyprojects_weatherapp.weather.viewmodel.WeatherState
import com.example.kh_studyprojects_weatherapp.weather.viewmodel.WeatherViewModel
/*수정해야할 사항
*
* WeatherViewModel 에서 로그 찍었는데 제대로 나옴 그런데 화면에 안나옴.....
* */







/*날씨 화면 현재 날씨 및 온도, 추천 옷*/
class WeatherCurrentForecast : Fragment() {
    private val viewModel: WeatherViewModel by viewModels()

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        viewModel.weatherState.observe(viewLifecycleOwner) { state ->
            when(state) {
                is WeatherState.Success -> {
                    // 현재 기온
                    view.findViewById<TextView>(R.id.currentTemperature).text =
                        "${state.data.currentTemperature}°"

                    // 최저 기온
                    view.findViewById<TextView>(R.id.tv02).text =
                        "${state.data.minTemperature}°"

                    // 최고 기온
                    view.findViewById<TextView>(R.id.tv04).text =
                        "${state.data.maxTemperature}°"

                    // 체감 온도
                    view.findViewById<TextView>(R.id.tv08).text =
                        "${state.data.apparentTemperature}°"
                }
                is WeatherState.Error -> {
                    // 에러 처리
                }
                WeatherState.Loading -> {
                    // 로딩 처리
                }
            }
        }

        // 데이터 요청
        viewModel.fetchWeatherData()
    }
}