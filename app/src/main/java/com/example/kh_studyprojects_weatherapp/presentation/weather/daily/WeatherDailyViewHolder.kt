package com.example.kh_studyprojects_weatherapp.presentation.weather.daily

import android.view.LayoutInflater
import android.view.View
import android.widget.FrameLayout
import androidx.recyclerview.widget.RecyclerView
import com.example.kh_studyprojects_weatherapp.R
import com.example.kh_studyprojects_weatherapp.databinding.ItemWeatherDailyTodayBinding
import com.example.kh_studyprojects_weatherapp.databinding.ItemWeatherDailyYesterdayBinding
import com.example.kh_studyprojects_weatherapp.databinding.ItemWeatherDailyOtherBinding
import com.example.kh_studyprojects_weatherapp.databinding.ItemWeatherDailyTohourlyForecastBinding
import com.example.kh_studyprojects_weatherapp.databinding.ItemWeatherDailyTohourlyForecastTimeBinding
import com.example.kh_studyprojects_weatherapp.data.model.weather.WeatherDailyDto
import com.example.kh_studyprojects_weatherapp.data.model.weather.WeatherHourlyForecastDto
import com.example.kh_studyprojects_weatherapp.domain.model.weather.WeatherCommon
import java.util.*
import android.util.Log
import android.util.TypedValue
import java.time.LocalDateTime

