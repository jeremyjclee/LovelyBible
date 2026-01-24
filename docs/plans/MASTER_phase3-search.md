# 📦 MASTER PLAN: Phase 3 - Search Feature (MVI)

**Version**: v2.0.0  
**Feature**: 검색 기능 (MVI 패턴)  
**Created**: 2026-01-19  
**Status**: ⚪ Not Started  
**Total Estimated Time**: 2-2.5 hours

---

## 📋 Overview

### Objective
MVI 패턴 기반 검색 기능을 구현한다. 책/장/절 입력, 자동완성, 최근 검색 기록을 포함한다.

### Key Deliverables
- [ ] SearchViewModel (MVI: Intent → State)
- [ ] 3-박스 검색 UI (책/장/절)
- [ ] 책 이름 자동완성
- [ ] Enter/Tab 키 체인 검색
- [ ] DataStore 기반 최근 검색

### Architecture: MVI Pattern

```
┌─────────────┐     Intent      ┌─────────────┐
│   Compose   │ ───────────────►│  ViewModel  │
│     UI      │                 │             │
│             │◄─────────────── │   State     │
└─────────────┘     State       └─────────────┘
                                       │
                                       ▼
                                ┌─────────────┐
                                │  UseCase    │
                                └─────────────┘
```

---

## 🚀 Phase Breakdown

---

### Phase 3.1: MVI 구조 정의 (30분)

**Goal**: 검색 기능의 Intent, State, Effect를 정의한다.

#### 🟢 GREEN Tasks
- [ ] `feature/search/SearchIntent.kt`:
  ```kotlin
  sealed class SearchIntent {
      data class UpdateBookQuery(val query: String) : SearchIntent()
      data class UpdateChapter(val chapter: Int?) : SearchIntent()
      data class UpdateVerse(val verse: Int?) : SearchIntent()
      data class SelectBook(val book: Book) : SearchIntent()
      object ExecuteSearch : SearchIntent()
      object ClearSearch : SearchIntent()
      data class SelectRecentSearch(val position: BiblePosition) : SearchIntent()
  }
  ```
- [ ] `feature/search/SearchState.kt`:
  ```kotlin
  data class SearchState(
      val bookQuery: String = "",
      val selectedBook: Book? = null,
      val chapter: Int? = null,
      val verse: Int? = null,
      val suggestions: List<Book> = emptyList(),
      val recentSearches: List<BiblePosition> = emptyList(),
      val isSearching: Boolean = false,
      val error: String? = null
  )
  ```
- [ ] `feature/search/SearchEffect.kt`:
  ```kotlin
  sealed class SearchEffect {
      data class NavigateToVerse(val position: BiblePosition) : SearchEffect()
      data class FocusField(val field: SearchField) : SearchEffect()
      data class ShowError(val message: String) : SearchEffect()
  }
  
  enum class SearchField { BOOK, CHAPTER, VERSE }
  ```

---

### Phase 3.2: SearchViewModel 구현 (40분)

**Goal**: Intent를 처리하고 State를 업데이트하는 ViewModel을 구현한다.

#### 🔴 RED Tasks
- [ ] `SearchViewModelTest.kt` 생성
- [ ] 테스트:
  - [ ] 책 쿼리 입력 시 suggestions 업데이트
  - [ ] 검색 실행 시 NavigateToVerse effect 발생

#### 🟢 GREEN Tasks
- [ ] `feature/search/SearchViewModel.kt`:
  ```kotlin
  class SearchViewModel(
      private val searchBooksUseCase: SearchBooksUseCase,
      private val recentSearchRepository: RecentSearchRepository
  ) : ViewModel() {
      
      private val _state = MutableStateFlow(SearchState())
      val state: StateFlow<SearchState> = _state.asStateFlow()
      
      private val _effect = Channel<SearchEffect>()
      val effect = _effect.receiveAsFlow()
      
      fun onIntent(intent: SearchIntent) {
          when (intent) {
              is SearchIntent.UpdateBookQuery -> updateBookQuery(intent.query)
              is SearchIntent.SelectBook -> selectBook(intent.book)
              is SearchIntent.ExecuteSearch -> executeSearch()
              // ...
          }
      }
      
      private fun updateBookQuery(query: String) {
          viewModelScope.launch {
              val suggestions = searchBooksUseCase(query)
              _state.update { it.copy(
                  bookQuery = query,
                  suggestions = suggestions
              )}
          }
      }
  }
  ```

---

