# WeatherApp 프로젝트 가이드

## 프로젝트 개요
안드로이드 날씨 앱 - Clean Architecture + MVVM 패턴 기반
- 백엔드 API 연동 (자체 서버 + 카카오 로컬 API)
- Hilt 의존성 주입
- Kotlin Coroutines + StateFlow 기반 반응형 프로그래밍

## 핵심 기술 스택

### 언어 및 버전
- **Kotlin**: 1.9.x (KSP 지원)
- **Android API**: min 30, target 34, compile 34
- **Java**: 17

### 주요 라이브러리
- **네트워크**: Retrofit 2, OkHttp3, Gson
- **의존성 주입**: Dagger Hilt 2.48.1
- **UI**: AndroidX Navigation, Material Design, ConstraintLayout, RecyclerView
- **비동기**: Kotlin Coroutines, StateFlow, Lifecycle 2.7.0
- **위치**: Google Play Services Location 21.1.0
- **바인딩**: ViewBinding, DataBinding

## 아키텍처 패턴

### Clean Architecture 3계층 구조

```
Presentation Layer (MVVM)
  ├── Fragment (@AndroidEntryPoint)
  ├── ViewModel (@HiltViewModel + StateFlow<UiState<T>>)
  └── Adapter (RecyclerView)
       ↓
Domain Layer
  ├── Repository Interface
  └── Domain Models (순수 비즈니스 로직)
       ↓
Data Layer
  ├── Repository Implementation (BaseRepository 상속)
  ├── API Service (Retrofit)
  └── DTO Models (Data Transfer Objects)
```

### 핵심 Base 클래스
- **BaseRepository**: 공통 API 호출, 에러 처리, 로깅
- **BaseLoadViewModel<T>**: 제네릭 상태 관리 (Initial, Loading, Success, Error)
- **UiState<T>**: Sealed class 타입 안전 상태 관리
- **BaseNavigationFragment**: 공통 Fragment 로직

## 패키지 구조

```
com.example.kh_studyprojects_weatherapp/
├── di/                          # Hilt 모듈 (Network, Repository, Location)
├── data/
│   ├── api/                     # Retrofit API 서비스
│   │   ├── weather/             # 날씨 API
│   │   ├── kakao/               # 카카오 로컬 API
│   │   ├── sidemenu/            # 즐겨찾기 API
│   │   └── common/              # 공통 API 응답
│   ├── model/                   # DTO 클래스
│   └── repository/              # Repository 구현체
│       └── base/                # BaseRepository, NetworkException
├── domain/
│   ├── model/                   # 도메인 모델 (비즈니스 로직용)
│   │   ├── weather/
│   │   └── location/
│   └── repository/              # Repository 인터페이스
├── presentation/
│   ├── common/
│   │   ├── base/                # Base Fragment, ViewModel
│   │   ├── location/            # LocationManager, 위치 선택 로직
│   │   └── sidemenu/            # SmManager, 즐겨찾기 관리
│   ├── weather/                 # 날씨 화면 (현재/시간별/일별/추가정보)
│   ├── finedust/                # 미세먼지 화면
│   └── setting/                 # 설정 화면
├── util/                        # 유틸리티 (Logger, DeviceId)
├── MainActivity.kt
└── WeatherApplication.kt        # @HiltAndroidApp
```

## 네이밍 규칙

### Kotlin 파일 및 클래스
- **파일명**: PascalCase (예: `LocationManager.kt`)
- **클래스명**: PascalCase (예: `FavoriteLocation`, `WeatherViewModel`)
- **인터페이스**: PascalCase (예: `WeatherRepository`)
- **구현체**: `*Impl` 접미사 (예: `WeatherRepositoryImpl`)

### 변수 및 함수
- **변수/함수명**: camelCase (예: `getCurrentLocation`, `weatherData`)
- **상수**: UPPER_SNAKE_CASE (예: `CACHE_DURATION_MS`, `KAKAO_BASE_URL`)
- **private 변수**: camelCase (접두사 없음)

### API 및 데이터 클래스
- **API Service**: `*ApiService` (예: `WeatherApiService`, `KakaoApiService`)
- **DTO**: `*Dto` 접미사 (예: `WeatherDailyDto`, `WeatherHourlyForecastDto`)
- **API 요청**: `*Request` (예: `WeatherRequest`, `SmFavoriteLocationRequest`)
- **API 응답**: `*Response` (예: `WeatherResponse`, `KakaoAddressResponse`)
- **Domain Model**: 접미사 없음 (예: `WeatherData`, `FavoriteLocation`)

