# Complete Fragment Integration Skill

기존 Fragment를 완전한 Clean Architecture 구조로 통합합니다.

## 사용법
```
/complete-fragment-integration [기능명]
```

**예시:**
```
/complete-fragment-integration finedust
/complete-fragment-integration setting
```

---

## 적용 대상

**현재 미완성 Fragment:**
- `FinedustFragment` ❌ (ViewModel, Repository 없음)
- `SettingFragment` ❌ (ViewModel, Repository 없음)

---

## 수행 작업

### Phase 1: 기존 Fragment 분석

1. **Fragment 코드 읽기**
   - 현재 UI 구성 파악
   - 사용 중인 데이터 타입 확인
   - 이벤트 리스너 분석

2. **필요한 데이터 모델 정의**
   - Domain Model 설계
   - DTO 설계
   - API 엔드포인트 확인

---

### Phase 2: Domain Layer 생성

#### 1. Domain Model 생성

**파일:** `domain/model/{기능명}/{기능명}Data.kt`

```kotlin
package com.example.kh_studyprojects_weatherapp.domain.model.{기능명}

/**
 * {기능명} 도메인 모델
 *
 * @property id 고유 ID
 * @property current 현재 상태
 * @property timestamp 타임스탬프
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

#### 2. Repository Interface 생성

**파일:** `domain/repository/{기능명}/{기능명}Repository.kt`

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

### Phase 3: Data Layer 생성

#### 1. API Service 생성

**파일:** `data/api/{기능명}/{기능명}ApiService.kt`

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
    @POST("/api/{기능명}/info")
    suspend fun get{기능명}Info(
        @Body request: {기능명}Request
    ): Response<ApiResponse<Map<String, Any>>>
}
```

#### 2. Repository Implementation 생성

**파일:** `data/repository/{기능명}/{기능명}RepositoryImpl.kt`

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
 * - BaseRepository를 상속받아 공통 API 호출 로직 재사용
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

    override suspend fun get{기능명}Info(
        latitude: Double,
        longitude: Double
    ): Result<{기능명}Data> {
        return safeApiCallWithTransform(
            apiCall = {
                apiService.get{기능명}Info(
                    {기능명}Request(latitude, longitude, "full")
                )
            },
            transform = { response ->
                {기능명}Mappers.toData(response)
            }
        )
    }
}
```

#### 3. Hilt Module 생성

**파일:** `data/repository/{기능명}/RepositoryModule.kt`

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

    @Binds
    @Singleton
    abstract fun bind{기능명}Repository(
        impl: {기능명}RepositoryImpl
    ): {기능명}Repository
}
```

---

### Phase 4: ViewModel 생성

**파일:** `presentation/{기능명}/{기능명}ViewModel.kt`

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
     * {기능명} 데이터 로드
     */
    fun load{기능명}Data() {
        viewModelScope.launch {
            val location = locationResolver.getEffectiveLocation() ?: return@launch

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
     * {기능명} 데이터 로드
     */
    fun load{기능명}Data() {
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

### Phase 5: Fragment 통합

**파일:** `presentation/{기능명}/{기능명}Fragment.kt`

기존 Fragment에 다음 코드 추가:

```kotlin
// 1. @AndroidEntryPoint 어노테이션 추가
@AndroidEntryPoint
class {기능명}Fragment : BaseNavigationFragment(), RefreshableFragment {

    // 2. ViewModel 주입
    private val viewModel: {기능명}ViewModel by viewModels()

    // 3. onViewCreated에 옵저버 설정
    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        setup{기능명}DataObserver()
        viewModel.load{기능명}Data()
    }

    // 4. 상태 옵저버 추가
    private fun setup{기능명}DataObserver() {
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
                            update{기능명}UI(state.data)
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

    // 5. UI 업데이트 함수 추가
    private fun update{기능명}UI(data: {기능명}Data) {
        // TODO: 데이터로 UI 업데이트
    }

    // 6. RefreshableFragment 구현
    override fun onRefresh() {
        viewModel.load{기능명}Data()
    }
}
```

---

### Phase 6: NetworkModule 업데이트

**파일:** `di/NetworkModule.kt`

API Service 제공 함수 추가:

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

1. **기능명 입력** (예: finedust)
2. **기존 Fragment 분석**
   - UI 구성 파악
   - 필요한 데이터 모델 추출
3. **Domain/Data/Presentation 레이어 생성**
4. **Fragment에 ViewModel 통합**
5. **빌드 및 테스트**

---

## 완성 후 체크리스트

- [ ] Domain Model 생성됨
- [ ] Repository Interface 생성됨
- [ ] API Service 생성됨
- [ ] Repository Implementation 생성됨 (BaseRepository 상속)
- [ ] Hilt Module 생성됨
- [ ] ViewModel 생성됨 (@HiltViewModel, BaseLoadViewModel 상속)
- [ ] Fragment에 @AndroidEntryPoint 추가됨
- [ ] Fragment에 ViewModel 주입됨
- [ ] UiState 옵저버 설정됨
- [ ] RefreshableFragment 구현됨
- [ ] NetworkModule에 API Service 추가됨
- [ ] 빌드 성공
- [ ] 앱 실행 시 크래시 없음

---

## 예시: FinedustFragment 완성

### Before (현재 상태)
```
presentation/finedust/
└── FinedustFragment.kt  (UI만 있음)
```

### After (완성)
```
domain/
├── model/finedust/
│   ├── FinedustData.kt
│   └── FinedustCurrent.kt
└── repository/FinedustRepository.kt

data/
├── api/finedust/
│   ├── FinedustApiService.kt
│   ├── FinedustRequest.kt
│   └── FinedustResponse.kt
├── model/finedust/
│   ├── FinedustDto.kt
│   └── FinedustMappers.kt
└── repository/
    ├── FinedustRepositoryImpl.kt
    └── FinedustRepositoryModule.kt

presentation/finedust/
├── FinedustFragment.kt  (ViewModel 통합)
└── FinedustViewModel.kt
```

---

## 주의사항

- **백엔드 API 엔드포인트** 확인 필수
- **기존 Fragment UI 코드** 최대한 보존
- **ViewBinding** null 처리 확인
- **Lifecycle** 인식 옵저버 사용 (repeatOnLifecycle)

---

## 예상 절감 시간

- **수작업**: 약 1~1.5시간
- **Skill 사용**: 약 10분
- **절감률**: 85%
