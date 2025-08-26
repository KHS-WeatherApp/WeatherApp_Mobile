package com.example.kh_studyprojects_weatherapp.presentation.common.sideMenu

import android.content.Context
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.kh_studyprojects_weatherapp.databinding.ActivityMainBinding
import com.example.kh_studyprojects_weatherapp.domain.model.location.FavoriteLocation
import com.example.kh_studyprojects_weatherapp.presentation.weather.adapter.FavoriteLocationAdapter
import android.widget.Toast

class SideMenuFavoriteManager(
    private val context: Context,
    private val binding: ActivityMainBinding,
    private val adapter: FavoriteLocationAdapter
) {
    fun setupFavoriteLocationsRecyclerView() {
        val recyclerView = binding.sideMenuContent.rvFavoriteLocations

        recyclerView.apply {
            layoutManager = LinearLayoutManager(context)
            adapter = this@SideMenuFavoriteManager.adapter
        }
    }

    fun handleFavoriteLocationClick(location: FavoriteLocation) {
        // TODO: 해당 지역의 날씨 정보를 가져와서 표시
        // 현재는 Toast 메시지로 표시
        Toast.makeText(
            context,
            "${location.addressName}의 날씨 정보를 가져옵니다.",
            Toast.LENGTH_SHORT
        ).show()
    }

    fun handleFavoriteLocationDelete(location: FavoriteLocation) {
        // TODO: DB에서 즐겨찾기 지역 삭제
        Toast.makeText(
            context,
            "${location.addressName}을(를) 즐겨찾기에서 삭제합니다.",
            Toast.LENGTH_SHORT
        ).show()

        // 현재 리스트에서 해당 지역 제거
        val currentLocations = adapter.getCurrentLocations().toMutableList()
        currentLocations.remove(location)
        adapter.updateLocations(currentLocations)
    }

    fun handleEditFavoriteClick() {
        // TODO: 편집 모드 활성화
        // 1. 즐겨찾기 아이템에 삭제 버튼 표시
        // 2. 드래그 앤 드롭으로 순서 변경 가능
        // 3. 편집 완료 버튼으로 모드 종료
        Toast.makeText(
            context,
            "편집 모드가 활성화되었습니다.",
            Toast.LENGTH_SHORT
        ).show()

        // 편집 모드 토글
        toggleEditMode()
    }

    fun toggleEditMode() {
        // TODO: 편집 모드 상태 관리
        // 1. 어댑터에 편집 모드 상태 전달
        // 2. UI 업데이트 (삭제 버튼 표시/숨김)
        // 3. 드래그 앤 드롭 활성화/비활성화
        adapter.toggleEditMode()
    }
}
