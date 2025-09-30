package com.example.kh_studyprojects_weatherapp.presentation.weather.daily

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.DiffUtil
import com.example.kh_studyprojects_weatherapp.databinding.ItemWeatherDailyOtherBinding
import com.example.kh_studyprojects_weatherapp.databinding.ItemWeatherDailyTodayBinding
import com.example.kh_studyprojects_weatherapp.databinding.ItemWeatherDailyYesterdayBinding
import com.example.kh_studyprojects_weatherapp.data.model.weather.WeatherDailyDto

/**
 * 일별 날씨 RecyclerView Adapter
 *
 * 날짜 타입(어제/오늘/기타)에 따라 다른 레이아웃을 사용하는 멀티 뷰타입 어댑터입니다.
 * ListAdapter를 상속하여 DiffUtil을 통한 효율적인 리스트 갱신을 지원합니다.
 *
 * @author 김지윤
 * @since 2024.01.01
 * @version 1.0
 */
class WeatherDailyAdapter : ListAdapter<WeatherDailyDto, WeatherDailyViewHolder>(DiffCallback()) {

    /**
     * ViewHolder 생성
     * 날짜 타입(어제/오늘/기타)에 따라 다른 ViewHolder를 반환합니다.
     */
    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): WeatherDailyViewHolder {
        val inflater = LayoutInflater.from(parent.context)
        return when (WeatherDailyDto.Type.values()[viewType]) {
            WeatherDailyDto.Type.TODAY -> WeatherDailyViewHolder.Today(
                ItemWeatherDailyTodayBinding.inflate(inflater, parent, false)
            )
            WeatherDailyDto.Type.YESTERDAY -> WeatherDailyViewHolder.Yesterday(
                ItemWeatherDailyYesterdayBinding.inflate(inflater, parent, false)
            )
            WeatherDailyDto.Type.OTHER -> WeatherDailyViewHolder.Other(
                ItemWeatherDailyOtherBinding.inflate(inflater, parent, false)
            )
        }
    }

    // API 시간 정보 (ViewHolder에서 사용)
    private var currentApiTime: String = ""

    /**
     * 리스트와 API 시간을 함께 업데이트
     * @param list 일별 날씨 데이터 목록
     * @param currentApiTime 현재 API 시간 정보
     */
    fun submitListWithTime(list: List<WeatherDailyDto>, currentApiTime: String) {
        this.currentApiTime = currentApiTime
        submitList(list)
    }

    /**
     * 데이터 바인딩
     * ViewHolder에 날씨 데이터와 API 시간 정보를 전달합니다.
     */
    override fun onBindViewHolder(holder: WeatherDailyViewHolder, position: Int) {
        holder.bind(getItem(position), currentApiTime)
    }

    /**
     * 아이템 뷰 타입 반환
     * 날짜 타입의 ordinal 값을 반환하여 다른 레이아웃을 사용하도록 합니다.
     */
    override fun getItemViewType(position: Int) = getItem(position).type.ordinal

    /**
     * DiffUtil 콜백
     * 리스트 변경 시 효율적인 갱신을 위해 아이템 비교 로직을 제공합니다.
     */
    private class DiffCallback : DiffUtil.ItemCallback<WeatherDailyDto>() {
        /** 같은 아이템인지 확인 (날짜와 타입으로 판단) */
        override fun areItemsTheSame(oldItem: WeatherDailyDto, newItem: WeatherDailyDto) =
            oldItem.date == newItem.date && oldItem.type == newItem.type

        /** 아이템 내용이 같은지 확인 */
        override fun areContentsTheSame(oldItem: WeatherDailyDto, newItem: WeatherDailyDto) =
            oldItem == newItem
    }
}