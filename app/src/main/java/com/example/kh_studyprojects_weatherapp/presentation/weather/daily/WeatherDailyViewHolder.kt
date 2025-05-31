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
import android.util.TypedValue

sealed class WeatherDailyViewHolder(
    private val binding: androidx.viewbinding.ViewBinding
) : RecyclerView.ViewHolder(binding.root) {

    abstract fun bind(item: WeatherDailyDto)

    class Today(
        private val binding: ItemWeatherDailyTodayBinding
    ) : WeatherDailyViewHolder(binding) {
        private var isExpanded = false
        private var isHourlyMode = true // true: 1시간 단위, false: 2시간 단위
        private var isClickable = true
        private var currentItem: WeatherDailyDto? = null
        
        override fun bind(item: WeatherDailyDto) {
            currentItem = item
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

                // 온도 파싱 및 온도 바 설정
                val minTemp = item.minTemp.replace("°", "").toDoubleOrNull()
                val maxTemp = item.maxTemp.replace("°", "").toDoubleOrNull()

                if (minTemp != null && maxTemp != null) {
                    val minTempRange = item.globalMinTemp
                    val maxTempRange = item.globalMaxTemp
                    val fullRange = (maxTempRange - minTempRange).toFloat()

                    val clampedMin = minTemp.coerceIn(minTempRange.toDouble(), maxTempRange.toDouble())
                    val clampedMax = maxTemp.coerceIn(minTempRange.toDouble(), maxTempRange.toDouble())

                    val dayMinOffset = (clampedMin - minTempRange).toFloat()
                    val dayMaxOffset = (clampedMax - minTempRange).toFloat()

                    val startRatio = dayMinOffset / fullRange
                    val endRatio = dayMaxOffset / fullRange
                    val widthRatio = (endRatio - startRatio).coerceAtLeast(0f)

                    flTemperatureContainer.post {
                        val containerWidth = flTemperatureContainer.width
                        if (containerWidth > 0) {
                            val barStartPx = containerWidth * startRatio
                            val barWidthPx = containerWidth * widthRatio

                            val lp = viewTemperatureBar.layoutParams as FrameLayout.LayoutParams
                            lp.width = barWidthPx.toInt().coerceAtLeast(0)
                            lp.height = FrameLayout.LayoutParams.MATCH_PARENT
                            lp.marginStart = barStartPx.toInt().coerceAtLeast(0)
                            viewTemperatureBar.layoutParams = lp

                            viewTemperatureBar.setBackgroundResource(R.drawable.sh_day_progressbar_01)
                        }
                    }

                    val avgTemp = (minTemp + maxTemp) / 2.0
                    imageClothing.setImageResource(getClothingIcon(avgTemp))
                } else {
                    Log.e("WeatherDailyViewHolder", "온도 변환 실패: min=${item.minTemp}, max=${item.maxTemp}")
                    flTemperatureContainer.post {
                        val lp = viewTemperatureBar.layoutParams as FrameLayout.LayoutParams
                        lp.width = 0
                        viewTemperatureBar.layoutParams = lp
                    }
                }

                // 오후 10시(22시) 이후라면 더보기 버튼 숨김
                val currentHour = Calendar.getInstance().get(Calendar.HOUR_OF_DAY)
                dayExpandMore24Btn.visibility = if (currentHour >= 22) View.GONE else View.VISIBLE

                // 클릭 리스너 설정
                val toggleListener = View.OnClickListener {
                    if (!isClickable) return@OnClickListener
                    isClickable = false
                    
                    isExpanded = !isExpanded
                    currentItem?.let { toggleExpandedView(it) }
                    
                    dayExpandMore24Btn.animate()
                        .rotation(if (isExpanded) 180f else 0f)
                        .setDuration(300)
                        .withEndAction { isClickable = true }
                        .start()
                }

                // 메인 레이아웃과 확장 영역에 클릭 리스너 설정
                rawLayout.setOnClickListener(toggleListener)
                hourlyExpandedContainer.setOnClickListener(toggleListener)
            }
        }

        private fun toggleExpandedView(item: WeatherDailyDto) {
            binding.hourlyExpandedContainer.visibility = if (isExpanded) View.VISIBLE else View.GONE
            
            if (isExpanded) {
                val hourlyForecastBinding = ItemWeatherDailyTohourlyForecastBinding.bind(binding.hourlyForecast.root)
                
                // 확장된 영역의 전체 레이아웃에 클릭 리스너 설정
                hourlyForecastBinding.hourlyForecastLayout.setOnClickListener {
                    if (!isClickable) return@setOnClickListener
                    isClickable = false
                    
                    isExpanded = false
                    binding.hourlyExpandedContainer.visibility = View.GONE
                    
                    binding.dayExpandMore24Btn.animate()
                        .rotation(0f)
                        .setDuration(300)
                        .withEndAction { isClickable = true }
                        .start()
                }

                hourlyForecastBinding.hourlyToggle.isChecked = !isHourlyMode
                hourlyForecastBinding.hourlyToggle.setOnCheckedChangeListener { _, isChecked ->
                    isHourlyMode = !isChecked
                    updateHourlyData(hourlyForecastBinding, item.hourlyForecast, item.globalMinTemp, item.globalMaxTemp)
                }
                
                updateHourlyData(hourlyForecastBinding, item.hourlyForecast, item.globalMinTemp, item.globalMaxTemp)
            }
        }

        private fun updateHourlyData(
            binding: ItemWeatherDailyTohourlyForecastBinding,
            data: List<WeatherHourlyForecastDto>,
            minTempRange: Double,
            maxTempRange: Double
        ) {
            val container = binding.hourlyItemsContainer
            container.removeAllViews()

            val currentHour = Calendar.getInstance().get(Calendar.HOUR_OF_DAY)
            val interval = if (isHourlyMode) 1 else 2

            val filtered = if (data.size == 24 && currentHour < 23) {
                data.subList(currentHour + 1, data.size)
                    .filterIndexed { index, _ -> index % interval == 0 }
            } else {
                data.filter {
                    val rawHour = it.tvHour?.toIntOrNull() ?: return@filter false
                    val hour = if (it.tvAmPm == "오전") {
                        if (rawHour == 12) 0 else rawHour
                    } else {
                        if (rawHour == 12) 12 else rawHour + 12
                    }
                    hour in (currentHour + 1)..23 && ((hour - (currentHour + 1)) % interval == 0)
                }
            }

            for (hourData in filtered) {
                val view = LayoutInflater.from(container.context)
                    .inflate(R.layout.item_weather_daily_tohourly_forecast_time, container, false)
                val hourBinding = ItemWeatherDailyTohourlyForecastTimeBinding.bind(view)

                // 각 아이템에 클릭 리스너 설정
                view.setOnClickListener {
                    if (!isClickable) return@setOnClickListener
                    isClickable = false
                    
                    isExpanded = false
                    (this@Today).binding.hourlyExpandedContainer.visibility = View.GONE
                    
                    (this@Today).binding.dayExpandMore24Btn.animate()
                        .rotation(0f)
                        .setDuration(300)
                        .withEndAction { isClickable = true }
                        .start()
                }

                hourBinding.hourlyTime.text = "${hourData.tvAmPm} ${hourData.tvHour}시"
                hourBinding.hourlyTemp.text = hourData.temperature
                val temperature = hourData.temperature?.replace("°", "")?.toDoubleOrNull() ?: 0.0
                hourBinding.hourlyTemp.setBackgroundResource(getBackgroundForTemperature(temperature))
                hourBinding.hourlyWeatherIcon.setImageResource(getWeatherIcon(hourData.weatherCode))
                hourBinding.hourlyClothingIcon.setImageResource(getClothingIcon(temperature))

                // 온도 선 설정
                val tempLine = hourBinding.hourlyTempLine
                tempLine.post {
                    // 최소폭 (예: 56dp)
                    val minWidthPx = TypedValue.applyDimension(
                        TypedValue.COMPLEX_UNIT_DIP, 56f, tempLine.context.resources.displayMetrics
                    ).toInt()

                    // 최대폭 (예: 120dp)
                    val maxWidthPx = TypedValue.applyDimension(
                        TypedValue.COMPLEX_UNIT_DIP, 120f, tempLine.context.resources.displayMetrics
                    ).toInt()

                    // 온도 → 비율 계산
                    val baseTemp = hourData.temperature?.replace("°", "")?.toDoubleOrNull() ?: 0.0
                    val adjustedTemp = baseTemp.coerceIn(minTempRange, maxTempRange)
                    val tempOffset = (adjustedTemp - minTempRange).toFloat()
                    val widthRatioLinear = tempOffset / (maxTempRange - minTempRange).toFloat()

                    // 곡선화 (원하면)
                    val widthRatio = (1 - Math.pow((1 - widthRatioLinear).toDouble(), 2.0)).toFloat()

                    // 🚀 최소~최대 폭 안에서 자연스럽게 비율로 계산
                    val finalWidth = (minWidthPx + (maxWidthPx - minWidthPx) * widthRatio).toInt()

                    //Log.d("온도 계산", "minWidthPx=$minWidthPx maxWidthPx=$maxWidthPx ,, minTempRange=$minTempRange, maxTempRange=$maxTempRange, temp=$adjustedTemp , width=$finalWidth")

                    val lp = tempLine.layoutParams
                    lp.width = finalWidth
                    tempLine.layoutParams = lp
                }

                val prob = hourData.probability?.replace("%", "")?.toIntOrNull() ?: 0
                if (prob >= 5) {
                    hourBinding.hourlyProbability.text = hourData.probability
                    hourBinding.hourlyPrecipitation.text = if (hourData.precipitation != "0.0mm") (" • "+hourData.precipitation) else ""
                    hourBinding.hourlyProbability.visibility = View.VISIBLE
                    hourBinding.hourlyPrecipitation.visibility = if(hourBinding.hourlyPrecipitation.text == "") View.GONE else View.VISIBLE
                } else {
                    hourBinding.hourlyPrecipitation.visibility = View.GONE
                    hourBinding.hourlyProbability.visibility = View.GONE
                }
                container.addView(view)
            }
        }
    }

    class Other(
        private val binding: ItemWeatherDailyOtherBinding
    ) : WeatherDailyViewHolder(binding) {
        private var isExpanded = false
        private var isHourlyMode = true
        private var isClickable = true
        private var currentItem: WeatherDailyDto? = null

        override fun bind(item: WeatherDailyDto) {
            currentItem = item
            binding.apply {
                textWeek.text = item.week
                textDate.text = item.date
                if (item.weatherCode !in intArrayOf(0, 1, 2)) {//, 3, 45, 48, 51, 53, 55
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
                val toggleListener = View.OnClickListener {
                    if (!isClickable) return@OnClickListener
                    isClickable = false
                    
                    isExpanded = !isExpanded
                    currentItem?.let { toggleExpandedView(it) }
                    
                    dayExpandMore24Btn.animate()
                        .rotation(if (isExpanded) 180f else 0f)
                        .setDuration(300)
                        .withEndAction { isClickable = true }
                        .start()
                }

                // rawLayout 클릭시
                rawLayout.setOnClickListener(toggleListener)

                // ✅ hourlyExpandedContainer 클릭시도 똑같이
                hourlyExpandedContainer.setOnClickListener(toggleListener)

                Log.i("WeatherDailyViewHolder", "hourlyForecast size: ${item.hourlyForecast?.size ?: 0}")
            }
        }

        private fun toggleExpandedView(item: WeatherDailyDto) {
            binding.hourlyExpandedContainer.visibility = if (isExpanded) View.VISIBLE else View.GONE
            
            if (isExpanded) {
                val hourlyForecastBinding = ItemWeatherDailyTohourlyForecastBinding.bind(binding.hourlyForecast.root)
                
                // 확장된 영역의 전체 레이아웃에 클릭 리스너 설정
                hourlyForecastBinding.hourlyForecastLayout.setOnClickListener {
                    if (!isClickable) return@setOnClickListener
                    isClickable = false
                    
                    isExpanded = false
                    binding.hourlyExpandedContainer.visibility = View.GONE
                    
                    binding.dayExpandMore24Btn.animate()
                        .rotation(0f)
                        .setDuration(300)
                        .withEndAction { isClickable = true }
                        .start()
                }

                hourlyForecastBinding.hourlyToggle.isChecked = !isHourlyMode
                hourlyForecastBinding.hourlyToggle.setOnCheckedChangeListener { _, isChecked ->
                    isHourlyMode = !isChecked
                    updateHourlyData(hourlyForecastBinding, item.hourlyForecast, item.globalMinTemp, item.globalMaxTemp)
                }
                
                updateHourlyData(hourlyForecastBinding, item.hourlyForecast, item.globalMinTemp, item.globalMaxTemp)
            }
        }

        private fun updateHourlyData(
            binding: ItemWeatherDailyTohourlyForecastBinding,
            data: List<WeatherHourlyForecastDto>,
            minTempRange: Double,
            maxTempRange: Double
        ) {
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

                // 각 아이템에 클릭 리스너 설정
                view.setOnClickListener {
                    if (!isClickable) return@setOnClickListener
                    isClickable = false
                    
                    isExpanded = false
                    (this@Other).binding.hourlyExpandedContainer.visibility = View.GONE
                    
                    (this@Other).binding.dayExpandMore24Btn.animate()
                        .rotation(0f)
                        .setDuration(300)
                        .withEndAction { isClickable = true }
                        .start()
                }

                hourBinding.hourlyTime.text = "${hourData.tvAmPm} ${hourData.tvHour}시"
                hourBinding.hourlyTemp.text = hourData.temperature
                val temperature = hourData.temperature?.replace("°", "")?.toDoubleOrNull() ?: 0.0
                hourBinding.hourlyTemp.setBackgroundResource(getBackgroundForTemperature(temperature))
                hourBinding.hourlyWeatherIcon.setImageResource(getWeatherIcon(hourData.weatherCode))
                hourBinding.hourlyClothingIcon.setImageResource(getClothingIcon(temperature))

                // 온도 선 설정
                // 온도선 설정
                val tempLine = hourBinding.hourlyTempLine
                tempLine.post {
                    // 최소폭 (예: 56dp)
                    val minWidthPx = TypedValue.applyDimension(
                        TypedValue.COMPLEX_UNIT_DIP, 56f, tempLine.context.resources.displayMetrics
                    ).toInt()

                    // 최대폭 (예: 120dp)
                    val maxWidthPx = TypedValue.applyDimension(
                        TypedValue.COMPLEX_UNIT_DIP, 120f, tempLine.context.resources.displayMetrics
                    ).toInt()

                    // 온도 → 비율 계산
                    val baseTemp = hourData.temperature?.replace("°", "")?.toDoubleOrNull() ?: 0.0
                    val adjustedTemp = baseTemp.coerceIn(minTempRange, maxTempRange)
                    val tempOffset = (adjustedTemp - minTempRange).toFloat()
                    val widthRatioLinear = tempOffset / (maxTempRange - minTempRange).toFloat()

                    // 곡선화 (원하면)
                    val widthRatio = (1 - Math.pow((1 - widthRatioLinear).toDouble(), 2.0)).toFloat()

                    // 🚀 최소~최대 폭 안에서 자연스럽게 비율로 계산
                    val finalWidth = (minWidthPx + (maxWidthPx - minWidthPx) * widthRatio).toInt()

                    //Log.d("온도 계산", "minWidthPx=$minWidthPx maxWidthPx=$maxWidthPx ,, minTempRange=$minTempRange, maxTempRange=$maxTempRange, temp=$adjustedTemp , width=$finalWidth")

                    val lp = tempLine.layoutParams
                    lp.width = finalWidth
                    tempLine.layoutParams = lp
                }




                val prob = hourData.probability?.replace("%", "")?.toIntOrNull() ?: 0
                if (prob >= 5) {
                    hourBinding.hourlyProbability.text = hourData.probability
                    hourBinding.hourlyPrecipitation.text = if (hourData.precipitation != "0.0mm") (" • "+hourData.precipitation) else ""
                    hourBinding.hourlyProbability.visibility = View.VISIBLE
                    hourBinding.hourlyPrecipitation.visibility = if(hourBinding.hourlyPrecipitation.text == "") View.GONE else View.VISIBLE
                } else {
                    hourBinding.hourlyPrecipitation.visibility = View.GONE
                    hourBinding.hourlyProbability.visibility = View.GONE
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
            temperature >= 23 -> R.drawable.clothing_icon_hawaiianshirt
            temperature >= 20 -> R.drawable.clothing_icon_hawaiianshirt
            temperature >= 17 -> R.drawable.clothing_icon_hawaiianshirt
            temperature >= 12 -> R.drawable.clothing_icon_hawaiianshirt
            temperature >= 9 -> R.drawable.clothing_icon_hawaiianshirt
            else -> R.drawable.clothing_icon_hawaiianshirt
        }
    }

    // 온도에 따른 배경 리소스를 설정하는 메서드
    protected fun getBackgroundForTemperature(temp: Double): Int = when {
        temp >= 30.0 -> R.drawable.sh_hourly_round_temperature_30
        temp >= 25.0 -> R.drawable.sh_hourly_round_temperature_20
        temp >= 20.0 -> R.drawable.sh_hourly_round_temperature_20
        temp >= 15.0 -> R.drawable.sh_hourly_round_temperature_15
        temp >= 10.0 -> R.drawable.sh_hourly_round_temperature_10
        else -> R.drawable.sh_hourly_round_temperature_10
    }
}