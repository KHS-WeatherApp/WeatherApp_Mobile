# Create Feature Module Skill

Clean Architecture 기반 새 기능 모듈을 자동으로 생성합니다.

## 사용법
```
/create-feature-module [기능명]
```

**예시:**
```
/create-feature-module alerts
/create-feature-module airquality
```

## 생성되는 파일 구조

### 1. Domain Layer (도메인 계층)

#### `domain/model/{기능명}/{기능명}Data.kt`
```kotlin
package com.example.kh_studyprojects_weatherapp.domain.model.{기능명}

/**
 * {기능명} 도메인 모델
 *
 * @author [작성자명]
 * @since [날짜]
 * @version 1.0
 */
data class {기능명}Data(
    val id: String,
    val current: {기능명}Current,
    val timestamp: Long = System.currentTimeMillis()
)
```

#### `domain/model/{기능명}/{기능명}Current.kt`
```kotlin
package com.example.kh_studyprojects_weatherapp.domain.model.{기능명}

/**
 * {기능명} 현재 상태 정보
 *
 * @author [작성자명]
 * @since [날짜]
 * @version 1.0
 */
data class {기능명}Current(
    val value: String,
    val description: String,
    val updatedAt: String
)
```

#### `domain/repository/{기능명}/{기능명}Repository.kt`
```kotlin
package com.example.kh_studyprojects_weatherapp.domain.repository.{기능명}

import com.example.kh_studyprojects_weatherapp.domain.model.{기능명}.{기능명}Data

/**
 * {기능명} Repository 인터페이스
 *
 * @author [작성자명]
 * @since [날짜]
 * @version 1.0
 */
interface {기능명}Repository {
    /**
     * {기능명} 정보 조회
     *
     * @param latitude 위도
     * @param longitude 경도
     * @return Result<{기능명}Data>
     */
    suspend fun get{기능명}Info(latitude: Double, longitude: Double): Result<{기능명}Data>
}
```

---

### 2. Data Layer (데이터 계층)

#### `data/api/{기능명}/{기능명}ApiService.kt`
```kotlin
package com.example.kh_studyprojects_weatherapp.data.api.{기능명}

import com.example.kh_studyprojects_weatherapp.data.api.common.ApiResponse
import retrofit2.Response
import retrofit2.http.Body
import retrofit2.http.POST

/**
 * {기능명} API Service
 *
 * @author [작성자명]
 * @since [날짜]
 * @version 1.0
 */
interface {기능명}ApiService {
    /**
     * {기능명} 정보 조회 API
     *
     * @param request {기능명}Request
     * @return Response<ApiResponse<Map<String, Any>>>
     */
    @POST("/api/{기능명}/info")
    suspend fun get{기능명}Info(
        @Body request: {기능명}Request
    ): Response<ApiResponse<Map<String, Any>>>
}
```

#### `data/api/{기능명}/{기능명}Request.kt`
```kotlin
package com.example.kh_studyprojects_weatherapp.data.api.{기능명}

import com.google.gson.annotations.SerializedName

/**
 * {기능명} API 요청 DTO
 *
 * @author [작성자명]
 * @since [날짜]
 * @version 1.0
 */
data class {기능명}Request(
    @SerializedName("latitude")
    val latitude: Double,

    @SerializedName("longitude")
    val longitude: Double,

    @SerializedName("query")
    val query: String
)
```

#### `data/api/{기능명}/{기능명}Response.kt`
```kotlin
package com.example.kh_studyprojects_weatherapp.data.api.{기능명}

import com.google.gson.annotations.SerializedName

/**
 * {기능명} API 응답 DTO
 *
 * @author [작성자명]
 * @since [날짜]
 * @version 1.0
 */
data class {기능명}Response(
    @SerializedName("id")
    val id: String?,

    @SerializedName("current")
    val current: {기능명}CurrentDto?,

    @SerializedName("timestamp")
    val timestamp: Long?
)

data class {기능명}CurrentDto(
    @SerializedName("value")
    val value: String?,

    @SerializedName("description")
    val description: String?,

    @SerializedName("updated_at")
    val updatedAt: String?
)
```

#### `data/model/{기능명}/{기능명}Dto.kt`
```kotlin
package com.example.kh_studyprojects_weatherapp.data.model.{기능명}

import com.google.gson.annotations.SerializedName

/**
 * {기능명} 데이터 전송 객체
 *
 * @author [작성자명]
 * @since [날짜]
 * @version 1.0
 */
data class {기능명}Dto(
    @SerializedName("id")
    val id: String?,

    @SerializedName("value")
    val value: String?,

    @SerializedName("description")
    val description: String?
)
```

