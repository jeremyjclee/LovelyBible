# PLAN_v1.0.5-ppt off

**CRITICAL INSTRUCTIONS**: After completing each phase:
1. ✅ Check off completed task checkboxes
2. 🧪 Run all quality gate validation commands
3. ⚠️ Verify ALL quality gate items pass
4. 📅 Update "Last Updated" date
5. 📝 Document learnings in Notes section
6. ➡️ Only then proceed to next phase

⛔ DO NOT skip quality gates or proceed with failing checks

**Last Updated**: 2026-02-11 14:00

---

# PPT 모드 안정화 & Spacebar 단축키 수정 & 버전관리

## Overview

PPT 모드와 관련된 3가지 핵심 버그를 수정하고 버전관리를 추가합니다.

### 문제 분석 (Root Cause)

#### 1. Spacebar 충돌
- **`MainScreen.kt` (line 74)**: Spacebar → `presentationViewModel.togglePresentation()` (PPT 토글)
- **`PresentationWindow.kt` (line 95)**: Spacebar → `NavigateNext` (다음 페이지)
- **문제**: PPT 모드 ON 상태에서 Spacebar를 누르면 `MainScreen`에서는 PPT를 종료하려 하고, `PresentationWindow`에서는 다음 페이지로 이동하려 해서 충돌 발생

#### 2. 앱 클릭 시 PPT 해제
- **`main.kt` (line 59-64)**: `PresentationWindow`의 `onCloseRequest`가 `ClosePresentation`을 호출
- **문제**: Compose Desktop의 `Window`는 사용자가 메인 앱 창을 클릭하면 PresentationWindow가 focus를 잃고, OS 레벨에서 `onCloseRequest`가 트리거될 수 있음
- **추가 문제**: `undecorated = true`와 `alwaysOnTop = true` 설정이 있지만, 메인 창 클릭 시 focus 변경으로 인해 Window가 닫히는 현상

#### 3. PPT ON/OFF 버튼 동기화 실패
- **`DisplayPanel.kt` (line 172, 184)**: `presentationViewModel.state.isPresentationWindowOpen`을 읽어 버튼 색상/텍스트 결정
- **문제**: 위 2번으로 PPT가 외부에서 닫혀도 `closePresentation()`이 호출되지 않으면 `isPresentationWindowOpen`이 `true`로 남아 버튼이 "PPT 종료"로 표시됨 → 사용자가 버튼을 2번 눌러야 다시 켜짐

#### 4. 버전관리 미비
- `build.gradle.kts`의 `packageVersion = "1.0.1"`만 존재
- 앱 코드 내 버전 상수 없음

---

## User Review Required

> [!IMPORTANT]
> **Spacebar 동작 변경**: 현재 PresentationWindow에서 Spacebar가 `NavigateNext`로 매핑되어 있습니다. 이를 제거하고 Spacebar는 **오직 PPT 토글(ON/OFF)**로만 동작하도록 변경합니다. PPT 모드 내 다음 페이지 이동은 방향키(→, ↓)로만 가능합니다.

> [!WARNING]
> **Window 닫힘 방지**: `PresentationWindow`의 `onCloseRequest`를 빈 함수(`{}`)로 변경하여 OS 레벨 닫힘을 완전히 차단합니다. PPT 종료는 오직 앱 내 버튼, Esc, Spacebar로만 가능합니다.

---

## Proposed Changes

### Presentation Feature

#### [MODIFY] [PresentationWindow.kt](file:///c:/Users/jerem/AndroidStudioProjects/LovelyBible/sample/composeApp/src/jvmMain/kotlin/com/lovelybible/feature/presentation/PresentationWindow.kt)
- `handleKeyEvent`에서 Spacebar를 `NavigateNext`에서 제거
- Spacebar를 PPT 종료(`onClose`) 동작으로 변경
- `Window`의 `onCloseRequest`를 빈 람다로 변경하여 외부 닫힘 차단

#### [MODIFY] [main.kt](file:///c:/Users/jerem/AndroidStudioProjects/LovelyBible/sample/composeApp/src/jvmMain/kotlin/sample/app/main.kt)
- `PresentationWindow`의 `onClose` 콜백이 오직 명시적 종료 경로에서만 호출되도록 보장

