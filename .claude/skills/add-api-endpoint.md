# Add API Endpoint Skill

기존 기능에 새로운 API 엔드포인트를 추가합니다.

## 사용법
```
/add-api-endpoint [기능명] [메서드명]
```

**예시:**
```
/add-api-endpoint weather getWeeklyForecast
/add-api-endpoint finedust getDustHistory
```

---

## 수행 작업

### 1. API Service에 메서드 추가

**파일:** `data/api/{기능명}/{기능명}ApiService.kt`

```kotlin
/**
 * {메서드 설명}
 *
 * @param request {기능명}Request
 * @return Response<ApiResponse<Map<String, Any>>>
 */
@POST("/api/{기능명}/{endpoint}")
suspend fun {메서드명}(
    @Body request: {기능명}Request
): Response<ApiResponse<Map<String, Any>>>
```

---

### 2. Request DTO 추가/수정

**파일:** `data/api/{기능명}/{메서드명}Request.kt` (필요시 생성)

```kotlin
package com.example.kh_studyprojects_weatherapp.data.api.{기능명}

import com.google.gson.annotations.SerializedName

/**
 * {메서드명} API 요청 DTO
 *
 * @author [작성자명]
 * @since [날짜]
 * @version 1.0
 */
data class {메서드명}Request(
    @SerializedName("latitude")
    val latitude: Double,

    @SerializedName("longitude")
    val longitude: Double,

    @SerializedName("days")
    val days: Int = 7
)
```

---

### 3. Response DTO 추가

**파일:** `data/api/{기능명}/{메서드명}Response.kt` (필요시)

```kotlin
package com.example.kh_studyprojects_weatherapp.data.api.{기능명}

import com.google.gson.annotations.SerializedName

/**
 * {메서드명} API 응답 DTO
 *
 * @author [작성자명]
 * @since [날짜]
 * @version 1.0
 */
data class {메서드명}Response(
    @SerializedName("data")
    val data: List<{기능명}ItemDto>?
)

data class {기능명}ItemDto(
    @SerializedName("timestamp")
    val timestamp: Long?,

    @SerializedName("value")
    val value: String?
)
```

---

### 4. Mapper에 변환 함수 추가

**파일:** `data/model/{기능명}/{기능명}Mappers.kt`

```kotlin
/**
 * {메서드명} API 응답을 Domain Model로 변환
 *
 * @param data API 응답 Map
 * @return {반환타입}
 */
fun to{메서드명}Data(data: Map<String, Any>): {반환타입} {
    // TODO: 변환 로직 구현
    return {반환타입}(
        // 필드 매핑
    )
}
```

---

### 5. Repository Interface에 메서드 추가

**파일:** `domain/repository/{기능명}Repository.kt`

```kotlin
/**
 * {메서드 설명}
 *
 * @param latitude 위도
 * @param longitude 경도
 * @param days 조회 일수
 * @return Result<{반환타입}>
 */
suspend fun {메서드명}(
    latitude: Double,
    longitude: Double,
    days: Int = 7
): Result<{반환타입}>
```

---

### 6. Repository Implementation에 구현 추가

**파일:** `data/repository/{기능명}RepositoryImpl.kt`

```kotlin
/**
 * {메서드 설명} 구현
 */
override suspend fun {메서드명}(
    latitude: Double,
    longitude: Double,
    days: Int
): Result<{반환타입}> {
    return safeApiCallWithTransform(
        call = {
            apiService.{메서드명}(
                {메서드명}Request(
                    latitude = latitude,
                    longitude = longitude,
                    days = days
                )
            )
        },
        transform = { response ->
            {기능명}Mappers.to{메서드명}Data(response)
        }
    )
}
```

---

### 7. ViewModel에 메서드 추가 (선택)

**파일:** `presentation/{기능명}/{기능명}ViewModel.kt`

```kotlin
private val _{메서드명}State = MutableStateFlow<UiState<{반환타입}>>(UiState.Initial)
val {메서드명}State: StateFlow<UiState<{반환타입}>> = _{메서드명}State.asStateFlow()

/**
 * {메서드 설명}
 */
fun load{메서드명}(days: Int = 7) {
    viewModelScope.launch {
        _{메서드명}State.value = UiState.Loading

        val location = locationResolver.getEffectiveLocation()
        if (location == null) {
            _{메서드명}State.value = UiState.Error("위치 정보를 가져올 수 없습니다", Exception())
            return@launch
        }

        val result = repository.{메서드명}(
            latitude = location.latitude,
            longitude = location.longitude,
            days = days
        )

        _{메서드명}State.value = result.fold(
            onSuccess = { UiState.Success(it) },
            onFailure = { UiState.Error(it.message ?: "알 수 없는 오류", it) }
        )
    }
}
```

---

### 8. Fragment에 옵저버 추가 (선택)

**파일:** `presentation/{기능명}/{기능명}Fragment.kt`

```kotlin
private fun setup{메서드명}Observer() {
    viewLifecycleOwner.lifecycleScope.launch {
        viewLifecycleOwner.repeatOnLifecycle(Lifecycle.State.STARTED) {
            viewModel.{메서드명}State.collect { state ->
                when (state) {
                    is UiState.Loading -> {
                        // 로딩 표시
                    }
                    is UiState.Success -> {
                        update{메서드명}UI(state.data)
                    }
                    is UiState.Error -> {
                        showError(state.message)
                    }
                    is UiState.Initial -> {}
                }
            }
        }
    }
}

private fun update{메서드명}UI(data: {반환타입}) {
    // TODO: UI 업데이트
}
```

---

## 실행 절차

1. **기능명, 메서드명 입력**
2. **엔드포인트 경로 확인** (백엔드 API 문서 참조)
3. **Request/Response 필드 정의**
4. **모든 레이어 자동 업데이트**
5. **캐싱 필요 여부 확인** (선택)

---

## 주의사항

- **백엔드 API 엔드포인트**가 실제로 존재하는지 확인
- **Request/Response DTO 필드**는 백엔드 스펙과 일치해야 함
- **캐싱 로직**이 필요하면 수동으로 추가
- **에러 처리** 로직 검토 필요

---

## 예상 절감 시간

- **수작업**: 약 30~40분
- **Skill 사용**: 약 3분
- **절감률**: 90%
