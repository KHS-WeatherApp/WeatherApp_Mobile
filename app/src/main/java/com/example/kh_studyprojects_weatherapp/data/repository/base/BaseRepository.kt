package com.example.kh_studyprojects_weatherapp.data.repository.base

import android.util.Log
import retrofit2.Response

/**
 * Repository의 공통 기능을 제공하는 베이스 클래스
 * - API 호출 및 에러 처리 로직을 중앙화
 * - 모든 Repository는 이 클래스를 상속하여 사용
 */
abstract class BaseRepository {

    /**
     * 로그 태그 (하위 클래스에서 override 가능)
     */
    protected open val TAG: String = this::class.java.simpleName

    /**
     * 안전한 API 호출 래퍼
     * - Retrofit Response를 처리하고 Result로 변환
     * - 네트워크 예외를 자동으로 처리
     *
     * @param apiCall API 호출 suspend 함수
     * @return Result<T> 성공 시 데이터, 실패 시 예외
     */
    protected suspend fun <T> safeApiCall(
        apiCall: suspend () -> Response<T>
    ): Result<T> {
        return try {
            val response = apiCall()

            Log.d(TAG, "API 호출 응답 코드: ${response.code()}")

            if (response.isSuccessful) {
                val body = response.body()
                if (body != null) {
                    Result.success(body)
                } else {
                    Log.e(TAG, "API 응답 본문이 null입니다")
                    Result.failure(NetworkException.ParseException("서버 응답이 비어있습니다"))
                }
            } else {
                val errorBody = response.errorBody()?.string() ?: "Unknown error"
                Log.e(TAG, "API 호출 실패: ${response.code()} - $errorBody")
                Result.failure(
                    NetworkException.HttpException(
                        code = response.code(),
                        message = "요청 실패: ${response.code()} - $errorBody"
                    )
                )
            }
        } catch (e: Exception) {
            Log.e(TAG, "API 호출 중 예외 발생", e)
            Result.failure(e.toNetworkException())
        }
    }

    /**
     * 로깅을 포함한 안전한 API 호출
     * - API 호출 전후 로그를 자동으로 기록
     *
     * @param logMessage 로그 메시지 (예: "날씨 정보 조회")
     * @param apiCall API 호출 suspend 함수
     * @return Result<T> 성공 시 데이터, 실패 시 예외
     */
    protected suspend fun <T> safeApiCallWithLog(
        logMessage: String,
        apiCall: suspend () -> Response<T>
    ): Result<T> {
        Log.d(TAG, "API 호출 시작: $logMessage")
        val result = safeApiCall(apiCall)

        result.fold(
            onSuccess = { Log.d(TAG, "API 호출 성공: $logMessage") },
            onFailure = { Log.e(TAG, "API 호출 실패: $logMessage - ${it.message}") }
        )

        return result
    }

    /**
     * 데이터 변환을 포함한 안전한 API 호출
     * - API 응답을 도메인 모델로 변환
     *
     * @param apiCall API 호출 suspend 함수
     * @param transform 응답 → 도메인 모델 변환 함수
     * @return Result<R> 변환된 도메인 모델 또는 예외
     */
    protected suspend fun <T, R> safeApiCallWithTransform(
        apiCall: suspend () -> Response<T>,
        transform: (T) -> R?
    ): Result<R> {
        return safeApiCall(apiCall).mapCatching { response ->
            val transformed = transform(response)
            transformed ?: throw NetworkException.ParseException("데이터 변환에 실패했습니다")
        }
    }
}