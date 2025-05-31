package com.example.kh_studyprojects_weatherapp.presentation.weather.hourly

import android.content.Context
import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.constraintlayout.widget.ConstraintLayout
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.example.kh_studyprojects_weatherapp.R
import com.example.kh_studyprojects_weatherapp.databinding.WeatherHourlyForecastItemHorizontalBinding
import com.example.kh_studyprojects_weatherapp.databinding.WeatherHourlyForecastItemVerticalBinding
import com.example.kh_studyprojects_weatherapp.data.model.weather.WeatherHourlyForecastDto
import java.util.*

// RecyclerView 어댑터 클래스
class WeatherHourlyForecastAdapter(
    private val context: Context,
    var isVertical: Boolean = false
) : ListAdapter<WeatherHourlyForecastDto, RecyclerView.ViewHolder>(WeatherHourlyForecastDiffCallback()) {

    // 최저 온도를 저장하는 프로퍼티
    private var minTemperature: Double = 0.0
    // 현재 시간을 저장하는 프로퍼티
    private var currentHour: Int = 0

    // 뷰 타입 상수
    companion object {
        const val VIEW_TYPE_HORIZONTAL = 0
        const val VIEW_TYPE_VERTICAL = 1
    }

    // ViewHolder 재사용을 위한 설정
    init {
        setHasStableIds(true)
        // 현재 시간 설정
        currentHour = Calendar.getInstance().get(Calendar.HOUR_OF_DAY)
    }

    // submitList를 오버라이드하여 최저 온도 업데이트
    override fun submitList(list: List<WeatherHourlyForecastDto>?) {
        super.submitList(list)
        minTemperature = list?.minOfOrNull { 
            it.temperature?.toDoubleOrNull() ?: 0.0 
        } ?: 0.0
    }

    // 각 아이템의 고유 ID 반환
    override fun getItemId(position: Int): Long = position.toLong()

    // 아이템의 뷰 타입을 반환
    override fun getItemViewType(position: Int): Int =
        if (isVertical) VIEW_TYPE_VERTICAL else VIEW_TYPE_HORIZONTAL

    // 시간에 따른 AM/PM 텍스트를 반환하는 메서드
    private fun getAmPmText(hour: String?): String {
        // 시간 문자열에서 숫자만 추출
        val hourInt = hour?.replace("시", "")?.toIntOrNull() ?: return ""
        return when (hourInt) {
            0 -> if (currentHour == 0) "오전" else "내일"
            6 -> "오전"
            12, 18 -> "오후"
            else -> ""
        }
    }

    // 24시간 형식을 12시간 형식으로 변환하는 메서드
    private fun convertTo12HourFormat(hour: String?): String {
        val hourInt = hour?.replace("시", "")?.toIntOrNull() ?: return ""
        return when (hourInt) {
            0 -> "12시"
            in 1..12 -> "${hourInt}시"
            else -> "${hourInt - 12}시"
        }
    }

    // 날씨 코드에 따른 아이콘 리소스를 반환하는 메서드
    private fun getWeatherIcon(weatherCode: Int): Int = when (weatherCode) {
        0 -> R.drawable.weather_icon_sun
        1, 2, 3 -> R.drawable.weather_icon_partly_cloudy
        45, 48 -> R.drawable.weather_icon_fog
        51, 53, 55 -> R.drawable.weather_icon_drizzle
        56, 57 -> R.drawable.weather_icon_freezing_drizzle
        61, 63, 65 -> R.drawable.weather_icon_shower
        66, 67 -> R.drawable.weather_icon_shower
        71, 73, 75 -> R.drawable.weather_icon_snow
        77 -> R.drawable.weather_icon_snow
        80, 81, 82 -> R.drawable.weather_icon_thunder
        85, 86 -> R.drawable.weather_icon_thunder
        95 -> R.drawable.weather_icon_thunder
        96, 99 -> R.drawable.weather_icon_thunder
        else -> R.drawable.weather_icon_unknown
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): RecyclerView.ViewHolder {
        return when (viewType) {
            VIEW_TYPE_VERTICAL -> {
                // 세로 모드 레이아웃 바인딩 생성 및 ViewHolder 반환
                val binding = WeatherHourlyForecastItemVerticalBinding.inflate(
                    LayoutInflater.from(parent.context), parent, false
                )
                VerticalViewHolder(binding, context, this)
            }
            else -> {
                // 가로 모드 레이아웃 바인딩 생성 및 ViewHolder 반환
                val binding = WeatherHourlyForecastItemHorizontalBinding.inflate(
                    LayoutInflater.from(parent.context), parent, false
                )
                HorizontalViewHolder(binding, context, this)
            }
        }
    }

    // onBindViewHolder() 메서드에서 ViewHolder에 데이터를 바인딩
    override fun onBindViewHolder(holder: RecyclerView.ViewHolder, position: Int) {
        // 현재 포지션의 아이템을 가져옴
        when (holder) {
            is VerticalViewHolder -> holder.bindItems(getItem(position))    // 세로 모드 ViewHolder에 데이터 바인딩
            is HorizontalViewHolder -> holder.bindItems(getItem(position))  // 가로 모드 ViewHolder에 데이터 바인딩
        }
    }

    private class WeatherHourlyForecastDiffCallback : DiffUtil.ItemCallback<WeatherHourlyForecastDto>() {
        override fun areItemsTheSame(oldItem: WeatherHourlyForecastDto, newItem: WeatherHourlyForecastDto): Boolean {
            return oldItem.tvHour == newItem.tvHour
        }

        override fun areContentsTheSame(oldItem: WeatherHourlyForecastDto, newItem: WeatherHourlyForecastDto): Boolean {
            return oldItem == newItem
        }
    }

    // ViewHolder 클래스들에서 layoutParams 캐싱
    class HorizontalViewHolder(
        private val binding: WeatherHourlyForecastItemHorizontalBinding,
        private val context: Context,
        private val adapter: WeatherHourlyForecastAdapter
    ) : RecyclerView.ViewHolder(binding.root) {

        // 레이아웃 파라미터 캐싱
        private val temperatureLayoutParams = binding.temperature.layoutParams as ConstraintLayout.LayoutParams
        private val resources = context.resources

        fun bindItems(item: WeatherHourlyForecastDto) {
            binding.apply {
                tvAmPm.text = adapter.getAmPmText(item.tvHour)  // AM/PM 텍스트 설정
                tvHour.text = adapter.convertTo12HourFormat(item.tvHour)  // 12시간 형식으로 변환
                probability.text = item.probability         // 강수 확률 텍스트 설정
                precipitation.text = item.precipitation     // 강수량 텍스트 설정
                temperature.text = "${item.temperature}°"   // 온도 텍스트 설정

                // 날씨 코드에 따른 이미지 설정
                imgWeather.setImageResource(adapter.getWeatherIcon(item.weatherCode))

                // 온도에 따른 마진 설정
                val temperatureDouble = item.temperature?.toDoubleOrNull() ?: 0.0
                temperatureLayoutParams.topMargin = getMarginForTemperature(temperatureDouble)
                temperature.layoutParams = temperatureLayoutParams

                // 온도 배경 설정
                temperature.setBackgroundResource(getBackgroundForTemperature(temperatureDouble))
            }
        }

        // 온도에 따른 마진을 설정하는 메서드
        private fun getMarginForTemperature(temp: Double): Int {
            // 어댑터에서 저장된 최저 온도 사용
            val minTemp = adapter.minTemperature
            // 온도 차이 계산
            val tempDiff = temp - minTemp
            // 기본 마진 (최저 온도일 때의 마진)
            val baseMargin = resources.getDimensionPixelSize(R.dimen.dp_0)
            // 온도 차이에 따른 추가 마진 계산 (1도당 5dp)
            val additionalMargin = (tempDiff * resources.getDimensionPixelSize(R.dimen.dp_5)).toInt()
            return baseMargin + additionalMargin
        }

        // 온도에 따른 배경 리소스를 설정하는 메서드
        private fun getBackgroundForTemperature(temp: Double): Int = when {
            temp >= 30.0 -> R.drawable.sh_hourly_round_temperature_30
            temp >= 25.0 -> R.drawable.sh_hourly_round_temperature_20
            temp >= 20.0 -> R.drawable.sh_hourly_round_temperature_20
            temp >= 15.0 -> R.drawable.sh_hourly_round_temperature_15
            temp >= 10.0 -> R.drawable.sh_hourly_round_temperature_10
            else -> R.drawable.sh_hourly_round_temperature_10
        }
    }

    // 세로 모드 ViewHolder 클래스
    class VerticalViewHolder(
        private val binding: WeatherHourlyForecastItemVerticalBinding,
        private val context: Context,
        private val adapter: WeatherHourlyForecastAdapter
    ) : RecyclerView.ViewHolder(binding.root) {

        // 레이아웃 파라미터 캐싱
        private val resources = context.resources

        fun bindItems(item: WeatherHourlyForecastDto) {
            binding.apply {
                tvAmPm.text = adapter.getAmPmText(item.tvHour)  // AM/PM 텍스트 설정
                tvHour.text = adapter.convertTo12HourFormat(item.tvHour)  // 12시간 형식으로 변환
                probability.text = item.probability         // 강수 확률 텍스트 설정
                precipitation.text = item.precipitation     // 강수량 텍스트 설정
                temperature.text = "${item.temperature}°"   // 온도 텍스트 설정

                // 날씨 코드에 따른 이미지 설정
                imgWeather.setImageResource(adapter.getWeatherIcon(item.weatherCode))

                // item.temperature를 Double로 변환하여 마진 설정
                val temperatureDouble = item.temperature?.toDoubleOrNull() ?: 0.0
                val layoutParams = vi01.layoutParams

                // 온도 넓이 속성 - 최저 온도 기준으로 상대적 넓이 계산
                val minTemp = adapter.minTemperature
                val tempDiff = temperatureDouble - minTemp
                val baseWidth = resources.getDimensionPixelSize(R.dimen.dp_30)  // 기본 넓이
                val additionalWidth = (tempDiff * resources.getDimensionPixelSize(R.dimen.dp_5)).toInt()  // 온도당 5dp 추가
                layoutParams.width = baseWidth + additionalWidth
                vi01.layoutParams = layoutParams

                // 온도 배경 설정
                temperature.setBackgroundResource(
                    when {
                        temperatureDouble >= 30.0 -> R.drawable.sh_hourly_round_temperature_30
                        temperatureDouble >= 25.0 -> R.drawable.sh_hourly_round_temperature_20
                        temperatureDouble >= 20.0 -> R.drawable.sh_hourly_round_temperature_20
                        temperatureDouble >= 15.0 -> R.drawable.sh_hourly_round_temperature_15
                        temperatureDouble >= 10.0 -> R.drawable.sh_hourly_round_temperature_10
                        else -> R.drawable.sh_hourly_round_temperature_10
                    }
                )
            }
        }
    }
}