#### [MODIFY] [MainScreen.kt](file:///c:/Users/jerem/AndroidStudioProjects/LovelyBible/sample/composeApp/src/commonMain/kotlin/com/lovelybible/feature/main/MainScreen.kt)
- Spacebar `onPreviewKeyEvent` 핸들러 유지 (PPT 토글 기능)
- 텍스트 입력 필드에서 Spacebar가 잡히지 않도록 조건 추가 검증

---

### Version Management

#### [NEW] [AppVersion.kt](file:///c:/Users/jerem/AndroidStudioProjects/LovelyBible/sample/composeApp/src/commonMain/kotlin/com/lovelybible/core/AppVersion.kt)
- 앱 버전 상수 `APP_VERSION = "1.0.5"` 정의

#### [MODIFY] [build.gradle.kts](file:///c:/Users/jerem/AndroidStudioProjects/LovelyBible/sample/composeApp/build.gradle.kts)
- `packageVersion`을 `"1.0.5"`로 업데이트

---

## Risk Assessment

| 리스크 | 확률 | 영향 | 완화 전략 |
|--------|------|------|-----------|
| `onCloseRequest = {}` 설정 시 OS 창 관리자(Alt+F4)로도 닫히지 않음 | Medium | Low | Esc/Spacebar/버튼으로 닫을 수 있으므로 사용자 수용 가능 |
| Spacebar 동작 변경 시 기존 사용자 혼란 | Low | Low | PPT 내 방향키 네비게이션은 유지 |
| `onPreviewKeyEvent`가 텍스트 입력 필드의 Space 입력을 가로챔 | Medium | High | PPT 상태 확인 조건 추가로 방지 |
| 테스트 환경에서 Window 관련 테스트 불가 | High | Medium | ViewModel 레벨 단위 테스트로 로직 검증, UI는 수동 테스트 |

---

## Rollback Strategy

- **Phase 별 독립 롤백 가능**: 각 Phase의 변경 파일이 명확하게 구분됨
- **Git 브랜치**: `feature/v1.0.5-ppt-fix` 브랜치에서 작업 후 머지
- Phase 1-2 롤백: `PresentationWindow.kt`, `main.kt` 복원
- Phase 3 롤백: `MainScreen.kt` 복원
- Phase 4 롤백: `build.gradle.kts`, `AppVersion.kt` 삭제

---

## Phase 1: PPT 모드 보호 — 앱 클릭 시 PPT 꺼지지 않도록 수정
**Goal**: 앱 화면 클릭 시 PPT 모드가 해제되지 않도록 `PresentationWindow`의 닫힘 경로를 제한합니다.
**소요 시간**: 1~2시간

### Test Strategy
- **Test Type**: Unit Test (ViewModel 레벨)
- **Coverage Target**: PresentationViewModel의 닫힘 로직 100%
- **Dependencies to Mock**: `MonitorManager`, `BibleRepository`

### Tasks

#### 🔴 RED: 테스트 작성

- [x] **테스트 파일**: `PresentationViewModelTest.kt`
- [x] **새 테스트 케이스**:
  ```kotlin
  // 1. ClosePresentation intent만이 PPT를 닫을 수 있음
  @Test
  fun testOnlyExplicitCloseCanClosePPT()

  // 2. TogglePresentation으로 열고 ClosePresentation으로 닫기
  @Test
  fun testClosePresentation_afterToggle_stateIsCorrect()

  // 3. 연속 토글 시 상태 정확성
  @Test
  fun testMultipleToggleCycles_stateRemainsConsistent()
  ```

#### 🟢 GREEN: 구현

- [x] **`PresentationWindow.kt` 수정**:
  ```diff
  Window(
      onCloseRequest = onClose,
  +   onCloseRequest = { /* 빈 함수: OS 레벨 닫힘 차단 */ },
  ```
- [x] **`main.kt` 수정**: `onClose` 콜백이 Esc/Spacebar/버튼에서만 호출되도록 보장 (PresentationWindow 내부 키 이벤트에서만)

#### 🔵 REFACTOR

