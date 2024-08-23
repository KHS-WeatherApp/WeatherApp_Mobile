package com.example.kh_studyprojects_weatherapp.weather

import android.content.Context
import android.util.Log
import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.example.kh_studyprojects_weatherapp.databinding.WeatherHourlyForecastItemHorizontalBinding
import com.example.kh_studyprojects_weatherapp.databinding.WeatherHourlyForecastItemVerticalBinding

// RecyclerView 어댑터 클래스
class WeatherHourlyForecastFragmentAdapter(val context: Context, var isVertical: Boolean = false) :
    RecyclerView.Adapter<RecyclerView.ViewHolder>() {

//    private var items: ArrayList<WeatherHourlyForecastFragmentDto>? = null

    // 초기 데이터를 가진 아이템 리스트를 MutableList로 선언
    var items: MutableList<WeatherHourlyForecastFragmentDto> = mutableListOf(
        // 초기 데이터 리스트
        WeatherHourlyForecastFragmentDto("AM", "1시", "75%", "1.1mm", "75"),
        WeatherHourlyForecastFragmentDto("AM", "2시", "75%", "1.1mm", "75"),
        WeatherHourlyForecastFragmentDto("AM", "3시", "75%", "1.1mm", "75"),
        WeatherHourlyForecastFragmentDto("AM", "4시", "75%", "1.1mm", "75"),
        WeatherHourlyForecastFragmentDto("AM", "5시", "75%", "1.1mm", "75"),
        WeatherHourlyForecastFragmentDto("AM", "6시", "75%", "1.1mm", "75"),
        WeatherHourlyForecastFragmentDto("AM", "6시", "75%", "1.1mm", "75"),
        WeatherHourlyForecastFragmentDto("AM", "6시", "75%", "1.1mm", "75"),
        WeatherHourlyForecastFragmentDto("AM", "6시", "75%", "1.1mm", "75"),
        WeatherHourlyForecastFragmentDto("AM", "6시", "75%", "1.1mm", "75"),
        WeatherHourlyForecastFragmentDto("AM", "6시", "75%", "1.1mm", "75"),
        WeatherHourlyForecastFragmentDto("AM", "6시", "75%", "1.1mm", "75"),
        WeatherHourlyForecastFragmentDto("AM", "6시", "75%", "1.1mm", "75"),
        WeatherHourlyForecastFragmentDto("AM", "7시", "75%", "1.1mm", "75")
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
                VerticalViewHolder(binding)
            }
            else -> {
                // 가로 모드 레이아웃 바인딩 생성 및 ViewHolder 반환
                val binding = WeatherHourlyForecastItemHorizontalBinding.inflate(
                    LayoutInflater.from(parent.context), parent, false
                )
                HorizontalViewHolder(binding)
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
        private val binding: WeatherHourlyForecastItemHorizontalBinding
    ) : RecyclerView.ViewHolder(binding.root) {

        // bindItems() 메서드에서 가로 모드 뷰에 데이터를 바인딩
        fun bindItems(item: WeatherHourlyForecastFragmentDto) {
            binding.apply {
                tvPmPa.text = item.tvPmPa               // AM/PM 텍스트 설정
                tvHour.text = item.tvHour               // 시간 텍스트 설정
                probability.text = item.probability     // 강수 확률 텍스트 설정
                precipitation.text = item.precipitation // 강수량 텍스트 설정
                temperature.text = item.temperature     // 온도 텍스트 설정
            }
        }
    }

    // 세로 모드 ViewHolder 클래스
    class VerticalViewHolder(
        private val binding: WeatherHourlyForecastItemVerticalBinding
    ) : RecyclerView.ViewHolder(binding.root) {

        // bindItems() 메서드에서 세로 모드 뷰에 데이터를 바인딩
        fun bindItems(item: WeatherHourlyForecastFragmentDto) {
            binding.apply {
                tvPmPa.text = item.tvPmPa               // AM/PM 텍스트 설정
                tvHour.text = item.tvHour               // 시간 텍스트 설정
                probability.text = item.probability     // 강수 확률 텍스트 설정
                precipitation.text = item.precipitation // 강수량 텍스트 설정
                temperature.text = item.temperature     // 온도 텍스트 설정
            }
        }
    }
}
