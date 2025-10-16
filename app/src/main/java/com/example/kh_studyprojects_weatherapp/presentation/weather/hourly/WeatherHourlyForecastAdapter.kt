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
import com.example.kh_studyprojects_weatherapp.domain.model.weather.WeatherCommon
import java.time.LocalDateTime

/**
 * 시간별 날씨 예보 RecyclerView Adapter
 *
 * 가로/세로 두 가지 레이아웃 모드를 지원하는 멀티 뷰타입 어댑터입니다.
 * 온도에 따른 동적 마진/너비 조정 및 시간별 날씨 아이콘, 옷차림 추천 등을 표시합니다.
 *
 * 주요 기능:
 * - 가로/세로 레이아웃 모드 전환
 * - 온도 기반 동적 마진/너비 계산
 * - 날씨 코드 기반 아이콘 표시
 * - 체감온도 기반 옷차림 아이콘 표시
 * - DiffUtil을 통한 효율적인 리스트 갱신
 *
 * @param context 컨텍스트
 * @param isVertical 세로 모드 여부 (기본값: false)
 * @author 김지윤
 * @since 2024.01.01
 * @version 1.0
 */
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
        // 현재 시간 설정 (java.time API 사용)
        currentHour = LocalDateTime.now().hour
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

    /**
     * 24시간 형식을 12시간 형식으로 변환
     *
     * @param hour 시간 문자열 (예: "15시")
     * @return 12시간 형식 시간 (예: "3시")
     */
    private fun convertTo12HourFormat(hour: String?): String {
        val hourInt = hour?.replace("시", "")?.toIntOrNull() ?: return ""
        return when (hourInt) {
            0 -> "12시"
            in 1..12 -> "${hourInt}시"
            else -> "${hourInt - 12}시"
        }
    }



    // onCreateViewHolder() 메서드에서 ViewHolder 생성
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

    // DiffUtil.ItemCallback 클래스를 상속받아 아이템 비교 로직 구현
    private class WeatherHourlyForecastDiffCallback : DiffUtil.ItemCallback<WeatherHourlyForecastDto>() {
        // 아이템 아이덴티티 비교
        override fun areItemsTheSame(oldItem: WeatherHourlyForecastDto, newItem: WeatherHourlyForecastDto): Boolean {
            return oldItem.tvHour == newItem.tvHour
        }

        // 아이템 내용 비교
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

        // bindItems() 메서드에서 아이템 데이터를 바인딩
        fun bindItems(item: WeatherHourlyForecastDto) {
            binding.apply {
                tvAmPm.text = adapter.getAmPmText(item.tvHour)  // AM/PM 텍스트 설정
                tvHour.text = adapter.convertTo12HourFormat(item.tvHour)  // 12시간 형식으로 변환
                probability.text = item.probability         // 강수 확률 텍스트 설정
                precipitation.text = item.precipitation     // 강수량 텍스트 설정
                temperature.text = "${item.temperature}°"   // 온도 텍스트 설정

                // 날씨 코드에 따른 이미지 설정
                imgWeather.setImageResource(WeatherCommon.getWeatherIcon(item.weatherCode))

                // 체감온도에 따른 옷 아이콘 설정
                val apparentTemp = item.apparent_temperature?.toDoubleOrNull() ?: 0.0
                imgClothes.setImageResource(WeatherCommon.getClothingIcon(apparentTemp))

                // 온도에 따른 마진 설정
                val temperatureDouble = item.temperature?.toDoubleOrNull() ?: 0.0
                temperatureLayoutParams.topMargin = getMarginForTemperature(temperatureDouble)
                temperature.layoutParams = temperatureLayoutParams

                // 온도 배경 설정
                temperature.setBackgroundResource(WeatherCommon.getBackgroundForTemperature(temperatureDouble))
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
                imgWeather.setImageResource(WeatherCommon.getWeatherIcon(item.weatherCode))

                // 체감온도에 따른 옷 아이콘 설정
                val apparentTemp = item.apparent_temperature?.toDoubleOrNull() ?: 0.0
                imgClothes.setImageResource(WeatherCommon.getClothingIcon(apparentTemp))

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
                temperature.setBackgroundResource(WeatherCommon.getBackgroundForTemperature(temperatureDouble))
            }
        }
    }
}
