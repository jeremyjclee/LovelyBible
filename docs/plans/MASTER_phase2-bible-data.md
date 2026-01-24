# 📦 MASTER PLAN: Phase 2 - Bible Data System (Room/SQLite)

**Version**: v2.0.0  
**Feature**: 성경 데이터 시스템 (Room/SQLite + Repository)  
**Created**: 2026-01-19  
**Status**: ⚪ Not Started  
**Total Estimated Time**: 2-2.5 hours

---

## 📋 Overview

### Objective
JSON 성경 데이터를 SQLite DB로 변환하고 Repository 패턴으로 데이터 접근 계층을 구현한다.

### Key Deliverables
- [ ] Room Entity 정의 (Book, Verse)
- [ ] DAO 인터페이스 및 쿼리
- [ ] Repository 구현 (캐싱 전략 포함)
- [ ] JSON → DB 초기 로딩 로직
- [ ] 구약/신약 책 목록 관리

### Architecture Decisions

| 결정 사항 | 선택 | 이유 |
|-----------|------|------|
| **데이터베이스** | SQLite (SQLDelight/Room) | KMP 호환, 빠른 검색 |
| **직렬화** | kotlinx.serialization | Kotlin 네이티브, 타입 안전 |
| **캐싱** | In-Memory + Repository | 데스크탑의 풍부한 메모리 활용 |

---

## 🚀 Phase Breakdown

---

### Phase 2.1: Domain 모델 정의 (25분)

**Goal**: 성경 데이터의 도메인 모델을 정의한다.

#### 🟢 GREEN Tasks
- [ ] `domain/model/Book.kt`:
  ```kotlin
  data class Book(
      val id: Int,
      val name: String,
      val testament: Testament,
      val chapterCount: Int
  )
  
  enum class Testament { OLD, NEW }
  ```
- [ ] `domain/model/Verse.kt`:
  ```kotlin
  data class Verse(
      val bookName: String,
      val chapter: Int,
      val verse: Int,
      val text: String
  )
  ```
- [ ] `domain/model/BiblePosition.kt`:
  ```kotlin
  data class BiblePosition(
      val book: String,
      val chapter: Int,
      val verse: Int
  )
  ```

#### Quality Gate
- [ ] 모델 클래스 컴파일 성공

---

### Phase 2.2: JSON 데이터 파싱 (35분)

**Goal**: JSON 성경 파일을 파싱하여 도메인 모델로 변환한다.

#### 🔴 RED Tasks
- [ ] `BibleParserTest.kt` 생성
- [ ] 테스트:
  - [ ] JSON 파싱 성공 확인
  - [ ] 66권 책 목록 확인
  - [ ] 특정 구절 조회 확인

#### 🟢 GREEN Tasks
- [ ] `data/local/parser/BibleJsonParser.kt`:
  ```kotlin
  @Serializable
  data class BibleJson(
      val books: List<BookJson>
  )
  
  @Serializable
  data class BookJson(
      val name: String,
      val chapters: List<ChapterJson>
  )
  
  @Serializable
  data class ChapterJson(
      val chapter: Int,
      val verses: List<VerseJson>
  )
  
  class BibleJsonParser {
      fun parse(jsonString: String): List<Verse> {
          val bible = Json.decodeFromString<BibleJson>(jsonString)
          return bible.books.flatMap { book ->
              book.chapters.flatMap { chapter ->
                  chapter.verses.map { verse ->
                      Verse(book.name, chapter.chapter, verse.verse, verse.text)
                  }
              }
          }
      }
  }
  ```
- [ ] 리소스에서 JSON 로딩:
  ```kotlin
  fun loadBibleJson(): String {
      return Thread.currentThread()
          .contextClassLoader
          .getResourceAsStream("bible/bible.json")
          ?.bufferedReader()
          ?.readText()
          ?: error("Bible JSON not found")
  }
  ```

#### Quality Gate
- [ ] 테스트 통과
- [ ] 전체 구절 파싱 성공

---

### Phase 2.3: Repository 인터페이스 정의 (20분)

**Goal**: 데이터 접근을 위한 Repository 인터페이스를 정의한다.

#### 🟢 GREEN Tasks
- [ ] `domain/repository/BibleRepository.kt`:
  ```kotlin
  interface BibleRepository {
      suspend fun getAllBooks(): List<Book>
      suspend fun getOldTestamentBooks(): List<Book>
      suspend fun getNewTestamentBooks(): List<Book>
      suspend fun getVerses(book: String, chapter: Int): List<Verse>
      suspend fun getVerse(book: String, chapter: Int, verse: Int): Verse?
      suspend fun getChapterCount(book: String): Int
      suspend fun getVerseCount(book: String, chapter: Int): Int
      suspend fun searchBooks(query: String): List<Book>
  }
  ```