#### `data/model/{기능명}/{기능명}Mappers.kt`
```kotlin
package com.example.kh_studyprojects_weatherapp.data.model.{기능명}

import com.example.kh_studyprojects_weatherapp.domain.model.{기능명}.{기능명}Data
import com.example.kh_studyprojects_weatherapp.domain.model.{기능명}.{기능명}Current

/**
 * {기능명} DTO → Domain Model 변환기
 *
 * @author [작성자명]
 * @since [날짜]
 * @version 1.0
 */
object {기능명}Mappers {
    /**
     * API 응답 Map을 {기능명}Data로 변환
     *
     * @param data API 응답 Map
     * @return {기능명}Data
     */
    fun toData(data: Map<String, Any>): {기능명}Data {
        val currentMap = data["current"] as? Map<*, *>

        return {기능명}Data(
            id = data["id"]?.toString() ?: "",
            current = {기능명}Current(
                value = currentMap?.get("value")?.toString() ?: "",
                description = currentMap?.get("description")?.toString() ?: "",
                updatedAt = currentMap?.get("updated_at")?.toString() ?: ""
            ),
            timestamp = (data["timestamp"] as? Number)?.toLong() ?: System.currentTimeMillis()
        )
    }
}
```

#### `data/repository/{기능명}/{기능명}RepositoryImpl.kt`
```kotlin
package com.example.kh_studyprojects_weatherapp.data.repository.{기능명}

import com.example.kh_studyprojects_weatherapp.data.api.{기능명}.{기능명}ApiService
import com.example.kh_studyprojects_weatherapp.data.api.{기능명}.{기능명}Request
import com.example.kh_studyprojects_weatherapp.data.model.{기능명}.{기능명}Mappers
import com.example.kh_studyprojects_weatherapp.data.repository.base.BaseRepository
import com.example.kh_studyprojects_weatherapp.domain.model.{기능명}.{기능명}Data
import com.example.kh_studyprojects_weatherapp.domain.repository.{기능명}.{기능명}Repository
import javax.inject.Inject

/**
 * {기능명} Repository 구현체
 * - BaseRepository를 상속하여 공통 API 호출 로직 재사용
 * - 30초 캐싱 지원 (위치 변경 시 캐시 무효화)
 *
 * @property apiService {기능명} API Service
 * @author [작성자명]
 * @since [날짜]
 * @version 1.0
 */
class {기능명}RepositoryImpl @Inject constructor(
    private val apiService: {기능명}ApiService
) : BaseRepository(), {기능명}Repository {
    override val TAG = "{기능명}Repository"

    // 캐시 데이터
    private var cachedData: {기능명}Data? = null
    private var cachedLocation: Pair<Double, Double>? = null
    private var cacheTimestamp: Long = 0L

    // 캐시 유효 기간 (30초)
    private companion object {
        const val CACHE_DURATION_MS = 30_000L
    }

    /**
     * {기능명} 정보 조회
     *
     * @param latitude 위도
     * @param longitude 경도
     * @return Result<{기능명}Data>
     */
    override suspend fun get{기능명}Info(
        latitude: Double,
        longitude: Double
    ): Result<{기능명}Data> {
        // 캐시 확인
        val isCacheValid = isCacheValid(latitude, longitude)
        if (isCacheValid && cachedData != null) {
            return Result.success(cachedData!!)
        }

        // 캐시가 없거나 만료됨 -> API 호출
        return safeApiCallWithTransform(
            apiCall = {
                apiService.get{기능명}Info(
                    {기능명}Request(
                        latitude = latitude,
                        longitude = longitude,
                        query = "full"
                    )
                )
            },
            transform = { response -> {기능명}Mappers.toData(response) }
        ).onSuccess { data ->
            // 캐시 업데이트
            updateCache(latitude, longitude, data)
        }
    }

    /**
     * 캐시가 유효한지 확인
     */
    private fun isCacheValid(latitude: Double, longitude: Double): Boolean {
        val now = System.currentTimeMillis()
        val isSameLocation = cachedLocation == Pair(latitude, longitude)
        val isNotExpired = (now - cacheTimestamp) < CACHE_DURATION_MS

        return isSameLocation && isNotExpired && cachedData != null
    }

    /**
     * 캐시 업데이트
     */
    private fun updateCache(latitude: Double, longitude: Double, data: {기능명}Data) {
        cachedData = data
        cachedLocation = Pair(latitude, longitude)
        cacheTimestamp = System.currentTimeMillis()
    }
}
```