### UI 컴포넌트
- **ViewModel**: `*ViewModel` (예: `WeatherCurrentViewModel`)
- **Fragment**: `*Fragment` (예: `WeatherFragment`, `FinedustFragment`)
- **Adapter**: `*Adapter` (예: `SmFavoriteLocationAdapter`)
- **ViewHolder**: `*ViewHolder` (예: `WeatherDailyViewHolder`)

### 리소스 파일
- **레이아웃**: `fragment_*.xml`, `activity_*.xml`, `item_*.xml`
- **드로어블 Shape**: `sh_[카테고리]_[목적]_[버전]` (예: `sh_nav_round_t_lr_01`)
- **드로어블 Icon**: `ic_[카테고리]_[목적]` (예: `ic_com_weatherimg_sunny`)

## 코딩 스타일

### 들여쓰기 및 포맷
- **들여쓰기**: 공백 4칸
- **줄 길이**: 최대 120자 권장
- **Import**: Android → androidx → com.google → kotlin → java 순서

### 주석 규칙
```kotlin
/**
 * 클래스/함수 설명
 *
 * @param paramName 파라미터 설명
 * @return 반환값 설명
 * @author 작성자
 * @since yyyy-mm-dd
 * @version 1.0
 *
 * <개정이력>
 * 수정일       수정자     수정내용
 * ----------  --------  ---------------------------
 * yyyy-mm-dd  홍길동    최초 생성
 */
```

### Null Safety
- **Nullable 타입**: 명시적으로 `?` 사용
- **Non-null 단언**: `!!` 사용 최소화, 안전 호출 `?.` 선호
- **Elvis 연산자**: `?:` 활용하여 기본값 제공

### 비동기 처리
- **Coroutines**: `viewModelScope.launch { }` 사용
- **StateFlow**: 읽기 전용 상태는 `StateFlow`, 쓰기는 `MutableStateFlow`
- **에러 처리**: try-catch + Result<T> 타입 조합

## API 연동 규칙

### 백엔드 API
- **Base URL**:
  - Debug: `local.properties`에서 `BACKEND_BASE_URL` 설정 (개발자마다 다름)
    - 실제 기기: `http://[개발자PC IP]:8080`
    - 에뮬레이터: `http://10.0.2.2:8080` (호스트 머신 localhost)
  - Release: `https://api.example.com` (프로덕션 서버)
- **타임아웃**: 연결/읽기/쓰기 각 30초
- **로깅**: Debug 빌드만 활성화
- **설정 방법**: `local.properties`에 `BACKEND_BASE_URL=http://YOUR_IP:8080` 추가

### 카카오 로컬 API
- **Base URL**: `https://dapi.kakao.com/`
- **인증**: `Authorization: KakaoAK {KAKAO_API_KEY}` (local.properties에서 로드)
- **주요 기능**: 위도/경도 → 주소 변환, 주소 검색

### API 호출 패턴
```kotlin
// Repository Implementation
override suspend fun getWeatherInfo(latitude: Double, longitude: Double): Result<WeatherData> =
    safeApiCallWithTransform(
        call = {
            weatherApiService.getWeatherInfo(
                WeatherRequest(latitude, longitude, WeatherApiConstants.FULL_WEATHER_QUERY)
            )
        },
        transform = { response -> WeatherMappers.toWeatherData(response) }
    )
```

### 공통 API 응답 구조
```kotlin
data class ApiResponse<T>(
    val code: String,
    val message: String,
    val data: T?
)
```

## 의존성 주입 (Hilt)

### 모듈 구조
1. **NetworkModule**: Retrofit, OkHttpClient, API Service 제공
2. **RepositoryModule**: Repository 인터페이스 → 구현체 바인딩
3. **LocationModule**: LocationSelectionStore 바인딩
4. **GeocodingRepositoryModule**: GeocodingRepository 바인딩
5. **SmRepositoryModule**: SmFavoriteLocationRepository 바인딩

### Qualifier 사용
```kotlin
@Qualifier
@Retention(AnnotationRetention.BINARY)
annotation class BackendRetrofit

@Qualifier
@Retention(AnnotationRetention.BINARY)
annotation class KakaoRetrofit
```

### ViewModel 주입
```kotlin
@HiltViewModel
class WeatherViewModel @Inject constructor(
    private val weatherRepository: WeatherRepository,
    private val locationResolver: EffectiveLocationResolver
) : BaseLoadViewModel<WeatherData>()
```