- [x] 불필요한 `onClose` 파라미터 관계 정리

### Quality Gate
- [x] 기존 테스트 모두 통과
- [x] 새 테스트 3개 통과
- [x] 빌드 에러 없음
- [ ] PPT 모드 ON 상태에서 메인 앱 클릭 → PPT 유지됨 (수동 확인)

---

## Phase 2: Spacebar 단축키 수정 & 키 이벤트 정리
**Goal**: Spacebar를 PPT ON/OFF 전용 토글 키로 통일합니다.
**소요 시간**: 1~2시간
**Dependencies**: Phase 1 완료

### Test Strategy
- **Test Type**: Unit Test (키 이벤트 핸들러 함수)
- **Coverage Target**: 키 이벤트 분기 100%
- **Dependencies to Mock**: 없음 (순수 함수)

### Tasks

#### 🔴 RED: 테스트 작성

- [x] **테스트 파일**: `PresentationViewModelTest.kt` (추가)
- [x] **새 테스트 케이스**:
  ```kotlin
  // 1. Spacebar: PPT OFF → ON
  @Test
  fun testSpacebar_whenPPTOff_turnsOn()

  // 2. Spacebar: PPT ON → OFF
  @Test
  fun testSpacebar_whenPPTOn_turnsOff()

  // 3. 연속 Spacebar 토글
  @Test
  fun testSpacebar_multipleToggles()
  ```

#### 🟢 GREEN: 구현

- [x] **`PresentationWindow.kt` — `handleKeyEvent` 수정**:
  ```diff
  - Key.DirectionRight, Key.DirectionDown, Key.Spacebar -> {
  -     onAction(PresentationIntent.NavigateNext)
  + Key.DirectionRight, Key.DirectionDown -> {
  +     onAction(PresentationIntent.NavigateNext)
  +     true
  + }
  + Key.Spacebar -> {
  +     onClose()  // PPT 종료
      true
  }
  ```
- [x] **`MainScreen.kt`** Spacebar 핸들러 유지 확인 (이미 `togglePresentation()` 호출 중)

#### 🔵 REFACTOR

- [x] `PresentationWindow.kt` 키 이벤트 핸들러에 주석 정리
- [x] `MainScreen.kt`의 중복 주석 정리 (line 64-65)

### Quality Gate
- [x] 기존 + Phase 1 테스트 모두 통과
- [x] 새 테스트 3개 통과
- [ ] Spacebar로 PPT ON/OFF 전환 확인 (수동)
- [ ] PPT 모드 내 방향키(→, ↓)로 네비게이션 정상 동작 (수동)

---

## Phase 3: PPT ON/OFF 상태 동기화 & 버튼 수정
**Goal**: PPT 종료(Esc/Spacebar/버튼) 시 버튼 상태가 즉시 동기화되도록 합니다.
**소요 시간**: 1시간
**Dependencies**: Phase 1, 2 완료

### Test Strategy
- **Test Type**: Unit Test (State 동기화)
- **Coverage Target**: 상태 전환 시나리오 100%

### Tasks

#### 🔴 RED: 테스트 작성

- [x] **테스트 파일**: `PresentationViewModelTest.kt` (추가)
- [x] **새 테스트 케이스**:
  ```kotlin
  // 1. ClosePresentation 후 상태 완전 초기화 확인
  @Test
  fun testClosePresentation_stateFullyReset()

  // 2. Toggle → Close → Toggle 사이클에서 상태 정확성
  @Test
  fun testToggleCloseToggle_stateSync()

  // 3. isPresentationWindowOpen, isActive, mode 모두 동기화
  @Test
  fun testAllStateFieldsSynced_afterClose()
  ```

#### 🟢 GREEN: 구현

- [x] **`PresentationViewModel.kt` — `closePresentation()` 검증**:
  - `isPresentationWindowOpen = false`, `isActive = false`, `mode = NONE` 모두 설정 확인
  - 이미 올바르게 구현되어 있으므로, Phase 1에서 `onCloseRequest` 차단이 핵심 수정
