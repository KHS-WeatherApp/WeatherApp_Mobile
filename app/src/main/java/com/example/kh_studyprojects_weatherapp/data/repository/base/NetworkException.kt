package com.example.kh_studyprojects_weatherapp.data.repository.base

/**
 * 네트워크 관련 예외 클래스
 */
sealed class NetworkException(message: String, cause: Throwable? = null) : Exception(message, cause) {

    /**
     * 타임아웃 예외
     */
    class TimeoutException(message: String = "요청 시간 제한을 초과했습니다. 네트워크 상태를 확인해 주세요")
        : NetworkException(message)

    /**
     * 연결 실패 예외
     */
    class ConnectionException(message: String = "서버에 연결할 수 없습니다. 데이터 연결 상태를 확인해 주세요")
        : NetworkException(message)

    /**
     * 호스트를 찾을 수 없음
     */
    class UnknownHostException(message: String = "호스트를 찾을 수 없습니다. IP 주소를 확인해 주세요")
        : NetworkException(message)

    /**
     * HTTP 오류 (4xx, 5xx)
     */
    class HttpException(
        val code: Int,
        message: String = "HTTP 오류: $code"
    ) : NetworkException(message)

    /**
     * API 응답 파싱 실패
     */
    class ParseException(message: String = "서버 응답을 처리할 수 없습니다")
        : NetworkException(message)

    /**
     * 알 수 없는 오류
     */
    class UnknownException(
        message: String = "알 수 없는 오류가 발생했습니다",
        cause: Throwable? = null
    ) : NetworkException(message, cause)
}

/**
 * Exception을 NetworkException으로 변환
 */
fun Throwable.toNetworkException(): NetworkException {
    return when (this) {
        is java.net.SocketTimeoutException -> NetworkException.TimeoutException()
        is java.net.ConnectException -> NetworkException.ConnectionException()
        is java.net.UnknownHostException -> NetworkException.UnknownHostException()
        is retrofit2.HttpException -> NetworkException.HttpException(code(), message())
        is NetworkException -> this
        else -> NetworkException.UnknownException(
            message = message ?: "처리 중 알 수 없는 오류가 발생했습니다",
            cause = this
        )
    }
}