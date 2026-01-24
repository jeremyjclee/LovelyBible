# 📦 MASTER PLAN: Phase 5 - Dual Monitor Presentation (핵심 기능)

**Version**: v2.0.0  
**Feature**: JVM 기반 듀얼 모니터 프레젠테이션  
**Created**: 2026-01-19  
**Status**: ⚪ Not Started  
**Total Estimated Time**: 3-4 hours (핵심 기능)

---

> [!IMPORTANT]
> 이 Phase가 LovelyBible의 **핵심 기능**입니다. JVM `GraphicsEnvironment`를 사용하여 두 번째 모니터를 감지하고 전체화면 프레젠테이션 창을 제어합니다.

---

## 📋 Overview

### Objective
JVM 환경에서 두 번째 모니터를 자동 감지하고, 전체화면 프레젠테이션 창을 표시하며, 메인 창과 실시간 동기화한다.

### Key Deliverables
- [ ] `GraphicsEnvironment` 기반 모니터 감지
- [ ] Compose `Window` 컴포저블로 두 번째 창 생성
- [ ] 전체화면 프레젠테이션 UI
- [ ] Shared ViewModel로 실시간 동기화
- [ ] 폴백 모드 (단일 모니터)

### Architecture: JVM 듀얼 윈도우

```
┌─────────────────────────────────────────────────────────────────┐
│                    Main Process (JVM)                           │
│                                                                 │
│  ┌─────────────────┐          ┌─────────────────────────────┐  │
│  │  Control Window │   ────►  │  Display Window (2nd Mon)   │  │
│  │  (Main Monitor) │ StateFlow│  (Full Screen)              │  │
│  │                 │◄──────── │                             │  │
│  └─────────────────┘          └─────────────────────────────┘  │
│           │                              │                      │
│           └──────────┬───────────────────┘                      │
│                      ▼                                          │
│           ┌─────────────────┐                                   │
│           │ Shared ViewModel │                                  │
│           │   (StateFlow)    │                                  │
│           └─────────────────┘                                   │
└─────────────────────────────────────────────────────────────────┘
```

---

## 🚀 Phase Breakdown

---

### Phase 5.1: 모니터 감지 서비스 (40분)

**Goal**: `GraphicsEnvironment`를 사용하여 연결된 모든 모니터를 감지한다.

#### 🔴 RED Tasks
- [ ] `MonitorManagerTest.kt` 생성
- [ ] 테스트:
  - [ ] 연결된 디스플레이 수 반환
  - [ ] 기본 디스플레이와 외부 디스플레이 구분

#### 🟢 GREEN Tasks
- [ ] `core/display/MonitorManager.kt`:
  ```kotlin
  class MonitorManager {
      private val ge = GraphicsEnvironment.getLocalGraphicsEnvironment()
      
      fun getAllDisplays(): List<DisplayInfo> {
          return ge.screenDevices.mapIndexed { index, device ->
              val bounds = device.defaultConfiguration.bounds
              DisplayInfo(
                  id = index,
                  name = device.iDstring,
                  bounds = bounds,
                  isPrimary = device == ge.defaultScreenDevice
              )
          }
      }
      
      fun getExternalDisplay(): DisplayInfo? {
          return getAllDisplays().find { !it.isPrimary }
      }
      
      fun hasExternalMonitor(): Boolean {
          return ge.screenDevices.size > 1
      }
  }
  
  data class DisplayInfo(
      val id: Int,
      val name: String,
      val bounds: Rectangle,
      val isPrimary: Boolean
  )
  ```
- [ ] 모니터 변경 감지 (polling 또는 이벤트)

#### Quality Gate
- [ ] 테스트 통과
- [ ] 연결된 모니터 정보 로깅 확인

---

### Phase 5.2: Presentation State 정의 (25분)

**Goal**: 프레젠테이션 모드의 상태 및 이벤트를 정의한다.

#### 🟢 GREEN Tasks
- [ ] `feature/presentation/PresentationState.kt`:
  ```kotlin
  data class PresentationState(
      val isActive: Boolean = false,
      val isPresentationWindowOpen: Boolean = false,
      val hasExternalMonitor: Boolean = false,
      val displayInfo: DisplayInfo? = null,
      val currentVerses: List<Verse> = emptyList(),
      val currentTitle: String = "",
      val mode: PresentationMode = PresentationMode.NONE
  )
  
  enum class PresentationMode {
      NONE,           // 프레젠테이션 비활성
      EXTERNAL,       // 외부 모니터 전체화면
      LOCAL_OVERLAY   // 로컬 전체화면 오버레이
  }
  ```
- [ ] `feature/presentation/PresentationIntent.kt`:
  ```kotlin
  sealed class PresentationIntent {
      object TogglePresentation : PresentationIntent()
      object ClosePresentation : PresentationIntent()
      object RefreshMonitors : PresentationIntent()
  }
  ```

