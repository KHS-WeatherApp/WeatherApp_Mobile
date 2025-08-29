package com.example.kh_studyprojects_weatherapp.presentation.common.sidemenu.adapter

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.example.kh_studyprojects_weatherapp.databinding.ItemFavoriteLocationBinding
import com.example.kh_studyprojects_weatherapp.domain.model.location.FavoriteLocation
import com.example.kh_studyprojects_weatherapp.domain.repository.weather.WeatherRepository
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

/**
 * 사이드메뉴 즐겨찾기 지역 어댑터
 * 
 * 사이드메뉴에서 즐겨찾기 지역 목록을 표시하고 관리합니다.
 * 편집 모드와 일반 모드를 지원하며, 클릭 및 삭제 이벤트를 처리합니다.
 * 
 * @author 김효동
 * @since 2025.08.26
 * @version 1.0
 */
class SmFavoriteLocationAdapter(
    private var onLocationClick: (FavoriteLocation) -> Unit,
    private var onDeleteClick: (FavoriteLocation) -> Unit = {},
    private var weatherRepository: WeatherRepository? = null
) : RecyclerView.Adapter<SmFavoriteLocationAdapter.FavoriteLocationViewHolder>() {

    private var locations: List<FavoriteLocation> = emptyList()
    private var isEditMode = false

    /**
     * 즐겨찾기 지역 목록을 업데이트합니다.
     * 
     * @param newLocations 새로운 즐겨찾기 지역 목록
     */
    fun updateLocations(newLocations: List<FavoriteLocation>) {
        locations = newLocations
        notifyDataSetChanged()
    }
    
    /**
     * 편집 모드를 토글합니다.
     * 편집 모드에서는 삭제 버튼이 표시되고 아이템 클릭이 비활성화됩니다.
     */
    fun toggleEditMode() {
        isEditMode = !isEditMode
        notifyDataSetChanged()
    }
    
    /**
     * 편집 모드를 초기화합니다.
     * 편집 모드를 비활성화하고 일반 모드로 돌아갑니다.
     */
    fun resetEditMode() {
        if (isEditMode) {
            isEditMode = false
            notifyDataSetChanged()
        }
    }
    
    /**
     * 날씨 데이터를 로딩합니다.
     * 
     * @param location 날씨 데이터를 가져올 위치 정보
     * @param binding ViewHolder의 바인딩 객체
     */
    private fun loadWeatherData(location: FavoriteLocation, binding: ItemFavoriteLocationBinding) {
        weatherRepository?.let { repo ->
            CoroutineScope(Dispatchers.IO).launch {
                try {
                    val result = repo.getWeatherInfo(location.latitude, location.longitude)
                    result.onSuccess { weatherData ->
                        withContext(Dispatchers.Main) {
                            // 온도 데이터 추출 및 표시
                            val current = weatherData["current"] as? Map<*, *>
                            val temperature = current?.get("temperature_2m") as? Double
                            if (temperature != null) {
                                binding.tvTemperature.text = "${temperature.toInt()}°"
                            } else {
                                binding.tvTemperature.text = "N/A"
                            }
                            
                            // 날씨 아이콘 설정 (기본값)
                            binding.ivWeatherIcon.setImageResource(
                                com.example.kh_studyprojects_weatherapp.R.drawable.weather_icon_sun
                            )
                        }
                    }.onFailure { exception ->
                        withContext(Dispatchers.Main) {
                            binding.tvTemperature.text = "오류"
                        }
                    }
                } catch (e: Exception) {
                    withContext(Dispatchers.Main) {
                        binding.tvTemperature.text = "오류"
                    }
                }
            }
        } ?: run {
            // WeatherRepository가 없으면 기본값 표시
            binding.tvTemperature.text = "N/A"
        }
    }
    
    /**
     * 현재 즐겨찾기 목록을 반환합니다.
     * 
     * @return 현재 즐겨찾기 지역 목록
     */
    fun getCurrentLocations(): List<FavoriteLocation> {
        return locations
    }

    /**
     * 위치 클릭 콜백을 업데이트합니다.
     */
    fun updateOnLocationClick(callback: (FavoriteLocation) -> Unit) {
        onLocationClick = callback
    }

    /**
     * 삭제 클릭 콜백을 업데이트합니다.
     */
    fun updateOnDeleteClick(callback: (FavoriteLocation) -> Unit) {
        onDeleteClick = callback
    }
    
    /**
     * WeatherRepository를 설정합니다.
     */
    fun setWeatherRepository(repository: WeatherRepository) {
        weatherRepository = repository
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

    /**
     * 즐겨찾기 지역 아이템을 표시하는 ViewHolder
     */
    inner class FavoriteLocationViewHolder(
        private val binding: ItemFavoriteLocationBinding
    ) : RecyclerView.ViewHolder(binding.root) {

        /**
         * 즐겨찾기 지역 데이터를 바인딩합니다.
         * 
         * @param location 바인딩할 즐겨찾기 지역 정보
         */
        fun bind(location: FavoriteLocation) {
            // tvLocationName: 동/읍/면 한글명 → 동/읍/면 → 구/군 → 시/도 순으로 우선 표시
            val displayName =
                location.region3depthHName?.takeIf { it.isNotBlank() }
                ?: location.region3depthName?.takeIf { it.isNotBlank() }
                ?: location.region2depthName?.takeIf { it.isNotBlank() } 
                ?: location.region1depthName?.takeIf { it.isNotBlank() }
            binding.tvLocationName.text = displayName
            
            // tvLocationAddress: 전체 주소 그대로 표시
            binding.tvLocationAddress.text = location.addressName

            // 날씨 데이터 로딩 시작
            loadWeatherData(location, binding)

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
