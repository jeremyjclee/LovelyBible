# 📦 MASTER PLAN: Phase 4 - Navigation System

**Version**: v2.0.0  
**Feature**: 성경 내비게이션 시스템  
**Created**: 2026-01-19  
**Status**: ⚪ Not Started  
**Total Estimated Time**: 2-2.5 hours

---

## 📋 Overview

### Objective
전체 성경을 연속적으로 탐색할 수 있는 글로벌 내비게이션 시스템을 구현한다.

### Key Deliverables
- [ ] NavigationViewModel (커서 상태 관리)
- [ ] 이전/다음 내비게이션 버튼
- [ ] 페이지당 절 개수 설정 (1-10절)
- [ ] 장/책 경계 자동 전환
- [ ] 구절 디스플레이 패널

---

## 🚀 Phase Breakdown

---

### Phase 4.1: Navigation State 정의 (25분)

**Goal**: 현재 위치 및 내비게이션 상태를 정의한다.

#### 🟢 GREEN Tasks
- [ ] `feature/navigation/NavigationState.kt`:
  ```kotlin
  data class NavigationState(
      val currentPosition: BiblePosition = BiblePosition("창세기", 1, 1),
      val displayedVerses: List<Verse> = emptyList(),
      val linesPerPage: Int = 1,
      val currentTitle: String = "",
      val canGoNext: Boolean = true,
      val canGoPrev: Boolean = false,
      val paginationInfo: PaginationInfo? = null
  )
  
  data class PaginationInfo(
      val currentPage: Int,
      val totalPages: Int
  )
  ```
- [ ] `feature/navigation/NavigationIntent.kt`:
  ```kotlin
  sealed class NavigationIntent {
      object GoNext : NavigationIntent()
      object GoPrev : NavigationIntent()
      data class SetLinesPerPage(val lines: Int) : NavigationIntent()
      data class GoToPosition(val position: BiblePosition) : NavigationIntent()
  }
  ```

---

### Phase 4.2: Navigation UseCase (35분)

**Goal**: 이전/다음 위치 계산 로직을 UseCase로 분리한다.

#### 🔴 RED Tasks
- [ ] `NavigationUseCaseTest.kt` 생성
- [ ] 테스트:
  - [ ] 장 끝에서 다음 장으로 이동
  - [ ] 책 끝에서 다음 책으로 이동
  - [ ] 창세기 1:1에서 이전 불가

#### 🟢 GREEN Tasks
- [ ] `domain/usecase/GetNextPositionUseCase.kt`:
  ```kotlin
  class GetNextPositionUseCase(
      private val repository: BibleRepository
  ) {
      suspend operator fun invoke(current: BiblePosition): BiblePosition? {
          val maxVerse = repository.getVerseCount(current.book, current.chapter)
          
          // 같은 장 내 다음 절
          if (current.verse < maxVerse) {
              return current.copy(verse = current.verse + 1)
          }
          
          // 다음 장
          val maxChapter = repository.getChapterCount(current.book)
          if (current.chapter < maxChapter) {
              return current.copy(chapter = current.chapter + 1, verse = 1)
          }
          
          // 다음 책
          val nextBook = getNextBook(current.book)
          return nextBook?.let { BiblePosition(it, 1, 1) }
      }
  }
  ```
- [ ] `domain/usecase/GetPrevPositionUseCase.kt`
- [ ] `domain/usecase/GetVersesFromPositionUseCase.kt`:
  ```kotlin
  class GetVersesFromPositionUseCase(
      private val repository: BibleRepository,
      private val getNextPosition: GetNextPositionUseCase
  ) {
      suspend operator fun invoke(start: BiblePosition, count: Int): List<Verse> {
          val verses = mutableListOf<Verse>()
          var pos: BiblePosition? = start
          
          while (verses.size < count && pos != null) {
              repository.getVerse(pos.book, pos.chapter, pos.verse)?.let {
                  verses.add(it)
              }
              pos = getNextPosition(pos)
          }
          return verses
      }
  }
  ```

---

### Phase 4.3: NavigationViewModel (35분)

**Goal**: 내비게이션 상태를 관리하는 ViewModel을 구현한다.