### Phase 3.3: 검색 UI 컴포넌트 (45분)

**Goal**: Compose 기반 검색 UI를 구현한다.

#### 🟢 GREEN Tasks
- [ ] `feature/search/SearchPanel.kt`:
  ```kotlin
  @Composable
  fun SearchPanel(
      state: SearchState,
      onIntent: (SearchIntent) -> Unit
  ) {
      GlassCard {
          Column(modifier = Modifier.padding(16.dp)) {
              Text("검색", style = MaterialTheme.typography.titleMedium)
              
              Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                  // 책 입력 + 자동완성
                  BookSearchField(
                      value = state.bookQuery,
                      suggestions = state.suggestions,
                      onValueChange = { onIntent(SearchIntent.UpdateBookQuery(it)) },
                      onBookSelect = { onIntent(SearchIntent.SelectBook(it)) },
                      modifier = Modifier.weight(2f)
                  )
                  
                  // 장 입력
                  NumberField(
                      value = state.chapter,
                      placeholder = "장",
                      onValueChange = { onIntent(SearchIntent.UpdateChapter(it)) },
                      modifier = Modifier.weight(1f)
                  )
                  
                  // 절 입력
                  NumberField(
                      value = state.verse,
                      placeholder = "절", 
                      onValueChange = { onIntent(SearchIntent.UpdateVerse(it)) },
                      onEnter = { onIntent(SearchIntent.ExecuteSearch) },
                      modifier = Modifier.weight(1f)
                  )
              }
          }
      }
  }
  ```
- [ ] `BookSearchField.kt` 자동완성 드롭다운
- [ ] `NumberField.kt` 숫자 입력 필드

---

### Phase 3.4: 키보드 체인 내비게이션 (25분)

**Goal**: Enter/Tab 키로 필드 간 자동 이동을 구현한다.

#### 🟢 GREEN Tasks
- [ ] FocusRequester 활용:
  ```kotlin
  val bookFocus = remember { FocusRequester() }
  val chapterFocus = remember { FocusRequester() }
  val verseFocus = remember { FocusRequester() }
  
  // 책 필드에서 Enter/Tab → 장 필드로 포커스
  BookSearchField(
      onEnter = {
          onIntent(SearchIntent.SelectBook(state.suggestions.firstOrNull() ?: return@))
          chapterFocus.requestFocus()
      },
      modifier = Modifier.focusRequester(bookFocus)
  )
  ```

---

### Phase 3.5: 최근 검색 기록 (30분)

**Goal**: DataStore를 사용하여 최근 검색을 저장/표시한다.

#### 🟢 GREEN Tasks
- [ ] `data/datastore/RecentSearchDataStore.kt`:
  ```kotlin
  class RecentSearchDataStore(
      private val dataStore: DataStore<Preferences>
  ) {
      suspend fun addRecentSearch(position: BiblePosition) {
          dataStore.edit { prefs ->
              val current = getRecentSearches(prefs)
              val updated = (listOf(position) + current)
                  .distinctBy { "${it.book}:${it.chapter}:${it.verse}" }
                  .take(15)
              prefs[RECENT_SEARCHES_KEY] = Json.encodeToString(updated)
          }
      }
      
      fun observeRecentSearches(): Flow<List<BiblePosition>>
  }
  ```
- [ ] 최근 검색 UI 컴포넌트:
  ```kotlin
  @Composable
  fun RecentSearchList(
      searches: List<BiblePosition>,
      onSelect: (BiblePosition) -> Unit
  ) {
      Column {
          Text("최근 검색", style = MaterialTheme.typography.labelMedium)
          FlowRow {
              searches.forEach { pos ->
                  RecentSearchChip(
                      text = "${pos.book} ${pos.chapter}장 ${pos.verse}절",
                      onClick = { onSelect(pos) }
                  )
              }
          }
      }
  }
  ```

---

## 📁 File Changes Summary

| 파일 경로 | 변경 유형 | 설명 |
|-----------|-----------|------|
| `feature/search/SearchIntent.kt` | Created | MVI Intent 정의 |
| `feature/search/SearchState.kt` | Created | MVI State 정의 |
| `feature/search/SearchViewModel.kt` | Created | ViewModel 구현 |
| `feature/search/SearchPanel.kt` | Created | 검색 UI |
| `data/datastore/RecentSearchDataStore.kt` | Created | 최근 검색 저장 |

---

**Next Step**: Phase 3.1 시작 - MVI 구조 정의
