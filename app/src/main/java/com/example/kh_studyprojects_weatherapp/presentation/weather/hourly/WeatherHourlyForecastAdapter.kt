package com.example.kh_studyprojects_weatherapp.weather.hourly

import android.content.Context
import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.constraintlayout.widget.ConstraintLayout
import androidx.recyclerview.widget.RecyclerView
import com.example.kh_studyprojects_weatherapp.R
import com.example.kh_studyprojects_weatherapp.databinding.WeatherHourlyForecastItemHorizontalBinding
import com.example.kh_studyprojects_weatherapp.databinding.WeatherHourlyForecastItemVerticalBinding

// RecyclerView 어댑터 클래스
class WeatherHourlyForecastAdapter(
    private val context: Context,
    var isVertical: Boolean = false
) : RecyclerView.Adapter<RecyclerView.ViewHolder>() {

    // 뷰 타입 상수
    companion object {
        const val VIEW_TYPE_HORIZONTAL = 0
        const val VIEW_TYPE_VERTICAL = 1
    }

    // 아이템 리스트를 List로 변경 (불변성 유지)
    private val items: List<WeatherHourlyForecastDto> = listOf(
        WeatherHourlyForecastDto("오전", "1시", "", "", "10"),
        WeatherHourlyForecastDto("", "2시", "", "", "14"),
        WeatherHourlyForecastDto("", "3시", "", "", "15"),
        WeatherHourlyForecastDto("", "4시", "", "", "16"),
        WeatherHourlyForecastDto("", "5시", "", "", "19"),
        WeatherHourlyForecastDto("", "6시", "75%", "1.1mm", "20"),
        WeatherHourlyForecastDto("", "7시", "75%", "1.1mm", "19"),
        WeatherHourlyForecastDto("", "8시", "75%", "1.1mm", "23"),
        WeatherHourlyForecastDto("", "9시", "", "", "25"),
        WeatherHourlyForecastDto("", "10시", "", "", "27"),
        WeatherHourlyForecastDto("", "11시", "", "", "30"),
        WeatherHourlyForecastDto("오후", "12시", "75%", "1.1mm", "32"),
        WeatherHourlyForecastDto("", "1시", "75%", "1.1mm", "33"),
        WeatherHourlyForecastDto("", "2시", "75%", "1.1mm", "33"),
        WeatherHourlyForecastDto("", "3시", "75%", "1.1mm", "30"),
        WeatherHourlyForecastDto("", "4시", "75%", "1.1mm", "27"),
        WeatherHourlyForecastDto("", "5시", "75%", "1.1mm", "25"),
        WeatherHourlyForecastDto("", "6시", "75%", "1.1mm", "25"),
        WeatherHourlyForecastDto("", "7시", "75%", "1.1mm", "25"),
        WeatherHourlyForecastDto("", "8시", "75%", "1.1mm", "25"),
        WeatherHourlyForecastDto("", "9시", "75%", "1.1mm", "25"),
        WeatherHourlyForecastDto("", "10시", "75%", "1.1mm", "24"),
        WeatherHourlyForecastDto("", "11시", "75%", "1.1mm", "25"),
        WeatherHourlyForecastDto("오전", "12시", "75%", "1.1mm", "25")
    )

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
        val item = items[position]
        // ViewHolder의 타입에 따라 bindItems() 메서드 호출
        when (holder) {
            is VerticalViewHolder -> holder.bindItems(item)    // 세로 모드 ViewHolder에 데이터 바인딩
            is HorizontalViewHolder -> holder.bindItems(item)  // 가로 모드 ViewHolder에 데이터 바인딩
        }
    }

    override fun getItemCount(): Int = items.size

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
                tvPmPa.text = item.tvPmPa                   // AM/PM 텍스트 설정
                tvHour.text = item.tvHour                   // 시간 텍스트 설정
                probability.text = item.probability         // 강수 확률 텍스트 설정
                precipitation.text = item.precipitation     // 강수량 텍스트 설정
                temperature.text = "${item.temperature}°"   // 온도 텍스트 설정

                // 온도에 따른 마진 설정
                val temperatureInt = item.temperature!!.toInt()
                temperatureLayoutParams.topMargin = getMarginForTemperature(temperatureInt)
                temperature.layoutParams = temperatureLayoutParams

                // 온도 배경 설정
                temperature.setBackgroundResource(getBackgroundForTemperature(temperatureInt))
            }
        }

        private fun getMarginForTemperature(temp: Int): Int = when {
            temp >= 30 -> resources.getDimensionPixelSize(R.dimen.dp_10)
            temp >= 28 -> resources.getDimensionPixelSize(R.dimen.dp_20)
            temp >= 26 -> resources.getDimensionPixelSize(R.dimen.dp_30)
            temp >= 24 -> resources.getDimensionPixelSize(R.dimen.dp_40)
            temp >= 22 -> resources.getDimensionPixelSize(R.dimen.dp_50)
            temp >= 20 -> resources.getDimensionPixelSize(R.dimen.dp_60)
            temp >= 18 -> resources.getDimensionPixelSize(R.dimen.dp_70)
            temp >= 16 -> resources.getDimensionPixelSize(R.dimen.dp_80)
            temp >= 14 -> resources.getDimensionPixelSize(R.dimen.dp_90)
            temp >= 12 -> resources.getDimensionPixelSize(R.dimen.dp_100)
            temp >= 10 -> resources.getDimensionPixelSize(R.dimen.dp_110)
            else -> resources.getDimensionPixelSize(R.dimen.dp_10)
        }

        private fun getBackgroundForTemperature(temp: Int): Int = when {
            temp >= 30 -> R.drawable.sh_hourly_round_temperature_30
            temp >= 20 -> R.drawable.sh_hourly_round_temperature_20
            temp >= 15 -> R.drawable.sh_hourly_round_temperature_15
            temp >= 10 -> R.drawable.sh_hourly_round_temperature_10
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
                tvPmPa.text = item.tvPmPa                   // AM/PM 텍스트 설정
                tvHour.text = item.tvHour                   // 시간 텍스트 설정
                probability.text = item.probability         // 강수 확률 텍스트 설정
                precipitation.text = item.precipitation     // 강수량 텍스트 설정
                temperature.text = "${item.temperature}°"   // 온도 텍스트 설정

                // item.temperature를 Int로 변환하여 마진 설정
                val temperatureInt = item.temperature!!.toInt()
                val layoutParams = vi01.layoutParams

                // 온도 넓이 속성
                layoutParams.width = when {
                    temperatureInt >= 30 -> context.resources.getDimensionPixelSize(R.dimen.dp_160)
                    temperatureInt >= 28 -> context.resources.getDimensionPixelSize(R.dimen.dp_150)
                    temperatureInt >= 26 -> context.resources.getDimensionPixelSize(R.dimen.dp_140)
                    temperatureInt >= 24 -> context.resources.getDimensionPixelSize(R.dimen.dp_130)
                    temperatureInt >= 22 -> context.resources.getDimensionPixelSize(R.dimen.dp_120)
                    temperatureInt >= 20 -> context.resources.getDimensionPixelSize(R.dimen.dp_110)
                    temperatureInt >= 18 -> context.resources.getDimensionPixelSize(R.dimen.dp_100)
                    temperatureInt >= 16 -> context.resources.getDimensionPixelSize(R.dimen.dp_90)
                    temperatureInt >= 14 -> context.resources.getDimensionPixelSize(R.dimen.dp_80)
                    temperatureInt >= 12 -> context.resources.getDimensionPixelSize(R.dimen.dp_70)
                    temperatureInt >= 10 -> context.resources.getDimensionPixelSize(R.dimen.dp_60)
                    else -> 0
                }
                vi01.layoutParams = layoutParams

                // 온도 배경 설정
                temperature.setBackgroundResource(
                    when {
                        temperatureInt >= 30 -> R.drawable.sh_hourly_round_temperature_30
                        temperatureInt >= 20 -> R.drawable.sh_hourly_round_temperature_20
                        temperatureInt >= 15 -> R.drawable.sh_hourly_round_temperature_15
                        temperatureInt >= 10 -> R.drawable.sh_hourly_round_temperature_10
                        else -> 0
                    }
                )
            }
        }
    }
}