#### 🟢 GREEN Tasks
- [ ] `feature/navigation/NavigationViewModel.kt`:
  ```kotlin
  class NavigationViewModel(
      private val getVersesFromPosition: GetVersesFromPositionUseCase,
      private val getNextPosition: GetNextPositionUseCase,
      private val getPrevPosition: GetPrevPositionUseCase
  ) : ViewModel() {
      
      private val _state = MutableStateFlow(NavigationState())
      val state: StateFlow<NavigationState> = _state.asStateFlow()
      
      init {
          loadCurrentPage()
      }
      
      fun onIntent(intent: NavigationIntent) {
          when (intent) {
              NavigationIntent.GoNext -> goNext()
              NavigationIntent.GoPrev -> goPrev()
              is NavigationIntent.SetLinesPerPage -> setLinesPerPage(intent.lines)
              is NavigationIntent.GoToPosition -> goToPosition(intent.position)
          }
      }
      
      private fun goNext() {
          viewModelScope.launch {
              val currentVerses = _state.value.displayedVerses
              val lastVerse = currentVerses.lastOrNull() ?: return@launch
              val lastPos = BiblePosition(lastVerse.bookName, lastVerse.chapter, lastVerse.verse)
              
              getNextPosition(lastPos)?.let { nextPos ->
                  _state.update { it.copy(currentPosition = nextPos) }
                  loadCurrentPage()
              }
          }
      }
  }
  ```

---

### Phase 4.4: Display Panel UI (40분)

**Goal**: 구절을 표시하는 디스플레이 패널을 구현한다.

#### 🟢 GREEN Tasks
- [ ] `feature/navigation/DisplayPanel.kt`:
  ```kotlin
  @Composable
  fun DisplayPanel(
      state: NavigationState,
      onIntent: (NavigationIntent) -> Unit,
      modifier: Modifier = Modifier
  ) {
      GlassCard(modifier = modifier) {
          Column(modifier = Modifier.padding(20.dp)) {
              // 제목
              Text(
                  text = state.currentTitle,
                  style = MaterialTheme.typography.headlineMedium,
                  color = AppColors.TextPrimary
              )
              
              Spacer(modifier = Modifier.height(16.dp))
              
              // 구절 목록
              state.displayedVerses.forEach { verse ->
                  VerseRow(verse = verse)
                  Spacer(modifier = Modifier.height(8.dp))
              }
              
              Spacer(modifier = Modifier.weight(1f))
              
              // 내비게이션 버튼
              NavigationButtons(
                  canGoNext = state.canGoNext,
                  canGoPrev = state.canGoPrev,
                  onNext = { onIntent(NavigationIntent.GoNext) },
                  onPrev = { onIntent(NavigationIntent.GoPrev) }
              )
          }
      }
  }
  
  @Composable
  fun VerseRow(verse: Verse) {
      Row {
          Text(
              text = "${verse.verse}",
              style = MaterialTheme.typography.bodyLarge,
              color = AppColors.Accent,
              modifier = Modifier.width(32.dp)
          )
          Text(
              text = verse.text,
              style = MaterialTheme.typography.bodyLarge,
              color = AppColors.TextPrimary
          )
      }
  }
  ```

---

### Phase 4.5: 페이지 크기 설정 (25분)

**Goal**: 페이지당 표시할 절 개수를 설정하는 UI를 구현한다.

#### 🟢 GREEN Tasks
- [ ] `feature/navigation/components/LinesPerPageSelector.kt`:
  ```kotlin
  @Composable
  fun LinesPerPageSelector(
      selected: Int,
      onSelect: (Int) -> Unit
  ) {
      Row(
          horizontalArrangement = Arrangement.spacedBy(4.dp),
          verticalAlignment = Alignment.CenterVertically
      ) {
          Text("절 개수:", color = AppColors.TextSecondary)
          (1..10).forEach { num ->
              LineButton(
                  number = num,
                  selected = num == selected,
                  onClick = { onSelect(num) }
              )
          }
      }
  }
  ```
- [ ] 키보드 단축키 (1-0 숫자키, ← →)

---

## 📁 File Changes Summary

| 파일 경로 | 변경 유형 | 설명 |
|-----------|-----------|------|
| `feature/navigation/NavigationState.kt` | Created | 상태 정의 |
| `feature/navigation/NavigationViewModel.kt` | Created | ViewModel |
| `domain/usecase/GetNextPositionUseCase.kt` | Created | UseCase |
| `feature/navigation/DisplayPanel.kt` | Created | 디스플레이 UI |

---

**Next Step**: Phase 4.1 시작 - Navigation State 정의
