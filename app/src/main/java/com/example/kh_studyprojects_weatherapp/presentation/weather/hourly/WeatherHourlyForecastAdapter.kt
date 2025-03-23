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
import com.example.kh_studyprojects_weatherapp.domain.model.weather.WeatherHourlyForecastDto

// RecyclerView 어댑터 클래스
class WeatherHourlyForecastAdapter(
    private val context: Context,
    var isVertical: Boolean = false
) : ListAdapter<WeatherHourlyForecastDto, RecyclerView.ViewHolder>(WeatherHourlyForecastDiffCallback()) {

    // 뷰 타입 상수
    companion object {
        const val VIEW_TYPE_HORIZONTAL = 0
        const val VIEW_TYPE_VERTICAL = 1
    }

    // ViewHolder 재사용을 위한 설정
    init {
        setHasStableIds(true)
    }

    // 각 아이템의 고유 ID 반환
    override fun getItemId(position: Int): Long = position.toLong()

    override fun getItemViewType(position: Int): Int =
        if (isVertical) VIEW_TYPE_VERTICAL else VIEW_TYPE_HORIZONTAL

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): RecyclerView.ViewHolder {
        return when (viewType) {
            VIEW_TYPE_VERTICAL -> {
                // 세로 모드 레이아웃 바인딩 생성 및 ViewHolder 반환
                val binding = WeatherHourlyForecastItemVerticalBinding.inflate(
                    LayoutInflater.from(parent.context), parent, false
                )
                VerticalViewHolder(binding, context)
            }
            else -> {
                // 가로 모드 레이아웃 바인딩 생성 및 ViewHolder 반환
                val binding = WeatherHourlyForecastItemHorizontalBinding.inflate(
                    LayoutInflater.from(parent.context), parent, false
                )
                HorizontalViewHolder(binding, context)
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
        private val context: Context
    ) : RecyclerView.ViewHolder(binding.root) {

        // 레이아웃 파라미터 캐싱
        private val temperatureLayoutParams = binding.temperature.layoutParams as ConstraintLayout.LayoutParams
        private val resources = context.resources

        fun bindItems(item: WeatherHourlyForecastDto) {
            binding.apply {
                tvAmPm.text = item.tvAmPm                   // AM/PM 텍스트 설정
                tvHour.text = item.tvHour                   // 시간 텍스트 설정
                probability.text = item.probability         // 강수 확률 텍스트 설정
                precipitation.text = item.precipitation     // 강수량 텍스트 설정
                temperature.text = "${item.temperature}°"   // 온도 텍스트 설정

                // 온도에 따른 마진 설정
                val temperatureDouble = item.temperature!!.toDouble()
                temperatureLayoutParams.topMargin = getMarginForTemperature(temperatureDouble)
                temperature.layoutParams = temperatureLayoutParams

                // 온도 배경 설정
                temperature.setBackgroundResource(getBackgroundForTemperature(temperatureDouble))
            }
        }

        private fun getMarginForTemperature(temp: Double): Int = when {
            temp >= 30.0 -> resources.getDimensionPixelSize(R.dimen.dp_150)
            temp >= 29.0 -> resources.getDimensionPixelSize(R.dimen.dp_145)
            temp >= 28.0 -> resources.getDimensionPixelSize(R.dimen.dp_140)
            temp >= 27.0 -> resources.getDimensionPixelSize(R.dimen.dp_135)
            temp >= 26.0 -> resources.getDimensionPixelSize(R.dimen.dp_130)
            temp >= 25.0 -> resources.getDimensionPixelSize(R.dimen.dp_125)
            temp >= 24.0 -> resources.getDimensionPixelSize(R.dimen.dp_120)
            temp >= 23.0 -> resources.getDimensionPixelSize(R.dimen.dp_115)
            temp >= 22.0 -> resources.getDimensionPixelSize(R.dimen.dp_110)
            temp >= 21.0 -> resources.getDimensionPixelSize(R.dimen.dp_105)
            temp >= 20.0 -> resources.getDimensionPixelSize(R.dimen.dp_100)
            temp >= 19.0 -> resources.getDimensionPixelSize(R.dimen.dp_95)
            temp >= 18.0 -> resources.getDimensionPixelSize(R.dimen.dp_90)
            temp >= 17.0 -> resources.getDimensionPixelSize(R.dimen.dp_85)
            temp >= 16.0 -> resources.getDimensionPixelSize(R.dimen.dp_80)
            temp >= 15.0 -> resources.getDimensionPixelSize(R.dimen.dp_75)
            temp >= 14.0 -> resources.getDimensionPixelSize(R.dimen.dp_70)
            temp >= 13.0 -> resources.getDimensionPixelSize(R.dimen.dp_65)
            temp >= 12.0 -> resources.getDimensionPixelSize(R.dimen.dp_60)
            temp >= 11.0 -> resources.getDimensionPixelSize(R.dimen.dp_55)
            temp >= 10.0 -> resources.getDimensionPixelSize(R.dimen.dp_50)
            temp >= 9.0 -> resources.getDimensionPixelSize(R.dimen.dp_45)
            temp >= 8.0 -> resources.getDimensionPixelSize(R.dimen.dp_40)
            temp >= 7.0 -> resources.getDimensionPixelSize(R.dimen.dp_35)
            temp >= 6.0 -> resources.getDimensionPixelSize(R.dimen.dp_30)
            temp >= 5.0 -> resources.getDimensionPixelSize(R.dimen.dp_25)
            temp >= 4.0 -> resources.getDimensionPixelSize(R.dimen.dp_20)
            temp >= 3.0 -> resources.getDimensionPixelSize(R.dimen.dp_15)
            temp >= 2.0 -> resources.getDimensionPixelSize(R.dimen.dp_10)
            temp >= 1.0 -> resources.getDimensionPixelSize(R.dimen.dp_5)
            temp >= 0.0 -> resources.getDimensionPixelSize(R.dimen.dp_0)
            else -> resources.getDimensionPixelSize(R.dimen.dp_0)
        }

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
        private val context: Context // context를 매개변수로 받음
    ) : RecyclerView.ViewHolder(binding.root) {

        // bindItems() 메서드에서 세로 모드 뷰에 데이터를 바인딩
        fun bindItems(item: WeatherHourlyForecastDto) {
            binding.apply {
                tvAmPm.text = item.tvAmPm                   // AM/PM 텍스트 설정
                tvHour.text = item.tvHour                   // 시간 텍스트 설정
                probability.text = item.probability         // 강수 확률 텍스트 설정
                precipitation.text = item.precipitation     // 강수량 텍스트 설정
                temperature.text = "${item.temperature}°"   // 온도 텍스트 설정

                // item.temperature를 Double로 변환하여 마진 설정
                val temperatureDouble = item.temperature!!.toDouble()
                val layoutParams = vi01.layoutParams

                // 온도 넓이 속성
                layoutParams.width = when {
                    temperatureDouble >= 30.0 -> context.resources.getDimensionPixelSize(R.dimen.dp_160)
                    temperatureDouble >= 29.0 -> context.resources.getDimensionPixelSize(R.dimen.dp_150)
                    temperatureDouble >= 28.0 -> context.resources.getDimensionPixelSize(R.dimen.dp_140)
                    temperatureDouble >= 27.0 -> context.resources.getDimensionPixelSize(R.dimen.dp_130)
                    temperatureDouble >= 26.0 -> context.resources.getDimensionPixelSize(R.dimen.dp_120)
                    temperatureDouble >= 25.0 -> context.resources.getDimensionPixelSize(R.dimen.dp_110)
                    temperatureDouble >= 24.0 -> context.resources.getDimensionPixelSize(R.dimen.dp_100)
                    temperatureDouble >= 23.0 -> context.resources.getDimensionPixelSize(R.dimen.dp_90)
                    temperatureDouble >= 22.0 -> context.resources.getDimensionPixelSize(R.dimen.dp_80)
                    temperatureDouble >= 21.0 -> context.resources.getDimensionPixelSize(R.dimen.dp_70)
                    temperatureDouble >= 20.0 -> context.resources.getDimensionPixelSize(R.dimen.dp_60)
                    temperatureDouble >= 19.0 -> context.resources.getDimensionPixelSize(R.dimen.dp_50)
                    temperatureDouble >= 18.0 -> context.resources.getDimensionPixelSize(R.dimen.dp_40)
                    temperatureDouble >= 17.0 -> context.resources.getDimensionPixelSize(R.dimen.dp_30)
                    temperatureDouble >= 16.0 -> context.resources.getDimensionPixelSize(R.dimen.dp_20)
                    temperatureDouble >= 15.0 -> context.resources.getDimensionPixelSize(R.dimen.dp_10)
                    temperatureDouble >= 14.0 -> context.resources.getDimensionPixelSize(R.dimen.dp_9)
                    temperatureDouble >= 13.0 -> context.resources.getDimensionPixelSize(R.dimen.dp_8)
                    temperatureDouble >= 12.0 -> context.resources.getDimensionPixelSize(R.dimen.dp_7)
                    temperatureDouble >= 11.0 -> context.resources.getDimensionPixelSize(R.dimen.dp_6)
                    temperatureDouble >= 10.0 -> context.resources.getDimensionPixelSize(R.dimen.dp_5)
                    else -> 0
                }
                vi01.layoutParams = layoutParams

                // 온도 배경 설정
                temperature.setBackgroundResource(
                    when {
                        temperatureDouble >= 30.0 -> R.drawable.sh_hourly_round_temperature_30
                        temperatureDouble >= 25.0 -> R.drawable.sh_hourly_round_temperature_20
                        temperatureDouble >= 20.0 -> R.drawable.sh_hourly_round_temperature_20
                        temperatureDouble >= 15.0 -> R.drawable.sh_hourly_round_temperature_15
                        temperatureDouble >= 10.0 -> R.drawable.sh_hourly_round_temperature_10
                        else -> 0
                    }
                )
            }
        }
    }
}
