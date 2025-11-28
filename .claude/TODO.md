# WeatherApp 구현 예정 기능

## 우선순위 High (긴급)

### 미완성 기능 완성
- [ ] **Finedust (미세먼지) 기능 완성**
  - [ ] Domain Layer: FinedustData 모델, FinedustRepository 인터페이스
  - [ ] Data Layer: FinedustApiService, DTO, Mapper, RepositoryImpl
  - [ ] Presentation Layer: FinedustViewModel (BaseLoadViewModel 상속)
  - [ ] UI: StateFlow 기반 상태 관리 구현
  - [ ] Hilt 의존성 주입 설정

- [ ] **Setting (설정) 기능 완성**
  - [ ] Domain Layer: SettingData 모델, SettingRepository 인터페이스
  - [ ] Data Layer: SettingApiService, DTO, Mapper, RepositoryImpl
  - [ ] Presentation Layer: SettingViewModel
  - [ ] UI: 설정 항목 RecyclerView 구현
  - [ ] Hilt 의존성 주입 설정

## 설정 개발해야 할 기능
-  바텀 네비게이션 설정 클릭시 설정 페이지 이동
-  설정 메인 페이지
    - 현재 위치 활요 (스위치버튼 클릭시 on/off , 텍스트 클릭 시 화면이동)
      - 이동화면 스위치 버튼과 간단한 텍스트 설명
    - 자동 새로고침 (클릭 시 작은 팝업 [안함, 1시간 간격, 3시간 간격, 6시간 간격, 12시간 간격 ,24시간 간격])
    - 이동 시 자동 새로고침(스위치버튼 클릭시 on/off , 텍스트 클릭 시 화면이동)
      -  이동화면 스위치버튼과 간단한 설명있음    
    - 날씨 알림(클릭시 화면 이동)
       - 상단 알림 받기 스위치버튼 (비활성화 시 아래 설정값 다안보임 알림 받기 버튼만노출)
       - 시간 오전 00 : 00 > (시간 클릭시 드래그 팝업)
       - 조건 (눈,비) 1시간전 (클릭 시 드래그 팝업 (현재 시간, 1시간전,2시간전 ... 4시간전 취소 선택 버튼 ))
       - 무음 모드 스위치 버튼
    - 미세먼지 알림(클릭시 화면이동)
       - 상단 알림 받기 스위치버튼 ON/off 비활성화 시 아래 설정값 다안보임 알림 받기 버튼만노출)
       - 시간 오전 00 : 00 >
       - 알림받을 상태(기본텍스트)
         - [나쁨] 이거나 더 나쁠 때 알림받음 (나쁨,좋음 클릭 시 드래그팝업으로 상태값(최고,좋음,양호,보통,나쁨 등) 선택 아래 취소 선택 버튼)
         - [좋음] 이거나 더 좋을 때 알림 받음 
       - 무음 모드 스위치버튼
    - 기온별 옷차림(클릭 시 화면이동)
      - 고민중.......
## 우선순위 Medium (중요)

### 버그 수정
- [ ]

### 성능 개선
- [ ] RecyclerView DiffUtil 적용 검토
- [ ] 네트워크 캐싱 전략 최적화
- [ ] 이미지 로딩 라이브러리 도입 검토 (Glide/Coil)

### 코드 품질 개선
- [ ] 테스트 코드 작성 (Repository, ViewModel)
- [ ] ProGuard/R8 난독화 규칙 정리
- [ ] 코드 커버리지 측정

## 우선순위 Low (개선 사항)

### 새 기능 추가
- [ ]

### UX 개선
- [ ] 로딩 애니메이션 추가
- [ ] 에러 메시지 사용자 친화적으로 개선
- [ ] 다크 모드 지원

### 접근성 개선
- [ ] contentDescription 누락 항목 추가
- [ ] 터치 영역 최소 크기 검증 (48dp)
- [ ] 색상 대비 WCAG 기준 검토

## 기술 부채

- [ ] Kotlin 버전 업그레이드 후 Deprecated API 정리
- [ ] 사용하지 않는 리소스 파일 제거
- [ ] 하드코딩된 문자열 strings.xml로 이동

## 완료된 작업

- [x] Kotlin 2.1.20 업그레이드
- [x] Hilt 2.56 업그레이드
- [x] KSP 마이그레이션 (kapt → KSP)
- [x] Weather 기능 구현 (현재/시간별/일별/추가정보)
- [x] 위치 기능 구현 (GPS + 카카오 API)
- [x] 즐겨찾기 기능 구현 (SmManager)

---

## 메모

### 참고사항
- 새 기능 추가 시 `.claude/skills/` 활용
- API 키는 반드시 `local.properties`에서 관리
- 모든 Fragment는 `@AndroidEntryPoint` 필수

### 개발 환경
- 백엔드 Base URL: `local.properties`에서 `BACKEND_BASE_URL` 설정
- 에뮬레이터: `http://10.0.2.2:8080`
- 실제 기기: `http://[개발자PC IP]:8080`