### Fragment 주입
```kotlin
@AndroidEntryPoint
class WeatherFragment : BaseNavigationFragment() {
    @Inject lateinit var smManager: SmManager
    // ...
}
```

## 상태 관리 패턴

### UiState 사용
```kotlin
sealed class UiState<out T> {
    object Initial : UiState<Nothing>()
    object Loading : UiState<Nothing>()
    data class Success<T>(val data: T) : UiState<T>()
    data class Error(val message: String, val throwable: Throwable) : UiState<Nothing>()
}
```

### ViewModel에서 StateFlow 관리
```kotlin
private val _weatherState = MutableStateFlow<UiState<WeatherData>>(UiState.Initial)
val weatherState: StateFlow<UiState<WeatherData>> = _weatherState.asStateFlow()

fun refreshWeatherData() {
    loadData { weatherRepository.getWeatherInfo(latitude, longitude) }
}
```

### Fragment에서 상태 구독
```kotlin
viewLifecycleOwner.lifecycleScope.launch {
    viewLifecycleOwner.repeatOnLifecycle(Lifecycle.State.STARTED) {
        viewModel.weatherState.collect { state ->
            when (state) {
                is UiState.Loading -> showLoading()
                is UiState.Success -> updateUI(state.data)
                is UiState.Error -> showError(state.message)
                is UiState.Initial -> {}
            }
        }
    }
}
```

## 에러 처리

### NetworkException 계층
```kotlin
sealed class NetworkException : Exception() {
    data class HttpException(val code: Int, override val message: String) : NetworkException()
    data class ParseException(override val message: String) : NetworkException()
    data class TimeoutException(override val message: String) : NetworkException()
    data class UnknownException(override val message: String, val cause: Throwable?) : NetworkException()
}
```

### BaseRepository 에러 처리
- HTTP 에러 → `NetworkException.HttpException`
- JSON 파싱 에러 → `NetworkException.ParseException`
- 타임아웃 → `NetworkException.TimeoutException`
- 모든 예외 → 로그 기록 (DebugLogger 사용)

## 캐싱 전략

### WeatherRepository 캐싱
```kotlin
private var cachedWeatherData: WeatherData? = null
private var cachedLocation: Pair<Double, Double>? = null
private var cacheTimestamp: Long = 0L
private const val CACHE_DURATION_MS = 30_000L  // 30초

// 동일 위치 + 30초 이내 → 캐시 반환
// 그 외 → API 재요청
```

### 캐싱 사용 시 주의사항
- 위치 변경 시 캐시 무효화
- 사용자가 명시적으로 새로고침 시 캐시 무시
- 캐시 만료 시간은 기능별로 다르게 설정 가능

## 위치 기능

### LocationManager
- **FusedLocationProviderClient** 사용 (Google Play Services)
- **권한**: ACCESS_FINE_LOCATION, ACCESS_COARSE_LOCATION
- **기능**: GPS 좌표 획득 + 카카오 API로 주소 변환
- **에러 처리**: 위치 권한 없음, GPS 비활성화, 네트워크 에러 등

### EffectiveLocationResolver
- 사용자가 선택한 위치 우선
- 선택 위치 없으면 현재 GPS 위치 사용
- LocationSelectionStore에서 상태 관리

### 즐겨찾기 위치
- **SmManager**: 사이드메뉴 전체 로직 관리
- **기능**: 위치 검색, 즐겨찾기 추가/삭제/정렬
- **저장**: 백엔드 API + DeviceIdUtil로 디바이스별 관리

## 뷰 바인딩

### ViewBinding 사용
```kotlin
private var _binding: FragmentWeatherBinding? = null
private val binding get() = _binding!!

override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View {
    _binding = FragmentWeatherBinding.inflate(inflater, container, false)
    return binding.root
}

override fun onDestroyView() {
    super.onDestroyView()
    _binding = null
}
```

### DataBinding (선택적 사용)
- XML에서 `<layout>` 태그로 감싸기
- `@{viewModel.data}` 형태로 데이터 바인딩
- 양방향 바인딩: `@={viewModel.text}`

## 테스트

### 단위 테스트
- **프레임워크**: JUnit4
- **대상**: Repository, ViewModel, Util 클래스
- **위치**: `app/src/test/`

### UI 테스트
- **프레임워크**: Espresso, AndroidX Test JUnit
- **대상**: Fragment, Activity 상호작용
- **위치**: `app/src/androidTest/`

## Git 커밋 메시지

