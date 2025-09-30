package com.example.kh_studyprojects_weatherapp.presentation.common.util

import android.util.Log
import com.example.kh_studyprojects_weatherapp.BuildConfig

/**
 * 디버그 전용 로거
 * - 릴리즈 빌드에서는 자동으로 로그가 제거됨
 * - BuildConfig.DEBUG 플래그를 활용하여 조건부 로깅
 */
object DebugLogger {
    fun d(tag: String, message: String) {
        if (BuildConfig.DEBUG) {
            Log.d(tag, message)
        }
    }

    fun i(tag: String, message: String) {
        if (BuildConfig.DEBUG) {
            Log.i(tag, message)
        }
    }

    fun w(tag: String, message: String) {
        if (BuildConfig.DEBUG) {
            Log.w(tag, message)
        }
    }

    fun e(tag: String, message: String, throwable: Throwable? = null) {
        // 에러는 릴리즈에서도 기록 (크래시 추적에 중요)
        if (throwable != null) {
            Log.e(tag, message, throwable)
        } else {
            Log.e(tag, message)
        }
    }
}