package com.example.kh_studyprojects_weatherapp.presentation.weather.daily

import android.graphics.Color
import android.graphics.drawable.GradientDrawable
import android.view.Gravity
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.FrameLayout
import android.widget.ImageView
import android.widget.LinearLayout
import android.widget.TextView
import android.widget.Switch
import androidx.recyclerview.widget.RecyclerView
import com.example.kh_studyprojects_weatherapp.R
import com.example.kh_studyprojects_weatherapp.databinding.ItemWeatherDailyTodayBinding
import com.example.kh_studyprojects_weatherapp.databinding.ItemWeatherDailyYesterdayBinding
import com.example.kh_studyprojects_weatherapp.databinding.ItemWeatherDailyOtherBinding
import com.example.kh_studyprojects_weatherapp.databinding.ItemWeatherDailyTohourlyForecastBinding
import com.example.kh_studyprojects_weatherapp.databinding.ItemWeatherDailyTohourlyForecastTimeBinding
import com.example.kh_studyprojects_weatherapp.domain.model.weather.WeatherDailyDto
import com.example.kh_studyprojects_weatherapp.domain.model.weather.WeatherHourlyForecastDto
import java.util.*
import android.util.Log

sealed class WeatherDailyViewHolder(
    private val binding: androidx.viewbinding.ViewBinding
) : RecyclerView.ViewHolder(binding.root) {

    abstract fun bind(item: WeatherDailyDto)

    class Today(
        private val binding: ItemWeatherDailyTodayBinding
    ) : WeatherDailyViewHolder(binding) {
        private var isExpanded = false
        private var isHourlyMode = true // true: 1시간 단위, false: 2시간 단위
        
        override fun bind(item: WeatherDailyDto) {
            binding.apply {
                textWeek.text = item.week
                if (item.weatherCode !in intArrayOf(0, 1, 2, 3, 45, 48, 51, 53, 55)) {
                    textPrecipitation.text = item.precipitation
                    textHumidity.text = item.humidity
                } else {
                    textPrecipitation.text = null
                    textHumidity.text = null
                }

                textMinTemp.text = item.minTemp
                textMaxTemp.text = item.maxTemp
                imageWeather.setImageResource(getWeatherIcon(item.weatherCode))

                Log.i("WeatherDailyViewHolder","오늘온도 : ${item.minTemp}  / ${item.maxTemp}")
                // 안전한 변환 (null 또는 숫자가 아닌 경우 0으로 처리)
                // 예: 전역 최저/최고온도 = -20 ~ 40 => fullRange = 60
                // (1) 온도 파싱
                val minTemp = item.minTemp.replace("°", "").toDoubleOrNull()
                val maxTemp = item.maxTemp.replace("°", "").toDoubleOrNull()

                if (minTemp != null && maxTemp != null) {
                    // (2) 전역 온도 범위 (예: -20 ~ 40 => fullRange = 60)
                    val minTempRange = item.globalMinTemp
                    val maxTempRange = item.globalMaxTemp
                    val fullRange = (maxTempRange - minTempRange).toFloat()

                    // (3) 안전 범위 보정
                    val clampedMin = minTemp.coerceIn(minTempRange.toDouble(), maxTempRange.toDouble())
                    val clampedMax = maxTemp.coerceIn(minTempRange.toDouble(), maxTempRange.toDouble())

                    // (4) 오프셋 계산 (예: min=4 => offset=24, max=23 => offset=43)
                    val dayMinOffset = (clampedMin - minTempRange).toFloat()
                    val dayMaxOffset = (clampedMax - minTempRange).toFloat()

                    // (5) 0~1 비율
                    val startRatio = dayMinOffset / fullRange  // (예: 24/60)
                    val endRatio = dayMaxOffset / fullRange    // (예: 43/60)
                    val widthRatio = (endRatio - startRatio).coerceAtLeast(0f)

                    // (6) 실제 뷰 폭 설정 (onBind 시점엔 width=0일 수 있으므로 post 사용)
                    flTemperatureContainer.post {
                        val containerWidth = flTemperatureContainer.width
                        if (containerWidth > 0) {
                            // 시작 위치, 너비를 픽셀로 변환
                            val barStartPx = containerWidth * startRatio
                            val barWidthPx = containerWidth * widthRatio

                            val lp = viewTemperatureBar.layoutParams as FrameLayout.LayoutParams
                            lp.width = barWidthPx.toInt().coerceAtLeast(0)
                            lp.height = FrameLayout.LayoutParams.MATCH_PARENT
                            lp.marginStart = barStartPx.toInt().coerceAtLeast(0)
                            viewTemperatureBar.layoutParams = lp

                            // 배경(그라데이션 pill) 설정
                            viewTemperatureBar.setBackgroundResource(R.drawable.sh_day_progressbar_01)
                        }
                    }

                    // 평균 온도 계산 및 옷 아이콘 설정
                    val avgTemp = (minTemp + maxTemp) / 2.0
                    imageClothing.setImageResource(getClothingIcon(avgTemp))
                } else {
                    // 온도 변환 실패 시 처리
                    Log.e("WeatherDailyViewHolder", "온도 변환 실패: min=${item.minTemp}, max=${item.maxTemp}")

                    // 막대를 숨기거나 width=0으로 설정
                    flTemperatureContainer.post {
                        val lp = viewTemperatureBar.layoutParams as FrameLayout.LayoutParams
                        lp.width = 0
                        viewTemperatureBar.layoutParams = lp
                    }
                }

                // 시간별 날씨 보기 버튼 처리
                dayExpandMore24Btn.setOnClickListener {
                    isExpanded = !isExpanded
                    toggleExpandedView(item.hourlyForecast)
                    // 애니메이션 효과
                    it.animate()
                        .rotation(if(isExpanded) 180f else 0f)
                        .setDuration(300)
                        .start()
                }

                Log.i("WeatherDailyViewHolder", "hourlyForecast size: ${item.hourlyForecast?.size ?: 0}")
            }
        }

        private fun toggleExpandedView(hourlyData: List<WeatherHourlyForecastDto>) {
            // 확장 영역 가져오기 - 바인딩 사용
            binding.hourlyExpandedContainer.visibility = if (isExpanded) View.VISIBLE else View.GONE
            
            if (isExpanded) {
                // 확장 영역의 토글 스위치 설정
                // 중첩된 바인딩 생성
                val hourlyForecastBinding = ItemWeatherDailyTohourlyForecastBinding.bind(binding.hourlyForecast.root)
                
                hourlyForecastBinding.hourlyToggle.isChecked = !isHourlyMode
                hourlyForecastBinding.hourlyToggle.setOnCheckedChangeListener { _, isChecked ->
                    isHourlyMode = !isChecked
                    updateHourlyData(hourlyForecastBinding, hourlyData)
                }
                
                // 시간별 데이터 업데이트
                updateHourlyData(hourlyForecastBinding, hourlyData)
            }
        }

        private fun updateHourlyData(binding: ItemWeatherDailyTohourlyForecastBinding, data: List<WeatherHourlyForecastDto>) {
            val container = binding.hourlyItemsContainer
            container.removeAllViews()

            val currentHour = Calendar.getInstance().get(Calendar.HOUR_OF_DAY)
            val interval = if (isHourlyMode) 1 else 2
            val filtered = data.filter {
                val hour = it.tvHour?.toIntOrNull() ?: return@filter false
                hour in (currentHour + 1)..23 && ((hour - (currentHour + 1)) % interval == 0)
            }

            for (hourData in filtered) {
                val view = LayoutInflater.from(container.context).inflate(R.layout.item_weather_daily_tohourly_forecast_time, container, false)
                val hourBinding = ItemWeatherDailyTohourlyForecastTimeBinding.bind(view)

                hourBinding.hourlyTime.text = "${hourData.tvAmPm} ${hourData.tvHour}시"
                hourBinding.hourlyTemp.text = hourData.temperature
                hourBinding.hourlyWeatherIcon.setImageResource(getWeatherIcon(hourData.weatherCode))

                val prob = hourData.probability?.replace("%", "")?.toIntOrNull() ?: 0
                if (prob >= 20) {
                    hourBinding.hourlyPrecipitation.text = hourData.probability
                    hourBinding.hourlyPrecipitation.visibility = View.VISIBLE
                } else {
                    hourBinding.hourlyPrecipitation.visibility = View.GONE
                }
                container.addView(view)
            }
        }
    }

    class Other(
        private val binding: ItemWeatherDailyOtherBinding
    ) : WeatherDailyViewHolder(binding) {
        private var isExpanded = false
        private var isHourlyMode = true // true: 1시간 단위, false: 2시간 단위

        override fun bind(item: WeatherDailyDto) {
            binding.apply {
                textWeek.text = item.week
                textDate.text = item.date
                if (item.weatherCode !in intArrayOf(0, 1, 2, 3, 45, 48, 51, 53, 55)) {
                    textPrecipitation.text = item.precipitation
                    textHumidity.text = item.humidity
                }else{
                    textPrecipitation.text = null
                    textHumidity.text = null
                }

                textMinTemp.text = item.minTemp
                textMaxTemp.text = item.maxTemp
                imageWeather.setImageResource(getWeatherIcon(item.weatherCode))

                // (1) 온도 파싱
                val minTemp = item.minTemp.replace("°", "").toDoubleOrNull()
                val maxTemp = item.maxTemp.replace("°", "").toDoubleOrNull()

                if (minTemp != null && maxTemp != null) {
                    // (2) 전역 온도 범위 (예: -20 ~ 40 => fullRange = 60)
                    val minTempRange = item.globalMinTemp
                    val maxTempRange = item.globalMaxTemp
                    val fullRange = (maxTempRange - minTempRange).toFloat()

                    // (3) 안전 범위 보정
                    val clampedMin = minTemp.coerceIn(minTempRange.toDouble(), maxTempRange.toDouble())
                    val clampedMax = maxTemp.coerceIn(minTempRange.toDouble(), maxTempRange.toDouble())

                    // (4) 오프셋 계산 (예: min=4 => offset=24, max=23 => offset=43)
                    val dayMinOffset = (clampedMin - minTempRange).toFloat()
                    val dayMaxOffset = (clampedMax - minTempRange).toFloat()

                    // (5) 0~1 비율
                    val startRatio = dayMinOffset / fullRange  // (예: 24/60)
                    val endRatio = dayMaxOffset / fullRange    // (예: 43/60)
                    val widthRatio = (endRatio - startRatio).coerceAtLeast(0f)

                    // (6) 실제 뷰 폭 설정 (onBind 시점엔 width=0일 수 있으므로 post 사용)
                    flTemperatureContainer.post {
                        val containerWidth = flTemperatureContainer.width
                        if (containerWidth > 0) {
                            // 시작 위치, 너비를 픽셀로 변환
                            val barStartPx = containerWidth * startRatio
                            val barWidthPx = containerWidth * widthRatio

                            val lp = viewTemperatureBar.layoutParams as FrameLayout.LayoutParams
                            lp.width = barWidthPx.toInt().coerceAtLeast(0)
                            lp.height = FrameLayout.LayoutParams.MATCH_PARENT
                            lp.marginStart = barStartPx.toInt().coerceAtLeast(0)
                            viewTemperatureBar.layoutParams = lp

                            // 배경(그라데이션 pill) 설정
                            viewTemperatureBar.setBackgroundResource(R.drawable.sh_day_progressbar_01)
                        }
                    }

                    // (7) 평균 온도로 옷 아이콘 결정 (기존 로직 유지)
                    val avgTemp = (minTemp + maxTemp) / 2.0
                    imageClothing.setImageResource(getClothingIcon(avgTemp))

                } else {
                    // 온도 변환 실패 시 처리
                    Log.e("WeatherDailyViewHolder", "온도 변환 실패: min=${item.minTemp}, max=${item.maxTemp}")

                    // 막대를 숨기거나 width=0으로 설정
                    flTemperatureContainer.post {
                        val lp = viewTemperatureBar.layoutParams as FrameLayout.LayoutParams
                        lp.width = 0
                        viewTemperatureBar.layoutParams = lp
                    }
                }
                // 시간별 날씨 보기 버튼 처리
                dayExpandMore24Btn.setOnClickListener {
                    isExpanded = !isExpanded
                    toggleExpandedView(item.hourlyForecast)
                    // 애니메이션 효과
                    it.animate()
                        .rotation(if(isExpanded) 180f else 0f)
                        .setDuration(300)
                        .start()
                }

                Log.i("WeatherDailyViewHolder", "hourlyForecast size: ${item.hourlyForecast?.size ?: 0}")
            }
        }

        private fun toggleExpandedView(hourlyData: List<WeatherHourlyForecastDto>) {
            // 확장 영역 가져오기 - 바인딩 사용
            binding.hourlyExpandedContainer.visibility = if (isExpanded) View.VISIBLE else View.GONE
            
            if (isExpanded) {
                // 확장 영역의 토글 스위치 설정
                // 중첩된 바인딩 생성
                val hourlyForecastBinding = ItemWeatherDailyTohourlyForecastBinding.bind(binding.hourlyForecast.root)
                
                hourlyForecastBinding.hourlyToggle.isChecked = !isHourlyMode
                hourlyForecastBinding.hourlyToggle.setOnCheckedChangeListener { _, isChecked ->
                    isHourlyMode = !isChecked
                    updateHourlyData(hourlyForecastBinding, hourlyData)
                }
                
                // 시간별 데이터 업데이트
                updateHourlyData(hourlyForecastBinding, hourlyData)
            }
        }

        private fun updateHourlyData(binding: ItemWeatherDailyTohourlyForecastBinding, data: List<WeatherHourlyForecastDto>) {
            val container = binding.hourlyItemsContainer
            container.removeAllViews()

            val currentHour = Calendar.getInstance().get(Calendar.HOUR_OF_DAY)
            val interval = if (isHourlyMode) 1 else 2

            val filtered = data.filter {
                val hour = it.tvHour?.toIntOrNull() ?: return@filter false
                hour in 0..23 && hour % interval == 0
            }

            for (hourData in filtered) {
                val view = LayoutInflater.from(container.context).inflate(R.layout.item_weather_daily_tohourly_forecast_time, container, false)
                val hourBinding = ItemWeatherDailyTohourlyForecastTimeBinding.bind(view)

                hourBinding.hourlyTime.text = "${hourData.tvAmPm} ${hourData.tvHour}시"
                hourBinding.hourlyTemp.text = hourData.temperature
                hourBinding.hourlyWeatherIcon.setImageResource(getWeatherIcon(hourData.weatherCode))

                val prob = hourData.probability?.replace("%", "")?.toIntOrNull() ?: 0
                if (prob >= 20) {
                    hourBinding.hourlyPrecipitation.text = hourData.probability
                    hourBinding.hourlyPrecipitation.visibility = View.VISIBLE
                } else {
                    hourBinding.hourlyPrecipitation.visibility = View.GONE
                }
                container.addView(view)
            }
        }
    }


    class Yesterday(
        private val binding: ItemWeatherDailyYesterdayBinding
    ) : WeatherDailyViewHolder(binding) {
        override fun bind(item: WeatherDailyDto) {
            binding.apply {
                textWeek.text = item.week
                textMinTemp.text = item.minTemp
                textMaxTemp.text = item.maxTemp
                // (1) 온도 파싱
                val minTemp = item.minTemp.replace("°", "").toDoubleOrNull()
                val maxTemp = item.maxTemp.replace("°", "").toDoubleOrNull()

                if (minTemp != null && maxTemp != null) {
                    // (2) 전역 온도 범위 (예: -20 ~ 40 => fullRange = 60)
                    val minTempRange = item.globalMinTemp
                    val maxTempRange = item.globalMaxTemp
                    val fullRange = (maxTempRange - minTempRange).toFloat()

                    // (3) 안전 범위 보정
                    val clampedMin = minTemp.coerceIn(minTempRange.toDouble(), maxTempRange.toDouble())
                    val clampedMax = maxTemp.coerceIn(minTempRange.toDouble(), maxTempRange.toDouble())

                    // (4) 오프셋 계산 (예: min=4 => offset=24, max=23 => offset=43)
                    val dayMinOffset = (clampedMin - minTempRange).toFloat()
                    val dayMaxOffset = (clampedMax - minTempRange).toFloat()

                    // (5) 0~1 비율
                    val startRatio = dayMinOffset / fullRange  // (예: 24/60)
                    val endRatio = dayMaxOffset / fullRange    // (예: 43/60)
                    val widthRatio = (endRatio - startRatio).coerceAtLeast(0f)

                    // (6) 실제 뷰 폭 설정 (onBind 시점엔 width=0일 수 있으므로 post 사용)
                    flTemperatureContainer.post {
                        val containerWidth = flTemperatureContainer.width
                        if (containerWidth > 0) {
                            // 시작 위치, 너비를 픽셀로 변환
                            val barStartPx = containerWidth * startRatio
                            val barWidthPx = containerWidth * widthRatio

                            val lp = viewTemperatureBar.layoutParams as FrameLayout.LayoutParams
                            lp.width = barWidthPx.toInt().coerceAtLeast(0)
                            lp.height = FrameLayout.LayoutParams.MATCH_PARENT
                            lp.marginStart = barStartPx.toInt().coerceAtLeast(0)
                            viewTemperatureBar.layoutParams = lp

                            // 배경(그라데이션 pill) 설정
                            viewTemperatureBar.setBackgroundResource(R.drawable.sh_day_progressbar_01)
                        }
                    }
                } else {
                    // 온도 변환 실패 시 처리
                    Log.e("WeatherDailyViewHolder", "온도 변환 실패: min=${item.minTemp}, max=${item.maxTemp}")

                    // 막대를 숨기거나 width=0으로 설정
                    flTemperatureContainer.post {
                        val lp = viewTemperatureBar.layoutParams as FrameLayout.LayoutParams
                        lp.width = 0
                        viewTemperatureBar.layoutParams = lp
                    }
                }
            }
        }
    }

    protected fun getWeatherIcon(weatherCode: Int): Int {
        return when (weatherCode) {
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
    }

    protected fun getClothingIcon(temperature: Double): Int {
        return when {
            temperature >= 28 -> R.drawable.clothing_icon_hawaiianshirt
            temperature >= 23 -> R.drawable.ic_com_clothes_01
            temperature >= 20 -> R.drawable.clothing_icon_hawaiianshirt
            temperature >= 17 -> R.drawable.clothing_icon_hawaiianshirt
            temperature >= 12 -> R.drawable.clothing_icon_hawaiianshirt
            temperature >= 9 -> R.drawable.clothing_icon_hawaiianshirt
            else -> R.drawable.clothing_icon_hawaiianshirt
        }
    }
}

// 시간별 날씨 데이터 클래스
data class HourlyForecast(
    val hour: Int,
    val temp: String,
    val weatherCode: Int
)