### 커밋 메시지 형식
```
feat: 기능 추가 설명
fix: 버그 수정 설명
refactor: 리팩토링 설명
style: 코드 포맷팅, 세미콜론 누락 등
docs: 문서 수정
test: 테스트 코드 추가/수정
chore: 빌드 설정, 라이브러리 업데이트 등
```

### 예시
```
feat: 시간별 날씨 예보 RecyclerView 어댑터 구현
fix: 위치 권한 거부 시 앱 크래시 문제 해결
refactor: WeatherRepository 캐싱 로직 개선
```

## 성능 최적화

### RecyclerView
- **ViewHolder 패턴** 필수
- **DiffUtil** 사용으로 효율적인 업데이트
- **이미지 로딩**: Glide 또는 Coil (필요 시)

### 네트워크
- **캐싱**: OkHttp Interceptor 활용 가능
- **타임아웃**: 적절한 타임아웃 설정 (30초)
- **재시도 로직**: 필요 시 Retrofit Call Adapter 활용

### 메모리 관리
- **ViewBinding**: `onDestroyView()`에서 반드시 `_binding = null`
- **Coroutine**: ViewModel 제거 시 자동 취소 (`viewModelScope`)
- **LiveData/StateFlow**: Lifecycle 인식 자동 구독 해제

## 보안

### API 키 관리
- **절대 하드코딩 금지**
- `local.properties`에 저장: `KAKAO_API_KEY=your_key_here`
- `build.gradle.kts`에서 BuildConfig로 로드
- `.gitignore`에 `local.properties` 추가 필수

### 난독화
- **ProGuard/R8**: Release 빌드 시 활성화
- 중요 클래스는 `proguard-rules.pro`에서 keep 설정

## 접근성

### UI 접근성 지침
- **contentDescription**: 모든 이미지뷰에 설명 추가
- **터치 영역**: 최소 48dp x 48dp 유지
- **색상 대비**: WCAG 2.1 AA 기준 준수 (최소 4.5:1)
- **텍스트 크기**: sp 단위 사용, 사용자 설정 반영

## 빌드 설정

### 빌드 타입
- **debug**: HTTP 로깅 활성화, 에뮬레이터 URL 자동 감지
- **release**: 로깅 비활성화, 난독화 활성화, 프로덕션 URL

### Gradle 빌드 스크립트
- **Kotlin DSL** 사용 (`.gradle.kts`)
- **버전 카탈로그**: `libs.versions.toml`에서 라이브러리 버전 관리 권장
- **KSP**: Hilt, Room 등에 kapt 대신 KSP 사용

## 주요 주의사항

### Fragment Lifecycle
- `onCreateView`: ViewBinding 초기화
- `onViewCreated`: 뷰 초기화 및 리스너 설정
- `onDestroyView`: ViewBinding null 처리 (메모리 누수 방지)

### ViewModel Scope
- `viewModelScope.launch`: ViewModel 제거 시 자동 취소
- UI 업데이트는 Main Dispatcher (기본값)
- 네트워크/DB 작업은 IO Dispatcher (Retrofit은 자동 처리)

### Hilt 사용 시
- Fragment는 반드시 `@AndroidEntryPoint` 어노테이션
- ViewModel은 `@HiltViewModel` + 생성자 `@Inject constructor`
- Application 클래스는 `@HiltAndroidApp`

### API 응답 처리
- null 체크 철저히 (서버 응답이 null일 수 있음)
- `ApiResponse.data`는 Nullable 타입
- 파싱 실패 시 ParseException 발생 가능

## 개발 팁

### 디버깅
- **DebugLogger**: 프로젝트 전용 로거 사용 (Release 빌드에서 자동 비활성화)
- **Logcat 필터**: Tag는 클래스명 사용 (예: `TAG = "WeatherViewModel"`)

### 에뮬레이터 vs 실제 기기
- 에뮬레이터: `10.0.2.2` → 호스트 머신 localhost
- 실제 기기: `192.168.x.x` → 동일 네트워크의 개발 서버

### 코드 리뷰 체크리스트
- [ ] Hilt 주입이 올바르게 설정되었는가?
- [ ] ViewBinding null 처리가 되었는가?
- [ ] API 에러 처리가 구현되었는가?
- [ ] 주석이 KDoc 형식으로 작성되었는가?
- [ ] 네이밍 규칙을 준수했는가?
- [ ] 불필요한 로그나 주석이 제거되었는가?
- [ ] API 키가 하드코딩되지 않았는가?