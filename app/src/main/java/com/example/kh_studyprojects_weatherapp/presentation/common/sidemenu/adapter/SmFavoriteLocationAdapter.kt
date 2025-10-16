package com.example.kh_studyprojects_weatherapp.presentation.common.sidemenu.adapter

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import androidx.recyclerview.widget.DiffUtil
import com.example.kh_studyprojects_weatherapp.databinding.ItemFavoriteLocationBinding
import com.example.kh_studyprojects_weatherapp.domain.model.location.FavoriteLocation
import com.example.kh_studyprojects_weatherapp.domain.model.weather.WeatherCommon
import com.example.kh_studyprojects_weatherapp.domain.repository.weather.WeatherRepository
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.cancel
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import kotlin.math.roundToInt

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
    private val locationIdToWeather: MutableMap<String, Pair<Int?, Int?>> = mutableMapOf()
    private var isEditMode = false

    /**
     * 구조화된 CoroutineScope
     * - SupervisorJob: 하나의 코루틴 실패가 다른 코루틴에 영향을 주지 않음
     * - Dispatchers.Main: UI 업데이트를 위한 메인 스레드
     * - onDetachedFromRecyclerView()에서 자동으로 취소됨
     */
    private val adapterScope = CoroutineScope(SupervisorJob() + Dispatchers.Main)

    /**
     * 즐겨찾기 지역 목록을 업데이트합니다.
     * 
     * @param newLocations 새로운 즐겨찾기 지역 목록
     */
    fun updateLocations(newLocations: List<FavoriteLocation>) {
        val diff = DiffUtil.calculateDiff(object : DiffUtil.Callback() {
            override fun getOldListSize(): Int = locations.size
            override fun getNewListSize(): Int = newLocations.size
            override fun areItemsTheSame(oldItemPosition: Int, newItemPosition: Int): Boolean {
                val o = locations[oldItemPosition]
                val n = newLocations[newItemPosition]
                return o.latitude == n.latitude && o.longitude == n.longitude && o.deviceId == n.deviceId
            }
            override fun areContentsTheSame(oldItemPosition: Int, newItemPosition: Int): Boolean {
                val o = locations[oldItemPosition]
                val n = newLocations[newItemPosition]
                return o == n
            }
        })
        locations = newLocations
        diff.dispatchUpdatesTo(this)
    }
    
    /**
     * 편집 모드를 토글합니다.
     * 편집 모드에서는 삭제 버튼이 표시되고 아이템 클릭이 비활성화됩니다.
     */
    fun toggleEditMode() {
        isEditMode = !isEditMode
        notifyItemRangeChanged(0, itemCount, PAYLOAD_EDIT_MODE_CHANGED)
    }

    /**
     * 편집 모드 활성 상태를 반환합니다.
     */
    fun isEditModeEnabled(): Boolean = isEditMode

    /**
     * 편집 모드를 초기화합니다.
     * 편집 모드를 비활성화하고 일반 모드로 돌아갑니다.
     */
    fun resetEditMode() {
        if (isEditMode) {
            isEditMode = false
            notifyItemRangeChanged(0, itemCount, PAYLOAD_EDIT_MODE_CHANGED)
        }
    }

    companion object {
        private const val PAYLOAD_EDIT_MODE_CHANGED = "EDIT_MODE_CHANGED"
    }
    
    /**
     * 날씨 데이터를 로딩합니다.
     *
     * 구조화된 동시성을 사용하여 생명주기 관리:
     * - adapterScope 사용으로 Adapter 제거 시 자동 취소
     * - ViewHolder 재활용 문제 해결을 위한 cacheKey 검증
     *
     * @param location 날씨 데이터를 가져올 위치 정보
     * @param holder ViewHolder (재활용 검증용)
     */
    private fun loadWeatherData(location: FavoriteLocation, holder: FavoriteLocationViewHolder) {
        weatherRepository?.let { repo ->
            val cacheKey = "${location.deviceId}:${location.latitude}:${location.longitude}"
            val cached = locationIdToWeather[cacheKey]
            if (cached != null) {
                val (temp, code) = cached
                holder.binding.tvTemperature.text = temp?.let { "${it}°" } ?: "N/A"
                code?.let { holder.binding.ivWeatherIcon.setImageResource(WeatherCommon.getWeatherIcon(it)) }
                return
            }

            // 구조화된 스코프 사용
            adapterScope.launch {
                try {
                    // IO 스레드에서 네트워크 호출
                    val result = withContext(Dispatchers.IO) {
                        repo.getWeatherInfo(location.latitude, location.longitude)
                    }

                    result.onSuccess { weatherData ->
                        // ViewHolder가 재활용되었는지 확인
                        if (holder.currentCacheKey == cacheKey) {
                            val tempInt = weatherData.current.temperature2m?.roundToInt()
                            holder.binding.tvTemperature.text = tempInt?.let { "${it}°" } ?: "N/A"

                            val weatherCode = weatherData.current.weatherCode ?: 0
                            holder.binding.ivWeatherIcon.setImageResource(WeatherCommon.getWeatherIcon(weatherCode))
                            locationIdToWeather[cacheKey] = Pair(tempInt, weatherCode)
                        }
                    }.onFailure { exception ->
                        // ViewHolder가 재활용되었는지 확인
                        if (holder.currentCacheKey == cacheKey) {
                            holder.binding.tvTemperature.text = "오류"
                        }
                    }
                } catch (e: Exception) {
                    // ViewHolder가 재활용되었는지 확인
                    if (holder.currentCacheKey == cacheKey) {
                        holder.binding.tvTemperature.text = "오류"
                    }
                }
            }
        } ?: run {
            // WeatherRepository가 없으면 기본값 표시
            holder.binding.tvTemperature.text = "N/A"
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

    override fun onBindViewHolder(holder: FavoriteLocationViewHolder, position: Int, payloads: MutableList<Any>) {
        if (payloads.isEmpty()) {
            // payload가 없으면 전체 바인딩
            super.onBindViewHolder(holder, position, payloads)
        } else {
            // payload가 있으면 편집 모드 UI만 업데이트
            if (payloads.contains(PAYLOAD_EDIT_MODE_CHANGED)) {
                holder.updateEditModeUI(isEditMode, locations[position])
            }
        }
    }

    override fun getItemCount(): Int = locations.size

    override fun getItemId(position: Int): Long {
        val item = locations[position]
        return (item.deviceId + ":" + item.latitude + ":" + item.longitude).hashCode().toLong()
    }

    /**
     * 드래그 앤 드롭 중 아이템 위치를 변경합니다.
     */
    fun onItemMove(fromPosition: Int, toPosition: Int) {
        if (fromPosition == toPosition) return
        val mutable = locations.toMutableList()
        val item = mutable.removeAt(fromPosition)
        val target = toPosition.coerceIn(0, mutable.size)
        mutable.add(target, item)
        locations = mutable
        notifyItemMoved(fromPosition, target)
    }

    /**
     * RecyclerView에서 Adapter가 분리될 때 호출됩니다.
     * 모든 진행 중인 코루틴을 취소하여 메모리 누수를 방지합니다.
     */
    override fun onDetachedFromRecyclerView(recyclerView: RecyclerView) {
        super.onDetachedFromRecyclerView(recyclerView)
        adapterScope.cancel()
    }

    /**
     * 즐겨찾기 지역 아이템을 표시하는 ViewHolder
     */
    inner class FavoriteLocationViewHolder(
        val binding: ItemFavoriteLocationBinding
    ) : RecyclerView.ViewHolder(binding.root) {

        /**
         * ViewHolder가 재활용되었는지 확인하기 위한 캐시 키
         * 코루틴이 완료되었을 때 현재 ViewHolder가 여전히 같은 데이터를 표시하는지 검증
         */
        var currentCacheKey: String? = null

        /**
         * 즐겨찾기 지역 데이터를 바인딩합니다.
         *
         * @param location 바인딩할 즐겨찾기 지역 정보
         */
        fun bind(location: FavoriteLocation) {
            // 현재 ViewHolder의 캐시 키 업데이트 (재활용 검증용)
            currentCacheKey = "${location.deviceId}:${location.latitude}:${location.longitude}"

            // tvLocationName: 동/읍/면 한글명 → 동/읍/면 → 구/군 → 시/도 순으로 우선 표시
            val displayName =
                location.region3depthHName?.takeIf { it.isNotBlank() }
                ?: location.region3depthName?.takeIf { it.isNotBlank() }
                ?: location.region2depthName?.takeIf { it.isNotBlank() }
                ?: location.region1depthName?.takeIf { it.isNotBlank() }
            binding.tvLocationName.text = displayName

            // tvLocationAddress: 전체 주소 그대로 표시
            binding.tvLocationAddress.text = location.addressName

            // 날씨 데이터 로딩 시작 (ViewHolder 전달)
            loadWeatherData(location, this)

            // 편집 모드에 따른 UI 변경
            updateEditModeUI(isEditMode, location)
        }

        /**
         * 편집 모드 UI만 업데이트합니다.
         * 날씨 데이터나 주소 정보는 재로딩하지 않습니다.
         *
         * @param editMode 편집 모드 여부
         * @param location 위치 정보 (클릭 리스너용)
         */
        fun updateEditModeUI(editMode: Boolean, location: FavoriteLocation) {
            if (editMode) {
                // 편집 모드: 삭제 버튼 표시, 아이템 클릭 비활성화, 아이콘 변경
                binding.ivDelete.visibility = android.view.View.VISIBLE
                binding.ivLocationIcon.setImageResource(com.example.kh_studyprojects_weatherapp.R.drawable.ic_current_menubar2)
                binding.root.setOnClickListener(null)

                // 삭제 버튼 클릭 리스너
                binding.ivDelete.setOnClickListener {
                    onDeleteClick(location)
                }
            } else {
                // 일반 모드: 삭제 버튼 숨김, 아이템 클릭 활성화, 아이콘 원래대로
                binding.ivDelete.visibility = android.view.View.GONE
                binding.ivLocationIcon.setImageResource(android.R.drawable.ic_menu_mylocation)
                binding.root.setOnClickListener {
                    onLocationClick(location)
                }
            }
        }
    }
}