#### Quality Gate
- [ ] 인터페이스 컴파일 성공

---

### Phase 2.4: Repository 구현 + 캐싱 (40분)

**Goal**: Repository를 구현하고 In-Memory 캐싱을 적용한다.

#### 🔴 RED Tasks
- [ ] `BibleRepositoryTest.kt` 생성
- [ ] 테스트:
  - [ ] 책 목록 조회
  - [ ] 구절 조회
  - [ ] 검색 기능

#### 🟢 GREEN Tasks
- [ ] `data/repository/BibleRepositoryImpl.kt`:
  ```kotlin
  class BibleRepositoryImpl(
      private val parser: BibleJsonParser
  ) : BibleRepository {
      
      // In-Memory Cache
      private var cachedVerses: Map<String, Map<Int, List<Verse>>>? = null
      private var cachedBooks: List<Book>? = null
      
      private suspend fun ensureLoaded() {
          if (cachedVerses == null) {
              withContext(Dispatchers.IO) {
                  val json = loadBibleJson()
                  val verses = parser.parse(json)
                  cachedVerses = verses.groupBy { it.bookName }
                      .mapValues { entry ->
                          entry.value.groupBy { it.chapter }
                      }
                  cachedBooks = buildBookList(verses)
              }
          }
      }
      
      override suspend fun getVerses(book: String, chapter: Int): List<Verse> {
          ensureLoaded()
          return cachedVerses?.get(book)?.get(chapter) ?: emptyList()
      }
      
      // ... 기타 메서드 구현
  }
  ```

#### 🔵 REFACTOR Tasks
- [ ] 로딩 Progress 콜백 추가
- [ ] 에러 핸들링 개선

#### Quality Gate
- [ ] 모든 테스트 통과
- [ ] 캐싱 동작 확인

---

### Phase 2.5: 책 목록 UI 컴포넌트 (30분)

**Goal**: 구약/신약 토글 및 책 버튼 그리드를 Compose로 구현한다.

#### 🟢 GREEN Tasks
- [ ] `feature/main/components/TestamentToggle.kt`:
  ```kotlin
  @Composable
  fun TestamentToggle(
      selected: Testament,
      onSelect: (Testament) -> Unit
  ) {
      Row(modifier = Modifier.fillMaxWidth()) {
          TestamentButton(
              text = "구약",
              selected = selected == Testament.OLD,
              onClick = { onSelect(Testament.OLD) },
              modifier = Modifier.weight(1f)
          )
          TestamentButton(
              text = "신약",
              selected = selected == Testament.NEW,
              onClick = { onSelect(Testament.NEW) },
              modifier = Modifier.weight(1f)
          )
      }
  }
  ```
- [ ] `feature/main/components/BookGrid.kt`:
  ```kotlin
  @Composable
  fun BookGrid(
      books: List<Book>,
      selectedBook: Book?,
      onBookClick: (Book) -> Unit
  ) {
      LazyVerticalGrid(
          columns = GridCells.Fixed(5),
          modifier = Modifier.fillMaxSize()
      ) {
          items(books) { book ->
              BookButton(
                  book = book,
                  selected = book == selectedBook,
                  onClick = { onBookClick(book) }
              )
          }
      }
  }
  ```

#### Quality Gate
- [ ] 구약 39권, 신약 27권 표시
- [ ] 토글 동작 확인
- [ ] 책 선택 시 하이라이트

---

## 📁 File Changes Summary

| 파일 경로 | 변경 유형 | 설명 |
|-----------|-----------|------|
| `domain/model/Book.kt` | Created | 책 모델 |
| `domain/model/Verse.kt` | Created | 구절 모델 |
| `domain/repository/BibleRepository.kt` | Created | Repository 인터페이스 |
| `data/local/parser/BibleJsonParser.kt` | Created | JSON 파서 |
| `data/repository/BibleRepositoryImpl.kt` | Created | Repository 구현 |
| `feature/main/components/BookGrid.kt` | Created | 책 그리드 UI |

---

## ✅ Validation Commands

```bash
./gradlew test
./gradlew run
```

---

**Next Step**: Phase 2.1 시작 - Domain 모델 정의