#### `data/repository/{기능명}/RepositoryModule.kt`
```kotlin
package com.example.kh_studyprojects_weatherapp.data.repository.{기능명}

import com.example.kh_studyprojects_weatherapp.domain.repository.{기능명}.{기능명}Repository
import dagger.Binds
import dagger.Module
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

/**
 * {기능명} Repository Hilt 모듈
 *
 * @author [작성자명]
 * @since [날짜]
 * @version 1.0
 */
@Module
@InstallIn(SingletonComponent::class)
abstract class RepositoryModule {

    /**
     * {기능명}Repository 인터페이스를 구현체로 바인딩
     */
    @Binds
    @Singleton
    abstract fun bind{기능명}Repository(
        impl: {기능명}RepositoryImpl
    ): {기능명}Repository
}
```

---

### 3. Presentation Layer (프레젠테이션 계층)

#### `presentation/{기능명}/{기능명}Fragment.kt`
```kotlin
package com.example.kh_studyprojects_weatherapp.presentation.{기능명}

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.viewModels
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.lifecycleScope
import androidx.lifecycle.repeatOnLifecycle
import com.example.kh_studyprojects_weatherapp.databinding.Fragment{기능명}Binding
import com.example.kh_studyprojects_weatherapp.presentation.common.base.BaseNavigationFragment
import com.example.kh_studyprojects_weatherapp.presentation.common.base.RefreshableFragment
import com.example.kh_studyprojects_weatherapp.presentation.common.base.UiState
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.launch

/**
 * {기능명} 화면 Fragment
 *
 * @author [작성자명]
 * @since [날짜]
 * @version 1.0
 */
@AndroidEntryPoint
class {기능명}Fragment : BaseNavigationFragment(), RefreshableFragment {

    private var _binding: Fragment{기능명}Binding? = null
    private val binding get() = _binding!!

    private val viewModel: {기능명}ViewModel by viewModels()

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = Fragment{기능명}Binding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        setupObservers()
        setupListeners()

        // 초기 데이터 로드
        viewModel.loadData()
    }

    /**
     * 상태 구독 설정
     */
    private fun setupObservers() {
        viewLifecycleOwner.lifecycleScope.launch {
            viewLifecycleOwner.repeatOnLifecycle(Lifecycle.State.STARTED) {
                viewModel.uiState.collect { state ->
                    when (state) {
                        is UiState.Initial -> {
                            // 초기 상태
                        }
                        is UiState.Loading -> {
                            showLoading()
                        }
                        is UiState.Success -> {
                            hideLoading()
                            updateUI(state.data)
                        }
                        is UiState.Error -> {
                            hideLoading()
                            showError(state.message)
                        }
                    }
                }
            }
        }
    }

    /**
     * 리스너 설정
     */
    private fun setupListeners() {
        // TODO: 버튼 클릭 리스너 등 추가
    }

    /**
     * UI 업데이트
     */
    private fun updateUI(data: Any) {
        // TODO: 데이터로 UI 업데이트
    }

    /**
     * 로딩 표시
     */
    private fun showLoading() {
        // TODO: 프로그레스바 표시
    }

    /**
     * 로딩 숨김
     */
    private fun hideLoading() {
        // TODO: 프로그레스바 숨김
    }

    /**
     * 에러 표시
     */
    private fun showError(message: String) {
        // TODO: 에러 메시지 표시
    }

    /**
     * 새로고침
     */
    override fun onRefresh() {
        viewModel.loadData()
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}
```

#### `presentation/{기능명}/{기능명}ViewModel.kt`

**옵션 A: BaseLoadViewModel 사용 (간결, 추천)**