---

### Phase 5.3: PresentationViewModel (SharedFlow) (35분)

**Goal**: 메인 창과 Display 창이 공유하는 ViewModel을 구현한다.

#### 🟢 GREEN Tasks
- [ ] `feature/presentation/PresentationViewModel.kt`:
  ```kotlin
  class PresentationViewModel(
      private val monitorManager: MonitorManager,
      private val navigationViewModel: NavigationViewModel
  ) : ViewModel() {
      
      private val _state = MutableStateFlow(PresentationState())
      val state: StateFlow<PresentationState> = _state.asStateFlow()
      
      // NavigationViewModel의 상태를 구독하여 동기화
      init {
          viewModelScope.launch {
              navigationViewModel.state.collect { navState ->
                  _state.update { it.copy(
                      currentVerses = navState.displayedVerses,
                      currentTitle = navState.currentTitle
                  )}
              }
          }
          refreshMonitors()
      }
      
      fun onIntent(intent: PresentationIntent) {
          when (intent) {
              PresentationIntent.TogglePresentation -> togglePresentation()
              PresentationIntent.ClosePresentation -> closePresentation()
              PresentationIntent.RefreshMonitors -> refreshMonitors()
          }
      }
      
      private fun togglePresentation() {
          val hasExternal = monitorManager.hasExternalMonitor()
          val externalDisplay = monitorManager.getExternalDisplay()
          
          _state.update { current ->
              if (current.isPresentationWindowOpen) {
                  current.copy(
                      isPresentationWindowOpen = false,
                      mode = PresentationMode.NONE
                  )
              } else {
                  current.copy(
                      isPresentationWindowOpen = true,
                      hasExternalMonitor = hasExternal,
                      displayInfo = externalDisplay,
                      mode = if (hasExternal) PresentationMode.EXTERNAL 
                             else PresentationMode.LOCAL_OVERLAY
                  )
              }
          }
      }
  }
  ```

---

### Phase 5.4: Presentation Window (핵심) (50분)

**Goal**: Compose `Window`를 사용하여 두 번째 모니터에 전체화면 창을 생성한다.

#### 🔴 RED Tasks
- [ ] 수동 테스트: 두 번째 모니터에 창 표시 확인

#### 🟢 GREEN Tasks
- [ ] `feature/presentation/PresentationWindow.kt`:
  ```kotlin
  @Composable
  fun PresentationWindow(
      state: PresentationState,
      onClose: () -> Unit
  ) {
      if (!state.isPresentationWindowOpen) return
      
      val windowState = rememberWindowState(
          placement = WindowPlacement.Fullscreen,
          position = state.displayInfo?.let { display ->
              WindowPosition(display.bounds.x.dp, display.bounds.y.dp)
          } ?: WindowPosition.PlatformDefault,
          size = state.displayInfo?.let { display ->
              DpSize(display.bounds.width.dp, display.bounds.height.dp)
          } ?: DpSize(1920.dp, 1080.dp)
      )
      
      Window(
          onCloseRequest = onClose,
          state = windowState,
          title = "Lovely Bible - Presentation",
          undecorated = true,  // 프레임 없음
          alwaysOnTop = true
      ) {
          PresentationContent(
              verses = state.currentVerses,
              title = state.currentTitle,
              onEscapePressed = onClose
          )
      }
  }
  ```
- [ ] 창 위치 계산 로직:
  ```kotlin
  private fun calculateWindowPosition(display: DisplayInfo): WindowPosition {
      return WindowPosition.Absolute(
          x = display.bounds.x.dp,
          y = display.bounds.y.dp
      )
  }
  ```
- [ ] ESC 키로 창 닫기

---

### Phase 5.5: Presentation UI (40분)

**Goal**: 전체화면 프레젠테이션에 표시될 UI를 구현한다.