sealed class WeatherDailyViewHolder(
    private val binding: androidx.viewbinding.ViewBinding
) : RecyclerView.ViewHolder(binding.root) {

    abstract fun bind(item: WeatherDailyDto, currentApiTime: String)

    /**
     * 온도 바를 설정하는 공통 헬퍼 함수
     *
     * @param container 온도 바를 포함하는 컨테이너 뷰
     * @param temperatureBar 온도를 표시할 바 뷰
     * @param minTemp 최소 온도 (파싱된 값)
     * @param maxTemp 최대 온도 (파싱된 값)
     * @param globalMinTemp 전역 최소 온도 범위
     * @param globalMaxTemp 전역 최대 온도 범위
     */
    protected fun setupTemperatureBar(
        container: FrameLayout,
        temperatureBar: View,
        minTemp: Double?,
        maxTemp: Double?,
        globalMinTemp: Double,
        globalMaxTemp: Double
    ) {
        if (minTemp == null || maxTemp == null) {
            // 온도 변환 실패 시 처리
            Log.e("WeatherDailyViewHolder", "온도 변환 실패: min=$minTemp, max=$maxTemp")
            container.post {
                val lp = temperatureBar.layoutParams as FrameLayout.LayoutParams
                lp.width = 0
                temperatureBar.layoutParams = lp
            }
            return
        }

        // 전역 온도 범위 계산
        val fullRange = (globalMaxTemp - globalMinTemp).toFloat()

        // 안전 범위 보정
        val clampedMin = minTemp.coerceIn(globalMinTemp, globalMaxTemp)
        val clampedMax = maxTemp.coerceIn(globalMinTemp, globalMaxTemp)

        // 오프셋 계산
        val dayMinOffset = (clampedMin - globalMinTemp).toFloat()
        val dayMaxOffset = (clampedMax - globalMinTemp).toFloat()

        // 0~1 비율 계산
        val startRatio = dayMinOffset / fullRange
        val endRatio = dayMaxOffset / fullRange
        val widthRatio = (endRatio - startRatio).coerceAtLeast(0f)

        // 실제 뷰 폭 설정
        container.post {
            val containerWidth = container.width
            if (containerWidth > 0) {
                val barStartPx = containerWidth * startRatio
                val barWidthPx = containerWidth * widthRatio

                val lp = temperatureBar.layoutParams as FrameLayout.LayoutParams
                lp.width = barWidthPx.toInt().coerceAtLeast(0)
                lp.height = FrameLayout.LayoutParams.MATCH_PARENT
                lp.marginStart = barStartPx.toInt().coerceAtLeast(0)
                temperatureBar.layoutParams = lp

                temperatureBar.setBackgroundResource(R.drawable.sh_day_progressbar_01)
            }
        }
    }

    /**
     * 시간별 예보의 온도 선 너비를 설정하는 공통 헬퍼 함수
     *
     * @param tempLine 온도 선 뷰
     * @param temperature 온도 값
     * @param minTempRange 최소 온도 범위
     * @param maxTempRange 최대 온도 범위
     */
    protected fun setupHourlyTempLine(
        tempLine: View,
        temperature: Double,
        minTempRange: Double,
        maxTempRange: Double
    ) {
        tempLine.post {
            // 최소폭 (56dp)
            val minWidthPx = TypedValue.applyDimension(
                TypedValue.COMPLEX_UNIT_DIP, 56f, tempLine.context.resources.displayMetrics
            ).toInt()

            // 최대폭 (120dp)
            val maxWidthPx = TypedValue.applyDimension(
                TypedValue.COMPLEX_UNIT_DIP, 120f, tempLine.context.resources.displayMetrics
            ).toInt()

            // 온도 → 비율 계산
            val adjustedTemp = temperature.coerceIn(minTempRange, maxTempRange)
            val tempOffset = (adjustedTemp - minTempRange).toFloat()
            val widthRatioLinear = tempOffset / (maxTempRange - minTempRange).toFloat()

            // 곡선화
            val widthRatio = (1 - Math.pow((1 - widthRatioLinear).toDouble(), 2.0)).toFloat()

            // 최소~최대 폭 안에서 자연스럽게 비율로 계산
            val finalWidth = (minWidthPx + (maxWidthPx - minWidthPx) * widthRatio).toInt()

            val lp = tempLine.layoutParams
            lp.width = finalWidth
            tempLine.layoutParams = lp
        }
    }

    /**
     * 시간별 예보의 강수 확률/강수량 표시를 설정하는 공통 헬퍼 함수
     *
     * @param hourBinding 시간별 예보 아이템 바인딩
     * @param probability 강수 확률 (예: "30%")
     * @param precipitation 강수량 (예: "1.5mm")
     */
    protected fun setupHourlyPrecipitation(
        hourBinding: ItemWeatherDailyTohourlyForecastTimeBinding,
        probability: String?,
        precipitation: String?
    ) {
        val prob = probability?.replace("%", "")?.toIntOrNull() ?: 0
        if (prob >= 5) {
            hourBinding.hourlyProbability.text = probability
            hourBinding.hourlyPrecipitation.text = if (precipitation != "0.0mm") (" • $precipitation") else ""
            hourBinding.hourlyProbability.visibility = View.VISIBLE
            hourBinding.hourlyPrecipitation.visibility = if (hourBinding.hourlyPrecipitation.text == "") View.GONE else View.VISIBLE
        } else {
            hourBinding.hourlyPrecipitation.visibility = View.GONE
            hourBinding.hourlyProbability.visibility = View.GONE
        }
    }

    class Today(
        private val binding: ItemWeatherDailyTodayBinding
    ) : WeatherDailyViewHolder(binding) {
        private var isExpanded = false
        private var isHourlyMode = true // true: 1시간 단위, false: 2시간 단위
        private var isClickable = true
        private var currentItem: WeatherDailyDto? = null
        private var currentApiHour: Int = -1 // 🚀 파싱된 시간을 저장할 변수
        
        override fun bind(item: WeatherDailyDto, currentApiTime: String) {
            currentItem = item

            // 🚀 3. API 시간을 파싱하여 사용
            currentApiHour = try {
                LocalDateTime.parse(currentApiTime).hour
            } catch (e: Exception) {
                Log.e("WeatherDailyViewHolder", "API 시간 파싱 실패: $currentApiTime", e)
                -1 // 실패 시 기본값
            }

            binding.apply {
                textWeek.text = item.week
                if (item.weatherCode !in intArrayOf(0, 1, 2)) {//, 3, 45, 48, 51, 53, 55
                    textPrecipitation.text = item.precipitation
                    textHumidity.text = item.humidity
                } else {
                    textPrecipitation.text = null
                    textHumidity.text = null
                }

                textMinTemp.text = item.minTemp
                textMaxTemp.text = item.maxTemp
                imageWeather.setImageResource(WeatherCommon.getWeatherIcon(item.weatherCode))

                // 온도 파싱 및 온도 바 설정
                val minTemp = item.minTemp.replace("°", "").toDoubleOrNull()
                val maxTemp = item.maxTemp.replace("°", "").toDoubleOrNull()

                setupTemperatureBar(
                    flTemperatureContainer,
                    viewTemperatureBar,
                    minTemp,
                    maxTemp,
                    item.globalMinTemp,
                    item.globalMaxTemp
                )

                // 옷차림 아이콘 설정
                if (minTemp != null && maxTemp != null) {
                    val avgTemp = (minTemp + maxTemp) / 2.0
                    imageClothing.setImageResource(WeatherCommon.getClothingIcon(avgTemp))
                }

                // 오후 10시(22시) 이후라면 더보기 버튼 숨김
                //val currentHour = Calendar.getInstance().get(Calendar.HOUR_OF_DAY)
                dayExpandMore24Btn.visibility = if (currentApiHour >= 22) View.GONE else View.VISIBLE

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

            val filtered = if (data.size == 24 && currentApiHour != -1 && currentApiHour < 23) {
                // 현재 API 시간부터 표시 (이후가 아님)
                println("🔍 Today 필터링: API 시간=$currentApiHour, 데이터 크기=${data.size}")
                val result = data.filterIndexed { index, _ ->
                    val hour = index
                    val shouldInclude = hour >= currentApiHour && hour <= 23 && (hour - currentApiHour) % interval == 0
                    println("  시간 $hour: ${if (shouldInclude) "포함" else "제외"}")
                    shouldInclude
                }
                println("✅ 필터링 결과: ${result.size}개 시간대")
                result
            } else {
                data.filter {
                    val rawHour = it.tvHour?.toIntOrNull() ?: return@filter false
                    val hour = if (it.tvAmPm == "오전") {
                        if (rawHour == 12) 0 else rawHour
                    } else {
                        if (rawHour == 12) 12 else rawHour + 12
                    }
                    // 현재 API 시간부터 표시 (이후가 아님)
                    val shouldInclude = hour >= currentApiHour && hour <= 23 && ((hour - currentApiHour) % interval == 0)
                    println("🔍 Other 필터링: $hour (API: $currentApiHour) -> ${if (shouldInclude) "포함" else "제외"}")
                    shouldInclude
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
                val apparent_temperature = hourData.apparent_temperature?.replace("°", "")?.toDoubleOrNull() ?: 0.0
                hourBinding.hourlyTemp.setBackgroundResource(WeatherCommon.getBackgroundForTemperature(temperature))
                hourBinding.hourlyWeatherIcon.setImageResource(WeatherCommon.getWeatherIcon(hourData.weatherCode))
                hourBinding.hourlyClothingIcon.setImageResource(WeatherCommon.getClothingIcon(apparent_temperature))

                // 온도 선 설정
                setupHourlyTempLine(hourBinding.hourlyTempLine, temperature, minTempRange, maxTempRange)

                // 강수 확률/강수량 설정
                setupHourlyPrecipitation(hourBinding, hourData.probability, hourData.precipitation)

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

        override fun bind(item: WeatherDailyDto, currentApiTime: String) {
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
                imageWeather.setImageResource(WeatherCommon.getWeatherIcon(item.weatherCode))

                // 온도 파싱 및 온도 바 설정
                val minTemp = item.minTemp.replace("°", "").toDoubleOrNull()
                val maxTemp = item.maxTemp.replace("°", "").toDoubleOrNull()
                val apparentMin = item.apparent_temperature_min.replace("°", "").toDoubleOrNull()
                val apparentMax = item.apparent_temperature_max.replace("°", "").toDoubleOrNull()

                setupTemperatureBar(
                    flTemperatureContainer,
                    viewTemperatureBar,
                    minTemp,
                    maxTemp,
                    item.globalMinTemp,
                    item.globalMaxTemp
                )

                // 평균 체감온도로 옷차림 아이콘 설정
                val avgTemp = ((apparentMin ?: 0.0) + (apparentMax ?: 0.0)) / 2.0
                imageClothing.setImageResource(WeatherCommon.getClothingIcon(avgTemp))

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
                hourBinding.hourlyTemp.setBackgroundResource(WeatherCommon.getBackgroundForTemperature(temperature))
                hourBinding.hourlyWeatherIcon.setImageResource(WeatherCommon.getWeatherIcon(hourData.weatherCode))
                hourBinding.hourlyClothingIcon.setImageResource(WeatherCommon.getClothingIcon(temperature))

                // 온도 선 설정
                setupHourlyTempLine(hourBinding.hourlyTempLine, temperature, minTempRange, maxTempRange)

                // 강수 확률/강수량 설정
                setupHourlyPrecipitation(hourBinding, hourData.probability, hourData.precipitation)

                container.addView(view)
            }
        }
    }

    class Yesterday(
        private val binding: ItemWeatherDailyYesterdayBinding
    ) : WeatherDailyViewHolder(binding) {
        override fun bind(item: WeatherDailyDto, currentApiTime: String) {
            binding.apply {
                textWeek.text = item.week
                textMinTemp.text = item.minTemp
                textMaxTemp.text = item.maxTemp

                // 온도 파싱 및 온도 바 설정
                val minTemp = item.minTemp.replace("°", "").toDoubleOrNull()
                val maxTemp = item.maxTemp.replace("°", "").toDoubleOrNull()

                setupTemperatureBar(
                    flTemperatureContainer,
                    viewTemperatureBar,
                    minTemp,
                    maxTemp,
                    item.globalMinTemp,
                    item.globalMaxTemp
                )
            }
        }
    }
}