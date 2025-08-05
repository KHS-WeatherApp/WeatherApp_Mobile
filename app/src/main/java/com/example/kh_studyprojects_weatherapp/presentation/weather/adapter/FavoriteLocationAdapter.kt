package com.example.kh_studyprojects_weatherapp.presentation.weather.adapter

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.example.kh_studyprojects_weatherapp.databinding.ItemFavoriteLocationBinding
import com.example.kh_studyprojects_weatherapp.domain.model.weather.FavoriteLocation

class FavoriteLocationAdapter(
    private val onLocationClick: (FavoriteLocation) -> Unit,
    private val onDeleteClick: (FavoriteLocation) -> Unit = {}
) : RecyclerView.Adapter<FavoriteLocationAdapter.FavoriteLocationViewHolder>() {

    private var locations: List<FavoriteLocation> = emptyList()
    private var isEditMode = false

    fun updateLocations(newLocations: List<FavoriteLocation>) {
        locations = newLocations
        notifyDataSetChanged()
    }
    
    /**
     * 편집 모드 토글
     */
    fun toggleEditMode() {
        isEditMode = !isEditMode
        notifyDataSetChanged()
    }
    
    /**
     * 현재 즐겨찾기 목록 반환
     */
    fun getCurrentLocations(): List<FavoriteLocation> {
        return locations
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): FavoriteLocationViewHolder {
        val binding = ItemFavoriteLocationBinding.inflate(
            LayoutInflater.from(parent.context),
            parent,
            false
        )
        return FavoriteLocationViewHolder(binding)
    }

    override fun onBindViewHolder(holder: FavoriteLocationViewHolder, position: Int) {
        holder.bind(locations[position])
    }

    override fun getItemCount(): Int = locations.size

    inner class FavoriteLocationViewHolder(
        private val binding: ItemFavoriteLocationBinding
    ) : RecyclerView.ViewHolder(binding.root) {

        fun bind(location: FavoriteLocation) {
            binding.tvLocationName.text = location.name
            binding.tvLocationAddress.text = location.address

            // TODO: 실제 날씨 데이터를 가져와서 표시
            // 임시로 기본값 설정
            binding.ivWeatherIcon.setImageResource(com.example.kh_studyprojects_weatherapp.R.drawable.weather_icon_sun)
            binding.tvTemperature.text = "25°"

            // 편집 모드에 따른 UI 변경
            if (isEditMode) {
                // 편집 모드: 삭제 버튼 표시, 아이템 클릭 비활성화
                binding.ivDelete.visibility = android.view.View.VISIBLE
                binding.root.setOnClickListener(null)
                
                // 삭제 버튼 클릭 리스너
                binding.ivDelete.setOnClickListener {
                    onDeleteClick(location)
                }
            } else {
                // 일반 모드: 삭제 버튼 숨김, 아이템 클릭 활성화
                binding.ivDelete.visibility = android.view.View.GONE
                binding.root.setOnClickListener {
                    onLocationClick(location)
                }
            }
        }
    }
} 