# PLAN_v1.0.5-auto ppt on

**CRITICAL INSTRUCTIONS**: After completing each phase:
1. ✅ Check off completed task checkboxes
2. 🧪 Run all quality gate validation commands
3. ⚠️ Verify ALL quality gate items pass
4. 📅 Update "Last Updated" date
5. 📝 Document learnings in Notes section
6. ➡️ Only then proceed to next phase

⛔ DO NOT skip quality gates or proceed with failing checks

**Last Updated**: 2026-02-11

---

# 설정 기능 & 검색 완료 시 자동 PPT ON

## Overview

사용자가 성경 구절 검색을 완료하면 자동으로 PPT 모드를 켜는 기능을 추가합니다. 이 기능은 설정 화면에서 ON/OFF 할 수 있으며, 향후 확장 가능한 설정 인프라를 구축합니다.

### 핵심 요구사항

1. **설정 버튼**: `DisplayPanel` 상단 오른쪽에 설정 아이콘 버튼 추가
2. **설정 다이얼로그**: 
   - 확장 가능한 디자인 (향후 설정 항목 추가 용이)
   - "검색 시 자동 PPT ON" 토글 스위치
   - 저장/취소 버튼 (취소 시 롤백)
3. **자동 PPT 기능**:
   - 검색 성공 시 설정이 ON이면 자동으로 `OpenPresentation` 호출
   - 두 가지 검색 방법 모두 지원:
     - 책 버튼 클릭 → 장/절 입력 → Enter
     - 책 이름 타이핑 → 장/절 입력 → Enter
   - PPT 버튼 상태 자동 동기화

---

## User Review Required

> [!IMPORTANT]
> **새로운 Settings 인프라 추가**: 이 기능을 위해 `SettingsViewModel`, `SettingsState`, `SettingsIntent`를 새로 생성합니다. 향후 다른 설정 항목(예: 테마, 폰트 기본값 등)을 쉽게 추가할 수 있도록 확장 가능한 구조로 설계합니다.

> [!NOTE]
> **설정 저장 방식**: 현재는 메모리 상태만 유지하며, 앱 재시작 시 초기화됩니다. 향후 Phase에서 `DataStore` 또는 `SharedPreferences`를 통한 영구 저장을 추가할 수 있습니다.

---

## Proposed Changes

### Settings Feature (NEW)

#### [NEW] [SettingsState.kt](file:///c:/Users/jerem/AndroidStudioProjects/LovelyBible/sample/composeApp/src/commonMain/kotlin/com/lovelybible/feature/settings/SettingsState.kt)
- `autoPptOnSearch: Boolean = false` — 검색 완료 시 자동 PPT ON 여부

#### [NEW] [SettingsIntent.kt](file:///c:/Users/jerem/AndroidStudioProjects/LovelyBible/sample/composeApp/src/commonMain/kotlin/com/lovelybible/feature/settings/SettingsIntent.kt)
- `UpdateAutoPptOnSearch(enabled: Boolean)` — 자동 PPT 설정 변경
- `SaveSettings` — 설정 저장
- `CancelSettings` — 설정 취소 (이전 상태로 롤백)

#### [NEW] [SettingsViewModel.kt](file:///c:/Users/jerem/AndroidStudioProjects/LovelyBible/sample/composeApp/src/commonMain/kotlin/com/lovelybible/feature/settings/SettingsViewModel.kt)
- MVI 패턴으로 설정 상태 관리
- `tempState` — 다이얼로그에서 수정 중인 임시 상태
- `savedState` — 저장된 설정 (취소 시 복원용)

#### [NEW] [SettingsDialog.kt](file:///c:/Users/jerem/AndroidStudioProjects/LovelyBible/sample/composeApp/src/commonMain/kotlin/com/lovelybible/feature/settings/SettingsDialog.kt)
- Material3 Dialog 사용
- 확장 가능한 레이아웃 (Column with Dividers)
- 자동 PPT 토글 스위치
- 저장/취소 버튼

---

### Display Panel

#### [MODIFY] [DisplayPanel.kt](file:///c:/Users/jerem/AndroidStudioProjects/LovelyBible/sample/composeApp/src/commonMain/kotlin/com/lovelybible/feature/navigation/DisplayPanel.kt)
- Preview Area 상단 오른쪽에 Settings 아이콘 버튼 추가 (사용자 이미지 참고)
- `SettingsDialog` 표시 상태 관리
- `SettingsViewModel` 주입

