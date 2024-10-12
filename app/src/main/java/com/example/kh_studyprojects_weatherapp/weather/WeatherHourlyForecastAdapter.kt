package com.example.kh_studyprojects_weatherapp.weather

import android.content.Context
import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.constraintlayout.widget.ConstraintLayout
import androidx.recyclerview.widget.RecyclerView
import com.example.kh_studyprojects_weatherapp.R
import com.example.kh_studyprojects_weatherapp.databinding.WeatherHourlyForecastItemHorizontalBinding
import com.example.kh_studyprojects_weatherapp.databinding.WeatherHourlyForecastItemVerticalBinding

// RecyclerView 어댑터 클래스
class WeatherHourlyForecastAdapter(val context: Context, var isVertical: Boolean = false) :
    RecyclerView.Adapter<RecyclerView.ViewHolder>() {

//    private var items: ArrayList<WeatherHourlyForecastFragmentDto>? = null

    // 초기 데이터를 가진 아이템 리스트를 MutableList로 선언
    var items: MutableList<WeatherHourlyForecastDto> = mutableListOf(
        // 초기 데이터 리스트
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

    // 뷰 타입 상수를 companion object로 선언
    companion object {
        const val VIEW_TYPE_HORIZONTAL = 0  // 가로 모드 뷰 타입
        const val VIEW_TYPE_VERTICAL = 1    // 세로 모드 뷰 타입
    }

    // getItemViewType() 메서드에서 아이템의 뷰 타입을 반환
    override fun getItemViewType(position: Int): Int {
        // isVertical 값에 따라 세로 모드 또는 가로 모드 뷰 타입을 반환
        return if (isVertical) VIEW_TYPE_VERTICAL else VIEW_TYPE_HORIZONTAL
    }

    // onCreateViewHolder() 메서드에서 각 뷰 타입에 맞는 ViewHolder를 생성
    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): RecyclerView.ViewHolder {
        return when (viewType) {
            VIEW_TYPE_VERTICAL -> {
                // 세로 모드 레이아웃 바인딩 생성 및 ViewHolder 반환
                val binding = WeatherHourlyForecastItemVerticalBinding.inflate(
                    LayoutInflater.from(parent.context), parent, false
                )
                VerticalViewHolder(binding, parent.context)
            }
            else -> {
                // 가로 모드 레이아웃 바인딩 생성 및 ViewHolder 반환
                val binding = WeatherHourlyForecastItemHorizontalBinding.inflate(
                    LayoutInflater.from(parent.context), parent, false
                )
                HorizontalViewHolder(binding, parent.context) // context 전달
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

    // getItemCount() 메서드에서 리스트의 아이템 수를 반환
    override fun getItemCount(): Int {
        return items.size  // 아이템 리스트의 크기를 반환
    }

    // 가로 모드 ViewHolder 클래스
    class HorizontalViewHolder(
        private val binding: WeatherHourlyForecastItemHorizontalBinding,
        private val context: Context // context를 매개변수로 받음
    ) : RecyclerView.ViewHolder(binding.root) {

        // bindItems() 메서드에서 가로 모드 뷰에 데이터를 바인딩
        fun bindItems(item: WeatherHourlyForecastDto) {
            binding.apply {
                tvPmPa.text = item.tvPmPa                   // AM/PM 텍스트 설정
                tvHour.text = item.tvHour                   // 시간 텍스트 설정
                probability.text = item.probability         // 강수 확률 텍스트 설정
                precipitation.text = item.precipitation     // 강수량 텍스트 설정
                temperature.text = "${item.temperature}°"   // 온도 텍스트 설정

                // item.temperature를 Int로 변환하여 마진 설정
                val temperatureInt = item.temperature!!.toInt()
                val layoutParams = clHourlyItem06.layoutParams as ConstraintLayout.LayoutParams

                // 온도 마진 속성
                layoutParams.topMargin = when {
                    temperatureInt >= 30 -> context.resources.getDimensionPixelSize(R.dimen.dp_10)
                    temperatureInt >= 28 -> context.resources.getDimensionPixelSize(R.dimen.dp_20)
                    temperatureInt >= 26 -> context.resources.getDimensionPixelSize(R.dimen.dp_30)
                    temperatureInt >= 24 -> context.resources.getDimensionPixelSize(R.dimen.dp_40)
                    temperatureInt >= 22 -> context.resources.getDimensionPixelSize(R.dimen.dp_50)
                    temperatureInt >= 20 -> context.resources.getDimensionPixelSize(R.dimen.dp_60)
                    temperatureInt >= 18 -> context.resources.getDimensionPixelSize(R.dimen.dp_70)
                    temperatureInt >= 16 -> context.resources.getDimensionPixelSize(R.dimen.dp_80)
                    temperatureInt >= 14 -> context.resources.getDimensionPixelSize(R.dimen.dp_90)
                    temperatureInt >= 12 -> context.resources.getDimensionPixelSize(R.dimen.dp_100)
                    temperatureInt >= 10 -> context.resources.getDimensionPixelSize(R.dimen.dp_110)
                    else -> 0
                }
                clHourlyItem06.layoutParams = layoutParams

                // 온도 배경 설정
                temperature.setBackgroundResource(
                    when {
                        temperatureInt >= 30 -> R.drawable.shape_radius_01_temperature_30
                        temperatureInt >= 20 -> R.drawable.shape_radius_01_temperature_20
                        temperatureInt >= 15 -> R.drawable.shape_radius_01_temperature_15
                        temperatureInt >= 10 -> R.drawable.shape_radius_01_temperature_10
                        else -> 0 // 기본 배경이 필요하다면 여기에 설정
                    }
                )
            }
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
                        temperatureInt >= 30 -> R.drawable.shape_radius_01_temperature_30
                        temperatureInt >= 20 -> R.drawable.shape_radius_01_temperature_20
                        temperatureInt >= 15 -> R.drawable.shape_radius_01_temperature_15
                        temperatureInt >= 10 -> R.drawable.shape_radius_01_temperature_10
                        else -> 0
                    }
                )
            }
        }
    }
}