```kotlin
package com.example.kh_studyprojects_weatherapp.presentation.{기능명}

import androidx.lifecycle.viewModelScope
import com.example.kh_studyprojects_weatherapp.domain.model.{기능명}.{기능명}Data
import com.example.kh_studyprojects_weatherapp.domain.repository.{기능명}.{기능명}Repository
import com.example.kh_studyprojects_weatherapp.presentation.common.base.BaseLoadViewModel
import com.example.kh_studyprojects_weatherapp.presentation.common.location.EffectiveLocationResolver
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.launch
import javax.inject.Inject

/**
 * {기능명} ViewModel
 * - BaseLoadViewModel<T>를 상속받아 공통 상태 관리 로직 재사용
 * - uiState: StateFlow<UiState<{기능명}Data>>
 *
 * @property repository {기능명} Repository
 * @property locationResolver 위치 정보 리졸버
 * @author [작성자명]
 * @since [날짜]
 * @version 1.0
 */
@HiltViewModel
class {기능명}ViewModel @Inject constructor(
    private val repository: {기능명}Repository,
    private val locationResolver: EffectiveLocationResolver
) : BaseLoadViewModel<{기능명}Data>() {

    /**
     * 데이터 로드
     */
    fun loadData() {
        viewModelScope.launch {
            // 현재 위치 가져오기
            val location = locationResolver.getEffectiveLocation() ?: return@launch

            // 데이터 로드 (BaseLoadViewModel의 loadData 사용)
            loadData {
                repository.get{기능명}Info(
                    latitude = location.latitude,
                    longitude = location.longitude
                )
            }
        }
    }
}
```

**옵션 B: ViewModel 직접 상속 (세밀한 제어)**

```kotlin
package com.example.kh_studyprojects_weatherapp.presentation.{기능명}

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.kh_studyprojects_weatherapp.domain.model.{기능명}.{기능명}Data
import com.example.kh_studyprojects_weatherapp.domain.repository.{기능명}.{기능명}Repository
import com.example.kh_studyprojects_weatherapp.presentation.common.location.EffectiveLocationResolver
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

/**
 * {기능명} ViewModel
 * - 직접 StateFlow 관리 (세밀한 제어 가능)
 *
 * @property repository {기능명} Repository
 * @property locationResolver 위치 정보 리졸버
 * @author [작성자명]
 * @since [날짜]
 * @version 1.0
 */
@HiltViewModel
class {기능명}ViewModel @Inject constructor(
    private val repository: {기능명}Repository,
    private val locationResolver: EffectiveLocationResolver
) : ViewModel() {

    private val _isLoading = MutableStateFlow(false)
    val isLoading: StateFlow<Boolean> = _isLoading.asStateFlow()

    private val _data = MutableStateFlow<{기능명}Data?>(null)
    val data: StateFlow<{기능명}Data?> = _data.asStateFlow()

    private val _error = MutableStateFlow<String?>(null)
    val error: StateFlow<String?> = _error.asStateFlow()

    /**
     * 데이터 로드
     */
    fun loadData() {
        viewModelScope.launch {
            _isLoading.value = true
            _error.value = null

            try {
                val location = locationResolver.getEffectiveLocation()
                if (location == null) {
                    _error.value = "위치 정보를 가져올 수 없습니다"
                    return@launch
                }

                val result = repository.get{기능명}Info(
                    latitude = location.latitude,
                    longitude = location.longitude
                )

                result.fold(
                    onSuccess = { _data.value = it },
                    onFailure = { _error.value = it.message ?: "알 수 없는 오류" }
                )
            } finally {
                _isLoading.value = false
            }
        }
    }
}
```

---

### 4. NetworkModule에 API Service 추가

`di/NetworkModule.kt`에 다음 코드 추가:

```kotlin
@Provides
@Singleton
fun provide{기능명}ApiService(
    @BackendRetrofit retrofit: Retrofit
): {기능명}ApiService {
    return retrofit.create({기능명}ApiService::class.java)
}
```

---

## 실행 절차

1. **기능명 입력** (PascalCase로 변환됨)
2. **작성자명, 날짜 입력**
3. **모든 파일 자동 생성**
4. **NetworkModule 자동 업데이트**
5. **레이아웃 XML 생성** (fragment_{기능명}.xml)

---

## 주의사항

- 기능명은 **영문 단수형** 권장 (예: Alert, AirQuality)
- 생성 후 **백엔드 API 엔드포인트**를 실제 값으로 수정 필요
- **레이아웃 XML**은 수동으로 UI 구성 필요
- **AndroidManifest.xml**, **Navigation Graph** 수동 업데이트 필요

---

## 예상 절감 시간

- **수작업**: 약 1.5~2시간
- **Skill 사용**: 약 5분
- **절감률**: 95%