---

### Search Integration

#### [MODIFY] [SearchViewModel.kt](file:///c:/Users/jerem/AndroidStudioProjects/LovelyBible/sample/composeApp/src/commonMain/kotlin/com/lovelybible/feature/search/SearchViewModel.kt)
- `SettingsViewModel` 주입
- `PresentationViewModel` 주입
- `executeSearch()` 성공 경로에서:
  - `if (settingsViewModel.state.autoPptOnSearch && !presentationViewModel.state.isPresentationWindowOpen)` 체크
  - `presentationViewModel.onIntent(PresentationIntent.OpenPresentation)` 호출

---

### Version Management

#### [MODIFY] [AppVersion.kt](file:///c:/Users/jerem/AndroidStudioProjects/LovelyBible/sample/composeApp/src/commonMain/kotlin/com/lovelybible/core/AppVersion.kt)
- `APP_VERSION = "1.0.5"`

#### [MODIFY] [build.gradle.kts](file:///c:/Users/jerem/AndroidStudioProjects/LovelyBible/sample/composeApp/build.gradle.kts)
- `packageVersion = "1.0.5"`

---

## Risk Assessment

| 리스크 | 확률 | 영향 | 완화 전략 |
|--------|------|------|-----------|
| 설정 저장 없이 앱 재시작 시 초기화 | High | Low | 현재 Phase에서는 메모리 상태만 유지. 향후 DataStore 추가 예정 |
| SearchViewModel에 너무 많은 의존성 주입 | Medium | Medium | DI 컨테이너(Koin)로 관리하여 테스트 가능성 유지 |
| 자동 PPT가 사용자 의도와 다를 수 있음 | Low | Low | 설정에서 쉽게 OFF 가능 |
| Dialog UI가 향후 설정 항목 추가 시 복잡해질 수 있음 | Medium | Low | LazyColumn 기반 확장 가능한 구조 사용 |

---

## Rollback Strategy

- **Phase 1 롤백**: `SettingsState.kt`, `SettingsIntent.kt`, `SettingsViewModel.kt` 삭제
- **Phase 2 롤백**: `SettingsDialog.kt` 삭제, `DisplayPanel.kt` 설정 버튼 제거
- **Phase 3 롤백**: `SearchViewModel.kt`의 auto-PPT 로직 제거
- **Phase 4 롤백**: 버전 번호 복원

---

## Phase 1: Settings 상태 관리 인프라
**Goal**: 설정 상태를 관리하는 ViewModel과 State를 TDD로 구현합니다.
**소요 시간**: 1~2시간

### Test Strategy
- **Test Type**: Unit Test (ViewModel 로직)
- **Coverage Target**: SettingsViewModel 100%
- **Dependencies to Mock**: 없음 (순수 상태 관리)

### Tasks

#### 🔴 RED: 테스트 작성

- [ ] **새 테스트 파일**: `SettingsViewModelTest.kt`
- [ ] **테스트 케이스**:
  ```kotlin
  // 1. 초기 상태 확인
  @Test
  fun testInitialState_autoPptIsOff()
  
  // 2. 자동 PPT 설정 변경
  @Test
  fun testUpdateAutoPptOnSearch_updatesState()
  
  // 3. 설정 저장
  @Test
  fun testSaveSettings_commitsTempState()
  
  // 4. 설정 취소
  @Test
  fun testCancelSettings_revertsToSavedState()
  
  // 5. 저장 → 수정 → 취소 사이클
  @Test
  fun testSaveModifyCancel_cycle()
  ```

#### 🟢 GREEN: 구현

- [ ] **[NEW] `SettingsState.kt`** 생성:
  ```kotlin
  data class SettingsState(
      val autoPptOnSearch: Boolean = false
  )
  ```
- [ ] **[NEW] `SettingsIntent.kt`** 생성:
  ```kotlin
  sealed class SettingsIntent {
      data class UpdateAutoPptOnSearch(val enabled: Boolean) : SettingsIntent()
      object SaveSettings : SettingsIntent()
      object CancelSettings : SettingsIntent()
  }
  ```