#### 🟢 GREEN Tasks
- [ ] `feature/presentation/PresentationContent.kt`:
  ```kotlin
  @Composable
  fun PresentationContent(
      verses: List<Verse>,
      title: String,
      onEscapePressed: () -> Unit
  ) {
      // ESC 키 처리
      LaunchedEffect(Unit) {
          // 키 이벤트 리스너
      }
      
      Box(
          modifier = Modifier
              .fillMaxSize()
              .background(
                  Brush.radialGradient(
                      colors = listOf(
                          Color(0xFF1a1a2e),
                          Color(0xFF0a0a14)
                      )
                  )
              ),
          contentAlignment = Alignment.Center
      ) {
          Column(
              horizontalAlignment = Alignment.CenterHorizontally,
              modifier = Modifier.padding(80.dp)
          ) {
              // 제목
              Text(
                  text = title,
                  style = MaterialTheme.typography.displayMedium,
                  color = AppColors.Accent,
                  fontWeight = FontWeight.Bold
              )
              
              Spacer(modifier = Modifier.height(48.dp))
              
              // 구절
              verses.forEach { verse ->
                  PresentationVerseRow(verse)
                  Spacer(modifier = Modifier.height(24.dp))
              }
          }
      }
  }
  
  @Composable
  fun PresentationVerseRow(verse: Verse) {
      Row(
          horizontalArrangement = Arrangement.Center,
          modifier = Modifier.fillMaxWidth()
      ) {
          Text(
              text = "${verse.verse}",
              style = MaterialTheme.typography.headlineLarge,
              color = AppColors.Accent,
              modifier = Modifier.width(80.dp)
          )
          Text(
              text = verse.text,
              style = MaterialTheme.typography.headlineLarge,
              color = Color.White,
              textAlign = TextAlign.Start
          )
      }
  }
  ```
- [ ] 폰트 크기 자동 조절 (구절 수에 따라)
- [ ] 부드러운 전환 애니메이션

---

### Phase 5.6: 폴백 모드 (로컬 오버레이) (30분)

**Goal**: 외부 모니터가 없을 때 로컬 전체화면 오버레이를 제공한다.

#### 🟢 GREEN Tasks
- [ ] 단일 모니터에서 전체화면 오버레이:
  ```kotlin
  @Composable
  fun LocalFullscreenOverlay(
      state: PresentationState,
      onClose: () -> Unit
  ) {
      if (state.mode != PresentationMode.LOCAL_OVERLAY) return
      
      Dialog(
          onDismissRequest = onClose,
          properties = DialogProperties(usePlatformDefaultWidth = false)
      ) {
          Box(modifier = Modifier.fillMaxSize()) {
              PresentationContent(
                  verses = state.currentVerses,
                  title = state.currentTitle,
                  onEscapePressed = onClose
              )
          }
      }
  }
  ```
- [ ] 키보드 내비게이션 (← → ESC)

---

### Phase 5.7: Main 통합 (30분)

**Goal**: Main.kt에서 두 Window를 조합한다.

#### 🟢 GREEN Tasks
- [ ] `Main.kt` 업데이트:
  ```kotlin
  fun main() = application {
      startKoin { modules(appModule) }
      
      val presentationViewModel: PresentationViewModel = koinInject()
      val presentationState by presentationViewModel.state.collectAsState()
      
      // 메인 윈도우
      Window(
          onCloseRequest = ::exitApplication,
          title = "Lovely Bible"
      ) {
          LovelyBibleTheme {
              App(presentationViewModel = presentationViewModel)
          }
      }
      
      // 프레젠테이션 윈도우 (조건부)
      PresentationWindow(
          state = presentationState,
          onClose = { presentationViewModel.onIntent(PresentationIntent.ClosePresentation) }
      )
  }
  ```
- [ ] PPT 모드 토글 버튼:
  ```kotlin
  @Composable
  fun PptModeButton(
      isActive: Boolean,
      hasExternalMonitor: Boolean,
      onClick: () -> Unit
  ) {
      Button(
          onClick = onClick,
          colors = ButtonDefaults.buttonColors(
              containerColor = if (isActive) AppColors.Accent else AppColors.CardBackground
          )
      ) {
          Icon(
              imageVector = Icons.Default.PresentToAll,
              contentDescription = null
          )
          Text(if (hasExternalMonitor) "PPT 모드" else "전체 화면")
      }
  }
  ```

---

## 📁 File Changes Summary

| 파일 경로 | 변경 유형 | 설명 |
|-----------|-----------|------|
| `core/display/MonitorManager.kt` | Created | 모니터 감지 |
| `feature/presentation/PresentationState.kt` | Created | 상태 정의 |
| `feature/presentation/PresentationViewModel.kt` | Created | 공유 ViewModel |
| `feature/presentation/PresentationWindow.kt` | Created | 두 번째 창 |
| `feature/presentation/PresentationContent.kt` | Created | 프레젠테이션 UI |
| `Main.kt` | Modified | 멀티 윈도우 조합 |

---

## 🎯 Architect's Tip

### OS별 창 관리 주의사항

| OS | 주의사항 |
|----|----------|
| **Windows** | `setAlwaysOnTop(true)` 필요, 전체화면 시 태스크바 숨김 확인 |
| **macOS** | 별도 Space로 이동 방지 설정 필요 |
| **Linux** | 창 관리자에 따라 동작 다름, 테스트 필수 |

### 메모리 누수 방지

- `Window` Composable이 닫힐 때 리소스 정리
- `DisposableEffect` 사용하여 cleanup 로직 추가

---

**Next Step**: Phase 5.1 시작 - 모니터 감지 서비스
