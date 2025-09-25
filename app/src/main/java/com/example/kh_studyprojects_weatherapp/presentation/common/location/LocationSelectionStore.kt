package com.example.kh_studyprojects_weatherapp.presentation.common.location

import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import javax.inject.Inject
import javax.inject.Singleton

/**
 * 사용자가 선택한 위치를 보관/제어하는 저장소 인터페이스입니다.
 * null이면 현재 선택된 위치가 없음을 의미합니다.
 */
interface LocationSelectionStore {
    /**
     * 현재 선택된 위치의 상태 스트림입니다.
     * UI는 이 값을 구독하여 선택 변경을 반영할 수 있습니다.
     */
    val selectedLocation: StateFlow<SelectedLocation?>
    /** 선택된 위치를 설정합니다. */
    fun setSelectedLocation(location: SelectedLocation)
    /** 선택된 위치를 해제합니다. (null) */
    fun clearSelectedLocation()
}

@Singleton
/**
 * 프로세스 메모리에만 저장하는 간단한 구현체입니다.
 * 앱을 재시작하면 값이 초기화됩니다.
 */
class InMemoryLocationSelectionStore @Inject constructor() : LocationSelectionStore {
    // 현재 선택된 위치를 보관하는 내부 상태
    private val _selectedLocation = MutableStateFlow<SelectedLocation?>(null)
    override val selectedLocation: StateFlow<SelectedLocation?> = _selectedLocation

    override fun setSelectedLocation(location: SelectedLocation) {
        _selectedLocation.value = location
    }

    override fun clearSelectedLocation() {
        _selectedLocation.value = null
    }
}