- [ ] **[NEW] `SettingsViewModel.kt`** 생성:
  - `tempState` (현재 편집 중)
  - `savedState` (저장된 상태)
  - `onIntent()` 구현

#### 🔵 REFACTOR

- [ ] 코드 정리 및 주석 추가
- [ ] Koin DI 모듈에 `SettingsViewModel` 등록

### Quality Gate
- [ ] 모든 테스트 통과 (5개)
- [ ] 빌드 에러 없음
- [ ] SettingsViewModel이 Koin에 등록됨

---

## Phase 2: 설정 UI (Dialog) 구현
**Goal**: 설정 다이얼로그와 설정 버튼을 추가합니다.
**소요 시간**: 2~3시간
**Dependencies**: Phase 1 완료

### Test Strategy
- **Test Type**: Unit Test (ViewModel 상태 변화) + Manual UI Test
- **Coverage Target**: Dialog 상태 관리 로직 100%

### Tasks

#### 🔴 RED: 테스트 작성

- [ ] **테스트 파일**: `SettingsViewModelTest.kt` (추가)
- [ ] **새 테스트 케이스**:
  ```kotlin
  // 1. Dialog 열기/닫기 상태 관리
  @Test
  fun testDialogOpenClose_stateManagement()
  
  // 2. 토글 변경 후 저장
  @Test
  fun testToggleAndSave_persistsChange()
  
  // 3. 토글 변경 후 취소
  @Test
  fun testToggleAndCancel_revertsChange()
  ```

#### 🟢 GREEN: 구현

- [ ] **[NEW] `SettingsDialog.kt`** 생성:
  - Material3 `AlertDialog` 사용
  - `Column` 레이아웃 (확장 가능)
  - "검색 시 자동 PPT ON" 라벨 + `Switch`
  - 하단 `Row`: 취소/저장 버튼
- [ ] **`DisplayPanel.kt` 수정**:
  - Preview Area 상단 오른쪽에 Settings `IconButton` 추가
  - `Icons.Default.Settings` 사용
  - `showSettingsDialog` 상태 관리
  - `SettingsDialog` 조건부 표시

#### 🔵 REFACTOR

- [ ] Dialog 디자인 폴리싱 (색상, 간격, 그림자)
- [ ] 설정 버튼 위치 미세 조정 (사용자 이미지 참고)

### Quality Gate
- [ ] 모든 테스트 통과 (기존 + 3개 추가)
- [ ] 빌드 에러 없음
- [ ] 설정 버튼 클릭 → Dialog 표시 (수동 확인)
- [ ] 토글 변경 → 저장 → 상태 유지 (수동 확인)
- [ ] 토글 변경 → 취소 → 상태 복원 (수동 확인)

---

## Phase 3: 자동 PPT 기능 통합
**Goal**: 검색 성공 시 설정에 따라 자동으로 PPT를 켭니다.
**소요 시간**: 1~2시간
**Dependencies**: Phase 1, 2 완료

### Test Strategy
- **Test Type**: Unit Test (SearchViewModel 로직)
- **Coverage Target**: Auto-PPT 로직 100%
- **Dependencies to Mock**: `SettingsViewModel`, `PresentationViewModel`, `BibleRepository`

### Tasks

#### 🔴 RED: 테스트 작성

- [ ] **테스트 파일**: `SearchViewModelTest.kt` (수정)
- [ ] **새 테스트 케이스**:
  ```kotlin
  // 1. 자동 PPT ON + 검색 성공 → PPT 열림
  @Test
  fun testExecuteSearch_whenAutoPptOn_opensPPT()
  
  // 2. 자동 PPT OFF + 검색 성공 → PPT 안 열림
  @Test
  fun testExecuteSearch_whenAutoPptOff_doesNotOpenPPT()
  
  // 3. 자동 PPT ON + PPT 이미 열림 → 중복 호출 안 함
  @Test
  fun testExecuteSearch_whenPPTAlreadyOpen_doesNotCallOpenAgain()
  
  // 4. 검색 실패 → PPT 안 열림
  @Test
  fun testExecuteSearch_whenSearchFails_doesNotOpenPPT()
  ```

#### 🟢 GREEN: 구현

