package com.example.kh_studyprojects_weatherapp.util

import android.content.Context
import android.provider.Settings
import android.util.Log

/**
 * 디바이스 고유 식별자 유틸리티
 * 
 * 시스템 ANDROID_ID를 반환합니다.
 * 즐겨찾기 지역 등 사용자별 데이터를 구분하는 데 사용됩니다.
 *
 * @author 김효동
 * @since 2025.08.26
 * @version 1.1
 */
object DeviceIdUtil {
    /**
     * 디바이스 고유 식별자를 가져옵니다.
     * 
     * @param context 컨텍스트
     * @return 디바이스 고유 식별자
     */
    fun getDeviceId(context: Context): String {
        val androidId = Settings.Secure.getString(context.contentResolver,Settings.Secure.ANDROID_ID)
        val result = androidId ?: "unknown"
        Log.d("DeviceIdUtil", "ANDROID_ID 사용: $result")
        return result
    }
}