- [x] **`DisplayPanel.kt` 버튼 상태 확인**:
  - `presentationViewModel.state.isPresentationWindowOpen` 읽기 로직이 Compose 리컴포지션에서 정상 반영되는지 확인
  - 필요 시 `derivedStateOf` 또는 명시적 상태 읽기로 보강

#### 🔵 REFACTOR

- [x] `DisplayPanel.kt` PPT 버튼 코드 가독성 개선
- [x] 상태 확인 로직을 변수로 추출하여 명확화

### Quality Gate
- [x] 모든 테스트 통과 (기존 + Phase 1 + Phase 2 + Phase 3)
- [ ] PPT 버튼 클릭 1회로 PPT ON/OFF 정상 전환 (수동)
- [ ] Esc로 PPT 종료 → 버튼 "PPT 모드"로 변경 (수동)
- [ ] Spacebar로 PPT 종료 → 버튼 "PPT 모드"로 변경 (수동)
- [ ] 버튼 2번 클릭 문제 해소 확인 (수동)

---

## Phase 4: 버전관리 (v1.0.5)
**Goal**: 앱 버전을 `v1.0.5`로 설정하고 GitHub 릴리즈 호환을 확인합니다.
**소요 시간**: 30분
**Dependencies**: Phase 1-3 완료

### Test Strategy
- **Test Type**: Unit Test (버전 상수)
- **Coverage Target**: 100%

### Tasks

#### 🔴 RED: 테스트 작성

- [x] **새 테스트 파일**: `AppVersionTest.kt`
  ```kotlin
  @Test
  fun testAppVersion_isCorrect() {
      assertEquals("1.0.5", APP_VERSION)
  }
  ```

#### 🟢 GREEN: 구현

- [x] **[NEW] `AppVersion.kt`** 생성:
  ```kotlin
  // package com.lovelybible.core
  
  const val APP_VERSION = "1.0.5"
  ```
- [x] **`build.gradle.kts` 수정**:
  ```diff
  - packageVersion = "1.0.1"
  + packageVersion = "1.0.5"
  ```

#### 🔵 REFACTOR

- [x] GitHub Actions `release_windows.yml`이 `v*` 태그로 트리거되는지 확인 (이미 설정됨)

### Quality Gate
- [x] `AppVersionTest` 통과
- [x] 빌드 에러 없음
- [x] `build.gradle.kts` 버전 `1.0.5` 확인

---

## Verification Plan

### Automated Tests
```bash
# 프로젝트 루트에서 실행
cd c:\Users\jerem\AndroidStudioProjects\LovelyBible
gradlew.bat :sample:composeApp:jvmTest --info
```

### Manual Verification (사용자 실행)
1. **앱 실행** → PPT 모드 버튼 클릭 → PPT 창 열림 확인
2. **메인 앱 클릭** → PPT 창이 **닫히지 않음** 확인
3. **Spacebar 누름** → PPT **종료** 확인 → 버튼이 "PPT 모드"로 변경
4. **다시 Spacebar** → PPT **켜짐** 확인 → 버튼이 "PPT 종료"로 변경
5. **PPT ON 상태에서 Esc** → PPT 종료 → 버튼 동기화 확인
6. **PPT ON 상태에서 PPT 종료 버튼** → PPT 종료 → 버튼 동기화 확인
7. **PPT ON 상태에서 방향키(→)** → 다음 페이지 정상 이동 확인

---

## Notes & Learnings

### Phase 1-4 통합 구현 (2026-02-11)
- **핵심 수정**: `PresentationWindow.onCloseRequest = {}` → OS 레벨 닫힘 완전 차단
- **Spacebar 충돌 해결**: `PresentationWindow`에서 Spacebar를 `NavigateNext`에서 `onClose()`로 변경
- **MainScreen Esc 추가**: PPT ON 상태에서 Esc 키로도 종료 가능하도록 MainScreen에 핸들러 추가
- **버전 관리**: `AppVersion.kt` 상수 + `build.gradle.kts` 동기화 방식 채택
- **테스트 결과**: BUILD SUCCESSFUL, 모든 자동화 테스트 통과
- **수동 검증 대기**: PPT 모드 ON/OFF, 앱 클릭, Spacebar/Esc 종료 동기화 수동 확인 필요