- [ ] **`SearchViewModel.kt` 수정**:
  - 생성자에 `SettingsViewModel`, `PresentationViewModel` 주입
  - `executeSearch()` 성공 경로 (line 132 이후):
    ```kotlin
    // 검색 성공 후
    if (settingsViewModel.state.autoPptOnSearch && 
        !presentationViewModel.state.isPresentationWindowOpen) {
        presentationViewModel.onIntent(PresentationIntent.OpenPresentation)
    }
    ```
- [ ] **Koin DI 모듈 수정**:
  - `SearchViewModel`에 `SettingsViewModel`, `PresentationViewModel` 주입

#### 🔵 REFACTOR

- [ ] 자동 PPT 로직을 별도 함수로 추출 (가독성)
- [ ] 주석 추가

### Quality Gate
- [ ] 모든 테스트 통과 (기존 + 4개 추가)
- [ ] 빌드 에러 없음
- [ ] 설정 ON + 검색 → PPT 자동 열림 (수동 확인)
- [ ] 설정 OFF + 검색 → PPT 안 열림 (수동 확인)
- [ ] PPT 버튼 상태 동기화 확인 (수동 확인)

---

## Phase 4: 버전 업데이트 & 최종 검증
**Goal**: 버전을 v1.0.5으로 업데이트하고 전체 기능을 검증합니다.
**소요 시간**: 30분
**Dependencies**: Phase 1-3 완료

### Test Strategy
- **Test Type**: Unit Test (버전 상수) + Manual E2E Test
- **Coverage Target**: 100%

### Tasks

#### 🔴 RED: 테스트 작성

- [ ] **테스트 파일**: `AppVersionTest.kt` (수정)
  ```kotlin
  @Test
  fun testAppVersion_is1_0_6() {
      assertEquals("1.0.5", APP_VERSION)
  }
  ```

#### 🟢 GREEN: 구현

- [ ] **`AppVersion.kt` 수정**:
  ```diff
  - const val APP_VERSION = "1.0.5"
  + const val APP_VERSION = "1.0.5"
  ```
- [ ] **`build.gradle.kts` 수정**:
  ```diff
  - packageVersion = "1.0.5"
  + packageVersion = "1.0.5"
  ```

#### 🔵 REFACTOR

- [ ] 없음

### Quality Gate
- [ ] `AppVersionTest` 통과
- [ ] 빌드 에러 없음
- [ ] `build.gradle.kts` 버전 `1.0.5` 확인

---

## Verification Plan

### Automated Tests
```bash
# 프로젝트 루트에서 실행
cd c:\Users\jerem\AndroidStudioProjects\LovelyBible
gradlew.bat :sample:composeApp:jvmTest --info
```

### Manual Verification (사용자 실행)

#### 설정 기능
1. **설정 버튼 위치**: DisplayPanel 상단 오른쪽에 설정 아이콘 확인
2. **설정 Dialog 열기**: 설정 버튼 클릭 → Dialog 표시
3. **토글 변경 + 저장**: 
   - 자동 PPT 토글 ON → 저장 → Dialog 닫기
   - 다시 설정 열기 → 토글 ON 유지 확인
4. **토글 변경 + 취소**:
   - 자동 PPT 토글 OFF → 취소 → Dialog 닫기
   - 다시 설정 열기 → 이전 상태(ON) 유지 확인

#### 자동 PPT 기능
5. **설정 ON + 책 버튼 클릭 검색**:
   - 설정에서 자동 PPT ON
   - 책 버튼 클릭 → 장/절 입력 → Enter
   - PPT 자동 열림 확인
   - PPT 버튼 "PPT 종료" 표시 확인
6. **설정 ON + 타이핑 검색**:
   - 책 이름 타이핑 → 장/절 입력 → Enter
   - PPT 자동 열림 확인
7. **설정 OFF + 검색**:
   - 설정에서 자동 PPT OFF
   - 검색 실행
   - PPT 안 열림 확인
8. **PPT 이미 열린 상태 + 검색**:
   - PPT 수동으로 열기
   - 설정 ON 상태에서 검색
   - PPT 중복 호출 없이 정상 동작 확인

---

## Notes & Learnings

_(Phase 완료 후 기록)_
