package com.example.kh_studyprojects_weatherapp.presentation.common.sidemenu

import android.content.Context
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.kh_studyprojects_weatherapp.databinding.ActivityMainBinding
import com.example.kh_studyprojects_weatherapp.domain.model.location.FavoriteLocation
import com.example.kh_studyprojects_weatherapp.presentation.common.sidemenu.adapter.SmFavoriteLocationAdapter
import android.widget.Toast

/**
 * 사이드메뉴 즐겨찾기 기능을 관리하는 Manager 클래스
 * 
 * 즐겨찾기 지역의 RecyclerView 설정, 클릭/삭제 처리, 편집 모드 등을 담당합니다.
 * 사용자가 등록한 즐겨찾기 지역을 효율적으로 관리하고 표시합니다.
 * 
 * @author 김효동
 * @since 2025.08.26
 * @version 1.0
 */
class SmFavoriteManager(
    private val context: Context,
    private val binding: ActivityMainBinding,
    private val adapter: SmFavoriteLocationAdapter
) {
    /**
     * 즐겨찾기 지역 RecyclerView를 설정합니다.
     * LinearLayoutManager와 어댑터를 연결하여 즐겨찾기 목록을 표시합니다.
     */
    fun setupFavoriteLocationsRecyclerView() {
        val recyclerView = binding.sideMenuContent.rvFavoriteLocations

        recyclerView.apply {
            layoutManager = LinearLayoutManager(context)
            adapter = this@SmFavoriteManager.adapter
        }
    }

    /**
     * 즐겨찾기 지역 클릭을 처리합니다.
     * 해당 지역의 날씨 정보를 가져와서 표시하는 기능을 담당합니다.
     * 
     * @param location 클릭된 즐겨찾기 지역 정보
     */
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
